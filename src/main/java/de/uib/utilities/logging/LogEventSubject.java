/*
  * LogEventSubject 
	* description: Implements an interface for the subject of log events
	* organization: uib.de
 * @author  R. Roeder 
 */
 
package de.uib.utilities.logging;
 
public interface LogEventSubject
{
	void registerLogEventObserver( LogEventObserver o);
}
 
