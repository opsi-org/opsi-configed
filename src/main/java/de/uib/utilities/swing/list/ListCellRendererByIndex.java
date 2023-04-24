package de.uib.utilities.swing.list;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2011 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import de.uib.configed.Globals;

public class ListCellRendererByIndex extends StandardListCellRenderer {

	protected Map<String, String> mapOfStrings;
	protected Map<String, String> mapOfTooltips;
	protected Map<String, ImageIcon> mapOfImages;
	protected boolean showOnlyIcon;

	public ListCellRendererByIndex(Map<String, String> mapOfStringValues, Map<String, String> mapOfDescriptions,
			String tooltipPrefix) {
		this(mapOfStringValues, mapOfDescriptions, null, false, tooltipPrefix);
	}

	public ListCellRendererByIndex(Map<String, String> mapOfStringValues, Map<String, String> mapOfDescriptions,
			String imagesBase, boolean showOnlyIcon, String tooltipPrefix) {
		super(tooltipPrefix);
		this.showOnlyIcon = showOnlyIcon;
		mapOfStrings = mapOfStringValues;
		mapOfTooltips = mapOfDescriptions;
		mapOfImages = new HashMap<>();

		if (mapOfStrings != null) {
			// Load the item values
			Iterator<Entry<String, String>> iter = mapOfStrings.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				String key = entry.getKey();
				String stringval = entry.getValue();

				ImageIcon image = null;

				if (imagesBase != null && key != null && stringval != null) {
					String imageFileString = imagesBase + "/" + key + ".png";

					image = Globals.createImageIcon(imageFileString, stringval);

					if (image == null) {
						// try with gif
						imageFileString = imagesBase + "/" + stringval + ".gif";

						image = Globals.createImageIcon(imageFileString, stringval);

					}

					if (image != null) {
						mapOfImages.put(key, image);
					}
				}
			}
		}

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
			((JLabel) jc).setToolTipText(Globals.fillStringToLength(tooltipPrefix + " " + tooltip + " ", FILL_LENGTH));
		}

		return jc;

	}
}
