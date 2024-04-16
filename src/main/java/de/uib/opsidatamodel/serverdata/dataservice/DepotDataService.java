/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import de.uib.configed.type.OpsiPackage;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utils.logging.Logging;

/**
 * Provides methods for working with depot data on the server.
 * <p>
 * Classes ending in {@code DataService} represent somewhat of a layer between
 * server and the client. It enables to work with specific data, that is saved
 * on the server.
 * <p>
 * {@code DataService} classes only allow to retrieve and update data. Data may
 * be internally cached. The internally cached data is identified by a method
 * name. If a method name ends in {@code PD}, it means that method either
 * retrieves or it updates internally cached data. {@code PD} stands for
 * {@code Persistent Data}.
 */
public class DepotDataService {
	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;
	private UserRolesConfigDataService userRolesConfigDataService;
	private ProductDataService productDataService;
	private HostInfoCollections hostInfoCollections;

	public DepotDataService(AbstractPOJOExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setProductDataService(ProductDataService productDataService) {
		this.productDataService = productDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	public boolean areDepotsSynchronous(Iterable<String> depots) {
		String lastIdent = null;
		for (String depot : depots) {
			List<String> productIdents = new ArrayList<>();
			String callReturnType = "dict";
			Map<String, String> callFilter = new HashMap<>();
			callFilter.put("depotId", depot);
			List<Map<String, Object>> products = exec.getListOfMaps(new OpsiMethodCall(
					RPCMethodName.PRODUCT_ON_DEPOT_GET_IDENTS, new Object[] { callReturnType, callFilter }));
			for (Map<String, Object> product : products) {
				productIdents.add(product.get("productId") + ";" + product.get("productVersion") + ";"
						+ product.get("packageVersion"));
			}
			Collections.sort(productIdents);
			String ident = String.join("|", productIdents);
			if (lastIdent != null && !ident.equals(lastIdent)) {
				return false;
			}
			lastIdent = ident;
		}
		return true;
	}

	public Map<String, Map<String, Object>> getDepotPropertiesForPermittedDepots() {
		Map<String, Map<String, Object>> depotProperties = hostInfoCollections.getAllDepots();
		Map<String, Map<String, Object>> depotPropertiesForPermittedDepots = new LinkedHashMap<>();

		String configServer = hostInfoCollections.getConfigServer();
		if (userRolesConfigDataService.hasDepotPermission(configServer)) {
			depotPropertiesForPermittedDepots.put(configServer, depotProperties.get(configServer));
		}

		for (Entry<String, Map<String, Object>> depotProperty : depotProperties.entrySet()) {
			if (!depotProperty.getKey().equals(configServer)
					&& userRolesConfigDataService.hasDepotPermission(depotProperty.getKey())) {
				depotPropertiesForPermittedDepots.put(depotProperty.getKey(), depotProperty.getValue());
			}
		}

		return depotPropertiesForPermittedDepots;
	}

	public List<String> getAllDepotsWithIdenticalProductStock(String depot) {
		List<String> result = new ArrayList<>();

		TreeSet<OpsiPackage> originalProductStock = productDataService.getDepot2PackagesPD().get(depot);
		Logging.info(this, "getAllDepotsWithIdenticalProductStock " + originalProductStock);

		for (String testDepot : hostInfoCollections.getAllDepots().keySet()) {
			if (depot.equals(testDepot) || areProductStocksIdentical(originalProductStock,
					productDataService.getDepot2PackagesPD().get(testDepot))) {
				result.add(testDepot);
			}
		}
		Logging.info(this, "getAllDepotsWithIdenticalProductStock  result " + result);

		return result;
	}

	private static boolean areProductStocksIdentical(TreeSet<OpsiPackage> firstProductStock,
			TreeSet<OpsiPackage> secondProductStock) {
		return (firstProductStock == null && secondProductStock == null)
				|| (firstProductStock != null && firstProductStock.equals(secondProductStock));
	}

	public void setDepot(String depotId) {
		Logging.info(this, "setDepot: " + depotId);
		cacheManager.setCachedData(CacheIdentifier.DEPOT, depotId);
	}

	public String getDepot() {
		return cacheManager.getCachedData(CacheIdentifier.DEPOT, String.class);
	}
}
