/* 
*
* 	uib, www.uib.de, 2009-2012
* 
*	author Rupert Röder 
*
*/

package de.uib.utilities.table.provider;

import java.util.ArrayList;

public interface TableProvider {

	void setTableSource(TableSource source);

	ArrayList<String> getColumnNames();

	ArrayList<String> getClassNames();

	// should deliver a working copy of the data
	ArrayList<ArrayList<Object>> getRows();

	// should set the working copy as new original data
	void setWorkingCopyAsNewOriginalRows();

	// should initiate reloading the original data
	void requestReturnToOriginal();

	// should initiate reloading the original data
	void requestReloadRows();

	// should initiate reloading the metadata
	void structureChanged();

	// yields a column as ordered ArrayList
	ArrayList<String> getOrderedColumn(int col, boolean empty_allowed);
}
