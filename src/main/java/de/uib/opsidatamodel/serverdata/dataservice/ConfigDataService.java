/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.json.JSONArray;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.OpsiHwAuditDevicePropertyTypes;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SavedSearch;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.RemoteControls;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.opsidatamodel.permission.UserOpsipermission;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.datapanel.MapTableModel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.table.ListCellOptions;
import utils.Utils;

/**
 * Provides methods for working with configuration data on the server.
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
public class ConfigDataService {
	private static final String DESCRIPTION_KEY = "description";
	private static final String EDITABLE_KEY = "editable";

	private static final String KEY_DISABLED_CLIENT_ACTIONS = "configed.host_actions_disabled";
	private static final String KEY_OPSICLIENTD_EXTRA_EVENTS = "configed.opsiclientd_events";
	private static final String OPSI_CLIENTD_EVENT_SILENT_INSTALL = "silent_install";
	private static final Boolean DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN = false;

	private static final String CONFIGED_WORKBENCH_KEY = "configed.workbench.default";

	private CacheManager cacheManager;
	private AbstractExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private GroupDataService groupDataService;
	private HardwareDataService hardwareDataService;
	private ModuleDataService moduleDataService;
	private HostDataService hostDataService;

	private List<Map<String, Object>> configCollection;
	private List<Map<String, Object>> configStateCollection;
	private List<Map<String, Object>> deleteConfigStateItems;

	public ConfigDataService(AbstractExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setGroupDataService(GroupDataService groupDataService) {
		this.groupDataService = groupDataService;
	}

	public void setHardwareDataService(HardwareDataService hardwareDataService) {
		this.hardwareDataService = hardwareDataService;
	}

	public void setModuleDataService(ModuleDataService moduleDataService) {
		this.moduleDataService = moduleDataService;
	}

	public void setHostDataService(HostDataService hostDataService) {
		this.hostDataService = hostDataService;
	}

	public final void checkConfigurationPD() {
		moduleDataService.retrieveOpsiModules();
		Logging.info(this,
				"checkConfiguration, modules " + cacheManager.getCachedData(CacheIdentifier.OPSI_MODULES, Map.class));

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValuesPD();

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
			keyUserRegisterValue = checkUserRolesModulePD();
			cacheManager.setCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE, keyUserRegisterValue);
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

		HostInfoCollections hostInfoCollections = hostDataService.getHostInfoCollectionsPD();
		List<Object> readyConfigObjects = new UserConfigProducing(applyUserSpecializedConfigPD(),
				hostInfoCollections.getConfigServer(), hostInfoCollections.getDepotNamesList(),
				groupDataService.getHostGroupIds(), groupDataService.getProductGroupsPD().keySet(),
				getConfigDefaultValuesPD(), getConfigListCellOptionsPD()).produce();

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

	private boolean applyUserSpecializedConfigPD() {
		Boolean applyUserSpecializedConfig = cacheManager.getCachedData(CacheIdentifier.APPLY_USER_SPECIALIZED_CONFIG,
				Boolean.class);
		if (applyUserSpecializedConfig != null) {
			return applyUserSpecializedConfig;
		}

		applyUserSpecializedConfig = moduleDataService.isWithUserRolesPD() && hasKeyUserRegisterValuePD();
		Logging.info(this, "applyUserSpecializedConfig initialized, " + applyUserSpecializedConfig);

		return applyUserSpecializedConfig;
	}

	private boolean checkReadOnlyBySystemuser() {
		boolean result = false;

		Logging.info(this, "checkReadOnly");
		if (exec.getBooleanResult(
				new OpsiMethodCall(RPCMethodName.ACCESS_CONTROL_USER_IS_READ_ONLY_USER, new String[] {}))) {
			result = true;
			Logging.info(this, "checkReadOnly " + isGlobalReadOnly());
		}

		return result;
	}

	public final boolean isUserRegisterActivated() {
		boolean result = false;

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValuesPD();
		// dont do anything if we have not got the config
		if (serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER) != null
				&& !serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER).isEmpty()) {
			result = (Boolean) ((List<?>) serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER))
					.get(0);
		}
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
			} else {
				if (locallySavedValueUserRegister != null && locallySavedValueUserRegister) {
					// if true was locally saved but is not the value from service then we ask
					Logging.warning(this, "setAgainUserRegistration, it seems that user check has been deactivated");

					FTextArea dialog = new FTextArea(ConfigedMain.getMainFrame(),
							Configed.getResourceValue("RegisterUserWarning.dialog.title"),

							true,
							new String[] { Configed.getResourceValue("RegisterUserWarning.dialog.button1"),
									Configed.getResourceValue("RegisterUserWarning.dialog.button2"),
									Configed.getResourceValue("RegisterUserWarning.dialog.button3") },
							new Icon[] { Utils.createImageIcon("images/checked_withoutbox_blue14.png", ""),
									Utils.createImageIcon("images/edit-delete.png", ""),
									Utils.createImageIcon("images/executing_command_red_16.png", "") },
							500, 200);
					StringBuilder msg = new StringBuilder(
							Configed.getResourceValue("RegisterUserWarning.dialog.info1"));
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.info2"));
					msg.append("\n");
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option1"));
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option2"));
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option3"));

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
				}
			}
		}

		return resultVal;

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

	private boolean checkStandardConfigs() {
		boolean result = getConfigListCellOptionsPD() != null;
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
			defaultValues.add(getOpsiDefaultDomainPD());

			possibleValues = new ArrayList<>();
			possibleValues.add(getOpsiDefaultDomainPD());

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

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, true,
					"(command may be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// ping_windows
		key = RemoteControl.CONFIG_KEY + "." + "ping_windows";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "cmd.exe /c start ping %host%";
			description = "ping, started in a Windows terminal";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, true,
					"(command may be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, linux
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_linux";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "firefox https://%host%:4441/info.html";
			description = "opsiclientd  timeline, called from a Linux environment, firefox recommended";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, false,
					"(command may not be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, windows
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_windows";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "cmd.exe /c start https://%host%:4441/info.html";
			description = "opsiclientd  timeline, called rfrom a Windows environment";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, false,
					"(command may not be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
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

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, query, description));
			readyObjects
					.add(produceConfigEntry("BoolConfig", key + "." + EDITABLE_KEY, false, "(command may be edited)"));
			// description entry
			readyObjects.add(produceConfigEntry("UnicodeConfig", key + "." + DESCRIPTION_KEY, description, ""));
		}

		// WAN_CONFIGURATION
		// does it exist?

		Map<String, ConfigOption> wanConfigOptions = getWANConfigOptionsPD();
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

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, value, description));

			// description entry
			readyObjects
					.add(produceConfigEntry("UnicodeConfig", key + "." + SavedSearch.DESCRIPTION_KEY, description, ""));
		}

		// configuration of host menus

		key = KEY_DISABLED_CLIENT_ACTIONS;

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_DISABLED_CLIENT_ACTIONS);
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
			readyObjects.add(produceConfigEntry("UnicodeConfig", key,
					OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINUSER_DEFAULT_VALUE,
					"default windows username for deploy-client-agent-script"));
		}

		key = OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINPW;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "
					+ OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINPW);
			readyObjects.add(produceConfigEntry("UnicodeConfig", key,
					OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINPW_DEFAULT_VALUE,
					"default windows password for deploy-client-agent-script"));
		}

		key = CONFIGED_WORKBENCH_KEY;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add(produceConfigEntry("UnicodeConfig", key, getConfigedWorkbenchDefaultValuePD(),
					"default path to opsiproducts"));
		} else {
			Logging.info(this, "checkStandardConfigs set WORKBENCH_defaultvalue to " + (String) defaultValues.get(0));
			setConfigedWorkbenchDefaultValuePD((String) defaultValues.get(0));
		}

		// configuration of opsiclientd extra events

		key = KEY_OPSICLIENTD_EXTRA_EVENTS;

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
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

		List<Map<String, Object>> defaultUserConfigsObsolete = new ArrayList<>();

		// delete obsolete configs

		for (Entry<String, List<Object>> configEntry : configDefaultValues.entrySet()) {
			if (configEntry.getKey().startsWith(OpsiServiceNOMPersistenceController.ALL_USER_KEY_START + "ssh")) {
				defaultValues = configEntry.getValue();

				if (defaultValues != null) {
					// still existing
					Logging.info(this, "handling ssh config key at old location " + configEntry.getKey());
					Map<String, Object> config = new HashMap<>();

					config.put("id", configEntry.getKey());

					String type = "BoolConfig";
					config.put("type", type);

					defaultUserConfigsObsolete.add(config);
				}
			}
		}

		for (Entry<String, List<Object>> configEntry : configDefaultValues.entrySet()) {
			if (configEntry.getKey().startsWith(OpsiServiceNOMPersistenceController.ALL_USER_KEY_START + "{ole.")) {
				defaultValues = configEntry.getValue();

				if (defaultValues != null) {
					// still existing
					Logging.info(this, "removing unwillingly generated entry  " + configEntry.getKey());
					Map<String, Object> config = new HashMap<>();

					config.put("id", configEntry.getKey());

					String type = "BoolConfig";
					config.put("type", type);

					defaultUserConfigsObsolete.add(config);
				}
			}
		}

		Logging.info(this, "defaultUserConfigsObsolete " + defaultUserConfigsObsolete);

		if (!defaultUserConfigsObsolete.isEmpty()) {
			exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS,
					new Object[] { defaultUserConfigsObsolete }));
		}

		return true;
	}

	/**
	 * delivers the default domain if it is not existing it retrieves it from
	 * servide
	 */
	public String getOpsiDefaultDomainPD() {
		retrieveOpsiDefaultDomainPD();
		return cacheManager.getCachedData(CacheIdentifier.OPSI_DEFAULT_DOMAIN, String.class);
	}

	/**
	 * retrieves default domain from service
	 */
	public void retrieveOpsiDefaultDomainPD() {
		if (cacheManager.getCachedData(CacheIdentifier.OPSI_DEFAULT_DOMAIN, String.class) != null) {
			return;
		}
		Object[] params = new Object[] {};
		String opsiDefaultDomain = exec.getStringResult(new OpsiMethodCall(RPCMethodName.GET_DOMAIN, params));
		cacheManager.setCachedData(CacheIdentifier.OPSI_DEFAULT_DOMAIN, opsiDefaultDomain);
	}

	private List<Map<String, Object>> buildWANConfigOptions(List<Map<String, Object>> readyObjects) {
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

	public Set<String> getDepotsPermittedPD() {
		return cacheManager.getCachedData(CacheIdentifier.DEPOTS_PERMITTED, Set.class);
	}

	private void checkPermissions() {
		UserOpsipermission.ActionPrivilege serverActionPermission;

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValuesPD();

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

		// false, //not only as specified but always
		boolean hostgroupsOnlyIfExplicitlyStated = checkFullPermission(hostgroupsPermitted, configKeyUseList,
				configKeyList, serverPropertyMap);

		if (hostgroupsOnlyIfExplicitlyStated) {
			hostgroupsPermitted = null;
		}
		cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED,
				hostgroupsOnlyIfExplicitlyStated);

		Logging.info(this, "checkPermissions hostgroupsPermitted " + hostgroupsPermitted);

		configKeyUseList = userPartPD()
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPartPD() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE;
		Set<String> productgroupsPermitted = new HashSet<>();

		boolean productgroupsFullPermission = checkFullPermission(productgroupsPermitted, configKeyUseList,
				configKeyList, serverPropertyMap);
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_GROUPS_FULL_PERMISSION, productgroupsFullPermission);

		Set<String> permittedProducts = null;

		if (!productgroupsFullPermission) {
			permittedProducts = new TreeSet<>();

			for (String group : productgroupsPermitted) {
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

	public Set<String> getPermittedProductsPD() {
		return cacheManager.getCachedData(CacheIdentifier.PERMITTED_PRODUCTS, Set.class);
	}

	private String userPartPD() {
		String userConfigPart = cacheManager.getCachedData(CacheIdentifier.USER_CONFIG_PART, String.class);
		if (userConfigPart != null) {
			return userConfigPart;
		}

		if (applyUserSpecializedConfigPD()) {
			userConfigPart = OpsiServiceNOMPersistenceController.KEY_USER_ROOT + ".{"
					+ cacheManager.getCachedData(CacheIdentifier.USER, String.class) + "}.";
		} else {
			userConfigPart = UserConfig.KEY_USER_ROLE_ROOT + ".{" + UserConfig.DEFAULT_ROLE_NAME + "}.";
		}

		cacheManager.setCachedData(CacheIdentifier.USER_CONFIG_PART, userConfigPart);
		Logging.info(this, "userConfigPart initialized, " + userConfigPart);

		return userConfigPart;
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

	public Map<String, List<Object>> getConfigDefaultValuesPD() {
		retrieveConfigOptionsPD();
		return cacheManager.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
	}

	public Map<String, RemoteControl> getRemoteControlsPD() {
		retrieveConfigOptionsPD();
		return cacheManager.getCachedData(CacheIdentifier.REMOTE_CONTROLS, Map.class);
	}

	public SavedSearches getSavedSearchesPD() {
		retrieveConfigOptionsPD();
		return cacheManager.getCachedData(CacheIdentifier.SAVED_SEARCHES, SavedSearches.class);
	}

	public Map<String, ConfigOption> getConfigOptionsPD() {
		retrieveConfigOptionsPD();
		return cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);
	}

	public Map<String, ListCellOptions> getConfigListCellOptionsPD() {
		retrieveConfigOptionsPD();
		return cacheManager.getCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS, Map.class);
	}

	public void retrieveConfigOptionsPD() {
		if (cacheManager.getCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS, Map.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class) != null) {
			return;
		}

		Logging.debug(this, "getConfigOptions() work");

		List<Map<String, Object>> deleteItems = new ArrayList<>();

		boolean tryIt = true;

		int tryOnceMoreCounter = 0;
		final int STOP_REPEATING_AT_THIS = 1;

		while (tryIt) {
			tryIt = false;
			tryOnceMoreCounter++;

			Map<String, ConfigOption> configOptions = new HashMap<>();
			Map<String, ListCellOptions> configListCellOptions = new HashMap<>();
			Map<String, List<Object>> configDefaultValues = new HashMap<>();

			RemoteControls remoteControls = new RemoteControls();
			SavedSearches savedSearches = new SavedSearches();

			OpsiHwAuditDevicePropertyTypes hwAuditDevicePropertyTypes = new OpsiHwAuditDevicePropertyTypes(
					hardwareDataService.getHwAuditDeviceClassesPD());

			// metaConfig for wan configuration is rebuilt in
			// getWANConfigOptions

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_GET_OBJECTS, new Object[0]);
			List<Map<String, Object>> retrievedList = exec.getListOfMaps(omc);

			Logging.info(this, "configOptions retrieved ");

			for (Map<String, Object> configItem : retrievedList) {
				// map to java type
				for (Entry<String, Object> configItemEntry : configItem.entrySet()) {
					if (configItemEntry.getValue() instanceof JSONArray) {
						configItem.put(configItemEntry.getKey(), ((JSONArray) configItemEntry.getValue()).toList());
					}
				}

				String key = (String) configItem.get("ident");

				// build a ConfigOption from the retrieved item

				// eliminate key produced by old version for role branch

				String pseudouserProducedByOldVersion = OpsiServiceNOMPersistenceController.KEY_USER_ROOT + ".{"
						+ UserConfig.ROLE.substring(1, UserConfig.ROLE.length());
				//

				if (key != null && key.startsWith(pseudouserProducedByOldVersion)) {
					Logging.warning(this, "user entry " + key
							+ " produced by a still somewhere running old configed version , please delete user entry "
							+ pseudouserProducedByOldVersion);

					deleteItems.add(configItem);

					Logging.info(this, "deleteItem " + configItem);

					continue;
				}

				ConfigOption configOption = new ConfigOption(configItem);

				configOptions.put(key, configOption);

				configListCellOptions.put(key, configOption);

				if (configOption.getDefaultValues() == null) {
					Logging.warning(this, "default values missing for config  " + key);

					if (tryOnceMoreCounter <= STOP_REPEATING_AT_THIS) {
						tryIt = true;
						Logging.warning(this,
								"repeat loading the values , we repeated  " + tryOnceMoreCounter + " times");

						Utils.threadSleep(this, 1000);
						break;
					}
				}

				configDefaultValues.put(key, configOption.getDefaultValues());

				if (configOption.getDefaultValues() != null && !configOption.getDefaultValues().isEmpty()) {
					remoteControls.checkIn(key, "" + configOption.getDefaultValues().get(0));
					savedSearches.checkIn(key, "" + configOption.getDefaultValues().get(0));
					hwAuditDevicePropertyTypes.checkIn(key, configOption.getDefaultValues());
				}
			}

			cacheManager.setCachedData(CacheIdentifier.REMOTE_CONTROLS, remoteControls);
			cacheManager.setCachedData(CacheIdentifier.SAVED_SEARCHES, savedSearches);
			cacheManager.setCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS, configListCellOptions);
			cacheManager.setCachedData(CacheIdentifier.CONFIG_OPTIONS, configOptions);
			cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);

			Logging.debug(this, " getConfigOptions produced hwAuditDevicePropertyTypes " + hwAuditDevicePropertyTypes);
		}

		Logging.info(this, "{ole deleteItems " + deleteItems.size());

		if (!deleteItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS,
					new Object[] { deleteItems.toArray() });

			if (exec.doCall(omc)) {
				deleteItems.clear();
			}
		}

		getWANConfigOptionsPD();
		Logging.debug(this, "getConfigOptions() work finished");

		return;
	}

	public Map<String, List<Object>> getWanConfigurationPD() {
		return cacheManager.getCachedData(CacheIdentifier.WAN_CONFIGURATION, Map.class);
	}

	public Map<String, List<Object>> getNotWanConfigurationPD() {
		return cacheManager.getCachedData(CacheIdentifier.NOT_WAN_CONFIGURATION, Map.class);
	}

	public Map<String, ConfigOption> getWANConfigOptionsPD() {
		Map<String, ConfigOption> allWanConfigOptions = extractSubConfigOptionsByInitial(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "." + OpsiServiceNOMPersistenceController.WAN_PARTKEY);

		Logging.info(this, " getWANConfigOptions   " + allWanConfigOptions);

		Map<String, ConfigOption> notWanConfigOptions = extractSubConfigOptionsByInitial(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "."
						+ OpsiServiceNOMPersistenceController.NOT_WAN_CONFIGURED_PARTKEY + ".");

		Map<String, List<Object>> notWanConfiguration = new HashMap<>();
		Map<String, List<Object>> wanConfiguration = new HashMap<>();

		List<Object> values = null;

		for (Entry<String, ConfigOption> notWanConfigOption : notWanConfigOptions.entrySet()) {
			if (notWanConfigOption.getValue().getType() != ConfigOption.TYPE.BOOL_CONFIG) {
				notWanConfiguration.put(notWanConfigOption.getKey(), null);
				wanConfiguration.put(notWanConfigOption.getKey(), null);
			} else {
				Boolean b = (Boolean) notWanConfigOption.getValue().getDefaultValues().get(0);

				values = new ArrayList<>();
				values.add(b);
				notWanConfiguration.put(notWanConfigOption.getKey(), values);

				values = new ArrayList<>();
				values.add(!b);
				wanConfiguration.put(notWanConfigOption.getKey(), values);
			}
		}

		cacheManager.setCachedData(CacheIdentifier.WAN_CONFIGURATION, wanConfiguration);
		Logging.info(this, "getWANConfigOptions wanConfiguration " + wanConfiguration);
		cacheManager.setCachedData(CacheIdentifier.NOT_WAN_CONFIGURATION, notWanConfiguration);
		Logging.info(this, "getWANConfigOptions notWanConfiguration  " + notWanConfiguration);

		return allWanConfigOptions;
	}

	private Map<String, ConfigOption> extractSubConfigOptionsByInitial(final String s) {
		HashMap<String, ConfigOption> result = new HashMap<>();
		retrieveConfigOptionsPD();
		Map<String, ConfigOption> configOptions = cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);
		for (Entry<String, ConfigOption> configOption : configOptions.entrySet()) {
			if (configOption.getKey().startsWith(s) && configOption.getKey().length() > s.length()) {
				String xKey = configOption.getKey().substring(s.length());
				result.put(xKey, configOption.getValue());
			}
		}

		return result;
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

	public boolean isAccessToHostgroupsOnlyIfExplicitlyStatedPD() {
		return Utils.toBoolean(
				cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED, Boolean.class));
	}

	public Set<String> getHostgroupsPermitted() {
		Set<String> result = null;
		if (!isAccessToHostgroupsOnlyIfExplicitlyStatedPD()) {
			result = cacheManager.getCachedData(CacheIdentifier.DEPOTS_PERMITTED, Set.class);
		}

		Logging.info(this, "getHostgroupsPermitted " + result);

		return result;
	}

	public Map<String, Map<String, Object>> getConfigsPD() {
		retrieveHostConfigsPD();
		return cacheManager.getCachedData(CacheIdentifier.HOST_CONFIGS, Map.class);
	}

	public void retrieveHostConfigsPD() {
		Map<String, Map<String, Object>> hostConfigs = cacheManager.getCachedData(CacheIdentifier.HOST_CONFIGS,
				Map.class);
		if (hostConfigs != null) {
			return;
		}

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();

		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
		hostConfigs = new HashMap<>();

		for (Map<String, Object> listElement : retrieved) {
			Object id = listElement.get("objectId");

			if (id instanceof String && !"".equals(id)) {
				String hostId = (String) id;
				Map<String, Object> configs1Host = hostConfigs.computeIfAbsent(hostId, arg -> new HashMap<>());

				Logging.debug(this, "retrieveHostConfigs objectId,  element " + id + ": " + listElement);

				String configId = (String) listElement.get("configId");

				if (listElement.get("values") == null) {
					configs1Host.put(configId, new ArrayList<>());
					// is a data error but can occur
				} else {
					configs1Host.put(configId, listElement.get("values"));
				}
			}
		}

		timeCheck.stop();
		Logging.info(this, "retrieveHostConfigs retrieved " + hostConfigs.keySet());

		cacheManager.setCachedData(CacheIdentifier.HOST_CONFIGS, hostConfigs);
		persistenceController.notifyPanelCompleteWinProducts();
	}

	public List<Map<String, Object>> addWANConfigStates(String clientId, boolean wan,
			List<Map<String, Object>> jsonObjects) {
		getWANConfigOptionsPD();

		Map<String, List<Object>> wanConfiguration = getWanConfigurationPD();
		Map<String, List<Object>> notWanConfiguration = getNotWanConfigurationPD();

		Logging.debug(this,
				"addWANConfigState  wanConfiguration " + wanConfiguration + "\n " + wanConfiguration.size());
		Logging.debug(this, "addWANConfigState  wanConfiguration.keySet() " + wanConfiguration.keySet() + "\n "
				+ wanConfiguration.keySet().size());

		Logging.debug(this,
				"addWANConfigState  notWanConfiguration " + notWanConfiguration + "\n " + notWanConfiguration.size());
		Logging.debug(this, "addWANConfigState  notWanConfiguration.keySet() " + notWanConfiguration.keySet() + "\n "
				+ notWanConfiguration.keySet().size());

		setConfig(notWanConfiguration);
		Logging.info(this, "set notWanConfiguration members where no entry exists");
		// send to opsiserver only new configs
		setConfig(true);

		Map<String, List<Object>> specifiedConfiguration;

		if (wan) {
			specifiedConfiguration = wanConfiguration;
		} else {
			specifiedConfiguration = notWanConfiguration;
		}

		if (jsonObjects == null) {
			jsonObjects = new ArrayList<>();
		}

		for (Entry<String, List<Object>> config : specifiedConfiguration.entrySet()) {
			Logging.info(this, "addWANConfigState configId " + config.getKey());
			Map<String, Object> item = Utils.createNOMitem(OpsiServiceNOMPersistenceController.CONFIG_STATE_TYPE);

			item.put(OpsiServiceNOMPersistenceController.CONFIG_ID, config.getKey());

			Logging.info(this, "addWANConfigState values " + config.getValue());

			item.put(OpsiServiceNOMPersistenceController.VALUES_ID, config.getValue());

			item.put(OpsiServiceNOMPersistenceController.OBJECT_ID, clientId);

			Logging.info(this, "addWANConfigState configId, item " + config.getKey() + ", " + item);

			// locally, hopefully the RPC call will work
			if (getConfigsPD().get(clientId) == null) {
				Logging.info(this, "addWANConfigState; until now, no config(State) existed for client " + clientId
						+ " no local update");
				getConfigsPD().put(clientId, new HashMap<>());
			}

			getConfigsPD().get(clientId).put(config.getKey(), config.getValue());

			// prepare for JSON RPC
			jsonObjects.add(item);
		}

		return jsonObjects;
	}

	public boolean setWANConfigs(String clientId, boolean wan) {
		boolean result = false;
		Logging.info(this, "setWANConfigs " + clientId + " . " + wan);

		List<Map<String, Object>> jsonObjects = addWANConfigStates(clientId, wan, null);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
				new Object[] { jsonObjects });
		result = exec.doCall(omc);

		return result;
	}

	// send config updates and clear the collection
	public void setConfig() {
		setConfig(false);
	}

	// send config updates, possibly not updating existing

	private void setConfig(boolean restrictToMissing) {
		Logging.info(this, "setConfig(),  configCollection null " + (configCollection == null));
		if (configCollection != null) {
			Logging.info(this, "setConfig(),  configCollection size  " + configCollection.size());
		}

		if (isGlobalReadOnly()) {
			return;
		}

		if (configCollection != null && !configCollection.isEmpty()) {
			// add configId where necessary
			List<String> usedConfigIds = new ArrayList<>();
			Map<String, String> typesOfUsedConfigIds = new HashMap<>();
			for (Map<String, Object> config : configCollection) {
				String ident = (String) config.get("ident");
				usedConfigIds.add(ident);
				typesOfUsedConfigIds.put(ident, (String) config.get("type"));
			}

			Logging.debug(this, "setConfig(), usedConfigIds: " + usedConfigIds);

			List<Object> existingConfigIds = exec
					.getListResult(new OpsiMethodCall(RPCMethodName.CONFIG_GET_IDENTS, new Object[] {}));

			Logging.info(this, "setConfig(), existingConfigIds: " + existingConfigIds.size());

			List<String> missingConfigIds = new ArrayList<>(usedConfigIds);
			for (Object configId : existingConfigIds) {
				missingConfigIds.remove(configId);
			}
			Logging.info(this, "setConfig(), missingConfigIds: " + missingConfigIds);
			List<Map<String, Object>> createItems = new ArrayList<>();
			for (String missingId : missingConfigIds) {
				Map<String, Object> item = Utils.createNOMitem(typesOfUsedConfigIds.get(missingId));
				item.put("ident", missingId);
				createItems.add(item);
			}

			if (!createItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_CREATE_OBJECTS,
						new Object[] { createItems.toArray() });
				exec.doCall(omc);
			}

			// remap to JSON types
			List<Map<String, Object>> callsConfigUpdateCollection = new ArrayList<>();
			List<Map<String, Object>> callsConfigDeleteCollection = new ArrayList<>();

			for (Map<String, Object> callConfig : configCollection) {

				if (callConfig.get("defaultValues") == MapTableModel.nullLIST) {
					callsConfigDeleteCollection.add(callConfig);
				} else {
					Logging.debug(this, "setConfig config with ident " + callConfig.get("ident"));

					boolean isMissing = missingConfigIds.contains(callConfig.get("ident"));

					if (!restrictToMissing || isMissing) {
						callConfig.put("defaultValues", callConfig.get("defaultValues"));
						callConfig.put("possibleValues", callConfig.get("possibleValues"));
						callsConfigUpdateCollection.add(callConfig);
					}
				}
			}

			Logging.debug(this, "setConfig() callsConfigUpdateCollection " + callsConfigUpdateCollection);

			if (!callsConfigDeleteCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS,
						new Object[] { callsConfigDeleteCollection }));
				// configOptionsRequestRefresh();
				// because of referential integrity
				// hostConfigsRequestRefresh();
			}

			Logging.debug(this, "setConfig() callsConfigUpdateCollection " + callsConfigUpdateCollection);

			if (!callsConfigUpdateCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS,
						new Object[] { callsConfigUpdateCollection }));
				// configOptionsRequestRefresh();
			}

			retrieveConfigOptionsPD();
			configCollection.clear();

			Logging.info(this, "setConfig(),  configCollection result: " + configCollection);
		}
	}

	// collect config updates
	public void setConfig(Map<String, List<Object>> settings) {
		Logging.debug(this, "setConfig settings " + settings);
		if (configCollection == null) {
			configCollection = new ArrayList<>();
		}

		Map<String, ConfigOption> configOptions = cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);

		for (Entry<String, List<Object>> setting : settings.entrySet()) {
			Logging.debug(this, "setConfig,  key, settings.get(key): " + setting.getKey() + ", " + setting.getValue());

			if (setting.getValue() != null) {
				Logging.debug(this, "setConfig,  settings.get(key), settings.get(key).getClass().getName(): "
						+ setting.getValue() + " , " + setting.getValue().getClass().getName());

				if (setting.getValue() instanceof List) {
					List<Object> oldValue = null;

					if (configOptions.get(setting.getKey()) != null) {
						oldValue = configOptions.get(setting.getKey()).getDefaultValues();
					}

					Logging.info(this, "setConfig, key, oldValue: " + setting.getKey() + ", " + oldValue);

					List<Object> valueList = setting.getValue();

					if (valueList != null && !valueList.equals(oldValue)) {
						Map<String, Object> config = new HashMap<>();

						config.put("ident", setting.getKey());

						String type = "UnicodeConfig";

						Logging.debug(this, "setConfig, key,  configOptions.get(key):  " + setting.getKey() + ", "
								+ configOptions.get(setting.getKey()));
						if (configOptions.get(setting.getKey()) != null) {
							type = (String) configOptions.get(setting.getKey()).get("type");
						} else {
							if (!valueList.isEmpty() && valueList.get(0) instanceof Boolean) {
								type = "BoolConfig";
							}
						}

						config.put("type", type);

						config.put("defaultValues", valueList);

						List<Object> possibleValues = null;
						if (configOptions.get(setting.getKey()) == null) {
							possibleValues = new ArrayList<>();
							if (type.equals(ConfigOption.BOOL_TYPE)) {
								possibleValues.add(true);
								possibleValues.add(false);
							}
						} else {
							possibleValues = configOptions.get(setting.getKey()).getPossibleValues();
						}

						for (Object item : valueList) {
							if (possibleValues.indexOf(item) == -1) {
								possibleValues.add(item);
							}
						}

						config.put("possibleValues", possibleValues);

						configCollection.add(config);
					}
				} else {
					Logging.error("setConfig,  setting.getKey(), setting.getValue(): " + setting.getKey() + ", "
							+ setting.getValue() + " \nUnexpected type");
				}
			}
		}
	}

	public void addRoleConfig(String name, String rolename) {
		String configkey = UserConfig.KEY_USER_ROLE_ROOT + ".{" + name + "}." + UserConfig.HAS_ROLE_ATTRIBUT;
		addRoleAndUserConfig(configkey, rolename);
	}

	public void addUserConfig(String name, String rolename) {
		String configkey = UserConfig.START_USER_KEY + name + "}." + UserConfig.HAS_ROLE_ATTRIBUT;
		addRoleAndUserConfig(configkey, rolename);
	}

	private void addRoleAndUserConfig(String configkey, String rolename) {
		List<Map<String, Object>> readyObjects = new ArrayList<>();
		String role = rolename;

		if (role == null) {
			role = UserConfig.NONE_PROTOTYPE;
		}

		List<Object> selectedValuesRole = new ArrayList<>();
		selectedValuesRole.add(role);

		Map<String, Object> itemRole = Utils.createNOMConfig(ConfigOption.TYPE.UNICODE_CONFIG, configkey,
				"which role should determine this configuration", false, false, selectedValuesRole, selectedValuesRole);

		readyObjects.add(itemRole);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

		exec.doCall(omc);

		Map<String, List<Object>> configDefaultValues = getConfigDefaultValuesPD();
		configDefaultValues.put(configkey, selectedValuesRole);
		cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);
	}

	public void deleteSavedSearch(String name) {
		Logging.debug(this, "deleteSavedSearch " + name);
		SavedSearches savedSearches = cacheManager.getCachedData(CacheIdentifier.SAVED_SEARCHES, SavedSearches.class);

		List<Map<String, Object>> readyObjects = new ArrayList<>();
		Map<String, Object> item;

		item = Utils.createNOMitem("UnicodeConfig");
		item.put("id", SavedSearch.CONFIG_KEY + "." + name);
		readyObjects.add(item);

		item = Utils.createNOMitem("UnicodeConfig");
		item.put("id", SavedSearch.CONFIG_KEY + "." + name + "." + SavedSearch.DESCRIPTION_KEY);
		readyObjects.add(item);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS, new Object[] { readyObjects });

		exec.doCall(omc);
		savedSearches.remove(name);
		cacheManager.setCachedData(CacheIdentifier.SAVED_SEARCHES, savedSearches);
	}

	public void saveSearch(SavedSearch ob) {
		Logging.debug(this, "saveSearch " + ob);

		List<Object> readyObjects = new ArrayList<>();
		// entry of serialization string
		readyObjects.add(produceConfigEntry("UnicodeConfig", SavedSearch.CONFIG_KEY + "." + ob.getName(),
				ob.getSerialization(), ob.getDescription(), false));
		// description entry
		readyObjects.add(produceConfigEntry("UnicodeConfig",
				SavedSearch.CONFIG_KEY + "." + ob.getName() + "." + SavedSearch.DESCRIPTION_KEY, ob.getDescription(),
				"", true));

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

		exec.doCall(omc);
	}

	private Map<String, Object> produceConfigEntry(String nomType, String key, Object value, String description) {
		return produceConfigEntry(nomType, key, value, description, true);
	}

	private Map<String, Object> produceConfigEntry(String nomType, String key, Object value, String description,
			boolean editable) {
		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(value);

		// defaultValues
		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(value);

		// create config for service
		Map<String, Object> item;

		item = Utils.createNOMitem(nomType);
		item.put("ident", key);
		item.put("description", description);
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", editable);
		item.put("multiValue", false);

		return item;
	}

	public List<String> getDisabledClientMenuEntries() {
		if (cacheManager.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class) == null) {
			retrieveConfigOptionsPD();
		}
		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		return Utils.takeAsStringList(configDefaultValues.get(KEY_DISABLED_CLIENT_ACTIONS));
	}

	public List<String> getOpsiclientdExtraEvents() {
		Logging.debug(this, "getOpsiclientdExtraEvents");

		if (cacheManager.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class) == null) {
			retrieveConfigOptionsPD();
		}

		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		if (configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS) == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
		}

		List<String> result = Utils.takeAsStringList(configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS));
		Logging.debug(this, "getOpsiclientdExtraEvents() " + result);
		return result;
	}

	public Map<String, Object> getConfig(String objectId) {
		retrieveConfigOptionsPD();
		Map<String, Object> retrieved = getConfigsPD().get(objectId);
		Map<String, ConfigOption> configOptions = cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);
		return new ConfigName2ConfigValue(retrieved, configOptions);
	}

	// collect config state updates
	public void setAdditionalConfiguration(String objectId, ConfigName2ConfigValue settings) {
		if (configStateCollection == null) {
			configStateCollection = new ArrayList<>();
		}

		Set<String> currentKeys = settings.keySet();
		Logging.info(this, "setAdditionalConfigurations current keySet size: " + currentKeys.size());
		if (settings.getRetrieved() != null) {
			Set<String> retrievedKeys = settings.getRetrieved().keySet();

			Logging.info(this, "setAdditionalConfigurations retrieved keys size  " + retrievedKeys.size());

			Set<String> removedKeys = new HashSet<>(retrievedKeys);
			removedKeys.removeAll(currentKeys);
			Logging.info(this, "setAdditionalConfigurations removed " + removedKeys);

			if (!removedKeys.isEmpty()) {
				if (deleteConfigStateItems == null) {
					deleteConfigStateItems = new ArrayList<>();
				}

				for (Object key : removedKeys) {
					String ident = "" + key + ";" + objectId;

					Map<String, Object> item = Utils.createNOMitem("ConfigState");
					item.put("ident", ident);
					deleteConfigStateItems.add(item);
				}
			}
		}

		for (Map.Entry<String, Object> entry : settings.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			Map<String, Object> state = new HashMap<>();

			state.put("type", "ConfigState");
			state.put("objectId", objectId);
			state.put("configId", key);
			state.put("values", value);

			Map<String, Object> retrievedConfig = settings.getRetrieved();
			Object oldValue = null;

			if (retrievedConfig != null) {
				oldValue = retrievedConfig.get(key);
			}

			if (value != oldValue) {
				configStateCollection.add(state);

				// we hope that the update works and directly update the retrievedConfig
				if (retrievedConfig != null) {
					retrievedConfig.put(key, value);
				}
			}
		}
	}

	// send config updates and clear the collection
	public void setAdditionalConfiguration() {
		if (isGlobalReadOnly()) {
			return;
		}

		if (Utils.checkCollection(this, "configStateCollection", configStateCollection)
				&& !configStateCollection.isEmpty()) {
			boolean configsChanged = false;

			if (deleteConfigStateItems == null) {
				deleteConfigStateItems = new ArrayList<>();
			}

			// add configId where necessary
			Set<String> usedConfigIds = new HashSet<>();
			Map<String, String> typesOfUsedConfigIds = new HashMap<>();

			List<Object> doneList = new ArrayList<>();

			for (Map<String, Object> configState : configStateCollection) {
				String ident = (String) configState.get("configId");
				usedConfigIds.add(ident);

				List<?> valueList = (List<?>) configState.get("values");

				if (!valueList.isEmpty() && valueList.get(0) instanceof Boolean) {
					typesOfUsedConfigIds.put(ident, "BoolConfig");
				} else {
					typesOfUsedConfigIds.put(ident, "UnicodeConfig");
				}

				if (valueList.equals(MapTableModel.nullLIST)) {
					Map<String, Object> item = Utils.createNOMitem("ConfigState");
					item.put("objectId", configState.get("objectId"));
					item.put("configId", configState.get("configId"));

					deleteConfigStateItems.add(item);

					doneList.add(configState);
				}
			}
			Logging.debug(this, "setAdditionalConfiguration(), usedConfigIds: " + usedConfigIds);

			Logging.debug(this, "setAdditionalConfiguration(), deleteConfigStateItems  " + deleteConfigStateItems);
			// not used
			if (!deleteConfigStateItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_DELETE_OBJECTS,
						new Object[] { deleteConfigStateItems.toArray() });

				if (exec.doCall(omc)) {
					deleteConfigStateItems.clear();
					configStateCollection.removeAll(doneList);
				}
			}

			List<Object> existingConfigIds = exec
					.getListResult(new OpsiMethodCall(RPCMethodName.CONFIG_GET_IDENTS, new Object[] {}));
			Logging.debug(this, "setAdditionalConfiguration(), existingConfigIds: " + existingConfigIds.size());

			Set<String> missingConfigIds = new HashSet<>(usedConfigIds);
			missingConfigIds.removeAll(existingConfigIds);

			Logging.debug(this, "setAdditionalConfiguration(), missingConfigIds: " + missingConfigIds);
			List<Map<String, Object>> createItems = new ArrayList<>();
			for (String missingId : missingConfigIds) {
				Map<String, Object> item = Utils.createNOMitem(typesOfUsedConfigIds.get(missingId));
				item.put("ident", missingId);
				createItems.add(item);
			}

			if (!createItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_CREATE_OBJECTS,
						new Object[] { createItems.toArray() });
				exec.doCall(omc);
				configsChanged = true;
			}

			if (configsChanged) {
				// configOptionsRequestRefresh();
				retrieveConfigOptionsPD();
			}

			// build calls

			List<Map<String, Object>> callsConfigName2ConfigValueCollection = new ArrayList<>();
			List<Map<String, Object>> callsConfigCollection = new ArrayList<>();

			for (Map<String, Object> state : configStateCollection) {

				state.put("values", state.get("values"));
				callsConfigName2ConfigValueCollection.add(state);
			}

			Logging.debug(this, "callsConfigCollection " + callsConfigCollection);
			if (!callsConfigCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS,
						new Object[] { callsConfigCollection }));
			}

			// do call

			// now we can set the values and clear the collected update items
			exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
					new Object[] { callsConfigName2ConfigValueCollection }));

			// at any rate:
			configStateCollection.clear();
		}
	}

	public Boolean isInstallByShutdownConfigured(String host) {
		return getHostBooleanConfigValue(OpsiServiceNOMPersistenceController.KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN, host,
				true, false);
	}

	public Boolean isWanConfigured(String host) {
		Map<String, List<Object>> wanConfiguration = getWanConfigurationPD();
		Logging.info(this, " isWanConfigured wanConfiguration  " + wanConfiguration + " for host " + host);
		return findBooleanConfigurationComparingToDefaults(host, wanConfiguration);
	}

	public Boolean isUefiConfigured(String hostname) {
		Boolean result = false;

		if (getConfigsPD().get(hostname) != null
				&& getConfigsPD().get(hostname).get(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME) != null
				&& !((List<?>) getConfigsPD().get(hostname)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME)).isEmpty()) {
			String configValue = (String) ((List<?>) getConfigsPD().get(hostname)
					.get(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME)).get(0);

			if (configValue.indexOf(OpsiServiceNOMPersistenceController.EFI_STRING) >= 0) {
				// something similar should work, but not this:

				result = true;
			}
		} else if (getConfigDefaultValuesPD().get(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME) != null
				&& !((List<?>) getConfigDefaultValuesPD()
						.get(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME)).isEmpty()) {
			String configValue = (String) ((List<?>) getConfigDefaultValuesPD()
					.get(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME)).get(0);

			if (configValue.indexOf(OpsiServiceNOMPersistenceController.EFI_STRING) >= 0) {
				// something similar should work, but not this:
				result = true;
			}
		} else {
			// No UEFI configuration
		}

		return result;
	}

	private Boolean valueFromConfigStateAsExpected(Map<String, Object> configs, String configKey, boolean expectValue) {
		Logging.debug(this, "valueFromConfigStateAsExpected configKey " + configKey);
		boolean result = false;

		if (configs != null && configs.get(configKey) != null && !((List<?>) (configs.get(configKey))).isEmpty()) {
			Logging.debug(this, "valueFromConfigStateAsExpected configKey, values " + configKey + ", valueList "
					+ configs.get(configKey) + " expected " + expectValue);

			Object value = ((List<?>) configs.get(configKey)).get(0);

			if (value instanceof Boolean) {
				if (((Boolean) value).equals(expectValue)) {
					result = true;
				}
			} else if (value instanceof String) {
				if (((String) value).equalsIgnoreCase("" + expectValue)) {
					result = true;
				}
			} else {
				Logging.error(this, "it is not a boolean and not a string, how to handle it ? " + " value " + value);
			}

			Logging.debug(this, "valueFromConfigStateAsExpected " + result);

		}
		return result;
	}

	public boolean configureInstallByShutdown(String clientId, boolean shutdownInstall) {
		return setHostBooleanConfigValue(OpsiServiceNOMPersistenceController.KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN,
				clientId, shutdownInstall);
	}

	// for checking if WAN default configuration is set
	public boolean findBooleanConfigurationComparingToDefaults(String host,
			Map<String, List<Object>> defaultConfiguration) {
		boolean tested = false;
		for (Entry<String, List<Object>> configuration : defaultConfiguration.entrySet()) {
			tested = valueFromConfigStateAsExpected(getConfig(host), configuration.getKey(),
					(Boolean) (configuration.getValue().get(0)));
			if (!tested) {
				break;
			}
		}

		return tested;
	}

	public boolean configureUefiBoot(String clientId, boolean uefiBoot) {
		boolean result = false;

		Logging.info(this, "configureUefiBoot, clientId " + clientId + " " + uefiBoot);

		List<String> values = new ArrayList<>();

		if (uefiBoot) {
			values.add(OpsiServiceNOMPersistenceController.EFI_DHCPD_FILENAME);

			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			jsonObjects.add(Utils.createUefiNOMEntry(clientId, OpsiServiceNOMPersistenceController.EFI_DHCPD_FILENAME));

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
					new Object[] { jsonObjects });
			result = exec.doCall(omc);
		} else {
			values.add(OpsiServiceNOMPersistenceController.EFI_DHCPD_NOT);

			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			jsonObjects.add(Utils.createUefiNOMEntry(clientId, OpsiServiceNOMPersistenceController.EFI_DHCPD_NOT));

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
					new Object[] { jsonObjects });
			result = exec.doCall(omc);
		}

		// locally
		if (result) {
			if (getConfigsPD().get(clientId) == null) {
				getConfigsPD().put(clientId, new HashMap<>());
			}

			Logging.info(this,
					"configureUefiBoot, configs for clientId " + clientId + " " + getConfigsPD().get(clientId));
			getConfigsPD().get(clientId).put(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME, values);
		}

		return result;
	}

	private boolean setHostBooleanConfigValue(String configId, String hostName, boolean val) {
		Logging.info(this, "setHostBooleanConfigValue " + hostName + " configId " + configId + " val " + val);

		List<Object> values = new ArrayList<>();
		values.add(val);

		Map<String, Object> item = Utils.createNOMitem(OpsiServiceNOMPersistenceController.CONFIG_STATE_TYPE);
		item.put(OpsiServiceNOMPersistenceController.OBJECT_ID, hostName);
		item.put(OpsiServiceNOMPersistenceController.VALUES_ID, values);
		item.put(OpsiServiceNOMPersistenceController.CONFIG_ID, configId);

		List<Map<String, Object>> jsonObjects = new ArrayList<>();
		jsonObjects.add(item);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
				new Object[] { jsonObjects });

		return exec.doCall(omc);
	}

	private Boolean getHostBooleanConfigValue(String key, String hostName, boolean useGlobalFallback,
			Boolean defaultVal) {

		Logging.debug(this, "getHostBooleanConfigValue key '" + key + "', host '" + hostName + "'");
		Boolean value = null;

		Map<String, Object> hostConfig = getConfigsPD().get(hostName);
		if (hostConfig != null && hostConfig.get(key) != null && !((List<?>) (hostConfig.get(key))).isEmpty()) {
			value = Utils.interpretAsBoolean(((List<?>) hostConfig.get(key)).get(0), (Boolean) null);
			Logging.debug(this,
					"getHostBooleanConfigValue key '" + key + "', host '" + hostName + "', value: " + value);
			if (value != null) {
				return value;
			}
		}

		if (useGlobalFallback) {
			value = getGlobalBooleanConfigValue(key, null);
			if (value != null) {
				Logging.debug(this,
						"getHostBooleanConfigValue key '" + key + "', host '" + hostName + "', global value: " + value);
				return value;
			}
		}
		Logging.info(this, "getHostBooleanConfigValue key '" + key + "', host '" + hostName
				+ "', returning default value: " + defaultVal);
		return defaultVal;
	}

	public Boolean getGlobalBooleanConfigValue(String key, Boolean defaultVal) {
		Boolean val = defaultVal;
		Object obj = getConfigListCellOptionsPD().get(key);

		Logging.debug(this, "getGlobalBooleanConfigValue '" + key + "'='" + obj + "'");
		if (obj == null) {
			Logging.warning(this, "getGlobalBooleanConfigValue '" + key + "' is null, returning default value: " + val);
			return val;
		}

		ConfigOption option = (ConfigOption) obj;
		if (option.getType() != ConfigOption.TYPE.BOOL_CONFIG) {
			Logging.warning(this, "getGlobalBooleanConfigValue type of '" + key + "' should be boolean, but is "
					+ option.getType() + ", returning default value: " + val);
			return val;

		}

		List<Object> values = option.getDefaultValues();
		Logging.debug(this, "getGlobalBooleanConfigValue '" + key + "' defaultValues: " + values);
		if (values != null && !values.isEmpty()) {
			val = (Boolean) values.get(0);
		}

		return val;
	}

	public List<String> getServerConfigStrings(String key) {
		if (cacheManager.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class) == null) {
			retrieveConfigOptionsPD();
		}
		return Utils.takeAsStringList(getConfigDefaultValuesPD().get(key));
	}

	/**
	 * Retrieve available backends.
	 * <p>
	 * This methods is only a viable option for servers/depots, that has opsi
	 * 4.2 or lower; Due to the RPC method deprecation in opsi 4.3.
	 * 
	 * @return available backends
	 * @deprecated since opsi 4.3
	 */
	public String getBackendInfos() {
		String bgColor0 = "#dedeff";
		String bgColor1 = "#ffffff";
		String bgColor = "";

		String titleSize = "14px";
		String fontSizeBig = "10px";
		String fontSizeSmall = "8px";

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GET_BACKEND_INFOS_LIST_OF_HASHES, new String[] {});
		List<Object> list = exec.getListResult(omc);
		Map<String, List<Map<String, Object>>> backends = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> listEntry = exec.getMapFromItem(list.get(i));
			String backendName = "UNKNOWN";

			if (listEntry.containsKey("name")) {
				backendName = (String) listEntry.get("name");
			}

			if (!backends.containsKey(backendName)) {
				backends.put(backendName, new ArrayList<>());
			}

			backends.get(backendName).add(listEntry);
		}

		StringBuilder buf = new StringBuilder("");
		buf.append("<table border='0' cellspacing='0' cellpadding='0'>\n");

		Iterator<String> backendIterator = backends.keySet().iterator();
		while (backendIterator.hasNext()) {
			String backendName = backendIterator.next();

			buf.append("<tr><td bgcolor='#fbeca5' color='#000000'  width='100%'  colspan='3'  align='left'>");
			buf.append("<font size='" + titleSize + "'><b>" + backendName + "</b></font></td></tr>");

			List<Map<String, Object>> backendEntries = backends.get(backendName);

			for (int i = 0; i < backendEntries.size(); i++) {
				Map<String, Object> listEntry = backendEntries.get(i);

				Iterator<String> eIt = listEntry.keySet().iterator();

				boolean entryIsEven = false;

				while (eIt.hasNext()) {
					String key = eIt.next();
					if ("name".equals(key)) {
						continue;
					}

					entryIsEven = !entryIsEven;
					if (entryIsEven) {
						bgColor = bgColor0;
					} else {
						bgColor = bgColor1;
					}

					Object value = listEntry.get(key);
					buf.append("<tr height='8px'>");
					buf.append("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
							+ fontSizeBig + "'>" + key + "</font></td>");

					if ("config".equals(key)) {
						buf.append("<td colspan='2'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
								+ fontSizeBig + "'>&nbsp;</font></td>");
						buf.append("</tr>");

						Map<String, Object> configItems = exec.getMapFromItem(value);

						if (!configItems.isEmpty()) {
							Iterator<String> configItemsIterator = configItems.keySet().iterator();

							while (configItemsIterator.hasNext()) {
								String configKey = configItemsIterator.next();

								Object jO = configItems.get(configKey);

								String configVal = "";

								configVal = jO.toString();

								buf.append("<td bgcolor='" + bgColor + "'>&nbsp;</td>");
								buf.append("<td width='200px'  bgcolor='" + bgColor
										+ "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configKey
										+ "</font></td>");
								buf.append("<td width='200px'  bgcolor='" + bgColor
										+ "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configVal
										+ "</font></td>");
								buf.append("</tr>");
							}
						}
					} else {
						buf.append("<td width='300px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
								+ fontSizeBig + "'>" + value + "</font></td>");
						buf.append("</tr>");
					}
				}
				buf.append("<tr height='10px'><td bgcolor='" + bgColor + "' colspan='3'></td></tr>");
			}

			buf.append(
					"<tr><td bgcolor='#ffffff' color='#000000' width='100%' height='30px' colspan='3'>&nbsp;</td></tr>");
		}

		buf.append("</table>\n");

		return buf.toString();
	}

	public List<String> getDomains() {
		List<String> result = new ArrayList<>();

		Map<String, List<Object>> configDefaultValues = getConfigDefaultValuesPD();
		if (configDefaultValues.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY) == null) {
			Logging.info(this,
					"no values found for   " + OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY);
		} else {
			Logging.info(this, "getDomains "
					+ configDefaultValues.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY));

			HashMap<String, Integer> numberedValues = new HashMap<>();
			TreeSet<String> orderedValues = new TreeSet<>();
			TreeSet<String> unorderedValues = new TreeSet<>();

			for (Object item : configDefaultValues
					.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY)) {
				String entry = (String) item;
				int p = entry.indexOf(":");
				if (p == -1 || p == 0) {
					unorderedValues.add(entry);
				} else if (p > 0) {
					// the only regular case
					int orderNumber = -1;
					try {
						orderNumber = Integer.valueOf(entry.substring(0, p));
						String value = entry.substring(p + 1);
						if (numberedValues.get(value) == null || orderNumber < numberedValues.get(value)) {
							orderedValues.add(entry);
							numberedValues.put(value, orderNumber);
						}
					} catch (NumberFormatException x) {
						Logging.warning(this, "illegal order format for domain entry: " + entry);
						unorderedValues.add(entry);
					}
				} else {
					Logging.warning(this, "p has unexpected value " + p);
				}
			}

			for (String entry : orderedValues) {
				int p = entry.indexOf(":");
				result.add(entry.substring(p + 1));
			}

			unorderedValues.removeAll(result);

			for (String entry : unorderedValues) {
				result.add(entry);
			}
		}

		Logging.info(this, "getDomains " + result);
		return result;
	}

	public void writeDomains(List<Object> domains) {
		String key = OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY;
		Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");

		item.put("ident", key);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", domains);
		item.put("possibleValues", domains);
		item.put("editable", true);
		item.put("multiValue", true);

		List<Map<String, Object>> readyObjects = new ArrayList<>();
		readyObjects.add(item);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

		exec.doCall(omc);

		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		configDefaultValues.put(key, domains);
		cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);
	}

	public String getConfigedWorkbenchDefaultValuePD() {
		return cacheManager.getCachedData(CacheIdentifier.CONFIGED_WORKBENCH_DEFAULT_VALUE, String.class);
	}

	public void setConfigedWorkbenchDefaultValuePD(String defaultWorkbenchValue) {
		cacheManager.setCachedData(CacheIdentifier.CONFIGED_WORKBENCH_DEFAULT_VALUE, defaultWorkbenchValue);
	}

	public String getPackageServerDirectoryPD() {
		return cacheManager.getCachedData(CacheIdentifier.PACKAGE_SERVER_DIRECTORY, String.class);
	}

	public void setPackageServerDirectoryPD(String packageServerDirectory) {
		cacheManager.setCachedData(CacheIdentifier.PACKAGE_SERVER_DIRECTORY, packageServerDirectory);
	}

	public boolean isGlobalReadOnly() {
		return Utils.toBoolean(cacheManager.getCachedData(CacheIdentifier.GLOBAL_READ_ONLY, Boolean.class));
	}
}
