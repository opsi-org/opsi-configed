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

import org.json.JSONArray;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SavedSearch;
import de.uib.opsicommand.AbstractExecutioner;
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
	protected static final String KEY_DISABLED_CLIENT_ACTIONS = "configed.host_actions_disabled";
	protected static final String KEY_OPSICLIENTD_EXTRA_EVENTS = "configed.opsiclientd_events";

	private CacheManager cacheManager;
	private AbstractExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private UserRolesConfigDataService userRolesConfigDataService;

	private List<Map<String, Object>> configCollection;
	private List<Map<String, Object>> configStateCollection;
	private List<Map<String, Object>> deleteConfigStateItems;

	public ConfigDataService(AbstractExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
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
				}
			}

			cacheManager.setCachedData(CacheIdentifier.REMOTE_CONTROLS, remoteControls);
			cacheManager.setCachedData(CacheIdentifier.SAVED_SEARCHES, savedSearches);
			cacheManager.setCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS, configListCellOptions);
			cacheManager.setCachedData(CacheIdentifier.CONFIG_OPTIONS, configOptions);
			cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);
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
		if (cacheManager.getCachedData(CacheIdentifier.HOST_CONFIGS, Map.class) != null) {
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
		if (cacheManager.getCachedData(CacheIdentifier.HOST_CONFIGS, Map.class) != null) {
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
			Object id = hostConfig.getKey();

			if (id instanceof String && !"".equals(id)) {
				String hostId = (String) id;
				Map<String, Object> configs1Host = hostConfigs.computeIfAbsent(hostId, arg -> new HashMap<>());
				Map<String, Object> configs = POJOReMapper.remap(hostConfig.getValue(),
						new TypeReference<Map<String, Object>>() {
						});

				for (Entry<String, Object> config : configs.entrySet()) {
					Logging.debug(this, "retrieveHostConfigs objectId,  element " + id + ": " + hostConfig);

					String configId = config.getKey();

					if (hostConfig.getValue() == null) {
						configs1Host.put(configId, new ArrayList<>());
						// is a data error but can occur
					} else {
						configs1Host.put(configId, config.getValue());
					}
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
		Logging.info(this, "setConfig(),  configCollection null " + (configCollection == null));
		if (configCollection != null) {
			Logging.info(this, "setConfig(),  configCollection size  " + configCollection.size());
		}

		if (userRolesConfigDataService.isGlobalReadOnly()) {
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

				if (value != null && !((List<Object>) value).contains(null)) {
					getHostConfigsPD().get(objectId).put(key, value);
				} else {
					getHostConfigsPD().get(objectId).remove(key);
				}
				// we hope that the update works and directly update the retrievedConfig
				if (retrievedConfig != null) {
					retrievedConfig.put(key, value);
				}
			}
		}
	}

	// send config updates and clear the collection
	public void setAdditionalConfiguration() {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
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
				persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
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
			tested = valueFromConfigStateAsExpected(getHostConfig(host), configuration.getKey(),
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

	private Boolean getHostBooleanConfigValue(String key, String hostName, boolean useGlobalFallback,
			Boolean defaultVal) {

		Logging.debug(this, "getHostBooleanConfigValue key '" + key + "', host '" + hostName + "'");
		Boolean value = null;

		Map<String, Object> hostConfig = getHostConfigsPD().get(hostName);
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
		for (Object element : list) {
			Map<String, Object> listEntry = exec.getMapFromItem(element);
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

			for (Map<String, Object> backendEntry : backendEntries) {

				Iterator<String> eIt = backendEntry.keySet().iterator();

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

					Object value = backendEntry.get(key);
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
}
