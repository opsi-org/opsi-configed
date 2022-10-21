package de.uib.opsidatamodel.permission;

import java.util.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.configed.*;
import java.sql.Timestamp;

/*

	concept originally designed eg. as follows 

		cf.
		de.uib.configed.type.user.UserXRole.getRole()

	if GLOBAL_READONLY (can be specified by user group or in config)
		everything readonly (ignoring a config  SERVER_READWRITE)

		if DEPOTACCESS_ONLY_AS_SPECIFIED: (default general access!)
			access only to specified depots

	else
		if SERVER_READWRITE false or not set:
			write access to server

		if DEPOTACCESS_AS_SPECIFIED (default general access!)
			access only to specified depots
			(of course, the user could change this in case of server_readwrite)



	*/




public class UserConfig
{
	public static String CONFIGKEY_STR_USER = "user"; // de.uib.opsidatamodel.PersistenceController.KEY_USER_ROOT;
	
	public final static String ROLE = "role";
	
	public final static String KEY_USER_ROOT =  CONFIGKEY_STR_USER;
	public final static String KEY_USER_ROLE_ROOT =  KEY_USER_ROOT + "."  + ROLE;
	public final static String ALL_USER_KEY_START = KEY_USER_ROOT + ".{}.";
	
	public final static String START_USER_KEY = UserConfig.KEY_USER_ROOT + ".{";
	

	
	public final static String DEFAULT_ROLE_NAME = "default";
	public final static String ARCHEO_ROLE_NAME = "archeo";
	public final static String NONE_PROTOTYPE = "";
	//public final static String RESTRICTED_ROLE_NAME = "restricted";
	
	public final static String roleBranchPart = KEY_USER_ROLE_ROOT;
	
	public final static String HAS_ROLE_ATTRIBUT = "has_role";
	public final static String MODIFICATION_INFO_KEY = "modified";
	
	public final static ArrayList<Object> EMPTY_LIST = new ArrayList<Object>();
	public final static ArrayList<Object> BOOLEAN_POSSIBLE_VALUES = new ArrayList<Object>();
	static{
		BOOLEAN_POSSIBLE_VALUES.add(true);
		BOOLEAN_POSSIBLE_VALUES.add(false);
	}
	
	public final static ArrayList<Object> ZERO_TIME;
	static{
		ZERO_TIME = new ArrayList<Object>();
		ZERO_TIME. add("0000-00-00 00:00:00");
	}
	
	
	protected String username; 
	
	protected UserConfig prototypeConfig;
	
	protected LinkedHashMap<String, Boolean> booleanMap;
	protected LinkedHashMap<String, java.util.List<Object>> valuesMap;
	protected LinkedHashMap<String, java.util.List<Object>> possibleValuesMap;
	
	
	
	// ============================================================================================
	
	
	
	private static LinkedHashSet<String> USER_BOOL_KEYS;
	
	public static LinkedHashSet<String> getUserBoolKeys()
	{
		if (
			USER_BOOL_KEYS == null
		)
		{
			USER_BOOL_KEYS = new LinkedHashSet<String>();
		
			logging.info("addAll ssh bool keys");
			USER_BOOL_KEYS.addAll( UserSshConfig.BOOL_KEYS );
			logging.info("addAll opsipermission bool keys");
			USER_BOOL_KEYS.addAll( UserOpsipermission.BOOL_KEYS );
		}
		
		return USER_BOOL_KEYS;
	
	}
	
	private static LinkedHashSet<String> USER_LIST_KEYS;
	
	private static LinkedHashSet<String> USER_STRINGVALUE_KEYS;
	
	private static LinkedHashSet<String> USER_STRINGVALUE_KEYS_WITHOUT_ROLE;
	
	
	public static LinkedHashSet<String> getUserStringValueKeys()
	{
		if (
			USER_STRINGVALUE_KEYS == null
		)
		{
			USER_STRINGVALUE_KEYS = new LinkedHashSet<String> (getUserStringValueKeys_withoutRole());
			USER_STRINGVALUE_KEYS.add( HAS_ROLE_ATTRIBUT );
		}
		
		return USER_STRINGVALUE_KEYS;
	}
		

	public static LinkedHashSet<String> getUserStringValueKeys_withoutRole()
	{
		if (USER_STRINGVALUE_KEYS_WITHOUT_ROLE == null)
		{
			USER_STRINGVALUE_KEYS_WITHOUT_ROLE = new LinkedHashSet<String>();
			USER_STRINGVALUE_KEYS_WITHOUT_ROLE.add(  MODIFICATION_INFO_KEY );
		}
		
		return USER_STRINGVALUE_KEYS_WITHOUT_ROLE;
		
	}
	
	
	public static LinkedHashSet<String> getUserListKeys()
	{
		//logging.info("getUserListKeys " + USER_LIST_KEYS);
		if (
			USER_LIST_KEYS == null
		)
		{
			USER_LIST_KEYS = new LinkedHashSet<String>();
			
			USER_LIST_KEYS.addAll( UserSshConfig.LIST_KEYS );
			USER_LIST_KEYS.addAll( UserOpsipermission.LIST_KEYS );
		}
		//logging.info("getUserListKeys " + USER_LIST_KEYS);
				
		
		return USER_LIST_KEYS;
	}
	
	
	//default UserConfig Objects
	private static UserConfig ARCHEO_PROTOTYPE_CONFIG;
	public static final UserConfig getArcheoConfig()
	{
		logging.info("getArcheoConfig");
		if (ARCHEO_PROTOTYPE_CONFIG == null)
		{
			ARCHEO_PROTOTYPE_CONFIG = new UserConfig( ARCHEO_ROLE_NAME  );
			ARCHEO_PROTOTYPE_CONFIG.setValues( HAS_ROLE_ATTRIBUT, EMPTY_LIST );
		}
		
		getUserBoolKeys();
		getUserListKeys();
		
		ARCHEO_PROTOTYPE_CONFIG.booleanMap.putAll( UserSshConfig.DEFAULT.booleanMap );
		ARCHEO_PROTOTYPE_CONFIG.booleanMap.putAll( UserOpsipermission.DEFAULT.booleanMap );
		
		ARCHEO_PROTOTYPE_CONFIG.valuesMap.putAll( UserOpsipermission.DEFAULT.valuesMap );
		ARCHEO_PROTOTYPE_CONFIG.possibleValuesMap.putAll( UserOpsipermission.DEFAULT.possibleValuesMap );
		
		ARCHEO_PROTOTYPE_CONFIG.setValues( MODIFICATION_INFO_KEY, ZERO_TIME );
		
		return ARCHEO_PROTOTYPE_CONFIG;
	}
		
	public UserConfig( String userName )
	{
		logging.info(this, "create for " + userName);
		this.username = userName;
		booleanMap = new LinkedHashMap<String, Boolean>();
		valuesMap = new LinkedHashMap<String, java.util.List<Object>>();
		possibleValuesMap = new LinkedHashMap<String, java.util.List<Object>>();
	}
	
	public String getUserName()
	{
		return username;
	}
	
	public boolean hasBooleanConfig( String key )
	{
		return USER_BOOL_KEYS.contains( key );
	}
	
	public boolean hasListConfig( String key )
	{
		return USER_LIST_KEYS.contains( key );
	}
	
	public void setBooleanValue( String key, Boolean val)
	{
		if ( !getUserBoolKeys().contains( key ) )
		{
			logging.error("UserConfig.USER_BOOL_KEYS " + UserConfig.USER_BOOL_KEYS);
			logging.error("UserConfig : illegal key " + key);
		}
		booleanMap.put( key, val );
	}
	
	public void setValues( String key, java.util.List<Object> values)
	{
		/*
		if ( !getUserListKeys().contains( key ) )
		{
			logging.error("UserConfig.USER_LIST_KEYS " + UserConfig.USER_LIST_KEYS);
			logging.error("UserSshConfig : illegal key " + key);
		}
		*/
		valuesMap.put( key, values );
	}
	
	public void setPossibleValues( String key, java.util.List<Object> possibleValues)
	{
		/*
		if ( !getUserListKeys().contains( key ) )
		{
			logging.error("UserConfig.USER_LIST_KEYS " + UserConfig.USER_LIST_KEYS);
			logging.error("UserSshConfig : illegal key " + key);
		}
		*/
		possibleValuesMap.put( key, possibleValues );
	}
	
	
	public Boolean getBooleanValue( String key )
	{
		if ( !USER_BOOL_KEYS.contains( key ) )
		{
			logging.error("UserConfig.USER_BOOL_KEYS " + UserConfig.USER_BOOL_KEYS);
			logging.error("UserConfig : illegal key " + key);
			return false;
		}
		
		if( booleanMap.get( key ) != null )
		{
			return booleanMap.get( key );
		}
		
		if (!getArcheoConfig().hasBooleanConfig( key ))
		{
			logging.warning(this, "UserConfig : no default value for key " + key + " for user " + username);
			return false;
		}
		else
		{
			boolean val = false;
			if ( username.equals( getArcheoConfig().getUserName() ) )
			{
				logging.warning(this, "UserConfig : setting value for key " + key + " for default user " );
			}
			else
			{
				logging.warning(this, "UserConfig : setting value for key " + key + " for user " + username + " to default value "
					+ getArcheoConfig().getBooleanValue( key ) );
				val = getArcheoConfig().getBooleanValue( key );
			}
			booleanMap.put( key, val );
		}
		
		return booleanMap.get( key );
	}

	public java.util.List<Object> getValues( String key )
	{
		if (valuesMap.get( key ) == null)
			return new ArrayList<Object>();
		
		return valuesMap.get( key );
	}
	
	public java.util.List<Object> getPossibleValues( String key )
	{
		if (hasBooleanConfig (key ))
			return BOOLEAN_POSSIBLE_VALUES;
		
		if (possibleValuesMap.get( key ) == null)
			return new ArrayList<Object>();
		
		return possibleValuesMap.get( key );
	}
	
	
	public String getUser()
	{
		return username;
	}
	
	public void setUser( String uname )
	{
		username = uname;
	}
	
	public UserConfig getPrototype()
	{
		if (prototypeConfig == null)
			return ARCHEO_PROTOTYPE_CONFIG;
		return prototypeConfig;
	}
	
	public void setPrototype( UserConfig prototype )
	{
		this.prototypeConfig = prototype;
	}
	
	private static UserConfig currentConfig;

	
	public static UserConfig getCurrentUserConfig(  )
	{
		if (currentConfig == null)
			return ARCHEO_PROTOTYPE_CONFIG;
		
		return currentConfig;
	}
	
	public static void setCurrentConfig( UserConfig userConfig )
	{
		currentConfig = userConfig;
	}
	
	public static String getUserFromKey( String key )
	{
		String result = null;
		
		
		if (key.startsWith( START_USER_KEY ))
		{
			result = key.substring(START_USER_KEY.length() );
			int userNameLength = result.indexOf( "}" ); 
			
			if (userNameLength > 0)
			{
				result = result.substring(0, userNameLength);
			}
			return result;
		}
		return result;
	}
	
	public static String getPartKeyFromUserconfigKey( String key )
	{
		String partKey = null;
		
		String user = getUserFromKey( key );
		
		if (user != null)
		{
			String userStart = UserConfig.KEY_USER_ROOT + ".{" + user + ".}.";
			partKey = key.substring(userStart.length());
			
			return partKey;
		}
		
		return partKey;
	}
		
	
	@Override
	public String toString()
	{
		return getClass().getName() + ": user " + username + ":: " + booleanMap + " :: " + valuesMap;
	}
	
}


	
	
