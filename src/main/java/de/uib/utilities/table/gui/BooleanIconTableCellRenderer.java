/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.utilities.logging.Logging;

public class BooleanIconTableCellRenderer extends StandardTableCellRenderer {
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
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		Logging.debug("row=" + row + ", column=" + column + ", comp=" + comp.getClass() + ", value=" + value
				+ ", allowingString=" + allowingString + ", trueIcon=" + trueIcon + ", falseIcon=" + falseIcon);

		if (!(comp instanceof JLabel)) {
			return comp;
		}

		if (value != null && !(value instanceof Boolean) && !(value instanceof String)) {
			return comp;
		}

		if (value != null && !allowingString && !(value instanceof Boolean)) {
			return comp;
		}

		JLabel label = (JLabel) comp;

		label.setText("");
		label.setHorizontalAlignment(SwingConstants.CENTER);

		if (objAsBoolean(value).booleanValue()) {
			label.setIcon(trueIcon);
		} else {
			label.setIcon(falseIcon);
		}

		return comp;
	}

	private Boolean objAsBoolean(Object obj) {
		Boolean result = Boolean.FALSE;
		if (obj != null) {
			if (obj instanceof Boolean) {
				result = (Boolean) obj;
			} else if (obj instanceof String) {
				result = Boolean.valueOf((String) obj);
			} else {
				Logging.warning(this, "Unexpected obj type " + obj);
			}
		}
		return result;
	}

}
