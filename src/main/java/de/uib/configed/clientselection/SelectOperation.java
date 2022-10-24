package de.uib.configed.clientselection;

/**
 * An instance of this class represents the data needed for one operation.
 */
public abstract class SelectOperation
{
    private SelectElement element;
    private SelectData data;

    /** Create a new SelectOperation, with this element as the property to operate on. */
    public SelectOperation( SelectElement element )
    {
        this.element = element;
    }
    
    /** Set the data given by the user to filter the clients. */
    public void setSelectData( SelectData data )
    {
        this.data = data;
    }
    
    public SelectElement getElement()
    {
        return element;
    }
    
    public Object getData()
    {
        return data.getData();
    }
    
    public SelectData getSelectData()
    {
        return data;
    }
    
    public String printOperation( String indent )
    {
        return indent + getClassName();
    }
    
    /**
     * Used for serialisation.
     */
    public String getClassName()
    {
        String name = getClass().getCanonicalName();
        return name.substring( name.lastIndexOf('.')+1 );
    }
    
    /**
     * Get the data type needed by this operation.
     */
    public abstract SelectData.DataType getDataType();
    
    /**
     * Get the user-visible string identifying this operation.
     */
     public abstract String getOperationString();
     
    /**
     * Get a debug string for this object 
     */
    @Override
    public String toString()
    {
    	return getClass().getName() + "; data " + data + ", type " + getDataType() 
    	+ ", operation " + getOperationString() + ", element " + getElement();
    }
}
