/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

import java.text.Collator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.gui.share.swing.ButtonTabComponent;
import de.uib.configed.gui.type.Object2GroupEntry;
import de.uib.configed.share.logging.Logging;

public class ClientTree extends AbstractGroupTree {
	public static final String DIRECTORY_NAME = Configed.getResourceValue("AbstractGroupTree.directory");
	public static final String DIRECTORY_PERSISTENT_NAME = "clientdirectory";
	public static final String DIRECTORY_NOT_ASSIGNED_NAME = Configed.getResourceValue("AbstractGroupTree.notAssigned");
	private static Set<String> topGroupNames;

	public static final String ALL_CLIENTS_NAME = Configed.getResourceValue("AbstractGroupTree.allClients");

	private GroupNode groupNodeDirectory;
	private GroupNode groupNodeDirectoryNotAssigned;

	private TreePath pathToALL;

	private Set<String> allowedClients;

	// supervising data
	private Map<String, Set<GroupNode>> locationsInDirectory;
	// clientId --> set of all containing groups

	private Map<String, DefaultMutableTreeNode> clientNodesInDirectory;
	// clientid --> client node
	// is a function, when the directory has been cleared

	static {
		topGroupNames = new HashSet<>();
		topGroupNames.add(ALL_CLIENTS_NAME);
		topGroupNames.add(ALL_GROUPS_NAME);
		topGroupNames.add(DIRECTORY_NAME);
		topGroupNames.add(DIRECTORY_NOT_ASSIGNED_NAME);
	}

	public ClientTree(ConfigedMain configedMain) {
		super(configedMain);

		locationsInDirectory = new HashMap<>();
		clientNodesInDirectory = new HashMap<>();
	}

	public static String translateToPersistentName(String name) {
		return DIRECTORY_NAME.equals(name) ? DIRECTORY_PERSISTENT_NAME : name;
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

	// publishing the private method
	@Override
	public TreePath[] getPathBetweenRows(int index0, int index1) {
		return super.getPathBetweenRows(index0, index1);
	}

	// interface TreeSelectionListener
	@Override
	public void reactOnTreeSelection() {
		if (ConfigedMain.getMainFrame() != null) {
			ButtonTabComponent comp = (ButtonTabComponent) ConfigedMain.getMainFrame().getMainPanelManager()
					.getTabbedPane().getTabComponentAt(1);
			comp.showButton(getSelectionPaths() == null
					|| !ALL_CLIENTS_NAME.equals(getSelectionPath().getLastPathComponent().toString())
					|| getSelectionPaths().length > 1);
		}
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
	protected void createTree() {
		rootNode.setImmutable(true);
		rootNode.setFixed(true);

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
		groupNodeAllObjects = produceGroupNode(ALL_CLIENTS_NAME,
				Configed.getResourceValue("AbstractGroupTree.allClients.tooltip"));

		rootNode.add(groupNodeAllObjects);
		groupNodeAllObjects.setImmutable(true);
		groupNodeAllObjects.setFixed(true);

		pathToALL = new TreePath(new Object[] { rootNode, groupNodeAllObjects });

		if (model != null) {
			build();
		}
	}

	public Set<String> getAllowedClients() {
		if (!persistenceController.getDataServices().userRoles.isAccessToHostgroupsOnlyIfExplicitlyStatedPD()
				&& allowedClients == null) {
			Map<String, Set<String>> group2Members = persistenceController.getDataServices().group
					.getFHostGroup2MembersPD();
			group2Members.put(DIRECTORY_NOT_ASSIGNED_NAME, new HashSet<>());

			Map<String, Map<String, String>> hostGroups = persistenceController.getDataServices().group
					.getHostGroupsPD();
			Set<String> expandedPermittedHostGroups = expandPermittedHostGroups(hostGroups);
			allowedClients = getAllowedClients(group2Members, expandedPermittedHostGroups);
		} else if (persistenceController.getDataServices().userRoles.isAccessToHostgroupsOnlyIfExplicitlyStatedPD()) {
			allowedClients = null;
		} else {
			// Not needed.
		}
		return allowedClients;
	}

	public void build() {
		Set<String> allPCs = persistenceController.getDataServices().hostInfoCollections
				.getClientsForDepots(configedMain.getSelectedDepots(), getAllowedClients());

		produceTreeForALL(allPCs);

		Map<String, Map<String, String>> hostGroups = persistenceController.getDataServices().group.getHostGroupsPD();
		Set<String> expandedPermittedHostGroups = expandPermittedHostGroups(hostGroups);
		produceAndLinkGroups(persistenceController.getDataServices().group.getHostGroupsPD(),
				expandedPermittedHostGroups);

		Map<String, Set<String>> group2Members = persistenceController.getDataServices().group
				.getFHostGroup2MembersPD();
		group2Members.put(DIRECTORY_NOT_ASSIGNED_NAME, new HashSet<>());
		associateClientsToGroups(allPCs, group2Members);

		if (allowedClients != null) {
			Logging.info(this, "build, allowedClients ", allowedClients.size());
		}
	}

	@SuppressWarnings("java:S1168")
	private Set<String> expandPermittedHostGroups(Map<String, Map<String, String>> hostGroups) {
		Set<String> permittedGroups = persistenceController.getDataServices().userRoles.getHostGroupsPermitted();
		if (permittedGroups == null) {
			return null;
		}

		Set<String> expanded = new HashSet<>(permittedGroups);
		Queue<String> queue = new ArrayDeque<>(permittedGroups);

		while (!queue.isEmpty()) {
			String currentGroup = queue.poll();
			for (Map.Entry<String, Map<String, String>> entry : hostGroups.entrySet()) {
				String groupId = entry.getKey();
				String parentId = entry.getValue().get("parentGroupId");
				if (currentGroup.equals(parentId) && expanded.add(groupId)) {
					queue.add(groupId);
				}
			}
		}
		return expanded;
	}

	public void clear() {
		// clear jtree model
		groupNodeAllObjects.removeAllChildren();
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
				Logging.debug(this, "not added Node for ", clientId, " under ", parent);
			}

			produceDIRECTORYinfo(node);
		}

		model.nodeStructureChanged(parent);
	}

	@Override
	public void setGroupAndSelect(DefaultMutableTreeNode groupNode) {
		configedMain.setGroupAndSelect(groupNode.toString());
	}

	@Override
	public void setGroupsAndSelect(DefaultMutableTreeNode[] groupNodes) {
		Set<String> clientIds = new HashSet<>();
		Set<String> selectedAclientIds = new HashSet<>();
		boolean anyIsLeaf = false;
		for (DefaultMutableTreeNode groupNode : groupNodes) {
			anyIsLeaf = anyIsLeaf || groupNode.isLeaf();

			if (groupNode.isLeaf() && !groupNode.getAllowsChildren()) {
				String nodeinfo = (String) groupNode.getUserObject();
				clientIds.add(nodeinfo);
				selectedAclientIds.add(groupNode.getUserObject().toString());
			} else {
				clientIds.addAll(getChildrenRecursively(groupNode));
			}
		}

		Logging.debug("ClientTree.setGroupsAndSelect clientIds " + clientIds);
		Logging.debug("ClientTree.setGroupsAndSelect selectedAclientIds " + selectedAclientIds);
		configedMain.setClientsFilteredAndSelected(clientIds, selectedAclientIds);
	}

	public void produceTreeForALL(Collection<String> clientIds) {
		clientNodesInDirectory.clear();
		produceClients(clientIds, groupNodeAllObjects);
	}

	// we produce all partial pathes that are defined by the persistent groups
	public void produceAndLinkGroups(final Map<String, Map<String, String>> importedGroups,
			Set<String> permittedGroups) {
		Logging.debug(this, "produceAndLinkGroups ", importedGroups.keySet());
		// we need a local copy since we add virtual groups
		groups.putAll(importedGroups);

		for (String group : importedGroups.keySet()) {
			if (topGroupNames.contains(group)) {
				continue;
			}
			groupNodes.put(group, new GroupNode(group));
		}

		createDirectoryNotAssigned();

		linkGroupNodes(permittedGroups, importedGroups);
	}

	private void linkGroupNodes(Set<String> permittedGroups, Map<String, Map<String, String>> importedGroups) {
		for (Entry<String, Map<String, String>> group : groups.entrySet()) {
			if (topGroupNames.contains(group.getKey())) {
				continue;
			}

			String parentId = findParentIdForGroupName(group.getValue().get("parentGroupId"), permittedGroups,
					importedGroups);

			if (permittedGroups == null || permittedGroups.contains(group.getKey())
					|| permittedGroups.contains(parentId)) {
				DefaultMutableTreeNode parent = groupNodes.get(parentId);
				DefaultMutableTreeNode node = groupNodes.get(group.getKey());
				addChildNodeIfNotPresent(parent, node);
				if (permittedGroups != null && !permittedGroups.contains(group.getKey())) {
					permittedGroups.add(group.getKey());
				}
			}
		}
	}

	private void addChildNodeIfNotPresent(DefaultMutableTreeNode parent, DefaultMutableTreeNode node) {
		if (parent == null || node == null) {
			Logging.info(this, "Either parent or node is null");
			return;
		}

		if (parent.getIndex(node) != -1) {
			Logging.info(this, "node already exists");
			return;
		}

		parent.add(node);
		int childIndex = model.getIndexOfChild(parent, node);
		if (childIndex >= 0) {
			model.nodesWereInserted(parent, new int[] { childIndex });
		}
	}

	private String findParentIdForGroupName(String parentId, Set<String> permittedGroups,
			Map<String, Map<String, String>> importedGroups) {
		if (!isValidGroupName(parentId)) {
			parentId = ALL_GROUPS_NAME;
		}

		while (permittedGroups != null && !permittedGroups.contains(parentId) && importedGroups.containsKey(parentId)) {
			parentId = groups.get(parentId).get("parentGroupId");

			if (!isValidGroupName(parentId)) {
				parentId = ALL_GROUPS_NAME;
			}
		}

		return parentId;
	}

	private boolean isValidGroupName(String productId) {
		return productId != null && !"null".equalsIgnoreCase(productId) && groups.containsKey(productId);
	}

	// Return null means, all clients are allowed
	@SuppressWarnings("java:S1168")
	public void associateClientsToGroups(Collection<String> clientIds, Map<String, Set<String>> group2Members) {
		locationsInDirectory.clear();

		// we build and link the groups
		for (Entry<String, Set<String>> entry : group2Members.entrySet()) {
			GroupNode groupNode = groupNodes.get(entry.getKey());
			if (groupNode == null) {
				Logging.warning("group for groupId ", entry.getKey(), " not found");
			} else {
				boolean register = isInDirectory(groupNode);
				Set<String> clientsOfGroup = new TreeSet<>(entry.getValue());
				clientsOfGroup.retainAll(clientIds);
				produceClients(clientsOfGroup, groupNode, register);
			}
		}

		for (String clientId : clientIds) {
			if (!isClientInAnyDIRECTORYGroup(clientId)) {
				group2Members.get(DIRECTORY_NOT_ASSIGNED_NAME).add(clientId);

				DefaultMutableTreeNode node = new DefaultMutableTreeNode(clientId, false);
				groupNodeDirectoryNotAssigned.add(node);

				clientNodesInDirectory.put(clientId, node);
			}
		}

		model.nodeStructureChanged(groupNodeDirectory);
	}

	@SuppressWarnings("java:S1168")
	private static Set<String> getAllowedClients(Map<String, Set<String>> group2Members,
			Set<String> permittedHostGroups) {
		if (permittedHostGroups == null) {
			// null means, all are allowed
			return null;
		}

		Set<String> result = new HashSet<>();
		for (String permittedGroup : permittedHostGroups) {
			if (group2Members.containsKey(permittedGroup)) {
				result.addAll(group2Members.get(permittedGroup));
			}
		}

		return result;
	}

	private boolean isClientInAnyDIRECTORYGroup(String clientId) {
		checkDirectory(clientId, null);
		Set<GroupNode> hostingGroups = locationsInDirectory.get(clientId);
		return !hostingGroups.isEmpty();
	}

	private boolean addObject2InternalGroup(String objectID, DefaultMutableTreeNode newGroupNode, TreePath newPath) {
		// child with this objectID not existing
		if (getChildWithUserObjectString(objectID, newGroupNode) == null) {
			Set<String> clientIds = new TreeSet<>();
			clientIds.add(objectID);

			// Must be a list and not a treeset because GroupNode is not comparable
			List<GroupNode> groups = new ArrayList<>();
			newGroupNode.children().asIterator().forEachRemaining((TreeNode node) -> {
				switch (node) {
				case GroupNode gn -> groups.add(gn);
				default -> clientIds.add(node.toString());
				}
			});

			newGroupNode.removeAllChildren();
			// Add all the groups alphabetically ordered
			groups.forEach(newGroupNode::add);
			produceClients(clientIds, newGroupNode);
			makeVisible(newPath.pathByAddingChild(objectID));
			return true;
		}

		return false;
	}

	@Override
	public void removeNodeInternally(String clientID, GroupNode parentNode) {
		Logging.debug("removeClientInternally clientId, parentNode ", clientID, ", ", parentNode);

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

		clientNodesInDirectory.remove(clientID);

		model.nodeStructureChanged(parentNode);

		repaint();
	}

	@Override
	public void moveObjectTo(String importID, String sourceParentID, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		DefaultMutableTreeNode existingNode = getChildWithUserObjectString(importID, dropParentNode);
		if (existingNode == null) {
			Logging.debug(this, "moveObjectTo sourcePath null, sourceParentNode ", sourceParentNode);

			DefaultMutableTreeNode clientNode = getChildWithUserObjectString(importID, sourceParentNode);
			insertNodeInOrder(clientNode, dropParentNode);
			model.nodeStructureChanged(sourceParentNode);

			if (!DIRECTORY_NOT_ASSIGNED_NAME.equals(dropParentID)) {
				persistenceController.getDataServices().group.addObject2Group(importID, dropParentID,
						Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			}

			// operations in DIRECTORY

			if (isInDirectory(dropPath)) {
				locationsInDirectory.get(importID).add(getGroupNode(dropParentID));
				locationsInDirectory.get(importID).remove(sourceParentNode);
			}

			activeParents.addAll(Arrays.stream(dropPath.getPath()).map(Object::toString).collect(Collectors.toSet()));

			Logging.debug(this, "moveObjectTo -- remove ", importID, " from ", sourceParentID,
					" clientNode, sourceParentNode ", clientNode, ", ", sourceParentNode);

			// persistent removal
			persistenceController.getDataServices().group.removeObject2Group(importID, sourceParentID);
			removeNodeInternally(importID, sourceParentNode);

			makeVisible(dropPath.pathByAddingChild(clientNode));
			repaint();

			checkDirectory(importID, (GroupNode) dropParentNode);
		}
	}

	@Override
	public void copyObjectTo(String objectID, String newParentID, DefaultMutableTreeNode newParentNode,
			TreePath newParentPath) {
		Logging.debug(this, " copying ", objectID, " into group ", newParentID);

		Logging.debug(this, " -- copyObjectTo childs are persistent, newParentNode ", newParentNode, " ",
				DIRECTORY_NOT_ASSIGNED_NAME.equals(newParentNode.toString()));

		boolean success = addObject2InternalGroup(objectID, newParentNode, newParentPath);

		if (success && !DIRECTORY_NOT_ASSIGNED_NAME.equals(newParentNode.toString())) {
			persistenceController.getDataServices().group.addObject2Group(objectID, newParentID,
					Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
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
				persistenceController.getDataServices().group.removeObject2Group(clientID,
						node.getUserObject().toString());
			}

			locationsInDirectory.put(clientID, new HashSet<>(correctNode));
		}
	}

	private static List<GroupNode> selectOneNode(Set<GroupNode> groupSet, String clientID, GroupNode preSelected) {
		List<GroupNode> result = null;

		//Ask only if mainFrame is not null; Otherwise, errors will occure
		if (groupSet.size() > 1 && ConfigedMain.getMainFrame() != null) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ClientTree.severalLocationsAssigned") + " " + clientID + ".\n"
							+ Configed.getResourceValue("ClientTree.selectCorrectLocation"));

			ListSelectionDialog dialog = new ListSelectionDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ClientTree.selectCorrectLocation"));
			dialog.setListData(groupSet.stream().map(Object::toString).toList());
			if (preSelected != null) {
				dialog.setPreviousSelectionValues(Set.of(preSelected.toString()));
			}

			// Repeat until the user has selected exactly one group
			do {
				dialog.show();
			} while (!dialog.wasAccepted() || dialog.getSelectedValues().size() != 1);

			result = groupSet.stream().filter(node -> dialog.getSelectedValues().contains(node.toString())).toList();
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

	@Override
	public Set<String> getSelectedObjectsInTable() {
		return configedMain.getClientTablePanel().getClientTable().getSelectedSet();
	}
}
