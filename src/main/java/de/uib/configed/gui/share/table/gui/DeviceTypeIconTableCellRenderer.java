/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.share.logging.Logging;

public class DeviceTypeIconTableCellRenderer extends ColorTableCellRenderer {
	private static final String DESKTOP = "desktop";
	private static final String NOTEBOOK = "notebook";
	private static final String VIRTUAL_MACHINE = "virtual_machine";
	private static final String CONVERTIBLE = "convertible";
	private static final String SERVER = "server";
	private static final String OTHER = "other";

	public DeviceTypeIconTableCellRenderer() {
		super.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
		Logging.debug("row=", row, ", column=", column, ", value=", value);
		if (NOTEBOOK.equals(value)) {
			setIcon(Icons.getThemeSVGRepoIcon("laptop", 16));
			setToolTipText(NOTEBOOK);
		} else if (DESKTOP.equals(value)) {
			setIcon(Icons.getThemeSVGRepoIcon("desktop", 16));
			setToolTipText(DESKTOP);
		} else if (VIRTUAL_MACHINE.equals(value)) {
			setIcon(Icons.getThemeSVGRepoIcon("virtualMachine", 16));
			setToolTipText(VIRTUAL_MACHINE);
		} else if (CONVERTIBLE.equals(value)) {
			setIcon(Icons.getThemeSVGRepoIcon("convertible", 16));
			setToolTipText(CONVERTIBLE);
		} else if (SERVER.equals(value)) {
			setIcon(Icons.getThemeSVGRepoIcon("server", 16));
			setToolTipText(SERVER);
		} else if (OTHER.equals(value)) {
			//setIcon(null); // its actually same as null, cause we do not know what the device type is
			// affects sorting..
			setIcon(Icons.getThemeIntellijIcon("questionMark", 16));
			setToolTipText(OTHER);
		} else {
			if (value != null && !value.toString().isBlank()) {
				Logging.warning(this, "no icon for " + value);
			}
			setIcon(null);
			setToolTipText(null);
		}
		return this;
	}
}
