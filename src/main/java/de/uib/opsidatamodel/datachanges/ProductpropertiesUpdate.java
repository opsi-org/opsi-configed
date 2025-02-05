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

public class ProductpropertiesUpdate implements UpdateCommand {
	private String pcname;
	private String productname;
	private Map<String, Object> newdata;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ProductpropertiesUpdate(String pcname, String productname, Map<String, Object> newdata) {
		this.pcname = pcname;
		this.productname = productname;
		this.newdata = newdata;
	}

	@Override
	public void doCall() {
		persistenceController.getProductDataService().setProductProperties(pcname, productname,
				new ConfigName2ConfigValue(newdata));
	}

	public void revert() {
		if (newdata instanceof ConfigName2ConfigValue configNameData) {
			configNameData.rebuild();
		}
	}
}
