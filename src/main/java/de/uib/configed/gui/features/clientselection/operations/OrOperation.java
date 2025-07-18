/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.operations;

import java.util.List;

import de.uib.configed.gui.features.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;

public class OrOperation extends AbstractSelectGroupOperation implements ExecutableOperation {
	public OrOperation(List<AbstractSelectOperation> operations) {
		for (AbstractSelectOperation operation : operations) {
			registerChildOperation(operation);
		}
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		for (AbstractSelectOperation operation : getChildOperations()) {
			if (operation instanceof ExecutableOperation executableOperation && executableOperation.doesMatch(client)) {
				return true;
			}
		}
		return false;
	}
}
