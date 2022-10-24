package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiDataGroupEqualsOperation extends OpsiDataStringEqualsOperation
{
    public OpsiDataGroupEqualsOperation( String data, SelectElement element )
    {
        super( OpsiDataClient.HOSTINFO_MAP, "", data, element );
    }
    
    @Override
    public boolean doesMatch( Client client )
    {
        OpsiDataClient oClient = (OpsiDataClient) client;
        for( Object obj: oClient.getGroups() )
        {
            String group = (String) obj;
            if( checkData(group) )
                return true;
        }
        return false;
    }
}