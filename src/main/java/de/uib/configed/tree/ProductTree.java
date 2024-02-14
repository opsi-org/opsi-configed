/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.itextpdf.text.Font;

import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class ProductTree extends AbstractGroupTree {

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private PanelProductSettings localbootPanel;
	private PanelProductSettings netbootPanel;

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
		DefaultMutableTreeNode groups = new DefaultMutableTreeNode("Produkt-Gruppen");
		DefaultMutableTreeNode allProducts = new DefaultMutableTreeNode("Alle Produkte");

		for (Entry<String, Map<String, String>> groupEntry : persistenceController.getGroupDataService()
				.getProductGroupsPD().entrySet()) {
			if ("null".equals(groupEntry.getValue().get("parentGroupId"))) {
				groups.add(nodeMap.get(groupEntry.getKey()));
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

		for (Map<String, Object> product : persistenceController.getProductDataService().getAllProducts()) {
			allProducts.add(new DefaultMutableTreeNode(product.get("productId"), false));
		}

		rootNode.add(groups);
		rootNode.add(allProducts);

		TreeModel treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);

		setCellRenderer(new ProductTreeNodeRenderer());

		expandPath(new TreePath(allProducts.getPath()));
	}

	public void setLocalbootPanel(PanelProductSettings localbootPanel) {
		this.localbootPanel = localbootPanel;
	}

	public void setNetbootPanel(PanelProductSettings netbootPanel) {
		this.netbootPanel = netbootPanel;
	}

	@Override
	protected void createTopNodes() {
		// TODO
	}

	private void setGroup(DefaultMutableTreeNode groupNode) {
		Set<String> productIds = new HashSet<>();

		addGroupsRecoursively(groupNode.children(), productIds);

		setSelection(productIds);
	}

	private static void addGroupsRecoursively(Enumeration<TreeNode> children, Set<String> productIds) {
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();

			if (child.getAllowsChildren()) {
				addGroupsRecoursively(child.children(), productIds);
			} else {
				productIds.add(child.getUserObject().toString());
			}
		}
	}

	private void setSelection(Set<String> productIds) {
		localbootPanel.setFilter(productIds);
		netbootPanel.setFilter(productIds);
	}

	private void setProduct(String productId) {
		Set<String> productSet = Collections.singleton(productId);

		setSelection(productSet);
		localbootPanel.setSelection(productSet);
		netbootPanel.setSelection(productSet);
	}

	private void nodeSelection(DefaultMutableTreeNode node) {
		if (node.getAllowsChildren()) {
			setGroup(node);
		} else {
			setProduct(node.getUserObject().toString());
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {

		if (getSelectionCount() == 0) {
			localbootPanel.noSelection();
			netbootPanel.noSelection();
		} else if (getSelectionCount() == 1) {
			nodeSelection((DefaultMutableTreeNode) getSelectionPath().getLastPathComponent());
		} else {
			Set<String> productIds = new HashSet<>();
			for (TreePath path : getSelectionPaths()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (!node.getAllowsChildren()) {
					productIds.add(node.getUserObject().toString());
				}
				setSelection(productIds);
			}
		}
	}

	private static class ProductTreeNodeRenderer extends DefaultTreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if (((DefaultMutableTreeNode) value).getAllowsChildren()) {
				setFont(getFont().deriveFont(Font.NORMAL));
			} else {
				setFont(getFont().deriveFont(Font.BOLD));
			}

			return this;
		}
	}
}
