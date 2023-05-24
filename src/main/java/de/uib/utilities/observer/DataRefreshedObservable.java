
package de.uib.utilities.observer;

public interface DataRefreshedObservable {
	void registerDataRefreshedObserver(DataRefreshedObserver ob);

	void unregisterDataRefreshedObserver(DataRefreshedObserver ob);

	void notifyDataRefreshedObservers(Object mesg);
}
