/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities;

import java.util.function.Consumer;

import javax.swing.JOptionPane;

import de.uib.utilities.logging.Logging;

public class DataChangedKeeper implements DataChangedObserver {
	protected boolean dataChanged;

	private Consumer<Object> actUpon;

	public static class TellWhat implements Consumer<Object> {
		@Override
		public void accept(Object source) {
			JOptionPane.showMessageDialog(null, "" + source, "alert", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void setActingOnSource(Consumer<Object> actUpon) {
		if (actUpon != null) {
			this.actUpon = actUpon;
		}
	}

	@Override
	public void dataHaveChanged(Object source) {
		Logging.debug(this, "dataHaveChanged " + source);
		dataChanged = true;

		if (actUpon != null) {
			actUpon.accept(source);
		}
	}

	public boolean isDataChanged() {
		return dataChanged;
	}

	public void unsetDataChanged() {
		dataChanged = false;
	}

	public void checkAndSave() {
		if (dataChanged) {
			dataChanged = false;
		}
		// overwrite e.g. with an dialog
	}
}
