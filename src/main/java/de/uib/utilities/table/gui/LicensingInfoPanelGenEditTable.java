/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import com.itextpdf.text.Font;

import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;

public class LicensingInfoPanelGenEditTable extends PanelGenEditTable {

	public LicensingInfoPanelGenEditTable(String title, int maxTableWidth, boolean editing, int generalPopupPosition,
			boolean switchLineColors, int[] popupsWanted, boolean withTablesearchPane) {
		super(title, maxTableWidth, editing, generalPopupPosition, switchLineColors, popupsWanted, withTablesearchPane);
	}

	@Override
	protected void setCellRenderers() {

		LicensingInfoMap lInfoMap = LicensingInfoMap.getInstance();
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			String name = tableModel.getColumnName(i);
			TableColumn col = theTable.getColumn(name);

			col.setCellRenderer(new LicensingInfoTableCellRenderer(lInfoMap));
		}
	}

	public void setMarkBoldHeaderCellRenderer() {
		theTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				JComponent jc = (JComponent) cell;

				if (value != null && value.toString().equals(LicensingInfoMap.getInstance().getLatestDate())) {
					jc.setFont(jc.getFont().deriveFont(Font.BOLD));
				}

				return cell;
			}
		});

		theTable.getTableHeader().setReorderingAllowed(false);
	}
}
