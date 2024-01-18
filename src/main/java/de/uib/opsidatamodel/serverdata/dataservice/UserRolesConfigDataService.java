/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SavedSearch;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.opsidatamodel.permission.UserOpsipermission;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.logging.Logging;
import utils.Utils;

/**
 * Provides methods for working with user roles configuration data on the
 * server.
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
public class UserRolesConfigDataService {
	private static final String DESCRIPTION_KEY = "description";
	private static final String EDITABLE_KEY = "editable";

	private static final String OPSI_CLIENTD_EVENT_SILENT_INSTALL = "silent_install";
	private static final Boolean DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN = false;

	private static final String CONFIGED_WORKBENCH_KEY = "configed.workbench.default";

	private CacheManager cacheManager;
	private AbstractExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private ConfigDataService configDataService;
	private GroupDataService groupDataService;
	private ModuleDataService moduleDataService;
	private HostInfoCollections hostInfoCollections;

	public UserRolesConfigDataService(AbstractExecutioner exec,
			OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setGroupDataService(GroupDataService groupDataService) {
		this.groupDataService = groupDataService;
	}

	public void setModuleDataService(ModuleDataService moduleDataService) {
		this.moduleDataService = moduleDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public boolean isGlobalReadOnly() {
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.GLOBAL_READ_ONLY, Boolean.class));
	}

	public boolean hasServerFullPermissionPD() {
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.SERVER_FULL_PERMISION, Boolean.class));
	}

	public boolean hasCreateClientPermissionPD() {
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION, Boolean.class));
	}

	public boolean hasDepotsFullPermissionPD() {
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION, Boolean.class));
	}

	public boolean hasKeyUserRegisterValuePD() {
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, Boolean.class));
	}

	public Set<String> getPermittedProductsPD() {
		return cacheManager.getCachedData(CacheIdentifier.PERMITTED_PRODUCTS, Set.class);
	}

	public Set<String> getPermittedProductGroupsPD() {
		return cacheManager.getCachedData(CacheIdentifier.PERMITTED_PRODUCT_GROUPS, Set.class);
	}

	public boolean hasProductGroupsFullPermissionPD() {
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_GROUPS_FULL_PERMISSION, Boolean.class);
	}

	public Set<String> getHostGroupsPermitted() {
		Set<String> result = null;
		if (!isAccessToHostgroupsOnlyIfExplicitlyStatedPD()) {
			result = cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS_PERMITTED, Set.class);
		}

		Logging.info(this, "getHostgroupsPermitted " + result);

		return result;
	}

	public boolean isAccessToHostgroupsOnlyIfExplicitlyStatedPD() {
		return Utils.toBoolean(
				cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED, Boolean.class));
	}

	public final void checkConfigurationPD() {
		persistenceController.getGroupDataService().retrieveAllObject2GroupsPD();
		moduleDataService.retrieveOpsiModules();
		Logging.info(this,
				"checkConfiguration, modules " + cacheManager.getCachedData(CacheIdentifier.OPSI_MODULES, Map.class));

		Map<String, List<Object>> serverPropertyMap = configDataService.getConfigDefaultValuesPD();

		cacheManager.setCachedData(CacheIdentifier.GLOBAL_READ_ONLY, checkReadOnlyBySystemuser());
		cacheManager.setCachedData(CacheIdentifier.SERVER_FULL_PERMISION, !isGlobalReadOnly());
		cacheManager.setCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION, true);
		cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED, false);
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_GROUPS_FULL_PERMISSION, true);
		cacheManager.setCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION, true);
		cacheManager.setCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, isUserRegisterActivated());

		boolean keyUserRegisterValue = cacheManager.getCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE,
				Boolean.class);
		boolean correctedUserRegisterVal = setAgainUserRegistration(keyUserRegisterValue);

		boolean setUserRegisterVal = !keyUserRegisterValue && correctedUserRegisterVal;

		if (setUserRegisterVal) {
			keyUserRegisterValue = true;
		}

		if (Boolean.TRUE.equals(keyUserRegisterValue)) {
			cacheManager.setCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, keyUserRegisterValue);
			keyUserRegisterValue = checkUserRolesModulePD();
		}

		if (serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER) == null
				|| setUserRegisterVal) {
			List<Object> readyObjects = new ArrayList<>();
			Map<String, Object> item = Utils.createNOMBoolConfig(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER,
					keyUserRegisterValue, "without given values the primary value setting is false");
			readyObjects.add(item);

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

			exec.doCall(omc);
		}

		applyUserSpecializedConfigPD();

		// Load all data together to prevent an extra RPC-call
		groupDataService.retrieveAllGroupsPD();

		List<Object> readyConfigObjects = new UserConfigProducing(applyUserSpecializedConfigPD(),
				hostInfoCollections.getConfigServer(), hostInfoCollections.getDepotNamesList(),
				groupDataService.getHostGroupIds(), groupDataService.getProductGroupsPD().keySet(),
				configDataService.getConfigDefaultValuesPD(), configDataService.getConfigListCellOptionsPD()).produce();

		if (readyConfigObjects == null) {
			Logging.warning(this, "readyObjects for userparts " + null);
		} else {
			if (!readyConfigObjects.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS,
						new Object[] { readyConfigObjects });
				exec.doCall(omc);
			}

			Logging.info(this, "readyObjects for userparts " + readyConfigObjects.size());
		}

		checkPermissions();

		if (hasServerFullPermissionPD()) {
			checkStandardConfigs();
		}
	}

	private boolean isUserRegisterActivated() {
		boolean result = false;
		Map<String, List<Object>> serverPropertyMap = configDataService.getConfigDefaultValuesPD();
		// dont do anything if we have not got the config
		if (serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER) != null
				&& !serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER).isEmpty()) {
			result = (Boolean) ((List<?>) serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER))
					.get(0);
		}
		return result;
	}

	private final boolean checkUserRolesModulePD() {
		boolean keyUserRegisterValue = cacheManager.getCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE,
				Boolean.class);
		if (Boolean.TRUE.equals(keyUserRegisterValue) && !moduleDataService.isWithUserRolesPD()) {
			keyUserRegisterValue = false;
			cacheManager.setCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, keyUserRegisterValue);
			SwingUtilities.invokeLater(this::callOpsiLicenceMissingText);
		}

		return keyUserRegisterValue;
	}

	private void callOpsiLicenceMissingText() {
		StringBuilder info = new StringBuilder();
		info.append(Configed.getResourceValue("Permission.modules.missing_user_roles") + "\n");
		info.append(Configed.getResourceValue("Permission.modules.missing_user_roles.1") + "\n");
		info.append(Configed.getResourceValue("Permission.modules.missing_user_roles.2") + "\n");
		info.append(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER + " "
				+ Configed.getResourceValue("Permission.modules.missing_user_roles.3"));
		info.append("\n");

		Logging.warning(this, " user role administration configured but not permitted by the modules file " + info);

		FOpsiLicenseMissingText.callInstanceWith(info.toString());
	}

	private boolean checkReadOnlyBySystemuser() {
		boolean result = false;

		Logging.info(this, "checkReadOnly");
		if (exec.getBooleanResult(
				new OpsiMethodCall(RPCMethodName.ACCESS_CONTROL_USER_IS_READ_ONLY_USER, new String[] {}))) {
			result = true;
		}

		cacheManager.setCachedData(CacheIdentifier.GLOBAL_READ_ONLY, result);
		Logging.info(this, "checkReadOnly " + isGlobalReadOnly());

		return result;
	}

	// final in order to avoid deactiviating by override
	private final boolean setAgainUserRegistration(final boolean userRegisterValueFromConfigs) {
		boolean withUserRoles = moduleDataService.isWithUserRolesPD();
		Logging.info(this, "setAgainUserRegistration, userRoles can be used " + withUserRoles);

		boolean resultVal = userRegisterValueFromConfigs;

		if (!withUserRoles) {
			return resultVal;
		}

		Boolean locallySavedValueUserRegister = null;
		if (Configed.getSavedStates() == null) {
			Logging.trace(this, "savedStates.saveRegisterUser not initialized");
		} else {
			locallySavedValueUserRegister = Boolean.parseBoolean(
					Configed.getSavedStates().getProperty(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER));
			Logging.info(this, "setAgainUserRegistration, userRegister was activated " + locallySavedValueUserRegister);
			if (userRegisterValueFromConfigs) {
				if (locallySavedValueUserRegister == null || !locallySavedValueUserRegister) {
					// we save true
					Configed.getSavedStates().setProperty(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER,
							"true");
				}
			} else if (Boolean.TRUE.equals(locallySavedValueUserRegister)) {
				// if true was locally saved but is not the value from service then we ask
				Logging.warning(this, "setAgainUserRegistration, it seems that user check has been deactivated");

				FTextArea dialog = new FTextArea(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("RegisterUserWarning.dialog.title"), true,
						new String[] { Configed.getResourceValue("buttonClose"),
								Configed.getResourceValue("RegisterUserWarning.dialog.button.dontWarnAgain"),
								Configed.getResourceValue("RegisterUserWarning.dialog.button.reactivateUserRoles") },
						600, 200);
				StringBuilder msg = new StringBuilder(Configed.getResourceValue("RegisterUserWarning.dialog.info1"));
				msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.info2"));

				dialog.setMessage(msg.toString());
				dialog.setVisible(true);
				int result = dialog.getResult();
				Logging.info(this, "setAgainUserRegistration, reaction via option " + dialog.getResult());

				switch (result) {
				case 1:
					Logging.info(this, "setAgainUserRegistration ignore ");
					break;

				case 2:
					Logging.info(this, "setAgainUserRegistration remove warning locally ");
					// remove from store
					Configed.getSavedStates().remove(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER);
					break;

				case 3:
					Logging.info(this, "setAgainUserRegistration reactivate user check ");
					resultVal = true;
					break;

				default:
					Logging.warning(this, "no case found for result in setAgainUserRegistration");
					break;
				}
			} else {
				// Nothing to do here
			}
		}

		return resultVal;
	}

	private void checkPermissions() {
		UserOpsipermission.ActionPrivilege serverActionPermission;

		Map<String, List<Object>> serverPropertyMap = configDataService.getConfigDefaultValuesPD();

		// variable for simplifying the use of the map
		String configKey = null;
		boolean globalReadOnly = isGlobalReadOnly();

		// already specified via systemuser group
		if (!globalReadOnly) {
			// lookup if we have a config for it and set it though not set by group
			configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY;
			Logging.info(this, "checkPermissions  configKey " + configKey);
			globalReadOnly = serverPropertyMap.get(configKey) != null
					&& (Boolean) serverPropertyMap.get(configKey).get(0);
		}

		Logging.info(this, " checkPermissions globalReadOnly " + globalReadOnly);

		if (globalReadOnly) {
			serverActionPermission = UserOpsipermission.ActionPrivilege.READ_ONLY;
		} else {
			// is default!!
			boolean mayWriteOnOpsiserver = true;

			configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_SERVER_READWRITE;
			Logging.info(this, "checkPermissions  configKey " + configKey);

			if (serverPropertyMap.get(configKey) != null) {
				Logging.info(this, " checkPermissions  value  " + (serverPropertyMap.get(configKey).get(0)));
				mayWriteOnOpsiserver = (Boolean) ((serverPropertyMap.get(configKey)).get(0));
			}

			Logging.info(this, " checkPermissions mayWriteOnOpsiserver " + mayWriteOnOpsiserver);
			if (mayWriteOnOpsiserver) {
				serverActionPermission = UserOpsipermission.ActionPrivilege.READ_WRITE;
			} else {
				serverActionPermission = UserOpsipermission.ActionPrivilege.READ_ONLY;
			}
		}

		boolean serverFullPermission = serverActionPermission == UserOpsipermission.ActionPrivilege.READ_WRITE;
		cacheManager.setCachedData(CacheIdentifier.SERVER_FULL_PERMISION, serverFullPermission);

		configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_CREATECLIENT;
		Logging.info(this, " checkPermissions key " + configKey);
		boolean withUserRoles = moduleDataService.isWithUserRolesPD();
		if (serverPropertyMap.get(configKey) != null && withUserRoles) {
			Logging.info(this, " checkPermissions  value  " + (serverPropertyMap.get(configKey).get(0)));
			boolean createClientPermission = (Boolean) ((serverPropertyMap.get(configKey)).get(0));
			cacheManager.setCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION, createClientPermission);
		}

		String configKeyUseList = null;
		String configKeyList = null;

		configKeyUseList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;
		Set<String> depotsPermitted = new HashSet<>();

		boolean depotsFullPermission = checkFullPermission(depotsPermitted, configKeyUseList, configKeyList,
				serverPropertyMap);
		cacheManager.setCachedData(CacheIdentifier.DEPOTS_PERMITTED, depotsPermitted);
		cacheManager.setCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION, depotsFullPermission);
		Logging.info(this,
				"checkPermissions depotsFullPermission (false means, depots must be specified " + depotsFullPermission);
		Logging.info(this, "checkPermissions depotsPermitted " + depotsPermitted);

		configKeyUseList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE;
		Set<String> hostgroupsPermitted = new HashSet<>();

		boolean hostgroupsOnlyIfExplicitlyStated = checkFullPermission(hostgroupsPermitted, configKeyUseList,
				configKeyList, serverPropertyMap);

		if (hostgroupsOnlyIfExplicitlyStated) {
			hostgroupsPermitted = null;
		}
		cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS_PERMITTED, hostgroupsPermitted);
		cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED,
				hostgroupsOnlyIfExplicitlyStated);

		Logging.info(this, "checkPermissions hostgroupsPermitted " + hostgroupsPermitted);

		configKeyUseList = userPartPD()
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE;
		Set<String> productGroupsPermitted = new HashSet<>();

		boolean productgroupsFullPermission = checkFullPermission(productGroupsPermitted, configKeyUseList,
				configKeyList, serverPropertyMap);
		cacheManager.setCachedData(CacheIdentifier.PERMITTED_PRODUCT_GROUPS, productGroupsPermitted);
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_GROUPS_FULL_PERMISSION, productgroupsFullPermission);

		Set<String> permittedProducts = null;

		if (!productgroupsFullPermission) {
			permittedProducts = new TreeSet<>();

			for (String group : productGroupsPermitted) {
				Map<String, Set<String>> fProductGroup2Members = cacheManager
						.getCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS, Map.class);
				Set<String> products = fProductGroup2Members.get(group);
				if (products != null) {
					permittedProducts.addAll(products);
				}
			}
			cacheManager.setCachedData(CacheIdentifier.PERMITTED_PRODUCTS, permittedProducts);
		}

		Logging.info(this, "checkPermissions permittedProducts " + permittedProducts);
	}

	private String userPartPD() {
		String userConfigPart = cacheManager.getCachedData(CacheIdentifier.USER_CONFIG_PART, String.class);
		if (userConfigPart != null) {
			return userConfigPart;
		}

		if (applyUserSpecializedConfigPD()) {
			userConfigPart = OpsiServiceNOMPersistenceController.KEY_USER_ROOT + ".{" + persistenceController.getUser()
					+ "}.";
		} else {
			userConfigPart = UserConfig.KEY_USER_ROLE_ROOT + ".{" + UserConfig.DEFAULT_ROLE_NAME + "}.";
		}

		cacheManager.setCachedData(CacheIdentifier.USER_CONFIG_PART, userConfigPart);
		Logging.info(this, "userConfigPart initialized, " + userConfigPart);

		return userConfigPart;
	}

	private boolean applyUserSpecializedConfigPD() {
		Boolean applyUserSpecializedConfig = cacheManager.getCachedData(CacheIdentifier.APPLY_USER_SPECIALIZED_CONFIG,
				Boolean.class);
		if (applyUserSpecializedConfig != null) {
			return applyUserSpecializedConfig;
		}

		applyUserSpecializedConfig = moduleDataService.isWithUserRolesPD() && hasKeyUserRegisterValuePD();
		cacheManager.setCachedData(CacheIdentifier.APPLY_USER_SPECIALIZED_CONFIG, applyUserSpecializedConfig);
		Logging.info(this, "applyUserSpecializedConfig initialized, " + applyUserSpecializedConfig);

		return applyUserSpecializedConfig;
	}

	private boolean checkFullPermission(Set<String> permittedEntities, final String keyUseList, final String keyList,
			final Map<String, List<Object>> serverPropertyMap) {
		Logging.info(this, "checkFullPermission  key name,  defaultResult true " + keyUseList);

		boolean fullPermission = true;

		if (serverPropertyMap.get(keyUseList) != null) {
			fullPermission = !(Boolean) (serverPropertyMap.get(keyUseList).get(0));
			// we don't give full permission if the config doesn't exist

			// we didn't configure anything, therefore we revoke the setting
			if (serverPropertyMap.get(keyList) == null) {
				fullPermission = true;
				Logging.info(this, "checkFullPermission not configured keyList " + keyList);
			}
		}

		Logging.info(this, "checkFullPermission  key for list,  fullPermission " + keyList + ", " + fullPermission);

		// we didn't configure anything, therefore we revoke the setting
		if (!fullPermission && serverPropertyMap.get(keyList) != null) {
			for (Object val : serverPropertyMap.get(keyList)) {
				permittedEntities.add((String) val);
			}
		}

		Logging.info(this, "checkFullPermission   result " + fullPermission);
		Logging.info(this, "checkFullPermission   produced list " + permittedEntities);

		return fullPermission;
	}

	@SuppressWarnings({ "java:S103" })
	private boolean checkStandardConfigs() {
		boolean result = configDataService.getConfigListCellOptionsPD() != null;
		Logging.info(this, "checkStandardConfigs, already there " + result);

		if (!result) {
			return false;
		}

		List<Object> defaultValues;
		List<Object> possibleValues;
		Map<String, Object> item;
		String key;
		List<Map<String, Object>> readyObjects = new ArrayList<>();

		// list of domains for new clients
		key = OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY;
		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = Utils.createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(configDataService.getOpsiDefaultDomainPD());

			possibleValues = new ArrayList<>();
			possibleValues.add(configDataService.getOpsiDefaultDomainPD());

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// search by sql if possible
		key = OpsiServiceNOMPersistenceController.KEY_SEARCH_BY_SQL;

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			item = Utils.createNOMBoolConfig(key, OpsiServiceNOMPersistenceController.DEFAULTVALUE_SEARCH_BY_SQL,
					"Use SQL calls for search if SQL backend is active");
			readyObjects.add(item);
		}

		// global value for install_by_shutdown

		key = OpsiServiceNOMPersistenceController.KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN;

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			item = Utils.createNOMBoolConfig(key, DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN,
					"Use install by shutdown if possible");
			readyObjects.add(item);
		}

		// product_sort_algorithm
		// will not be used in opsi 4.3
		if (!ServerFacade.isOpsi43()) {
			key = OpsiServiceNOMPersistenceController.KEY_PRODUCT_SORT_ALGORITHM;
			// defaultValues
			defaultValues = configDefaultValues.get(key);
			Logging.info(this, "checkStandardConfigs:  from server product_sort_algorithm " + defaultValues);

			if (defaultValues == null) {
				Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

				defaultValues = new ArrayList<>();
				defaultValues.add("algorithm1");

				possibleValues = new ArrayList<>();
				possibleValues.add("algorithm1");
				possibleValues.add("algorithm2");

				// create config for service
				item = Utils.createNOMitem("UnicodeConfig");
				item.put("ident", key);
				item.put("description", "algorithm1 = dependencies first; algorithm2 = priorities first");
				item.put("defaultValues", defaultValues);

				item.put("possibleValues", possibleValues);
				item.put("editable", false);
				item.put("multiValue", false);

				readyObjects.add(item);
			}
		}

		// extra display fields in licencing

		key = OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION;

		// defaultValues

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			// key not yet configured
			defaultValues = new ArrayList<>();
			// example for standard configuration other than empty
			// extra columns for licence management, page licences reconciliation
			possibleValues = new ArrayList<>();
			possibleValues.add("description");
			possibleValues.add("inventoryNumber");
			possibleValues.add("notes");
			possibleValues.add("ipAddress");
			possibleValues.add("lastSeen");

			// create config for service
			item = Utils.createNOMitem("UnicodeConfig");
			item.put("ident", key);
			item.put("description",
					Configed.getResourceValue("ConfigedMain.Licences.TabLicenceReconciliation.ExtraHostFields"));
			item.put("defaultValues", defaultValues);

			item.put("possibleValues", possibleValues);
			item.put("editable", false);
			item.put("multiValue", true);

			readyObjects.add(item);
		}

		// remote controls
		String command;
		String description;

		// ping_linux
		key = RemoteControl.CONFIG_KEY + "." + "ping_linux";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "xterm +hold -e ping %host%";
			description = "ping, started in a Linux environment";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY,
					true, "(command may be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// ping_windows
		key = RemoteControl.CONFIG_KEY + "." + "ping_windows";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "cmd.exe /c start ping %host%";
			description = "ping, started in a Windows terminal";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY,
					true, "(command may be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, linux
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_linux";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "firefox https://%host%:4441/info.html";
			description = "opsiclientd  timeline, called from a Linux environment, firefox recommended";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY,
					false, "(command may not be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, windows
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_windows";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "cmd.exe /c start https://%host%:4441/info.html";
			description = "opsiclientd  timeline, called rfrom a Windows environment";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY,
					false, "(command may not be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// additional queries
		String query;
		StringBuilder qbuf;
		key = OpsiServiceNOMPersistenceController.CONFIG_KEY_SUPPLEMENTARY_QUERY + "." + "hosts_with_products";

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			qbuf = new StringBuilder("select");
			qbuf.append(" hostId, productId, installationStatus from ");
			qbuf.append(" HOST, PRODUCT_ON_CLIENT ");
			qbuf.append(" WHERE HOST.hostId  = PRODUCT_ON_CLIENT.clientId ");
			qbuf.append(" AND =  installationStatus='installed' ");
			qbuf.append(" order by hostId, productId ");

			query = qbuf.toString();
			description = "all hosts and their installed products";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, query, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + EDITABLE_KEY, false,
					"(command may be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key + "." + DESCRIPTION_KEY,
					description, ""));
		}

		// WAN_CONFIGURATION
		// does it exist?

		Map<String, ConfigOption> wanConfigOptions = configDataService.retrieveWANConfigOptionsPD();
		if (wanConfigOptions == null || wanConfigOptions.isEmpty()) {
			Logging.info(this, "build default wanConfigOptions");
			readyObjects = buildWANConfigOptions(readyObjects);
		}

		// saved searches

		key = SavedSearch.CONFIG_KEY + "." + "product_failed";

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			StringBuilder val = new StringBuilder();
			val.append("{ \"version\" : \"2\", ");
			val.append("\"data\" : {");
			val.append(" \"element\" : null, ");
			val.append(" \"elementPath\" : null,");
			val.append(" \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, ");
			val.append(
					" \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] ");
			val.append("} }");

			String value = val.toString();

			description = "any product failed";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, value, description));

			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + SavedSearch.DESCRIPTION_KEY, description, ""));
		}

		// configuration of host menus

		key = ConfigDataService.KEY_DISABLED_CLIENT_ACTIONS;

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
					+ ConfigDataService.KEY_DISABLED_CLIENT_ACTIONS);
			// key not yet configured
			defaultValues = new ArrayList<>();
			configDefaultValues.put(key, defaultValues);

			possibleValues = new ArrayList<>();
			possibleValues.add(MainFrame.ITEM_ADD_CLIENT);
			possibleValues.add(MainFrame.ITEM_DELETE_CLIENT);
			possibleValues.add(MainFrame.ITEM_FREE_LICENCES);

			item = Utils.createNOMitem("UnicodeConfig");
			item.put("id", key);
			item.put("description", "");
			item.put("defaultValues", defaultValues);

			item.put("possibleValues", possibleValues);
			item.put("editable", false);
			item.put("multiValue", true);

			readyObjects.add(item);
		}

		key = OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINUSER;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
					+ OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINUSER);
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key,
					OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINUSER_DEFAULT_VALUE,
					"default windows username for deploy-client-agent-script"));
		}

		key = OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINPW;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
					+ OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINPW);
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key,
					OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINPW_DEFAULT_VALUE,
					"default windows password for deploy-client-agent-script"));
		}

		key = CONFIGED_WORKBENCH_KEY;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key,
					configDataService.getConfigedWorkbenchDefaultValuePD(), "default path to opsiproducts"));
		} else {
			Logging.info(this, "checkStandardConfigs set WORKBENCH_defaultvalue to " + (String) defaultValues.get(0));
			configDataService.setConfigedWorkbenchDefaultValuePD((String) defaultValues.get(0));
		}

		// configuration of opsiclientd extra events

		key = ConfigDataService.KEY_OPSICLIENTD_EXTRA_EVENTS;

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
					+ ConfigDataService.KEY_OPSICLIENTD_EXTRA_EVENTS);
			// key not yet configured
			defaultValues = new ArrayList<>();

			defaultValues.add(OpsiServiceNOMPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND);

			configDefaultValues.put(key, defaultValues);

			possibleValues = new ArrayList<>();

			possibleValues.add(OpsiServiceNOMPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND);
			possibleValues.add(OPSI_CLIENTD_EVENT_SILENT_INSTALL);

			item = Utils.createNOMitem("UnicodeConfig");
			item.put("id", key);
			item.put("description", "");
			item.put("defaultValues", defaultValues);

			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(item);
		}

		// for warnings for opsi licences

		// percentage number of clients
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = Utils.createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// absolute number of clients
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = Utils.createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// days limit warning
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = Utils.createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// modules disabled for warnings
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.DISABLE_WARNING_FOR_MODULES;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = Utils.createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();

			possibleValues = new ArrayList<>();

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// add metaconfigs

		// Update configs if there are some to update
		if (!readyObjects.isEmpty()) {
			Logging.notice(this, "There are " + readyObjects.size() + "configurations to update, so we do this now:");

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

			exec.doCall(omc);
		} else {
			Logging.notice(this, "there are no configurations to update");
		}

		deleteObsoleteDefaultUserConfigs(configDefaultValues);

		return true;
	}

	private void deleteObsoleteDefaultUserConfigs(Map<String, List<Object>> configDefaultValues) {
		List<Map<String, Object>> defaultUserConfigsObsolete = new ArrayList<>();

		for (Entry<String, List<Object>> configEntry : configDefaultValues.entrySet()) {
			if ((configEntry.getKey().startsWith(OpsiServiceNOMPersistenceController.ALL_USER_KEY_START + "ssh")
					|| configEntry.getKey()
							.startsWith(OpsiServiceNOMPersistenceController.ALL_USER_KEY_START + "{ole."))
					&& configEntry.getValue() != null) {
				Map<String, Object> config = new HashMap<>();
				config.put("id", configEntry.getKey());
				config.put("type", "BoolConfig");
				defaultUserConfigsObsolete.add(config);
			}
		}

		Logging.info(this, "Obsolete default user configs " + defaultUserConfigsObsolete);

		if (!defaultUserConfigsObsolete.isEmpty()) {
			exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS,
					new Object[] { defaultUserConfigsObsolete }));
		}
	}

	private static List<Map<String, Object>> buildWANConfigOptions(List<Map<String, Object>> readyObjects) {
		// NOT_WAN meta configs
		Map<String, Object> item = Utils.createNOMBoolConfig(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "."
						+ OpsiServiceNOMPersistenceController.NOT_WAN_CONFIGURED_PARTKEY + "."
						+ OpsiServiceNOMPersistenceController.CONFIG_CLIENTD_EVENT_GUISTARTUP,
				true, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = Utils.createNOMBoolConfig(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "."
						+ OpsiServiceNOMPersistenceController.NOT_WAN_CONFIGURED_PARTKEY + "."
						+ OpsiServiceNOMPersistenceController.CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN,
				true, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = Utils.createNOMBoolConfig(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "."
						+ OpsiServiceNOMPersistenceController.NOT_WAN_CONFIGURED_PARTKEY + "."
						+ OpsiServiceNOMPersistenceController.CONFIG_CLIENTD_EVENT_NET_CONNECTION,
				false, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = Utils.createNOMBoolConfig(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "."
						+ OpsiServiceNOMPersistenceController.NOT_WAN_CONFIGURED_PARTKEY + "."
						+ OpsiServiceNOMPersistenceController.CONFIG_CLIENTD_EVENT_TIMER,
				false, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		return readyObjects;
	}

	public boolean hasDepotPermission(String depotId) {
		if (hasDepotsFullPermissionPD()) {
			return true;
		}

		boolean result = false;

		Set<String> depotsPermitted = getDepotsPermittedPD();
		if (depotsPermitted != null) {
			result = depotsPermitted.contains(depotId);
		}

		return result;
	}

	private Set<String> getDepotsPermittedPD() {
		return cacheManager.getCachedData(CacheIdentifier.DEPOTS_PERMITTED, Set.class);
	}
}
