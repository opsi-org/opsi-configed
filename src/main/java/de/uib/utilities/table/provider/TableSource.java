/* 
 *
 * 	uib, www.uib.de, 2009-2012
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;

public interface TableSource {
	public static int ROW_COUNT_START = 1;

	ArrayList<String> retrieveColumnNames();

	ArrayList<String> retrieveClassNames();

	// we get a new version
	ArrayList<ArrayList<Object>> retrieveRows();

	// Map<String, java.util.List<String>> getFunction(Integer defIndex, Integer
	// valIndex);

	void setRowCounting(boolean b);

	boolean isRowCounting();

	String getRowCounterName();

	void requestReload();

	void structureChanged();
}
