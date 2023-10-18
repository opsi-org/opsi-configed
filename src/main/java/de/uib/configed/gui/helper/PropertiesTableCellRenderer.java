/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.helper;

import java.awt.Component;

import javax.swing.JTable;

import de.uib.utilities.table.gui.ColorTableCellRenderer;

public class PropertiesTableCellRenderer extends ColorTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Object formattedValue = formatList(value);

		return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
	}

	private static Object formatList(Object value) {

		Object result = value;
		if (value != null) {
			String s = value.toString();
			if (s.length() >= 2 && s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']') {
				result = s.substring(1, s.length() - 1);
			}
		}

		return result;
	}
}
