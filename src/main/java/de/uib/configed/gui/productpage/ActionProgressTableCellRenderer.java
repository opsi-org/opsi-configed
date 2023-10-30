/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.Component;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;

import de.uib.configed.Globals;
import de.uib.configed.guidata.ColoredTableCellRendererByIndex;
import de.uib.opsidatamodel.productstate.ActionProgress;
import utils.Utils;

public class ActionProgressTableCellRenderer extends ColoredTableCellRendererByIndex {
	ActionProgressTableCellRenderer(Map<String, String> mapOfStringValues, String tooltipPrefix) {
		super(mapOfStringValues, tooltipPrefix);
	}

	// overwrite the renderer in order to get the behaviour:
	// - if the cell value is not empty or null, display the installing gif
	// - write the cell value text as tooltip
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component result = null;
		if (!isValueNull(value)) {
			result = super.getTableCellRendererComponent(table, "installing", isSelected, hasFocus, row, column);

			((JLabel) result).setToolTipText(Utils.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));
		} else if (value != null && value.toString().equalsIgnoreCase(Globals.CONFLICT_STATE_STRING)) {
			result = super.getTableCellRendererComponent(table, Globals.CONFLICT_STATE_STRING, isSelected, hasFocus,
					row, column);

			((JLabel) result).setToolTipText(
					Utils.fillStringToLength(tooltipPrefix + " " + Globals.CONFLICT_STATE_STRING + " ", FILL_LENGTH));
		} else {
			result = super.getTableCellRendererComponent(table, "none", isSelected, hasFocus, row, column);

			((JLabel) result).setToolTipText(Utils.fillStringToLength(
					tooltipPrefix + " " + ActionProgress.getDisplayLabel(ActionProgress.NONE) + " ", FILL_LENGTH));
		}

		return result;
	}

	// Tests if the value is such that it should not be shown
	private static boolean isValueNull(Object value) {
		if (value == null) {
			return true;
		}

		if ("".equals(value) || "null".equals(value) || "none".equals(value)) {
			return true;
		}

		return value.toString().equalsIgnoreCase(Globals.CONFLICT_STATE_STRING);
	}
}
