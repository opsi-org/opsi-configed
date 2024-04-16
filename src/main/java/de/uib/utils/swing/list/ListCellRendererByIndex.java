/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing.list;

import java.awt.Component;
import java.util.Map;

import javax.swing.JList;

public class ListCellRendererByIndex extends StandardListCellRenderer {
	private Map<String, String> mapOfTooltips;

	public ListCellRendererByIndex(Map<String, String> mapOfTooltips) {
		super();

		this.mapOfTooltips = mapOfTooltips;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		String tooltip = mapOfTooltips.get(value);
		if (tooltip != null && !tooltip.isEmpty()) {
			setToolTipText(tooltip);
		} else {
			setToolTipText(null);
		}

		return this;
	}
}
