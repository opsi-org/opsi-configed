/* 
 *
 * 	uib, www.uib.de, 2009-2010
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import de.uib.utilities.logging.Logging;

public class DefaultTableProvider implements TableProvider {
	protected TableSource source;
	protected List<String> columnNames;
	protected List<String> classNames;
	protected List<List<Object>> rows;
	protected List<List<Object>> rowsCopy;

	public DefaultTableProvider(TableSource source) {
		this.source = source;
	}

	@Override
	public void setTableSource(TableSource source) {
		this.source = source;
	}

	@Override
	public List<String> getColumnNames() {
		if (columnNames == null) {
			columnNames = source.retrieveColumnNames();
		}

		return columnNames;
	}

	@Override
	public List<String> getClassNames() {
		if (classNames == null) {
			classNames = source.retrieveClassNames();
		}

		return classNames;
	}

	// should deliver a copy of the data
	@Override
	public List<List<Object>> getRows() {
		Logging.info(this, " -- getRows()");

		if (rowsCopy == null) {
			resetRows();
		}

		return rowsCopy;
	}

	// should set back the copy of the data to the original values
	protected void resetRows() {
		Logging.info(this, " -- resetRows()");
		if (rowsCopy != null) {
			rowsCopy.clear();
		} else {
			rowsCopy = new ArrayList<>();
		}

		if (rows == null) {
			rows = source.retrieveRows();
		}

		Logging.info(this, "resetRows(), rows.size() " + rows.size());

		if (rows == null) {
			Logging.info(" no data rows retrieved ");
			return;
		}

		for (int i = 0; i < rows.size(); i++) {
			List<Object> row = new ArrayList<>(rows.get(i));
			rowsCopy.add(row);
		}

	}

	// should set the working copy as new original values
	@Override
	public void setWorkingCopyAsNewOriginalRows() {
		if (rows == null) {
			// TODO: request reload?
			// in the following reset, we request a reload
		} else {
			if (rowsCopy != null) {
				rows.clear();

				for (int i = 0; i < rowsCopy.size(); i++) {
					rows.add(rowsCopy.get(i));
				}
			}
		}

		resetRows();
	}

	// should initiate returning to the original data
	@Override
	public void requestReturnToOriginal() {
		rowsCopy = null;
	}

	// should initiate reloading the original data
	@Override
	public void requestReloadRows() {
		rows = null;
		rowsCopy = null;
		source.requestReload();
	}

	// should initiate reloading the metadata
	@Override
	public void structureChanged() {
		classNames = null;
		columnNames = null;
	}

	// yields a column as ordered List
	@Override
	public List<String> getOrderedColumn(int col, boolean emptyAllowed) {
		TreeSet<String> set = new TreeSet<>();
		for (int row = 0; row < rowsCopy.size(); row++) {
			String val = (String) rowsCopy.get(row).get(col);

			if (emptyAllowed || val != null && !val.isEmpty()) {

				set.add((String) rowsCopy.get(row).get(col));
			}
		}

		return new ArrayList<>(set);
	}

}
