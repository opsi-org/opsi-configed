package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.configed;

public class SwAuditSoftwareIdElement extends GenericTextElement
{
    public SwAuditSoftwareIdElement()
    {
        super( new String[]{"SwAudit", "Software ID"}, 
                configed.getResourceValue("ClientSelectionDialog.swaudit"), configed.getResourceValue("PanelSWInfo.tableheader_softwareId") );
    }
}
