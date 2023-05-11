package de.uib.logviewer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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

		System.out.println("starting " + getClass().getName());

		configureUI();

		String imageHandled = "(we start image retrieving)";
		System.out.println(imageHandled);
		try {
			URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
			if (resource == null) {
				System.out.println("image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
				imageHandled = "setIconImage";
			}
		} catch (Exception ex) {
			System.out.println("imageHandled failed: " + ex.toString());
		}

		System.out.println("--  wantedDirectory " + Logging.logDirectoryName);

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
			System.out.println(" --  wantedDirectory " + Logging.logDirectoryName);
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
			System.out.println(" --  fileName " + Logging.logDirectoryName);
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
		System.out.println(usage);

		final int tabWidth = 8;
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

		int allTabs0 = length0 / tabWidth + 1;

		int allTabs1 = length1 / tabWidth + 1;

		for (int i = 0; i < usageLines.length; i++) {

			int startedTabs0 = usageLines[i][0].length() / tabWidth;
			int startedTabs1 = usageLines[i][1].length() / tabWidth;

			System.out.println("\t" + usageLines[i][0] + tabs(allTabs0 - startedTabs0) + usageLines[i][1]
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

	public static void configureUI() {
		boolean trynimbus = true;
		boolean found = false;

		if (trynimbus) {
			try {
				for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						Logging.info("setting Nimbus look&feel");
						UIManager.setLookAndFeel(info.getClassName());
						Logging.info("Nimbus look&feel set");

						UIManager.put("Tree.selectionBackground", UIManager.get("controlHighlight"));

						found = true;
						break;
					}
				}
			} catch (javax.swing.UnsupportedLookAndFeelException e) {
				// handle exception
				System.out.println(e);
			} catch (ClassNotFoundException e) {
				// handle exception
				System.out.println(e);
			} catch (InstantiationException e) {
				// handle exception
				System.out.println(e);
			} catch (IllegalAccessException e) {
				// handle exception
				System.out.println(e);
			}
		}

		if (!found) {
			trynimbus = false;
		}

		if (!trynimbus) {
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			} catch (Exception ex) {
				System.out.println("UIManager.setLookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel')," + ex);
			}
		}
	}

	/**
	 * main-Methode
	 */
	public static void main(String[] args) {

		//processArgs(args);
		try {
			URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
			if (resource == null) {
				System.out.println("image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			}
		} catch (Exception ex) {
			System.out.println("imageHandled failed: " + ex.toString());
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
