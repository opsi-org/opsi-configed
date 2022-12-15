/* 
 *
 * 	uib, www.uib.de, 2009-2015, 2017
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import de.uib.utilities.logging.logging;

public class MapSource implements TableSource
// based on a regular map (rows indexed by a String key)
// of maps (representing the rows as pairs columnname - value)
// "regular" means that all rows have identical structure (missing
// columns may represent null values)
// column 0 of the table is the key of the outer map (therefore the first
// classname
// has to be String)
{
	protected final String rowCounterName = "rowcounter";

	protected boolean rowCounting = false;

	protected Vector<String> columnNames;

	protected Vector<String> classNames;

	protected Map<String, Map> table;

	protected Vector<Vector<Object>> rows;

	protected boolean reloadRequested = true;

	static final Map<String, Object> class2defaultValue;
	static {
		class2defaultValue = new HashMap<String, Object>();
		class2defaultValue.put("java.lang.Boolean", false);
		class2defaultValue.put("java.lang.String", "");
	}

	public MapSource(Vector<String> columnNames, Vector<String> classNames, Map<String, Map> table,
			boolean rowCounting) {
		logging.info(this, "constructed with cols " + columnNames);
		logging.info(this, "constructed with classes " + classNames);
		this.columnNames = columnNames;
		this.classNames = classNames;
		setRowCounting(rowCounting);
		if (rowCounting) {
			logging.info(this, "completed to cols " + columnNames);
			logging.info(this, "completed to classes " + classNames);
		}
		this.table = table;
		rows = new Vector();

	}

	public MapSource(Vector<String> columnNames, Vector<String> classNames, Map<String, Map> table) {
		this(columnNames, classNames, table, false);
	}

	private static boolean dynInstanceOf(Object ob, Class cl) {
		return cl.isAssignableFrom(ob.getClass());
	}

	protected void fetchData() {
		rows.clear();
		// logging.debug(this, "MapSource fetchData() : " + table);

		// logging.info(this, "fetchData , columns " + columnNames);
		int rowCount = 0;

		for (String key : table.keySet()) {
			Vector vRow = new Vector();

			Map mRow = table.get(key);

			// logging.debug ( " -------- key '" + key + "', mRow = " + mRow );

			// vRow.add(key);
			// previously we assumed that column 0 hold the key

			for (int i = 0; i < columnNames.size(); i++) {
				Object ob = mRow.get(columnNames.get(i));

				if (key.startsWith("A"))
					logging.debug(this, "fetchData for A-key " + key + " col  " + columnNames.get(i) + " index " + i
							+ " val " + ob);

				// logging.debug(this, " getting ob to column " + i + ", " + columnNames.get(i)
				// + " ob:" + ob);

				if (ob != null) {
					vRow.add(ob);

					try {
						// logging.debug( "??? is " + ob + " dyninstance class of " +
						// classNames.get(i));
						Class cl = Class.forName(classNames.get(i));
						if (!dynInstanceOf(ob, cl)) {
							// Class.forName( classNames.get(i) ) ).isAssignableFrom ( ob.getClass() ) )
							// e.g. java.lang.String valueInColumnI = ob; works!
							logging.warning(this, "MapSource fetchData(): data type does not fit");
							logging.info(this, " ob " + ob + " class " + ob.getClass().getName());
							logging.info(this, "class should be " + cl);
						}
					} catch (java.lang.NullPointerException ex) {
						logging.warning(this,
								" " + ex + ", could not get dyninstance " + i + ", " + columnNames.get(i));
					} catch (Exception ex) {
						logging.error("MapSource fetchData(): class " + classNames.get(i) + " not found, " + ex);
					}

				} else {
					if (mRow.containsKey(columnNames.get(i))) {
						logging.debug(this, "fetchData row " + mRow + " no value in column  " + columnNames.get(i)
								+ " supplement by null");
						vRow.add(ob); // we complete the row by null
					} else {
						String className = classNames.get(i);

						// if (className.equals("java.lang.Integer"))
						if (columnNames.get(i).equals(rowCounterName)) {
							vRow.add("" + rowCount);
							// vRow.add( rowCount );
						} else {
							if (class2defaultValue.get(className) != null) {
								vRow.add(class2defaultValue.get(className));
								// logging.info(this, "fetchData row " + mRow + " ob == null, setting default
								// for " + className );
							}

							else {
								logging.warning(this,
										"fetchData row " + mRow
												+ " ob == null, possibly the column name is not correct, column " + i
												+ ", " + columnNames.get(i));
							}
						}
					}
				}

			}

			if (key.startsWith("A"))
				logging.debug(this, "fetchData for A-key " + key + " produced row " + vRow);

			rows.add(vRow);

			rowCount++;

		}
	}

	public Vector<String> retrieveColumnNames() {
		return columnNames;
	}

	public Vector<String> retrieveClassNames() {
		return classNames;
	}

	public Vector<Vector<Object>> retrieveRows() {
		logging.info(this, " -- retrieveRows");
		if (reloadRequested) {
			fetchData();
			reloadRequested = false;
		}
		logging.info(this, " -- retrieveRows rows.size() " + rows.size());
		return rows;
	}

	public void requestReload() {
		reloadRequested = true;
	}

	@Override
	public String getRowCounterName() {
		return rowCounterName;
	}

	@Override
	public boolean isRowCounting() {
		return rowCounting;
	}

	@Override
	public void setRowCounting(boolean b) {
		if (!rowCounting && b) {
			rowCounting = true;
			// classNames.add( "java.lang.String" );
			classNames.add("java.lang.Integer");
			// has the effect that IntComparatorForStrings is applied
			columnNames.add(rowCounterName);
			structureChanged();
		}
	}

	@Override
	public void structureChanged() {
		// TODO Auto-generated method stub

	}

}
