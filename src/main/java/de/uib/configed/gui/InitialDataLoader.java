/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.SwingWorker;

import de.uib.configed.share.logging.Logging;

public class InitialDataLoader extends SwingWorker<Void, Void> {
	private ConfigedMain configedMain;
	private boolean isDataLoaded;

	public InitialDataLoader(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	@Override
	protected Void doInBackground() throws Exception {
		isDataLoaded = false;
		configedMain.preloadData();
		return null;
	}

	@Override
	public void done() {
		configedMain.initGui();

		isDataLoaded = true;

		Logging.checkErrorList();
		ConfigedMain.getLoginDialog().setVisible(false);

		Logging.info("setting mainframe visible");

		ConfigedMain.getMainFrame().setVisible(true);
		ConfigedMain.getMainFrame().toFront();
	}

	public boolean isDataLoaded() {
		return isDataLoaded;
	}
}
