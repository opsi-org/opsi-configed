package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.configed;

public class SwAuditLicenseKeyElement extends GenericTextElement
{
    public SwAuditLicenseKeyElement()
    {
        super( new String[]{"SwAudit", "License Key"},
                configed.getResourceValue("ClientSelectionDialog.swaudit"), configed.getResourceValue("PanelSWInfo.tableheader_displayLicenseKey") );
    }
}
