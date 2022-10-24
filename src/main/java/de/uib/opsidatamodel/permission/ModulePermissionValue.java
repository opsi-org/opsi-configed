package de.uib.opsidatamodel.permission;

import java.util.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;


public class ModulePermissionValue
{
	de.uib.opsicommand.Executioner exec;
	
	//private String moduleKey; 
	private ExtendedInteger maxClients;
	private ExtendedDate expiresDate;
	private Boolean booleanValue;
	
	public static final String keyExpires = "expires";
	public static final String keyMaxClients = "maxclients";
	
	public static final Map<String, Boolean> MODULE_CHECKED;
	//the modules which are known and should be checked
	static { 
		MODULE_CHECKED = new LinkedHashMap<String, Boolean>();
		MODULE_CHECKED.put("license_management", true);
		MODULE_CHECKED.put("local_imaging", true);
		MODULE_CHECKED.put("monitoring", true);
		MODULE_CHECKED.put("wim-capture", true);
		MODULE_CHECKED.put("scalability1", true);
		MODULE_CHECKED.put("linux_agent", true);
		MODULE_CHECKED.put("vpn", true);
		MODULE_CHECKED.put("mysql_backend", true);
		MODULE_CHECKED.put("uefi", true);
		MODULE_CHECKED.put("userroles", true);
	}
	/*
	public static final Map<String, Boolean> MODULE_PERMITTED;
	static { 
		MODULE_PERMITTED = new LinkedHashMap<String, Boolean>();
		for (String key : MODULE_CHECKED.keySet() )
		{
			MODULE_PERMITTED.put(key, true);
		}
	}
	*/
	
	
		
	private Boolean checkBoolean(Object ob)
	{
		//logging.info(this, "checkBoolean " + ob);
		Boolean result = null;
		
		if (ob instanceof Boolean)
		{
			result = (Boolean) ob;
		}
		
		else if (ob instanceof String)
		{
			String sValue = ((String) ob).trim();
			boolean checked = 
				sValue.equalsIgnoreCase("yes")
				||
				sValue.equalsIgnoreCase("true")
			;
			if (checked) 
				result = sValue.equalsIgnoreCase("yes");
		}
		
		//logging.info(this, "checkBoolean " +result);
			
		return result;
	}
	
	private ExtendedInteger retrieveMaxClients(Object ob)
	{
		ExtendedInteger result = null;
		
		if (ob == null)
			
			result = ExtendedInteger.ZERO;
		
		else
		{
			Boolean b = checkBoolean( ob);
			if (b != null)
			{
				if (b) 
					result = ExtendedInteger.INFINITE;
				else
					result = ExtendedInteger.ZERO;
			}
			else if (ob instanceof Integer)
			{
				result = new ExtendedInteger( (Integer) ob );
			}
			else if (ob instanceof String)
			{
				Integer number = null;
				try
				{
					number = Integer.valueOf( (String) ob );
				}
				catch(NumberFormatException ex)
				{
					logging.debug(this, "not a number: " + ob);
				}
				if (number != null)
					result = new ExtendedInteger(number);
			}
		
		}
		
		return result;
	}
	
	
	private ExtendedDate retrieveExpiresDate(Object ob)
	{
		ExtendedDate result = null;
		
		if (ob != null)
		{
			try
			{
				result = new ExtendedDate( (String) ob );
			}
			catch( ClassCastException ex )
			{
				logging.warning( this, "no String: " + ob);
			}
			catch ( Exception ex )
			{
				logging.debug(this,  "DateParseException for " + ob);
			}
			
		}
		
		if (result == null)
			result = ExtendedDate.ZERO;
		
		return result;
	}
	
	private Map<String, Object> interpretAsJson(Object ob)
	{
		Map<String, Object> result = exec.getMapFromItem(ob);
		
		if (result.entrySet().size() == 0)
			return null;
		
		return result;
	}	
			

	
	public ModulePermissionValue(de.uib.opsicommand.Executioner exec, Object ob, ExtendedDate defaultExpires)
	{
		this.exec = exec;
		logging.info(this, "value object given: " + ob);
		booleanValue = null;
		expiresDate = ExtendedDate.ZERO;
		maxClients = ExtendedInteger.ZERO;
		if (ob != null) 
		{
			Map<String, Object> detailled = interpretAsJson(ob);
			logging.debug(this, "detailled "  + detailled);
			if (detailled != null)
			{
				maxClients = retrieveMaxClients( detailled.get( keyMaxClients ) );
				logging.debug(this, "detailled  maxClients "  + maxClients );
				expiresDate = retrieveExpiresDate( detailled.get( keyExpires ) ); 
			}
			else
			{
				booleanValue = checkBoolean( ob );
			 
				if (booleanValue == null)
				{
					expiresDate = retrieveExpiresDate( ob );
					maxClients = retrieveMaxClients( ob );
					logging.debug(this, "maxClients directly given "  + maxClients);
				}
				
				else if (booleanValue)
				{
					maxClients = ExtendedInteger.INFINITE;
				}
				else
				{
					maxClients = ExtendedInteger.ZERO;
				}
			}
		}
		
		if (expiresDate == ExtendedDate.ZERO)
			expiresDate = defaultExpires;
		
	}
		
	
	public ExtendedInteger getMaxClients()
	{
		return maxClients;
	}
	
	public ExtendedDate getExpires()
	{
		return expiresDate;
	}
	
	public Boolean getBoolean()
	{
		return booleanValue;
	}
	
	
	@Override 
	public String toString()
	{
		return "  :" + getBoolean() + " maxClients: "  + getMaxClients() + ";  expires  " + getExpires(); 
	}
	
}

	
	
	
	
	
	
