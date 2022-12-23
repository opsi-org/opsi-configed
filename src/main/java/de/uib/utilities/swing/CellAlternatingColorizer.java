package de.uib.utilities.swing;

import de.uib.configed.Globals;

public class CellAlternatingColorizer {
	static java.awt.Color selectedEven = Globals.defaultTableSelectedRowDark;
	static java.awt.Color selectedUneven = Globals.defaultTableSelectedRowBright;
	static java.awt.Color unselectedEven = Globals.defaultTableCellBgColor2;
	static java.awt.Color unselectedUneven = Globals.defaultTableCellBgColor1;

	public static void colorize(java.awt.Component cell, boolean isSelected, boolean isEven, boolean textColoring) {
		colorize(cell, isSelected, isEven, textColoring, selectedEven, selectedUneven, unselectedEven,
				unselectedUneven);
	}

	public static void colorize(java.awt.Component cell, boolean isSelected, boolean isEven, boolean textColoring,
			java.awt.Color selectedEvenColor, java.awt.Color selectedUnevenColor, java.awt.Color unselectedEvenColor,
			java.awt.Color unselectedUnevenColor) {
		if (textColoring)
			cell.setForeground(Globals.lightBlack);

		/*
		 * if (isSelected)
		 * {
		 * cell.setBackground( Globals.defaultTableSelectedRowDark );
		 * }
		 */
		
		

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
			} else if (rowEven && !colEven) // 0,1
			{
				cell.setBackground(Globals.defaultTableCellBgColor01);
			} else if (!rowEven && colEven) // 1,0
			{
				cell.setBackground(Globals.defaultTableCellBgColor10);
			} else {
				cell.setBackground(Globals.defaultTableCellBgColor11);
			}
		}

	}
}
