/*
 * StrList2BooleanFunction.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import java.util.List;

public abstract class StrList2BooleanFunction {
	public abstract boolean sendUpdate(String id, List list);
}
