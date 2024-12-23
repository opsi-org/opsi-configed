/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import javax.swing.JOptionPane;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utils.logging.Logging;

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
