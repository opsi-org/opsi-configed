package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.configed;

public class SwAuditArchitectureElement extends GenericEnumElement
{
    public SwAuditArchitectureElement()
    {
        super( new String[] {"x86", "x64"}, new String[] {"SwAudit", "Architecture"}, 
            configed.getResourceValue("ClientSelectionDialog.swaudit"), configed.getResourceValue("PanelSWInfo.tableheader_architecture") );
    }
}
