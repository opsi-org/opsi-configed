/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import de.uib.utilities.logging.Logging;

public class DefaultTableProvider {
	private TableSource source;
	private List<String> columnNames;
	private List<String> classNames;
	private List<List<Object>> rows;
	private List<List<Object>> rowsCopy;

	public DefaultTableProvider(TableSource source) {
		this.source = source;
	}

	public List<String> getColumnNames() {
		if (columnNames == null) {
			columnNames = source.retrieveColumnNames();
		}

		return columnNames;
	}

	public List<String> getClassNames() {
		if (classNames == null) {
			classNames = source.retrieveClassNames();
		}

		return classNames;
	}

	// should deliver a copy of the data
	public List<List<Object>> getRows() {
		Logging.info(this, " -- getRows()");

		if (rowsCopy == null) {
			resetRows();
		}

		return rowsCopy;
	}

	// should set back the copy of the data to the original values
	private void resetRows() {
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
	public void requestReturnToOriginal() {
		rowsCopy = null;
	}

	// should initiate reloading the original data
	public void requestReloadRows() {
		rows = null;
		rowsCopy = null;
		source.requestReload();
	}

	// should initiate reloading the metadata
	public void structureChanged() {
		classNames = null;
		columnNames = null;
	}

	// yields a column as ordered List
	public List<String> getOrderedColumn(int col, boolean emptyAllowed) {
		TreeSet<String> set = new TreeSet<>();
		for (int row = 0; row < rowsCopy.size(); row++) {
			String val = (String) rowsCopy.get(row).get(col);

			if (emptyAllowed || (val != null && !val.isEmpty())) {

				set.add((String) rowsCopy.get(row).get(col));
			}
		}

		return new ArrayList<>(set);
	}
}
