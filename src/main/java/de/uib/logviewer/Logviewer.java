package de.uib.logviewer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.logviewer.gui.LogFrame;
import de.uib.messages.Messages;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.UncaughtConfigedExceptionHandler;

public class Logviewer {

	private static String fileName = "";

	/** construct the application */
	public Logviewer(String paramLocale) {
		UncaughtConfigedExceptionHandler errorHandler = new UncaughtConfigedExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);

		Logging.essential(this, "starting " + getClass().getName());

		String imageHandled = "(we start image retrieving)";
		Logging.info(this, imageHandled);
		try {
			URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
			if (resource == null) {
				Logging.warning(this, "image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
				imageHandled = "setIconImage";
			}
		} catch (Exception ex) {
			Logging.warning(this, "imageHandled failed: " + ex.toString());
		}

		Logging.info(this, "--  wantedDirectory " + Logging.logDirectoryName);

		//
		List<String> existingLocales = Messages.getLocaleNames();
		Messages.setLocale(paramLocale);

		Logging.info("getLocales: " + existingLocales);
		Logging.info("selected locale characteristic " + Messages.getSelectedLocale());

		// set wanted fileName
		if (fileName != null) {
			if (new File(fileName).isDirectory()) {
				Logging.info(this, "This is a directory: " + fileName);
				LogFrame.setFileName("");
			} else if (!new File(fileName).exists()) {
				Logging.info(this, "File does not exist: " + fileName);
				LogFrame.setFileName("");
			} else {
				LogFrame.setFileName(fileName);
			}
		} else {
			Logging.info(" --  fileName " + Logging.logDirectoryName);
		}

		SwingUtilities.invokeLater(this::init);
	}

	private static Options createLogviewerOptions() {
		Options options = new Options();

		options.addOption("f", "filename", true, "filename for the log file");
		options.addOption("v", "version", false, "Tell logviewer version");
		options.addOption(null, "help", false, "Give this help");

		// Add the general options to configed-specific options
		for (Option option : Main.getGeneralOptions()) {
			options.addOption(option);
		}

		return options;
	}

	private static void processArgs(Options options, String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser(false);
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("help")) {
			Main.showHelp(options);
			endApp(0);
		}

		if (cmd.hasOption("f")) {
			fileName = cmd.getOptionValue("f");
		}

		if (cmd.hasOption("version")) {
			Logging.essential(
					"configed version " + Globals.VERSION + " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);
			endApp(0);
		}
	}

	private void init() {
		Logging.debug(this, "init");
		Logging.clearErrorList();

		LogFrame logFrame = new LogFrame();

		// for passing it to message frames everywhere

		Globals.container1 = logFrame;
		Globals.frame1 = logFrame;

		//rearranging visual components
		logFrame.pack();

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		Logging.info(this, "screensize " + screenSize);
		logFrame.setSize((int) screenSize.getWidth() - 150, (int) screenSize.getHeight() - 150);

		logFrame.setLocationRelativeTo(null);

		// init visual states
		Logging.info(this, "mainframe nearly initialized");

		logFrame.setVisible(true);
		logFrame.setFocusToJTextPane();

	}

	private static void endApp(int exitcode) {
		System.exit(exitcode);
	}

	/**
	 * main-Methode
	 */
	public static void main(String[] args) {

		Options options = createLogviewerOptions();

		try {
			processArgs(options, args);
		} catch (ParseException e) {
			Logging.error("Problem parsing arguments in logviewer main", e);
			endApp(Configed.ERROR_INVALID_OPTION);
		}

		try {
			URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
			if (resource == null) {
				Logging.warning("image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			}
		} catch (Exception ex) {
			Logging.warning("imageHandled failed: ", ex);
		}

		// Turn on antialiasing for text 
		try {
			System.setProperty("swing.aatext", "true");
		} catch (Exception ex) {
			Logging.info(" setting property swing.aatext" + ex.toString());
		}

		new Logviewer("en");
	}
}
