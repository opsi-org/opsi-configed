/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.uib.configed.gui.productpage.PanelGroupedProductSettings;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModel;

public class SearchTargetModelFromInstallationStateTable implements SearchTargetModel {

	private JTable table;

	private int[] viewRowfilter = new int[0];

	private PanelGroupedProductSettings panelProductSettings;

	private boolean filtered;

	public SearchTargetModelFromInstallationStateTable(JTable table, PanelGroupedProductSettings panelProductSettings) {

		this.table = table;
		Logging.info(this.getClass(), "table null? " + (table == null));

		this.panelProductSettings = panelProductSettings;
	}

	private AbstractTableModel getTableModel() {
		return (AbstractTableModel) table.getModel();
	}

	@Override
	public String getColumnName(int col) {
		return getTableModel().getColumnName(col);
	}

	@Override
	public int findColumn(String name) {
		return getTableModel().findColumn(name);
	}

	@Override
	public int getColumnCount() {

		return getTableModel().getColumnCount();
	}

	@Override
	public int getRowCount() {
		return getTableModel().getRowCount();
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getTableModel().getValueAt(row, col);
	}

	@Override
	public int getColForVisualCol(int visualCol) {
		return table.convertColumnIndexToModel(visualCol);
	}

	@Override
	public int getRowForVisualRow(int visualRow) {
		return table.convertRowIndexToModel(visualRow);
	}

	@Override
	public void clearSelection() {
		table.clearSelection();
	}

	@Override
	public int getSelectedRow() {
		return table.getSelectedRow();
	}

	@Override
	public int[] getSelectedRows() {
		return table.getSelectedRows();
	}

	@Override
	public void ensureRowIsVisible(int row) {

		table.scrollRectToVisible(table.getCellRect(row, 0, false));

	}

	@Override
	public void setCursorRow(int row) {
		Logging.debug(this, "setCursorRow row, produced modelrow, produced viewrow, not implemented ");
	}

	@Override
	public void setSelectedRow(int row) {
		if (table.getRowCount() == 0) {
			return;
		}

		if (row == -1) {
			table.clearSelection();
			return;
		}

		table.setRowSelectionInterval(row, row);

		ensureRowIsVisible(row);
	}

	@Override
	public void addSelectedRow(int row) {
		Logging.debug(this, "addSelectedRow " + row);

		if (table.getRowCount() == 0) {
			return;
		}

		table.addRowSelectionInterval(row, row);

		ensureRowIsVisible(row);
	}

	@Override
	public int[] getUnfilteredSelection() {
		return viewRowfilter;
	}

	@Override
	public void setValueIsAdjusting(boolean b) {
		table.getSelectionModel().setValueIsAdjusting(b);
	}

	@Override
	public void setSelection(int[] selection) {
		Logging.info(this, "setSelection --- " + Arrays.toString(selection));
		table.getSelectionModel().clearSelection();
		for (int i = 0; i < selection.length; i++) {
			table.getSelectionModel().addSelectionInterval(selection[i], selection[i]);
		}
	}

	@Override
	public void setFiltered(boolean b) {

		if (!filtered) {
			viewRowfilter = table.getSelectedRows();
		}

		if (b && viewRowfilter.length > 0) {
			int[] modelRowFilter = new int[viewRowfilter.length];
			for (int i = 0; i < viewRowfilter.length; i++) {
				modelRowFilter[i] = table.convertRowIndexToModel(viewRowfilter[i]);
			}

			Logging.info(this, "setFiltered modelRowFilter " + Arrays.toString(modelRowFilter));

			panelProductSettings.reduceToSelected();

		} else {
			panelProductSettings.showAll();
		}
		filtered = b;

	}

	@Override
	public boolean isFiltered() {
		return filtered;

	}

	@Override
	public int getListSelectionMode() {
		return table.getSelectionModel().getSelectionMode();
	}
}
