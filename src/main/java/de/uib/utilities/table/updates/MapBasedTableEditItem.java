/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapBasedTableEditItem {
	private List<String> columnNames;
	private List<Object> rowV;
	protected Object source;
	private boolean keyChanged;

	public MapBasedTableEditItem(Object source, List<String> columnNames, List<Object> rowV, boolean keyChanged) {
		this.columnNames = columnNames;
		this.rowV = rowV;
		this.source = source;
		this.keyChanged = keyChanged;
	}

	public Map<String, Object> getRowAsMap() {
		Map<String, Object> result = new HashMap<>();

		for (int i = 0; i < columnNames.size(); i++) {
			result.put(columnNames.get(i), rowV.get(i));
		}

		return result;
	}

	public Object getSource() {
		return source;
	}

	public boolean keyChanged() {
		return keyChanged;
	}

	@Override
	public String toString() {
		return getRowAsMap().toString() + " source " + source;
	}
}
