package de.uib.configed.clientselection.elements;

import de.uib.configed.configed;

public class SwAuditNameElement extends GenericTextElement {
	public SwAuditNameElement() {
		super(new String[] { "SwAudit", "Name" },
				configed.getResourceValue("ClientSelectionDialog.swaudit"), "Name");
	}
}
