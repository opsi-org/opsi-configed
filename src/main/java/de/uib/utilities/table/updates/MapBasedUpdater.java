/*
 * MapBasedUpdater.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import java.util.Map;

public abstract class MapBasedUpdater{	
	abstract public String sendUpdate(Map<String, Object> rowmap);
	abstract public boolean sendDelete(Map<String, Object> rowmap);
}
	
