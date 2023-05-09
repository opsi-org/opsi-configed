/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

// in comparison with the super class we add just the information that we delete data
public class GenericTableDeleteItem extends MapBasedTableEditItem {
	public GenericTableDeleteItem(Object source, int keyCol, List<String> columnNames, List<String> classNames,
			List<Object> rowV) {
		super(source, keyCol, columnNames, rowV);
	}
}
