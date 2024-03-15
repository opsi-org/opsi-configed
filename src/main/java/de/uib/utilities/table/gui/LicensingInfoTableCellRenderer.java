/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;
import java.awt.Font;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTable;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class LicensingInfoTableCellRenderer extends ColorTableCellRenderer {
	private static final String DISPLAY_INFINITE = "\u221E";
	private static final ImageIcon availableIcon = Utils.getThemeIconPNG("bootstrap/check", "");

	protected LicensingInfoMap licensingInfoMap;

	public LicensingInfoTableCellRenderer(LicensingInfoMap lInfoMap) {
		super();
		licensingInfoMap = lInfoMap;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (value.equals(LicensingInfoMap.UNLIMITED_NUMBER)) {
			value = DISPLAY_INFINITE;
		}

		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String latestChange = licensingInfoMap.getLatestDate();
		String columnName = licensingInfoMap.getColumnNames().get(column);
		String rowName = licensingInfoMap.getModules().get(row);

		if (columnName == null) {
			Logging.warning(this, "columnName is null");
		} else if (columnName.equals(Configed.getResourceValue("LicensingInfo.available"))) {
			setText(null);
			setToolTipText(null);
			setAvailabilityIcon(value);
		} else if (!columnName.equals(Configed.getResourceValue("LicensingInfo.modules"))) {
			Map<String, Map<String, Map<String, Object>>> datesMap = licensingInfoMap.getDatesMap();
			Map<String, Object> moduleToDateData = datesMap.get(columnName).get(rowName);
			String state = moduleToDateData.get(LicensingInfoMap.STATE).toString();
			String licenses = moduleToDateData.get(LicensingInfoMap.LICENSE_IDS).toString().replace(", ", ", <br>");

			makeTooltipText(columnName, latestChange, state, value, licenses);

			setIcon(null);

			String prevCol = licensingInfoMap.getColumnNames().get(column - 1);
			if (!prevCol.equals(Configed.getResourceValue("LicensingInfo.modules"))
					&& !prevCol.equals(Configed.getResourceValue("LicensingInfo.available"))) {
				String clientNum = moduleToDateData.get(LicensingInfoMap.CLIENT_NUMBER).toString();
				String prevClientNum = datesMap.get(prevCol).get(rowName).get(LicensingInfoMap.CLIENT_NUMBER)
						.toString();
				if (clientNum != null && prevClientNum != null && !clientNum.equals(prevClientNum)) {
					setFont(getFont().deriveFont(Font.BOLD));
				}
			}
		} else {
			// columnName is Configed.getResourceValue("LicensingInfo.modules"), so do nothing; should remain empty		
			setIcon(null);
			setToolTipText(null);
		}

		return this;
	}

	private void setAvailabilityIcon(Object value) {
		if (Boolean.TRUE.equals(value)) {
			setIcon(availableIcon);
		} else {
			setIcon(null);
		}
	}

	private void makeTooltipText(String columnName, String latestChange, String state, Object value, String licenses) {
		if (!state.equals(LicensingInfoMap.STATE_UNLICENSED)) {
			setToolTipText("<html>" + "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
		} else {
			setToolTipText("<html>" + "clients: " + value.toString() + "</html>");
		}

		if (columnName.equals(latestChange)) {
			if (state.equals(LicensingInfoMap.STATE_CLOSE_TO_LIMIT)) {
				setForeground(Globals.OPSI_WARNING);

				setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.close_to_limit") + "<br>"
						+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
			} else if (state.equals(LicensingInfoMap.STATE_OVER_LIMIT)) {
				setForeground(Globals.OPSI_ERROR);

				setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.over_limit") + "<br>"
						+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
			} else if (state.equals(LicensingInfoMap.STATE_DAYS_WARNING)) {
				setForeground(Globals.OPSI_WARNING);

				setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.days") + "<br>" + "clients: "
						+ value.toString() + "<br>" + "license ids: " + licenses + "</html>");
			} else if (state.equals(LicensingInfoMap.STATE_DAYS_OVER)) {
				setForeground(Globals.OPSI_ERROR);

				setToolTipText("<html>" + Configed.getResourceValue("LicensingInfo.warning.days_over") + "<br>"
						+ "clients: " + value.toString() + "<br>" + "license ids: " + licenses + "</html>");
			} else {
				setForeground(Globals.OPSI_OK);
			}
		}
	}
}
