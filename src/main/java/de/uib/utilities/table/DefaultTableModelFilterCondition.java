package de.uib.utilities.table;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public class DefaultTableModelFilterCondition implements TableModelFilterCondition {

	protected NavigableSet<? extends Object> filterSet;
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
	public boolean test(List<Object> row) {

		if (filterSet == null)
			return true;

		if (keyCol == -1)
			return true;

		boolean result = filterSet.contains(row.get(keyCol));

		return result;
	}

	public void setFilter(int[] rowNoFilter, final List<List<Object>> row) {
	}

	@Override
	public String toString() {
		return filterLabel;
	}

}
