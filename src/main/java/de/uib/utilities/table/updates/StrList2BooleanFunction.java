/*
 * StrList2BooleanFunction.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import java.util.List;

public interface StrList2BooleanFunction {
	boolean sendUpdate(String id, List<String> list);
}
