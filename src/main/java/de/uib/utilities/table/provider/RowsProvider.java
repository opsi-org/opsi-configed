/* 
 *
 * 	uib, www.uib.de, 2014
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.List;

/**
 * delivers rows which are externally stored
 */
public interface RowsProvider {
	void requestReload();

	List<List<Object>> getRows();
}
