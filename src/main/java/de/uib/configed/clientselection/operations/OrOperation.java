package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;

public class OrOperation extends SelectGroupOperation implements ExecutableOperation {
	public OrOperation(List<SelectOperation> operations) {
		for (SelectOperation operation : operations)
			registerChildOperation(operation);
	}

	@Override
	public boolean doesMatch(Client client) {
		for (SelectOperation operation : getChildOperations()) {
			if (operation instanceof ExecutableOperation && ((ExecutableOperation) operation).doesMatch(client))
				return true;
		}
		return false;
	}
}