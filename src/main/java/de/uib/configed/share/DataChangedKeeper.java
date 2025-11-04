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
			int answer = JOptionPane
					.showOptionDialog(ConfigedMain.getMainFrame(),
							Configed.getResourceValue("ConfigedMain.confirmUnsavedChanges"),
							Configed.getResourceValue("ConfigedMain.unsavedChanges"), JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, new String[] { Configed.getResourceValue("save"),
									Configed.getResourceValue("discard"), Configed.getResourceValue("buttonCancel") },
							null);

			result = answer == JOptionPane.YES_OPTION;
		}

		return result;
	}

	public boolean isDataChanged() {
		return dataChanged;
	}
}
