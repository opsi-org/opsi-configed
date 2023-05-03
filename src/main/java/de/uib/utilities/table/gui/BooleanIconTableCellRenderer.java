package de.uib.utilities.table.gui;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * BooleanIconTableCellRenderer.java
 * Copyright (C) uib.de 2018
 *
 *	GPL licensed
 *   Author Rupert Röder
*/
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.utilities.logging.Logging;

public class BooleanIconTableCellRenderer extends StandardTableCellRenderer {
	Icon trueIcon;
	Icon falseIcon;
	boolean allowingString;

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

		if (Boolean.TRUE.equals(value)) {
			label.setIcon(trueIcon);
		} else {
			label.setIcon(falseIcon);
		}

		return comp;
	}

}
