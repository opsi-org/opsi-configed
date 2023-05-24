/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.utilities.logging.Logging;

public class HostUpdate implements UpdateCommand {

	private Map<String, Object> newdata;

	private OpsiserviceNOMPersistenceController persis;

	public HostUpdate(OpsiserviceNOMPersistenceController persis, Map<String, Object> newdata) {
		super();
		this.newdata = newdata;
		setController(persis);
	}

	@Override
	public void doCall() {
		Logging.debug(this, "doCall, newdata " + newdata);
		persis.setHostValues(newdata);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (OpsiserviceNOMPersistenceController) obj;
	}

	@Override
	public Object getController() {
		return persis;
	}
}
