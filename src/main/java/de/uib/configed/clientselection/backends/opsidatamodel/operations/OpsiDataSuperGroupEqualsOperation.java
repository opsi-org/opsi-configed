package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiDataSuperGroupEqualsOperation extends OpsiDataStringEqualsOperation
{
	private static boolean issuedTreeError = false;
    public OpsiDataSuperGroupEqualsOperation( String data, SelectElement element )
    {
        super( OpsiDataClient.HOSTINFO_MAP, "", data, element );
    }
    
    @Override
    public boolean doesMatch( Client client )
    {
    	//System.out.println( " ------------ ");
    	//System.out.println( " client " + client );
        OpsiDataClient oClient = (OpsiDataClient) client;
        if ( oClient.getSuperGroups() == null )
        {
        	if (!issuedTreeError)
        	{
        		logging.writeToConsole( "Selection by tree structure not possible in headless mode, please remove this selection criterion.");
        		logging.writeToConsole( "( The tree is built by the visual component.) ");
        		issuedTreeError = true;
        	}
        	return false;
        }
        	
        for( Object obj: oClient.getSuperGroups() )
        {
            String group = (String) obj;
            if( checkData(group) )
                return true;
        }
        return false;
    }
}