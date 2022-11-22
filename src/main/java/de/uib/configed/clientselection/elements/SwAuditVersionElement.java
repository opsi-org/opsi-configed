package de.uib.configed.clientselection.elements;

import de.uib.configed.configed;

public class SwAuditVersionElement extends GenericTextElement {
	public SwAuditVersionElement() {
		super(new String[] { "SwAudit", "Version" },
				configed.getResourceValue("ClientSelectionDialog.swaudit"), "Version");
	}
}
