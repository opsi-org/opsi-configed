/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

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

	void applyFilter(String query, int columnIndex, boolean useRegex, boolean caseSensitive);

	int getListSelectionMode();
}
