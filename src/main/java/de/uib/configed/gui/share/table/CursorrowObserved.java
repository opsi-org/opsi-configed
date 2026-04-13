/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table;

import java.util.HashSet;
import java.util.Set;

import de.uib.configed.share.logging.Logging;

public class CursorrowObserved {
	private Set<CursorrowObserver> observers = new HashSet<>();

	public void notifyObservers(int newrow) {
		Logging.info(this, "notify Observers with ", newrow);
		for (CursorrowObserver o : observers) {
			o.rowUpdated(newrow);
		}
	}

	public void addObserver(CursorrowObserver o) {
		Logging.debug(this, "add Observer  ", o);
		observers.add(o);
	}
}
