/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.utils.logging.Logging;

public class BooleanIconTableCellRenderer extends ColorTableCellRenderer {
	private Icon trueIcon;
	private Icon falseIcon;

	public BooleanIconTableCellRenderer(Icon trueIcon, Icon falseIcon) {
		this.trueIcon = trueIcon;
		this.falseIcon = falseIcon;

		super.setHorizontalAlignment(SwingConstants.CENTER);

	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

		Logging.debug("row=" + row + ", column=" + column + ", value=" + value + ", trueIcon=" + trueIcon
				+ ", falseIcon=" + falseIcon);

		if (Boolean.TRUE.equals(value)) {
			setIcon(trueIcon);
		} else {
			setIcon(falseIcon);
		}

		return this;
	}
}
