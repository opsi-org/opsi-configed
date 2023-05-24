/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;

public class ProductpropertiesUpdate implements UpdateCommand {
	private String pcname;
	private String productname;
	private Map<?, ?> newdata;

	private OpsiserviceNOMPersistenceController persis;

	public ProductpropertiesUpdate(OpsiserviceNOMPersistenceController persis, String pcname, String productname,
			Map<?, ?> newdata) {
		this.pcname = pcname;
		this.productname = productname;
		this.newdata = newdata;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (OpsiserviceNOMPersistenceController) obj;
	}

	@Override
	public Object getController() {
		return persis;
	}

	@Override
	public void doCall() {

		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue) {

			persis.setProductProperties(pcname, productname, newdata);
		}
	}

	public void revert() {
		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue) {

			((de.uib.configed.type.ConfigName2ConfigValue) newdata).rebuild();

		}
	}

}
