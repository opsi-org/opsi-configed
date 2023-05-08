package de.uib.configed.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ListSelectionModel;

import org.json.JSONArray;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ListCellOptions;

// has a problem with type of defaultValues
public class ConfigOption extends RetrievedMap implements ListCellOptions {
	public static final String REFERENCE_ID = "configId";

	public enum TYPE {
		BOOL_CONFIG {
			@Override
			public String toString() {
				return "BoolConfig";
			}
		},
		UNICODE_CONFIG {
			@Override
			public String toString() {
				return "UnicodeConfig";
			}
		},
		UNDEFINED_CONFIG {
			@Override
			public String toString() {
				return "UndefinedConfig";
			}
		}
	}

	// UndefinedConfig should not occur

	public static final String BOOL_TYPE = TYPE.BOOL_CONFIG.toString();
	public static final String UNICODE_TYPE = TYPE.UNICODE_CONFIG.toString();
	public static final String UNDEFINED_TYPE = TYPE.UNDEFINED_CONFIG.toString();

	private TYPE type;

	public ConfigOption(Map<String, Object> object) {
		super(object);
		build();
	}

	@Override
	protected void build() {
		// overwrite values
		if (retrieved == null || retrieved.get("possibleValues") == null) {
			put("possibleValues", new ArrayList<>());
		} else {
			put("possibleValues", retrieved.get("possibleValues"));
		}

		if (retrieved == null || retrieved.get("defaultValues") == null) {
			put("defaultValues", new ArrayList<>());
		} else {
			if (retrieved.get("defaultValues") instanceof JSONArray) {

				Logging.info(this, "gotdefaultvalues unexpectedly " + retrieved.get("defaultValues").getClass() + " "
						+ retrieved.get("defaultValues"));
				put("defaultValues", ((JSONArray) retrieved.get("defaultValues")).toList());
			} else {
				put("defaultValues", retrieved.get("defaultValues"));
			}

		}

		if (retrieved == null || retrieved.get("description") == null) {
			put("description", "");
		} else {
			put("description", retrieved.get("description"));
		}

		if (retrieved == null || retrieved.get("type") == null) {
			Logging.debug(this, "set default UnicodeConfig");
			put("type", "UnicodeConfig");
			type = TYPE.UNICODE_CONFIG;
		} else {
			if (retrieved.get("type") == null) {
				put("type", UNDEFINED_TYPE);
			} else {
				put("type", retrieved.get("type"));
			}

			if (get("type").equals(BOOL_TYPE) || "BoolProductProperty".equals(get("type"))) {
				type = TYPE.BOOL_CONFIG;
			} else {
				type = TYPE.UNICODE_CONFIG;
			}
		}

		if (retrieved == null) {
			put("selectionMode", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if (retrieved.get("multiValue") == null) {
			put("selectionMode", ListSelectionModel.SINGLE_SELECTION);
		} else {
			if (Boolean.TRUE.equals(retrieved.get("multiValue"))) {
				put("selectionMode", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			} else {
				put("selectionMode", ListSelectionModel.SINGLE_SELECTION);
			}
		}

		put("classname", "List");

		if (retrieved == null) {
			put("editable", true);
		} else if (retrieved.get("editable") == null) {
			put("editable", false);
		} else {
			put("editable", retrieved.get("editable"));
		}

		if (type != TYPE.BOOL_CONFIG) {
			put("nullable", false);
		}
	}

	// interface ListCellOptions
	@Override
	public List<Object> getPossibleValues() {
		return (List<Object>) get("possibleValues");
	}

	@Override
	public List<Object> getDefaultValues() {
		return (List<Object>) get("defaultValues");
	}

	@Override
	public void setDefaultValues(List<Object> values) {
		put("defaultValues", values);
	}

	@Override
	public int getSelectionMode() {
		return (Integer) get("selectionMode");
	}

	@Override
	public boolean isNullable() {
		// until we extend the data structure
		return (type != TYPE.BOOL_CONFIG);
	}

	@Override
	public boolean isEditable() {
		return (Boolean) get("editable");
	}

	@Override
	public String getDescription() {
		return (String) get("description");
	}

	public TYPE getType() {
		return type;
	}
}
