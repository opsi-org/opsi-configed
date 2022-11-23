/*
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2022 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 * 
 * Models the Dependencies Tree; Creates a graph of all dependencies for
 * a given depot and can create a Tree of the dependencies of any product;
 * For both directions ('requires' or 'is required by')
 * 
 * @author Nils Otto
 
 */

package de.uib.configed.guidata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;

public class DependenciesTreeModel {

	private Graph graph;

	private List<String> productList;
	private Map<String, Integer> productMap;

	private String productId = "";

	private boolean graphIsInitialized;
	// private boolean isCyclic;

	private PersistenceController pc;

	// Nur Persistencecontroller verwenden
	public DependenciesTreeModel(PersistenceController pc) {
		this.pc = pc;

		graphIsInitialized = false;
		// isCyclic = false;
	}

	private void initGraph(String depotId) {

		if (depotId == null)
			depotId = pc.getDepot();

		// Erstmal die Dependencies laden
		Map<String, java.util.List<Map<String, String>>> dependencies = pc.getProductDependencies(depotId);
		// <productId> requiredProductId, '"Lösung"'

		productList = new LinkedList<>(dependencies.keySet());

		for (List<Map<String, String>> dependenciesForOneProduct : dependencies.values()) {
			for (Map<String, String> dependency : dependenciesForOneProduct) {
				productList.add(dependency.get("requiredProductId"));
			}
		}

		// productList wird benötigt für die Zuordnung

		/*
		 * productList = new LinkedList<>();
		 * //productList.addAll(dependencies.keySet());
		 * if(pc.getDepot2LocalbootProducts().get(depotId) != null)
		 * productList.addAll(pc.getDepot2LocalbootProducts().get(depotId).keySet());
		 */

		// Die Map zur productList
		productMap = new HashMap<>();

		Iterator<String> productIterator = productList.iterator();
		int i = 0;
		while (productIterator.hasNext()) {
			productMap.put(productIterator.next(), i);
			i++;
		}

		graph = new Graph(productList.size());
		graph.addEdges(dependencies, productMap);

		graphIsInitialized = true;
	}

	public void setActualProduct(String depotId, String productId) {
		this.productId = productId;

		initGraph(depotId);
	}

	public DefaultMutableTreeNode getTreeNodeForProductDependencies(boolean benoetigt) {

		logging.debug(this, productId + "-tree wird erstellt");

		if (graphIsInitialized && productList.contains(productId)) {
			DefaultMutableTreeNode mainNode;

			if (benoetigt)
				mainNode = graph.getTreeDerBenoetigtenProdukte(productId);
			else
				mainNode = graph.getTreeDerAbhaengigenProdukte(productId);

			// Return only if taller than null
			if (mainNode.getChildCount() > 0)
				return mainNode;

			else
				return null;
		}

		else
			return null;
	}

	public String getListOfTreeNodes(DefaultMutableTreeNode root) {
		Set<String> setOfTreeNodes = graph.getRecursiveSetOfTreeNodes(root);

		List<String> sortedList = new LinkedList<>(setOfTreeNodes);
		Collections.sort(sortedList);

		String listAsString = "";
		if (!sortedList.isEmpty())
			listAsString += sortedList.remove(0);

		for (String productId : sortedList)
			listAsString += "\n" + productId;

		return listAsString;
	}

	/*
	 * Mit dieser Klasse lässt sich ein gerichteter Graph erstellen,
	 * und dann testen, ob dieser einen Zykel enthält
	 */
	private class Graph {

		private final int V;
		private final List<List<Integer>> adj;

		public Graph(int V) {
			this.V = V;
			adj = new ArrayList<>(V);

			for (int i = 0; i < V; i++)
				adj.add(new LinkedList<>());
		}

		/*
		 * private boolean isCyclicUtil(int i, boolean[] visited,
		 * boolean[] recStack) {
		 * 
		 * if (recStack[i])
		 * return true;
		 * 
		 * if (visited[i])
		 * return false;
		 * 
		 * visited[i] = true;
		 * 
		 * recStack[i] = true;
		 * List<Integer> children = adj.get(i);
		 * 
		 * for (Integer c: children)
		 * if (isCyclicUtil(c, visited, recStack))
		 * return true;
		 * 
		 * recStack[i] = false;
		 * 
		 * return false;
		 * }
		 * 
		 * // Testet, ob der Graph einen Zykel enthält
		 * private boolean isCyclic() {
		 * 
		 * boolean[] visited = new boolean[V];
		 * boolean[] recStack = new boolean[V];
		 * 
		 * for (int i = 0; i < V; i++)
		 * if (isCyclicUtil(i, visited, recStack))
		 * return true;
		 * 
		 * return false;
		 * }
		 */

		// Fügt eine (gerichtete) Kante an den Graphen hinzu
		private void addEdge(int source, int dest) {
			adj.get(source).add(dest);
		}

		public void addEdges(Map<String, java.util.List<Map<String, String>>> dependencies,
				Map<String, Integer> productMap) {
			for (Map.Entry<String, java.util.List<Map<String, String>>> entry : dependencies.entrySet()) {
				Object fst = productMap.get(entry.getKey());

				if (fst == null)
					continue;

				int first = (int) fst;

				for (Map<String, String> dependenciesElement : entry.getValue()) {
					Object sec = productMap.get(dependenciesElement.get("requiredProductId"));

					if (sec == null)
						continue;

					int second = (int) sec;

					addEdge(first, second);

					/*
					 * switch(requirementType) {
					 * case "before":
					 * addEdge(first, second);
					 * break;
					 * 
					 * case "after":
					 * addEdge(second, first);
					 * break;
					 * }
					 */
				}
			}
		}

		public boolean isPartOfPath(DefaultMutableTreeNode node, String productId) {
			while (node != null) {
				if (node.getUserObject().equals(productId))
					return true;

				node = (DefaultMutableTreeNode) node.getParent();
			}

			return false;
		}

		public DefaultMutableTreeNode getTreeDerAbhaengigenProdukte(String productId) {
			DefaultMutableTreeNode abhaengigeProdukte = new DefaultMutableTreeNode(productId);

			addRecursiveAbhaengigeProdukte(abhaengigeProdukte);

			return abhaengigeProdukte;
		}

		public void addRecursiveAbhaengigeProdukte(DefaultMutableTreeNode node) {
			List<String> childStrings = new LinkedList<>();

			int product = productMap.get(node.toString());

			for (int i = 0; i < V; i++) {
				if (adj.get(i).contains(product)) {
					String productId = productList.get(i);

					if (!isPartOfPath(node, productId))
						childStrings.add(productId);
				}
			}

			// Sort list alphabetically
			childStrings.sort(Comparator.naturalOrder());

			for (String childString : childStrings) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childString);
				node.add(childNode);
				addRecursiveAbhaengigeProdukte(childNode);
			}
		}

		public DefaultMutableTreeNode getTreeDerBenoetigtenProdukte(String productId) {
			DefaultMutableTreeNode benoetigteProdukte = new DefaultMutableTreeNode(productId);

			addRecursiveBenoetigteProdukte(benoetigteProdukte);

			return benoetigteProdukte;
		}

		public void addRecursiveBenoetigteProdukte(DefaultMutableTreeNode node) {
			List<String> childStrings = new LinkedList<>();

			for (int i : adj.get(productMap.get(node.toString()))) {
				String productId = productList.get(i);

				if (!isPartOfPath(node, productId))
					childStrings.add(productId);
			}
			// Sort list alphabetically
			childStrings.sort(Comparator.naturalOrder());

			for (String childString : childStrings) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childString);
				node.add(childNode);
				addRecursiveBenoetigteProdukte(childNode);
			}
		}

		public Set<String> getRecursiveSetOfTreeNodes(DefaultMutableTreeNode root) {
			Set<String> setOfTreeNodes = new HashSet<>();
			return getRecursiveSetOfTreeNodes(setOfTreeNodes, root);
		}

		public Set<String> getRecursiveSetOfTreeNodes(Set<String> setOfTreeNodes, DefaultMutableTreeNode node) {
			Enumeration<TreeNode> children = node.children();

			while (children.hasMoreElements()) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
				setOfTreeNodes.add(childNode.getUserObject().toString());
				getRecursiveSetOfTreeNodes(setOfTreeNodes, childNode);
			}

			return setOfTreeNodes;
		}
	}
}
