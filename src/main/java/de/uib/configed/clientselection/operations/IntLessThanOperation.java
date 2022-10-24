package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;

public class IntLessThanOperation extends SelectOperation
{   
    public IntLessThanOperation( SelectElement element )
    {
        super(element);
    }
    
    @Override
    public SelectData.DataType getDataType()
    {
        return SelectData.DataType.IntegerType;
    }
    
    @Override
    public String getOperationString()
    {
        return "<";
    }
}