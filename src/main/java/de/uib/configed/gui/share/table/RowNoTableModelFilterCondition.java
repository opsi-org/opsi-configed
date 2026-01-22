/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uib.configed.share.logging.Logging;

public class RowNoTableModelFilterCondition implements TableModelFilterCondition {
	private Set<Object> filter;

	@Override
	public void setFilter(Set<Object> filter) {
		/* Not needed */}

	public void setFilter(int[] modelRowNoFilter, final List<List<Object>> rows) {
		Logging.info(this, "setFilter int[]  ", modelRowNoFilter);
		if (modelRowNoFilter != null) {
			Logging.info(this, "setFilter as string ", Arrays.toString(modelRowNoFilter));
		}

		if (rows == null || modelRowNoFilter == null || modelRowNoFilter.length == 0) {
			filter = null;
			return;
		}

		filter = new HashSet<>();

		for (int i : modelRowNoFilter) {
			filter.add(rows.get(i));
		}
	}

	@Override
	public boolean test(List<Object> row) {
		if (filter == null) {
			return true;
		}

		return filter.contains(row);
	}

	@Override
	public String toString() {
		return getClass().getName() + " ( filter == null? ) " + (filter == null);
	}
}
