package de.uib.opsidatamodel;

import java.util.*;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;
import de.uib.configed.type.*;


public class SavedSearches extends HashMap<String, SavedSearch>
{
	//public static final String SEARCHfailedAnyProduct = "{ \"version\" : \"2\", \"data\" : { \"element\" : null,  \"elementPath\" : null, \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null,  \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] } }";
	public static final String SEARCHfailedProduct  = "{ \"version\" : \"2\", \"data\" : { \"element\" : null, \"elementPath\" : null, \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : null, \"elementPath\" : null, \"operation\" : \"AndOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : \"SoftwareNameElement\", \"elementPath\" : [ \"Product\", \"Name\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"%s\", \"children\" : null }, { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] } ] } }";
	public static final String SEARCHfailedByTimeS =  "{ \"version\" : \"2\", \"data\" : { \"element\" : null, \"elementPath\" : null, \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : null, \"elementPath\" : null, \"operation\" : \"AndOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null }, { \"element\" : \"SoftwareModificationTimeElement\", \"elementPath\" : [ \"Product\", \"Modification Time\" ], \"operation\" : \"DateGreaterOrEqualOperation\", \"dataType\" : DateType, \"data\" : \"%s\", \"children\" : null } ] } ] } }";
	//public static final String SEARCHfailedByTimeTestS =  "{ \"version\" : \"2\", \"data\" : { \"element\" : null, \"elementPath\" : null, \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : null, \"elementPath\" : null, \"operation\" : \"AndOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null }, { \"element\" : \"SoftwareModificationTimeElement\", \"elementPath\" : [ \"Product\", \"Modification Time\" ], \"operation\" : \"DateGreaterOrEqualOperation\", \"dataType\" : DateType, \"data\" : \"2014-01-27\", \"children\" : null } ] } ] } }";
	public static final String SEARCHfailedAtAnyTimeS = "{ \"version\" : \"2\", \"data\" : { \"element\" : null, \"elementPath\" : null, \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] } }";

	public SavedSearches()
	{
	}
	
	public void checkIn(String key, Object value)
	{
		if (!key.startsWith(SavedSearch.CONFIG_KEY))
			return;
		
		String rcPartOfKey = key.substring(SavedSearch.CONFIG_KEY.length());
		
		if (rcPartOfKey.length()<2  || rcPartOfKey.charAt(0) != '.' ) 
			logging.error("No name key given after '" + SavedSearch.CONFIG_KEY + "'");
		else
		{
			rcPartOfKey = rcPartOfKey.substring(1);
			
			int i = nextPartAt(rcPartOfKey);
			
			//first level key
			
			if (i  == -1)
			{
				SavedSearch rc = retrieveRC(rcPartOfKey);
				if (rc.getSerialization().equals(""))
					rc.setSerialization(value);
				
				//if serialized command is specified by an explicit command key, leave it
			}
			
			else
			{
				//second level key
				
				String name = rcPartOfKey.substring(0, i);
				
				SavedSearch rc = retrieveRC(name);
				
				String remainder = rcPartOfKey.substring(i+1);
				
				//logging.debug(this, "checkIn, remainder " + remainder);
				
				i = nextPartAt(remainder);
				
				if (i ==-1)
				{
					if (remainder.equals(SavedSearch.DESCRIPTION_KEY))
						rc.setDescription(value);
					
					/*
					else if (remainder.equals(SavedSearch.COMMAND_KEY))
						rc.setSerialization(value);
					*/
				}
					
					
					
				else
				//there are no 3rd level keys
					logging.error("Remote control key has too many parts");
			}
				
		}

	}
	
	private int nextPartAt(String remainder)
	{
		int posDot = remainder.indexOf(".");
		if (posDot == -1 || remainder.length() == posDot+1)
		{
			return -1;
		}
		else
			return posDot;
	}
		
		
	private SavedSearch retrieveRC(String name)
	{
		if (get(name) != null)
			return get(name);
		
		else
		{
			SavedSearch rc = new SavedSearch();
			rc.setName(name);
			put(name, rc);
			return rc;
		}
	}
		
			
	
}
		
		
		
	
	
