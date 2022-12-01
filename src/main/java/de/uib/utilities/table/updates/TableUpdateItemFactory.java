/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.ArrayList;

public abstract class TableUpdateItemFactory {
	public abstract TableEditItem produceUpdateItem(ArrayList oldValues, ArrayList rowV);

	public abstract TableEditItem produceInsertItem(ArrayList rowV);

	public abstract TableEditItem produceDeleteItem(ArrayList rowV);

}
