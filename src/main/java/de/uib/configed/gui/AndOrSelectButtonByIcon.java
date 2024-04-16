/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import de.uib.utils.logging.Logging;

/**
 * Select AND or OR Created for the ClientSelectionDialog.
 */
public class AndOrSelectButtonByIcon extends IconAsButton {
	public AndOrSelectButtonByIcon() {
		super("and/or", "images/boolean_and_or_disabled.png", "images/boolean_and_or_over.png",
				"images/boolean_and_or.png", null);

		super.addActionListener(event -> setActivated(!activated));
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
}
