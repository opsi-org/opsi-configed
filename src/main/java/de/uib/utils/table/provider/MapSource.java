/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uib.utils.logging.Logging;

/**
 * based on a regular map (rows indexed by a String key) of maps (representing
 * the rows as pairs columnname - value) columns may represent null values)
 * column 0 of the table is the key of the outer map (therefore the first
 * classname has to be String)
 */
public class MapSource implements TableSource {
	private static final String ROW_COUNTER_NAME = "rowcounter";

	private boolean rowCounting;

	private List<String> columnNames;

	protected Map<String, Map<String, Object>> table;

	protected List<List<Object>> rows;

	protected boolean reloadRequested;

	public MapSource(List<String> columnNames, Map<String, Map<String, Object>> table, boolean rowCounting) {
		Logging.info(this.getClass(), "constructed with cols ", columnNames);
		this.columnNames = columnNames;
		this.table = table;
		this.rowCounting = rowCounting;

		init();
	}

	private void init() {
		setRowCounting(rowCounting);
		if (rowCounting) {
			Logging.info(this, "completed to cols ", columnNames);
		}
		rows = new ArrayList<>();
	}

	protected void fetchData() {
		rows.clear();

		int rowCount = 0;

		for (Entry<String, Map<String, Object>> tableEntry : table.entrySet()) {
			List<Object> vRow = new ArrayList<>();

			Map<String, Object> mRow = tableEntry.getValue();

			// previously we assumed that column 0 hold the key

			for (int i = 0; i < columnNames.size(); i++) {
				Object obj = mRow.get(columnNames.get(i));

				if (tableEntry.getKey().startsWith("A")) {
					Logging.debug(this, "fetchData for A-key " + tableEntry.getKey() + " col  " + columnNames.get(i)
							+ " index " + i + " val " + obj);
				}

				if (obj != null) {
					vRow.add(obj);
				} else if (mRow.containsKey(columnNames.get(i))) {
					Logging.debug(this, "fetchData row " + mRow + " no value in column  " + columnNames.get(i)
							+ " supplement by null");

					// we complete the row by null
					vRow.add(obj);
				} else if (columnNames.get(i).equals(ROW_COUNTER_NAME)) {
					vRow.add("" + rowCount);
				} else {
					vRow.add("");
					Logging.info(this, "fetchData row ", mRow,
							" ob == null, possibly the column name is not correct, column ", i, ", ",
							columnNames.get(i));
				}
			}

			if (tableEntry.getKey().startsWith("A")) {
				Logging.debug(this, "fetchData for A-key " + tableEntry.getKey() + " produced row " + vRow);
			}

			rows.add(vRow);

			rowCount++;
		}
	}

	@Override
	public List<String> retrieveColumnNames() {
		return columnNames;
	}

	@Override
	public List<List<Object>> retrieveRows() {
		Logging.info(this, " -- retrieveRows");
		fetchData();
		Logging.info(this, " -- retrieveRows rows.size() ", rows.size());
		return rows;
	}

	@Override
	public void requestReload() {
		reloadRequested = true;
	}

	@Override
	public void cancelRequestReload() {
		reloadRequested = false;
	}

	@Override
	public void setRowCounting(boolean b) {
		if (!rowCounting && b) {
			rowCounting = true;

			// has the effect that IntComparatorForStrings is applied
			columnNames.add(ROW_COUNTER_NAME);
		}
	}
}
