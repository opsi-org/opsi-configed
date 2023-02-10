package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;

public class SoftwareOperation extends AbstractSelectGroupOperation {
	public SoftwareOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	public SoftwareOperation(List<AbstractSelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}
}