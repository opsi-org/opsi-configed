/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.formdev.flatlaf.util.SystemInfo;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.logviewer.Logviewer;
import de.uib.messages.Messages;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.utils.FeatureActivationChecker;
import de.uib.utils.logging.Logging;
import de.uib.utils.logging.UncaughtConfigedExceptionHandler;
import de.uib.utils.userprefs.ThemeManager;
import de.uib.utils.userprefs.UserPreferences;

public class Main {
	// --------------------------------------------------------------------------------------------------------
	// exit codes

	public static final int NO_ERROR = 0;
	public static final int ERROR_INVALID_OPTION = 1;
	public static final int ERROR_CANNOT_READ_EXTRA_LOCALIZATION = 11;
	public static final int ERROR_OUT_OF_MEMORY = 21;

	public static final String USAGE_INFO = "configed [OPTIONS] " + ", where an OPTION may be\n";

	private static FTextArea fErrorOutOfMemory;

	private static boolean isLogviewer;

	private static Options options;

	private static void createOptions() {
		options = new Options();

		// General options
		options.addOption("lv", "logviewer", false, "Use this option to start logviewer instead of configed");
		options.addOption("l", "locale", true, "Set locale LOC (format: <language>_<country>). DEFAULT: System.locale");
		options.addOption("d", "directory", true, """
				Directory for log files. DEFAULT: an opsi log directory
				dependent on system and user privileges, see /help/logfile""");
		options.addOption(null, "loglevel", true, "Set logging level N, N is a number >= " + Logging.LEVEL_NONE
				+ ", <= " + Logging.LEVEL_SECRET + " . DEFAULT: " + Logging.getLogLevelConsole());
		options.addOption(null, "help", false, "Give this help");
		options.addOption("v", "version", false, "Tell configed version");

		// Configed specific options
		options.addOption("h", "host", true, "Configuration server HOST to connect to. DEFAULT: choose interactive");
		options.addOption("u", "user", true, "User for authentication. DEFAULT: give interactive");
		options.addOption("p", "password", true, "Password for authentication. DEFAULT: give interactive");
		options.addOption("otp", "one-time-password", true, """
				One time password for authentication. DEFAULT: give interactive
				OTP is a paid feature. Should be used when license is available and OTP is enabled for a user""");
		options.addOption("s", "savedstates", true,
				"Directory for the files which keep states specific for a server connection. DEFAULT: Similar to log directory");
		options.addOption("qs", "querysavedsearch", true,
				"On command line: tell saved host searches list resp. the search result for [SAVEDSEARCH_NAME])");
		options.addOption("qg", "definegroupbysearch", true,
				"On command line: populate existing group GROUP_NAME with clients resulting from search SAVEDSEARCH_NAME");
		options.getOption("qg").setArgs(2);
		options.addOption(null, "initUserRoles", false,
				"On command line, perform  the complete initialization of user roles if something was changed");
		options.addOption(null, "collect_queries_until_no", true, "Collect the first N queries; N = "
				+ OpsiMethodCall.getMaxCollecSize() + " (DEFAULT).  -1 meaning 'no collect'. 0 meaning 'infinite' ");
		options.addOption(null, "localizationfile", true,
				"For translation work, use  EXTRA_LOCALIZATION_FILENAME as localization file, the file name format has to be: ");
		options.addOption(null, "localizationstrings", false,
				"For translation work, show internal labels together with the strings of selected localization");
		options.addOption(null, "swaudit-pdf", true, "export pdf swaudit reports for given clients");
		options.getOption("swaudit-pdf").setArgs(2);
		options.addOption(null, "swaudit-csv", true, "export csv swaudit reports for given clients");
		options.getOption("swaudit-csv").setArgs(2);
		options.addOption(null, "disable-certificate-verification", false,
				"Disable opsi-certificate verification with server, by DEFAULT enabled");
		options.addOption("ff", "feature-flags", true,
				"A list of features to activate on start. Available features are: "
						+ (FeatureActivationChecker.hasAvailableFeatures()
								? FeatureActivationChecker.getAvailableFeaturesAsString()
								: "NONE"));
		options.getOption("ff").setArgs(Option.UNLIMITED_VALUES);

		// Logviewer specific options
		options.addOption("f", "filename", true, "filename for the log file");
	}

	public static JFrame getMainFrame() {
		if (isLogviewer) {
			return Logviewer.getLogFrame();
		} else {
			return ConfigedMain.getMainFrame();
		}
	}

	public static void showHelp() {
		Logging.essential("configed version ", Globals.VERSION, " (", Globals.VERDATE, ")");

		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(Integer.MAX_VALUE);
		formatter.printHelp(Main.USAGE_INFO, options);
	}

	private static void setGlobalValues() {
		if (UserPreferences.get(UserPreferences.LANGUAGE) != null) {
			Messages.setLocale(UserPreferences.get(UserPreferences.LANGUAGE));
		}

		if (UserPreferences.get(UserPreferences.THEME) != null) {
			ThemeManager.setTheme(UserPreferences.get(UserPreferences.THEME));
			ThemeManager.setOpsiLaf();
		}
	}

	private static void parseArgs(CommandLine cmd) {
		if (cmd.hasOption("d")) {
			Logging.setLogDirectoryName(cmd.getOptionValue("d"));
		}

		if (cmd.hasOption("loglevel")) {
			String loglevelString = "";
			try {
				loglevelString = cmd.getOptionValue("loglevel");
				Integer loglevel = Integer.valueOf(loglevelString);

				Logging.setLogLevel(loglevel);
			} catch (NumberFormatException ex) {
				Logging.debug(" \n\nArgument >", loglevelString, "< has no integer format");
			}
		}

		if (cmd.hasOption("version")) {
			Logging.essential("configed version ", Globals.VERSION, " (", Globals.VERDATE, ")");
			endApp(0);
		}

		if (cmd.hasOption("help")) {
			Main.showHelp();
			endApp(0);
		}

		if (cmd.hasOption("lv")) {
			isLogviewer = true;
		}

		if (cmd.hasOption("l")) {
			String locale = cmd.getOptionValue("l");
			Messages.setLocale(locale);
		}

		if (cmd.hasOption("ff")) {
			String[] activatedFeatures = cmd.getOptionValues("ff");
			FeatureActivationChecker.setActivatedFeatures(activatedFeatures);
		}

		// After setting locale then we can use localization values
		Set<String> existingLocales = Messages.getLocaleNames();
		Logging.info("Available locales: ", existingLocales);
	}

	public static void endApp(int exitcode) {
		if (Configed.getSavedStates() != null) {
			try {
				Configed.getSavedStates().store("states on finishing configed");
			} catch (IOException iox) {
				Logging.debug("could not store saved states, ", iox);
			}
		}

		OpsiMethodCall.report();
		Logging.info("regularly exiting app with code ", exitcode);

		if (exitcode == ERROR_OUT_OF_MEMORY) {
			fErrorOutOfMemory.setVisible(true);
		}

		System.exit(exitcode);
	}

	public static boolean isLogviewer() {
		return isLogviewer;
	}

	private static void setSystemSpecificProperties() {
		if (SystemInfo.isLinux) {
			// enable custom window decorations
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		} else if (SystemInfo.isMacOS) {
			System.setProperty("flatlaf.useNativeLibrary", "false");
		} else {
			// Do nothing for other operating systems
		}
	}

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtConfigedExceptionHandler());

		setGlobalValues();

		createOptions();
		CommandLine cmd;
		try {
			CommandLineParser parser = new DefaultParser(false);
			cmd = parser.parse(options, args, false);
		} catch (ParseException e) {
			Logging.error(e, "Problem parsing arguments");
			showHelp();
			return;
		}

		parseArgs(cmd);

		Locale.setDefault(Messages.getLocale());

		ThemeManager.setOpsiLaf();

		setSystemSpecificProperties();

		if (isLogviewer) {
			Logviewer.main(cmd);
		} else {
			Configed.main(cmd);
		}

		fErrorOutOfMemory = new FTextArea(null, "configed", true,
				new String[] { Configed.getResourceValue("buttonClose") }, 400, 400);

		// we activate it in case of an appropriate error
		fErrorOutOfMemory
				.setMessage("The program will be terminated,\nsince more memory is required than was assigned.");
	}
}
