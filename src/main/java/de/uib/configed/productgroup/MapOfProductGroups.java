package de.uib.configed.productgroup;

import java.util.*;

public class MapOfProductGroups extends HashMap<String, TreeSetBuddy>  
{
		
	public MapOfProductGroups(Map<String, Set<String>> fName2ProductGroup)
	{
		super();
		
		for (String name : fName2ProductGroup.keySet())
		{
			TreeSetBuddy set = new TreeSetBuddy(fName2ProductGroup.get(name));
			put(name, set);
		}
	}
}
			
