/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.datachanges;

import java.util.Map;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.type.ConfigName2ConfigValue;

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
		persistenceController.getDataServices().config.setConfigStates(objectId, new ConfigName2ConfigValue(newdata));
	}
}
