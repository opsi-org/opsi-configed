/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ChainedTableModelFilter extends TableModelFilter {
	private final Map<String, TableModelFilter> chain = new HashMap<>();

	public ChainedTableModelFilter set(String filterName, TableModelFilter filter) {
		chain.put(filterName, filter);
		return this;
	}

	public void clear() {
		chain.clear();
	}

	public TableModelFilter getElement(String name) {
		return chain.get(name);
	}

	@Override
	public boolean isInUse() {
		return chain.values().stream().anyMatch(TableModelFilter::isInUse);
	}

	@Override
	public boolean test(List<Object> row) {
		if (!inUse) {
			return true;
		}

		boolean testResult = chain.values().stream().filter(TableModelFilter::isInUse).allMatch(f -> f.test(row));

		return inverted ? !testResult : testResult;
	}

	public String getActiveFilters() {
		return chain.entrySet().stream().filter(e -> e.getValue().isInUse()).map(Entry::getKey)
				.collect(Collectors.joining(" - "));
	}

	@Override
	public String toString() {
		return getClass().getName() + ", chain is: " + chain;
	}
}
