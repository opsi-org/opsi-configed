package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.configed;

public class SwAuditSubversionElement extends GenericTextElement
{
    public SwAuditSubversionElement()
    {
        super( new String[]{"SwAudit", "Subversion"}, 
                configed.getResourceValue("ClientSelectionDialog.swaudit"), "Subversion" );
    }
}
