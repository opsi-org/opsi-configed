/* 
 *
 * 	uib, www.uib.de, 2009-2012
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.Vector;

public interface TableSource {
	public static int ROW_COUNT_START = 1;

	Vector<String> retrieveColumnNames();

	Vector<String> retrieveClassNames();

	// we get a new version
	Vector<Vector<Object>> retrieveRows();

	// Map<String, List<String>> getFunction(Integer defIndex, Integer

	void setRowCounting(boolean b);

	boolean isRowCounting();

	String getRowCounterName();

	void requestReload();

	void structureChanged();
}
