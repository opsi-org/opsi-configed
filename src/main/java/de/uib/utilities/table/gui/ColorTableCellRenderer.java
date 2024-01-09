/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.Globals;

public class ColorTableCellRenderer extends DefaultTableCellRenderer {
	public ColorTableCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		colorize(cell, isSelected, row % 2 == 0, column % 2 == 0);
		return cell;
	}

	public static void colorize(Component cell, boolean isSelected, boolean isRowEven) {
		if (FlatLaf.isLafDark()) {
			cell.setForeground(Globals.OPSI_FOREGROUND_DARK);
		} else {
			cell.setForeground(Globals.OPSI_FOREGROUND_LIGHT);
		}

		colorizeTableCell(cell, isSelected, isRowEven);
	}

	public static void colorize(Component cell, boolean isSelected, boolean rowEven, boolean colEven) {
		ColorTableCellRenderer.colorize(cell, isSelected, rowEven);
		makeCellDarker(cell, colEven);
	}

	private static void makeCellDarker(Component cell, boolean shouldMakeDarker) {
		if (shouldMakeDarker) {
			Color backgroudColor = cell.getBackground();

			Color newBackgroundColor = new Color(backgroudColor.getRed() - 8, backgroudColor.getGreen() - 8,
					backgroudColor.getBlue() - 8);

			cell.setBackground(newBackgroundColor);
		}
	}

	private static void colorizeTableCell(Component component, boolean isSelected, boolean isRowEven) {
		if (!isSelected) {
			if (isRowEven) {
				component.setBackground(Globals.getGreyCell2());
			} else {
				component.setBackground(Globals.getGreyCell1());
			}
		} else {
			if (isRowEven) {
				component.setBackground(Globals.getMagentaCell2());
			} else {
				component.setBackground(Globals.getMagentaCell1());
			}
		}
	}
}
