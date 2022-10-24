package de.uib.opsidatamodel.datachanges;

import de.uib.opsidatamodel.*;
import java.util.Map;
import de.uib.utilities.logging.*;

public class ConfigUpdate implements UpdateCommand
{
    String objectId;
    Map newdata;
    
    PersistenceController persis;
    
    public ConfigUpdate (PersistenceController persis, Map newdata)
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
    
    public void doCall()
    {
    		logging.info(this, "doCall, setting class " + newdata.getClass() + ", the new data is " + newdata); 
    			//logging.debug(this, "retrieved property:" + config.getRetrieved());
    		persis.setConfig(newdata);
    }
}
   
