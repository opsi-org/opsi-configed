package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class PropertyIdElement extends SelectElement {
	public PropertyIdElement() {
		super(new String[] { "Property-Id" }, "opsi-Product/Property/Id");
	}

	@Override
	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<SelectOperation>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}
