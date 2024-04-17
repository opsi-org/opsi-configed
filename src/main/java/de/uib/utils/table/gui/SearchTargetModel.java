/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

public interface SearchTargetModel {
	String getColumnName(int col);

	int findColumn(String name);

	int getColumnCount();

	int getRowCount();

	Object getValueAt(int row, int col);

	int getColForVisualCol(int visualCol);

	int getRowForVisualRow(int visualRow);

	void clearSelection();

	int getSelectedRow();

	int[] getSelectedRows();

	void ensureRowIsVisible(int row);

	void setCursorRow(int row);

	void setSelectedRow(int row);

	void addSelectedRow(int row);

	int[] getUnfilteredSelection();

	void setSelection(int[] selection);

	void setValueIsAdjusting(boolean b);

	void setFiltered(boolean b);

	int getListSelectionMode();
}
