/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/* 
 *
 * 	uib, www.uib.de, 2012
 * 
 *	author Rupert Röder
 *
 */

package de.uib.utilities.table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class ChainedTableModelFilter extends TableModelFilter {
	private LinkedHashMap<String, TableModelFilter> chain;

	public ChainedTableModelFilter() {
		chain = new LinkedHashMap<>();
	}

	public ChainedTableModelFilter set(String filterName, TableModelFilter filter) {
		chain.put(filterName, filter);
		return this;
	}

	public void clear() {
		chain.clear();
	}

	public boolean hasFilterName(String name) {
		return chain.containsKey(name);
	}

	public TableModelFilter getElement(String name) {
		return chain.get(name);
	}

	@Override
	public boolean isInUse() {
		boolean result = false;

		for (TableModelFilter filter : chain.values()) {
			if (filter.isInUse()) {
				result = true;
				break;
			}
		}

		return result;
	}

	@Override
	public boolean test(List<Object> row) {
		if (!inUse) {
			return true;
		}

		boolean testresult = true;

		for (TableModelFilter filter : chain.values()) {
			if (filter.isInUse()) {
				testresult = testresult && filter.test(row);
			}

		}

		if (inverted) {
			testresult = !testresult;
		}

		return testresult;
	}

	public String getActiveFilters() {
		StringBuilder result = new StringBuilder();

		for (Entry<String, TableModelFilter> filterEntry : chain.entrySet()) {
			if (filterEntry.getValue().isInUse()) {
				result.append(" - ");
				result.append(filterEntry.getKey());
			}
		}

		return result.toString();
	}

	@Override
	public String toString() {
		return getClass().getName() + ", chain is: " + chain;
	}

}
