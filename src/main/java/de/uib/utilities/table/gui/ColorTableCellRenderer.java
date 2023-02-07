package de.uib.utilities.table.gui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.utilities.swing.CellAlternatingColorizer;

public class ColorTableCellRenderer extends DefaultTableCellRenderer {
	public ColorTableCellRenderer() {
		super();
	}

	@Override
	public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		java.awt.Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		CellAlternatingColorizer.colorize(cell, isSelected, (row % 2 == 0), true);

		return cell;

	}
}
