package de.uib.utilities.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.uib.configed.Globals;

public class ColoredListCellRenderer extends DefaultListCellRenderer {
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		CellAlternatingColorizer.colorize(c, isSelected, (index % 2 == 0), true);

		c.setFont(Globals.defaultFont);

		return c;
	}
}
