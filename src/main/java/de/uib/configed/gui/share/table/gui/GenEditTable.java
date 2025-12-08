/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultRowSorter;
import javax.swing.DropMode;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.share.logging.Logging;

public class GenEditTable extends JTable implements KeyListener {
	private boolean deleteAllowed = true;
	private Map<Integer, SortOrder> sortDescriptor;

	public GenEditTable() {
		super.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		super.getTableHeader().setReorderingAllowed(false);
		super.addKeyListener(this);
		super.setDragEnabled(true);
		super.setDropMode(DropMode.ON);
		super.setAutoCreateRowSorter(false);

		// ComponentListener for table
		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				showSelectedRow();
			}
		});
	}

	public void deleteRelation() {
		if (getSelectedRowCount() == 0) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("PanelGenEditTable.noRowSelected"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
		} else if (isDeleteAllowed()) {
			getGenTableModel().deleteRow(getSelectedRowInModelTerms());
		} else {
			Logging.warning(this, "nothing to delete, since nothing selected or deleting not allowed");
		}
	}

	public void setSortDescriptor(Map<Integer, SortOrder> sortDescriptor) {
		this.sortDescriptor = sortDescriptor;
	}

	public GenTableModel getGenTableModel() {
		TableModel model = super.getModel();
		if (model instanceof GenTableModel genTableModel) {
			return genTableModel;
		} else {
			// This is not a problem, the model has just not been set yet
			Logging.info(this, "getModel: Model is not a GenTableModel, but ", model.getClass().getName());
			return null;
		}
	}

	public void setSorter() {
		Logging.info(this, "setSorter");

		if (getGenTableModel() == null) {
			Logging.warning(this, "setSorter: Model is not a GenTableModel, cannot set sorter");
			return;
		}

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(getModel());

		List<SortKey> sortKeys = buildSortkeysFromColumns();

		if (sortKeys != null && !sortKeys.isEmpty()) {
			sorter.setSortKeys(sortKeys);
		}

		setRowSorter(sorter);
	}

	public void sortAgainAsConfigured() {
		if (sortDescriptor != null && !sortDescriptor.isEmpty()) {
			int selRow = getSelectedRow();

			Object selVal = null;
			if (selRow > -1 && getGenTableModel().getKeyCol() > -1) {
				selVal = getValueAt(convertRowIndexToModel(selRow), getGenTableModel().getKeyCol());
			}

			((DefaultRowSorter<?, ?>) getRowSorter()).sort();
			setSorter();

			if (selVal != null) {
				int viewRow = findViewRowFromValue(selVal, getGenTableModel().getKeyCol());
				moveToRow(viewRow);
				setSelectedRow(viewRow);
			}
		}
	}

	public void setSelectedRow(int row) {
		setRowSelectionInterval(row, row);

		showSelectedRow();
	}

	public void showSelectedRow() {
		int row = getSelectedRow();
		if (row != -1) {
			scrollRectToVisible(getCellRect(row, 0, false));
		}
	}

	public int findViewRowFromValue(Object value, int col) {
		Logging.debug(this, "findViewRowFromValue value, col ", value, ", ", col);

		if (value == null) {
			return -1;
		}

		String val = value.toString();

		for (int viewrow = 0; viewrow < getRowCount(); viewrow++) {
			Object compareValue = getValueAt(viewrow, col);

			if ((compareValue == null && val.isEmpty())
					|| (compareValue != null && val.equals(compareValue.toString()))) {
				return viewrow;
			}
		}

		return -1;
	}

	public void moveToRow(int n) {
		if (getRowCount() == 0) {
			return;
		}

		if (getSelectedRowCount() != 1) {
			return;
		}

		if (n < 0 || n >= getRowCount()) {
			return;
		}

		scrollRectToVisible(getCellRect(n, 0, true));
		setRowSelectionInterval(n, n);
		getGenTableModel().setCursorRow(convertRowIndexToModel(n));
	}

	private List<SortKey> buildSortkeysFromColumns() {
		Logging.debug(this, "buildSortkeysFromColumns,  sortDescriptor ", sortDescriptor);
		List<SortKey> sortKeys = new ArrayList<>();

		if (getColumnCount() == 0) {
			return new ArrayList<>();
		} else if (sortDescriptor == null) {
			// default sorting
			sortDescriptor = new LinkedHashMap<>();

			if (getGenTableModel().getKeyCol() > -1) {
				sortKeys.add(new SortKey(getGenTableModel().getKeyCol(), SortOrder.ASCENDING));

				sortDescriptor.put(getGenTableModel().getKeyCol(), SortOrder.ASCENDING);
			} else if (getGenTableModel().getFinalCols() != null && !getGenTableModel().getFinalCols().isEmpty()) {
				for (Integer col : getGenTableModel().getFinalCols()) {
					sortKeys.add(new SortKey(col, SortOrder.ASCENDING));

					sortDescriptor.put(col, SortOrder.ASCENDING);
				}
			} else {
				sortKeys = null;
			}
		} else {
			for (Entry<Integer, SortOrder> entry : sortDescriptor.entrySet()) {
				sortKeys.add(new SortKey(entry.getKey(), entry.getValue()));
			}
		}

		return sortKeys;
	}

	// KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == this && e.getKeyCode() == KeyEvent.VK_DELETE && deleteAllowed) {
			deleteSelectedRow();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	public void setDeleteAllowed(boolean deleteAllowed) {
		this.deleteAllowed = deleteAllowed;
	}

	public boolean isDeleteAllowed() {
		return deleteAllowed;
	}

	public int getSelectedRowInModelTerms() {
		return convertRowIndexToModel(getSelectedRow());
	}

	public void deleteSelectedRow() {
		if (getSelectedRowCount() > 0) {
			getGenTableModel().deleteRow(getSelectedRowInModelTerms());
		}
	}
}
