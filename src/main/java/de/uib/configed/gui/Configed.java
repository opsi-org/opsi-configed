/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;

import de.uib.configed.app.Main;
import de.uib.configed.core.domain.modulelicense.LicensingInfoMap;
import de.uib.configed.core.domain.permission.UserConfigProducing;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.HostData;
import de.uib.configed.core.infrastructure.ServerFacade;
import de.uib.configed.gui.data.InstallationStateTableModel;
import de.uib.configed.gui.features.clientselection.SavedSearchQuery;
import de.uib.configed.gui.features.swinfopage.SWcsvExporter;
import de.uib.configed.gui.features.swinfopage.SwPdfExporter;
import de.uib.configed.gui.messages.Messages;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.savedstates.SavedStates;

public final class Configed {
	private static final String LOCALIZATION_FILENAME_REGEX = "configed_...*\\.properties";
	private static final Pattern localizationFilenameRegex = Pattern.compile(LOCALIZATION_FILENAME_REGEX);

	public static final Charset serverCharset = StandardCharsets.UTF_8;
	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String JAVA_VENDOR = System.getProperty("java.vendor", "");
	public static final String SYSTEM_SSL_VERSION = System.getProperty("https.protocols");

	private static Properties extraLocalization;
	private static boolean showLocalizationStrings;

	private static HostData paramHostData;

	private static boolean optionCLIQuerySearch;
	private static String savedSearch;
	private static boolean optionCLIDefineGroupBySearch;
	private static String group;
	private static boolean optionCLISwAuditPDF;
	private static boolean optionCLISwAuditCSV;
	private static String clientsFile;
	private static String outDir;

	private static boolean optionCLIuserConfigProducing;

	private static SavedStates savedStates;
	public static final String SAVED_STATES_FILENAME = "configedStates.prop";

	private static HostData hostData = new HostData();

	private Configed(HostData paramHostData) {
		setParamValues(paramHostData);

		Logging.debug("starting ", getClass().getName());
		Logging.debug("default charset is ", Charset.defaultCharset().displayName());
		Logging.debug("server charset is configured as ", serverCharset);

		if (serverCharset.equals(Charset.defaultCharset())) {
			Logging.debug("they are equal");
		}

		startConfiged();
	}

	public static void startConfiged() {
		Logging.notice("system information: ");

		Logging.notice(" configed version ", Globals.VERSION, " (", Globals.VERDATE, ")");
		Logging.notice(" running by java version ", JAVA_VERSION, " and java vendor ", JAVA_VENDOR);

		// Try with resources so that it will be closed in implicit finally statement
		try (Formatter formatter = new Formatter()) {
			Logging.info(" we get max memory ",
					formatter.format("%,d MB", Runtime.getRuntime().maxMemory() / 1_000_000));
		}

		LicensingInfoMap.requestRefresh();

		SwingUtilities.invokeLater(Configed::initLoginDialog);
	}

	private static void initLoginDialog() {
		Logging.debug("init");

		// we start with a language

		InstallationStateTableModel.restartColumnDict();

		OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
				.getPersistenceController();
		HostData hostData = paramHostData;

		if (persistenceController != null) {
			ServerFacade exec = persistenceController.getExecutioner();
			hostData = exec.getHostData();
		}

		ConfigedMain.setupLoginDialog(hostData);
	}

	private static void setParamValues(HostData paramHostData) {
		Configed.paramHostData = paramHostData;
	}

	public static String getResourceValue(String key) {
		String result = null;
		if (extraLocalization != null) {
			result = extraLocalization.getProperty(key);
			if (result == null) {
				Logging.info("extraLocalization.getProperty null for key ", key);
			}
		}

		if (result == null) {
			if (Messages.getResourceBundle() == null) {
				Logging.warning("Messages.messagesBundle is null...");
			} else if (!Messages.getResourceBundle().containsKey(key)) {
				Logging.warning("Messagebundle does not contain key ", key);
			} else {
				result = Messages.getResourceBundle().getString(key);
			}
		}

		if (showLocalizationStrings) {
			Logging.essential("Localization of '", key, "': ", result);
			result = "" + result + "[[" + key + "]]";
		}

		if (result == null) {
			result = key;
		}

		return result;
	}

	private static void addMissingArgs() {
		if (hostData.getHost() == null) {
			hostData.setHost(Utils.getCLIParam("Host: "));
		}
		if (hostData.getUser() == null) {
			hostData.setUser(Utils.getCLIParam("User: ").toLowerCase(Locale.ROOT));
		}
		if (hostData.getPassword() == null) {
			hostData.setPassword(Utils.getCLIPasswordParam("Password: "));
		}
		if (hostData.getOtp() == null) {
			hostData.setOtp(
					Utils.getCLIParam("One Time Password (not required if you don't have license or OTP enabled): "));
		}
	}

	private static void processLoginOptions(CommandLine cmd) {
		if (cmd.hasOption("h")) {
			hostData.setHost(cmd.getOptionValue("h"));
		}

		if (cmd.hasOption("u")) {
			hostData.setUser(cmd.getOptionValue("u"));
		}

		if (cmd.hasOption("p")) {
			hostData.setPassword(cmd.getOptionValue("p"));
		}

		if (cmd.hasOption("otp")) {
			hostData.setOtp(cmd.getOptionValue("otp"));
		}

		if (cmd.hasOption("sso")) {
			// Single sign-on is not implemented
			hostData.useSSO(true);
		}
	}

	private static void processNonGUIOptions(CommandLine cmd) {
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

		if (cmd.hasOption("swaudit-pdf")) {
			optionCLISwAuditPDF = true;
			String[] values = cmd.getOptionValues("swaudit-pdf");
			clientsFile = values[0];
			outDir = values[1];
		}

		if (cmd.hasOption("swaudit-csv")) {
			optionCLISwAuditCSV = true;
			String[] values = cmd.getOptionValues("swaudit-csv");
			clientsFile = values[0];
			outDir = values[1];
		}
	}

	private static void processLocalizationOptions(CommandLine cmd) {
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
	}

	private static void processArgs(CommandLine cmd) {
		processLoginOptions(cmd);

		processNonGUIOptions(cmd);

		processLocalizationOptions(cmd);

		if (cmd.hasOption("disable-certificate-verification")) {
			Utils.setDisableCertificateVerification(true);
		}
	}

	private static boolean loadLocalizationFile(String extraLocalizationFileName) {
		String[] parts = null;

		File extraLocalizationFile = new File(extraLocalizationFileName);
		if (!extraLocalizationFile.exists()) {
			Logging.debug("File not found: ", extraLocalizationFileName);
		} else if (!extraLocalizationFile.canRead()) {
			Logging.debug("File not readable ", extraLocalizationFileName);
		} else {
			Logging.debug(" ok ", LOCALIZATION_FILENAME_REGEX, "? ",
					localizationFilenameRegex.matcher(extraLocalizationFileName).matches());

			parts = extraLocalizationFileName.split("_");

			Logging.debug(" . ", parts[1], " .. ", Arrays.toString(parts[1].split("\\.")));

			if (localizationFilenameRegex.matcher(extraLocalizationFileName).matches()) {
				return loadExtraLocalization(extraLocalizationFile);
			} else {
				Logging.debug("localization file does not have the expected format opsi-configed_LOCALE.properties");
			}
		}

		return false;
	}

	private static boolean loadExtraLocalization(File extraLocalizationFile) {
		extraLocalization = new Properties();

		try (FileInputStream inputStream = new FileInputStream(extraLocalizationFile)) {
			extraLocalization.load(inputStream);
		} catch (IOException ex) {
			Logging.warning(ex, "could not load properties file ", extraLocalizationFile);
			return false;
		}

		return true;
	}

	private static void initLogging() {
		Logging.initLogFile();
		Logging.essential("Configed version ", Globals.VERSION, " (", Globals.VERDATE, ") starting");
		if (optionCLIQuerySearch || optionCLIDefineGroupBySearch) {
			Logging.setLogLevelConsole(0);
		}
	}

	public static void main(CommandLine cmd) {
		processArgs(cmd);

		// Set timeout for login
		// seems that this timeout is not used if server is incorrect; setting timeout additionally in ServerFacade
		System.setProperty("sun.net.client.defaultConnectTimeout", Globals.DEFAULT_TIMEOUT + "");
		Logging.debug("configed: args recognized");

		initLogging();

		checkArgsAndStart();
	}

	private static void checkArgsAndStart() {
		Logging.debug("initiating configed");

		if (optionCLIQuerySearch) {
			addMissingArgs();
			initSavedStates(hostData.getHost());
			Logging.debug("optionCLIQuerySearch");
			SavedSearchQuery query = new SavedSearchQuery(hostData, savedSearch);

			query.runSearch(true);
			Main.endApp(Main.NO_ERROR);
		} else if (optionCLIDefineGroupBySearch) {
			addMissingArgs();
			initSavedStates(hostData.getHost());
			Logging.debug("optionCLIDefineGroupBySearch");

			SavedSearchQuery query = new SavedSearchQuery(hostData, savedSearch);

			Collection<String> newGroupMembers = query.runSearch(false);

			query.populateHostGroup(newGroupMembers, group);
			Main.endApp(Main.NO_ERROR);
		} else if (optionCLISwAuditPDF) {
			Logging.debug("optionCLISwAuditPDF");
			SwPdfExporter exporter = new SwPdfExporter();
			exporter.setArgs(hostData, clientsFile, outDir);
			exporter.addMissingArgs();
			exporter.run();

			Main.endApp(Main.NO_ERROR);
		} else if (optionCLISwAuditCSV) {
			Logging.debug("optionCLISwAuditCSV");
			SWcsvExporter exporter = new SWcsvExporter();
			exporter.setArgs(hostData, clientsFile, outDir);
			exporter.addMissingArgs();
			exporter.run();

			Main.endApp(Main.NO_ERROR);
		} else if (optionCLIuserConfigProducing) {
			Logging.debug("UserConfigProducing");

			addMissingArgs();
			initSavedStates(hostData.getHost());

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getNewPersistenceController(hostData);

			new UserConfigProducing(false, hostData.getHost(),
					persistenceController.getDataServices().hostInfoCollections.getDepotNamesList(),
					persistenceController.getDataServices().group.getHostGroupIds(),
					persistenceController.getDataServices().group.getProductGroupsPD().keySet(),
					persistenceController.getDataServices().config.getConfigDefaultValuesPD(),
					persistenceController.getDataServices().config.getConfigOptionsPD()).produce();

			Main.endApp(Main.NO_ERROR);
		} else {
			Logging.info("start configed gui since no options for CLI-mode were chosen");
		}

		new Configed(hostData);
	}

	public static void initSavedStates(String host) {
		File directory = prepareDirectory(Main.getSavedStatesLocationName(), host);
		savedStates = new SavedStates(new File(directory, Configed.SAVED_STATES_FILENAME));

		loadSavedStates();
		incrementUsageCounter();
	}

	private static File prepareDirectory(String location, String host) {
		String directoryName = getSavedStatesDirectoryName(location, host);
		File dir = new File(directoryName);

		if (!dir.exists() && !dir.mkdirs()) {
			Logging.warning("mkdirs for saved states failed for", dir);
		}

		if (!dir.setWritable(true, true)) {
			Logging.warning("Setting savedStatesDir writable failed");
		}

		return dir;
	}

	private static String getSavedStatesDirectoryName(String locationName, String host) {
		return locationName + File.separator + host.replace(":", "_");
	}

	private static void loadSavedStates() {
		try {
			savedStates.load();
		} catch (IOException ex) {
			Logging.warning(ex, "Saved states file could not be loaded");
		}
	}

	private static void incrementUsageCounter() {
		int oldUsageCount = Integer.parseInt(savedStates.getProperty("saveUsageCount", "0"));
		savedStates.setProperty("saveUsageCount", String.valueOf(oldUsageCount + 1));
	}

	public static SavedStates getSavedStates() {
		return savedStates;
	}
}
