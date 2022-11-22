package de.uib.configed.clientselection.elements;

import de.uib.configed.configed;

public class SoftwareModificationTimeElement extends GenericDateElement {
	public SoftwareModificationTimeElement() {
		super(new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Modification Time" },
				configed.getResourceValue("ClientSelectionDialog.softwareName"),
				configed.getResourceValue("InstallationStateTableModel.lastStateChange"));
	}
}
