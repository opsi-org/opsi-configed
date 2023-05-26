/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

/**
*/
public class AdditionalconfigurationUpdateCollection extends UpdateCollection {
	private String[] objectIds;
	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private boolean determineConfigOptions;
	private boolean masterConfig;

	public AdditionalconfigurationUpdateCollection(String[] objectIds) {
		super(new ArrayList<>(0));
		this.objectIds = objectIds;
	}

	

	@Override
	public boolean addAll(Collection<? extends UpdateCommand> c) {
		boolean result = true;

		if (c.size() != objectIds.length) {
			result = false;
			Logging.warning(this, "object ids (not fitting to edited item) " + Arrays.toString(objectIds));
			Logging.error("list of data has size " + c.size() + " differs from  length of objectIds list  "
					+ objectIds.length);
		}

		if (result) {
			Iterator<? extends UpdateCommand> it = c.iterator();
			int i = 0;
			while (it.hasNext()) {
				Map<?, ?> map = null;
				Object updateCommand = it.next();

				try {
					map = (Map<?, ?>) updateCommand;
				} catch (ClassCastException ccex) {
					Logging.error(
							"Wrong element type, found " + updateCommand.getClass().getName() + ", expected a Map",
							ccex);
				}

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
			persistenceController.setConfig();
		} else {
			persistenceController.setAdditionalConfiguration(determineConfigOptions);
		}
		clear();
	}

	public void setDetermineConfigOptions(boolean b) {
		determineConfigOptions = b;
	}

	public void setMasterConfig(boolean b) {
		masterConfig = b;
	}

	public boolean isMasterConfig() {
		return masterConfig;
	}

}
