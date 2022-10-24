package de.uib.configed.clientselection.operations;

import java.util.*;
import de.uib.configed.clientselection.*;

public class SwAuditOperation extends SelectGroupOperation
{
    public SwAuditOperation( SelectOperation operation )
    {
        registerChildOperation( operation );
    }
    
    public SwAuditOperation( List<SelectOperation> operations )
    {
        registerChildOperation( operations.get(0) );
    }
}