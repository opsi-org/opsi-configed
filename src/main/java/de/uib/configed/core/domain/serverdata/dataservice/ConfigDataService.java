/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uib.configed.core.domain.RemoteControls;
import de.uib.configed.core.domain.SavedSearches;
import de.uib.configed.core.domain.permission.UserConfig;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.type.ConfigName2ConfigValue;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.gui.type.ConfigOption.TYPE;
import de.uib.configed.gui.type.RemoteControl;
import de.uib.configed.gui.type.SavedSearch;
import de.uib.configed.share.ConfigUtils;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.logging.TimeCheck;

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
public class ConfigDataService extends DataService {
	// wan meta configuration
	public static final String WAN_PARTKEY = "wan_";
	public static final String NOT_WAN_CONFIGURED_PARTKEY = "wan_mode_off";

	protected static final String KEY_DISABLED_CLIENT_ACTIONS = "configed.host_actions_disabled";
	protected static final String KEY_OPSICLIENTD_EXTRA_EVENTS = "configed.opsiclientd_events";

	private static final String KEY_DOWNTIME_START = "opsi.check.downtime.start";
	private static final String KEY_DOWNTIME_END = "opsi.check.downtime.end";
	private static final String KEY_DOWNTIME_ENABLED = "opsi.check.enabled";

	private List<Map<String, Object>> configCollection;
	private List<Map<String, Object>> configStateCollection;
	private List<Map<String, Object>> deleteConfigStateItems;

	public ConfigDataService(DataServices dataServices) {
		super(dataServices);
	}

	/**
	 * delivers the default domain if it is not existing it retrieves it from
	 * servide
	 */
	public String getOpsiDefaultDomainPD() {
		retrieveOpsiDefaultDomainPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_DEFAULT_DOMAIN, String.class);
	}

	/**
	 * retrieves default domain from service
	 */
	public void retrieveOpsiDefaultDomainPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.OPSI_DEFAULT_DOMAIN)) {
			return;
		}
		String opsiDefaultDomain = dataServices.exec.getStringResult(RPCMethodName.GET_DOMAIN);
		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_DEFAULT_DOMAIN, opsiDefaultDomain);
	}

	public Map<String, List<Object>> getConfigDefaultValuesPD() {
		retrieveConfigOptionsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
	}

	public Map<String, RemoteControl> getRemoteControlsPD() {
		retrieveConfigOptionsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.REMOTE_CONTROLS, Map.class);
	}

	public SavedSearches getSavedSearchesPD() {
		retrieveConfigOptionsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.SAVED_SEARCHES, SavedSearches.class);
	}

	public Map<String, ConfigOption> getConfigOptionsPD() {
		retrieveConfigOptionsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);
	}

	public void retrieveConfigOptionsPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.CONFIG_DEFAULT_VALUES)) {
			return;
		}

		Logging.debug(this, "getConfigOptions() work");

		List<Map<String, Object>> deleteItems = new ArrayList<>();

		Map<String, ConfigOption> configOptions = new HashMap<>();
		Map<String, List<Object>> configDefaultValues = new HashMap<>();

		RemoteControls remoteControls = new RemoteControls();
		SavedSearches savedSearches = new SavedSearches();

		// metaConfig for wan configuration is rebuilt in
		// getWANConfigOptions

		List<Map<String, Object>> retrievedList = dataServices.exec.getListOfMaps(RPCMethodName.CONFIG_GET_OBJECTS);
		Logging.info(this, "configOptions retrieved ");
		for (Map<String, Object> configItem : retrievedList) {
			String key = (String) configItem.get("ident");

			// build a ConfigOption from the retrieved item
			// eliminate key produced by old version for role branch

			String pseudouserProducedByOldVersion = OpsiServiceNOMPersistenceController.KEY_USER_ROOT + ".{"
					+ UserConfig.ROLE.substring(1, UserConfig.ROLE.length());

			if (key != null && key.startsWith(pseudouserProducedByOldVersion)) {
				Logging.warning(this, "user entry ", key,
						" produced by a still somewhere running old configed version , please delete user entry ",
						pseudouserProducedByOldVersion);

				deleteItems.add(configItem);

				Logging.info(this, "deleteItem ", configItem);

				continue;
			}

			ConfigOption configOption = new ConfigOption(configItem);
			configOptions.put(key, configOption);
			configDefaultValues.put(key, configOption.getDefaultValues());

			if (configOption.getDefaultValues() != null && !configOption.getDefaultValues().isEmpty()) {
				remoteControls.checkIn(key, "" + configOption.getDefaultValues().get(0));
				savedSearches.checkIn(key, "" + configOption.getDefaultValues().get(0));
			}
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.REMOTE_CONTROLS, remoteControls);
		dataServices.cacheManager.setCachedData(CacheIdentifier.SAVED_SEARCHES, savedSearches);
		dataServices.cacheManager.setCachedData(CacheIdentifier.CONFIG_OPTIONS, configOptions);
		dataServices.cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);

		Logging.info(this, "{ole deleteItems ", deleteItems.size());

		if (!deleteItems.isEmpty() && dataServices.exec.doCall(RPCMethodName.CONFIG_DELETE_OBJECTS, deleteItems)) {
			deleteItems.clear();
		}

		Logging.debug(this, "getConfigOptions() work finished");
	}

	public Map<String, Map<String, Object>> getHostConfigsPD() {
		retrieveHostConfigsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.HOST_CONFIGS, Map.class);
	}

	public void retrieveHostConfigsPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.HOST_CONFIGS)) {
			return;
		}

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();

		Map<String, Object> retrieved = dataServices.exec.getMapResult(RPCMethodName.CONFIG_STATE_GET_VALUES,
				new String[0], new String[0], false);
		Map<String, Map<String, Object>> hostConfigs = new HashMap<>();

		for (Entry<String, Object> hostConfig : retrieved.entrySet()) {
			if (hostConfig.getKey() != null && !"".equals(hostConfig.getKey())) {
				Map<String, Object> configs1Host = hostConfigs.computeIfAbsent(hostConfig.getKey(),
						arg -> new HashMap<>());
				Map<String, Object> configs = POJOReMapper.remap(hostConfig.getValue());

				Logging.debug(this, "retrieveHostConfigs objectId,  element ", hostConfig.getKey(), ": ", hostConfig);

				configs1Host.putAll(configs);
			}
		}

		timeCheck.stop();
		Logging.info(this, "retrieveHostConfigs retrieved ", hostConfigs.keySet());

		dataServices.cacheManager.setCachedData(CacheIdentifier.HOST_CONFIGS, hostConfigs);
		dataServices.persistenceController.notifyPanelCompleteWinProducts();
	}

	// send config updates and clear the collection
	public void updateConfigs() {
		setConfig(false);
	}

	// send config updates, possibly not updating existing

	private void setConfig(boolean restrictToMissing) {
		if (dataServices.userRoles.isGlobalReadOnly()) {
			return;
		}

		Logging.info(this, "setConfig(),  configCollection null ", (configCollection == null));

		if (configCollection == null || configCollection.isEmpty()) {
			return;
		}

		Logging.info(this, "setConfig(),  configCollection size  ", configCollection.size());
		// add configId where necessary
		List<String> usedConfigIds = new ArrayList<>();
		Map<String, String> typesOfUsedConfigIds = new HashMap<>();
		for (Map<String, Object> config : configCollection) {
			String ident = (String) config.get("ident");
			usedConfigIds.add(ident);
			typesOfUsedConfigIds.put(ident, (String) config.get("type"));
		}

		Logging.debug(this, "setConfig(), usedConfigIds: ", usedConfigIds);

		List<Object> existingConfigIds = dataServices.exec.getListResult(RPCMethodName.CONFIG_GET_IDENTS);

		Logging.info(this, "setConfig(), existingConfigIds: ", existingConfigIds.size());

		usedConfigIds.removeAll(existingConfigIds);

		Logging.info(this, "setConfig(), usedConfigIds: ", usedConfigIds);
		List<Map<String, Object>> createItems = new ArrayList<>();
		for (String missingId : usedConfigIds) {
			Map<String, Object> item = ConfigUtils.createNOMitem(typesOfUsedConfigIds.get(missingId));
			item.put("ident", missingId);
			createItems.add(item);
		}

		// remap to JSON types
		List<Map<String, Object>> callsConfigUpdateCollection = new ArrayList<>();
		List<Map<String, Object>> callsConfigDeleteCollection = new ArrayList<>();

		for (Map<String, Object> callConfig : configCollection) {
			if (callConfig.get("defaultValues") == null) {
				callsConfigDeleteCollection.add(callConfig);
				callsConfigUpdateCollection.removeIf(item -> callConfig.get("ident").equals(item.get("ident")));
			} else if ((!restrictToMissing || usedConfigIds.contains(callConfig.get("ident")))) {
				callsConfigUpdateCollection.add(callConfig);
			} else {
				// Do nothing, config does not need to be deleted or updated
			}
		}

		updateConfigsOnServer(createItems, callsConfigDeleteCollection, callsConfigUpdateCollection);

		retrieveConfigOptionsPD();
		configCollection.clear();

		Logging.info(this, "setConfig(),  configCollection result: ", configCollection);
	}

	private void updateConfigsOnServer(List<Map<String, Object>> createItems,
			List<Map<String, Object>> callsConfigDeleteCollection,
			List<Map<String, Object>> callsConfigUpdateCollection) {
		Logging.debug(this, "setConfig() createItems ", createItems);
		if (!createItems.isEmpty()) {
			dataServices.exec.doCall(RPCMethodName.CONFIG_CREATE_OBJECTS, createItems);
		}

		Logging.debug(this, "setConfig() callsConfigDeleteCollection ", callsConfigDeleteCollection);

		if (!callsConfigDeleteCollection.isEmpty()) {
			dataServices.exec.doCall(RPCMethodName.CONFIG_DELETE_OBJECTS, callsConfigDeleteCollection);
			dataServices.persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
			// because of referential integrity
			dataServices.persistenceController.reloadData(CacheIdentifier.HOST_CONFIGS.toString());
		}

		Logging.debug(this, "setConfig() callsConfigUpdateCollection ", callsConfigUpdateCollection);

		if (!callsConfigUpdateCollection.isEmpty()) {
			dataServices.exec.doCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, callsConfigUpdateCollection);
			dataServices.persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		}
	}

	// collect config updates
	public void setConfig(Map<String, List<Object>> settings) {
		Logging.debug(this, "setConfig settings ", settings);
		if (configCollection == null) {
			configCollection = new ArrayList<>();
		}

		Map<String, ConfigOption> configOptions = getConfigOptionsPD();

		for (Entry<String, List<Object>> setting : settings.entrySet()) {
			Logging.debug(this, "setConfig,  key, settings.get(key): ", setting.getKey(), ", ", setting.getValue());

			Logging.debug(this, "setConfig,  settings.get(key), settings.get(key).getClass().getName(): ",
					setting.getValue());

			Logging.info(this, "setConfig, key: ", setting.getKey());

			Map<String, Object> config = new HashMap<>();

			config.put("ident", setting.getKey());

			String type;

			Logging.debug(this, "setConfig, key,  configOptions.get(key):  ", setting.getKey(), ", ",
					configOptions.get(setting.getKey()));
			if (configOptions.get(setting.getKey()) != null) {
				config.put("multiValue", configOptions.get(setting.getKey()).get("multiValue"));

				type = (String) configOptions.get(setting.getKey()).get("type");
			} else if (!setting.getValue().isEmpty() && setting.getValue().get(0) instanceof Boolean) {
				type = "BoolConfig";
			} else {
				type = "UnicodeConfig";
			}

			config.put("type", type);

			config.put("description", configOptions.get(setting.getKey()).get("description"));

			Logging.devel(configOptions.get(setting.getKey()).toString());

			config.put("defaultValues", setting.getValue());

			List<Object> possibleValues = createPossibleValues(type, setting.getValue(),
					configOptions.get(setting.getKey()));

			config.put("possibleValues", possibleValues);

			configCollection.add(config);
		}
	}

	private static List<Object> createPossibleValues(String type, List<Object> defaultValues,
			ConfigOption configOption) {
		List<Object> possibleValues;
		if (configOption == null) {
			possibleValues = new ArrayList<>();
			if (type.equals(TYPE.BOOL_CONFIG.toString())) {
				possibleValues.add(true);
				possibleValues.add(false);
			}
		} else {
			possibleValues = configOption.getPossibleValues();
		}

		// defaultValues is null when we delete a config
		if (defaultValues != null) {
			for (Object item : defaultValues) {
				if (!possibleValues.contains(item)) {
					possibleValues.add(item);
				}
			}
		}

		return possibleValues;
	}

	public void addRoleConfig(String name) {
		String configkey = UserConfig.KEY_USER_ROLE_ROOT + ".{" + name + "}." + UserConfig.HAS_ROLE_ATTRIBUT;
		addRoleAndUserConfig(configkey, "");
	}

	public void addUserConfig(String name, String rolename) {
		String configkey = UserConfig.START_USER_KEY + name + "}." + UserConfig.HAS_ROLE_ATTRIBUT;
		addRoleAndUserConfig(configkey, rolename);
	}

	private void addRoleAndUserConfig(String configkey, String rolename) {
		String role = rolename;

		if (role == null) {
			role = UserConfig.NONE_PROTOTYPE;
		}

		List<Object> selectedValuesRole = new ArrayList<>();
		selectedValuesRole.add(role);

		Map<String, Object> itemRole = ConfigUtils.createNOMConfig(ConfigOption.TYPE.UNICODE_CONFIG, configkey,
				"which role should determine this configuration", false, false, selectedValuesRole, selectedValuesRole);

		dataServices.exec.doCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, Set.of(itemRole));

		Map<String, List<Object>> configDefaultValues = getConfigDefaultValuesPD();
		configDefaultValues.put(configkey, selectedValuesRole);
		dataServices.cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);
	}

	public void deleteSavedSearch(String name) {
		Logging.debug(this, "deleteSavedSearch ", name);
		SavedSearches savedSearches = getSavedSearchesPD();

		Map<String, Object> item1 = ConfigUtils.createNOMitem("UnicodeConfig");
		item1.put("id", SavedSearch.CONFIG_KEY + "." + name);

		Map<String, Object> item2 = ConfigUtils.createNOMitem("UnicodeConfig");
		item2.put("id", SavedSearch.CONFIG_KEY + "." + name + "." + SavedSearch.DESCRIPTION_KEY);

		if (dataServices.exec.doCall(RPCMethodName.CONFIG_DELETE_OBJECTS, Set.of(item1, item2))) {
			savedSearches.remove(name);
			dataServices.cacheManager.setCachedData(CacheIdentifier.SAVED_SEARCHES, savedSearches);
		}
	}

	public void saveSearch(SavedSearch ob) {
		Logging.debug(this, "saveSearch ", ob);

		List<Object> readyObjects = new ArrayList<>();
		// entry of serialization string
		readyObjects.add(produceConfigEntry("UnicodeConfig", SavedSearch.CONFIG_KEY + "." + ob.getName(),
				ob.getSerialization(), ob.getDescription(), false));
		// description entry
		readyObjects.add(produceConfigEntry("UnicodeConfig",
				SavedSearch.CONFIG_KEY + "." + ob.getName() + "." + SavedSearch.DESCRIPTION_KEY, ob.getDescription(),
				"", true));

		dataServices.exec.doCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, readyObjects);
	}

	protected static Map<String, Object> produceConfigEntry(String nomType, String key, Object value,
			String description) {
		return produceConfigEntry(nomType, key, value, description, true);
	}

	private static Map<String, Object> produceConfigEntry(String nomType, String key, Object value, String description,
			boolean editable) {
		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(value);

		// defaultValues
		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(value);

		// create config for service
		Map<String, Object> item;

		item = ConfigUtils.createNOMitem(nomType);
		item.put("ident", key);
		item.put("description", description);
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", editable);
		item.put("multiValue", false);

		return item;
	}

	public List<String> getDisabledClientMenuEntries() {
		if (!dataServices.cacheManager.isDataCached(CacheIdentifier.CONFIG_DEFAULT_VALUES)) {
			retrieveConfigOptionsPD();
		}
		Map<String, List<Object>> configDefaultValues = dataServices.cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);

		return POJOReMapper.remap(configDefaultValues.get(KEY_DISABLED_CLIENT_ACTIONS));
	}

	public List<String> getOpsiclientdExtraEvents() {
		Logging.debug(this, "getOpsiclientdExtraEvents");

		if (!dataServices.cacheManager.isDataCached(CacheIdentifier.CONFIG_DEFAULT_VALUES)) {
			retrieveConfigOptionsPD();
		}

		Map<String, List<Object>> configDefaultValues = dataServices.cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		if (configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS) == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  ",
					KEY_OPSICLIENTD_EXTRA_EVENTS);
		}

		List<String> result = POJOReMapper.remap(configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS));
		Logging.debug(this, "getOpsiclientdExtraEvents() ", result);
		return result;
	}

	public List<Map<String, Object>> getHostsConfigsWithDefaults(List<String> objectIds) {
		Logging.info(this, "getHostsConfigsWithDefaults for ", objectIds);

		if (objectIds == null || objectIds.isEmpty()) {
			return new ArrayList<>();
		}

		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Map<String, Object>> retrieved = dataServices.exec
				.getMapOfMaps(RPCMethodName.CONFIG_STATE_GET_VALUES, Set.of(), objectIds, true);
		for (Entry<String, Map<String, Object>> entry : retrieved.entrySet()) {
			result.add(new ConfigName2ConfigValue(entry.getValue(), getConfigOptionsPD()));
		}
		return result;
	}

	public List<Map<String, Object>> getHostsConfigsWithoutDefaults(Iterable<String> objectIds) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (String objectId : objectIds) {
			Map<String, Object> hostConfig = getHostConfigsPD().get(objectId) != null ? getHostConfigsPD().get(objectId)
					: new HashMap<>();
			result.add(hostConfig);
		}
		return result;
	}

	// collect config state updates
	public void setConfigStates(String objectId, ConfigName2ConfigValue settings) {
		if (configStateCollection == null) {
			configStateCollection = new ArrayList<>();
		}

		Set<String> currentKeys = settings.keySet();
		Logging.info(this, "setAdditionalConfigurations current keySet size: ", currentKeys.size());
		if (settings.getRetrieved() != null) {
			Set<String> retrievedKeys = settings.getRetrieved().keySet();

			Logging.info(this, "setAdditionalConfigurations retrieved keys size  ", retrievedKeys.size());

			Set<String> removedKeys = new HashSet<>(retrievedKeys);
			removedKeys.removeAll(currentKeys);
			Logging.info(this, "setAdditionalConfigurations removed ", removedKeys);

			if (!removedKeys.isEmpty()) {
				if (deleteConfigStateItems == null) {
					deleteConfigStateItems = new ArrayList<>();
				}

				for (Object key : removedKeys) {
					String ident = "" + key + ";" + objectId;

					Map<String, Object> item = ConfigUtils.createNOMitem("ConfigState");
					item.put("ident", ident);
					deleteConfigStateItems.add(item);
				}
			}
		}

		for (Entry<String, Object> entry : settings.entrySet()) {
			Map<String, Object> state = new HashMap<>();
			state.put("type", "ConfigState");
			state.put("objectId", objectId);
			state.put("configId", entry.getKey());
			state.put("values", entry.getValue());

			configStateCollection.add(state);
		}
	}

	// send config updates and clear the collection
	public void updateConfigStates() {
		if (dataServices.userRoles.isGlobalReadOnly()) {
			return;
		}

		if (configStateCollection == null || configStateCollection.isEmpty()) {
			return;
		}

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

			if (valueList == null) {
				Map<String, Object> item = ConfigUtils.createNOMitem("ConfigState");
				item.put("objectId", configState.get("objectId"));
				item.put("configId", configState.get("configId"));

				deleteConfigStateItems.add(item);
				doneList.add(configState);
			} else if (!valueList.isEmpty() && valueList.get(0) instanceof Boolean) {
				typesOfUsedConfigIds.put(ident, "BoolConfig");
			} else {
				typesOfUsedConfigIds.put(ident, "UnicodeConfig");
			}
		}

		updateAdditionalConfigsOnServer(doneList, usedConfigIds, typesOfUsedConfigIds);

		// at any rate:
		configStateCollection.clear();
	}

	private void updateAdditionalConfigsOnServer(List<Object> doneList, Set<String> usedConfigIds,
			Map<String, String> typesOfUsedConfigIds) {
		Logging.debug(this, "setAdditionalConfiguration(), usedConfigIds: ", usedConfigIds);
		Logging.debug(this, "setAdditionalConfiguration(), deleteConfigStateItems  ", deleteConfigStateItems);
		// not used
		if (!deleteConfigStateItems.isEmpty()
				&& dataServices.exec.doCall(RPCMethodName.CONFIG_STATE_DELETE_OBJECTS, deleteConfigStateItems)) {
			deleteConfigStateItems.clear();
			configStateCollection.removeAll(doneList);
		}

		List<Object> existingConfigIds = dataServices.exec.getListResult(RPCMethodName.CONFIG_GET_IDENTS);
		Logging.debug(this, "setAdditionalConfiguration(), existingConfigIds: ", existingConfigIds.size());

		Set<String> missingConfigIds = new HashSet<>(usedConfigIds);
		missingConfigIds.removeAll(existingConfigIds);
		Logging.debug(this, "setAdditionalConfiguration(), missingConfigIds: ", missingConfigIds);
		List<Map<String, Object>> createItems = new ArrayList<>();
		for (String missingId : missingConfigIds) {
			Map<String, Object> item = ConfigUtils.createNOMitem(typesOfUsedConfigIds.get(missingId));
			item.put("ident", missingId);
			createItems.add(item);
		}

		if (!createItems.isEmpty()) {
			dataServices.exec.doCall(RPCMethodName.CONFIG_CREATE_OBJECTS, createItems);
			dataServices.persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		}

		// do call
		if (!configStateCollection.isEmpty()) {
			// now we can set the values and clear the collected update items
			dataServices.exec.doCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS, configStateCollection);
		}
	}

	public Boolean isInstallByShutdownConfiguredOnConfigserver() {
		final String configserver = dataServices.hostInfoCollections.getConfigServer();
		String key = OpsiServiceNOMPersistenceController.KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN;
		Logging.debug(this, "getHostBooleanConfigValue key '", key, "', host '", configserver, "'");
		Boolean value = null;

		Map<String, Object> hostConfig = getHostConfigsPD().get(configserver);
		if (hostConfig != null && hostConfig.get(key) != null && !((List<?>) (hostConfig.get(key))).isEmpty()) {
			value = (Boolean) ((List<?>) hostConfig.get(key)).get(0);
			Logging.debug(this, "getHostBooleanConfigValue key '", key, "', host '", configserver, "', value: ", value);
			if (value != null) {
				return value;
			}
		}

		value = getGlobalBooleanConfigValue(key, null);
		if (value != null) {
			Logging.debug(this, "getHostBooleanConfigValue key '", key, "', host '", configserver, "', global value: ",
					value);
			return value;
		}
		Logging.info(this, "getHostBooleanConfigValue key '", key, "', host '", configserver,
				"', returning default value: ", false);
		return false;
	}

	public Boolean isWanConfiguredOnConfigserver() {
		final String CONFIG_SERVER = dataServices.hostInfoCollections.getConfigServer();
		final String NET_CONNECTION_ACTIVE_KEY = "opsiclientd.event_net_connection.active";
		final String TIMER_ACTIVE_KEY = "opsiclientd.event_timer.active";
		final String GUI_STARTUP_ACTIVE_KEY = "opsiclientd.event_gui_startup.active";
		final String GUI_STARTUP_USER_LOGGED_IN_ACTIVE_KEY = "opsiclientd.event_gui_startup{user_logged_in}.active";

		Logging.debug(this, "isWanConfigured evaluating host '", CONFIG_SERVER, "' with keys: '",
				NET_CONNECTION_ACTIVE_KEY, "; ", TIMER_ACTIVE_KEY, "; ", GUI_STARTUP_ACTIVE_KEY, "; ",
				GUI_STARTUP_USER_LOGGED_IN_ACTIVE_KEY, "'");

		Map<String, Object> hostConfig = getHostConfigsPD().get(CONFIG_SERVER);

		Boolean[] enabling = resolvePair(CONFIG_SERVER, hostConfig, NET_CONNECTION_ACTIVE_KEY, TIMER_ACTIVE_KEY,
				"[enabling]");

		boolean enabledByEvents = Boolean.TRUE.equals(enabling[0]) && Boolean.TRUE.equals(enabling[1]);
		if (!enabledByEvents) {
			Logging.info(this, "isWanConfigured: WAN not enabled by net/timer for host '", CONFIG_SERVER,
					"'. Returning: ", false);
			return false;
		}

		Boolean[] disabling = resolvePair(CONFIG_SERVER, hostConfig, GUI_STARTUP_ACTIVE_KEY,
				GUI_STARTUP_USER_LOGGED_IN_ACTIVE_KEY, "[disabling]");

		boolean guiStartupBlocks = Boolean.TRUE.equals(disabling[0]) || Boolean.TRUE.equals(disabling[1]);
		if (guiStartupBlocks) {
			// Covers "all four active": prefer safety and disable WAN
			Logging.warning(this, "isWanConfigured: conflicting settings for host '", CONFIG_SERVER,
					"': WAN enabling events are active but GUI startup is active as well. Disabling WAN.");
			return false;
		}

		Logging.debug(this, "isWanConfigured: WAN enabled for host '", CONFIG_SERVER, "'.");

		return true;
	}

	private static Boolean getHostBoolean(Map<String, Object> hostConfig, String key) {
		Object v = null;
		if (hostConfig != null) {
			Object raw = hostConfig.get(key);
			if (raw instanceof List<?> list && !list.isEmpty()) {
				v = list.get(0);
			}
		}
		return (v instanceof Boolean b) ? b : null;
	}

	private Boolean[] resolvePair(String hostId, Map<String, Object> hostConfig, String keyA, String keyB,
			String pairLabel) {
		Boolean hostA = getHostBoolean(hostConfig, keyA);
		Boolean hostB = getHostBoolean(hostConfig, keyB);
		final Boolean a;
		final Boolean b;

		if (hostA != null && hostB != null) {
			a = hostA;
			b = hostB;
			Logging.debug(this, "resolvePair ", pairLabel, " (host-level) for '", hostId, "': ", keyA, "=", a, ", ",
					keyB, "=", b);
		} else {
			a = getGlobalBooleanConfigValue(keyA, null);
			b = getGlobalBooleanConfigValue(keyB, null);
			Logging.debug(this, "resolvePair ", pairLabel, " (global) for '", hostId, "': ", keyA, "=", a, ", ", keyB,
					"=", b);
		}
		return new Boolean[] { a, b };
	}

	public Boolean getGlobalBooleanConfigValue(String key, Boolean defaultVal) {
		Boolean val = defaultVal;
		ConfigOption configOption = getConfigOptionsPD().get(key);

		Logging.debug(this, "getGlobalBooleanConfigValue '", key, "'='", configOption, "'");
		if (configOption == null) {
			Logging.warning(this, "getGlobalBooleanConfigValue '", key, "' is null, returning default value: ", val);
			return val;
		}

		if (configOption.getType() != ConfigOption.TYPE.BOOL_CONFIG) {
			Logging.warning(this, "getGlobalBooleanConfigValue type of '", key, "' should be boolean, but is ",
					configOption.getType(), ", returning default value: ", val);
			return val;
		}

		List<Object> values = configOption.getDefaultValues();
		Logging.debug(this, "getGlobalBooleanConfigValue '", key, "' defaultValues: ", values);
		if (values != null && !values.isEmpty()) {
			val = (Boolean) values.get(0);
		}

		return val;
	}

	public List<String> getServerConfigStrings(String key) {
		retrieveConfigOptionsPD();
		return POJOReMapper.remap(getConfigDefaultValuesPD().get(key));
	}

	// setConfig(Map<String, List<Object>> settings)
	public void setMessageOfTheDayConfigs(Map<String, String> configs) {
		String[] keys = new String[] { OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE,
				OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL,
				OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER,
				OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL };
		Object[] data = new Object[] { configs.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE),
				configs.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL),
				configs.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER),
				configs.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL) };

		dataServices.exec.doCall(RPCMethodName.CONFIG_UPDATE_MESSAGE_OF_THE_DAY, data);

		String possibleValues = "possibleValues";
		for (int i = 0; i < keys.length; i++) {
			Logging.debug(this, "setMessageOfTheDayConfigs key ", keys[i], " data ", data[i]);
			ConfigOption option = getConfigOptionsPD().get(keys[i]);
			if (option == null) {
				Map<String, Object> options = new HashMap<>();
				options.put("id", keys[i]);
				options.put("ident", keys[i]);
				option = new ConfigOption(options);
			}
			option.setDefaultValues(List.of(data[i]));
			option.put(possibleValues, List.of(data[i]));
		}
	}

	public Map<String, String> getMessageOfTheDayConfigs() {
		Logging.debug(this, "getMessageOfTheDayConfigs");

		String[] keys = new String[] { OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE,
				OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL,
				OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER,
				OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL };

		Map<String, String> result = new HashMap<>();
		for (String key : keys) {
			ConfigOption option = getConfigOptionsPD().get(key);
			if (option == null) {
				Logging.warning(this, "getMessageOfTheDayConfigs, no option found for key ", key);
				continue;
			}
			result.put(key, option.getDefaultValues().get(0).toString());
		}
		Logging.debug(this, "getMessageOfTheDayConfigs result ", result);
		return result;
	}

	public List<String> getDomains() {
		List<String> result = new ArrayList<>();

		Map<String, List<Object>> configDefaultValues = getConfigDefaultValuesPD();
		if (configDefaultValues.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY) == null) {
			Logging.info(this, "no values found for   ",
					OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY);
		} else {
			Logging.info(this, "getDomains ",
					configDefaultValues.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY));

			for (Object entry : configDefaultValues
					.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY)) {
				int p = ((String) entry).indexOf(":");
				result.add(((String) entry).substring(p + 1));
			}
		}

		if (!result.contains(getOpsiDefaultDomainPD())) {
			result.add(getOpsiDefaultDomainPD());
		}

		Logging.info(this, "getDomains ", result);
		return result;
	}

	public void writeDomains(List<Object> domains) {
		String key = OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY;
		Map<String, Object> item = ConfigUtils.createNOMitem("UnicodeConfig");

		item.put("ident", key);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", domains);
		item.put("possibleValues", domains);
		item.put("editable", true);
		item.put("multiValue", true);

		dataServices.exec.doCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, Set.of(item));

		Map<String, List<Object>> configDefaultValues = dataServices.cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		configDefaultValues.put(key, domains);
		dataServices.cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);
	}

	public void writeDownTime(Collection<String> hostIds, boolean enabled, String startTime, String endTime) {
		if (hostIds.isEmpty()) {
			return;
		}

		List<Map<String, Object>> readyObjects = new ArrayList<>();

		for (String hostId : hostIds) {
			Map<String, Object> enabledItem = ConfigUtils
					.createNOMitem(OpsiServiceNOMPersistenceController.CONFIG_STATE_TYPE);
			enabledItem.put(OpsiServiceNOMPersistenceController.OBJECT_ID, hostId);
			enabledItem.put(OpsiServiceNOMPersistenceController.CONFIG_ID, KEY_DOWNTIME_ENABLED);
			enabledItem.put(OpsiServiceNOMPersistenceController.VALUES_ID, List.of(enabled));

			Map<String, Object> startTimeItem = ConfigUtils
					.createNOMitem(OpsiServiceNOMPersistenceController.CONFIG_STATE_TYPE);
			startTimeItem.put(OpsiServiceNOMPersistenceController.OBJECT_ID, hostId);
			startTimeItem.put(OpsiServiceNOMPersistenceController.CONFIG_ID, KEY_DOWNTIME_START);
			startTimeItem.put(OpsiServiceNOMPersistenceController.VALUES_ID, List.of(startTime));

			Map<String, Object> endTimeItem = ConfigUtils
					.createNOMitem(OpsiServiceNOMPersistenceController.CONFIG_STATE_TYPE);
			endTimeItem.put(OpsiServiceNOMPersistenceController.OBJECT_ID, hostId);
			endTimeItem.put(OpsiServiceNOMPersistenceController.CONFIG_ID, KEY_DOWNTIME_END);
			endTimeItem.put(OpsiServiceNOMPersistenceController.VALUES_ID, List.of(endTime));

			readyObjects.add(enabledItem);
			readyObjects.add(startTimeItem);
			readyObjects.add(endTimeItem);
		}

		dataServices.exec.doCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS, readyObjects);
	}

	public String getConfigedWorkbenchDefaultValuePD() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.CONFIGED_WORKBENCH_DEFAULT_VALUE, String.class);
	}

	public void setConfigedWorkbenchDefaultValuePD(String defaultWorkbenchValue) {
		dataServices.cacheManager.setCachedData(CacheIdentifier.CONFIGED_WORKBENCH_DEFAULT_VALUE,
				defaultWorkbenchValue);
	}
}
