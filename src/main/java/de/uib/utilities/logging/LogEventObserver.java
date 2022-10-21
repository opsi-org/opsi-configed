/*
  * LogEventObserver 
	* description: Implements an interface that receives notifications that a log event occurred
	* organization: uib.de
 * @author  R. Roeder 
 */
 
package de.uib.utilities.logging;
 
public interface LogEventObserver
{
	void logEventOccurred(LogEvent event);  
}
 
