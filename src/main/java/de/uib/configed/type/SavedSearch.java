package de.uib.configed.type;

import java.util.*;
import de.uib.utilities.logging.*;

public class SavedSearch 
{
	
	public static final String CONFIG_KEY = "configed.saved_search";
	public static final String NAME_KEY = "name";
	public static final String DESCRIPTION_KEY = "description";
	
	public String name = "";
	public String serialization = "";
	public String description = "";
	
	
	
	public SavedSearch()
	{
	}
	
	public SavedSearch(String name, String serialization, String description)
	{
		setName(name);
		setSerialization(serialization);
		setDescription(description);
	}
	
	
	public void setName(Object s)
	{
		name = "" + s;
	}
	
	
	public void setSerialization(Object s)
	{
		serialization = "" + s;
	}
	
	public void setDescription(Object s)
	{
		if (s != null)
			description = "" + s;
		else
			description =  name;
	}
	

	
	public String getName()
	{
		return name;
	}
	
	public String getSerialization()
	{
		return serialization;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	
	public String toString()
	{
		return getName() + " ( " + getDescription() + "): " + getSerialization();
	}
}
