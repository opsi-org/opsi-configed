package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;

public class BigIntLessOrEqualOperation extends SelectOperation
{   
    public BigIntLessOrEqualOperation( SelectElement element )
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
        return "<=";
    }
}