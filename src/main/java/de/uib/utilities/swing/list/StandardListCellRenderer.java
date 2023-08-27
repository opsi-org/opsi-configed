/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.utilities.swing.CellAlternatingColorizer;
import utils.Utils;

public class StandardListCellRenderer extends DefaultListCellRenderer {

	// this is the normal preferred height for components with content.
	// We want this height also for empty components
	private static final int CELL_HEIGHT = 20;
	protected static final int FILL_LENGTH = 20;

	protected String tooltipPrefix = "";

	private Color selectedEven = Globals.DEFAULT_TABLE_SELECTION_ROW_DARK;
	private Color selectedUneven = Globals.DEFAULT_TABLE_SELECTED_ROW_BRIGHT;
	private Color unselectedEven = Globals.DEFAULT_TABLE_CELL_BG_COLOR_2;
	private Color unselectedUneven = Globals.DEFAULT_TABLE_CELL_BG_COLOR_1;

	public StandardListCellRenderer() {
		super();
	}

	public StandardListCellRenderer(String tooltipPrefix) {
		super();
		this.tooltipPrefix = tooltipPrefix;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		Dimension prefDimension = c.getPreferredSize();
		prefDimension.setSize(prefDimension.getWidth(), CELL_HEIGHT);
		c.setPreferredSize(prefDimension);

		// condition true if c is null
		if (!(c instanceof JComponent)) {
			return c;
		}

		JComponent jc = (JComponent) c;

		if (!Main.THEMES) {
			CellAlternatingColorizer.colorize(jc, isSelected, index % 2 == 0, true, selectedEven, selectedUneven,
					unselectedEven, unselectedUneven);
		}

		if (!Main.FONT) {
			jc.setFont(Globals.DEFAULT_FONT);
		}

		if (jc instanceof JLabel) {
			((JLabel) jc).setToolTipText(Utils.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));
		}

		return jc;
	}

	public void setAlternatingColors(Color selectedEvenColor, Color selectedUnevenColor, Color unselectedEvenColor,
			Color unselectedUnevenColor) {
		selectedEven = selectedEvenColor;
		selectedUneven = selectedUnevenColor;
		unselectedEven = unselectedEvenColor;
		unselectedUneven = unselectedUnevenColor;
	}
}
