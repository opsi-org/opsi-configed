/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uib.configed.app.Main;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.ConnectionState;
import de.uib.configed.core.infrastructure.HostData;
import de.uib.configed.gui.messages.Messages;
import de.uib.configed.gui.type.Object2GroupEntry;
import de.uib.configed.share.logging.Logging;

/**
 * This class is a little command line tool which can execute saved searches.
 */
public class SavedSearchQuery {
	private HostData hostData;
	private String searchName;

	private OpsiServiceNOMPersistenceController persistenceController;

	public SavedSearchQuery(HostData hostData, String searchName) {
		setArgs(hostData, searchName);
		initConnection();
	}

	private void setArgs(HostData hostData, String searchName) {
		Logging.info(this, "setArgs ", hostData.getHost(), ", PASSWORD, ", searchName);
		this.hostData = hostData;
		this.searchName = searchName;
	}

	private void initConnection() {
		persistenceController = PersistenceControllerFactory.getNewPersistenceController(hostData);

		if (persistenceController == null
				|| persistenceController.getConnectionState().getState() != ConnectionState.CONNECTED) {
			Logging.error("Authentication error.");
			Main.endApp(1);
		}

		Messages.setLocale("en");
	}

	public Collection<String> runSearch(boolean printing) {
		Map<String, Map<String, Object>> depots = persistenceController.getDataServices().hostInfoCollections
				.getAllDepots();

		// Load data that we need to find clients for selection
		persistenceController.getDataServices().hostInfoCollections.getClientsForDepots(depots.keySet(), null);

		SelectionManager manager = new SelectionManager();
		Set<String> searches = manager.getSavedSearchesNames();
		if (searchName == null && printing) {
			printResult(searches);
			return new HashSet<>();
		}

		if (!searches.contains(searchName)) {
			Logging.error("Search not found.");
			Main.endApp(2);
		}

		manager.loadSearch(searchName);

		Collection<String> result = manager.selectClients();
		if (printing) {
			printResult(result);
		}
		return result;
	}

	public void populateHostGroup(Collection<String> hosts, String groupName) {
		if (hosts == null) {
			Logging.error("hosts collection not initialized");
			Main.endApp(4);
		}

		Map<String, Map<String, String>> hostGroups = persistenceController.getDataServices().group.getHostGroupsPD();

		if (!hostGroups.keySet().contains(groupName)) {
			Logging.error("group not found");
			Main.endApp(5);
		}

		if (!persistenceController.getDataServices().group.deleteGroup(groupName,
				Object2GroupEntry.GROUP_TYPE_HOSTGROUP)) {
			Logging.error("delete group error, groupName ", groupName);
			Main.endApp(6);
		}

		if (!persistenceController.getDataServices().group.addGroup(hostGroups.get(groupName), true)) {
			Logging.error("add group error, group ", hostGroups.get(groupName));
			Main.endApp(7);
		}

		if (!persistenceController.getDataServices().group.addHosts2Group(hosts, groupName)) {
			Logging.error("addHosts2Group error, group ", groupName);
			Main.endApp(8);
		}
	}

	@SuppressWarnings("java:S106")
	private static void printResult(Collection<String> result) {
		for (String line : result) {
			System.out.println(line);
		}
	}
}
