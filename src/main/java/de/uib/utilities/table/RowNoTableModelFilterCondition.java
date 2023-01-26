package de.uib.utilities.table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;

import de.uib.utilities.logging.Logging;

public class RowNoTableModelFilterCondition implements TableModelFilterCondition {

	protected Map<Object, Boolean> selectionInfo;

	protected JTable table;

	public RowNoTableModelFilterCondition(JTable table) {
		this.table = table;
	}

	@Override
	public void setFilter(Set<Object> filter) {
		/* Not needed */}

	public void setFilter(int[] modelRowNoFilter, final List<List<Object>> rows) {
		Logging.info(this, "setFilter int[]  " + modelRowNoFilter);
		if (modelRowNoFilter != null)
			Logging.info(this, "setFilter as string " + Arrays.toString(modelRowNoFilter));

		if (rows == null || modelRowNoFilter == null || modelRowNoFilter.length == 0) {
			selectionInfo = null;
			return;
		}

		selectionInfo = new HashMap<>();

		for (int i : modelRowNoFilter) {
			if (i >= rows.size())
				Logging.warning(this, "setFilter: impossible selection index " + i);
			else

				selectionInfo.put(rows.get(i), true);
		}

	}

	@Override
	public boolean test(List<Object> row) {

		if (selectionInfo == null)
			return true;

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
