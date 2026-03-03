/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.Icons;
import de.uib.configed.share.PopupMouseListener;
import de.uib.configed.share.logging.Logging;

public class TreePopupMouseListener {
	private AbstractGroupTree tree;

	private JMenuItem menuItemCreateNode;
	private JMenuItem menuItemEditNode;
	private JMenuItem menuItemDeleteNode;
	private JMenuItem menuItemDeleteGroupNode;
	private JMenuItem menuItemRemoveElements;

	private boolean anyVisible;

	public TreePopupMouseListener(JPopupMenu jPopupMenu, AbstractGroupTree tree) {
		new PopupMouseListener(jPopupMenu, this::checkAccepted, new JComponent[] { tree });

		this.tree = tree;

		menuItemCreateNode = new JMenuItem();
		Icons.addIntellijIconToMenuItem(menuItemCreateNode, "add");
		menuItemCreateNode.addActionListener(actionEvent -> makeSubGroup());
		menuItemCreateNode
				.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
						.isGlobalReadOnly());
		jPopupMenu.add(menuItemCreateNode);

		menuItemEditNode = new JMenuItem();
		Icons.addIntellijIconToMenuItem(menuItemEditNode, "edit");
		menuItemEditNode.addActionListener(actionEvent -> tree.editGroupNode(tree.getSelectionPath()));
		menuItemEditNode.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
				.isGlobalReadOnly());
		jPopupMenu.add(menuItemEditNode);

		menuItemDeleteNode = new JMenuItem(Configed.getResourceValue("ClientTree.deleteNode"));
		Icons.addIntellijIconToMenuItem(menuItemDeleteNode, "remove");
		menuItemDeleteNode.addActionListener(actionEvent -> tree.deleteNodes(tree.getSelectionPaths()));
		menuItemDeleteNode
				.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
						.isGlobalReadOnly());
		jPopupMenu.add(menuItemDeleteNode);

		menuItemDeleteGroupNode = new JMenuItem(Configed.getResourceValue("ClientTree.deleteGroupNode"));
		Icons.addIntellijIconToMenuItem(menuItemDeleteGroupNode, "delete");
		menuItemDeleteGroupNode.addActionListener(actionEvent -> tree.deleteGroupNodes(tree.getSelectionPaths()));
		menuItemDeleteGroupNode
				.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
						.isGlobalReadOnly());
		jPopupMenu.add(menuItemDeleteGroupNode);

		String removeAllKey = tree instanceof ClientTree ? "ClientTree.removeAllElements"
				: "ProductTree.removeAllElements";
		menuItemRemoveElements = new JMenuItem(Configed.getResourceValue(removeAllKey));
		Icons.addIntellijIconToMenuItem(menuItemRemoveElements, "remove");
		menuItemRemoveElements.addActionListener(actionEvent -> removeElements());
		menuItemRemoveElements
				.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
						.isGlobalReadOnly());
		jPopupMenu.add(menuItemRemoveElements);
	}

	private void makeSubGroup() {
		TreePath selectedPath = tree.getSelectionPath();
		GroupNode resultNode = tree.makeSubgroupAt(selectedPath);
		if (resultNode != null) {
			tree.makeVisible(selectedPath.pathByAddingChild(resultNode));
			tree.repaint();
		}
	}

	private void removeElements() {
		TreePath selectedPath = tree.getSelectionPath();

		if (selectedPath != null
				&& selectedPath.getPathComponent(selectedPath.getPathCount() - 1) instanceof GroupNode groupNode) {

			int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
					String.format(Configed.getResourceValue("AbstractGroupTree.removeAllElements.message"), groupNode),
					Configed.getResourceValue("AbstractGroupTree.removeAllElements.title"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer != JOptionPane.YES_OPTION) {
				return;
			}

			List<DefaultMutableTreeNode> clientNodesToRemove = new ArrayList<>();

			groupNode.breadthFirstEnumeration().asIterator().forEachRemaining((TreeNode node) -> {
				if (!node.getAllowsChildren()) {
					clientNodesToRemove.add((DefaultMutableTreeNode) node);
				}
			});

			if (tree.removeNodes(clientNodesToRemove)) {
				// refresh internal view
				tree.setGroupAndSelect(groupNode);
			}
		}
	}

	private boolean shouldShow() {
		TreePath selectedPath = tree.getSelectionPath();

		if (selectedPath == null) {
			return false;
		}

		Logging.debug(this, "shouldShow clickPath  ", selectedPath);

		DefaultMutableTreeNode clickNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

		return !containsGroupAndNode(tree.getSelectionPaths()) && !tree.isGroupNodeFullList(clickNode);
	}

	private static boolean containsGroupAndNode(TreePath[] paths) {
		boolean containsNode = false;
		boolean containsGroup = false;

		for (TreePath path : paths) {
			DefaultMutableTreeNode contextNode = (DefaultMutableTreeNode) path.getLastPathComponent();

			boolean isGroup = contextNode.getAllowsChildren();
			containsGroup |= isGroup;
			containsNode |= !isGroup;

			if (containsGroup && containsNode) {
				return true;
			}
		}
		return false;
	}

	private boolean checkAccepted(MouseEvent e) {
		if (!tree.isEnabled()) {
			return false;
		}

		selectMousePathIfNotSelected(e);

		if (!shouldShow()) {
			return false;
		}

		hideAllMenuItems();

		TreePath selectedPath = tree.getSelectionPath();
		DefaultMutableTreeNode contextNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

		configureMenuVisibility(contextNode);

		updateContextLabels(contextNode);

		return anyVisible;
	}

	private void configureMenuVisibility(DefaultMutableTreeNode contextNode) {
		boolean contextIsGroup = contextNode.getAllowsChildren();

		List<DefaultMutableTreeNode> selectedNodes = getSelectedNodes();

		if (contextIsGroup) {
			boolean contextIsFixed = ((GroupNode) contextNode).isFixed();
			boolean contextIsAssignable = !ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(contextNode.toString());

			long selectedGroupCount = selectedNodes.stream().filter(TreeNode::getAllowsChildren).count();
			boolean oneGroupSelected = selectedGroupCount == 1;
			boolean anyGroupsSelected = selectedGroupCount > 0;

			setMenuItemVisible(menuItemCreateNode, oneGroupSelected && contextIsAssignable);
			setMenuItemVisible(menuItemEditNode, oneGroupSelected && contextIsAssignable && !contextIsFixed);
			setMenuItemVisible(menuItemDeleteGroupNode, anyGroupsSelected && !contextIsFixed);
			setMenuItemVisible(menuItemRemoveElements, oneGroupSelected && !contextIsFixed);
		} else {
			boolean contextParentIsFixed = ((GroupNode) contextNode.getParent()).isFixed();
			boolean anyMembersSelected = selectedNodes.stream().anyMatch(n -> !n.getAllowsChildren());

			setMenuItemVisible(menuItemDeleteNode, anyMembersSelected && !contextParentIsFixed);
		}
	}

	private void selectMousePathIfNotSelected(MouseEvent e) {
		TreePath mousePath = getMousePath(e);
		if (mousePath == null) {
			return;
		}

		boolean isSelected = Arrays.asList(tree.getSelectionPaths()).contains(mousePath);
		if (!isSelected) {
			tree.setSelectionPath(mousePath);
		}
	}

	private TreePath getMousePath(MouseEvent e) {
		int mouseRow = tree.getRowForLocation(e.getX(), e.getY());
		TreePath mousePath = tree.getPathForLocation(e.getX(), e.getY());

		return mouseRow == -1 ? null : mousePath;
	}

	private void hideAllMenuItems() {
		menuItemCreateNode.setVisible(false);
		menuItemEditNode.setVisible(false);
		menuItemDeleteNode.setVisible(false);
		menuItemDeleteGroupNode.setVisible(false);
		menuItemRemoveElements.setVisible(false);
	}

	private List<DefaultMutableTreeNode> getSelectedNodes() {
		TreePath[] paths = tree.getSelectionPaths();
		if (paths == null) {
			return List.of();
		}
		return Arrays.stream(paths).map(p -> (DefaultMutableTreeNode) p.getLastPathComponent()).toList();
	}

	private void updateContextLabels(DefaultMutableTreeNode contextNode) {
		if (menuItemCreateNode.isVisible()) {
			menuItemCreateNode.setText(
					String.format(Configed.getResourceValue("ClientTree.addNode"), contextNode.getUserObject()));
		}
		if (menuItemEditNode.isVisible()) {
			menuItemEditNode.setText(
					String.format(Configed.getResourceValue("ClientTree.editGroup"), contextNode.getUserObject()));
		}
		if (menuItemRemoveElements.isVisible()) {
			menuItemRemoveElements.setText(String.format(Configed.getResourceValue(
					tree instanceof ClientTree ? "ClientTree.removeAllElements" : "ProductTree.removeAllElements"),
					contextNode.getUserObject()));
		}
	}

	private void setMenuItemVisible(JMenuItem menuItem, boolean visible) {
		menuItem.setVisible(visible);
		anyVisible |= menuItem.isVisible();
	}
}
