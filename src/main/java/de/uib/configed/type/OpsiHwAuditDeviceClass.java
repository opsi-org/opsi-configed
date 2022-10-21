/*
	OpsiHwAuditDeviceClass.java
	
	is identified by a hwclass name
	has as members, on principle, the db columns of all properties 
	which describe a hwclass device of this hwclass instance:
	1) the properties which are global for a device
	2) the properties for which there can be many items for a device
*/
	

package de.uib.configed.type;
import java.util.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.datastructure.*;

public class OpsiHwAuditDeviceClass
{
	
	public final static String CONFIG_KEY = "configed.usecolumns_hwaudit";
	//public final static String HOST_ASSIGNED = "host_assigned";
	//public final static String HWITEM_ASSIGNED = "hwitem_assigned";
	public final static String hostAssignedTableType = "CONFIG";
	public final static String hostAssignedTableTypeLower = hostAssignedTableType.toLowerCase();
	public final static String hwItemAssignedTableType = "DEVICE";
	public final static String hwItemAssignedTableTypeLower = hwItemAssignedTableType.toLowerCase();
	
	
	public final static String CLASS_KEY = "Class";
	public final static String OPSI_KEY = "Opsi";
	public final static String WMI_KEY = "WMI"; 
	public final static String LINUX_KEY = "Linux";
	public final static String TYPE_KEY = "Type";
	public final static String UI_KEY = "UI";
	public final static String LIST_KEY = "Values";
	public final static String SCOPE_KEY = "Scope";
	
	
	public final static String firstseenColName = "firstseen";
	public final static String lastseenColName = "lastseen";
	
	protected String hwClassName;
	protected String opsiDbTableIdentifier;
	protected String ui;
	protected String wmiQuery;
	protected String linuxQuery;
	protected java.util.List< OpsiHwAuditDevicePropertyType > deviceHostProperties;
	protected java.util.List< OpsiHwAuditDevicePropertyType > deviceHwItemProperties;
	//protected java.util.Vector<String> usedHostColumns;
	//protected java.util.Vector<String> usedHwItemColumns;
	protected ConfigOption hostConfig;
	protected ConfigOption hwItemConfig;
	protected String hostConfigKey;
	protected String hwItemConfigKey;
	
	
	
	public OpsiHwAuditDeviceClass( String hwClassName )
	{
		this.hwClassName = hwClassName;
		
		deviceHostProperties = new ArrayList<OpsiHwAuditDevicePropertyType>();
		deviceHwItemProperties = new ArrayList<OpsiHwAuditDevicePropertyType>();
		//hostPossibleColumns = new java.util.List<Object>();
		//hwItemPossibleColumns = new java.util.List<Object>();
	}
	
	/*
	public boolean checkIn( String configKey,  java.util.List valuesUsed)
	//importing configs
	{
		if (!configKey.startsWith(CONFIG_KEY))
			return false;
		
		String hwdevicePartOfKey = configKey.substring( CONFIG_KEY.length() );
		
		String tableType = null;
		String hwClass = null;
		
		if ( hwdevicePartOfKey.endsWith( OpsiHwAuditDeviceClass.hostAssignedTableType ) )
		{
			tableType = hostAssignedTableType;
		}
		else if ( hwdevicePartOfKey.endsWith( hwItemAssignedTableType ) )
		{
			tableType = hwItemAssignedTableType;
		}
		
		int i = hwdevicePartOfKey.lastIndexOf("_");
		if (i > 0)
			hwClass = hwdevicePartOfKey.substring(0, i);
		
		if (tableType == null || hwClass == null)
			return false;
		
		for( OpsiHwAuditDevicePropertyType propertyType : deviceHwItemProperties )
		{
			if (valuesUsed.indexOf( propertyType.getOpsiDbColumnName() ) >= 0)
				usedHwItemColumns.add( propertyType.getOpsiDbColumnName() );
		}
		
		for( OpsiHwAuditDevicePropertyType propertyType : deviceHostProperties )
		{
			if (valuesUsed.indexOf( propertyType.getOpsiDbColumnName() ) >= 0)
				usedHostColumns.add( propertyType.getOpsiDbColumnName() );
		}
		
		return true;
				
	}
	*/
		
	
	public String getHwClassName()
	{
		return hwClassName;
	}
	
	/*
	public void setHostConfig( ConfigOption hostConfig )
	{
		this.hostConfig = hostConfig;
	}
	
	public ConfigOption getHostConfig()
	{
		return hostConfig;
	}
	*/
	
	public void setHostConfigKey( String key )
	{
		hostConfigKey = key;
	}
	
	public String getHostConfigKey( )
	{
		return hostConfigKey;
	}
	
	public void setHwItemConfigKey( String key )
	{
		hwItemConfigKey = key;
	}
	
	public String getHwItemConfigKey( )
	{
		return hwItemConfigKey;
	}
	
	public void setLinuxQuery( String s )
	{
		linuxQuery = s;
	}
	
	public String getLinuxQuery()
	{
		return linuxQuery;
	}
	
	public void setWmiQuery( String s )
	{
		wmiQuery = s;
	}
	
	public String getWmiQuery()
	{
		return wmiQuery;
	}
	
	
	public void addHostRelatedProperty( OpsiHwAuditDevicePropertyType devProperty )
	{
		deviceHostProperties.add( devProperty );
		//hostPossibleColumns.add( devProperty.getOpsiDbColumnName() );
	}
	
	public void addHwItemRelatedProperty( OpsiHwAuditDevicePropertyType deviceProperty )
	{
		deviceHwItemProperties.add( deviceProperty );
		//hwItemPossibleColumns.add( devProperty.getOpsiDbColumnName() );
	}
	
	/*
	public java.util.List<Object> getHostPossibleColumns()
	{
		return hostPossibleColumns;
	}
	
	public java.util.List<Object> getHwItemPossibleColumns()
	{
		return hwItemPossibleColumns;
	}
	*/
		
	@Override
	public String toString()
	{
		return this.getClass().getName() + " hwclass name " + hwClassName  + ", HOST:COLUMNS " + deviceHostProperties
		+ ", HW_ITEM_COLUMNS " + deviceHwItemProperties;
	}
	
	public java.util.List<OpsiHwAuditDevicePropertyType> getDeviceHostProperties()
	{
		return deviceHostProperties;
	}
	
	public java.util.List<OpsiHwAuditDevicePropertyType> getDeviceHwItemProperties()
	{
		return deviceHwItemProperties;
	}
	
	/*
	public void setUsedHostColumns(Vector<String> usedHostColumns )
	{
		this.usedHostColumns = usedHostColumns;
	}
	
	
	public Vector<String> getUsedHostColumns()
	{
		return usedHostColumns;
	}
	
	public void setUsedHwItemColumns( Vector<String> usedHwItemColumns )
	{
		this.usedHwItemColumns = usedHwItemColumns;
	}
	
	public Vector<String> getUsedHwItemColumns()
	{
		return usedHwItemColumns;
	}
	*/
}
	
	
	