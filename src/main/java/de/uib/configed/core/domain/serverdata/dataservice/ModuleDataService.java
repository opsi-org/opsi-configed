/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.modulelicense.LicensingInfoMap;
import de.uib.configed.core.domain.modulelicense.OpsiLicensing;
import de.uib.configed.core.domain.permission.ModulePermissionValue;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.share.ExtendedDate;
import de.uib.configed.share.ExtendedInteger;
import de.uib.configed.share.logging.Logging;

/**
 * Provides methods for working with module data on the server.
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
public class ModuleDataService extends DataService {
	// opsi module information
	private static final int CLIENT_COUNT_WARNING_LIMIT = 10;
	private static final int CLIENT_COUNT_TOLERANCE_LIMIT = 50;

	private static final List<String> MODULE_CHECKED = Arrays.asList("license_management", "local_imaging",
			"monitoring", "wim-capture", "scalability1", "linux_agent", "vpn", "mysql_backend", "uefi", "userroles",
			"directory-connector", "macos_agent", "secureboot", "win-vhd", "os_install_by_wlan");

	public ModuleDataService(DataServices dataServices) {
		super(dataServices);
	}

	public final void retrieveOpsiModules() {
		Logging.info(this, "retrieveOpsiModules ");

		Map<String, Object> licensingInfoOpsiAdmin = getOpsiLicensingInfoOpsiAdminPD();

		// probably old opsi service version
		if (licensingInfoOpsiAdmin == null) {
			produceOpsiModulesInfoClassicOpsi43PD();
		} else {
			produceOpsiModulesInfoPD();
		}

		Logging.info(this, " withUserRoles ", isOpsiModuleActive(OpsiModule.USER_ROLES));
	}

	public final Map<String, Object> getOpsiLicensingInfoOpsiAdminPD() {
		retrieveOpsiLicensingInfoOpsiAdminPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN, Map.class);
	}

	public final void retrieveOpsiLicensingInfoOpsiAdminPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN)) {
			return;
		}

		if (isOpsiUserAdminPD()) {
			Map<String, Object> licencingInfoOpsiAdmin = dataServices.exec
					.getMapResult(RPCMethodName.BACKEND_GET_LICENSING_INFO, true, false, true, false);
			dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN,
					licencingInfoOpsiAdmin);
		}
	}

	public Map<String, Object> getOpsiLicensingInfoNoOpsiAdminPD() {
		Logging.info(this, "getLicensingInfoNoOpsiAdmin");
		retrieveOpsiLicensingInfoNoOpsiAdminPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_LICENSING_INFO_NO_OPSI_ADMIN, Map.class);
	}

	public void retrieveOpsiLicensingInfoNoOpsiAdminPD() {
		if (!dataServices.cacheManager.isDataCached(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN)) {
			Map<String, Object> licensingInfoNoOpsiAdmin = dataServices.exec
					.getMapResult(RPCMethodName.BACKEND_GET_LICENSING_INFO);
			dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_LICENSING_INFO_NO_OPSI_ADMIN,
					licensingInfoNoOpsiAdmin);
		}
	}

	private void produceOpsiModulesInfoPD() {
		// has the actual signal if a module is activ
		Map<String, Boolean> opsiModules = new HashMap<>();

		// opsiinformation which delivers the service information on checked modules
		// displaying to the user

		dataServices.hostInfoCollections.retrieveOpsiHostsPD();
		Map<String, List<Object>> configDefaultValues = dataServices.cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		Logging.info(this, "getOverLimitModuleList() ",
				LicensingInfoMap.getInstance(getOpsiLicensingInfoOpsiAdminPD(), configDefaultValues, true)
						.getCurrentOverLimitModuleList());

		LicensingInfoMap licInfoMap = LicensingInfoMap.getInstance(getOpsiLicensingInfoOpsiAdminPD(),
				configDefaultValues, !OpsiLicensing.isExtendedView());

		List<String> availableModules = licInfoMap.getAvailableModules();

		for (String mod : licInfoMap.getModules()) {
			opsiModules.put(mod, availableModules.indexOf(mod) != -1);
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES, opsiModules);

		Logging.info(this, "opsiModules result ", opsiModules);

		Logging.info(this, "produceOpsiModulesInfo withUserRoles ", isOpsiModuleActive(OpsiModule.USER_ROLES));
		Logging.info(this, "produceOpsiModulesInfo wan ", isOpsiModuleActive(OpsiModule.VPN));
		Logging.info(this, "produceOpsiModulesInfo withLicenseManagement ",
				isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT));
		// sets value to true if we use the mysql backend and informs that we are
		// underlicensed
	}

	public Map<String, Object> getOpsiModulesInfosPD() {
		retrieveOpsiModules();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_MODULES_DISPLAY_INFO, Map.class);
	}

	private Map<String, Object> createOpsiModulesInformation(Map<String, Boolean> opsiModules,
			Map<String, ModulePermissionValue> opsiModulesPermissions) {
		// keeps the info for displaying to the user
		Map<String, Object> opsiModulesDisplayInfo = new HashMap<>();

		Map<String, Object> opsiInformation = produceOpsiInformationPD();
		// prepare the user info
		Map<String, Object> opsiModulesInfo = POJOReMapper.remap(opsiInformation.get("modules"));
		Logging.info(this, "opsi module information ", opsiModulesInfo);

		ExtendedDate validUntil = ExtendedDate.INFINITE;

		// analyse the real module info
		Map<String, Object> opsiCountModules = POJOReMapper.remap(opsiInformation.get("modules"));

		opsiCountModules.keySet().removeAll(POJOReMapper.remap(opsiInformation.get("obsolete_modules")));
		dataServices.hostInfoCollections.retrieveOpsiHostsPD();

		Logging.info(this, "opsiModulesInfo ", opsiModulesInfo);

		// read in modules
		for (Entry<String, Object> opsiModuleInfo : opsiModulesInfo.entrySet()) {
			Logging.info(this, "module from opsiModulesInfo, key ", opsiModuleInfo);
			Map<String, Object> opsiModuleData = POJOReMapper.remap(opsiModuleInfo.getValue());
			ModulePermissionValue modulePermission = new ModulePermissionValue(opsiModuleData.get("available"),
					validUntil);

			Logging.info(this, "handle modules key, modulePermission  ", modulePermission);
			Boolean permissionCheck = modulePermission.getBoolean();
			opsiModulesPermissions.put(opsiModuleInfo.getKey(), modulePermission);
			if (permissionCheck != null) {
				opsiModules.put(opsiModuleInfo.getKey(), permissionCheck);
			}

			if (opsiModuleData.get("available") != null) {
				opsiModulesDisplayInfo.put(opsiModuleInfo.getKey(), opsiModuleData.get("available"));
			}
		}

		Logging.info(this, "modules resulting step 0  ", opsiModules);

		// existing
		for (Entry<String, Object> opsiCountModule : opsiCountModules.entrySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(opsiCountModule.getKey());
			Logging.info(this, "handle modules key ", opsiCountModule.getKey(), " permission was ", modulePermission);
			Map<String, Object> opsiModuleData = POJOReMapper.remap(opsiCountModule.getValue());

			if ("free".equals(opsiModuleData.get("state"))) {
				continue;
			}

			modulePermission = new ModulePermissionValue(opsiModuleData.get("client_number"), validUntil);

			Logging.info(this, "handle modules key ", opsiCountModule.getKey(), " permission set ", modulePermission);
			// replace value got from modulesInfo
			opsiModulesPermissions.put(opsiCountModule.getKey(), modulePermission);

			if (opsiModuleData.get("client_number") != null) {
				opsiModulesDisplayInfo.put(opsiCountModule.getKey(), opsiModuleData.get("client_number"));
			}
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES_DISPLAY_INFO, opsiModulesDisplayInfo);

		return opsiCountModules;
	}

	private void produceModulesData(Map<String, Boolean> opsiModules, Map<String, Object> opsiCountModules,
			Map<String, ModulePermissionValue> opsiModulesPermissions) {
		ExtendedInteger globalMaxClients = ExtendedInteger.INFINITE;

		Logging.info(this, "modules resulting step 1 ", opsiModules);
		Logging.info(this, "countModules is  ", opsiCountModules);

		// set values for modules checked by configed
		for (String key : MODULE_CHECKED) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);

			if (modulePermission == null) {
				continue;
			}

			ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			if (modulePermission.getBoolean() != null) {
				opsiModules.put(key, modulePermission.getBoolean());
				Logging.info(this, " retrieveOpsiModules, set opsiModules for key ", key, ": ",
						modulePermission.getBoolean());
			} else {
				opsiModules.put(key, true);
				Logging.info(this, " retrieveOpsiModules ", key, " ", maxClientsForThisModule.getNumber());

				if (maxClientsForThisModule.equals(ExtendedInteger.ZERO)) {
					opsiModules.put(key, false);
				} else {
					globalMaxClients = treatModuleLicense(globalMaxClients, opsiModules, key, maxClientsForThisModule,
							expiresForThisModule);
				}
			}
		}
	}

	private ExtendedInteger treatModuleLicense(ExtendedInteger globalMaxClients, Map<String, Boolean> opsiModules,
			String key, ExtendedInteger maxClientsForThisModule, ExtendedDate expiresForThisModule) {
		Integer warningLimit = null;
		Integer stopLimit = null;

		Logging.info(this, " retrieveOpsiModules ", key, " up to now globalMaxClients ", globalMaxClients);

		Logging.info(this, " retrieveOpsiModules ", key, " maxClientsForThisModule.getNumber ",
				maxClientsForThisModule.getNumber());

		globalMaxClients = calculateModulePermission(globalMaxClients, maxClientsForThisModule.getNumber());

		Logging.info(this, " retrieveOpsiModules ", key, " result:  globalMaxClients is ", globalMaxClients);

		Integer newGlobalLimit = globalMaxClients.getNumber();

		// global limit is changed by this module a real warning
		// and error limit exists
		if (newGlobalLimit != null) {
			warningLimit = newGlobalLimit - CLIENT_COUNT_WARNING_LIMIT;
			stopLimit = newGlobalLimit + CLIENT_COUNT_TOLERANCE_LIMIT;
		}

		Logging.info(this, " retrieveOpsiModules ", key, " old  warningLimit ", warningLimit, " stopLimit ", stopLimit);

		int allActiveClients = ((int) LicensingInfoMap.getInstance().getClientNumbersMap().get(LicensingInfoMap.ALL));
		if (stopLimit != null && allActiveClients > stopLimit) {
			opsiModules.put(key, false);
		} else if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
			LocalDateTime expiresDate = expiresForThisModule.getDate();

			if (LocalDateTime.now().isAfter(expiresDate)) {
				opsiModules.put(key, false);
			}
		} else {
			// Do nothing since nothing expired
		}

		return globalMaxClients;
	}

	private List<String> produceMissingModulesPermissionInfo(Map<String, Boolean> opsiModules,
			Map<String, ModulePermissionValue> opsiModulesPermissions) {
		final List<String> missingModulesPermissionInfo = new ArrayList<>();

		for (String key : MODULE_CHECKED) {
			int allActiveClients = ((int) LicensingInfoMap.getInstance().getClientNumbersMap()
					.get(LicensingInfoMap.ALL));

			// tests

			if (!opsiModules.containsKey(key)) {
				continue;
			}

			Logging.info(this, "check module ", key, " problem on start ", (!(opsiModules.get(key))));
			boolean problemToIndicate = true;
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxAllowedClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			Logging.info(this, "check  module ", key, " maxAllowedClientsForThisModule ",
					maxAllowedClientsForThisModule, " expiresForThisModule ", expiresForThisModule);

			if (maxAllowedClientsForThisModule.equals(ExtendedInteger.ZERO)) {
				problemToIndicate = false;
			}

			if (problemToIndicate && ("linux_agent".equals(key)
					|| ("userroles".equals(key) && !dataServices.userRoles.hasKeyUserRegisterValuePD()))) {
				problemToIndicate = false;
			}

			Logging.info(this, "check module ", key, "  problemToIndicate ", problemToIndicate);

			if (problemToIndicate) {
				Logging.info(this, "retrieveOpsiModules ", key, " , maxClients ", maxAllowedClientsForThisModule,
						" count ", allActiveClients);

				addExpiredModulePermissionInfo(key, expiresForThisModule, missingModulesPermissionInfo);
				addOverused(key, maxAllowedClientsForThisModule, allActiveClients, missingModulesPermissionInfo);
			}
		}

		return missingModulesPermissionInfo;
	}

	private static void addExpiredModulePermissionInfo(String key, ExtendedDate expiresForThisModule,
			List<String> missingModulesPermissionInfo) {
		if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
			LocalDateTime noticeDate = expiresForThisModule.getDate().minusDays(14);
			missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);

			if (LocalDateTime.now().isAfter(noticeDate)) {
				missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);
			}
		}
	}

	private void addOverused(String key, ExtendedInteger maxAllowedClientsForThisModule, int allActiveClients,
			List<String> missingModulesPermissionInfo) {
		if (!ExtendedInteger.INFINITE.equals(maxAllowedClientsForThisModule)) {
			int startWarningCount = maxAllowedClientsForThisModule.getNumber() - CLIENT_COUNT_WARNING_LIMIT;
			int stopCount = maxAllowedClientsForThisModule.getNumber() + CLIENT_COUNT_TOLERANCE_LIMIT;

			if (allActiveClients > stopCount) {
				Logging.info(this, "retrieveOpsiModules ", key, " stopCount ", stopCount, " count clients ",
						allActiveClients);

				String warningText = String.format(Configed.getResourceValue("Permission.modules.clientcount.error"),
						"" + allActiveClients, "" + key, "" + maxAllowedClientsForThisModule.getNumber());

				missingModulesPermissionInfo.add(warningText);

				Logging.warning(this, warningText);
			} else if (allActiveClients > startWarningCount) {
				Logging.info(this, "retrieveOpsiModules ", key, " startWarningCount ", startWarningCount,
						" count clients ", allActiveClients);

				String warningText = String.format(Configed.getResourceValue("Permission.modules.clientcount.warning"),
						"" + allActiveClients, "" + key, "" + maxAllowedClientsForThisModule.getNumber());

				missingModulesPermissionInfo.add(warningText);
				Logging.warning(this, warningText);
			} else {
				// Do nothing when countClientsInThisBlock <= startWarningCount
			}
		}
	}

	private void produceOpsiModulesInfoClassicOpsi43PD() {
		// has the actual signal if a module is active
		Map<String, Boolean> opsiModules = new HashMap<>();
		Map<String, ModulePermissionValue> opsiModulesPermissions = new HashMap<>();

		Map<String, Object> opsiCountModules = createOpsiModulesInformation(opsiModules, opsiModulesPermissions);

		Logging.info(this, "modules resulting step 2  ", opsiModules);
		Logging.info(this, "count Modules is  ", opsiCountModules);

		produceModulesData(opsiModules, opsiCountModules, opsiModulesPermissions);

		List<String> missingModulesPermissionInfo = produceMissingModulesPermissionInfo(opsiModules,
				opsiModulesPermissions);

		Logging.info(this, "modules resulting  ", opsiModules);
		Logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos ", missingModulesPermissionInfo);

		// Will be called only, when info empty
		callOpsiLicenseMissingModules(missingModulesPermissionInfo);

		Logging.info(this, "retrieveOpsiModules opsiCountModules ", opsiCountModules);
		Logging.info(this, "retrieveOpsiModules opsiModulesPermissions ", opsiModulesPermissions);
		Logging.info(this, "retrieveOpsiModules opsiModules ", opsiModules);
		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES, opsiModules);
	}

	private void callOpsiLicenseMissingModules(List<String> missingModulesPermissionInfo) {
		if (!missingModulesPermissionInfo.isEmpty()) {
			SwingUtilities.invokeLater(() -> {
				StringBuilder info = new StringBuilder();
				for (String moduleInfo : missingModulesPermissionInfo) {
					info.append(moduleInfo + "<br>");
				}

				Logging.info(this, "missingModules ", info);
				DialogUtils.showMissingLicenseModules(info.toString());
			});
		}
	}

	private ExtendedInteger calculateModulePermission(ExtendedInteger globalMaxClients,
			final Integer specialMaxClientNumber) {
		Logging.info(this, "calculateModulePermission globalMaxClients ", globalMaxClients, " specialMaxClientNumber ",
				specialMaxClientNumber);
		Integer maxClients = null;

		if (specialMaxClientNumber != null) {
			int compareResult = globalMaxClients.compareTo(specialMaxClientNumber);
			Logging.info(this, "calculateModulePermission compareResult ", compareResult);

			// the global max client count is reduced, a real warning and error limit exists
			if (compareResult < 0) {
				maxClients = specialMaxClientNumber;
				globalMaxClients = new ExtendedInteger(maxClients);
			} else {
				maxClients = specialMaxClientNumber;
			}
		}

		Logging.info(this, "calculateModulePermission returns ", maxClients);

		if (maxClients == null) {
			return globalMaxClients;
		} else {
			return new ExtendedInteger(maxClients);
		}
	}

	public boolean isOpsiUserAdminPD() {
		if (!dataServices.cacheManager.isDataCached(CacheIdentifier.IS_OPSI_ADMIN_USER)) {
			dataServices.cacheManager.setCachedData(CacheIdentifier.IS_OPSI_ADMIN_USER,
					dataServices.exec.getBooleanResult(RPCMethodName.ACCESS_CONTROL_USER_IS_ADMIN));
		}

		return Boolean.TRUE
				.equals(dataServices.cacheManager.getCachedData(CacheIdentifier.IS_OPSI_ADMIN_USER, Boolean.class));
	}

	private Map<String, Object> produceOpsiInformationPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.OPSI_INFORMATION)) {
			return dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_INFORMATION, Map.class);
		}

		Map<String, Object> opsiInformation = dataServices.exec.getMapResult(RPCMethodName.BACKEND_GET_LICENSING_INFO);

		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_INFORMATION, opsiInformation);
		return opsiInformation;
	}

	public boolean isOpsiModuleActive(OpsiModule opsiModule) {
		Map<String, Boolean> opsiModules = getOpsiModulesPD();
		if (opsiModules == null || opsiModules.isEmpty()) {
			return false;
		}
		return Boolean.TRUE.equals(opsiModules.get(opsiModule.toString()));
	}

	private Map<String, Boolean> getOpsiModulesPD() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_MODULES, Map.class);
	}

	public List<Map<String, Object>> getModules() {
		Logging.info(this, "getModules");
		Map<String, Object> producedLicencingInfo = retrieveProducedLicensingInfo();
		return POJOReMapper.remap(producedLicencingInfo.get("licenses"));
	}

	private Map<String, Object> retrieveProducedLicensingInfo() {
		Map<String, Object> producedLicencingInfo;
		if (isOpsiUserAdminPD() && getOpsiLicensingInfoOpsiAdminPD() != null) {
			producedLicencingInfo = getOpsiLicensingInfoOpsiAdminPD();
		} else {
			producedLicencingInfo = getOpsiLicensingInfoNoOpsiAdminPD();
		}
		return producedLicencingInfo;
	}

	public Map<String, Integer> getInstalledOsOverview() {
		Logging.info(this, "getInstalledOsOverview");
		Map<String, Object> producedLicencingInfo = retrieveProducedLicensingInfo();
		return POJOReMapper.remap(producedLicencingInfo.get("client_numbers"));
	}
}
