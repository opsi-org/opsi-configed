/*
 *
 * 	uib, www.uib.de, 2020
 * 
 *	authors Rupert Röder
 *
 */

package de.uib.utilities.table;

import java.util.HashSet;
import java.util.Set;

import de.uib.utilities.logging.Logging;

public class CursorrowObserved {
	Set<CursorrowObserver> observers = new HashSet<>();

	public void notifyObservers(int newrow) {
		Logging.info(this, "notify Observers with " + newrow);
		for (CursorrowObserver o : observers) {
			o.rowUpdated(newrow);
		}
	}

	public void addObserver(CursorrowObserver o) {
		Logging.debug(this, "add Observer  " + o);
		observers.add(o);
	}
}
