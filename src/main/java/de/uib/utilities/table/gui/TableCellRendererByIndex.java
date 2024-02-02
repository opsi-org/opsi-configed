/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

public class TableCellRendererByIndex extends StandardTableCellRenderer {
	private Map<String, ImageIcon> mapOfImages;

	public TableCellRendererByIndex(String tooltipPrefix) {
		super(tooltipPrefix);
		mapOfImages = new HashMap<>();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		ImageIcon selectedIcon = null;

		if (value != null && mapOfImages != null) {
			selectedIcon = mapOfImages.get("" + value);
		}

		if (result instanceof JLabel) {
			((JLabel) result).setText(value + "");
			((JLabel) result).setIcon(selectedIcon);
		}

		return result;
	}
}
