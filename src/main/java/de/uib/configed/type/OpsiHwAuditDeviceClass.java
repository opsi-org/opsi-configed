/*
	OpsiHwAuditDeviceClass.java
	
	is identified by a hwclass name
	has as members, on principle, the db columns of all properties 
	which describe a hwclass device of this hwclass instance:
	1) the properties which are global for a device
	2) the properties for which there can be many items for a device
*/

package de.uib.configed.type;

import java.util.ArrayList;
import java.util.List;

public class OpsiHwAuditDeviceClass {

	public static final String CONFIG_KEY = "configed.usecolumns_hwaudit";
	// public static final String HOST_ASSIGNED = "host_assigned";
	// public static final String HWITEM_ASSIGNED = "hwitem_assigned";
	public static final String hostAssignedTableType = "CONFIG";
	public static final String hostAssignedTableTypeLower = hostAssignedTableType.toLowerCase();
	public static final String hwItemAssignedTableType = "DEVICE";
	public static final String hwItemAssignedTableTypeLower = hwItemAssignedTableType.toLowerCase();

	public static final String CLASS_KEY = "Class";
	public static final String OPSI_KEY = "Opsi";
	public static final String WMI_KEY = "WMI";
	public static final String LINUX_KEY = "Linux";
	public static final String TYPE_KEY = "Type";
	public static final String UI_KEY = "UI";
	public static final String LIST_KEY = "Values";
	public static final String SCOPE_KEY = "Scope";

	public static final String firstseenColName = "firstseen";
	public static final String lastseenColName = "lastseen";

	protected String hwClassName;
	protected String opsiDbTableIdentifier;
	protected String ui;
	protected String wmiQuery;
	protected String linuxQuery;
	protected List<OpsiHwAuditDevicePropertyType> deviceHostProperties;
	protected List<OpsiHwAuditDevicePropertyType> deviceHwItemProperties;
	// protected java.util.Vector<String> usedHostColumns;
	// protected java.util.Vector<String> usedHwItemColumns;
	protected ConfigOption hostConfig;
	protected ConfigOption hwItemConfig;
	protected String hostConfigKey;
	protected String hwItemConfigKey;

	public OpsiHwAuditDeviceClass(String hwClassName) {
		this.hwClassName = hwClassName;

		deviceHostProperties = new ArrayList<OpsiHwAuditDevicePropertyType>();
		deviceHwItemProperties = new ArrayList<OpsiHwAuditDevicePropertyType>();
		// hostPossibleColumns = new List<Object>();
		// hwItemPossibleColumns = new List<Object>();
	}

	/*
	 * public boolean checkIn( String configKey, List valuesUsed)
	 * //importing configs
	 * {
	 * if (!configKey.startsWith(CONFIG_KEY))
	 * return false;
	 * 
	 * String hwdevicePartOfKey = configKey.substring( CONFIG_KEY.length() );
	 * 
	 * String tableType = null;
	 * String hwClass = null;
	 * 
	 * if ( hwdevicePartOfKey.endsWith( OpsiHwAuditDeviceClass.hostAssignedTableType
	 * ) )
	 * {
	 * tableType = hostAssignedTableType;
	 * }
	 * else if ( hwdevicePartOfKey.endsWith( hwItemAssignedTableType ) )
	 * {
	 * tableType = hwItemAssignedTableType;
	 * }
	 * 
	 * int i = hwdevicePartOfKey.lastIndexOf("_");
	 * if (i > 0)
	 * hwClass = hwdevicePartOfKey.substring(0, i);
	 * 
	 * if (tableType == null || hwClass == null)
	 * return false;
	 * 
	 * for( OpsiHwAuditDevicePropertyType propertyType : deviceHwItemProperties )
	 * {
	 * if (valuesUsed.indexOf( propertyType.getOpsiDbColumnName() ) >= 0)
	 * usedHwItemColumns.add( propertyType.getOpsiDbColumnName() );
	 * }
	 * 
	 * for( OpsiHwAuditDevicePropertyType propertyType : deviceHostProperties )
	 * {
	 * if (valuesUsed.indexOf( propertyType.getOpsiDbColumnName() ) >= 0)
	 * usedHostColumns.add( propertyType.getOpsiDbColumnName() );
	 * }
	 * 
	 * return true;
	 * 
	 * }
	 */

	public String getHwClassName() {
		return hwClassName;
	}

	/*
	 * public void setHostConfig( ConfigOption hostConfig )
	 * {
	 * this.hostConfig = hostConfig;
	 * }
	 * 
	 * public ConfigOption getHostConfig()
	 * {
	 * return hostConfig;
	 * }
	 */

	public void setHostConfigKey(String key) {
		hostConfigKey = key;
	}

	public String getHostConfigKey() {
		return hostConfigKey;
	}

	public void setHwItemConfigKey(String key) {
		hwItemConfigKey = key;
	}

	public String getHwItemConfigKey() {
		return hwItemConfigKey;
	}

	public void setLinuxQuery(String s) {
		linuxQuery = s;
	}

	public String getLinuxQuery() {
		return linuxQuery;
	}

	public void setWmiQuery(String s) {
		wmiQuery = s;
	}

	public String getWmiQuery() {
		return wmiQuery;
	}

	public void addHostRelatedProperty(OpsiHwAuditDevicePropertyType devProperty) {
		deviceHostProperties.add(devProperty);
		// hostPossibleColumns.add( devProperty.getOpsiDbColumnName() );
	}

	public void addHwItemRelatedProperty(OpsiHwAuditDevicePropertyType deviceProperty) {
		deviceHwItemProperties.add(deviceProperty);
		// hwItemPossibleColumns.add( devProperty.getOpsiDbColumnName() );
	}

	/*
	 * public List<Object> getHostPossibleColumns()
	 * {
	 * return hostPossibleColumns;
	 * }
	 * 
	 * public List<Object> getHwItemPossibleColumns()
	 * {
	 * return hwItemPossibleColumns;
	 * }
	 */

	@Override
	public String toString() {
		return this.getClass().getName() + " hwclass name " + hwClassName + ", HOST:COLUMNS " + deviceHostProperties
				+ ", HW_ITEM_COLUMNS " + deviceHwItemProperties;
	}

	public List<OpsiHwAuditDevicePropertyType> getDeviceHostProperties() {
		return deviceHostProperties;
	}

	public List<OpsiHwAuditDevicePropertyType> getDeviceHwItemProperties() {
		return deviceHwItemProperties;
	}

	/*
	 * public void setUsedHostColumns(Vector<String> usedHostColumns )
	 * {
	 * this.usedHostColumns = usedHostColumns;
	 * }
	 * 
	 * 
	 * public Vector<String> getUsedHostColumns()
	 * {
	 * return usedHostColumns;
	 * }
	 * 
	 * public void setUsedHwItemColumns( Vector<String> usedHwItemColumns )
	 * {
	 * this.usedHwItemColumns = usedHwItemColumns;
	 * }
	 * 
	 * public Vector<String> getUsedHwItemColumns()
	 * {
	 * return usedHwItemColumns;
	 * }
	 */
}
