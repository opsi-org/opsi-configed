package de.uib.configed.clientselection.operations;

import java.util.*;
import de.uib.configed.clientselection.*;

public class NotOperation extends SelectGroupOperation implements ExecutableOperation
{
    public NotOperation( SelectOperation operation )
    {
        registerChildOperation( operation );
    }
    
    public NotOperation( List<SelectOperation> operations )
    {
        registerChildOperation( operations.get(0) );
    }
    
    public boolean doesMatch( Client client )
    {
        return !((ExecutableOperation) getChildOperations().get(0)).doesMatch(client);
    }
}