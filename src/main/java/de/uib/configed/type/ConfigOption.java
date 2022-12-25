package de.uib.configed.type;

import java.util.ArrayList;
import java.util.List;
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
			put("possibleValues", new ArrayList<>());
		else
			put("possibleValues", retrieved.get("possibleValues"));

		if (retrieved == null || retrieved.get("defaultValues") == null)
			put("defaultValues", new ArrayList<>());
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

		// else

		put("classname", "List");

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
	@Override
	public List getPossibleValues() {
		return (List) get("possibleValues");
	}

	@Override
	public List getDefaultValues() {
		return (List) get("defaultValues");
	}

	@Override
	public void setDefaultValues(List values) {
		put("defaultValues", values);
	}

	@Override
	public int getSelectionMode() {
		return (Integer) get("selectionMode");
	}

	@Override
	public boolean isNullable() {
		return (!type.equals(TYPE.BoolConfig)); // until we extend the data structure
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

	// ======================
}
