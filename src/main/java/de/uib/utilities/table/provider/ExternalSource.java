/* 
 *
 * 	uib, www.uib.de, 2014
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;

public class ExternalSource implements TableSource
// adapter for external source for table data
{
	protected ArrayList<String> columnNames;

	protected ArrayList<String> classNames;

	protected boolean reloadRequested = true;

	protected RowsProvider rowsProvider;

	public ExternalSource(ArrayList<String> columnNames, ArrayList<String> classNames, RowsProvider rowsProvider) {
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.rowsProvider = rowsProvider;

	}

	public ArrayList<String> retrieveColumnNames() {
		return columnNames;
	}

	public ArrayList<String> retrieveClassNames() {
		return classNames;
	}

	public ArrayList<ArrayList<Object>> retrieveRows() {
		if (reloadRequested) {
			rowsProvider.requestReload();
			reloadRequested = false;
		}
		// logging.debug (" --- MapSource retrieveRows() rows.size(): " +
		// rows.size());
		return rowsProvider.getRows();
	}

	public void requestReload() {
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
