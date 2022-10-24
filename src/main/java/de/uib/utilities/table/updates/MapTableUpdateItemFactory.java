/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.updates;

import java.util.*;

public class MapTableUpdateItemFactory extends TableUpdateItemFactory
{
	protected Vector<String> columnNames;
	protected Vector<String> classNames;
	protected int keyCol;
	protected Object source;
	
	public MapTableUpdateItemFactory(
			Object source,
			Vector<String> columnNames, Vector<String> classNames,
			int keyCol)
	{
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.keyCol = keyCol;
		this.source = source;
	}
	
	public MapTableUpdateItemFactory(
			Vector<String> columnNames, Vector<String> classNames,
			int keyCol)
	{
		this(null, columnNames, classNames, keyCol);
	}
	
	public void setSource(Object source)
	{
		this.source = source;
	}
	
	public TableEditItem produceUpdateItem(Vector oldValues, Vector rowV)
	{
		return new MapDeliveryItem(source, keyCol, columnNames, classNames, oldValues, rowV);
	}
	
	public TableEditItem produceInsertItem(Vector rowV)
	{
		return new MapDeliveryItem(source, keyCol, columnNames, classNames, rowV);
	}
	
	public TableEditItem produceDeleteItem(Vector rowV)
	{
		return new MapDeleteItem(source, keyCol, columnNames, classNames, rowV);
	}
	
}
	
