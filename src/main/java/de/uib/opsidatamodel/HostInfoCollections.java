/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.utilities.logging.Logging;
import utils.Utils;

/**
 * Provides a way to retrieve the current data (and update it) about hosts and
 * depots, without requiring to retrieve data from the server.
 * <p>
 * It does it by keeping all of its data internally cached and updating the
 * internally cached data with a new data, when it is required.
 */
@SuppressWarnings({ "unchecked" })
public class HostInfoCollections {
	private CacheManager cacheManager;
	private ClientTree connectedTree;
	private OpsiServiceNOMPersistenceController persistenceController;

	// We need the argument here since the controller is not loaded yet
	public HostInfoCollections(OpsiServiceNOMPersistenceController pc) {
		this.cacheManager = CacheManager.getInstance();
		this.persistenceController = pc;
	}

	// deliver data

	private static Map<String, Object> hideOpsiHostKey(Map<String, Object> source) {
		Map<String, Object> result = new HashMap<>(source);
		result.put(HostInfo.HOST_KEY_KEY, "****");
		return result;
	}

	public void setTree(ClientTree tree) {
		connectedTree = tree;
	}

	public String getConfigServer() {
		return cacheManager.getCachedData(CacheIdentifier.CONFIG_SERVER, String.class);
	}

	public Map<String, String> getMapPcBelongsToDepot() {
		return cacheManager.getCachedData(CacheIdentifier.MAP_PC_BELONGS_TO_DEPOT, Map.class);
	}

	public List<String> getOpsiHostNames() {
		retrieveOpsiHostsPD();
		return new ArrayList<>(cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class));
	}

	public int getCountClients() {
		retrieveOpsiHostsPD();
		List<String> opsiHostNames = cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class);
		List<String> depotNamesList = cacheManager.getCachedData(CacheIdentifier.DEPOT_NAMES_LIST, List.class);
		boolean removed = opsiHostNames.removeAll(depotNamesList);
		Logging.debug(this, "depots were removed from opsi host names " + removed);
		return opsiHostNames.size();
	}

	public Map<String, Map<String, Object>> getDepots() {
		retrieveOpsiHostsPD();
		return cacheManager.getCachedData(CacheIdentifier.MASTER_DEPOTS, Map.class);
	}

	public List<String> getDepotNamesList() {
		retrieveOpsiHostsPD();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_NAMES_LIST, List.class);
	}

	public Map<String, Map<String, Object>> getAllDepots() {
		retrieveOpsiHostsPD();
		return cacheManager.getCachedData(CacheIdentifier.ALL_DEPOTS, Map.class);
	}

	public Map<String, HostInfo> getMapOfPCInfoMaps() {
		return cacheManager.getCachedData(CacheIdentifier.MAP_PC_INFO_MAP, Map.class);
	}

	public Map<String, HostInfo> getMapOfAllPCInfoMaps() {
		return cacheManager.getCachedData(CacheIdentifier.HOST_TO_HOST_INFO, Map.class);
	}

	// build data
	public void retrieveOpsiHostsPD() {
		Logging.debug(this, "retrieveOpsiHosts , opsiHostNames == null "
				+ (cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class) == null));

		if (cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class) != null) {
			return;
		}

		List<Map<String, Object>> opsiHosts = persistenceController.getHostDataService().getOpsiHosts();
		HostInfo.resetInstancesCount();

		// find opsi configserver and give it the top position
		retrieveConfigServerPD(opsiHosts);

		String configServer = cacheManager.getCachedData(CacheIdentifier.CONFIG_SERVER, String.class);
		if (configServer == null) {
			showNoDataDialog(opsiHosts.size());
			Main.endApp(1);
		}

		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = new TreeMap<>();
		depot2Host2HostInfo.put(configServer, new TreeMap<>());
		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);

		// find depots and build entries for them
		retrieveDepotsPD(opsiHosts);
		retrieveClientsPD(opsiHosts);

		Map<String, Map<String, Object>> masterDepots = cacheManager.getCachedData(CacheIdentifier.MASTER_DEPOTS,
				Map.class);
		Logging.info(this, "retrieveOpsiHost found masterDepots " + masterDepots.size());

		Map<String, Map<String, HostInfo>> depot2Host2HostInfos = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		for (String depot : masterDepots.keySet()) {
			Logging.info(this, "retrieveOpsiHosts clients in " + depot + ": " + depot2Host2HostInfos.get(depot).size());
		}

		TreeSet<String> depotNamesSorted = new TreeSet<>(masterDepots.keySet());
		depotNamesSorted.remove(configServer);

		List<String> depotNamesList = cacheManager.getCachedData(CacheIdentifier.DEPOT_NAMES_LIST, List.class);
		for (String depot : depotNamesSorted) {
			depotNamesList.add(depot);
		}

		cacheManager.setCachedData(CacheIdentifier.DEPOT_NAMES_LIST, depotNamesList);
		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);

		Logging.info(this, "retrieveOpsiHosts  HostInfo instances counter " + HostInfo.getInstancesCount());
		Logging.info(this, "retrieveOpsiHosts  hostnames size "
				+ cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class).size());
		Logging.info(this, "retrieveOpsiHosts   depotNamesList size " + depotNamesList.size());
	}

	private void showNoDataDialog(int countHosts) {
		StringBuilder messbuff = new StringBuilder();
		final String BASE_LABEL = "PersistenceController.noData";

		messbuff.append(Configed.getResourceValue(BASE_LABEL + "0"));
		messbuff.append("\n");
		messbuff.append(Configed.getResourceValue(BASE_LABEL + "1") + " " + countHosts);
		messbuff.append("\n");
		messbuff.append("\n");

		for (int i = 2; i <= 4; i++) {
			messbuff.append(Configed.getResourceValue(BASE_LABEL + i));
			messbuff.append("\n");
			messbuff.append("\n");
		}

		String message = messbuff.toString();
		Logging.error(this, message);

		FTextArea f = new FTextArea(null, "opsi configed", true,
				new String[] { Configed.getResourceValue("PersistenceController.endApp") }, 500, 400);
		f.setMessage(message);

		f.setVisible(true);
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
				Logging.info(this, "retrieveOpsiHosts  type opsiconfigserver host " + hideOpsiHostKey(host));

				configServer = name;
				depotNamesList.add(name);
				allDepots.put(name, host);

				if (Utils.interpretAsBoolean(host.get(HostInfo.IS_MASTER_DEPOT_KEY), true).booleanValue()) {
					Map<String, Object> hostMap = new HashMap<>(host);
					masterDepots.put(name, hostMap);
				}

				String workbenchPath = retrieveWorkbenchPath(host);

				if (!workbenchPath.isEmpty()) {
					persistenceController.getConfigDataService().setConfigedWorkbenchDefaultValuePD(workbenchPath);
					persistenceController.getConfigDataService().setPackageServerDirectoryPD(workbenchPath);
				}
			}
		}
		cacheManager.setCachedData(CacheIdentifier.CONFIG_SERVER, configServer);
		cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
		cacheManager.setCachedData(CacheIdentifier.DEPOT_NAMES_LIST, depotNamesList);
		cacheManager.setCachedData(CacheIdentifier.ALL_DEPOTS, allDepots);
		cacheManager.setCachedData(CacheIdentifier.MASTER_DEPOTS, masterDepots);
	}

	private String retrieveWorkbenchPath(Map<String, Object> host) {
		String filepath = "";
		Object val = host.get(HostInfo.DEPOT_WORKBENCH_KEY);

		if (val != null && !"".equals(val)) {
			try {
				filepath = new URL((String) val).getPath();
				Logging.info(this, "retrieveOpsiHosts workbenchpath " + filepath);
			} catch (MalformedURLException netex) {
				Logging.error("not a correctly formed file URL: " + val, netex);
			}
		}

		return filepath;
	}

	private void retrieveDepotsPD(List<Map<String, Object>> opsiHosts) {
		Map<String, Map<String, Object>> allDepots = cacheManager.getCachedData(CacheIdentifier.ALL_DEPOTS, Map.class);
		Map<String, Map<String, Object>> masterDepots = cacheManager.getCachedData(CacheIdentifier.MASTER_DEPOTS,
				Map.class);
		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		for (Map<String, Object> host : opsiHosts) {
			if (!host.get(HostInfo.HOST_TYPE_KEY).equals(HostInfo.HOST_TYPE_VALUE_OPSI_DEPOT_SERVER)) {
				continue;
			}

			String name = (String) host.get(HostInfo.HOSTNAME_KEY);
			allDepots.put(name, host);

			boolean isMasterDepot = Utils.interpretAsBoolean(host.get(HostInfo.IS_MASTER_DEPOT_KEY), false);

			if (isMasterDepot) {
				Map<String, Object> hostMap = new HashMap<>(host);
				masterDepots.put(name, hostMap);
				depot2Host2HostInfo.put(name, new TreeMap<>());
			}
		}
		cacheManager.setCachedData(CacheIdentifier.ALL_DEPOTS, allDepots);
		cacheManager.setCachedData(CacheIdentifier.MASTER_DEPOTS, masterDepots);
		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);
	}

	private void retrieveClientsPD(List<Map<String, Object>> opsiHosts) {
		Map<String, HostInfo> host2hostInfo = new HashMap<>();
		Map<String, Map<String, Object>> masterDepots = cacheManager.getCachedData(CacheIdentifier.MASTER_DEPOTS,
				Map.class);
		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		for (Map<String, Object> host : opsiHosts) {
			if (!((String) host.get(HostInfo.HOST_TYPE_KEY)).equals(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT)) {
				continue;
			}

			String name = (String) host.get(HostInfo.HOSTNAME_KEY);
			boolean depotFound = false;
			String depotId = null;

			if (!hasConfig(name)) {
				Logging.debug(this, "retrieveOpsiHosts client  " + name + " has no config for "
						+ OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID);
			} else {
				depotId = (String) ((List<?>) persistenceController.getConfigDataService().getHostConfigsPD().get(name)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID)).get(0);
			}

			if (depotId != null && masterDepots.keySet().contains(depotId)) {
				depotFound = true;
			} else if (depotId != null) {
				Logging.warning("Host " + name + " is in " + depotId + " which is not a master depot");
			} else {
				// Do nothing if depotId is null
			}

			Logging.debug(this, "getConfigs for " + name);

			host.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY,
					persistenceController.getConfigDataService().isInstallByShutdownConfigured(name));
			host.put(HostInfo.CLIENT_UEFI_BOOT_KEY,
					persistenceController.getConfigDataService().isUefiConfigured(name));

			if (persistenceController.getConfigDataService().getHostConfig(name) != null) {
				boolean result = persistenceController.getConfigDataService()
						.findBooleanConfigurationComparingToDefaults(name,
								persistenceController.getConfigDataService().getWanConfigurationPD());
				Logging.debug(this, "host " + name + " wan config " + result);
				host.put(HostInfo.CLIENT_WAN_CONFIG_KEY, result);
			}

			HostInfo hostInfo = null;
			String myDepot = null;

			depotId = depotFound ? depotId : cacheManager.getCachedData(CacheIdentifier.CONFIG_SERVER, String.class);
			host.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);
			hostInfo = new HostInfo(host);
			hostInfo.setInDepot(depotId);
			myDepot = depotId;

			host2hostInfo.put(name, hostInfo);
			depot2Host2HostInfo.get(myDepot).put(name, hostInfo);
		}
		cacheManager.setCachedData(CacheIdentifier.HOST_TO_HOST_INFO, host2hostInfo);
		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);
	}

	private boolean hasConfig(String clientId) {
		return persistenceController.getConfigDataService().getHostConfigsPD().get(clientId) != null
				&& persistenceController.getConfigDataService().getHostConfigsPD().get(clientId)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID) != null
				&& !((List<?>) (persistenceController.getConfigDataService()).getHostConfigsPD().get(clientId)
						.get(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID)).isEmpty();
	}

	public Map<String, Set<String>> getFNode2TreeparentsPD() {
		retrieveFNode2TreeparentsPD();
		return cacheManager.getCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS, Map.class);
	}

	public void retrieveFNode2TreeparentsPD() {
		if (cacheManager.getCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS, Map.class) != null) {
			return;
		}

		retrieveOpsiHostsPD();
		Map<String, Set<String>> fNode2Treeparents = new HashMap<>();
		if (connectedTree != null) {
			List<String> opsiHostNames = cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class);
			for (String host : opsiHostNames) {
				fNode2Treeparents.put(host, connectedTree.collectParentIDs(host));
			}
		}
		cacheManager.setCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS, fNode2Treeparents);
	}

	public Map<String, Boolean> getClientListForDepots(String[] depots, Collection<String> allowedClients) {
		retrieveOpsiHostsPD();

		Logging.debug(this, " ------ building pcList");
		Map<String, String> mapPcBelongsToDepot = new HashMap<>();
		Map<String, Boolean> mapOfPCs = new HashMap<>();
		Map<String, HostInfo> mapPCInfomap = new HashMap<>();

		List<String> depotList = new ArrayList<>();
		for (String depot : depots) {
			if (persistenceController.getUserRolesConfigDataService().hasDepotPermission(depot)) {
				depotList.add(depot);
			}
		}

		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		for (String depot : depotList) {
			if (depot2Host2HostInfo.get(depot) == null) {
				Logging.info(this, "getPcListForDepots depot " + depot + " is null");
			} else {
				for (String clientName : depot2Host2HostInfo.get(depot).keySet()) {
					HostInfo hostInfo = depot2Host2HostInfo.get(depot).get(clientName);

					if (allowedClients != null && !allowedClients.contains(clientName)) {
						continue;
					}

					mapOfPCs.put(clientName, false);
					mapPCInfomap.put(clientName, hostInfo);
					mapPcBelongsToDepot.put(clientName, depot);
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.MAP_PC_INFO_MAP, mapPCInfomap);
		cacheManager.setCachedData(CacheIdentifier.MAP_PC_BELONGS_TO_DEPOT, mapPcBelongsToDepot);

		return mapOfPCs;
	}

	private void setDepot(String clientName, String depotId) {
		// set config
		if (persistenceController.getConfigDataService().getHostConfigsPD().get(clientName) == null) {
			persistenceController.getConfigDataService().getHostConfigsPD().put(clientName, new HashMap<>());
		}
		List<String> depotList = new ArrayList<>();
		depotList.add(depotId);
		persistenceController.getConfigDataService().getHostConfigsPD().get(clientName)
				.put(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID, depotList);

		// set in mapPC_Infomap
		Map<String, HostInfo> mapPCInfomap = cacheManager.getCachedData(CacheIdentifier.MAP_PC_INFO_MAP, Map.class);
		HostInfo hostInfo = mapPCInfomap.get(clientName);

		Logging.info(this, "setDepot, hostinfo for client " + clientName + " : " + mapPCInfomap.get(clientName));

		hostInfo.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);

		Map<String, String> mapPcBelongsToDepot = cacheManager.getCachedData(CacheIdentifier.MAP_PC_BELONGS_TO_DEPOT,
				Map.class);
		String oldDepot = mapPcBelongsToDepot.get(clientName);
		Logging.info(this, "setDepot clientName, oldDepot " + clientName + ", " + oldDepot);
		mapPcBelongsToDepot.put(clientName, depotId);
		cacheManager.setCachedData(CacheIdentifier.MAP_PC_BELONGS_TO_DEPOT, mapPcBelongsToDepot);

		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		depot2Host2HostInfo.get(oldDepot).remove(clientName);
		depot2Host2HostInfo.get(depotId).put(clientName, hostInfo);
		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);
	}

	public void setDepotForClients(String[] clients, String depotId) {
		if (!persistenceController.getUserRolesConfigDataService().hasDepotPermission(depotId)) {
			return;
		}

		List<String> depots = new ArrayList<>();

		ConfigName2ConfigValue config = new ConfigName2ConfigValue(null);
		depots.add(depotId);

		config.put(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID, depots);
		for (String client : clients) {
			// collect data
			persistenceController.getConfigDataService().setAdditionalConfiguration(client, config);
		}
		// send data
		persistenceController.getConfigDataService().setAdditionalConfiguration();

		// change transitory data
		for (String client : clients) {
			setDepot(client, depotId);
		}

		// we hope to have completely changed the internal data
	}

	// update derived data (caution!), does not create a HostInfo
	public void addOpsiHostName(String newName) {
		List<String> opsiHostNames = cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class);
		opsiHostNames.add(newName);
		cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
	}

	public void addOpsiHostNames(String[] newNames) {
		List<String> opsiHostNames = cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class);
		opsiHostNames.addAll(Arrays.asList(newNames));
		cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
	}

	public void removeOpsiHostName(String name) {
		List<String> opsiHostNames = cacheManager.getCachedData(CacheIdentifier.OPSI_HOST_NAMES, List.class);
		opsiHostNames.remove(name);
		cacheManager.setCachedData(CacheIdentifier.OPSI_HOST_NAMES, opsiHostNames);
	}

	// for table
	public void updateLocalHostInfo(String hostId, String property, Object value) {
		Map<String, HostInfo> mapPCInfomap = cacheManager.getCachedData(CacheIdentifier.MAP_PC_INFO_MAP, Map.class);
		if (mapPCInfomap != null && mapPCInfomap.get(hostId) != null) {
			mapPCInfomap.get(hostId).put(property, value);
			cacheManager.setCachedData(CacheIdentifier.MAP_PC_INFO_MAP, mapPCInfomap);
			Logging.info(this, "updateLocalHostInfo " + hostId + " - " + property + " : " + value);
		}
	}

	public void setLocalHostInfo(String hostId, String depotId, HostInfo hostInfo) {
		Map<String, HostInfo> mapPCInfomap = cacheManager.getCachedData(CacheIdentifier.MAP_PC_INFO_MAP, Map.class);
		Map<String, HostInfo> host2HostInfo = cacheManager.getCachedData(CacheIdentifier.HOST_TO_HOST_INFO, Map.class);
		Map<String, Map<String, HostInfo>> depot2Host2HostInfo = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, Map.class);
		Logging.debug(this, "setLocalHostInfo " + " " + hostId + ", " + depotId + ", " + hostInfo);
		mapPCInfomap.put(hostId, hostInfo);
		host2HostInfo.put(hostId, hostInfo);
		depot2Host2HostInfo.get(depotId).put(hostId, hostInfo);
		cacheManager.setCachedData(CacheIdentifier.MAP_PC_INFO_MAP, mapPCInfomap);
		cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_HOST_TO_HOST_INFO, depot2Host2HostInfo);
	}
}
