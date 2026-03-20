/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.gui.features.hwinfopage.PanelHWSingleClientInfo;
import de.uib.configed.gui.messages.Messages;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

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
public class HardwareDataService extends DataService {
	// constants for building hw queries
	public static final String HW_INFO_CONFIG = "HARDWARE_CONFIG_";
	public static final String HW_INFO_DEVICE = "HARDWARE_DEVICE_";

	public HardwareDataService(DataServices dataServices) {
		super(dataServices);
	}

	public List<Map<String, Object>> getOpsiHWAuditConfPD(String locale) {
		retrieveOpsiHWAuditConfPD(locale);
		Map<String, List<Map<String, Object>>> hwAuditConf = dataServices.cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class);
		return hwAuditConf.get(locale);
	}

	public void retrieveOpsiHWAuditConfPD() {
		retrieveOpsiHWAuditConfPD(Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry());
	}

	public void retrieveOpsiHWAuditConfPD(String locale) {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.HW_AUDIT_CONF) && dataServices.cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class).get(locale) != null) {
			return;
		}

		Map<String, List<Map<String, Object>>> hwAuditConf;
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.HW_AUDIT_CONF)) {
			hwAuditConf = dataServices.cacheManager.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class);
		} else {
			hwAuditConf = new HashMap<>();
		}

		hwAuditConf.computeIfAbsent(locale,
				s -> dataServices.exec.getListOfMaps(RPCMethodName.AUDIT_HARDWARE_GET_CONFIG, locale));
		dataServices.cacheManager.setCachedData(CacheIdentifier.HW_AUDIT_CONF, hwAuditConf);
	}

	public Map<String, List<Map<String, Object>>> getHardwareInfo(String clientId) {
		if (clientId == null) {
			return new HashMap<>();
		}

		Map<String, String> callFilter = Map.of("hostId", clientId);

		List<Map<String, Object>> hardwareInfos = dataServices.exec
				.getListOfMaps(RPCMethodName.AUDIT_HARDWARE_ON_HOST_GET_OBJECTS, Set.of(), callFilter);

		// Every "lastseen" is the same for a client, so we take the first one
		String scanTime = hardwareInfos.isEmpty() ? ""
				: Utils.formatDateTimeStringToLocal((String) hardwareInfos.get(0).get("lastseen"));
		Map<String, List<Map<String, Object>>> result = new HashMap<>();
		for (Map<String, Object> hardwareInfo : hardwareInfos) {
			hardwareInfo.values().removeIf(Objects::isNull);
			String hardwareClass = (String) hardwareInfo.get("hardwareClass");
			hardwareInfo.keySet()
					.removeAll(Set.of("firstseen", "lastseen", "state", "hostId", "hardwareClass", "ident"));

			List<Map<String, Object>> hardwareClassInfos = result.computeIfAbsent(hardwareClass,
					s -> new ArrayList<>());
			hardwareClassInfos.add(hardwareInfo);
		}

		// Add the scan time info "lastseen"
		result.put(PanelHWSingleClientInfo.SCANPROPERTYNAME,
				List.of(Map.of(PanelHWSingleClientInfo.SCANTIME, scanTime)));
		return result.size() > 1 ? result : new HashMap<>();
	}

	public Map<String, List<Map<String, Object>>> getHardwareAuditOnClients(Collection<String> clients) {
		Logging.info(this, "getHardwareAuditOnClients used memory on start ", Utils.usedMemory());
		Logging.info(this, "getHardwareAuditOnClients clients count: ", clients.size());
		Map<String, List<Map<String, Object>>> client2hardware = new ConcurrentHashMap<>();

		int stepSize = 100;
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		Iterator<String> clientIterator = clients.iterator();

		while (clientIterator.hasNext()) {
			List<String> clientListForCall = new ArrayList<>();
			for (int i = 0; i < stepSize && clientIterator.hasNext(); i++) {
				clientListForCall.add(clientIterator.next());
			}

			executor.runInParallel(() -> computeClient2Hardware(client2hardware, clientListForCall));
		}

		executor.waitForCompletion();

		Logging.info(this, "getHardwareAuditOnClients used memory on end ", Utils.usedMemory());

		return client2hardware;
	}

	private void computeClient2Hardware(Map<String, List<Map<String, Object>>> client2hardware, List<String> clients) {
		for (Map<String, Object> item : getAuditHardwareOnClients(clients)) {
			String clientId = (String) item.get("hostId");
			if (clientId == null) {
				continue;
			}
			client2hardware.computeIfAbsent(clientId, v -> new ArrayList<>()).add(item);
		}
	}

	private List<Map<String, Object>> getAuditHardwareOnClients(List<String> clients) {
		Logging.info(this, "getAuditHardwareOnClients request started");
		Map<String, Object> callFilter = Map.of("hostId", clients, "state", "1");

		List<Map<String, Object>> hardwareAuditOnClients = dataServices.exec
				.getListOfMaps(RPCMethodName.AUDIT_HARDWARE_ON_HOST_GET_OBJECTS, new String[0], callFilter);
		Logging.info(this, "getAuditHardwareOnClients, finished a request, map size ", hardwareAuditOnClients.size());

		return hardwareAuditOnClients;
	}
}
