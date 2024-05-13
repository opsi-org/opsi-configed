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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.uib.messages.Messages;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.RPCMethodName;

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

	public HardwareDataService(AbstractPOJOExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
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
