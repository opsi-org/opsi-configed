package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;

public class SoftwareWithPropertiesOperation extends AbstractSelectGroupOperation {
	public SoftwareWithPropertiesOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	public SoftwareWithPropertiesOperation(List<AbstractSelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}
}
