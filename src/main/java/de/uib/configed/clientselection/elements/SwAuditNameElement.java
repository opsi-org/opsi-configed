package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SwAuditNameElement extends GenericTextElement {
	public SwAuditNameElement() {
		super(new String[] { "SwAudit", "Name" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"), "Name");
	}
}
