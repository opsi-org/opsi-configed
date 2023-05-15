package de.uib;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.tree.ClientTreeUI;
import de.uib.logviewer.Logviewer;
import de.uib.messages.Messages;
import de.uib.utilities.logging.Logging;

public class Main {

	private static boolean isLogviewer;

	private static Options createOptions() {
		Options options = new Options();

		options.addOption("lv", "logviewer", false, "description of logviewer");

		return options;
	}

	private static void parseArgs(Options options, String[] args) throws ParseException {

		CommandLineParser parser = new DefaultParser(false);
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("lv")) {
			isLogviewer = true;
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

		Options options = createOptions();
		try {
			parseArgs(options, args);
		} catch (ParseException e) {
			// TODO 
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
