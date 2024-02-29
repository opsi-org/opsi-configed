/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.type.HostInfo;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditList;

public class ClientTree extends AbstractGroupTree {
	public static final String DIRECTORY_NAME = Configed.getResourceValue("AbstractGroupTree.directory");
	public static final String DIRECTORY_PERSISTENT_NAME = "clientdirectory";
	public static final String DIRECTORY_NOT_ASSIGNED_NAME = Configed.getResourceValue("AbstractGroupTree.notAssigned");
	private static Set<String> topGroupNames;

	public static final String ALL_CLIENTS_NAME = Configed.getResourceValue("AbstractGroupTree.allClients");

	private GroupNode groupNodeDirectory;
	private GroupNode groupNodeDirectoryNotAssigned;

	private TreePath pathToROOT;
	private TreePath pathToALL;

	// supervising data
	private Map<String, Set<GroupNode>> locationsInDirectory;
	// clientId --> set of all containing groups

	private Map<String, DefaultMutableTreeNode> clientNodesInDirectory;
	// clientid --> client node
	// is a function, when the directory has been cleared

	private Set<String> activeParents = new HashSet<>();
	// groups containing clients (especially the selected ones)

	private ClientTreeRenderer renderer;

	static {
		topGroupNames = new HashSet<>();
		topGroupNames.add(ALL_CLIENTS_NAME);
		topGroupNames.add(ALL_GROUPS_NAME);
		topGroupNames.add(DIRECTORY_NAME);
		topGroupNames.add(DIRECTORY_NOT_ASSIGNED_NAME);
	}

	public ClientTree(ConfigedMain configedMain) {
		super(configedMain);

		initClientTree();
	}

	public static String translateToPersistentName(String name) {
		if (DIRECTORY_NAME.equals(name)) {
			return DIRECTORY_PERSISTENT_NAME;
		} else {
			return name;
		}
	}

	private static class NodeComparator implements Comparator<DefaultMutableTreeNode> {
		final Collator myCollator = Collator.getInstance();

		NodeComparator() {
			myCollator.setStrength(Collator.IDENTICAL);
		}

		@Override
		public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
			return myCollator.compare("" + o1.getUserObject(), "" + o2.getUserObject());
		}
	}

	private void initClientTree() {
		Logging.debug(this, "UI " + getUI());

		setToggleClickCount(0);

		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getPathForRow(selRow);
				if (selRow != -1 && e.getClickCount() == 2
						&& groups.containsKey(selPath.getLastPathComponent().toString())) {
					expandPath(selPath);
					configedMain.setGroupAndSelect(selPath.getLastPathComponent().toString());
				}
			}
		};

		addMouseListener(ml);

		renderer = new ClientTreeRenderer(this);
		setCellRenderer(renderer);

		locationsInDirectory = new HashMap<>();
		clientNodesInDirectory = new HashMap<>();
	}

	public void setClientInfo(Map<String, HostInfo> host2HostInfo) {
		renderer.setHost2HostInfo(host2HostInfo);
	}

	// publishing the private method
	@Override
	public TreePath[] getPathBetweenRows(int index0, int index1) {
		return super.getPathBetweenRows(index0, index1);
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

	// interface TreeSelectionListener
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		configedMain.treeClientsSelectAction(getSelectionPaths());
	}

	private void createDirectoryNotAssigned() {
		groupNodeDirectoryNotAssigned = produceGroupNode(DIRECTORY_NOT_ASSIGNED_NAME,
				Configed.getResourceValue("AbstractGroupTree.notAssigned.tooltip"));

		groupNodeDirectoryNotAssigned.setFixed(true);

		groupNodeDirectory.add(groupNodeDirectoryNotAssigned);
	}

	// generate tree structure
	@Override
	protected void createTopNodes() {
		rootNode.setImmutable(true);
		rootNode.setFixed(true);

		pathToROOT = new TreePath(new Object[] { rootNode });

		// GROUPS
		groupNodeGroups = produceGroupNode(ALL_GROUPS_NAME,
				Configed.getResourceValue("AbstractGroupTree.groupsName.tooltip"));
		groupNodeGroups.setAllowsOnlyGroupChilds(true);
		groupNodeGroups.setFixed(true);

		rootNode.add(groupNodeGroups);

		// DIRECTORY
		groupNodeDirectory = produceGroupNode(DIRECTORY_NAME,
				Configed.getResourceValue("AbstractGroupTree.directory.tooltip"));

		groupNodeDirectory.setAllowsOnlyGroupChilds(true);
		groupNodeDirectory.setFixed(true);

		rootNode.add(groupNodeDirectory);

		// ALL
		groupNodeFullList = produceGroupNode(ALL_CLIENTS_NAME,
				Configed.getResourceValue("AbstractGroupTree.allClients.tooltip"));

		rootNode.add(groupNodeFullList);
		groupNodeFullList.setImmutable(true);
		groupNodeFullList.setFixed(true);

		pathToALL = new TreePath(new Object[] { rootNode, groupNodeFullList });
	}

	public void clear() {
		// clear jtree model
		groupNodeFullList.removeAllChildren();
		groupNodeDirectory.removeAllChildren();
		groupNodeGroups.removeAllChildren();

		model.nodeStructureChanged(groupNodeGroups);

		// clear supervising data
		clientNodesInDirectory.clear();
		locationsInDirectory.clear();

		// it is not necessary to clear groups and groupnodes since they will be rebuilt
		// by produceAndLinkGroups
	}

	private void produceDIRECTORYinfo(DefaultMutableTreeNode node) {
		if (isInDirectory(new TreePath(node.getPath()))) {
			String nodeID = (String) node.getUserObject();
			locationsInDirectory.computeIfAbsent(nodeID, arg -> new TreeSet<>(new NodeComparator()))
					.add((GroupNode) node.getParent());
		}
	}

	private void produceClients(Collection<String> clientIds, DefaultMutableTreeNode parent) {
		produceClients(clientIds, parent, false);
	}

	private void produceClients(Collection<String> clientIds, DefaultMutableTreeNode parent, boolean register) {
		for (String clientId : clientIds) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(clientId, false);
			if (register) {
				clientNodesInDirectory.put(clientId, node);
			}

			if (parent != null) {
				parent.add(node);
			} else {
				Logging.debug(this, "not added Node for " + clientId + " under " + parent);
			}

			produceDIRECTORYinfo(node);
		}

		model.nodeStructureChanged(parent);
	}

	@Override
	public void setGroupAndSelect(DefaultMutableTreeNode groupNode) {
		configedMain.setGroupAndSelect(groupNode.toString());
	}

	public void produceTreeForALL(Collection<String> clientIds) {
		clientNodesInDirectory.clear();
		produceClients(clientIds, groupNodeFullList);
	}

	// we produce all partial pathes that are defined by the persistent groups
	public void produceAndLinkGroups(final Map<String, Map<String, String>> importedGroups) {
		Logging.debug(this, "produceAndLinkGroups " + importedGroups.keySet());
		// we need a local copy since we add virtual groups
		groups.putAll(importedGroups);

		for (String group : importedGroups.keySet()) {
			groupNodes.put(group, new GroupNode(group));
		}

		renderer.setGroupNodeTooltips(groups);

		createDirectoryNotAssigned();

		linkGroupNodes();
	}

	private void linkGroupNodes() {
		for (Entry<String, Map<String, String>> group : groups.entrySet()) {
			if (topGroupNames.contains(group.getKey())) {
				continue;
			}

			String parentId = group.getValue().get("parentGroupId");
			if (parentId == null || "null".equalsIgnoreCase(parentId)) {
				parentId = ALL_GROUPS_NAME;
			}

			DefaultMutableTreeNode parent = null;
			if (groupNodes.get(parentId) == null) {
				parent = groupNodes.get(ALL_GROUPS_NAME);
			} else {
				parent = groupNodes.get(parentId);
			}

			DefaultMutableTreeNode node = groupNodes.get(group.getKey());
			parent.add(node);
			model.nodesWereInserted(parent, new int[] { model.getIndexOfChild(parent, node) });
		}
	}

	// Return null means, all clients are allowed
	@SuppressWarnings("java:S1168")
	public Set<String> associateClientsToGroups(Iterable<String> clientIds, Map<String, Set<String>> fObject2Groups,
			Set<String> permittedHostGroups) {
		locationsInDirectory.clear();

		// we must rebuild this map since the direct call of persist.getFGroup2Members
		// would eliminate the filter by depot etc.
		Map<String, List<String>> group2Members = produceGroup2Members(clientIds, fObject2Groups);

		List<String> membersOfDirectoryNotAssigned = new ArrayList<>();
		group2Members.put(DIRECTORY_NOT_ASSIGNED_NAME, membersOfDirectoryNotAssigned);

		// we build and link the groups
		for (Entry<String, List<String>> entry : group2Members.entrySet()) {
			GroupNode groupNode = groupNodes.get(entry.getKey());
			if (groupNode == null) {
				Logging.warning("group for groupId " + entry.getKey() + " not found");
			} else {
				boolean register = isInDirectory(groupNode);
				produceClients(entry.getValue(), groupNode, register);
			}
		}

		for (String clientId : clientIds) {
			if (!isClientInAnyDIRECTORYGroup(clientId)) {
				membersOfDirectoryNotAssigned.add(clientId);

				DefaultMutableTreeNode node = new DefaultMutableTreeNode(clientId, false);
				groupNodeDirectoryNotAssigned.add(node);

				clientNodesInDirectory.put(clientId, node);
			}
		}

		model.nodeStructureChanged(groupNodeDirectory);

		if (permittedHostGroups == null) {
			// null means, all are allowed
			return null;
		}

		Set<String> allowedClients = new HashSet<>();
		for (String permittedGroup : permittedHostGroups) {
			allowedClients.addAll(group2Members.get(permittedGroup));
		}

		return allowedClients;
	}

	private boolean isClientInAnyDIRECTORYGroup(String clientId) {
		checkDirectory(clientId, null);
		Set<GroupNode> hostingGroups = locationsInDirectory.get(clientId);
		return !hostingGroups.isEmpty();
	}

	private static Map<String, List<String>> produceGroup2Members(Iterable<String> clientIds,
			Map<String, Set<String>> fObject2Groups) {
		Map<String, List<String>> group2Members = new HashMap<>();
		for (String clientId : clientIds) {
			if (fObject2Groups.get(clientId) != null) {
				Set<String> belongingTo = fObject2Groups.get(clientId);
				for (String groupId : belongingTo) {
					List<String> memberList = group2Members.computeIfAbsent(groupId, id -> new ArrayList<>());
					memberList.add(clientId);
				}
			}
		}
		return group2Members;
	}

	private boolean addObject2InternalGroup(String objectID, DefaultMutableTreeNode newGroupNode, TreePath newPath) {
		// child with this objectID not existing
		if (getChildWithUserObjectString(objectID, newGroupNode) == null) {
			produceClients(Collections.singleton(objectID), newGroupNode);
			makeVisible(newPath.pathByAddingChild(objectID));
			return true;
		}

		return false;
	}

	@Override
	public void removeNodeInternally(String clientID, GroupNode parentNode) {
		Logging.debug("removeClientInternally clientId, parentNode " + clientID + ", " + parentNode);

		DefaultMutableTreeNode clientNode = getChildWithUserObjectString(clientID, parentNode);

		int stopCounter = 0;

		while (clientNode != null && stopCounter <= clientNodesInDirectory.size()) {
			parentNode.remove(clientNode);
			// with more than one clientNode we seem to get as many instances of one client
			// node supplied as there are clients altogether, why ever
			// as a hack we go into looping
			clientNode = getChildWithUserObjectString(clientID, parentNode);
			stopCounter++;
		}
		if (stopCounter > clientNodesInDirectory.size()) {
			Logging.warning("removing client not successful but stopped because of reaching the repetition limit");
		}

		clientNodesInDirectory.remove(clientID); // 11

		model.nodeStructureChanged(parentNode);

		repaint();
	}

	@Override
	public void moveObjectTo(String importID, TreePath sourcePath, String sourceParentID, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		DefaultMutableTreeNode existingNode = getChildWithUserObjectString(importID, dropParentNode);
		if (existingNode == null) {
			// we have not a node with this name in the target group
			if (sourcePath != null) {
				Logging.debug(this,
						"moveObjectTo checked importID sourcePath.getLastPathComponent(); "
								+ sourcePath.getLastPathComponent() + " class "
								+ ((sourcePath.getLastPathComponent()).getClass()));
			} else {
				Logging.debug(this, "moveCmoveObjectToientTo sourcePath null, sourceParentNode " + sourceParentNode);
			}

			DefaultMutableTreeNode clientNode = getChildWithUserObjectString(importID, sourceParentNode);
			insertNodeInOrder(clientNode, dropParentNode);
			model.nodeStructureChanged(sourceParentNode);

			if (DIRECTORY_NOT_ASSIGNED_NAME.equals(dropParentID)) {
				persistenceController.getGroupDataService().addObject2Group(importID, dropParentID, false);
			}

			// operations in DIRECTORY

			if (isInDirectory(dropPath)) {
				locationsInDirectory.get(importID).add(getGroupNode(dropParentID));
				locationsInDirectory.get(importID).remove(sourceParentNode);
			}

			activeParents.addAll(Arrays.stream(dropPath.getPath()).map(Object::toString).collect(Collectors.toSet()));

			Logging.debug(this,
					"moveObjectTo -- remove " + importID + " from " + sourceParentID
							+ " clientNode, sourceParentNode, sourcePath " + clientNode + ", " + sourceParentNode + ", "
							+ sourcePath);

			// persistent removal
			persistenceController.getGroupDataService().removeObject2Group(importID, sourceParentID);
			removeNodeInternally(importID, sourceParentNode);

			makeVisible(dropPath.pathByAddingChild(clientNode));
			repaint();

			checkDirectory(importID, (GroupNode) dropParentNode);
		}
	}

	@Override
	public void copyObjectTo(String objectID, TreePath sourcePath, String newParentID,
			DefaultMutableTreeNode newParentNode, TreePath newParentPath) {
		Logging.debug(this, " copying " + objectID + ", sourcePath " + sourcePath + " into group " + newParentID);

		Logging.debug(this, " -- copyObjectTo childs are persistent, newParentNode " + newParentNode + " "
				+ DIRECTORY_NOT_ASSIGNED_NAME.equals(newParentNode.toString()));

		boolean success = addObject2InternalGroup(objectID, newParentNode, newParentPath);

		if (success && !DIRECTORY_NOT_ASSIGNED_NAME.equals(newParentNode.toString())) {
			persistenceController.getGroupDataService().addObject2Group(objectID, newParentID, true);
		}

		activeParents.addAll(Arrays.stream(newParentPath.getPath()).map(Object::toString).collect(Collectors.toSet()));

		// operations in DIRECTORY

		Set<GroupNode> groupsInDIRECTORY = locationsInDirectory.get(objectID);

		// remove entry in NOT_ASSIGNED
		if (groupsInDIRECTORY.contains(groupNodeDirectoryNotAssigned) && groupsInDIRECTORY.size() > 1) {
			locationsInDirectory.get(objectID).remove(groupNodeDirectoryNotAssigned);
			removeNodeInternally(objectID, groupNodeDirectoryNotAssigned);
		}

		repaint();

		checkDirectory(objectID, (GroupNode) newParentNode);
	}

	private void checkDirectory(String clientID, GroupNode selectedNode) {
		Set<GroupNode> groupsInDIRECTORY = locationsInDirectory.computeIfAbsent(clientID,
				s -> new TreeSet<>(new NodeComparator()));

		if (groupsInDIRECTORY.size() <= 1) {
			return;
		}

		// size should always be at least 1
		// we handle the case that is > 1

		List<GroupNode> correctNode = selectOneNode(groupsInDIRECTORY, clientID, selectedNode);

		if (correctNode != null) {
			// we did some selection

			// we remove the one selected node, the not desired nodes remain
			groupsInDIRECTORY.removeAll(correctNode);

			for (GroupNode node : groupsInDIRECTORY) {
				removeNodeInternally(clientID, node);
				persistenceController.getGroupDataService().removeObject2Group(clientID,
						node.getUserObject().toString());
			}

			locationsInDirectory.put(clientID, new HashSet<>(correctNode));
		}
	}

	private static List<GroupNode> selectOneNode(Set<GroupNode> groupSet, String clientID, GroupNode preSelected) {
		List<GroupNode> result = null;

		//Ask only if mainFrame is not null; Otherwise, errors will occure
		if (groupSet.size() > 1 && ConfigedMain.getMainFrame() != null) {
			FEditList<GroupNode> fList = new FEditList<>();
			fList.setListModel(new DefaultComboBoxModel<>(groupSet.toArray(new GroupNode[0])));
			fList.setTitle(Configed.getResourceValue("ClientTree.DIRECTORYname") + " "
					+ Configed.getResourceValue("ClientTree.checkDIRECTORYAssignments"));
			fList.setExtraLabel(Configed.getResourceValue("ClientTree.severalLocationsAssigned") + " >> " + clientID
					+ " <<, " + Configed.getResourceValue("ClientTree.selectCorrectLocation"));
			fList.setPreferredScrollPaneSize(new Dimension(640, 60));
			fList.init();

			fList.setLocationRelativeTo(ConfigedMain.getMainFrame());
			fList.setModal(true);

			if (preSelected != null) {
				fList.setSelectedValue(preSelected);
				fList.setDataChanged(true);
			}

			fList.setVisible(true);

			if (fList.getSelectedList().isEmpty()) {
				int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("ClientTree.abandonUniqueLocation"),
						Configed.getResourceValue("ClientTree.requestInformation"), -1, JOptionPane.WARNING_MESSAGE,
						null,
						new String[] { Configed.getResourceValue("buttonYES"), Configed.getResourceValue("buttonNO") },
						Configed.getResourceValue("buttonNO"));

				if (returnedOption == 1 || returnedOption == JOptionPane.CLOSED_OPTION) {
					result = selectOneNode(groupSet, clientID, preSelected);
				}
			} else {
				result = fList.getSelectedList();
			}
		}

		return result;
	}

	@Override
	public boolean isInDirectory(String groupName) {
		GroupNode node = groupNodes.get(groupName);
		if (node == null) {
			return false;
		}

		return isInDirectory(node);
	}

	@Override
	public boolean isInDirectory(TreePath path) {
		return path.getPathCount() >= 2 && path.getPathComponent(1) == groupNodeDirectory;
	}

	private boolean isInDirectory(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		return path.length >= 2 && path[1] == groupNodeDirectory;
	}

	public TreePath getPathToALL() {
		return pathToALL;
	}

	public void initActiveParents() {
		activeParents.clear();
	}

	public void produceActiveParents(Collection<String> clientIds) {
		initActiveParents();

		activeParents.addAll(collectParentIDs(clientIds));
		Logging.debug(this, "produceActiveParents activeParents " + activeParents);

		repaint();
	}

	public Set<String> collectParentIDs(Collection<String> nodeIds) {
		Set<String> result = new HashSet<>();

		recursivelyCollectParentIDs(result, rootNode, nodeIds);

		return result;
	}

	private static void recursivelyCollectParentIDs(Set<String> allNodes, DefaultMutableTreeNode node,
			Collection<String> nodeIds) {
		Enumeration<TreeNode> children = node.children();

		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();

			if (nodeIds.contains(child.toString())) {
				allNodes.addAll(Arrays.stream(node.getPath()).map(Object::toString).collect(Collectors.toList()));
			}

			recursivelyCollectParentIDs(allNodes, child, nodeIds);
		}
	}

	public Set<String> getActiveParents() {
		return activeParents;
	}

	@Override
	public Set<GroupNode> getLocationsInDirectory(String clientId) {
		return locationsInDirectory.get(clientId);
	}

	@Override
	public Set<String> getSelectedObjectsInTable() {
		return configedMain.getClientTable().getSelectedSet();
	}
}
