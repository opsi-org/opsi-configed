/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing.list;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import utils.Utils;

public class StandardListCellRenderer extends DefaultListCellRenderer {
	// this is the normal preferred height for components with content.
	// We want this height also for empty components
	private static final int CELL_HEIGHT = 20;
	protected static final int FILL_LENGTH = 20;

	protected String tooltipPrefix = "";

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

		if (jc instanceof JLabel) {
			((JLabel) jc).setToolTipText(Utils.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));
		}

		return jc;
	}
}
