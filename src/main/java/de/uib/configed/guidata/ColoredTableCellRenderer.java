/**
	private ColoredTableCellRenderer.java
	
	utility class for PanelProductSettings
	
*/

package de.uib.configed.guidata;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;

import de.uib.Main;
import de.uib.configed.Globals;

public class ColoredTableCellRenderer extends de.uib.utilities.table.gui.StandardTableCellRenderer {

	public ColoredTableCellRenderer(String tooltipPrefix) {
		super(tooltipPrefix);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (value != null && value.equals(InstallationStateTableModel.CONFLICT_STRING)) {
			if (!Main.THEMES) {
				c.setBackground(Globals.BACKGROUND_COLOR_4);
				c.setForeground(Globals.BACKGROUND_COLOR_4);
			} else {
				c.setBackground(Color.PINK);
				c.setForeground(Color.PINK);
			}
		}

		return c;
	}
}
