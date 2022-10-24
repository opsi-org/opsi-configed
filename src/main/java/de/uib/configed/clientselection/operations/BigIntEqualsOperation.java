package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;

public class BigIntEqualsOperation extends SelectOperation
{   
    public BigIntEqualsOperation( SelectElement element )
    {
        super(element);
    }
    
    @Override
    public SelectData.DataType getDataType()
    {
        return SelectData.DataType.BigIntegerType;
    }
    
    @Override
    public String getOperationString()
    {
        return "=";
    }
}