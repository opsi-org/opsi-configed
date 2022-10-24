package de.uib.configed.type.licences;
import java.util.*;
import de.uib.configed.type.*;
import de.uib.configed.Globals;
import de.uib.utilities.logging.*;

public class LicenceUsableForEntry extends HashMap<String, String>
{
	/*
	desc SOFTWARE_LICENSE_TO_LICENSE_POOL;
	| Field             		| Type         		| Null | Key | Default | Extra 
	| softwareLicenseId 	| varchar(100) 	| NO   | PRI | NULL    |       
	| licensePoolId     		| varchar(100) 	| NO   | PRI | NULL    |       
	| licenseKey        		| varchar(100) 	| NO   |
	
	*/
	
	public final static String idKEY = "id";
	public final static String licenceIdKEY =  "softwareLicenseId";
	public final static String licencePoolIdKEY = "licensePoolId";
	public final static String licencekeyKEY = "licenseKey";
	
	private static List<String> KEYS;
	static  {
		KEYS = new ArrayList<String>();
		KEYS.add( licenceIdKEY );
		KEYS.add( licencePoolIdKEY );
		KEYS.add( licencekeyKEY );
	}
	
	
	public static LicenceUsableForEntry produceFrom(Map<String, Object> importedEntry)
	{
		LicenceUsableForEntry entry = new LicenceUsableForEntry();
		for (String key : importedEntry.keySet())
		{
			entry.put(key, (String) importedEntry.get(key));
		}
		if (importedEntry.get(licenceIdKEY) == null || importedEntry.get(licencePoolIdKEY) == null)
			logging.warning("LicenceUsableForEntry,  missing primary key in " + importedEntry);
		
		String pseudokey = Globals.pseudokey(
			new String[]{
				entry.get(licenceIdKEY),
				entry.get(licencePoolIdKEY)
			}
		);
		
		entry.put(idKEY, pseudokey);
		
		return entry;
		
	}
	
	public String getId()
	{
		return get(idKEY);
	}
	
	public String getLicenceId()
	{
		return get(licenceIdKEY);
	}
	
	public String getLicencePoolId()
	{
		return get(licencePoolIdKEY);
	}
		
	
}
	
