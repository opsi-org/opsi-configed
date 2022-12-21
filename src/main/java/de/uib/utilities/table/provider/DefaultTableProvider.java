/* 
 *
 * 	uib, www.uib.de, 2009-2010
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.TreeSet;
import java.util.Vector;

import de.uib.utilities.logging.logging;

public class DefaultTableProvider implements TableProvider {
	protected TableSource source;
	protected Vector<String> columnNames;
	protected Vector<String> classNames;
	protected Vector<Vector<Object>> rows;
	protected Vector<Vector<Object>> rowsCopy;
	private boolean isDecorated = false;

	public DefaultTableProvider(TableSource source) {
		this.source = source;
	}

	@Override
	public void setTableSource(TableSource source) {
		this.source = source;
	}

	@Override
	public Vector<String> getColumnNames() {
		if (columnNames == null)
			columnNames = source.retrieveColumnNames();

		return columnNames;
	}

	@Override
	public Vector<String> getClassNames() {
		if (classNames == null)
			classNames = source.retrieveClassNames();

		return classNames;
	}

	// should deliver a copy of the data
	@Override
	public Vector<Vector<Object>> getRows() {
		logging.info(this, " -- getRows()");

		// logging.debug( " rowsCopy == null " + (rowsCopy == null) );
		if (rowsCopy == null)
			resetRows();

		return rowsCopy;
	}

	protected void decorateRow(Vector<Object> row) {
	}

	// should set back the copy of the data to the original values
	protected void resetRows() {
		logging.info(this, " -- resetRows()");
		if (rowsCopy != null)
			rowsCopy.clear();
		else
			rowsCopy = new Vector<Vector<Object>>();

		if (rows == null) {
			rows = source.retrieveRows();
		}

		logging.info(this, "resetRows(), rows.size() " + rows.size());

		if (!isDecorated) {
			logging.info(this, "resetRows decorating rows");
			if (rows != null) {
				for (int i = 0; i < rows.size(); i++) {
					decorateRow(rows.get(i));
				}
			}
			isDecorated = true;
		}

		// logging.debug (" rows.size() " + rows.size());

		if (rows == null) {
			logging.info(" no data rows retrieved ");
			return;
		}

		for (int i = 0; i < rows.size(); i++) {
			Vector<Object> row = (Vector<Object>) (rows.get(i).clone());
			rowsCopy.add(row);
		}

		// logging.debug (" rowsCopy.size() " + rowsCopy.size());
	}

	// should set the working copy as new original values
	@Override
	public void setWorkingCopyAsNewOriginalRows() {
		/// logging.debug(" setWorkingCopyAsNewOriginalRows() ");
		// "deep" rows = rowsCopy:

		if (rows == null) {
			// logging.debug(" --- original rows null ");
			// in the following reset, we request a reload
		}

		else {
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
		isDecorated = false;
	}

	// should initiate reloading the metadata
	@Override
	public void structureChanged() {
		source.structureChanged();
		classNames = null;
		columnNames = null;
	}

	// yields a column as ordered vector
	@Override
	public Vector<String> getOrderedColumn(int col, boolean empty_allowed) {
		// logging.debug(this, "getOrderedColumn " + col + ", empty_allowed " +
		// empty_allowed);

		TreeSet<String> set = new TreeSet<String>();
		for (int row = 0; row < rowsCopy.size(); row++) {
			String val = (String) rowsCopy.get(row).get(col);
			// logging.debug(this, "getOrderedColumn(" + col + ") row " + row + ": " +val );
			if (empty_allowed || val != null && !val.equals("")) {
				// logging.debug(this, "getOrderedColumn, added " +val );
				set.add((String) rowsCopy.get(row).get(col));
			}
		}

		Vector<String> result = new Vector<String>(set);

		// logging.debug(this, "getOrderedColumn, result " +result);

		return result;
	}

}
