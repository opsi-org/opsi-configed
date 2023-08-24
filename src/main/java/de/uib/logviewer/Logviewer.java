/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.logviewer;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;

import de.uib.logviewer.gui.LogFrame;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.UncaughtConfigedExceptionHandler;
import utils.Utils;

public final class Logviewer {
	private static String fileName = "";
	private static LogFrame logFrame;

	private Logviewer() {
		UncaughtConfigedExceptionHandler errorHandler = new UncaughtConfigedExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);

		Logging.essential(this.getClass(), "starting " + getClass().getName());
		Logging.info(this.getClass(), "--  wantedDirectory " + Logging.logDirectoryName);

		if (fileName != null) {
			LogFrame.setFileName(fileName);
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
		Utils.setMasterFrame(logFrame);
		logFrame.pack();

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Logging.info("screensize " + screenSize);
		logFrame.setSize((int) screenSize.getWidth() - 150, (int) screenSize.getHeight() - 150);
		logFrame.setLocationRelativeTo(null);

		Logging.info("mainframe nearly initialized");
		logFrame.setVisible(true);
	}

	public static LogFrame getLogFrame() {
		return logFrame;
	}

	public static void main(CommandLine cmd) {
		processArgs(cmd);
		new Logviewer();
	}
}
