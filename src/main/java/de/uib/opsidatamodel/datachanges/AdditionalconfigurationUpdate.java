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

public class AdditionalconfigurationUpdate implements UpdateCommand {
	private String objectId;
	private Map<?, ?> newdata;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public AdditionalconfigurationUpdate(String objectId, Map<?, ?> newdata) {
		this.objectId = objectId;
		this.newdata = newdata;
	}

	@Override
	public void doCall() {

		if (newdata instanceof ConfigName2ConfigValue) {
			ConfigName2ConfigValue configState = (ConfigName2ConfigValue) newdata;

			persistenceController.setAdditionalConfiguration(objectId, configState);
			// for opsi 4.0, this only collects the data
		}
	}
}
