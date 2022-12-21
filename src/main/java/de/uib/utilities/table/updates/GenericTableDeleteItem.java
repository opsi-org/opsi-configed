/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.Vector;

// in comparison with the super class we add just the information that we delete data
public class GenericTableDeleteItem extends MapBasedTableEditItem {
	public GenericTableDeleteItem(Object source, int keyCol, Vector<String> columnNames, Vector<String> classNames,
			Vector<Object> rowV) {
		super(source, keyCol, columnNames, classNames, null, rowV);
	}
}
