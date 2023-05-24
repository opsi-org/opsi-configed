/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/* 
 *
 * 	uib, www.uib.de, 2009-2012
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.List;

public interface TableSource {

	List<String> retrieveColumnNames();

	List<String> retrieveClassNames();

	// we get a new version
	List<List<Object>> retrieveRows();

	void setRowCounting(boolean b);

	void requestReload();
}
