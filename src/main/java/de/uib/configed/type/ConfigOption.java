package de.uib.configed.type;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.ListSelectionModel;

import de.uib.utilities.logging.logging;

public class ConfigOption extends RetrievedMap implements de.uib.utilities.table.ListCellOptions

// has a problem with type of defaultValues
{

	public static final String referenceID = "configId";

	public enum TYPE {
		BoolConfig, UnicodeConfig, UndefinedConfig
	};
	// UndefinedConfig should not occur

	public static final String BOOL_TYPE = TYPE.BoolConfig.toString();
	public static final String UNICODE_TYPE = TYPE.UnicodeConfig.toString();
	public static final String UNDEFINED_TYPE = TYPE.UndefinedConfig.toString();

	protected TYPE type;

	public ConfigOption(Map<String, Object> object) {
		super(object);
		build();
	}

	@Override
	protected void build() {
		// overwrite values
		if (retrieved == null || retrieved.get("possibleValues") == null)
			put("possibleValues", new ArrayList<Object>());
		else
			put("possibleValues", retrieved.get("possibleValues"));

		if (retrieved == null || retrieved.get("defaultValues") == null)
			put("defaultValues", new ArrayList<Object>());
		else {
			if (retrieved.get("defaultValues") instanceof org.json.JSONArray) {

				logging.info(this, "gotdefaultvalues unexpectedly " + retrieved.get("defaultValues").getClass() + " "
						+ retrieved.get("defaultValues"));
				put("defaultValues", ((org.json.JSONArray) retrieved.get("defaultValues")).toList());
			} else
				put("defaultValues", retrieved.get("defaultValues"));

			/*
			 * if (retrieved.get("defaultValues") instanceof org.json.JSONArray)
			 * {
			 * logging.error("defaultValues instanceof org.jsonArrayList");
			 * }
			 */

		}

		if (retrieved == null || retrieved.get("description") == null)
			put("description", "");
		else
			put("description", retrieved.get("description"));

		if (retrieved == null || retrieved.get("type") == null) {
			logging.debug(this, "set default UnicodeConfig");
			put("type", "UnicodeConfig");
			type = TYPE.UnicodeConfig;
		}

		else {
			if (retrieved.get("type") == null)
				put("type", UNDEFINED_TYPE);

			else
				put("type", retrieved.get("type"));

			// logging.info(this, "type found " + get("type"));
			if (get("type").equals(BOOL_TYPE) || get("type").equals("BoolProductProperty"))
				type = TYPE.BoolConfig;
			else
				type = TYPE.UnicodeConfig;
		}

		if (retrieved == null)
			put("selectionMode", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		else if (retrieved.get("multiValue") == null)
			put("selectionMode", ListSelectionModel.SINGLE_SELECTION);

		else {
			if ((Boolean) retrieved.get("multiValue"))
				put("selectionMode", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			else
				put("selectionMode", ListSelectionModel.SINGLE_SELECTION);
		}

		// if (retrieved.get("type") == null )
		// put("classname", "java.lang.String");
		// else if (retrieved.get("type").equals("BoolConfig"))
		// put("classname", "java.lang.Boolean");
		// else

		put("classname", "java.util.List");

		if (retrieved == null)
			put("editable", true);
		else if (retrieved.get("editable") == null)
			put("editable", false);
		else if ((Boolean) retrieved.get("editable"))
			put("editable", true);
		else
			put("editable", false);

		if (type != TYPE.BoolConfig)
			put("nullable", false);
	}

	// ======================
	// interface de.uib.utilities.table.ListCellOptions
	public java.util.List getPossibleValues() {
		return (java.util.List) get("possibleValues");
	}

	public java.util.List getDefaultValues() {
		return (java.util.List) get("defaultValues");
	}

	public void setDefaultValues(java.util.List values) {
		put("defaultValues", values);
	}

	public int getSelectionMode() {
		return (Integer) get("selectionMode");
	}

	public boolean isNullable() {
		return (!type.equals(TYPE.BoolConfig)); // until we extend the data structure
	}

	public boolean isEditable() {
		return (Boolean) get("editable");
	}

	public String getDescription() {
		return (String) get("description");
	}

	public TYPE getType() {
		return type;
	}

	// ======================
}
