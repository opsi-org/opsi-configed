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

	private static final Color selectedEven = Globals.defaultTableSelectedRowDark;
	private static final Color selectedUneven = Globals.defaultTableSelectedRowBright;
	private static final Color unselectedEven = Globals.defaultTableCellBgColor2;
	private static final Color unselectedUneven = Globals.defaultTableCellBgColor1;

	// private constructor to hide the implicit public one
	private CellAlternatingColorizer() {
	}

	public static void colorize(Component cell, boolean isSelected, boolean isEven, boolean textColoring) {
		colorize(cell, isSelected, isEven, textColoring, selectedEven, selectedUneven, unselectedEven,
				unselectedUneven);
	}

	public static void colorize(Component cell, boolean isSelected, boolean isEven, boolean textColoring,
			Color selectedEvenColor, Color selectedUnevenColor, Color unselectedEvenColor,
			Color unselectedUnevenColor) {
		if (textColoring && !Main.THEMES) {
			cell.setForeground(Globals.lightBlack);
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
			cell.setBackground(Globals.defaultTableSelectedRowBright);
			cell.setForeground(Globals.defaultTableSelectedRowBright);
		}
	}

	public static void colorize(Component cell, boolean isSelected, boolean rowEven, boolean colEven,
			boolean textColoring) {

		if (!Main.THEMES) {
			if (textColoring) {
				cell.setForeground(Globals.lightBlack);
			}

			if (isSelected) {
				if (rowEven) {
					cell.setBackground(Globals.defaultTableSelectedRowDark);
				} else {
					cell.setBackground(Globals.defaultTableSelectedRowBright);
				}
			} else {
				if (rowEven && colEven) {
					cell.setBackground(Globals.defaultTableCellBgColor00);
				} else if (rowEven) {
					cell.setBackground(Globals.defaultTableCellBgColor01);
				} else if (colEven) {
					cell.setBackground(Globals.defaultTableCellBgColor10);
				} else {
					cell.setBackground(Globals.defaultTableCellBgColor11);
				}
			}
		} else {
			StandardTableCellRenderer.colorizeTableCellTheme(cell, isSelected, rowEven);
		}
	}
}
