/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.awt.Component;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import de.uib.configed.Globals;
import de.uib.utilities.table.gui.ColorTableCellRenderer;

public class TableCellRendererDate extends ColorTableCellRenderer {

	private JLabel label = new JLabel();
	private java.text.DateFormat dateFormat;

	public TableCellRendererDate() {
		dateFormat = java.text.DateFormat.getDateInstance(Globals.DATE_FORMAT_STYLE_PATTERN);

		label.setText("");
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String selectedString = "";
		ImageIcon selectedIcon = null;

		if (value instanceof String && !((String) value).isEmpty()) {

			Date d = Timestamp.valueOf((String) value);
			selectedString = dateFormat.format(d);

		} else {
			selectedString = "";
		}

		if (result instanceof JLabel) {
			((JLabel) result).setText(selectedString);
			((JLabel) result).setIcon(selectedIcon);
			((JLabel) result).setToolTipText(selectedString);
		}

		return result;
	}
}
