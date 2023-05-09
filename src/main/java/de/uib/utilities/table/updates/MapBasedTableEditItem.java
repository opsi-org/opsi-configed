/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapBasedTableEditItem extends TableEditItem {
	private List<String> columnNames;
	private List<Object> rowV;

	public MapBasedTableEditItem(Object source, int keyCol, List<String> columnNames, List<Object> rowV) {
		this.columnNames = columnNames;
		this.rowV = rowV;
		this.source = source;
		this.keyCol = keyCol;
	}

	public Map<String, Object> getRowAsMap() {
		Map<String, Object> result = new HashMap<>();

		for (int i = 0; i < columnNames.size(); i++) {
			result.put(columnNames.get(i), rowV.get(i));

		}

		return result;
	}

	public String getKeyColumnStringValue() {
		if (keyCol < 0 || keyCol == columnNames.size()) {
			return null;
		}

		return rowV.get(keyCol).toString();
	}

	@Override
	public String toString() {
		return getRowAsMap().toString() + " keyCol " + keyCol + " source " + source;
	}

}
