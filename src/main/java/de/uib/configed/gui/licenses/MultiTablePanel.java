/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licenses;

import javax.swing.JPanel;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class MultiTablePanel extends JPanel {
	protected AbstractControlMultiTablePanel controller;

	public MultiTablePanel(AbstractControlMultiTablePanel controller) {
		this.controller = controller;
	}

	public void reset() {
		controller.refreshTables();
		controller.initializeVisualSettings();
	}

	public boolean mayLeave() {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			return true;
		}

		return controller.mayLeave();
	}
}
