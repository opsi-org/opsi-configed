package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.BigIntEqualsOperation;
import de.uib.configed.clientselection.operations.BigIntGreaterOrEqualOperation;
import de.uib.configed.clientselection.operations.BigIntGreaterThanOperation;
import de.uib.configed.clientselection.operations.BigIntLessOrEqualOperation;
import de.uib.configed.clientselection.operations.BigIntLessThanOperation;

public class GenericBigIntegerElement extends SelectElement {
	public GenericBigIntegerElement(String[] name, String... localizedName) {
		super(name, localizedName);
	}

	@Override
	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<>();
		result.add(new BigIntLessThanOperation(this));
		result.add(new BigIntLessOrEqualOperation(this));
		result.add(new BigIntGreaterThanOperation(this));
		result.add(new BigIntGreaterOrEqualOperation(this));
		result.add(new BigIntEqualsOperation(this));
		return result;
	}

	
	// {
	
	// }
}