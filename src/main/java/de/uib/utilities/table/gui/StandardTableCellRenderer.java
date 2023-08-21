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
import javax.swing.table.DefaultTableCellRenderer;

import com.formdev.flatlaf.FlatLaf;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.utilities.swing.CellAlternatingColorizer;
import utils.Utils;

public class StandardTableCellRenderer extends DefaultTableCellRenderer {
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
		CellAlternatingColorizer.colorize(jc, isSelected, row % 2 == 0, column % 2 == 0, true);

		if (jc instanceof JLabel) {
			String tooltipText = null;
			if (tooltipPrefix != null && !tooltipPrefix.isEmpty()) {
				tooltipText = Utils.fillStringToLength(tooltipPrefix + separator + value + " ", FILL_LENGTH);
			} else {
				tooltipText = Utils.fillStringToLength(value + " ", FILL_LENGTH);
			}

			((JLabel) jc).setToolTipText(tooltipText);
		}

		if (Main.THEMES) {
			if (isSelected) {
				jc.setBackground(Globals.OPSI_BLUE);
				if (FlatLaf.isLafDark()) {
					jc.setForeground(Globals.OPSI_FOREGROUND_DARK);
				} else {
					jc.setForeground(Globals.OPSI_BACKGROUND_LIGHT);
				}
			} else if (FlatLaf.isLafDark()) {
				jc.setBackground(Globals.OPSI_BACKGROUND_DARK);
				jc.setForeground(Globals.OPSI_FOREGROUND_DARK);
			} else {
				jc.setBackground(Globals.OPSI_BACKGROUND_LIGHT);
				jc.setForeground(Globals.OPSI_FOREGROUND_LIGHT);
			}

			if (row % 2 != 0) {
				if (!FlatLaf.isLafDark() || isSelected) {
					jc.setBackground(jc.getBackground().darker());
				} else {
					jc.setBackground(jc.getBackground().brighter());
				}
			}
		}

		return jc;
	}
}
