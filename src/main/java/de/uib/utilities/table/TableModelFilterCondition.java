package de.uib.utilities.table;

import java.util.List;
import java.util.Set;

public interface TableModelFilterCondition {
	public void setFilter(Set<Object> filter);

	public boolean test(List<Object> row);
}
