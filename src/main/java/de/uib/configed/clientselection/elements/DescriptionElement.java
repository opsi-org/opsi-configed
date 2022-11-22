package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.configed;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class DescriptionElement extends SelectElement {
	public DescriptionElement() {
		super(new String[] { "Description" },
				/* "Description" */configed.getResourceValue("NewClientDialog.description"));
	}

	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<SelectOperation>();
		result.add(new StringEqualsOperation(this));
		return result;
	}

	// public SelectOperation createOperation( String operation, SelectData data )
	// {
	// return Backend.getBackend().createOperation( operation, data, this );
	// }
}