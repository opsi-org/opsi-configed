package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class SoftwarePackageVersionElement extends AbstractSelectElement {
	public SoftwarePackageVersionElement() {
		super(new String[] { NAME, "Package Version" }, Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("ClientSelectionDialog.softwarePackageVersion"));
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new LinkedList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}
