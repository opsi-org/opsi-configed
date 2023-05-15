package de.uib.configed;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.uib.Main;
import de.uib.configed.clientselection.SavedSearchQuery;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.swinfopage.SWcsvExporter;
import de.uib.configed.gui.swinfopage.SwPdfExporter;
import de.uib.messages.Messages;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.utilities.PropertiesStore;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.savedstates.SavedStates;

public class Configed {

	private static final String LOCALIZATION_FILENAME_REGEX = Messages.APPNAME + "_...*\\.properties";

	public static boolean sshConnectOnStart;

	public static final Charset serverCharset = StandardCharsets.UTF_8;
	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String JAVA_VENDOR = System.getProperty("java.vendor", "");
	public static final String SYSTEM_SSL_VERSION = System.getProperty("https.protocols");

	private static PropertiesStore extraLocalization;
	private static boolean showLocalizationStrings;

	private static String locale;
	private static String host;
	public static String user;
	private static String password;
	private static int loglevelConsole = Logging.getLogLevelConsole();
	private static int loglevelFile = Logging.getLogLevelFile();

	private static String sshKey;
	private static String sshKeyPass;
	private static String client;
	private static String clientgroup;
	private static Integer tab;
	private static boolean optionCLIQuerySearch;
	private static String savedSearch;
	private static boolean optionCLIDefineGroupBySearch;
	private static String group;
	private static boolean optionCLISwAuditPDF;
	private static boolean optionCLISwAuditCSV;
	private static String clientsFile;
	private static String outDir;

	private static boolean optionCLIuserConfigProducing;

	public static SavedStates savedStates;
	public static String savedStatesLocationName;
	public static final String SAVED_STATES_FILENAME = "configedStates.prop";

	private static Integer refreshMinutes;

	private static String paramHost;
	private static String paramUser;
	private static String paramPassword;
	private static String paramClient;
	private static String paramClientgroup;
	private static Integer paramTab;

	// --------------------------------------------------------------------------------------------------------
	// exit codes

	public static final int NO_ERROR = 0;
	public static final int ERROR_INVALID_OPTION = 1;
	public static final int ERROR_MISSING_VALUE_FOR_OPTION = 2;

	public static final int ERROR_CANNOT_READ_EXTRA_LOCALIZATION = 11;

	public static final int ERROR_OUT_OF_MEMORY = 21;

	private static FTextArea fErrorOutOfMemory;

	/** construct the application */
	public Configed(String paramLocale, String paramHost, String paramUser, String paramPassword,
			final String paramClient, final String paramClientgroup, final Integer paramTab) {

		setParamValues(paramHost, paramUser, paramPassword, paramTab, paramClient, paramClientgroup);

		UncaughtConfigedExceptionHandlerLocalized errorHandler = new UncaughtConfigedExceptionHandlerLocalized();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);

		Logging.debug("starting " + getClass().getName());
		Logging.debug("default charset is " + Charset.defaultCharset().displayName());
		Logging.debug("server charset is configured as " + serverCharset);

		if (serverCharset.equals(Charset.defaultCharset())) {
			Logging.debug("they are equal");
		}

		try {
			URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
			if (resource == null) {
				Logging.debug("image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			}
		} catch (Exception ex) {
			Logging.debug("imageHandled failed: " + ex.toString());
		}

		// Set locale
		List<String> existingLocales = Messages.getLocaleNames();
		Messages.setLocale(paramLocale);
		Logging.info("getLocales: " + existingLocales);
		Logging.info("selected locale characteristic " + Messages.getSelectedLocale());

		startWithLocale();
	}

	// --------------------------------------------------------------------------------------------------------

	private static String getYNforBoolean(boolean b) {
		if (b) {
			return "y";
		} else {
			return "n";
		}
	}

	public static void startWithLocale() {
		Logging.info("system information: ");

		Logging.info(" configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);
		Logging.info(" running by java version " + JAVA_VERSION);

		// Try with resources so that it will be closed in implicit finally statement
		try (Formatter formatter = new Formatter()) {
			Logging.info(
					" we get max memory " + formatter.format("%,d MB", Runtime.getRuntime().maxMemory() / 1_000_000));
		}

		FOpsiLicenseMissingText.reset();
		LicensingInfoMap.requestRefresh();

		ConfigedMain configedMain = new ConfigedMain(paramHost, paramUser, paramPassword, sshKey, sshKeyPass);

		SwingUtilities.invokeLater(configedMain::init);

		try {
			SwingUtilities.invokeAndWait(() -> {
				if (paramClient != null || paramClientgroup != null) {
					if (paramClientgroup != null) {
						configedMain.setGroup(paramClientgroup);
					}

					if (paramClient != null) {
						configedMain.setClient(paramClient);
					}

					Logging.info("set client " + paramClient);

					if (paramTab != null) {
						configedMain.setVisualViewIndex(paramTab);
					}
				}
			});

		} catch (InvocationTargetException ex) {
			Logging.info(" run " + ex);
		} catch (InterruptedException ie) {
			Logging.info(" run " + ie);
			Thread.currentThread().interrupt();
		}
	}

	private static void setParamValues(String paramHost, String paramUser, String paramPassword, Integer paramTab,
			String paramClient, String paramClientgroup) {

		Configed.paramHost = paramHost;
		Configed.paramUser = paramUser;
		Configed.paramPassword = paramPassword;
		Configed.paramTab = paramTab;
		Configed.paramClient = paramClient;
		Configed.paramClientgroup = paramClientgroup;
	}

	public static Integer getRefreshMinutes() {
		return refreshMinutes;
	}

	public static void endApp(int exitcode) {
		if (savedStates != null) {
			try {
				savedStates.store("states on finishing configed");
			} catch (IOException iox) {
				Logging.debug("could not store saved states, " + iox);
			}
		}

		OpsiMethodCall.report();
		Logging.info("regularly exiting app with code " + exitcode);

		if (exitcode == ERROR_OUT_OF_MEMORY) {
			fErrorOutOfMemory.setVisible(true);
		}

		System.exit(exitcode);
	}

	public static String getResourceValue(String key) {
		String result = null;
		try {
			if (extraLocalization != null) {
				result = extraLocalization.getProperty(key);
				if (result == null) {
					Logging.info("extraLocalization.getProperty null for key " + key);
				}
			}

			if (result == null) {
				result = Messages.messagesBundle.getString(key);
			}

			if (showLocalizationStrings) {
				Logging.info("LOCALIZE " + key + " by " + result);
				result = "" + result + "[[" + key + "]]";

			}
		} catch (MissingResourceException mre) {
			// we return the key and log the problem:
			Logging.debug("Problem: " + mre.toString());

			try {
				result = Messages.messagesEnBundle.getString(key);

				if (showLocalizationStrings) {
					Logging.info("LOCALIZE " + key + " by " + result);
					result = "" + result + "?? [[" + key + "]]";

				}
			} catch (MissingResourceException mre2) {
				Logging.debug("Problem: " + mre2.toString());

			}
		} catch (Exception ex) {
			Logging.warning("Failed to message " + key + ": " + ex);
		}

		if (result == null) {
			result = key;
		}
		return result;
	}

	private static void addMissingArgs() {
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

	private static Options createConfigedOptions() {
		Options options = new Options();
		options.addOption(new Option("l", "locale", true,
				"Set locale LOC (format: <language>_<country>). DEFAULT: System.locale"));
		options.addOption("h", "host", true, "Configuration server HOST to connect to. DEFAULT: choose interactive");
		options.addOption("u", "user", true, "user for authentication. DEFAULT: give interactive");
		options.addOption("p", "password", true, "password for authentication. DEFAULT: give interactive");
		options.addOption("c", "client", true, "CLIENT to preselect.  DEFAULT: no client selected");
		options.addOption("g", "clientgroup", true,
				"clientgroup to preselect. DEFAULT: last selected group reselected");
		options.addOption("t", "tab", true,
				"Start with tab number <arg>, index counting starts with 0, works only if a CLIENT is preselected. DEFAULT 0");
		options.addOption("s", "savedstates", true,
				"Directory for the files which keep states specific for a server connection. DEFAULT: Similar to log directory");
		options.addOption("r", "refreshminutes", true,
				"Refresh data every REFRESHMINUTES  (where this feature is implemented, 0 = never).DEFAULT: 0");
		options.addOption("qs", "querysavedsearch", true,
				"On command line: tell saved host searches list resp. the search result for [SAVEDSEARCH_NAME])");
		options.addOption("qg", "definegroupbysearch", true,
				"On command line: populate existing group GROUP_NAME with clients resulting frim search SAVEDSEARCH_NAME");
		options.addOption(null, "initUserRoles", false,
				"On command line, perform  the complete initialization of user roles if something was changed");
		options.addOption(null, "gzip", true,
				"Activate compressed transmission of data from opsi server yes/no. DEFAULT: y");
		options.addOption(null, "ssh-immediate-connect", true,
				"Try to create a SSH connection on start. DEFAULT: " + getYNforBoolean(sshConnectOnStart) + "");
		options.addOption(null, "ssh-key", true,
				"Full path with filename from sshkey used for authentication on ssh server");
		options.addOption(null, "ssh-passphrase", true,
				"Passphrase for given sshkey used for authentication on ssh server");
		options.addOption("v", "version", false, "Tell configed version");
		options.addOption(null, "collect_queries_until_no", true, "Collect the first N queries; N = "
				+ OpsiMethodCall.maxCollectSize + " (DEFAULT).  -1 meaning 'no collect'. 0 meaning 'infinite' ");
		options.addOption(null, "help", false, "Give this help");
		options.addOption(null, "localizationfile", true,
				"For translation work, use  EXTRA_LOCALIZATION_FILENAME as localization file, the file name format has to be: ");
		options.addOption(null, "localizationstrings", false,
				"For translation work, show internal labels together with the strings of selected localization");
		options.addOption(null, "swaudit-pdf", true,
				"export pdf swaudit reports for given clients (if no OUTPUT_PATH given, use home directory)");
		options.addOption(null, "swaudit-csv", true,
				"export csv swaudit reports for given clients (if no OUTPUT_PATH given, use home directory)");
		options.addOption(null, "disable-certificate-verification", false,
				"Disable opsi-certificate verification with server, by DEFAULT enabled");

		options.addOptionGroup(Main.getGeneralOptions());

		return options;
	}

	private static void parseArgs(Options options, String[] args) throws ParseException {

		CommandLineParser parser = new DefaultParser(false);
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("l")) {
			locale = cmd.getOptionValue("");
		}

		if (cmd.hasOption("h")) {
			host = cmd.getOptionValue("h");
		}

		if (cmd.hasOption("u")) {
			user = cmd.getOptionValue("u");
		}

		if (cmd.hasOption("p")) {
			password = cmd.getOptionValue("p");
		}

		if (cmd.hasOption("c")) {
			client = cmd.getOptionValue("c");
		}

		if (cmd.hasOption("g")) {
			clientgroup = cmd.getOptionValue("g");
		}

		if (cmd.hasOption("t")) {
			String tabString = cmd.getOptionValue("t");
			try {
				tab = Integer.parseInt(tabString);
			} catch (NumberFormatException ex) {
				Logging.debug("  \n\nArgument >" + tabString + "< has no integer format");
				Main.showHelp(options);
				endApp(ERROR_INVALID_OPTION);
			}
		}

		if (cmd.hasOption("s")) {
			savedStatesLocationName = cmd.getOptionValue("s");

			String canonicalPath = null;
			try {
				canonicalPath = new File(savedStatesLocationName).getCanonicalPath();
			} catch (IOException ex) {
				Logging.debug("savedstates argument " + ex);
			}
			if (canonicalPath != null) {
				savedStatesLocationName = canonicalPath;
			}
		}

		if (cmd.hasOption("r")) {
			String refreshMinutesString = cmd.getOptionValue("r");

			try {
				refreshMinutes = Integer.valueOf(refreshMinutesString);
			} catch (NumberFormatException ex) {
				Logging.debug("  \n\nArgument >" + refreshMinutesString + "< has no integer format");
				Main.showHelp(options);
				endApp(ERROR_INVALID_OPTION);
			}
		}

		if (cmd.hasOption("ssh-immediate-connect")) {
			String sshImmediateConnectString = cmd.getOptionValue("ssh-immediate-connect");

			if ("Y".equalsIgnoreCase(sshImmediateConnectString)) {
				sshConnectOnStart = true;
			} else if ("N".equalsIgnoreCase(sshImmediateConnectString)) {
				sshConnectOnStart = false;
			} else {
				Main.showHelp(options);
				endApp(ERROR_INVALID_OPTION);
			}
		}

		if (cmd.hasOption("ssh-key")) {
			sshKey = cmd.getOptionValue("ssh-key");
		}

		if (cmd.hasOption("ssh-passphrase")) {
			sshKeyPass = cmd.getOptionValue("ssh-passphrase");
		}

		if (cmd.hasOption("qs")) {
			optionCLIQuerySearch = true;
			savedSearch = cmd.getOptionValue("qs");
		}

		if (cmd.hasOption("qg")) {
			optionCLIDefineGroupBySearch = true;
			String[] values = cmd.getOptionValues("qg");
			savedSearch = values[0];
			group = values[1];
		}

		if (cmd.hasOption("initUserRoles")) {
			optionCLIuserConfigProducing = true;
		}

		if (cmd.hasOption("version")) {
			Logging.essential("configed version: " + Globals.VERSION + " (" + Globals.VERDATE + ") ");
			System.exit(0);
		}

		if (cmd.hasOption("help")) {
			Main.showHelp(options);
			endApp(NO_ERROR);
		}

		if (cmd.hasOption("collect_queries_until_no")) {
			String no = cmd.getOptionValue("collect_queries_until_no");

			try {
				OpsiMethodCall.maxCollectSize = Integer.parseInt(no);
			} catch (NumberFormatException ex) {
				Logging.debug("  \n\nArgument >" + no + "< has no integer format");
				Main.showHelp(options);
				endApp(ERROR_INVALID_OPTION);
			}
		}

		if (cmd.hasOption("loglevel")) {
			String loglevelString = "";
			try {
				loglevelString = cmd.getOptionValue("loglevel");
				loglevelFile = loglevelConsole = Integer.valueOf(loglevelString);
			} catch (NumberFormatException ex) {
				Logging.debug(" \n\nArgument >" + loglevelString + "< has no integer format");
			}
		}

		if (cmd.hasOption("localizationfile")) {
			String extraLocalizationFileName = cmd.getOptionValue("localizationfile");
			boolean success = false;

			String[] parts = null;

			try {
				File extraLocalizationFile = new File(extraLocalizationFileName);
				if (!extraLocalizationFile.exists()) {
					Logging.debug("File not found: " + extraLocalizationFileName);
				} else if (!extraLocalizationFile.canRead()) {
					Logging.debug("File not readable " + extraLocalizationFileName);
				} else {
					Logging.debug(" ok " + LOCALIZATION_FILENAME_REGEX + "? "
							+ extraLocalizationFileName.matches("configed_...*\\.properties") + " --  "
							+ extraLocalizationFileName.matches(LOCALIZATION_FILENAME_REGEX));

					parts = extraLocalizationFileName.split("_");

					Logging.debug(" . " + parts[1] + " .. " + Arrays.toString(parts[1].split("\\.")));

					if (!extraLocalizationFileName.matches(LOCALIZATION_FILENAME_REGEX)) {
						Logging.debug("localization file does not have the expected format " + Messages.APPNAME
								+ "_LOCALE.properties");
					} else {
						extraLocalization = new PropertiesStore(extraLocalizationFile);
						extraLocalization.load();

						success = true;
					}
				}
			} catch (Exception ex) {
				Logging.error(extraLocalizationFileName + " problem", ex);
			}

			if (!success) {
				endApp(ERROR_CANNOT_READ_EXTRA_LOCALIZATION);
			}
		}

		if (cmd.hasOption("localizationstrings")) {
			showLocalizationStrings = true;
		}

		if (cmd.hasOption("swaudit-pdf")) {
			optionCLISwAuditPDF = true;
			String[] values = cmd.getOptionValues("swaudit-pdf");
			clientsFile = values[0];

			outDir = args[1];
		}

		if (cmd.hasOption("swaudit-csv")) {
			optionCLISwAuditCSV = true;
			String[] values = cmd.getOptionValues("swaudit-pdf");
			clientsFile = values[0];

			outDir = args[1];
		}

		if (cmd.hasOption("disable-certificate-verification")) {
			Globals.disableCertificateVerification = true;
		}

		Logging.debug("configed: args recognized");

		Logging.setLogLevelConsole(loglevelConsole);
		Logging.setLogLevelFile(loglevelFile);
		Logging.setLogfileMarker(host);
		Logging.init();
		Logging.essential("Configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") starting");
		if (optionCLIQuerySearch || optionCLIDefineGroupBySearch) {
			Logging.setSuppressConsole();
		}
	}

	/**
	 * main-Methode
	 */
	public static void main(String[] args) {

		Options options = createConfigedOptions();
		try {
			parseArgs(options, args);
		} catch (ParseException pe) {
			Main.showHelp(options);
			Logging.error("could not parse parameters", pe);
			endApp(ERROR_MISSING_VALUE_FOR_OPTION);
		}

		startConfiged();
	}

	private static void startConfiged() {
		Logging.debug("initiating configed");

		if (optionCLIQuerySearch) {

			Logging.debug("optionCLIQuerySearch");
			SavedSearchQuery query = new SavedSearchQuery();

			query.setArgs(host, user, password, savedSearch);
			query.addMissingArgs();

			query.runSearch(true);
			System.exit(0);
		} else if (optionCLIDefineGroupBySearch) {
			Logging.debug("optionCLIDefineGroupBySearch");

			SavedSearchQuery query = new SavedSearchQuery();

			query.setArgs(host, user, password, savedSearch);
			query.addMissingArgs();
			List<String> newGroupMembers = query.runSearch(false);

			query.populateHostGroup(newGroupMembers, group);
			System.exit(0);
		} else if (optionCLISwAuditPDF) {
			Logging.debug("optionCLISwAuditPDF");
			SwPdfExporter exporter = new SwPdfExporter();
			exporter.setArgs(host, user, password, clientsFile, outDir);
			exporter.addMissingArgs();
			exporter.run();

			System.exit(0);
		} else if (optionCLISwAuditCSV) {
			Logging.debug("optionCLISwAuditCSV");
			SWcsvExporter exporter = new SWcsvExporter();
			exporter.setArgs(host, user, password, clientsFile, outDir);
			exporter.addMissingArgs();
			exporter.run();

			System.exit(0);
		} else if (optionCLIuserConfigProducing) {
			Logging.debug("UserConfigProducing");
			Logging.setLogLevelConsole(loglevelConsole);
			Logging.setLogLevelFile(loglevelFile);

			addMissingArgs();

			AbstractPersistenceController persist = PersistenceControllerFactory.getNewPersistenceController(host, user,
					password);

			UserConfigProducing up = new UserConfigProducing(false, host,
					persist.getHostInfoCollections().getDepotNamesList(), persist.getHostGroupIds(),
					persist.getProductGroups().keySet(),

					// data. on which changes are based
					persist.getConfigDefaultValues(), persist.getConfigOptions());

			List<Object> newData = up.produce();
			Logging.debug("UserConfigProducing: newData " + newData);

			System.exit(0);
		}

		try {
			URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
			if (resource == null) {
				Logging.debug("image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			}
		} catch (Exception ex) {
			Logging.debug("imageHandled failed: " + ex.toString());
		}

		// Turn on antialiasing for text (not for applets)
		try {
			System.setProperty("swing.aatext", "true");
		} catch (Exception ex) {
			Logging.info(" setting property swing.aatext" + ex);
		}

		fErrorOutOfMemory = new FTextArea(null, "configed", true, new String[] { "ok" }, 400, 400);

		if (!ConfigedMain.THEMES) {
			fErrorOutOfMemory.setContentBackground(Globals.darkOrange);
		}
		// we activate it in case of an appropriate error

		if (!ConfigedMain.FONT) {
			fErrorOutOfMemory.setFont(Globals.defaultFontBig);
		}
		fErrorOutOfMemory
				.setMessage("The program will be terminated,\nsince more memory is required than was assigned.");

		new Configed(locale, host, user, password, client, clientgroup, tab);
	}
}
