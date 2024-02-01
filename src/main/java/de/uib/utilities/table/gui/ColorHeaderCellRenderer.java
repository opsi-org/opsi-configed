/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ColorHeaderCellRenderer implements TableCellRenderer {
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
		return rend.getTableCellRendererComponent(table, modifyValue(value), isSelected, hasFocus, row, column);
	}
}
