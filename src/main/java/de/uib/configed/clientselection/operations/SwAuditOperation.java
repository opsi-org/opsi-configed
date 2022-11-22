package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;

public class SwAuditOperation extends SelectGroupOperation {
	public SwAuditOperation(SelectOperation operation) {
		registerChildOperation(operation);
	}

	public SwAuditOperation(List<SelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}
}