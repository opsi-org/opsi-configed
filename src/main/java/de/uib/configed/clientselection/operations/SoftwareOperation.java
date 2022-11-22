package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;

public class SoftwareOperation extends SelectGroupOperation {
	public SoftwareOperation(SelectOperation operation) {
		registerChildOperation(operation);
	}

	public SoftwareOperation(List<SelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}
}