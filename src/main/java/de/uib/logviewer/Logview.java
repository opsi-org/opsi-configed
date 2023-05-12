package de.uib.logviewer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.SwingUtilities;

import de.uib.configed.Globals;
import de.uib.logviewer.gui.LogFrame;
import de.uib.messages.Messages;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.UncaughtConfigedExceptionHandler;

public class Logview {

	public static final String usage = "\n" + "\tlogview [OPTIONS] \n" + "\t\twhere an OPTION may be \n";

	public static final String[][] usageLines = new String[][] {
			new String[] { "-d PATH", "--logdirectory PATH", "Directory for the log files" },
			new String[] { "-f FILENAME", "--filename FILENAME", "filename for the log file" },
			new String[] { "--version", "", "Tell logview version" },
			new String[] { "--help", "", "Give this help" }, };

	private static String fileName = "";
	private static String logdirectory = "";

	/** construct the application */
	public Logview(String paramLogdirectory, String paramLocale, String paramFilename) {
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

		// set wanted directory for logging
		if (logdirectory != null) {
			if (new File(logdirectory).isDirectory()) {
				Logging.logDirectoryName = logdirectory;
			} else {
				Logging.error("This is no directory: " + logdirectory);
				Logging.logDirectoryName = "";
			}
		} else {
			Logging.info(" --  wantedDirectory " + Logging.logDirectoryName);
		}

		Logging.logDirectoryName = "";

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

	private static String tabs(int count) {
		StringBuilder buf = new StringBuilder("");
		for (int j = 0; j < count; j++) {
			buf.append("\t");
		}

		return buf.toString();
	}

	private static void usage() {
		Logging.essential(usage);

		final int TAB_WIDTH = 8;
		int length0 = 0;
		int length1 = 0;

		for (int i = 0; i < usageLines.length; i++) {
			//we find max of fillTabs0, fillTabs1
			int len = usageLines[i][0].length();

			if (len > length0) {
				length0 = len;
			}

			len = usageLines[i][1].length();

			if (len > length1) {
				length1 = len;
			}
		}

		int allTabs0 = length0 / TAB_WIDTH + 1;

		int allTabs1 = length1 / TAB_WIDTH + 1;

		for (int i = 0; i < usageLines.length; i++) {

			int startedTabs0 = usageLines[i][0].length() / TAB_WIDTH;
			int startedTabs1 = usageLines[i][1].length() / TAB_WIDTH;

			Logging.info("\t" + usageLines[i][0] + tabs(allTabs0 - startedTabs0) + usageLines[i][1]
					+ tabs(allTabs1 - startedTabs1) + usageLines[i][2]);
		}

	}

	private static String getArg(String[] args, int i) {
		if (args.length <= i + 1 || args[i + 1].indexOf('-') == 0) {
			System.err.println("Missing value for option " + args[i]);
			usage();
			endApp(1);
		}
		i++;
		return args[i];
	}

	private static void processArgs(String[] args) {

		for (int i = 0; i < args.length; i++) {
			if ("--help".equals(args[i])) {
				usage();
				endApp(0);
			}
		}

		int firstPossibleNonOptionIndex = args.length - 1;

		int i = 0;
		while (i < args.length) {

			if (args[i].charAt(0) != '-') {
				//no option
				if (i < firstPossibleNonOptionIndex) {
					usage();
					endApp(0);
				}
				i++;
			} else {
				// options

				if ("-d".equals(args[i]) || "--logdirectory".equals(args[i])) {
					logdirectory = getArg(args, i);
					i = i + 2;
				} else if ("-f".equals(args[i]) || "--filename".equals(args[i])) {
					fileName = getArg(args, i);
					i = i + 2;
				} else if ("--help".equals(args[i])) {
					usage();
					System.exit(0);
				} else if ("--logviewer".equals(args[i])) {
					// Do nothing since it was used for starting the logviewer
					i++;
				} else {
					usage();
					endApp(0);
				}
			}
		}
	}

	private static void endApp(int exitcode) {
		System.exit(exitcode);
	}

	/**
	 * main-Methode
	 */
	public static void main(String[] args) {

		processArgs(args);

		try {
			URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
			if (resource == null) {
				Logging.warning("image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			}
		} catch (Exception ex) {
			Logging.warning("imageHandled failed: " + ex.toString());
		}

		// Turn on antialiasing for text 
		try {
			System.setProperty("swing.aatext", "true");
		} catch (Exception ex) {
			Logging.info(" setting property swing.aatext" + ex);
		}

		new Logview(logdirectory, "de", fileName);
	}
}
