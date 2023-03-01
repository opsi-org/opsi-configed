package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.ActionRequest;

public class SoftwareRequestElement extends GenericEnumElement {
	public SoftwareRequestElement() {
		super(removeFirst(2, ActionRequest.getLabels().toArray(new String[0])),
				new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Requested Action" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("ClientSelectionDialog.softwareInstallationRequest"));
	}
}
