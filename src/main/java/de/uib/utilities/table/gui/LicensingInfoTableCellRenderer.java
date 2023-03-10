package de.uib.utilities.table.gui;

import java.awt.Component;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CellAlternatingColorizer;

public class LicensingInfoTableCellRenderer extends DefaultTableCellRenderer {
	LicensingInfoMap licensingInfoMap;

	public LicensingInfoTableCellRenderer(LicensingInfoMap lInfoMap) {
		super();
		licensingInfoMap = lInfoMap;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (value.equals(LicensingInfoMap.UNLIMITED_NUMBER)) {
			value = LicensingInfoMap.DISPLAY_INFINITE;
		}

		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		JLabel jc = (JLabel) cell;

		CellAlternatingColorizer.colorize(jc, isSelected, (row % 2 == 0), (column % 2 == 0), true);

		String latestChange = licensingInfoMap.getLatestDate();
		String columnName = licensingInfoMap.getColumnNames().get(column);
		String rowName = licensingInfoMap.getModules().get(row);

		if ((columnName != null && columnName.equals(Configed.getResourceValue("LicensingInfo.modules")))
				|| columnName.equals(Configed.getResourceValue("LicensingInfo.available"))) {
			jc.setToolTipText(value.toString());
		}

		if (columnName != null && columnName.equals(Configed.getResourceValue("LicensingInfo.available"))) {
			jc.setText("");

			if (value.equals(true)) {
				jc.setIcon(Globals.createImageIcon("images/checked_withoutbox.png", ""));
			} else {
				jc.setIcon(Globals.createImageIcon("images/checked_void.png", ""));
			}

		} else if (columnName != null && !columnName.equals(Configed.getResourceValue("LicensingInfo.modules"))
				&& !columnName.equals(Configed.getResourceValue("LicensingInfo.available"))) {
			Map<String, Map<String, Map<String, Object>>> datesMap = licensingInfoMap.getDatesMap();
			Map<String, Object> moduleToDateData = datesMap.get(columnName).get(rowName);
			String state = moduleToDateData.get(LicensingInfoMap.STATE).toString();

			String licenses = moduleToDateData.get(LicensingInfoMap.LICENSE_IDS).toString().replace(", ", ", <br>");
			if (!state.equals(LicensingInfoMap.STATE_UNLICENSED)) {

				jc.setToolTipText(
						"<html>" + "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
			} else {
				jc.setToolTipText("<html>" + "clients: " + value.toString() + "</html>");
			}

			if (columnName.equals(latestChange)) {
				if (!ConfigedMain.OPSI_4_3) {
					jc.setBackground(Globals.CHECK_COLOR);
				}

				if (state.equals(LicensingInfoMap.STATE_CLOSE_TO_LIMIT)) {
					if (!ConfigedMain.OPSI_4_3) {
						jc.setBackground(Globals.darkOrange);
					}
					jc.setToolTipText(
							"<html>" + Configed.getResourceValue("LicensingInfo.warning.close_to_limit") + "<br>"
									+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");

				} else if (state.equals(LicensingInfoMap.STATE_OVER_LIMIT)) {
					if (!ConfigedMain.OPSI_4_3) {
						jc.setBackground(Globals.WARNING_COLOR);
					}
					jc.setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.over_limit") + "<br>"
							+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");

				} else if (state.equals(LicensingInfoMap.STATE_DAYS_WARNING)) {
					if (!ConfigedMain.OPSI_4_3) {
						jc.setBackground(Globals.darkOrange);
					}
					jc.setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.days") + "<br>"
							+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");

				} else if (state.equals(LicensingInfoMap.STATE_DAYS_OVER)) {
					if (!ConfigedMain.OPSI_4_3) {
						jc.setBackground(Globals.WARNING_COLOR);
					}
					jc.setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.days_over") + "<br>"
							+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
				}

			}

			String prevCol = licensingInfoMap.getColumnNames().get(column - 1);
			try {
				if (!prevCol.equals(Configed.getResourceValue("LicensingInfo.modules"))
						&& !prevCol.equals(Configed.getResourceValue("LicensingInfo.available"))

				) {
					String clientNum = moduleToDateData.get(LicensingInfoMap.CLIENT_NUMBER).toString();
					String prevClientNum = datesMap.get(prevCol).get(rowName).get(LicensingInfoMap.CLIENT_NUMBER)
							.toString();

					if (!prevCol.equals(Configed.getResourceValue("LicensingInfo.modules")) && clientNum != null
							&& prevClientNum != null && !clientNum.equals(prevClientNum)) {
						jc.setFont(Globals.defaultFontBold);
					}
				}

			} catch (Exception ex) {
				Logging.error(this, "Exception thrown: " + ex);
			}
		}

		return jc;
	}

}
