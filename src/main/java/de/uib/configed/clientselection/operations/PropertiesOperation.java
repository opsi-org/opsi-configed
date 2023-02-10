package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;

public class PropertiesOperation extends AbstractSelectGroupOperation {
	public PropertiesOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	public PropertiesOperation(List<AbstractSelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}
}
