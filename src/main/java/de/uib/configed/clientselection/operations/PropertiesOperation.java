package de.uib.configed.clientselection.operations;

import java.util.*;
import de.uib.configed.clientselection.*;


public class PropertiesOperation extends SelectGroupOperation
{
    public PropertiesOperation( SelectOperation operation )
    {
        registerChildOperation( operation );
    }
    
    public PropertiesOperation( List<SelectOperation> operations )
    {
        registerChildOperation( operations.get(0) );
    }
} 
 
