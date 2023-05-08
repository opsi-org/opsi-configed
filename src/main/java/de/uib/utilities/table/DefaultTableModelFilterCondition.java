package de.uib.utilities.table;

import java.util.List;
import java.util.Set;

public class DefaultTableModelFilterCondition implements TableModelFilterCondition {

	private Set<? extends Object> filterSet;
	private int keyCol = -1;
	private String filterLabel = "";

	public DefaultTableModelFilterCondition(int keyCol, String label) {
		this.keyCol = keyCol;
		if (label != null) {
			this.filterLabel = label;
		}
	}

	public DefaultTableModelFilterCondition() {
	}

	public DefaultTableModelFilterCondition(int keyCol) {
		this(keyCol, null);
	}

	public DefaultTableModelFilterCondition(String label) {
		if (label != null) {
			this.filterLabel = label;
		}
	}

	@Override
	public void setFilter(Set<Object> filterParam) {
		filterSet = filterParam;
	}

	@Override
	public boolean test(List<Object> row) {

		if (filterSet == null) {
			return true;
		}

		if (keyCol == -1) {
			return true;
		}

		return filterSet.contains(row.get(keyCol));
	}

	@Override
	public String toString() {
		return filterLabel;
	}
}
