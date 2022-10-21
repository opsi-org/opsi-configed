package de.uib.utilities.datastructure;

import java.util.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;


public abstract class TableEntry extends LinkedHashMap<String, String>
{
	//very similar to RelationElement
	//static values and methods (resp. methods based on static values)
	//must be defined in subclasses
	
	/*
	protected static List<String> KEYS;
	
	protected static Map<String, String> locale;
	
	@Override
	public String put(String key, String value)
	{
		assert KEYS.indexOf(key) > -1 : "not valid key " + key;
		
		if (KEYS.indexOf(key) > -1)
		{
			return super.put(key, value);
		}
		
		return null;
		
	}
	*/
	
	
	
	public TableEntry(Map entry)
	{
		pureEntry = entry;
		entryRetrieved = entry;
	}
	
	//object values and methods
	protected Map pureEntry;
	protected Map<String, String> entryRetrieved;
	
	
	protected void remap(String key)
	//if key are identically named
	{
		remap(key, key);
	}
	
	
	protected void remap(String key, String keyRetrieved)
	{
		remap(key, keyRetrieved, true);
	}
	
	protected void remap(String key, String keyRetrieved, boolean replaceNull)
	{
		//logging.debug(this, "remap keyRetrieved " + keyRetrieved 
		//	+ ", value "+ entryRetrieved.get(keyRetrieved));
		try
		{
			if (entryRetrieved.get(keyRetrieved) != null)
				put(key, entryRetrieved.get(keyRetrieved));
			
			else
			{ 
				if (replaceNull)
					put(key, "");
			}
		}
		catch(Exception ex)
		{
			logging.debug(this, "remap keyRetrieved, exception " + ex);
			logging.debug(this, "remap keyRetrieved " + keyRetrieved 
			+ ", value "+ pureEntry.get(keyRetrieved)
			+ ", class " + pureEntry.get(keyRetrieved).getClass());
			put(key, "");
		}
			
	}
	
	protected String encodeString(String s)
	{
		return configed.encodeStringFromService(s);
	}
	
	
			
}
