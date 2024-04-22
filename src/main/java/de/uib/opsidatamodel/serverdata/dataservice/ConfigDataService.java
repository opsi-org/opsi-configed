/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.OpsiHwAuditDevicePropertyTypes;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SavedSearch;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.RemoteControls;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Utils;
import de.uib.utils.datapanel.MapTableModel;
import de.uib.utils.logging.Logging;
import de.uib.utils.logging.TimeCheck;
import de.uib.utils.table.ListCellOptions;

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
	protected static final String KEY_DISABLED_CLIENT_ACTIONS = "configed.host_actions_disabled";
	protected static final String KEY_OPSICLIENTD_EXTRA_EVENTS = "configed.opsiclientd_events";

	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private UserRolesConfigDataService userRolesConfigDataService;
	private HardwareDataService hardwareDataService;

	private List<Map<String, Object>> configCollection;
	private List<Map<String, Object>> configStateCollection;
	private List<Map<String, Object>> deleteConfigStateItems;

	public ConfigDataService(AbstractPOJOExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setHardwareDataService(HardwareDataService hardwareDataService) {
		this.hardwareDataService = hardwareDataService;
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
		if (cacheManager.isDataCached(CacheIdentifier.OPSI_DEFAULT_DOMAIN)) {
			return;
		}
		Object[] params = new Object[] {};
		String opsiDefaultDomain = exec.getStringResult(new OpsiMethodCall(RPCMethodName.GET_DOMAIN, params));
		cacheManager.setCachedData(CacheIdentifier.OPSI_DEFAULT_DOMAIN, opsiDefaultDomain);
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
		if (cacheManager.isDataCached(Arrays.asList(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS,
				CacheIdentifier.CONFIG_OPTIONS, CacheIdentifier.CONFIG_DEFAULT_VALUES))) {
			return;
		}

		Logging.debug(this, "getConfigOptions() work");

		List<Map<String, Object>> deleteItems = new ArrayList<>();

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
			String key = (String) configItem.get("ident");

			// build a ConfigOption from the retrieved item
			// eliminate key produced by old version for role branch

			String pseudouserProducedByOldVersion = OpsiServiceNOMPersistenceController.KEY_USER_ROOT + ".{"
					+ UserConfig.ROLE.substring(1, UserConfig.ROLE.length());

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
			configDefaultValues.put(key, configOption.getDefaultValues());

			if (configOption.getDefaultValues() != null && !configOption.getDefaultValues().isEmpty()) {
				remoteControls.checkIn(key, "" + configOption.getDefaultValues().get(0));
				savedSearches.checkIn(key, "" + configOption.getDefaultValues().get(0));
			}
			hwAuditDevicePropertyTypes.checkIn(key, configOption.getDefaultValues());
		}

		cacheManager.setCachedData(CacheIdentifier.REMOTE_CONTROLS, remoteControls);
		cacheManager.setCachedData(CacheIdentifier.SAVED_SEARCHES, savedSearches);
		cacheManager.setCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS, configListCellOptions);
		cacheManager.setCachedData(CacheIdentifier.CONFIG_OPTIONS, configOptions);
		cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);

		Logging.info(this, "{ole deleteItems " + deleteItems.size());

		if (!deleteItems.isEmpty()) {
			OpsiMethodCall omcDeleteItems = new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS,
					new Object[] { deleteItems });

			if (exec.doCall(omcDeleteItems)) {
				deleteItems.clear();
			}
		}

		retrieveWANConfigOptionsPD();
		Logging.debug(this, "getConfigOptions() work finished");
	}

	public Map<String, List<Object>> getWanConfigurationPD() {
		retrieveWANConfigOptionsPD();
		return cacheManager.getCachedData(CacheIdentifier.WAN_CONFIGURATION, Map.class);
	}

	public Map<String, List<Object>> getNotWanConfigurationPD() {
		retrieveWANConfigOptionsPD();
		return cacheManager.getCachedData(CacheIdentifier.NOT_WAN_CONFIGURATION, Map.class);
	}

	public Map<String, ConfigOption> retrieveWANConfigOptionsPD() {
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
				Logging.error(this, "WAN config option key " + notWanConfigOption.getKey() + " is non BOOL_CONFIG");
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
		Map<String, ConfigOption> result = new HashMap<>();
		retrieveConfigOptionsPD();
		Map<String, ConfigOption> configOptions = getConfigOptionsPD();
		for (Entry<String, ConfigOption> configOption : configOptions.entrySet()) {
			if (configOption.getKey().startsWith(s) && configOption.getKey().length() > s.length()) {
				String xKey = configOption.getKey().substring(s.length());
				result.put(xKey, configOption.getValue());
			}
		}

		return result;
	}

	public Map<String, Map<String, Object>> getHostConfigsPD() {
		retrieveHostConfigsPD();
		return cacheManager.getCachedData(CacheIdentifier.HOST_CONFIGS, Map.class);
	}

	public void retrieveHostConfigsPD() {
		if (ServerFacade.isOpsi43()) {
			retrieveHostConfigsPDOpsi43();
		} else {
			retrieveHostConfigsPDOpsi42Lower();
		}
	}

	private void retrieveHostConfigsPDOpsi42Lower() {
		if (cacheManager.isDataCached(CacheIdentifier.HOST_CONFIGS)) {
			return;
		}

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();

		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
		Map<String, Map<String, Object>> hostConfigs = new HashMap<>();

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

	private void retrieveHostConfigsPDOpsi43() {
		if (cacheManager.isDataCached(CacheIdentifier.HOST_CONFIGS)) {
			return;
		}

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();

		String[] configIds = new String[] {};
		String[] objectIds = new String[] {};
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_GET_VALUES,
				new Object[] { configIds, objectIds, false });
		Map<String, Object> retrieved = exec.getMapResult(omc);
		Map<String, Map<String, Object>> hostConfigs = new HashMap<>();

		for (Entry<String, Object> hostConfig : retrieved.entrySet()) {
			if (hostConfig.getKey() != null && !"".equals(hostConfig.getKey())) {
				Map<String, Object> configs1Host = hostConfigs.computeIfAbsent(hostConfig.getKey(),
						arg -> new HashMap<>());
				Map<String, Object> configs = POJOReMapper.remap(hostConfig.getValue(),
						new TypeReference<Map<String, Object>>() {
						});

				Logging.debug(this,
						"retrieveHostConfigs objectId,  element " + hostConfig.getKey() + ": " + hostConfig);

				configs1Host.putAll(configs);
			}
		}

		timeCheck.stop();
		Logging.info(this, "retrieveHostConfigs retrieved " + hostConfigs.keySet());

		cacheManager.setCachedData(CacheIdentifier.HOST_CONFIGS, hostConfigs);
		persistenceController.notifyPanelCompleteWinProducts();
	}

	public List<Map<String, Object>> addWANConfigStates(String clientId, List<Map<String, Object>> jsonObjects) {
		return addWANConfigStates(clientId, true, jsonObjects);
	}

	private List<Map<String, Object>> addWANConfigStates(String clientId, boolean wan,
			List<Map<String, Object>> jsonObjects) {
		retrieveWANConfigOptionsPD();

		Map<String, List<Object>> wanConfiguration = getWanConfigurationPD();
		Map<String, List<Object>> notWanConfiguration = getNotWanConfigurationPD();

		Logging.debug(this,
				"addWANConfigState  wanConfiguration " + wanConfiguration + "\n " + wanConfiguration.size());
		Logging.debug(this, "addWANConfigState  wanConfiguration.keySet() " + wanConfiguration.keySet());

		Logging.debug(this,
				"addWANConfigState  notWanConfiguration " + notWanConfiguration + "\n " + notWanConfiguration.size());
		Logging.debug(this, "addWANConfigState  notWanConfiguration.keySet() " + notWanConfiguration.keySet());

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
			if (getHostConfigsPD().get(clientId) == null) {
				Logging.info(this, "addWANConfigState; until now, no config(State) existed for client " + clientId
						+ " no local update");
				getHostConfigsPD().put(clientId, new HashMap<>());
			}

			getHostConfigsPD().get(clientId).put(config.getKey(), config.getValue());

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
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return;
		}

		Logging.info(this, "setConfig(),  configCollection null " + (configCollection == null));

		if (configCollection == null || configCollection.isEmpty()) {
			return;
		}

		Logging.info(this, "setConfig(),  configCollection size  " + configCollection.size());
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

		usedConfigIds.removeAll(existingConfigIds);

		Logging.info(this, "setConfig(), usedConfigIds: " + usedConfigIds);
		List<Map<String, Object>> createItems = new ArrayList<>();
		for (String missingId : usedConfigIds) {
			Map<String, Object> item = Utils.createNOMitem(typesOfUsedConfigIds.get(missingId));
			item.put("ident", missingId);
			createItems.add(item);
		}

		// remap to JSON types
		List<Map<String, Object>> callsConfigUpdateCollection = new ArrayList<>();
		List<Map<String, Object>> callsConfigDeleteCollection = new ArrayList<>();

		for (Map<String, Object> callConfig : configCollection) {
			if (callConfig.get("defaultValues") == MapTableModel.nullLIST) {
				callsConfigDeleteCollection.add(callConfig);
			} else if (!restrictToMissing || usedConfigIds.contains(callConfig.get("ident"))) {
				callConfig.put("defaultValues", callConfig.get("defaultValues"));
				callConfig.put("possibleValues", callConfig.get("possibleValues"));
				callsConfigUpdateCollection.add(callConfig);
			} else {
				// Do nothing, config does not need to be deleted or updated
			}
		}

		updateConfigsOnServer(createItems, callsConfigDeleteCollection, callsConfigUpdateCollection);

		retrieveConfigOptionsPD();
		configCollection.clear();

		Logging.info(this, "setConfig(),  configCollection result: " + configCollection);
	}

	private void updateConfigsOnServer(List<Map<String, Object>> createItems,
			List<Map<String, Object>> callsConfigDeleteCollection,
			List<Map<String, Object>> callsConfigUpdateCollection) {
		Logging.debug(this, "setConfig() createItems " + createItems);
		if (!createItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_CREATE_OBJECTS, new Object[] { createItems });
			exec.doCall(omc);
		}

		Logging.debug(this, "setConfig() callsConfigDeleteCollection " + callsConfigDeleteCollection);

		if (!callsConfigDeleteCollection.isEmpty()) {
			exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS,
					new Object[] { callsConfigDeleteCollection }));
			persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
			// because of referential integrity
			persistenceController.reloadData(CacheIdentifier.HOST_CONFIGS.toString());
		}

		Logging.debug(this, "setConfig() callsConfigUpdateCollection " + callsConfigUpdateCollection);

		if (!callsConfigUpdateCollection.isEmpty()) {
			exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS,
					new Object[] { callsConfigUpdateCollection }));
			persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		}
	}

	// collect config updates
	public void setConfig(Map<String, List<Object>> settings) {
		Logging.debug(this, "setConfig settings " + settings);
		if (configCollection == null) {
			configCollection = new ArrayList<>();
		}

		Map<String, ConfigOption> configOptions = getConfigOptionsPD();

		for (Entry<String, List<Object>> setting : settings.entrySet()) {
			Logging.debug(this, "setConfig,  key, settings.get(key): " + setting.getKey() + ", " + setting.getValue());

			Logging.debug(this, "setConfig,  settings.get(key), settings.get(key).getClass().getName(): "
					+ setting.getValue() + " , " + setting.getValue().getClass().getName());

			List<Object> oldValue = null;

			if (configOptions.get(setting.getKey()) != null) {
				oldValue = configOptions.get(setting.getKey()).getDefaultValues();
			}

			Logging.info(this, "setConfig, key, oldValue: " + setting.getKey() + ", " + oldValue);

			if (!setting.getValue().equals(oldValue)) {
				Map<String, Object> config = new HashMap<>();

				config.put("ident", setting.getKey());

				String type;

				Logging.debug(this, "setConfig, key,  configOptions.get(key):  " + setting.getKey() + ", "
						+ configOptions.get(setting.getKey()));
				if (configOptions.get(setting.getKey()) != null) {
					type = (String) configOptions.get(setting.getKey()).get("type");
				} else if (!setting.getValue().isEmpty() && setting.getValue().get(0) instanceof Boolean) {
					type = "BoolConfig";
				} else {
					type = "UnicodeConfig";
				}

				config.put("type", type);

				config.put("defaultValues", setting.getValue());

				List<Object> possibleValues = createPossibleValues(type, setting.getValue(),
						configOptions.get(setting.getKey()));

				config.put("possibleValues", possibleValues);

				configCollection.add(config);
			}
		}
	}

	private static List<Object> createPossibleValues(String type, List<Object> defaultValues,
			ConfigOption configOption) {
		List<Object> possibleValues;
		if (configOption == null) {
			possibleValues = new ArrayList<>();
			if (type.equals(ConfigOption.BOOL_TYPE)) {
				possibleValues.add(true);
				possibleValues.add(false);
			}
		} else {
			possibleValues = configOption.getPossibleValues();
		}

		for (Object item : defaultValues) {
			if (!possibleValues.contains(item)) {
				possibleValues.add(item);
			}
		}

		return possibleValues;
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
		SavedSearches savedSearches = getSavedSearchesPD();
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
		if (!cacheManager.isDataCached(CacheIdentifier.CONFIG_DEFAULT_VALUES)) {
			retrieveConfigOptionsPD();
		}
		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		return Utils.takeAsStringList(configDefaultValues.get(KEY_DISABLED_CLIENT_ACTIONS));
	}

	public List<String> getOpsiclientdExtraEvents() {
		Logging.debug(this, "getOpsiclientdExtraEvents");

		if (!cacheManager.isDataCached(CacheIdentifier.CONFIG_DEFAULT_VALUES)) {
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

	public Map<String, Object> getHostConfig(String objectId) {
		Map<String, Object> hostConfig = new HashMap<>();
		if (getHostConfigsPD().get(objectId) != null) {
			hostConfig.putAll(getHostConfigsPD().get(objectId));
		}
		return new ConfigName2ConfigValue(hostConfig, getConfigOptionsPD());
	}

	public List<Map<String, Object>> getHostsConfigsWithDefaults(List<String> objectIds) {
		List<Map<String, Object>> result = new ArrayList<>();
		Set<String> configIds = new HashSet<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_GET_VALUES,
				new Object[] { configIds, objectIds, true });
		Map<String, Object> retrieved = exec.getMapResult(omc);
		for (Entry<String, Object> entry : retrieved.entrySet()) {
			Map<String, Object> configs = POJOReMapper.remap(entry.getValue(),
					new TypeReference<Map<String, Object>>() {
					});
			result.add(new ConfigName2ConfigValue(configs, getConfigOptionsPD()));
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

		for (Entry<String, Object> entry : settings.entrySet()) {
			Map<String, Object> state = new HashMap<>();
			state.put("type", "ConfigState");
			state.put("objectId", objectId);
			state.put("configId", entry.getKey());
			state.put("values", entry.getValue());

			Map<String, Object> retrievedConfig = settings.getRetrieved();

			if (retrievedConfig == null) {
				configStateCollection.add(state);
			} else if (entry.getValue() != retrievedConfig.get(entry.getKey())) {
				configStateCollection.add(state);

				// we hope that the update works and directly update the retrievedConfig
				retrievedConfig.put(entry.getKey(), entry.getValue());
			} else {
				// Do nothing when retrieved config is not null and equals entry value
			}
		}
	}

	// send config updates and clear the collection
	public void setAdditionalConfiguration() {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
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

		updateAdditionalConfigsOnServer(doneList, usedConfigIds, typesOfUsedConfigIds);

		// at any rate:
		configStateCollection.clear();
	}

	private void updateAdditionalConfigsOnServer(List<Object> doneList, Set<String> usedConfigIds,
			Map<String, String> typesOfUsedConfigIds) {
		Logging.debug(this, "setAdditionalConfiguration(), usedConfigIds: " + usedConfigIds);
		Logging.debug(this, "setAdditionalConfiguration(), deleteConfigStateItems  " + deleteConfigStateItems);
		// not used
		if (!deleteConfigStateItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_DELETE_OBJECTS,
					new Object[] { deleteConfigStateItems });

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
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_CREATE_OBJECTS, new Object[] { createItems });
			exec.doCall(omc);
			persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		}

		// build calls
		List<Map<String, Object>> callsConfigName2ConfigValueCollection = new ArrayList<>();

		for (Map<String, Object> state : configStateCollection) {
			state.put("values", state.get("values"));
			callsConfigName2ConfigValueCollection.add(state);
		}

		// do call
		if (!callsConfigName2ConfigValueCollection.isEmpty()) {
			// now we can set the values and clear the collected update items
			exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
					new Object[] { callsConfigName2ConfigValueCollection }));
		}
	}

	public Boolean isInstallByShutdownConfigured(String hostId) {
		String key = OpsiServiceNOMPersistenceController.KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN;
		Logging.debug(this, "getHostBooleanConfigValue key '" + key + "', host '" + hostId + "'");
		Boolean value = null;

		Map<String, Object> hostConfig = getHostConfigsPD().get(hostId);
		if (hostConfig != null && hostConfig.get(key) != null && !((List<?>) (hostConfig.get(key))).isEmpty()) {
			value = Utils.interpretAsBoolean(((List<?>) hostConfig.get(key)).get(0), (Boolean) null);
			Logging.debug(this, "getHostBooleanConfigValue key '" + key + "', host '" + hostId + "', value: " + value);
			if (value != null) {
				return value;
			}
		}

		value = getGlobalBooleanConfigValue(key, null);
		if (value != null) {
			Logging.debug(this,
					"getHostBooleanConfigValue key '" + key + "', host '" + hostId + "', global value: " + value);
			return value;
		}
		Logging.info(this, "getHostBooleanConfigValue key '" + key + "', host '" + hostId
				+ "', returning default value: " + false);
		return false;
	}

	public Boolean isWanConfigured(String host) {
		Map<String, List<Object>> wanConfiguration = getWanConfigurationPD();
		Logging.info(this, " isWanConfigured wanConfiguration  " + wanConfiguration + " for host " + host);
		return findBooleanConfigurationComparingToDefaults(host, wanConfiguration);
	}

	public Boolean isUefiConfigured(String hostname) {
		Boolean result = false;

		if (getHostConfigsPD().get(hostname) != null
				&& getHostConfigsPD().get(hostname)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME) != null
				&& !((List<?>) getHostConfigsPD().get(hostname)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME)).isEmpty()) {
			String configValue = (String) ((List<?>) getHostConfigsPD().get(hostname)
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

	/**
	 * Checks if the given clients have an entry for UEFI boot. That means, the
	 * client has a client config with an entry
	 * {@code clientconfig.uefinetbootlabel}.
	 * <p>
	 * Should only be used in opsi 4.3 (or later) since in opsi 4.3
	 * {@code clientconfig.dhcpd.filename} entry has been replaced by
	 * {@code clientconfig.uefinetbootlabel} entry and is no longer editable.
	 *
	 * @param clients for which to check the existence of UEFI boot entry
	 * @return null if clients have different values or if client list is empty
	 * @see #isUefiConfigured(String)
	 */
	@SuppressWarnings({ "java:S2447" })
	public Boolean isUEFI43(Iterable<String> clients) {
		Boolean isUEFI = null;

		for (String client : clients) {
			Map<String, Object> clientConfig = getHostConfigsPD().get(client);
			if (clientConfig == null) {
				isUEFI = false;
				continue;
			}

			Object uefiConfig = clientConfig.get("clientconfig.uefinetbootlabel");

			if (uefiConfig instanceof List && !((List<?>) uefiConfig).isEmpty()) {
				if (Boolean.FALSE.equals(isUEFI)) {
					return null;
				} else {
					isUEFI = true;
				}
			} else {
				if (Boolean.TRUE.equals(isUEFI)) {
					return null;
				} else {
					isUEFI = false;
				}
			}
		}

		return isUEFI;
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
			if (configuration.getValue() == null) {
				Logging.info(this, "We encountered non BOOL_CONFIG option " + configuration.getKey() + "; We skip it");
			} else {
				tested = valueFromConfigStateAsExpected(getHostConfig(host), configuration.getKey(),
						(Boolean) (configuration.getValue().get(0)));
				if (!tested) {
					break;
				}
			}
		}
		return tested;
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
			if (getHostConfigsPD().get(clientId) == null) {
				getHostConfigsPD().put(clientId, new HashMap<>());
			}

			Logging.info(this,
					"configureUefiBoot, configs for clientId " + clientId + " " + getHostConfigsPD().get(clientId));
			getHostConfigsPD().get(clientId).put(OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME, values);
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
		retrieveConfigOptionsPD();
		return Utils.takeAsStringList(getConfigDefaultValuesPD().get(key));
	}

	/**
	 * Retrieve available backends.
	 * <p>
	 * This methods is only a viable option for servers/depots, that has opsi
	 * 4.2 or lower; Due to the RPC method deprecation in opsi 4.3.
	 * 
	 * @return available backends
	 */
	public String getBackendInfos() {
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

			List<Map<String, Object>> backend = backends.computeIfAbsent(backendName, name -> new ArrayList<>());
			backend.add(listEntry);
		}

		StringBuilder buf = new StringBuilder();
		buf.append("<table border='0' cellspacing='0' cellpadding='0'>\n");

		for (Entry<String, List<Map<String, Object>>> backendEntry : backends.entrySet()) {
			buf.append("<tr><td bgcolor='#fbeca5' color='#000000'  width='100%'  colspan='3'  align='left'>");
			buf.append("<font size='" + titleSize + "'><b>" + backendEntry.getKey() + "</b></font></td></tr>");

			for (Map<String, Object> listEntry : backendEntry.getValue()) {
				addMapEntriesToString(buf, listEntry, fontSizeBig, fontSizeSmall);
			}

			buf.append(
					"<tr><td bgcolor='#ffffff' color='#000000' width='100%' height='30px' colspan='3'>&nbsp;</td></tr>");
		}

		buf.append("</table>\n");

		return buf.toString();
	}

	private void addMapEntriesToString(StringBuilder buf, Map<String, Object> listEntry, String fontSizeBig,
			String fontSizeSmall) {
		boolean entryIsEven = false;
		String bgColor = "#ffffff";
		for (Entry<String, Object> mapEntry : listEntry.entrySet()) {
			if ("name".equals(mapEntry.getKey())) {
				continue;
			}

			entryIsEven = !entryIsEven;
			if (entryIsEven) {
				bgColor = "#dedeff";
			} else {
				bgColor = "#ffffff";
			}

			buf.append("<tr height='8px'>");
			buf.append("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
					+ fontSizeBig + "'>" + mapEntry.getKey() + "</font></td>");

			if ("config".equals(mapEntry.getKey())) {
				buf.append("<td colspan='2'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
						+ fontSizeBig + "'>&nbsp;</font></td>");
				buf.append("</tr>");

				Map<String, Object> configItems = exec.getMapFromItem(mapEntry.getValue());

				for (Entry<String, Object> configItem : configItems.entrySet()) {
					buf.append("<td bgcolor='" + bgColor + "'>&nbsp;</td>");
					buf.append("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
							+ fontSizeSmall + "'>" + configItem.getKey() + "</font></td>");
					buf.append("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
							+ fontSizeSmall + "'>" + configItem.getValue() + "</font></td>");
					buf.append("</tr>");
				}
			} else {
				buf.append("<td width='300px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
						+ fontSizeBig + "'>" + mapEntry.getValue() + "</font></td>");
				buf.append("</tr>");
			}
		}

		buf.append("<tr height='10px'><td bgcolor='" + bgColor + "' colspan='3'></td></tr>");
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

			Map<String, Integer> numberedValues = new HashMap<>();
			Set<String> orderedValues = new TreeSet<>();
			Set<String> unorderedValues = new TreeSet<>();

			sortValues(numberedValues, orderedValues, unorderedValues,
					configDefaultValues.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY));

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

	private void sortValues(Map<String, Integer> numberedValues, Set<String> orderedValues, Set<String> unorderedValues,
			List<Object> domainsGiven) {
		for (Object item : domainsGiven) {
			String entry = (String) item;
			int p = entry.indexOf(":");
			if (p == -1 || p == 0) {
				unorderedValues.add(entry);
			} else if (p > 0) {
				// the only regular case
				sortRegularValue(entry, p, numberedValues, orderedValues, unorderedValues);
			} else {
				Logging.warning(this, "p has unexpected value " + p);
			}
		}
	}

	private void sortRegularValue(String entry, int p, Map<String, Integer> numberedValues, Set<String> orderedValues,
			Set<String> unorderedValues) {
		try {
			int orderNumber = Integer.parseInt(entry.substring(0, p));
			String value = entry.substring(p + 1);
			if (numberedValues.get(value) == null || orderNumber < numberedValues.get(value)) {
				orderedValues.add(entry);
				numberedValues.put(value, orderNumber);
			}
		} catch (NumberFormatException x) {
			Logging.warning(this, "illegal order format for domain entry: " + entry);
			unorderedValues.add(entry);
		}
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
}
