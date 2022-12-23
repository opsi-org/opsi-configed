package de.uib.utilities.table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JTable;

import de.uib.utilities.logging.logging;

public class RowNoTableModelFilterCondition implements TableModelFilterCondition {
	// protected Map<String, Boolean> selectionInfo;
	protected Map<Object, Boolean> selectionInfo;

	protected JTable table;

	public RowNoTableModelFilterCondition(JTable table) {
		this.table = table;
	}

	@Override
	public void setFilter(TreeSet<Object> filter) {
	}

	public void setFilter(int[] modelRowNoFilter, final Vector<Vector<Object>> rows) {
		logging.info(this, "setFilter int[]  " + modelRowNoFilter);
		if (modelRowNoFilter != null)
			logging.info(this, "setFilter as string " + Arrays.toString(modelRowNoFilter));

		if (rows == null || modelRowNoFilter == null || modelRowNoFilter.length == 0) {
			selectionInfo = null;
			return;
		}

		
		selectionInfo = new HashMap<>();

		for (int i : modelRowNoFilter) {
			if (i >= rows.size())
				logging.warning(this, "setFilter: impossible selection index " + i);
			else
				
				selectionInfo.put(rows.get(i), true);
		}

		
	}

	@Override
	public boolean test(Vector<Object> row) {
		

		if (selectionInfo == null)
			return true;

		// Boolean found = selectionInfo.get( Globals.pseudokey(row) )
		// ;
		Boolean found = selectionInfo.get(row);
		
		if (found == null)
			return false;

		return found;
	}

	@Override
	public String toString() {
		return getClass().getName() + " ( selectionInfo == null? ) " + (selectionInfo == null);
	}

}
