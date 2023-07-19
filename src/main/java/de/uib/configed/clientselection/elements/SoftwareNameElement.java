/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import java.util.Set;

import de.uib.configed.Configed;
import de.uib.utilities.logging.Logging;

public class SoftwareNameElement extends GenericTextElement {
	public SoftwareNameElement() {
		super(new String[] { NAME, "Name" }, Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.productId"));
	}

	public SoftwareNameElement(Set<String> proposedData) {
		super(proposedData, new String[] { NAME, "Name" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.productId"));

		Logging.debug(this.getClass(), "proposed " + proposedData);
	}
}
