package de.uib.opsidatamodel.datachanges;

import de.uib.opsidatamodel.*;
import java.util.Map;
import de.uib.utilities.logging.*;


public class ProductpropertiesUpdate implements UpdateCommand
{
    String pcname;
    String productname; 
    Map newdata;
    
    PersistenceController persis;
    
    public ProductpropertiesUpdate (PersistenceController persis, String pcname, String productname, Map newdata)
    {
      this.pcname = pcname;
      this.productname = productname;
      this.newdata = newdata;
      setController(persis);
    }

	public void setController( Object obj )
	{
	   this.persis = (PersistenceController) obj;  
	}
	
	public Object getController( )
	{
	   return persis;  
	}
	
	public void doCall()
	{
		//if (newdata != null) logging.debug(this, "doCall, setting " + newdata + ", class " + newdata.getClass());
		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue)
		{
			de.uib.configed.type.ConfigName2ConfigValue configState = (de.uib.configed.type.ConfigName2ConfigValue) newdata;
			//logging.info(this, "doCall, set " + newdata + " , we retrieved: " + configState.getRetrieved());
			
			persis.setProductproperties(pcname, productname, newdata);
		}
	}
	
	public void revert()
	{
		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue)
		{
			//logging.info(this, "revert, rebuild  newdata " + newdata);
			((de.uib.configed.type.ConfigName2ConfigValue) newdata).rebuild();
			
			//logging.infothis, "revert, rebuilt newdata " + newdata );
		}
	}
		
}
   
    
    
    
    
