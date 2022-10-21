package de.uib.configed.type;
import java.util.*;
import de.uib.utilities.logging.*;

public class RetrievedMap extends HashMap<String, Object>
{
	protected Map<String, Object> retrieved; //pass the original map 
	protected Map<String, String>  classnames;
	
	public RetrievedMap(Map<String, Object> retrieved)
	{
		super();
		this.retrieved = retrieved;
		classnames = new HashMap<String, String>();
		build();
	}
	
	protected void build()
	{
		if (retrieved == null)
			return;
		
		Iterator iter = retrieved.keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			Object value  = retrieved.get(key);
			classnames.put(key, value.getClass().getName());
			put(key, value);
		}
	}
	
	
	public Map<String, Object> getRetrieved()
	{
		return retrieved;
	}
	
	public void rebuild()
	{
		build();
	}
	
	public Map<String, String> getClassnames()
	{
		return classnames;
	}
	
}
