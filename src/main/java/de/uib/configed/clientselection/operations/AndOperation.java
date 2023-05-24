/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.operations;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;

/**
 * Connects two or more operations with a logical and, i.e. this Operation only
 * matches if all child operations match.
 */
public class AndOperation extends AbstractSelectGroupOperation implements ExecutableOperation {
	private List<AbstractSelectOperation> operations;

	public AndOperation(List<AbstractSelectOperation> operations) {
		this.operations = new LinkedList<>();
		for (AbstractSelectOperation operation : operations) {
			this.operations.add(operation);
			registerChildOperation(operation);
		}
	}

	@Override
	public boolean doesMatch(Client client) {
		for (AbstractSelectOperation operation : operations) {
			if (!((ExecutableOperation) operation).doesMatch(client)) {
				return false;
			}
		}
		return true;
	}
}
