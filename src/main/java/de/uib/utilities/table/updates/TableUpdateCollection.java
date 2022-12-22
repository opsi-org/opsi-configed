/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.HashSet;
import java.util.Vector;

public class TableUpdateCollection extends Vector<TableEditItem> {

	HashSet<de.uib.utilities.table.GenTableModel> modelsToReload = new HashSet<>();

	/*
	 * public boolean add(TableEditItem ob)
	 * {
	 * if (ob instanceof TableGenModelInsertItem)
	 * {
	 * modelsToReload.add( (GenTableModel)((TableGenModelInsertItem)ob).getModel() )
	 * ;
	 * }
	 * return super.add(ob);
	 * }
	 */

	/*
	 * public void sendReset()
	 * {
	 * Iterator iter = modelsToReload.iterator();
	 * 
	 * while (iter.hasNext())
	 * {
	 * ((GenTableModel)iter.next()).reset();
	 * }
	 * 
	 * modelsToReload.clear();
	 * }
	 */

}
