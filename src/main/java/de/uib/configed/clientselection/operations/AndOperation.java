package de.uib.configed.clientselection.operations;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;

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
