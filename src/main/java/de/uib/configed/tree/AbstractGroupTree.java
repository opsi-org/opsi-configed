/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditRecord;

public abstract class AbstractGroupTree extends JTree implements TreeSelectionListener {
	OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory.getPersistenceController();

	public final GroupNode rootNode = new GroupNode("");

	protected GroupNode groupNodeGroups;

	protected Map<String, Map<String, String>> groups = new HashMap<>();
	// map of all group maps,
	// groupid --> group map

	protected Map<String, GroupNode> groupNodes = new HashMap<>();;
	// groupid --> group node
	// is a function since a group name cannot occur twice

	protected DefaultTreeModel model;

	protected ConfigedMain configedMain;

	protected AbstractGroupTree(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		init();

		model = new DefaultTreeModel(rootNode);
		super.setModel(model);
	}

	@Override
	public DefaultTreeModel getModel() {
		return model;
	}

	private void init() {
		super.addTreeSelectionListener(this);

		createTopNodes();

		setRootVisible(false);
		setShowsRootHandles(true);

		// popups on nodes
		JPopupMenu popupMenu = new JPopupMenu();
		TreePopupMouseListener treePopupMouseListener = new TreePopupMouseListener(popupMenu, this, configedMain);
		addMouseListener(treePopupMouseListener);

	}

	abstract void createTopNodes();

	public void editGroupNode(TreePath path) {
		DefaultMutableTreeNode node = null;

		if (path == null) {
			return;
		} else {
			node = (DefaultMutableTreeNode) path.getLastPathComponent();
		}

		if (!node.getAllowsChildren()) {
			return;
		}

		String groupId = node.toString();

		Map<String, String> groupData = new LinkedHashMap<>();
		groupData.put("groupname", groupId);
		groupData.put("description", groups.get(groupId).get("description"));
		Map<String, String> labels = new HashMap<>();
		labels.put("groupname", Configed.getResourceValue("ClientTree.editNode.label.groupname"));
		labels.put("description", Configed.getResourceValue("ClientTree.editNode.label.description"));
		Map<String, Boolean> editable = new HashMap<>();
		editable.put("groupname", false);
		editable.put("description", true);

		FEditRecord fEdit = new FEditRecord(Configed.getResourceValue("ClientTree.editGroup"));
		fEdit.setRecord(groupData, labels, null, editable, null);
		fEdit.setTitle(Configed.getResourceValue("ClientTree.editNode"));
		fEdit.init();
		fEdit.setSize(450, 250);
		fEdit.setLocationRelativeTo(ConfigedMain.getMainFrame());

		fEdit.setModal(true);

		fEdit.setVisible(true);

		groupData = fEdit.getData();

		if (!fEdit.isCancelled()) {
			groups.get(groupId).put("description", groupData.get("description"));
			persistenceController.getGroupDataService().updateGroup(groupId, groups.get(groupId));
		}
	}

	protected boolean deleteNode(TreePath path) {
		if (path == null) {
			return false;
		}

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

		String nodeID = (String) node.getUserObject();

		GroupNode parent = (GroupNode) node.getParent();

		if (groupNodes.get(nodeID) != null && groupNodes.get(nodeID).getParent() != parent) {
			Logging.warning(this, "groupNodes.get(nodeID).getParent() != parent");
			parent = (GroupNode) groupNodes.get(nodeID).getParent();
		}

		String parentID = (String) parent.getUserObject();

		if (groupNodes.get(nodeID) != null) {
			// found a group
			int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ClientTree.deleteGroupWarning"),
					Configed.getResourceValue("ClientTree.deleteGroupWarningTitle"), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.OK_OPTION) {
				groupNodes.remove(nodeID);
				groups.remove(nodeID);

				deleteGroupWithSubgroups(node);
				parent.remove(node);

				getModel().nodeStructureChanged(parent);
			}
		} else {
			// client node
			removeNodeInternally(nodeID, parent);
			persistenceController.getGroupDataService().removeObject2Group(nodeID, parentID);
		}
		return true;
	}

	// calls main controller for getting persistence for the new subgroup
	public DefaultMutableTreeNode makeSubgroupAt(TreePath path) {
		DefaultMutableTreeNode result = null;

		DefaultMutableTreeNode node;

		if (path == null) {
			node = groupNodeGroups;
		} else {
			node = (DefaultMutableTreeNode) path.getLastPathComponent();
		}

		if (node.getAllowsChildren()) {
			Map<String, String> groupData = new LinkedHashMap<>();
			groupData.put("groupname", "");
			groupData.put("description", "");
			Map<String, String> labels = new HashMap<>();
			labels.put("groupname", Configed.getResourceValue("ClientTree.editNode.label.groupname"));
			labels.put("description", Configed.getResourceValue("ClientTree.editNode.label.description"));
			Map<String, Boolean> editable = new HashMap<>();
			editable.put("groupname", true);
			editable.put("description", true);

			String newGroupKey = "";

			String inscription = "";

			FEditRecord fEdit = new FEditRecord(inscription);
			fEdit.setRecord(groupData, labels, null, editable, null);
			fEdit.setTitle(Configed.getResourceValue("ClientTree.addNode.title"));
			fEdit.init();
			fEdit.setSize(450, 250);
			fEdit.setLocationRelativeTo(ConfigedMain.getMainFrame());

			fEdit.setModal(true);

			while ("".equals(newGroupKey) || groups.keySet().contains(newGroupKey)) {
				if ("".equals(newGroupKey)) {
					inscription = Configed.getResourceValue("ClientTree.requestGroup");
				} else {
					inscription = "'" + newGroupKey + "' "
							+ Configed.getResourceValue("ClientTree.requestNotExistingGroupName");
				}

				fEdit.setHint(inscription);

				fEdit.setVisible(true);

				newGroupKey = fEdit.getData().get("groupname").toLowerCase(Locale.ROOT);

				if (fEdit.isCancelled()) {
					return null;
				}
			}
			// Now variable gotName equals true

			StringValuedRelationElement newGroup = new StringValuedRelationElement();

			newGroup.put("groupId", newGroupKey);
			newGroup.put("parentGroupId", node.toString());
			newGroup.put("description", groupData.get("description"));

			// get persistence
			if (persistenceController.getGroupDataService().addGroup(newGroup)) {
				groups.put(newGroupKey, newGroup);
				Logging.debug(this, "makeSubGroupAt newGroupKey, newGroup " + newGroupKey + ", " + newGroup);
				GroupNode newNode = insertGroup(newGroupKey, groupData.get("description"), node);
				groupNodes.put(newGroupKey, newNode);

				result = newNode;
			}
		}

		return result;
	}

	protected GroupNode insertGroup(String groupObject, String groupDescription, DefaultMutableTreeNode parent) {
		GroupNode node = produceGroupNode(groupObject, groupDescription);

		if (parent == null) {
			parent = groupNodeGroups;
		}

		insertNodeInOrder(node, parent);

		return node;
	}

	protected void insertNodeInOrder(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {
		if (node == null || parent == null) {
			return;
		}

		// for groups, we should look only for groups

		DefaultMutableTreeNode insertNode = findLocation(parent.children(), node);

		if (insertNode == null) {
			// append
			parent.add(node);
		} else {
			int i = parent.getIndex(insertNode);
			parent.insert(node, i);
		}

		model.nodesWereInserted(parent, new int[] { model.getIndexOfChild(parent, node) });
	}

	private static DefaultMutableTreeNode findLocation(Enumeration<TreeNode> children, DefaultMutableTreeNode node) {
		DefaultMutableTreeNode insertNode = null;

		String nodeObject = node.toString();

		while (children.hasMoreElements()) {
			insertNode = (DefaultMutableTreeNode) children.nextElement();

			// node with subnodes = group
			if (insertNode.getAllowsChildren() && !node.getAllowsChildren()) {
				continue;
			}

			// leaf && group
			if (!insertNode.getAllowsChildren() && node.getAllowsChildren()) {
				return insertNode;
			}

			// both are leafs or both are groups
			if (insertNode.toString().compareToIgnoreCase(nodeObject) > 0) {
				return insertNode;
			}
		}

		return null;
	}

	public GroupNode getGroupNode(String groupId) {
		return groupNodes.get(groupId);
	}

	public boolean removeClientNodes(Iterable<DefaultMutableTreeNode> clientNodes) {
		List<Object2GroupEntry> groupEntries = new ArrayList<>();

		for (DefaultMutableTreeNode clientNode : clientNodes) {
			String clientId = (String) (clientNode.getUserObject());
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) clientNode.getParent();

			removeNodeInternally(clientId, (GroupNode) parent);
			groupEntries.add(new Object2GroupEntry(clientId, parent.toString()));
		}

		return persistenceController.getGroupDataService().removeHostGroupElements(groupEntries);
	}

	protected TreePath pathByAddingChild(TreePath treePath, Object child) {
		if (child == null) {
			Logging.debug(this, "pathByAddingChild: child null cannot be added");
			return null;
		}

		return treePath.pathByAddingChild(child);
	}

	protected GroupNode produceGroupNode(String groupId, String description) {
		GroupNode groupNode = new GroupNode(groupId);

		Map<String, String> groupMap = new HashMap<>();
		groupMap.put("groupId", groupId);
		groupMap.put("description", description);

		groups.put(groupId, groupMap);
		groupNodes.put(groupId, groupNode);

		return groupNode;
	}

	abstract void removeNodeInternally(String nodeID, GroupNode parent);

	private void deleteGroupWithSubgroups(DefaultMutableTreeNode node) {
		Enumeration<TreeNode> e = node.depthFirstEnumeration();

		while (e.hasMoreElements()) {
			DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) e.nextElement();
			if (nextNode.getAllowsChildren()) {
				persistenceController.getGroupDataService().deleteGroup(nextNode.toString());
			}
		}

	}
}
