/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.datachanges;

import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ConfigedMain.EditingTarget;
import de.uib.configed.share.logging.Logging;

/**
*/
public class ConfigUpdateCollection extends DefaultUpdateCollection {
	private List<String> objectIds;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ConfigUpdateCollection(List<String> objectIds) {
		super();
		this.objectIds = objectIds;
	}

	@Override
	public boolean addMap(Map<String, Object> map) {
		boolean result = true;

		for (String objectId : objectIds) {
			if (ConfigedMain.getEditingTarget() == EditingTarget.SERVER) {
				Logging.debug(this, "adding ConfigUpdateCommand");
				result = add(new ConfigUpdateCommand(POJOReMapper.remap(map)));
			} else {
				Logging.debug(this, "adding ConfigStateUpdateCommand");
				result = add(new ConfigStateUpdateCommand(objectId, map));
			}
		}

		return result;
	}

	@Override
	public void clearElements() {
		Logging.debug(this, "clearElements()");
		clear();
	}

	@Override
	public void doCall() {
		super.doCall();
		Logging.debug(this, "doCall, after recursion, element count: ", size());
		persistenceController.getConfigDataService().updateConfigs();
		persistenceController.getConfigDataService().updateConfigStates();

		clear();
	}
}
