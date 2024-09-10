/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.ConfigedMain;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.ColorTableCellRenderer;

public class ClientTable extends JTable implements MessagebusListener {
	private List<SortKey> primaryOrderingKeys;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientTable(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		super.setDragEnabled(true);
		super.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		super.setAutoCreateRowSorter(true);
		super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		super.getTableHeader().setReorderingAllowed(false);

		// true destroys setSelectedRow etc
		super.setColumnSelectionAllowed(false);

		primaryOrderingKeys = new ArrayList<>();
		primaryOrderingKeys.add(new SortKey(0, SortOrder.ASCENDING));
	}

	public Set<String> getSelectedSet() {
		Set<String> result = new HashSet<>(getSelectedRowCount());

		for (int i : getSelectedRows()) {
			result.add((String) getValueAt(i, 0));
		}

		return result;
	}

	public void initSortKeys() {
		getRowSorter().setSortKeys(primaryOrderingKeys);
	}

	public void updateModel(TableModel tableModel) {
		Logging.info(this, "set model with column count ", tableModel.getColumnCount());

		Logging.info(this, " [JTableSelectionPanel] setModel with row count ", tableModel.getRowCount());

		tableModel.addTableModelListener(this);

		setModel(tableModel);
		((TableRowSorter<?>) getRowSorter()).setComparator(0, Comparator.comparing(String::toString));
	}

	public void moveToFirstSelected() {
		if (getSelectedRow() != -1) {
			Rectangle selectedRectangle = getCellRect(getSelectedRow(), 0, true);
			scrollRectToVisible(selectedRectangle);
		}
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

		if (WebSocketEvent.HOST_CREATED.toString().equals(eventType)) {
			addClientToTable((String) eventData.get("id"));
		} else if (WebSocketEvent.HOST_DELETED.toString().equals(eventType)) {
			removeClientFromTable((String) eventData.get("id"));
		} else {
			// Other events are handled by other listeners.
		}
	}

	public void addClientToTable(String clientId) {
		if (persistenceController.getHostInfoCollections().getOpsiHostNames().contains(clientId)
				|| ConfigedMain.getMainFrame().getClientConfiguration().getSelectedIndex() != 0) {
			return;
		}

		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		SwingUtilities.invokeLater(() -> {
			Set<String> selectedValues = getSelectedSet();
			clearSelection();
			configedMain.refreshClientListKeepingGroup();
			configedMain.setClients(selectedValues);
		});
	}

	public void removeClientFromTable(String clientId) {
		if (!persistenceController.getHostInfoCollections().getOpsiHostNames().contains(clientId)
				|| ConfigedMain.getMainFrame().getClientConfiguration().getSelectedIndex() != 0) {
			return;
		}

		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		SwingUtilities.invokeLater(configedMain::refreshClientListKeepingGroup);
	}
}
