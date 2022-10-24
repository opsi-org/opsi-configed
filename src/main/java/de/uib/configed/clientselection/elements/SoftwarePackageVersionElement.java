package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.*;

public class SoftwarePackageVersionElement extends SelectElement
{
    public SoftwarePackageVersionElement()
    {
        super( new String[]{de.uib.opsidatamodel.OpsiProduct.NAME, "Package Version"}, configed.getResourceValue("ClientSelectionDialog.softwareName"), 
            configed.getResourceValue("ClientSelectionDialog.softwarePackageVersion") );
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new StringEqualsOperation(this));
        return result;
    }
}
