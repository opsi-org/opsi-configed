package de.uib.utilities.swing;

import javax.swing.Icon;
import javax.swing.JLabel;

import de.uib.configed.Globals;

public class LabelChecked extends JLabel {

	Icon iconChecked = Globals.createImageIcon("images/checked_box_blue_14.png", "");
	Icon iconUnchecked = Globals.createImageIcon("images/checked_box_blue_empty_14.png", "");
	Icon iconEmpty = Globals.createImageIcon("images/checked_void.png", "");

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
