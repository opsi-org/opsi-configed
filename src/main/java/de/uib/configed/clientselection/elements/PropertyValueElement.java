package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class PropertyValueElement extends SelectElement {
	public PropertyValueElement() {
		super(new String[] { "Property-Value" }, "opsi-Product/Property/Value");
	}

	@Override
	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<SelectOperation>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}
