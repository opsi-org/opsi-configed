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
public abstract class AbstractHostInfoCollections {
	public abstract int getCountClients();

	public abstract String getConfigServer();

	public abstract void addOpsiHostName(String newName);

	public abstract void addOpsiHostNames(String[] newNames);

	public abstract List<String> getOpsiHostNames();

	public abstract Map<String, Map<String, Object>> getDepots();// only master depots

	public abstract Map<String, Map<String, Object>> getAllDepots();

	public abstract List<String> getDepotNamesList(); // master depots in display order

	public abstract Map<String, String> getMapPcBelongsToDepot();

	public abstract Map<String, HostInfo> getMapOfPCInfoMaps();

	public abstract Map<String, HostInfo> getMapOfAllPCInfoMaps();

	public abstract Map<String, Boolean> getClientListForDepots(String[] depots, Set<String> allowedClients);

	public abstract Map<String, Set<String>> getFNode2Treeparents();

	public abstract void opsiHostsRequestRefresh();
	// includes all refreshes

	public abstract void setDepotForClients(String[] clients, String depotId);

	public abstract void updateLocalHostInfo(String hostID, String property, Object value);

	public abstract void setLocalHostInfo(String hostId, String depotId, HostInfo hostInfo);

	// valueFromConfigStateAsExpected

	protected abstract void retrieveOpsiHosts();

	public abstract void setTree(ClientTree tree);

}
