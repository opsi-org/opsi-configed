/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.configed.type.HostGroupRelation;
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;

/**
 * This class is a little command line tool which can execute saved searches.
 */
public class SavedSearchQuery {

	private String host;
	private String user;
	private String password;
	private String searchName;

	private OpsiserviceNOMPersistenceController controller;

	public SavedSearchQuery(String host, String user, String password, String searchName) {
		Logging.setLogLevelFile(Logging.LEVEL_NONE);
		Logging.setLogLevelConsole(Logging.LEVEL_NONE);

		setArgs(host, user, password, searchName);
		addMissingArgs();

		initConnection();
	}

	private void setArgs(String host, String user, String password, String searchName) {
		Logging.info(this, "setArgs " + host + ", PASSWORD, " + searchName);
		this.host = host;
		this.user = user;
		this.password = password;
		this.searchName = searchName;
	}

	private void addMissingArgs() {
		if (host == null) {
			host = Globals.getCLIparam("Host: ", false);
		}
		if (user == null) {
			user = Globals.getCLIparam("User: ", false);
		}
		if (password == null) {
			password = Globals.getCLIparam("Password: ", true);
		}
	}

	private void initConnection() {
		controller = PersistenceControllerFactory.getNewPersistenceController(host, user, password);

		if (controller == null || controller.getConnectionState().getState() != ConnectionState.CONNECTED) {
			Logging.error("Authentication error.");
			Main.endApp(1);
		}

		Messages.setLocale("en");
	}

	public List<String> runSearch(boolean printing) {

		Map<String, Map<String, Object>> depots = controller.getHostInfoCollections().getAllDepots();

		controller.getHostInfoCollections().getClientListForDepots(depots.keySet().toArray(new String[0]), null);

		SelectionManager manager = new SelectionManager(null);
		List<String> searches = manager.getSavedSearchesNames();
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

		Map<String, Map<String, String>> hostGroups = controller.getHostGroups();

		if (!hostGroups.keySet().contains(groupName)) {
			Logging.error("group not found");
			Main.endApp(5);
		}

		List<String> groupAttributes = new HostGroupRelation().getAttributes();
		StringValuedRelationElement saveGroupRelation = new StringValuedRelationElement(groupAttributes,
				hostGroups.get(groupName));

		if (!controller.deleteGroup(groupName)) {
			Logging.error("delete group error, groupName " + groupName);
			Main.endApp(6);
		}

		if (!controller.addGroup(saveGroupRelation)) {
			Logging.error("add group error, group " + saveGroupRelation);
			Main.endApp(7);
		}

		if (!controller.addHosts2Group(hosts, groupName)) {
			Logging.error("addHosts2Group error, group " + groupName);
			Main.endApp(8);
		}
	}

	private static void printResult(List<String> result) {
		for (String line : result) {
			Logging.debug(line);
		}
	}
}
