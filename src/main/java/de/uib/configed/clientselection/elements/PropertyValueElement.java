package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;

import de.uib.utilities.logging.logging;
import de.uib.configed.*;


public class PropertyValueElement extends SelectElement
{
    public PropertyValueElement()
    {
        super( new String[] {"Property-Value"}, "opsi-Product/Property/Value");
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new StringEqualsOperation(this));
        return result;
    }
} 
 
