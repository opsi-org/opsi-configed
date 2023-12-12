/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.itextpdf.text.Font;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class ProductTree extends JTree {

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private Map<String, DefaultMutableTreeNode> nodeMap;

	public ProductTree() {
		nodeMap = new HashMap<>();

		for (Entry<String, Map<String, String>> groupEntry : persistenceController.getGroupDataService()
				.getProductGroupsPD().entrySet()) {
			nodeMap.put(groupEntry.getKey(), new DefaultMutableTreeNode(groupEntry.getKey(), true));
		}

		setModel();
	}

	private void setModel() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		for (Entry<String, Map<String, String>> groupEntry : persistenceController.getGroupDataService()
				.getProductGroupsPD().entrySet()) {
			if ("null".equals(groupEntry.getValue().get("parentGroupId"))) {
				root.add(nodeMap.get(groupEntry.getKey()));
			} else {
				nodeMap.get(groupEntry.getValue().get("parentGroupId")).add(nodeMap.get(groupEntry.getKey()));
			}
		}

		for (Entry<String, Set<String>> groupMembers : persistenceController.getGroupDataService()
				.getFProductGroup2Members().entrySet()) {
			DefaultMutableTreeNode groupNode = nodeMap.get(groupMembers.getKey());

			for (String productId : groupMembers.getValue()) {
				groupNode.add(new DefaultMutableTreeNode(productId, false));
			}
		}

		TreeModel treeModel = new DefaultTreeModel(root);
		setModel(treeModel);

		setCellRenderer(new ProductTreeNodeRenderer());
	}

	private static class ProductTreeNodeRenderer extends DefaultTreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if (((DefaultMutableTreeNode) value).getAllowsChildren()) {
				((JLabel) c).setFont(getFont().deriveFont(Font.NORMAL));
			} else {
				((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
			}

			return c;
		}
	}
}
