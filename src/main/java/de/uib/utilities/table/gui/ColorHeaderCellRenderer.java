/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.uib.Main;
import de.uib.configed.Globals;

public class ColorHeaderCellRenderer extends DefaultTableCellRenderer {
	private TableCellRenderer rend;

	public ColorHeaderCellRenderer(TableCellRenderer rend) {
		this.rend = rend;
	}

	// to override in subclasses for manipulation the value
	protected Object modifyValue(Object value) {
		return value;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cell = rend.getTableCellRendererComponent(table, modifyValue(value), isSelected, hasFocus, row,
				column);
		if (!Main.THEMES) {
			cell.setBackground(Globals.defaultTableHeaderBgColor);
		}

		if (cell instanceof JComponent) {
			JComponent jc = (JComponent) cell;

			if (value != null) {
				String val1 = "" + modifyValue(value);
				jc.setToolTipText(val1);
			}

		}

		return cell;
	}
}
