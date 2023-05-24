/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;

public class NotOperation extends AbstractSelectGroupOperation implements ExecutableOperation {
	public NotOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	public NotOperation(List<AbstractSelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}

	@Override
	public boolean doesMatch(Client client) {
		return !((ExecutableOperation) getChildOperations().get(0)).doesMatch(client);
	}
}
