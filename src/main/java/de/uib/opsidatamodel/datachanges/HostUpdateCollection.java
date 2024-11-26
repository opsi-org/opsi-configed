/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.utils.logging.Logging;

/**
*/
public class HostUpdateCollection extends UpdateCollection {
	public HostUpdateCollection() {
		super();
	}

	@Override
	public boolean addMap(Map<String, Object> map) {
		return add(new HostUpdate(map));
	}

	@Override
	public void clearElements() {
		Logging.debug(this, "clearElements()");
		clear();
	}
}
