/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MapBasedTableEditItem extends TableEditItem {
	Vector<String> columnNames;
	Vector<String> classNames;
	Vector<Object> oldValues;
	Vector<Object> rowV;

	public MapBasedTableEditItem(Object source, int keyCol, Vector<String> columnNames, Vector<String> classNames,
			Vector<Object> oldValues, Vector<Object> rowV) {
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.oldValues = oldValues;
		this.rowV = rowV;
		this.source = source;
		this.keyCol = keyCol;
	}

	public MapBasedTableEditItem(Object source, int keyCol, Vector<String> columnNames, Vector<String> classNames,
			Vector<Object> rowV) {
		this(source, keyCol, columnNames, classNames, null, rowV);
	}

	public Map<String, Object> getRowAsMap() {
		Map<String, Object> result = new HashMap<String, Object>();

		for (int i = 0; i < columnNames.size(); i++) {
			result.put(columnNames.get(i), rowV.get(i));
			// logging.debug( "MapBasedTableEditItem.getRowAsMap(): " +
			// columnNames.get(i) + ", " + rowV.get(i) );
		}

		return result;
	}

	public String getKeyColumnStringValue() {
		if (keyCol < 0 || keyCol == columnNames.size())
			return null;

		return (rowV.get(keyCol).toString());
	}

	@Override
	public String toString() {
		return getRowAsMap().toString() + " keyCol " + keyCol + " source " + source;
	}

}
