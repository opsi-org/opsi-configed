/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.ArrayList;

public class MapDeliveryItem extends MapBasedTableEditItem {
	ArrayList<String> columnNames;
	ArrayList<String> classNames;
	ArrayList<Object> oldValues;
	ArrayList<Object> rowV;

	public MapDeliveryItem(Object source, int keyCol, ArrayList<String> columnNames, ArrayList<String> classNames,
			ArrayList<Object> oldValues, ArrayList<Object> rowV) {
		super(source, keyCol, columnNames, classNames, oldValues, rowV);
	}

	public MapDeliveryItem(Object source, int keyCol, ArrayList<String> columnNames, ArrayList<String> classNames,
			ArrayList<Object> rowV) {
		this(source, keyCol, columnNames, classNames, null, rowV);
	}

	public boolean keyChanged() {
		if (keyCol < 0)
			return false;

		// logging.debug (" keyChanged? oldValues " + oldValues);

		if (oldValues == null)
			return true;

		// logging.debug(" keyChanged? oldValues.get(keyCol).toString() " +
		// oldValues.get(keyCol).toString());
		// logging.debug(" =? rowV.get(keyCol).toString() " +
		// rowV.get(keyCol).toString());

		if (oldValues.get(keyCol).toString().equals(rowV.get(keyCol).toString())) {
			return true;
		}

		return false;
	}
}
