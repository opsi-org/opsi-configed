package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;

public class NotOperation extends SelectGroupOperation implements ExecutableOperation {
	public NotOperation(SelectOperation operation) {
		registerChildOperation(operation);
	}

	public NotOperation(List<SelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}

	public boolean doesMatch(Client client) {
		return !((ExecutableOperation) getChildOperations().get(0)).doesMatch(client);
	}
}