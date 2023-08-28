/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class MapTableUpdateItemFactory implements TableUpdateItemInterface {
	private List<String> columnNames;
	private int keyCol;
	private Object source;

	public MapTableUpdateItemFactory(Object source, List<String> columnNames, int keyCol) {
		this.columnNames = columnNames;
		this.keyCol = keyCol;
		this.source = source;
	}

	public MapTableUpdateItemFactory(List<String> columnNames, int keyCol) {
		this(null, columnNames, keyCol);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public TableEditItem produceUpdateItem(List<Object> oldValues, List<Object> rowV) {
		return new MapDeliveryItem(source, keyCol, columnNames, rowV);
	}

	@Override
	public TableEditItem produceInsertItem(List<Object> rowV) {
		return new MapDeliveryItem(source, keyCol, columnNames, rowV);
	}

	@Override
	public TableEditItem produceDeleteItem(List<Object> rowV) {
		return new MapDeleteItem(source, keyCol, columnNames, rowV);
	}

}
