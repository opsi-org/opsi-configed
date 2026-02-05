/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
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

	private TreePath mousePath;

	private JMenuItem menuItemCreateNode;
	private JMenuItem menuItemEditNode;
	private JMenuItem menuItemDeleteNode;
	private JMenuItem menuItemDeleteGroupNode;
	private JMenuItem menuItemRemoveElements;

	private boolean anyVisible;

	public TreePopupMouseListener(JPopupMenu jPopupMenu, AbstractGroupTree tree) {
		new PopupMouseListener(jPopupMenu, this::checkAccepted, new JComponent[] { tree });

		this.tree = tree;

		menuItemCreateNode = new JMenuItem(Configed.getResourceValue("ClientTree.addNode"));
		Icons.addIntellijIconToMenuItem(menuItemCreateNode, "add");
		menuItemCreateNode.addActionListener(actionEvent -> makeSubGroup());
		menuItemCreateNode
				.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
						.isGlobalReadOnly());
		jPopupMenu.add(menuItemCreateNode);

		menuItemEditNode = new JMenuItem(Configed.getResourceValue("ClientTree.editGroup"));
		Icons.addIntellijIconToMenuItem(menuItemEditNode, "edit");
		menuItemEditNode.addActionListener(actionEvent -> tree.editGroupNode(mousePath));
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
		GroupNode resultNode = tree.makeSubgroupAt(mousePath);
		if (resultNode != null) {
			tree.makeVisible(mousePath.pathByAddingChild(resultNode));
			tree.repaint();
		}
	}

	private void removeElements() {
		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("AbstractGroupTree.removeAllElements.message"),
				Configed.getResourceValue("AbstractGroupTree.removeAllElements.title"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}

		if (mousePath != null
				&& mousePath.getPathComponent(mousePath.getPathCount() - 1) instanceof GroupNode groupNode) {
			Enumeration<TreeNode> enumer = groupNode.breadthFirstEnumeration();

			List<DefaultMutableTreeNode> clientNodesToRemove = new ArrayList<>();

			while (enumer.hasMoreElements()) {
				DefaultMutableTreeNode element = (DefaultMutableTreeNode) enumer.nextElement();
				if (!element.getAllowsChildren()) {
					clientNodesToRemove.add(element);
				}
			}

			if (tree.removeNodes(clientNodesToRemove)) {
				// refresh internal view
				tree.setGroupAndSelect(groupNode);
			}
		}
	}

	private boolean shouldShow(MouseEvent e) {
		if (!tree.isEnabled()) {
			return false;
		}

		int mouseRow = tree.getRowForLocation(e.getX(), e.getY());
		mousePath = tree.getPathForLocation(e.getX(), e.getY());

		// no node selection area
		if (mouseRow == -1) {
			mousePath = null;
			return false;
		}

		Logging.debug(this, "shouldShow clickPath  ", mousePath);

		DefaultMutableTreeNode clickNode = (DefaultMutableTreeNode) mousePath.getLastPathComponent();

		return !tree.isGroupNodeFullList(clickNode);
	}

	private boolean checkAccepted(MouseEvent e) {
		if (!shouldShow(e)) {
			return false;
		}

		hideAllMenuItems();

		DefaultMutableTreeNode contextNode = (DefaultMutableTreeNode) mousePath.getLastPathComponent();

		boolean contextIsGroup = contextNode.getAllowsChildren();
		boolean contextIsFixed = contextIsGroup && ((GroupNode) contextNode).isFixed();
		boolean contextIsAssignable = contextIsGroup
				&& !ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(contextNode.toString());

		List<DefaultMutableTreeNode> selectedNodes = getSelectedNodes();

		boolean anyGroupsSelected = selectedNodes.stream().anyMatch(TreeNode::getAllowsChildren);
		boolean anyMembersSelected = selectedNodes.stream().anyMatch(n -> !n.getAllowsChildren());

		setMenuItemVisible(menuItemCreateNode, contextIsAssignable);
		setMenuItemVisible(menuItemEditNode, contextIsAssignable && !contextIsFixed);
		setMenuItemVisible(menuItemDeleteGroupNode, anyGroupsSelected && !contextIsFixed);
		setMenuItemVisible(menuItemDeleteNode, anyMembersSelected && !contextIsFixed);
		setMenuItemVisible(menuItemRemoveElements, anyGroupsSelected && !anyMembersSelected && !contextIsFixed);

		updateContextLabels(contextNode);

		return anyVisible;
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
