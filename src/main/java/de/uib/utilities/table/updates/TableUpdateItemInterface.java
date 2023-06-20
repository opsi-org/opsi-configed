/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

import java.util.List;

public interface TableUpdateItemInterface {
	TableEditItem produceUpdateItem(List<Object> oldValues, List<Object> rowV);

	TableEditItem produceInsertItem(List<Object> rowV);

	TableEditItem produceDeleteItem(List<Object> rowV);
}
