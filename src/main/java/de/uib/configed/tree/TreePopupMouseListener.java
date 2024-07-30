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
import de.uib.utils.PopupMouseListener;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class TreePopupMouseListener extends PopupMouseListener {
	private AbstractGroupTree tree;

	private TreePath mousePath;

	private JMenuItem menuItemCreateNode;
	private JMenuItem menuItemEditNode;
	private JMenuItem menuItemDeleteNode;
	private JMenuItem menuItemDeleteGroupNode;
	private JMenuItem menuItemRemoveElements;

	public TreePopupMouseListener(JPopupMenu jPopupMenu, AbstractGroupTree tree) {
		super(jPopupMenu);
		this.tree = tree;

		menuItemCreateNode = new JMenuItem(Configed.getResourceValue("ClientTree.addNode"));
		Utils.addIntellijIconToMenuItem(menuItemCreateNode, "add");
		menuItemCreateNode.addActionListener(actionEvent -> makeSubGroup());
		jPopupMenu.add(menuItemCreateNode);

		menuItemEditNode = new JMenuItem(Configed.getResourceValue("ClientTree.editNode"));
		Utils.addIntellijIconToMenuItem(menuItemEditNode, "edit");
		menuItemEditNode.addActionListener(actionEvent -> tree.editGroupNode(mousePath));
		jPopupMenu.add(menuItemEditNode);

		menuItemDeleteNode = new JMenuItem(Configed.getResourceValue("ClientTree.deleteNode"));
		Utils.addIntellijIconToMenuItem(menuItemDeleteNode, "remove");
		menuItemDeleteNode.addActionListener(actionEvent -> tree.deleteNode(mousePath));
		jPopupMenu.add(menuItemDeleteNode);

		menuItemDeleteGroupNode = new JMenuItem(Configed.getResourceValue("ClientTree.deleteGroupNode"));
		Utils.addIntellijIconToMenuItem(menuItemDeleteGroupNode, "delete");
		menuItemDeleteGroupNode.addActionListener(actionEvent -> tree.deleteNode(mousePath));
		jPopupMenu.add(menuItemDeleteGroupNode);

		String removeAllKey = tree instanceof ClientTree ? "ClientTree.removeAllElements"
				: "ProductTree.removeAllElements";
		menuItemRemoveElements = new JMenuItem(Configed.getResourceValue(removeAllKey));
		Utils.addIntellijIconToMenuItem(menuItemRemoveElements, "remove");
		menuItemRemoveElements.addActionListener(actionEvent -> removeElements());
		jPopupMenu.add(menuItemRemoveElements);
	}

	private void makeSubGroup() {
		DefaultMutableTreeNode resultNode = tree.makeSubgroupAt(mousePath);
		if (resultNode != null) {
			tree.makeVisible(mousePath.pathByAddingChild(resultNode));
			tree.repaint();
		}
	}

	private void removeElements() {
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

		menuItemCreateNode.setVisible(false);
		menuItemEditNode.setVisible(false);
		menuItemDeleteNode.setVisible(false);
		menuItemDeleteGroupNode.setVisible(false);
		menuItemRemoveElements.setVisible(false);

		int numberVisibleItems = 0;

		DefaultMutableTreeNode clickNode = (DefaultMutableTreeNode) mousePath.getLastPathComponent();
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
