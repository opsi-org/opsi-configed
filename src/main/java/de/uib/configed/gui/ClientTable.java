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
import de.uib.configed.type.HostInfo;
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
			int col = persistenceController.getHostDataService().getHostDisplayFields()
					.get(HostInfo.CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL) ? 1 : 0;
			result.add((String) getValueAt(i, col));
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
		TableRowSorter<?> rowSorter = (TableRowSorter<?>) getRowSorter();
		// if table has more than 1 column, we need to sort the second column by name
		if (tableModel.getColumnCount() > 1) {
			rowSorter.setComparator(1, Comparator.comparing(String::toString));
		}
		rowSorter.setComparator(0, stringComparatorIgnoringNullEmpty());
	}

	/**
	 * Returns a comparator that sorts string values while always placing null
	 * or empty strings at the bottom regardless of sort direction.
	 *
	 * @param ascending if true, sorts in natural order; if false, in reverse
	 *                  order.
	 */
	@SuppressWarnings("unchecked")
	private Comparator<Object> stringComparatorIgnoringNullEmpty() {
		return (Object o1, Object o2) -> {
			boolean isO1Invalid = (o1 == null || o1.toString().trim().isEmpty());
			boolean isO2Invalid = (o2 == null || o2.toString().trim().isEmpty());

			if (isO1Invalid && isO2Invalid) {
				return 0;
			}

			boolean isAscending = ((TableRowSorter<?>) getRowSorter()).getSortKeys().get(0)
					.getSortOrder() == SortOrder.ASCENDING;

			if (isO1Invalid) {
				return isAscending ? 1 : -1;
			}
			if (isO2Invalid) {
				return isAscending ? -1 : 1;
			}
			return ((Comparable<Object>) o1).compareTo(o2);
		};
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
