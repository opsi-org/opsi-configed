/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/**
	private ColoredTableCellRendererByIndex.java
	
	utility class for PanelProductSettings
	
*/

package de.uib.configed.guidata;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.JTable;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.utilities.table.gui.TableCellRendererByIndex;

public class ColoredTableCellRendererByIndex extends TableCellRendererByIndex {

	private Map<String, Color> mapOfTextColors;

	public ColoredTableCellRendererByIndex(Map<String, String> mapOfStringValues, String tooltipPrefix) {
		this(null, mapOfStringValues, tooltipPrefix);
	}

	public ColoredTableCellRendererByIndex(Map<String, Color> mapOfTextColors, Map<String, String> mapOfStringValues,
			String tooltipPrefix) {
		super(mapOfStringValues, tooltipPrefix);
		this.mapOfTextColors = mapOfTextColors;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (value == null) {
			return c;
		}

		if (value.equals(Globals.CONFLICT_STATE_STRING)) {
			if (!Main.THEMES) {
				c.setBackground(Globals.BACKGROUND_COLOR_4);
				c.setForeground(Globals.BACKGROUND_COLOR_4);
			} else {
				c.setForeground(Globals.PRODUCT_STATUS_MIXED_COLOR);
			}
		} else {
			if (mapOfTextColors != null && value instanceof String) {
				Color textcolor = mapOfTextColors.get(value);
				if (textcolor != null) {
					if (textcolor.equals(Globals.INVISIBLE)) {
						c.setForeground(c.getBackground());
					} else {
						c.setForeground(textcolor);
					}
				}
			}
		}

		return c;
	}
}
