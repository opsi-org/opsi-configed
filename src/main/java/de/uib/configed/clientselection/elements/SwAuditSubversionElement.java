package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SwAuditSubversionElement extends GenericTextElement {
	public SwAuditSubversionElement() {
		super(new String[] { "SwAudit", "Subversion" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				"Subversion");
	}
}
