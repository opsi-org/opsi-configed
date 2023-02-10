/* 
 *
 * 	uib, www.uib.de, 2009-2015, 2017
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uib.utilities.logging.Logging;

public class MapSource implements TableSource
// based on a regular map (rows indexed by a String key)
// of maps (representing the rows as pairs columnname - value)

// columns may represent null values)
// column 0 of the table is the key of the outer map (therefore the first
// classname
// has to be String)
{
	protected static final String ROW_COUNTER_NAME = "rowcounter";

	protected boolean rowCounting = false;

	protected List<String> columnNames;

	protected List<String> classNames;

	protected Map<String, Map> table;

	protected List<List<Object>> rows;

	protected boolean reloadRequested = true;

	static final Map<String, Object> class2defaultValue;
	static {
		class2defaultValue = new HashMap<>();
		class2defaultValue.put("java.lang.Boolean", false);
		class2defaultValue.put("java.lang.String", "");
	}

	public MapSource(List<String> columnNames, List<String> classNames, Map<String, Map> table, boolean rowCounting) {
		Logging.info(this, "constructed with cols " + columnNames);
		Logging.info(this, "constructed with classes " + classNames);
		this.columnNames = columnNames;
		this.classNames = classNames;
		setRowCounting(rowCounting);
		if (rowCounting) {
			Logging.info(this, "completed to cols " + columnNames);
			Logging.info(this, "completed to classes " + classNames);
		}
		this.table = table;
		rows = new ArrayList<>();

	}

	public MapSource(List<String> columnNames, List<String> classNames, Map<String, Map> table) {
		this(columnNames, classNames, table, false);
	}

	private static boolean dynInstanceOf(Object ob, Class<?> cl) {
		return cl.isAssignableFrom(ob.getClass());
	}

	protected void fetchData() {
		rows.clear();

		int rowCount = 0;

		for (Entry<String, Map> tableEntry : table.entrySet()) {
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

					try {
						Class<?> cl = Class.forName(classNames.get(i));
						if (!dynInstanceOf(obj, cl)) {

							Logging.warning(this, "MapSource fetchData(): data type does not fit");
							Logging.info(this, " ob " + obj + " class " + obj.getClass().getName());
							Logging.info(this, "class should be " + cl);
						}
					} catch (ClassNotFoundException e) {
						Logging.error(this, "could not find class " + classNames.get(i));
					}

				} else {
					if (mRow.containsKey(columnNames.get(i))) {
						Logging.debug(this, "fetchData row " + mRow + " no value in column  " + columnNames.get(i)
								+ " supplement by null");

						// we complete the row by null
						vRow.add(obj);
					} else {
						String className = classNames.get(i);

						if (columnNames.get(i).equals(ROW_COUNTER_NAME)) {
							vRow.add("" + rowCount);

						} else {
							if (class2defaultValue.get(className) != null) {
								vRow.add(class2defaultValue.get(className));

							}

							else {
								Logging.warning(this,
										"fetchData row " + mRow
												+ " ob == null, possibly the column name is not correct, column " + i
												+ ", " + columnNames.get(i));
							}
						}
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
	public String getRowCounterName() {
		return ROW_COUNTER_NAME;
	}

	@Override
	public boolean isRowCounting() {
		return rowCounting;
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
