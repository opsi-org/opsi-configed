/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.logging.Logging;

public class SessionInfoRetriever extends SwingWorker<Void, Void> {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;

	public SessionInfoRetriever(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	@Override
	protected Void doInBackground() throws Exception {
		persistenceController.getHostDataService().retrieveSessionInfo(configedMain.getSelectedClients());
		return null;
	}

	@Override
	protected void done() {
		Logging.info(this, "Session information retrieved");

		// update column
		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL))) {
			ClientTable clientTable = configedMain.getClientTablePanel().getClientTable();
			DefaultTableModel model = configedMain.getClientTablePanel().getTableModel();

			int col = model.findColumn(Configed.getResourceValue("sessionInfo"));

			for (int row = 0; row < clientTable.getRowCount(); row++) {
				String clientId = clientTable.getClientName(row);
				clientTable.setValueAt(persistenceController.getHostDataService().getSessionInfo().get(clientId), row,
						col);
			}

			model.fireTableDataChanged();
			configedMain.getClientTablePanel()
					.setSelectedValues(persistenceController.getHostDataService().getSessionInfo().keySet());
		}
		ConfigedMain.getMainFrame().setCursor(null);
	}

	public static void retrieveSessionInfo(ConfigedMain configedMain) {
		ConfigedMain.getMainFrame().setCursor(Globals.WAIT_CURSOR);
		boolean visible = PersistenceControllerFactory.getPersistenceController().getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		if (!visible) {
			configedMain.toggleColumn(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		}

		Logging.info("setColumnSessionInfo ", visible);

		SessionInfoRetriever infoRetriever = new SessionInfoRetriever(configedMain);
		infoRetriever.execute();
	}
}
