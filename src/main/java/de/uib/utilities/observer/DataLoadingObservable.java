
package de.uib.utilities.observer;

public interface DataLoadingObservable {
	void registerDataLoadingObserver(DataLoadingObserver ob);

	void unregisterDataLoadingObserver(DataLoadingObserver ob);

	void notifyDataLoadingObservers(Object mesg);
}
