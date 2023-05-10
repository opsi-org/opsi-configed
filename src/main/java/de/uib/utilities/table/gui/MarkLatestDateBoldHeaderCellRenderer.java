package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;

public class MarkLatestDateBoldHeaderCellRenderer extends DefaultTableCellRenderer {
	private TableCellRenderer rend;
	private LicensingInfoMap licensingInfoMap;

	public MarkLatestDateBoldHeaderCellRenderer(TableCellRenderer rend, LicensingInfoMap lInfoMap) {
		this.rend = rend;
		licensingInfoMap = lInfoMap;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component cell = rend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (!ConfigedMain.THEMES) {
			cell.setBackground(Globals.defaultTableHeaderBgColor);
		}

		JComponent jc = (JComponent) cell;

		if (value != null) {
			String latestDate = licensingInfoMap.getLatestDate();
			if (value.toString().equals(latestDate)) {
				if (!ConfigedMain.FONT) {
					jc.setFont(Globals.defaultFontBold);
				}
			}
		}

		return cell;
	}
}
