/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import de.uib.configed.gui.Configed;

public class SwAuditLicenseKeyElement extends GenericTextElement {
	public SwAuditLicenseKeyElement() {
		super(new String[] { "SwAudit", "License Key" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				Configed.getResourceValue("PanelSWInfo.tableheader_displayLicenseKey"));
	}
}
