/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.datachanges;

import java.util.List;
import java.util.Map;

import de.uib.configed.share.logging.Logging;

/**
*/
public class HostUpdateCollection extends DefaultUpdateCollection {
	private String depot;
	private String type;

	public HostUpdateCollection(String depot, Map<String, Object> depotInfo) {
		this.depot = depot;
		if (depotInfo.get("type") instanceof List) {
			type = (String) ((List<?>) depotInfo.get("type")).get(0);
		} else {
			type = (String) depotInfo.get("type");
		}
	}

	@Override
	public boolean addMap(Map<String, Object> map) {
		return add(new HostUpdate(map, depot, type));
	}

	@Override
	public void clearElements() {
		Logging.debug(this, "clearElements()");
		clear();
	}
}
