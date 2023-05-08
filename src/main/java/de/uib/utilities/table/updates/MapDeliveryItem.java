/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class MapDeliveryItem extends MapBasedTableEditItem {
	private List<String> columnNames;
	private List<String> classNames;
	private List<Object> oldValues;
	private List<Object> rowV;

	public MapDeliveryItem(Object source, int keyCol, List<String> columnNames, List<String> classNames,
			List<Object> oldValues, List<Object> rowV) {
		super(source, keyCol, columnNames, classNames, oldValues, rowV);
	}

	public MapDeliveryItem(Object source, int keyCol, List<String> columnNames, List<String> classNames,
			List<Object> rowV) {
		this(source, keyCol, columnNames, classNames, null, rowV);
	}

	@Override
	public boolean keyChanged() {
		if (keyCol < 0) {
			return false;
		}

		if (oldValues == null) {
			return true;
		}

		return oldValues.get(keyCol).toString().equals(rowV.get(keyCol).toString());
	}
}
