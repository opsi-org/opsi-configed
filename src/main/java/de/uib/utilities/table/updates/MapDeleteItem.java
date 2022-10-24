/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.updates;

import java.util.*;

public class MapDeleteItem extends MapBasedTableEditItem
{
	public MapDeleteItem(Object source, int keyCol, Vector<String> columnNames, Vector<String> classNames, Vector<Object> rowV) 
	{
		super(source, keyCol, columnNames, classNames, null, rowV);
	}
	
	public boolean keyChanged()
	{
		return false;
	}
}
