/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.uib.Main;
import de.uib.configed.Globals;

public class CellRendererByIndex extends ImagePlusTextLabel implements ListCellRenderer<String> {
	private static final int IMAGE_DEFAULT_WIDTH = 30;

	private Font uhOhFont;

	private Map<String, String> mapOfStrings;
	private Map<String, String> mapOfTooltips;
	private Map<String, ImageIcon> mapOfImages;

	public CellRendererByIndex(Set<String> keySet, String imagesBase, int imageWidth) {
		super(imageWidth);

		super.setOpaque(true);

		mapOfImages = new HashMap<>();
		mapOfStrings = new HashMap<>();

		// Load the item images
		if (imagesBase == null) {
			super.setIconVisible(false);
		} else {
			Iterator<String> iter = keySet.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				String stringval = key;
				mapOfStrings.put(key, key);

				ImageIcon image = null;

				String imageFileString = imagesBase + "/" + stringval + ".png";

				image = Globals.createImageIcon(imageFileString, stringval);
				if (image != null) {
					mapOfImages.put(key, image);
				}
			}
		}
		mapOfTooltips = mapOfStrings;

	}

	public CellRendererByIndex(Map<String, String> mapOfStringValues, String imagesBase) {
		super(IMAGE_DEFAULT_WIDTH);

		super.setOpaque(true);

		mapOfStrings = mapOfStringValues;
		mapOfTooltips = mapOfStringValues;
		mapOfImages = new HashMap<>();

		// Load the item images
		if (imagesBase == null) {
			super.setIconVisible(false);
		} else {
			Iterator<Entry<String, String>> iter = mapOfStrings.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				String key = entry.getKey();
				String stringval = entry.getValue();

				ImageIcon image = null;

				if (key != null && stringval != null) {
					String imageFileString = imagesBase + "/" + stringval + ".png";

					image = Globals.createImageIcon(imageFileString, stringval);
					if (image != null) {
						mapOfImages.put(key, image);
					}
				}
			}
		}
	}

	/*
	 * This method finds the image and text corresponding
	 * to the selected value and returns the label, set up
	 * to display the text and image.
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
			boolean isSelected, boolean cellHasFocus) {
		// Get the selected index. (The index param isn't
		// always valid, so just use the value.)

		Color background;
		Color foreground;

		if (isSelected) {
			background = Globals.nimbusSelectionBackground;
			foreground = Globals.CELL_RENDERER_BY_INDEX_SELECTED_FOREGROUND_COLOR;
		} else {
			background = Globals.nimbusBackground;
			foreground = Globals.nimbusSelectionBackground;
		}

		if (!Main.THEMES) {
			setBackground(background);
			setForeground(foreground);
		}

		String selectedString = "";
		ImageIcon selectedIcon = null;
		String selectedTooltip = "";

		if (uhOhFont == null) {
			// lazily create this font
			uhOhFont = list.getFont().deriveFont((float) 10);
		}
		if (!Main.FONT) {
			setFont(uhOhFont);
		}

		if (value != null) {
			if (mapOfStrings != null) {
				selectedString = mapOfStrings.get(value);
			}

			if (mapOfImages != null) {
				selectedIcon = mapOfImages.get(value);
			}

			if (mapOfTooltips != null) {
				selectedTooltip = mapOfTooltips.get(value);
			}
		}

		if (selectedString == null) {
			selectedString = "" + value;
		}

		setIcon(selectedIcon);
		setText(selectedString);

		setToolTipText(selectedTooltip);

		if (!Main.FONT) {
			setFont(Globals.defaultFont);
		}

		return this;
	}
}
