/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.permission.UserConfig;
import de.uib.configed.core.domain.permission.UserConfigProducing;
import de.uib.configed.core.domain.permission.UserFeaturesConfig;
import de.uib.configed.core.domain.permission.UserOpsipermission;
import de.uib.configed.core.domain.permission.UserServerConsoleConfig;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ConfigedMain.EditingTarget;
import de.uib.configed.gui.ServerConfiguration;
import de.uib.configed.gui.type.RemoteControl;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

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
public class UserRolesConfigDataService extends DataService {
	private static final String OPSI_CLIENTD_EVENT_SILENT_INSTALL = "silent_install";

	private static final String CONFIGED_WORKBENCH_KEY = "configed.workbench.default";

	public static final String ITEM_ADD_CLIENT = "add client";
	public static final String ITEM_DELETE_CLIENT = "remove client";
	public static final String ITEM_FREE_LICENSES = "free licenses for client";

	// keys for default wan configuration
	public static final String CONFIG_CLIENTD_EVENT_STARTUP = "opsiclientd.event_gui_startup.active";
	public static final String CONFIG_CLIENTD_EVENT_STARTUP_USER = "opsiclientd.event_gui_startup{user_logged_in}.active";
	public static final String CONFIG_CLIENTD_EVENT_TIMER = "opsiclientd.event_timer.active";
	public static final String CONFIG_CLIENTD_EVENT_NET_CONNECTION = "opsiclientd.event_net_connection.active";

	public UserRolesConfigDataService(DataServices dataServices) {
		super(dataServices);
	}

	public boolean isGlobalReadOnly() {
		return Boolean.TRUE
				.equals(dataServices.cacheManager.getCachedData(CacheIdentifier.GLOBAL_READ_ONLY, Boolean.class));
	}

	public boolean hasServerFullPermissionPD() {
		return Boolean.TRUE
				.equals(dataServices.cacheManager.getCachedData(CacheIdentifier.SERVER_FULL_PERMISION, Boolean.class));
	}

	public boolean canCreateClient() {
		return !isGlobalReadOnly()
				&& Boolean.TRUE.equals(dataServices.cacheManager.getCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION,
						Boolean.class))
				&& !dataServices.config.getDisabledClientMenuEntries()
						.contains(UserRolesConfigDataService.ITEM_ADD_CLIENT);
	}

	public boolean hasDepotsFullPermissionPD() {
		return Boolean.TRUE
				.equals(dataServices.cacheManager.getCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION, Boolean.class));
	}

	public boolean hasProductGroupsFullPermissionPD() {
		return Boolean.TRUE.equals(
				dataServices.cacheManager.getCachedData(CacheIdentifier.PRODUCT_GROUPS_FULL_PERMISSION, Boolean.class));
	}

	public boolean hasKeyUserRegisterValuePD() {
		return Boolean.TRUE.equals(
				dataServices.cacheManager.getCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, Boolean.class));
	}

	public Set<String> getPermittedProductsPD() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.PERMITTED_PRODUCTS, Set.class);
	}

	public Set<String> getPermittedProductGroupsPD() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.PERMITTED_PRODUCT_GROUPS, Set.class);
	}

	public Set<String> getHostGroupsPermitted() {
		Set<String> result = null;
		if (!isAccessToHostgroupsOnlyIfExplicitlyStatedPD()) {
			result = dataServices.cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS_PERMITTED, Set.class);
		}

		Logging.info(this, "getHostgroupsPermitted ", result);

		return result;
	}

	public boolean isAccessToHostgroupsOnlyIfExplicitlyStatedPD() {
		return Boolean.TRUE.equals(dataServices.cacheManager
				.getCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED, Boolean.class));
	}

	/**
	 * Determines whether the currently logged-in user is allowed to edit their
	 * own role in the server configuration, even if the global read-only
	 * privilege is enabled.
	 * <p>
	 * Domain-specific context:
	 * <ul>
	 * <li>Users can have privileges that restrict actions (e.g. read-only,
	 * write, etc.).</li>
	 * <li>The {@code privilege.host.all.registered_readonly} flag normally
	 * prevents all data modifications.</li>
	 * <li>However, if the user has {@code privilege.host.opsiserver.write} set
	 * to {@code true}, and they are currently viewing the Server Configuration
	 * and editing their own role, they are allowed to make changes to their own
	 * role configuration despite being globally read-only.</li>
	 * </ul>
	 * <p>
	 * This method checks whether:
	 * <ol>
	 * <li>The current editing target is the Server Configuration view,</li>
	 * <li>The user has full server write permission
	 * ({@code opsiserver.write == true}), and</li>
	 * <li>The role currently selected in the configuration matches the
	 * logged-in user's own role.</li>
	 * </ol>
	 * <p>
	 * If all conditions are met, the user can edit their own server role.
	 * </p>
	 *
	 * @return {@code true} if the user is allowed to edit their own server
	 *         role, {@code false} otherwise.
	 */
	public boolean canEditOwnServerRole() {
		boolean isViewServerConfiguration = ConfigedMain.getEditingTarget() == EditingTarget.SERVER;
		boolean hasServerFullPermission = PersistenceControllerFactory.getPersistenceController()
				.getDataServices().userRoles.hasServerFullPermissionPD();
		ServerConfiguration serverConfiguration = ConfigedMain.getMainFrame().getMainPanelManager()
				.getServerConfiguration();
		boolean isCurrentUserRoleSelected = serverConfiguration != null
				&& serverConfiguration.isCurrentUserRoleSelected();
		return isViewServerConfiguration && hasServerFullPermission && isCurrentUserRoleSelected;
	}

	public final void checkConfigurationPD() {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		executor.runInParallel(dataServices.group::retrieveAllObject2GroupsPD);
		executor.runInParallel(dataServices.module::retrieveOpsiModules);

		// Load all data together to prevent an extra RPC-call
		executor.runInParallel(dataServices.group::retrieveAllGroupsPD);
		executor.runInParallel(() -> dataServices.cacheManager.setCachedData(CacheIdentifier.GLOBAL_READ_ONLY,
				doesUserBelongToSystemsReadOnlyGroup()));
		executor.waitForCompletion();

		// We need to set default data in case the user roles are deactivated
		dataServices.cacheManager.setCachedData(CacheIdentifier.SERVER_FULL_PERMISION, !isGlobalReadOnly());
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION, true);
		dataServices.cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED, false);
		dataServices.cacheManager.setCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION, true);
		dataServices.cacheManager.setCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, isUserRegisterActivated());
		boolean keyUserRegisterValue = dataServices.cacheManager.getCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE,
				Boolean.class);
		boolean correctedUserRegisterVal = setAgainUserRegistration(keyUserRegisterValue);

		boolean setUserRegisterVal = !keyUserRegisterValue && correctedUserRegisterVal;

		if (setUserRegisterVal) {
			keyUserRegisterValue = true;
		}

		if (keyUserRegisterValue) {
			dataServices.cacheManager.setCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, keyUserRegisterValue);
			keyUserRegisterValue = checkUserRolesModulePD();
		}

		if (dataServices.config.getConfigDefaultValuesPD()
				.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER) == null || setUserRegisterVal) {
			List<Object> readyObjects = new ArrayList<>();
			Map<String, Object> item = Utils.createNOMBoolConfig(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER,
					keyUserRegisterValue, "without given values the primary value setting is false");
			readyObjects.add(item);

			dataServices.exec.doCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, readyObjects);
		}

		new UserConfigProducing(applyUserSpecializedConfigPD(), dataServices.hostInfoCollections.getConfigServer(),
				dataServices.hostInfoCollections.getDepotNamesList(), dataServices.group.getHostGroupIds(),
				dataServices.group.getProductGroupsPD().keySet(), dataServices.config.getConfigDefaultValuesPD(),
				dataServices.config.getConfigOptionsPD()).produce();
		checkPermissions();

		if (hasServerFullPermissionPD()) {
			checkStandardConfigs();
		}
	}

	private boolean isUserRegisterActivated() {
		boolean result = false;
		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();
		// dont do anything if we have not got the config
		if (serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER) != null
				&& !serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER).isEmpty()) {
			result = (Boolean) ((List<?>) serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER))
					.get(0);
		}
		return result;
	}

	private final boolean checkUserRolesModulePD() {
		boolean keyUserRegisterValue = dataServices.cacheManager.getCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE,
				Boolean.class);
		if (Boolean.TRUE.equals(keyUserRegisterValue)
				&& !dataServices.module.isOpsiModuleActive(OpsiModule.USER_ROLES)) {
			keyUserRegisterValue = false;
			dataServices.cacheManager.setCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, keyUserRegisterValue);
			SwingUtilities.invokeLater(this::callOpsiLicenseMissingText);
		}

		return keyUserRegisterValue;
	}

	private void callOpsiLicenseMissingText() {
		StringBuilder info = new StringBuilder();
		info.append(Configed.getResourceValue("Permission.modules.missing_user_roles.1") + "<br>");
		info.append(Configed.getResourceValue("Permission.modules.missing_user_roles.2") + "<br>");
		info.append(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER + " "
				+ Configed.getResourceValue("Permission.modules.missing_user_roles.3"));

		Logging.warning(this, " user role administration configured but not permitted by the modules file ", info);

		Utils.showMissingLicenseModules(info.toString());
	}

	private boolean doesUserBelongToSystemsReadOnlyGroup() {
		boolean isUserReadOnlyUser = dataServices.exec
				.getBooleanResult(RPCMethodName.ACCESS_CONTROL_USER_IS_READ_ONLY_USER);
		Logging.info(this, "does user belong to system's read-only group? ", isUserReadOnlyUser);
		return isUserReadOnlyUser;
	}

	// final in order to avoid deactiviating by override
	private final boolean setAgainUserRegistration(final boolean userRegisterValueFromConfigs) {
		boolean withUserRoles = dataServices.module.isOpsiModuleActive(OpsiModule.USER_ROLES);
		Logging.info(this, "setAgainUserRegistration, userRoles can be used ", withUserRoles);

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
			Logging.info(this, "setAgainUserRegistration, userRegister was activated ", locallySavedValueUserRegister);
			if (userRegisterValueFromConfigs) {
				if (locallySavedValueUserRegister == null || !locallySavedValueUserRegister) {
					// we save true
					Configed.getSavedStates().setProperty(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER,
							"true");
				}
			} else if (Boolean.TRUE.equals(locallySavedValueUserRegister)) {
				// if true was locally saved but is not the value from service then we ask
				Logging.warning(this, "setAgainUserRegistration, it seems that user check has been deactivated");
				StringBuilder message = new StringBuilder(
						Configed.getResourceValue("RegisterUserWarning.dialog.info1"));
				message.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.info2"));

				int answer = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(), message.toString(),
						Configed.getResourceValue("RegisterUserWarning.dialog.title"), 0, JOptionPane.WARNING_MESSAGE,
						null,
						new Object[] { Configed.getResourceValue("buttonClose"),
								Configed.getResourceValue("RegisterUserWarning.dialog.button.dontWarnAgain"),
								Configed.getResourceValue("RegisterUserWarning.dialog.button.reactivateUserRoles") },
						null);

				Logging.info(this, "setAgainUserRegistration, reaction via option ", answer);

				switch (answer) {
				case 1 -> {
					Logging.info(this, "setAgainUserRegistration remove warning locally ");
					// remove from store
					Configed.getSavedStates().remove(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER);
				}
				case 2 -> {
					Logging.info(this, "setAgainUserRegistration reactivate user check ");
					resultVal = true;
				}
				// We pressed cancel or closed the dialog
				default -> Logging.info(this, "setAgainUserRegistration ignore ");
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
		checkTerminalPermissions();
		checkFeaturesPermissions();
	}

	private void checkServerAccessPermissions() {
		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();

		// variable for simplifying the use of the map
		String configKey;
		boolean globalReadOnly = isGlobalReadOnly();

		// already specified via systemuser group
		if (!globalReadOnly) {
			// lookup if we have a config for it and set it though not set by group
			configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY;
			Logging.info(this, "checkPermissions  configKey ", configKey);
			globalReadOnly = serverPropertyMap.get(configKey) != null
					&& (Boolean) serverPropertyMap.get(configKey).get(0);
			dataServices.cacheManager.setCachedData(CacheIdentifier.GLOBAL_READ_ONLY, globalReadOnly);
		}

		Logging.info(this, " checkPermissions globalReadOnly ", globalReadOnly);

		boolean serverActionPermission = true;

		configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_SERVER_READWRITE;
		Logging.info(this, "checkPermissions  configKey ", configKey);

		if (serverPropertyMap.get(configKey) != null) {
			Logging.info(this, " checkPermissions  value  ", serverPropertyMap.get(configKey).get(0));
			serverActionPermission = (Boolean) serverPropertyMap.get(configKey).get(0);
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.SERVER_FULL_PERMISION, serverActionPermission);
	}

	private void checkKeyPermission(Map<String, List<Object>> serverPropertyMap, String configKey,
			CacheIdentifier cacheIdentifier) {
		if (serverPropertyMap.get(configKey) != null && dataServices.module.isOpsiModuleActive(OpsiModule.USER_ROLES)) {
			Logging.info(this, " checkPermissions  value  ", serverPropertyMap.get(configKey));
			List<Object> items = serverPropertyMap.get(configKey);
			dataServices.cacheManager.setCachedData(cacheIdentifier, items.get(0));
		} else {
			Logging.info(this, " checkPermissions default value ", configKey);
			dataServices.cacheManager.setCachedData(cacheIdentifier, true);
		}
	}

	private void checkTerminalPermissions() {
		Logging.debug(this, "checkTerminalPermissions");

		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();

		checkKeyPermission(serverPropertyMap, userPartPD() + UserServerConsoleConfig.KEY_SERVER_CONSOLE_MENU_ACTIVE,
				CacheIdentifier.TERMINAL_MENU_ACTIVE);
		checkKeyPermission(serverPropertyMap, userPartPD() + UserServerConsoleConfig.KEY_SERVER_CONSOLE_COMMANDS_ACTIVE,
				CacheIdentifier.TERMINAL_COMMANDS_ACTIVE);
		checkKeyPermission(serverPropertyMap,
				userPartPD() + UserServerConsoleConfig.KEY_SERVER_CONSOLE_COMMANDCONTROL_ACTIVE,
				CacheIdentifier.TERMINAL_COMMAND_CONTROL_ACTIVE);

		String configKey = userPartPD() + UserServerConsoleConfig.KEY_TERMINAL_ACCESS_FORBIDDEN;
		if (serverPropertyMap.get(configKey) != null && dataServices.module.isOpsiModuleActive(OpsiModule.USER_ROLES)) {
			Logging.info(this, "checkPermissions value:", serverPropertyMap.get(configKey));
			List<Object> forbiddenItems = serverPropertyMap.get(configKey);
			dataServices.cacheManager.setCachedData(CacheIdentifier.TERMINAL_FORBIDDEN, forbiddenItems);
		} else {
			dataServices.cacheManager.setCachedData(CacheIdentifier.TERMINAL_FORBIDDEN, Collections.emptyList());
		}
	}

	private void checkFeaturesPermissions() {
		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();
		String configKey = userPartPD() + UserFeaturesConfig.KEY_MOTD_ACCESS_FORBIDDEN;

		if (serverPropertyMap.get(configKey) != null && dataServices.module.isOpsiModuleActive(OpsiModule.USER_ROLES)) {
			Logging.info(this, " checkPermissions  value  ", serverPropertyMap.get(configKey));
			List<Object> forbiddenItems = serverPropertyMap.get(configKey);
			dataServices.cacheManager.setCachedData(CacheIdentifier.MOTD_FORBIDDEN, forbiddenItems);
		} else {
			dataServices.cacheManager.setCachedData(CacheIdentifier.MOTD_FORBIDDEN, new ArrayList<>());
		}
	}

	private void checkCreateClientPermission() {
		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();
		String configKey = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_CREATECLIENT;
		Logging.info(this, " checkPermissions key ", configKey);

		if (serverPropertyMap.get(configKey) != null && dataServices.module.isOpsiModuleActive(OpsiModule.USER_ROLES)) {
			Logging.info(this, " checkPermissions  value  ", serverPropertyMap.get(configKey).get(0));
			boolean createClientPermission = (Boolean) serverPropertyMap.get(configKey).get(0);
			dataServices.cacheManager.setCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION, createClientPermission);
		}
	}

	private void setProductsPermitted(Set<String> productGroupsPermitted) {
		Set<String> permittedProducts = new HashSet<>();

		for (String group : productGroupsPermitted) {
			Map<String, Set<String>> fProductGroup2Members = dataServices.cacheManager
					.getCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS, Map.class);
			Set<String> products = fProductGroup2Members.get(group);
			if (products != null) {
				permittedProducts.addAll(products);
			}
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.PERMITTED_PRODUCTS, permittedProducts);

		Logging.info(this, "checkPermissions permittedProducts ", permittedProducts);
	}

	private void checkDepotPermissions() {
		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();
		String configKeyUseList = userPartPD()
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;
		String configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;

		Set<String> depotsPermitted = new HashSet<>();

		boolean depotsFullPermission = checkFullPermission(depotsPermitted, configKeyUseList, configKeyList,
				serverPropertyMap);
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOTS_PERMITTED, depotsPermitted);
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION, depotsFullPermission);
		Logging.info(this, "checkPermissions depotsFullPermission (false means, depots must be specified) ",
				depotsFullPermission);
		Logging.info(this, "checkPermissions depotsPermitted ", depotsPermitted);
	}

	private void checkHostGroupPermissions() {
		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();

		String configKeyUseList = userPartPD()
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED;
		String configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE;
		Set<String> hostgroupsPermitted = new HashSet<>();

		boolean hostgroupsOnlyIfExplicitlyStated = checkFullPermission(hostgroupsPermitted, configKeyUseList,
				configKeyList, serverPropertyMap);

		if (hostgroupsOnlyIfExplicitlyStated) {
			hostgroupsPermitted = null;
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS_PERMITTED, hostgroupsPermitted);
		dataServices.cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED,
				hostgroupsOnlyIfExplicitlyStated);

		Logging.info(this, "checkPermissions hostgroupsPermitted ", hostgroupsPermitted);
	}

	private void checkProductPermissions() {
		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();

		String configKeyUseList = userPartPD()
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED;
		String configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE;
		Set<String> productGroupsPermitted = new HashSet<>();

		boolean productGroupsFullPermission = checkFullPermission(productGroupsPermitted, configKeyUseList,
				configKeyList, serverPropertyMap);
		dataServices.cacheManager.setCachedData(CacheIdentifier.PRODUCT_GROUPS_FULL_PERMISSION,
				productGroupsFullPermission);
		// Add subgroups of permitted groups to permitted groups
		Map<String, Map<String, String>> productGroups = dataServices.group.getProductGroupsPD();
		for (Entry<String, Map<String, String>> groupEntry : productGroups.entrySet()) {
			if (!productGroupsPermitted.contains(groupEntry.getKey())
					&& hasPermittedParentGroup(productGroups, productGroupsPermitted, groupEntry.getKey())) {
				productGroupsPermitted.add(groupEntry.getKey());
			}
		}

		if (!productGroupsFullPermission) {
			dataServices.cacheManager.setCachedData(CacheIdentifier.PERMITTED_PRODUCT_GROUPS, productGroupsPermitted);
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

		// this cannot be null since it's a TreeMap
		while (parentGroupId != null && productGroups.containsKey(parentGroupId)) {
			if (productGroupsPermitted.contains(parentGroupId)) {
				return true;
			} else {
				parentGroupId = productGroups.get(parentGroupId).get("parentGroupId");
			}
		}

		return false;
	}

	private String userPartPD() {
		String userConfigPart = dataServices.cacheManager.getCachedData(CacheIdentifier.USER_CONFIG_PART, String.class);
		if (userConfigPart != null) {
			return userConfigPart;
		}

		if (applyUserSpecializedConfigPD()) {
			userConfigPart = OpsiServiceNOMPersistenceController.KEY_USER_ROOT + ".{"
					+ dataServices.persistenceController.getExecutioner().getHostData().getUser() + "}.";
		} else {
			userConfigPart = UserConfig.KEY_USER_ROLE_ROOT + ".{" + UserConfig.DEFAULT_ROLE_NAME + "}.";
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.USER_CONFIG_PART, userConfigPart);
		Logging.info(this, "userConfigPart initialized, ", userConfigPart);

		return userConfigPart;
	}

	private boolean applyUserSpecializedConfigPD() {
		Boolean applyUserSpecializedConfig = dataServices.cacheManager
				.getCachedData(CacheIdentifier.APPLY_USER_SPECIALIZED_CONFIG, Boolean.class);
		if (applyUserSpecializedConfig != null) {
			return applyUserSpecializedConfig;
		}

		applyUserSpecializedConfig = dataServices.module.isOpsiModuleActive(OpsiModule.USER_ROLES)
				&& hasKeyUserRegisterValuePD();
		dataServices.cacheManager.setCachedData(CacheIdentifier.APPLY_USER_SPECIALIZED_CONFIG,
				applyUserSpecializedConfig);
		Logging.info(this, "applyUserSpecializedConfig initialized, ", applyUserSpecializedConfig);

		return applyUserSpecializedConfig;
	}

	private boolean checkFullPermission(Set<String> permittedEntities, final String keyUseList, final String keyList,
			final Map<String, List<Object>> serverPropertyMap) {
		Logging.info(this, "checkFullPermission  key name,  defaultResult true ", keyUseList);

		boolean fullPermission = true;

		if (serverPropertyMap.get(keyUseList) != null) {
			fullPermission = !(Boolean) (serverPropertyMap.get(keyUseList).get(0));
			// we don't give full permission if the config doesn't exist

			// we didn't configure anything, therefore we revoke the setting
			if (serverPropertyMap.get(keyList) == null) {
				fullPermission = true;
				Logging.info(this, "checkFullPermission not configured keyList ", keyList);
			}
		}

		Logging.info(this, "checkFullPermission  key for list,  fullPermission ", keyList, ", ", fullPermission);

		// we didn't configure anything, therefore we revoke the setting
		if (!fullPermission && serverPropertyMap.get(keyList) != null) {
			for (Object val : serverPropertyMap.get(keyList)) {
				permittedEntities.add((String) val);
			}
		}

		Logging.info(this, "checkFullPermission   result ", fullPermission);
		Logging.info(this, "checkFullPermission   produced list ", permittedEntities);

		return fullPermission;
	}

	private List<Object> computeConfigedGivenDomains(List<Map<String, Object>> readyObjects) {
		Logging.info(this, "checkStandardConfigs: create domain list");

		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");

		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(dataServices.config.getOpsiDefaultDomainPD());

		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(dataServices.config.getOpsiDefaultDomainPD());

		item.put("ident", OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", true);
		item.put("multiValue", true);

		readyObjects.add(item);

		return defaultValues;
	}

	private List<Object> computeHostExtraDisplayfieldsInPanelLicensesReconciliation(
			List<Map<String, Object>> readyObjects) {
		Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ",
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION);
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
		Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ",
				ConfigDataService.KEY_DISABLED_CLIENT_ACTIONS);
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
		Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ",
				ConfigDataService.KEY_OPSICLIENTD_EXTRA_EVENTS);
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

	private void checkRemoteControlConfigs(Map<String, List<Object>> configDefaultValues,
			List<Map<String, Object>> readyObjects) {
		// ping_linux
		String key = RemoteControl.CONFIG_KEY + "." + "ping_linux";
		if (!configDefaultValues.containsKey(key)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ", key);

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
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ", key);

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
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ", key);

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
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ", key);

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

	private boolean checkStandardConfigs() {
		boolean result = dataServices.config.getConfigOptionsPD() != null;
		Logging.info(this, "checkStandardConfigs, already there ", result);

		if (!result) {
			return false;
		}

		List<Map<String, Object>> readyObjects = new ArrayList<>();

		Map<String, List<Object>> configDefaultValues = dataServices.cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);

		// list of domains for new clients
		configDefaultValues.computeIfAbsent(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY,
				arg -> computeConfigedGivenDomains(readyObjects));

		// extra display fields in licencing
		configDefaultValues.computeIfAbsent(
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION,
				arg -> computeHostExtraDisplayfieldsInPanelLicensesReconciliation(readyObjects));

		// remote controls
		checkRemoteControlConfigs(configDefaultValues, readyObjects);

		// configuration of host menus
		configDefaultValues.computeIfAbsent(ConfigDataService.KEY_DISABLED_CLIENT_ACTIONS,
				arg -> computeDisabledClientActions(readyObjects));

		if (!configDefaultValues.containsKey(CONFIGED_WORKBENCH_KEY)) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ",
					CONFIGED_WORKBENCH_KEY);
			readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", CONFIGED_WORKBENCH_KEY,
					dataServices.config.getConfigedWorkbenchDefaultValuePD(), "default path to opsiproducts"));
		} else {
			String workbenchDefaultValue = configDefaultValues.get(CONFIGED_WORKBENCH_KEY).isEmpty()
					? dataServices.config.getConfigedWorkbenchDefaultValuePD()
					: (String) configDefaultValues.get(CONFIGED_WORKBENCH_KEY).get(0);
			Logging.info(this, "checkStandardConfigs set WORKBENCH_defaultvalue to ", workbenchDefaultValue);

			if (configDefaultValues.get(CONFIGED_WORKBENCH_KEY).isEmpty()) {
				readyObjects.add(ConfigDataService.produceConfigEntry("UnicodeConfig", CONFIGED_WORKBENCH_KEY,
						workbenchDefaultValue, "default path to opsiproducts"));
			}

			dataServices.config.setConfigedWorkbenchDefaultValuePD(workbenchDefaultValue);
		}

		// configuration of opsiclientd extra events
		configDefaultValues.computeIfAbsent(ConfigDataService.KEY_OPSICLIENTD_EXTRA_EVENTS,
				arg -> computeOpsiclientdExtraEvents(readyObjects));

		// add metaconfigs

		// Update configs if there are some to update
		if (!readyObjects.isEmpty()) {
			Logging.notice(this, "There are ", readyObjects.size(), "configurations to update, so we do this now:");

			dataServices.exec.doCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, readyObjects);
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

		Logging.info(this, "Obsolete default user configs ", defaultUserConfigsObsolete);

		if (!defaultUserConfigsObsolete.isEmpty()) {
			dataServices.exec.doCall(RPCMethodName.CONFIG_DELETE_OBJECTS, defaultUserConfigsObsolete);
		}
	}

	public boolean hasDepotPermission(String depotId) {
		if (hasDepotsFullPermissionPD()) {
			return true;
		}

		Set<String> depotsPermitted = dataServices.cacheManager.getCachedData(CacheIdentifier.DEPOTS_PERMITTED,
				Set.class);

		return depotsPermitted != null && depotsPermitted.contains(depotId);
	}

	public boolean terminalMenuIsActive() {
		if (dataServices.cacheManager.getCachedData(CacheIdentifier.TERMINAL_MENU_ACTIVE, Boolean.class) == null) {
			checkTerminalPermissions();
		}
		return Boolean.TRUE
				.equals(dataServices.cacheManager.getCachedData(CacheIdentifier.TERMINAL_MENU_ACTIVE, Boolean.class));
	}

	public boolean terminalCommandsIsActive() {
		if (dataServices.cacheManager.getCachedData(CacheIdentifier.TERMINAL_COMMANDS_ACTIVE, Boolean.class) == null) {
			checkTerminalPermissions();
		}
		return Boolean.TRUE.equals(
				dataServices.cacheManager.getCachedData(CacheIdentifier.TERMINAL_COMMANDS_ACTIVE, Boolean.class));
	}

	public boolean terminalCommandControlIsActive() {
		if (dataServices.cacheManager.getCachedData(CacheIdentifier.TERMINAL_COMMAND_CONTROL_ACTIVE,
				Boolean.class) == null) {
			checkTerminalPermissions();
		}
		return Boolean.TRUE.equals(dataServices.cacheManager
				.getCachedData(CacheIdentifier.TERMINAL_COMMAND_CONTROL_ACTIVE, Boolean.class));
	}

	public List<Object> terminalsForbidden() {
		if (dataServices.cacheManager.getCachedData(CacheIdentifier.TERMINAL_FORBIDDEN, List.class) == null) {
			checkTerminalPermissions();
		}
		return dataServices.cacheManager.getCachedData(CacheIdentifier.TERMINAL_FORBIDDEN, List.class);
	}

	public List<Object> getForbiddenMOTD() {
		if (dataServices.cacheManager.getCachedData(CacheIdentifier.MOTD_FORBIDDEN, List.class) == null) {
			checkFeaturesPermissions();
		}
		return dataServices.cacheManager.getCachedData(CacheIdentifier.MOTD_FORBIDDEN, List.class);
	}

	public Set<Object> getPermittedDepots() {
		if (dataServices.cacheManager.getCachedData(CacheIdentifier.DEPOTS_PERMITTED, Set.class) == null) {
			checkDepotPermissions();
		}
		return dataServices.cacheManager.getCachedData(CacheIdentifier.DEPOTS_PERMITTED, Set.class);
	}
}
