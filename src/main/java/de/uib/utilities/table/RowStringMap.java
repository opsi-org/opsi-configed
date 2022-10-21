/* 
 * Map<String, String> is the map columnname -> value as String for some table row 
 *
 * 	uib, www.uib.de, 2012-2015
 * 
 *	author Rupert Röder
 *
 */
 
package de.uib.utilities.table;

import java.util.HashMap;

public class RowStringMap extends HashMap<String, Object> 
{
	@Override
	public String get(Object key)
	{
		String result = (String) super.get((String) key);
		
		if (result == null)
			result = (String) super.get( ((String)key).toUpperCase() );
		
		if (result == null)
			result = (String) super.get( ((String)key).toLowerCase() );
		
		return result;
	}
}
