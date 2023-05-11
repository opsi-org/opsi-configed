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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import de.uib.configed.clientselection.SavedSearchQuery;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.swinfopage.SWcsvExporter;
import de.uib.configed.gui.swinfopage.SwPdfExporter;
import de.uib.configed.tree.ClientTreeUI;
import de.uib.logviewer.Logview;
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.utilities.PropertiesStore;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.savedstates.SavedStates;

public class Configed {

	private static boolean logviewer;

	private static final String LOCALIZATION_FILENAME_REGEX = Messages.APPNAME + "_...*\\.properties";

	public static boolean sshConnectOnStart;

	public static final String USAGE_INFO = "\n" + "\tconfiged [OPTIONS] \n" + "\t\twhere an OPTION may be \n";
	/*
	 * "-l LOC, \t--locale LOC \t\t\t(Set locale LOC (format: <language>_<country>)) \n"
	 * +
	 * "-h HOST, \t--host HOST \t\t\t(Configuration server HOST to connect to, --help shows the command usage) \n"
	 * +
	 * "-u NAME, \t--user NAME \t\t\t(user for authentication) \n" +
	 * "-p PASSWORD, \t--password PASSWORD\t\t(password for authentication) \n" +
	 * "-c CLIENT, \t--client CLIENT \t\t(CLIENT to preselect) + \n" +
	 * "-t INDEX, \t--tab INDEX\t\t\t(Start with tab number INDEX, index counting starts with 0, works only if a CLIENT is preselected ) + \n"
	 * +
	 * "-d PATH, \t--logdirectory PATH \t\t(Directory for the log files) \n" +
	 * "-qs [SAVEDSEARCH_NAME], \t\t--querysavedsearch [SAVEDSEARCH_NAME]\t\t(On command line: tell saved host searches list resp. the search result for [SAVEDSEARCH_NAME])\n"
	 * +
	 * //"-qsj, \t--querysavedsearchjson \t(CLI: same as querysavedsearch, but output as jsonlist)\n"
	 * +
	 * "--gzip \t\t\t\t\t\t(Activate gzip transmission of data from opsi server) \n"
	 * +
	 * "--version \t\t\t\t\t(Tell configed version)\n" +
	 * "--help \t\t\t\t\t\t(Give this help)\n" +
	 * "--loglevel L \t\t\t\t\t(Set logging level L, L is a number >= " +
	 */

	private static final String[][] usageLines = new String[][] {
			new String[] { "-l LOC", "--locale LOC",
					"Set locale LOC (format: <language>_<country>). DEFAULT: System.locale" },
			new String[] { "-h HOST", "--host HOST",
					"Configuration server HOST to connect to. DEFAULT: choose interactive" },
			new String[] { "-u NAME", "--user NAME", "user for authentication. DEFAULT: give interactive" },
			new String[] { "-p PASSWORD", "--password PASSWORD",
					"password for authentication. DEFAULT: give interactive " },
			new String[] { "-c CLIENT", "--client CLIENT", "CLIENT to preselect.  DEFAULT: no client selected" },
			new String[] { "-g CLIENTGROUP", "--group CLIENTGROUP",
					"CLIENTGROUP to preselect. DEFAULT: last selected group reselected" },
			new String[] { "-t INDEX", "--tab INDEX",
					"Start with tab number INDEX, index counting starts with 0, works only if a CLIENT is preselected. DEFAULT 0" },
			new String[] { "-d PATH", "--logdirectory PATH",
					"Directory for the log files. DEFAULT: an opsi log directory, dependent on system and user privileges, lookup in /help/logfile" },
			new String[] { "-s PATH", "--savedstates PATH",
					"Directory for the files which keep states specific for a server connection. DEFAULT: Similar to log directory" },

			new String[] { "-r REFRESHMINUTES", "--refreshminutes REFRESHMINUTES",
					"Refresh data every REFRESHMINUTES  (where this feature is implemented, 0 = never).DEFAULT: 0" },
			new String[] { "-qs [SAVEDSEARCH_NAME]", "--querysavedsearch [SAVEDSEARCH_NAME]",
					"On command line: tell saved host searches list resp. the search result for [SAVEDSEARCH_NAME])" },
			// "-qsj, \t--querysavedsearchjson \t(CLI: same as querysavedsearch, but output
			// as jsonlist)\n" +

			new String[] { "-qg SAVEDSEARCH_NAME GROUP_NAME", "--definegroupbysearch SAVEDSEARCH_NAME GROUP_NAME",
					"On command line: populate existing group GROUP_NAME with clients resulting frim search SAVEDSEARCH_NAME" },
			new String[] { "--initUserRoles", "",
					"On command line, perform  the complete initialization of user roles if something was changed" },

			new String[] { "--gzip [y/n]", "",
					"Activate compressed transmission of data from opsi server yes/no. DEFAULT: y" },
			new String[] { "--ssh-immediate-connect [y/n]", "",
					"Try to create a SSH connection on start. DEFAULT: " + getYNforBoolean(sshConnectOnStart) + "" },
			new String[] { "--ssh-key SSHKEY", "",
					"Full path with filename from sshkey used for authentication on ssh server" },
			new String[] { "--ssh-passphrase PASSPHRASE", "",
					"Passphrase for given sshkey used for authentication on ssh server" },
			new String[] { "--version", "", "Tell configed version" },
			new String[] { "--collect_queries_until_no N", "",
					"Collect the first N queries; N = " + OpsiMethodCall.maxCollectSize
							+ " (DEFAULT).  -1 meaning 'no collect'. 0 meaning 'infinite' " },
			new String[] { "--help", "", "Give this help" },
			new String[] { "--loglevel L", "",
					"Set logging level L, L is a number >= " + Logging.LEVEL_NONE + ", <= " + Logging.LEVEL_SECRET
							+ " . DEFAULT: " + Logging.getLogLevelConsole() },
			new String[] { "--halt", "", "Use  first occurring debug halt point that may be in the code" },

			// if possible in PersistenceController,
			new String[] { "--localizationfile EXTRA_LOCALIZATION_FILENAME", "",
					"For translation work, use  EXTRA_LOCALIZATION_FILENAME as localization file, the file name format has to be: "
							+ LOCALIZATION_FILENAME_REGEX.replace("...*\\", "LOCALENAME") },
			new String[] { "--localizationstrings", "",
					"For translation work, show internal labels together with the strings of selected localization" },
			new String[] { "--swaudit-pdf FILE_WITH_CLIENT_IDS_EACH_IN_A_LINE [OUTPUT_PATH]", "",
					"export pdf swaudit reports for given clients (if no OUTPUT_PATH given, use home directory)" },
			new String[] { "--swaudit-csv FILE_WITH_CLIENT_IDS_EACH_IN_A_LINE [OUTPUT_PATH]", "",
					"export csv swaudit reports for given clients (if no OUTPUT_PATH given, use home directory)" },

			new String[] { "--disable-certificate-verification", "",
					"Disable opsi-certificate verification with server, by DEFAULT enabled" },

			new String[] { "--logviewer", "", "this option will start the logviewer instead of the configed" }

			// undocumented
	};

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

	private static boolean optionPersistenceControllerMethodCall;

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

		if (ConfigedMain.THEMES) {
			setOpsiLaf();
		} else {
			configureUI();
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

	private static String tabs(int count) {
		StringBuilder buf = new StringBuilder("");
		for (int j = 0; j < count; j++) {
			buf.append("\t");
		}
		return buf.toString();
	}

	@SuppressWarnings("java:S106")
	private static void usage() {
		Logging.essential("configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);
		Logging.essential(USAGE_INFO);

		final int tabWidth = 8;
		int length0 = 0;
		int length1 = 0;

		for (int i = 0; i < usageLines.length; i++) {
			// we find max of fillTabs0, fillTabs1
			int len = usageLines[i][0].length();

			if (len > length0) {
				length0 = len;
			}

			len = usageLines[i][1].length();

			if (len > length1) {
				length1 = len;
			}
		}

		int allTabs0 = length0 / tabWidth + 1;
		int allTabs1 = length1 / tabWidth + 1;

		for (int i = 0; i < usageLines.length; i++) {
			int startedTabs0 = usageLines[i][0].length() / tabWidth;
			int startedTabs1 = usageLines[i][1].length() / tabWidth;

			Logging.essential("\t" + usageLines[i][0] + tabs(allTabs0 - startedTabs0) + usageLines[i][1]
					+ tabs(allTabs1 - startedTabs1) + usageLines[i][2]);
		}
	}

	private static boolean isValue(String[] args, int i) {
		return i < args.length && args[i].indexOf('-') != 0;
	}

	private static String getArg(String[] args, int i) {
		if (args.length <= i + 1 || args[i + 1].indexOf('-') == 0) {
			Logging.error("Missing value for option " + args[i]);
			usage();
			endApp(ERROR_MISSING_VALUE_FOR_OPTION);
		}
		i++;
		return args[i];
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

		de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText.reset();
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

	@SuppressWarnings("java:S106")
	private static void processArgs(String[] args) {
		Logging.debug("args " + Arrays.toString(args));

		if (args.length == 2 && "--args".equals(args[0])) {
			args = args[1].split(";;");
		}

		for (int i = 0; i < args.length; i++) {

			if ("--help".equals(args[i])) {
				usage();
				endApp(NO_ERROR);
			}
		}

		int firstPossibleNonOptionIndex = args.length - 1;

		int i = 0;
		while (i < args.length) {

			// no option
			if (args[i].charAt(0) != '-') {
				if (i < firstPossibleNonOptionIndex) {
					usage();
					endApp(NO_ERROR);
				}
				i++;
			} else {
				// options

				if ("-l".equals(args[i]) || "--locale".equals(args[i])) {
					locale = getArg(args, i);
					i = i + 2;
				} else if ("-h".equals(args[i]) || "--host".equals(args[i])) {
					host = getArg(args, i);
					i = i + 2;
				} else if ("-u".equals(args[i]) || "--user".equals(args[i])) {
					user = getArg(args, i);
					i = i + 2;
				} else if ("-p".equals(args[i]) || "--password".equals(args[i])) {
					password = getArg(args, i);
					i = i + 2;
				} else if ("-c".equals(args[i]) || "--client".equals(args[i])) {
					client = getArg(args, i);
					i = i + 2;
				} else if ("-g".equals(args[i]) || "--group".equals(args[i])) {
					clientgroup = getArg(args, i);
					i = i + 2;
				} else if ("-t".equals(args[i]) || "--tab".equals(args[i])) {
					String tabS = getArg(args, i);
					try {
						tab = Integer.parseInt(tabS);
					} catch (NumberFormatException ex) {
						Logging.debug("  \n\nArgument >" + tabS + "< has no integer format");
						usage();
						endApp(ERROR_INVALID_OPTION);
					}
					i = i + 2;
				} else if ("-d".equals(args[i]) || "--logdirectory".equals(args[i])) {
					Logging.logDirectoryName = getArg(args, i);
					i = i + 2;
				} else if ("-s".equals(args[i]) || "--savedstates".equals(args[i])) {
					savedStatesLocationName = getArg(args, i);
					String canonicalPath = null;
					try {
						canonicalPath = new File(savedStatesLocationName).getCanonicalPath();
					} catch (IOException ex) {
						Logging.debug("savedstates argument " + ex);
					}
					if (canonicalPath != null) {
						savedStatesLocationName = canonicalPath;
					}

					i = i + 2;
				} else if ("-r".equals(args[i]) || "--refreshminutes".equals(args[i])) {
					String test = getArg(args, i);

					try {
						refreshMinutes = Integer.valueOf(test);
					} catch (NumberFormatException ex) {
						Logging.debug("  \n\nArgument >" + test + "< has no integer format");
						usage();
						endApp(ERROR_INVALID_OPTION);
					}

					i = i + 2;
				} else if ("--ssh-immediate-connect".equals(args[i])) {
					i = i + 1;

					if (isValue(args, i)) {
						if ("Y".equalsIgnoreCase(args[i])) {
							sshConnectOnStart = true;

						} else if ("N".equalsIgnoreCase(args[i])) {
							sshConnectOnStart = false;

						} else {
							usage();
							endApp(ERROR_INVALID_OPTION);
						}
					}
					i = i + 1;
				} else if ("--ssh-key".equals(args[i])) {
					sshKey = getArg(args, i);
					i = i + 2;
				} else if ("--ssh-passphrase".equals(args[i])) {
					sshKeyPass = getArg(args, i);
					i = i + 2;
				} else if ("-qs".equals(args[i]) || "--querysavedsearch".equals(args[i])) {
					optionCLIQuerySearch = true;
					savedSearch = getArg(args, i);
					i = i + 2;

				} else if ("-qg".equals(args[i]) || "--definegroupbysearch".equals(args[i])) {
					optionCLIDefineGroupBySearch = true;
					savedSearch = getArg(args, i);
					i++;
					group = getArg(args, i);
					i = i + 2;

				} else if ("--initUserRoles".equals(args[i])) {
					optionCLIuserConfigProducing = true;

					i++;
				} else if ("-me".equals(args[i]) || "--testPersistenceControllerMethod".equals(args[i])) {
					optionPersistenceControllerMethodCall = true;
					i = i + 1;
				} else if ("--version".equals(args[i])) {
					Logging.essential("configed version: " + Globals.VERSION + " (" + Globals.VERDATE + ") ");
					System.exit(0);
				} else if ("--help".equals(args[i])) {
					usage();
					System.exit(0);
				} else if ("--collect_queries_until_no".equals(args[i])) {
					String no = getArg(args, i);
					try {
						OpsiMethodCall.maxCollectSize = Integer.parseInt(no);
					} catch (NumberFormatException ex) {
						Logging.debug("  \n\nArgument >" + no + "< has no integer format");
						usage();
						endApp(ERROR_INVALID_OPTION);
					}
					i = i + 2;
				} else if ("--loglevel".equals(args[i])) {
					String s = "?";
					try {
						s = getArg(args, i);
						loglevelFile = loglevelConsole = Integer.valueOf(s);
					} catch (NumberFormatException ex) {
						Logging.debug(" \n\nArgument >" + s + "< has no integer format");
					}
					i = i + 2;
				} else if ("--localizationfile".equals(args[i])) {
					String extraLocalizationFileName = getArg(args, i);

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

					i = i + 2;

					if (!success) {
						endApp(ERROR_CANNOT_READ_EXTRA_LOCALIZATION);
					}
				} else if ("--localizationstrings".equals(args[i])) {
					showLocalizationStrings = true;
					i = i + 1;
				} else if ("--swaudit-pdf".equals(args[i])) {
					optionCLISwAuditPDF = true;
					clientsFile = getArg(args, i);
					i = i + 2;
					if (args.length > i && args[i].charAt(0) != '-') {
						outDir = args[i];
						i = i + 1;
					}
				} else if ("--swaudit-csv".equals(args[i])) {
					optionCLISwAuditCSV = true;
					clientsFile = getArg(args, i);
					i = i + 2;
					if (args.length > i && args[i].charAt(0) != '-') {
						outDir = args[i];
						i = i + 1;
					}
				} else if ("--halt".equals(args[i])) {
					i = i + 1;
				} else if ("--disable-certificate-verification".equals(args[i])) {
					Globals.disableCertificateVerification = true;
					i = i + 1;
				} else if ("--logviewer".equals(args[i])) {
					logviewer = true;
					i = i + 1;
				} else {
					Logging.debug("an option is not valid: " + args[i]);
					usage();
					endApp(ERROR_INVALID_OPTION);
				}
			}
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

	// from the JGoodies Library, we take the following function, observing

	/*
	 * Copyright (c) 2001-2008 JGoodies Karsten Lentzsch. All Rights Reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 *
	 * o Redistributions of source code must retain the above copyright notice,
	 * this list of conditions and the following disclaimer.
	 *
	 * o Redistributions in binary form must reproduce the above copyright notice,
	 * this list of conditions and the following disclaimer in the documentation
	 * and/or other materials provided with the distribution.
	 *
	 * o Neither the name of JGoodies Karsten Lentzsch nor the names of
	 * its contributors may be used to endorse or promote products derived
	 * from this software without specific prior written permission.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
	 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
	 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
	 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
	 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS,
	 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
	 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
	 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
	 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */

	public static void configureUI() {
		boolean trynimbus = true;
		boolean found = false;

		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					Logging.info("setting Nimbus look&feel");
					UIManager.setLookAndFeel(info.getClassName());
					Logging.info("Nimbus look&feel set, by " + info.getClassName());

					UIManager.put("Tree.selectionBackground", UIManager.get("controlHighlight"));

					UIManager.put("TreeUI", ClientTreeUI.class.getName());

					found = true;
					break;
				}
			}
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			// handle exception
			Logging.error("Failed to configure ui " + e);
		}

		if (!found) {
			trynimbus = false;
		}

		if (!trynimbus) {
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			} catch (Exception ex) {
				Logging.debug("UIManager.setLookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel')," + ex);
			}
		}

		// destroys some popups, saves others
	}

	public static void setOpsiLaf() {
		Logging.info("set look and feel " + Messages.getSelectedTheme());

		// Location of the theme property files - register them
		FlatLaf.registerCustomDefaultsSource("de.uib.configed.themes");

		switch (Messages.getSelectedTheme()) {
		case "Light":
			FlatLightLaf.setup();
			break;

		case "Dark":
			FlatDarkLaf.setup();
			break;

		default:
			Logging.warning("tried to set theme in setOpsiLaf that does not exist: " + Messages.getSelectedTheme());
			break;
		}
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

	public static AbstractPersistenceController connect() {
		Messages.setLocale("en");
		AbstractPersistenceController controller = PersistenceControllerFactory.getNewPersistenceController(host, user,
				password);
		if (controller == null) {
			Logging.error("Authentication error.");
			System.exit(1);
		}

		if (controller.getConnectionState().getState() != ConnectionState.CONNECTED) {
			Logging.error("Authentication error.");
			System.exit(1);
		}

		return controller;
	}

	/**
	 * main-Methode
	 */
	public static void main(String[] args) {

		processArgs(args);

		if (logviewer) {
			Logview.main(args);
		} else {
			startConfiged();
		}
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

		if (optionPersistenceControllerMethodCall) {
			Logging.debug("optionPersistenceControllerMethodCall");
			addMissingArgs();

			connect();

			System.exit(0);
		}

		try {
			String resourceS = Globals.ICON_RESOURCE_NAME;
			URL resource = Globals.class.getResource(resourceS);
			if (resource == null) {
				Logging.debug("image resource " + resourceS + "  not found");
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
