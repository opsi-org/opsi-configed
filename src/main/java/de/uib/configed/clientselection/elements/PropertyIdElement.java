package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;

import de.uib.utilities.logging.logging;
import de.uib.configed.*;


public class PropertyIdElement extends SelectElement
{
    public PropertyIdElement()
    {
        super( new String[] {"Property-Id"}, "opsi-Product/Property/Id");
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new StringEqualsOperation(this));
        return result;
    }
} 
