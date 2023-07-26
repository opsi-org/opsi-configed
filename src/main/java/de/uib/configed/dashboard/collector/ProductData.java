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
import java.util.Optional;
import java.util.stream.Collectors;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.Helper;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.OpsiPackage;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

public final class ProductData {
	private static Map<String, List<String>> products = new HashMap<>();
	private static Map<String, List<String>> netbootProducts = new HashMap<>();
	private static Map<String, List<String>> localbootProducts = new HashMap<>();
	private static Map<String, Map<Product, Product>> installedProducts = new HashMap<>();
	private static Map<String, Map<Product, Product>> failedProducts = new HashMap<>();
	private static Map<String, Map<Product, Product>> unusedProducts = new HashMap<>();
	private static Map<String, List<String>> tmpUnusedProductsList = new HashMap<>();

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

	public static List<String> getNetbootProducts() {
		if (netbootProducts.isEmpty() || !netbootProducts.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(netbootProducts.get(selectedDepot));
	}

	public static List<String> getLocalbootProducts() {
		if (localbootProducts.isEmpty() || !localbootProducts.containsKey(selectedDepot)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(localbootProducts.get(selectedDepot));
	}

	private static void retrieveProducts() {
		if (!products.isEmpty() && !localbootProducts.isEmpty() && !netbootProducts.isEmpty()) {
			return;
		}

		for (String depot : depots) {
			List<Map<String, Object>> allProductsInDepot = persistenceController.getAllProductsInDepot(depot);
			Helper.fillMapOfListsForDepots(products,
					allProductsInDepot.stream().map(v -> (String) v.get("productId")).collect(Collectors.toList()),
					depot);
			Helper.fillMapOfListsForDepots(netbootProducts,
					allProductsInDepot.stream()
							.filter(v -> v.get("productType").equals(OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING))
							.map(v -> (String) v.get("productId")).collect(Collectors.toList()),
					depot);
			Helper.fillMapOfListsForDepots(localbootProducts,
					allProductsInDepot.stream()
							.filter(v -> v.get("productType").equals(OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING))
							.map(v -> (String) v.get("productId")).collect(Collectors.toList()),
					depot);
		}
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
			List<String> allUnusedProducts = new ArrayList<>(products.get(depot));
			Map<Product, Product> installedProductsList = new HashMap<>();
			Map<Product, Product> failedProductsList = new HashMap<>();
			Map<Product, Product> unusedProductsList = new HashMap<>();

			List<String> clientsMap = persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps().values()
					.stream().filter(v -> depot.equals(v.getInDepot())).map(HostInfo::getName)
					.collect(Collectors.toList());
			String[] clientIds = clientsMap.toArray(new String[0]);
			Map<String, List<Map<String, String>>> productsStatesAndActions = persistenceController
					.getMapOfProductStatesAndActions(clientIds);

			if (!productsStatesAndActions.isEmpty()) {
				for (Map.Entry<String, List<Map<String, String>>> entry : productsStatesAndActions.entrySet()) {
					String hostname = entry.getKey();
					fillProductStatesListsWithClientProducts(entry.getValue(), depot, hostname, installedProductsList,
							failedProductsList, allUnusedProducts);
				}
				tmpUnusedProductsList.put(depot, allUnusedProducts);
			} else {
				allUnusedProducts.forEach((String productId) -> {
					if (installedProductsList.keySet().stream().noneMatch(p -> p.getId().equals(productId))
							&& failedProductsList.keySet().stream().noneMatch(p -> p.getId().equals(productId))
							&& unusedProductsList.keySet().stream().noneMatch(p -> p.getId().equals(productId))) {
						Product product = produceProduct(Configed.getResourceValue("Dashboard.products.unused"),
								new HashMap<>(), productId, "", depot);
						unusedProductsList.put(product, product);
					}
				});
			}

			Helper.fillMapOfMapsForDepots(installedProducts, installedProductsList, depot);
			Helper.fillMapOfMapsForDepots(failedProducts, failedProductsList, depot);
			Helper.fillMapOfMapsForDepots(unusedProducts, unusedProductsList, depot);
		}
	}

	private static void fillProductStatesListsWithClientProducts(List<Map<String, String>> data, String depot,
			String hostname, Map<Product, Product> installedProductsList, Map<Product, Product> failedProductsList,
			List<String> allUnusedProducts) {
		for (Map<String, String> productInfo : data) {
			String productId = productInfo.get("productId");
			if ("installed".equals(productInfo.get("installationStatus"))) {
				Product product = produceProduct(Configed.getResourceValue("Dashboard.products.installed"),
						installedProductsList, productId, hostname, depot);
				installedProductsList.put(product, product);
				allUnusedProducts.remove(productId);
			} else if ("failed".equals(productInfo.get("actionResult"))) {
				Product product = produceProduct(Configed.getResourceValue("Dashboard.products.failed"),
						failedProductsList, productId, hostname, depot);
				failedProductsList.put(product, product);
				allUnusedProducts.remove(productId);
			} else {
				// Do nothing when product is not installed or product installation has not failed.
			}
		}
	}

	public static void retrieveUnusedProducts() {
		depots.forEach((String depot) -> {
			if (tmpUnusedProductsList.get(depot) != null) {
				Helper.fillMapOfMapsForDepots(unusedProducts, createUnusedProductList(depot), depot);
			}
		});
		tmpUnusedProductsList.clear();
	}

	private static Map<Product, Product> createUnusedProductList(String depot) {
		Map<Product, Product> unusedProductsList = new HashMap<>();
		List<String> hostnames = persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps().values()
				.stream().filter(v -> depot.equals(v.getInDepot())).map(HostInfo::getName).collect(Collectors.toList());
		for (String productId : tmpUnusedProductsList.get(depot)) {
			addUnusedProductToList(depot, productId, hostnames, unusedProductsList);
		}
		return unusedProductsList;
	}

	private static void addUnusedProductToList(String depot, String productId, List<String> hostnames,
			Map<Product, Product> unusedProductsList) {
		Product product = produceProduct(Configed.getResourceValue("Dashboard.products.unused"), unusedProductsList,
				productId, hostnames, depot);
		unusedProductsList.put(product, product);
	}

	private static Product produceProduct(String productStatus, Map<Product, Product> productsList, String productId,
			String hostname, String depot) {
		List<String> hostnames = new ArrayList<>();
		hostnames.add(hostname);
		return produceProduct(productStatus, productsList, productId, hostnames, depot);
	}

	private static Product produceProduct(String productStatus, Map<Product, Product> productsList, String productId,
			List<String> hostnames, String depot) {
		Product product = new Product();

		Optional<Product> matchedProduct = productsList.keySet().stream()
				.filter(p -> p.getId().equals(productId) && p.getStatus().equals(productStatus)).findFirst();
		if (matchedProduct.isPresent()) {
			product = matchedProduct.get();
		} else {
			product.setDepot(depot);
			product.setId(productId);
			product.setStatus(productStatus);
		}

		if (!hostnames.isEmpty()) {
			List<String> clients = product.getClients();
			clients.addAll(hostnames);
			product.setClients(clients);
		}

		return product;
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
		retrieveInstalledOS();
		retrieveInstalledAndFailedProducts();
	}
}
