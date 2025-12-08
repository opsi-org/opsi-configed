/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.datachanges;

import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.share.logging.Logging;

public class ConfigUpdateCommand implements UpdateCommand {
	private Map<String, List<Object>> newdata;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ConfigUpdateCommand(Map<String, List<Object>> newdata) {
		this.newdata = newdata;
	}

	@Override
	public void doCall() {
		Logging.info(this, "doCall, setting class ", newdata.getClass(), ", the new data is ", newdata);

		persistenceController.getConfigDataService().setConfig(newdata);
	}
}
