/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class GenericTableUpdateItemFactory implements TableUpdateItemInterface {
	private List<String> columnNames;
	private int keyCol;
	private Object source;

	public GenericTableUpdateItemFactory(int keyCol) {
		this.keyCol = keyCol;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public TableEditItem produceUpdateItem(List<Object> oldValues, List<Object> rowV) {
		return new MapBasedTableEditItem(source, keyCol, columnNames, rowV);
	}

	@Override
	public TableEditItem produceInsertItem(List<Object> rowV) {
		return new MapBasedTableEditItem(source, keyCol, columnNames, rowV);
	}

	@Override
	public TableEditItem produceDeleteItem(List<Object> rowV) {
		return new MapBasedTableEditItem(source, keyCol, columnNames, rowV);
	}
}
