/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.logviewer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;

import de.uib.configed.Globals;
import de.uib.logviewer.gui.LogFrame;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.UncaughtConfigedExceptionHandler;

public final class Logviewer {

	private static String fileName = "";

	private static LogFrame logFrame;

	/** construct the application */
	private Logviewer() {
		UncaughtConfigedExceptionHandler errorHandler = new UncaughtConfigedExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);

		Logging.essential(this.getClass(), "starting " + getClass().getName());

		String imageHandled = "(we start image retrieving)";
		Logging.info(this.getClass(), imageHandled);

		URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
		if (resource == null) {
			Logging.warning(this.getClass(), "image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
		} else {
			Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
			imageHandled = "setIconImage";
		}

		Logging.info(this.getClass(), "--  wantedDirectory " + Logging.logDirectoryName);

		// set wanted fileName
		if (fileName != null) {
			if (new File(fileName).isDirectory()) {
				Logging.info(this.getClass(), "This is a directory: " + fileName);
				LogFrame.setFileName("");
			} else if (!new File(fileName).exists()) {
				Logging.info(this.getClass(), "File does not exist: " + fileName);
				LogFrame.setFileName("");
			} else {
				LogFrame.setFileName(fileName);
			}
		} else {
			Logging.info(" --  fileName " + Logging.logDirectoryName);
		}

		SwingUtilities.invokeLater(Logviewer::init);
	}

	private static void processArgs(CommandLine cmd) {

		if (cmd.hasOption("f")) {
			fileName = cmd.getOptionValue("f");
		}
	}

	public static void init() {
		Logging.debug("init in logviewer");
		Logging.clearErrorList();

		logFrame = new LogFrame();

		// for passing it to message frames everywhere

		Globals.frame1 = logFrame;

		//rearranging visual components
		logFrame.pack();

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		Logging.info("screensize " + screenSize);
		logFrame.setSize((int) screenSize.getWidth() - 150, (int) screenSize.getHeight() - 150);

		logFrame.setLocationRelativeTo(null);

		// init visual states
		Logging.info("mainframe nearly initialized");

		logFrame.setVisible(true);
	}

	public static LogFrame getLogFrame() {
		return logFrame;
	}

	/**
	 * main-Methode
	 */
	public static void main(CommandLine cmd) {

		processArgs(cmd);

		URL resource = Globals.class.getResource(Globals.ICON_RESOURCE_NAME);
		if (resource == null) {
			Logging.warning("image resource " + Globals.ICON_RESOURCE_NAME + "  not found");
		} else {
			Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
		}

		new Logviewer();
	}
}
