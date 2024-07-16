/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */
package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.configed.type.licenses.LicenseContractEntry;
import de.uib.configed.type.licenses.LicenseEntry;
import de.uib.configed.type.licenses.LicensePoolXOpsiProduct;
import de.uib.configed.type.licenses.LicenseUsableForEntry;
import de.uib.configed.type.licenses.LicenseUsageEntry;
import de.uib.configed.type.licenses.LicensepoolEntry;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utils.Utils;
import de.uib.utils.datastructure.StringValuedRelationElement;
import de.uib.utils.logging.Logging;

/**
 * Provides methods for working with license data on the server.
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
public class LicenseDataService {
	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;
	private UserRolesConfigDataService userRolesConfigDataService;
	private ModuleDataService moduleDataService;

	private List<LicenseUsageEntry> itemsDeletionLicenseUsage;

	public LicenseDataService(AbstractPOJOExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setModuleDataService(ModuleDataService moduleDataService) {
		this.moduleDataService = moduleDataService;
	}

	public Map<String, LicensepoolEntry> getLicensePoolsPD() {
		retrieveLicensePoolsPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_POOLS, Map.class);
	}

	public LicensePoolXOpsiProduct getLicensePoolXOpsiProductPD() {
		retrieveLicensePoolsPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, LicensePoolXOpsiProduct.class);
	}

	public void retrieveLicensePoolsPD() {
		if (cacheManager.isDataCached(CacheIdentifier.LICENSE_POOLS)
				|| cacheManager.isDataCached(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT)) {
			return;
		}

		LicensePoolXOpsiProduct licensePoolXOpsiProduct = new LicensePoolXOpsiProduct();
		Map<String, LicensepoolEntry> licensePools = new TreeMap<>();
		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_GET_OBJECTS,
					new Object[] { new Object[0], new HashMap<>() });
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
			for (Map<String, Object> importedEntry : retrieved) {
				LicensepoolEntry entry = new LicensepoolEntry(importedEntry);
				licensePools.put(entry.getLicensepoolId(), entry);
				licensePoolXOpsiProduct.integrateRawFromService(importedEntry);
			}
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_POOLS, licensePools);
		cacheManager.setCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, licensePoolXOpsiProduct);
	}

	public Map<String, LicenseContractEntry> getLicenseContractsPD() {
		retrieveLicenseContractsPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS, Map.class);
	}

	public NavigableMap<String, NavigableSet<String>> getLicenseContractsToNotifyPD() {
		retrieveLicenseContractsPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, NavigableMap.class);
	}

	public void retrieveLicenseContractsPD() {
		if (cacheManager.isDataCached(CacheIdentifier.LICENSE_CONTRACTS)
				|| cacheManager.isDataCached(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY)) {
			return;
		}

		String today = new java.sql.Date(System.currentTimeMillis()).toString();
		Map<String, LicenseContractEntry> licenseContracts = new HashMap<>();
		NavigableMap<String, NavigableSet<String>> contractsToNotify = new TreeMap<>();
		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_CONTRACT_GET_OBJECTS, new Object[] {});
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

			for (Map<String, Object> importedEntry : retrieved) {
				LicenseContractEntry entry = new LicenseContractEntry(importedEntry);
				licenseContracts.put(entry.getId(), entry);

				String notiDate = entry.get(LicenseContractEntry.NOTIFICATION_DATE_KEY);
				if (notiDate != null && !notiDate.isBlank() && notiDate.compareTo(today) <= 0) {
					NavigableSet<String> contractSet = contractsToNotify.computeIfAbsent(notiDate,
							s -> new TreeSet<>());

					contractSet.add(entry.getId());
				}
			}
			Logging.info(this, "contractsToNotify " + contractsToNotify);
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_CONTRACTS, licenseContracts);
		cacheManager.setCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, contractsToNotify);
	}

	public Map<String, LicenseEntry> getLicensesPD() {
		retrieveLicensesPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSES, Map.class);
	}

	public void retrieveLicensesPD() {
		if (cacheManager.isDataCached(CacheIdentifier.LICENSES)) {
			return;
		}
		Map<String, LicenseEntry> licenses = new HashMap<>();
		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_GET_OBJECTS, new Object[0]);
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
			for (Map<String, Object> importedEntry : retrieved) {
				LicenseEntry entry = new LicenseEntry(importedEntry);
				licenses.put(entry.getId(), entry);
			}
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSES, licenses);
	}

	public List<LicenseUsableForEntry> getLicenseUsabilitiesPD() {
		retrieveSoftwareLicense2LicensePoolPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_USABILITIES, List.class);
	}

	public Map<String, Map<String, Object>> getRelationsSoftwareL2LPool() {
		retrieveSoftwareLicense2LicensePoolPD();
		return cacheManager.getCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL, Map.class);
	}

	public void retrieveSoftwareLicense2LicensePoolPD() {
		if (!moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)
				|| cacheManager.isDataCached(CacheIdentifier.LICENSE_USABILITIES)
				|| cacheManager.isDataCached(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL)) {
			return;
		}
		Map<String, Map<String, Object>> rowsSoftwareL2LPool = new HashMap<>();
		List<LicenseUsableForEntry> licenseUsabilities = new ArrayList<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_GET_OBJECTS,
				new Object[0]);
		List<Map<String, Object>> softwareL2LPools = exec.getListOfMaps(omc);
		for (Map<String, Object> softwareL2LPool : softwareL2LPools) {
			LicenseUsableForEntry entry = LicenseUsableForEntry.produceFrom(softwareL2LPool);
			licenseUsabilities.add(entry);
			softwareL2LPool.remove("ident");
			softwareL2LPool.remove("type");
			rowsSoftwareL2LPool.put(Utils.pseudokey(new String[] { (String) softwareL2LPool.get("softwareLicenseId"),
					(String) softwareL2LPool.get("licensePoolId") }), softwareL2LPool);
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_USABILITIES, licenseUsabilities);
		cacheManager.setCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL, rowsSoftwareL2LPool);
	}

	// retrieves the used software license - or tries to reserve one - for the given
	// host and license pool
	public String getLicenseUsage(String hostId, String licensePoolId) {
		String result = null;
		Map<String, Object> resultMap = null;

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc0 = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_GET_OR_CREATE_OBJECT,
					new String[] { hostId, licensePoolId });

			resultMap = exec.getMapResult(omc0);

			if (!resultMap.isEmpty()) {
				result = Utils
						.pseudokey(new String[] { "" + resultMap.get(OpsiServiceNOMPersistenceController.HOST_KEY),
								"" + resultMap.get("softwareLicenseId"), "" + resultMap.get("licensePoolId") });
			}
		}

		return result;
	}

	public String editLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId, String licenseKey,
			String notes) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return null;
		}

		String result = null;
		Map<String, Object> resultMap = null;

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_CREATE,
					new String[] { softwareLicenseId, licensePoolId, hostId, licenseKey, notes });

			resultMap = exec.getMapResult(omc);

			if (!resultMap.isEmpty()) {
				result = Utils
						.pseudokey(new String[] { "" + resultMap.get(OpsiServiceNOMPersistenceController.HOST_KEY),
								"" + resultMap.get("softwareLicenseId"), "" + resultMap.get("licensePoolId") });
			}
		}

		return result;
	}

	public void addDeletionLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (itemsDeletionLicenseUsage == null) {
			itemsDeletionLicenseUsage = new ArrayList<>();
		}
		addDeletionLicenseUsage(hostId, softwareLicenseId, licensePoolId, itemsDeletionLicenseUsage);
	}

	private void addDeletionLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId,
			List<LicenseUsageEntry> deletionItems) {
		if (deletionItems == null || !moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)
				|| !userRolesConfigDataService.hasServerFullPermissionPD()) {
			return;
		}

		LicenseUsageEntry deletionItem = new LicenseUsageEntry(hostId, softwareLicenseId, licensePoolId, "", "");
		deletionItems.add(deletionItem);
	}

	public boolean executeCollectedDeletionsLicenseUsage() {
		Logging.info(this, "executeCollectedDeletionsLicenseUsage itemsDeletionLicenseUsage == null "
				+ (itemsDeletionLicenseUsage == null));
		boolean result = false;
		if (itemsDeletionLicenseUsage == null) {
			result = true;
		} else if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			result = false;
		} else if (!moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			result = false;
		} else {
			List<Map<String, Object>> jsonPreparedList = new ArrayList<>();
			for (LicenseUsageEntry item : itemsDeletionLicenseUsage) {
				jsonPreparedList.add(item.getNOMobject());
			}

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_DELETE_OBJECTS,
					new Object[] { jsonPreparedList });

			result = exec.doCall(omc);

			if (result) {
				Map<String, LicenseUsageEntry> rowsLicensesUsage = cacheManager
						.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
				Map<String, List<LicenseUsageEntry>> fClient2LicensesUsageList = cacheManager
						.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
				for (LicenseUsageEntry item : itemsDeletionLicenseUsage) {
					String key = item.getPseudoKey();
					String hostX = item.getClientId();

					LicenseUsageEntry rowmap = rowsLicensesUsage.get(key);
					rowsLicensesUsage.remove(key);
					fClient2LicensesUsageList.get(hostX).remove(rowmap);

					Logging.debug(this, "deleteLicenseUsage check fClient2LicensesUsageList "
							+ fClient2LicensesUsageList.get(hostX));
				}
			}

			itemsDeletionLicenseUsage.clear();
		}

		return result;
	}

	public boolean deleteLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		boolean result = false;

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_DELETE,
					new String[] { softwareLicenseId, licensePoolId, hostId });
			result = exec.doCall(omc);
			if (result) {
				Map<String, LicenseUsageEntry> rowsLicensesUsage = cacheManager
						.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
				Map<String, List<LicenseUsageEntry>> fClient2LicensesUsageList = cacheManager
						.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
				String key = LicenseUsageEntry.produceKey(hostId, licensePoolId, softwareLicenseId);
				LicenseUsageEntry rowmap = rowsLicensesUsage.get(key);
				rowsLicensesUsage.remove(key);
				fClient2LicensesUsageList.get(hostId).remove(rowmap);
				Logging.info(this,
						"deleteLicenseUsage check fClient2LicensesUsageList " + fClient2LicensesUsageList.get(hostId));
			}
		}

		return result;
	}

	public List<LicenseUsageEntry> getLicenseUsagesPD() {
		retrieveLicenseUsagesPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_USAGE, List.class);
	}

	public void retrieveLicenseUsagesPD() {
		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)
				&& cacheManager.isDataCached(CacheIdentifier.LICENSE_USAGE)) {
			return;
		}
		Logging.info(this, "retrieveLicenseUsages");
		List<LicenseUsageEntry> licenseUsages = new ArrayList<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_GET_OBJECTS, new Object[0]);
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

		for (Map<String, Object> importedEntry : retrieved) {
			LicenseUsageEntry entry = new LicenseUsageEntry(importedEntry);

			licenseUsages.add(entry);
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_USAGE, licenseUsages);
	}

	public Map<String, Map<String, String>> getRelationsProductId2LPool() {
		Map<String, Map<String, String>> rowsLicensePoolXOpsiProduct = new HashMap<>();
		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Logging.info(this, "licensePoolXOpsiProduct size " + getLicensePoolXOpsiProductPD().size());
			for (StringValuedRelationElement element : getLicensePoolXOpsiProductPD()) {
				rowsLicensePoolXOpsiProduct
						.put(Utils.pseudokey(new String[] { element.get(LicensePoolXOpsiProduct.LICENSE_POOL_KEY),
								element.get(LicensePoolXOpsiProduct.PRODUCT_ID_KEY) }), element);
			}
		}
		Logging.info(this, "rowsLicensePoolXOpsiProduct size " + rowsLicensePoolXOpsiProduct.size());
		return rowsLicensePoolXOpsiProduct;
	}

	public Map<String, LicenseUsageEntry> getRowsLicensesUsagePD() {
		retrieveLicensesUsagePD();
		return cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
	}

	public Map<String, List<LicenseUsageEntry>> getFClient2LicensesUsageListPD() {
		retrieveLicensesUsagePD();
		return cacheManager.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
	}

	private void retrieveLicensesUsagePD() {
		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT) && cacheManager.isDataCached(
				Arrays.asList(CacheIdentifier.ROWS_LICENSE_USAGE, CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST))) {
			return;
		}

		Map<String, LicenseUsageEntry> rowsLicensesUsage = new HashMap<>();
		Map<String, List<LicenseUsageEntry>> fClient2LicensesUsageList = new HashMap<>();
		for (LicenseUsageEntry m : getLicenseUsagesPD()) {
			rowsLicensesUsage.put(m.getPseudoKey(), m);
			List<LicenseUsageEntry> licensesUsagesForClient = fClient2LicensesUsageList.computeIfAbsent(m.getClientId(),
					s -> new ArrayList<>());
			licensesUsagesForClient.add(m);
		}
		cacheManager.setCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, rowsLicensesUsage);
		cacheManager.setCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, fClient2LicensesUsageList);
	}

	// returns the ID of the edited data record
	public String editLicenseContract(String licenseContractId, String partner, String conclusionDate,
			String notificationDate, String expirationDate, String notes) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return "";
		}
		String result = "";

		Logging.debug(this, "editLicenseContract " + licenseContractId);

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_CONTRACT_CREATE, new String[] {
					licenseContractId, "", notes, partner, conclusionDate, notificationDate, expirationDate });

			// the method gives the first letter instead of the complete string as return
			// value, therefore we set it in a shortcut:

			if (exec.doCall(omc)) {
				result = licenseContractId;
			} else {
				Logging.error(this, "could not create license ", licenseContractId);
			}
		}

		Logging.debug(this, "editLicenseContract result " + result);

		return result;
	}

	public boolean deleteLicenseContract(String licenseContractId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_CONTRACT_DELETE,
					new String[] { licenseContractId });
			return exec.doCall(omc);
		}

		return false;
	}

	// returns the ID of the edited data record
	public String editLicensePool(String licensePoolId, String description) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return "";
		}

		String result = "";

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_CREATE,
					new String[] { licensePoolId, description });

			if (exec.doCall(omc)) {
				result = licensePoolId;
			} else {
				Logging.warning(this, "could not create licensepool ", licensePoolId);
			}
		}

		return result;
	}

	public boolean deleteLicensePool(String licensePoolId) {
		Logging.info(this, "deleteLicensePool " + licensePoolId);

		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_DELETE, new Object[] { licensePoolId });
			return exec.doCall(omc);
		}

		return false;
	}

	public String editRelationProductId2LPool(String productId, String licensePoolId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return "";
		}

		String result = "";

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Map<String, Object> licensePool = getLicensePool(licensePoolId);

			// Replace old product list with actualized list
			List<Object> licensePoolProductIds = new ArrayList<>((List<?>) licensePool.get("productIds"));
			licensePoolProductIds.add(productId);
			licensePool.put("productIds", licensePoolProductIds);

			if (exec.doCall(
					new OpsiMethodCall(RPCMethodName.LICENSE_POOL_UPDATE_OBJECT, new Object[] { licensePool }))) {
				result = licensePoolId;
			} else {
				Logging.error(this, "could not update product ", productId, " to licensepool ", licensePoolId);
			}
		}

		return result;
	}

	public boolean deleteRelationProductId2LPool(String productId, String licensePoolId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Map<String, Object> licensePool = getLicensePool(licensePoolId);
			// Replace old product list with actualized list
			List<Object> licensePoolProductIds = new ArrayList<>((List<?>) licensePool.get("productIds"));
			licensePoolProductIds.remove(productId);
			licensePool.put("productIds", licensePoolProductIds);

			return exec
					.doCall(new OpsiMethodCall(RPCMethodName.LICENSE_POOL_UPDATE_OBJECT, new Object[] { licensePool }));
		}

		return false;
	}

	public Map<String, Object> getLicensePool(String licensePoolId) {
		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("id", licensePoolId);
		return exec.getListOfMaps(
				new OpsiMethodCall(RPCMethodName.LICENSE_POOL_GET_OBJECTS, new Object[] { callAttributes, callFilter }))
				.get(0);
	}
}
