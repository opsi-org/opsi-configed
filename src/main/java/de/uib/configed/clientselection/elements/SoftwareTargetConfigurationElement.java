package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.productstate.TargetConfiguration;

public class SoftwareTargetConfigurationElement extends GenericEnumElement
{
    public SoftwareTargetConfigurationElement()
    {
        super( removeFirst( 2, TargetConfiguration.getLabels().toArray(new String[0])), new String[]{de.uib.opsidatamodel.OpsiProduct.NAME, "Target Configuration"},
                configed.getResourceValue("ClientSelectionDialog.softwareName"), configed.getResourceValue("InstallationStateTableModel.targetConfiguration") );
    }
}
