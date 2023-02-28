package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;

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
