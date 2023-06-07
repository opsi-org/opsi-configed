/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.ColorTableCellRenderer;

public class TableCellRendererCurrency extends ColorTableCellRenderer {
	private JLabel label = new JLabel();
	private NumberFormat decimalFormat;

	public TableCellRendererCurrency() {
		label.setText("");
		decimalFormat = NumberFormat.getCurrencyInstance();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String selectedString = "";
		ImageIcon selectedIcon = null;

		if (value instanceof String && !((String) value).isEmpty()) {

			try {
				double number = Double.parseDouble((String) value);
				selectedString = decimalFormat.format(number);
			} catch (Exception ex) {
				Logging.warning(this, " format exception", ex);
			}

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
