package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.TargetConfiguration;

public class SoftwareTargetConfigurationElement extends GenericEnumElement {
	public SoftwareTargetConfigurationElement() {
		super(removeFirst(2, TargetConfiguration.getLabels().toArray(new String[0])),
				new String[] { NAME, "Target Configuration" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.targetConfiguration"));
	}
}
