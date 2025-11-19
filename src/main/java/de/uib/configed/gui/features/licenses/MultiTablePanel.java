/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.licenses;

import javax.swing.JPanel;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.AbstractControlMultiTablePanel;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;

public class MultiTablePanel extends JPanel {
	protected AbstractControlMultiTablePanel controller;

	public MultiTablePanel(AbstractControlMultiTablePanel controller) {
		this.controller = controller;
	}

	public void reset() {
		controller.refreshPanelGenEdits();
		controller.initializeVisualSettings();
	}

	/**
	 * commits all changes in this site
	 */
	public void saveSettings() {
		controller.getPanelGenEdits().stream().forEach(PanelGenEdit::commit);
	}

	public int mayLeave() {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			return -2;
		}

		return controller.mayLeave();
	}
}
