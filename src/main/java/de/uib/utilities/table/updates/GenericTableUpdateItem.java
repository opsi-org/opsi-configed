/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

// in comparison with the super class we add just the information that we update data
public class GenericTableUpdateItem extends MapBasedTableEditItem {
	public GenericTableUpdateItem(Object source, int keyCol, List<String> columnNames, List<String> classNames,
			List<Object> oldValues, List<Object> rowV) {
		super(source, keyCol, columnNames, rowV);
	}
}
