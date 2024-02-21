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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.itextpdf.text.Font;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.productpage.PanelProductSettings;

public class ProductTree extends AbstractGroupTree {
	public static final String ALL_PRODUCT_GROUPS_NAME = "Alle Gruppen";

	private PanelProductSettings localbootPanel;
	private PanelProductSettings netbootPanel;

	public ProductTree(ConfigedMain configedMain) {
		super(configedMain);

		setModel();
	}

	private void setModel() {
		setCellRenderer(new ProductTreeNodeRenderer());

		expandPath(new TreePath(groupNodeFullList.getPath()));
	}

	public void setLocalbootPanel(PanelProductSettings localbootPanel) {
		this.localbootPanel = localbootPanel;
	}

	public void setNetbootPanel(PanelProductSettings netbootPanel) {
		this.netbootPanel = netbootPanel;
	}

	@Override
	protected void createTopNodes() {
		Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

		// Create groups
		for (Entry<String, Map<String, String>> groupEntry : persistenceController.getGroupDataService()
				.getProductGroupsPD().entrySet()) {
			GroupNode groupNode = new GroupNode(groupEntry.getKey());
			nodeMap.put(groupEntry.getKey(), groupNode);
			groupNodes.put(groupEntry.getKey(), groupNode);
			groups.put(groupEntry.getKey(), groupEntry.getValue());
		}

		groupNodeGroups = new GroupNode(ALL_PRODUCT_GROUPS_NAME);
		groupNodeGroups.setAllowsOnlyGroupChilds(true);
		groupNodeGroups.setFixed(true);

		groupNodes.put(groupNodeGroups.toString(), groupNodeGroups);

		groupNodeFullList = new GroupNode("Alle Produkte");
		groupNodeFullList.setImmutable(true);
		groupNodeFullList.setFixed(true);

		groupNodes.put(groupNodeFullList.toString(), groupNodeFullList);

		for (Entry<String, Map<String, String>> groupEntry : persistenceController.getGroupDataService()
				.getProductGroupsPD().entrySet()) {
			if ("null".equals(groupEntry.getValue().get("parentGroupId"))) {
				groupNodeGroups.add(nodeMap.get(groupEntry.getKey()));
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
			groupNodeFullList.add(new DefaultMutableTreeNode(product.get("productId"), false));
		}

		rootNode.add(groupNodeGroups);
		rootNode.add(groupNodeFullList);
	}

	@Override
	public void removeNodeInternally(String clientID, GroupNode parentNode) {
		// TODO
	}

	@Override
	public void moveObjectTo(String importID, TreePath sourcePath, String sourceParentID, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		//TODO
	}

	@Override
	public void copyObjectTo(String objectID, TreePath sourcePath, String newParentID,
			DefaultMutableTreeNode newParentNode, TreePath newParentPath) {

		if (getChildWithUserObjectString(objectID, newParentNode) == null) {
			newParentNode.add(new DefaultMutableTreeNode(objectID, false));

			persistenceController.getGroupDataService().addObject2Group(objectID, newParentID, false);

			Logging.devel(newParentPath.toString());
			model.nodeStructureChanged(newParentNode);

			makeVisible(pathByAddingChild(newParentPath, objectID));
		}
	}

	@Override
	public Set<String> getSelectedObjectsInTable() {

		Set<String> selectedProducts = localbootPanel.getSelectedIDs();
		selectedProducts.addAll(netbootPanel.getSelectedIDs());

		return selectedProducts;
	}

	@Override
	public boolean isInDirectory(String groupName) {
		return false;
	}

	@Override
	public boolean isInDirectory(TreePath path) {
		return false;
	}

	@Override
	public Set<GroupNode> getLocationsInDirectory(String clientId) {
		return new HashSet<>();
	}

	@Override
	public void setGroupAndSelect(DefaultMutableTreeNode groupNode) {
		Set<String> productIds = getChildrenRecursively(groupNode);
		setFilter(productIds);
		localbootPanel.setSelection(productIds);
		netbootPanel.setSelection(productIds);
	}

	private void setGroup(DefaultMutableTreeNode groupNode) {
		Set<String> productIds = getChildrenRecursively(groupNode);
		setFilter(productIds);
	}

	private static Set<String> getChildrenRecursively(DefaultMutableTreeNode groupNode) {
		Set<String> productIds = new HashSet<>();

		addChildrenRecoursively(groupNode.children(), productIds);

		return productIds;
	}

	private static void addChildrenRecoursively(Enumeration<TreeNode> children, Set<String> productIds) {
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();

			if (child.getAllowsChildren()) {
				addChildrenRecoursively(child.children(), productIds);
			} else {
				productIds.add(child.getUserObject().toString());
			}
		}
	}

	private void setFilter(Set<String> productIds) {
		localbootPanel.setFilter(productIds);
		netbootPanel.setFilter(productIds);
	}

	private void setProduct(String productId) {
		Set<String> productSet = Collections.singleton(productId);

		setFilter(productSet);
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
				setFilter(productIds);
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
