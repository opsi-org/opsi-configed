/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class MapDeleteItem extends MapBasedTableEditItem {
	public MapDeleteItem(Object source, int keyCol, List<String> columnNames, List<String> classNames,
			List<Object> rowV) {
		super(source, keyCol, columnNames, rowV);
	}

	@Override
	public boolean keyChanged() {
		return false;
	}
}
