package de.uib.opsidatamodel.permission;

import java.util.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.configed.*;

public class UserConfigModule
{
	protected String username;
	
	public LinkedHashSet<String> bool_keys;
	public LinkedHashSet<String> list_keys;
	
	
	public LinkedHashMap<String, Boolean> booleanMap;
	public LinkedHashMap<String, java.util.List<Object>> valuesMap;
	public LinkedHashMap<String, java.util.List<Object>> possibleValuesMap;
	
	protected UserConfigModule( String userName )
	{
		this( userName, null );
	}
	
	protected UserConfigModule( String userName, UserConfigModule prototype )
	{
		this.username = userName;
		
		logging.info(this, "create UserConfigModule for user named " + username + " with prototype  " + prototype );
		
		
		
		booleanMap = new LinkedHashMap<String, Boolean>();
		bool_keys = new LinkedHashSet<String>();
		valuesMap = new LinkedHashMap<String, java.util.List<Object>>();
		possibleValuesMap = new LinkedHashMap<String, java.util.List<Object>>();
		list_keys = new LinkedHashSet<String>();
		
		
		
		if (prototype != null)
		{
			booleanMap.putAll( prototype.booleanMap );
			//bool_keys = (LinkedHashSet<String>) booleanMap.keySet();
			extractKeys( prototype.booleanMap, bool_keys );
			
			valuesMap.putAll( prototype.valuesMap );
			//list_keys = (LinkedHashSet<String>) valuesMap.keySet();
			extractKeys( prototype.valuesMap, list_keys );
		}
		
		logging.info(this, "for user " + userName + " we got by prototype " + booleanMap + " -- " + valuesMap);
		logging.info(this, "for user " + userName + " bool keys " + bool_keys + " -- list keys " + list_keys);
	
		
	
	}
	
	
	@Override
	public String toString()
	{
		return getClass().getName() + ": user " + username + ":: " + booleanMap + " :: " + valuesMap;
	}
	
	public boolean withBooleanConfig( String key )
	{
		return bool_keys.contains( key );
	}
	
	public boolean withListConfig( String key )
	{
		return list_keys.contains( key );
	}
	
	
	public Boolean getBooleanValue( String key )
	{
		if ( booleanMap.get( key ) != null )
			return booleanMap.get( key );
		
		return UserConfig.getArcheoConfig().getBooleanValue( key );
	}
	
	public java.util.List<Object> getValues( String key)
	{
		if (valuesMap.get( key ) == null)
			return new ArrayList<Object>();
		
		return valuesMap.get( key );
	}
	
	public java.util.List<Object> getPossibleValues( String key)
	{
		if (possibleValuesMap.get( key ) == null)
			return new ArrayList<Object>();
		
		return valuesMap.get( key );
	}
	
	public void setBooleanValue( String key, Boolean val )
	{
		//if ( !bool_keys.contains( key ) )
		//	logging.error("UserConfigModule : illegal key " + key);
		logging.info(this, "for user " + username + " setBooleanValue " + key + " : " + val);
		booleanMap.put( key, val );
		
	}
	
	private void extractKeys( final LinkedHashMap<String, ? extends Object> map, LinkedHashSet<String> result)
	{
		for( String key : map.keySet() )
		{
			result.add( key );
		}
	}
	
	public void setValues( String key, java.util.List<Object> values )
	{
		logging.info(this, "for user " + username + ", key " + key + " setValues " + values);
		logging.info(this, "we have list_keys " + list_keys);
		logging.info(this, "we have bool_keys " + bool_keys);
		
		if ( !list_keys.contains( key ) )
			logging.info("UserOpsiPermisson : still missing key " + key);
		
		valuesMap.put( key, values );
		
		
	}
	
	
	public void setPossibleValues( String key, java.util.List<Object> possibleValues )
	{
		logging.info(this, "for user " + username + ", key " + key + " setPossibleValues " + possibleValues);
		logging.info(this, "we have list_keys " + list_keys);
		logging.info(this, "we have bool_keys " + bool_keys);
		
		if ( !list_keys.contains( key ) )
			logging.info("UserOpsiPermisson : still missing key " + key);
		
		possibleValuesMap.put( key, possibleValues );
		
		
	}
		

	
	
	
}

	
	
	
	
	
	
