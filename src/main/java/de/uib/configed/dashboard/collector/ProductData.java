/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.Helper;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

public final class ProductData {
	private static Map<String, List<String>> products = new HashMap<>();
	private static Map<String, List<String>> netbootProducts = new HashMap<>();
	private static Map<String, List<String>> localbootProducts = new HashMap<>();
	private static Map<String, Map<Product, Product>> installedProducts = new HashMap<>();
	private static Map<String, Map<Product, Product>> failedProducts = new HashMap<>();
	private static Map<String, Map<Product, Product>> unusedProducts = new HashMap<>();
	private static Map<String, Map<String, List<String>>> clientUnusedProductsList = new HashMap<>();

	private static int totalOSInstallations;
	private static int totalLinuxInstallations;
	private static int totalWindowsInstallations;
	private static int totalMacOSInstallations;

	private static OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private static String selectedDepot;
	private static List<String> depots = new ArrayList<>(
			persistenceController.getHostInfoCollections().getAllDepots().keySet());

	private ProductData() {
	}

	public static List<String> getProducts() {
		if (products.isEmpty() || !products.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(products.get(selectedDepot));
	}

	private static void retrieveProducts() {
		if (!products.isEmpty()) {
			return;
		}

		for (String depot : depots) {
			if (products.containsKey(Configed.getResourceValue("Dashboard.selection.allDepots"))) {
				List<String> allDepotProducts = products
						.get(Configed.getResourceValue("Dashboard.selection.allDepots"));
				allDepotProducts.addAll(persistenceController.getAllProductNames(depot));
				products.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allDepotProducts);
			} else {
				products.put(Configed.getResourceValue("Dashboard.selection.allDepots"),
						persistenceController.getAllProductNames(depot));
			}

			products.put(depot, persistenceController.getAllProductNames(depot));
		}
	}

	public static List<String> getNetbootProducts() {
		if (netbootProducts.isEmpty() || !netbootProducts.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(netbootProducts.get(selectedDepot));
	}

	private static void retrieveNetbootProducts() {
		if (!netbootProducts.isEmpty()) {
			return;
		}

		for (String depot : depots) {
			List<String> netbootProductsList = persistenceController.getProvidedNetbootProducts(depot);
			netbootProducts.put(depot, netbootProductsList);
		}

		List<String> allNetbootProducts = Helper.combineListsFromMap(netbootProducts);
		netbootProducts.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allNetbootProducts);
	}

	public static List<String> getLocalbootProducts() {
		if (localbootProducts.isEmpty() || !localbootProducts.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(localbootProducts.get(selectedDepot));
	}

	private static void retrieveLocalbootProducts() {
		if (!localbootProducts.isEmpty()) {
			return;
		}

		for (String depot : depots) {
			List<String> localbootProductsList = persistenceController.getProvidedLocalbootProducts(depot);
			localbootProducts.put(depot, localbootProductsList);
		}

		List<String> allLocalbootProducts = Helper.combineListsFromMap(localbootProducts);
		localbootProducts.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allLocalbootProducts);
	}

	public static Map<Product, Product> getInstalledProducts() {
		if (installedProducts.isEmpty() || !installedProducts.containsKey(selectedDepot)) {
			return new HashMap<>();
		}

		return new HashMap<>(installedProducts.get(selectedDepot));
	}

	public static Map<Product, Product> getFailedProducts() {
		if (failedProducts.isEmpty() || !failedProducts.containsKey(selectedDepot)) {
			return new HashMap<>();
		}

		return new HashMap<>(failedProducts.get(selectedDepot));
	}

	public static Map<Product, Product> getUnusedProducts() {
		if (unusedProducts.isEmpty() || !unusedProducts.containsKey(selectedDepot)) {
			return new HashMap<>();
		}

		return new HashMap<>(unusedProducts.get(selectedDepot));
	}

	private static void retrieveInstalledAndFailedProducts() {
		if (!installedProducts.isEmpty() && !failedProducts.isEmpty() && !unusedProducts.isEmpty()) {
			return;
		}

		installedProducts.clear();
		failedProducts.clear();
		unusedProducts.clear();

		for (String depot : depots) {
			List<String> allUnusedProducts = persistenceController.getAllProductNames(depot);
			Map<Product, Product> installedProductsList = new HashMap<>();
			Map<Product, Product> failedProductsList = new HashMap<>();
			Map<Product, Product> unusedProductsList = new HashMap<>();
			Map<String, List<String>> clientUnusedProducts = new HashMap<>();

			List<String> clientsMap = persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps().values()
					.stream().filter(v -> depot.equals(v.getInDepot())).map(HostInfo::getName)
					.collect(Collectors.toList());
			String[] clientIds = clientsMap.toArray(new String[0]);
			Map<String, List<Map<String, String>>> productsStatesAndActions = persistenceController
					.getMapOfProductStatesAndActions(clientIds);

			if (!productsStatesAndActions.isEmpty()) {
				for (Map.Entry<String, List<Map<String, String>>> entry : productsStatesAndActions.entrySet()) {
					String hostname = entry.getKey();

					for (Map<String, String> productInfo : entry.getValue()) {
						String productId = productInfo.get("productId");

						Product product = new Product();
						product.setId(productId);
						product.setDepot(depot);

						if ("installed".equals(productInfo.get("installationStatus"))) {
							product.setStatus(Configed.getResourceValue("Dashboard.products.installed"));

							Optional<Product> matchedProduct = installedProductsList.keySet().stream()
									.filter(p -> p.getId().equals(productId) && p.getStatus()
											.equals(Configed.getResourceValue("Dashboard.products.installed")))
									.findFirst();

							if (matchedProduct.isPresent()) {
								List<String> clients = matchedProduct.get().getClients();
								clients.add(hostname);
								product.setClients(clients);
								installedProductsList.replace(matchedProduct.get(), product);
							} else {
								List<String> clients = new ArrayList<>();
								clients.add(hostname);
								product.setClients(clients);
								installedProductsList.put(product, product);
							}

							allUnusedProducts.remove(productId);
						} else if ("failed".equals(productInfo.get("actionResult"))) {
							product.setStatus(Configed.getResourceValue("Dashboard.products.failed"));

							Optional<Product> matchedProduct = failedProductsList.keySet().stream()
									.filter(p -> p.getId().equals(productId) && p.getStatus()
											.equals(Configed.getResourceValue("Dashboard.products.failed")))
									.findFirst();

							if (matchedProduct.isPresent()) {
								List<String> clients = matchedProduct.get().getClients();
								clients.add(hostname);
								product.setClients(clients);
								failedProductsList.replace(matchedProduct.get(), product);
							} else {
								List<String> clients = new ArrayList<>();
								clients.add(hostname);
								product.setClients(clients);
								failedProductsList.put(product, product);
							}

							allUnusedProducts.remove(productId);
						} else {
							// Do nothing when product not installed or installation failed
						}
					}

					clientUnusedProducts.put(hostname, allUnusedProducts);
				}

				clientUnusedProductsList.put(depot, clientUnusedProducts);
			} else {
				allUnusedProducts.forEach((String productId) -> {
					if (installedProductsList.keySet().stream().noneMatch(p -> p.getId().equals(productId))
							&& failedProductsList.keySet().stream().noneMatch(p -> p.getId().equals(productId))
							&& unusedProductsList.keySet().stream().noneMatch(p -> p.getId().equals(productId))) {

						Product product = createProduct(productId, depot);

						unusedProductsList.put(product, product);
					}
				});
			}

			installedProducts.put(depot, installedProductsList);
			failedProducts.put(depot, failedProductsList);
			unusedProducts.put(depot, unusedProductsList);
		}

		Map<Product, Product> allInstalledProducts = Helper.combineMapsFromMap2(installedProducts);
		Map<Product, Product> allFailedProducts = Helper.combineMapsFromMap2(failedProducts);
		Map<Product, Product> allUnusedProducts = Helper.combineMapsFromMap2(unusedProducts);

		installedProducts.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allInstalledProducts);
		failedProducts.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allFailedProducts);
		unusedProducts.put(Configed.getResourceValue("Dashboard.selection.allDepots"), allUnusedProducts);
	}

	private static Product createProduct(String productId, String depot) {

		Product product = new Product();
		product.setId(productId);
		product.setDepot(depot);
		product.setStatus(Configed.getResourceValue("Dashboard.products.unused"));
		product.setClients(new ArrayList<>());

		return product;
	}

	public static void retrieveUnusedProducts() {
		depots.forEach((String depot) -> {

			if (clientUnusedProductsList.get(depot) != null) {

				unusedProducts.put(depot, createUnusedProductList(depot));
			}
		});

		Map<Product, Product> allUnusedProducts = Helper.combineMapsFromMap2(unusedProducts);
		unusedProducts.get(selectedDepot).putAll(allUnusedProducts);
	}

	private static Map<Product, Product> createUnusedProductList(String depot) {
		Map<Product, Product> unusedProductsList = new HashMap<>();

		for (Entry<String, List<String>> entry : clientUnusedProductsList.get(depot).entrySet()) {
			String hostname = entry.getKey();

			entry.getValue().forEach(
					(String productId) -> addUnusedProductToList(depot, productId, hostname, unusedProductsList));
		}

		return unusedProductsList;
	}

	private static void addUnusedProductToList(String depot, String productId, String hostname,
			Map<Product, Product> unusedProductsList) {
		Product product = new Product();
		product.setDepot(depot);
		product.setId(productId);
		product.setStatus(Configed.getResourceValue("Dashboard.products.unused"));

		Optional<Product> matchedProduct = unusedProductsList.keySet().stream().filter(p -> p.getId().equals(productId)
				&& p.getStatus().equals(Configed.getResourceValue("Dashboard.products.unused"))).findFirst();

		if (matchedProduct.isPresent()) {
			List<String> clientss = matchedProduct.get().getClients();
			clientss.add(hostname);
			product.setClients(clientss);
			unusedProductsList.replace(matchedProduct.get(), product);
		} else {
			List<String> clientss = new ArrayList<>();
			clientss.add(hostname);
			product.setClients(clientss);
			unusedProductsList.put(product, product);
		}
	}

	public static int getTotalOSInstallations() {
		return totalOSInstallations;
	}

	public static int getTotalLinuxInstallations() {
		return totalLinuxInstallations;
	}

	public static int getTotalWindowsInstallations() {
		return totalWindowsInstallations;
	}

	public static int getTotalMacOSInstallations() {
		return totalMacOSInstallations;
	}

	private static void retrieveInstalledOS() {
		if (totalOSInstallations != 0 && totalLinuxInstallations != 0 && totalWindowsInstallations != 0
				&& totalMacOSInstallations != 0) {
			return;
		}

		Map<String, Integer> installedOSs = persistenceController.getInstalledOsOverview();

		if (installedOSs.isEmpty()) {
			return;
		}

		totalOSInstallations = installedOSs.get("all");
		totalLinuxInstallations = installedOSs.get("linux");
		totalWindowsInstallations = installedOSs.get("windows");
		totalMacOSInstallations = installedOSs.get("macos");
	}

	public static void clear() {
		products.clear();
		netbootProducts.clear();
		localbootProducts.clear();
		installedProducts.clear();
		failedProducts.clear();
		unusedProducts.clear();
	}

	public static void retrieveData(String depot) {
		selectedDepot = depot;

		retrieveProducts();
		retrieveLocalbootProducts();
		retrieveNetbootProducts();

		retrieveInstalledOS();
		retrieveInstalledAndFailedProducts();
	}
}
