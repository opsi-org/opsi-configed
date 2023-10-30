/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uib.utilities.logging.Logging;

/**
 * based on a regular map (rows indexed by a String key) of maps (representing
 * the rows as pairs columnname - value) columns may represent null values)
 * column 0 of the table is the key of the outer map (therefore the first
 * classname has to be String)
 */
public class MapSource implements TableSource {
	private static final String ROW_COUNTER_NAME = "rowcounter";

	private static final Map<String, Object> class2defaultValue;
	static {
		class2defaultValue = new HashMap<>();
		class2defaultValue.put("java.lang.Boolean", false);
		class2defaultValue.put("java.lang.String", "");
	}

	private boolean rowCounting;

	private List<String> columnNames;

	private List<String> classNames;

	protected Map<String, Map<String, Object>> table;

	protected List<List<Object>> rows;

	private boolean reloadRequested = true;

	public MapSource(List<String> columnNames, List<String> classNames, Map<String, Map<String, Object>> table,
			boolean rowCounting) {
		Logging.info(this.getClass(), "constructed with cols " + columnNames);
		Logging.info(this.getClass(), "constructed with classes " + classNames);
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.table = table;
		this.rowCounting = rowCounting;

		init();
	}

	private void init() {
		setRowCounting(rowCounting);
		if (rowCounting) {
			Logging.info(this, "completed to cols " + columnNames);
			Logging.info(this, "completed to classes " + classNames);
		}
		rows = new ArrayList<>();
	}

	private static boolean dynInstanceOf(Object ob, Class<?> cl) {
		return cl.isAssignableFrom(ob.getClass());
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
					warnIfDataWrongClass(obj, classNames.get(i));
				} else if (mRow.containsKey(columnNames.get(i))) {
					Logging.debug(this, "fetchData row " + mRow + " no value in column  " + columnNames.get(i)
							+ " supplement by null");

					// we complete the row by null
					vRow.add(obj);
				} else {
					String className = classNames.get(i);

					if (columnNames.get(i).equals(ROW_COUNTER_NAME)) {
						vRow.add("" + rowCount);
					} else if (class2defaultValue.get(className) != null) {
						vRow.add(class2defaultValue.get(className));
					} else {
						Logging.warning(this,
								"fetchData row " + mRow
										+ " ob == null, possibly the column name is not correct, column " + i + ", "
										+ columnNames.get(i));
					}
				}
			}

			if (tableEntry.getKey().startsWith("A")) {
				Logging.debug(this, "fetchData for A-key " + tableEntry.getKey() + " produced row " + vRow);
			}

			rows.add(vRow);

			rowCount++;
		}
	}

	private void warnIfDataWrongClass(Object obj, String className) {
		try {
			Class<?> cl = Class.forName(className);
			if (!dynInstanceOf(obj, cl)) {
				Logging.warning(this, "MapSource fetchData(): data type does not fit");
				Logging.info(this, " ob " + obj + " class " + obj.getClass().getName());
				Logging.info(this, "class should be " + cl);
			}
		} catch (ClassNotFoundException e) {
			Logging.error(this, "could not find class " + className, e);
		}
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
		Logging.info(this, " -- retrieveRows");
		if (reloadRequested) {
			fetchData();
			reloadRequested = false;
		}
		Logging.info(this, " -- retrieveRows rows.size() " + rows.size());
		return rows;
	}

	@Override
	public void requestReload() {
		reloadRequested = true;
	}

	@Override
	public void setRowCounting(boolean b) {
		if (!rowCounting && b) {
			rowCounting = true;

			classNames.add("java.lang.Integer");
			// has the effect that IntComparatorForStrings is applied
			columnNames.add(ROW_COUNTER_NAME);
		}
	}
}
