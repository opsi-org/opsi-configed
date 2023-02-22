package de.uib.utilities.table.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableCellRendererConfigured extends DefaultTableCellRenderer {
	Font f;
	Color color;
	Color bg1;
	Color bg2; // for even line numbers
	Color selectionBackground;
	Color selectionEditingBackground;

	public TableCellRendererConfigured(Font f, Color c, Color bg1, Color bg2, Color selectionBackground,
			Color selectionEditingBackground) {
		super();
		this.f = f;
		this.color = c;
		this.bg1 = bg1;
		if (bg2 != null) {
			this.bg2 = bg2;
		} else {
			this.bg2 = bg1;
		}

		this.selectionBackground = selectionBackground;
		this.selectionEditingBackground = selectionEditingBackground;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, // value to display
			boolean isSelected, // is the cell selected
			boolean hasFocus, int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (f != null) {
			result.setFont(f);
		}

		if (bg1 != null) {
			if (selectionEditingBackground != null && isSelected) {
				result.setBackground(selectionEditingBackground);
			} else {
				if (row % 2 == 0) {
					result.setBackground(bg2);
				} else {
					result.setBackground(bg1);
				}
			}
		}

		if (color != null) {
			result.setForeground(color);
		}

		return result;
	}

}
