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
	private boolean allowingString;

	public BooleanIconTableCellRenderer(Icon trueIcon, Icon falseIcon) {
		this(trueIcon, falseIcon, false);
	}

	public BooleanIconTableCellRenderer(Icon trueIcon, Icon falseIcon, boolean allowingString) {
		super();
		this.allowingString = allowingString;
		this.trueIcon = trueIcon;
		this.falseIcon = falseIcon;

		super.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		Logging.debug("row=" + row + ", column=" + column + ", value=" + value + ", allowingString=" + allowingString
				+ ", trueIcon=" + trueIcon + ", falseIcon=" + falseIcon);

		if (value != null && !(value instanceof Boolean) && !(value instanceof String)) {
			return this;
		}

		if (value != null && !allowingString && !(value instanceof Boolean)) {
			return this;
		}

		super.setText(null);

		if (objAsBoolean(value).booleanValue()) {
			setIcon(trueIcon);
		} else {
			setIcon(falseIcon);
		}

		return this;
	}

	private Boolean objAsBoolean(Object object) {
		if (object instanceof Boolean b) {
			return b;
		} else if (object instanceof String string) {
			return Boolean.valueOf(string);
		} else {
			Logging.warning(this, "Unexpected obj type " + object);
			return Boolean.FALSE;
		}
	}
}
