package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SoftwareActionProgressElement extends GenericEnumElement {
	public SoftwareActionProgressElement() {
		super(new String[0], new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Action Progress" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.actionProgress"));
	}
}
