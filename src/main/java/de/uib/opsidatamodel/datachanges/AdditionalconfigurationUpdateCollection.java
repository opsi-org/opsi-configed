/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

/**
*/
public class AdditionalconfigurationUpdateCollection extends UpdateCollection {
	private String[] objectIds;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private boolean masterConfig;

	public AdditionalconfigurationUpdateCollection(String[] objectIds) {
		super(new ArrayList<>(0));
		this.objectIds = objectIds;
	}

	@Override
	public boolean addAll(Collection<? extends UpdateCommand> c) {

		if (c.size() != objectIds.length) {
			Logging.warning(this, "object ids (not fitting to edited item) " + Arrays.toString(objectIds));
			Logging.error("list of data has size " + c.size() + " differs from  length of objectIds list  "
					+ objectIds.length);

			return false;
		}

		boolean result = true;
		int i = 0;

		// TODO Sometimes these are not updatecommands?!?
		for (Object updateCommand : (Collection<?>) c) {
			if (updateCommand instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) updateCommand;

				Logging.debug(this, "addAll for one obj, map " + map);

				if (masterConfig) {
					Logging.debug(this, "adding ConfigUpdate");
					result = add(new ConfigUpdate(map));
				} else {
					Logging.debug(this, "adding AdditionalconfigurationUpdate");
					result = add(new AdditionalconfigurationUpdate(objectIds[i], map));
				}
				i++;
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
		Logging.debug(this, "doCall, after recursion, element count: " + size());
		if (masterConfig) {
			persistenceController.getConfigDataService().setConfig();
		} else {
			persistenceController.getConfigDataService().setAdditionalConfiguration();
		}
		clear();
	}

	public void setMasterConfig(boolean b) {
		masterConfig = b;
	}
}
