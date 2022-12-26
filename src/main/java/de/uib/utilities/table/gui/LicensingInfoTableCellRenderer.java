package de.uib.utilities.table.gui;

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.CellAlternatingColorizer;

public class LicensingInfoTableCellRenderer extends DefaultTableCellRenderer {
	LicensingInfoMap licensingInfoMap;
	private final String CLASSNAME = "LicensingInfoTableCellRenderer";

	public LicensingInfoTableCellRenderer(LicensingInfoMap lInfoMap) {
		super();
		licensingInfoMap = lInfoMap;
	}

	@Override
	public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		if (value.equals(LicensingInfoMap.UNLIMITED_NUMBER))
			value = LicensingInfoMap.DISPLAY_INFINITE;

		java.awt.Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		JLabel jc = (JLabel) cell;

		CellAlternatingColorizer.colorize(jc, isSelected, (row % 2 == 0), (column % 2 == 0), true);

		String latestChange = licensingInfoMap.getLatestDate();
		String columnName = licensingInfoMap.getColumnNames().get(column);
		String rowName = licensingInfoMap.getModules().get(row);

		if (columnName != null && (columnName.equals(configed.getResourceValue("LicensingInfo.modules")))
				| columnName.equals(configed.getResourceValue("LicensingInfo.available"))

		)
			jc.setToolTipText(value.toString());

		if (columnName != null && columnName.equals(configed.getResourceValue("LicensingInfo.modules"))) {

		}

		if (columnName != null && columnName.equals(configed.getResourceValue("LicensingInfo.available"))) {
			jc.setText("");

			if (value.equals(true))
				jc.setIcon(Globals.createImageIcon("images/checked_withoutbox.png", ""));

			else
				jc.setIcon(Globals.createImageIcon("images/checked_void.png", ""));

		} else if (columnName != null && !columnName.equals(configed.getResourceValue("LicensingInfo.modules"))
				&& !columnName.equals(configed.getResourceValue("LicensingInfo.available"))

		) {
			Map<String, Map<String, Map<String, Object>>> datesMap = licensingInfoMap.getDatesMap();
			Map<String, Object> moduleToDateData = datesMap.get(columnName).get(rowName);
			String state = moduleToDateData.get(LicensingInfoMap.STATE).toString();

			String licenses = moduleToDateData.get(LicensingInfoMap.LICENSE_IDS).toString().replace(", ", ", <br>");
			if (!state.equals(LicensingInfoMap.STATE_UNLICENSED)) {

				jc.setToolTipText(
						"<html>" + "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
			} else
				jc.setToolTipText("<html>" + "clients: " + value.toString() + "</html>");

			if (columnName == latestChange) {
				jc.setBackground(Globals.checkGreenLight);

				if (state.equals(LicensingInfoMap.STATE_CLOSE_TO_LIMIT)) {
					jc.setBackground(Globals.darkOrange);
					jc.setToolTipText(
							"<html>" + configed.getResourceValue("LicensingInfo.warning.close_to_limit") + "<br>"
									+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");

				} else if (state.equals(LicensingInfoMap.STATE_OVER_LIMIT)) {
					jc.setBackground(Globals.warningRed);
					jc.setToolTipText("<html>" + configed.getResourceValue("LicensingInfo.warning.over_limit") + "<br>"
							+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");

				} else if (state.equals(LicensingInfoMap.STATE_DAYS_WARNING)) {
					jc.setBackground(Globals.darkOrange);
					jc.setToolTipText("<html>" + configed.getResourceValue("LicensingInfo.warning.days") + "<br>"
							+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
				}

			}

			String prevCol = licensingInfoMap.getColumnNames().get(column - 1);
			try {
				if (!prevCol.equals(configed.getResourceValue("LicensingInfo.modules"))
						&& !prevCol.equals(configed.getResourceValue("LicensingInfo.available"))

				) {
					String clientNum = moduleToDateData.get(LicensingInfoMap.CLIENT_NUMBER).toString();
					String prevClientNum = datesMap.get(prevCol).get(rowName).get(LicensingInfoMap.CLIENT_NUMBER)
							.toString();

					if (!prevCol.equals(configed.getResourceValue("LicensingInfo.modules")) && clientNum != null
							&& prevClientNum != null) {
						if (!clientNum.equals(prevClientNum))
							jc.setFont(Globals.defaultFontBold);
					}
				}

			} catch (Exception ex) {
				logging.error(CLASSNAME, ex);
			}

		}

		return jc;
	}

}
