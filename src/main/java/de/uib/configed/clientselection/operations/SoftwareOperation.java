package de.uib.configed.clientselection.operations;

import java.util.*;
import de.uib.configed.clientselection.*;

public class SoftwareOperation extends SelectGroupOperation
{
    public SoftwareOperation( SelectOperation operation )
    {
        registerChildOperation( operation );
    }
    
    public SoftwareOperation( List<SelectOperation> operations )
    {
        registerChildOperation( operations.get(0) );
    }
}