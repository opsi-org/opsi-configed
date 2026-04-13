/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import de.uib.configed.gui.Configed;

public class SoftwareModificationTimeElement extends GenericDateElement {
	public SoftwareModificationTimeElement() {
		super(new String[] { NAME, "Modification Time" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.lastStateChange"));
	}
}
