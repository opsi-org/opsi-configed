/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;

import de.uib.configed.share.logging.Logging;

public final class DefaultTableProvider {
	private TableSource source;
	private List<String> columnNames;
	private List<List<Object>> rows;
	private List<List<Object>> rowsCopy;

	private DefaultTableProvider(TableSource source) {
		this.source = source;
	}

	public static DefaultTableProvider createWithMapSource(List<String> columnNames,
			Map<String, ? extends Map<String, ? extends Object>> table) {
		return new DefaultTableProvider(new MapSource(columnNames, table));
	}

	public static DefaultTableProvider createWithExternalSource(List<String> columnNames, List<String> depotIds) {
		return new DefaultTableProvider(new ExternalSource(columnNames, depotIds));
	}

	public static DefaultTableProvider createWithRetrieverMapSource(List<String> columnNames, Object reloadEvent,
			Supplier<Map<String, ? extends Map<String, ? extends Object>>> mapSupplier) {
		return new DefaultTableProvider(new RetrieverMapSource(columnNames, reloadEvent, mapSupplier));
	}

	public List<String> getColumnNames() {
		if (columnNames == null) {
			columnNames = source.retrieveColumnNames();
		}

		return columnNames;
	}

	// should deliver a copy of the data
	public List<List<Object>> getRows() {
		Logging.info(this, " -- getRows()");

		if (rowsCopy == null) {
			resetRows();
		}

		return rowsCopy;
	}

	public void refreshRows() {
		rows = null;
		resetRows();
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

		Logging.info(this, "resetRows(), rows.size() ", rows.size());

		if (rows == null) {
			Logging.info(" no data rows retrieved ");
			return;
		}

		for (List<Object> row : rows) {
			rowsCopy.add(new ArrayList<>(row));
		}
	}

	// should set the working copy as new original values
	public void setWorkingCopyAsNewOriginalRows() {
		if (rows != null && rowsCopy != null) {
			rows.clear();

			rows.addAll(rowsCopy);
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

	public void cancelRequestReload() {
		source.cancelRequestReload();
	}

	// should initiate reloading the metadata
	public void structureChanged() {
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
