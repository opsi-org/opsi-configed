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
import de.uib.utils.swing.PopupMenuTrait;

public class LicensingInfoPanelGenEditTable extends PanelGenEditTable {
	public LicensingInfoPanelGenEditTable() {
		super(null, false, 0,
				new int[] { PopupMenuTrait.POPUP_PRINT, PopupMenuTrait.POPUP_PDF, PanelGenEditTable.POPUP_SORT_AGAIN,
						PopupMenuTrait.POPUP_EXPORT_CSV, PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV,
						PopupMenuTrait.POPUP_RELOAD },
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
