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

	LogFrame mainFrame;

	protected void initGui() {

		initMainFrame();
	}

	public void init() {
		Logging.debug(this, "init");

		Logging.clearErrorList();

		initGui();

	}

	protected void initMainFrame() {

		mainFrame = new LogFrame(this);

		// for passing it to message frames everywhere

		Globals.container1 = mainFrame;
		Globals.frame1 = mainFrame;

		//rearranging visual components
		mainFrame.pack();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		int wTaken = LogFrame.fwidth;
		int hTaken = LogFrame.fheight;

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

		int wDiff = wTaken - 30 - LogFrame.fwidth;
		if (wDiff < 0) {
			wDiff = 0;
		}
		int hDiff = hTaken - 30 - LogFrame.fheight;
		if (hDiff < 0) {
			hDiff = 0;
		}

		final int width = LogFrame.fwidth + (wDiff * 2) / 3;
		final int height = LogFrame.fheight + (hDiff * 2) / 3;

		mainFrame.startSizing(width, height);

		Dimension frameSize = mainFrame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		mainFrame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		// init visual states
		Logging.info(this, "mainframe nearly initialized");

		mainFrame.setVisible(true);
		mainFrame.setFocusToJTextPane();

	}

	public void showExternalDocument(String urlS) {
		try {
			Runtime rt = Runtime.getRuntime();
			String osName = System.getProperty("os.name");
			if (osName.toLowerCase().startsWith("windows")) {
				String title = "";
				Process proc = rt.exec("cmd.exe /c start \"" + title + "\" \"" + urlS.replace("\\", "\\\\") + "\"");
			} else {
				//Linux, we assume that there is a firefox and it will handle the url // TODO use something different
				String[] cmdarray = new String[] { "firefox", urlS };
				Process proc = rt.exec(cmdarray);
			}
		} catch (Exception ex) {
			Logging.error("" + ex);
		}
	}

	protected void checkErrorList() {
		Logging.checkErrorList(mainFrame);
	}

	public void finishApp(boolean checkdirty, int exitcode) {
		System.exit(exitcode);
	}
}
