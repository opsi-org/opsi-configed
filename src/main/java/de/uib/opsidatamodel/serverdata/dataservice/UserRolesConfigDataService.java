/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SavedSearch;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.opsidatamodel.permission.UserOpsipermission;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

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

	public static final String ITEM_ADD_CLIENT = "add client";
	public static final String ITEM_DELETE_CLIENT = "remove client";
	public static final String ITEM_FREE_LICENSES = "free licenses for client";

	public static final String KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER = "configed.ssh.deploy-client-agent.default.user";
	public static final String KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER_DEFAULT_VALUE = "Administrator";
	public static final String KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW = "configed.ssh.deploy-client-agent.default.password";
	public static final String KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW_DEFAULT_VALUE = "";

	// keys for default wan configuration
	public static final String CONFIG_CLIENTD_EVENT_STARTUP = "opsiclientd.event_gui_startup.active";
	public static final String CONFIG_CLIENTD_EVENT_STARTUP_USER = "opsiclientd.event_gui_startup{user_logged_in}.active";
	public static final String CONFIG_CLIENTD_EVENT_TIMER = "opsiclientd.event_timer.active";
	public static final String CONFIG_CLIENTD_EVENT_NET_CONNECTION = "event_net_connection.active";

	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;

	public UserRolesConfigDataService(AbstractPOJOExecutioner exec,
			OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public boolean isGlobalReadOnly() {
		return Boolean.TRUE.equals(cacheManager.getCachedData(CacheIdentifier.GLOBAL_READ_ONLY, Boolean.class));
	}

	public boolean hasServerFullPermissionPD() {
		return Boolean.TRUE.equals(cacheManager.getCachedData(CacheIdentifier.SERVER_FULL_PERMISION, Boolean.class));
	}

	public boolean hasCreateClientPermissionPD() {
		return Boolean.TRUE.equals(cacheManager.getCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION, Boolean.class));
	}

	public boolean hasDepotsFullPermissionPD() {
		return Boolean.TRUE.equals(cacheManager.getCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION, Boolean.class));
	}

	public boolean hasKeyUserRegisterValuePD() {
		return Boolean.TRUE.equals(cacheManager.getCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, Boolean.class));
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
		return Boolean.TRUE.equals(
				cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED, Boolean.class));
	}

	public final void checkConfigurationPD() {
		persistenceController.getGroupDataService().retrieveAllObject2GroupsPD();
		persistenceController.getModuleDataService().retrieveOpsiModules();

		Map<String, List<Object>> serverPropertyMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();

		cacheManager.setCachedData(CacheIdentifier.GLOBAL_READ_ONLY, doesUserBelongToSystemsReadOnlyGroup());
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

		if (keyUserRegisterValue) {
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
		persistenceController.getGroupDataService().retrieveAllGroupsPD();

		List<Object> readyConfigObjects = new UserConfigProducing(applyUserSpecializedConfigPD(),
				persistenceController.getHostInfoCollections().getConfigServer(),
				persistenceController.getHostInfoCollections().getDepotNamesList(),
				persistenceController.getGroupDataService().getHostGroupIds(),
				persistenceController.getGroupDataService().getProductGroupsPD().keySet(),
				persistenceController.getConfigDataService().getConfigDefaultValuesPD(),
				persistenceController.getConfigDataService().getConfigListCellOptionsPD()).produce();

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
		Map<String, List<Object>> serverPropertyMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();
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
		if (Boolean.TRUE.equals(keyUserRegisterValue)
				&& !persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.USER_ROLES)) {
			keyUserRegisterValue = false;
			cacheManager.setCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, keyUserRegisterValue);
			SwingUtilities.invokeLater(this::callOpsiLicenseMissingText);
		}

		return keyUserRegisterValue;
	}

	private void callOpsiLicenseMissingText() {
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

	private boolean doesUserBelongToSystemsReadOnlyGroup() {
		boolean isUserReadOnlyUser = exec.getBooleanResult(
				new OpsiMethodCall(RPCMethodName.ACCESS_CONTROL_USER_IS_READ_ONLY_USER, new String[] {}));
		Logging.info(this, "does user belong to system's read-only group? " + isUserReadOnlyUser);
		return isUserReadOnlyUser;
	}

	// final in order to avoid deactiviating by override
	private final boolean setAgainUserRegistration(final boolean userRegisterValueFromConfigs) {
		boolean withUserRoles = persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.USER_ROLES);
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
		checkServerAccessPermissions();
		checkCreateClientPermission();
		checkDepotPermissions();
		checkHostGroupPermissions();
		checkProductPermissions();
	}

	private void checkServerAccessPermissions() {
		Map<String, List<Object>> serverPropertyMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();

		// variable for simplifying the use of the map
		String configKey;
		boolean globalReadOnly = isGlobalReadOnly();

		// already specified via systemuser group
		if (!globalReadOnly) {
			// lookup if we have a config for it and set it though not set by group
			configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY;
			Logging.info(this, "checkPermissions  configKey " + configKey);
			globalReadOnly = serverPropertyMap.get(configKey) != null
					&& (Boolean) serverPropertyMap.get(configKey).get(0);
			cacheManager.setCachedData(CacheIdentifier.GLOBAL_READ_ONLY, globalReadOnly);
		}

		Logging.info(this, " checkPermissions globalReadOnly " + globalReadOnly);

		boolean serverActionPermission = true;

		if (globalReadOnly) {
			serverActionPermission = false;
		} else {
			configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_SERVER_READWRITE;
			Logging.info(this, "checkPermissions  configKey " + configKey);

			if (serverPropertyMap.get(configKey) != null) {
				Logging.info(this, " checkPermissions  value  " + serverPropertyMap.get(configKey).get(0));
				serverActionPermission = (Boolean) serverPropertyMap.get(configKey).get(0);
			}
		}

		cacheManager.setCachedData(CacheIdentifier.SERVER_FULL_PERMISION, serverActionPermission);
	}

	private void checkCreateClientPermission() {
		Map<String, List<Object>> serverPropertyMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();
		String configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_CREATECLIENT;
		Logging.info(this, " checkPermissions key " + configKey);

		if (serverPropertyMap.get(configKey) != null
				&& persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.USER_ROLES)) {
			Logging.info(this, " checkPermissions  value  " + (serverPropertyMap.get(configKey).get(0)));
			boolean createClientPermission = (Boolean) serverPropertyMap.get(configKey).get(0);
			cacheManager.setCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION, createClientPermission);
		}
	}

	private void setProductsPermitted(Set<String> productGroupsPermitted) {
		Set<String> permittedProducts = new HashSet<>();

		for (String group : productGroupsPermitted) {
			Map<String, Set<String>> fProductGroup2Members = cacheManager
					.getCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS, Map.class);
			Set<String> products = fProductGroup2Members.get(group);
			if (products != null) {
				permittedProducts.addAll(products);
			}
		}
		cacheManager.setCachedData(CacheIdentifier.PERMITTED_PRODUCTS, permittedProducts);

		Logging.info(this, "checkPermissions permittedProducts " + permittedProducts);
	}

	private void checkDepotPermissions() {
		Map<String, List<Object>> serverPropertyMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();
		String configKeyUseList = userPartPD()
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;
		String configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;

		Set<String> depotsPermitted = new HashSet<>();

		boolean depotsFullPermission = checkFullPermission(depotsPermitted, configKeyUseList, configKeyList,
				serverPropertyMap);
		cacheManager.setCachedData(CacheIdentifier.DEPOTS_PERMITTED, depotsPermitted);
		cacheManager.setCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION, depotsFullPermission);
		Logging.info(this, "checkPermissions depotsFullPermission (false means, depots must be specified) "
				+ depotsFullPermission);
		Logging.info(this, "checkPermissions depotsPermitted " + depotsPermitted);
	}

	private void checkHostGroupPermissions() {
		Map<String, List<Object>> serverPropertyMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();

		String configKeyUseList = userPartPD()
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED;
		String configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE;
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
	}

	private void checkProductPermissions() {
		Map<String, List<Object>> serverPropertyMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();

		String configKeyUseList = userPartPD()
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED;
		String configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE;
		Set<String> productGroupsPermitted = new HashSet<>();

		boolean productgroupsFullPermission = checkFullPermission(productGroupsPermitted, configKeyUseList,
				configKeyList, serverPropertyMap);

		// Add subgroups of permitted groups to permitted groups
		Map<String, Map<String, String>> productGroups = persistenceController.getGroupDataService()
				.getProductGroupsPD();
		for (Entry<String, Map<String, String>> groupEntry : productGroups.entrySet()) {
			if (!productGroupsPermitted.contains(groupEntry.getKey())
					&& hasPermittedParentGroup(productGroups, productGroupsPermitted, groupEntry.getKey())) {
				productGroupsPermitted.add(groupEntry.getKey());
			}
		}

		cacheManager.setCachedData(CacheIdentifier.PERMITTED_PRODUCT_GROUPS, productGroupsPermitted);
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_GROUPS_FULL_PERMISSION, productgroupsFullPermission);

		if (!productgroupsFullPermission) {
			setProductsPermitted(productGroupsPermitted);
		}
	}

	/**
	 * This Method will check recursively if a parent group of the given
	 * productGroup is a permitted product group
	 */
	private static boolean hasPermittedParentGroup(Map<String, Map<String, String>> productGroups,
			Set<String> productGroupsPermitted, String productGroup) {
		String parentGroupId = productGroups.get(productGroup).get("parentGroupId");

		while (productGroups.containsKey(parentGroupId)) {
			if (productGroupsPermitted.contains(parentGroupId)) {
				return true;
			} else {
				parentGroupId = productGroups.get(parentGroupId).get("parentGroupId");
			}
		}

		return false;
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

		applyUserSpecializedConfig = persistenceController.getModuleDataService()
				.isOpsiModuleActive(OpsiModule.USER_ROLES) && hasKeyUserRegisterValuePD();
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

	private List<Object> computeConfigedGivenDomains(List<Map<String, Object>> readyObjects) {
		Logging.info(this, "checkStandardConfigs: create domain list");

		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");

		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(persistenceController.getConfigDataService().getOpsiDefaultDomainPD());

		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(persistenceController.getConfigDataService().getOpsiDefaultDomainPD());

		item.put("ident", OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", true);
		item.put("multiValue", true);

		readyObjects.add(item);

		return defaultValues;
	}

	private List<Object> computeClientConfigInstallByShutdown(List<Map<String, Object>> readyObjects) {
		Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
				+ OpsiServiceNOMPersistenceController.KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN);

		Map<String, Object> item = Utils.createNOMBoolConfig(
				OpsiServiceNOMPersistenceController.KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN,
				DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN, "Use install by shutdown if possible");
		readyObjects.add(item);

		return Collections.singletonList(DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN);
	}

	private List<Object> computeHostExtraDisplayfieldsInPanelLicensesReconciliation(
			List<Map<String, Object>> readyObjects) {
		Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
				+ OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION);
		// key not yet configured
		List<Object> defaultValues = new ArrayList<>();
		// example for standard configuration other than empty
		// extra columns for license management, page licenses reconciliation
		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add("description");
		possibleValues.add("inventoryNumber");
		possibleValues.add("notes");
		possibleValues.add("ipAddress");
		possibleValues.add("lastSeen");

		// create config for service
		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");
		item.put("ident",
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION);
		item.put("description",
				Configed.getResourceValue("ConfigedMain.Licenses.TabLicenseReconciliation.ExtraHostFields"));
		item.put("defaultValues", defaultValues);

		item.put("possibleValues", possibleValues);
		item.put("editable", false);
		item.put("multiValue", true);

		readyObjects.add(item);

		return defaultValues;
	}

	private List<Object> computeDisabledClientActions(List<Map<String, Object>> readyObjects) {
		Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
				+ ConfigDataService.KEY_DISABLED_CLIENT_ACTIONS);
		// key not yet configured
		List<Object> defaultValues = Collections.emptyList();

		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(ITEM_ADD_CLIENT);
		possibleValues.add(ITEM_DELETE_CLIENT);
		possibleValues.add(ITEM_FREE_LICENSES);

		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");
		item.put("id", ConfigDataService.KEY_DISABLED_CLIENT_ACTIONS);
		item.put("description", "");
		item.put("defaultValues", defaultValues);

		item.put("possibleValues", possibleValues);
		item.put("editable", false);
		item.put("multiValue", true);

		readyObjects.add(item);

		return defaultValues;
	}

	private List<Object> computeOpsiclientdExtraEvents(List<Map<String, Object>> readyObjects) {
		Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
				+ ConfigDataService.KEY_OPSICLIENTD_EXTRA_EVENTS);
		// key not yet configured
		List<Object> defaultValues = Collections
				.singletonList(OpsiServiceNOMPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND);

		List<Object> possibleValues = new ArrayList<>();

		possibleValues.add(OpsiServiceNOMPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND);
		possibleValues.add(OPSI_CLIENTD_EVENT_SILENT_INSTALL);

		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");
		item.put("id", ConfigDataService.KEY_OPSICLIENTD_EXTRA_EVENTS);
		item.put("description", "");
		item.put("defaultValues", defaultValues);

		item.put("possibleValues", possibleValues);
		item.put("editable", true);
		item.put("multiValue", true);

		readyObjects.add(item);

		return defaultValues;
	}

	private List<Object> computeClientLimitWarningPercent(List<Map<String, Object>> readyObjects) {
		Logging.info(this, "checkStandardConfigs: create domain list");

		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");

		List<Object> defaultValues = Collections.singletonList(LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT_DEFAULT);

		List<Object> possibleValues = Collections.singletonList(LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT_DEFAULT);

		item.put("ident", LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", true);
		item.put("multiValue", false);

		readyObjects.add(item);

		return defaultValues;
	}

	private List<Object> computeClientLimitWarningAbsolute(List<Map<String, Object>> readyObjects) {
		Logging.info(this, "checkStandardConfigs: create domain list");

		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");

		List<Object> defaultValues = Collections.singletonList(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

		List<Object> possibleValues = Collections.singletonList(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

		item.put("ident", LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", true);
		item.put("multiValue", false);

		readyObjects.add(item);

		return defaultValues;
	}

	private List<Object> computeClientLimitWarningDays(List<Map<String, Object>> readyObjects) {
		Logging.info(this, "checkStandardConfigs: create domain list");

		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");

		List<Object> defaultValues = Collections.singletonList(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

		List<Object> possibleValues = Collections.singletonList(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

		item.put("ident", LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", true);
		item.put("multiValue", false);

		readyObjects.add(item);

		return defaultValues;
	}

	private List<Object> computeDisableWarningModules(List<Map<String, Object>> readyObjects) {
		Logging.info(this, "checkStandardConfigs: create domain list");

		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");

		List<Object> defaultValues = Collections.emptyList();

		List<Object> possibleValues = Collections.emptyList();

		item.put("ident", LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.DISABLE_WARNING_FOR_MODULES);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", true);
		item.put("multiValue", true);

		readyObjects.add(item);

		return defaultValues;
	}

	private void checkRemoteControlConfigs(Map<String, List<Object>> configDefaultValues,
			List<Map<String, Object>> readyObjects) {
		// ping_linux
		String key = RemoteControl.CONFIG_KEY + "." + "ping_linux";
		if (!configDefaultValues.containsKey(key)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			String command = "xterm +hold -e ping %host%";
			String description = "ping, started in a Linux environment";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY,
					true, "(command may be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// ping_windows
		key = RemoteControl.CONFIG_KEY + "." + "ping_windows";

		if (!configDefaultValues.containsKey(key)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			String command = "cmd.exe /c start ping %host%";
			String description = "ping, started in a Windows terminal";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY,
					true, "(command may be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, linux
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_linux";

		if (!configDefaultValues.containsKey(key)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			String command = "firefox https://%host%:4441/info.html";
			String description = "opsiclientd  timeline, called from a Linux environment, firefox recommended";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY,
					false, "(command may not be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, windows
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_windows";

		if (!configDefaultValues.containsKey(key)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			String command = "cmd.exe /c start https://%host%:4441/info.html";
			String description = "opsiclientd  timeline, called rfrom a Windows environment";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY,
					false, "(command may not be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}
	}

	private void checkAdditionalQueries(Map<String, List<Object>> configDefaultValues,
			List<Map<String, Object>> readyObjects) {
		String key = OpsiServiceNOMPersistenceController.CONFIG_KEY_SUPPLEMENTARY_QUERY + "." + "hosts_with_products";

		if (!configDefaultValues.containsKey(key)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			StringBuilder qbuf = new StringBuilder("select");
			qbuf.append(" hostId, productId, installationStatus from ");
			qbuf.append(" HOST, PRODUCT_ON_CLIENT ");
			qbuf.append(" WHERE HOST.hostId  = PRODUCT_ON_CLIENT.clientId ");
			qbuf.append(" AND =  installationStatus='installed' ");
			qbuf.append(" order by hostId, productId ");

			String query = qbuf.toString();
			String description = "all hosts and their installed products";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, query, description));
			readyObjects.add(ConfigDataService.produceConfigEntry("BoolConfig", key + "." + EDITABLE_KEY, false,
					"(command may be edited)"));
			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key + "." + DESCRIPTION_KEY,
					description, ""));
		}
	}

	private void checkSavedSearches(Map<String, List<Object>> configDefaultValues,
			List<Map<String, Object>> readyObjects) {
		String key = SavedSearch.CONFIG_KEY + "." + "product_failed";

		if (!configDefaultValues.containsKey(key)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			StringBuilder val = new StringBuilder();
			val.append("{ \"version\" : \"2\", ");
			val.append("\"data\" : {");
			val.append(" \"element\" : null, ");
			val.append(" \"elementPath\" : null,");
			val.append(" \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, ");
			val.append(" \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : ");
			val.append("[ \\\"Product\\\", \\\"Action Result\\\" ], \"operation\" : \"StringEqualsOperation\",");
			val.append(" \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] ");
			val.append("} }");

			String value = val.toString();

			String description = "any product failed";

			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", key, value, description));

			// description entry
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig",
					key + "." + SavedSearch.DESCRIPTION_KEY, description, ""));
		}
	}

	private void checkSSHCommands(Map<String, List<Object>> configDefaultValues,
			List<Map<String, Object>> readyObjects) {
		if (!configDefaultValues.containsKey(KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
					+ KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER);
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER,
					KEY_DEPLOY_CLIENT_AGENT_DEFAULT_USER_DEFAULT_VALUE,
					"default windows username for deploy-client-agent-script"));
		}

		if (!configDefaultValues.containsKey(KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
					+ KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW);
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW,
					KEY_DEPLOY_CLIENT_AGENT_DEFAULT_PW_DEFAULT_VALUE,
					"default windows password for deploy-client-agent-script"));
		}
	}

	@SuppressWarnings({ "java:S103" })
	private boolean checkStandardConfigs() {
		boolean result = persistenceController.getConfigDataService().getConfigListCellOptionsPD() != null;
		Logging.info(this, "checkStandardConfigs, already there " + result);

		if (!result) {
			return false;
		}

		List<Map<String, Object>> readyObjects = new ArrayList<>();

		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);

		// list of domains for new clients		
		configDefaultValues.computeIfAbsent(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY,
				arg -> computeConfigedGivenDomains(readyObjects));

		// global value for install_by_shutdown
		configDefaultValues.computeIfAbsent(OpsiServiceNOMPersistenceController.KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN,
				arg -> computeClientConfigInstallByShutdown(readyObjects));

		// extra display fields in licencing
		configDefaultValues.computeIfAbsent(
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION,
				arg -> computeHostExtraDisplayfieldsInPanelLicensesReconciliation(readyObjects));

		// remote controls
		checkRemoteControlConfigs(configDefaultValues, readyObjects);

		// additional queries
		checkAdditionalQueries(configDefaultValues, readyObjects);

		// WAN_CONFIGURATION
		// does it exist?

		Map<String, ConfigOption> wanConfigOptions = persistenceController.getConfigDataService()
				.retrieveWANConfigOptionsPD();
		if (wanConfigOptions == null || wanConfigOptions.isEmpty()) {
			Logging.info(this, "build default wanConfigOptions");
			buildWANConfigOptions(readyObjects);
		}

		// saved searches
		checkSavedSearches(configDefaultValues, readyObjects);

		// configuration of host menus
		configDefaultValues.computeIfAbsent(ConfigDataService.KEY_DISABLED_CLIENT_ACTIONS,
				arg -> computeDisabledClientActions(readyObjects));

		checkSSHCommands(configDefaultValues, readyObjects);

		if (!configDefaultValues.containsKey(CONFIGED_WORKBENCH_KEY)) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + CONFIGED_WORKBENCH_KEY);
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", CONFIGED_WORKBENCH_KEY,
					persistenceController.getConfigDataService().getConfigedWorkbenchDefaultValuePD(),
					"default path to opsiproducts"));
		} else {
			Logging.info(this, "checkStandardConfigs set WORKBENCH_defaultvalue to "
					+ (String) configDefaultValues.get(CONFIGED_WORKBENCH_KEY).get(0));
			persistenceController.getConfigDataService().setConfigedWorkbenchDefaultValuePD(
					(String) configDefaultValues.get(CONFIGED_WORKBENCH_KEY).get(0));
		}

		// configuration of opsiclientd extra events
		configDefaultValues.computeIfAbsent(ConfigDataService.KEY_OPSICLIENTD_EXTRA_EVENTS,
				arg -> computeOpsiclientdExtraEvents(readyObjects));

		// for warnings for opsi licenses
		// percentage number of clients
		configDefaultValues.computeIfAbsent(
				LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT,
				arg -> computeClientLimitWarningPercent(readyObjects));

		// absolute number of clients
		configDefaultValues.computeIfAbsent(
				LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE,
				arg -> computeClientLimitWarningAbsolute(readyObjects));

		// days limit warning
		configDefaultValues.computeIfAbsent(
				LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS,
				arg -> computeClientLimitWarningDays(readyObjects));

		// modules disabled for warnings
		configDefaultValues.computeIfAbsent(
				LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.DISABLE_WARNING_FOR_MODULES,
				arg -> computeDisableWarningModules(readyObjects));

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

	private static void buildWANConfigOptions(List<Map<String, Object>> readyObjects) {
		// NOT_WAN meta configs
		Map<String, Object> item = Utils
				.createNOMBoolConfig(
						OpsiServiceNOMPersistenceController.CONFIG_KEY + "."
								+ ConfigDataService.NOT_WAN_CONFIGURED_PARTKEY + "." + CONFIG_CLIENTD_EVENT_STARTUP,
						true, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = Utils.createNOMBoolConfig(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "." + ConfigDataService.NOT_WAN_CONFIGURED_PARTKEY
						+ "." + CONFIG_CLIENTD_EVENT_STARTUP_USER,
				true, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = Utils.createNOMBoolConfig(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "." + ConfigDataService.NOT_WAN_CONFIGURED_PARTKEY
						+ "." + CONFIG_CLIENTD_EVENT_NET_CONNECTION,
				false, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = Utils
				.createNOMBoolConfig(
						OpsiServiceNOMPersistenceController.CONFIG_KEY + "."
								+ ConfigDataService.NOT_WAN_CONFIGURED_PARTKEY + "." + CONFIG_CLIENTD_EVENT_TIMER,
						false, "meta configuration for default not wan behaviour");

		readyObjects.add(item);
	}

	public boolean hasDepotPermission(String depotId) {
		if (hasDepotsFullPermissionPD()) {
			return true;
		}

		Set<String> depotsPermitted = cacheManager.getCachedData(CacheIdentifier.DEPOTS_PERMITTED, Set.class);

		return depotsPermitted != null && depotsPermitted.contains(depotId);
	}
}
