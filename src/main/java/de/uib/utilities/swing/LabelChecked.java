/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import javax.swing.Icon;
import javax.swing.JLabel;

import de.uib.configed.Globals;

public class LabelChecked extends JLabel {

	private Icon iconChecked = Globals.createImageIcon("images/checked_box_blue_14.png", "");
	private Icon iconUnchecked = Globals.createImageIcon("images/checked_box_blue_empty_14.png", "");
	private Icon iconEmpty = Globals.createImageIcon("images/checked_void.png", "");

	public LabelChecked() {
		setUnchecked();
	}

	public void setValue(Boolean b) {
		if (b == null) {
			setEmpty();
		} else if (b) {
			setChecked();
		} else {
			setUnchecked();
		}
	}

	private void setChecked() {
		setIcon(iconChecked);
	}

	private void setUnchecked() {
		setIcon(iconUnchecked);
	}

	private void setEmpty() {
		setIcon(iconEmpty);
	}
}
