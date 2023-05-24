/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SwAuditSubversionElement extends GenericTextElement {
	public SwAuditSubversionElement() {
		super(new String[] { "SwAudit", "Subversion" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				"Subversion");
	}
}
