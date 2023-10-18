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

public class ColorHeaderCellRenderer extends DefaultTableCellRenderer {
	private TableCellRenderer rend;

	public ColorHeaderCellRenderer(TableCellRenderer rend) {
		this.rend = rend;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cell = rend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (cell instanceof JComponent) {
			JComponent jc = (JComponent) cell;

			if (value != null) {
				String val1 = "" + value;

				jc.setToolTipText(val1);
			}
		}

		return cell;
	}
}
