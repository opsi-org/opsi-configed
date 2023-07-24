/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.uib.Main;
import de.uib.configed.Globals;

public class CellRendererByIndex extends JLabel implements ListCellRenderer<String> {

	private Font uhOhFont;

	private Map<String, String> mapOfStrings;
	private Map<String, String> mapOfTooltips;

	public CellRendererByIndex(Map<String, String> mapOfStringValues) {
		mapOfStrings = mapOfStringValues;
		mapOfTooltips = mapOfStringValues;
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

		String selectedString = null;
		String selectedTooltip = null;

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

			if (mapOfTooltips != null) {
				selectedTooltip = mapOfTooltips.get(value);
			}
		}

		if (selectedString == null) {
			selectedString = "" + value;
		}

		if (selectedTooltip == null) {
			selectedTooltip = "" + value;
		}

		setText(selectedString);

		setToolTipText(selectedTooltip);

		if (!Main.FONT) {
			setFont(Globals.defaultFont);
		}

		return this;
	}
}
