/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.ArrayList;

public class MapTableUpdateItemFactory extends TableUpdateItemFactory {
	protected ArrayList<String> columnNames;
	protected ArrayList<String> classNames;
	protected int keyCol;
	protected Object source;

	public MapTableUpdateItemFactory(Object source, ArrayList<String> columnNames, ArrayList<String> classNames,
			int keyCol) {
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.keyCol = keyCol;
		this.source = source;
	}

	public MapTableUpdateItemFactory(ArrayList<String> columnNames, ArrayList<String> classNames, int keyCol) {
		this(null, columnNames, classNames, keyCol);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public TableEditItem produceUpdateItem(ArrayList oldValues, ArrayList rowV) {
		return new MapDeliveryItem(source, keyCol, columnNames, classNames, oldValues, rowV);
	}

	public TableEditItem produceInsertItem(ArrayList rowV) {
		return new MapDeliveryItem(source, keyCol, columnNames, classNames, rowV);
	}

	public TableEditItem produceDeleteItem(ArrayList rowV) {
		return new MapDeleteItem(source, keyCol, columnNames, classNames, rowV);
	}

}
