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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DropMode;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditList;
import de.uib.utilities.swing.FEditRecord;
import de.uib.utilities.tree.SimpleTreePath;

public class ClientTree extends JTree implements TreeSelectionListener {
	public static final String ALL_GROUPS_NAME = Configed.getResourceValue("ClientTree.GROUPSname");
	public static final String DIRECTORY_NAME = Configed.getResourceValue("ClientTree.DIRECTORYname");
	public static final String DIRECTORY_PERSISTENT_NAME = "clientdirectory";
	public static final String DIRECTORY_NOT_ASSIGNED_NAME = Configed.getResourceValue("ClientTree.NOTASSIGNEDname");
	private static Map<String, String> translationsToPersistentNames;
	private static Set<String> topGroupNames;

	private static final Map<String, String> translationsFromPersistentNames;

	public static final String ALL_CLIENTS_NAME = Configed.getResourceValue("ClientTree.ALLname");

	private DefaultTreeModel model;

	private GroupNode groupNodeAllClients;
	private GroupNode groupNodeGroups;

	private GroupNode groupNodeDirectory;
	private GroupNode groupNodeDirectoryNotAssigned;

	private TreePath pathToROOT;
	private TreePath pathToALL;

	private final Map<String, String> mapAllClients = new HashMap<>();
	private final Map<String, String> mapGroups = new HashMap<>();
	private final Map<String, String> mapDirectory = new HashMap<>();
	private final Map<String, String> mapDirectoryNotAssigned = new HashMap<>();

	public final GroupNode rootNode = new GroupNode("top");

	// supervising data
	private Map<String, Set<GroupNode>> locationsInDIRECTORY;
	// clientId --> set of all containing groups

	private Leafname2AllItsPaths leafname2AllItsPaths;
	// clientId --> list of all paths that have the leaf clientid

	private Map<String, Map<String, String>> groups;
	// map of all group maps,
	// groupid --> group map

	private Map<String, GroupNode> groupNodes;
	// groupid --> group node
	// is a function since a group name cannot occur twice

	private Map<String, DefaultMutableTreeNode> clientNodesInDIRECTORY;
	// clientid --> client node
	// is a function, when the directory has been cleared

	private Set<String> activeParents = new HashSet<>();
	// groups containing clients (especially the selected ones)

	private ClientTreeRenderer renderer;

	private Set<String> directlyAllowedGroups;

	private ConfigedMain configedMain;

	static {
		translationsToPersistentNames = new HashMap<>();
		translationsFromPersistentNames = new HashMap<>();
		translationsToPersistentNames.put(DIRECTORY_NAME, DIRECTORY_PERSISTENT_NAME);
		translationsFromPersistentNames.put(DIRECTORY_PERSISTENT_NAME, DIRECTORY_NAME);

		topGroupNames = new HashSet<>();
		topGroupNames.add(ALL_CLIENTS_NAME);
		topGroupNames.add(ALL_GROUPS_NAME);
		topGroupNames.add(DIRECTORY_NAME);
		topGroupNames.add(DIRECTORY_NOT_ASSIGNED_NAME);
	}

	public ClientTree(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;

		init();
	}

	public static Map<String, String> getTranslationsFromPersistentNames() {
		return translationsFromPersistentNames;
	}

	public static String translateToPersistentName(String name) {
		if (translationsToPersistentNames.get(name) != null) {
			return translationsToPersistentNames.get(name);
		}
		return name;
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

	private void init() {
		ToolTipManager.sharedInstance().registerComponent(this);

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
					configedMain.setGroup(selPath.getLastPathComponent().toString());
				}
			}
		};

		addMouseListener(ml);

		// preparing Drag and Drop
		TransferHandler handler = new ClientTreeTransferHandler(this);
		setTransferHandler(handler);
		setDragEnabled(true);

		// for debugging
		setDropMode(DropMode.ON);

		createTopNodes();

		setRootVisible(false);
		setShowsRootHandles(true);

		renderer = new ClientTreeRenderer(configedMain);
		setCellRenderer(renderer);

		model = new DefaultTreeModel(rootNode);
		setModel(model);
		model.setAsksAllowsChildren(true);
		// If true, a node is a leaf node if it does not allow children.
		// (If it allows children, it is not a leaf node, even if no children are
		// present.)

		TreeSelectionModel selectionmodel = new DefaultTreeSelectionModel();

		// comment is not more valid:
		// not allowing discontigous multiselection, we build a similar behavior based
		// on activeTreeNodes
		// since otherwise we could not discriminate between open and select click

		selectionmodel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		setSelectionModel(selectionmodel);

		initTreePopup();

		addTreeSelectionListener(this);

		locationsInDIRECTORY = new HashMap<>();
		clientNodesInDIRECTORY = new HashMap<>();
		leafname2AllItsPaths = new Leafname2AllItsPaths();
	}

	private void initTreePopup() {
		// popups on nodes
		JPopupMenu popupMenu = new JPopupMenu();
		TreePopupMouseListener treePopupMouseListener = new TreePopupMouseListener(popupMenu, this, configedMain);
		addMouseListener(treePopupMouseListener);
	}

	public void setClientInfo(Map<String, HostInfo> host2HostInfo) {
		renderer.setHost2HostInfo(host2HostInfo);
	}

	// publishing the private method
	@Override
	public TreePath[] getPathBetweenRows(int index0, int index1) {
		return super.getPathBetweenRows(index0, index1);
	}

	public TreePath pathByAddingChild(TreePath treePath, Object child) {
		if (child == null) {
			Logging.debug(this, "pathByAddingChild: child null cannot be added");
			return null;
		}

		return treePath.pathByAddingChild(child);
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

	private static DefaultMutableTreeNode produceClientNode(Object x) {
		return new DefaultMutableTreeNode(x, false);
	}

	private static GroupNode produceGroupNode(Object x, String description) {
		GroupNode n = new GroupNode(x);
		n.setToolTipText(description);
		return n;
	}

	private void createDirectoryNotAssigned() {
		groupNodeDirectoryNotAssigned = produceGroupNode(DIRECTORY_NOT_ASSIGNED_NAME,
				Configed.getResourceValue("ClientTree.NOTASSIGNEDdescription"));

		groupNodeDirectoryNotAssigned.setAllowsSubGroups(false);
		groupNodeDirectoryNotAssigned.setFixed(true);

		groupNodeDirectory.add(groupNodeDirectoryNotAssigned);
	}

	// generate tree structure
	private void createTopNodes() {
		rootNode.setImmutable(true);
		rootNode.setFixed(true);

		pathToROOT = new TreePath(new Object[] { rootNode });

		// GROUPS
		groupNodeGroups = produceGroupNode(ALL_GROUPS_NAME, Configed.getResourceValue("ClientTree.GROUPSdescription"));
		groupNodeGroups.setAllowsOnlyGroupChilds(true);
		groupNodeGroups.setFixed(true);

		rootNode.add(groupNodeGroups);

		// DIRECTORY
		groupNodeDirectory = produceGroupNode(DIRECTORY_NAME,
				Configed.getResourceValue("ClientTree.DIRECTORYdescription"));

		groupNodeDirectory.setAllowsOnlyGroupChilds(true);
		groupNodeDirectory.setFixed(true);

		rootNode.add(groupNodeDirectory);

		// ALL
		groupNodeAllClients = produceGroupNode(ALL_CLIENTS_NAME,
				Configed.getResourceValue("ClientTree.ALLdescription"));

		rootNode.add(groupNodeAllClients);
		groupNodeAllClients.setImmutable(true);
		groupNodeAllClients.setFixed(true);

		pathToALL = new TreePath(new Object[] { rootNode, groupNodeAllClients });
	}

	public void clear() {
		// clear jtree model
		groupNodeAllClients.removeAllChildren();
		groupNodeDirectory.removeAllChildren();
		groupNodeGroups.removeAllChildren();

		model.nodeStructureChanged(groupNodeGroups);

		// clear supervising data
		clientNodesInDIRECTORY.clear();
		locationsInDIRECTORY.clear();
		leafname2AllItsPaths.clear();

		// it is not necessary to clear groups and groupnodes since they will be rebuilt
		// by produceAndLinkGroups
	}

	private void deleteGroupWithSubgroups(DefaultMutableTreeNode node) {
		Enumeration<TreeNode> e = node.depthFirstEnumeration();

		while (e.hasMoreElements()) {
			DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) e.nextElement();
			if (nextNode.getAllowsChildren()) {
				deleteGroup(nextNode.toString());
			}
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
			removeClientInternally(nodeID, parent);
			removeObject2Group(nodeID, parentID);
		}
		return true;
	}

	private void produceDIRECTORYinfo(TreePath clientPath, DefaultMutableTreeNode node) {
		if (isInDIRECTORY(clientPath)) {
			String nodeID = (String) node.getUserObject();
			locationsInDIRECTORY.computeIfAbsent(nodeID, arg -> new TreeSet<>(new NodeComparator()))
					.add((GroupNode) node.getParent());
		}
	}

	private TreePath addClientNodeInfo(DefaultMutableTreeNode node) {
		TreePath clientPath = new TreePath(node.getPath());
		String clientId = (String) node.getUserObject();

		leafname2AllItsPaths.add(clientId, clientPath);
		return clientPath;
	}

	private void produceClients(Collection<String> clientIds, DefaultMutableTreeNode parent) {
		produceClients(clientIds, parent, false);
	}

	private void produceClients(Collection<String> clientIds, DefaultMutableTreeNode parent, boolean register) {
		for (String clientId : clientIds) {
			DefaultMutableTreeNode node = produceClientNode(clientId);
			if (register) {
				clientNodesInDIRECTORY.put(clientId, node);
			}

			if (parent != null) {
				parent.add(node);
			} else {
				Logging.debug(this, "not added Node for " + clientId + " under " + parent);
			}

			TreePath clientPath = addClientNodeInfo(node);

			produceDIRECTORYinfo(clientPath, node);
		}

		model.nodeStructureChanged(parent);
	}

	public void produceTreeForALL(Collection<String> clientIds) {
		clientNodesInDIRECTORY.clear();
		produceClients(clientIds, groupNodeAllClients);
	}

	private void initTopGroups() {
		mapAllClients.put("groupId", ALL_CLIENTS_NAME);
		mapAllClients.put("description", "root of complete client listing");
		groupNodes.put(ALL_CLIENTS_NAME, groupNodeAllClients);

		groups.put(ALL_CLIENTS_NAME, mapAllClients);

		mapGroups.put("groupId", ALL_GROUPS_NAME);

		mapGroups.put("description", "root of groups");

		groupNodes.put(ALL_GROUPS_NAME, groupNodeGroups);

		groups.put(ALL_GROUPS_NAME, mapGroups);

		mapDirectory.put("groupId", DIRECTORY_NAME);

		mapDirectory.put("description", "root of directory");

		groupNodes.put(DIRECTORY_NAME, groupNodeDirectory);

		groups.put(DIRECTORY_NAME, mapDirectory);

		mapDirectoryNotAssigned.put("groupId", DIRECTORY_NOT_ASSIGNED_NAME);
		mapDirectoryNotAssigned.put("description", "root of DIRECTORY_NOT_ASSIGNED");

		groupNodes.put(DIRECTORY_NOT_ASSIGNED_NAME, groupNodeDirectoryNotAssigned);

		groups.put(DIRECTORY_NOT_ASSIGNED_NAME, mapDirectoryNotAssigned);
	}

	public boolean groupNodesExists() {
		return groupNodes != null;
	}

	// we produce all partial pathes that are defined by the persistent groups
	public void produceAndLinkGroups(final Map<String, Map<String, String>> importedGroups) {
		Logging.debug(this, "produceAndLinkGroups " + importedGroups.keySet());
		// we need a local copy since we add virtual groups
		this.groups = new TreeMap<>(importedGroups);

		createDirectoryNotAssigned();
		groupNodes = new HashMap<>();
		initTopGroups();

		produceGroupNodes();
		linkGroupNodes();
	}

	private void produceGroupNodes() {
		for (Entry<String, Map<String, String>> group : groups.entrySet()) {
			if (topGroupNames.contains(group.getKey())) {
				continue;
			}

			GroupNode node = produceGroupNode(group.getValue().get("groupId"), group.getValue().get("description"));
			groupNodes.put(group.getKey(), node);
		}
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

	@Override
	public DefaultTreeModel getModel() {
		return model;
	}

	public Set<String> associateClientsToGroups(Iterable<String> clientIds, Map<String, Set<String>> fObject2Groups,
			Set<String> permittedHostGroups) {
		locationsInDIRECTORY.clear();

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
				boolean register = isInDIRECTORY(groupNode);
				produceClients(entry.getValue(), groupNode, register);
			}
		}

		for (String clientId : clientIds) {
			if (!isClientInAnyDIRECTORYGroup(clientId)) {
				membersOfDirectoryNotAssigned.add(clientId);

				DefaultMutableTreeNode node = produceClientNode(clientId);
				groupNodeDirectoryNotAssigned.add(node);

				clientNodesInDIRECTORY.put(clientId, node);

				addClientNodeInfo(node);
			}
		}

		model.nodeStructureChanged(groupNodeDirectory);

		return getAllowedClients(permittedHostGroups);
	}

	private boolean isClientInAnyDIRECTORYGroup(String clientId) {
		checkDIRECTORY(clientId, null);
		Set<GroupNode> hostingGroups = locationsInDIRECTORY.get(clientId);
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

	private Set<String> getAllowedClients(Set<String> permittedHostGroups) {
		TreeSet<String> allowedClients = null;

		Logging.info(this, "associateClientsToGroups, evaluate permittedHostGroups " + permittedHostGroups);

		if (permittedHostGroups == null) {
			return allowedClients;
		}

		allowedClients = new TreeSet<>();

		if (directlyAllowedGroups == null) {
			directlyAllowedGroups = new TreeSet<>();
		}

		for (Entry<String, ArrayList<SimpleTreePath>> entry : leafname2AllItsPaths.entrySet()) {
			for (SimpleTreePath path : entry.getValue()) {
				// retained are the elements not permitted
				if (!Collections.disjoint(path, permittedHostGroups)) {
					allowedClients.add(entry.getKey());
					directlyAllowedGroups.addAll(path);
				}
			}
		}

		// they were the last element in pathElements
		directlyAllowedGroups.removeAll(allowedClients);

		Logging.info(this, "associateClientsToGroups allowed Groups " + directlyAllowedGroups);

		return allowedClients;
	}

	public Set<String> getDirectlyAllowedGroups() {
		return directlyAllowedGroups;
	}

	protected void editGroupNode(TreePath path) {
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
			groupNodes.get(groupId).setToolTipText(groupData.get("description"));
			updateGroup(groupId, groups.get(groupId));
		}
	}

	// calls main controller for getting persistence for the new subgroup
	public IconNode makeSubgroupAt(TreePath path) {
		IconNode result = null;

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
			if (addGroup(newGroup)) {
				groups.put(newGroupKey, newGroup);
				Logging.debug(this, "makeSubGroupAt newGroupKey, newGroup " + newGroupKey + ", " + newGroup);
				GroupNode newNode = insertGroup(newGroupKey, groupData.get("description"), node);
				groupNodes.put(newGroupKey, newNode);

				result = newNode;
			}
		}

		return result;
	}

	private boolean addObject2InternalGroup(String objectID, DefaultMutableTreeNode newGroupNode, TreePath newPath) {
		// child with this objectID not existing
		if (getChildWithUserObjectString(objectID, newGroupNode) == null) {
			produceClients(Collections.singleton(objectID), newGroupNode);
			makeVisible(pathByAddingChild(newPath, objectID));
			return true;
		}

		return false;
	}

	public void moveGroupTo(String importID, GroupNode groupNode, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		insertNodeInOrder(groupNode, dropParentNode);
		getModel().nodeStructureChanged(sourceParentNode);
		makeVisible(pathByAddingChild(dropPath, groupNode));

		Map<String, String> theGroup = getGroups().get(importID);
		theGroup.put("parentGroupId", dropParentID);
		updateGroup(importID, theGroup);

		leafname2AllItsPaths.rebuildFromTree(rootNode);
	}

	public boolean removeClientNodes(Iterable<DefaultMutableTreeNode> clientNodes) {
		List<Object2GroupEntry> groupEntries = new ArrayList<>();

		for (DefaultMutableTreeNode clientNode : clientNodes) {
			String clientId = (String) (clientNode.getUserObject());
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) clientNode.getParent();

			removeClientInternally(clientId, (GroupNode) parent);
			groupEntries.add(new Object2GroupEntry(clientId, parent.toString()));
		}

		return configedMain.removeHostGroupElements(groupEntries);
	}

	private void removeClientInternally(String clientID, GroupNode parentNode) {
		Logging.debug("removeClientInternally clientId, parentNode " + clientID + ", " + parentNode);

		DefaultMutableTreeNode clientNode = getChildWithUserObjectString(clientID, parentNode);

		int stopCounter = 0;

		while (clientNode != null && stopCounter <= clientNodesInDIRECTORY.size()) {
			parentNode.remove(clientNode);
			// with more than one clientNode we seem to get as many instances of one client
			// node supplied as there are clients altogether, why ever
			// as a hack we go into looping
			clientNode = getChildWithUserObjectString(clientID, parentNode);
			stopCounter++;
		}
		if (stopCounter > clientNodesInDIRECTORY.size()) {
			Logging.warning("removing client not successful but stopped because of reaching the repetition limit");
		}

		clientNodesInDIRECTORY.remove(clientID); // 11

		SimpleTreePath simplePathToClient = new SimpleTreePath(parentNode.getPath());
		simplePathToClient.add(clientID);

		leafname2AllItsPaths.remove(clientID, simplePathToClient); // 13
		activeParents.removeAll(simplePathToClient.collectNodeNames());

		getModel().nodeStructureChanged(parentNode);

		repaint();
	}

	private void moveClientTo(String importID, TreePath sourcePath, String sourceParentID, GroupNode sourceParentNode,

			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		DefaultMutableTreeNode existingNode = getChildWithUserObjectString(importID, dropParentNode);
		if (existingNode == null) {
			// we have not a node with this name in the target group
			if (sourcePath != null) {
				Logging.debug(this,
						"moveClientTo checked importID sourcePath.getLastPathComponent(); "
								+ sourcePath.getLastPathComponent() + " class "
								+ ((sourcePath.getLastPathComponent()).getClass()));
			} else {
				Logging.debug(this, "moveClientTo sourcePath null, sourceParentNode " + sourceParentNode);
			}

			DefaultMutableTreeNode clientNode = getChildWithUserObjectString(importID, sourceParentNode);
			insertNodeInOrder(clientNode, dropParentNode);
			getModel().nodeStructureChanged(sourceParentNode);

			if (DIRECTORY_NOT_ASSIGNED_NAME.equals(dropParentID)) {
				addObject2PersistentGroup(importID, dropParentID);
			}

			// operations in DIRECTORY

			if (isInDIRECTORY(dropPath)) {
				locationsInDIRECTORY.get(importID).add(getGroupNode(dropParentID));
				locationsInDIRECTORY.get(importID).remove(sourceParentNode);
			}

			TreePath newPath = pathByAddingChild(dropPath, clientNode);
			SimpleTreePath simplePath = new SimpleTreePath(dropPath.getPath());
			leafname2AllItsPaths.add(importID, newPath);
			activeParents.addAll(simplePath.collectNodeNames());

			Logging.debug(this,
					"moveClientTo -- remove " + importID + " from " + sourceParentID
							+ " clientNode, sourceParentNode, sourcePath " + clientNode + ", " + sourceParentNode + ", "
							+ sourcePath);

			// persistent removal
			removeObject2Group(importID, sourceParentID);
			removeClientInternally(importID, sourceParentNode);

			makeVisible(newPath);
			repaint();

			checkDIRECTORY(importID, (GroupNode) dropParentNode);
		}
	}

	public void clientCopyOrMoveTo(String importID, TreePath sourcePath, String sourceParentID,
			GroupNode sourceParentNode, DefaultMutableTreeNode newParentNode, TreePath newParentPath,
			String newParentID, boolean moving) {
		Logging.debug(this, "clientCopyOrMoveTo moving " + moving);
		if (moving) {
			moveClientTo(importID, sourcePath, sourceParentID, sourceParentNode, newParentNode, newParentPath,
					newParentID);
		} else {
			// including the case sourcePath == null, meaning import from other source
			copyClientTo(importID, sourcePath, newParentID, newParentNode, newParentPath);
		}
	}

	private void copyClientTo(String objectID, TreePath sourcePath, String newParentID,
			DefaultMutableTreeNode newParentNode, TreePath newParentPath) {
		Logging.debug(this, " copying " + objectID + ", sourcePath " + sourcePath + " into group " + newParentID);

		DefaultMutableTreeNode clientNode = null;

		if (sourcePath == null) {
			clientNode = getClientNode(objectID);
		} else {
			clientNode = (DefaultMutableTreeNode) sourcePath.getLastPathComponent();
		}

		Logging.debug(this, " -- copyClientTo childs are persistent, newParentNode " + newParentNode + " "
				+ DIRECTORY_NOT_ASSIGNED_NAME.equals(newParentNode.toString()));

		boolean success = addObject2InternalGroup(objectID, newParentNode, newParentPath);

		if (success && !DIRECTORY_NOT_ASSIGNED_NAME.equals(newParentNode.toString())) {
			addObject2PersistentGroup(objectID, newParentID);
		}

		TreePath newPath = pathByAddingChild(newParentPath, clientNode);
		SimpleTreePath simplePath = new SimpleTreePath(newPath.getPath());
		leafname2AllItsPaths.add(objectID, newPath);
		activeParents.addAll(simplePath.collectNodeNames());

		// operations in DIRECTORY

		Set<GroupNode> groupsInDIRECTORY = locationsInDIRECTORY.get(objectID);

		// remove entry in NOT_ASSIGNED
		if (groupsInDIRECTORY.contains(groupNodeDirectoryNotAssigned) && groupsInDIRECTORY.size() > 1) {
			locationsInDIRECTORY.get(objectID).remove(groupNodeDirectoryNotAssigned);
			removeClientInternally(objectID, groupNodeDirectoryNotAssigned);
		}

		repaint();

		checkDIRECTORY(objectID, (GroupNode) newParentNode);
	}

	private void checkDIRECTORY(String clientID, GroupNode selectedNode) {
		Set<GroupNode> groupsInDIRECTORY = locationsInDIRECTORY.computeIfAbsent(clientID,
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
				removeClientInternally(clientID, node);
				removeObject2Group(clientID, node.getUserObject().toString());
			}

			locationsInDIRECTORY.put(clientID, new HashSet<>(correctNode));
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

	public boolean isChildOfALL(TreeNode node) {
		return node.getParent() == groupNodeAllClients;
	}

	public boolean isInGROUPS(String groupName) {
		GroupNode node = groupNodes.get(groupName);
		if (node == null) {
			return false;
		}

		return isInGROUPS(node);
	}

	public boolean isInDIRECTORY(String groupName) {
		GroupNode node = groupNodes.get(groupName);
		if (node == null) {
			return false;
		}

		return isInDIRECTORY(node);
	}

	public boolean isInGROUPS(TreePath path) {
		return path.getPathCount() >= 2 && path.getPathComponent(1) == groupNodeGroups;
	}

	public boolean isInDIRECTORY(TreePath path) {
		return path.getPathCount() >= 2 && path.getPathComponent(1) == groupNodeDirectory;
	}

	private boolean isInDIRECTORY(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		return path.length >= 2 && path[1] == groupNodeDirectory;
	}

	private boolean isInGROUPS(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		return path.length >= 2 && path[1] == groupNodeGroups;
	}

	private void insertNodeInOrder(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {
		if (node == null || parent == null) {
			return;
		}

		String nodeObject = node.getUserObject().toString();

		boolean foundLoc = false;

		Enumeration<TreeNode> en = parent.children();

		// for groups, we should look only for groups

		DefaultMutableTreeNode insertNode = null;
		while (en.hasMoreElements() && !foundLoc) {
			insertNode = (DefaultMutableTreeNode) en.nextElement();

			// node with subnodes = group
			if (insertNode.getAllowsChildren() && !node.getAllowsChildren()) {
				// leaf
				continue;
			}

			// leaf && group
			if (!insertNode.getAllowsChildren() && node.getAllowsChildren()) {
				foundLoc = true;
				continue;
			}

			// both are leafs or both are groups
			if (insertNode.toString().compareToIgnoreCase(nodeObject) > 0) {
				foundLoc = true;
			}
		}

		if (insertNode == null || !foundLoc) {
			// append
			parent.add(node);
		} else {
			int i = parent.getIndex(insertNode);
			parent.insert(node, i);
		}

		model.nodesWereInserted(parent, new int[] { model.getIndexOfChild(parent, node) });
	}

	private GroupNode insertGroup(Object groupObject, String groupDescription, DefaultMutableTreeNode parent) {
		GroupNode node = produceGroupNode(groupObject, groupDescription);

		if (parent == null) {
			parent = groupNodeGroups;
		}

		insertNodeInOrder(node, parent);

		return node;
	}

	public TreePath getPathToALL() {
		return pathToALL;
	}

	public TreePath getActiveTreePath(String id) {
		return Arrays.stream(getSelectionPaths()).filter(
				treePath -> ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject().equals(id))
				.findAny().orElse(null);
	}

	public void collectParentIDsFrom(DefaultMutableTreeNode node) {
		activeParents.addAll(collectParentIDs(node));
	}

	public void initActiveParents() {
		activeParents.clear();
	}

	public void produceActiveParents(Iterable<String> clientIds) {
		initActiveParents();

		for (String clientId : clientIds) {
			activeParents.addAll(collectParentIDs(clientId));
		}

		Logging.debug(this, "produceActiveParents activeParents " + activeParents);

		repaint();
	}

	private Set<String> collectParentIDs(DefaultMutableTreeNode node) {
		String nodeID = (String) node.getUserObject();
		return collectParentIDs(nodeID);
	}

	private List<SimpleTreePath> getSimpleTreePaths(String leafname) {
		return leafname2AllItsPaths.getSimpleTreePaths(leafname);
	}

	public Set<String> collectParentIDs(String nodeID) {
		Set<String> allParents = new HashSet<>();

		List<SimpleTreePath> treePaths = getSimpleTreePaths(nodeID);

		if (treePaths != null) {
			for (SimpleTreePath path : treePaths) {
				allParents.addAll(path.collectNodeNames());
			}
		}

		return allParents;
	}

	public Set<String> getActiveParents() {
		if (activeParents == null) {
			initActiveParents();
		}

		return activeParents;
	}

	public TreePath getGroupPathActivatedByTree() {
		return configedMain.getGroupPathActivatedByTree();
	}

	private boolean addObject2PersistentGroup(String objectId, String groupId) {
		return configedMain.addObject2Group(objectId, groupId);
	}

	private boolean removeObject2Group(String objectId, String groupId) {
		return configedMain.removeObject2Group(objectId, groupId);
	}

	private boolean addGroup(StringValuedRelationElement newGroup) {
		return configedMain.addGroup(newGroup);
	}

	private boolean updateGroup(String groupId, Map<String, String> groupInfo) {
		return configedMain.updateGroup(groupId, groupInfo);
	}

	private boolean deleteGroup(String groupId) {
		return configedMain.deleteGroup(groupId);
	}

	public GroupNode getGroupNode(String groupId) {
		return groupNodes.get(groupId);
	}

	public Map<String, Map<String, String>> getGroups() {
		return groups;
	}

	public DefaultMutableTreeNode getClientNode(String clientId) {
		return clientNodesInDIRECTORY.get(clientId);
	}

	public Set<GroupNode> getLocationsInDIRECTORY(String clientId) {
		return locationsInDIRECTORY.get(clientId);
	}

	@SuppressWarnings("java:S3242")
	public DefaultMutableTreeNode getChildWithUserObjectString(String objectID, DefaultMutableTreeNode groupNode) {
		Enumeration<TreeNode> enumer = groupNode.children();
		DefaultMutableTreeNode result = null;

		boolean foundAny = false;
		while (!foundAny && enumer.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumer.nextElement();

			if (child.getUserObject().toString().equals(objectID)) {
				foundAny = true;
				result = child;
			}
		}

		return result;
	}

	public List<String> getSelectedClientsInTable() {
		return configedMain.getSelectedClientsInTable();
	}
}
