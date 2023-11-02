/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */
package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.configed.type.licences.LicenceContractEntry;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.configed.type.licences.LicencePoolXOpsiProduct;
import de.uib.configed.type.licences.LicenceUsableForEntry;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.configed.type.licences.TableLicenceContracts;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import utils.Utils;

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
	private AbstractExecutioner exec;
	private UserRolesConfigDataService userRolesConfigDataService;
	private ModuleDataService moduleDataService;

	private List<LicenceUsageEntry> itemsDeletionLicenceUsage;

	public LicenseDataService(AbstractExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setModuleDataService(ModuleDataService moduleDataService) {
		this.moduleDataService = moduleDataService;
	}

	public Map<String, LicencepoolEntry> getLicensePoolsPD() {
		retrieveLicensePoolsPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_POOLS, Map.class);
	}

	public LicencePoolXOpsiProduct getLicencePoolXOpsiProductPD() {
		retrieveLicensePoolsPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, LicencePoolXOpsiProduct.class);
	}

	public void retrieveLicensePoolsPD() {
		if (cacheManager.getCachedData(CacheIdentifier.LICENSE_POOLS, Map.class) != null || cacheManager
				.getCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, LicencePoolXOpsiProduct.class) != null) {
			return;
		}

		LicencePoolXOpsiProduct licensePoolXOpsiProduct = new LicencePoolXOpsiProduct();
		Map<String, LicencepoolEntry> licensePools = new TreeMap<>();
		if (moduleDataService.isWithLicenceManagementPD()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_GET_OBJECTS,
					new Object[] { new Object[0], new HashMap<>() });
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
			for (Map<String, Object> importedEntry : retrieved) {
				LicencepoolEntry entry = new LicencepoolEntry(importedEntry);
				licensePools.put(entry.getLicencepoolId(), entry);
				licensePoolXOpsiProduct.integrateRawFromService(importedEntry);
			}
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_POOLS, licensePools);
		cacheManager.setCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, licensePoolXOpsiProduct);
	}

	public Map<String, LicenceContractEntry> getLicenseContractsPD() {
		retrieveLicenseContractsPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS, Map.class);
	}

	public NavigableMap<String, NavigableSet<String>> getLicenseContractsToNotifyPD() {
		retrieveLicenseContractsPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, NavigableMap.class);
	}

	public void retrieveLicenseContractsPD() {
		if (cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS, Map.class) != null || cacheManager
				.getCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, NavigableMap.class) != null) {
			return;
		}

		String today = new java.sql.Date(System.currentTimeMillis()).toString();
		Map<String, LicenceContractEntry> licenceContracts = new HashMap<>();
		NavigableMap<String, NavigableSet<String>> contractsToNotify = new TreeMap<>();
		if (moduleDataService.isWithLicenceManagementPD()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_CONTRACT_GET_OBJECTS, new Object[] {});
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceContractEntry entry = new LicenceContractEntry(importedEntry);
				licenceContracts.put(entry.getId(), entry);

				String notiDate = entry.get(TableLicenceContracts.NOTIFICATION_DATE_KEY);
				if (notiDate != null && notiDate.trim().length() > 0 && notiDate.compareTo(today) <= 0) {
					NavigableSet<String> contractSet = contractsToNotify.computeIfAbsent(notiDate,
							s -> new TreeSet<>());

					contractSet.add(entry.getId());
				}
			}
			Logging.info(this, "contractsToNotify " + contractsToNotify);
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_CONTRACTS, licenceContracts);
		cacheManager.setCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, contractsToNotify);
	}

	public Map<String, LicenceEntry> getLicensesPD() {
		retrieveLicensesPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSES, Map.class);
	}

	public void retrieveLicensesPD() {
		if (cacheManager.getCachedData(CacheIdentifier.LICENSES, Map.class) != null) {
			return;
		}
		Map<String, LicenceEntry> licences = new HashMap<>();
		if (moduleDataService.isWithLicenceManagementPD()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_GET_OBJECTS, new Object[0]);
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
			for (Map<String, Object> importedEntry : retrieved) {
				LicenceEntry entry = new LicenceEntry(importedEntry);
				licences.put(entry.getId(), entry);
			}
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSES, licences);
	}

	public List<LicenceUsableForEntry> getLicenseUsabilitiesPD() {
		retrieveSoftwareLicense2LicensePoolPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_USABILITIES, List.class);
	}

	public Map<String, Map<String, Object>> getRelationsSoftwareL2LPool() {
		retrieveSoftwareLicense2LicensePoolPD();
		return cacheManager.getCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL, Map.class);
	}

	public void retrieveSoftwareLicense2LicensePoolPD() {
		if (!moduleDataService.isWithLicenceManagementPD()
				|| cacheManager.getCachedData(CacheIdentifier.LICENSE_USABILITIES, List.class) != null
				|| cacheManager.getCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL, List.class) != null) {
			return;
		}
		Map<String, Map<String, Object>> rowsSoftwareL2LPool = new HashMap<>();
		List<LicenceUsableForEntry> licenceUsabilities = new ArrayList<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_GET_OBJECTS,
				new Object[0]);
		List<Map<String, Object>> softwareL2LPools = exec.getListOfMaps(omc);
		for (Map<String, Object> softwareL2LPool : softwareL2LPools) {
			LicenceUsableForEntry entry = LicenceUsableForEntry.produceFrom(softwareL2LPool);
			licenceUsabilities.add(entry);
			softwareL2LPool.remove("ident");
			softwareL2LPool.remove("type");
			rowsSoftwareL2LPool.put(Utils.pseudokey(new String[] { (String) softwareL2LPool.get("softwareLicenseId"),
					(String) softwareL2LPool.get("licensePoolId") }), softwareL2LPool);
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_USABILITIES, licenceUsabilities);
		cacheManager.setCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL, rowsSoftwareL2LPool);
	}

	// retrieves the used software licence - or tries to reserve one - for the given
	// host and licence pool
	public String getLicenseUsage(String hostId, String licensePoolId) {
		String result = null;
		Map<String, Object> resultMap = null;

		if (moduleDataService.isWithLicenceManagementPD()) {
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

		if (moduleDataService.isWithLicenceManagementPD()) {
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
		if (itemsDeletionLicenceUsage == null) {
			itemsDeletionLicenceUsage = new ArrayList<>();
		}
		addDeletionLicenseUsage(hostId, softwareLicenseId, licensePoolId, itemsDeletionLicenceUsage);
	}

	private void addDeletionLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId,
			List<LicenceUsageEntry> deletionItems) {
		if (deletionItems == null) {
			return;
		}

		if (!moduleDataService.isWithLicenceManagementPD()) {
			return;
		}

		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return;
		}

		LicenceUsageEntry deletionItem = new LicenceUsageEntry(hostId, softwareLicenseId, licensePoolId, "", "");
		deletionItems.add(deletionItem);
	}

	public boolean executeCollectedDeletionsLicenceUsage() {
		Logging.info(this, "executeCollectedDeletionsLicenceUsage itemsDeletionLicenceUsage == null "
				+ (itemsDeletionLicenceUsage == null));
		boolean result = false;
		if (itemsDeletionLicenceUsage == null) {
			result = true;
		} else if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			result = false;
		} else if (!moduleDataService.isWithLicenceManagementPD()) {
			result = false;
		} else {
			List<Map<String, Object>> jsonPreparedList = new ArrayList<>();
			for (LicenceUsageEntry item : itemsDeletionLicenceUsage) {
				jsonPreparedList.add(item.getNOMobject());
			}

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_DELETE_OBJECTS,
					new Object[] { jsonPreparedList });

			result = exec.doCall(omc);

			if (result) {
				Map<String, LicenceUsageEntry> rowsLicencesUsage = cacheManager
						.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
				Map<String, List<LicenceUsageEntry>> fClient2LicencesUsageList = cacheManager
						.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
				for (LicenceUsageEntry item : itemsDeletionLicenceUsage) {
					String key = item.getPseudoKey();
					String hostX = item.getClientId();

					LicenceUsageEntry rowmap = rowsLicencesUsage.get(key);
					rowsLicencesUsage.remove(key);
					fClient2LicencesUsageList.get(hostX).remove(rowmap);

					Logging.debug(this, "deleteLicenceUsage check fClient2LicencesUsageList "
							+ fClient2LicencesUsageList.get(hostX));
				}
			}

			itemsDeletionLicenceUsage.clear();
		}

		return result;
	}

	public boolean deleteLicenseUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		boolean result = false;

		if (moduleDataService.isWithLicenceManagementPD()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_DELETE,
					new String[] { softwareLicenseId, licensePoolId, hostId });
			result = exec.doCall(omc);
			if (result) {
				Map<String, LicenceUsageEntry> rowsLicencesUsage = cacheManager
						.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
				Map<String, List<LicenceUsageEntry>> fClient2LicencesUsageList = cacheManager
						.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
				String key = LicenceUsageEntry.produceKey(hostId, licensePoolId, softwareLicenseId);
				LicenceUsageEntry rowmap = rowsLicencesUsage.get(key);
				rowsLicencesUsage.remove(key);
				fClient2LicencesUsageList.get(hostId).remove(rowmap);
				Logging.info(this,
						"deleteLicenceUsage check fClient2LicencesUsageList " + fClient2LicencesUsageList.get(hostId));
			}
		}

		return result;
	}

	public List<LicenceUsageEntry> getLicenseUsagesPD() {
		retrieveLicenseUsagesPD();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_USAGE, List.class);
	}

	public void retrieveLicenseUsagesPD() {
		if (moduleDataService.isWithLicenceManagementPD()
				&& cacheManager.getCachedData(CacheIdentifier.LICENSE_USAGE, List.class) != null) {
			return;
		}
		Logging.info(this, "retrieveLicenceUsages");
		List<LicenceUsageEntry> licenceUsages = new ArrayList<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_GET_OBJECTS, new Object[0]);
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

		for (Map<String, Object> importedEntry : retrieved) {
			LicenceUsageEntry entry = new LicenceUsageEntry(importedEntry);

			licenceUsages.add(entry);
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_USAGE, licenceUsages);
	}

	public Map<String, Map<String, String>> getRelationsProductId2LPool() {
		Map<String, Map<String, String>> rowsLicencePoolXOpsiProduct = new HashMap<>();
		if (moduleDataService.isWithLicenceManagementPD()) {
			Logging.info(this, "licencePoolXOpsiProduct size " + getLicencePoolXOpsiProductPD().size());
			for (StringValuedRelationElement element : getLicencePoolXOpsiProductPD()) {
				rowsLicencePoolXOpsiProduct
						.put(Utils.pseudokey(new String[] { element.get(LicencePoolXOpsiProduct.LICENCE_POOL_KEY),
								element.get(LicencePoolXOpsiProduct.PRODUCT_ID_KEY) }), element);
			}
		}
		Logging.info(this, "rowsLicencePoolXOpsiProduct size " + rowsLicencePoolXOpsiProduct.size());
		return rowsLicencePoolXOpsiProduct;
	}

	public Map<String, LicenceUsageEntry> getRowsLicensesUsagePD() {
		retrieveLicensesUsagePD();
		return cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class);
	}

	public Map<String, List<LicenceUsageEntry>> getFClient2LicensesUsageListPD() {
		retrieveLicensesUsagePD();
		return cacheManager.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class);
	}

	private void retrieveLicensesUsagePD() {
		if (moduleDataService.isWithLicenceManagementPD()
				&& (cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, Map.class) != null && cacheManager
						.getCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, Map.class) != null)) {
			return;
		}

		Map<String, LicenceUsageEntry> rowsLicencesUsage = new HashMap<>();
		Map<String, List<LicenceUsageEntry>> fClient2LicencesUsageList = new HashMap<>();
		for (LicenceUsageEntry m : getLicenseUsagesPD()) {
			rowsLicencesUsage.put(m.getPseudoKey(), m);
			List<LicenceUsageEntry> licencesUsagesForClient = fClient2LicencesUsageList.computeIfAbsent(m.getClientId(),
					s -> new ArrayList<>());
			licencesUsagesForClient.add(m);
		}
		cacheManager.setCachedData(CacheIdentifier.ROWS_LICENSE_USAGE, rowsLicencesUsage);
		cacheManager.setCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST, fClient2LicencesUsageList);
	}

	// returns the ID of the edited data record
	public String editLicenseContract(String licenseContractId, String partner, String conclusionDate,
			String notificationDate, String expirationDate, String notes) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return "";
		}
		String result = "";

		Logging.debug(this, "editLicenceContract " + licenseContractId);

		if (moduleDataService.isWithLicenceManagementPD()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_CONTRACT_CREATE, new String[] {
					licenseContractId, "", notes, partner, conclusionDate, notificationDate, expirationDate });

			// the method gives the first letter instead of the complete string as return
			// value, therefore we set it in a shortcut:

			if (exec.doCall(omc)) {
				result = licenseContractId;
			} else {
				Logging.error(this, "could not create license " + licenseContractId);
			}
		}

		Logging.debug(this, "editLicenceContract result " + result);

		return result;
	}

	public boolean deleteLicenseContract(String licenseContractId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (moduleDataService.isWithLicenceManagementPD()) {
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

		if (moduleDataService.isWithLicenceManagementPD()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_CREATE,
					new String[] { licensePoolId, description });

			if (exec.doCall(omc)) {
				result = licensePoolId;
			} else {
				Logging.warning(this, "could not create licensepool " + licensePoolId);
			}
		}

		return result;
	}

	public boolean deleteLicensePool(String licensePoolId) {
		Logging.info(this, "deleteLicencePool " + licensePoolId);

		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (moduleDataService.isWithLicenceManagementPD()) {
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

		if (moduleDataService.isWithLicenceManagementPD()) {
			Map<String, Object> licensePool = getLicensePool(licensePoolId);

			// Replace old product list with actualized list
			List<Object> licensePoolProductIds = new ArrayList<>((List<?>) licensePool.get("productIds"));
			licensePoolProductIds.add(productId);
			licensePool.put("productIds", licensePoolProductIds);

			if (exec.doCall(
					new OpsiMethodCall(RPCMethodName.LICENSE_POOL_UPDATE_OBJECT, new Object[] { licensePool }))) {
				result = licensePoolId;
			} else {
				Logging.error(this, "could not update product " + productId + " to licensepool " + licensePoolId);
			}
		}

		return result;
	}

	public boolean deleteRelationProductId2LPool(String productId, String licensePoolId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (moduleDataService.isWithLicenceManagementPD()) {
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
