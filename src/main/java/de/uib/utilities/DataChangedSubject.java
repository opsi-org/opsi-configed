/*
  * DataChangedSubject 
	* description: Implements an interface for the subject of data changes (and change notifications)
	* organization: uib.de
 * @author  R. Roeder 
 */
 
package de.uib.utilities;
 
public interface DataChangedSubject
{
	void registerDataChangedObserver( DataChangedObserver o);
}
 
