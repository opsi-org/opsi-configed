/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table;

import java.util.HashSet;
import java.util.Set;

import de.uib.utils.logging.Logging;

public class CursorrowObserved {
	private Set<CursorrowObserver> observers = new HashSet<>();

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
