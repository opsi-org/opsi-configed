/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DropMode;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.GenTableModel;

public class GenEditTable extends JTable implements KeyListener {
	private boolean deleteAllowed = true;

	public GenEditTable() {
		super.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		super.getTableHeader().setReorderingAllowed(false);
		super.addKeyListener(this);
		super.setDragEnabled(true);
		super.setDropMode(DropMode.ON);
		super.setAutoCreateRowSorter(false);
	}

	public void deleteRelation() {
		if (getSelectedRowCount() == 0) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("PanelGenEditTable.noRowSelected"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
		} else if (isDeleteAllowed()) {
			((GenTableModel) getModel()).deleteRow(getSelectedRowInModelTerms());
		} else {
			Logging.warning(this, "nothing to delete, since nothing selected or deleting not allowed");
		}
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
			((GenTableModel) getModel()).deleteRow(getSelectedRowInModelTerms());
		}
	}
}
