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

public abstract class AbstractDataChangedKeeper {
	protected boolean dataChanged;

	public void dataHaveChanged(Object source) {
		Logging.debug(this, "dataHaveChanged ", source);
		dataChanged = true;
	}

	public int askSave() {
		// We have NO_OPTION as default result since this will mean "do not save" if no changes
		// have been made
		int result = JOptionPane.NO_OPTION;

		if (this.dataChanged) {
			result = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.confirmUnsavedChanges"),
					Configed.getResourceValue("ConfigedMain.unsavedChanges"), JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		}

		return result;
	}

	public boolean isDataChanged() {
		return dataChanged;
	}

	public void cancel() {
		Logging.info(this, "cancel");
		dataChanged = false;
	}

	public abstract void save();
}
