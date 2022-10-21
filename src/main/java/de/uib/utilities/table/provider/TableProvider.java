 /* 
 *
 * 	uib, www.uib.de, 2009-2012
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.provider;

import java.util.*;

public interface TableProvider
{
	
	void setTableSource(TableSource source);
	
	Vector<String> getColumnNames();
	
	Vector<String> getClassNames();
	
	// should deliver a working copy of the data
	Vector<Vector<Object>>getRows();
	
	// should set the working copy as new original data
	void setWorkingCopyAsNewOriginalRows();
	
	// should initiate reloading the original data
	void requestReturnToOriginal();
	
	// should initiate reloading the original data
	void requestReloadRows();
	
	//should initiate reloading the metadata
	void structureChanged();
	
	//yields a column as ordered vector
	Vector<String> getOrderedColumn(int col, boolean empty_allowed);
}
