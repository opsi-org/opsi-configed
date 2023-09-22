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

		String imageHandled = "(we start image retrieving)";
		Logging.info(this.getClass(), imageHandled);

		Logging.info(this.getClass(), "--  wantedDirectory " + Logging.getLogDirectoryName());

		if (fileName != null) {
			LogFrame.setFileName(fileName);
		} else {
			Logging.info(" --  fileName " + Logging.getLogDirectoryName());
		}

		SwingUtilities.invokeLater(Logviewer::init);
	}

	private static void processArgs(CommandLine cmd) {
		if (cmd.hasOption("f")) {
			fileName = cmd.getOptionValue("f");
		} else if (cmd.getArgList().size() == 1) {
			// Handle right-click menu file opening in Linux:  When opening log
			// file in Linux using the righ-click menu, the "-f" option may not
			// be added (i.e, automatic file opening won't be handled by Linux).
			// Therefore, the logviewer will be opened without a file.
			fileName = cmd.getArgList().get(0);
		} else {
			// No file attached or too many files attached.
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
