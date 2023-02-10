package de.uib.configed.type;

import java.util.HashMap;

public class ConfigStateEntry extends HashMap<String, Object> {
	public static final String TYPE = "ConfigState";

	public static final String DB_TABLE_NAME = "CONFIG_STATE";

	public static final String ID = "config_state_id";
	public static final String OBJECT_ID = "objectId";
	public static final String CONFIG_ID = "configId";
	public static final String VALUES_ID = "values";
}
