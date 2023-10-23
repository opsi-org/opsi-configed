/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;
import java.awt.Font;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class LicensingInfoTableCellRenderer extends DefaultTableCellRenderer {
	protected LicensingInfoMap licensingInfoMap;

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

		JLabel jc = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		ColorTableCellRenderer.colorize(jc, isSelected, row % 2 == 0, column % 2 == 0);

		String latestChange = licensingInfoMap.getLatestDate();
		String columnName = licensingInfoMap.getColumnNames().get(column);
		String rowName = licensingInfoMap.getModules().get(row);

		if (columnName != null && (columnName.equals(Configed.getResourceValue("LicensingInfo.modules"))
				|| columnName.equals(Configed.getResourceValue("LicensingInfo.available")))) {
			jc.setToolTipText(value.toString());
		}

		if (columnName == null) {
			Logging.warning(this, "columnName is null");
		} else if (columnName.equals(Configed.getResourceValue("LicensingInfo.available"))) {
			jc.setText("");

			if (value.equals(true)) {
				jc.setIcon(Utils.createImageIcon("images/checked_withoutbox.png", ""));
			} else {
				jc.setIcon(Utils.createImageIcon("images/checked_void.png", ""));
			}

		} else if (!columnName.equals(Configed.getResourceValue("LicensingInfo.modules"))
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

				if (state.equals(LicensingInfoMap.STATE_CLOSE_TO_LIMIT)) {

					jc.setForeground(Globals.OPSI_WARNING);

					jc.setToolTipText(
							"<html>" + Configed.getResourceValue("LicensingInfo.warning.close_to_limit") + "<br>"
									+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");

				} else if (state.equals(LicensingInfoMap.STATE_OVER_LIMIT)) {

					jc.setForeground(Globals.OPSI_ERROR);

					jc.setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.over_limit") + "<br>"
							+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");

				} else if (state.equals(LicensingInfoMap.STATE_DAYS_WARNING)) {

					jc.setForeground(Globals.OPSI_WARNING);

					jc.setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.days") + "<br>"
							+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");

				} else if (state.equals(LicensingInfoMap.STATE_DAYS_OVER)) {
					jc.setForeground(Globals.OPSI_ERROR);

					jc.setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.days_over") + "<br>"
							+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
				} else {
					jc.setForeground(Globals.OPSI_OK);
				}
			}

			String prevCol = licensingInfoMap.getColumnNames().get(column - 1);
			if (!prevCol.equals(Configed.getResourceValue("LicensingInfo.modules"))
					&& !prevCol.equals(Configed.getResourceValue("LicensingInfo.available"))) {
				String clientNum = moduleToDateData.get(LicensingInfoMap.CLIENT_NUMBER).toString();
				String prevClientNum = datesMap.get(prevCol).get(rowName).get(LicensingInfoMap.CLIENT_NUMBER)
						.toString();
				if (clientNum != null && prevClientNum != null && !clientNum.equals(prevClientNum)) {
					jc.setFont(jc.getFont().deriveFont(Font.BOLD));
				}
			}
		} else {
			// columnName is Configed.getResourceValue("LicensingInfo.modules"), so do nothing; should remain empty
		}

		return jc;
	}
}
