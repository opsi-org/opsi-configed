package de.uib.configed.clientselection.operations;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.utilities.logging.logging;

public class HardwareOperation extends SelectGroupOperation
{
    public HardwareOperation( SelectOperation operation )
    {
        registerChildOperation( operation );
    }
    
    public HardwareOperation( List<SelectOperation> operations )
    {
    	logging.info(this, "created, with operations " + operations);
        registerChildOperation( operations.get(0) );
    }
}