package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiDataHardwareOperation extends HardwareOperation implements ExecutableOperation
{
    public OpsiDataHardwareOperation( SelectOperation operation )
    {
        super(operation);
        logging.info(this, "created");
    }
    
    public boolean doesMatch( Client client )
    {
    	 //logging.debug(this, "doesMatch " + client);
        OpsiDataClient oClient = (OpsiDataClient) client;
        oClient.startHardwareIterator();
        while( true )
        {
            if( ((ExecutableOperation) getChildOperations().get(0)).doesMatch(client) )
            {
            	  //logging.debug(this,  "doesMatch  operation " + getChildOperations().get(0)); 
                return true;
            }
            if( !oClient.hardwareIteratorNext() )
                break;
        }
        return false;
    }
}
