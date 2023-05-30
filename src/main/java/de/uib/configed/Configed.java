/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;

import de.uib.Main;
import de.uib.configed.clientselection.SavedSearchQuery;
import de.uib.configed.gui.swinfopage.SWcsvExporter;
import de.uib.configed.gui.swinfopage.SwPdfExporter;
import de.uib.messages.Messages;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.savedstates.SavedStates;

public final class Configed {

	private static final String LOCALIZATION_FILENAME_REGEX = Messages.APPNAME + "_...*\\.properties";

	public static boolean sshConnectOnStart;

	public static final Charset serverCharset = StandardCharsets.UTF_8;
	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String JAVA_VENDOR = System.getProperty("java.vendor", "");
	public static final String SYSTEM_SSL_VERSION = System.getProperty("https.protocols");

	private static Properties extraLocalization;
	private static boolean showLocalizationStrings;

	private static String host;
	private static String user;
	private static String password;

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

	/** construct the application */
	private Configed(String paramHost, String paramUser, String paramPassword, final String paramClient,
			final String paramClientgroup, final Integer paramTab) {

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

		startConfiged();
	}

	public static void startConfiged() {
		Logging.notice("system information: ");

		Logging.notice(" configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);
		Logging.notice(" running by java version " + JAVA_VERSION + " and java vendor " + JAVA_VENDOR);

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
			SwingUtilities.invokeAndWait(() -> setStartSettings(configedMain));
		} catch (InvocationTargetException ex) {
			Logging.info(" run " + ex);
		} catch (InterruptedException ie) {
			Logging.info(" run " + ie);
			Thread.currentThread().interrupt();
		}
	}

	public static void restartConfiged() {
		new Thread() {
			@Override
			public void run() {
				Configed.startConfiged();
			}
		}.start();
	}

	private static void setStartSettings(ConfigedMain configedMain) {
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
			Logging.warning("Failed to message " + key, ex);
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

	private static void processArgs(CommandLine cmd) {

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
				Main.showHelp();
				Main.endApp(Main.ERROR_INVALID_OPTION);
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
				Main.showHelp();
				Main.endApp(Main.ERROR_INVALID_OPTION);
			}
		}

		if (cmd.hasOption("ssh-immediate-connect")) {
			String sshImmediateConnectString = cmd.getOptionValue("ssh-immediate-connect");

			if ("Y".equalsIgnoreCase(sshImmediateConnectString)) {
				sshConnectOnStart = true;
			} else if ("N".equalsIgnoreCase(sshImmediateConnectString)) {
				sshConnectOnStart = false;
			} else {
				Main.showHelp();
				Main.endApp(Main.ERROR_INVALID_OPTION);
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

		if (cmd.hasOption("collect_queries_until_no")) {
			String no = cmd.getOptionValue("collect_queries_until_no");

			try {
				OpsiMethodCall.maxCollectSize = Integer.parseInt(no);
			} catch (NumberFormatException ex) {
				Logging.debug("  \n\nArgument >" + no + "< has no integer format");
				Main.showHelp();
				Main.endApp(Main.ERROR_INVALID_OPTION);
			}
		}

		if (cmd.hasOption("localizationfile")) {
			String extraLocalizationFileName = cmd.getOptionValue("localizationfile");
			boolean success = loadLocalizationFile(extraLocalizationFileName);

			if (!success) {
				Main.endApp(Main.ERROR_CANNOT_READ_EXTRA_LOCALIZATION);
			}
		}

		if (cmd.hasOption("localizationstrings")) {
			showLocalizationStrings = true;
		}

		if (cmd.hasOption("swaudit-pdf")) {
			optionCLISwAuditPDF = true;
			String[] values = cmd.getOptionValues("swaudit-pdf");
			clientsFile = values[0];
			outDir = values[1];
		}

		if (cmd.hasOption("swaudit-csv")) {
			optionCLISwAuditCSV = true;
			String[] values = cmd.getOptionValues("swaudit-pdf");
			clientsFile = values[0];
			outDir = values[1];
		}

		if (cmd.hasOption("disable-certificate-verification")) {
			Globals.disableCertificateVerification = true;
		}

		Logging.debug("configed: args recognized");

		Logging.setLogfileMarker(host);
		Logging.init();
		Logging.essential("Configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") starting");
		if (optionCLIQuerySearch || optionCLIDefineGroupBySearch) {
			Logging.setSuppressConsole();
		}
	}

	private static boolean loadLocalizationFile(String extraLocalizationFileName) {

		String[] parts = null;

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
				return loadExtraLocalization(extraLocalizationFile);
			}
		}

		return false;
	}

	private static boolean loadExtraLocalization(File extraLocalizationFile) {

		extraLocalization = new Properties();

		try (FileInputStream inputStream = new FileInputStream(extraLocalizationFile)) {
			extraLocalization.load(inputStream);
		} catch (IOException ex) {
			Logging.warning("could not load properties file " + extraLocalizationFile, ex);
			return false;
		}

		return true;
	}

	/**
	 * main-Methode
	 */
	public static void main(CommandLine cmd) {

		processArgs(cmd);

		checkArgsAndStart();
	}

	private static void checkArgsAndStart() {
		Logging.debug("initiating configed");

		if (optionCLIQuerySearch) {

			Logging.debug("optionCLIQuerySearch");
			SavedSearchQuery query = new SavedSearchQuery();

			query.setArgs(host, user, password, savedSearch);
			query.addMissingArgs();

			query.runSearch(true);
			Main.endApp(Main.NO_ERROR);
		} else if (optionCLIDefineGroupBySearch) {
			Logging.debug("optionCLIDefineGroupBySearch");

			SavedSearchQuery query = new SavedSearchQuery();

			query.setArgs(host, user, password, savedSearch);
			query.addMissingArgs();
			List<String> newGroupMembers = query.runSearch(false);

			query.populateHostGroup(newGroupMembers, group);
			Main.endApp(Main.NO_ERROR);
		} else if (optionCLISwAuditPDF) {
			Logging.debug("optionCLISwAuditPDF");
			SwPdfExporter exporter = new SwPdfExporter();
			exporter.setArgs(host, user, password, clientsFile, outDir);
			exporter.addMissingArgs();
			exporter.run();

			Main.endApp(Main.NO_ERROR);
		} else if (optionCLISwAuditCSV) {
			Logging.debug("optionCLISwAuditCSV");
			SWcsvExporter exporter = new SWcsvExporter();
			exporter.setArgs(host, user, password, clientsFile, outDir);
			exporter.addMissingArgs();
			exporter.run();

			Main.endApp(Main.NO_ERROR);
		} else if (optionCLIuserConfigProducing) {
			Logging.debug("UserConfigProducing");

			addMissingArgs();

			OpsiserviceNOMPersistenceController persist = PersistenceControllerFactory.getNewPersistenceController(host,
					user, password);

			UserConfigProducing up = new UserConfigProducing(false, host,
					persist.getHostInfoCollections().getDepotNamesList(), persist.getHostGroupIds(),
					persist.getProductGroups().keySet(),

					// data. on which changes are based
					persist.getConfigDefaultValues(), persist.getConfigOptions());

			List<Object> newData = up.produce();
			Logging.debug("UserConfigProducing: newData " + newData);

			Main.endApp(Main.NO_ERROR);
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

		new Configed(host, user, password, client, clientgroup, tab);
	}
}
