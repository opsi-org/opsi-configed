/*
 * MapBasedUpdater.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import java.util.Map;

public abstract class MapBasedUpdater {
	public abstract String sendUpdate(Map<String, Object> rowmap);

	public abstract boolean sendDelete(Map<String, Object> rowmap);
}
