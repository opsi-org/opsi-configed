package de.uib.configed.type.licences;
import java.util.*;
import de.uib.configed.type.*;
import de.uib.utilities.datastructure.*;

public class LicencepoolEntry extends TableEntry
{
	
	public final static String idSERVICEKEY = "licensePoolId";
	public final static String idKEY = "id";
	public final static String descriptionKEY = "description";
	
	private static List<String> KEYS;
	static  {
		KEYS = new ArrayList<String>();
		KEYS.add(idSERVICEKEY);
		KEYS.add(descriptionKEY);
	}
	
	private static Map<String, String> locale;
	
	static {
		locale = new HashMap<String, String>();
	}
	
	public static List<String> getKeys()
	{
		return KEYS;
	}
	
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
	
	public LicencepoolEntry(Map entry)
	{
		super(entry);
		remap(idSERVICEKEY, idKEY);
		remap(descriptionKEY,  descriptionKEY);
	}
	
	public String getLicencepoolId()
	{
		if ( get( idSERVICEKEY ) != null )
			return get ( idSERVICEKEY );
		
		return get( idKEY );
	}
	
}
