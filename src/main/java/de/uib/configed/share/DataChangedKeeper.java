/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import javax.swing.JOptionPane;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.logging.Logging;

public class DataChangedKeeper implements DataChangedObserver {
	protected boolean dataChanged;

	@Override
	public void dataHaveChanged(Object source) {
		Logging.debug(this, "dataHaveChanged ", source);
		dataChanged = true;
	}

	public boolean askSave() {
		boolean result = false;

		if (this.dataChanged) {
			int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.reminderSaveConfig"),
					Configed.getResourceValue("PanelGenEditTable.saveData"), JOptionPane.YES_NO_OPTION);

			result = answer == JOptionPane.YES_OPTION;
		}

		return result;
	}

	public boolean isDataChanged() {
		return dataChanged;
	}
}
