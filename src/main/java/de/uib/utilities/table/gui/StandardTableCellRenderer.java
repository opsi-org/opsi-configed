/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;

import utils.Utils;

public class StandardTableCellRenderer extends ColorTableCellRenderer {
	protected static final int FILL_LENGTH = 20;

	protected String tooltipPrefix;
	private String separator = ": ";

	public StandardTableCellRenderer() {
		super();
	}

	public StandardTableCellRenderer(String tooltipPrefix) {
		this();
		this.tooltipPrefix = tooltipPrefix;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (!(c instanceof JComponent)) {
			return c;
		}

		JComponent jc = (JComponent) c;

		if (jc instanceof JLabel) {
			String tooltipText = null;
			if (tooltipPrefix != null && !tooltipPrefix.isEmpty()) {
				tooltipText = Utils.fillStringToLength(tooltipPrefix + separator + value + " ", FILL_LENGTH);
			} else {
				tooltipText = Utils.fillStringToLength(value + " ", FILL_LENGTH);
			}

			((JLabel) jc).setToolTipText(tooltipText);
		}

		return jc;
	}
}
