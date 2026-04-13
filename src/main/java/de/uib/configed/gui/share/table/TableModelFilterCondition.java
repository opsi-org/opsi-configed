/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table;

import java.util.List;
import java.util.Set;

public interface TableModelFilterCondition {
	void setFilter(Set<Object> filter);

	boolean test(List<Object> row);
}
