package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;


public class GenericBigIntegerElement extends SelectElement
{
    public GenericBigIntegerElement( String[] name, String... localizedName )
    {
        super(name, localizedName);
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new BigIntLessThanOperation(this));
        result.add(new BigIntLessOrEqualOperation(this));
        result.add(new BigIntGreaterThanOperation(this));
        result.add(new BigIntGreaterOrEqualOperation(this));
        result.add(new BigIntEqualsOperation(this));
        return result;
    }
    
//     public SelectOperation createOperation( String operation, SelectData data )
//     {
//         return Backend.getBackend().createOperation( operation, data, this );
//     }
}