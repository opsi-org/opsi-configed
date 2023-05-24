/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing.list;

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

	private Map<String, String> mapOfStrings;
	private Map<String, String> mapOfTooltips;
	private Map<String, ImageIcon> mapOfImages;

	public ListCellRendererByIndex(Map<String, String> mapOfStringValues, Map<String, String> mapOfDescriptions,
			String tooltipPrefix) {
		this(mapOfStringValues, mapOfDescriptions, null, tooltipPrefix);
	}

	public ListCellRendererByIndex(Map<String, String> mapOfStringValues, Map<String, String> mapOfDescriptions,
			String imagesBase, String tooltipPrefix) {
		super(tooltipPrefix);
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
