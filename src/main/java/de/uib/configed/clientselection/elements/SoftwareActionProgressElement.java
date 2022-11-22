package de.uib.configed.clientselection.elements;

import de.uib.configed.configed;

public class SoftwareActionProgressElement extends GenericEnumElement {
	public SoftwareActionProgressElement() {
		super(new String[0], new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Action Progress" },
				configed.getResourceValue("ClientSelectionDialog.softwareName"),
				configed.getResourceValue("InstallationStateTableModel.actionProgress"));
	}
}
