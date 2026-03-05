/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class CheckBoxTableCellRenderer extends JCheckBox implements TableCellRenderer {
	private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	private Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");

	public CheckBoxTableCellRenderer() {
		super();

		super.setBorderPainted(true);
		super.setHorizontalAlignment(SwingConstants.CENTER);
		super.setVerticalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		ColorTableCellRenderer.colorize(this, isSelected, row, column);

		setBorder(hasFocus ? focusBorder : noFocusBorder);
		setSelected(Boolean.TRUE.equals(value));

		return this;
	}
}
