package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;

public class OrOperation extends AbstractSelectGroupOperation implements ExecutableOperation {
	public OrOperation(List<AbstractSelectOperation> operations) {
		for (AbstractSelectOperation operation : operations) {
			registerChildOperation(operation);
		}
	}

	@Override
	public boolean doesMatch(Client client) {
		for (AbstractSelectOperation operation : getChildOperations()) {
			if (operation instanceof ExecutableOperation && ((ExecutableOperation) operation).doesMatch(client)) {
				return true;
			}
		}
		return false;
	}
}
