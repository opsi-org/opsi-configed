package de.uib.opsidatamodel.datachanges;

import de.uib.opsidatamodel.*;
import java.util.Map;
import de.uib.utilities.logging.*;

public abstract class MapUpdate implements UpdateCommand
{
    String objectId;
    Map newdata;
    
    PersistenceController persis;
    
    public MapUpdate (PersistenceController persis, Map newdata)
    {
		this.newdata = newdata;
		setController(persis);
    }

   public void setController( Object obj)
    {
    		this.persis = (PersistenceController) obj;  
    }
    
    public Object getController( )
    {
    		return persis;  
    }
    
    public abstract void doCall();
}

