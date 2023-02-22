/*
  * DataChangedObserver 
	* description: Implements an interface that receives notifications that some data have changed
	* organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.utilities;

public interface DataChangedObserver {
	// means (and will be implemented): DataChangedSubject source
	void dataHaveChanged(Object source);
}
