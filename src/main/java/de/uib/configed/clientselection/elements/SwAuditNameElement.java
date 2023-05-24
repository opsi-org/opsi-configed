/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SwAuditNameElement extends GenericTextElement {
	public SwAuditNameElement() {
		super(new String[] { "SwAudit", "Name" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"), "Name");
	}
}
