package de.uib.configed.clientselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;

/**
 * This class is a little command line tool which can execute saved searches.
 */
public class SavedSearchQuery {
	private static final String USAGE = "\n" + "configed_savedsearch [OPTIONS] [NAME]\n\n"
			+ "Runs the given search NAME and returns the matching clients. "
			+ "If NAME is not set, list all available searches.\n\n" + "OPTIONS:\n"
			+ "  -h\tConfiguration server to connect to\n" + "  -u\tUsername for authentication\n"
			+ "  -p\tPassword for authentication\n";

	private String[] args;
	private String host;
	private String user;
	private String password;
	private String searchName;

	private AbstractPersistenceController controller;

	public SavedSearchQuery() {
		Logging.setLogLevelFile(Logging.LEVEL_NONE);
		Logging.setLogLevelConsole(Logging.LEVEL_NONE);
	}

	/*
	 * constructor for standalone call of this class
	 */
	public SavedSearchQuery(String[] args) {
		this();
		this.args = args;
		if (!parseArgs()) {
			showUsage();
			System.exit(10);
		}
	}

	public boolean parseArgs() {
		String lastOption = null;
		searchName = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-h") || args[i].equals("-u") || args[i].equals("-p")) {
				if (lastOption != null)
					return false;
				lastOption = args[i];
			} else {
				if (lastOption != null) {
					addInfo(lastOption.trim(), args[i]);
					lastOption = null;
				} else {
					if (searchName != null)
						return false;
					searchName = args[i];
				}
			}
		}
		return true;
	}

	public void showUsage() {
		Logging.debug(USAGE);
	}

	public void setArgs(String host, String user, String password, String searchName) {
		Logging.info(this, "setArgs " + host + ", PASSWORD, " + searchName);
		this.host = host;
		this.user = user;
		this.password = password;
		this.searchName = searchName;
	}

	public void addMissingArgs() {
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

	public List<String> runSearch(boolean printing) {

		Messages.setLocale("en");
		controller = PersistenceControllerFactory.getNewPersistenceController(host, user, password);

		if (controller == null) {
			Logging.error("Authentication error.");
			System.exit(1);
		}

		if (controller.getConnectionState().getState() != ConnectionState.CONNECTED) {
			Logging.error("Authentication error.");
			System.exit(1);
		}

		Map<String, Map<java.lang.String, java.lang.Object>> depots = controller.getHostInfoCollections()
				.getAllDepots();

		controller.getHostInfoCollections().getClientListForDepots(depots.keySet().toArray(new String[0]), null);

		SelectionManager manager = new SelectionManager(null);
		List<String> searches = manager.getSavedSearchesNames();
		if (searchName == null && printing) {
			printResult(searches);
			return new ArrayList<>();
		}

		if (!searches.contains(searchName)) {
			Logging.error("Search not found.");
			System.exit(2);
		}

		manager.loadSearch(searchName);

		List<String> result = manager.selectClients();
		if (printing)
			printResult(result);
		return result;
	}

	public void populateHostGroup(List<String> hosts, String groupName) {
		if (controller == null) {
			Logging.error("controller not initialized");
			System.exit(3);
		}

		if (hosts == null) {
			Logging.error("hosts collection not initialized");
			System.exit(4);
		}

		Map<String, Map<String, String>> hostGroups = controller.getHostGroups();

		if (!hostGroups.keySet().contains(groupName)) {
			Logging.error("group not found");
			System.exit(5);
		}

		List<String> groupAttributes = new de.uib.configed.type.HostGroupRelation().getAttributes();
		StringValuedRelationElement saveGroupRelation = new StringValuedRelationElement(groupAttributes,
				hostGroups.get(groupName));

		if (!controller.deleteGroup(groupName)) {
			Logging.error("delete group error, groupName " + groupName);
			System.exit(6);
		}

		if (!controller.addGroup(saveGroupRelation)) {
			Logging.error("add group error, group " + saveGroupRelation);
			System.exit(7);
		}

		if (!controller.addHosts2Group(hosts, groupName)) {
			Logging.error("addHosts2Group error, group " + groupName);
			System.exit(8);
		}

	}

	private void addInfo(String option, String value) {
		if (option.equals("-h"))
			host = value;
		else if (option.equals("-u"))
			user = value;
		else if (option.equals("-p"))
			password = value;
		else
			throw new IllegalArgumentException("Unknown option " + option);
	}

	private void printResult(List<String> result) {
		for (String line : result)
			Logging.debug(line);
	}
}
