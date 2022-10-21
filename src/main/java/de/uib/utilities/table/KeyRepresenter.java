/* 
 * KeyRepresenter provides a representation of a (primary) key by some String value
 * which is generated from the entries in other columns
 *
 * 	uib, www.uib.de, 2012
 * 
 *	author Rupert Röder
 *
 */
 
package de.uib.utilities.table;

public abstract class KeyRepresenter<K>
{
	public abstract String represents(K key, RowStringMap rowMap);
}
