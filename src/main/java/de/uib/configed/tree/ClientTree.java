/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 * ClientTree.java
 *
 * Copyright (C) 2010-2019 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */
package de.uib.configed.tree;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
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
import de.uib.configed.Globals;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditList;
import de.uib.utilities.swing.FEditRecord;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.tree.SimpleTreePath;

public class ClientTree extends JTree implements TreeSelectionListener, MouseListener, KeyListener {

	ConfigedMain configedMain;

	protected DefaultTreeModel model;

	private GroupNode groupNodeAllClients;
	protected GroupNode groupNodeGroups;

	protected GroupNode groupNodeDirectory;
	protected GroupNode groupNodeDirectoryNotAssigned;

	public static final String ALL_CLIENTS_NAME;

	public static final String ALL_GROUPS_NAME;
	public static final String DIRECTORY_NAME;
	public static final String DIRECTORY_PERSISTENT_NAME;
	public static final String DIRECTORY_NOT_ASSIGNED_NAME;
	private static Map<String, String> translationsToPersistentNames;
	private static Set<String> topGroupNames;

	private static final Map<String, String> translationsFromPersistentNames;

	private boolean mouseClicked;

	static {
		ALL_CLIENTS_NAME = Configed.getResourceValue("ClientTree.ALLname");

		ALL_GROUPS_NAME = Configed.getResourceValue("ClientTree.GROUPSname");
		DIRECTORY_NAME = Configed.getResourceValue("ClientTree.DIRECTORYname");
		DIRECTORY_PERSISTENT_NAME = "clientdirectory";
		DIRECTORY_NOT_ASSIGNED_NAME = Configed.getResourceValue("ClientTree.NOTASSIGNEDname");
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

	public static Map<String, String> getTranslationsFromPersistentNames() {
		return translationsFromPersistentNames;
	}

	public static String translateToPersistentName(String name) {
		if (translationsToPersistentNames.get(name) != null) {
			return translationsToPersistentNames.get(name);
		}
		return name;
	}

	public static String translateFromPersistentName(String name) {
		if (translationsFromPersistentNames.get(name) != null) {
			return translationsFromPersistentNames.get(name);
		}
		return name;
	}

	class NodeComparator implements Comparator<DefaultMutableTreeNode> {
		final Collator myCollator = Collator.getInstance();

		NodeComparator() {
			myCollator.setStrength(Collator.IDENTICAL);
		}

		@Override
		public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
			return myCollator.compare("" + o1.getUserObject(), "" + o2.getUserObject());
		}
	}

	class TreeException extends Exception {
		TreeException(String s) {
			super(s);
		}
	}

	protected TreePath pathToROOT;
	protected TreePath pathToALL;
	protected TreePath pathToGROUPS;

	protected TreePath pathToDIRECTORY;
	protected TreePath pathToDirectoryNotAssigned;

	protected final Map<String, String> mapAllClients = new HashMap<>();
	protected final Map<String, String> mapGroups = new HashMap<>();
	protected final Map<String, String> mapDirectory = new HashMap<>();
	protected final Map<String, String> mapDirectoryNotAssigned = new HashMap<>();

	public final GroupNode rootNode = new GroupNode("top");

	protected IconNodeRendererClientTree nodeRenderer;

	// supervising data
	protected Map<String, Set<GroupNode>> locationsInDIRECTORY;
	// clientId --> set of all containing groups

	protected Leafname2AllItsPaths leafname2AllItsPaths;
	// clientId --> list of all paths that have the leaf clientid

	protected Map<String, Map<String, String>> groups;
	// map of all group maps,
	// groupid --> group map

	protected Map<String, GroupNode> groupNodes;
	// groupid --> group node
	// is a function since a group name cannot occur twice

	protected Map<String, DefaultMutableTreeNode> clientNodesInDIRECTORY;
	// clientid --> client node
	// is a function, when the directory has been cleared

	protected HashSet<String> activeParents = new HashSet<>();
	// groups containing clients (especially the selected ones)

	protected Map<String, HostInfo> host2HostInfo;

	protected Set<String> directlyAllowedGroups;

	class Leafname2AllItsPaths {
		Map<String, ArrayList<SimpleTreePath>> invertedSimpleClientPaths = new HashMap<>();

		Leafname2AllItsPaths() {
			invertedSimpleClientPaths = new HashMap<>();

		}

		public Set<String> keySet() {
			return invertedSimpleClientPaths.keySet();
		}

		public List<SimpleTreePath> get(String k) {
			return invertedSimpleClientPaths.get(k);
		}

		public void clear() {
			invertedSimpleClientPaths.clear();
		}

		public void rebuildFromTree(DefaultMutableTreeNode node) {
			clear();

			Enumeration<TreeNode> e = node.breadthFirstEnumeration();

			while (e.hasMoreElements()) {
				DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();

				if (!element.getAllowsChildren()) {
					String nodeinfo = (String) element.getUserObject();
					add(nodeinfo, new SimpleTreePath(element.getPath()));
				}
			}
		}

		public List<SimpleTreePath> getSimpleTreePaths(String leafname) {
			return invertedSimpleClientPaths.get(leafname);
		}

		public void add(String leafname, SimpleTreePath simpleTreePath) {
			invertedSimpleClientPaths.computeIfAbsent(leafname, arg -> new ArrayList<>()).add(simpleTreePath);
		}

		public void add(String leafname, TreePath clientPath) {

			add(leafname, new SimpleTreePath(clientPath.getPath()));
		}

		public void remove(String leafname, SimpleTreePath clientPath) {
			if (invertedSimpleClientPaths.get(leafname) != null) {
				invertedSimpleClientPaths.get(leafname).remove(clientPath);

			}
		}

		public void remove(String leafname, TreePath clientPath) {
			Logging.debug(this, "remove leafname, clientPath " + leafname + ", " + clientPath);

			if (invertedSimpleClientPaths.get(leafname) != null) {
				invertedSimpleClientPaths.get(leafname).remove(new SimpleTreePath(clientPath.getPath()));
			}
		}
	}

	public ClientTree(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;

		init();
	}

	private void init() {
		// do not expand tree nodes when clicking the node name, default is 2, meaning
		// double click expands
		setToggleClickCount(0);
		ToolTipManager.sharedInstance().registerComponent(this);
		if (!configedMain.treeViewAllowed()) {
			setEnabled(false);
			setToolTipText(Globals.wrapToHTML(Configed.getResourceValue("ConfigedMain.TreeViewNotActive")));
		}

		Logging.debug(this, "UI " + getUI());

		// preparing DnD
		TransferHandler handler = new ClientTreeTransferHandler(this);
		setTransferHandler(handler);
		setDragEnabled(true);

		// for debugging
		setDropMode(DropMode.ON);

		createTopNodes();

		setRootVisible(false);

		setShowsRootHandles(true);

		nodeRenderer = new IconNodeRendererClientTree(configedMain);
		setCellRenderer(nodeRenderer);

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

		addKeyListener(this);

		addTreeSelectionListener(this);
		addMouseListener(this);

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
		this.host2HostInfo = host2HostInfo;
	}

	// publishing the protected method
	@Override
	public TreePath[] getPathBetweenRows(int index0, int index1) {
		return super.getPathBetweenRows(index0, index1);
	}

	public TreePath pathByAddingChild(TreePath treePath, Object child) {
		TreePath result;

		if (child == null) {
			Logging.debug(this, "pathByAddingChild: child null cannot be added");
			return null;
		}

		result = treePath.pathByAddingChild(child);

		return result;
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

	// interface KeyListener

	@Override
	public void keyPressed(KeyEvent e) {
		mouseClicked = false;

		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			TreePath selectedPath = getSelectionPath();

			Logging.info(this, " selected path " + selectedPath);
			if (selectedPath != null) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

				if (selectedNode instanceof GroupNode) {
					configedMain.activateGroupByTree(false, selectedNode, selectedPath);
					configedMain.setGroup(selectedNode.toString());
				}

			}
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			configedMain.clearSelectionOnPanel();
		} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			// don't go backwards by this key
			e.consume();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	// interface TreeSelectionListener
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if (mouseClicked) {
			mouseClicked = false;
			return;
		}

		TreePath selectedPath = getSelectionPath();

		if (selectedPath != null && getSelectionRows().length == 1) {
			configedMain.treeClientsSelectAction(selectedPath);
		}
	}

	// interface MouseListener
	@Override
	public void mousePressed(final MouseEvent e) {
		Logging.debug(this, "mousePressed event " + e);

		mouseClicked = true;

		final java.awt.Cursor initialCursor = getCursor();
		final JTree theTree = this;
		getRowForLocation(e.getX(), e.getY());

		theTree.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
		configedMain.treeClientsMouseAction(true, e);
		theTree.setCursor(initialCursor);

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}

	private IconNode produceClientNode(Object x) {
		IconNode n = new IconNode(x, false);
		n.setEnabled(false);
		if (host2HostInfo != null && host2HostInfo.get(x) != null
				&& !host2HostInfo.get(x).getDescription().equals("")) {
			n.setToolTipText(host2HostInfo.get(x).getDescription());
		} else {
			n.setToolTipText(x.toString());
		}

		n.setIcon(Globals.createImageIcon("images/client_small.png", "client"));
		n.setNonSelectedLeafIcon(Globals.createImageIcon("images/client_small_unselected.png", "client"));
		n.setDisabledLeafIcon();

		return n;
	}

	private GroupNode produceGroupNode(Object x, String description) {
		GroupNode n = new GroupNode(x, description);
		n.setToolTipText(description);
		n.setEnabled(true);
		n.setIcon(Globals.createImageIcon("images/group_small.png", "group"));
		n.setClosedIcon(Globals.createImageIcon("images/group_small_unselected.png", "group unselected"));
		n.setEmphasizedIcon(Globals.createImageIcon("images/group_small_1selected.png", "group 1selected"));
		n.setDisabledLeafIcon();

		return n;

	}

	private GroupNode produceGroupNode(Map<String, String> group) {
		String description = group.get("description");
		if (description == null || description.trim().equals("")) {
			description = group.get("groupId");
		}

		return produceGroupNode(group.get("groupId"), description);
	}

	private void createDirectoryNotAssigned() {
		groupNodeDirectoryNotAssigned = produceGroupNode(DIRECTORY_NOT_ASSIGNED_NAME,

				Configed.getResourceValue("ClientTree.NOTASSIGNEDdescription"));

		groupNodeDirectoryNotAssigned.setAllowsSubGroups(false);
		groupNodeDirectoryNotAssigned.setFixed(true);
		groupNodeDirectoryNotAssigned.setChildsArePersistent(false);

		groupNodeDirectory.add(groupNodeDirectoryNotAssigned);

		pathToDirectoryNotAssigned = new TreePath(
				new Object[] { rootNode, groupNodeDirectory, groupNodeDirectoryNotAssigned });
	}

	// generate tree structure
	private void createTopNodes() {
		rootNode.setImmutable(true);
		rootNode.setFixed(true);

		pathToROOT = new TreePath(new Object[] { rootNode });

		// GROUPS
		groupNodeGroups = produceGroupNode(ALL_GROUPS_NAME,

				Configed.getResourceValue("ClientTree.GROUPSdescription"));
		groupNodeGroups.setAllowsOnlyGroupChilds(true);
		groupNodeGroups.setFixed(true);

		rootNode.add(groupNodeGroups);

		pathToGROUPS = new TreePath(new Object[] { rootNode, groupNodeGroups });

		// DIRECTORY
		groupNodeDirectory = produceGroupNode(DIRECTORY_NAME,

				Configed.getResourceValue("ClientTree.DIRECTORYdescription"));

		groupNodeDirectory.setAllowsOnlyGroupChilds(true);
		groupNodeDirectory.setFixed(true);

		rootNode.add(groupNodeDirectory);

		pathToDIRECTORY = new TreePath(new Object[] { rootNode, groupNodeDirectory });

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
					Globals.APPNAME + " " + Configed.getResourceValue("ClientTree.deleteGroupWarningTitle"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.OK_OPTION) {
				groupNodes.remove(nodeID);
				groups.remove(nodeID);

				deleteGroupWithSubgroups(node);
				parent.remove(node);

				getActivePaths().remove(path);
				getActivePaths().add(path.getParentPath());

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

	protected TreePath addClientNodeInfo(DefaultMutableTreeNode node) {
		TreePath clientPath = new TreePath(node.getPath());

		String clientId = (String) node.getUserObject();

		leafname2AllItsPaths.add(clientId, clientPath);

		return clientPath;
	}

	public void produceClients(Object[] x, DefaultMutableTreeNode parent) {
		produceClients(x, parent, false);
	}

	public void produceClients(Object[] x, DefaultMutableTreeNode parent, boolean register)
	// expects Strings as Objects
	{

		for (int i = 0; i < x.length; i++) {
			String clientId = (String) x[i];
			IconNode node = produceClientNode(clientId);
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

	protected void produceClients(Object[] x) {
		produceClients(x, groupNodeAllClients);
	}

	public void produceTreeForALL(Object[] x) {

		clientNodesInDIRECTORY.clear();
		produceClients(x);
	}

	protected void initTopGroups() {

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
		this.groups = new TreeMap<>(importedGroups);
		// we need a local copy since we add virtual groups

		createDirectoryNotAssigned();

		groupNodes = new HashMap<>();

		initTopGroups();

		// produce other nodes

		for (Entry<String, Map<String, String>> group : groups.entrySet()) {
			if (topGroupNames.contains(group.getKey())) {
				continue;
			}

			GroupNode node = produceGroupNode(group.getValue());
			groupNodes.put(group.getKey(), node);
		}

		// now we link them

		for (Entry<String, Map<String, String>> group : groups.entrySet()) {
			if (topGroupNames.contains(group.getKey()))
				continue;

			DefaultMutableTreeNode node = groupNodes.get(group.getKey());

			String parentId = group.getValue().get("parentGroupId");

			if (parentId == null || parentId.equalsIgnoreCase("null"))
				parentId = ALL_GROUPS_NAME;

			DefaultMutableTreeNode parent = null;

			if (groupNodes.get(parentId) == null)
				// group not existing
				parent = groupNodes.get(ALL_GROUPS_NAME);
			else
				parent = groupNodes.get(parentId);

			try {
				parent.add(node);
				model.nodesWereInserted(parent, new int[] { model.getIndexOfChild(parent, node) });

			} catch (IllegalArgumentException ex) {
				Logging.error(this, "Cannot add node to parent " + node + ", " + parent + ": " + ex, ex);
				JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("ClientTree.cannot_add_node.text") + " " + node + " in  " + parent
								+ "(" + ex + ")",
						Configed.getResourceValue("ClientTree.cannot_add_node.title"), JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	@Override
	public DefaultTreeModel getModel() {
		return model;
	}

	public Set<String> associateClientsToGroups(String[] x, Map<String, Set<String>> fObject2Groups,
			Set<String> permittedHostGroups) {
		locationsInDIRECTORY.clear();

		HashMap<String, List<String>> group2Members = new HashMap<>();

		// we must rebuild this map since the direct call of persist.getFGroup2Members
		// would eliminate
		// the filter by depot etc.

		for (String clientId : x) {
			if (fObject2Groups.get(clientId) != null) {
				Set<String> belongingTo = fObject2Groups.get(clientId);

				for (String groupId : belongingTo) {

					List<String> memberList = group2Members.get(groupId);

					if (memberList == null) {
						memberList = new ArrayList<>();
					}

					memberList.add(clientId);
					group2Members.put(groupId, memberList);
				}
			}
		}

		List<String> membersOfDirectoryNotAssigned = new ArrayList<>();
		group2Members.put(DIRECTORY_NOT_ASSIGNED_NAME, membersOfDirectoryNotAssigned);

		// we build and link the groups
		for (Entry<String, List<String>> entry : group2Members.entrySet()) {

			GroupNode groupNode = groupNodes.get(entry.getKey());
			if (groupNode == null) {
				Logging.warning("group for groupId " + entry.getKey() + " not found");

			}

			else {
				boolean register = isInDIRECTORY(groupNode);
				produceClients(entry.getValue().toArray(), groupNode, register);
			}

		}

		// check produced DIRECTORY
		for (int i = 0; i < x.length; i++) {
			String clientId = x[i];
			checkDIRECTORY(clientId, null);
		}

		// build membersOfDIRECTORY_NOT_ASSIGNED
		for (int i = 0; i < x.length; i++) {
			String clientId = x[i];
			Set<GroupNode> hostingGroups = locationsInDIRECTORY.get(clientId);

			// client is not in any DIRECTORY group
			if (hostingGroups.isEmpty()) {
				membersOfDirectoryNotAssigned.add(clientId);

				IconNode node = produceClientNode(clientId);
				groupNodeDirectoryNotAssigned.add(node);

				clientNodesInDIRECTORY.put(clientId, node);

				addClientNodeInfo(node);

				hostingGroups.add(groupNodeDirectoryNotAssigned);
			}
		}

		model.nodeStructureChanged(groupNodeDirectory);

		TreeSet<String> allowedClients = null;

		Logging.info(this, "associateClientsToGroups, evaluate permittedHostGroups " + permittedHostGroups);

		if (permittedHostGroups != null) {
			allowedClients = new TreeSet<>();

			if (directlyAllowedGroups == null) {
				directlyAllowedGroups = new TreeSet<>();
			}

			for (String clientId : leafname2AllItsPaths.keySet()) {
				for (SimpleTreePath path : leafname2AllItsPaths.get(clientId)) {

					Set<String> pathElements = new TreeSet<>(path);
					int allElementsNumber = pathElements.size();

					// retained are the elements not permitted
					pathElements.removeAll(permittedHostGroups);
					int notPermittedNumber = pathElements.size();

					if (notPermittedNumber < allElementsNumber) {

						allowedClients.add(clientId);
						directlyAllowedGroups.addAll(path);
					}
				}
			}

			directlyAllowedGroups.removeAll(allowedClients); // they were the last element in pathElements

		}

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
		}

		else {
			node = (DefaultMutableTreeNode) path.getLastPathComponent();
		}

		if (!node.getAllowsChildren()) {
			return;
		}

		String groupId = node.toString();

		Map<String, String> groupData = new LinkedHashMap<>();
		groupData.put("groupname", groupId);
		groupData.put("description", groups.get(groupId).get("description"));
		HashMap<String, String> labels = new HashMap<>();
		labels.put("groupname", Configed.getResourceValue("ClientTree.editNode.label.groupname"));
		labels.put("description", Configed.getResourceValue("ClientTree.editNode.label.description"));
		HashMap<String, Boolean> editable = new HashMap<>();
		editable.put("groupname", false);
		editable.put("description", true);

		FEditRecord fEdit = new FEditRecord(Configed.getResourceValue("ClientTree.editGroup"));
		fEdit.setRecord(groupData, labels, null, editable);
		fEdit.setTitle(Configed.getResourceValue("ClientTree.editNode.title") + " (" + Globals.APPNAME + ")");
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

	public IconNode makeSubgroupAt(TreePath path)
	// calls main controller for getting persistence for the new subgroup
	{

		IconNode result = null;

		DefaultMutableTreeNode node;

		if (path == null) {
			node = groupNodeGroups;
		}

		else {
			node = (DefaultMutableTreeNode) path.getLastPathComponent();
		}

		if (node.getAllowsChildren()) {
			if (!node.toString().equals(ALL_GROUPS_NAME) && configedMain.getOpsiVersion().compareTo("3.4.9") < 0) {
				JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
						"group in group not supported for opsiVersion < 3.4.9, \nopsiVersion is "
								+ configedMain.getOpsiVersion(),
						"opsi info ",

						// synchronous",

						JOptionPane.WARNING_MESSAGE);
				return result;
			}

			LinkedHashMap<String, String> groupData = new LinkedHashMap<>();
			groupData.put("groupname", "");
			groupData.put("description", "");
			HashMap<String, String> labels = new HashMap<>();
			labels.put("groupname", Configed.getResourceValue("ClientTree.editNode.label.groupname"));
			labels.put("description", Configed.getResourceValue("ClientTree.editNode.label.description"));
			HashMap<String, Boolean> editable = new HashMap<>();
			editable.put("groupname", true);
			editable.put("description", true);

			String newGroupKey = "";

			boolean gotName = !(newGroupKey.equals("")) && !(groups.keySet().contains(newGroupKey));

			String inscription = "";

			FEditRecord fEdit = new FEditRecord(inscription);
			fEdit.setRecord(groupData, labels, null, editable);
			fEdit.setTitle(Configed.getResourceValue("ClientTree.addNode.title") + " (" + Globals.APPNAME + ")");
			fEdit.init();
			fEdit.setSize(450, 250);
			fEdit.setLocationRelativeTo(ConfigedMain.getMainFrame());

			fEdit.setModal(true);

			while (!gotName) {
				if (newGroupKey.equals("")) {
					inscription = Configed.getResourceValue("ClientTree.requestGroup");
				}

				else if (groups.keySet().contains(newGroupKey)) {
					inscription = "'" + newGroupKey + "' "
							+ Configed.getResourceValue("ClientTree.requestNotExistingGroupName");
				}

				else {
					gotName = true;
				}

				fEdit.setHint(inscription);

				if (!gotName) {

					fEdit.setVisible(true);

					newGroupKey = fEdit.getData().get("groupname").toLowerCase();

					if (fEdit.isCancelled()) {
						return null;
					}
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
			produceClients(new String[] { objectID }, newGroupNode);
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

	public boolean removeClientNodes(List<DefaultMutableTreeNode> clientNodes) {
		List<Object2GroupEntry> groupEntries = new ArrayList<>();

		for (DefaultMutableTreeNode clientNode : clientNodes) {
			String clientId = (String) (clientNode.getUserObject());
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) clientNode.getParent();

			removeClientInternally(clientId, (GroupNode) parent);

			groupEntries.add(new Object2GroupEntry(null, clientId, parent.toString()));

		}

		return configedMain.removeHostGroupElements(groupEntries);
	}

	public void removeClientInternally(String clientID, GroupNode parentNode) {

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

			if (getGroupNode(dropParentID).getChildsArePersistent()) {
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
							+ sourcePath

			);

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
			String newParentID, Boolean moving) {

		Logging.debug(this, "clientCopyOrMoveTo moving " + moving);
		if ((moving != null && moving)) {
			moveClientTo(importID, sourcePath, sourceParentID, sourceParentNode, newParentNode, newParentPath,
					newParentID);
		} else {
			// including the case sourcePath == null, meaning import from other source
			copyClientTo(importID, sourcePath, newParentID, newParentNode, newParentPath);
		}
	}

	public void copyClientTo(String objectID, TreePath sourcePath, String newParentID,
			DefaultMutableTreeNode newParentNode, TreePath newParentPath) {
		Logging.debug(this, " copying " + objectID + ", sourcePath " + sourcePath + " into group " + newParentID);

		DefaultMutableTreeNode clientNode = null;

		if (sourcePath == null) {

			clientNode = getClientNode(objectID);
		} else
			clientNode = (DefaultMutableTreeNode) sourcePath.getLastPathComponent();

		Logging.debug(this, " -- copyClientTo childs are persistent, newParentNode " + newParentNode + " "
				+ ((GroupNode) newParentNode).getChildsArePersistent());

		boolean success = addObject2InternalGroup(objectID, newParentNode, newParentPath);
		if (success && ((GroupNode) newParentNode).getChildsArePersistent())
			addObject2PersistentGroup(objectID, newParentID);

		TreePath newPath = pathByAddingChild(newParentPath, clientNode);
		SimpleTreePath simplePath = new SimpleTreePath(newPath.getPath());
		leafname2AllItsPaths.add(objectID, newPath);
		activeParents.addAll(simplePath.collectNodeNames());

		// operations in DIRECTORY

		java.util.Set<GroupNode> groupsInDIRECTORY = locationsInDIRECTORY.get(objectID);

		// remove entry in NOT_ASSIGNED
		if (groupsInDIRECTORY.contains(groupNodeDirectoryNotAssigned) && groupsInDIRECTORY.size() > 1) {
			locationsInDIRECTORY.get(objectID).remove(groupNodeDirectoryNotAssigned);
			removeClientInternally(objectID, groupNodeDirectoryNotAssigned);
		}

		repaint();

		checkDIRECTORY(objectID, (GroupNode) newParentNode);

	}

	private void checkDIRECTORY(String clientID, GroupNode selectedNode) {
		java.util.Set<GroupNode> groupsInDIRECTORY = locationsInDIRECTORY.get(clientID);
		if (groupsInDIRECTORY == null) {
			groupsInDIRECTORY = new TreeSet<>(new NodeComparator());
			locationsInDIRECTORY.put(clientID, groupsInDIRECTORY);
		}

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

	private List<GroupNode> selectOneNode(java.util.Set<GroupNode> groupSet, String clientID, GroupNode preSelected) {
		List<GroupNode> result = null;

		if (groupSet.size() > 1) {

			FEditList fList = new FEditList(null);
			fList.setListModel(new DefaultComboBoxModel<>(groupSet.toArray()));
			fList.setTitle(Globals.APPNAME + ":  " + Configed.getResourceValue("ClientTree.DIRECTORYname") + " "
					+ Configed.getResourceValue("ClientTree.checkDIRECTORYAssignments"));
			fList.setExtraLabel(Configed.getResourceValue("ClientTree.severalLocationsAssigned") + " >> " + clientID
					+ " <<, " + Configed.getResourceValue("ClientTree.selectCorrectLocation"));
			fList.init(new java.awt.Dimension(640, 60));

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
						Globals.APPNAME + " " + Configed.getResourceValue("ClientTree.requestInformation"), -1,
						JOptionPane.WARNING_MESSAGE, null,
						new String[] { Configed.getResourceValue("yesOption"), Configed.getResourceValue("noOption") },
						Configed.getResourceValue("noOption"));

				if (returnedOption == 1 || returnedOption == JOptionPane.CLOSED_OPTION)
				// do it again
				{
					result = selectOneNode(groupSet, clientID, preSelected);
				}
			} else {
				result = fList.getSelectedList();
			}
		}

		return result;
	}

	public boolean isChildOfALL(DefaultMutableTreeNode node) {
		return (node.getParent() == groupNodeAllClients);
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

	public boolean isInDIRECTORY(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		return (path.length >= 2 && path[1] == groupNodeDirectory);
	}

	public boolean isInGROUPS(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		return (path.length >= 2 && path[1] == groupNodeGroups);
	}

	public void insertNodeInOrder(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {

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
		}

		else {
			int i = parent.getIndex(insertNode);
			parent.insert(node, i);
		}

		model.nodesWereInserted(parent, new int[] { model.getIndexOfChild(parent, node) });

	}

	protected GroupNode insertGroup(Object groupObject, DefaultMutableTreeNode parent) {
		return insertGroup(groupObject, groupObject.toString(), parent);
	}

	protected GroupNode insertGroup(Object groupObject, String groupDescription, DefaultMutableTreeNode parent) {
		String xGroupDescription = groupDescription;
		if (groupDescription == null || groupDescription.trim().equals("")) {
			xGroupDescription = groupObject.toString();
		}

		GroupNode node = produceGroupNode(groupObject, xGroupDescription);

		DefaultMutableTreeNode xParent = parent;
		if (parent == null) {
			xParent = groupNodeGroups;
		}

		insertNodeInOrder(node, xParent);

		return node;
	}

	public TreePath getPathToALL() {
		return pathToALL;
	}

	public TreePath getPathToGROUPS() {
		return pathToGROUPS;
	}

	public TreePath getPathToDIRECTORY() {
		return pathToDIRECTORY;
	}

	public List<TreePath> getActivePaths() {
		return configedMain.getActivePaths();
	}

	public TreePath getActiveTreePath(String id) {
		return configedMain.getActiveTreeNodes().get(id);
	}

	public void collectParentIDsFrom(DefaultMutableTreeNode node) {
		activeParents.addAll(collectParentIDs(node));

	}

	public void initActiveParents() {
		activeParents.clear();
	}

	public void produceActiveParents(String[] clients) {

		initActiveParents();

		for (int i = 0; i < clients.length; i++) {
			activeParents.addAll(collectParentIDs(clients[i]));
		}

		Logging.debug(this, "produceActiveParents activeParents " + activeParents);

		repaint();
	}

	private List<String> enumerateLeafNodes(DefaultMutableTreeNode node) {
		List<String> result = new ArrayList<>();

		Enumeration<TreeNode> e = node.breadthFirstEnumeration();

		while (e.hasMoreElements()) {
			DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();

			if (!element.getAllowsChildren()) {
				String nodeinfo = (String) element.getUserObject();
				result.add(nodeinfo);
			}
		}

		return result;
	}

	public NavigableSet<String> collectLeafs(DefaultMutableTreeNode node) {
		return new TreeSet<>(enumerateLeafNodes(node));
	}

	private Set<String> collectParentIDs(DefaultMutableTreeNode node) {
		String nodeID = (String) node.getUserObject();
		return collectParentIDs(nodeID);
	}

	public List<SimpleTreePath> getSimpleTreePaths(String leafname) {
		return leafname2AllItsPaths.getSimpleTreePaths(leafname);
	}

	public void remove(String leafname, SimpleTreePath clientPath) {
		leafname2AllItsPaths.remove(leafname, clientPath);
	}

	public Set<String> collectParentIDs(String nodeID) {

		HashSet<String> allParents = new HashSet<>();

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

	public boolean addObject2PersistentGroup(String objectId, String groupId) {
		return configedMain.addObject2Group(objectId, groupId);
	}

	public boolean removeObject2Group(String objectId, String groupId) {
		return configedMain.removeObject2Group(objectId, groupId);
	}

	public boolean addGroup(StringValuedRelationElement newGroup) {
		return configedMain.addGroup(newGroup);
	}

	public boolean updateGroup(String groupId, Map<String, String> groupInfo) {
		return configedMain.updateGroup(groupId, groupInfo);
	}

	public boolean deleteGroup(String groupId) {
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

	List<String> getSelectedClientsInTable() {
		return configedMain.getSelectedClientsInTable();
	}

	@Override
	public void paint(java.awt.Graphics g) {
		try {
			super.paint(g);
		} catch (java.lang.ClassCastException ex) {
			Logging.warning(this, "the ugly well known exception " + ex);
			WaitCursor.stopAll();
		}
	}

}
