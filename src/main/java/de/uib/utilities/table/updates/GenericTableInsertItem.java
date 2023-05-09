/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

// in comparison with the super class we add just the information that we insert data
public class GenericTableInsertItem extends MapBasedTableEditItem {
	public GenericTableInsertItem(Object source, int keyCol, List<String> columnNames, List<String> classNames,
			List<Object> rowV) {
		super(source, keyCol, columnNames, rowV);
	}
}
