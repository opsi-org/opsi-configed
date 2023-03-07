/* 
 *
 * 	uib, www.uib.de, 2014
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.List;

public class ExternalSource implements TableSource {
	// adapter for external source for table data

	protected List<String> columnNames;

	protected List<String> classNames;

	protected boolean reloadRequested = true;

	protected RowsProvider rowsProvider;

	public ExternalSource(List<String> columnNames, List<String> classNames, RowsProvider rowsProvider) {
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.rowsProvider = rowsProvider;

	}

	@Override
	public List<String> retrieveColumnNames() {
		return columnNames;
	}

	@Override
	public List<String> retrieveClassNames() {
		return classNames;
	}

	@Override
	public List<List<Object>> retrieveRows() {
		if (reloadRequested) {
			rowsProvider.requestReload();
			reloadRequested = false;
		}

		return rowsProvider.getRows();
	}

	@Override
	public void requestReload() {
		reloadRequested = true;
	}

	@Override
	public String getRowCounterName() {
		return null;
	}

	@Override
	public boolean isRowCounting() {
		return false;
	}

	@Override
	public void setRowCounting(boolean b) {
		/* Not needed */}
}
