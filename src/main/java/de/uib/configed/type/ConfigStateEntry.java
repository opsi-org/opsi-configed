package de.uib.configed.type;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;

public class ConfigStateEntry extends HashMap<String, Object>
{
	public final static String TYPE = "ConfigState";
	
	public final static String DB_TABLE_NAME = "CONFIG_STATE";
	
	final public static String ID = "config_state_id";
	final public static String OBJECT_ID = "objectId";
	final public static String CONFIG_ID = "configId";
	final public static String VALUES = "values";
	
	
	
	public final static LinkedHashMap<String, String> DB_COLUMNS = new LinkedHashMap<String, String>();
	static {
		DB_COLUMNS.put(ID,  DB_TABLE_NAME + "."  + ID);
		DB_COLUMNS.put(CONFIG_ID,  DB_TABLE_NAME + "."  + CONFIG_ID);
		DB_COLUMNS.put(OBJECT_ID,  DB_TABLE_NAME + "."  + OBJECT_ID);
		DB_COLUMNS.put(VALUES, DB_TABLE_NAME + "." + "VALUES");
		//DB_COLUMNS.put(LAST_MODIFICATION, DB_TABLE_NAME + "." + "lastseen");
		
	}
	
	public final static List<String> DB_COLUMN_NAMES = new ArrayList<String>();
	static {
		for (String key : DB_COLUMNS.keySet())
		{
			DB_COLUMN_NAMES.add(DB_COLUMNS.get(key));
		}
	}
	
	//public final static int columnIndexLastStateChange = DB_COLUMN_NAMES.indexOf("modificationTime");
	
	
	public ConfigStateEntry(String configId, String hostId, java.util.List values)
	{
		super();
		put(CONFIG_ID, configId);
		put(OBJECT_ID, hostId);
		put(VALUES, values);
	}
	
}
		



	
	

