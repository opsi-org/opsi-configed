/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.gui.ConfigedMain;

public class CellInputDialogEditor extends DefaultCellEditor {
	private String currentString;

	public CellInputDialogEditor() {
		super(new JTextField());

		((JTextField) super.getComponent()).setEditable(false);
	}

	private void showEditor(JTable table, int column, String oldValue) {
		JTextArea inputField = new JTextArea(oldValue);
		inputField.setRows(4);

		JScrollPane scrollPane = new JScrollPane(inputField);

		// show the input dialog in a dialog, so
		// the user can enter a value or cancel the dialog
		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), scrollPane, table.getColumnName(column),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (answer == 0) {
			currentString = inputField.getText();
			stopCellEditing();
		} else {
			cancelCellEditing();
		}
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		String oldValue = (String) value;

		// show the date picker in a dialog, so
		// the user can select a date or cancel the dialog
		SwingUtilities.invokeLater(() -> showEditor(table, column, oldValue));

		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	@Override
	public Object getCellEditorValue() {
		return currentString;
	}
}
