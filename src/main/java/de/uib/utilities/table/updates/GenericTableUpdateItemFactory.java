/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class GenericTableUpdateItemFactory implements TableUpdateItemInterface {
	private List<String> columnNames;
	private Object source;

	public void setSource(Object source) {
		this.source = source;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public MapBasedTableEditItem produceUpdateItem(List<Object> rowV) {
		return new MapBasedTableEditItem(source, columnNames, rowV);
	}

	@Override
	public MapBasedTableEditItem produceDeleteItem(List<Object> rowV) {
		return new MapBasedTableEditItem(source, columnNames, rowV);
	}
}
