/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */


package de.uib.utilities.observer;

public interface DataRefreshedObservable {
	void registerDataRefreshedObserver(DataRefreshedObserver ob);

	void unregisterDataRefreshedObserver(DataRefreshedObserver ob);

	void notifyDataRefreshedObservers(Object mesg);
}
