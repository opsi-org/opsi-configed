/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.dataservice.DataService;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;
import de.uib.configed.gui.features.tree.ClientTree;
import de.uib.configed.gui.type.ConfigName2ConfigValue;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.logging.Logging;

/**
 * Provides a way to retrieve the current data (and update it) about hosts and
 * depots, without requiring to retrieve data from the server.
 * <p>
 * It does it by keeping all of its data internally cached and updating the
 * internally cached data with a new data, when it is required.
 */
@SuppressWarnings({ "unchecked" })
public class HostInfoCollections extends DataService {
	private ClientTree clientTree;

	// We need the argument here since the controller is not loaded yet
	public HostInfoCollections(DataServices dataServices) {
		super(dataServices);
	}

	// deliver data

	private static Map<String, Object> hideOpsiHostKey(Map<String, Object> source) {
		Map<String, Object> result = new HashMap<>(source);
		result.put(HostInfo.HOST_KEY_KEY, "****");
		return result;
	}

	public void setTree(ClientTree clientTree) {
		this.clientTree = clientTree;
	}

	public String getConfigServer() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.CONFIG_SERVER, String.class);
	}

	public Map<String, String> getMapPcBelongsToDepot() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.MAP_PC_BELONGS_TO_DEPOT, Map.class);
	}

	public List<String> getOpsiHostNames() {
		retrieveOpsiHostsPD();
		return new ArrayList<>(dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class));
	}

	public int getCountClients() {
		retrieveOpsiHostsPD();
		List<String> opsiHostNames = dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES,
				List.class);
		List<String> depotNamesList = dataServices.cacheManager.getCachedData(CacheIdentifier.DEPOT_NAMES_LIST,
				List.class);
		boolean removed = opsiHostNames.removeAll(depotNamesList);
		Logging.debug(this, "depots were removed from opsi host names ", removed);
		return opsiHostNames.size();
	}

	public Map<String, Map<String, Object>> getDepots() {
		retrieveOpsiHostsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.MASTER_DEPOTS, Map.class);
	}

	public List<String> getDepotNamesList() {
		retrieveOpsiHostsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.DEPOT_NAMES_LIST, List.class);
	}

	public List<String> getAllDepotNamesList() {
		retrieveOpsiHostsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.ALL_DEPOT_NAMES_LIST, List.class);
	}

	public Map<String, Map<String, Object>> getAllDepots() {
		retrieveOpsiHostsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.ALL_DEPOTS, Map.class);
	}

	public Map<String, HostInfo> getMapOfPCInfoMaps() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.MAP_PC_INFO_MAP, Map.class);
	}

	public Map<String, HostInfo> getMapOfAllPCInfoMaps() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.HOST_TO_HOST_INFO, Map.class);
	}

	public String getConfigServerWebDavBaseURI() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.CONFIG_SERVER_WEBDAV_BASE_URI, String.class);
	}

	public String getConfigServerWebDavPath() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.CONFIG_SERVER_WEBDAV_PATH, String.class);
	}

	// build data
	public void retrieveOpsiHostsPD() {
		Logging.debug(this, "retrieveOpsiHosts , opsiHostNames == null ",
				dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class) == null);

		if (dataServices.cacheManager.isDataCached(CacheIdentifier.OPSI_HOST_NAMES)) {
			return;
		}

		List<Map<String, Object>> opsiHosts = dataServices.host.getOpsiHosts();

		// find opsi configserver and give it the top position
		retrieveConfigServerPD(opsiHosts);

		String configServer = getConfigServer();

		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = new TreeMap<>();
		depot2Host2HostInfo.put(configServer, new TreeMap<>());
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);

		// find depots and build entries for them
		retrieveDepotsPD(opsiHosts);
		retrieveClientsPD(dataServices.host.getOpsiClients());

		Map<String, Map<String, Object>> masterDepots = dataServices.cacheManager
				.getCachedData(CacheIdentifier.MASTER_DEPOTS, Map.class);
		Logging.info(this, "retrieveOpsiHost found masterDepots ", masterDepots.size());

		Map<String, Map<String, HostInfo>> depot2Host2HostInfos = dataServices.cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		for (String depot : masterDepots.keySet()) {
			Logging.info(this, "retrieveOpsiHosts clients in ", depot, ": ", depot2Host2HostInfos.get(depot).size());
		}

		Set<String> depotNamesSorted = new TreeSet<>(masterDepots.keySet());
		Set<String> allDepotNamesSorted = new TreeSet<>(
				dataServices.cacheManager.getCachedData(CacheIdentifier.ALL_DEPOTS, Map.class).keySet());

		depotNamesSorted.remove(configServer);
		allDepotNamesSorted.remove(configServer);

		List<String> depotNamesList = dataServices.cacheManager.getCachedData(CacheIdentifier.DEPOT_NAMES_LIST,
				List.class);
		List<String> allDepotNamesList = new ArrayList<>();
		allDepotNamesList.add(configServer);

		depotNamesList.addAll(depotNamesSorted);
		allDepotNamesList.addAll(allDepotNamesSorted);

		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOT_NAMES_LIST, depotNamesList);
		dataServices.cacheManager.setCachedData(CacheIdentifier.ALL_DEPOT_NAMES_LIST, allDepotNamesList);
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);
		Logging.info(this, "retrieveOpsiHosts  hostnames size ",
				dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class).size());
		Logging.info(this, "retrieveOpsiHosts   depotNamesList size ", depotNamesList.size());
	}

	private void retrieveConfigServerPD(List<Map<String, Object>> opsiHosts) {
		String configServer = "";
		List<String> opsiHostNames = new ArrayList<>();
		List<String> depotNamesList = new ArrayList<>();
		Map<String, Map<String, Object>> allDepots = new HashMap<>();
		Map<String, Map<String, Object>> masterDepots = new LinkedHashMap<>();
		for (Map<String, Object> host : opsiHosts) {
			String name = (String) host.get(HostInfo.HOSTNAME_KEY);
			opsiHostNames.add(name);

			host.replaceAll((key, value) -> value == null ? "" : value);

			boolean isConfigserver = host.get(HostInfo.HOST_TYPE_KEY)
					.equals(HostInfo.HOST_TYPE_VALUE_OPSI_CONFIG_SERVER);

			if (isConfigserver) {
				Logging.info(this, "retrieveOpsiHosts  type opsiconfigserver host ", hideOpsiHostKey(host));

				configServer = name;
				depotNamesList.add(name);
				allDepots.put(name, host);

				if (Boolean.TRUE.equals(host.get(HostInfo.IS_MASTER_DEPOT_KEY))) {
					Map<String, Object> hostMap = new HashMap<>(host);
					masterDepots.put(name, hostMap);
				}

				String workbenchPath = retrieveWorkbenchPath(host);

				if (!workbenchPath.isEmpty()) {
					dataServices.config.setConfigedWorkbenchDefaultValuePD(workbenchPath);
				}

				retrieveConfigServerWebDavURLPD(host);
			}
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.CONFIG_SERVER, configServer);
		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOT_NAMES_LIST, depotNamesList);
		dataServices.cacheManager.setCachedData(CacheIdentifier.ALL_DEPOTS, allDepots);
		dataServices.cacheManager.setCachedData(CacheIdentifier.MASTER_DEPOTS, masterDepots);
	}

	private String retrieveWorkbenchPath(Map<String, Object> host) {
		String filepath = "";
		Object val = host.get(HostInfo.DEPOT_WORKBENCH_KEY);

		if (val != null && !"".equals(val)) {
			try {
				filepath = new URI((String) val).getPath();
				Logging.info(this, "retrieveOpsiHosts workbenchpath ", filepath);
			} catch (URISyntaxException netex) {
				Logging.error(netex, "not a correctly formed file URI: ", val);
			}
		}

		return filepath;
	}

	private void retrieveConfigServerWebDavURLPD(Map<String, Object> host) {
		String webdavURL = (String) host.get(HostInfo.DEPOT_WEBDAV_URL);

		try {
			URI uri = new URI(webdavURL);
			String scheme = uri.getScheme();
			scheme = scheme.startsWith("webdavs") ? "https" : "http";
			String baseURI = scheme + "://" + uri.getAuthority();
			if (!webdavURL.isEmpty()) {
				CacheManager.getInstance().setCachedData(CacheIdentifier.CONFIG_SERVER_WEBDAV_BASE_URI, baseURI);
			}
			if (uri.getPath() != null && !uri.getPath().isEmpty()) {
				CacheManager.getInstance().setCachedData(CacheIdentifier.CONFIG_SERVER_WEBDAV_PATH, uri.getPath());
			}
		} catch (URISyntaxException e) {
			Logging.warning(this, "Failed to retrieve WebDAV URL - malformed URL ", e);
		}
	}

	private void retrieveDepotsPD(List<Map<String, Object>> opsiHosts) {
		Map<String, Map<String, Object>> allDepots = dataServices.cacheManager.getCachedData(CacheIdentifier.ALL_DEPOTS,
				Map.class);
		Map<String, Map<String, Object>> masterDepots = dataServices.cacheManager
				.getCachedData(CacheIdentifier.MASTER_DEPOTS, Map.class);
		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = dataServices.cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		for (Map<String, Object> host : opsiHosts) {
			if (!host.get(HostInfo.HOST_TYPE_KEY).equals(HostInfo.HOST_TYPE_VALUE_OPSI_DEPOT_SERVER)) {
				continue;
			}

			String name = (String) host.get(HostInfo.HOSTNAME_KEY);
			allDepots.put(name, host);

			if (Boolean.TRUE.equals(host.get(HostInfo.IS_MASTER_DEPOT_KEY))) {
				Map<String, Object> hostMap = new HashMap<>(host);
				masterDepots.put(name, hostMap);
				depot2Host2HostInfo.put(name, new TreeMap<>());
			}
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.ALL_DEPOTS, allDepots);
		dataServices.cacheManager.setCachedData(CacheIdentifier.MASTER_DEPOTS, masterDepots);
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);
	}

	private void retrieveClientsPD(List<Map<String, Object>> opsiHosts) {
		Map<String, HostInfo> host2hostInfo = new HashMap<>();
		Map<String, Map<String, Object>> masterDepots = dataServices.cacheManager
				.getCachedData(CacheIdentifier.MASTER_DEPOTS, Map.class);
		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = dataServices.cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		for (Map<String, Object> host : opsiHosts) {
			String name = (String) host.get(HostInfo.HOSTNAME_KEY);
			boolean depotFound = false;
			String depotId = null;

			if (!hasConfig(name)) {
				Logging.debug(this, "retrieveOpsiHosts client  ", name, " has no config for ",
						OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID);
			} else {
				depotId = (String) ((List<?>) dataServices.config.getHostConfigsPD().get(name)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID)).get(0);
			}

			if (depotId != null && masterDepots.keySet().contains(depotId)) {
				depotFound = true;
			} else if (depotId != null) {
				Logging.warning("Host ", name, " is in ", depotId, " which is not a master depot");
			} else {
				// Do nothing if depotId is null
			}

			Logging.debug(this, "getConfigs for ", name);

			depotId = depotFound ? depotId : getConfigServer();
			host.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);
			HostInfo hostInfo = new HostInfo();
			hostInfo.setValues(host);
			hostInfo.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);

			host2hostInfo.put(name, hostInfo);
			depot2Host2HostInfo.get(depotId).put(name, hostInfo);
		}
		addOpsiHostNames(host2hostInfo.keySet());
		dataServices.cacheManager.setCachedData(CacheIdentifier.HOST_TO_HOST_INFO, host2hostInfo);
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);
	}

	private boolean hasConfig(String clientId) {
		return dataServices.config.getHostConfigsPD().get(clientId) != null
				&& dataServices.config.getHostConfigsPD().get(clientId)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID) != null
				&& !((List<?>) dataServices.config.getHostConfigsPD().get(clientId)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID)).isEmpty();
	}

	public Map<String, Set<String>> getFNode2TreeparentsPD() {
		retrieveFNode2TreeparentsPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS, Map.class);
	}

	public void retrieveFNode2TreeparentsPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.FNODE_TO_TREE_PARENTS)) {
			return;
		}

		retrieveOpsiHostsPD();
		Map<String, Set<String>> fNode2TreeParents = new HashMap<>();
		if (clientTree != null) {
			List<String> opsiHostNames = dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES,
					List.class);
			fNode2TreeParents.putAll(clientTree.collectAggregatedParentIDs(opsiHostNames));
		}
		dataServices.cacheManager.setCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS, fNode2TreeParents);
	}

	public void updateClientsForDepots(Iterable<String> depots, Collection<String> allowedClients) {
		retrieveOpsiHostsPD();

		Logging.debug(this, " ------ building pcList");
		Set<String> setOfPCs = new TreeSet<>();

		List<String> depotList = new ArrayList<>();
		for (String depot : depots) {
			if (dataServices.userRoles.hasDepotPermission(depot)) {
				depotList.add(depot);
			}
		}

		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = dataServices.cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);

		Map<String, HostInfo> mapPCInfomap = new HashMap<>();
		Map<String, String> mapPCBelongsToDepot = new HashMap<>();

		for (String depot : depotList) {
			if (depot2Host2HostInfo.get(depot) == null) {
				break;
			}

			for (Entry<String, HostInfo> client : depot2Host2HostInfo.get(depot).entrySet()) {
				HostInfo hostInfo = client.getValue();

				if (allowedClients == null || allowedClients.contains(client.getKey())) {
					setOfPCs.add(client.getKey());
					mapPCInfomap.put(client.getKey(), hostInfo);
					mapPCBelongsToDepot.put(client.getKey(), depot);
				}
			}
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.MAP_PC_INFO_MAP, mapPCInfomap);
		dataServices.cacheManager.setCachedData(CacheIdentifier.MAP_PC_BELONGS_TO_DEPOT, mapPCBelongsToDepot);
		dataServices.cacheManager.setCachedData(CacheIdentifier.CLIENTS_FOR_DEPOTS, setOfPCs);
	}

	/**
	 * This Method loads all clients for given depots As a side effect, all
	 * hostinfos and the map to which depots these clients belong are loaded
	 * 
	 * @return Set of the clients
	 */
	public Set<String> getClientsForDepots(Iterable<String> depots, Collection<String> allowedClients) {
		if (!dataServices.cacheManager.isDataCached(CacheIdentifier.CLIENTS_FOR_DEPOTS)) {
			updateClientsForDepots(depots, allowedClients);
		}

		return dataServices.cacheManager.getCachedData(CacheIdentifier.CLIENTS_FOR_DEPOTS, Set.class);
	}

	private void setDepot(String clientName, String depotId) {
		// set config
		if (dataServices.config.getHostConfigsPD().get(clientName) == null) {
			dataServices.config.getHostConfigsPD().put(clientName, new HashMap<>());
		}
		List<String> depotList = new ArrayList<>();
		depotList.add(depotId);
		dataServices.config.getHostConfigsPD().get(clientName).put(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID,
				depotList);

		// set in mapPC_Infomap
		Map<String, HostInfo> mapPCInfomap = dataServices.cacheManager.getCachedData(CacheIdentifier.MAP_PC_INFO_MAP,
				Map.class);
		HostInfo hostInfo = mapPCInfomap.get(clientName);

		Logging.info(this, "setDepot, hostinfo for client ", clientName, " : ", mapPCInfomap.get(clientName));

		hostInfo.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);

		Map<String, String> mapPcBelongsToDepot = dataServices.cacheManager
				.getCachedData(CacheIdentifier.MAP_PC_BELONGS_TO_DEPOT, Map.class);
		String oldDepot = mapPcBelongsToDepot.get(clientName);
		Logging.info(this, "setDepot clientName, oldDepot ", clientName, ", ", oldDepot);
		mapPcBelongsToDepot.put(clientName, depotId);
		dataServices.cacheManager.setCachedData(CacheIdentifier.MAP_PC_BELONGS_TO_DEPOT, mapPcBelongsToDepot);

		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = dataServices.cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		if (oldDepot != null) {
			depot2Host2HostInfo.get(oldDepot).remove(clientName);
		}
		depot2Host2HostInfo.get(depotId).put(clientName, hostInfo);
		dataServices.cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);
	}

	public void setDepotForClients(Iterable<String> clients, String depotId) {
		if (!dataServices.userRoles.hasDepotPermission(depotId)) {
			return;
		}

		ConfigName2ConfigValue config = new ConfigName2ConfigValue(null);
		List<String> depots = new ArrayList<>();
		depots.add(depotId);
		config.put(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID, depots);

		for (String client : clients) {
			setDepot(client, depotId);
			// collect data
			dataServices.config.setConfigStates(client, config);
		}
		// send data
		dataServices.config.updateConfigStates();
	}

	// update derived data (caution!), does not create a HostInfo
	public void addOpsiHostName(String newName) {
		List<String> opsiHostNames = dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES,
				List.class);
		if (opsiHostNames == null) {
			opsiHostNames = new ArrayList<>();
		}
		opsiHostNames.add(newName);
		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
	}

	public void addOpsiHostNames(Collection<String> newNames) {
		List<String> opsiHostNames = dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES,
				List.class);
		if (opsiHostNames == null) {
			opsiHostNames = new ArrayList<>();
		}
		opsiHostNames.addAll(newNames);
		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
	}

	public void removeOpsiHostName(String name) {
		List<String> opsiHostNames = dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES,
				List.class);
		if (opsiHostNames == null) {
			opsiHostNames = new ArrayList<>();
		}
		opsiHostNames.remove(name);
		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
	}

	public void removeOpsiHostNames(List<String> names) {
		List<String> opsiHostNames = dataServices.cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES,
				List.class);
		if (opsiHostNames == null) {
			opsiHostNames = new ArrayList<>();
		}
		opsiHostNames.removeAll(names);
		dataServices.cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
	}

	// for table
	public void updateLocalHostInfo(String hostId, String property, Object value) {
		Map<String, HostInfo> mapPCInfomap = dataServices.cacheManager.getCachedData(CacheIdentifier.MAP_PC_INFO_MAP,
				Map.class);
		if (mapPCInfomap != null && mapPCInfomap.get(hostId) != null) {
			mapPCInfomap.get(hostId).put(property, value);
			dataServices.cacheManager.setCachedData(CacheIdentifier.MAP_PC_INFO_MAP, mapPCInfomap);
			Logging.info(this, "updateLocalHostInfo ", hostId, " - ", property, " : ", value);
		}
	}

	public void setLocalHostInfo(String hostId, HostInfo hostInfo) {
		Map<String, HostInfo> mapPCInfomap = dataServices.cacheManager.getCachedData(CacheIdentifier.MAP_PC_INFO_MAP,
				Map.class);
		Logging.debug(this, "setLocalHostInfo ", " ", hostId, ", ", hostInfo);
		mapPCInfomap.put(hostId, hostInfo);
		dataServices.cacheManager.setCachedData(CacheIdentifier.MAP_PC_INFO_MAP, mapPCInfomap);
	}
}
