/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licences;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Globals;

public class MultiTablePanel extends de.uib.utilities.swing.tabbedpane.TabClientAdapter {
	protected AbstractControlMultiTablePanel controller;

	public MultiTablePanel(AbstractControlMultiTablePanel controller) {
		this.controller = controller;
	}

	@Override
	public void reset() {
		super.reset();

		controller.refreshTables();
		controller.initializeVisualSettings();
	}

	@Override
	public boolean mayLeave() {

		if (Globals.isGlobalReadOnly()) {
			return true;
		}

		boolean result = super.mayLeave();
		if (result) {
			result = controller.mayLeave();
		}

		return result;
	}

}
