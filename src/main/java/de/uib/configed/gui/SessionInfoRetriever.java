/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.SwingWorker;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.features.table.TableColumnConfig;
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
		persistenceController.getDataServices().host.retrieveSessionInfo(configedMain.getSelectedClients());
		return null;
	}

	@Override
	protected void done() {
		Logging.info(this, "Session information retrieved");

		if (Boolean.FALSE.equals(persistenceController.getDataServices().host.getHostDisplayFields()
				.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL))) {
			ConfigedMain.getMainFrame().setCursor(null);
			return;
		}

		GenericTableViewComponent component = configedMain.getClientTablePanel().getTableComponent();
		int sessionColumnIndex = -1;

		for (int i = 0; i < component.model.getColumns().size(); i++) {
			TableColumnConfig columnConfig = component.model.getColumns().get(i);
			if (HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL.equals(columnConfig.getKey())) {
				sessionColumnIndex = i;
			}
		}

		if (sessionColumnIndex != -1) {
			for (int i = 0; i < component.model.getRows().size(); i++) {
				if (!component.model.getSelectedRows().contains(i)) {
					continue;
				}

				RowData data = component.model.getRows().get(i);
				String clientName = data.getValue(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, String.class);
				String result = persistenceController.getDataServices().host.getSessionInfo().get(clientName);
				component.dispatch(new GenericTableViewMsg.CellEdited(i, sessionColumnIndex, result));
			}
		}

		configedMain.getClientTablePanel()
				.setSelectedValues(persistenceController.getDataServices().host.getSessionInfo().keySet());
		ConfigedMain.getMainFrame().setCursor(null);
	}

	public static void retrieveSessionInfo(ConfigedMain configedMain) {
		ConfigedMain.getMainFrame().setCursor(Globals.WAIT_CURSOR);
		boolean visible = PersistenceControllerFactory.getPersistenceController().getDataServices().host
				.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		if (!visible) {
			configedMain.toggleColumn(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		}

		Logging.info("setColumnSessionInfo ", visible);

		SessionInfoRetriever infoRetriever = new SessionInfoRetriever(configedMain);
		infoRetriever.execute();
	}
}
