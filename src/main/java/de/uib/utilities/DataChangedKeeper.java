/*
  * DataChangedKeeper
	* description: keeps the information when data have changed
	* organization: uib.de
	* copyright:     Copyright (c) 2000-2022
	* @author  R. Roeder 
 */

package de.uib.utilities;

import javax.swing.JOptionPane;

import de.uib.utilities.logging.logging;

public class DataChangedKeeper implements DataChangedObserver {
	protected boolean dataChanged = false;

	protected ActUpon actUpon;

	public class TellWhat implements ActUpon {
		@Override
		public void act(Object source) {
			JOptionPane.showMessageDialog(null, "" + source, "alert", JOptionPane.INFORMATION_MESSAGE);
		}
	};

	public DataChangedKeeper() {

	}

	public void setActingOnSource(ActUpon actUpon) {
		if (actUpon != null)
			this.actUpon = actUpon;
	}

	@FunctionalInterface
	public interface ActUpon {
		public void act(Object source);
	}

	@Override
	public void dataHaveChanged(Object source) {
		logging.debug(this, "dataHaveChanged " + source);
		dataChanged = true;

		if (actUpon != null)
			actUpon.act(source);

	}

	public void actionOnChangeXXX(boolean condition, Object source) {
		if (condition) {
			JOptionPane.showMessageDialog(null, "" + source, "alert", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/*
	 * public void actionOnChange( boolean condition, Object source, OnChangeExecute
	 * )
	 * {
	 * if (condition)
	 * {
	 * JOptionPane.showMessageDialog(null, "" + source, "alert",
	 * JOptionPane.INFORMATION_MESSAGE);
	 * }
	 * }
	 */

	public boolean isDataChanged() {
		boolean b = dataChanged;
		return b;
	}

	public void unsetDataChanged() {
		dataChanged = false;
	}

	public void checkAndSave() {
		if (dataChanged)
			dataChanged = false;
		// overwrite e.g. with an dialog
	}

}
