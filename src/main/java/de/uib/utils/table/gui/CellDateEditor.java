/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Component;
import java.time.LocalDate;

import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.ConfigedMain;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.StackPane;

public class CellDateEditor extends DefaultCellEditor {
	private String currentString;

	public CellDateEditor() {
		super(new JTextField());

		((JTextField) super.getComponent()).setEditable(false);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
		String oldValue = (String) value;

		JFXPanel jfxPanel = new JFXPanel();
		DatePicker datePicker = createDatePicker(oldValue, jfxPanel);

		// show the date picker in a dialog, so
		// the user can select a date or cancel the dialog
		SwingUtilities.invokeLater(() -> showEditor(table, c, column, jfxPanel, datePicker));

		return c;
	}

	private void showEditor(JTable table, Component c, int column, JFXPanel jfxPanel, DatePicker datePicker) {
		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), jfxPanel, table.getColumnName(column),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		// answer == 0 means OK
		// if the value is not null, the user did not select a date
		if (answer == 0 && datePicker.getValue() != null) {
			currentString = datePicker.getValue().toString();
			((JTextField) c).setText(currentString);
			stopCellEditing();
		} else {
			cancelCellEditing();
		}
	}

	public static DatePicker createDatePicker(String oldValue, JFXPanel jfxPanel) {
		DatePicker datePicker = new DatePicker();
		if (oldValue != null && !oldValue.isBlank()) {
			datePicker.setValue(LocalDate.parse(oldValue));
		}

		DatePickerSkin skin = new DatePickerSkin(datePicker);
		StackPane pane = new StackPane(skin.getPopupContent());
		Scene scene = new Scene(pane);
		if (FlatLaf.isLafDark()) {
			scene.getStylesheets().add(CellDateEditor.class.getResource("/css/date-picker-dark.css").toExternalForm());
		} else {
			scene.getStylesheets().add(CellDateEditor.class.getResource("/css/date-picker-light.css").toExternalForm());
		}
		jfxPanel.setScene(scene);

		return datePicker;
	}

	@Override
	public Object getCellEditorValue() {
		return currentString;
	}
}
