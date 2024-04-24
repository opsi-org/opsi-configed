/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.ActionRequest;

public class SoftwareRequestElement extends GenericEnumElement {
	public SoftwareRequestElement() {
		super(ActionRequest.getLabels().toArray(new String[0]), new String[] { NAME, "Requested Action" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("ClientSelectionDialog.softwareInstallationRequest"));
	}
}
