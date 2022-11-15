package de.uib.messages;

public class LocaleRepresentation 
//a String separated by '='
{
	private String value = "";
	
	public LocaleRepresentation(String name) throws Exception
	{
		if (name == null)
			throw new Exception("LocaleRepresentation name must not be null");
		
		value = name;
	}
	
	public LocaleRepresentation(String name, String iconName) throws Exception
	{
		this(name);
		if (iconName != null)
			value = value + "=" + iconName;
	}
	
	public String getName()
	{
		int pos = value.indexOf('=');
		if (pos > -1)
			return value.substring(0, pos);
		
		return value;
	}
	
	public String getIconName()
	{
		int pos = value.indexOf('=');
		if (pos > -1)
			return value.substring(pos+1);
		
		return "";
	}
	
	public String toString()
	{
		return value;
	}
}
	
