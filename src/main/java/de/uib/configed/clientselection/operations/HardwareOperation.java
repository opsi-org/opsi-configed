package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.utilities.logging.Logging;

public class HardwareOperation extends SelectGroupOperation {
	public HardwareOperation(SelectOperation operation) {
		registerChildOperation(operation);
	}

	public HardwareOperation(List<SelectOperation> operations) {
		Logging.info(this, "created, with operations " + operations);
		registerChildOperation(operations.get(0));
	}
}