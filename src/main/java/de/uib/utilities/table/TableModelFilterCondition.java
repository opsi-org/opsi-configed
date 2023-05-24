/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.util.List;
import java.util.Set;

public interface TableModelFilterCondition {
	void setFilter(Set<Object> filter);

	boolean test(List<Object> row);
}
