/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.List;
import java.util.Map;

import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

/**
*/
public class ConfigUpdateCollection extends UpdateCollection {
	private List<String> objectIds;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private boolean masterConfig;

	public ConfigUpdateCollection(List<String> objectIds) {
		super();
		this.objectIds = objectIds;
	}

	@Override
	public boolean addMap(Map<String, Object> map) {
		boolean result = true;

		for (String objectId : objectIds) {
			if (masterConfig) {
				Logging.debug(this, "adding ConfigUpdate");
				result = add(new ConfigUpdate(POJOReMapper.remap(map)));
			} else {
				Logging.debug(this, "adding AdditionalconfigurationUpdate");
				result = add(new ConfigUpdateCommand(objectId, map));
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
		if (masterConfig) {
			persistenceController.getConfigDataService().setConfig();
		} else {
			persistenceController.getConfigDataService().setConfg();
		}
		clear();
	}

	public void setMasterConfig(boolean b) {
		masterConfig = b;
	}
}
