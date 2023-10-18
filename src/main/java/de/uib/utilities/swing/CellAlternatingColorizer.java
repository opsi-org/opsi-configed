/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.utilities.table.gui.StandardTableCellRenderer;

public final class CellAlternatingColorizer {

	private static final Color selectedEven = Globals.DEFAULT_TABLE_SELECTION_ROW_DARK;
	private static final Color selectedUneven = Globals.DEFAULT_TABLE_SELECTED_ROW_BRIGHT;
	private static final Color unselectedEven = Globals.DEFAULT_TABLE_CELL_BG_COLOR_2;
	private static final Color unselectedUneven = Globals.DEFAULT_TABLE_CELL_BG_COLOR_1;

	// private constructor to hide the implicit public one
	private CellAlternatingColorizer() {
	}

	public static void colorize(Component cell, boolean isSelected, boolean isEven, boolean textColoring) {
		colorize(cell, isSelected, isEven, textColoring, selectedEven, selectedUneven, unselectedEven,
				unselectedUneven);
	}

	private static void colorize(Component cell, boolean isSelected, boolean isEven, boolean textColoring,
			Color selectedEvenColor, Color selectedUnevenColor, Color unselectedEvenColor,
			Color unselectedUnevenColor) {
		if (textColoring && !Main.THEMES) {
			cell.setForeground(Globals.LIGHT_BLACK);
		}

		if (!Main.THEMES) {
			if (isSelected) {
				if (isEven) {
					cell.setBackground(selectedEvenColor);
				} else {
					cell.setBackground(selectedUnevenColor);
				}
			} else {
				if (isEven) {
					cell.setBackground(unselectedEvenColor);
				} else {
					cell.setBackground(unselectedUnevenColor);
				}
			}
		} else {
			StandardTableCellRenderer.colorizeTableCellTheme(cell, isSelected, isEven);
		}
	}

	public static void colorizeSecret(Component cell) {
		if (!Main.THEMES) {
			cell.setBackground(Globals.DEFAULT_TABLE_SELECTED_ROW_BRIGHT);
			cell.setForeground(Globals.DEFAULT_TABLE_SELECTED_ROW_BRIGHT);
		}
	}

	public static void colorize(Component cell, boolean isSelected, boolean rowEven, boolean colEven,
			boolean textColoring) {

		if (!Main.THEMES) {
			if (textColoring) {
				cell.setForeground(Globals.LIGHT_BLACK);
			}

			if (isSelected) {
				if (rowEven) {
					cell.setBackground(Globals.DEFAULT_TABLE_SELECTION_ROW_DARK);
				} else {
					cell.setBackground(Globals.DEFAULT_TABLE_SELECTED_ROW_BRIGHT);
				}
			} else {
				if (rowEven && colEven) {
					cell.setBackground(Globals.DEFAULT_TABLE_CELL_BG_COLOR_00);
				} else if (rowEven) {
					cell.setBackground(Globals.DEFAULT_TABLE_CELL_BG_COLOR_01);
				} else if (colEven) {
					cell.setBackground(Globals.DEFAULT_TABLE_CELL_BG_COLOR_10);
				} else {
					cell.setBackground(Globals.DEFAULT_TABLE_CELL_GB_COLOR_11);
				}
			}
		} else {
			StandardTableCellRenderer.colorizeTableCellTheme(cell, isSelected, rowEven);
			if (colEven) {
				Color backgroudColor = cell.getBackground();

				Color newBackgroundColor = new Color(backgroudColor.getRed() - 8, backgroudColor.getGreen() - 8,
						backgroudColor.getBlue() - 8);

				cell.setBackground(newBackgroundColor);
			}
		}
	}
}
