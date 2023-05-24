/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.HardwareOperation;
import de.uib.utilities.logging.Logging;

public class OpsiDataHardwareOperation extends HardwareOperation implements ExecutableOperation {
	public OpsiDataHardwareOperation(AbstractSelectOperation operation) {
		super(operation);
		Logging.info(this, "created");
	}

	@Override
	public boolean doesMatch(Client client) {

		OpsiDataClient oClient = (OpsiDataClient) client;
		oClient.startHardwareIterator();
		while (true) {
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client)) {

				return true;
			}
			if (!oClient.hardwareIteratorNext()) {
				break;
			}
		}
		return false;
	}
}
