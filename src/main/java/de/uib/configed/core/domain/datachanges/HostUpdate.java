/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.datachanges;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.share.logging.Logging;

public class HostUpdate implements UpdateCommand {
	private Map<String, Object> newdata;
	private String depot;
	private String type;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public HostUpdate(Map<String, Object> newdata, String depot, String type) {
		super();
		this.newdata = newdata;
		this.depot = depot;
		this.type = type;
	}

	@Override
	public void doCall() {
		Logging.debug(this, "doCall, newdata ", newdata);

		// All values are maps, so we need to get the (only) value of the map
		for (Entry<String, Object> entry : newdata.entrySet()) {
			newdata.put(entry.getKey(), ((List<?>) entry.getValue()).get(0));
		}

		// We need to add the depot as an id
		newdata.put("id", depot);

		// So the entry "maxBandwith" is actually an integer, but we can only edit as a string,
		// so we have to parse it here
		if (newdata.containsKey("maxBandwidth")) {
			newdata.put("maxBandwidth", Integer.parseInt(newdata.get("maxBandwidth").toString().strip()));
		}

		// Without the type, the method will not work on the server
		newdata.put("type", type);

		persistenceController.getDataServices().host.setHostValues(newdata);
	}
}
