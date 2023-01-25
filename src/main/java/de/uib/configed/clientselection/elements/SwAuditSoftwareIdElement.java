package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class SwAuditSoftwareIdElement extends GenericTextElement {
	public SwAuditSoftwareIdElement() {
		super(new String[] { "SwAudit", "Software ID" }, Configed.getResourceValue("ClientSelectionDialog.swaudit"),
				Configed.getResourceValue("PanelSWInfo.tableheader_softwareId"));
	}
}
