package de.uib.utilities.swing.list;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2010 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.swing.CellAlternatingColorizer;

public class StandardListCellRenderer extends DefaultListCellRenderer {

	// this is the normal preferred height for components with content.
	// We want this height also for empty components
	private static final int CELL_HEIGHT = 20;
	protected static final int FILL_LENGTH = 20;

	protected String tooltipPrefix = "";

	protected Color uniformColor;
	protected Color uniformSelectedColor;

	protected Color selectedEven = Globals.defaultTableSelectedRowDark;
	protected Color selectedUneven = Globals.defaultTableSelectedRowBright;
	protected Color unselectedEven = Globals.defaultTableCellBgColor2;
	protected Color unselectedUneven = Globals.defaultTableCellBgColor1;

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

		if (uniformColor == null) {
			CellAlternatingColorizer.colorize(jc, isSelected, (index % 2 == 0), true, selectedEven, selectedUneven,
					unselectedEven, unselectedUneven);
		} else {
			if (!ConfigedMain.OPSI_4_3) {
				if (isSelected) {
					jc.setBackground(uniformSelectedColor);
				} else {
					jc.setBackground(uniformColor);
				}
			}
		}

		jc.setFont(Globals.defaultFont);

		if (jc instanceof JLabel) {
			((JLabel) jc).setToolTipText(Globals.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));
		}

		return jc;
	}

	public void setUniformColors(Color c, Color cSelected) {
		// ignore the inherited alterna

		uniformColor = c;
		uniformSelectedColor = cSelected;
	}

	public void setAlternatingColors(Color selectedEvenColor, Color selectedUnevenColor, Color unselectedEvenColor,
			Color unselectedUnevenColor) {
		selectedEven = selectedEvenColor;
		selectedUneven = selectedUnevenColor;
		unselectedEven = unselectedEvenColor;
		unselectedUneven = unselectedUnevenColor;
	}
}
