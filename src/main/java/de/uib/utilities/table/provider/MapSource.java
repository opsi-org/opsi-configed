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

	protected Map<String, Map<String, Object>> table;

	protected List<List<Object>> rows;

	protected boolean reloadRequested = true;

	static final Map<String, Object> class2defaultValue;
	static {
		class2defaultValue = new HashMap<>();
		class2defaultValue.put("java.lang.Boolean", false);
		class2defaultValue.put("java.lang.String", "");
	}

	public MapSource(List<String> columnNames, List<String> classNames, Map<String, Map<String, Object>> table,
			boolean rowCounting) {
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

	public MapSource(List<String> columnNames, List<String> classNames, Map<String, Map<String, Object>> table) {
		this(columnNames, classNames, table, false);
	}

	private static boolean dynInstanceOf(Object ob, Class cl) {
		return cl.isAssignableFrom(ob.getClass());
	}

	protected void fetchData() {
		rows.clear();

		int rowCount = 0;

		for (String key : table.keySet()) {
			List<Object> vRow = new ArrayList<>();

			Map<String, Object> mRow = table.get(key);

			// previously we assumed that column 0 hold the key

			for (int i = 0; i < columnNames.size(); i++) {
				Object ob = mRow.get(columnNames.get(i));

				if (key.startsWith("A"))
					Logging.debug(this, "fetchData for A-key " + key + " col  " + columnNames.get(i) + " index " + i
							+ " val " + ob);

				if (ob != null) {
					vRow.add(ob);

					try {

						Class cl = Class.forName(classNames.get(i));
						if (!dynInstanceOf(ob, cl)) {
							// Class.forName( classNames.get(i) ) ).isAssignableFrom ( ob.getClass() ) )

							Logging.warning(this, "MapSource fetchData(): data type does not fit");
							Logging.info(this, " ob " + ob + " class " + ob.getClass().getName());
							Logging.info(this, "class should be " + cl);
						}
					} catch (java.lang.NullPointerException ex) {
						Logging.warning(this,
								" " + ex + ", could not get dyninstance " + i + ", " + columnNames.get(i));
					} catch (Exception ex) {
						Logging.error("MapSource fetchData(): class " + classNames.get(i) + " not found, " + ex);
					}

				} else {
					if (mRow.containsKey(columnNames.get(i))) {
						Logging.debug(this, "fetchData row " + mRow + " no value in column  " + columnNames.get(i)
								+ " supplement by null");
						vRow.add(ob); // we complete the row by null
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

			if (key.startsWith("A"))
				Logging.debug(this, "fetchData for A-key " + key + " produced row " + vRow);

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
			structureChanged();
		}
	}

	@Override
	public void structureChanged() {
		// TODO Auto-generated method stub

	}

}
