package de.uib.utilities.table;

import java.util.TreeSet;
import java.util.Vector;

public interface TableModelFilterCondition {
	public void setFilter(TreeSet<Object> filter);

	public boolean test(Vector<Object> row);
	
}
