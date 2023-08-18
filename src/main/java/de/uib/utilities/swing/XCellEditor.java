/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import de.uib.Main;
import de.uib.configed.Globals;

public class XCellEditor extends DefaultCellEditor {

	public XCellEditor(JComboBox<?> combo) {
		super(combo);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);

		Color background;
		Color foreground;

		if (isSelected) {
			background = Globals.NIMBUS_SELECTION_BACKGROUND;
			foreground = Globals.X_CELL_EDITOR_SELECTED_FOREGROUND;
		} else {
			background = Globals.NIMBUS_BACKGROUND;
			foreground = Globals.X_CELL_EDITOR_NOT_SELECTED_FOREGROUND;
		}

		if (!Main.THEMES) {
			c.setBackground(background);
			c.setForeground(foreground);
		}

		return c;
	}
}
