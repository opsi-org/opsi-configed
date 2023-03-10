package de.uib.configed.clientselection.elements;

import java.util.Set;

import de.uib.configed.Configed;
import de.uib.utilities.logging.Logging;

public class SoftwareNameElement extends GenericTextElement {
	public SoftwareNameElement() {
		super(new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Name" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.productId"));
	}

	public SoftwareNameElement(Set<String> proposedData) {
		super(proposedData, new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Name" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.productId"));

		Logging.debug(this, "proposed " + proposedData);
	}

}