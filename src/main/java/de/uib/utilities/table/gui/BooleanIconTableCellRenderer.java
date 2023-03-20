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

public class BooleanIconTableCellRenderer extends StandardTableCellRenderer {
	Icon trueIcon;
	Icon falseIcon;
	Icon nullIcon;
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
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (!(c instanceof JLabel)) {
			return c;
		}

		if (value != null && !(value instanceof Boolean) && !(value instanceof String)) {
			return c;
		}

		if (value != null && !allowingString && !(value instanceof Boolean)) {
			return c;
		}

		JLabel label = (JLabel) c;

		label.setText("");
		label.setIcon(null);
		label.setHorizontalAlignment(SwingConstants.CENTER);

		if (value == null) {
			if (nullIcon != null) {
				label.setIcon(nullIcon);
			}
		} else {
			if (Boolean.TRUE.equals(value)) {
				if (trueIcon != null) {
					label.setIcon(trueIcon);
				}
			} else {
				if (falseIcon != null) {
					label.setIcon(falseIcon);
				}
			}
		}

		return c;

	}

}
