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
import java.awt.event.MouseMotionListener;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DropMode;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.FEditList;
import de.uib.utilities.swing.FEditRecord;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.tree.SimpleTreePath;

public class ClientTree extends JTree implements TreeSelectionListener, MouseListener, MouseMotionListener, // for debugging
		TreeModelListener, KeyListener

{

	ConfigedMain main;

	protected DefaultTreeModel model;
	protected TreeSelectionModel selectionmodel;

	public GroupNode ALL;
	protected GroupNode GROUPS;

	protected GroupNode DIRECTORY;
	protected GroupNode DIRECTORY_NOT_ASSIGNED;

	public static String ALL_NAME;

	public static String GROUPS_NAME;
	public static String DIRECTORY_NAME;
	public static String DIRECTORY_PERSISTENT_NAME;
	public static String DIRECTORY_NOT_ASSIGNED_NAME;
	public static Map<String, String> translationsToPersistentNames;
	public static Map<String, String> translationsFromPersistentNames;
	public static java.util.Set<String> topGroupNames;

	static {
		ALL_NAME = configed.getResourceValue("ClientTree.ALLname");

		GROUPS_NAME = configed.getResourceValue("ClientTree.GROUPSname");
		DIRECTORY_NAME = configed.getResourceValue("ClientTree.DIRECTORYname");
		DIRECTORY_PERSISTENT_NAME = "clientdirectory";
		DIRECTORY_NOT_ASSIGNED_NAME = configed.getResourceValue("ClientTree.NOTASSIGNEDname");
		translationsToPersistentNames = new HashMap<>();
		translationsFromPersistentNames = new HashMap<>();
		translationsToPersistentNames.put(DIRECTORY_NAME, DIRECTORY_PERSISTENT_NAME);
		translationsFromPersistentNames.put(DIRECTORY_PERSISTENT_NAME, DIRECTORY_NAME);
		topGroupNames = new HashSet<>();
		{
			topGroupNames.add(ALL_NAME);
			topGroupNames.add(GROUPS_NAME);
			topGroupNames.add(DIRECTORY_NAME);
			topGroupNames.add(DIRECTORY_NOT_ASSIGNED_NAME);
		}
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
	protected TreePath pathToDIRECTORY_NOT_ASSIGNED;

	protected final Map<String, String> groupALL = new HashMap<>();
	protected final Map<String, String> groupGROUPS = new HashMap<>();
	protected final Map<String, String> groupDIRECTORY = new HashMap<>();
	protected final Map<String, String> groupDIRECTORY_NOT_ASSIGNED = new HashMap<>();

	public final GroupNode ROOT = new GroupNode("top");

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

		public ArrayList<SimpleTreePath> get(String k) {
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

		public ArrayList<SimpleTreePath> getSimpleTreePaths(String leafname) {
			return invertedSimpleClientPaths.get(leafname);
		}

		public void add(String leafname, SimpleTreePath simpleTreePath) {

			if (invertedSimpleClientPaths.get(leafname) == null)
				invertedSimpleClientPaths.put(leafname, new ArrayList<>());

			invertedSimpleClientPaths.get(leafname).add(simpleTreePath);

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
			logging.debug(this, "remove leafname, clientPath " + leafname + ", " + clientPath);

			if (invertedSimpleClientPaths.get(leafname) != null) {
				invertedSimpleClientPaths.get(leafname).remove(new SimpleTreePath(clientPath.getPath()));
			}
		}
	}

	JPopupMenu popupMenu;
	TreePopupMouseListener treePopupMouseListener;

	public ClientTree(ConfigedMain configMain) {
		super();
		setToggleClickCount(0); // do not expand tree nodes when clicking the node name, default is 2, meaning
								// double click expands
		ToolTipManager.sharedInstance().registerComponent(this);
		main = configMain;
		if (!main.treeViewAllowed()) {
			setEnabled(false);
			setToolTipText(Globals.wrapToHTML(configed.getResourceValue("ConfigedMain.TreeViewNotActive")));
		}

		logging.debug(this, "UI " + getUI());

		// preparing DnD
		TransferHandler handler = new ClientTreeTransferHandler(this);
		setTransferHandler(handler);
		setDragEnabled(true);
		setDropMode(DropMode.ON); // on existing components

		createTopNodes();

		setRootVisible(false);

		setShowsRootHandles(true);

		nodeRenderer = new IconNodeRendererClientTree(main);
		setCellRenderer(nodeRenderer);

		model = new DefaultTreeModel(ROOT);
		setModel(model);
		model.setAsksAllowsChildren(true);
		// If true, a node is a leaf node if it does not allow children.
		// (If it allows children, it is not a leaf node, even if no children are
		// present.)

		model.addTreeModelListener(this);

		selectionmodel = new DefaultTreeSelectionModel();

		// comment is not more valid:
		// not allowing discontigous multiselection, we build a similar behavior based
		// on activeTreeNodes
		// since otherwise we could not discriminate between open and select click

		selectionmodel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		setSelectionModel(selectionmodel);

		addKeyListener(this);

		// popups on nodes
		popupMenu = new JPopupMenu();
		treePopupMouseListener = new TreePopupMouseListener(popupMenu, this, null);
		addMouseListener(treePopupMouseListener);

		JMenuItem menuItemCreateNode = new JMenuItem(configed.getResourceValue("ClientTree.addNode"));

		TreePopupMouseListener.createSubnodePosition = 0;

		menuItemCreateNode.addActionListener(actionEvent -> {
			IconNode resultNode = makeSubgroupAt(treePopupMouseListener.getPopupSourcePath());
			if (resultNode != null) {
				makeVisible(pathByAddingChild(treePopupMouseListener.getPopupSourcePath(), resultNode));
				repaint();
			}
		});
		popupMenu.add(menuItemCreateNode);

		JMenuItem menuItemEditNode = new JMenuItem(configed.getResourceValue("ClientTree.editNode"));

		TreePopupMouseListener.editNodePosition = 1;

		menuItemEditNode.addActionListener(actionEvent -> editGroupNode(treePopupMouseListener.getPopupSourcePath()));
		popupMenu.add(menuItemEditNode);

		JMenuItem menuItemDeleteNode = new JMenuItem(configed.getResourceValue("ClientTree.deleteNode"));

		TreePopupMouseListener.deleteNodePosition = 2;

		menuItemDeleteNode.addActionListener(actionEvent -> deleteNode(treePopupMouseListener.getPopupSourcePath()));
		popupMenu.add(menuItemDeleteNode);

		JMenuItem menuItemDeleteGroupNode = new JMenuItem(configed.getResourceValue("ClientTree.deleteGroupNode"));

		TreePopupMouseListener.deleteGroupNodePosition = 3;

		menuItemDeleteGroupNode
				.addActionListener(actionEvent -> deleteNode(treePopupMouseListener.getPopupSourcePath()));
		popupMenu.add(menuItemDeleteGroupNode);

		JMenuItem menuItemActivateElements = new JMenuItem(configed.getResourceValue("ClientTree.selectAllElements"));

		TreePopupMouseListener.activateElementsPosition = 4;

		menuItemActivateElements.addActionListener(actionEvent -> {

			TreePath sourcePath = treePopupMouseListener.getPopupSourcePath();
			if (sourcePath != null && sourcePath.getPathComponent(sourcePath.getPathCount() - 1) instanceof GroupNode) {

				GroupNode node = (GroupNode) sourcePath.getPathComponent(sourcePath.getPathCount() - 1);
				main.setGroup(node.toString());

			}
		});
		popupMenu.add(menuItemActivateElements);

		JMenuItem menuItemRemoveElements = new JMenuItem(configed.getResourceValue("ClientTree.removeAllElements"));

		TreePopupMouseListener.removeElementsPosition = 5;

		menuItemRemoveElements.addActionListener(actionEvent -> {

			TreePath sourcePath = treePopupMouseListener.getPopupSourcePath();
			if (sourcePath != null && sourcePath.getPathComponent(sourcePath.getPathCount() - 1) instanceof GroupNode) {

				GroupNode node = (GroupNode) sourcePath.getPathComponent(sourcePath.getPathCount() - 1);

				Enumeration<TreeNode> enumer = node.breadthFirstEnumeration();

				List<DefaultMutableTreeNode> clientNodesToRemove = new ArrayList<>();

				while (enumer.hasMoreElements()) {
					DefaultMutableTreeNode element = (DefaultMutableTreeNode) enumer.nextElement();
					if (!element.getAllowsChildren())
						clientNodesToRemove.add(element);
				}

				if (removeClientNodes(clientNodesToRemove)) {
					main.setGroup(node.toString()); // refresh internal view
				}
			}
		});
		popupMenu.add(menuItemRemoveElements);

		addTreeSelectionListener(this);
		addMouseListener(this);

		locationsInDIRECTORY = new HashMap<>();
		clientNodesInDIRECTORY = new HashMap<>();
		leafname2AllItsPaths = new Leafname2AllItsPaths();

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
			logging.debug(this, "pathByAddingChild: child null cannot be added");
			return null;
		}

		result = treePath.pathByAddingChild(child);

		return result;
	}

	public TreePath getPathToNode(DefaultMutableTreeNode node) {
		if (node == null)
			return null;

		TreeNode[] ancestors = node.getPath();
		TreePath path = pathToROOT;

		for (int i = 1; i < ancestors.length; i++) {

			path = path.pathByAddingChild(ancestors[i]);
		}

		return path;

	}

	// =====
	// interface KeyListener

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			TreePath selectedPath = getSelectionPath();

			logging.info(this, " selected path " + selectedPath);
			if (selectedPath != null) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

				if (selectedNode instanceof GroupNode) {
					main.activateGroupByTree(false, selectedNode, selectedPath);
					main.setGroup(selectedNode.toString());
				}

			}
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			main.clearSelectionOnPanel();
		}

		else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			// don't go backwards by this key
			e.consume();
		}

	}

	// ======================
	// interface TreeSelectionListener
	@Override
	public void valueChanged(TreeSelectionEvent e) {

		TreePath selectedPath = getSelectionPath();

		if (selectedPath != null && getSelectionRows().length == 1) {
			main.treeClients_selectAction(selectedPath);

		}

	}

	// ======================
	// TreeModelListener
	@Override
	public void treeNodesChanged(TreeModelEvent e) {

	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {

	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {

	}
	// ======================

	// ======================
	// interface MouseMotionListener
	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}
	// ======================

	// ======================

	boolean mouse_ready = true;

	// interface MouseListener
	@Override
	public void mousePressed(final MouseEvent e) {
		logging.debug(this, "mousePressed event " + e);

		if (!mouse_ready) {
			// only occures if we start a thread for the reactions

			return;
		}

		mouse_ready = true;

		final java.awt.Cursor initialCursor = getCursor();
		final JTree theTree = this;
		getRowForLocation(e.getX(), e.getY());

		theTree.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
		main.treeClients_mouseAction(true, e);
		theTree.setCursor(initialCursor);

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	// ======================

	private IconNode produceClientNode(Object x) {
		IconNode n = new IconNode(x, false);
		n.setEnabled(false);
		if (host2HostInfo != null && host2HostInfo.get(x) != null && !host2HostInfo.get(x).getDescription().equals(""))
			n.setToolTipText(host2HostInfo.get(x).getDescription());
		else
			n.setToolTipText(x.toString());
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
		if (description == null || description.trim().equals(""))
			description = group.get("groupId");
		return produceGroupNode(group.get("groupId"), description);
	}

	private void createDIRECTORY_NOT_ASSIGNED() {
		DIRECTORY_NOT_ASSIGNED = produceGroupNode(DIRECTORY_NOT_ASSIGNED_NAME,

				configed.getResourceValue("ClientTree.NOTASSIGNEDdescription"));

		DIRECTORY_NOT_ASSIGNED.setAllowsSubGroups(false);
		DIRECTORY_NOT_ASSIGNED.setFixed(true);
		DIRECTORY_NOT_ASSIGNED.setChildsArePersistent(false);

		DIRECTORY.add(DIRECTORY_NOT_ASSIGNED);

		pathToDIRECTORY_NOT_ASSIGNED = new TreePath(new Object[] { ROOT, DIRECTORY, DIRECTORY_NOT_ASSIGNED });
	}

	// generate tree structure
	private void createTopNodes() {
		ROOT.setImmutable(true);
		ROOT.setFixed(true);

		pathToROOT = new TreePath(new Object[] { ROOT });

		// GROUPS
		GROUPS = produceGroupNode(GROUPS_NAME,

				configed.getResourceValue("ClientTree.GROUPSdescription"));
		GROUPS.setAllowsOnlyGroupChilds(true);
		GROUPS.setFixed(true);

		ROOT.add(GROUPS);

		pathToGROUPS = new TreePath(new Object[] { ROOT, GROUPS });

		// DIRECTORY
		DIRECTORY = produceGroupNode(DIRECTORY_NAME,

				configed.getResourceValue("ClientTree.DIRECTORYdescription"));

		DIRECTORY.setAllowsOnlyGroupChilds(true);
		DIRECTORY.setFixed(true);

		ROOT.add(DIRECTORY);

		pathToDIRECTORY = new TreePath(new Object[] { ROOT, DIRECTORY });

		// ALL
		ALL = produceGroupNode(ALL_NAME,

				configed.getResourceValue("ClientTree.ALLdescription"));

		ROOT.add(ALL);
		ALL.setImmutable(true);
		ALL.setFixed(true);

		pathToALL = new TreePath(new Object[] { ROOT, ALL });

	}

	public void clear() {
		// clear jtree model
		ALL.removeAllChildren();
		DIRECTORY.removeAllChildren();
		GROUPS.removeAllChildren();

		model.nodeStructureChanged(GROUPS);

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

		if (path == null)
			return false;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

		String nodeID = (String) node.getUserObject();

		GroupNode parent = (GroupNode) node.getParent();

		if (groupNodes.get(nodeID) != null && groupNodes.get(nodeID).getParent() != parent) {
			logging.warning(this, "groupNodes.get(nodeID).getParent() != parent");
			parent = (GroupNode) groupNodes.get(nodeID).getParent();
		}

		String parentID = (String) parent.getUserObject();

		if (groupNodes.get(nodeID) != null) {

			// found a group
			int returnedOption = JOptionPane.showOptionDialog(Globals.mainContainer,
					configed.getResourceValue("ClientTree.deleteGroupWarning"),
					Globals.APPNAME + " " + configed.getResourceValue("ClientTree.deleteGroupWarningTitle"),
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
			if (locationsInDIRECTORY.get(nodeID) == null) {
				locationsInDIRECTORY.put(nodeID, new TreeSet<>(new NodeComparator()));
			}

			java.util.Set<GroupNode> hostingGroups = locationsInDIRECTORY.get(nodeID);
			hostingGroups.add((GroupNode) node.getParent());
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
			if (register)
				clientNodesInDIRECTORY.put(clientId, node);

			if (parent != null) {
				parent.add(node);

			} else
				logging.debug(this, "not added Node for " + clientId + " under " + parent);

			TreePath clientPath = addClientNodeInfo(node);

			produceDIRECTORYinfo(clientPath, node);
		}

		model.nodeStructureChanged(parent);

	}

	protected void produceClients(Object[] x) {
		produceClients(x, ALL);
	}

	public void produceTreeForALL(Object[] x) {

		clientNodesInDIRECTORY.clear();
		produceClients(x);
	}

	protected void initTopGroups() {

		groupALL.put("groupId", ALL_NAME);
		groupALL.put("description", "root of complete client listing");
		groupNodes.put(ALL_NAME, ALL);

		groups.put(ALL_NAME, groupALL);

		groupGROUPS.put("groupId", GROUPS_NAME);

		groupGROUPS.put("description", "root of groups");

		groupNodes.put(GROUPS_NAME, GROUPS);

		groups.put(GROUPS_NAME, groupGROUPS);

		groupDIRECTORY.put("groupId", DIRECTORY_NAME);

		groupDIRECTORY.put("description", "root of directory");

		groupNodes.put(DIRECTORY_NAME, DIRECTORY);

		groups.put(DIRECTORY_NAME, groupDIRECTORY);

		groupDIRECTORY_NOT_ASSIGNED.put("groupId", DIRECTORY_NOT_ASSIGNED_NAME);
		groupDIRECTORY_NOT_ASSIGNED.put("description", "root of DIRECTORY_NOT_ASSIGNED");

		groupNodes.put(DIRECTORY_NOT_ASSIGNED_NAME, DIRECTORY_NOT_ASSIGNED);

		groups.put(DIRECTORY_NOT_ASSIGNED_NAME, groupDIRECTORY_NOT_ASSIGNED);

	}

	public boolean groupNodesExists() {
		return groupNodes != null;
	}

	// we produce all partial pathes that are defined by the persistent groups
	public void produceAndLinkGroups(final Map<String, Map<String, String>> importedGroups) {
		logging.debug(this, "produceAndLinkGroups " + importedGroups.keySet());
		this.groups = new TreeMap<>(importedGroups);
		// we need a local copy since we add virtual groups

		createDIRECTORY_NOT_ASSIGNED();

		groupNodes = new HashMap<>();

		initTopGroups();

		// produce other nodes

		for (String groupId : groups.keySet()) {

			if (topGroupNames.contains(groupId))
				continue;

			GroupNode node = produceGroupNode(groups.get(groupId));
			groupNodes.put(groupId, node);
		}

		// now we link them

		for (String groupId : groups.keySet()) {
			if (topGroupNames.contains(groupId))
				continue;

			DefaultMutableTreeNode node = groupNodes.get(groupId);

			String parentId = groups.get(groupId).get("parentGroupId");

			if (parentId == null || parentId.equalsIgnoreCase("null"))
				parentId = GROUPS_NAME;

			DefaultMutableTreeNode parent = null;

			if (groupNodes.get(parentId) == null)
				// group not existing
				parent = groupNodes.get(GROUPS_NAME);
			else
				parent = groupNodes.get(parentId);

			try {
				parent.add(node);
				model.nodesWereInserted(parent, new int[] { model.getIndexOfChild(parent, node) });

			} catch (IllegalArgumentException ex) {
				logging.error(this, "Cannot add node to parent " + node + ", " + parent + ": " + ex, ex);
				JOptionPane.showMessageDialog(Globals.mainContainer,
						configed.getResourceValue("ClientTree.cannot_add_node.text") + " " + node + " in  " + parent
								+ "(" + ex + ")",
						configed.getResourceValue("ClientTree.cannot_add_node.title"), JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	@Override
	public DefaultTreeModel getModel() {
		return model; // Can this be removed?
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

				// " + belongingTo);

				{
					for (String groupId : belongingTo) {

						// )
						{
							List<String> memberList = group2Members.get(groupId);

							if (memberList == null) {
								memberList = new ArrayList<>();
							}

							memberList.add(clientId);
							group2Members.put(groupId, memberList);
						}
					}
				}

			}

		}

		List<String> membersOfDIRECTORY_NOT_ASSIGNED = new ArrayList<>();
		group2Members.put(DIRECTORY_NOT_ASSIGNED_NAME, membersOfDIRECTORY_NOT_ASSIGNED);

		// we build and link the groups
		for (Entry<String, List<String>> entry : group2Members.entrySet()) {

			GroupNode groupNode = groupNodes.get(entry.getKey());
			if (groupNode == null) {
				logging.warning("group for groupId " + entry.getKey() + " not found");

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

			if (hostingGroups.isEmpty()) // client is not in any DIRECTORY group
			{
				membersOfDIRECTORY_NOT_ASSIGNED.add(clientId);

				IconNode node = produceClientNode(clientId);
				DIRECTORY_NOT_ASSIGNED.add(node);

				clientNodesInDIRECTORY.put(clientId, node);

				addClientNodeInfo(node);

				hostingGroups.add(DIRECTORY_NOT_ASSIGNED);
			}

		}

		model.nodeStructureChanged(DIRECTORY);

		TreeSet<String> allowedClients = null;

		logging.info(this, "associateClientsToGroups, evaluate permittedHostGroups " + permittedHostGroups);

		if (permittedHostGroups != null) {
			allowedClients = new TreeSet<>();

			if (directlyAllowedGroups == null)
				directlyAllowedGroups = new TreeSet<>();

			for (String clientId : leafname2AllItsPaths.keySet()) {
				for (SimpleTreePath path : leafname2AllItsPaths.get(clientId)) {

					Set<String> pathElements = new TreeSet<>(path);
					int allElementsNumber = pathElements.size();
					pathElements.removeAll(permittedHostGroups);// retained are the elements not permitted
					int notPermittedNumber = pathElements.size();

					if (notPermittedNumber < allElementsNumber) {

						allowedClients.add(clientId);
						directlyAllowedGroups.addAll(path);

					}
				}
			}

			directlyAllowedGroups.removeAll(allowedClients); // they were the last element in pathElements

		}

		logging.info(this, "associateClientsToGroups allowed Groups " + directlyAllowedGroups);

		return allowedClients;

	}

	public Set<String> getDirectlyAllowedGroups() {
		return directlyAllowedGroups;
	}

	public void editGroupNode(DefaultMutableTreeNode node) {
		if (!node.getAllowsChildren())
			return;

	}

	protected void editGroupNode(TreePath path) {
		DefaultMutableTreeNode node = null;

		if (path == null)
			return;

		else
			node = (DefaultMutableTreeNode) path.getLastPathComponent();

		if (!node.getAllowsChildren())
			return;

		String groupId = node.toString();

		LinkedHashMap<String, String> groupData = new LinkedHashMap<>();
		groupData.put("groupname", groupId);
		groupData.put("description", groups.get(groupId).get("description"));
		HashMap<String, String> labels = new HashMap<>();
		labels.put("groupname", configed.getResourceValue("ClientTree.editNode.label.groupname"));
		labels.put("description", configed.getResourceValue("ClientTree.editNode.label.description"));
		HashMap<String, Boolean> editable = new HashMap<>();
		editable.put("groupname", false);
		editable.put("description", true);

		FEditRecord fEdit = new FEditRecord(configed.getResourceValue("ClientTree.editGroup"));
		fEdit.setRecord(groupData, labels, null, editable);
		fEdit.setTitle(configed.getResourceValue("ClientTree.editNode.title") + " (" + Globals.APPNAME + ")");
		fEdit.init();
		fEdit.setSize(450, 250);
		fEdit.centerOn(Globals.mainContainer);

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

		if (path == null)
			node = GROUPS;

		else
			node = (DefaultMutableTreeNode) path.getLastPathComponent();

		if (node.getAllowsChildren()) {
			if (!node.toString().equals(GROUPS_NAME) && main.getOpsiVersion().compareTo("3.4.9") < 0) {
				JOptionPane.showMessageDialog(Globals.mainContainer,
						"group in group not supported for opsiVersion < 3.4.9, \nopsiVersion is "
								+ main.getOpsiVersion(),
						"opsi info ",

						// synchronous",

						JOptionPane.WARNING_MESSAGE);
				return result;
			}

			LinkedHashMap<String, String> groupData = new LinkedHashMap<>();
			groupData.put("groupname", "");
			groupData.put("description", "");
			HashMap<String, String> labels = new HashMap<>();
			labels.put("groupname", configed.getResourceValue("ClientTree.editNode.label.groupname"));
			labels.put("description", configed.getResourceValue("ClientTree.editNode.label.description"));
			HashMap<String, Boolean> editable = new HashMap<>();
			editable.put("groupname", true);
			editable.put("description", true);

			String newGroupKey = "";

			boolean gotName = !(newGroupKey.equals("")) && !(groups.keySet().contains(newGroupKey));

			String inscription = "";

			FEditRecord fEdit = new FEditRecord(inscription);
			fEdit.setRecord(groupData, labels, null, editable);
			fEdit.setTitle(configed.getResourceValue("ClientTree.addNode.title") + " (" + Globals.APPNAME + ")");
			fEdit.init();
			fEdit.setSize(450, 250);
			fEdit.centerOn(Globals.mainContainer);

			fEdit.setModal(true);

			while (!gotName) {
				if (newGroupKey.equals(""))
					inscription = configed.getResourceValue("ClientTree.requestGroup");

				else if (groups.keySet().contains(newGroupKey))
					inscription = "'" + newGroupKey + "' "
							+ configed.getResourceValue("ClientTree.requestNotExistingGroupName");

				else
					gotName = true;

				fEdit.setHint(inscription);

				if (!gotName) {

					fEdit.setVisible(true);

					newGroupKey = fEdit.getData().get("groupname").toLowerCase();

					if (fEdit.isCancelled())
						return null;

				}

			} // Now variable gotName equals true

			StringValuedRelationElement newGroup = new StringValuedRelationElement();

			newGroup.put("groupId", newGroupKey);
			newGroup.put("parentGroupId", node.toString());
			newGroup.put("description", groupData.get("description"));

			if (addGroup(newGroup)) // get persistence
			{
				groups.put(newGroupKey, newGroup);
				logging.debug(this, "makeSubGroupAt newGroupKey, newGroup " + newGroupKey + ", " + newGroup);
				GroupNode newNode = insertGroup(newGroupKey, groupData.get("description"), node);
				groupNodes.put(newGroupKey, newNode);

				result = newNode;
			}

		}

		return result;
	}

	private boolean addObject2InternalGroup(String objectID, DefaultMutableTreeNode newGroupNode, TreePath newPath) {

		if (getChildWithUserObjectString(objectID, newGroupNode) == null) // child with this objectID not existing
		{
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

		leafname2AllItsPaths.rebuildFromTree(ROOT);

	}

	private boolean removeClientNodes(List<DefaultMutableTreeNode> clientNodes) {
		List<Object2GroupEntry> groupEntries = new ArrayList<>();

		for (DefaultMutableTreeNode clientNode : clientNodes) {
			String clientId = (String) (clientNode.getUserObject());
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) clientNode.getParent();

			removeClientInternally(clientId, (GroupNode) parent);

			groupEntries.add(new Object2GroupEntry(null, clientId, parent.toString()));

		}

		return main.removeHostGroupElements(groupEntries);

	}

	public void removeClientInternally(String clientID, GroupNode parentNode) {

		logging.debug("removeClientInternally clientId, parentNode " + clientID + ", " + parentNode);

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
		if (stopCounter > clientNodesInDIRECTORY.size())
			logging.warning("removing client not successful but stopped because of reaching the repetition limit");

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
		if (existingNode == null) // we have not a node with this name in the target group
		{

			if (sourcePath != null) {
				logging.debug(this,
						"moveClientTo checked importID sourcePath.getLastPathComponent(); "
								+ sourcePath.getLastPathComponent() + " class "
								+ ((sourcePath.getLastPathComponent()).getClass()));
			} else {
				logging.debug(this, "moveClientTo sourcePath null, sourceParentNode " + sourceParentNode);
			}

			DefaultMutableTreeNode clientNode = getChildWithUserObjectString(importID, sourceParentNode);

			insertNodeInOrder(clientNode, dropParentNode);
			getModel().nodeStructureChanged(sourceParentNode);

			if (getGroupNode(dropParentID).getChildsArePersistent())
				addObject2PersistentGroup(importID, dropParentID);

			// operations in DIRECTORY

			if (isInDIRECTORY(dropPath)) {
				locationsInDIRECTORY.get(importID).add(getGroupNode(dropParentID));
				locationsInDIRECTORY.get(importID).remove(sourceParentNode);
			}

			TreePath newPath = pathByAddingChild(dropPath, clientNode);
			SimpleTreePath simplePath = new SimpleTreePath(dropPath.getPath());
			leafname2AllItsPaths.add(importID, newPath);
			activeParents.addAll(simplePath.collectNodeNames());

			logging.debug(this,
					"moveClientTo -- remove " + importID + " from " + sourceParentID
							+ " clientNode, sourceParentNode, sourcePath " + clientNode + ", " + sourceParentNode + ", "
							+ sourcePath

			);
			removeObject2Group(importID, sourceParentID); // persistent removal
			removeClientInternally(importID, sourceParentNode);

			makeVisible(newPath);
			repaint();

			checkDIRECTORY(importID, (GroupNode) dropParentNode);

		}
	}

	public void clientCopyOrMoveTo(String importID, TreePath sourcePath, String sourceParentID,
			GroupNode sourceParentNode,

			DefaultMutableTreeNode newParentNode, TreePath newParentPath, String newParentID,

			Boolean moving)

	{
		logging.debug(this, "clientCopyOrMoveTo moving " + moving);
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
		logging.debug(this, " copying " + objectID + ", sourcePath " + sourcePath + " into group " + newParentID);

		DefaultMutableTreeNode clientNode = null;

		if (sourcePath == null) {

			clientNode = getClientNode(objectID);
		} else
			clientNode = (DefaultMutableTreeNode) sourcePath.getLastPathComponent();

		logging.debug(this, " -- copyClientTo childs are persistent, newParentNode " + newParentNode + " "
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
		if (groupsInDIRECTORY.contains(DIRECTORY_NOT_ASSIGNED) && groupsInDIRECTORY.size() > 1) {
			locationsInDIRECTORY.get(objectID).remove(DIRECTORY_NOT_ASSIGNED);
			removeClientInternally(objectID, DIRECTORY_NOT_ASSIGNED);
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

		if (groupsInDIRECTORY.size() <= 1)
			return;

		// size should always be at least 1
		// we handle the case that is > 1

		List<GroupNode> correctNode = selectOneNode(groupsInDIRECTORY, clientID, selectedNode);

		if (correctNode != null)
		// we did some selection
		{
			groupsInDIRECTORY.removeAll(correctNode); // we remove the one selected node, the not desired nodes remain

			for (GroupNode node : groupsInDIRECTORY) {

				removeClientInternally(clientID, node);
				removeObject2Group(clientID, node.getUserObject().toString());
			}

			locationsInDIRECTORY.put(clientID, new HashSet<>(correctNode));
		}

	}

	private List<GroupNode> selectOneNode(java.util.Set<GroupNode> groupSet, String clientID, GroupNode preSelected)

	{
		List<GroupNode> result = null;

		if (groupSet.size() > 1) {

			FEditList fList = new FEditList(null);
			fList.setListModel(new DefaultComboBoxModel<GroupNode>(new Vector<>(groupSet)));
			fList.setTitle(Globals.APPNAME + ":  " + configed.getResourceValue("ClientTree.DIRECTORYname") + " "
					+ configed.getResourceValue("ClientTree.checkDIRECTORYAssignments"));
			fList.setExtraLabel(configed.getResourceValue("ClientTree.severalLocationsAssigned") + " >> " + clientID
					+ " <<, " + configed.getResourceValue("ClientTree.selectCorrectLocation"));
			fList.init(new java.awt.Dimension(640, 60));

			fList.locateLeftTo(Globals.mainContainer);
			fList.setModal(true);

			if (preSelected != null) {
				fList.setSelectedValue(preSelected);
				fList.setDataChanged(true);
			}

			fList.setVisible(true);

			if (fList.getSelectedList().isEmpty()) {
				int returnedOption = JOptionPane.showOptionDialog(Globals.mainContainer,
						configed.getResourceValue("ClientTree.abandonUniqueLocation"),
						Globals.APPNAME + " " + configed.getResourceValue("ClientTree.requestInformation"), //
						-1, JOptionPane.WARNING_MESSAGE, null,
						new String[] { configed.getResourceValue("yesOption"), configed.getResourceValue("noOption") },
						configed.getResourceValue("noOption"));

				if (returnedOption == 1 || returnedOption == JOptionPane.CLOSED_OPTION)
				// do it again
				{
					result = selectOneNode(groupSet, clientID, preSelected);
				}
			} else
				result = fList.getSelectedList();
		}

		return result;
	}

	public boolean isChildOfALL(DefaultMutableTreeNode node) {
		return (node.getParent() == ALL);
	}

	public boolean isInGROUPS(String groupName) {
		GroupNode node = groupNodes.get(groupName);
		if (node == null)
			return false;

		return isInGROUPS(node);
	}

	public boolean isInDIRECTORY(String groupName) {
		GroupNode node = groupNodes.get(groupName);
		if (node == null)
			return false;

		return isInDIRECTORY(node);
	}

	public boolean isInGROUPS(TreePath path) {
		return path.getPathCount() >= 2 && path.getPathComponent(1) == GROUPS;
	}

	public boolean isInDIRECTORY(TreePath path) {
		return path.getPathCount() >= 2 && path.getPathComponent(1) == DIRECTORY;
	}

	public boolean isInDIRECTORY(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		return (path.length >= 2 && path[1] == DIRECTORY);
	}

	public boolean isInGROUPS(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		return (path.length >= 2 && path[1] == GROUPS);
	}

	public void insertNodeInOrder(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {

		if (node == null || parent == null)
			return;

		String nodeObject = node.getUserObject().toString();

		boolean foundLoc = false;

		Enumeration<TreeNode> en = parent.children();

		// for groups, we should look only for groups

		DefaultMutableTreeNode insertNode = null;
		while (en.hasMoreElements() && !foundLoc) {
			insertNode = (DefaultMutableTreeNode) en.nextElement();

			if (insertNode.getAllowsChildren() // node with subnodes = group
					&& !node.getAllowsChildren() // leaf
			) {

				continue;
			}

			if (!insertNode.getAllowsChildren() // leaf
					&& node.getAllowsChildren() // group
			) {

				foundLoc = true;
				continue;
			}

			// both are leafs or both are groups

			if (insertNode.toString().compareToIgnoreCase(nodeObject) > 0)

				foundLoc = true;
		}

		if (insertNode == null || !foundLoc) // append
			parent.add(node);

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
		if (groupDescription == null || groupDescription.trim().equals(""))
			xGroupDescription = groupObject.toString();

		GroupNode node = produceGroupNode(groupObject, xGroupDescription);

		DefaultMutableTreeNode xParent = parent;
		if (parent == null)
			xParent = GROUPS;

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
		return main.getActivePaths();
	}

	public TreePath getActiveTreePath(String id) {
		return main.getActiveTreeNodes().get(id);
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

		logging.debug(this, "produceActiveParents activeParents " + activeParents);

		repaint();
	}

	private ArrayList<String> enumerateLeafNodes(DefaultMutableTreeNode node) {
		ArrayList<String> result = new ArrayList<>();

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

	public TreeSet<String> collectLeafs(DefaultMutableTreeNode node) {
		TreeSet<String> clients = new TreeSet<>(enumerateLeafNodes(node));

		return clients;
	}

	private HashSet<String> collectParentIDs(DefaultMutableTreeNode node) {
		String nodeID = (String) node.getUserObject();
		return collectParentIDs(nodeID);
	}

	public ArrayList<SimpleTreePath> getSimpleTreePaths(String leafname) {
		return leafname2AllItsPaths.getSimpleTreePaths(leafname);
	}

	public void remove(String leafname, SimpleTreePath clientPath) {
		leafname2AllItsPaths.remove(leafname, clientPath);
	}

	public HashSet<String> collectParentIDs(String nodeID) {

		HashSet<String> allParents = new HashSet<>();

		List<SimpleTreePath> treePaths = getSimpleTreePaths(nodeID);

		if (treePaths == null) {

		} else {
			for (SimpleTreePath path : treePaths) {
				allParents.addAll(path.collectNodeNames());
			}
		}

		return allParents;
	}

	public Set<String> getActiveParents() {
		if (activeParents == null)
			initActiveParents();

		return activeParents;
	}

	public TreePath getGroupPathActivatedByTree() {
		return main.getGroupPathActivatedByTree();
	}

	public boolean addObject2PersistentGroup(String objectId, String groupId) {
		return main.addObject2Group(objectId, groupId);
	}

	public boolean removeObject2Group(String objectId, String groupId) {
		return main.removeObject2Group(objectId, groupId);
	}

	public boolean addGroup(StringValuedRelationElement newGroup) {
		return main.addGroup(newGroup);
	}

	public boolean updateGroup(String groupId, Map<String, String> groupInfo) {
		return main.updateGroup(groupId, groupInfo);
	}

	public boolean deleteGroup(String groupId) {
		return main.deleteGroup(groupId);
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
		return main.getSelectedClientsInTable();
	}

	@Override
	public void paint(java.awt.Graphics g) {
		try {
			super.paint(g);
		} catch (java.lang.ClassCastException ex) {
			logging.warning(this, "the ugly well known exception " + ex);
			WaitCursor.stopAll();
		}
	}

}
