/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.productstate.ActionResult;

public class SoftwareActionResultElement extends GenericEnumElement {
	public SoftwareActionResultElement() {
		super(removeFirst(2, ActionResult.getLabels().toArray(new String[0])), new String[] { NAME, "Action Result" },
				Configed.getResourceValue("ClientSelectionDialog.softwareName"),
				Configed.getResourceValue("InstallationStateTableModel.actionResult"));
	}
}
