package de.uib.utilities.table;
import java.util.*;

public interface TableModelFilterCondition
{
	public void setFilter( TreeSet<Object> filter);
	public boolean test(Vector<Object> row);
	//public void setFilter( int[] rowNoFilter, final  Vector<Vector<Object>> rows);
}
	


