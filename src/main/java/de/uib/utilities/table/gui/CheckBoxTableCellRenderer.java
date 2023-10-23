/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class CheckBoxTableCellRenderer extends JCheckBox implements TableCellRenderer {

	private Border noFocusBorder;
	private Border focusBorder;

	public CheckBoxTableCellRenderer() {
		super();

		super.setBorderPainted(true);
		super.setHorizontalAlignment(SwingConstants.CENTER);
		super.setVerticalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		ColorTableCellRenderer.colorize(this, isSelected, row % 2 == 0, column % 2 == 0);

		if (hasFocus) {
			if (focusBorder == null) {
				focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
			}
			setBorder(focusBorder);
		} else {
			if (noFocusBorder == null) {
				noFocusBorder = new EmptyBorder(1, 1, 1, 1);
			}
			setBorder(noFocusBorder);
		}

		setSelected(Boolean.TRUE.equals(value));
		return this;
	}
}
