/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.clientselection.elements;

import de.uib.configed.gui.Configed;

public class SwAuditSoftwareIdElement extends GenericTextElement {
	public SwAuditSoftwareIdElement() {
		super(new String[] { "SwAudit", "Software ID" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				Configed.getResourceValue("PanelSWInfo.tableheader_softwareId"));
	}
}
