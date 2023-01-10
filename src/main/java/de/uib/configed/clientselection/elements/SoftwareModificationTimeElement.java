package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SoftwareModificationTimeElement extends GenericDateElement {
	public SoftwareModificationTimeElement() {
		super(new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Modification Time" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.lastStateChange"));
	}
}
