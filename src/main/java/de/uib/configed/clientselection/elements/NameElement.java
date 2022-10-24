package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;

import de.uib.utilities.logging.logging;
import de.uib.configed.*;

public class NameElement extends SelectElement
{
	
    public NameElement(String displayLabel) 
    {
        super( new String[] {"Name"}, displayLabel);
    }
    
    public NameElement()
    {
        super( new String[] {"Name"}, configed.getResourceValue("PanelSWInfo.tableheader_displayName") );
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new StringEqualsOperation(this));
        return result;
    }
}