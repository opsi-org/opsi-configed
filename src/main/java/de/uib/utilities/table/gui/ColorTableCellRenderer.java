/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.utilities.swing.CellAlternatingColorizer;

public class ColorTableCellRenderer extends DefaultTableCellRenderer {
	public ColorTableCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		CellAlternatingColorizer.colorize(cell, isSelected, row % 2 == 0, column % 2 == 0, true);

		return cell;

	}
}
