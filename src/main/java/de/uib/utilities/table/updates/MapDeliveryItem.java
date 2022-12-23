/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.Vector;

public class MapDeliveryItem extends MapBasedTableEditItem {
	Vector<String> columnNames;
	Vector<String> classNames;
	Vector<Object> oldValues;
	Vector<Object> rowV;

	public MapDeliveryItem(Object source, int keyCol, Vector<String> columnNames, Vector<String> classNames,
			Vector<Object> oldValues, Vector<Object> rowV) {
		super(source, keyCol, columnNames, classNames, oldValues, rowV);
	}

	public MapDeliveryItem(Object source, int keyCol, Vector<String> columnNames, Vector<String> classNames,
			Vector<Object> rowV) {
		this(source, keyCol, columnNames, classNames, null, rowV);
	}

	@Override
	public boolean keyChanged() {
		if (keyCol < 0)
			return false;

		

		if (oldValues == null)
			return true;

		// logging.debug(" keyChanged? oldValues.get(keyCol).toString() " +
		
		// logging.debug(" =? rowV.get(keyCol).toString() " +
		

		if (oldValues.get(keyCol).toString().equals(rowV.get(keyCol).toString())) {
			return true;
		}

		return false;
	}
}
