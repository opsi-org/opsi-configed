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
	TableEditItem produceUpdateItem(List<Object> oldValues, List<Object> rowV);

	TableEditItem produceInsertItem(List<Object> rowV);

	TableEditItem produceDeleteItem(List<Object> rowV);
}
