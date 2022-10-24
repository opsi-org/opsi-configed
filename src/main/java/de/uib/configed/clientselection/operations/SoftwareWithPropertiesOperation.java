package de.uib.configed.clientselection.operations;

import java.util.*;
import de.uib.configed.clientselection.*;


public class SoftwareWithPropertiesOperation extends SelectGroupOperation
{
    public SoftwareWithPropertiesOperation( SelectOperation operation )
    {
        registerChildOperation( operation );
    }
    
    public SoftwareWithPropertiesOperation( List<SelectOperation> operations )
    {
        registerChildOperation( operations.get(0) );
    }
} 
