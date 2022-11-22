/* 
 *
 * 	uib, www.uib.de, 2014
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.Vector;

/**
 * delivers rows which are externally stored
 */
public interface RowsProvider {
	public void requestReload();

	public Vector<Vector<Object>> getRows();
}
