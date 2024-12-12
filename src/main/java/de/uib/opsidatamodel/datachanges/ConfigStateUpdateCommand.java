/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class ConfigStateUpdateCommand implements UpdateCommand {
	private String objectId;
	private Map<String, Object> newdata;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ConfigStateUpdateCommand(String objectId, Map<String, Object> newdata) {
		this.objectId = objectId;
		this.newdata = newdata;
	}

	@Override
	public void doCall() {
		persistenceController.getConfigDataService().setConfigStates(objectId, new ConfigName2ConfigValue(newdata));
	}
}
