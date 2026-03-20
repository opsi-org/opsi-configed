/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;

public class HWInfoCellRenderer extends DefaultTableCellRenderer {
	private JCheckBox checkBox;

	public HWInfoCellRenderer() {
		checkBox = new JCheckBox();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cellComponent;
		if (value instanceof Boolean booleanValue) {
			cellComponent = checkBox;
			checkBox.setSelected(booleanValue);
		} else {
			cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

		ColorTableCellRenderer.colorize(cellComponent, isSelected, row, column);
		return cellComponent;
	}
}
