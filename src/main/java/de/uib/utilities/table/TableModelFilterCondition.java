package de.uib.utilities.table;

import java.util.List;
import java.util.TreeSet;

public interface TableModelFilterCondition {
	public void setFilter(TreeSet<Object> filter);

	public boolean test(List<Object> row);

}
