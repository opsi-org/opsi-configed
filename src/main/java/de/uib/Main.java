package de.uib;

import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
import de.uib.configed.tree.ClientTreeUI;
import de.uib.logviewer.Logviewer;
import de.uib.messages.Messages;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.utilities.logging.Logging;

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
		options.addOption(null, "gzip", true,
				"Activate compressed transmission of data from opsi server yes/no. DEFAULT: y");
		options.addOption(null, "ssh-immediate-connect", true, "Try to create a SSH connection on start. DEFAULT: N");
		options.addOption(null, "ssh-key", true,
				"Full path with filename from sshkey used for authentication on ssh server");
		options.addOption(null, "ssh-passphrase", true,
				"Passphrase for given sshkey used for authentication on ssh server");
		options.addOption(null, "collect_queries_until_no", true, "Collect the first N queries; N = "
				+ OpsiMethodCall.maxCollectSize + " (DEFAULT).  -1 meaning 'no collect'. 0 meaning 'infinite' ");
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

	private static void parseArgs(CommandLine cmd) {

		if (cmd.hasOption("lv")) {
			isLogviewer = true;
		}

		String locale = null;
		if (cmd.hasOption("l")) {
			locale = cmd.getOptionValue("l");
		}

		// Set locale, then we can use localization values
		List<String> existingLocales = Messages.getLocaleNames();
		Messages.setLocale(locale);
		Logging.info("getLocales: " + existingLocales);
		Logging.info("selected locale characteristic " + Messages.getSelectedLocale());

		if (cmd.hasOption("d")) {
			Logging.logDirectoryName = cmd.getOptionValue("d");
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
	}

	public static void endApp(int exitcode) {
		if (Configed.savedStates != null) {
			try {
				Configed.savedStates.store("states on finishing configed");
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
			} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
					| IllegalAccessException ex) {
				Logging.warning("UIManager.setLookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel')", ex);
			}
		}
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

	public static void main(String[] args) {
		createOptions();

		try {
			CommandLineParser parser = new DefaultParser(false);

			CommandLine cmd = parser.parse(options, args, false);

			parseArgs(cmd);

			if (ConfigedMain.THEMES) {
				setOpsiLaf();
			} else {
				configureUI();
			}

			if (isLogviewer) {
				Logviewer.main(cmd);
			} else {
				Configed.main(cmd);
			}
		} catch (ParseException e) {
			Logging.error("Problem parsing arguments", e);
			showHelp();
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

	}
}
