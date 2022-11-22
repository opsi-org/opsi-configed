package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.IntEqualsOperation;
import de.uib.configed.clientselection.operations.IntGreaterOrEqualOperation;
import de.uib.configed.clientselection.operations.IntGreaterThanOperation;
import de.uib.configed.clientselection.operations.IntLessOrEqualOperation;
import de.uib.configed.clientselection.operations.IntLessThanOperation;

public class GenericIntegerElement extends SelectElement {
	public GenericIntegerElement(String[] name, String... localizedName) {
		super(name, localizedName);
	}

	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<SelectOperation>();
		result.add(new IntLessThanOperation(this));
		result.add(new IntLessOrEqualOperation(this));
		result.add(new IntEqualsOperation(this));
		result.add(new IntGreaterOrEqualOperation(this));
		result.add(new IntGreaterThanOperation(this));

		return result;
	}
}