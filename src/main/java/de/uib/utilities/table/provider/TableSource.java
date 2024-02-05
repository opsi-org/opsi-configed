/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.provider;

import java.util.List;

public interface TableSource {
	List<String> retrieveColumnNames();

	// we get a new version
	List<List<Object>> retrieveRows();

	void setRowCounting(boolean b);

	void requestReload();

	void cancelRequestReload();
}
