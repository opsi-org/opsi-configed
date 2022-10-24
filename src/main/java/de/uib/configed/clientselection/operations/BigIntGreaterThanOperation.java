package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;

public class BigIntGreaterThanOperation extends SelectOperation
{   
    public BigIntGreaterThanOperation( SelectElement element )
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
        return ">";
    }
}