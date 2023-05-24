/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SwAuditLanguageElement extends GenericTextElement {
	public SwAuditLanguageElement() {
		super(new String[] { "SwAudit", "Language" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				Configed.getResourceValue("PanelSWInfo.tableheader_displayLanguage"));
	}
}
