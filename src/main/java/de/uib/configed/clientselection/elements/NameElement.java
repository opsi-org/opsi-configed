package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class NameElement extends SelectElement {

	public NameElement(String displayLabel) {
		super(new String[] { "Name" }, displayLabel);
	}

	public NameElement() {
		super(new String[] { "Name" }, Configed.getResourceValue("PanelSWInfo.tableheader_displayName"));
	}

	@Override
	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}