package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;

import de.uib.configed.*;

public class IPElement extends SelectElement
{
    public IPElement()
    {
        super( new String[]{"IP Address"}, configed.getResourceValue("NewClientDialog.IpAddress") );
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new StringEqualsOperation(this));
        return result;
    }
}