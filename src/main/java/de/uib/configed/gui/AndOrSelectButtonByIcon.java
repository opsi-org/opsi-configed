package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.uib.utilities.logging.logging;

/**
 * Select AND or OR Created for the ClientSelectionDialog.
 */
public class AndOrSelectButtonByIcon extends IconAsButton {

	public AndOrSelectButtonByIcon() {
		super("and/or", "images/boolean_and_or_disabled.png", "images/boolean_and_or_over.png",
				"images/boolean_and_or.png", null);
		addActionListener(new ButtonActionListener());
	}

	public boolean isAndSelected() {
		logging.debug(this, "isEnabled " + isEnabled());
		return !isActivated();
	}

	public boolean isOrSelected() {
		logging.debug(this, "isEnabled " + isEnabled());
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
			logging.debug(this, "actionPerformed  " + e + " activated " + activated);
			setActivated(!activated);
			// createActionEvents();
		}
	}
}
