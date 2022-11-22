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
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import de.uib.configed.Globals;
import de.uib.utilities.swing.CellAlternatingColorizer;

public class StandardListCellRenderer extends DefaultListCellRenderer {

	protected String tooltipPrefix = "";

	protected int FILL_LENGTH = 20;

	protected java.awt.Color uniformColor = null;
	protected java.awt.Color uniformSelectedColor = null;

	protected java.awt.Color selectedEven = Globals.defaultTableSelectedRowDark;
	protected java.awt.Color selectedUneven = Globals.defaultTableSelectedRowBright;
	protected java.awt.Color unselectedEven = Globals.defaultTableCellBgColor2;
	protected java.awt.Color unselectedUneven = Globals.defaultTableCellBgColor1;

	public StandardListCellRenderer() {
		super();
	}

	public StandardListCellRenderer(String tooltipPrefix) {
		super();
		this.tooltipPrefix = tooltipPrefix;
	}

	public Component getListCellRendererComponent(
			JList list,
			Object value, // value to display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the focus
	{
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (c == null || !(c instanceof JComponent))
			return c;

		JComponent jc = (JComponent) c;

		// logging.info(this, "value " + value + " index " + index + " hasfocus " +
		// cellHasFocus);
		if (uniformColor == null)
			CellAlternatingColorizer.colorize(
					jc, isSelected, (index % 2 == 0), true,
					selectedEven, selectedUneven, unselectedEven, unselectedUneven);

		else {

			if (isSelected)
				jc.setBackground(uniformSelectedColor);
			else
				jc.setBackground(uniformColor);
		}

		jc.setFont(Globals.defaultFont);

		if (jc instanceof JLabel) {
			((JLabel) jc).setToolTipText(Globals.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));
		}

		return jc;
	}

	public void setUniformColors(java.awt.Color c, java.awt.Color cSelected)
	// ignore the inherited alterna
	{
		uniformColor = c;
		uniformSelectedColor = cSelected;
	}

	public void setAlternatingColors(
			java.awt.Color selectedEvenColor, java.awt.Color selectedUnevenColor,
			java.awt.Color unselectedEvenColor, java.awt.Color unselectedUnevenColor) {
		selectedEven = selectedEvenColor;
		selectedUneven = selectedUnevenColor;
		unselectedEven = unselectedEvenColor;
		unselectedUneven = unselectedUnevenColor;
	}

}
