package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.*;

public class DescriptionElement extends SelectElement
{
    public DescriptionElement()
    {
        super(new String[] {"Description"}, /*"Description"*/configed.getResourceValue("NewClientDialog.description"));
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new StringEqualsOperation(this));
        return result;
    }
    
//     public SelectOperation createOperation( String operation, SelectData data )
//     {
//         return Backend.getBackend().createOperation( operation, data, this );
//     }
}