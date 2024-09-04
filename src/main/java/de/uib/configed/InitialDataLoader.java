/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import javax.swing.SwingWorker;

import de.uib.utils.logging.Logging;

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
		configedMain.getLoginDialog().setVisible(false);

		Logging.info("setting mainframe visible");

		ConfigedMain.getMainFrame().setVisible(true);
		ConfigedMain.getMainFrame().getClientConfiguration().initSplitPanes();
		ConfigedMain.getMainFrame().toFront();
	}

	public boolean isDataLoaded() {
		return isDataLoaded;
	}
}
