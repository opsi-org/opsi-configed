package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;
import java.math.BigInteger;

public class OpsiDataBigIntLessThanOperation extends BigIntLessThanOperation implements ExecutableOperation
{
    private String map;
    private String key;
    private long data;
    
    public OpsiDataBigIntLessThanOperation( String map, String key, long data, SelectElement element )
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
        logging.debug( this, realData.getClass().getCanonicalName() );
        if( realData instanceof Long )
        {
            if( (Long) realData < data )
                return true;
        }
        else {
            if( realData instanceof Integer )
            {
                if( (Integer) realData < data )
                    return true;
            }
            else
            {
                logging.error( this, "data is no BigInteger!"+realData);
            }
        }
        return false;
    }
}