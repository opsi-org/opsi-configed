
package de.uib.utilities.observer;

import java.util.Set;

/**
 * Observing RunningInstaces
 * 
 * @version 2016/01/15
 * @author Rupert Roeder
 */

public interface RunningInstancesObserver<T> {
	void executeCommandOnInstances(String command, Set<T> instances);

	void instancesChanged(Set<T> instances);
}
