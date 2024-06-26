/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.Main;
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

/**
 * This class is a little command line tool which can execute saved searches.
 */
public class SavedSearchQuery {
	private String host;
	private String user;
	private String password;
	private String otp;
	private String searchName;

	private OpsiServiceNOMPersistenceController persistenceController;

	public SavedSearchQuery(String host, String user, String password, String otp, String searchName) {
		setArgs(host, user, password, otp, searchName);
		initConnection();
	}

	private void setArgs(String host, String user, String password, String otp, String searchName) {
		Logging.info(this, "setArgs " + host + ", PASSWORD, " + searchName);
		this.host = host;
		this.user = user;
		this.password = password;
		this.otp = otp;
		this.searchName = searchName;
	}

	private void initConnection() {
		persistenceController = PersistenceControllerFactory.getNewPersistenceController(host, user, password, otp);

		if (persistenceController == null
				|| persistenceController.getConnectionState().getState() != ConnectionState.CONNECTED) {
			Logging.error("Authentication error.");
			Main.endApp(1);
		}

		Messages.setLocale("en");
	}

	public List<String> runSearch(boolean printing) {
		Map<String, Map<String, Object>> depots = persistenceController.getHostInfoCollections().getAllDepots();

		// Load data that we need to find clients for selection
		persistenceController.getHostInfoCollections().getClientsForDepots(depots.keySet(), null);

		SelectionManager manager = new SelectionManager(null);
		Set<String> searches = manager.getSavedSearchesNames();
		if (searchName == null && printing) {
			printResult(searches);
			return new ArrayList<>();
		}

		if (!searches.contains(searchName)) {
			Logging.error("Search not found.");
			Main.endApp(2);
		}

		manager.loadSearch(searchName);

		List<String> result = manager.selectClients();
		if (printing) {
			printResult(result);
		}
		return result;
	}

	public void populateHostGroup(List<String> hosts, String groupName) {
		if (hosts == null) {
			Logging.error("hosts collection not initialized");
			Main.endApp(4);
		}

		Map<String, Map<String, String>> hostGroups = persistenceController.getGroupDataService().getHostGroupsPD();

		if (!hostGroups.keySet().contains(groupName)) {
			Logging.error("group not found");
			Main.endApp(5);
		}

		if (!persistenceController.getGroupDataService().deleteGroup(groupName)) {
			Logging.error("delete group error, groupName " + groupName);
			Main.endApp(6);
		}

		if (!persistenceController.getGroupDataService().addGroup(hostGroups.get(groupName), true)) {
			Logging.error("add group error, group " + hostGroups.get(groupName));
			Main.endApp(7);
		}

		if (!persistenceController.getGroupDataService().addHosts2Group(hosts, groupName)) {
			Logging.error("addHosts2Group error, group " + groupName);
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
