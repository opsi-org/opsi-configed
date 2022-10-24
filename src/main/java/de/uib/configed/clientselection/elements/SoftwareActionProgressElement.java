package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.productstate.ActionProgress;

public class SoftwareActionProgressElement extends GenericEnumElement
{
    public SoftwareActionProgressElement()
    {
        super(new String[0], new String[]{de.uib.opsidatamodel.OpsiProduct.NAME, "Action Progress"}, 
                configed.getResourceValue("ClientSelectionDialog.softwareName"), configed.getResourceValue("InstallationStateTableModel.actionProgress") );
    }
}
