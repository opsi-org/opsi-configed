/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.uib.configed.Globals;

public class JComboBoxSimpleToolTip extends JComboBox<String> {

	private static final int FILL_LENGTH = 40;

	public JComboBoxSimpleToolTip() {
		super();
		super.setRenderer(new MyComboBoxRenderer());
	}

	private static class MyComboBoxRenderer extends BasicComboBoxRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			String val;
			if (value == null) {
				val = "";
			} else {
				val = value.toString();
			}

			setText(val);

			String tooltipText = Globals.fillStringToLength(val + " ", FILL_LENGTH);

			setToolTipText(tooltipText);

			return this;
		}
	}
}
