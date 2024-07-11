/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.Map;

import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

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

		ConfigedMain.getMainFrame().getIconBarPanel().getjButtonSessionInfo().setEnabled(true);

		// update column
		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL))) {
			AbstractTableModel model = configedMain.getClientTable().getTableModel();

			int col = model.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientSessionInfo"));

			for (int row = 0; row < model.getRowCount(); row++) {
				String clientId = (String) model.getValueAt(row, 0);
				model.setValueAt(sessionInfo.get(clientId), row, col);
			}

			model.fireTableDataChanged();
			configedMain.setSelectedClients(configedMain.getSelectedClients());
		}
		ConfigedMain.getMainFrame().setCursor(null);
	}
}
