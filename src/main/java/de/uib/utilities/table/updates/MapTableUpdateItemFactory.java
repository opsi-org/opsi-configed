/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class MapTableUpdateItemFactory implements TableUpdateItemInterface {
	protected List<String> columnNames;
	protected List<String> classNames;
	protected int keyCol;
	protected Object source;

	public MapTableUpdateItemFactory(Object source, List<String> columnNames, List<String> classNames, int keyCol) {
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.keyCol = keyCol;
		this.source = source;
	}

	public MapTableUpdateItemFactory(List<String> columnNames, List<String> classNames, int keyCol) {
		this(null, columnNames, classNames, keyCol);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public TableEditItem produceUpdateItem(List oldValues, List rowV) {
		return new MapDeliveryItem(source, keyCol, columnNames, classNames, oldValues, rowV);
	}

	@Override
	public TableEditItem produceInsertItem(List rowV) {
		return new MapDeliveryItem(source, keyCol, columnNames, classNames, rowV);
	}

	@Override
	public TableEditItem produceDeleteItem(List rowV) {
		return new MapDeleteItem(source, keyCol, columnNames, classNames, rowV);
	}

}
