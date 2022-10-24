package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;


public class GenericEnumElement extends SelectElement
{   
    protected Vector<String> enumData;

    public GenericEnumElement( String[] enumData, String[] name, String... localizedName )
    {
        super(name, localizedName);
        this.enumData = new Vector<String>(Arrays.asList((String []) enumData));
        //this.enumData.add(0, "*");
    }
    
    public List<SelectOperation> supportedOperations()
    {
        List<SelectOperation> result = new LinkedList<SelectOperation>();
        result.add(new StringEqualsOperation(this));
        return result;
    }
    
    @Override
    public Vector<String> getEnumData()
    {
        return enumData;
    }
    
    @Override
    public boolean hasEnumData()
    {
        return true;
    }
    
//     public SelectOperation createOperation( String operation, SelectData data )
//     {
//         return Backend.getBackend().createOperation( operation, data, this );
//     }

    protected static String[] removeFirst( int n, String[] data )
    {
        return Arrays.copyOfRange(data, n, data.length);
    }
}