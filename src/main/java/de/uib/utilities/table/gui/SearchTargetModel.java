/*
 * SearchTargetModel.java
 *
 * By uib, www.uib.de, 2017
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.gui;

public interface SearchTargetModel {

	public String getColumnName(int col);

	public int findColumn(String name);

	public int getColumnCount();

	public int getRowCount();

	public Object getValueAt(int row, int col);

	public int getColForVisualCol(int visualCol);

	public int getRowForVisualRow(int visualRow);

	public void setRenderAsCurrentRow(int currentRow);

	public void clearSelection();

	public int getSelectedRow();

	public int[] getSelectedRows();

	public void ensureRowIsVisible(int row);

	public void setCursorRow(int row);

	public void setSelectedRow(int row);

	public void addSelectedRow(int row);

	public int[] getUnfilteredSelection();

	public void setSelection(int[] selection);

	public void setValueIsAdjusting(boolean b);

	public void setFiltered(boolean b);

	public boolean isFiltered();

	public int getListSelectionMode();

}
