/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;

public class LicensingInfoPanelGenEditTable extends PanelGenEditTable {
	public LicensingInfoPanelGenEditTable(String title, boolean editing, int generalPopupPosition, int[] popupsWanted,
			boolean withTablesearchPane) {
		super(title, editing, generalPopupPosition, popupsWanted, withTablesearchPane);
	}

	public void setMarkBoldHeaderCellRenderer() {
		theTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (value != null && value.toString().equals(LicensingInfoMap.getInstance().getLatestDate())) {
					setFont(getFont().deriveFont(Font.BOLD));
				}

				return this;
			}
		});

		theTable.getTableHeader().setReorderingAllowed(false);
	}
}
