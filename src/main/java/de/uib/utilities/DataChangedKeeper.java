/*
  * DataChangedKeeper
	* description: keeps the information when data have changed
	* organization: uib.de
	* copyright:     Copyright (c) 2000-2022
	* @author  R. Roeder 
 */

package de.uib.utilities;

import java.util.function.Consumer;

import javax.swing.JOptionPane;

import de.uib.utilities.logging.Logging;

public class DataChangedKeeper implements DataChangedObserver {
	protected boolean dataChanged;

	protected Consumer<Object> actUpon;

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
