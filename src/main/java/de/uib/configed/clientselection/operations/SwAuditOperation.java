package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;

public class SwAuditOperation extends AbstractSelectGroupOperation {
	public SwAuditOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	public SwAuditOperation(List<AbstractSelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}
}
