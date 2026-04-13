/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */
package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.type.licenses.LicenseContractEntry;
import de.uib.configed.gui.type.licenses.LicenseEntry;
import de.uib.configed.gui.type.licenses.LicensePoolXOpsiProduct;
import de.uib.configed.gui.type.licenses.LicenseUsableForEntry;
import de.uib.configed.gui.type.licenses.LicenseUsageEntry;
import de.uib.configed.gui.type.licenses.LicensepoolEntry;
import de.uib.configed.share.ConfigUtils;
import de.uib.configed.share.datastructure.StringValuedRelationElement;
import de.uib.configed.share.logging.Logging;

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
public class LicenseDataService extends DataService {
	private List<LicenseUsageEntry> itemsDeletionLicenseUsage;

	public LicenseDataService(DataServices dataServices) {
		super(dataServices);
	}

	public Map<String, LicensepoolEntry> getLicensePoolsPD() {
		retrieveLicensePoolsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.LICENSE_POOLS, Map.class);
	}

	public LicensePoolXOpsiProduct getLicensePoolXOpsiProductPD() {
		retrieveLicensePoolsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT,
				LicensePoolXOpsiProduct.class);
	}

	public void retrieveLicensePoolsPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.LICENSE_POOLS)
				|| dataServices.cacheManager.isDataCached(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT)) {
			return;
		}

		LicensePoolXOpsiProduct licensePoolXOpsiProduct = new LicensePoolXOpsiProduct();
		Map<String, LicensepoolEntry> licensePools = new TreeMap<>();
		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			List<Map<String, Object>> retrieved = dataServices.exec
					.getListOfMaps(RPCMethodName.LICENSE_POOL_GET_OBJECTS);
			for (Map<String, Object> importedEntry : retrieved) {
				LicensepoolEntry entry = new LicensepoolEntry(POJOReMapper.remap(importedEntry));
				licensePools.put(entry.getLicensepoolId(), entry);
				licensePoolXOpsiProduct.integrateRawFromService(importedEntry);
			}
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.LICENSE_POOLS, licensePools);
		dataServices.cacheManager.setCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, licensePoolXOpsiProduct);
	}

	public Map<String, LicenseContractEntry> getLicenseContractsPD() {
		retrieveLicenseContractsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS, Map.class);
	}

	public Map<String, Set<String>> getLicenseContractsToNotifyPD() {
		retrieveLicenseContractsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, Map.class);
	}

	public void retrieveLicenseContractsPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.LICENSE_CONTRACTS)
				|| dataServices.cacheManager.isDataCached(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY)) {
			return;
		}

		String today = new java.sql.Date(System.currentTimeMillis()).toString();
		Map<String, LicenseContractEntry> licenseContracts = new HashMap<>();
		Map<String, Set<String>> contractsToNotify = new HashMap<>();
		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			List<Map<String, Object>> retrieved = dataServices.exec
					.getListOfMaps(RPCMethodName.LICENSE_CONTRACT_GET_OBJECTS);

			for (Map<String, Object> importedEntry : retrieved) {
				LicenseContractEntry entry = new LicenseContractEntry(importedEntry);
				licenseContracts.put(entry.getId(), entry);

				String notiDate = entry.get(LicenseContractEntry.NOTIFICATION_DATE_KEY);
				if (notiDate != null && !notiDate.isBlank() && notiDate.compareTo(today) <= 0) {
					Set<String> contractSet = contractsToNotify.computeIfAbsent(notiDate, s -> new TreeSet<>());

					contractSet.add(entry.getId());
				}
			}
			Logging.info(this, "contractsToNotify ", contractsToNotify);
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.LICENSE_CONTRACTS, licenseContracts);
		dataServices.cacheManager.setCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, contractsToNotify);
	}

	public Map<String, LicenseEntry> getLicensesPD() {
		retrieveLicensesPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.LICENSES, Map.class);
	}

	public void retrieveLicensesPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.LICENSES)) {
			return;
		}
		Map<String, LicenseEntry> licenses = new HashMap<>();
		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			List<Map<String, Object>> retrieved = dataServices.exec
					.getListOfMaps(RPCMethodName.SOFTWARE_LICENSE_GET_OBJECTS);
			for (Map<String, Object> importedEntry : retrieved) {
				LicenseEntry entry = new LicenseEntry(importedEntry);
				licenses.put(entry.getId(), entry);
			}
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.LICENSES, licenses);
	}

	public List<LicenseUsableForEntry> getLicenseUsabilitiesPD() {
		retrieveSoftwareLicense2LicensePoolPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.LICENSE_USABILITIES, List.class);
	}

	public Map<String, Map<String, Object>> getRelationsSoftwareL2LPool() {
		retrieveSoftwareLicense2LicensePoolPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL, Map.class);
	}

	public void retrieveSoftwareLicense2LicensePoolPD() {
		if (!dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)
				|| dataServices.cacheManager.isDataCached(CacheIdentifier.LICENSE_USABILITIES)
				|| dataServices.cacheManager.isDataCached(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL)) {
			return;
		}
		Map<String, Map<String, Object>> rowsSoftwareL2LPool = new HashMap<>();
		List<LicenseUsableForEntry> licenseUsabilities = new ArrayList<>();
		List<Map<String, Object>> softwareL2LPools = dataServices.exec
				.getListOfMaps(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_GET_OBJECTS);
		for (Map<String, Object> softwareL2LPool : softwareL2LPools) {
			LicenseUsableForEntry entry = LicenseUsableForEntry.produceFrom(softwareL2LPool);
			licenseUsabilities.add(entry);
			softwareL2LPool.remove("ident");
			softwareL2LPool.remove("type");
			rowsSoftwareL2LPool.put(ConfigUtils.pseudokey(new String[] {
					(String) softwareL2LPool.get("softwareLicenseId"), (String) softwareL2LPool.get("licensePoolId") }),
					softwareL2LPool);
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.LICENSE_USABILITIES, licenseUsabilities);
		dataServices.cacheManager.setCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL, rowsSoftwareL2LPool);
	}

	// retrieves the used software license - or tries to reserve one - for the given
	// host and license pool
	public String getLicenseUsage(String hostId, String licensePoolId) {
		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Map<String, Object> resultMap = dataServices.exec
					.getMapResult(RPCMethodName.LICENSE_ON_CLIENT_GET_OR_CREATE_OBJECT, hostId, licensePoolId);

			if (!resultMap.isEmpty()) {
				return ConfigUtils
						.pseudokey(new String[] { "" + resultMap.get(OpsiServiceNOMPersistenceController.HOST_KEY),
								"" + resultMap.get("softwareLicenseId"), "" + resultMap.get("licensePoolId") });
			}
		}

		return null;
	}

	public String editLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId, String licenseKey,
			String notes) {
		if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			return null;
		}

		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Map<String, Object> resultMap = dataServices.exec.getMapResult(RPCMethodName.LICENSE_ON_CLIENT_CREATE,
					softwareLicenseId, licensePoolId, hostId, licenseKey, notes);

			if (!resultMap.isEmpty()) {
				return ConfigUtils
						.pseudokey(new String[] { "" + resultMap.get(OpsiServiceNOMPersistenceController.HOST_KEY),
								"" + resultMap.get("softwareLicenseId"), "" + resultMap.get("licensePoolId") });
			}
		}

		return null;
	}

	public void addDeletionLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (itemsDeletionLicenseUsage == null) {
			itemsDeletionLicenseUsage = new ArrayList<>();
		}
		addDeletionLicenseUsage(hostId, softwareLicenseId, licensePoolId, itemsDeletionLicenseUsage);
	}

	private void addDeletionLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId,
			List<LicenseUsageEntry> deletionItems) {
		if (deletionItems == null || !dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)
				|| !dataServices.userRoles.hasServerFullPermissionPD()) {
			return;
		}

		LicenseUsageEntry deletionItem = new LicenseUsageEntry(hostId, softwareLicenseId, licensePoolId, "", "");
		deletionItems.add(deletionItem);
	}

	public boolean executeCollectedDeletionsLicenseUsage() {
		Logging.info(this, "executeCollectedDeletionsLicenseUsage itemsDeletionLicenseUsage == null ",
				itemsDeletionLicenseUsage == null);
		boolean result = false;
		if (itemsDeletionLicenseUsage == null) {
			result = true;
		} else if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			result = false;
		} else if (!dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			result = false;
		} else {
			List<Map<String, Object>> jsonPreparedList = new ArrayList<>();
			for (LicenseUsageEntry item : itemsDeletionLicenseUsage) {
				jsonPreparedList.add(item.getNOMobject());
			}

			result = dataServices.exec.doCall(RPCMethodName.LICENSE_ON_CLIENT_DELETE_OBJECTS, jsonPreparedList);

			if (result) {
				Map<String, LicenseUsageEntry> rowsLicensesUsage = dataServices.cacheManager
						.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
				Map<String, List<LicenseUsageEntry>> fClient2LicensesUsageList = dataServices.cacheManager
						.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
				for (LicenseUsageEntry item : itemsDeletionLicenseUsage) {
					String key = item.getPseudoKey();
					String hostX = item.getClientId();

					LicenseUsageEntry rowmap = rowsLicensesUsage.get(key);
					rowsLicensesUsage.remove(key);
					fClient2LicensesUsageList.get(hostX).remove(rowmap);

					Logging.debug(this, "deleteLicenseUsage check fClient2LicensesUsageList ",
							fClient2LicensesUsageList.get(hostX));
				}
			}

			itemsDeletionLicenseUsage.clear();
		}

		return result;
	}

	public boolean deleteLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			return false;
		}

		boolean result = false;

		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			result = dataServices.exec.doCall(RPCMethodName.LICENSE_ON_CLIENT_DELETE, softwareLicenseId, licensePoolId,
					hostId);
			if (result) {
				Map<String, LicenseUsageEntry> rowsLicensesUsage = dataServices.cacheManager
						.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
				Map<String, List<LicenseUsageEntry>> fClient2LicensesUsageList = dataServices.cacheManager
						.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
				String key = LicenseUsageEntry.produceKey(hostId, licensePoolId, softwareLicenseId);
				LicenseUsageEntry rowmap = rowsLicensesUsage.get(key);
				rowsLicensesUsage.remove(key);
				fClient2LicensesUsageList.get(hostId).remove(rowmap);
				Logging.info(this, "deleteLicenseUsage check fClient2LicensesUsageList ",
						fClient2LicensesUsageList.get(hostId));
			}
		}

		return result;
	}

	public List<LicenseUsageEntry> getLicenseUsagesPD() {
		retrieveLicenseUsagesPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.LICENSE_USAGE, List.class);
	}

	public void retrieveLicenseUsagesPD() {
		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)
				&& dataServices.cacheManager.isDataCached(CacheIdentifier.LICENSE_USAGE)) {
			return;
		}
		Logging.info(this, "retrieveLicenseUsages");
		List<LicenseUsageEntry> licenseUsages = new ArrayList<>();
		List<Map<String, Object>> retrieved = dataServices.exec
				.getListOfMaps(RPCMethodName.LICENSE_ON_CLIENT_GET_OBJECTS);

		for (Map<String, Object> importedEntry : retrieved) {
			LicenseUsageEntry entry = new LicenseUsageEntry(importedEntry);

			licenseUsages.add(entry);
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.LICENSE_USAGE, licenseUsages);
	}

	public Map<String, Map<String, String>> getRelationsProductId2LPool() {
		Map<String, Map<String, String>> rowsLicensePoolXOpsiProduct = new HashMap<>();
		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			List<StringValuedRelationElement> relations = getLicensePoolXOpsiProductPD().getRelations();
			Logging.info(this, "licensePoolXOpsiProduct size ", relations.size());
			for (StringValuedRelationElement element : relations) {
				rowsLicensePoolXOpsiProduct
						.put(ConfigUtils.pseudokey(new String[] { element.get(LicensePoolXOpsiProduct.LICENSE_POOL_KEY),
								element.get(LicensePoolXOpsiProduct.PRODUCT_ID_KEY) }), element);
			}
		}
		Logging.info(this, "rowsLicensePoolXOpsiProduct size ", rowsLicensePoolXOpsiProduct.size());
		return rowsLicensePoolXOpsiProduct;
	}

	public Map<String, LicenseUsageEntry> getRowsLicensesUsagePD() {
		retrieveLicensesUsagePD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
	}

	public Map<String, List<LicenseUsageEntry>> getFClient2LicensesUsageListPD() {
		retrieveLicensesUsagePD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
	}

	public void retrieveLicensesUsagePD() {
		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)
				&& dataServices.cacheManager.isDataCached(
						List.of(CacheIdentifier.ROWS_LICENSE_USAGE, CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST))) {
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
		dataServices.cacheManager.setCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, rowsLicensesUsage);
		dataServices.cacheManager.setCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST,
				fClient2LicensesUsageList);
	}

	// returns the ID of the edited data record
	public String editLicenseContract(String licenseContractId, String partner, String conclusionDate,
			String notificationDate, String expirationDate, String notes) {
		if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			return "";
		}
		String result = "";

		Logging.debug(this, "editLicenseContract ", licenseContractId);

		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			// the method gives the first letter instead of the complete string as return
			// value, therefore we set it in a shortcut:

			if (dataServices.exec.doCall(RPCMethodName.LICENSE_CONTRACT_CREATE, licenseContractId, "", notes, partner,
					conclusionDate, notificationDate, expirationDate)) {
				result = licenseContractId;
			} else {
				Logging.error(this, "could not create license ", licenseContractId);
			}
		}

		Logging.debug(this, "editLicenseContract result ", result);

		return result;
	}

	public boolean deleteLicenseContract(String licenseContractId) {
		if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			return false;
		}

		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return dataServices.exec.doCall(RPCMethodName.LICENSE_CONTRACT_DELETE, licenseContractId);
		}

		return false;
	}

	// returns the ID of the edited data record
	public String editLicensePool(String licensePoolId, String description) {
		if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			return "";
		}

		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			if (dataServices.exec.doCall(RPCMethodName.LICENSE_POOL_CREATE, licensePoolId, description)) {
				return licensePoolId;
			} else {
				Logging.warning(this, "could not create licensepool ", licensePoolId);
			}
		}

		return "";
	}

	public boolean deleteLicensePool(String licensePoolId) {
		Logging.info(this, "deleteLicensePool ", licensePoolId);

		if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			return false;
		}

		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return dataServices.exec.doCall(RPCMethodName.LICENSE_POOL_DELETE, licensePoolId);
		}

		return false;
	}

	public String editRelationProductId2LPool(String productId, String licensePoolId) {
		if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			return "";
		}

		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Map<String, Object> licensePool = getLicensePool(licensePoolId);

			// Replace old product list with actualized list
			List<Object> licensePoolProductIds = new ArrayList<>((List<?>) licensePool.get("productIds"));
			licensePoolProductIds.add(productId);
			licensePool.put("productIds", licensePoolProductIds);

			if (dataServices.exec.doCall(RPCMethodName.LICENSE_POOL_UPDATE_OBJECT, licensePool)) {
				return licensePoolId;
			} else {
				Logging.error(this, "could not update product ", productId, " to licensepool ", licensePoolId);
			}
		}

		return "";
	}

	public boolean deleteRelationProductId2LPool(String productId, String licensePoolId) {
		if (!dataServices.userRoles.hasServerFullPermissionPD()) {
			return false;
		}

		if (dataServices.module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Map<String, Object> licensePool = getLicensePool(licensePoolId);
			// Replace old product list with actualized list
			List<Object> licensePoolProductIds = new ArrayList<>((List<?>) licensePool.get("productIds"));
			licensePoolProductIds.remove(productId);
			licensePool.put("productIds", licensePoolProductIds);

			return dataServices.exec.doCall(RPCMethodName.LICENSE_POOL_UPDATE_OBJECT, licensePool);
		}

		return false;
	}

	public Map<String, Object> getLicensePool(String licensePoolId) {
		Map<String, String> callFilter = Map.of("id", licensePoolId);
		return dataServices.exec.getListOfMaps(RPCMethodName.LICENSE_POOL_GET_OBJECTS, Set.of(), callFilter).get(0);
	}
}
