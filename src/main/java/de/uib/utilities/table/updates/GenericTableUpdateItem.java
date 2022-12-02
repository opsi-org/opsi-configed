/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.Vector;

// in comparison with the super class we add just the information that we update data
public class GenericTableUpdateItem extends MapBasedTableEditItem {
	public GenericTableUpdateItem(Object source,
			int keyCol,
			Vector<String> columnNames, Vector<String> classNames,
			Vector<Object> oldValues, Vector<Object> rowV) {
		super(source, keyCol, columnNames, classNames, oldValues, rowV);
	}
}
