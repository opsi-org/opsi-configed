/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class HostUpdate implements UpdateCommand {
	private Map<String, Object> newdata;
	private String depot;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public HostUpdate(Map<String, Object> newdata, String depot) {
		super();
		this.newdata = newdata;
		this.depot = depot;
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

		persistenceController.getHostDataService().setHostValues(newdata);
	}
}
