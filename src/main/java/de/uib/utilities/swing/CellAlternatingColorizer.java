/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;

import de.uib.utilities.table.gui.StandardTableCellRenderer;

// TODO check methods
public final class CellAlternatingColorizer {

	// private constructor to hide the implicit public one
	private CellAlternatingColorizer() {
	}

	public static void colorize(Component cell, boolean isSelected, boolean isEven) {

		StandardTableCellRenderer.colorizeTableCellTheme(cell, isSelected, isEven);
	}

	public static void colorize(Component cell, boolean isSelected, boolean rowEven, boolean colEven) {

		StandardTableCellRenderer.colorizeTableCellTheme(cell, isSelected, rowEven);
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
}
