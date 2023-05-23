/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000 - 2017 uib.de
 *
 *  @author  Rupert Roeder
 */

package de.uib.opsidatamodel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.HostInfo;
import de.uib.utilities.logging.Logging;

/**
 * HostInfoCollections description: abstract methods for retrieving and setting
 * host table data copyright: Copyright (c) 2014-2017 organization: uib.de
 * 
 * @author Rupert Roeder
 */

public class HostInfoCollections {
	private String configServer;
	private List<String> opsiHostNames;

	private int countClients;

	private Map<String, Map<String, Object>> masterDepots;
	private Map<String, Map<String, Object>> allDepots;
	private Map<String, Map<String, HostInfo>> depot2Host2HostInfo;
	private LinkedList<String> depotNamesList;

	// for some depots
	private Map<String, HostInfo> mapPCInfomap;

	// all hosts
	private Map<String, HostInfo> host2hostInfo;

	// essentially client --> all groups with it
	private Map<String, Set<String>> fNode2Treeparents;
	private Map<String, String> mapPcBelongsToDepot;

	private ClientTree connectedTree;

	private OpsiserviceNOMPersistenceController pc;

	public HostInfoCollections(OpsiserviceNOMPersistenceController pc) {
		this.pc = pc;
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
		return configServer;
	}

	private void checkMapPcBelongsToDepot() {
		if (mapPcBelongsToDepot == null) {
			mapPcBelongsToDepot = new HashMap<>();
		}
	}

	public Map<String, String> getMapPcBelongsToDepot() {
		checkMapPcBelongsToDepot();
		return mapPcBelongsToDepot;
	}

	public List<String> getOpsiHostNames() {
		retrieveOpsiHosts();
		return opsiHostNames;

	}

	public int getCountClients() {
		retrieveOpsiHosts();
		return countClients;
	}

	public Map<String, Map<String, Object>> getDepots() {
		retrieveOpsiHosts();
		Logging.debug(this, "getDepots masterDepots " + masterDepots);

		return masterDepots;
	}

	public List<String> getDepotNamesList() {
		retrieveOpsiHosts();
		return depotNamesList;
	}

	public Map<String, Map<String, Object>> getAllDepots() {
		retrieveOpsiHosts();
		return allDepots;
	}

	public Map<String, HostInfo> getMapOfPCInfoMaps() {

		return mapPCInfomap;
	}

	public Map<String, HostInfo> getMapOfAllPCInfoMaps() {
		Logging.info(this, "getMapOfAllPCInfoMaps() size " + host2hostInfo.size());
		return host2hostInfo;
	}

	// request data refreshes
	public void opsiHostsRequestRefresh() {
		opsiHostNames = null;
		fNode2Treeparents = null;
	}

	// build data
	public void retrieveOpsiHosts() {
		Logging.debug(this, "retrieveOpsiHosts , opsiHostNames == null " + (opsiHostNames == null));

		int countHosts = 0;

		if (opsiHostNames == null) {
			List<Map<String, Object>> opsiHosts = pc.hostRead();
			HostInfo.resetInstancesCount();

			opsiHostNames = new ArrayList<>();
			allDepots = new TreeMap<>();

			masterDepots = new LinkedHashMap<>();
			depotNamesList = new LinkedList<>();

			countHosts = opsiHosts.size();

			countClients = countHosts;

			host2hostInfo = new HashMap<>();

			Logging.info(this, "retrieveOpsiHosts countHosts " + countClients);

			// find opsi configserver and give it the top position
			for (Map<String, Object> host : opsiHosts) {
				String name = (String) host.get(HostInfo.HOSTNAME_KEY);
				opsiHostNames.add(name);

				for (Entry<String, Object> hostEntry : host.entrySet()) {
					if (hostEntry.getValue() == null) {
						host.put(hostEntry.getKey(), "");
					}
				}

				boolean isConfigserver = host.get(HostInfo.HOST_TYPE_KEY)
						.equals(HostInfo.HOST_TYPE_VALUE_OPSI_CONFIG_SERVER);

				if (isConfigserver) {
					Logging.info(this, "retrieveOpsiHosts  type opsiconfigserver host " + hideOpsiHostKey(host));

					configServer = name;

					depotNamesList.add(name);

					allDepots.put(name, host);
					countClients--;

					boolean isMasterDepot = OpsiserviceNOMPersistenceController
							.interpretAsBoolean(host.get(HostInfo.IS_MASTER_DEPOT_KEY), true);

					if (isMasterDepot) {
						Map<String, Object> hostMap = new HashMap<>(host);
						masterDepots.put(name, hostMap);
					}

					Object val = host.get(HostInfo.DEPOT_WORKBENCH_KEY);

					if (val != null && !"".equals(val)) {
						try {
							String filepath = new URL((String) val).getPath();
							Logging.info(this, "retrieveOpsiHosts workbenchpath " + filepath);

							OpsiserviceNOMPersistenceController.configedWorkbenchDefaultValue = filepath;
							OpsiserviceNOMPersistenceController.packageServerDirectoryS = filepath;
						} catch (MalformedURLException netex) {
							Logging.error("not a correctly formed file URL: " + val, netex);
						}
					}
				}
			}

			Logging.info(this, "retrieveOpsiHost found masterDepots " + masterDepots.size());
			if (configServer == null) {
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

				Main.endApp(1);
			}

			depot2Host2HostInfo = new TreeMap<>();
			depot2Host2HostInfo.put(configServer, new TreeMap<>());

			// find depots and build entries for them
			for (Map<String, Object> host : opsiHosts) {
				String name = (String) host.get(HostInfo.HOSTNAME_KEY);

				if (name == null) {
					Logging.info(this, "retrieveOpsiHosts, host  " + host);

				}

				if (host.get(HostInfo.HOST_TYPE_KEY).equals(HostInfo.HOST_TYPE_VALUE_OPSI_DEPOT_SERVER)) {
					allDepots.put(name, host);
					countClients--;

					boolean isMasterDepot = OpsiserviceNOMPersistenceController
							.interpretAsBoolean(host.get(HostInfo.IS_MASTER_DEPOT_KEY), false);

					if (isMasterDepot) {
						Map<String, Object> hostMap = new HashMap<>(host);
						masterDepots.put(name, hostMap);

						depot2Host2HostInfo.put(name, new TreeMap<>());
					}
				}
			}

			for (Map<String, Object> host : opsiHosts) {
				String name = (String) host.get(HostInfo.HOSTNAME_KEY);
				if (((String) host.get(HostInfo.HOST_TYPE_KEY)).equals(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT)) {
					boolean depotFound = false;
					String depotId = null;

					if (pc.getConfigs().get(name) == null
							|| pc.getConfigs().get(name)
									.get(OpsiserviceNOMPersistenceController.CONFIG_DEPOT_ID) == null
							|| ((List<?>) (pc.getConfigs().get(name)
									.get(OpsiserviceNOMPersistenceController.CONFIG_DEPOT_ID))).isEmpty()) {
						Logging.debug(this, "retrieveOpsiHosts client  " + name + " has no config for "
								+ OpsiserviceNOMPersistenceController.CONFIG_DEPOT_ID);
					} else {
						depotId = (String) ((List<?>) (pc.getConfigs().get(name)
								.get(OpsiserviceNOMPersistenceController.CONFIG_DEPOT_ID))).get(0);
					}

					if (depotId != null && masterDepots.keySet().contains(depotId)) {
						depotFound = true;
					} else {
						if (depotId != null) {
							Logging.warning("Host " + name + " is in " + depotId + " which is not a master depot");
						}
					}

					Logging.debug(this, "getConfigs for " + name);

					// Get Install by Shutdown

					host.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY, pc.isInstallByShutdownConfigured(name));

					// Get UEFI Boot

					host.put(HostInfo.CLIENT_UEFI_BOOT_KEY, pc.isUefiConfigured(name));

					// CHECK WAN STANDARD CONFIG
					if (pc.getConfig(name) != null) {

						Boolean result = false;
						boolean tested = pc.findBooleanConfigurationComparingToDefaults(name, pc.getWanConfiguration());

						Logging.debug(this, "host " + name + " wan config " + result);

						if (tested) {
							result = true;
						} else {
							tested = pc.findBooleanConfigurationComparingToDefaults(name, pc.getNotWanConfiguration());

							if (tested) {
								result = false;
							}
						}

						Logging.debug(this, "host " + name + " wan config " + result);

						host.put(HostInfo.CLIENT_WAN_CONFIG_KEY, result);

					}

					HostInfo hostInfo = null;

					String myDepot = null;

					if (depotFound) {
						host.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);
						hostInfo = new HostInfo(host);
						hostInfo.setInDepot(depotId);
						myDepot = depotId;

						// depot_restriction:

					} else {
						host.put(HostInfo.DEPOT_OF_CLIENT_KEY, configServer);
						hostInfo = new HostInfo(host);
						hostInfo.setInDepot(configServer);
						myDepot = configServer;

						// depot_restriction:

					}

					host2hostInfo.put(name, hostInfo);
					depot2Host2HostInfo.get(myDepot).put(name, hostInfo);
				}
			}

			for (String depot : masterDepots.keySet()) {
				Logging.info(this,
						"retrieveOpsiHosts clients in " + depot + ": " + depot2Host2HostInfo.get(depot).size());
			}

			TreeSet<String> depotNamesSorted = new TreeSet<>(masterDepots.keySet());
			depotNamesSorted.remove(configServer);

			for (String depot : depotNamesSorted) {
				depotNamesList.add(depot);
			}

			// test for depot_restriction:

			Logging.info(this, "retrieveOpsiHosts  HostInfo instances counter " + HostInfo.getInstancesCount());
			Logging.info(this, "retrieveOpsiHosts  hostnames size " + opsiHostNames.size());
			Logging.info(this, "retrieveOpsiHosts   depotNamesList size " + depotNamesList.size());

		}
	}

	public Map<String, Set<String>> getFNode2Treeparents() {
		retrieveFNode2Treeparents();
		return fNode2Treeparents;
	}

	private void retrieveFNode2Treeparents() {
		retrieveOpsiHosts();

		if (fNode2Treeparents == null) {
			fNode2Treeparents = new HashMap<>();
		}

		if (connectedTree != null) {
			for (String host : opsiHostNames) {
				fNode2Treeparents.put(host, connectedTree.collectParentIDs(host));
			}
		}
	}

	public Map<String, Boolean> getClientListForDepots(String[] depots, Collection<String> allowedClients) {
		retrieveOpsiHosts();

		Logging.debug(this, " ------ building pcList");
		mapPcBelongsToDepot = new HashMap<>();

		Map<String, Boolean> mapOfPCs = new HashMap<>();
		mapPCInfomap = new HashMap<>();

		List<String> depotList = new ArrayList<>();
		for (String depot : depots) {
			if (pc.hasDepotPermission(depot)) {
				depotList.add(depot);
			}
		}

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

		return mapOfPCs;
	}

	private void setDepot(String clientName, String depotId) {
		// set config
		if (pc.getConfigs().get(clientName) == null) {
			pc.getConfigs().put(clientName, new HashMap<>());
		}
		List<String> depotList = new ArrayList<>();
		depotList.add(depotId);
		pc.getConfigs().get(clientName).put(OpsiserviceNOMPersistenceController.CONFIG_DEPOT_ID, depotList);

		// set in mapPC_Infomap
		HostInfo hostInfo = mapPCInfomap.get(clientName);

		Logging.info(this, "setDepot, hostinfo for client " + clientName + " : " + mapPCInfomap.get(clientName));

		hostInfo.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);

		String oldDepot = mapPcBelongsToDepot.get(clientName);
		Logging.info(this, "setDepot clientName, oldDepot " + clientName + ", " + oldDepot);
		// set in mapPcBelongsToDepot
		mapPcBelongsToDepot.put(clientName, depotId);

		depot2Host2HostInfo.get(oldDepot).remove(clientName);
		depot2Host2HostInfo.get(depotId).put(clientName, hostInfo);
	}

	public void setDepotForClients(String[] clients, String depotId) {
		if (!pc.hasDepotPermission(depotId)) {
			return;
		}

		List<String> depots = new ArrayList<>();

		ConfigName2ConfigValue config = new ConfigName2ConfigValue(null);
		depots.add(depotId);

		config.put(OpsiserviceNOMPersistenceController.CONFIG_DEPOT_ID, depots);
		for (int i = 0; i < clients.length; i++) {
			// collect data
			pc.setAdditionalConfiguration(clients[i], config);
		}
		// send data
		pc.setAdditionalConfiguration(false);

		// change transitory data
		for (int i = 0; i < clients.length; i++) {
			setDepot(clients[i], depotId);
		}

		// we hope to have completely changed the internal data
	}

	// update derived data (caution!), does not create a HostInfo
	public void addOpsiHostName(String newName) {
		opsiHostNames.add(newName);
	}

	public void addOpsiHostNames(String[] newNames) {
		opsiHostNames.addAll(Arrays.asList(newNames));
	}

	public void removeOpsiHostName(String name) {
		opsiHostNames.remove(name);
	}

	public void removeOpsiHostNames(String[] names) {
		opsiHostNames.removeAll(Arrays.asList(names));
	}

	// for table
	public void updateLocalHostInfo(String hostId, String property, Object value) {
		if (mapPCInfomap != null && mapPCInfomap.get(hostId) != null) {
			mapPCInfomap.get(hostId).put(property, value);
			Logging.info(this, "updateLocalHostInfo " + hostId + " - " + property + " : " + value);
		}
	}

	public void setLocalHostInfo(String hostId, String depotId, HostInfo hostInfo) {
		Logging.debug(this, "setLocalHostInfo " + " " + hostId + ", " + depotId + ", " + hostInfo);
		mapPCInfomap.put(hostId, hostInfo);
		depot2Host2HostInfo.get(depotId).put(hostId, hostInfo);
	}
}
