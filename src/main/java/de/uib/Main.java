/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.logviewer.Logviewer;
import de.uib.messages.Messages;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.savedstates.UserPreferences;

public class Main {

	// --------------------------------------------------------------------------------------------------------
	// exit codes

	public static final int NO_ERROR = 0;
	public static final int ERROR_INVALID_OPTION = 1;
	public static final int ERROR_MISSING_VALUE_FOR_OPTION = 2;

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
		options.addOption("d", "directory", true, "Directory for log files. DEFAULT: an opsi log directory "
				+ "dependent on system and user privileges, see /help/logfile");
		options.addOption(null, "loglevel", true, "Set logging level L, L is a number >= " + Logging.LEVEL_NONE
				+ ", <= " + Logging.LEVEL_SECRET + " . DEFAULT: " + Logging.getLogLevelConsole());
		options.addOption(null, "help", false, "Give this help");
		options.addOption("v", "version", false, "Tell configed version");

		// Configed specific options
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
		options.addOption(null, "ssh-immediate-connect", true, "Try to create a SSH connection on start. DEFAULT: N");
		options.addOption(null, "ssh-key", true,
				"Full path with filename from sshkey used for authentication on ssh server");
		options.addOption(null, "ssh-passphrase", true,
				"Passphrase for given sshkey used for authentication on ssh server");
		options.addOption(null, "collect_queries_until_no", true, "Collect the first N queries; N = "
				+ OpsiMethodCall.getMaxCollecSize() + " (DEFAULT).  -1 meaning 'no collect'. 0 meaning 'infinite' ");
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
		Logging.essential("configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);

		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(Integer.MAX_VALUE);
		formatter.printHelp(Main.USAGE_INFO, options);
	}

	private static void setGlobalValues() {
		if (UserPreferences.get(UserPreferences.LANGUAGE) != null) {
			Messages.setLocale(UserPreferences.get(UserPreferences.LANGUAGE));
		}

		if (UserPreferences.get(UserPreferences.THEME) != null) {
			Messages.setTheme(UserPreferences.get(UserPreferences.THEME));
			Main.setOpsiLaf();
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

				Logging.setLogLevelConsole(loglevel);
				Logging.setLogLevelFile(loglevel);
			} catch (NumberFormatException ex) {
				Logging.debug(" \n\nArgument >" + loglevelString + "< has no integer format");
			}
		}

		if (cmd.hasOption("version")) {
			Logging.essential(
					"configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);
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

		// After setting locale then we can use localization values
		List<String> existingLocales = Messages.getLocaleNames();
		Logging.info("Available locales: " + existingLocales);

	}

	public static void endApp(int exitcode) {
		if (Configed.getSavedStates() != null) {
			try {
				Configed.getSavedStates().store("states on finishing configed");
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

	public static boolean isLogviewer() {
		return isLogviewer;
	}

	public static void setOpsiLaf() {
		Logging.info("set look and feel " + Messages.getSelectedTheme());

		// Location of the theme property files - register them
		FlatLaf.registerCustomDefaultsSource("de.uib.configed.themes");

		registerOpenSansFont();

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

	private static void registerOpenSansFont() {
		try (InputStream fontStream = Main.class.getResourceAsStream("/fonts/OpenSans.ttf")) {
			Font openSansFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
			openSansFont = openSansFont.deriveFont(14F);
			UIManager.put("defaultFont", openSansFont);
		} catch (IOException e) {
			Logging.error("Failed to retrieve font from resources (using font chosen by the system)", e);
		} catch (FontFormatException e) {
			Logging.error("Font is faulty", e);
		}
	}

	public static void main(String[] args) {
		setGlobalValues();

		createOptions();
		CommandLine cmd;
		try {
			CommandLineParser parser = new DefaultParser(false);
			cmd = parser.parse(options, args, false);
		} catch (ParseException e) {
			Logging.error("Problem parsing arguments", e);
			showHelp();
			return;
		}

		parseArgs(cmd);

		UIManager.put("FileChooser.cancelButtonText", Configed.getResourceValue("buttonCancel"));
		UIManager.put("FileChooser.cancelButtonToolTipText", "");
		// TODO Translate
		UIManager.put("FileChooser.lookInLabelText", "Suchen in:");

		UIManager.put("OptionPane.yesButtonText", Configed.getResourceValue("buttonYES"));
		UIManager.put("OptionPane.noButtonText", Configed.getResourceValue("buttonNO"));
		UIManager.put("OptionPane.cancelButtonText", Configed.getResourceValue("buttonCancel"));

		setOpsiLaf();

		// Turn on antialiasing for text (not for applets)
		System.setProperty("swing.aatext", "true");

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
