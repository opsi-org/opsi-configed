/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class ProductTree extends JTree {
	private Map<String, DefaultMutableTreeNode> nodeMap;

	public ProductTree() {
		nodeMap = new HashMap<>();

		for (Entry<String, Map<String, String>> groupEntry : PersistenceControllerFactory.getPersistenceController()
				.getGroupDataService().getProductGroupsPD().entrySet()) {
			nodeMap.put(groupEntry.getKey(), new DefaultMutableTreeNode(groupEntry.getKey()));
		}

		setModel();
	}

	private void setModel() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		for (Entry<String, Map<String, String>> groupEntry : PersistenceControllerFactory.getPersistenceController()
				.getGroupDataService().getProductGroupsPD().entrySet()) {
			if ("null".equals(groupEntry.getValue().get("parentGroupId"))) {
				root.add(nodeMap.get(groupEntry.getKey()));
			} else {
				nodeMap.get(groupEntry.getValue().get("parentGroupId")).add(nodeMap.get(groupEntry.getKey()));
			}
		}

		TreeModel treeModel = new DefaultTreeModel(root);

		setModel(treeModel);
	}
}
