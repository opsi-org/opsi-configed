package de.uib.configed.dashboard.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.uib.configed.configed;
import de.uib.configed.dashboard.Helper;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

public class ProductData {
	private static HashMap<String, List<String>> products = new HashMap<>();
	private static HashMap<String, List<String>> netbootProducts = new HashMap<>();
	private static HashMap<String, List<String>> localbootProducts = new HashMap<>();
	private static HashMap<String, Map<Product, Product>> installedProducts = new HashMap<>();
	private static HashMap<String, Map<Product, Product>> failedProducts = new HashMap<>();
	private static HashMap<String, Map<Product, Product>> unusedProducts = new HashMap<>();

	private static int totalOSInstallations = 0;
	private static int totalLinuxInstallations = 0;
	private static int totalWindowsInstallations = 0;
	private static int totalMacOSInstallations = 0;

	private static PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	private static String selectedDepot;
	private static List<String> depots = new ArrayList<>(persist.getHostInfoCollections().getAllDepots().keySet());

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
			if (products.containsKey(configed.getResourceValue("Dashboard.selection.allDepots"))) {
				List<String> allDepotProducts = products
						.get(configed.getResourceValue("Dashboard.selection.allDepots"));
				allDepotProducts.addAll(persist.getAllProductNames(depot));
				products.put(configed.getResourceValue("Dashboard.selection.allDepots"), allDepotProducts);
			} else {
				products.put(configed.getResourceValue("Dashboard.selection.allDepots"),
						persist.getAllProductNames(depot));
			}

			products.put(depot, persist.getAllProductNames(depot));
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
			List<String> netbootProductsList = persist.getProvidedNetbootProducts(depot);
			netbootProducts.put(depot, netbootProductsList);
		}

		List<String> allNetbootProducts = Helper.combineListsFromMap(netbootProducts);
		netbootProducts.put(configed.getResourceValue("Dashboard.selection.allDepots"), allNetbootProducts);
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
			List<String> localbootProductsList = persist.getProvidedLocalbootProducts(depot);
			localbootProducts.put(depot, localbootProductsList);
		}

		List<String> allLocalbootProducts = Helper.combineListsFromMap(localbootProducts);
		localbootProducts.put(configed.getResourceValue("Dashboard.selection.allDepots"), allLocalbootProducts);
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

	private static void retrieveProductState() {
		if (!installedProducts.isEmpty() && !failedProducts.isEmpty() && !unusedProducts.isEmpty()) {
			return;
		}

		installedProducts.clear();
		failedProducts.clear();
		unusedProducts.clear();

		for (String depot : depots) {
			List<String> allProducts = persist.getAllProductNames(depot);
			Map<Product, Product> installedProductsList = new HashMap<>();
			Map<Product, Product> failedProductsList = new HashMap<>();
			Map<Product, Product> unusedProductsList = new HashMap<>();

			List<String> clientsMap = persist.getHostInfoCollections().getMapOfAllPCInfoMaps().values().stream()
					.filter(v -> depot.equals(v.getInDepot())).map(HostInfo::getName).collect(Collectors.toList());
			String[] clientIds = clientsMap.toArray(new String[clientsMap.size()]);
			Map<String, java.util.List<Map<String, String>>> productsStatesAndActions = persist
					.getMapOfProductStatesAndActions(clientIds);

			if (!productsStatesAndActions.isEmpty()) {
				for (Map.Entry<String, java.util.List<Map<String, String>>> entry : productsStatesAndActions
						.entrySet()) {
					String hostname = entry.getKey();

					for (Map<String, String> productInfo : entry.getValue()) {
						String productId = productInfo.get("productId");

						Product product = new Product();
						product.setId(productId);
						product.setDepot(depot);

						if (productInfo.get("installationStatus").equals("installed")) {
							product.setStatus(configed.getResourceValue("Dashboard.products.installed"));

							Optional<Product> matchedProduct = installedProductsList.keySet().stream()
									.filter(p -> p.getId().equals(productId) && p.getStatus()
											.equals(configed.getResourceValue("Dashboard.products.installed")))
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

							allProducts.remove(productId);
						} else if (productInfo.get("actionResult").equals("failed")) {
							product.setStatus(configed.getResourceValue("Dashboard.products.failed"));

							Optional<Product> matchedProduct = failedProductsList.keySet().stream()
									.filter(p -> p.getId().equals(productId) && p.getStatus()
											.equals(configed.getResourceValue("Dashboard.products.failed")))
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

							allProducts.remove(productId);
						}
					}
				}

				productsStatesAndActions.keySet().forEach(hostname -> {
					allProducts.forEach(productId -> {
						Product product = new Product();
						product.setId(productId);
						product.setDepot(depot);
						product.setStatus(configed.getResourceValue("Dashboard.products.unused"));

						Optional<Product> matchedProduct = unusedProductsList.keySet().stream()
								.filter(p -> p.getId().equals(productId)).findFirst();

						if (matchedProduct.isPresent()) {
							List<String> clients = matchedProduct.get().getClients();
							clients.add(hostname);
							product.setClients(clients);
							unusedProductsList.replace(matchedProduct.get(), product);
						} else {
							List<String> clients = new ArrayList<>();
							clients.add(hostname);
							product.setClients(clients);
							unusedProductsList.put(product, product);
						}
					});
				});
			} else {
				allProducts.forEach(productId -> {
					if (!installedProductsList.keySet().stream().anyMatch(p -> p.getId().equals(productId))
							&& !failedProductsList.keySet().stream().anyMatch(p -> p.getId().equals(productId))
							&& !unusedProductsList.keySet().stream().anyMatch(p -> p.getId().equals(productId))) {
						Product product = new Product();
						product.setId(productId);
						product.setDepot(depot);
						product.setStatus(configed.getResourceValue("Dashboard.products.unused"));
						product.setClients(new ArrayList<>());

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

		installedProducts.put(configed.getResourceValue("Dashboard.selection.allDepots"), allInstalledProducts);
		failedProducts.put(configed.getResourceValue("Dashboard.selection.allDepots"), allFailedProducts);
		unusedProducts.put(configed.getResourceValue("Dashboard.selection.allDepots"), allUnusedProducts);
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

		Map<String, Integer> installedOSs = persist.getInstalledOsOverview();

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

		System.out.println("Retrieving localboot products");
		retrieveLocalbootProducts();
		System.out.println("Retrieving netboot products");
		retrieveNetbootProducts();
		System.out.println("Retrieving products");
		retrieveProducts();

		System.out.println("Retrieving installed products");
		retrieveInstalledOS();
		System.out.println("Retrieving product state");
		retrieveProductState();
	}
}
