/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.awt.Component;

import javax.swing.JTable;

import de.uib.configed.Globals;
import de.uib.utilities.table.gui.StandardTableCellRenderer;

public class ColoredTableCellRenderer extends StandardTableCellRenderer {
	public ColoredTableCellRenderer(String tooltipPrefix) {
		super(tooltipPrefix);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (value != null && value.equals(Globals.CONFLICT_STATE_STRING)) {
			c.setForeground(Globals.PRODUCT_STATUS_MIXED_COLOR);
		}

		return c;
	}
}
