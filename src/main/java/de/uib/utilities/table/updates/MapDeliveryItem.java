/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.List;

// TODO can we remove this, this implementation does nothing?
public class MapDeliveryItem extends MapBasedTableEditItem {
	private List<Object> oldValues;
	private List<Object> rowV;

	public MapDeliveryItem(Object source, List<String> columnNames, List<Object> rowV) {
		super(source, columnNames, rowV);
	}

	@Override
	public boolean keyChanged() {
		if (oldValues == null) {
			return true;
		}

		return oldValues.get(keyCol).toString().equals(rowV.get(keyCol).toString());
	}
}
