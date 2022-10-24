/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.updates;

import java.util.*;

public class MapDeliveryItem extends MapBasedTableEditItem
{
	Vector<String> columnNames;
	Vector<String> classNames;
	Vector<Object> oldValues;
	Vector<Object> rowV;
	
	
	public MapDeliveryItem
		(	Object source, 
			int keyCol, 
			Vector<String> columnNames, Vector<String> classNames, 
			Vector<Object> oldValues, Vector<Object> rowV)
	{
		super(source, keyCol, columnNames, classNames, oldValues, rowV);
	}
	
	public MapDeliveryItem(Object source, int keyCol, Vector<String> columnNames, Vector<String> classNames, Vector<Object> rowV) 
	{
		this(source, keyCol, columnNames, classNames, null, rowV);
	}
	
	public boolean keyChanged()
	{
		if (keyCol < 0)
			return false;
		
		//System.out.println (" keyChanged? oldValues " + oldValues);
		
		if (oldValues == null)
			return true;
		
		//System.out.println(" keyChanged? oldValues.get(keyCol).toString() " + oldValues.get(keyCol).toString());
		//System.out.println(" =? rowV.get(keyCol).toString() " + rowV.get(keyCol).toString());
		
		if	(oldValues.get(keyCol).toString().equals( rowV.get(keyCol).toString() )
			)
		{
			return true;
		}
		
		return false;
	}
}
