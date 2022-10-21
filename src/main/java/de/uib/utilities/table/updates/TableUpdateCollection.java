/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.updates;

import java.util.*;

public class TableUpdateCollection extends java.util.Vector<TableEditItem>
{

	HashSet<de.uib.utilities.table.GenTableModel> modelsToReload = new HashSet<de.uib.utilities.table.GenTableModel>();
	

	/*
	public boolean add(TableEditItem ob)
	{
		if (ob instanceof TableGenModelInsertItem)
		{
			modelsToReload.add( (GenTableModel)((TableGenModelInsertItem)ob).getModel() ) ;
		}
		return super.add(ob);
	}
	*/
	
	
	/*
	public void sendReset()
	{
		Iterator iter = modelsToReload.iterator();
		
		while (iter.hasNext())
		{
			((GenTableModel)iter.next()).reset();
		}
		
		modelsToReload.clear();
	}
	*/	
	
}
