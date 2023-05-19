package de.uib;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.tree.ClientTreeUI;
import de.uib.logviewer.Logviewer;
import de.uib.messages.Messages;
import de.uib.utilities.logging.Logging;

public class Main {

	public static final String USAGE_INFO = "configed [OPTIONS] " + ", where an OPTION may be\n";

	private static boolean isLogviewer;

	public static List<Option> getGeneralOptions() {
		List<Option> options = new ArrayList<>();

		options.add(new Option("lv", "logviewer", false, "MUST BE FIRST OPTION, use this option to start logviewer"));
		options.add(new Option("l", "locale", true,
				"Set locale LOC (format: <language>_<country>). DEFAULT: System.locale"));
		options.add(new Option("d", "directory", true, "Directory for log files. DEFAULT: an opsi log directory "
				+ "dependent on system and user privileges, see /help/logfile"));
		options.add(new Option(null, "loglevel", true, "Set logging level L, L is a number >= " + Logging.LEVEL_NONE
				+ ", <= " + Logging.LEVEL_SECRET + " . DEFAULT: " + Logging.getLogLevelConsole()));

		return options;
	}

	private static Options createGeneralOptions() {
		Options options = new Options();

		// Get the general options
		for (Option option : getGeneralOptions()) {
			options.addOption(option);
		}

		return options;
	}

	public static JFrame getMainFrame() {
		if (isLogviewer) {
			return Logviewer.getLogFrame();
		} else {
			return ConfigedMain.getMainFrame();
		}
	}

	public static void showHelp(Options options) {
		Logging.essential("configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);

		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(Integer.MAX_VALUE);
		formatter.printHelp(Main.USAGE_INFO, options);
	}

	private static void parseArgs(Options options, String[] args) throws ParseException {

		CommandLineParser parser = new DefaultParser(false);
		CommandLine cmd = parser.parse(options, args, true);

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

		Options options = createGeneralOptions();
		try {
			parseArgs(options, args);
		} catch (ParseException e) {
			Logging.error("could not parse arguments in main", e);
		}

		if (ConfigedMain.THEMES) {
			setOpsiLaf();
		} else {
			configureUI();
		}

		if (isLogviewer) {
			Logviewer.main(args);
		} else {
			Configed.main(args);
		}
	}
}
