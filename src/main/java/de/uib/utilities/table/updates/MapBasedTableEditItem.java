/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapBasedTableEditItem extends TableEditItem {
	private List<String> columnNames;
	private List<Object> rowV;

	public MapBasedTableEditItem(Object source, int keyCol, List<String> columnNames, List<Object> rowV) {
		this.columnNames = columnNames;
		this.rowV = rowV;
		this.source = source;
		this.keyCol = keyCol;
	}

	public Map<String, Object> getRowAsMap() {
		Map<String, Object> result = new HashMap<>();

		for (int i = 0; i < columnNames.size(); i++) {
			result.put(columnNames.get(i), rowV.get(i));

		}

		return result;
	}

	@Override
	public String toString() {
		return getRowAsMap().toString() + " keyCol " + keyCol + " source " + source;
	}
}
