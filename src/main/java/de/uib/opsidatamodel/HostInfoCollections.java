/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000 - 2017 uib.de
 *
 *  @author  Rupert Roeder
 */

package de.uib.opsidatamodel;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.HostInfo;

/**
 * HostInfoCollections description: abstract methods for retrieving and setting
 * host table data copyright: Copyright (c) 2014-2017 organization: uib.de
 * 
 * @author Rupert Roeder
 */

// TODO Remove? since only implemented once
public interface HostInfoCollections {
	int getCountClients();

	String getConfigServer();

	void addOpsiHostName(String newName);

	void addOpsiHostNames(String[] newNames);

	List<String> getOpsiHostNames();

	// only master depots
	Map<String, Map<String, Object>> getDepots();

	Map<String, Map<String, Object>> getAllDepots();

	// master depots in display order
	List<String> getDepotNamesList();

	Map<String, String> getMapPcBelongsToDepot();

	Map<String, HostInfo> getMapOfPCInfoMaps();

	Map<String, HostInfo> getMapOfAllPCInfoMaps();

	Map<String, Boolean> getClientListForDepots(String[] depots, Set<String> allowedClients);

	Map<String, Set<String>> getFNode2Treeparents();

	void opsiHostsRequestRefresh();
	// includes all refreshes

	void setDepotForClients(String[] clients, String depotId);

	void updateLocalHostInfo(String hostID, String property, Object value);

	void setLocalHostInfo(String hostId, String depotId, HostInfo hostInfo);

	// valueFromConfigStateAsExpected

	void retrieveOpsiHosts();

	void setTree(ClientTree tree);
}
