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
	public abstract TableEditItem produceUpdateItem(List<Object> oldValues, List<Object> rowV);

	public abstract TableEditItem produceInsertItem(List<Object> rowV);

	public abstract TableEditItem produceDeleteItem(List<Object> rowV);

}
