/* 
 *
 * 	uib, www.uib.de, 2009-2012
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.List;

public interface TableSource {
	public static int ROW_COUNT_START = 1;

	List<String> retrieveColumnNames();

	List<String> retrieveClassNames();

	// we get a new version
	List<List<Object>> retrieveRows();

	// Map<String, List<String>> getFunction(Integer defIndex, Integer

	void setRowCounting(boolean b);

	boolean isRowCounting();

	String getRowCounterName();

	void requestReload();

	void structureChanged();
}
