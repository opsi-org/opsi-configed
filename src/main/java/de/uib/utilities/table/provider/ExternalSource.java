/* 
 *
 * 	uib, www.uib.de, 2014
 * 
 *	author Rupert Röder 
 *
 */
 
 
package de.uib.utilities.table.provider;

import java.util.*;

public class ExternalSource implements TableSource
//adapter for external source for table data
{
	protected Vector<String> columnNames;
	
	protected Vector<String> classNames;
	
	protected boolean reloadRequested = true;
	
	protected RowsProvider rowsProvider;
	
	public ExternalSource(Vector<String> columnNames, Vector<String> classNames,
		RowsProvider rowsProvider)
	{
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.rowsProvider = rowsProvider;
		
	}
	
	public Vector<String> retrieveColumnNames()
	{
		return columnNames;
	}
	
	public Vector<String> retrieveClassNames()
	{
		return classNames;
	}
	
	public Vector<Vector<Object>> retrieveRows()
	{
		if (reloadRequested)
		{
			rowsProvider.requestReload();
			reloadRequested = false;
		}
		//System.out.println (" --- MapSource retrieveRows() rows.size(): " + rows.size());
		return rowsProvider.getRows();
	}
	
	public void requestReload()
	{
		reloadRequested = true;
	}
	
	@Override
	public String getRowCounterName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRowCounting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRowCounting(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void structureChanged() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
