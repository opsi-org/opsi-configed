package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import de.uib.configed.Globals;

public class XCellEditor extends DefaultCellEditor {

	public XCellEditor(JTextField textfield) {
		super(textfield);
	}

	public XCellEditor(JComboBox<?> combo) {
		super(combo);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);

		Color background;
		Color foreground;

		if (isSelected) {
			background = Globals.nimbusSelectionBackground;
			foreground = Globals.X_CELL_EDITOR_SELECTED_FOREGROUND;
		} else {
			background = Globals.nimbusBackground;
			foreground = Globals.X_CELL_EDITOR_NOT_SELECTED_FOREGROUND;
		}

		c.setBackground(background);
		c.setForeground(foreground);

		return c;
	}

}
