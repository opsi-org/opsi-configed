package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SwAuditVersionElement extends GenericTextElement {
	public SwAuditVersionElement() {
		super(new String[] { "SwAudit", "Version" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				"Version");
	}
}
