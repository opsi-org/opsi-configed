package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.productstate.ActionResult;

public class SoftwareActionResultElement extends GenericEnumElement
{
    public SoftwareActionResultElement()
    {
        super( removeFirst(2, ActionResult.getLabels().toArray(new String[0])), new String[]{de.uib.opsidatamodel.OpsiProduct.NAME, "Action Result"},
                 configed.getResourceValue("ClientSelectionDialog.softwareName"), configed.getResourceValue("InstallationStateTableModel.actionResult") );
    }
}
