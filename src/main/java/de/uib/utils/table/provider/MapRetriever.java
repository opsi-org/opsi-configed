/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.provider;

import java.util.Map;

public interface MapRetriever {
	void reloadMap();

	Map<String, Map<String, Object>> retrieveMap();
}
