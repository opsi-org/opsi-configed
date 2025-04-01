/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public abstract class AbstractGroupTree extends JTree implements TreeSelectionListener {
	public static final String ALL_GROUPS_NAME = Configed.getResourceValue("AbstractGroupTree.groupsName");

	OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory.getPersistenceController();

	public final GroupNode rootNode = new GroupNode("");

	protected GroupNode groupNodeGroups;
	protected GroupNode groupNodeFullList;

	protected Map<String, Map<String, String>> groups = new TreeMap<>();
	// map of all group maps,
	// groupid --> group map

	protected Map<String, GroupNode> groupNodes = new HashMap<>();
	// groupid --> group node
	// is a function since a group name cannot occur twice

	protected Set<String> activeParents = new HashSet<>();
	// groups containing clients (especially the selected ones)

	protected DefaultTreeModel model;

	protected ConfigedMain configedMain;

	private Collection<String> selectedObjectsInTable = new HashSet<>();

	private TreePath pathToROOT = new TreePath(new Object[] { rootNode });

	protected AbstractGroupTree(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		init();

		model = new DefaultTreeModel(rootNode);
		super.setModel(model);
		super.setCellRenderer(new GroupTreeRenderer(this));
	}

	public boolean isGroupNodeFullList(DefaultMutableTreeNode compareNode) {
		return groupNodeFullList.equals(compareNode);
	}

	@Override
	public DefaultTreeModel getModel() {
		return model;
	}

	private void init() {
		ToolTipManager.sharedInstance().registerComponent(this);

		super.addTreeSelectionListener(this);

		createTree();

		setRootVisible(false);
		setShowsRootHandles(true);

		// popups on nodes
		JPopupMenu popupMenu = new JPopupMenu();
		TreePopupMouseListener treePopupMouseListener = new TreePopupMouseListener(popupMenu, this);
		addMouseListener(treePopupMouseListener);

		// preparing Drag and Drop
		TransferHandler handler = new GroupTreeTransferHandler(this);
		setTransferHandler(handler);
		setDragEnabled(true);
		setDropMode(DropMode.ON);

		setToggleClickCount(0);

		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				TreePath[] selectedPaths = getSelectionPaths();
				int selRow = getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getPathForRow(selRow);

				if (selectedPaths != null && selectedPaths.length > 1) {
					Logging.debug(this, "mousePressed (multi groups) ", selectedPaths.length, " ",
							Arrays.toString(selectedPaths));
					setGroupsAndSelect(Arrays.stream(getSelectionPaths()).map(TreePath::getLastPathComponent)
							.map(DefaultMutableTreeNode.class::cast).toArray(DefaultMutableTreeNode[]::new));
				} else if (selRow != -1 && e.getClickCount() == 2
						&& groups.containsKey(selPath.getLastPathComponent().toString())) {
					Logging.debug(this, "mousePressed (single groups) ", selPath);
					expandPath(selPath);
					setGroupAndSelect((DefaultMutableTreeNode) selPath.getLastPathComponent());
				} else {
					// Nothing to do in other cases
				}
			}
		};

		addMouseListener(ml);
	}

	public void reInitTree() {
		Map<String, Map<String, Object>> nodes = getExpandedAndSelectedNodes();

		groupNodes.clear();
		groups.clear();
		rootNode.removeAllChildren();
		groupNodeFullList.removeAllChildren();
		model.nodeStructureChanged(rootNode);
		createTree();

		removeTreeSelectionListener(this);
		model = new DefaultTreeModel(rootNode);
		setModel(model);

		expandAndSelectNodes(nodes);

		addTreeSelectionListener(this);
	}

	public Map<String, Map<String, Object>> getExpandedAndSelectedNodes() {
		Map<String, Map<String, Object>> expandedNodes = new HashMap<>();

		Enumeration<TreePath> expanded = getExpandedDescendants(new TreePath(rootNode));
		List<TreePath> selectionPaths = Arrays.asList(getSelectionPaths());

		if (expanded != null) {
			while (expanded.hasMoreElements()) {
				TreePath path = expanded.nextElement();
				Map<String, Object> map = new HashMap<>();
				map.put("expanded", true);
				expandedNodes.put(path.getLastPathComponent().toString(), map);
			}
		}

		if (selectionPaths != null) {
			for (TreePath path : selectionPaths) {
				Map<String, Object> map = expandedNodes.get(path.getLastPathComponent().toString()) != null
						? expandedNodes.get(path.getLastPathComponent().toString())
						: new HashMap<>();

				map.put("selected", true);
				if (!((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()) {
					map.put("parent", path.getParentPath().getLastPathComponent().toString());
					map.put("index", model.getIndexOfChild(path.getParentPath().getLastPathComponent(),
							path.getLastPathComponent()));
				}
				String parent = !((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()
						? (path.getParentPath().getLastPathComponent().toString() + "/"
								+ path.getLastPathComponent().toString())
						: path.getLastPathComponent().toString();
				expandedNodes.put(parent, map);
			}
		}

		return expandedNodes;
	}

	public void expandAndSelectNodes(Map<String, Map<String, Object>> nodes) {
		for (Map.Entry<String, Map<String, Object>> node : nodes.entrySet()) {
			DefaultMutableTreeNode currentNode = getNodeFromMap(node);

			if (currentNode == null) {
				continue;
			}

			if (Boolean.TRUE.equals(node.getValue().get("expanded"))) {
				TreePath path = new TreePath(getModel().getPathToRoot(currentNode));
				expandPath(path);
			}

			if (Boolean.TRUE.equals(node.getValue().get("selected"))) {
				TreePath path = new TreePath(getModel().getPathToRoot(currentNode));
				addSelectionPath(path);
			}
		}
	}

	private DefaultMutableTreeNode getNodeFromMap(Map.Entry<String, Map<String, Object>> node) {
		DefaultMutableTreeNode result = groupNodes.get(node.getKey());
		if (result == null && node.getValue().containsKey("parent")) {
			DefaultMutableTreeNode parentNode = groupNodes.get(node.getValue().get("parent"));
			if (parentNode.getChildCount() > 0) {
				result = (DefaultMutableTreeNode) parentNode.getChildAt((int) node.getValue().get("index"));
			}
		}
		return result;
	}

	abstract void createTree();

	abstract void setGroupAndSelect(DefaultMutableTreeNode groupNode);

	abstract void setGroupsAndSelect(DefaultMutableTreeNode[] groupNode);

	public void initActiveParents() {
		activeParents.clear();
	}

	public void produceActiveParents() {
		initActiveParents();

		activeParents.addAll(collectParentIDs(getSelectedObjectsInTable()));
		Logging.debug(this, "produceActiveParents activeParents ", activeParents);

		repaint();
	}

	public Set<String> collectParentIDs(Collection<String> elementIds) {
		Set<String> result = new HashSet<>();

		recursivelyCollectParentIDs(result, rootNode, elementIds);

		return result;
	}

	private static void recursivelyCollectParentIDs(Set<String> allNodes, DefaultMutableTreeNode node,
			Collection<String> nodeIds) {
		Enumeration<TreeNode> children = node.children();

		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();

			if (nodeIds.contains(child.toString())) {
				allNodes.addAll(Arrays.stream(node.getPath()).map(Object::toString).toList());
			}

			recursivelyCollectParentIDs(allNodes, child, nodeIds);
		}
	}

	public void updateSelectedObjectsInTable() {
		selectedObjectsInTable = getSelectedObjectsInTable();
	}

	public boolean isSelectedInTable(String objectId) {
		return selectedObjectsInTable.contains(objectId);
	}

	public Set<String> getActiveParents() {
		return activeParents;
	}

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

		String answer = (String) JOptionPane.showInputDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("description"),
				Configed.getResourceValue("ClientTree.editGroup") + ": " + groupId, JOptionPane.PLAIN_MESSAGE, null,
				null, groups.get(groupId).get("description"));

		if (answer != null) {
			groups.get(groupId).put("description", answer);
			persistenceController.getGroupDataService().updateGroup(groupId, groups.get(groupId),
					this instanceof ClientTree);
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
			int returnedOption = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ClientTree.deleteGroupWarning"),
					Configed.getResourceValue("ClientTree.deleteGroupNode") + ": " + nodeID,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

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
	public GroupNode makeSubgroupAt(TreePath path) {
		GroupNode result = null;

		DefaultMutableTreeNode node;

		if (path == null) {
			node = groupNodeGroups;
		} else {
			node = (DefaultMutableTreeNode) path.getLastPathComponent();
		}

		if (node.getAllowsChildren()) {
			JLabel labelGroupName = new JLabel(Configed.getResourceValue("ClientTree.editNode.label.groupname"));
			labelGroupName.setFont(labelGroupName.getFont().deriveFont(Font.BOLD));

			JTextField groupNameField = new JTextField();

			JLabel labelDescription = new JLabel(Configed.getResourceValue("description"));
			labelDescription.setFont(labelDescription.getFont().deriveFont(Font.BOLD));

			JTextField groupDescriptionField = new JTextField();
			String inscription = "";

			JPanel panel = new JPanel();
			GroupLayout layout = new GroupLayout(panel);
			panel.setLayout(layout);

			layout.setVerticalGroup(layout.createSequentialGroup().addComponent(labelGroupName)
					.addComponent(groupNameField).addGap(Globals.GAP_SIZE).addComponent(labelDescription)
					.addComponent(groupDescriptionField));
			layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(labelGroupName).addComponent(groupNameField).addComponent(labelDescription)
					.addComponent(groupDescriptionField));

			String newGroupKey = null;

			do {
				int answer = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
						new Object[] { inscription, panel }, Configed.getResourceValue("ClientTree.addNode"),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 0);

				if (answer == JOptionPane.OK_OPTION) {
					newGroupKey = groupNameField.getText().toLowerCase(Locale.ROOT);
				} else {
					return null;
				}
				inscription = Configed.getResourceValue("ClientTree.requestNotExistingGroupName");
			} while ("".equals(newGroupKey) || groups.keySet().contains(newGroupKey));

			String description = groupDescriptionField.getText();

			// Now variable gotName equals true
			Map<String, String> newGroup = new HashMap<>();
			newGroup.put("groupId", newGroupKey);
			newGroup.put("parentGroupId", node.toString());
			newGroup.put("description", description);

			// send data to server
			if (persistenceController.getGroupDataService().addGroup(newGroup, this instanceof ClientTree)) {
				Logging.debug(this, "makeSubGroupAt newGroupKey, newGroup ", newGroupKey, ", ", newGroup);

				result = produceGroupNode(newGroupKey, description, node.toString());
				insertNodeInOrder(result, node);
			}
		}

		return result;
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

	public TreePath getPathToNode(DefaultMutableTreeNode node) {
		if (node == null) {
			return null;
		}

		TreeNode[] ancestors = node.getPath();
		TreePath path = pathToROOT;

		for (int i = 1; i < ancestors.length; i++) {
			path = path.pathByAddingChild(ancestors[i]);
		}

		return path;
	}

	public boolean removeNodes(Iterable<DefaultMutableTreeNode> nodes) {
		List<Object2GroupEntry> groupEntries = new ArrayList<>();

		for (DefaultMutableTreeNode node : nodes) {
			String clientId = (String) node.getUserObject();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

			removeNodeInternally(clientId, (GroupNode) parent);
			groupEntries.add(new Object2GroupEntry(clientId, parent.toString()));
		}

		String groupType = this instanceof ClientTree ? Object2GroupEntry.GROUP_TYPE_HOSTGROUP
				: Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP;

		return persistenceController.getGroupDataService().removeHostGroupElements(groupEntries, groupType);
	}

	protected GroupNode produceGroupNode(String groupId, String description) {
		return produceGroupNode(groupId, description, null);
	}

	protected GroupNode produceGroupNode(String groupId, String description, String parentId) {
		GroupNode groupNode = new GroupNode(groupId);

		Map<String, String> groupMap = new HashMap<>();
		groupMap.put("groupId", groupId);
		if (parentId != null) {
			groupMap.put("parentGroupId", parentId);
		}
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

	public boolean isInGROUPS(String groupName) {
		GroupNode node = groupNodes.get(groupName);
		if (node == null) {
			return false;
		}

		TreeNode[] path = node.getPath();
		return path.length >= 2 && path[1] == groupNodeGroups;
	}

	public boolean isInGROUPS(TreePath path) {
		return path.getPathCount() >= 2 && path.getPathComponent(1) == groupNodeGroups;
	}

	public Map<String, Map<String, String>> getGroups() {
		return groups;
	}

	public boolean isChildOfALL(TreeNode node) {
		return node.getParent() == groupNodeFullList;
	}

	public void moveGroupTo(String importID, GroupNode groupNode, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		insertNodeInOrder(groupNode, dropParentNode);
		model.nodeStructureChanged(sourceParentNode);
		makeVisible(dropPath.pathByAddingChild(groupNode));

		Map<String, String> theGroup = getGroups().get(importID);
		theGroup.put("parentGroupId", dropParentID);
		persistenceController.getGroupDataService().updateGroup(importID, theGroup, this instanceof ClientTree);
	}

	public TreePath getActiveTreePath(String id) {
		return Arrays.stream(getSelectionPaths()).filter(
				treePath -> ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject().equals(id))
				.findAny().orElse(null);
	}

	protected static DefaultMutableTreeNode getChildWithUserObjectString(String objectID,
			DefaultMutableTreeNode groupNode) {
		Enumeration<TreeNode> enumer = groupNode.children();
		DefaultMutableTreeNode result = null;

		while (enumer.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumer.nextElement();
			if (child.getUserObject().toString().equals(objectID)) {
				result = child;
				break;
			}
		}

		return result;
	}

	public String getGroupDescription(String groupId) {
		if (groups.containsKey(groupId)) {
			return groups.get(groupId).get("description");
		} else {
			return null;
		}
	}

	abstract boolean isInDirectory(String node);

	abstract boolean isInDirectory(TreePath path);

	abstract Set<GroupNode> getLocationsInDirectory(String importID);

	abstract void moveObjectTo(String importID, TreePath sourcePath, String sourceParentID, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID);

	abstract void copyObjectTo(String objectID, TreePath sourcePath, String newParentID,
			DefaultMutableTreeNode newParentNode, TreePath newParentPath);

	abstract Set<String> getSelectedObjectsInTable();

	public static Set<String> getChildrenRecursively(TreeNode groupNode) {
		Set<String> resultIds = new HashSet<>();

		addChildrenRecoursively(groupNode.children(), resultIds);

		return resultIds;
	}

	protected static void addChildrenRecoursively(Enumeration<? extends TreeNode> children, Set<String> resultIds) {
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();

			if (child.getAllowsChildren()) {
				addChildrenRecoursively(child.children(), resultIds);
			} else {
				resultIds.add(child.getUserObject().toString());
			}
		}
	}
}
