package de.uib.logviewer;

import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.MissingResourceException;

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

	private static LogviewMain cm;

	private static String fileName = "";
	private static String logdirectory = "";

	private static String tabs(int count) {
		StringBuilder buf = new StringBuilder("");
		for (int j = 0; j < count; j++) {
			buf.append("\t");
		}

		return buf.toString();
	}

	protected static void usage() {
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

	protected static String getArg(String[] args, int i) {
		if (args.length <= i + 1 || args[i + 1].indexOf('-') == 0) {
			System.err.println("Missing value for option " + args[i]);
			usage();
			endApp(1);
		}
		i++;
		return args[i];
	}

	protected static boolean isValue(String[] args, int i) {

		if (i >= args.length || args[i].indexOf('-') == 0) {
			return false;
		}

		return true;
	}

	/** construct the application */
	public Logview(String paramLogdirectory, String paramLocale, String paramFilename) {
		UncaughtConfigedExceptionHandler errorHandler = new UncaughtConfigedExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);

		System.out.println("starting " + getClass().getName());

		configureUI();

		String imageHandled = "(we start image retrieving)";
		System.out.println(imageHandled);
		try {
			String resourceS = "opsi.gif";
			URL resource = Globals.class.getResource(resourceS);
			if (resource == null) {
				System.out.println("image resource " + resourceS + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
				imageHandled = "setIconImage";
			}
		} catch (Exception ex) {
			System.out.println("imageHandled failed: " + ex.toString());
		}

		System.out.println("--  wantedDirectory " + Logging.wantedDirectory);

		//
		List<String> existingLocales = Messages.getLocaleNames();
		Messages.setLocale(paramLocale);

		Logging.info("getLocales: " + existingLocales);
		Logging.info("selected locale characteristic " + Messages.getSelectedLocale());

		// set wanted directory for logging
		if (logdirectory != null) {
			if (new File(logdirectory).isDirectory())
				Logging.wantedDirectory = logdirectory;
			else {
				Logging.error("This is no directory: " + logdirectory);
				Logging.wantedDirectory = "";
			}
		} else {
			System.out.println(" --  wantedDirectory " + Logging.wantedDirectory);
		}

		Logging.wantedDirectory = "";

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
			System.out.println(" --  fileName " + Logging.wantedDirectory);
		}

		String[] nameParts = getClass().getName().split("\\.");
		if (nameParts.length > 0) {
			Logging.programSubDir = "." + nameParts[nameParts.length - 1];
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				cm = new LogviewMain();
				cm.init();
			}
		});

	}

	protected static void processArgs(String[] args) {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--help")) {
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
			} else // options
			{

				if (args[i].equals("-d") || args[i].equals("--logdirectory")) {
					logdirectory = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("-f") || args[i].equals("--filename")) {
					fileName = getArg(args, i);
					i = i + 2;
				} else if (args[i].equals("--version")) {
					System.out.println("logview version: " + Globals.VERSION + " (" + Globals.VERDATE + ") ");
					System.exit(0);
				}

				else if (args[i].equals("--help")) {
					usage();
					System.exit(0);
				} else if (args[i].equals("--loglevel")) {
					int newlevel = Logging.AKT_DEBUG_LEVEL;
					try {
						newlevel = Integer.parseInt(getArg(args, i));
						if (newlevel != Logging.AKT_DEBUG_LEVEL) {
							// ? is setting allowed
							if (newlevel <= Logging.LEVEL_DEBUG && newlevel >= Logging.LEVEL_FATAL)

								Logging.setAktDebugLevel(newlevel);
							else
								Logging.info(" valid log levels between " + Logging.LEVEL_FATAL + " and "
										+ Logging.LEVEL_DEBUG);

						}
					} catch (NumberFormatException ex) {
						System.out.println(" \n\nArgument " + getArg(args, i) + " has no integer format");
					}
					i = i + 2;

				} else {
					usage();
					endApp(0);
				}
			}
		}
	}

	public static void endApp(int exitcode) {
		System.exit(exitcode);
	}

	public static String getResourceValue(String key) {
		String result = key;
		try {
			result = Messages.messages.getString(key);
		} catch (MissingResourceException mre) {
			// we return the key and log the problem:
			Logging.debug("Problem: " + mre.toString());

			try {
				result = Messages.messagesEN.getString(key);
			} catch (MissingResourceException mre2) {
				Logging.debug("Problem: " + mre2.toString());
			}
		} catch (Exception ex) {
			Logging.warning("messages not there");
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
	 *  o Redistributions of source code must retain the above copyright notice,
	 *    this list of conditions and the following disclaimer.
	 *
	 *  o Redistributions in binary form must reproduce the above copyright notice,
	 *    this list of conditions and the following disclaimer in the documentation
	 *    and/or other materials provided with the distribution.
	 *
	 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
	 *    its contributors may be used to endorse or promote products derived
	 *    from this software without specific prior written permission.
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

	public static String getInfo(String question, boolean password) {
		if (System.console() == null) {
			return "";
		}
		System.out.print(question);

		if (password) {
			return String.valueOf(System.console().readPassword()).trim();
		}

		return String.valueOf(System.console().readLine()).trim();
	}

	/**
	 * main-Methode
	 */
	public static void main(String[] args) {

		processArgs(args);
		try {
			String resourceS = Globals.iconresourcename;
			URL resource = Globals.class.getResource(resourceS);
			if (resource == null) {
				System.out.println("image resource " + resourceS + "  not found");
			} else {
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			}
		} catch (Exception ex) {
			System.out.println("imageHandled failed: " + ex.toString());
		}

		// Turn on antialiasing for text (not for applets)
		try {
			System.setProperty("swing.aatext", "true");
		} catch (Exception ex) {
			Logging.info(" setting property swing.aatext" + ex);
		}

		new Logview(logdirectory, "de", fileName);
	}
}
