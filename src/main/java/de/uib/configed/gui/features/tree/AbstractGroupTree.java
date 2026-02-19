/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

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
import java.util.function.BiConsumer;

import javax.swing.DropMode;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.type.Object2GroupEntry;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public abstract class AbstractGroupTree extends JTree implements TreeSelectionListener {
	public static final String ALL_GROUPS_NAME = Configed.getResourceValue("AbstractGroupTree.groupsName");

	OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory.getPersistenceController();

	private TreePath[] lastSelectionPaths = new TreePath[0];

	public final GroupNode rootNode = new GroupNode("");

	protected GroupNode groupNodeGroups;
	protected GroupNode groupNodeAllObjects;

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

	private static class DeletionPlan {
		final Set<String> memberIds = new HashSet<>();
		final Set<String> groupIds = new HashSet<>();
		final List<Object2GroupEntry> entries = new ArrayList<>();
	}

	private static class GroupDeletionPlan {
		final Set<String> selectedGroupIds = new HashSet<>();
		final Set<String> allGroupIds = new HashSet<>();
		final List<DefaultMutableTreeNode> selectedGroupNodes = new ArrayList<>();
	}

	protected AbstractGroupTree(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		init();

		model = new DefaultTreeModel(rootNode);
		super.setModel(model);
		super.setCellRenderer(new GroupTreeRenderer(this));
	}

	public boolean isGroupNodeFullList(DefaultMutableTreeNode compareNode) {
		return groupNodeAllObjects.equals(compareNode);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Logging.debug(this, "valueChanged ", e);
		if (ChangedDataManager.checkSaveAll(true)) {
			reactOnTreeSelection();
			lastSelectionPaths = getSelectionPaths();
		} else {
			removeTreeSelectionListener(this);
			setSelectionPaths(lastSelectionPaths);
			addTreeSelectionListener(this);
		}
	}

	abstract void reactOnTreeSelection();

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
		new TreePopupMouseListener(new JPopupMenu(), this);

		// Drag and drop needs to be enabled
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
		groupNodeAllObjects.removeAllChildren();
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

		if (expanded != null) {
			expanded.asIterator().forEachRemaining((TreePath path) -> {
				Map<String, Object> internalMap = new HashMap<>();
				internalMap.put("expanded", true);
				expandedNodes.put(path.getLastPathComponent().toString(), internalMap);
			});
		}

		List<TreePath> selectionPaths = Arrays
				.asList(getSelectionPaths() != null ? getSelectionPaths() : new TreePath[0]);

		if (selectionPaths != null) {
			for (TreePath path : selectionPaths) {
				Map<String, Object> map = expandedNodes.get(path.getLastPathComponent().toString()) != null
						? expandedNodes.get(path.getLastPathComponent().toString())
						: new HashMap<>();

				map.put("selected", true);
				if (!((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()) {
					map.put("parent", path.getParentPath().getLastPathComponent().toString());
					map.put("child", path.getLastPathComponent());
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

		if (getSelectionPaths() == null || getSelectionPaths().length == 0) {
			addSelectionPath(new TreePath(model.getPathToRoot(groupNodeAllObjects)));
		}
	}

	private DefaultMutableTreeNode getNodeFromMap(Map.Entry<String, Map<String, Object>> node) {
		DefaultMutableTreeNode result = groupNodes.get(node.getKey());
		if (result == null && node.getValue().containsKey("parent")) {
			DefaultMutableTreeNode parentNode = groupNodes.get(node.getValue().get("parent"));
			if (parentNode.getChildCount() > 0) {
				String child = node.getValue().get("child").toString();
				result = getChildWithUserObjectString(child, parentNode);
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
		recursivelyCollectParentIDs(rootNode, elementIds, (id, path) -> result.addAll(path));
		return result;
	}

	public Map<String, Set<String>> collectAggregatedParentIDs(Collection<String> elementIds) {
		Map<String, Set<String>> result = new HashMap<>();
		recursivelyCollectParentIDs(rootNode, elementIds,
				(id, path) -> result.computeIfAbsent(id, k -> new HashSet<>()).addAll(path));
		return result;
	}

	private static void recursivelyCollectParentIDs(DefaultMutableTreeNode node, Collection<String> nodeIds,
			BiConsumer<String, List<String>> onMatch) {
		node.children().asIterator().forEachRemaining((TreeNode child) -> {
			String childId = child.toString();

			if (nodeIds.contains(childId)) {
				List<String> path = Arrays.stream(((DefaultMutableTreeNode) child).getPath()).map(Object::toString)
						.toList();
				onMatch.accept(childId, path);
			}

			recursivelyCollectParentIDs((DefaultMutableTreeNode) child, nodeIds, onMatch);
		});
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

		JLabel labelDescription = Utils.createBoldLabel("description");

		String answer = (String) JOptionPane.showInputDialog(ConfigedMain.getMainFrame(), labelDescription,
				Configed.getResourceValue("ClientTree.editGroup") + ": " + groupId, JOptionPane.PLAIN_MESSAGE, null,
				null, groups.get(groupId).get("description"));

		if (answer != null) {
			groups.get(groupId).put("description", answer);
			persistenceController.getDataServices().group.updateGroup(groupId, groups.get(groupId),
					this instanceof ClientTree);
		}
	}

	protected boolean deleteNodes(TreePath[] paths) {
		if (paths == null || paths.length == 0) {
			return false;
		}

		DeletionPlan plan = buildDeletionPlan(paths);

		boolean hasWork = !plan.entries.isEmpty();
		if (hasWork) {
			boolean confirmed = confirmNodeDeletion(plan.memberIds.size(), plan.groupIds.size());

			if (confirmed) {
				for (Object2GroupEntry entry : plan.entries) {
					removeNodeInternally(entry.getMember(), groupNodes.get(entry.getGroupId()));
				}

				String groupType = this instanceof ClientTree ? Object2GroupEntry.GROUP_TYPE_HOSTGROUP
						: Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP;

				return persistenceController.getDataServices().group.removeHostGroupElements(plan.entries, groupType);
			}
		}

		return false;
	}

	private DeletionPlan buildDeletionPlan(TreePath[] paths) {
		DeletionPlan plan = new DeletionPlan();

		for (TreePath path : paths) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

			String nodeId = (String) node.getUserObject();

			boolean isMember = isMemberNode(nodeId);
			GroupNode group = null;

			if (isMember) {
				group = resolveGroup(node);
			}

			if (isMember && group != null) {
				String groupId = (String) group.getUserObject();

				plan.memberIds.add(nodeId);
				plan.groupIds.add(groupId);
				plan.entries.add(new Object2GroupEntry(nodeId, groupId));
			}
		}

		return plan;
	}

	private boolean isMemberNode(String nodeId) {
		return groupNodes.get(nodeId) == null;
	}

	private GroupNode resolveGroup(DefaultMutableTreeNode node) {
		GroupNode parent = (GroupNode) node.getParent();
		String nodeId = (String) node.getUserObject();

		GroupNode registered = groupNodes.get(nodeId);
		if (registered != null && registered.getParent() != parent) {
			return (GroupNode) registered.getParent();
		}

		return parent;
	}

	private boolean confirmNodeDeletion(int memberCount, int groupCount) {
		String message = String.format(Configed.getResourceValue("AbstractGroupTree.deleteNodesWarning"), memberCount,
				groupCount);

		int option = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("AbstractGroupTree.deleteNodesTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		return option == JOptionPane.YES_OPTION;
	}

	protected boolean deleteGroupNodes(TreePath[] paths) {
		boolean success = false;

		if (paths != null && paths.length > 0) {
			GroupDeletionPlan plan = buildGroupDeletionPlan(paths);

			if (!plan.allGroupIds.isEmpty() && confirmGroupDeletion(plan)) {
				executeGroupDeletion(plan);
				success = true;
			}
		}

		return success;
	}

	private GroupDeletionPlan buildGroupDeletionPlan(TreePath[] paths) {
		GroupDeletionPlan plan = new GroupDeletionPlan();

		for (TreePath path : paths) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

			String nodeId = (String) node.getUserObject();

			if (groupNodes.get(nodeId) != null) {
				plan.selectedGroupIds.add(nodeId);
				plan.selectedGroupNodes.add(node);

				collectGroupSubtree(node, plan);
			}
		}

		return plan;
	}

	private static void collectGroupSubtree(DefaultMutableTreeNode root, GroupDeletionPlan plan) {
		Enumeration<TreeNode> e = root.depthFirstEnumeration();

		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();

			String nodeId = node.getUserObject().toString();

			if (node.getAllowsChildren()) {
				plan.allGroupIds.add(nodeId);
			}
		}
	}

	private static boolean confirmGroupDeletion(GroupDeletionPlan plan) {
		String message = String.format(Configed.getResourceValue("ClientTree.deleteGroupWarning"),
				plan.selectedGroupIds.size());

		return JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("ClientTree.deleteGroupNode"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
	}

	private void executeGroupDeletion(GroupDeletionPlan plan) {
		if (plan.allGroupIds.isEmpty()) {
			return;
		}

		String groupType = this instanceof ClientTree ? Object2GroupEntry.GROUP_TYPE_HOSTGROUP
				: Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP;

		boolean result = persistenceController.getDataServices().group.deleteGroups(new ArrayList<>(plan.allGroupIds),
				groupType);

		if (result) {
			for (String groupId : plan.allGroupIds) {

				groupNodes.remove(groupId);
				groups.remove(groupId);
			}

			for (DefaultMutableTreeNode node : plan.selectedGroupNodes) {
				MutableTreeNode parent = (MutableTreeNode) node.getParent();
				if (parent != null) {
					parent.remove(node);
					getModel().nodeStructureChanged(parent);
				}
			}
		}
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
			JLabel labelGroupName = Utils.createBoldLabel("ClientTree.editNode.label.groupname");

			JTextField groupNameField = new JTextField();

			JLabel labelDescription = Utils.createBoldLabel("description");

			JTextField groupDescriptionField = new JTextField();
			String inscription = "";

			JPanel panel = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "", "[]0"));

			panel.add(labelGroupName);
			panel.add(groupNameField, "growx, gapbottom " + Globals.GAP_SIZE);
			panel.add(labelDescription);
			panel.add(groupDescriptionField, "growx");

			String newGroupKey = null;
			JOptionPane optionPane = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
				@Override
				public void selectInitialValue() {
					super.selectInitialValue();
					groupNameField.requestFocusInWindow();
				}
			};

			JDialog dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ClientTree.addNode"));

			do {
				optionPane.setMessage(new Object[] { inscription, panel });
				dialog.pack();
				dialog.setVisible(true);

				if (optionPane.getValue() != null && optionPane.getValue().equals(JOptionPane.OK_OPTION)) {
					newGroupKey = groupNameField.getText().toLowerCase(Locale.ROOT);
				} else {
					return null;
				}

				inscription = Configed.getResourceValue("ClientTree.requestNotExistingGroupName");
			} while ("".equals(newGroupKey) || groups.keySet().contains(newGroupKey));

			String description = groupDescriptionField.getText();

			// Now variable gotName equals true
			Map<String, String> newGroup = new HashMap<>();
			newGroup.put("id", newGroupKey);
			newGroup.put("parentGroupId", node.toString());
			newGroup.put("description", description);

			// send data to server
			if (persistenceController.getDataServices().group.addGroup(newGroup, this instanceof ClientTree)) {
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
		String nodeObject = node.toString();

		while (children.hasMoreElements()) {
			DefaultMutableTreeNode insertNode = (DefaultMutableTreeNode) children.nextElement();

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

		return persistenceController.getDataServices().group.removeHostGroupElements(groupEntries, groupType);
	}

	protected GroupNode produceGroupNode(String groupId, String description) {
		return produceGroupNode(groupId, description, null);
	}

	protected GroupNode produceGroupNode(String groupId, String description, String parentId) {
		GroupNode groupNode = new GroupNode(groupId);

		Map<String, String> groupMap = new HashMap<>();
		groupMap.put("id", groupId);
		if (parentId != null) {
			groupMap.put("parentGroupId", parentId);
		}
		groupMap.put("description", description);

		groups.put(groupId, groupMap);
		groupNodes.put(groupId, groupNode);

		return groupNode;
	}

	abstract void removeNodeInternally(String nodeID, GroupNode parent);

	public boolean isInGROUPS(String groupName) {
		GroupNode node = groupNodes.get(groupName);
		if (node == null) {
			return false;
		}

		TreeNode[] path = node.getPath();
		return path.length >= 2 && path[1] == groupNodeGroups;
	}

	public boolean equalsGroupNodeGroups(DefaultMutableTreeNode node) {
		return node == groupNodeGroups;
	}

	public boolean isInGROUPS(TreePath path) {
		return path.getPathCount() >= 2 && path.getPathComponent(1) == groupNodeGroups;
	}

	public Map<String, Map<String, String>> getGroups() {
		return groups;
	}

	public boolean isChildOfALL(TreeNode node) {
		return node.getParent() == groupNodeAllObjects;
	}

	public void moveGroupTo(String importID, GroupNode groupNode, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		insertNodeInOrder(groupNode, dropParentNode);
		model.nodeStructureChanged(sourceParentNode);
		makeVisible(dropPath.pathByAddingChild(groupNode));

		Map<String, String> theGroup = getGroups().get(importID);
		theGroup.put("parentGroupId", dropParentID);
		persistenceController.getDataServices().group.updateGroup(importID, theGroup, this instanceof ClientTree);
	}

	public TreePath getActiveTreePath(String id) {
		return Arrays.stream(getSelectionPaths()).filter(
				treePath -> ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject().equals(id))
				.findAny().orElse(null);
	}

	protected static DefaultMutableTreeNode getChildWithUserObjectString(String objectID,
			DefaultMutableTreeNode groupNode) {
		Enumeration<TreeNode> enumer = groupNode.children();

		while (enumer.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumer.nextElement();
			if (child.getUserObject().toString().equals(objectID)) {
				return child;
			}
		}

		return null;
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

	abstract void moveObjectTo(String importID, String sourceParentID, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID);

	abstract void copyObjectTo(String objectID, String newParentID, DefaultMutableTreeNode newParentNode,
			TreePath newParentPath);

	abstract Set<String> getSelectedObjectsInTable();

	public static Set<String> getChildrenRecursively(TreeNode groupNode) {
		Set<String> resultIds = new HashSet<>();

		addChildrenRecoursively(groupNode.children(), resultIds);

		return resultIds;
	}

	protected static void addChildrenRecoursively(Enumeration<? extends TreeNode> children, Set<String> resultIds) {
		children.asIterator().forEachRemaining((TreeNode child) -> {
			if (child.getAllowsChildren()) {
				addChildrenRecoursively(child.children(), resultIds);
			} else {
				resultIds.add(((DefaultMutableTreeNode) child).getUserObject().toString());
			}
		});
	}
}
