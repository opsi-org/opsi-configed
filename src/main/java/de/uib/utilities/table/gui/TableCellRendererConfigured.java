/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;

import de.uib.Main;

public class TableCellRendererConfigured extends ColorTableCellRenderer {
	private Font f;
	private Color color;
	private Color bg1;
	private Color bg2;
	private Color selectionEditingBackground;

	public TableCellRendererConfigured(Font f, Color c, Color bg1, Color bg2, Color selectionEditingBackground) {
		super();
		this.f = f;
		this.color = c;
		this.bg1 = bg1;
		if (bg2 != null) {
			this.bg2 = bg2;
		} else {
			this.bg2 = bg1;
		}

		this.selectionEditingBackground = selectionEditingBackground;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (bg1 != null && !Main.THEMES) {
			if (selectionEditingBackground != null && isSelected) {
				result.setBackground(selectionEditingBackground);
			} else {
				if (row % 2 == 0) {
					result.setBackground(bg2);
				} else {
					result.setBackground(bg1);
				}
			}
		}

		if (!Main.THEMES && color != null) {
			result.setForeground(color);
		}

		return result;
	}
}
