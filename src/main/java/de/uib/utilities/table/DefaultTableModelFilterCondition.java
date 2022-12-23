package de.uib.utilities.table;

import java.util.TreeSet;
import java.util.Vector;

public class DefaultTableModelFilterCondition implements TableModelFilterCondition {

	protected TreeSet<? extends Object> filterSet;
	protected int keyCol = -1;
	protected String filterLabel = "";

	public DefaultTableModelFilterCondition(int keyCol, String label) {
		this.keyCol = keyCol;
		if (label != null)
			this.filterLabel = label;
	}

	public DefaultTableModelFilterCondition() {
	}

	public DefaultTableModelFilterCondition(int keyCol) {
		this(keyCol, null);
	}

	public DefaultTableModelFilterCondition(String label) {
		if (label != null)
			this.filterLabel = label;
	}

	@Override
	public void setFilter(TreeSet<Object> filterParam) {
		filterSet = filterParam;
	}

	@Override
	public boolean test(Vector<Object> row) {
		// logging.info(this, "test " + row);

		if (filterSet == null)
			return true;

		if (keyCol == -1)
			return true;

		boolean result = filterSet.contains(row.get(keyCol));
		// if (result) logging.info(this, "test: " + row.get(keyCol) + " " + result);

		return result;
	}

	public void setFilter(int[] rowNoFilter, final Vector<Vector<Object>> row) {
	}

	@Override
	public String toString() {
		return filterLabel;
	}

}
