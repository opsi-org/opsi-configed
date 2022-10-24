package de.uib.configed.clientselection;

import java.util.*;

/**
 * This is the base class for all operations operating on a group of operations.
 */
public abstract class SelectGroupOperation extends SelectOperation
{
    private List<SelectOperation> childOperations;
    
    public SelectGroupOperation()
    {
        super( null );
        childOperations = new LinkedList<SelectOperation>();
    }
    
    /** Register an operation as child of this operation. */
    protected void registerChildOperation( SelectOperation operation )
    {
        childOperations.add( operation );
    }
    
    /** Get the registered children. */
    public List<SelectOperation> getChildOperations()
    {
        return childOperations;
    }
    
    @Override
    public SelectData.DataType getDataType()
    {
        return SelectData.DataType.NoneType;
    }
    
    @Override
    public String getOperationString()
    {
        return "";
    }
    
    @Override
    public String printOperation( String indent )
    {
        String result = indent + getClassName() + " {\n";
        for( SelectOperation op: childOperations )
            result += op.printOperation( indent + "\t" ) + "\n";
        return result + indent + "}";
    }
}