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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiHwAuditDevicePropertyType;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import utils.Utils;

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
	public static final String LAST_SEEN_VISIBLE_COL_NAME = "HOST.last_scan_time";

	private CacheManager cacheManager;
	private AbstractExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private ConfigDataService configDataService;
	private HostInfoCollections hostInfoCollections;

	public HardwareDataService(AbstractExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	public List<Map<String, Object>> getHardwareOnClientPD() {
		retrieveHardwareOnClientPD();
		return cacheManager.getCachedData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST, List.class);
	}

	public void retrieveHardwareOnClientPD() {
		if (cacheManager.getCachedData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST, List.class) != null) {
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
		if (cacheManager.getCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, Map.class) != null) {
			return;
		}

		cacheManager.setCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, new TreeMap<>());
		if (getOpsiHWAuditConfPD().isEmpty()) {
			Logging.error(this, "no hwaudit config found ");
			return;
		}

		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = new TreeMap<>();
		for (Map<String, List<Map<String, Object>>> hwAuditClass : getOpsiHWAuditConfPD()) {
			if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) == null
					|| hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) == null) {
				Logging.warning(this, "getAllHwClassNames illegal hw config item, having hwAuditClass.get Class "
						+ hwAuditClass.get("Class"));
				if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) != null) {
					Logging.warning(this,
							"getAllHwClassNames illegal hw config item,  hwAuditClass.get Class is of class "
									+ hwAuditClass.get("Class").getClass());
				}
				if (hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) != null) {
					Logging.warning(this,
							"getAllHwClassNames illegal hw config item,  hwAuditClass.get Values is of class "
									+ hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY).getClass());
				}

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

			for (Object m : (List<?>) hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY)) {
				if (!(m instanceof Map)) {
					Logging.warning(this, "getAllHwClassNames illegal VALUES item, m " + m);
					continue;
				}

				Map<?, ?> ma = (Map<?, ?>) m;

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

	public List<String> getAllHwClassNamesPD() {
		retrieveHwClassesPD(getOpsiHWAuditConfPD());
		List<String> opsiHwClassNames = cacheManager.getCachedData(CacheIdentifier.OPSI_HW_CLASS_NAMES, List.class);
		Logging.info(this, "getAllHwClassNames, hw classes " + opsiHwClassNames);
		return opsiHwClassNames;
	}

	// partial version of produceHwAuditDeviceClasses()
	public List<String> retrieveHwClassesPD(Iterable<Map<String, List<Map<String, Object>>>> hwAuditConf) {
		if (cacheManager.getCachedData(CacheIdentifier.OPSI_HW_CLASS_NAMES, List.class) != null) {
			return new ArrayList<>();
		}

		List<String> result = new ArrayList<>();
		for (Map<String, List<Map<String, Object>>> hwAuditClass : hwAuditConf) {
			String hwClass = (String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.OPSI_KEY);
			result.add(hwClass);
		}
		cacheManager.setCachedData(CacheIdentifier.OPSI_HW_CLASS_NAMES, result);
		return result;
	}

	public List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConfPD(String locale) {
		retrieveOpsiHWAuditConfPD(locale);
		Map<String, List<Map<String, List<Map<String, Object>>>>> hwAuditConf = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class);
		return hwAuditConf.get(locale);
	}

	public List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConfPD() {
		retrieveOpsiHWAuditConfPD();
		Map<String, List<Map<String, List<Map<String, Object>>>>> hwAuditConf = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class);
		if (!hwAuditConf.containsKey("")) {
			Logging.warning(this, "got no hardware config");
		}
		return hwAuditConf.get("");
	}

	public void retrieveOpsiHWAuditConfPD() {
		retrieveOpsiHWAuditConfPD("");
	}

	public void retrieveOpsiHWAuditConfPD(String locale) {
		if (cacheManager.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class) != null) {
			return;
		}
		Map<String, List<Map<String, List<Map<String, Object>>>>> hwAuditConf = new HashMap<>();
		hwAuditConf.computeIfAbsent(locale, s -> exec.getListOfMapsOfListsOfMaps(
				new OpsiMethodCall(RPCMethodName.AUDIT_HARDWARE_GET_CONFIG, new String[] { locale })));
		cacheManager.setCachedData(CacheIdentifier.HW_AUDIT_CONF, hwAuditConf);
	}

	public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts) {
		Logging.info(this, "retrieveClient2HwRows( hosts )  for hosts " + hosts.length);
		Map<String, Map<String, Object>> client2HwRows = new HashMap<>();

		// set default rows
		for (String host : hostInfoCollections.getOpsiHostNames()) {
			Map<String, Object> nearlyEmptyHwRow = new HashMap<>();
			nearlyEmptyHwRow.put("HOST.hostId", host);

			String hostDescription = "";
			String macAddress = "";
			if (hostInfoCollections.getMapOfPCInfoMaps().get(host) != null) {
				hostDescription = hostInfoCollections.getMapOfPCInfoMaps().get(host).getDescription();
				macAddress = hostInfoCollections.getMapOfPCInfoMaps().get(host).getMacAddress();
			}
			nearlyEmptyHwRow.put("HOST.description", hostDescription);
			nearlyEmptyHwRow.put("HOST.hardwareAdress", macAddress);

			client2HwRows.put(host, nearlyEmptyHwRow);
		}

		TimeCheck timeCheck = new TimeCheck(this, " retrieveClient2HwRows all ");
		timeCheck.start();

		for (String hwClass : getHwInfoClassNamesPD()) {
			Logging.info(this, "retrieveClient2HwRows hwClass " + hwClass);

			Map<String, Map<String, Object>> client2ClassInfos = client2HwRowsForHwClass(hwClass);

			if (!client2ClassInfos.isEmpty()) {
				for (Entry<String, Map<String, Object>> client2ClassInfo : client2ClassInfos.entrySet()) {
					Map<String, Object> allInfosForAClient = client2HwRows.get(client2ClassInfo.getKey());
					// find max lastseen time as last scan time

					String lastseen1 = (String) allInfosForAClient.get(LAST_SEEN_VISIBLE_COL_NAME);
					String lastseen2 = (String) client2ClassInfo.getValue().get(LAST_SEEN_VISIBLE_COL_NAME);
					if (lastseen1 != null && lastseen2 != null) {
						client2ClassInfo.getValue().put(LAST_SEEN_VISIBLE_COL_NAME, maxTime(lastseen1, lastseen2));
					}

					allInfosForAClient.putAll(client2ClassInfo.getValue());
				}
			}
		}

		Logging.info(this, "retrieveClient2HwRows result size " + client2HwRows.size());

		timeCheck.stop();
		Logging.info(this, "retrieveClient2HwRows finished  ");
		persistenceController.notifyPanelCompleteWinProducts();
		return client2HwRows;
	}

	private Map<String, Map<String, Object>> client2HwRowsForHwClass(String hwClass) {
		Logging.info(this, "client2HwRowsForHwClass " + hwClass);

		List<String> specificColumns = new ArrayList<>();
		specificColumns.add("HOST.hostId");

		StringBuilder buf = new StringBuilder("select HOST.hostId, ");
		StringBuilder cols = new StringBuilder("");

		String configTable = HW_INFO_CONFIG + hwClass;

		String lastseenCol = configTable + "." + "lastseen";
		specificColumns.add(lastseenCol);
		buf.append(lastseenCol);
		buf.append(", ");

		boolean foundAnEntry = false;

		// build and collect database columnnames
		for (String hwInfoCol : getClient2HwRowsColumnNamesPD()) {
			if (hwInfoCol.startsWith("HOST.") || hwInfoCol.equals(LAST_SEEN_VISIBLE_COL_NAME)) {
				continue;
			}

			Logging.info(this,
					"hwInfoCol " + hwInfoCol + " look for " + HW_INFO_DEVICE + " as well as " + HW_INFO_CONFIG);
			String part0 = hwInfoCol.substring(0, HW_INFO_DEVICE.length());

			boolean colFound = false;
			// check if colname is from a CONFIG or a DEVICE table
			if (hwInfoCol.startsWith(hwClass, part0.length())) {
				colFound = true;
				// we found a DEVICE column name
			} else {
				part0 = hwInfoCol.substring(0, HW_INFO_CONFIG.length());

				if (hwInfoCol.startsWith(hwClass, part0.length())) {
					colFound = true;
					// we found a CONFIG column name
				}

			}

			if (colFound) {
				cols.append(" ");
				cols.append(hwInfoCol);
				cols.append(",");
				specificColumns.add(hwInfoCol);
				foundAnEntry = true;
			}
		}

		if (!foundAnEntry) {
			Logging.info(this, "no columns found for hwClass " + hwClass);
			return new HashMap<>();
		}

		String deviceTable = HW_INFO_DEVICE + hwClass;

		String colsS = cols.toString();
		buf.append(colsS.substring(0, colsS.length() - 1));

		buf.append(" \nfrom HOST ");

		buf.append(", ");
		buf.append(deviceTable);
		buf.append(", ");
		buf.append(configTable);

		buf.append("\n where ");

		buf.append("HOST.hostId");
		buf.append(" = ");
		buf.append(configTable);
		buf.append(".hostId");

		buf.append("\nAND ");
		buf.append(configTable);
		buf.append(".hardware_id");
		buf.append(" = ");
		buf.append(deviceTable);
		buf.append(".hardware_id");

		buf.append("\nAND ");
		buf.append(configTable);
		buf.append(".state = 1 ");

		String query = buf.toString();

		Logging.info(this, "retrieveClient2HwRows, query " + query);

		List<List<String>> rows = exec
				.getListOfStringLists(new OpsiMethodCall(RPCMethodName.GET_RAW_DATA, new Object[] { query }));
		Logging.info(this, "retrieveClient2HwRows, finished a request");
		Logging.info(this, "retrieveClient2HwRows, got rows for class " + hwClass);
		Logging.info(this, "retrieveClient2HwRows, got rows,  size  " + rows.size());

		// shrink to one line per client

		Map<String, Map<String, Object>> clientInfo = new HashMap<>();

		for (List<String> row : rows) {
			Map<String, Object> rowMap = clientInfo.computeIfAbsent(row.get(0), s -> new HashMap<>());

			for (int i = 1; i < specificColumns.size(); i++) {
				Object value = rowMap.get(specificColumns.get(i));
				String valInRow = row.get(i);
				if (valInRow == null || "null".equals(valInRow)) {
					valInRow = "";
				}

				if (value == null) {
					value = valInRow;
				} else {
					value = value + "|" + valInRow;
				}

				if (specificColumns.get(i).equals(lastseenCol)) {
					String timeS = maxTime((String) value, row.get(i));
					rowMap.put(LAST_SEEN_VISIBLE_COL_NAME, timeS);
				} else {
					rowMap.put(specificColumns.get(i), value);
				}

			}

		}

		Logging.info(this, "retrieveClient2HwRows, got clientInfo, with size " + clientInfo.size());
		return clientInfo;
	}

	private static String maxTime(String time0, String time1) {
		String result = null;
		if (time0 == null && time1 == null) {
			result = null;
		} else if (time0 == null || "".equals(time0)) {
			result = time1;
		} else if (time1 == null || "".equals(time1)) {
			result = time0;
		} else if (time0.compareTo(time1) < 0) {
			result = time1;
		} else {
			result = time0;
		}

		return result;
	}

	public List<String> getHostColumnNamesPD() {
		retrieveClient2HwRowsColumnNamesPD();
		return cacheManager.getCachedData(CacheIdentifier.HOST_COLUMN_NAMES, List.class);
	}

	public List<String> getClient2HwRowsColumnNamesPD() {
		retrieveClient2HwRowsColumnNamesPD();
		return cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES, List.class);
	}

	public List<String> getClient2HwRowsJavaclassNamesPD() {
		retrieveClient2HwRowsColumnNamesPD();
		return cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES, List.class);
	}

	public List<String> getHwInfoClassNamesPD() {
		retrieveClient2HwRowsColumnNamesPD();
		return cacheManager.getCachedData(CacheIdentifier.HW_INFO_CLASS_NAMES, List.class);
	}

	public void retrieveClient2HwRowsColumnNamesPD() {
		configDataService.retrieveConfigOptionsPD();
		Logging.info(this, "retrieveClient2HwRowsColumnNames " + "client2HwRowsColumnNames == null " + (CacheManager
				.getInstance().getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES, List.class) == null));
		if (cacheManager.getCachedData(CacheIdentifier.HOST_COLUMN_NAMES, List.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES, List.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.HW_INFO_CLASS_NAMES, List.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES, List.class) != null) {
			return;
		}

		List<String> hostColumnNames = new ArrayList<>();

		// todo make static variables
		hostColumnNames.add("HOST.hostId");
		hostColumnNames.add("HOST.description");
		hostColumnNames.add("HOST.hardwareAdress");
		hostColumnNames.add(LAST_SEEN_VISIBLE_COL_NAME);

		// there is produced client2HwRowsColumnNames

		List<String> client2HwRowsColumnNames = new ArrayList<>(hostColumnNames);

		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, Map.class);
		for (Entry<String, OpsiHwAuditDeviceClass> hwClass : hwAuditDeviceClasses.entrySet()) {
			OpsiHwAuditDeviceClass hwAuditDeviceClass = hwClass.getValue();

			for (OpsiHwAuditDevicePropertyType deviceProperty : hwAuditDeviceClass.getDeviceHostProperties()) {
				if (deviceProperty.getDisplayed() != null && deviceProperty.getDisplayed()) {
					String col = HW_INFO_CONFIG + hwClass.getKey() + "." + deviceProperty.getOpsiDbColumnName();
					client2HwRowsColumnNames.add(col);
				}
			}

			for (OpsiHwAuditDevicePropertyType deviceProperty : hwAuditDeviceClass.getDeviceHwItemProperties()) {
				if (deviceProperty.getDisplayed() != null && deviceProperty.getDisplayed()) {
					String col = HW_INFO_DEVICE + hwClass.getKey() + "." + deviceProperty.getOpsiDbColumnName();
					client2HwRowsColumnNames.add(col);
				}
			}
		}
		List<String> client2HwRowsJavaclassNames = new ArrayList<>();
		Set<String> hwInfoClasses = new HashSet<>();

		for (String columnName : client2HwRowsColumnNames) {
			Logging.info(this, "retrieveClient2HwRowsColumnNames col " + columnName);
			client2HwRowsJavaclassNames.add("java.lang.String");
			String className = cutClassName(columnName);
			if (className != null) {
				hwInfoClasses.add(className);
			}
		}

		List<String> hwInfoClassNames = new ArrayList<>(hwInfoClasses);

		Logging.info(this, "retrieveClient2HwRowsColumnNames hwInfoClassNames " + hwInfoClassNames);
		cacheManager.setCachedData(CacheIdentifier.HOST_COLUMN_NAMES, hostColumnNames);
		cacheManager.setCachedData(CacheIdentifier.HW_INFO_CLASS_NAMES, hwInfoClassNames);
		cacheManager.setCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES, client2HwRowsColumnNames);
		cacheManager.setCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES, client2HwRowsJavaclassNames);
	}

	private String cutClassName(String columnName) {
		String result = null;

		if (!columnName.startsWith("HOST") && columnName.startsWith(HW_INFO_CONFIG)) {
			result = columnName.substring(HW_INFO_CONFIG.length());
			result = result.substring(0, result.indexOf('.'));
		} else if (columnName.startsWith(HW_INFO_DEVICE)) {
			result = columnName.substring(HW_INFO_DEVICE.length());
			result = result.substring(0, result.indexOf('.'));
		} else {
			Logging.warning(this, "cutClassName " + "unexpected columnName " + columnName);
		}

		return result;
	}

	private Map<String, Object> produceHwAuditColumnConfig(String configKey,
			List<OpsiHwAuditDevicePropertyType> deviceProperties, Map<String, Boolean> tableConfigUpdates) {
		List<Object> oldDefaultValues = new ArrayList<>();

		Map<String, ConfigOption> configOptions = cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);
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

		Map<String, ConfigOption> configOptions = cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);
		List<Object> readyObjects = new ArrayList<>();

		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, Map.class);
		for (Entry<String, OpsiHwAuditDeviceClass> hwClass : hwAuditDeviceClasses.entrySet()) {
			OpsiHwAuditDeviceClass hwAuditDeviceClass = hwAuditDeviceClasses.get(hwClass.getKey());

			// case hostAssignedTableType
			String configKey = hwAuditDeviceClass.getHostConfigKey();
			String configIdent = hwClass.getKey() + "_" + OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE;

			Logging.debug(this, " saveHwColumnConfig for HOST configIdent " + configIdent);

			Map<String, Boolean> tableConfigUpdates = updateItems.get(configIdent.toUpperCase(Locale.ROOT));

			if (tableConfigUpdates != null) {
				Logging.info(this,
						" saveHwColumnConfig tableConfigUpdates  for the host configIdent,  " + tableConfigUpdates);
			}

			// we have got updates for this table configuration
			if (tableConfigUpdates != null) {
				Map<String, Object> configItem = produceHwAuditColumnConfig(configKey,
						hwAuditDeviceClass.getDeviceHostProperties(), tableConfigUpdates);

				readyObjects.add(configItem);

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				// save the data locally, we hope that the upload later will work as well

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

			if (tableConfigUpdates != null) {
				Logging.info(this,
						" saveHwColumnConfig tableConfigUpdates  for the hw configIdent,  " + tableConfigUpdates);
			}

			// we have got updates for this table configuration
			if (tableConfigUpdates != null) {

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
			if (result.containsKey(hardwareInfo.get("hardwareClass"))) {
				List<Map<String, Object>> hardwareClassInfos = result.get(hardwareInfo.get("hardwareClass"));
				hardwareClassInfos.add(hardwareInfo);
			} else {
				List<Map<String, Object>> hardwareClassInfos = new ArrayList<>();
				hardwareClassInfos.add(hardwareInfo);
				result.put((String) hardwareInfo.get("hardwareClass"), hardwareClassInfos);
			}
			Object lastSeenStr = hardwareInfo.get("lastseen");
			LocalDateTime lastSeen = scanTime;
			if (lastSeenStr != null) {
				lastSeen = LocalDateTime.parse(lastSeenStr.toString(), timeFormatter);
			}
			if (scanTime.compareTo(lastSeen) < 0) {
				scanTime = lastSeen;
			}
		}

		List<Map<String, Object>> scanProperties = new ArrayList<>();
		Map<String, Object> scanProperty = new HashMap<>();
		scanProperty.put("scantime", scanTime.format(timeFormatter));
		scanProperties.add(scanProperty);
		result.put("SCANPROPERTIES", scanProperties);

		if (result.size() > 1) {
			return result;
		}

		return new HashMap<>();
	}
}
