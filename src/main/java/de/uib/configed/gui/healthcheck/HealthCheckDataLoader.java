/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import javax.swing.SwingWorker;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ConfigedMain;

public class HealthCheckDataLoader extends SwingWorker<Void, Void> {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	@Override
	protected Void doInBackground() throws Exception {
		persistenceController.getHealthDataService().retrieveHealthDataPD();
		return null;
	}

	@Override
	public void done() {
		ConfigedMain.getMainFrame().showHealthCheckPanel();
		ConfigedMain.getMainFrame().deactivateLoadingPane();
	}
}
