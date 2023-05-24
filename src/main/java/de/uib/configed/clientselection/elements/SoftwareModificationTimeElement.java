/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SoftwareModificationTimeElement extends GenericDateElement {
	public SoftwareModificationTimeElement() {
		super(new String[] { NAME, "Modification Time" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.lastStateChange"));
	}
}
