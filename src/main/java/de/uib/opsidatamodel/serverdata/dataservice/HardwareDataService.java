/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiHwAuditDevicePropertyType;
import de.uib.messages.Messages;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

/**
 * Provides methods for working with hardware data on the server.
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
public class HardwareDataService {
	// constants for building hw queries
	public static final String HW_INFO_CONFIG = "HARDWARE_CONFIG_";
	public static final String HW_INFO_DEVICE = "HARDWARE_DEVICE_";

	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;
	private ConfigDataService configDataService;

	public HardwareDataService(AbstractPOJOExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public List<Map<String, Object>> getHardwareOnClientPD() {
		retrieveHardwareOnClientPD();
		return cacheManager.getCachedData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST, List.class);
	}

	public void retrieveHardwareOnClientPD() {
		if (cacheManager.isDataCached(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST)) {
			return;
		}
		Map<String, String> filterMap = new HashMap<>();
		filterMap.put("state", "1");
		List<Map<String, Object>> relationsAuditHardwareOnHost = exec.getListOfMaps(new OpsiMethodCall(
				RPCMethodName.AUDIT_HARDWARE_ON_HOST_GET_OBJECTS, new Object[] { new String[0], filterMap }));
		cacheManager.setCachedData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST, relationsAuditHardwareOnHost);
	}

	public Map<String, OpsiHwAuditDeviceClass> getHwAuditDeviceClassesPD() {
		retrieveHwAuditDeviceClassesPD();
		return cacheManager.getCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, Map.class);
	}

	public void retrieveHwAuditDeviceClassesPD() {
		if (cacheManager.isDataCached(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES)) {
			return;
		}

		cacheManager.setCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, new TreeMap<>());
		if (getOpsiHWAuditConfPD(Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry())
				.isEmpty()) {
			Logging.error(this, "no hwaudit config found ");
			return;
		}

		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = new TreeMap<>();
		for (Map<String, List<Map<String, Object>>> hwAuditClass : getOpsiHWAuditConfPD(
				Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry())) {
			if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) == null
					|| hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) == null) {
				Logging.info(this, "getAllHwClassNames illegal hw config item,  hwAuditClass.get Class is "
						+ hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY));
				Logging.info(this, "getAllHwClassNames illegal hw config item,  hwAuditClass.get Values is "
						+ hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY));

				continue;
			}

			String hwClass = (String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.OPSI_KEY);

			OpsiHwAuditDevicePropertyType firstSeen = new OpsiHwAuditDevicePropertyType(hwClass);
			firstSeen.setOpsiDbColumnName(OpsiHwAuditDeviceClass.FIRST_SEEN_COL_NAME);
			firstSeen.setOpsiDbColumnType("timestamp");
			OpsiHwAuditDevicePropertyType lastSeen = new OpsiHwAuditDevicePropertyType(hwClass);
			lastSeen.setOpsiDbColumnName(OpsiHwAuditDeviceClass.LAST_SEEN_COL_NAME);
			lastSeen.setOpsiDbColumnType("timestamp");

			OpsiHwAuditDeviceClass hwAuditDeviceClass = new OpsiHwAuditDeviceClass(hwClass);
			hwAuditDeviceClasses.put(hwClass, hwAuditDeviceClass);

			hwAuditDeviceClass.setLinuxQuery((String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.LINUX_KEY));
			hwAuditDeviceClass.setWmiQuery((String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.WMI_KEY));

			Logging.info(this, "hw audit class " + hwClass);

			for (Map<?, ?> ma : hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY)) {
				if ("i".equals(ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY))) {
					OpsiHwAuditDevicePropertyType devProperty = new OpsiHwAuditDevicePropertyType(hwClass);
					devProperty.setOpsiDbColumnName((String) ma.get(OpsiHwAuditDeviceClass.OPSI_KEY));
					devProperty.setOpsiDbColumnType((String) ma.get(OpsiHwAuditDeviceClass.TYPE_KEY));

					hwAuditDeviceClass.addHostRelatedProperty(devProperty);
					hwAuditDeviceClass.setHostConfigKey((OpsiHwAuditDeviceClass.CONFIG_KEY + "." + hwClass + "_"
							+ OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE).toLowerCase(Locale.ROOT));
				} else if ("g".equals(ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY))) {
					OpsiHwAuditDevicePropertyType devProperty = new OpsiHwAuditDevicePropertyType(hwClass);
					devProperty.setOpsiDbColumnName((String) ma.get(OpsiHwAuditDeviceClass.OPSI_KEY));
					devProperty.setOpsiDbColumnType((String) ma.get(OpsiHwAuditDeviceClass.TYPE_KEY));

					hwAuditDeviceClass.addHwItemRelatedProperty(devProperty);
					hwAuditDeviceClass.setHwItemConfigKey((OpsiHwAuditDeviceClass.CONFIG_KEY + "." + hwClass + "_"
							+ OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE).toLowerCase(Locale.ROOT));
				} else {
					Logging.warning(this, "getAllHwClassNames illegal value for key " + OpsiHwAuditDeviceClass.SCOPE_KEY
							+ " " + ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY));
				}
			}

			hwAuditDeviceClass.addHostRelatedProperty(firstSeen);
			hwAuditDeviceClass.addHostRelatedProperty(lastSeen);

			Logging.info(this, "hw audit class " + hwAuditDeviceClass);
		}

		cacheManager.setCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, hwAuditDeviceClasses);
		Logging.info(this, "produceHwAuditDeviceClasses hwAuditDeviceClasses size " + hwAuditDeviceClasses.size());
	}

	public List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConfPD(String locale) {
		retrieveOpsiHWAuditConfPD(locale);
		Map<String, List<Map<String, List<Map<String, Object>>>>> hwAuditConf = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class);
		return hwAuditConf.get(locale);
	}

	public void retrieveOpsiHWAuditConfPD() {
		retrieveOpsiHWAuditConfPD(Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry());
	}

	public void retrieveOpsiHWAuditConfPD(String locale) {
		if (cacheManager.isDataCached(CacheIdentifier.HW_AUDIT_CONF)
				&& cacheManager.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class).get(locale) != null) {
			return;
		}

		Map<String, List<Map<String, List<Map<String, Object>>>>> hwAuditConf = new HashMap<>();
		hwAuditConf.computeIfAbsent(locale, s -> exec.getListOfMapsOfListsOfMaps(
				new OpsiMethodCall(RPCMethodName.AUDIT_HARDWARE_GET_CONFIG, new String[] { locale })));
		cacheManager.setCachedData(CacheIdentifier.HW_AUDIT_CONF, hwAuditConf);
	}

	private Map<String, Object> produceHwAuditColumnConfig(String configKey,
			List<OpsiHwAuditDevicePropertyType> deviceProperties, Map<String, Boolean> tableConfigUpdates) {
		List<Object> oldDefaultValues = new ArrayList<>();

		Map<String, ConfigOption> configOptions = configDataService.getConfigOptionsPD();
		if (configOptions.get(configKey) != null) {
			oldDefaultValues = configOptions.get(configKey).getDefaultValues();
		}

		Logging.info(this, "produceHwAuditColumnConfig " + oldDefaultValues);

		List<Object> possibleValues = new ArrayList<>();
		for (OpsiHwAuditDevicePropertyType deviceProperty : deviceProperties) {
			possibleValues.add(deviceProperty.getOpsiDbColumnName());
		}

		Logging.info(this, "produceConfig, possibleValues " + possibleValues);

		List<Object> newDefaultValues = new ArrayList<>();
		for (Object value : possibleValues) {
			if (oldDefaultValues.contains(value)) {
				// was in default values and no change, or value is in (old) default values and
				// set again
				if (tableConfigUpdates.get(value) == null || Boolean.TRUE.equals(tableConfigUpdates.get(value))) {
					newDefaultValues.add(value);
				}
			} else if (tableConfigUpdates.get(value) != null && tableConfigUpdates.get(value)) {
				// change, value is now configured
				newDefaultValues.add(value);
			} else {
				// value is contained nowhere
			}
		}

		Map<String, Object> configItem = Utils.createNOMConfig(ConfigOption.TYPE.UNICODE_CONFIG, configKey, "", false,
				true, newDefaultValues, possibleValues);

		Logging.info(this, "produceConfig, created an item " + configItem);

		return configItem;
	}

	public boolean saveHwColumnConfig(Map<String, Map<String, Boolean>> updateItems) {
		configDataService.retrieveConfigOptionsPD();

		Map<String, ConfigOption> configOptions = configDataService.getConfigOptionsPD();
		List<Object> readyObjects = new ArrayList<>();

		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, Map.class);
		for (Entry<String, OpsiHwAuditDeviceClass> hwClass : hwAuditDeviceClasses.entrySet()) {
			OpsiHwAuditDeviceClass hwAuditDeviceClass = hwClass.getValue();

			// case hostAssignedTableType
			String configKey = hwAuditDeviceClass.getHostConfigKey();
			String configIdent = hwClass.getKey() + "_" + OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE;

			Logging.debug(this, " saveHwColumnConfig for HOST configIdent " + configIdent);

			Map<String, Boolean> tableConfigUpdates = updateItems.get(configIdent.toUpperCase(Locale.ROOT));

			// we have got updates for this table configuration
			if (tableConfigUpdates != null) {
				Logging.info(this,
						" saveHwColumnConfig tableConfigUpdates  for the host configIdent,  " + tableConfigUpdates);

				Map<String, Object> configItem = produceHwAuditColumnConfig(configKey,
						hwAuditDeviceClass.getDeviceHostProperties(), tableConfigUpdates);

				readyObjects.add(configItem);

				// now, we have got them in a view model

				Logging.info(this,
						"saveHwColumnConfig, locally saving " + " key " + hwAuditDeviceClass.getHwItemConfigKey());

				Logging.info(this,
						"saveHwColumnConfig, old configOption for key" + " " + hwAuditDeviceClass.getHostConfigKey()
								+ " " + configOptions.get(hwAuditDeviceClass.getHostConfigKey()));

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				ConfigOption configOption = new ConfigOption(configItem);

				configOptions.put(hwAuditDeviceClass.getHostConfigKey(), configOption);
			}

			// case hwItemAssignedTableType
			configKey = hwAuditDeviceClass.getHwItemConfigKey();
			configIdent = hwClass.getKey() + "_" + OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE;

			Logging.debug(this, " saveHwColumnConfig for HW configIdent " + configIdent);

			tableConfigUpdates = updateItems.get(configIdent.toUpperCase(Locale.ROOT));

			// we have got updates for this table configuration
			if (tableConfigUpdates != null) {
				Logging.info(this,
						" saveHwColumnConfig tableConfigUpdates  for the hw configIdent,  " + tableConfigUpdates);

				Map<String, Object> configItem = produceHwAuditColumnConfig(configKey,
						hwAuditDeviceClass.getDeviceHwItemProperties(), tableConfigUpdates);

				readyObjects.add(configItem);

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				// save the data locally, we hope that the upload later will work as well
				// now, we have got them in a view model

				Logging.info(this, "saveHwColumnConfig, produce a ConfigOption from configItem " + configItem);

				Logging.info(this,
						"saveHwColumnConfig, locally saving " + " key " + hwAuditDeviceClass.getHwItemConfigKey());

				Logging.info(this,
						"saveHwColumnConfig, we had configOption for key" + " "
								+ hwAuditDeviceClass.getHwItemConfigKey() + " "
								+ configOptions.get(hwAuditDeviceClass.getHwItemConfigKey()));

				ConfigOption configOption = new ConfigOption(configItem);

				configOptions.put(hwAuditDeviceClass.getHostConfigKey(), configOption);
			}
		}

		Logging.info(this, "saveHwColumnConfig readyObjects " + readyObjects.size());
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

		return exec.doCall(omc);
	}

	public Map<String, List<Map<String, Object>>> getHardwareInfo(String clientId) {
		if (clientId == null) {
			return new HashMap<>();
		}

		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("hostId", clientId);

		List<Map<String, Object>> hardwareInfos = exec.getListOfMaps(new OpsiMethodCall(
				RPCMethodName.AUDIT_HARDWARE_ON_HOST_GET_OBJECTS, new Object[] { callAttributes, callFilter }));

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime scanTime = LocalDateTime.parse("2000-01-01 00:00:00", timeFormatter);
		Map<String, List<Map<String, Object>>> result = new HashMap<>();
		for (Map<String, Object> hardwareInfo : hardwareInfos) {
			hardwareInfo.values().removeIf(Objects::isNull);
			scanTime = getScanTime(hardwareInfo.get("lastseen"), scanTime);
			String hardwareClass = (String) hardwareInfo.get("hardwareClass");
			hardwareInfo.keySet()
					.removeAll(Set.of("firstseen", "lastseen", "state", "hostId", "hardwareClass", "ident"));
			if (result.containsKey(hardwareClass)) {
				List<Map<String, Object>> hardwareClassInfos = result.get(hardwareClass);
				hardwareClassInfos.add(hardwareInfo);
			} else {
				List<Map<String, Object>> hardwareClassInfos = new ArrayList<>();
				hardwareClassInfos.add(hardwareInfo);
				result.put(hardwareClass, hardwareClassInfos);
			}
		}

		List<Map<String, Object>> scanProperties = new ArrayList<>();
		Map<String, Object> scanProperty = new HashMap<>();
		scanProperty.put("scantime", scanTime.format(timeFormatter));
		scanProperties.add(scanProperty);
		result.put("SCANPROPERTIES", scanProperties);
		return result.size() > 1 ? result : new HashMap<>();
	}

	private static LocalDateTime getScanTime(Object currentScanTime, LocalDateTime previousScanTime) {
		LocalDateTime lastSeen = previousScanTime;
		if (currentScanTime != null) {
			lastSeen = LocalDateTime.parse(currentScanTime.toString(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
		if (previousScanTime.compareTo(lastSeen) < 0) {
			previousScanTime = lastSeen;
		}
		return previousScanTime;
	}
}
