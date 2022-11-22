package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;

public class PropertiesOperation extends SelectGroupOperation {
	public PropertiesOperation(SelectOperation operation) {
		registerChildOperation(operation);
	}

	public PropertiesOperation(List<SelectOperation> operations) {
		registerChildOperation(operations.get(0));
	}
}
