/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

public class PlatfromIconTableCellRenderer extends ColorTableCellRenderer {
	private static final String WINDOWS = "windows";
	private static final String MACOS = "macos";
	private static final String LINUX = "linux";

	public PlatfromIconTableCellRenderer() {
		super.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
		Logging.debug("row=", row, ", column=", column, ", value=", value);
		if (MACOS.equals(value)) {
			setIcon(Icons.getThemeSVGRepoIcon("macos", 16));
			setToolTipText(MACOS);
		} else if (WINDOWS.equals(value)) {
			setIcon(Icons.getThemeSVGRepoIcon("windows", 16));
			setToolTipText(WINDOWS);
		} else if (LINUX.equals(value)) {
			setIcon(Icons.getThemeSVGRepoIcon("linux", 16));
			setToolTipText(LINUX);
		} else {
			setIcon(null);
			setToolTipText(null);
		}
		return this;
	}
}
