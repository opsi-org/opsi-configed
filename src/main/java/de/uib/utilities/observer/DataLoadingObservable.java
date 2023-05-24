/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */


package de.uib.utilities.observer;

public interface DataLoadingObservable {
	void registerDataLoadingObserver(DataLoadingObserver ob);

	void unregisterDataLoadingObserver(DataLoadingObserver ob);

	void notifyDataLoadingObservers(Object mesg);
}
