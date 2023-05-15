package de.uib;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
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

	public static OptionGroup getGeneralOptions() {
		OptionGroup optionGroup = new OptionGroup();
		optionGroup.addOption(
				new Option("lv", "logviewer", false, "MUST BE FIRST OPTION, use this option to start logviewer"));
		optionGroup.addOption(new Option("d", "directory", true,
				"Directory for the log files. DEFAULT: an opsi log directory, dependent on system and user privileges, lookup in /help/logfile"));

		return optionGroup;
	}

	private static Options createGeneralOptions() {
		Options options = new Options();
		options.addOptionGroup(getGeneralOptions());

		return options;
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

		if (cmd.hasOption("d")) {
			Logging.logDirectoryName = cmd.getOptionValue("d");
		}
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
			} catch (Exception ex) {
				Logging.debug("UIManager.setLookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel')," + ex);
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
