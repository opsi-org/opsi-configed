/* 
 *
 * 	uib, www.uib.de, 2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.Map;

public interface MapRetriever {
	Map<String, Map<String, Object>> retrieveMap();
}
