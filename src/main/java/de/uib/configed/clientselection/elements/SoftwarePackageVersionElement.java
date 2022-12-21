package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.configed;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class SoftwarePackageVersionElement extends SelectElement {
	public SoftwarePackageVersionElement() {
		super(new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Package Version" },
				configed.getResourceValue("ClientSelectionDialog.softwareName"),
				configed.getResourceValue("ClientSelectionDialog.softwarePackageVersion"));
	}

	@Override
	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}
