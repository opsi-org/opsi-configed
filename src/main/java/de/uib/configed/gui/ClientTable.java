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
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.ColorTableCellRenderer;

public class ClientTable extends JTable {
	private List<SortKey> primaryOrderingKeys;

	public ClientTable() {
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

	public List<String> getSelectedList() {
		long start = System.nanoTime();
		List<String> valuesList = new ArrayList<>(getSelectedRowCount());

		for (int i : getSelectedRows()) {
			valuesList.add((String) getValueAt(i, 0));
		}

		Logging.devel("", System.nanoTime() - start);
		return valuesList;
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
}
