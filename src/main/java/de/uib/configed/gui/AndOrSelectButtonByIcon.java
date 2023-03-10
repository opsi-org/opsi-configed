package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.uib.utilities.logging.Logging;

/**
 * Select AND or OR Created for the ClientSelectionDialog.
 */
public class AndOrSelectButtonByIcon extends IconAsButton {

	public AndOrSelectButtonByIcon() {
		super("and/or", "images/boolean_and_or_disabled.png", "images/boolean_and_or_over.png",
				"images/boolean_and_or.png", null);

		super.addActionListener(new ButtonActionListener());
	}

	public boolean isAndSelected() {
		Logging.debug(this, "isEnabled " + isEnabled());
		return !isActivated();
	}

	public boolean isOrSelected() {
		Logging.debug(this, "isEnabled " + isEnabled());
		return isActivated();
	}

	public void selectAnd() {
		setActivated(false);
	}

	public void selectOr() {
		setActivated(true);
	}

	private class ButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Logging.debug(this, "actionPerformed  " + e + " activated " + activated);
			setActivated(!activated);

		}
	}
}
