/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class HWInfoTableModel extends AbstractTableModel {
	private List<String[]> data;
	private final String[] header = { "Name", "Wert" };

	public HWInfoTableModel() {
		super();
		data = new ArrayList<>();
	}

	public void setData(List<String[]> data) {
		this.data = data;
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		return header[column];
	}

	@Override
	public Object getValueAt(int row, int col) {
		return data.get(row)[col];
	}
}
