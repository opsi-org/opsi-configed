package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.InstallationStatus;

public class SoftwareInstallationStatusElement extends GenericEnumElement {
	public SoftwareInstallationStatusElement() {
		super(removeFirst(2, InstallationStatus.getLabels().toArray(new String[0])),
				new String[] { NAME, "Installation Status" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("ClientSelectionDialog.softwareInstallationStatus"));
	}
}
