/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class MyListCellRenderer extends DefaultListCellRenderer {
	private static final int FILL_LENGTH = 30;

	Map<String, Map<String, Object>> extendedInfo;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public void setInfo(Map<String, Map<String, Object>> extendedInfo) {
		Logging.debug(this, "setInfo " + extendedInfo);
		this.extendedInfo = extendedInfo;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (!(c instanceof JComponent)) {
			return c;
		}

		JComponent jc = (JComponent) c;

		if (jc instanceof JLabel) {
			String tooltipText = null;

			String key = "";

			if (value != null) {
				key = "" + value;
			}

			if (extendedInfo != null && extendedInfo.get(key) != null
					&& extendedInfo.get(key).get("description") != null
					&& !("" + extendedInfo.get(key).get("description")).isEmpty()) {
				tooltipText = "" + extendedInfo.get(value).get("description");
			} else {
				tooltipText = key;
			}

			tooltipText = Utils.fillStringToLength(tooltipText + " ", FILL_LENGTH);

			String depot = (String) value;
			if (!persistenceController.hasDepotPermission(depot)) {
				if (!Main.THEMES) {
					((JLabel) jc).setBackground(Globals.BACKGROUND_COLOR_3);
				}

				((JLabel) jc).setToolTipText(
						"Depot " + depot + " " + Configed.getResourceValue("Permission.depot.not_accessible"));
			} else {
				((JLabel) jc).setToolTipText(tooltipText);
			}
		}

		return jc;
	}
}
