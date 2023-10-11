/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.json.JSONArray;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.configed.type.RetrievedMap;
import de.uib.connectx.SmbConnect;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.Object2Product2VersionList;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.datapanel.MapTableModel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ListCellOptions;
import utils.ProductPackageVersionSeparator;
import utils.Utils;

/**
 * Provides methods for working with proudct data on the server.
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
@SuppressWarnings({ "unchecked" })
public class ProductDataService {
	private static final String EMPTYFIELD = "-";
	private static final String NAME_REQUIREMENT_TYPE_BEFORE = "before";
	private static final String NAME_REQUIREMENT_TYPE_AFTER = "after";
	private static final String NAME_REQUIREMENT_TYPE_NEUTRAL = "";
	private static final String NAME_REQUIREMENT_TYPE_ON_DEINSTALL = "on_deinstall";

	private static final String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT = "configed.productonclient_displayfields_localboot";
	private static final String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT = "configed.productonclient_displayfields_netboot";

	private CacheManager cacheManager;
	private AbstractExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private ConfigDataService configDataService;
	private UserRolesConfigDataService userRolesConfigDataService;
	private DepotDataService depotDataService;
	private HostInfoCollections hostInfoCollections;

	private List<Map<String, Object>> updateProductOnClientItems;
	private List<Map<String, Object>> productPropertyStateUpdateCollection;
	private List<Map<String, Object>> productPropertyStateDeleteCollection;

	public ProductDataService(AbstractExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setDepotDataService(DepotDataService depotDataService) {
		this.depotDataService = depotDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	public List<String> getAllNetbootProductNames() {
		return getAllNetbootProductNames(depotDataService.getDepot());
	}

	public List<String> getAllNetbootProductNames(String depotId) {
		Object2Product2VersionList netbootProducts = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, Object2Product2VersionList.class);

		return netbootProducts.get(depotId) != null ? new ArrayList<>(netbootProducts.get(depotId).keySet())
				: new ArrayList<>();
	}

	public List<String> getAllLocalbootProductNames() {
		return getAllLocalbootProductNames(depotDataService.getDepot());
	}

	public List<String> getAllLocalbootProductNames(String depotId) {
		Logging.debug(this, "getAllLocalbootProductNames for depot " + depotId);
		List<String> localbootProductNames;
		if (ServerFacade.isOpsi43()) {
			localbootProductNames = getDepot2LocalbootProductsPD().get(depotId) != null
					? new ArrayList<>(getDepot2LocalbootProductsPD().get(depotId).keySet())
					: new ArrayList<>();
		} else {
			Map<String, List<String>> productOrderingResult = exec.getMapOfStringLists(
					new OpsiMethodCall(RPCMethodName.GET_PRODUCT_ORDERING, new String[] { depotId }));

			List<String> sortedProducts = productOrderingResult.get("sorted");
			if (sortedProducts == null) {
				sortedProducts = new ArrayList<>();
			}

			List<String> notSortedProducts = productOrderingResult.get("not_sorted");
			if (notSortedProducts == null) {
				notSortedProducts = new ArrayList<>();
			}

			Logging.info(this, "not ordered " + (notSortedProducts.size() - sortedProducts.size()) + "");

			notSortedProducts.removeAll(sortedProducts);
			Logging.info(this, "missing: " + notSortedProducts);

			localbootProductNames = sortedProducts;
			localbootProductNames.addAll(notSortedProducts);

			// we don't have a productsgroupsFullPermission)
			Set<String> permittedProducts = userRolesConfigDataService.getPermittedProductsPD();
			if (permittedProducts != null) {
				localbootProductNames.retainAll(permittedProducts);
			}
		}
		Logging.info(this, "localbootProductNames sorted, size " + localbootProductNames.size());
		return new ArrayList<>(localbootProductNames);
	}

	public Map<String, TreeSet<OpsiPackage>> getDepot2PackagesPD() {
		retrieveProductsAllDepotsPD();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PACKAGES, Map.class);
	}

	public Map<String, Map<String, List<String>>> getProduct2VersionInfo2DepotsPD() {
		retrieveProductsAllDepotsPD();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, Map.class);
	}

	public Object2Product2VersionList getDepot2LocalbootProductsPD() {
		retrieveProductsAllDepotsPD();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS,
				Object2Product2VersionList.class);
	}

	public Object2Product2VersionList getDepot2NetbootProductsPD() {
		retrieveProductsAllDepotsPD();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, Object2Product2VersionList.class);
	}

	public void retrieveProductsAllDepotsPD() {
		Logging.debug(this, "retrieveProductsAllDepotsPD");
		if (cacheManager.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, Map.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS, Map.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, Map.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PACKAGES, Map.class) != null) {
			Logging.debug(this, "depot2LocalbootProducts " + cacheManager
					.getCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS, Object2Product2VersionList.class)
					.size());
			Logging.debug(this, "depot2NetbootProducts" + cacheManager
					.getCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, Object2Product2VersionList.class).size());
			return;
		}

		retrieveProductInfosPD();

		Logging.info(this, "retrieveProductsAllDepotsPD, reload");

		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_DEPOT_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> packages = exec.getListOfMaps(omc);

		Map<String, TreeSet<OpsiPackage>> depot2Packages = new HashMap<>();
		Object2Product2VersionList depot2NetbootProducts = new Object2Product2VersionList();
		Object2Product2VersionList depot2LocalbootProducts = new Object2Product2VersionList();
		Map<String, Map<String, List<String>>> product2VersionInfo2Depots = new HashMap<>();

		for (Map<String, Object> m : packages) {
			String depot = "" + m.get("depotId");

			if (!userRolesConfigDataService.hasDepotPermission(depot)) {
				continue;
			}

			OpsiPackage p = new OpsiPackage(m);
			Logging.debug(this, "retrieveProductsAllDepotsPD, opsi package " + p);

			if (p.isNetbootProduct()) {
				depot2NetbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
			} else if (p.isLocalbootProduct()) {
				depot2LocalbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
			} else {
				Logging.warning(this, "unexpected product type " + p.toString());
			}

			Map<String, List<String>> versionInfo2Depots = product2VersionInfo2Depots.computeIfAbsent(p.getProductId(),
					s -> new HashMap<>());
			List<String> depotsWithThisVersion = versionInfo2Depots.computeIfAbsent(p.getVersionInfo(),
					s -> new ArrayList<>());

			depotsWithThisVersion.add(depot);

			TreeSet<OpsiPackage> depotPackages = depot2Packages.computeIfAbsent(depot, s -> new TreeSet<>());
			depotPackages.add(p);

			List<Object> productRow = new ArrayList<>();
			productRow.add(p.getProductId());

			Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos = cacheManager
					.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS, Map.class);
			String productName = product2versionInfo2infos.get(p.getProductId()).get(p.getVersionInfo())
					.getProductName();
			productRow.add(productName);
			p.appendValues(productRow);
		}

		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_PACKAGES, depot2Packages);
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, product2VersionInfo2Depots);
		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS, depot2LocalbootProducts);
		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, depot2NetbootProducts);
		persistenceController.notifyPanelCompleteWinProducts();
	}

	public List<List<Object>> getProductRowsForDepots(Iterable<String> depotIds) {

		Map<String, Set<OpsiPackage>> depot2packages = cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PACKAGES,
				Map.class);

		List<List<Object>> productRows = new ArrayList<>();

		for (String depotId : depotIds) {
			Set<OpsiPackage> packages = depot2packages.get(depotId);

			if (!userRolesConfigDataService.hasDepotPermission(depotId) || packages == null) {
				continue;
			}

			for (OpsiPackage p : packages) {
				List<Object> productRow = new ArrayList<>();
				productRow.add(p.getProductId());

				Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos = getProduct2VersionInfo2InfosPD();
				String productName = product2versionInfo2infos.get(p.getProductId()).get(p.getVersionInfo())
						.getProductName();
				productRow.add(productName);
				p.appendValues(productRow);

				List<String> depotsWithThisVersion = getProduct2VersionInfo2DepotsPD().get(p.getProductId())
						.get(p.getVersionInfo());
				if (depotsWithThisVersion.size() == 1) {
					productRows.add(productRow);
				}
			}
		}

		persistenceController.notifyPanelCompleteWinProducts();
		return productRows;
	}

	public Map<String, Map<String, OpsiProductInfo>> getProduct2VersionInfo2InfosPD() {
		retrieveProductInfosPD();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS, Map.class);
	}

	public void retrieveProductInfosPD() {
		if (cacheManager.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS, Map.class) != null) {
			return;
		}

		List<String> attribs = new ArrayList<>();

		for (String key : OpsiPackage.SERVICE_KEYS) {
			attribs.add(key);
		}

		for (String scriptKey : ActionRequest.getScriptKeys()) {
			attribs.add(scriptKey);
		}

		attribs.add(OpsiProductInfo.SERVICE_KEY_USER_LOGIN_SCRIPT);
		attribs.add(OpsiProductInfo.SERVICE_KEY_PRIORITY);

		attribs.remove(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE);
		attribs.add(OpsiProductInfo.SERVICE_KEY_PRODUCT_ADVICE);
		attribs.add(OpsiProductInfo.SERVICE_KEY_PRODUCT_NAME);
		attribs.add(OpsiProductInfo.SERVICE_KEY_PRODUCT_DESCRIPTION);

		String[] callAttributes = attribs.toArray(new String[] {});

		Logging.debug(this, "retrieveProductInfos callAttributes " + Arrays.asList(callAttributes));

		Map<String, Object> callFilter = new HashMap<>();

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> retrievedList = exec.getListOfMaps(omc);

		Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos = new HashMap<>();

		for (Map<String, Object> m : retrievedList) {
			String productId = "" + m.get(OpsiPackage.SERVICE_KEY_PRODUCT_ID0);
			String versionInfo = OpsiPackage.produceVersionInfo("" + m.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION),
					"" + m.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION));

			OpsiProductInfo productInfo = new OpsiProductInfo(m);
			Map<String, OpsiProductInfo> version2productInfos = product2versionInfo2infos.computeIfAbsent(productId,
					arg -> new HashMap<>());
			version2productInfos.put(versionInfo, productInfo);
		}

		Logging.debug(this, "retrieveProductInfos " + product2versionInfo2infos);

		cacheManager.setCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS, product2versionInfo2infos);
		persistenceController.notifyPanelCompleteWinProducts();
	}

	public void retrieveProductPropertyDefinitions() {
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_PROPERTY_DEFINITIONS,
				getDepot2Product2PropertyDefinitionsPD().get(depotDataService.getDepot()));
	}

	public Map<String, Map<String, Map<String, ListCellOptions>>> getDepot2Product2PropertyDefinitionsPD() {
		retrieveAllProductPropertyDefinitionsPD();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS, Map.class);
	}

	public void retrieveAllProductPropertyDefinitionsPD() {
		if (cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS, Map.class) != null) {
			return;
		}
		retrieveProductsAllDepotsPD();

		Map<String, Map<String, Map<String, ListCellOptions>>> depot2Product2PropertyDefinitions = new HashMap<>();
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

		Iterator<Map<String, Object>> iter = retrieved.iterator();

		while (iter.hasNext()) {

			Map<String, Object> retrievedMap = iter.next();
			Map<String, Object> adaptedMap = new HashMap<>(retrievedMap);
			// rebuild JSON objects
			Iterator<String> iterInner = retrievedMap.keySet().iterator();
			while (iterInner.hasNext()) {
				String key = iterInner.next();
				adaptedMap.put(key, retrievedMap.get(key));
			}

			ConfigOption productPropertyMap = new ConfigOption(adaptedMap);

			String propertyId = (String) retrievedMap.get("propertyId");
			String productId = (String) retrievedMap.get("productId");

			String productVersion = (String) retrievedMap.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
			String packageVersion = (String) retrievedMap.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
			String versionInfo = productVersion + ProductPackageVersionSeparator.FOR_KEY + packageVersion;

			Map<String, Map<String, List<String>>> product2VersionInfo2Depots = cacheManager
					.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, Map.class);
			if (product2VersionInfo2Depots.get(productId) == null
					|| product2VersionInfo2Depots.get(productId).get(versionInfo) == null) {
				Logging.debug(this,
						"retrieveAllProductPropertyDefinitions: no depot for " + productId + " version " + versionInfo
								+ "  product2VersionInfo2Depots.get(productId) "
								+ product2VersionInfo2Depots.get(productId));

			} else {
				for (String depot : product2VersionInfo2Depots.get(productId).get(versionInfo)) {
					Map<String, Map<String, ListCellOptions>> product2PropertyDefinitions = depot2Product2PropertyDefinitions
							.computeIfAbsent(depot, s -> new HashMap<>());

					Map<String, ListCellOptions> propertyDefinitions = product2PropertyDefinitions
							.computeIfAbsent(productId, s -> new HashMap<>());

					propertyDefinitions.put(propertyId, productPropertyMap);
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS,
				depot2Product2PropertyDefinitions);
		Logging.debug(this, "retrieveAllProductPropertyDefinitions ");

		persistenceController.notifyPanelCompleteWinProducts();
	}

	public Map<String, Map<String, List<Map<String, String>>>> getDepot2product2dependencyInfosPD() {
		retrieveAllProductDependenciesPD();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS, Map.class);
	}

	public void retrieveAllProductDependenciesPD() {
		if (cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS, Map.class) != null) {
			return;
		}
		retrieveProductsAllDepotsPD();

		Map<String, Map<String, List<Map<String, String>>>> depot2product2dependencyInfos = new HashMap<>();

		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_DEPENDENCY_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> retrievedList = exec.getListOfMaps(omc);

		for (Map<String, Object> dependencyItem : retrievedList) {
			String productId = "" + dependencyItem.get(OpsiPackage.DB_KEY_PRODUCT_ID);

			String productVersion = "" + dependencyItem.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
			String packageVersion = "" + dependencyItem.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
			String versionInfo = productVersion + ProductPackageVersionSeparator.FOR_KEY + packageVersion;

			String action = "" + dependencyItem.get("productAction");
			String requirementType = "";
			if (dependencyItem.get("requirementType") != null) {
				requirementType = "" + dependencyItem.get("requirementType");
			}

			String requiredProductId = "" + dependencyItem.get("requiredProductId");
			String requiredAction = "";
			if (dependencyItem.get("requiredAction") != null) {
				requiredAction = "" + dependencyItem.get("requiredAction");
			}
			String requiredInstallationStatus = "";

			if (dependencyItem.get("requiredInstallationStatus") != null) {
				requiredInstallationStatus = "" + dependencyItem.get("requiredInstallationStatus");
			}

			Map<String, Map<String, List<String>>> product2VersionInfo2Depots = cacheManager
					.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, Map.class);
			if (product2VersionInfo2Depots == null || product2VersionInfo2Depots.get(productId) == null
					|| product2VersionInfo2Depots.get(productId).get(versionInfo) == null) {
				Logging.warning(this, "unexpected null for product2VersionInfo2Depots productId, versionInfo   "
						+ productId + ", " + versionInfo);
				continue;
			}
			for (String depot : product2VersionInfo2Depots.get(productId).get(versionInfo)) {
				Map<String, List<Map<String, String>>> product2dependencyInfos = depot2product2dependencyInfos
						.computeIfAbsent(depot, s -> new HashMap<>());

				List<Map<String, String>> dependencyInfos = product2dependencyInfos.computeIfAbsent(productId,
						s -> new ArrayList<>());

				Map<String, String> dependencyInfo = new HashMap<>();
				dependencyInfo.put("action", action);
				dependencyInfo.put("requiredProductId", requiredProductId);
				dependencyInfo.put("requiredAction", requiredAction);
				dependencyInfo.put("requiredInstallationStatus", requiredInstallationStatus);
				dependencyInfo.put("requirementType", requirementType);

				dependencyInfos.add(dependencyInfo);
			}
		}

		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS, depot2product2dependencyInfos);
		persistenceController.notifyPanelCompleteWinProducts();
	}

	private List<Map<String, Object>> getProductPropertyStates(Collection<String> clients) {
		Logging.info(this, "retrieveProductPropertyStates for " + clients);
		return produceProductPropertyStates(clients);
	}

	private List<Map<String, Object>> getProductPropertyDepotStates(Set<String> depots) {
		Logging.info(this, "retrieveProductPropertyDepotStates for depots " + depots);
		List<Map<String, Object>> productPropertyDepotStates = produceProductPropertyStates(depots);
		Logging.info(this, "retrieveProductPropertyDepotStates ready  size " + productPropertyDepotStates.size());
		return productPropertyDepotStates;
	}

	// client is a set of added hosts, host represents the totality and will be
	// updated as a side effect
	private List<Map<String, Object>> produceProductPropertyStates(final Collection<String> clients) {
		Logging.info(this, "produceProductPropertyStates new hosts " + clients/* + " old hosts " + hosts */);
		List<String> newClients = null;
		if (clients == null) {
			newClients = new ArrayList<>();
		} else {
			newClients = new ArrayList<>(clients);
		}

		List<Map<String, Object>> result = null;

		if (newClients.isEmpty()) {
			// look if propstates is initialized
			result = new ArrayList<>();
		} else {
			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();
			callFilter.put("objectId", newClients);

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			result = exec.getListOfMaps(omc);
		}

		return result;
	}

	public Map<String, Map<String, Object>> getProductGlobalInfosPD(String depotId) {
		checkProductGlobalInfosPD(depotId);
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_GLOBAL_INFOS, Map.class);
	}

	// map with key productId
	public Map<String, List<String>> getPossibleActionsPD(String depotId) {
		Logging.debug(this, "getPossibleActions depot irregular " + !depotDataService.getDepot().equals(depotId));
		checkProductGlobalInfosPD(depotId);
		return cacheManager.getCachedData(CacheIdentifier.POSSIBLE_ACTIONS, Map.class);
	}

	public void checkProductGlobalInfosPD(String depotId) {
		if (cacheManager.getCachedData(CacheIdentifier.PRODUCT_GLOBAL_INFOS, Map.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.POSSIBLE_ACTIONS, Map.class) != null
				&& depotDataService.getDepot() != null && depotDataService.getDepot().equals(depotId)) {
			return;
		}

		Logging.info(this, "checkProductGlobalInfos for Depot " + depotId);
		if (!depotDataService.getDepot().equals(depotId)) {
			Logging.warning(this, "depot irregular, preset " + depotDataService.getDepot());
		}
		if (depotId == null || depotId.isEmpty()) {
			Logging.notice(this, "checkProductGlobalInfos called for no depot");
		}
		retrieveProductGlobalInfosPD(depotId);
	}

	public void retrieveProductGlobalInfosPD(String depotId) {
		Logging.info(this, "retrieveProductGlobalInfos , depot " + depotId);

		Map<String, Map<String, Object>> productGlobalInfos = new HashMap<>();
		Map<String, List<String>> possibleActions = new HashMap<>();

		for (String productId : getProduct2VersionInfo2InfosPD().keySet()) {
			if (getProduct2VersionInfo2InfosPD().get(productId) == null) {
				Logging.warning(this, "retrieveProductGlobalInfos productId == null for product " + productId);
			}

			if (getProduct2VersionInfo2InfosPD().get(productId) != null) {
				String versionInfo = null;
				Map<String, OpsiProductInfo> productAllInfos = getProduct2VersionInfo2InfosPD().get(productId);

				// look for associated product on depot info
				HashMap<String, List<String>> product2VersionList = getDepot2LocalbootProductsPD().get(depotId);
				if (product2VersionList != null && product2VersionList.get(productId) != null
						&& !product2VersionList.get(productId).isEmpty()) {
					versionInfo = product2VersionList.get(productId).get(0);
				}

				if (versionInfo == null) {
					product2VersionList = getDepot2NetbootProductsPD().get(depotId);

					if (product2VersionList != null && product2VersionList.get(productId) != null
							&& !product2VersionList.get(productId).isEmpty()) {
						versionInfo = product2VersionList.get(productId).get(0);
					}
				}

				// if found go on

				if (versionInfo != null && productAllInfos.get(versionInfo) != null) {
					OpsiProductInfo productInfo = productAllInfos.get(versionInfo);

					possibleActions.put(productId, productInfo.getPossibleActions());

					Map<String, Object> aProductInfo = new HashMap<>();

					aProductInfo.put("actions", productInfo.getPossibleActions());

					aProductInfo.put(ProductState.KEY_PRODUCT_ID, productId
					// productInfo.getProductId()
					);
					aProductInfo.put(ProductState.KEY_VERSION_INFO,
							ProductPackageVersionSeparator.formatKeyForDisplay(productInfo.getVersionInfo()));

					aProductInfo.put(ProductState.KEY_PRODUCT_PRIORITY, productInfo.getPriority());

					aProductInfo.put(ProductState.KEY_PRODUCT_NAME,
							// OpsiProductInfo.SERVICEkeyPRODUCT_NAME,
							productInfo.getProductName());

					aProductInfo.put(OpsiProductInfo.SERVICE_KEY_PRODUCT_DESCRIPTION, productInfo.getDescription());

					aProductInfo.put(OpsiProductInfo.SERVICE_KEY_PRODUCT_ADVICE, productInfo.getAdvice());

					aProductInfo.put(ProductState.KEY_PRODUCT_VERSION, productInfo.getProductVersion());

					aProductInfo.put(ProductState.KEY_PACKAGE_VERSION, productInfo.getPackageVersion());

					aProductInfo.put(OpsiPackage.SERVICE_KEY_LOCKED, productInfo.getLockedInfo());

					Logging.debug(this, "productInfo " + aProductInfo);

					productGlobalInfos.put(productId, aProductInfo);
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.PRODUCT_GLOBAL_INFOS, productGlobalInfos);
		cacheManager.setCachedData(CacheIdentifier.POSSIBLE_ACTIONS, possibleActions);
		Logging.info(this, "retrieveProductGlobalInfos  found number  " + productGlobalInfos.size());
	}

	public Map<String, Map<String, String>> getProductDefaultStatesPD() {
		retrieveProductIdsAndDefaultStatesPD();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_DEFAULT_STATES, Map.class);
	}

	public NavigableSet<String> getProductIdsPD() {
		retrieveProductIdsAndDefaultStatesPD();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_IDS, NavigableSet.class);
	}

	public void retrieveProductIdsAndDefaultStatesPD() {
		if (cacheManager.getCachedData(CacheIdentifier.PRODUCT_IDS, NavigableSet.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.PRODUCT_DEFAULT_STATES, Map.class) != null) {
			return;
		}

		retrieveProductInfosPD();

		NavigableSet<String> productIds = new TreeSet<>();
		Map<String, Map<String, String>> productDefaultStates = new TreeMap<>();

		for (String productId : getProduct2VersionInfo2InfosPD().keySet()) {
			productIds.add(productId);
			ProductState productDefault = new ProductState(null);
			productDefault.put("productId", productId);
			productDefaultStates.put(productId, productDefault);
		}
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_IDS, productIds);
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_DEFAULT_STATES, productDefaultStates);

		Logging.info(this, "getProductIds size / names " + productIds.size() + " / ... ");
	}

	public Map<String, ConfigName2ConfigValue> getProductPropertiesPD(String pcname) {
		Logging.debug(this, "getProductsProperties for host " + pcname);

		Set<String> pcs = new TreeSet<>();
		pcs.add(pcname);
		retrieveProductPropertiesPD(pcs);

		Map<String, Map<String, ConfigName2ConfigValue>> productProperties = cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_PROPERTIES, Map.class);
		if (productProperties.get(pcname) == null) {
			return new HashMap<>();
		}

		return productProperties.get(pcname);
	}

	/**
	 * @param pcname      - if it changes productproperties should have been set
	 *                    to null.
	 * @param productname
	 */
	public Map<String, Object> getProductPropertiesPD(String pcname, String productname) {
		Logging.debug(this, "getProductProperties for product, host " + productname + ", " + pcname);

		Set<String> pcs = new TreeSet<>();
		pcs.add(pcname);
		retrieveProductPropertiesPD(pcs);

		Map<String, Map<String, ConfigName2ConfigValue>> productProperties = cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_PROPERTIES, Map.class);
		if (productProperties.get(pcname) == null || productProperties.get(pcname).get(productname) == null) {
			return new HashMap<>();
		}

		return productProperties.get(pcname).get(productname);
	}

	public Boolean hasClientSpecificProperties(String productName) {
		return (Boolean) cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_HAVING_CLIENT_SPECIFIC_PROPERTIES, Map.class).get(productName);
	}

	/**
	 * This method collects properties for all selected clients and all
	 * products,<br \> as a sideeffect, it produces the depot specific default
	 * values <br \>
	 *
	 * @param clientNames -
	 */
	public void retrieveProductPropertiesPD(final Set<String> clientNames) {
		boolean existing = true;
		Map<String, Map<String, ConfigName2ConfigValue>> productProperties = cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_PROPERTIES, Map.class);
		if (productProperties == null) {
			existing = false;
		} else {
			for (String client : clientNames) {
				if (productProperties.get(client) == null) {
					existing = false;
					break;
				}
			}
		}

		if (existing) {
			return;
		}

		productProperties = new HashMap<>();
		Map<String, Map<String, Map<String, Object>>> productPropertiesRetrieved = new HashMap<>();

		List<Map<String, Object>> retrieved = getProductPropertyStates(clientNames);
		Set<String> productsWithProductPropertyStates = new HashSet<>();

		for (Map<String, Object> map : retrieved) {
			String host = (String) map.get("objectId");

			productsWithProductPropertyStates.add((String) map.get("productId"));

			Map<String, Map<String, Object>> productproperties1Client = productPropertiesRetrieved.computeIfAbsent(host,
					s -> new HashMap<>());

			Map<String, Object> properties = productproperties1Client.computeIfAbsent((String) map.get("productId"),
					s -> new HashMap<>());

			properties.put((String) map.get("propertyId"),
					POJOReMapper.remap(map.get("values"), new TypeReference<List<Object>>() {
					}));
		}

		Logging.info(this,
				" retrieveProductproperties  productsWithProductPropertyStates " + productsWithProductPropertyStates);

		Map<String, ConfigName2ConfigValue> defaultProperties = getDefaultProductPropertiesPD(
				depotDataService.getDepot());
		Map<String, Map<String, Object>> defaultPropertiesRetrieved = new HashMap<>();
		if (!defaultProperties.isEmpty()) {
			for (Entry<String, ConfigName2ConfigValue> defaultProperty : defaultProperties.entrySet()) {
				defaultPropertiesRetrieved.put(defaultProperty.getKey(), defaultProperty.getValue());
			}
		}

		Set<String> products = defaultPropertiesRetrieved.keySet();

		Set<String> productsHavingSpecificProperties = new TreeSet<>(products);

		for (String host : clientNames) {
			HashMap<String, ConfigName2ConfigValue> productproperties1Client = new HashMap<>();
			productProperties.put(host, productproperties1Client);

			Map<String, Map<String, Object>> retrievedProperties = productPropertiesRetrieved.get(host);
			if (retrievedProperties == null) {
				retrievedProperties = defaultPropertiesRetrieved;
				productsHavingSpecificProperties.clear();
			}

			for (String product : products) {
				Map<String, Object> retrievedProperties1Product = retrievedProperties.get(product);
				// complete set of default values
				Map<String, Object> properties1Product = new HashMap<>(defaultPropertiesRetrieved.get(product));

				if (retrievedProperties1Product == null) {
					productsHavingSpecificProperties.remove(product);
				} else {
					for (Entry<String, Object> retrievedProperty : retrievedProperties1Product.entrySet()) {
						properties1Product.put(retrievedProperty.getKey(), retrievedProperty.getValue());
					}
				}

				ConfigName2ConfigValue state = new ConfigName2ConfigValue(properties1Product, null);
				productproperties1Client.put(product, state);
			}
		}

		cacheManager.setCachedData(CacheIdentifier.PRODUCT_PROPERTIES, productProperties);

		Logging.info(this,
				" retrieveProductproperties productsHavingSpecificProperties " + productsHavingSpecificProperties);

		Map<String, ConfigName2ConfigValue> depotValues = getDefaultProductPropertiesPD(depotDataService.getDepot());

		Map<String, Boolean> productHavingClientSpecificProperties = new HashMap<>();
		Map<String, Map<String, ListCellOptions>> productPropertyDefinitions = cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_PROPERTY_DEFINITIONS, Map.class);

		for (String product : products) {
			if (productPropertyDefinitions != null && productPropertyDefinitions.get(product) != null) {
				ConfigName2ConfigValue productPropertyConfig = depotValues.get(product);

				Iterator<String> iterProperties = productPropertyDefinitions.get(product).keySet().iterator();
				while (iterProperties.hasNext()) {
					String property = iterProperties.next();

					if (productPropertyConfig.isEmpty() || productPropertyConfig.get(property) == null) {
						productPropertyDefinitions.get(product).get(property).setDefaultValues(new ArrayList<>());
					} else {
						productPropertyDefinitions.get(product).get(property)
								.setDefaultValues((List<Object>) productPropertyConfig.get(property));
					}
				}
			}

			productHavingClientSpecificProperties.put(product, productsHavingSpecificProperties.contains(product));
		}

		cacheManager.setCachedData(CacheIdentifier.PRODUCT_HAVING_CLIENT_SPECIFIC_PROPERTIES,
				productHavingClientSpecificProperties);
	}

	public Map<String, ConfigName2ConfigValue> getDefaultProductPropertiesPD(String depotId) {
		Logging.debug(this, "getDefaultProductProperties for depot " + depotId);
		retrieveDepotProductPropertiesPD();
		Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES, Map.class);
		if (depot2product2properties == null) {
			Logging.error("no product properties ");
			return new HashMap<>();
		} else {
			if (depot2product2properties.get(depotId) == null) {
				return new HashMap<>();
			}

			if (!depot2product2properties.get(depotId).isEmpty()) {
				Logging.info(this, "getDefaultProductProperties for depotId " + depotId + " starts with "
						+ new ArrayList<>(depot2product2properties.get(depotId).keySet()).get(0));
			}

			return depot2product2properties.get(depotId);
		}
	}

	/**
	 * This method collects properties for all selected clients and all
	 * products,<br \> as a sideeffect, it produces the depot specific default
	 * values <br \>
	 *
	 * @param clientNames -
	 */
	public void retrieveProductPropertiesPD(List<String> clientNames) {
		retrieveProductPropertiesPD(new HashSet<>(clientNames));
	}

	public Map<String, Map<String, ConfigName2ConfigValue>> getDepot2product2propertiesPD() {
		retrieveDepotProductPropertiesPD();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES, Map.class);
	}

	public void retrieveDepotProductPropertiesPD() {
		if (cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES, Map.class) != null) {
			return;
		}

		Logging.info(this, "retrieveDepotProductProperties, build depot2product2properties");

		Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties = new HashMap<>();
		List<Map<String, Object>> retrieved = getProductPropertyDepotStates(hostInfoCollections.getDepots().keySet());

		for (Map<String, Object> map : retrieved) {
			String host = (String) map.get("objectId");

			if (!hostInfoCollections.getDepots().keySet().contains(host)) {
				Logging.warning(this, "should be a productPropertyState for a depot, but host " + host);
				continue;
			}

			Map<String, ConfigName2ConfigValue> productproperties1Host = depot2product2properties.computeIfAbsent(host,
					arg -> new HashMap<>());

			ConfigName2ConfigValue properties = productproperties1Host.computeIfAbsent(
					(String) map.get(OpsiPackage.DB_KEY_PRODUCT_ID),
					arg -> new ConfigName2ConfigValue(new HashMap<>()));

			properties.put((String) map.get("propertyId"), new JSONArray((List<?>) map.get("values")).toList());
			properties.getRetrieved().put((String) map.get("propertyId"),
					new JSONArray((List<?>) map.get("values")).toList());

			Logging.debug(this,
					"retrieveDepotProductProperties product properties " + map.get(OpsiPackage.DB_KEY_PRODUCT_ID));
		}

		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES, depot2product2properties);
	}

	/**
	 * Collects the common property values of some product for a client
	 * collection; Needed for local imaging handling.
	 * 
	 * @param clients  collection of clients
	 * @param product  for which to collect property values
	 * @param property from which to collect values
	 */
	public List<String> getCommonProductPropertyValues(List<String> clients, String product, String property) {
		Logging.info(this, "getCommonProductPropertyValues for product, property, clients " + product + ", " + property
				+ "  -- " + clients);
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("objectId", clients);
		callFilter.put("productId", product);
		callFilter.put("propertyId", property);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> properties = exec.getListOfMaps(omc);
		Set<String> resultSet = new HashSet<>();
		boolean starting = true;
		for (Map<String, Object> map : properties) {
			List<?> valueList = (List<?>) map.get("values");
			Set<String> values = new HashSet<>();
			for (int i = 0; i < valueList.size(); i++) {
				values.add((String) valueList.get(i));
			}

			if (starting) {
				resultSet = values;
				starting = false;
			} else {
				resultSet.retainAll(values);
			}
		}
		Logging.info(this, "getCommonProductPropertyValues " + resultSet);
		return new ArrayList<>(resultSet);
	}

	public List<Map<String, String>> getProductInfos(String clientId, String[] attributes) {
		return new ArrayList<>(getProductInfos(new HashSet<>(), clientId, attributes));
	}

	public List<Map<String, String>> getProductInfos(Set<String> productIds, String clientId, String[] attributes) {
		HashMap<String, Object> callFilter = new HashMap<>();
		if (!productIds.isEmpty()) {
			callFilter.put(OpsiPackage.DB_KEY_PRODUCT_ID, productIds);
		}
		callFilter.put("clientId", clientId);
		RPCMethodName methodName = ServerFacade.isOpsi43() && attributes.length != 0
				? RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS_WITH_SEQUENCE
				: RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS;
		OpsiMethodCall omc = new OpsiMethodCall(methodName, new Object[] { attributes, callFilter });

		List<Map<String, String>> result = new ArrayList<>();

		for (Map<String, Object> m : exec.getListOfMaps(omc)) {
			result.add(new ProductState(POJOReMapper.giveEmptyForNull(m), true));
		}

		return result;
	}

	public Map<String, List<Map<String, String>>> getMapOfNetbootProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfNetbootProductStatesAndActions for : " + Arrays.toString(clientIds));
		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}

		String[] callAttributes = new String[0];
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));
		callFilter.put("productType", OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
		List<Map<String, Object>> productOnClients = exec.getListOfMaps(new OpsiMethodCall(
				RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS, new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		for (Map<String, Object> m : productOnClients) {

			String client = (String) m.get("clientId");
			result.computeIfAbsent(client, arg -> new ArrayList<>())
					.add(new ProductState(POJOReMapper.giveEmptyForNull(m), true));

		}
		return result;
	}

	public Map<String, List<Map<String, String>>> getMapOfLocalbootProductStatesAndActions(String[] clientIds,
			String[] attributes) {
		Logging.debug(this, "getMapOfLocalbootProductStatesAndActions for : " + Arrays.toString(clientIds));

		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}

		String[] callAttributes = attributes;
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));
		callFilter.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);

		RPCMethodName methodName = ServerFacade.isOpsi43() && attributes.length != 0
				? RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS_WITH_SEQUENCE
				: RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS;
		List<Map<String, Object>> productOnClients = exec
				.getListOfMaps(new OpsiMethodCall(methodName, new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		for (Map<String, Object> m : productOnClients) {
			String client = (String) m.get("clientId");
			List<Map<String, String>> states1Client = result.computeIfAbsent(client, arg -> new ArrayList<>());
			Map<String, String> aState = new ProductState(POJOReMapper.giveEmptyForNull(m), true);
			states1Client.add(aState);
		}

		return result;
	}

	public Map<String, List<Map<String, String>>> getMapOfProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfProductStatesAndActions for : " + Arrays.toString(clientIds));
		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}
		return getProductStatesNOM(clientIds);
	}

	public Map<String, List<Map<String, String>>> getProductStatesNOM(String[] clientIds) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));
		List<Map<String, Object>> productOnClients = exec.getListOfMaps(new OpsiMethodCall(
				RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS, new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		for (Map<String, Object> m : productOnClients) {
			String client = (String) m.get("clientId");

			result.computeIfAbsent(client, arg -> new ArrayList<>())
					.add(new ProductState(POJOReMapper.giveEmptyForNull(m), true));
		}
		return result;
	}

	public List<Map<String, Object>> getAllProducts() {
		String callReturnType = "dict";
		Map<String, String> callFilter = new HashMap<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_DEPOT_GET_IDENTS,
				new Object[] { callReturnType, callFilter });
		return exec.getListOfMaps(omc);
	}

	public List<String> getWinProducts(String depotId, String depotProductDirectory) {
		List<String> winProducts = new ArrayList<>();
		if (depotProductDirectory == null) {
			return winProducts;
		}

		boolean smbMounted = new File(depotProductDirectory).exists();

		for (String product : new TreeSet<>(getAllNetbootProductNames(depotId))) {
			if (!smbMounted
					|| new File(
							depotProductDirectory + File.separator + product + File.separator + SmbConnect.DIRECTORY_PE)
									.exists()
					|| new File(depotProductDirectory + File.separator + product + File.separator
							+ SmbConnect.DIRECTORY_I368).exists()) {
				winProducts.add(product);
			}
		}

		return winProducts;
	}

	public void updateProductOnClient(String pcname, String productname, int producttype,
			Map<String, String> updateValues) {
		if (updateProductOnClientItems == null) {
			updateProductOnClientItems = new ArrayList<>();
		}

		updateProductOnClient(pcname, productname, producttype, updateValues, updateProductOnClientItems);
	}

	private void updateProductOnClient(String pcname, String productname, int producttype,
			Map<String, String> updateValues, List<Map<String, Object>> updateItems) {
		Map<String, Object> values = new HashMap<>();

		values.put("productType", OpsiPackage.giveProductType(producttype));
		values.put("type", "ProductOnClient");
		values.put("clientId", pcname);
		values.put("productId", productname);
		values.putAll(updateValues);

		Logging.debug(this, "updateProductOnClient, values " + values);
		updateItems.add(values);
	}

	public boolean updateProductOnClients() {
		return updateProductOnClients(updateProductOnClientItems);
	}

	public boolean updateProductOnClients(Set<String> clients, String productName, int productType,
			Map<String, String> changedValues) {
		List<Map<String, Object>> updateCollection = new ArrayList<>();

		// collect updates for all clients
		for (String client : clients) {
			updateProductOnClient(client, productName, productType, changedValues, updateCollection);
		}

		// execute
		return updateProductOnClients(updateCollection);
	}

	// hopefully we get only updateItems for allowed clients
	private boolean updateProductOnClients(List<Map<String, Object>> updateItems) {
		Logging.info(this, "updateProductOnClients ");

		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return false;
		}

		boolean result = false;

		if (updateItems != null && !updateItems.isEmpty()) {
			Logging.info(this, "updateProductOnClients  updateItems.size " + updateItems.size());
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_UPDATE_OBJECTS,
					new Object[] { updateItems });
			result = exec.doCall(omc);
			// at any rate
			updateItems.clear();
		}

		return result;
	}

	public boolean resetLocalbootProducts(String[] selectedClients, boolean withDependencies) {
		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return false;
		}

		List<Map<String, Object>> deleteProductItems = produceDeleteProductItems(selectedClients,
				OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
		Logging.info(this, "resetLocalbootProducts deleteProductItems.size " + deleteProductItems.size());
		boolean result = resetProducts(deleteProductItems, withDependencies);
		Logging.debug(this, "resetLocalbootProducts result " + result);
		return result;
	}

	public boolean resetNetbootProducts(String[] selectedClients, boolean withDependencies) {
		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return false;
		}

		List<Map<String, Object>> deleteProductItems = produceDeleteProductItems(selectedClients,
				OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
		Logging.info(this, "resetNetbootProducts deleteProductItems.size " + deleteProductItems.size());
		boolean result = resetProducts(deleteProductItems, withDependencies);
		Logging.debug(this, "resetNetbootProducts result " + result);
		return result;
	}

	private List<Map<String, Object>> produceDeleteProductItems(String[] selectedClients, String productType) {
		List<Map<String, Object>> deleteProductItems = new ArrayList<>();
		List<Map<String, Object>> modifiedProductsOnClients = retrieveModifiedProductsOnClients(
				Arrays.asList(selectedClients));

		for (final String clientId : selectedClients) {
			List<String> modifiedProductsOnClient = modifiedProductsOnClients.stream()
					.filter(m -> clientId.equals(m.get("clientId"))).map(m -> (String) m.get("productId"))
					.collect(Collectors.toList());
			for (final String product : modifiedProductsOnClient) {
				Map<String, Object> productOnClientItem = Utils.createNOMitem("ProductOnClient");
				productOnClientItem.put("productType", productType);
				productOnClientItem.put("clientId", clientId);
				productOnClientItem.put("productId", product);
				deleteProductItems.add(productOnClientItem);
			}
		}

		return deleteProductItems;
	}

	private List<Map<String, Object>> retrieveModifiedProductsOnClients(List<String> clientIds) {
		String[] callAttributes = new String[] {};
		HashMap<String, Object> callFilter = new HashMap<>();
		callFilter.put("clientId", clientIds);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		return exec.getListOfMaps(omc);
	}

	private boolean resetProducts(Collection<Map<String, Object>> productItems, boolean withDependencies) {
		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return false;
		}

		boolean result = true;

		Logging.info(this, "resetProducts productItems.size " + productItems.size());

		if (!productItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_DELETE_OBJECTS,
					new Object[] { productItems.toArray() });

			result = exec.doCall(omc);

			Logging.debug(this, "resetProducts result " + result);

			if (result && withDependencies) {
				omc = new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_DELETE,
						new Object[] { productItems.stream().map(p -> p.get("productId")).toArray(), "*",
								productItems.stream().map(p -> p.get("clientId")).toArray() });

				result = exec.doCall(omc);
			}
		}

		Logging.debug(this, "resetProducts result " + result);

		return result;
	}

	private List<Map<String, String>> getProductDependencies(String depotId, String productId) {
		List<Map<String, String>> result = null;

		if (getDepot2product2dependencyInfosPD().get(depotId) != null) {
			result = getDepot2product2dependencyInfosPD().get(depotId).get(productId);
		}

		if (result == null) {
			result = new ArrayList<>();
		}

		Logging.debug(this,
				"getProductDependencies for depot, product " + depotId + ", " + productId + " , result " + result);
		return result;
	}

	public Map<String, List<Map<String, String>>> getProductDependencies(String depotId) {
		Map<String, List<Map<String, String>>> result = null;

		if (getDepot2product2dependencyInfosPD().get(depotId) != null) {
			result = getDepot2product2dependencyInfosPD().get(depotId);
		} else {
			result = new HashMap<>();
		}

		return result;
	}

	public Set<String> extendToDependentProducts(final Set<String> startProductSet, final String depot) {
		HashSet<String> notHandled = new HashSet<>(startProductSet);
		HashSet<String> endResultSet = new HashSet<>(startProductSet);
		HashSet<String> startResultSet = null;

		while (!notHandled.isEmpty()) {
			startResultSet = new HashSet<>(endResultSet);

			for (String prod : notHandled) {
				Logging.info(this, " extendToDependentProducts prod " + prod);
				for (Map<String, String> m : getProductDependencies(depot, prod)) {
					Logging.info(this, " extendToDependentProducts m " + m.get("requiredProductId"));
					endResultSet.add(m.get("requiredProductId"));
				}
			}

			notHandled = new HashSet<>(endResultSet);
			notHandled.removeAll(startResultSet);
		}

		return new TreeSet<>(endResultSet);
	}

	// collect productPropertyState updates and deletions
	public void setProductProperties(String pcname, String productname, Map<?, ?> properties,
			List<Map<String, Object>> updateCollection, List<Map<String, Object>> deleteCollection) {
		if (!(properties instanceof ConfigName2ConfigValue)) {
			Logging.warning(this, "! properties instanceof ConfigName2ConfigValue ");
			return;
		}

		Iterator<?> propertiesKeyIterator = properties.keySet().iterator();

		while (propertiesKeyIterator.hasNext()) {
			String propertyId = (String) propertiesKeyIterator.next();

			List<?> newValue = (List<?>) properties.get(propertyId);

			Map<String, Object> retrievedConfig = ((RetrievedMap) properties).getRetrieved();
			Object oldValue = retrievedConfig == null ? null : retrievedConfig.get(propertyId);

			if (newValue != oldValue) {
				Map<String, Object> state = new HashMap<>();
				state.put("type", "ProductPropertyState");
				state.put("objectId", pcname);
				state.put("productId", productname);
				state.put("propertyId", propertyId);

				if (newValue == null || newValue.equals(MapTableModel.nullLIST)) {
					Logging.debug(this, "setProductProperties,  requested deletion " + properties.get(propertyId));
					deleteState(state, deleteCollection, retrievedConfig, propertyId);
				} else {
					Logging.debug(this, "setProductProperties,  requested update " + properties.get(propertyId)
							+ " for oldValue " + oldValue);

					state.put("values", newValue);
					updateState(state, updateCollection, retrievedConfig, propertyId, properties.get(propertyId));
				}
			}
		}
	}

	private static void deleteState(Map<String, Object> state, List<Map<String, Object>> deleteCollection,
			Map<String, Object> retrievedConfig, String propertyId) {
		deleteCollection.add(state);

		// we hope that the update works and directly update the retrievedConfig
		if (retrievedConfig != null) {
			retrievedConfig.remove(propertyId);
		}
	}

	private void updateState(Map<String, Object> state, List<Map<String, Object>> updateCollection,
			Map<String, Object> retrievedConfig, String propertyId, Object propertyValue) {

		Logging.debug(this, "setProductProperties,  we have new state " + state);
		updateCollection.add(state);

		// we hope that the update works and directly update the retrievedConfig
		if (retrievedConfig != null) {
			retrievedConfig.put(propertyId, propertyValue);
		}
	}

	// collect productPropertyState updates and deletions in standard lists
	public void setProductProperties(String pcname, String productname, Map<?, ?> properties) {
		// old version

		if (productPropertyStateUpdateCollection == null) {
			productPropertyStateUpdateCollection = new ArrayList<>();
		}

		if (productPropertyStateDeleteCollection == null) {
			productPropertyStateDeleteCollection = new ArrayList<>();
		}

		setProductProperties(pcname, productname, properties, productPropertyStateUpdateCollection,
				productPropertyStateDeleteCollection);
	}

	// send productPropertyState updates and clear the collections for standard
	// collections
	public void setProductProperties() {
		setProductProperties(productPropertyStateUpdateCollection, productPropertyStateDeleteCollection);
	}

	// send productPropertyState updates and clear the collections
	private void setProductProperties(List<Map<String, Object>> updateCollection, List<?> deleteCollection) {
		Logging.debug(this, "setProductproperties() ");

		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return;
		}

		if (updateCollection != null && !updateCollection.isEmpty()
				&& exec.doCall(new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_UPDATE_OBJECTS,
						new Object[] { updateCollection }))) {
			updateCollection.clear();
		}

		if (deleteCollection != null && !deleteCollection.isEmpty()
				&& exec.doCall(new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_DELETE_OBJECTS,
						new Object[] { deleteCollection }))) {
			deleteCollection.clear();
		}
	}

	public void setCommonProductPropertyValue(Iterable<String> clientNames, String productName, String propertyName,
			List<String> values) {
		List<Map<String, Object>> updateCollection = new ArrayList<>();
		List<Map<String, Object>> deleteCollection = new ArrayList<>();

		// collect updates for all clients
		for (String client : clientNames) {
			Map<String, Object> newdata = new ConfigName2ConfigValue(null);

			newdata.put(propertyName, values);

			// collect the updates
			setProductProperties(client, productName, newdata, updateCollection, deleteCollection);
		}
		// execute updates
		setProductProperties(updateCollection, deleteCollection);
	}

	public Map<String, ListCellOptions> getProductPropertyOptionsMap(String depotId, String productId) {
		Map<String, ListCellOptions> result = null;

		if (getDepot2Product2PropertyDefinitionsPD().get(depotId) == null) {
			result = new HashMap<>();
			Logging.info("getProductPropertyOptionsMap: no productproperty definitions for depot " + depotId);
		} else {
			result = getDepot2Product2PropertyDefinitionsPD().get(depotId).get(productId);
		}

		if (result == null) {
			Logging.info("getProductPropertyOptionsMap: no productproperty definitions  for depot, product " + depotId
					+ ", " + productId);
			result = new HashMap<>();
		}

		return result;
	}

	public Map<String, ListCellOptions> getProductPropertyOptionsMap(String productId) {
		retrieveProductPropertyDefinitions();
		Map<String, ListCellOptions> result;

		Map<String, Map<String, ListCellOptions>> productPropertyDefinitions = cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_PROPERTY_DEFINITIONS, Map.class);
		if (productPropertyDefinitions == null) {
			result = new HashMap<>();
		} else {
			result = productPropertyDefinitions.get(productId);
			if (result == null) {
				result = new HashMap<>();
			}
		}

		return result;
	}

	public String getProductTitle(String product) {
		Map<String, Map<String, Object>> productGlobalInfos = getProductGlobalInfosPD(depotDataService.getDepot());
		Logging.info(this, "getProductTitle for product " + product + " on depot " + depotDataService.getDepot());
		Logging.info(this, "getProductTitle for productGlobalsInfos found number " + productGlobalInfos.size());
		Logging.info(this, "getProductTitle, productInfos " + productGlobalInfos.get(product));
		Object result = productGlobalInfos.get(product).get(ProductState.KEY_PRODUCT_NAME);
		Logging.info(this, "getProductTitle for product " + result);

		String resultS = null;
		if (result == null) {
			resultS = EMPTYFIELD;
		} else {
			resultS = "" + result;
		}
		return resultS;
	}

	public String getProductInfo(String product) {
		Map<String, Map<String, Object>> productGlobalInfos = getProductGlobalInfosPD(depotDataService.getDepot());
		String result = "" + productGlobalInfos.get(product).get(OpsiProductInfo.SERVICE_KEY_PRODUCT_DESCRIPTION);
		Logging.debug(this, " getProductInfo for product " + product + ": " + result);

		return result;
	}

	public String getProductHint(String product) {
		Map<String, Map<String, Object>> productGlobalInfos = getProductGlobalInfosPD(depotDataService.getDepot());
		return (String) productGlobalInfos.get(product).get(OpsiProductInfo.SERVICE_KEY_PRODUCT_ADVICE);
	}

	public String getProductVersion(String product) {
		Map<String, Map<String, Object>> productGlobalInfos = getProductGlobalInfosPD(depotDataService.getDepot());
		String result = (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);

		if (result == null) {
			result = EMPTYFIELD;
		}

		Logging.debug(this, "getProductVersion which? " + result + " for product: " + product);

		return result;
	}

	public String getProductPackageVersion(String product) {
		Map<String, Map<String, Object>> productGlobalInfos = getProductGlobalInfosPD(depotDataService.getDepot());
		return (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
	}

	public String getProductLockedInfo(String product) {
		Map<String, Map<String, Object>> productGlobalInfos = getProductGlobalInfosPD(depotDataService.getDepot());
		return (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_LOCKED);
	}

	private Map<String, String> getProductRequirements(String depotId, String productname, String requirementType) {
		Map<String, String> result = new HashMap<>();

		String depot = null;
		if (depotId == null) {
			depot = depotDataService.getDepot();
		} else {
			depot = depotId;
		}

		Logging.debug(this,
				"getProductRequirements productname, requirementType  " + productname + ", " + requirementType);

		List<Map<String, String>> dependenciesFor1product = getProductDependencies(depot, productname);

		if (dependenciesFor1product == null) {
			return result;
		}

		for (Map<String, String> aDependency : dependenciesFor1product) {
			Logging.debug(this, " dependency map : " + aDependency);

			if (requirementType.equals(NAME_REQUIREMENT_TYPE_ON_DEINSTALL)
					// we demand information for this type,
					// this is not specified by type in the dependency map
					// but only by the action value
					&& aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.UNINSTALL))) {
				result.put(aDependency.get("requiredProductId"),
						aDependency.get("requiredInstallationStatus") + ":" + aDependency.get("requiredAction"));
			} else {
				Logging.debug(this, " dependency map : ");

				boolean hasRequirementType = requirementType.equals(NAME_REQUIREMENT_TYPE_NEUTRAL)
						|| requirementType.equals(NAME_REQUIREMENT_TYPE_BEFORE)
						|| requirementType.equals(NAME_REQUIREMENT_TYPE_AFTER);
				boolean hasActionRequest = aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.SETUP))
						|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.ONCE))
						|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.ALWAYS))
						|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.CUSTOM));

				if (hasRequirementType && hasActionRequest
						&& aDependency.get("requirementType").equals(requirementType)) {
					result.put(aDependency.get("requiredProductId"),
							aDependency.get("requiredInstallationStatus") + ":" + aDependency.get("requiredAction"));
				}
			}
		}

		Logging.debug(this, "getProductRequirements depot, productname, requirementType  " + depotId + ", "
				+ productname + ", " + requirementType);
		Logging.info(this, "getProductRequirements " + result);

		return result;
	}

	public Map<String, String> getProductPreRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_BEFORE);
	}

	public Map<String, String> getProductRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_NEUTRAL);
	}

	public Map<String, String> getProductPostRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_AFTER);
	}

	public Map<String, String> getProductDeinstallRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_ON_DEINSTALL);
	}

	public Map<String, Boolean> getProductOnClientsDisplayFieldsNetbootProducts() {
		retrieveProductOnClientsDisplayFieldsNetbootProducts();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_NETBOOT_PRODUCTS,
				Map.class);
	}

	public void retrieveProductOnClientsDisplayFieldsNetbootProducts() {
		retrieveProductOnClientsDisplayFields(CacheIdentifier.PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_NETBOOT_PRODUCTS,
				KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT);
	}

	public Map<String, Boolean> getProductOnClientsDisplayFieldsLocalbootProducts() {
		retrieveProductOnClientsDisplayFieldsLocalbootProducts();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_LOCALBOOT_PRODUCTS,
				Map.class);
	}

	public void retrieveProductOnClientsDisplayFieldsLocalbootProducts() {
		retrieveProductOnClientsDisplayFields(CacheIdentifier.PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_LOCALBOOT_PRODUCTS,
				KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT);
	}

	private void retrieveProductOnClientsDisplayFields(CacheIdentifier cacheId, String key) {
		if (cacheManager.getCachedData(cacheId, Map.class) != null) {
			return;
		}
		Map<String, List<Object>> serverPropertyMap = configDataService.getConfigDefaultValuesPD();
		Map<String, ConfigOption> configOptions = configDataService.getConfigOptionsPD();
		Logging.debug(this, "getProductOnClientsDisplayFields() " + configOptions.get(key));

		List<String> configuredByService = Utils.takeAsStringList(serverPropertyMap.get(key));
		List<?> possibleValuesAccordingToService = new ArrayList<>();
		if (configOptions.get(key) != null) {
			possibleValuesAccordingToService = (List<?>) configOptions.get(key).get("possibleValues");
		}

		Logging.debug(this, "getProductOnClientsDisplayFields() possibleValuesAccordingToService "
				+ possibleValuesAccordingToService);

		if (configuredByService.isEmpty() || !((new HashSet<>(getPossibleValuesProductOnClientDisplayFields()))
				.equals(new HashSet<>(possibleValuesAccordingToService)))) {
			// we did not initialize server property
			configuredByService = produceProductOnClientDisplayfields(key);
		}

		Map<String, Boolean> productOnClientsDisplayFields = new LinkedHashMap<>();
		if (configuredByService == null) {
			Logging.warning(this, "configuredByService is null");
			cacheManager.setCachedData(cacheId, productOnClientsDisplayFields);
			return;
		}

		// key names from ProductState
		productOnClientsDisplayFields.put(ProductState.KEY_PRODUCT_ID, true);

		productOnClientsDisplayFields.put(ProductState.KEY_PRODUCT_NAME,
				configuredByService.indexOf(ProductState.KEY_PRODUCT_NAME) > -1);

		productOnClientsDisplayFields.put(ProductState.KEY_TARGET_CONFIGURATION,
				configuredByService.indexOf(ProductState.KEY_TARGET_CONFIGURATION) > -1);

		productOnClientsDisplayFields.put(ProductState.KEY_INSTALLATION_STATUS, true);

		productOnClientsDisplayFields.put(ProductState.KEY_INSTALLATION_INFO,
				configuredByService.indexOf(ProductState.KEY_INSTALLATION_INFO) > -1);

		productOnClientsDisplayFields.put(ProductState.KEY_ACTION_REQUEST, true);

		productOnClientsDisplayFields.put(ProductState.KEY_PRODUCT_PRIORITY,
				configuredByService.indexOf(ProductState.KEY_PRODUCT_PRIORITY) > -1);
		productOnClientsDisplayFields.put(ProductState.KEY_POSITION,
				configuredByService.indexOf(ProductState.KEY_POSITION) > -1);

		productOnClientsDisplayFields.put(ProductState.KEY_LAST_STATE_CHANGE,
				configuredByService.indexOf(ProductState.KEY_LAST_STATE_CHANGE) > -1);

		productOnClientsDisplayFields.put(ProductState.KEY_VERSION_INFO, true);
		cacheManager.setCachedData(cacheId, productOnClientsDisplayFields);
	}

	private List<String> produceProductOnClientDisplayfields(String key) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return null;
		}

		List<String> result = getDefaultValuesProductOnClientDisplayFields();
		List<String> possibleValues = getPossibleValuesProductOnClientDisplayFields();

		// create config for service
		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");
		item.put("ident", key);
		item.put("description", "");
		item.put("defaultValues", result);
		item.put("possibleValues", possibleValues);
		item.put("editable", false);
		item.put("multiValue", true);

		Logging.info(this, "produceProductOnClientDisplayfields");
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { item });
		exec.doCall(omc);
		return result;
	}

	private static List<String> getDefaultValuesProductOnClientDisplayFields() {
		List<String> result = new ArrayList<>();
		result.add("productId");
		result.add(ProductState.KEY_INSTALLATION_STATUS);
		result.add(ProductState.KEY_INSTALLATION_INFO);
		result.add(ProductState.KEY_ACTION_REQUEST);
		result.add(ProductState.KEY_VERSION_INFO);
		return result;
	}

	private static List<String> getPossibleValuesProductOnClientDisplayFields() {
		List<String> possibleValues = new ArrayList<>();
		possibleValues.add("productId");
		possibleValues.add(ProductState.KEY_PRODUCT_NAME);
		possibleValues.add(ProductState.KEY_INSTALLATION_STATUS);
		possibleValues.add(ProductState.KEY_INSTALLATION_INFO);
		possibleValues.add(ProductState.KEY_ACTION_REQUEST);
		possibleValues.add(ProductState.KEY_PRODUCT_PRIORITY);
		possibleValues.add(ProductState.KEY_POSITION);
		possibleValues.add(ProductState.KEY_LAST_STATE_CHANGE);
		possibleValues.add(ProductState.KEY_TARGET_CONFIGURATION);
		possibleValues.add(ProductState.KEY_VERSION_INFO);
		return possibleValues;
	}
}
