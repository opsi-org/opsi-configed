package de.uib.utilities.datastructure;

import java.util.*;


//very similar to TableEntry
public class StringValuedRelationElement extends RelationElement<String, String>
{
	
	public final static String NULLDATE = "0000-00-00";
	
	public StringValuedRelationElement()
	{
		super();
	}
	
	
	public StringValuedRelationElement(StringValuedRelationElement rowmap)
	{
		super(rowmap);
	}
	
	
	public StringValuedRelationElement(java.util.List<String> allowedAttributes, Map<String, ? extends Object> map)
	{
		this();
		//System.out.println("" +map);
		this.allowedAttributes = allowedAttributes;
		produceFrom(map);
		//System.out.println("" + this);
	}
	
	/*
	public String getString(String key)
	{
		return super.get(key);
	}
	*/
	
		
	
	protected void produceFrom(Map<String, ? extends Object > map)
	{
		for (String attribute : allowedAttributes)
		{
			if (map.get(attribute) != null && !map.get(attribute).equals("null"))
			{
				if (map.get(attribute) instanceof String)
					put(attribute, (String) map.get(attribute));
				else //create String object by toString() method
					put(attribute, "" + map.get(attribute));


				if (get(attribute).startsWith(NULLDATE))
					put(attribute, "");


			}
			else
			{
				put(attribute, null);
			}
		}
	}

	
	
			
}



