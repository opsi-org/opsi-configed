/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import de.uib.configed.gui.Configed;

public class SoftwareActionProgressElement extends GenericEnumElement {
	public SoftwareActionProgressElement() {
		super(new String[0], new String[] { NAME, "Action Progress" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.actionProgress"));
	}
}
