package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;

import de.uib.configed.Globals;

public final class CellAlternatingColorizer {
	// private constructor to hide the implicit public one
	private CellAlternatingColorizer() {
	}

	static Color selectedEven = Globals.defaultTableSelectedRowDark;
	static Color selectedUneven = Globals.defaultTableSelectedRowBright;
	static Color unselectedEven = Globals.defaultTableCellBgColor2;
	static Color unselectedUneven = Globals.defaultTableCellBgColor1;

	public static void colorize(Component cell, boolean isSelected, boolean isEven, boolean textColoring) {
		colorize(cell, isSelected, isEven, textColoring, selectedEven, selectedUneven, unselectedEven,
				unselectedUneven);
	}

	public static void colorize(Component cell, boolean isSelected, boolean isEven, boolean textColoring,
			Color selectedEvenColor, Color selectedUnevenColor, Color unselectedEvenColor,
			Color unselectedUnevenColor) {
		if (textColoring)
			cell.setForeground(Globals.lightBlack);

		if (isSelected) {
			if (isEven)
				cell.setBackground(selectedEvenColor);
			else
				cell.setBackground(selectedUnevenColor);
		}

		else {
			if (isEven) {
				cell.setBackground(unselectedEvenColor);
			} else {
				cell.setBackground(unselectedUnevenColor);
			}
		}

	}

	public static void colorizeSecret(java.awt.Component cell) {
		cell.setBackground(Globals.defaultTableSelectedRowBright);
		cell.setForeground(Globals.defaultTableSelectedRowBright);
	}

	public static void colorize(java.awt.Component cell, boolean isSelected, boolean rowEven, boolean colEven,
			boolean textColoring) {

		if (textColoring)
			cell.setForeground(Globals.lightBlack);

		if (isSelected) {
			if (rowEven)
				cell.setBackground(Globals.defaultTableSelectedRowDark);
			else
				cell.setBackground(Globals.defaultTableSelectedRowBright);

		}

		else {
			if (rowEven && colEven) // 0,0
			{
				cell.setBackground(Globals.defaultTableCellBgColor00);
			} else if (rowEven) // 0,1
			{
				cell.setBackground(Globals.defaultTableCellBgColor01);
			} else if (colEven) // 1,0
			{
				cell.setBackground(Globals.defaultTableCellBgColor10);
			} else { // 1,1
				cell.setBackground(Globals.defaultTableCellBgColor11);
			}
		}
	}
}
