package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;

public class DateLessThanOperation extends SelectOperation
{   
    public DateLessThanOperation( SelectElement element )
    {
        super(element);
    }
    
    @Override
    public SelectData.DataType getDataType()
    {
        return SelectData.DataType.DateType;
    }
    
    @Override
    public String getOperationString()
    {
        return "<";
    }
}