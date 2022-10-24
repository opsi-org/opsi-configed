package de.uib.configed.clientselection.operations;

import java.util.*;
import de.uib.configed.clientselection.*;

public class HostOperation extends SelectGroupOperation implements ExecutableOperation
{   
    public HostOperation( SelectOperation operation )
    {
        registerChildOperation( operation );
    }

    public HostOperation( List<SelectOperation> operations )
    {
        registerChildOperation( operations.get(0) );
    }
    
    @Override
    public boolean doesMatch( Client client )
    {
        return ((ExecutableOperation) getChildOperations().get(0)).doesMatch( client );
    }
}