/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.productpage.PanelProductSettings;
import de.uib.configed.gui.share.swing.ButtonTabComponent;
import de.uib.configed.gui.type.Object2GroupEntry;
import de.uib.configed.share.logging.Logging;

public class ProductTree extends AbstractGroupTree {
	private PanelProductSettings localbootPanel;
	private PanelProductSettings netbootPanel;

	public ProductTree(ConfigedMain configedMain) {
		super(configedMain);

		super.expandPath(new TreePath(groupNodeAllObjects.getPath()));
	}

	public void setPanels(PanelProductSettings localbootPanel, PanelProductSettings netbootPanel) {
		this.localbootPanel = localbootPanel;
		this.netbootPanel = netbootPanel;

		setSelectionPath(new TreePath(groupNodeAllObjects.getPath()));
	}

	@Override
	protected void createTree() {
		List<String> depotIds = configedMain.getSelectedDepots();
		Set<String> productIds = persistenceController.getDataServices().product.getAllLocalbootProductNames(depotIds);
		productIds.addAll(persistenceController.getDataServices().product.getAllNetbootProductNames(depotIds));

		Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

		Map<String, Map<String, String>> productGroups = persistenceController.getDataServices().group
				.getProductGroupsPD();

		if (!persistenceController.getDataServices().userRoles.hasProductGroupsFullPermissionPD()) {
			Set<String> permittedProductGroups = persistenceController.getDataServices().userRoles
					.getPermittedProductGroupsPD();
			productGroups.keySet().retainAll(permittedProductGroups);
		}

		// Create groups
		for (Entry<String, Map<String, String>> groupEntry : productGroups.entrySet()) {
			GroupNode groupNode = new GroupNode(groupEntry.getKey());
			nodeMap.put(groupEntry.getKey(), groupNode);
			groupNodes.put(groupEntry.getKey(), groupNode);
			groups.put(groupEntry.getKey(), groupEntry.getValue());
		}

		groupNodeGroups = produceGroupNode(ALL_GROUPS_NAME,
				Configed.getResourceValue("ProductTree.groupsName.tooltip"));
		groupNodeGroups.setAllowsOnlyGroupChilds(true);
		groupNodeGroups.setFixed(true);

		groupNodeAllObjects = produceGroupNode(Configed.getResourceValue("ProductTree.allProducts"),
				Configed.getResourceValue("ProductTree.allProducts.tooltip"));
		groupNodeAllObjects.setImmutable(true);
		groupNodeAllObjects.setFixed(true);

		for (Entry<String, Map<String, String>> groupEntry : productGroups.entrySet()) {
			if ("null".equals(groupEntry.getValue().get("parentGroupId"))
					|| nodeMap.get(groupEntry.getValue().get("parentGroupId")) == null) {
				groupNodeGroups.add(nodeMap.get(groupEntry.getKey()));
			} else {
				nodeMap.get(groupEntry.getValue().get("parentGroupId")).add(nodeMap.get(groupEntry.getKey()));
			}
		}

		Map<String, Set<String>> allowedGroups2Members = persistenceController.getDataServices().group
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
			groupNodeAllObjects.add(new DefaultMutableTreeNode(productId, false));
		}

		rootNode.add(groupNodeGroups);
		rootNode.add(groupNodeAllObjects);
	}

	@Override
	public void removeNodeInternally(String clientID, GroupNode parentNode) {
		DefaultMutableTreeNode clientNode = getChildWithUserObjectString(clientID, parentNode);
		parentNode.remove(clientNode);

		getModel().nodeStructureChanged(parentNode);

		repaint();
	}

	@Override
	public void moveObjectTo(String importID, String sourceParentID, GroupNode sourceParentNode,
			DefaultMutableTreeNode dropParentNode, TreePath dropPath, String dropParentID) {
		// This method is never invoked in the product tree
	}

	@Override
	public void copyObjectTo(String objectID, String newParentID, DefaultMutableTreeNode newParentNode,
			TreePath newParentPath) {
		if (getChildWithUserObjectString(objectID, newParentNode) == null) {
			newParentNode.add(new DefaultMutableTreeNode(objectID, false));

			persistenceController.getDataServices().group.addObject2Group(objectID, newParentID,
					Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);

			model.nodeStructureChanged(newParentNode);

			makeVisible(newParentPath.pathByAddingChild(objectID));
		}
	}

	@Override
	public Set<String> getSelectedObjectsInTable() {
		Set<String> selectedProducts = localbootPanel.getProductTableModified().getSelectedIDs();
		selectedProducts.addAll(netbootPanel.getProductTableModified().getSelectedIDs());
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
	public void setGroupAndSelect(DefaultMutableTreeNode groupNode) {
		Set<String> productIds = getChildrenRecursively(groupNode);
		setFilter(productIds);
		localbootPanel.getProductTableModified().setPendingSelection(productIds);
		netbootPanel.getProductTableModified().setPendingSelection(productIds);
	}

	@Override
	public void setGroupsAndSelect(DefaultMutableTreeNode[] groupNodes) {
		Set<String> productIds = new HashSet<>();
		Set<String> selectedProductIds = new HashSet<>();
		boolean anyIsLeaf = false;
		for (DefaultMutableTreeNode groupNode : groupNodes) {
			anyIsLeaf = anyIsLeaf || groupNode.isLeaf();
			if (groupNode.isLeaf() && !groupNode.getAllowsChildren()) {
				selectedProductIds.add(groupNode.getUserObject().toString());
				productIds.add(groupNode.getUserObject().toString());
			} else {
				productIds.addAll(getChildrenRecursively(groupNode));
			}
		}
		setFilter(productIds);
		if (anyIsLeaf) {
			Logging.devel(this, "selectedProductIds ", selectedProductIds);
			localbootPanel.getProductTableModified().setPendingSelection(selectedProductIds);
			netbootPanel.getProductTableModified().setPendingSelection(selectedProductIds);
		}
		Logging.debug("ProductTree.setGroupsAndSelect productIds " + productIds);
		Logging.debug("ProductTree.setGroupsAndSelect selectedProductIds " + selectedProductIds);
	}

	private void setFilter(Set<String> productIds) {
		localbootPanel.getProductTableModified().setFilter(productIds);
		netbootPanel.getProductTableModified().setFilter(productIds);
	}

	@Override
	public void reactOnTreeSelection() {
		if (ConfigedMain.getMainFrame() != null) {
			ButtonTabComponent comp = (ButtonTabComponent) ConfigedMain.getMainFrame().getMainPanelManager()
					.getTabbedPane().getTabComponentAt(2);
			comp.showButton(getSelectionPaths() == null
					|| !Configed.getResourceValue("ProductTree.allProducts")
							.equals(getSelectionPath().getLastPathComponent().toString())
					|| getSelectionPaths().length > 1);
		}
		localbootPanel.valueChanged(true);
		netbootPanel.valueChanged(true);
	}
}
