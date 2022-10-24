package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;

public class IntEqualsOperation extends SelectOperation
{   
    public IntEqualsOperation( SelectElement element )
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
        return "=";
    }
}