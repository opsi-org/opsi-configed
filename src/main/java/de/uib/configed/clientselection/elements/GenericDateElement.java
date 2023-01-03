package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.DateEqualsOperation;
import de.uib.configed.clientselection.operations.DateGreaterOrEqualOperation;
import de.uib.configed.clientselection.operations.DateGreaterThanOperation;
import de.uib.configed.clientselection.operations.DateLessOrEqualOperation;
import de.uib.configed.clientselection.operations.DateLessThanOperation;

public class GenericDateElement extends SelectElement {
	public GenericDateElement(String[] name, String... localizedName) {
		super(name, localizedName);
	}

	@Override
	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<>();

		result.add(new DateGreaterThanOperation(this));
		result.add(new DateGreaterOrEqualOperation(this));
		result.add(new DateEqualsOperation(this));
		result.add(new DateLessOrEqualOperation(this));
		result.add(new DateLessThanOperation(this));

		return result;
	}

}