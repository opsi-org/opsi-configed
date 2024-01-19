/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.SwingWorker;

import de.uib.configed.ConfigedMain;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

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
		HealthCheckDialog dialog = new HealthCheckDialog();
		dialog.setupLayout();
		dialog.setVisible(true);
		ConfigedMain.getMainFrame().deactivateLoadingPane();
	}
}
