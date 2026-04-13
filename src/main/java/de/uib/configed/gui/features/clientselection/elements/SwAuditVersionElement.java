/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import de.uib.configed.gui.Configed;

public class SwAuditVersionElement extends GenericTextElement {
	public SwAuditVersionElement() {
		super(new String[] { "SwAudit", "Version" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				"Version");
	}
}
