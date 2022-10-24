package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.configed;

public class SwAuditLanguageElement extends GenericTextElement
{
    public SwAuditLanguageElement()
    {
        super( new String[]{"SwAudit", "Language"}, 
                configed.getResourceValue("ClientSelectionDialog.swaudit"), configed.getResourceValue("PanelSWInfo.tableheader_displayLanguage") );
    }
}
