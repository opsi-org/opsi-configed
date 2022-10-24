package de.uib.configed.clientselection.operations;

import java.util.*;
import de.uib.configed.clientselection.*;

public class OrOperation extends SelectGroupOperation implements ExecutableOperation
{
    public OrOperation( List<SelectOperation> operations )
    {
        for( SelectOperation operation: operations )
            registerChildOperation( operation );
    }
    
    public boolean doesMatch( Client client )
    {
        for( SelectOperation operation: getChildOperations() )
        {
            if( operation instanceof ExecutableOperation && ((ExecutableOperation) operation).doesMatch( client ) )
                return true;
        }
        return false;
    }
}