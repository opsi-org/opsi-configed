/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import java.util.Set;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.features.clientselection.operations.StringEqualsOperation;
import de.uib.configed.share.logging.Logging;

public class OpsiDataConnectionEqualsOperation extends StringEqualsOperation implements ExecutableOperation {
	private String data;

	public OpsiDataConnectionEqualsOperation(String data, AbstractSelectElement element) {
		super(element);
		Logging.debug(this, "OpsiDataConnectionEqualsOperation data: ", data);
		this.data = data;
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		Logging.debug(this, " (OpsiDataStringEqualsOperation) doesMatch client ", client);

		Set<String> clientsConnectedByMessagebus = client.getConnectedByMessagebus();
		Logging.devel(this, "realmap ", clientsConnectedByMessagebus, " client id ", client.getId());
		if (clientsConnectedByMessagebus == null) {
			return false;
		}

		if (Configed.getResourceValue("connected").equals(data)) {
			return clientsConnectedByMessagebus.contains(client.getId());
		}

		return !clientsConnectedByMessagebus.contains(client.getId());
	}
}