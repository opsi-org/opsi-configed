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
	public void clearChanged() {
		
		super.clearChanged(); // was protected
	}

	@Override
	public void notifyObservers() {
		
		super.notifyObservers();
	}

	@Override
	public void notifyObservers(Object arg) {
		
		super.notifyObservers(arg);
	}

	@Override
	public void addObserver(java.util.Observer o) {
		logging.debug(this, "add Observer  " + o);
		super.addObserver(o);
	}
}
