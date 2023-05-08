/*
	OpsiHwAuditDeviceClass.java
	
	private is identified by a hwclass name
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

	public static final String HOST_ASSIGNED_TABLE_TYPE = "CONFIG";
	public static final String HOST_ASSIGNED_TABLE_TYPE_LOWER = HOST_ASSIGNED_TABLE_TYPE.toLowerCase();
	public static final String HW_ITEM_ASSIGNED_TABLE_TYPE = "DEVICE";
	public static final String HW_ITEM_ASSIGNED_TABLE_TYPE_LOWER = HW_ITEM_ASSIGNED_TABLE_TYPE.toLowerCase();

	public static final String CLASS_KEY = "Class";
	public static final String OPSI_KEY = "Opsi";
	public static final String WMI_KEY = "WMI";
	public static final String LINUX_KEY = "Linux";
	public static final String TYPE_KEY = "Type";
	public static final String UI_KEY = "UI";
	public static final String LIST_KEY = "Values";
	public static final String SCOPE_KEY = "Scope";

	public static final String FIRST_SEEN_COL_NAME = "firstseen";
	public static final String LAST_SEEN_COL_NAME = "lastseen";

	protected String hwClassName;
	protected String opsiDbTableIdentifier;
	protected String ui;
	protected String wmiQuery;
	protected String linuxQuery;
	protected List<OpsiHwAuditDevicePropertyType> deviceHostProperties;
	protected List<OpsiHwAuditDevicePropertyType> deviceHwItemProperties;

	protected ConfigOption hostConfig;
	protected ConfigOption hwItemConfig;
	protected String hostConfigKey;
	protected String hwItemConfigKey;

	public OpsiHwAuditDeviceClass(String hwClassName) {
		this.hwClassName = hwClassName;

		deviceHostProperties = new ArrayList<>();
		deviceHwItemProperties = new ArrayList<>();

	}

	public String getHwClassName() {
		return hwClassName;
	}

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

	}

	public void addHwItemRelatedProperty(OpsiHwAuditDevicePropertyType deviceProperty) {
		deviceHwItemProperties.add(deviceProperty);

	}

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

}
