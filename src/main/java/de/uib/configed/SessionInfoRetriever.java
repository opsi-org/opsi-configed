/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.Map;

import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import de.uib.configed.gui.ClientTable;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class SessionInfoRetriever extends SwingWorker<Void, Void> {
	private boolean onlySelectedClients;
	private Map<String, String> sessionInfo;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;

	public SessionInfoRetriever(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public void setOnlySelectedClients(boolean onlySelectedClients) {
		this.onlySelectedClients = onlySelectedClients;
	}

	@Override
	protected Void doInBackground() throws Exception {
		sessionInfo = persistenceController.getHostDataService()
				.sessionInfo(onlySelectedClients ? configedMain.getSelectedClients() : null);
		configedMain.setSessionInfo(sessionInfo);
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
				clientTable.setValueAt(sessionInfo.get(clientId), row, col);
			}

			model.fireTableDataChanged();
			configedMain.getClientTablePanel().setSelectedValues(sessionInfo.keySet());
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
		infoRetriever.setOnlySelectedClients(!configedMain.getSelectedClients().isEmpty());
		infoRetriever.execute();
	}
}
