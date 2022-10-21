/* 
 * RowMap is the map columnname -> value for some table row 
 *
 * 	uib, www.uib.de, 2012
 * 
 *	author Rupert Röder
 *
 */
 
package de.uib.utilities.table;



public class RowMap<K, V> extends java.util.HashMap<K, V> 
{
	@Override
	public V get(Object key)
	{
		V result = super.get(key);
		
		if (! (key instanceof java.lang.String) )
			return result;
		
		if (result == null)
			result = super.get(((java.lang.String) key).toUpperCase());
		
		if (result == null)
			result = super.get(((java.lang.String) key).toLowerCase());
		
		return result;
	}
}		
