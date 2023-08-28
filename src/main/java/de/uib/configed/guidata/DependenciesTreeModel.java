/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class DependenciesTreeModel {

	private Graph graph;

	private List<String> productList;
	private Map<String, Integer> productMap;

	private String mainProductId = "";

	private boolean graphIsInitialized;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public DependenciesTreeModel() {
		// Leave constructor empty since values will be provided when initGraph() is called
	}

	private void initGraph(String depotId) {

		if (depotId == null) {
			depotId = persistenceController.getDepot();
		}

		// Erstmal die Dependencies laden
		Map<String, List<Map<String, String>>> dependencies = persistenceController.getProductDependencies(depotId);

		productList = new LinkedList<>(dependencies.keySet());

		for (List<Map<String, String>> dependenciesForOneProduct : dependencies.values()) {
			for (Map<String, String> dependency : dependenciesForOneProduct) {
				productList.add(dependency.get("requiredProductId"));
			}
		}

		// productList wird benötigt für die Zuordnung

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
		this.mainProductId = productId;

		initGraph(depotId);
	}

	public DefaultMutableTreeNode getTreeNodeForProductDependencies(boolean benoetigt) {

		Logging.debug(this, mainProductId + "-tree wird erstellt");

		if (graphIsInitialized && productMap.containsKey(mainProductId)) {
			DefaultMutableTreeNode mainNode;

			if (benoetigt) {
				mainNode = graph.getTreeDerBenoetigtenProdukte(mainProductId, productMap, productList);
			} else {
				mainNode = graph.getTreeDerAbhaengigenProdukte(mainProductId, productMap, productList);
			}

			// Return only if taller than null
			if (mainNode.getChildCount() > 0) {
				return mainNode;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getListOfTreeNodes(DefaultMutableTreeNode root) {
		Set<String> setOfTreeNodes = graph.getRecursiveSetOfTreeNodes(root);

		List<String> sortedList = new LinkedList<>(setOfTreeNodes);
		Collections.sort(sortedList);

		StringBuilder listAsString = new StringBuilder();
		if (!sortedList.isEmpty()) {
			listAsString.append(sortedList.remove(0));
		}

		for (String productId : sortedList) {
			listAsString.append("\n" + productId);
		}

		return listAsString.toString();
	}
}
