/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;

public class NotOperation extends AbstractSelectGroupOperation implements ExecutableOperation {
	public NotOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		return !((ExecutableOperation) getChildOperations().get(0)).doesMatch(client);
	}
}
