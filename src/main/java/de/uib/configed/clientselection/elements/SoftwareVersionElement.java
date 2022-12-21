package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.configed;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class SoftwareVersionElement extends SelectElement {
	public SoftwareVersionElement() {
		super(new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Version" },
				new String[] { configed.getResourceValue("ClientSelectionDialog.softwareName"),
						configed.getResourceValue("ClientSelectionDialog.softwareProductVersion") });
	}

	@Override
	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<SelectOperation>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}