/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.gui.Globals;

public class ColorTableCellRenderer extends DefaultTableCellRenderer {
	public ColorTableCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		colorize(cell, isSelected, row, column);
		return cell;
	}

	public static void colorize(Component cell, boolean isSelected, int row) {
		cell.setForeground(Globals.getForegroundColor());

		colorizeTableCell(cell, isSelected, row % 2 == 0);
	}

	public static void colorize(Component cell, boolean isSelected, int row, int column) {
		colorize(cell, isSelected, row);
		if (column % 2 == 0) {
			makeCellDarker(cell);
		}
	}

	private static void makeCellDarker(Component cell) {
		Color backgroudColor = cell.getBackground();

		Color newBackgroundColor = new Color(backgroudColor.getRed() - 8, backgroudColor.getGreen() - 8,
				backgroudColor.getBlue() - 8);

		cell.setBackground(newBackgroundColor);
	}

	private static void colorizeTableCell(Component component, boolean isSelected, boolean isRowEven) {
		if (!isSelected) {
			component.setBackground(isRowEven ? Globals.getGreyCell2() : Globals.getGreyCell1());
		} else {
			component.setBackground(isRowEven ? Globals.getMagentaCell2() : Globals.getMagentaCell1());
		}
	}
}
