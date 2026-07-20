/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */
package de.uib.configed.gui;

import java.util.Map;
import java.util.Set;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.core.infrastructure.messagebus.MessagebusListener;
import de.uib.configed.core.infrastructure.messagebus.WebSocketEvent;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.GenericTableViewMsg.CellEdited;
import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class ConnectedHostsManager implements MessagebusListener {
	private Set<String> connectedHostsByMessagebus;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ConnectedHostsManager(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		connectedHostsByMessagebus = persistenceController.getDataServices().host.getMessagebusConnectedClients();
	}

	public boolean isHostConnected(String hostId) {
		return connectedHostsByMessagebus.contains(hostId);
	}

	public void addClientToConnectedList(String clientId) {
		connectedHostsByMessagebus.add(clientId);
		updateConnectionStatusInGUI(clientId);
	}

	public void removeClientFromConnectedList(String clientId) {
		connectedHostsByMessagebus.remove(clientId);
		updateConnectionStatusInGUI(clientId);
	}

	private void updateConnectionStatusInGUI(String clientName) {
		// Update the connection status in the HostsStatusPanel
		ConfigedMain.getMainFrame().getMainPanelManager().getHostsStatusPanel().updateClientConnectionStatus();

		// Update the connection status in the ClientTable
		updateConnectionStatusInTable(clientName);
	}

	private void updateConnectionStatusInTable(String clientName) {
		GenericTableViewComponent clientTable = configedMain.getClientTablePanel().getTableComponent();
		int col = clientTable.getColumnIndexByKey(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);

		for (int row = 0; row < clientTable.getRowCount(); row++) {
			RowData data = clientTable.getRowByModelIndex(row);
			if (data.getValue(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, String.class).equals(clientName)) {
				clientTable.dispatch(new CellEdited(row, col, connectedHostsByMessagebus.contains(clientName)));
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
