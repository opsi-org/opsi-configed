/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */
package de.uib.configed;

import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.gui.ClientTable;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class ConnectedHostsManager implements MessagebusListener {
	private Set<String> connectedHostsByMessagebus;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ConnectedHostsManager(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		connectedHostsByMessagebus = persistenceController.getHostDataService().getMessagebusConnectedClients();
	}

	public boolean isHostConnected(String hostId) {
		return connectedHostsByMessagebus.contains(hostId);
	}

	public void addClientToConnectedList(String clientId) {
		connectedHostsByMessagebus.add(clientId);
		updateConnectionStatusInTable(clientId);
	}

	public void removeClientFromConnectedList(String clientId) {
		connectedHostsByMessagebus.remove(clientId);
		updateConnectionStatusInTable(clientId);
	}

	private void updateConnectionStatusInTable(String clientName) {
		AbstractTableModel model = configedMain.getClientTablePanel().getTableModel();

		int col = model.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));
		ClientTable clientTable = configedMain.getClientTablePanel().getClientTable();

		for (int row = 0; row < clientTable.getRowCount(); row++) {
			if (clientTable.getClientName(row).equals(clientName)) {
				clientTable.setValueAt(connectedHostsByMessagebus.contains(clientName), row, col);
				model.fireTableCellUpdated(row, col);
				Logging.info(this, "connectionStatus for client ", clientName, " updated in table");
				return;
			}
		}
		Logging.info(this, "could not update connectionStatus for client ", clientName, ": not in list of shown table");
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// Not required to implement.
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		// Sleep for a little because otherwise we cannot get the needed data from the server.
		Utils.threadSleep(this, 5);

		if (!WebSocketEvent.GENERAL_EVENT.toString().equals(message.get("type")) && !message.containsKey("event")) {
			return;
		}

		String eventType = (String) message.get("event");

		Map<String, Object> eventData = POJOReMapper.remap(message.get("data"));

		if (WebSocketEvent.HOST_CONNECTED.toString().equals(eventType)) {
			addClientToConnectedList((String) ((Map<?, ?>) eventData.get("host")).get("id"));
		} else if (WebSocketEvent.HOST_DISCONNECTED.toString().equals(eventType)) {
			removeClientFromConnectedList((String) ((Map<?, ?>) eventData.get("host")).get("id"));
		} else {
			// Other events are handled by other listeners.
		}
	}
}
