/*
 * SearchTargetModel.java
 *
 * By uib, www.uib.de, 2017
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.gui;

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

	boolean isFiltered();

	int getListSelectionMode();
}
