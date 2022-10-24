package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.*;

public class SoftwareModificationTimeElement extends GenericDateElement
{
    public SoftwareModificationTimeElement()
    {
        super( new String[]{de.uib.opsidatamodel.OpsiProduct.NAME, "Modification Time"}, 
            configed.getResourceValue("ClientSelectionDialog.softwareName"), configed.getResourceValue("InstallationStateTableModel.lastStateChange") );
    }
}
