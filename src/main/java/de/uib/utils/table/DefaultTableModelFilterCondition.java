/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table;

import java.util.List;
import java.util.Set;

public class DefaultTableModelFilterCondition implements TableModelFilterCondition {
	private Set<? extends Object> filterSet;
	private int keyCol = -1;
	private String filterLabel = "";

	public DefaultTableModelFilterCondition(int keyCol) {
		this.keyCol = keyCol;
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
