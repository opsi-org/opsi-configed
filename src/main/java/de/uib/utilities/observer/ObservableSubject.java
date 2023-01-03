/*
  * ObservableSubject 
	* description: customizes the Observable class
	* organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.utilities.observer;

import de.uib.utilities.logging.logging;

public class ObservableSubject extends java.util.Observable {
	@Override
	public void setChanged() {

		super.setChanged(); // was protected
	}

	@Override
	public void addObserver(java.util.Observer o) {
		logging.debug(this, "add Observer  " + o);
		super.addObserver(o);
	}
}
