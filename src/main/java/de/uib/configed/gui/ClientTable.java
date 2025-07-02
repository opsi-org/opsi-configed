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

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ServerActionManager;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.ColorTableCellRenderer;
import javafx.util.Pair;

public class ClientTable extends JTable implements MessagebusListener {
	private String clientNameTitle;

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

		clientNameTitle = Configed.getResourceValue("ConfigedMain.pclistTableModel.clientName");
	}

	/**
	 * Returns the default ordering for the table. Tries to get the index of the
	 * clientName column, if not found, it defaults to 0 (clientName may not be
	 * the first column)
	 * 
	 * @return
	 */
	private List<SortKey> getPrimaryOrderingKeys() {
		List<SortKey> primaryOrderingKeys = new ArrayList<>();
		// try getting index of column clientName (it might not be zero, because of new column "platform")
		int sortIndex = getColumnIndexByTitle(clientNameTitle);
		if (sortIndex == -1) {
			sortIndex = 0;
		}
		primaryOrderingKeys.add(new SortKey(sortIndex, SortOrder.ASCENDING));
		return primaryOrderingKeys;
	}

	public String getClientName(int row) {
		int col = getColumnIndexByTitle(clientNameTitle);

		return (String) getValueAt(row, col);
	}

	public Set<String> getSelectedSet() {
		Set<String> result = new HashSet<>(getSelectedRowCount());

		for (int i : getSelectedRows()) {
			result.add(getClientName(i));
		}

		return result;
	}

	/**
	 * Returns the index of the column with the given title. If no column with
	 * the given title is found, -1 is returned.
	 * 
	 * @param columnTitle
	 * @return
	 */
	private int getColumnIndexByTitle(String columnTitle) {
		try {
			return convertColumnIndexToView(getColumn(columnTitle).getModelIndex());
		} catch (IllegalArgumentException e) {
			Logging.info(this, e, "getColumnIndexByTitle: ", columnTitle, " not found");
			return -1;
		}
	}

	public void initSortKeys() {
		Logging.debug("Init Sort Keys");
		setSortKeys(getPrimaryOrderingKeys());
	}

	@SuppressWarnings("java:S1452")
	public List<? extends SortKey> getSortKeys() {
		return getRowSorter().getSortKeys();
	}

	public void setSortKeys(List<SortKey> sortKeys) {
		getRowSorter().setSortKeys(sortKeys);
	}

	/**
	 * Returns the column title and sortorder of current sort keys.
	 * 
	 * @return List of pairs of column title and sort order
	 */
	public List<Pair<String, SortOrder>> getSortedNames() {
		List<? extends SortKey> saveSortKeys = getSortKeys();
		Logging.debug(this, "getSortedNames sort keys ");
		List<Pair<String, SortOrder>> sortKeyNames = new ArrayList<>();
		for (SortKey sortKey : saveSortKeys) {
			String columnKey = getColumnName(sortKey.getColumn());
			Logging.debug("\tColumn index " + sortKey.getColumn(), " key ", columnKey);
			sortKeyNames.add(new Pair<>(columnKey, sortKey.getSortOrder()));
		}
		Logging.debug(this, "getSortedNames sort names ", sortKeyNames);
		return sortKeyNames;
	}

	/**
	 * Get list of SortKey objects (includes indexes) from list of column names
	 * 
	 * @param sortKeyNames
	 * @return
	 */
	private List<SortKey> getSortedKeysByNames(List<Pair<String, SortOrder>> sortKeyNames) {
		List<SortKey> newSortKeys = new ArrayList<>();
		for (Pair<String, SortOrder> pair : sortKeyNames) {
			int columnIndex = getColumnIndexByTitle(pair.getKey());
			if (columnIndex != -1) {
				newSortKeys.add(new SortKey(columnIndex, pair.getValue()));
			}
		}
		Logging.debug(this, "getSortedKeysByNames new sort keys ", newSortKeys);
		return newSortKeys;
	}

	/**
	 * Set the sort keys of the table by the given list of column names and sort
	 * orders.
	 * 
	 * @param sortKeyNames
	 */
	public void setSortedByNames(List<Pair<String, SortOrder>> sortKeyNames) {
		setSortKeys(getSortedKeysByNames(sortKeyNames));
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
		rowSorter.setComparator(0, this::compareStringIgnoringNull);
	}

	/**
	 * Returns a comparator that sorts string values while always placing null
	 * or empty strings at the bottom regardless of sort direction.
	 *
	 * @param ascending if true, sorts in natural order; if false, in reverse
	 *                  order.
	 */
	private int compareStringIgnoringNull(Object o1, Object o2) {
		boolean isO1Invalid = (o1 == null || o1.toString().trim().isEmpty());
		boolean isO2Invalid = (o2 == null || o2.toString().trim().isEmpty());

		if (isO1Invalid && isO2Invalid) {
			return 0;
		} else if (isO1Invalid || isO2Invalid) {
			return compareInvalids(isO1Invalid);
		} else {
			return ((Comparable<Object>) o1).compareTo(o2);
		}
	}

	private int compareInvalids(boolean isO1Invalid) {
		boolean isAscending = ((TableRowSorter<?>) getRowSorter()).getSortKeys().get(0)
				.getSortOrder() == SortOrder.ASCENDING;

		if (isO1Invalid) {
			return isAscending ? 1 : -1;
		} else {
			return isAscending ? -1 : 1;
		}
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

		if ((!WebSocketEvent.GENERAL_EVENT.toString().equals(message.get("type")) && !message.containsKey("event"))
				|| ServerActionManager.isLocalChangeInProgress()) {
			return;
		}

		String eventType = (String) message.get("event");
		if (WebSocketEvent.HOST_CREATED.toString().equals(eventType)) {
			addClientToTable();
		} else if (WebSocketEvent.HOST_DELETED.toString().equals(eventType)) {
			removeClientFromTable();
		} else {
			// Other events are handled by other listeners.
		}
	}

	public void addClientToTable() {
		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		SwingUtilities.invokeLater(() -> {
			Set<String> selectedValues = getSelectedSet();
			clearSelection();
			configedMain.refreshClientListKeepingGroup();
			configedMain.setClients(selectedValues);
		});
	}

	public void removeClientFromTable() {
		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		SwingUtilities.invokeLater(configedMain::refreshClientListKeepingGroup);
	}
}
