/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.InstallationStatus;

public class SoftwareInstallationStatusElement extends GenericEnumElement {
	public SoftwareInstallationStatusElement() {
		super(InstallationStatus.getLabels().toArray(new String[0]), new String[] { NAME, "Installation Status" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("ClientSelectionDialog.softwareInstallationStatus"));
	}
}
