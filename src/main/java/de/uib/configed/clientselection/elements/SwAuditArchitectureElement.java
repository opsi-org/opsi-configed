/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SwAuditArchitectureElement extends GenericEnumElement {
	public SwAuditArchitectureElement() {
		super(new String[] { "x86", "x64" }, new String[] { "SwAudit", "Architecture" },
				Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				Configed.getResourceValue("PanelSWInfo.tableheader_architecture"));
	}
}
