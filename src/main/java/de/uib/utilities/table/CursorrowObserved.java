/*
 *
 * 	uib, www.uib.de, 2020
 * 
 *	authors Rupert Röder
 *
 */

package de.uib.utilities.table;

import java.util.HashSet;

import de.uib.utilities.logging.logging;

public class CursorrowObserved {
	java.util.Set<CursorrowObserver> observers = new HashSet<>();

	public void setChanged() {

	}

	public void clearChanged() {

	}

	public void notifyObservers() {

	}

	public void notifyObservers(int newrow) {
		logging.info(this, "notify Observers with " + newrow);
		for (CursorrowObserver o : observers) {
			o.rowUpdated(newrow);
		}
	}

	public void addObserver(CursorrowObserver o) {
		logging.debug(this, "add Observer  " + o);
		observers.add(o);
	}
}
