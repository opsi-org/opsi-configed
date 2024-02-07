/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;
import utils.PopupMouseListener;

public class TreePopupMouseListener extends PopupMouseListener {
	private ClientTree tree;

	private TreePath mousePath;

	private JMenuItem menuItemCreateNode;
	private JMenuItem menuItemEditNode;
	private JMenuItem menuItemDeleteNode;
	private JMenuItem menuItemDeleteGroupNode;
	private JMenuItem menuItemActivateElements;
	private JMenuItem menuItemRemoveElements;

	public TreePopupMouseListener(JPopupMenu jPopupMenu, ClientTree tree, ConfigedMain configedMain) {
		super(jPopupMenu);
		this.tree = tree;

		menuItemCreateNode = new JMenuItem(Configed.getResourceValue("ClientTree.addNode"));
		menuItemCreateNode.addActionListener(actionEvent -> makeSubGroup());
		jPopupMenu.add(menuItemCreateNode);

		menuItemEditNode = new JMenuItem(Configed.getResourceValue("ClientTree.editNode"));
		menuItemEditNode.addActionListener(actionEvent -> tree.editGroupNode(mousePath));
		jPopupMenu.add(menuItemEditNode);

		menuItemDeleteNode = new JMenuItem(Configed.getResourceValue("ClientTree.deleteNode"));
		menuItemDeleteNode.addActionListener(actionEvent -> tree.deleteNode(mousePath));
		jPopupMenu.add(menuItemDeleteNode);

		menuItemDeleteGroupNode = new JMenuItem(Configed.getResourceValue("ClientTree.deleteGroupNode"));
		menuItemDeleteGroupNode.addActionListener(actionEvent -> tree.deleteNode(mousePath));
		jPopupMenu.add(menuItemDeleteGroupNode);

		menuItemActivateElements = new JMenuItem(Configed.getResourceValue("ClientTree.selectAllElements"));
		menuItemActivateElements.addActionListener(actionEvent -> activateElements(configedMain));
		jPopupMenu.add(menuItemActivateElements);

		menuItemRemoveElements = new JMenuItem(Configed.getResourceValue("ClientTree.removeAllElements"));
		menuItemRemoveElements.addActionListener(actionEvent -> removeElements(configedMain));
		jPopupMenu.add(menuItemRemoveElements);
	}

	private void makeSubGroup() {
		IconNode resultNode = tree.makeSubgroupAt(mousePath);
		if (resultNode != null) {
			tree.makeVisible(tree.pathByAddingChild(mousePath, resultNode));
			tree.repaint();
		}
	}

	private void activateElements(ConfigedMain configedMain) {
		TreePath sourcePath = mousePath;
		if (sourcePath != null && sourcePath.getPathComponent(sourcePath.getPathCount() - 1) instanceof GroupNode) {
			GroupNode node = (GroupNode) sourcePath.getPathComponent(sourcePath.getPathCount() - 1);
			configedMain.setGroup(node.toString());
		}
	}

	private void removeElements(ConfigedMain configedMain) {
		if (mousePath != null && mousePath.getPathComponent(mousePath.getPathCount() - 1) instanceof GroupNode) {
			GroupNode node = (GroupNode) mousePath.getPathComponent(mousePath.getPathCount() - 1);

			Enumeration<TreeNode> enumer = node.breadthFirstEnumeration();

			List<DefaultMutableTreeNode> clientNodesToRemove = new ArrayList<>();

			while (enumer.hasMoreElements()) {
				DefaultMutableTreeNode element = (DefaultMutableTreeNode) enumer.nextElement();
				if (!element.getAllowsChildren()) {
					clientNodesToRemove.add(element);
				}
			}

			if (tree.removeClientNodes(clientNodesToRemove)) {
				// refresh internal view
				configedMain.setGroup(node.toString());
			}
		}
	}

	private boolean checkAccepted(MouseEvent e) {
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

		Logging.debug(this, "checkAccepted clickPath  " + mousePath);

		DefaultMutableTreeNode clickNode = (DefaultMutableTreeNode) mousePath.getLastPathComponent();

		String nodeName = clickNode.getUserObject().toString();

		if (tree.getGroupNode(nodeName) != null && clickNode != tree.getGroupNode(nodeName)) {
			Logging.warning(this, "checkAccepted clickNode != tree.getGroupNode(nodeName)");
			clickNode = tree.getGroupNode(nodeName);
		}

		Logging.debug(this, "checkAccepted clickNode.getParent() " + clickNode.getParent());

		if (nodeName.equals(ClientTree.ALL_CLIENTS_NAME)
				|| (((DefaultMutableTreeNode) clickNode.getParent()).getUserObject().toString()
						.equals(ClientTree.ALL_CLIENTS_NAME) && !nodeName.equals(ClientTree.ALL_GROUPS_NAME))) {
			// dont show here any menu
			return false;
		}

		menuItemCreateNode.setVisible(false);
		menuItemEditNode.setVisible(false);
		menuItemDeleteNode.setVisible(false);
		menuItemDeleteGroupNode.setVisible(false);
		menuItemActivateElements.setVisible(false);
		menuItemRemoveElements.setVisible(false);

		int numberVisibleItems = 0;

		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) clickNode.getParent();

		if (clickNode.getAllowsChildren()) {
			if (!ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(clickNode.toString())) {
				menuItemCreateNode.setVisible(true);
				numberVisibleItems++;
			}

			if (!ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(clickNode.toString())
					&& !(((GroupNode) clickNode).isFixed())) {
				menuItemEditNode.setVisible(true);
				numberVisibleItems++;
			}

			if (!(((GroupNode) clickNode).isFixed())) {
				menuItemDeleteGroupNode.setVisible(true);
				numberVisibleItems++;
			}

			menuItemActivateElements.setVisible(true);
			numberVisibleItems++;

			if (!(((GroupNode) clickNode).isFixed())) {
				menuItemRemoveElements.setVisible(true);
				numberVisibleItems++;
			}
		} else if (!(((GroupNode) parentNode).isFixed())) {
			menuItemDeleteNode.setVisible(true);
			numberVisibleItems++;
		} else {
			// Do nothing since no item visible
		}

		return numberVisibleItems > 0;
	}

	@Override
	protected void maybeShowPopup(MouseEvent e) {
		if (checkAccepted(e)) {
			super.maybeShowPopup(e);
		}
	}
}
