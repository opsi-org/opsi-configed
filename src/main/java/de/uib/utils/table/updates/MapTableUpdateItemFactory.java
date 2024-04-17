/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.updates;

import java.util.List;

public class MapTableUpdateItemFactory {
	private List<String> columnNames;
	private Object source;

	public MapTableUpdateItemFactory(Object source, List<String> columnNames) {
		this.columnNames = columnNames;
		this.source = source;
	}

	public MapTableUpdateItemFactory(List<String> columnNames) {
		this(null, columnNames);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public MapBasedTableEditItem produceUpdateItem(List<Object> rowV) {
		return new MapBasedTableEditItem(source, columnNames, rowV, true);
	}

	public MapBasedTableEditItem produceDeleteItem(List<Object> rowV) {
		return new MapBasedTableEditItem(source, columnNames, rowV, false);
	}
}
