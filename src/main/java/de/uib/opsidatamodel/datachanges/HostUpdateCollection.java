package de.uib.opsidatamodel.datachanges;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.*;
import java.util.*;

/**
*/   
public  class HostUpdateCollection extends UpdateCollection 
{
	PersistenceController persis;
	
	public HostUpdateCollection(Object persis)
	{
		super(new Vector(0)); 
		setController(persis);
	}
	
	public void setController(Object obj)
	{
		this.persis = (PersistenceController) obj;  
	}
	
	public boolean addAll(Collection c)
	{
		boolean  result = true;
		
		Iterator it = c.iterator();
		while (it.hasNext())
		{
			Map map = null;
			Object obj = it.next();
		
			try
			{
				map = (Map) obj;
			}
			
			catch (ClassCastException ccex)
			{
				result = false;
				logging.debugOut(logging.LEVEL_ERROR, "wrong element type, found " + obj.getClass().getName() 
																								 + ", expected a Map" );
			}
				
			result = add (new HostUpdate(persis, map));
		}
		return result;
	}
	
	public void clearElements()
	{
		logging.debug(this, "clearElements()");
		clear();
	}
		
}
