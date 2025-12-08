/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import de.uib.configed.core.domain.productstate.LastAction;
import de.uib.configed.gui.Configed;

public class SoftwareLastActionElement extends GenericEnumElement {
	public SoftwareLastActionElement() {
		super(LastAction.getLabels().toArray(new String[0]), new String[] { NAME, "Last Action" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.lastAction"));
	}
}
