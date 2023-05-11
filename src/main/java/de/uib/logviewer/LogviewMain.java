/*
 * logview - logfile viewer for configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2017 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */

package de.uib.logviewer;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import de.uib.configed.Globals;
import de.uib.logviewer.gui.LogFrame;
import de.uib.utilities.logging.Logging;

/**
 * LogviewMain description: The main controller of the program copyright:
 * Copyright (c) 2000-2018 organization: uib.de
 * 
 * @author D. Oertel, R. Roeder, J. Schneider, M. Hammel
 */
public class LogviewMain {

	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 600;

	private LogFrame logFrame;

	public void init() {
		Logging.debug(this, "init");
		Logging.clearErrorList();

		initGui();
	}

	private void initGui() {

		logFrame = new LogFrame();

		// for passing it to message frames everywhere

		Globals.container1 = logFrame;
		Globals.frame1 = logFrame;

		//rearranging visual components
		logFrame.pack();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		int wTaken = FRAME_WIDTH;
		int hTaken = FRAME_HEIGHT;

		for (int i = 0; i < gs.length; i++) {
			DisplayMode dm = gs[i].getDisplayMode();
			Logging.info(this, "width " + i + ": " + dm.getWidth());
			if (dm.getWidth() > wTaken) {
				wTaken = dm.getWidth();
				hTaken = dm.getHeight();
			}
			Logging.info(this, "height " + i + ": " + dm.getHeight());
		}

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		Logging.info(this, "startSizing width, height " + wTaken + ", " + hTaken);

		int wDiff = wTaken - 30 - FRAME_WIDTH;
		if (wDiff < 0) {
			wDiff = 0;
		}
		int hDiff = hTaken - 30 - FRAME_HEIGHT;
		if (hDiff < 0) {
			hDiff = 0;
		}

		final int width = FRAME_WIDTH + (wDiff * 2) / 3;
		final int height = FRAME_HEIGHT + (hDiff * 2) / 3;

		logFrame.setSize(width, height);

		Dimension frameSize = logFrame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		logFrame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		// init visual states
		Logging.info(this, "mainframe nearly initialized");

		logFrame.setVisible(true);
		logFrame.setFocusToJTextPane();

	}
}
