package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.LastAction;

public class SoftwareLastActionElement extends GenericEnumElement {
	public SoftwareLastActionElement() {
		super(removeFirst(2, LastAction.getLabels().toArray(new String[0])),
				new String[] { de.uib.opsidatamodel.OpsiProduct.NAME, "Last Action" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.lastAction"));
	}
}
