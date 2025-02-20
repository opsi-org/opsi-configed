/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.utils.logging.Logging;

public class ProductTree extends AbstractGroupTree {
	private PanelProductSettings localbootPanel;
	private PanelProductSettings netbootPanel;

	public ProductTree(ConfigedMain configedMain) {
		super(configedMain);

		super.expandPath(new TreePath(groupNodeFullList.getPath()));
	}

	public void setPanels(PanelProductSettings localbootPanel, PanelProductSettings netbootPanel) {
		this.localbootPanel = localbootPanel;
		this.netbootPanel = netbootPanel;

		setSelectionPath(new TreePath(groupNodeFullList.getPath()));
	}

	@Override
	protected void createTopNodes() {
		List<String> depotIds = configedMain.getSelectedDepots();
		Set<String> productIds = new TreeSet<>(
				persistenceController.getProductDataService().getAllLocalbootProductNames(depotIds));
		productIds.addAll(persistenceController.getProductDataService().getAllNetbootProductNames(depotIds));

		Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

		// Create groups
		for (Entry<String, Map<String, String>> groupEntry : persistenceController.getGroupDataService()
				.getProductGroupsPD().entrySet()) {
			GroupNode groupNode = new GroupNode(groupEntry.getKey());
			nodeMap.put(groupEntry.getKey(), groupNode);
			groupNodes.put(groupEntry.getKey(), groupNode);
			groups.put(groupEntry.getKey(), groupEntry.getValue());
		}

		groupNodeGroups = produceGroupNode(ALL_GROUPS_NAME,
				Configed.getResourceValue("ProductTree.groupsName.tooltip"));
		groupNodeGroups.setAllowsOnlyGroupChilds(true);
		groupNodeGroups.setFixed(true);

		groupNodeFullList = produceGroupNode(Configed.getResourceValue("ProductTree.allProducts"),
				Configed.getResourceValue("ProductTree.allProducts.tooltip"));
		groupNodeFullList.setImmutable(true);
		groupNodeFullList.setFixed(true);

		for (Entry<String, Map<String, String>> groupEntry : persistenceController.getGroupDataService()
				.getProductGroupsPD().entrySet()) {
			if ("null".equals(groupEntry.getValue().get("parentGroupId"))
					|| nodeMap.get(groupEntry.getValue().get("parentGroupId")) == null) {
				groupNodeGroups.add(nodeMap.get(groupEntry.getKey()));
			} else {
				nodeMap.get(groupEntry.getValue().get("parentGroupId")).add(nodeMap.get(groupEntry.getKey()));
			}
		}

		Map<String, Set<String>> allowedGroups2Members = persistenceController.getGroupDataService()
				.getFProductGroup2Members();
		allowedGroups2Members.keySet().retainAll(nodeMap.keySet());

		for (Entry<String, Set<String>> groupMembers : allowedGroups2Members.entrySet()) {
			DefaultMutableTreeNode groupNode = nodeMap.get(groupMembers.getKey());

			for (String productId : groupMembers.getValue()) {
				if (productIds.contains(productId)) {
					groupNode.add(new DefaultMutableTreeNode(productId, false));
				}
			}
		}

		for (String productId : productIds) {
			groupNodeFullList.add(new DefaultMutableTreeNode(productId, false));
		}

		rootNode.add(groupNodeGroups);
		rootNode.add(groupNodeFullList);
	}

	@Override
	public void removeNodeInternally(String clientID, GroupNode parentNode) {
		DefaultMutableTreeNode clientNode = getChildWithUserObjectString(clientID, parentNode);
		parentNode.remove(clientNode);

		getModel().nodeStructureChanged(parentNode);

		repaint();
	}

	@Override
	public void moveObjectTo(String importID, TreePath sourcePath, String sourceParentID, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		// This method is never invoked in the product tree
	}

	@Override
	public void copyObjectTo(String objectID, TreePath sourcePath, String newParentID,
			DefaultMutableTreeNode newParentNode, TreePath newParentPath) {
		if (getChildWithUserObjectString(objectID, newParentNode) == null) {
			newParentNode.add(new DefaultMutableTreeNode(objectID, false));

			persistenceController.getGroupDataService().addObject2Group(objectID, newParentID,
					Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);

			model.nodeStructureChanged(newParentNode);

			makeVisible(newParentPath.pathByAddingChild(objectID));
		}
	}

	@Override
	public Set<String> getSelectedObjectsInTable() {
		Set<String> selectedProducts = localbootPanel.getProductTable().getSelectedIDs();
		selectedProducts.addAll(netbootPanel.getProductTable().getSelectedIDs());
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
		localbootPanel.getProductTable().setSelection(productIds);
		netbootPanel.getProductTable().setSelection(productIds);
	}

	@Override
	public void setGroupsAndSelect(DefaultMutableTreeNode[] groupNodes) {
		Set<String> productIds = new HashSet<>();
		Set<String> selectedProductIds = new HashSet<>();
		boolean anyIsLeaf = false;
		for (DefaultMutableTreeNode groupNode : groupNodes) {
			anyIsLeaf = anyIsLeaf || groupNode.isLeaf();
			Logging.info(this, "setGroupsAndSelect groupNode: ", groupNode, " isLeaf: ", anyIsLeaf);
			if (groupNode.isLeaf() && !groupNode.getAllowsChildren()) {
				selectedProductIds.add(groupNode.getUserObject().toString());
				productIds.add(groupNode.getUserObject().toString());
			} else {
				productIds.addAll(getChildrenRecursively(groupNode));
			}
		}
		setFilter(productIds);
		if (anyIsLeaf) {
			localbootPanel.getProductTable().setSelection(selectedProductIds);
			netbootPanel.getProductTable().setSelection(selectedProductIds);
		}
	}

	private void setFilter(Set<String> productIds) {
		localbootPanel.getProductTable().setFilter(productIds);
		netbootPanel.getProductTable().setFilter(productIds);
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		localbootPanel.valueChanged(true);
		netbootPanel.valueChanged(true);
	}
}
