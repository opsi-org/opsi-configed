/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/*
	 * Mit dieser Klasse lässt sich ein gerichteter Graph erstellen,
	 * und dann testen, ob dieser einen Zykel enthält
	 */
public class Graph {

	private final int numberOfElements;
	private final List<List<Integer>> adj;

	public Graph(int numberOfElements) {
		this.numberOfElements = numberOfElements;

		adj = new ArrayList<>(numberOfElements);

		for (int i = 0; i < numberOfElements; i++) {
			adj.add(new LinkedList<>());
		}
	}

	// Fügt eine (gerichtete) Kante an den Graphen hinzu
	private void addEdge(int source, int dest) {
		adj.get(source).add(dest);
	}

	public void addEdges(Map<String, List<Map<String, String>>> dependencies, Map<String, Integer> productMap) {
		for (Entry<String, List<Map<String, String>>> entry : dependencies.entrySet()) {
			Integer first = productMap.get(entry.getKey());

			if (first == null) {
				continue;
			}

			for (Map<String, String> dependenciesElement : entry.getValue()) {
				Object sec = productMap.get(dependenciesElement.get("requiredProductId"));

				if (sec == null) {
					continue;
				}

				int second = (int) sec;

				addEdge(first, second);

			}
		}
	}

	private static boolean isPartOfPath(DefaultMutableTreeNode node, String productId) {
		while (node != null) {
			if (node.getUserObject().equals(productId)) {
				return true;
			}

			node = (DefaultMutableTreeNode) node.getParent();
		}

		return false;
	}

	public DefaultMutableTreeNode getTreeDerAbhaengigenProdukte(String productId, Map<String, Integer> productMap,
			List<String> productList) {
		DefaultMutableTreeNode abhaengigeProdukte = new DefaultMutableTreeNode(productId);

		addRecursiveAbhaengigeProdukte(abhaengigeProdukte, productMap, productList);

		return abhaengigeProdukte;
	}

	private void addRecursiveAbhaengigeProdukte(DefaultMutableTreeNode node, Map<String, Integer> productMap,
			List<String> productList) {
		List<String> childStrings = new LinkedList<>();

		int product = productMap.get(node.toString());

		for (int i = 0; i < numberOfElements; i++) {
			if (adj.get(i).contains(product)) {
				String productId = productList.get(i);

				if (!isPartOfPath(node, productId)) {
					childStrings.add(productId);
				}
			}
		}

		// Sort list alphabetically
		childStrings.sort(Comparator.naturalOrder());

		for (String childString : childStrings) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childString);
			node.add(childNode);
			addRecursiveAbhaengigeProdukte(childNode, productMap, productList);
		}
	}

	public DefaultMutableTreeNode getTreeDerBenoetigtenProdukte(String productId, Map<String, Integer> productMap,
			List<String> productList) {
		DefaultMutableTreeNode benoetigteProdukte = new DefaultMutableTreeNode(productId);

		addRecursiveBenoetigteProdukte(benoetigteProdukte, productMap, productList);

		return benoetigteProdukte;
	}

	private void addRecursiveBenoetigteProdukte(DefaultMutableTreeNode node, Map<String, Integer> productMap,
			List<String> productList) {
		List<String> childStrings = new LinkedList<>();

		for (int i : adj.get(productMap.get(node.toString()))) {
			String productId = productList.get(i);

			if (!isPartOfPath(node, productId)) {
				childStrings.add(productId);
			}
		}
		// Sort list alphabetically
		childStrings.sort(Comparator.naturalOrder());

		for (String childString : childStrings) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childString);
			node.add(childNode);
			addRecursiveBenoetigteProdukte(childNode, productMap, productList);
		}
	}

	public Set<String> getRecursiveSetOfTreeNodes(DefaultMutableTreeNode root) {
		Set<String> setOfTreeNodes = new HashSet<>();
		return getRecursiveSetOfTreeNodes(setOfTreeNodes, root);
	}

	private static Set<String> getRecursiveSetOfTreeNodes(Set<String> setOfTreeNodes, DefaultMutableTreeNode node) {
		Enumeration<TreeNode> children = node.children();

		while (children.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
			setOfTreeNodes.add(childNode.getUserObject().toString());
			getRecursiveSetOfTreeNodes(setOfTreeNodes, childNode);
		}

		return setOfTreeNodes;
	}
}
