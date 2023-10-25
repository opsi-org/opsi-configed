/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing.list;

import java.awt.Component;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import utils.Utils;

public class ListCellRendererByIndex extends StandardListCellRenderer {

	private Map<String, String> mapOfStrings;
	private Map<String, String> mapOfTooltips;

	public ListCellRendererByIndex(Map<String, String> mapOfStringValues, Map<String, String> mapOfDescriptions,
			String tooltipPrefix) {
		super(tooltipPrefix);

		mapOfStrings = mapOfStringValues;
		mapOfTooltips = mapOfDescriptions;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (!(c instanceof JComponent)) {
			return c;
		}

		if (value == null) {
			return c;
		}

		String tooltip = mapOfTooltips.get(value);
		if (tooltip == null || tooltip.isEmpty()) {
			if (mapOfStrings == null) {
				tooltip = "" + value;
			} else {
				tooltip = mapOfStrings.get(value);
			}
		}

		JComponent jc = (JComponent) c;

		if (jc instanceof JLabel) {
			((JLabel) jc).setToolTipText(Utils.fillStringToLength(tooltipPrefix + " " + tooltip + " ", FILL_LENGTH));
		}

		return jc;
	}
}
