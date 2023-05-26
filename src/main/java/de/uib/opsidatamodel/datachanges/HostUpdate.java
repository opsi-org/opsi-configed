/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class HostUpdate implements UpdateCommand {

	private Map<String, Object> newdata;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public HostUpdate(Map<String, Object> newdata) {
		super();
		this.newdata = newdata;
	}

	@Override
	public void doCall() {
		Logging.debug(this, "doCall, newdata " + newdata);
		persistenceController.setHostValues(newdata);
	}
}
