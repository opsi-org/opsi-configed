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

	public XCellEditor(JComboBox combo) {
		super(combo);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);

		Color background;
		Color foreground;

		if (isSelected) {
			background = Globals.nimbusSelectionBackground;
			foreground = Color.WHITE;
		} else {
			background = Globals.nimbusBackground;
			foreground = Color.black;
		} ;

		c.setBackground(background);
		c.setForeground(foreground);

		// logging.debug("XCellEditor active");
		return c;
	}

}
