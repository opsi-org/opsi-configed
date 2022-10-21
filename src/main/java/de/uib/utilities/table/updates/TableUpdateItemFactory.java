/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.updates;

import java.util.*;

public abstract class TableUpdateItemFactory
{
	public abstract TableEditItem produceUpdateItem(Vector oldValues, Vector rowV);
	
	public abstract TableEditItem produceInsertItem(Vector rowV);
	
	public abstract TableEditItem produceDeleteItem(Vector rowV);
	
}
	
