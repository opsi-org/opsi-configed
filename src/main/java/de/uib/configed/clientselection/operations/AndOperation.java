package de.uib.configed.clientselection.operations;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;

/**
 * Connects two or more operations with a logical and, i.e. this Operation only
 * matches if all child operations match.
 */
public class AndOperation extends SelectGroupOperation implements ExecutableOperation {
	private List<SelectOperation> operations;

	public AndOperation(List<SelectOperation> operations) {
		this.operations = new LinkedList<SelectOperation>();
		for (SelectOperation operation : operations) {
			this.operations.add(operation);
			registerChildOperation(operation);
		}
	}

	@Override
	public boolean doesMatch(Client client) {
		for (SelectOperation operation : operations) {
			if (!((ExecutableOperation) operation).doesMatch(client)) {
				return false;
			}
		}
		return true;
	}
}
