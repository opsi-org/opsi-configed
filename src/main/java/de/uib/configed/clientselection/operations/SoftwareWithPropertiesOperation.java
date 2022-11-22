package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;

public class SoftwareWithPropertiesOperation extends SelectGroupOperation {
	public SoftwareWithPropertiesOperation(SelectOperation operation) {
		registerChildOperation(operation);
	}

	public SoftwareWithPropertiesOperation(List<SelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}
}
