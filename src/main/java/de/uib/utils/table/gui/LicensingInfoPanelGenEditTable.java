/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;

public class LicensingInfoPanelGenEditTable extends PanelGenEditTable {
	public LicensingInfoPanelGenEditTable() {
		super(null, false, 0,
				new int[] { PanelGenEditTable.POPUP_PRINT, PanelGenEditTable.POPUP_PDF,
						PanelGenEditTable.POPUP_SORT_AGAIN, PanelGenEditTable.POPUP_EXPORT_CSV,
						PanelGenEditTable.POPUP_EXPORT_SELECTED_CSV, PanelGenEditTable.POPUP_RELOAD },
				false);

		setMarkBoldHeaderCellRenderer();
	}

	private void setMarkBoldHeaderCellRenderer() {
		jTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
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

		jTable.getTableHeader().setReorderingAllowed(true);
	}
}
