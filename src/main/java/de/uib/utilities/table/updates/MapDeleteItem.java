/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class MapDeleteItem extends MapBasedTableEditItem {
	public MapDeleteItem(Object source, int keyCol, List<String> columnNames, List<String> classNames,
			List<Object> rowV) {
		super(source, keyCol, columnNames, classNames, null, rowV);
	}

	@Override
	public boolean keyChanged() {
		return false;
	}
}
