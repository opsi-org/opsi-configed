/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

public class GenericTableModel extends AbstractTableModel {
	private final GenericTableViewModel tableModel;
	private final Consumer<GenericTableViewMsg> dispatcher;
	private final Function<Integer, Boolean> isCellEditable;

	public GenericTableModel(GenericTableViewModel model, Consumer<GenericTableViewMsg> dispatcher,
			Function<Integer, Boolean> isCellEditable) {
		this.tableModel = model;
		this.dispatcher = dispatcher;
		this.isCellEditable = isCellEditable;
	}

	@Override
	public int getColumnCount() {
		return (int) tableModel.getColumns().stream().filter(TableColumnConfig::isVisible).count();
	}

	@Override
	public String getColumnName(int column) {
		TableColumnConfig config = tableModel.getColumnByModelIndex(column);
		return config != null ? config.getHeader() : null;
	}

	@Override
	public int getRowCount() {
		return tableModel.getRows().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex >= tableModel.getRows().size()) {
			return null;
		}

		TableColumnConfig config = tableModel.getColumnByModelIndex(columnIndex);
		String logicalKey = config.getKey();

		RowData rowData = tableModel.getRows().get(rowIndex);

		return rowData.getValue(logicalKey, Object.class);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return tableModel.getColumnByModelIndex(col).isEditable()
				&& (isCellEditable == null || isCellEditable.apply(row));
	}

	@Override
	public void setValueAt(Object newValue, int row, int col) {
		dispatcher.accept(new GenericTableViewMsg.CellEdited(row, col, newValue));
	}
}
