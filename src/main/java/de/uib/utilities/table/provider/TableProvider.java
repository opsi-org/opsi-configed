/* 
*
* 	uib, www.uib.de, 2009-2012
* 
*	author Rupert Röder 
*
*/

package de.uib.utilities.table.provider;

import java.util.List;

public interface TableProvider {

	void setTableSource(TableSource source);

	List<String> getColumnNames();

	List<String> getClassNames();

	// should deliver a working copy of the data
	List<List<Object>> getRows();

	// should set the working copy as new original data
	void setWorkingCopyAsNewOriginalRows();

	// should initiate reloading the original data
	void requestReturnToOriginal();

	// should initiate reloading the original data
	void requestReloadRows();

	// should initiate reloading the metadata
	void structureChanged();

	// yields a column as ordered List
	List<String> getOrderedColumn(int col, boolean empty_allowed);
}
