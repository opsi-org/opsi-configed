/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.List;

public interface TableUpdateItemInterface {
	MapBasedTableEditItem produceUpdateItem(List<Object> oldValues, List<Object> rowV);

	// TODO remove, every implementation is same as produceUpdateItem
	MapBasedTableEditItem produceInsertItem(List<Object> rowV);

	MapBasedTableEditItem produceDeleteItem(List<Object> rowV);
}
