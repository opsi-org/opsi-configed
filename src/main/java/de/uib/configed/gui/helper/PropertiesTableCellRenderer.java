/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.helper;

import java.awt.Component;

import javax.swing.JTable;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.configed.guidata.ListMerger;
import de.uib.utilities.table.gui.ColorTableCellRenderer;

public class PropertiesTableCellRenderer extends ColorTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Object sValue = formatList(value);
		Component result = super.getTableCellRendererComponent(table, sValue, isSelected, hasFocus, row, column);

		if (column == 1) {
			mergeColorize(result, value);
		}

		return result;
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

	private static void mergeColorize(Component comp, Object value) {

		if (value == ListMerger.NO_COMMON_VALUE && !Main.THEMES) {
			comp.setBackground(Globals.LIST_MERGER_NO_COMMON_VALUE_BACKGROUND_COLOR);
			comp.setForeground(Globals.LIST_MERGER_NO_COMMON_VALUE_TEXT_COLOR);
		}
	}
}
