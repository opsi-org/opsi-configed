package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiDataIntLessOrEqualOperation extends IntLessOrEqualOperation implements ExecutableOperation
{
    private String map;
    private String key;
    private int data;
    
    public OpsiDataIntLessOrEqualOperation( String map, String key, int data, SelectElement element )
    {
        super(element);
        this.map = map;
        this.key = key;
        this.data = data;
    }
    
    public boolean doesMatch( Client client )
    {
        OpsiDataClient oClient = (OpsiDataClient) client;
        Map realMap = oClient.getMap( map );
        if( !realMap.containsKey(key) || realMap.get(key) == null )
        {
            logging.debug(this, "key " + key + " not found!");
            return false;
        }
        
        Object realData = realMap.get(key);
        if( realData instanceof Integer )
        {
            if( (Integer) realData <= data )
                return true;
        }
        else {
            logging.warning( this, "data is no Integer!");
        }
        return false;
    }
}