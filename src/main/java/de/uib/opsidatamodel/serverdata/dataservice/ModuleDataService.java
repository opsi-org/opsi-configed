/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.Configed;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoDialog;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.ModulePermissionValue;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utils.ExtendedDate;
import de.uib.utils.ExtendedInteger;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

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
public class ModuleDataService {
	// opsi module information
	private static final int CLIENT_COUNT_WARNING_LIMIT = 10;
	private static final int CLIENT_COUNT_TOLERANCE_LIMIT = 50;

	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;

	private UserRolesConfigDataService userRolesConfigDataService;
	private HostInfoCollections hostInfoCollections;

	public ModuleDataService(AbstractPOJOExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	public final void retrieveOpsiModules() {
		Logging.info(this, "retrieveOpsiModules ");

		Map<String, Object> licensingInfoOpsiAdmin = getOpsiLicensingInfoOpsiAdminPD();

		// probably old opsi service version
		if (licensingInfoOpsiAdmin == null) {
			if (ServerFacade.isOpsi43()) {
				produceOpsiModulesInfoClassicOpsi43PD();
			} else {
				produceOpsiModulesInfoClassicPD();
			}
		} else {
			produceOpsiModulesInfoPD();
		}

		Logging.info(this, " withMySQL " + isWithMySQLPD());
		Logging.info(this, " withUserRoles " + isOpsiModuleActive(OpsiModule.USER_ROLES));
	}

	public final Map<String, Object> getOpsiLicensingInfoOpsiAdminPD() {
		retrieveOpsiLicensingInfoOpsiAdminPD();
		return cacheManager.getCachedData(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN, Map.class);
	}

	public final void retrieveOpsiLicensingInfoOpsiAdminPD() {
		if (cacheManager.isDataCached(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN)) {
			return;
		}

		if (isOpsiLicensingAvailablePD() && isOpsiUserAdminPD()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.BACKEND_GET_LICENSING_INFO,
					new Object[] { true, false, true, false });
			Map<String, Object> licencingInfoOpsiAdmin = exec.retrieveResponse(omc);
			cacheManager.setCachedData(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN, licencingInfoOpsiAdmin);
		}
	}

	public Map<String, Object> getOpsiLicensingInfoNoOpsiAdminPD() {
		Logging.info(this, "getLicensingInfoNoOpsiAdmin");
		retrieveOpsiLicensingInfoNoOpsiAdminPD();
		return cacheManager.getCachedData(CacheIdentifier.OPSI_LICENSING_INFO_NO_OPSI_ADMIN, Map.class);
	}

	public void retrieveOpsiLicensingInfoNoOpsiAdminPD() {
		if (!cacheManager.isDataCached(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN)
				&& isOpsiLicensingAvailablePD()) {
			Object[] callParameters = {};
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.BACKEND_GET_LICENSING_INFO, callParameters,
					OpsiMethodCall.BACKGROUND_DEFAULT);
			Map<String, Object> licensingInfoNoOpsiAdmin = exec.getMapResult(omc);
			cacheManager.setCachedData(CacheIdentifier.OPSI_LICENSING_INFO_NO_OPSI_ADMIN, licensingInfoNoOpsiAdmin);
		}
	}

	private void produceOpsiModulesInfoPD() {
		// has the actual signal if a module is activ
		Map<String, Boolean> opsiModules = new HashMap<>();

		// opsiinformation which delivers the service information on checked modules
		// displaying to the user

		hostInfoCollections.retrieveOpsiHostsPD();
		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		Logging.info(this,
				"getOverLimitModuleList() "
						+ LicensingInfoMap.getInstance(getOpsiLicensingInfoOpsiAdminPD(), configDefaultValues, true)
								.getCurrentOverLimitModuleList());

		LicensingInfoMap licInfoMap = LicensingInfoMap.getInstance(getOpsiLicensingInfoOpsiAdminPD(),
				configDefaultValues, !LicensingInfoDialog.isExtendedView());

		List<String> availableModules = licInfoMap.getAvailableModules();

		for (String mod : licInfoMap.getModules()) {
			opsiModules.put(mod, availableModules.indexOf(mod) != -1);
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES, opsiModules);

		Logging.info(this, "opsiModules result " + opsiModules);

		cacheManager.setCachedData(CacheIdentifier.WITH_MY_SQL, canCallMySQLPD());

		Logging.info(this, "produceOpsiModulesInfo withUserRoles " + isOpsiModuleActive(OpsiModule.USER_ROLES));
		Logging.info(this, "produceOpsiModulesInfo withUEFI " + isOpsiModuleActive(OpsiModule.UEFI));
		Logging.info(this, "produceOpsiModulesInfo withWAN " + isOpsiModuleActive(OpsiModule.WAN));
		Logging.info(this,
				"produceOpsiModulesInfo withLicenseManagement " + isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT));
		Logging.info(this, "produceOpsiModulesInfo withMySQL " + isWithMySQLPD());

		// sets value to true if we use the mysql backend and informs that we are
		// underlicensed
	}

	public Map<String, Object> getOpsiModulesInfosPD() {
		retrieveOpsiModules();
		return cacheManager.getCachedData(CacheIdentifier.OPSI_MODULES_DISPLAY_INFO, Map.class);
	}

	private void produceOpsiModulesInfoClassicOpsi43PD() {
		// keeps the info for displaying to the user
		Map<String, Object> opsiModulesDisplayInfo = new HashMap<>();

		Map<String, ModulePermissionValue> opsiModulesPermissions = new HashMap<>();
		// has the actual signal if a module is active
		Map<String, Boolean> opsiModules = new HashMap<>();

		final List<String> missingModulesPermissionInfo = new ArrayList<>();

		Map<String, Object> opsiInformation = produceOpsiInformationPD();
		// prepare the user info
		Map<String, Object> opsiModulesInfo = exec.getMapFromItem(opsiInformation.get("modules"));
		Logging.info(this, "opsi module information " + opsiModulesInfo);

		ExtendedDate validUntil = ExtendedDate.INFINITE;

		// analyse the real module info
		Map<String, Object> opsiCountModules = exec.getMapFromItem(opsiInformation.get("modules"));
		opsiCountModules.keySet().removeAll(exec.getListFromItem(opsiInformation.get("obsolete_modules") + ""));
		hostInfoCollections.retrieveOpsiHostsPD();

		ExtendedInteger globalMaxClients = ExtendedInteger.INFINITE;

		int countClients = hostInfoCollections.getCountClients();

		LocalDateTime today = LocalDateTime.now();

		Logging.info(this, "opsiModulesInfo " + opsiModulesInfo);

		// read in modules
		for (Entry<String, Object> opsiModuleInfo : opsiModulesInfo.entrySet()) {
			Logging.info(this, "module from opsiModulesInfo, key " + opsiModuleInfo);
			Map<String, Object> opsiModuleData = POJOReMapper.remap(opsiModuleInfo.getValue(),
					new TypeReference<Map<String, Object>>() {
					});
			ModulePermissionValue modulePermission = new ModulePermissionValue(opsiModuleData.get("available"),
					validUntil);

			Logging.info(this, "handle modules key, modulePermission  " + modulePermission);
			Boolean permissionCheck = modulePermission.getBoolean();
			opsiModulesPermissions.put(opsiModuleInfo.getKey(), modulePermission);
			if (permissionCheck != null) {
				opsiModules.put(opsiModuleInfo.getKey(), permissionCheck);
			}

			if (opsiModuleData.get("available") != null) {
				opsiModulesDisplayInfo.put(opsiModuleInfo.getKey(), opsiModuleData.get("available"));
			}
		}

		Logging.info(this, "modules resulting step 0  " + opsiModules);

		// existing
		for (Entry<String, Object> opsiCountModule : opsiCountModules.entrySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(opsiCountModule.getKey());
			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission was " + modulePermission);
			Map<String, Object> opsiModuleData = POJOReMapper.remap(opsiCountModule.getValue(),
					new TypeReference<Map<String, Object>>() {
					});

			if ("free".equals(opsiModuleData.get("state"))) {
				continue;
			}

			modulePermission = new ModulePermissionValue(opsiModuleData.get("client_number"), validUntil);

			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission set " + modulePermission);
			// replace value got from modulesInfo
			opsiModulesPermissions.put(opsiCountModule.getKey(), modulePermission);

			if (opsiModuleData.get("client_number") != null) {
				opsiModulesDisplayInfo.put(opsiCountModule.getKey(), opsiModuleData.get("client_number"));
			}
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES_DISPLAY_INFO, opsiModulesDisplayInfo);

		Logging.info(this, "modules resulting step 1 " + opsiModules);
		Logging.info(this, "countModules is  " + opsiCountModules);

		// set values for modules checked by configed
		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);

			if (modulePermission == null) {
				continue;
			}

			ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			if (modulePermission.getBoolean() != null) {
				opsiModules.put(key, modulePermission.getBoolean());
				Logging.info(this,
						" retrieveOpsiModules, set opsiModules for key " + key + ": " + modulePermission.getBoolean());
			} else {
				opsiModules.put(key, true);
				Logging.info(this, " retrieveOpsiModules " + key + " " + maxClientsForThisModule.getNumber());

				if (maxClientsForThisModule.equals(ExtendedInteger.ZERO)) {
					opsiModules.put(key, false);
				} else {
					Integer warningLimit = null;
					Integer stopLimit = null;

					Logging.info(this,
							" retrieveOpsiModules " + key + " up to now globalMaxClients " + globalMaxClients);

					Logging.info(this, " retrieveOpsiModules " + key + " maxClientsForThisModule.getNumber "
							+ maxClientsForThisModule.getNumber());

					globalMaxClients = calculateModulePermission(globalMaxClients, maxClientsForThisModule.getNumber());

					Logging.info(this,
							" retrieveOpsiModules " + key + " result:  globalMaxClients is " + globalMaxClients);

					Integer newGlobalLimit = globalMaxClients.getNumber();

					// global limit is changed by this module a real warning
					// and error limit exists
					if (newGlobalLimit != null) {
						warningLimit = newGlobalLimit - CLIENT_COUNT_WARNING_LIMIT;
						stopLimit = newGlobalLimit + CLIENT_COUNT_TOLERANCE_LIMIT;
					}

					Logging.info(this, " retrieveOpsiModules " + key + " old  warningLimit " + warningLimit
							+ " stopLimit " + stopLimit);

					if (stopLimit != null && hostInfoCollections.getCountClients() > stopLimit) {
						opsiModules.put(key, false);
					} else {
						if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
							LocalDateTime expiresDate = expiresForThisModule.getDate();

							if (today.isAfter(expiresDate)) {
								opsiModules.put(key, false);
							}
						}
					}
				}
			}
		}

		Logging.info(this, "modules resulting step 2  " + opsiModules);
		Logging.info(this, "count Modules is  " + opsiCountModules);

		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			int countClientsInThisBlock = countClients;

			// tests

			if (!opsiModules.containsKey(key)) {
				continue;
			}

			Logging.info(this, "check module " + key + " problem on start " + (!(opsiModules.get(key))));
			boolean problemToIndicate = true;
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxAllowedClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			Logging.info(this, "check  module " + key + " maxAllowedClientsForThisModule "
					+ maxAllowedClientsForThisModule + " expiresForThisModule " + expiresForThisModule);

			if (maxAllowedClientsForThisModule.equals(ExtendedInteger.ZERO)) {
				problemToIndicate = false;
			}

			if (problemToIndicate && ("linux_agent".equals(key) || ("userroles".equals(key)
					&& Boolean.FALSE.equals(userRolesConfigDataService.hasKeyUserRegisterValuePD())))) {
				problemToIndicate = false;
			}

			Logging.info(this, "check module " + key + "  problemToIndicate " + problemToIndicate);

			if (problemToIndicate) {
				Logging.info(this, "retrieveOpsiModules " + key + " , maxClients " + maxAllowedClientsForThisModule
						+ " count " + countClientsInThisBlock);

				if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
					LocalDateTime noticeDate = expiresForThisModule.getDate().minusDays(14);

					if (today.isAfter(noticeDate)) {
						missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);
					}
				}

				if (!ExtendedInteger.INFINITE.equals(maxAllowedClientsForThisModule)) {
					int startWarningCount = maxAllowedClientsForThisModule.getNumber() - CLIENT_COUNT_WARNING_LIMIT;
					int stopCount = maxAllowedClientsForThisModule.getNumber() + CLIENT_COUNT_TOLERANCE_LIMIT;

					if (countClientsInThisBlock > stopCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " stopCount " + stopCount + " count clients "
								+ countClients);

						String warningText =

								String.format(
										// locale.
										Configed.getResourceValue("Permission.modules.clientcount.error"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);

						Logging.warning(this, warningText);
					} else if (countClientsInThisBlock > startWarningCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " startWarningCount " + startWarningCount
								+ " count clients " + countClients);

						String warningText =

								String.format(
										// locale,
										Configed.getResourceValue("Permission.modules.clientcount.warning"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);
						Logging.warning(this, warningText);
					} else {
						// Do nothing when countClientsInThisBlock <= startWarningCount
					}
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES, opsiModules);

		Logging.info(this, "modules resulting  " + opsiModules);
		Logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos " + missingModulesPermissionInfo);

		// Will be called only, when info empty
		callOpsiLicenseMissingModules(missingModulesPermissionInfo);

		cacheManager.setCachedData(CacheIdentifier.WITH_MY_SQL, canCallMySQLPD());

		Logging.info(this, "retrieveOpsiModules opsiCountModules " + opsiCountModules);
		Logging.info(this, "retrieveOpsiModules opsiModulesPermissions " + opsiModulesPermissions);
		Logging.info(this, "retrieveOpsiModules opsiModules " + opsiModules);
	}

	private void callOpsiLicenseMissingModules(List<String> missingModulesPermissionInfo) {
		if (!missingModulesPermissionInfo.isEmpty()) {
			SwingUtilities.invokeLater(() -> {
				StringBuilder info = new StringBuilder(":\n");
				for (String moduleInfo : missingModulesPermissionInfo) {
					info.append(moduleInfo + "\n");
				}

				Logging.info(this, "missingModules " + info);
				FOpsiLicenseMissingText.callInstanceWith(info.toString());
			});
		}
	}

	private void produceOpsiModulesInfoClassicPD() {
		Map<String, ModulePermissionValue> opsiModulesPermissions = new HashMap<>();
		// has the actual signal if a module is active
		Map<String, Boolean> opsiModules = new HashMap<>();

		Map<String, Object> opsiInformation = produceOpsiInformationPD();
		String opsiVersion = (String) opsiInformation.get("opsiVersion");
		Logging.info(this, "opsi version information " + opsiVersion);

		final List<String> missingModulesPermissionInfo = new ArrayList<>();

		// prepare the user info
		Map<String, Object> opsiModulesInfo = exec.getMapFromItem(opsiInformation.get("modules"));
		opsiModulesInfo.remove("signature");
		opsiModulesInfo.remove("valid");

		// keeps the info for displaying to the user
		Map<String, Object> opsiModulesDisplayInfo = new HashMap<>(opsiModulesInfo);

		ExtendedDate validUntil = ExtendedDate.INFINITE;

		// analyse the real module info
		Map<String, Object> opsiCountModules = exec.getMapFromItem(opsiInformation.get("realmodules"));
		hostInfoCollections.retrieveOpsiHostsPD();

		ExtendedInteger globalMaxClients = ExtendedInteger.INFINITE;

		int countClients = hostInfoCollections.getCountClients();

		LocalDateTime today = LocalDateTime.now();

		Logging.info(this, "opsiModulesInfo " + opsiModulesInfo);

		// read in modules
		for (Entry<String, Object> opsiModuleInfo : opsiModulesInfo.entrySet()) {
			Logging.info(this, "module from opsiModulesInfo, key " + opsiModuleInfo.getKey());
			ModulePermissionValue modulePermission = new ModulePermissionValue(opsiModuleInfo.getValue(), validUntil);

			Logging.info(this, "handle modules key, modulePermission  " + modulePermission);
			Boolean permissionCheck = modulePermission.getBoolean();
			opsiModulesPermissions.put(opsiModuleInfo.getKey(), modulePermission);
			if (permissionCheck != null) {
				opsiModules.put(opsiModuleInfo.getKey(), permissionCheck);
			}
		}

		Logging.info(this, "modules resulting step 0  " + opsiModules);

		// existing
		for (Entry<String, Object> opsiCountModule : opsiCountModules.entrySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(opsiCountModule.getKey());
			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission was " + modulePermission);

			modulePermission = new ModulePermissionValue(opsiCountModule.getValue(), validUntil);

			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission set " + modulePermission);
			// replace value got from modulesInfo
			opsiModulesPermissions.put(opsiCountModule.getKey(), modulePermission);

			if (opsiCountModule.getValue() != null) {
				opsiModulesDisplayInfo.put(opsiCountModule.getKey(), opsiCountModule.getValue());
			}
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES_DISPLAY_INFO, opsiModulesDisplayInfo);

		Logging.info(this, "modules resulting step 1 " + opsiModules);
		Logging.info(this, "countModules is  " + opsiCountModules);

		// set values for modules checked by configed
		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			if (modulePermission.getBoolean() != null) {
				opsiModules.put(key, modulePermission.getBoolean());
				Logging.info(this,
						" retrieveOpsiModules, set opsiModules for key " + key + ": " + modulePermission.getBoolean());
			} else {
				opsiModules.put(key, true);
				Logging.info(this, " retrieveOpsiModules " + key + " " + maxClientsForThisModule.getNumber());

				if (maxClientsForThisModule.equals(ExtendedInteger.ZERO)) {
					opsiModules.put(key, false);
				} else {
					Integer warningLimit = null;
					Integer stopLimit = null;

					Logging.info(this,
							" retrieveOpsiModules " + key + " up to now globalMaxClients " + globalMaxClients);

					Logging.info(this, " retrieveOpsiModules " + key + " maxClientsForThisModule.getNumber "
							+ maxClientsForThisModule.getNumber());

					globalMaxClients = calculateModulePermission(globalMaxClients, maxClientsForThisModule.getNumber());

					Logging.info(this,
							" retrieveOpsiModules " + key + " result:  globalMaxClients is " + globalMaxClients);

					Integer newGlobalLimit = globalMaxClients.getNumber();

					// global limit is changed by this module a real warning
					// and error limit exists
					if (newGlobalLimit != null) {
						warningLimit = newGlobalLimit - CLIENT_COUNT_WARNING_LIMIT;
						stopLimit = newGlobalLimit + CLIENT_COUNT_TOLERANCE_LIMIT;
					}

					Logging.info(this, " retrieveOpsiModules " + key + " old  warningLimit " + warningLimit
							+ " stopLimit " + stopLimit);

					if (stopLimit != null && hostInfoCollections.getCountClients() > stopLimit) {
						opsiModules.put(key, false);
					} else {
						if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
							LocalDateTime expiresDate = expiresForThisModule.getDate();

							if (today.isAfter(expiresDate)) {
								opsiModules.put(key, false);
							}
						}
					}
				}
			}
		}

		Logging.info(this, "modules resulting step 2  " + opsiModules);
		Logging.info(this, "count Modules is  " + opsiCountModules);

		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			int countClientsInThisBlock = countClients;

			// tests

			Logging.info(this, "check module " + key + " problem on start " + (!(opsiModules.get(key))));
			boolean problemToIndicate = true;
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxAllowedClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			Logging.info(this, "check  module " + key + " maxAllowedClientsForThisModule "
					+ maxAllowedClientsForThisModule + " expiresForThisModule " + expiresForThisModule);

			if (maxAllowedClientsForThisModule.equals(ExtendedInteger.ZERO)) {
				problemToIndicate = false;
			}

			if (problemToIndicate && ("linux_agent".equals(key) || ("userroles".equals(key)
					&& Boolean.FALSE.equals(userRolesConfigDataService.hasKeyUserRegisterValuePD())))) {
				problemToIndicate = false;
			}

			Logging.info(this, "check module " + key + "  problemToIndicate " + problemToIndicate);

			if (problemToIndicate) {
				Logging.info(this, "retrieveOpsiModules " + key + " , maxClients " + maxAllowedClientsForThisModule
						+ " count " + countClientsInThisBlock);

				if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
					LocalDateTime noticeDate = expiresForThisModule.getDate().minusDays(14);

					if (today.isAfter(noticeDate)) {
						missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);
					}
				}

				if (!ExtendedInteger.INFINITE.equals(maxAllowedClientsForThisModule)) {
					int startWarningCount = maxAllowedClientsForThisModule.getNumber() - CLIENT_COUNT_WARNING_LIMIT;
					int stopCount = maxAllowedClientsForThisModule.getNumber() + CLIENT_COUNT_TOLERANCE_LIMIT;

					if (countClientsInThisBlock > stopCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " stopCount " + stopCount + " count clients "
								+ countClients);

						String warningText =

								String.format(Configed.getResourceValue("Permission.modules.clientcount.error"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);

						Logging.warning(this, warningText);
					} else if (countClientsInThisBlock > startWarningCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " startWarningCount " + startWarningCount
								+ " count clients " + countClients);

						String warningText =

								String.format(Configed.getResourceValue("Permission.modules.clientcount.warning"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);
						Logging.warning(this, warningText);
					} else {
						// countClientsInThisBlock small enough, so nothing to do
					}
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES, opsiModules);

		Logging.info(this, "modules resulting  " + opsiModules);
		Logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos " + missingModulesPermissionInfo);

		// Will be called only when info empty
		callOpsiLicenseMissingModules(missingModulesPermissionInfo);

		cacheManager.setCachedData(CacheIdentifier.WITH_MY_SQL, canCallMySQLPD());

		Logging.info(this, "retrieveOpsiModules opsiCountModules " + opsiCountModules);
		Logging.info(this, "retrieveOpsiModules opsiModulesPermissions " + opsiModulesPermissions);
		Logging.info(this, "retrieveOpsiModules opsiModules " + opsiModules);
	}

	private ExtendedInteger calculateModulePermission(ExtendedInteger globalMaxClients,
			final Integer specialMaxClientNumber) {
		Logging.info(this, "calculateModulePermission globalMaxClients " + globalMaxClients + " specialMaxClientNumber "
				+ specialMaxClientNumber);
		Integer maxClients = null;

		if (specialMaxClientNumber != null) {
			int compareResult = globalMaxClients.compareTo(specialMaxClientNumber);
			Logging.info(this, "calculateModulePermission compareResult " + compareResult);

			// the global max client count is reduced, a real warning and error limit exists
			if (compareResult < 0) {
				maxClients = specialMaxClientNumber;
				globalMaxClients = new ExtendedInteger(maxClients);
			} else {
				maxClients = specialMaxClientNumber;
			}
		}

		Logging.info(this, "calculateModulePermission returns " + maxClients);

		if (maxClients == null) {
			return globalMaxClients;
		} else {
			return new ExtendedInteger(maxClients);
		}
	}

	public boolean isOpsiUserAdminPD() {
		boolean hasIsOpisUserAdminBeenChecked = Utils.toBoolean(
				cacheManager.getCachedData(CacheIdentifier.HAS_IS_OPSI_USER_ADMIN_BEEN_CHECKED, Boolean.class));
		if (!hasIsOpisUserAdminBeenChecked) {
			retrieveIsOpsiUserAdminPD();
		}
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.IS_OPSI_ADMIN_USER, Boolean.class));
	}

	private void retrieveIsOpsiUserAdminPD() {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.ACCESS_CONTROL_USER_IS_ADMIN, new Object[] {});
		Map<String, Object> json = exec.retrieveResponse(omc);

		Boolean isOpsiUserAdmin = null;
		if (json.containsKey("result") && json.get("result") != null) {
			isOpsiUserAdmin = (Boolean) json.get("result");
		} else {
			Logging.warning(this, "cannot check if user is admin, fallback to false...");

			isOpsiUserAdmin = false;
		}
		cacheManager.setCachedData(CacheIdentifier.IS_OPSI_ADMIN_USER, isOpsiUserAdmin);
		cacheManager.setCachedData(CacheIdentifier.HAS_IS_OPSI_USER_ADMIN_BEEN_CHECKED, true);
	}

	public boolean isOpsiLicensingAvailablePD() {
		retrieveOpsiLicensingInfoVersion();
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.IS_OPSI_LICENSING_AVAILABLE, Boolean.class));
	}

	private void retrieveOpsiLicensingInfoVersion() {
		boolean hasOpsiLicencingBeenChecked = Utils
				.toBoolean(cacheManager.getCachedData(CacheIdentifier.HAS_OPSI_LICENSING_BEEN_CHECKED, Boolean.class));
		if (!hasOpsiLicencingBeenChecked) {
			Logging.info(this, "retrieveOpsiLicensingInfoVersion getMethodSignature( backend_getLicensingInfo "
					+ getMethodSignaturePD(RPCMethodName.BACKEND_GET_LICENSING_INFO));

			boolean isOpsiLicencingAvailable;
			if (getMethodSignaturePD(RPCMethodName.BACKEND_GET_LICENSING_INFO) == null) {
				Logging.info(this,
						"method " + RPCMethodName.BACKEND_GET_LICENSING_INFO + " not existing in this opsi service");
				isOpsiLicencingAvailable = false;
			} else {
				isOpsiLicencingAvailable = true;
			}

			hasOpsiLicencingBeenChecked = true;
			cacheManager.setCachedData(CacheIdentifier.IS_OPSI_LICENSING_AVAILABLE, isOpsiLicencingAvailable);
			cacheManager.setCachedData(CacheIdentifier.HAS_OPSI_LICENSING_BEEN_CHECKED, hasOpsiLicencingBeenChecked);
		}
	}

	public List<String> getMethodSignaturePD(RPCMethodName methodname) {
		retrieveMethodSignaturesPD();
		Map<String, List<String>> mapOfMethodSignatures = cacheManager
				.getCachedData(CacheIdentifier.MAP_OF_METHOD_SIGNATURES, Map.class);
		Logging.debug(this, "mapOfMethodSignatures " + mapOfMethodSignatures);

		return mapOfMethodSignatures.get(methodname.toString());
	}

	public void retrieveMethodSignaturesPD() {
		if (cacheManager.isDataCached(CacheIdentifier.MAP_OF_METHOD_SIGNATURES)) {
			return;
		}

		Map<String, List<String>> mapOfMethodSignatures = new HashMap<>();
		List<Object> methodsList = exec
				.getListResult(new OpsiMethodCall(RPCMethodName.BACKEND_GET_INTERFACE, new Object[] {}));
		Iterator<Object> iter = methodsList.iterator();
		while (iter.hasNext()) {
			Map<String, Object> listEntry = exec.getMapFromItem(iter.next());

			String name = (String) listEntry.get("name");
			List<String> signature = new ArrayList<>();

			// should never result
			List<Object> signature1 = exec.getListFromItem(listEntry.get("params").toString());

			// to null
			for (Object elementObject : signature1) {
				String element = (String) elementObject;

				if (element != null && element.length() > 0 && element.charAt(0) == '*') {
					signature.add(element.substring(1));
				} else {
					signature.add(element);
				}

				Logging.debug(this, "mapOfMethodSignatures :: " + name + ": " + signature);
			}
			mapOfMethodSignatures.put(name, signature);
		}
		cacheManager.setCachedData(CacheIdentifier.MAP_OF_METHOD_SIGNATURES, mapOfMethodSignatures);
	}

	private Map<String, Object> produceOpsiInformationPD() {
		Map<String, Object> opsiInformation = cacheManager.getCachedData(CacheIdentifier.OPSI_INFORMATION, Map.class);
		if (opsiInformation != null && !opsiInformation.isEmpty()) {
			return opsiInformation;
		}

		RPCMethodName methodName = RPCMethodName.BACKEND_INFO;

		if (ServerFacade.isOpsi43()) {
			methodName = RPCMethodName.BACKEND_GET_LICENSING_INFO;
		}

		OpsiMethodCall omc = new OpsiMethodCall(methodName, new String[] {});
		opsiInformation = new HashMap<>();

		// method does not exist before opsi 3.4
		if (getMethodSignaturePD(methodName) != null) {
			opsiInformation = exec.getMapResult(omc);
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_INFORMATION, opsiInformation);
		return opsiInformation;
	}

	public boolean canCallMySQLPD() {
		Boolean acceptMySQL = cacheManager.getCachedData(CacheIdentifier.ACCEPT_MY_SQL, Boolean.class);
		if (acceptMySQL == null) {
			if (ServerFacade.isOpsi43()) {
				acceptMySQL = false;
			} else {
				// test if we can access any table
				String query = "select  *  from " + SWAuditClientEntry.DB_TABLE_NAME + " LIMIT 1 ";
				Logging.info(this, "test, query " + query);
				acceptMySQL = exec.doCall(new OpsiMethodCall(RPCMethodName.GET_RAW_DATA, new Object[] { query }));
				Logging.info(this, "test result " + acceptMySQL);
			}
			cacheManager.setCachedData(CacheIdentifier.ACCEPT_MY_SQL, acceptMySQL);
		}

		// we cannot call MySQL if version before 4.3
		return acceptMySQL;
	}

	public boolean isOpsiModuleActive(OpsiModule opsiModule) {
		retrieveOpsiModules();
		Map<String, Boolean> opsiModules = getOpsiModulesPD();
		return opsiModules.get(opsiModule.toString()) != null && opsiModules.get(opsiModule.toString());
	}

	private Map<String, Boolean> getOpsiModulesPD() {
		retrieveOpsiModules();
		return cacheManager.getCachedData(CacheIdentifier.OPSI_MODULES, Map.class);
	}

	public boolean isWithMySQLPD() {
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.WITH_MY_SQL, Boolean.class));
	}

	public List<Map<String, Object>> getModules() {
		Logging.info(this, "getModules");
		Map<String, Object> producedLicencingInfo = retrieveProducedLicensingInfo();
		return POJOReMapper.remap(producedLicencingInfo.get("licenses"),
				new TypeReference<List<Map<String, Object>>>() {
				});
	}

	private Map<String, Object> retrieveProducedLicensingInfo() {
		Map<String, Object> producedLicencingInfo;
		if (isOpsiUserAdminPD() && getOpsiLicensingInfoOpsiAdminPD() != null) {
			producedLicencingInfo = POJOReMapper.remap(getOpsiLicensingInfoOpsiAdminPD().get("result"),
					new TypeReference<Map<String, Object>>() {
					});
		} else {
			producedLicencingInfo = getOpsiLicensingInfoNoOpsiAdminPD();
		}
		return producedLicencingInfo;
	}

	public Map<String, Integer> getInstalledOsOverview() {
		Logging.info(this, "getInstalledOsOverview");
		Map<String, Object> producedLicencingInfo = retrieveProducedLicensingInfo();
		return POJOReMapper.remap(producedLicencingInfo.get("client_numbers"),
				new TypeReference<Map<String, Integer>>() {
				});
	}
}
