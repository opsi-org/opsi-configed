package de.uib.configed;

import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.uib.configed.gui.FTextArea;
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.utilities.PropertiesStore;
import de.uib.utilities.logging.UncaughtExceptionHandler;
import de.uib.utilities.logging.logging;
import de.uib.utilities.savedstates.SavedStates;

public class configed {
	public static boolean useHalt = false;

	public static de.uib.utilities.swing.FLoadingWaiter fProgress;

	private static final String localizationFilenameRegex = Messages.appname + "_...*\\.properties";

	public static boolean sshconnect_onstart = false;

	public static final String usageInfo = "\n" + "\tconfiged [OPTIONS] \n" + "\t\twhere an OPTION may be \n";
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
	 * logging.LEVEL_CRITICAL + ", <= " + logging.LEVEL_DEBUG + ") \n" ;
	 */

	public static final String[][] usageLines = new String[][] {
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
					"Try to create a SSH connection on start. DEFAULT: " + getYNforBoolean(sshconnect_onstart) + "" },
			new String[] { "--ssh-key SSHKEY", "",
					"Full path with filename from sshkey used for authentication on ssh server" },
			new String[] { "--ssh-passphrase PASSPHRASE", "",
					"Passphrase for given sshkey used for authentication on ssh server" },
			new String[] { "--version", "", "Tell configed version" },
			new String[] { "--collect_queries_until_no N", "",
					"Collect the first N queries; N = " + de.uib.opsicommand.OpsiMethodCall.maxCollectSize
							+ " (DEFAULT).  -1 meaning 'no collect'. 0 meaning 'infinite' " },
			new String[] { "--help", "", "Give this help" },
			new String[] { "--loglevel L", "",
					"Set logging level L, L is a number >= " + logging.LEVEL_NONE + ", <= " + logging.LEVEL_SECRET
							+ " . DEFAULT: " + de.uib.utilities.logging.logging.LOG_LEVEL_CONSOLE },
			new String[] { "--halt", "", "Use  first occurring debug halt point that may be in the code" },
			// new String[]{"--sqlgethashes", "", "Use sql statements with getHashes where
			// implemented in PersistenceController "},
			new String[] { "--sqlgetrows", "", "Force use sql statements by getRawData" },
			new String[] { "--nosqlrawdata", "", "Avoid getRawData" },
			// new String[]{"--synced", "", "Load table first and do only sync afterwards
			// "},
			// new String[]{"--dblocal", "", "Tries to make use of an embedded local
			// database "},
			// new String[]{"--dblocalnew", "", "Tries to make use of an embedded local
			// database after renewing it"},
			// new String[]{"--sqldirect", "", "Use direct sql access if possible in
			// PersistenceController "}
			// new String[]{"--sqldirect-cleanup-auditsoftware", "", "Use direct sql access
			// if possible in PersistenceController "},
			new String[] { "--localizationfile EXTRA_LOCALIZATION_FILENAME", "",
					"For translation work, use  EXTRA_LOCALIZATION_FILENAME as localization file, the file name format has to be: "
							+ localizationFilenameRegex.replace("...*\\", "LOCALENAME") },
			new String[] { "--localizationstrings", "",
					"For translation work, show internal labels together with the strings of selected localization" },
			new String[] { "--swaudit-pdf FILE_WITH_CLIENT_IDS_EACH_IN_A_LINE [OUTPUT_PATH]", "",
					"export pdf swaudit reports for given clients (if no OUTPUT_PATH given, use home directory)" },
			new String[] { "--swaudit-csv FILE_WITH_CLIENT_IDS_EACH_IN_A_LINE [OUTPUT_PATH]", "",
					"export csv swaudit reports for given clients (if no OUTPUT_PATH given, use home directory)" }

			// undocumented
			// new String[]{"--me", "--testPersistenceControllerMethod", ""}

	};

	public static final Charset serverCharset = Charset.forName("UTF-8");
	public static final String javaVersion = System.getProperty("java.version");
	public static final String javaVendor = System.getProperty("java.vendor", "");
	public static final LinkedHashMap<String, Object> javaSysExtraProperties = new LinkedHashMap<>();
	public static final String systemSSLversion = System.getProperty("https.protocols");
	public static String EXTRA_LOCALIZATION_FILENAME = null;
	public static PropertiesStore extraLocalization;
	public static boolean SHOW_LOCALIZATION_STRINGS = false;
	protected static boolean serverCharset_equals_vm_charset = false;

	public static ConfigedMain cm;

	private static String locale = null;
	private static String host = null;
	public static String user = null;
	private static String password = null;
	private static int loglevelConsole = logging.LOG_LEVEL_CONSOLE;
	private static int loglevelFile = logging.LOG_LEVEL_FILE;

	public static String sshkey = null;
	public static String sshkeypassphrase = null;
	private static String client = null;
	private static String clientgroup = null;
	private static Integer tab = null;
	private static String logdirectory = "";
	private static boolean optionCLIQuerySearch = false;
	private static String savedSearch = null;
	private static boolean optionCLIDefineGroupBySearch = false;
	private static String group = null;
	private static boolean optionCLISwAuditPDF = false;
	private static boolean optionCLISwAuditCSV = false;
	private static String clientsFile = null;
	private static String outDir = null;

	private static boolean optionPersistenceControllerMethodCall = false;

	private static boolean optionCLIuserConfigProducing = false;

	public static SavedStates savedStates;
	public static String savedStatesLocationName;
	public static final String savedStatesFilename = "configedStates.prop";

	public static Integer refreshMinutes = 0;

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
	public static final int ERROR_UNSUPPORTED_CIPHERSUITE = 12;

	public static final int ERROR_CANNOT_READ_EXTRA_LOCALIZATION = 11;

	public static final int ERROR_OUT_OF_MEMORY = 21;

	private static FTextArea fErrorOutOfMemory;

	// --------------------------------------------------------------------------------------------------------

	private static String getYNforBoolean(Boolean b) {
		if (b)
			return "y";
		else
			return "n";
	}

	private static String tabs(int count) {
		StringBuilder buf = new StringBuilder("");
		for (int j = 0; j < count; j++) {
			buf.append("\t");
		}
		return buf.toString();
	}

	protected static void usage() {
		System.out.println("configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);
		System.out.println(usageInfo);

		final int tabWidth = 8;
		int length0 = 0;
		int length1 = 0;

		for (int i = 0; i < usageLines.length; i++) {
			// we find max of fillTabs0, fillTabs1
			int len = usageLines[i][0].length();

			if (len > length0)
				length0 = len;

			len = usageLines[i][1].length();

			if (len > length1)
				length1 = len;
		}

		int allTabs0 = 0;
		if (length0 % tabWidth == 0) {
			allTabs0 = length0 / tabWidth + 1;
		} else {
			allTabs0 = (length0 / tabWidth) + 1;
		}

		int allTabs1 = 0;
		if (length1 % tabWidth == 0) {
			allTabs1 = length1 / tabWidth + 1;
		} else {
			allTabs1 = (length1 / tabWidth) + 1;
		}

		for (int i = 0; i < usageLines.length; i++) {
			// System.out.println("usageLines " + i + ", " + 0 + usageLines[i][0]);

			int startedTabs0 = (usageLines[i][0].length() / tabWidth);
			int startedTabs1 = (usageLines[i][1].length() / tabWidth);

			System.out.println("\t" + usageLines[i][0] + tabs(allTabs0 - startedTabs0) + usageLines[i][1]
					+ tabs(allTabs1 - startedTabs1) + usageLines[i][2]);
		}
	}

	protected static boolean isValue(String[] args, int i) {
		
		// i + " has - " + (args[i].indexOf('-') == 0) );
		return i >= args.length || args[i].indexOf('-') == 0;
	}

	protected static String getArg(String[] args, int i) {
		if (args.length <= i + 1 || args[i + 1].indexOf('-') == 0) {
			logging.error("Missing value for option " + args[i]);
			usage();
			endApp(ERROR_MISSING_VALUE_FOR_OPTION);
		}
		i++;
		return args[i];
	}

	public static void startWithLocale() {
		logging.info("system information: ");

		logging.info(" configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);
		logging.info(" running by java version " + javaVersion);

		Properties sysProperties = System.getProperties();
		Set<String> propNames = sysProperties.stringPropertyNames();
		Set<String> priorizedNames = new HashSet<>();

		LinkedHashSet<String> javaNames = new LinkedHashSet<>();

		String s = "java.version";
		if (propNames.contains(s)) {
			
			priorizedNames.add(s);
		}

		s = "java.vendor";
		if (propNames.contains(s)) {
			javaNames.add(s);
			priorizedNames.add(s);
		}

		TreeSet<String> sortedJavaNames = new TreeSet<>();

		for (String name : propNames) {
			if (name.indexOf("java") >= 0 && !priorizedNames.contains(name) && sysProperties.get(name) != null) {
				sortedJavaNames.add(name);
			}
		}

		for (String name : sortedJavaNames) {
			javaNames.add(name);
		}

		for (String name : javaNames) {
			Object value = sysProperties.get(name);
			String key = name;
			if (key.length() > 5 && key.substring(0, 5).equals("java."))
				key = key.substring(5); // omitting "java."

			logging.info(key + ":: " + value);

			javaSysExtraProperties.put(key, value);
		}

		// Try with resources so that it will be closed in implicit finally statement
		try (Formatter formatter = new Formatter()) {
			logging.info(
					" we get max memory " + formatter.format("%,d MB", Runtime.getRuntime().maxMemory() / 1000000));
		}

		de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText.reset();
		LicensingInfoMap.requestRefresh();

		cm = new ConfigedMain(paramHost, paramUser, paramPassword);

		SwingUtilities.invokeLater(() -> cm.init());

		try {

			SwingUtilities.invokeAndWait(() -> {
				if (paramClient != null || paramClientgroup != null) {
					if (paramClientgroup != null) {
						cm.setGroup(paramClientgroup);
					}

					if (paramClient != null) {
						cm.setClient(paramClient);
					}

					logging.info("set client " + paramClient);

					if (paramTab != null) {
						cm.setVisualViewIndex(paramTab);
					}
				}
			});

		} catch (InvocationTargetException ex) {
			logging.info(" run " + ex);
		} catch (InterruptedException ie) {
			logging.info(" run " + ie);
			Thread.currentThread().interrupt();
		}
	}

	/** construct the application */
	public configed(String paramLocale, String paramHost, String paramUser, String paramPassword,
			final String paramClient, final String paramClientgroup, final Integer paramTab, String paramLogdirectory) {

		UncaughtExceptionHandler errorHandler = new UncaughtExceptionHandlerLocalized();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);

		logging.debug("starting " + getClass().getName());
		logging.debug("default charset is " + Charset.defaultCharset().displayName());
		logging.debug("server charset is configured as " + serverCharset);

		if (serverCharset.equals(Charset.defaultCharset())) {
			serverCharset_equals_vm_charset = true;
			logging.debug("they are equal");
		}

		configureUI();

		
		try {
			String resourceS = "opsi.gif";
			URL resource = Globals.class.getResource(resourceS);
			if (resource == null) {
				logging.debug("image resource " + resourceS + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			}
		} catch (Exception ex) {
			logging.debug("imageHandled failed: " + ex.toString());
		}

		// Set directory for logging
		logging.logDirectoryName = logdirectory;

		// Set locale
		List<String> existingLocales = Messages.getLocaleNames();
		Messages.setLocale(paramLocale);
		logging.info("getLocales: " + existingLocales);
		logging.info("selected locale characteristic " + Messages.getSelectedLocale());

		configed.paramHost = paramHost;
		configed.paramUser = paramUser;
		configed.paramPassword = paramPassword;
		configed.paramTab = paramTab;
		configed.paramClient = paramClient;
		configed.paramClientgroup = paramClientgroup;

		startWithLocale();
	}

	protected void revalidate() {
		cm.initialTreeActivation();
	}

	protected static void processArgs(String[] args) {
		
		/*for (int i = 0; i < args.length; i++)
			logging.debug();*/
		/*
		 * logging.debug("args:");
		 * for (int i = 0; i < args.length; i++)
		 * {
		 * logging.debug(args[i]);
		 * }
		 */
		
		logging.debug("args " + Arrays.toString(args));

		
		de.uib.opsicommand.JSONthroughHTTP.compressTransmission = true;

		if (args.length == 2 && args[0].equals("--args")) {
			args = args[1].split(";;");
		}

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("--help")) {
				usage();
				endApp(NO_ERROR);
			}
		}

		int firstPossibleNonOptionIndex = args.length - 1;

		int i = 0;
		while (i < args.length) {
			

			if (args[i].charAt(0) != '-') // no option
			{
				if (i < firstPossibleNonOptionIndex) {
					usage();
					endApp(NO_ERROR);
				} else {
					
					
					// only one of them can be used
				}
				i++;
			} else // options

			{
				
				if (args[i].equals("-l") || args[i].equals("--locale")) {
					locale = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("-h") || args[i].equals("--host")) {
					host = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("-u") || args[i].equals("--user")) {
					user = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("-p") || args[i].equals("--password")) {
					password = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("-c") || args[i].equals("--client")) {
					client = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("-g") || args[i].equals("--group")) {
					clientgroup = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("-t") || args[i].equals("--tab")) {
					String tabS = getArg(args, i);
					try {
						tab = Integer.parseInt(tabS);
					} catch (NumberFormatException ex) {
						logging.debug("  \n\nArgument >" + tabS + "< has no integer format");
						usage();
						endApp(ERROR_INVALID_OPTION);
					}
					i = i + 2;
				} else if (args[i].equals("-d") || args[i].equals("--logdirectory")) {
					logdirectory = getArg(args, i);

					i = i + 2;
				} else if (args[i].equals("-s") || args[i].equals("--savedstates")) {
					savedStatesLocationName = getArg(args, i);
					String canonicalPath = null;
					try {
						canonicalPath = new File(savedStatesLocationName).getCanonicalPath();
					} catch (IOException ex) {
						logging.debug("savedstates argument " + ex);
					}
					if (canonicalPath != null)
						savedStatesLocationName = canonicalPath;

					i = i + 2;
				} else if (args[i].equals("-r") || args[i].equals("--refreshminutes")) {
					String test = getArg(args, i);

					try {
						refreshMinutes = Integer.valueOf(test);
					} catch (NumberFormatException ex) {
						logging.debug("  \n\nArgument >" + test + "< has no integer format");
						usage();
						endApp(ERROR_INVALID_OPTION);
					}

					i = i + 2;
				} else if (args[i].equals("--ssh-immediate-connect")) {
					// de.uib.opsicommand.sshcommand.SSHConnectionInfo
					

					

					i = i + 1;

					if (isValue(args, i)) {
						if (args[i].equalsIgnoreCase("Y")) {
							sshconnect_onstart = true;
							
						} else if (args[i].equalsIgnoreCase("N")) {
							sshconnect_onstart = false;
							
						} else {
							usage();
							endApp(ERROR_INVALID_OPTION);
						}
					}
					i = i + 1;
				} else if (args[i].equals("--ssh-key")) {
					sshkey = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("--ssh-passphrase")) {
					sshkeypassphrase = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("--gzip")) {
					
					de.uib.opsicommand.JSONthroughHTTP.compressTransmission = true;
					i = i + 1;
					

					if (isValue(args, i)) {
						
						if (args[i].equalsIgnoreCase("Y")) {
							
							de.uib.opsicommand.JSONthroughHTTP.compressTransmission = true;
						} else if (args[i].equalsIgnoreCase("N")) {
							de.uib.opsicommand.JSONthroughHTTP.compressTransmission = false;
							
						} else {
							usage();
							endApp(ERROR_INVALID_OPTION);
						}
					}
					i = i + 1;
				} else if (args[i].equals("-qs") || args[i].equals("--querysavedsearch")) {
					optionCLIQuerySearch = true;
					savedSearch = getArg(args, i);
					i = i + 2;

				} else if (args[i].equals("-qg") || args[i].equals("--definegroupbysearch")) {
					optionCLIDefineGroupBySearch = true;
					savedSearch = getArg(args, i);
					i++;
					group = getArg(args, i);
					i = i + 2;
					
				} else if (args[i].equals("--initUserRoles")) {
					optionCLIuserConfigProducing = true;
					
					i++;
				} else if (args[i].equals("-me") || args[i].equals("--testPersistenceControllerMethod")) {
					optionPersistenceControllerMethodCall = true;
					i = i + 1;
				} else if (args[i].equals("--sqlgethashes")) {
					de.uib.opsidatamodel.PersistenceControllerFactory.sqlAndGetHashes = true;
					i = i + 1;
				} else if (args[i].equals("--sqlgetrows")) {
					de.uib.opsidatamodel.PersistenceControllerFactory.sqlAndGetRows = true;
					i = i + 1;
				} else if (args[i].equals("--nosqlrawdata")) {
					de.uib.opsidatamodel.PersistenceControllerFactory.avoidSqlRawData = true;
					i = i + 1;
				}

				/*
				 * else if (args[i].equals("--dblocal"))
				 * {
				 * de.uib.opsidatamodel.PersistenceControllerFactory.localDB= true;
				 * i=i+1;
				 * }
				 * 
				 * else if (args[i].equals("--dblocalnew"))
				 * {
				 * de.uib.opsidatamodel.PersistenceControllerFactory.localDBResync= true;
				 * i=i+1;
				 * }
				 * 
				 * else if (args[i].equals("--synced"))
				 * {
				 * de.uib.opsidatamodel.PersistenceControllerFactory.synced= true;
				 * i=i+1;
				 * }
				 * 
				 * else if (args[i].equals("--sqldirect"))
				 * {
				 * de.uib.opsidatamodel.PersistenceControllerFactory.sqlDirect = true;
				 * i=i+1;
				 * }
				 */

				else if (args[i].equals("--sqldirect-cleanup-auditsoftware")) {
					de.uib.opsidatamodel.PersistenceControllerFactory.sqlDirect = true;
					de.uib.opsidatamodel.PersistenceControllerFactory.directmethodcall = de.uib.opsidatamodel.PersistenceControllerFactory.directmethodcall_cleanupAuditsoftware;
					i = i + 1;
				} else if (args[i].equals("--version")) {
					System.out.println("configed version: " + Globals.VERSION + " (" + Globals.VERDATE + ") ");
					System.exit(0);
				} else if (args[i].equals("--help")) {
					usage();
					System.exit(0);
				} else if (args[i].equals("--collect_queries_until_no")) {
					String no = getArg(args, i);
					try {
						de.uib.opsicommand.OpsiMethodCall.maxCollectSize = Integer.parseInt(no);
					} catch (NumberFormatException ex) {
						logging.debug("  \n\nArgument >" + no + "< has no integer format");
						usage();
						endApp(ERROR_INVALID_OPTION);
					}
					i = i + 2;
				} else if (args[i].equals("--loglevel")) {
					String s = "?";
					try {
						s = getArg(args, i);
						loglevelFile = loglevelConsole = Integer.valueOf(s);
					} catch (NumberFormatException ex) {
						logging.debug(" \n\nArgument >" + s + "< has no integer format");
					}
					i = i + 2;
				} else if (args[i].equals("--localizationfile")) {
					EXTRA_LOCALIZATION_FILENAME = getArg(args, i);

					boolean success = false;

					String[] parts = null;

					try {
						File extraLocalizationFile = new File(EXTRA_LOCALIZATION_FILENAME);
						if (!extraLocalizationFile.exists()) {
							logging.debug("File not found: " + EXTRA_LOCALIZATION_FILENAME);
						} else if (!extraLocalizationFile.canRead()) {
							logging.debug("File not readable " + EXTRA_LOCALIZATION_FILENAME);
						} else

						{
							logging.debug(" ok " + localizationFilenameRegex + "? "
									+ EXTRA_LOCALIZATION_FILENAME.matches("configed_...*\\.properties") + " --  "
									+ EXTRA_LOCALIZATION_FILENAME.matches(localizationFilenameRegex));

							parts = EXTRA_LOCALIZATION_FILENAME.split("_");

							logging.debug(" . " + parts[1] + " .. " + Arrays.toString(parts[1].split("\\.")));

							if (!EXTRA_LOCALIZATION_FILENAME.matches(localizationFilenameRegex)) {
								logging.debug("localization file does not have the expected format " + Messages.appname
										+ "_LOCALE.properties");
							} else {
								extraLocalization = new PropertiesStore(extraLocalizationFile);
								extraLocalization.load();

								success = true;
							}
						}
					} catch (Exception ex) {
						logging.error(EXTRA_LOCALIZATION_FILENAME + " problem", ex);
					}

					i = i + 2;

					if (!success) {
						endApp(ERROR_CANNOT_READ_EXTRA_LOCALIZATION);
					}
				} else if (args[i].equals("--localizationstrings")) {
					SHOW_LOCALIZATION_STRINGS = true;
					i = i + 1;
				} else if (args[i].equals("--swaudit-pdf")) {
					optionCLISwAuditPDF = true;
					clientsFile = getArg(args, i);
					i = i + 2;
					if (args.length > i && args[i].charAt(0) != '-') {
						outDir = args[i];
						i = i + 1;
					}
				} else if (args[i].equals("--swaudit-csv")) {
					optionCLISwAuditCSV = true;
					clientsFile = getArg(args, i);
					i = i + 2;
					if (args.length > i && args[i].charAt(0) != '-') {
						outDir = args[i];
						i = i + 1;
					}
				} else if (args[i].equals("--halt")) {
					useHalt = true;
					i = i + 1;
				} else {
					logging.debug("an option is not valid: " + args[i]);
					usage();
					endApp(ERROR_INVALID_OPTION);
				}
			}
		}
		logging.debug("configed: args recognized");

		logging.setLogLevelConsole(loglevelConsole);
		logging.setLogLevelFile(loglevelFile);
		logging.setLogfileMarker(host);
		logging.init();
		logging.essential("Configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") starting");
		if (optionCLIQuerySearch || optionCLIDefineGroupBySearch)
			logging.setSuppressConsole(true);
	}

	public static String encodeStringFromService(String s) {
		

		return s;

	}

	public static String encodeStringForService(String s) {
		return s;
	}

	public static boolean get_serverCharset_equals_vm_charset() {
		return serverCharset_equals_vm_charset;
	}

	public static void showExternalInfo(String s) {
		try {
			File messagefile = File.createTempFile("configed", "html");

			// try-with-resources so that writers will be closed and there's no leak
			try (FileWriter fw = new FileWriter(messagefile);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw)) {

				out.println("<HTML>");
				out.println("<title>opsi-configed message</title>");
				out.println("<body>");
				out.println("<h1 center>opsi-configed</h1>");
				out.println("<p center>opsi-configed closed</p>");
				out.println("<p center>reason:</p>");
				out.println("<p center>" + s + "</p>");
				out.println("</body>");
				out.println("</HTML>");

				// {
				
				// String osName = System.getProperty("os.name");
				// if (osName.toLowerCase().startsWith("win")) {
				// Process proc = rt.exec("cmd.exe /c start \"" + messagefile.getPath() + "\"");
				// } else
				// //Linux, we assume that there is a firefox and it will handle the url
				// {
				// String[] cmdarray = new String[] { "firefox", messagefile.getPath() };
				

				// }
				// }
			}
		} catch (IOException ex) {
			logging.debug("configed showExternalInfo " + s);
		}
	}

	public static void endApp(int exitcode) {
		if (savedStates != null) {
			try {
				savedStates.store("states on finishing configed");
			} catch (IOException iox) {
				logging.debug("could not store saved states, " + iox);
			}
		}

		de.uib.opsicommand.OpsiMethodCall.report();
		logging.info("regularly exiting app with code " + exitcode);

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
					logging.info("extraLocalization.getProperty null for key " + key);
				}
			}

			if (result == null) {
				result = Messages.messages.getString(key);
			}

			if (SHOW_LOCALIZATION_STRINGS) {
				logging.info("LOCALIZE " + key + " by " + result);
				result = "" + result + "[[" + key + "]]";
				// result = " for " + Messages.getSelectedLocale() + " " + result + "[[" + key +
				// "]]";
			}
		} catch (MissingResourceException mre) {
			// we return the key and log the problem:
			logging.debug("Problem: " + mre.toString());
			

			try {
				result = Messages.messagesEN.getString(key);

				if (SHOW_LOCALIZATION_STRINGS) {
					logging.info("LOCALIZE " + key + " by " + result);
					result = "" + result + "?? [[" + key + "]]";
					// result = " for " + Messages.getSelectedLocale() + "
				}
			} catch (MissingResourceException mre2) {
				logging.debug("Problem: " + mre2.toString());
				
			}
		} catch (Exception ex) {
			logging.warning("Failed to message " + key + ": " + ex);
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
	 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
	 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
	 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
	 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
	 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */

	/**
	 * private void configureUI() {
	 * UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
	 * Options.setDefaultIconSize(new Dimension(18, 18)); String lafName =
	 * LookUtils.IS_OS_WINDOWS_XP ?
	 * Options.getCrossPlatformLookAndFeelClassName() :
	 * Options.getSystemLookAndFeelClassName(); try {
	 * UIManager.setLookAndFeel(lafName); } catch (Exception e) {
	 * logging.error("Can't set look & feel:" + e); } }
	 */

	public static void configureUI() {
		boolean trynimbus = true;
		boolean found = false;

		// if (trynimbus) {
		try {

			/*
			 * UIManager.setLookAndFeel(
			 * UIManager.getSystemLookAndFeelClassName());
			 */

			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					logging.info("setting Nimbus look&feel");
					UIManager.setLookAndFeel(info.getClassName());
					logging.info("Nimbus look&feel set, by " + info.getClassName());

					

					// UIManager.put("nimbusSelectionBackground",
					// UIManager.get("nimbusLightBackground"));

					UIManager.put("Tree.selectionBackground", UIManager.get("controlHighlight"));
					// was chosen: UIManager.put("nimbusSelectionBackground",
					// UIManager.get("controlHighlight"));
					// UIManager.put("Tree[Enabled+Selected].collapsedIconPainter", new
					
					// UIManager.put("Tree.rendererMargins", new Insets(0,0,0,0));

					// UIManager.put("Tree.drawHorizontalLines", true);
					// UIManager.put("Tree.drawVerticalLines", true);

					UIManager.put("TreeUI", de.uib.configed.tree.ClientTreeUI.class.getName());

					found = true;
					break;
				}
			}
		} catch (javax.swing.UnsupportedLookAndFeelException e) {
			// handle exception
			logging.error("Failed to configure ui " + e);
		} catch (ClassNotFoundException e) {
			// handle exception
			logging.error("Failed to configure ui " + e);
		} catch (InstantiationException e) {
			// handle exception
			logging.error("Failed to configure ui " + e);
		} catch (IllegalAccessException e) {
			// handle exception
			logging.error("Failed to configure ui " + e);
		}
		// }

		if (!found) {
			trynimbus = false;
		}

		if (!trynimbus) {
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			} catch (Exception ex) {
				logging.debug("UIManager.setLookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel')," + ex);
			}
		}

		// UIManager.put("SplitPane.dividerFocusColor", Globals.backBlue);
		// UIManager.put("SplitPane.darkShadow", Globals.backBlue);

		/*
		 * UIManager.put("ProgressBar.background", Globals.backLightBlue);
		 * UIManager.put("ProgressBar.foreground", Globals.backLightBlue);
		 * UIManager.put("ProgressBar.selectionBackground", Color.red);
		 * UIManager.put("ProgressBar.selectionForeground", Globals.backLightBlue);
		 */

		
		// destroys some popups, saves others
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

	public static de.uib.opsidatamodel.PersistenceController connect() {
		Messages.setLocale("en");
		de.uib.opsidatamodel.PersistenceController controller = de.uib.opsidatamodel.PersistenceControllerFactory
				.getNewPersistenceController(host, user, password);
		if (controller == null) {
			logging.error("Authentication error.");
			System.exit(1);
		}

		if (controller.getConnectionState().getState() != ConnectionState.CONNECTED) {
			logging.error("Authentication error.");
			System.exit(1);
		}

		return controller;
	}

	/**
	 * main-Methode
	 */
	public static void main(String[] args) {

		
		processArgs(args);

		logging.debug("initiating configed");

		if (optionCLIQuerySearch) {
			
			logging.debug("optionCLIQuerySearch");
			de.uib.configed.clientselection.SavedSearchQuery query = new de.uib.configed.clientselection.SavedSearchQuery();
			

			query.setArgs(host, user, password, savedSearch, null);
			query.addMissingArgs();

			
			query.runSearch(true);
			System.exit(0);
		} else if (optionCLIDefineGroupBySearch) {
			logging.debug("optionCLIDefineGroupBySearch");
			// group_getObjects // exists group?
			// parentGroupId
			// removeHostGroupElements
			// addGroup
			// addObject2Group

			de.uib.configed.clientselection.SavedSearchQuery query = new de.uib.configed.clientselection.SavedSearchQuery();
			
			// + ", " + group);
			query.setArgs(host, user, password, savedSearch, group);
			query.addMissingArgs();
			List<String> newGroupMembers = query.runSearch(false);
			

			query.populateHostGroup(newGroupMembers, group);
			System.exit(0);
		} else if (optionCLISwAuditPDF) {
			logging.debug("optionCLISwAuditPDF");
			de.uib.configed.gui.swinfopage.SwPdfExporter exporter = new de.uib.configed.gui.swinfopage.SwPdfExporter();
			exporter.setArgs(host, user, password, clientsFile, outDir);
			exporter.addMissingArgs();
			exporter.run();

			System.exit(0);
		} else if (optionCLISwAuditCSV) {
			logging.debug("optionCLISwAuditCSV");
			de.uib.configed.gui.swinfopage.SWcsvExporter exporter = new de.uib.configed.gui.swinfopage.SWcsvExporter();
			exporter.setArgs(host, user, password, clientsFile, outDir);
			exporter.addMissingArgs();
			exporter.run();

			System.exit(0);
		} else if (optionCLIuserConfigProducing) {
			logging.debug("UserConfigProducing");
			logging.setLogLevelConsole(loglevelConsole);
			logging.setLogLevelFile(loglevelFile);

			addMissingArgs();

			PersistenceController persist = PersistenceControllerFactory.getNewPersistenceController(host, user,
					password);

			de.uib.opsidatamodel.permission.UserConfigProducing up = new de.uib.opsidatamodel.permission.UserConfigProducing(
					false, // boolean notUsingDefaultUser,

					host, // String configserver,
					persist.getHostInfoCollections().getDepotNamesList(), // Collection<String> existingDepots,
					persist.getHostGroupIds(), // Collection<String> existingHostgroups,
					persist.getProductGroups().keySet(), // Collection<String> existingProductgroups,

					// data. on which changes are based
					persist.getConfigDefaultValues(), // Map<String, List<Object>> serverconfigValuesMap,
					persist.getConfigOptions()// Map<String, de.uib.utilities.table.ListCellOptions> configOptionsMap
			);

			ArrayList<Object> newData = up.produce();
			logging.debug("UserConfigProducing: newData " + newData);

			System.exit(0);
		}

		if (optionPersistenceControllerMethodCall) {
			logging.debug("optionPersistenceControllerMethodCall");
			addMissingArgs();

			connect();

			
			System.exit(0);

			
			
		}

		if (de.uib.opsidatamodel.PersistenceControllerFactory.sqlDirect) {
			logging.debug("de.uib.opsidatamodel.PersistenceControllerFactory.sqlDirect");
			logging.logDirectoryName = logdirectory;

			addMissingArgs();

			System.exit(0);
		}

		
		try {
			String resourceS = Globals.iconresourcename;
			URL resource = Globals.class.getResource(resourceS);
			if (resource == null) {
				logging.debug("image resource " + resourceS + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			}
		} catch (Exception ex) {
			logging.debug("imageHandled failed: " + ex.toString());
		}

		// Turn on antialiasing for text (not for applets)
		try {
			System.setProperty("swing.aatext", "true");
		} catch (Exception ex) {
			logging.info(" setting property swing.aatext" + ex);
		}

		fErrorOutOfMemory = new FTextArea(null, "configed", true, new String[] { "ok" }, 400, 400);

		fErrorOutOfMemory.setContentBackground(Globals.darkOrange);
		// we activate it in case of an appropriate error

		fErrorOutOfMemory.setFont(Globals.defaultFontBig);
		fErrorOutOfMemory
				.setMessage("The program will be terminated,\nsince more memory is required than was assigned.");

		

		new configed(locale, host, user, password, client, clientgroup, tab, logdirectory);
	}
}
