package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.utilities.logging.Logging;

public class HardwareOperation extends AbstractSelectGroupOperation {
	public HardwareOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	public HardwareOperation(List<AbstractSelectOperation> operations) {
		Logging.info(this, "created, with operations " + operations);
		registerChildOperation(operations.get(0));
	}
}
