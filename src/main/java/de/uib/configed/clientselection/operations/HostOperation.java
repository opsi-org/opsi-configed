/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;

public class HostOperation extends AbstractSelectGroupOperation implements ExecutableOperation {
	public HostOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	public HostOperation(List<AbstractSelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}

	@Override
	public boolean doesMatch(Client client) {
		return ((ExecutableOperation) getChildOperations().get(0)).doesMatch(client);
	}
}
