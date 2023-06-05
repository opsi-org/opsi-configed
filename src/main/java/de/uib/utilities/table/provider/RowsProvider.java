/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.provider;

import java.util.List;

/**
 * delivers rows which are externally stored
 */
public interface RowsProvider {
	void requestReload();

	List<List<Object>> getRows();
}
