package de.uib.configed.gui.helper;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2010 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import java.awt.Component;

import javax.swing.JTable;

import de.uib.configed.ConfigedMain;
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

	protected void mergeColorize(Component comp, Object value) {

		if (value == ListMerger.NO_COMMON_VALUE) {

			if (!ConfigedMain.OPSI_4_3) {
				comp.setBackground(Globals.LIST_MERGER_NO_COMMON_VALUE_BACKGROUND_COLOR);
				comp.setForeground(Globals.LIST_MERGER_NO_COMMON_VALUE_TEXT_COLOR);
			}
		}
	}
}
