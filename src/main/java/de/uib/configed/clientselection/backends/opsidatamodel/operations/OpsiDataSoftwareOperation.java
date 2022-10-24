package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiDataSoftwareOperation extends SoftwareOperation implements ExecutableOperation
{
	protected Map<String, Map<String, String>> productDefaultStates;
	protected Set<String> productsWithDefaultValues;
	protected de.uib.opsidatamodel.PersistenceController controller;
	
    public OpsiDataSoftwareOperation( SelectOperation operation )
    {
        super(operation);
        controller = de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController();
        if( controller == null )
            logging.warning(this, "Warning, controller is null!");
        productDefaultStates = controller.getProductDefaultStates();
        productsWithDefaultValues = new TreeSet<String>(productDefaultStates.keySet());
    }
    
    public boolean doesMatch( Client client )
    {
        logging.debug(this, "doesMatch starting" );
        OpsiDataClient oClient = (OpsiDataClient) client;
        //logging.debug(this, "doesMatch " + oClient);
        List softwareSet = oClient.getSoftwareList();
        List<String> theProductNames = oClient.getProductNames();
        TreeSet<String> productsWithDefaultValues_client = new TreeSet<String>(productsWithDefaultValues);
        //logging.debug(this, "doesMatch " + softwareSet);
        //logging.debug(this, "doesMatch  productsWithDefaultValues_client " + productsWithDefaultValues_client);
        //logging.debug(this, "doesMatch  theProductNames " + theProductNames); 
        productsWithDefaultValues_client.removeAll(theProductNames);
        //logging.debug(this, "doesMatch  productsWithDefaultValues " + productsWithDefaultValues_client);
        
        //logging.debug(this, "Child: " + getChildOperations().get(0) );
        
        
        for( Object value: softwareSet )
        {
            if( value instanceof Map )
            {
                oClient.setCurrentSoftwareValue( (Map) value );
                logging.debug(this, " getChildOperations().get(0) instance of " + (getChildOperations().get(0)).getClass());
                if( ((ExecutableOperation) getChildOperations().get(0)).doesMatch( client ) )
                    return true;
            }
            else
            {
                logging.error( this, "Software map returned bad value (not a Map)" );
            }
        }
        
        
        for( String product: productsWithDefaultValues_client )
        {
              oClient.setCurrentSoftwareValue( productDefaultStates.get(product) );
              logging.debug(this, " getChildOperations().get(0) check default product values, instance of " + (getChildOperations().get(0)).getClass()); 
              if( ((ExecutableOperation) getChildOperations().get(0)).doesMatch( client ) )
                    return true;
        }
        
        
        return false;
    }
}
