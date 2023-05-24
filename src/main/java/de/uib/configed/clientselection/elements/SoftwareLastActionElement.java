/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.LastAction;

public class SoftwareLastActionElement extends GenericEnumElement {
	public SoftwareLastActionElement() {
		super(removeFirst(2, LastAction.getLabels().toArray(new String[0])), new String[] { NAME, "Last Action" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.lastAction"));
	}
}
