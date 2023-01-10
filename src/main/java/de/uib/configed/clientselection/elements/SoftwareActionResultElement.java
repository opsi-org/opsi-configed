package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.ActionResult;

public class SoftwareActionResultElement extends GenericEnumElement {
	public SoftwareActionResultElement() {
		super(removeFirst(2, ActionResult.getLabels().toArray(new String[0])),
				new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Action Result" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.actionResult"));
	}
}
