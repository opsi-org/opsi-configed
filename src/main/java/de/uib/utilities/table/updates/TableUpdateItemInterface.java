/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

public interface TableUpdateItemInterface {
	public abstract TableEditItem produceUpdateItem(List oldValues, List rowV);

	public abstract TableEditItem produceInsertItem(List rowV);

	public abstract TableEditItem produceDeleteItem(List rowV);

}
