package de.uib.utilities.table;

import java.util.TreeSet;
import java.util.ArrayList;

public interface TableModelFilterCondition {
	public void setFilter(TreeSet<Object> filter);

	public boolean test(ArrayList<Object> row);
	// public void setFilter( int[] rowNoFilter, final ArrayList<ArrayList<Object>> rows);
}
