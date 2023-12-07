/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class MapDeleteItem extends MapBasedTableEditItem {
	public MapDeleteItem(Object source, List<String> columnNames, List<Object> rowV) {
		super(source, columnNames, rowV);
	}

	@Override
	public boolean keyChanged() {
		return false;
	}
}
