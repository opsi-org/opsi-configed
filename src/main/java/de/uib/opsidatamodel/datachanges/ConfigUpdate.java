/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class ConfigUpdate implements UpdateCommand {
	private Map newdata;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	// TODO check generics here
	public ConfigUpdate(Map newdata) {
		this.newdata = newdata;
	}

	@Override
	public void doCall() {
		Logging.info(this, "doCall, setting class " + newdata.getClass() + ", the new data is " + newdata);

		persistenceController.setConfig(newdata);
	}
}
