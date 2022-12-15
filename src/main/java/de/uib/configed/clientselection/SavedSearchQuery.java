package de.uib.configed.clientselection;

import java.util.List;
import java.util.Map;

import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.logging;

/**
 * This class is a little command line tool which can execute saved searches.
 */
public class SavedSearchQuery {
	private final String usage = "\n" + "configed_savedsearch [OPTIONS] [NAME]\n\n"
			+ "Runs the given search NAME and returns the matching clients. "
			+ "If NAME is not set, list all available searches.\n\n" + "OPTIONS:\n"
			+ "  -h\tConfiguration server to connect to\n" + "  -u\tUsername for authentication\n"
			+ "  -p\tPassword for authentication\n";

	private String[] args;
	private String host;
	private String user;
	private String password;
	private String searchName;
	private String group;

	private PersistenceController controller;

	public SavedSearchQuery() {
		logging.setLogLevelFile(logging.LEVEL_NONE);
		logging.setLogLevelConsole(logging.LEVEL_NONE);
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
		logging.debug(usage);
	}

	public void setArgs(String host, String user, String password, String searchName, String group) {
		logging.info(this, "setArgs " + host + ", PASSWORD, " + searchName + ", " + group);
		this.host = host;
		this.user = user;
		this.password = password;
		this.searchName = searchName;
		this.group = group;
		// System.exit(0);
	}

	public void addMissingArgs() {
		if (host == null)
			host = de.uib.utilities.Globals.getCLIparam("Host: ", false);
		if (user == null)
			user = de.uib.utilities.Globals.getCLIparam("User: ", false);
		if (password == null)
			password = de.uib.utilities.Globals.getCLIparam("Password: ", true);
	}

	public List<String> runSearch(boolean printing) {
		List<String> result = null;

		Messages.setLocale("en");
		controller = PersistenceControllerFactory.getNewPersistenceController(host, user, password);

		if (controller == null) {
			logging.error("Authentication error.");
			System.exit(1);
		}

		if (controller.getConnectionState().getState() != ConnectionState.CONNECTED) {
			logging.error("Authentication error.");
			System.exit(1);
		}

		Map<String, Map<java.lang.String, java.lang.Object>> depots = controller.getHostInfoCollections()
				.getAllDepots();

		controller.getHostInfoCollections().getClientListForDepots(depots.keySet().toArray(new String[0]), null);

		SelectionManager manager = new SelectionManager(null);
		java.util.List<String> searches = manager.getSavedSearchesNames();
		if (searchName == null && printing) {
			printResult(searches);
			return null;
		}

		// logging.debug("searches, searchName " + searches + ", " + searchName);

		if (!searches.contains(searchName)) {
			logging.error("Search not found.");
			System.exit(2);
		}

		manager.loadSearch(searchName);
		result = manager.selectClients();
		if (printing)
			printResult(result);
		return result;
	}

	public void populateHostGroup(java.util.List<String> hosts, String groupName) {
		if (controller == null) {
			logging.error("controller not initialized");
			System.exit(3);
		}

		if (hosts == null) {
			logging.error("hosts collection not initialized");
			System.exit(4);
		}

		Map<String, Map<String, String>> hostGroups = controller.getHostGroups();

		// logging.debug(" hostGroups " + hostGroups);

		if (!hostGroups.keySet().contains(groupName)) {
			logging.error("group not found");
			System.exit(5);
		}

		java.util.List<String> groupAttributes = new de.uib.configed.type.HostGroupRelation().getAttributes();
		StringValuedRelationElement saveGroupRelation = new StringValuedRelationElement(groupAttributes,
				hostGroups.get(groupName));

		if (!controller.deleteGroup(groupName)) {
			logging.error("delete group error, groupName " + groupName);
			System.exit(6);
		}

		/*
		 * removes group and therefore the memberships of hosts in this group
		 * subgroups become root subgroups
		 * 
		 * 
		 * try{
		 * logging.debug(" ......... waiting ");
		 * Thread.sleep(10000);
		 * }
		 * catch(Exception ex)
		 * {
		 * }
		 */

		if (!controller.addGroup(saveGroupRelation)) {
			logging.error("add group error, group " + saveGroupRelation);
			System.exit(7);
		}

		if (!controller.addHosts2Group(hosts, groupName)) {
			logging.error("addHosts2Group error, group " + groupName);
			System.exit(8);
		}

	}

	public static void main(String[] args) {
		SavedSearchQuery query = new SavedSearchQuery(args);
		if (!query.parseArgs()) {
			query.showUsage();
			System.exit(10);
		}
		query.addMissingArgs();
		query.runSearch(true);
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
			logging.debug(line);
	}
}
