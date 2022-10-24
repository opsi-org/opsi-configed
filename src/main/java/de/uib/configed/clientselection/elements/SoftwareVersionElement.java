package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;

import de.uib.configed.*;

public class SoftwareVersionElement extends SelectElement
{
    public SoftwareVersionElement()
    {
        super(new String[] {de.uib.opsidatamodel.OpsiProduct.NAME, "Version"}, 
            new String[] {configed.getResourceValue("ClientSelectionDialog.softwareName"),configed.getResourceValue("ClientSelectionDialog.softwareProductVersion") });
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new StringEqualsOperation(this));
        return result;
    }
}