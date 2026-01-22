/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.data;

import java.awt.Component;

import javax.swing.JTable;

import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;

public class ColoredTableCellRenderer extends ColorTableCellRenderer {
	public ColoredTableCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (value != null && value.equals(Globals.CONFLICT_STATE_STRING)) {
			setForeground(Globals.PRODUCT_STATUS_MIXED_COLOR);
		}

		return this;
	}
}
