/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import java.util.Set;

import de.uib.configed.gui.Configed;
import de.uib.configed.share.logging.Logging;

public class SoftwareNameElement extends GenericTextElement {
	public SoftwareNameElement() {
		super(new String[] { NAME, "Name" }, Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.productId"));
	}

	public SoftwareNameElement(Set<String> proposedData) {
		super(proposedData, new String[] { NAME, "Name" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.productId"));

		Logging.debug(this, "proposed ", proposedData);
	}
}
