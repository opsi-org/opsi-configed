/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class ReachableUpdater extends Thread {
	private ConfigedMain configedMain;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private int interval;

	ReachableUpdater(int interval, ConfigedMain configedMain) {
		super();
		this.interval = interval;
		this.configedMain = configedMain;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			Logging.debug(this, " editingTarget, viewIndex " + ConfigedMain.getEditingTarget() + ", "
					+ configedMain.getViewIndex());

			if (configedMain.getViewIndex() == ConfigedMain.VIEW_CLIENTS && Boolean.TRUE
					.equals(persistenceController.getHostDataService().getHostDisplayFields().get("clientConnected"))) {
				configedMain.setReachableInfo(null);
			}

			try {
				int millisecs = interval * 60 * 1000;
				Logging.debug(this, "Thread going to sleep for ms " + millisecs);
				sleep(millisecs);
			} catch (InterruptedException ex) {
				Logging.info(this, "Thread interrupted ");
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void startUpdater(Integer interval, ConfigedMain configedMain) {
		if (interval != null && interval > 0) {
			new ReachableUpdater(interval, configedMain).start();
		}
	}
}
