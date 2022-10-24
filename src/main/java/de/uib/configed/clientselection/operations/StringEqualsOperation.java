package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.*;
import de.uib.utilities.logging.logging;

public class StringEqualsOperation extends SelectOperation
{   
    public StringEqualsOperation( SelectElement element )
    {
        super(element);
        //logging.info(this, " element " + element);
    }
    
    @Override
    public SelectData.DataType getDataType()
    {
        return SelectData.DataType.TextType;
    }
    
    @Override
    public String getOperationString()
    {
        return "=";
    }
}