/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing.list;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class StandardListCellRenderer extends DefaultListCellRenderer {
	// this is the normal preferred height for components with content.
	// We want this height also for empty components
	private static final int CELL_HEIGHT = 20;
	protected static final int FILL_LENGTH = 20;

	public StandardListCellRenderer() {
		super();
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		Dimension prefDimension = getPreferredSize();
		prefDimension.setSize(prefDimension.getWidth(), CELL_HEIGHT);
		setPreferredSize(prefDimension);

		return this;
	}
}
