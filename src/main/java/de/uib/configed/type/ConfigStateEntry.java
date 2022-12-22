package de.uib.configed.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ConfigStateEntry extends HashMap<String, Object> {
	public static final String TYPE = "ConfigState";

	public static final String DB_TABLE_NAME = "CONFIG_STATE";

	public static final String ID = "config_state_id";
	public static final String OBJECT_ID = "objectId";
	public static final String CONFIG_ID = "configId";
	public static final String VALUES = "values";

	public static final LinkedHashMap<String, String> DB_COLUMNS = new LinkedHashMap<String, String>();
	static {
		DB_COLUMNS.put(ID, DB_TABLE_NAME + "." + ID);
		DB_COLUMNS.put(CONFIG_ID, DB_TABLE_NAME + "." + CONFIG_ID);
		DB_COLUMNS.put(OBJECT_ID, DB_TABLE_NAME + "." + OBJECT_ID);
		DB_COLUMNS.put(VALUES, DB_TABLE_NAME + "." + "VALUES");
		// DB_COLUMNS.put(LAST_MODIFICATION, DB_TABLE_NAME + "." + "lastseen");

	}

	public static final List<String> DB_COLUMN_NAMES = new ArrayList<>();
	static {
		for (String key : DB_COLUMNS.keySet()) {
			DB_COLUMN_NAMES.add(DB_COLUMNS.get(key));
		}
	}

	// public static final int columnIndexLastStateChange =
	// DB_COLUMN_NAMES.indexOf("modificationTime");

	public ConfigStateEntry(String configId, String hostId, List values) {
		super();
		put(CONFIG_ID, configId);
		put(OBJECT_ID, hostId);
		put(VALUES, values);
	}

}
