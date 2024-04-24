/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.TargetConfiguration;

public class SoftwareTargetConfigurationElement extends GenericEnumElement {
	public SoftwareTargetConfigurationElement() {
		super(TargetConfiguration.getLabels().toArray(new String[0]), new String[] { NAME, "Target Configuration" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.targetConfiguration"));
	}
}
