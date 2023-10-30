/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

public class ExporterToTerminal extends AbstractExportTable {
	public ExporterToTerminal(JTable table) {
		super(table, null);
	}

	@SuppressWarnings("java:S106")
	@Override
	public void execute(String fileName, boolean onlySelectedRows) {
		Boolean selectedOnly = checkSelection(onlySelectedRows);
		if (selectedOnly == null) {
			return;
		}

		for (int row = 0; row < theTable.getRowCount(); row++) {
			if (Boolean.TRUE.equals(!selectedOnly) || theTable.isRowSelected(row)) {
				printRow(row);
			}
		}
	}

	private void printRow(int row) {
		List<String> rowList = new ArrayList<>();
		for (int col = 0; col < theTable.getColumnCount(); col++) {
			if (theTable.getValueAt(row, col) != null && ((classNames == null || classNames.isEmpty())
					&& theTable.getValueAt(row, col) instanceof String)) {
				rowList.add((String) theTable.getValueAt(row, col));
			}
		}

		for (int i = 0; i < rowList.size(); i++) {
			if (!" ".equals(rowList.get(i))) {
				System.out.print(rowList.get(i));
				if (i != rowList.size() - 1) {
					System.out.print(", ");
				}
			}
		}
	}
}
