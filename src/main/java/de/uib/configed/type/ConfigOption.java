/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListSelectionModel;

import de.uib.utils.logging.Logging;

// has a problem with type of defaultValues
public class ConfigOption extends RetrievedMap {
	public static final String REFERENCE_ID = "configId";

	public enum TYPE {
		BOOL_CONFIG("BoolConfig"), UNICODE_CONFIG("UnicodeConfig"), UNDEFINED_CONFIG("UndefinedConfig");

		private final String displayName;

		TYPE(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private TYPE type;

	public ConfigOption(Map<String, Object> object) {
		super(object);
		buildConfigOption();
	}

	public ConfigOption() {
		this(null);
	}

	public static ConfigOption createConfigOption(String description, TYPE type, boolean editable, boolean multiValue,
			List<?> defaultValues, List<?> possibleValues) {
		Map<String, Object> retrieved = new HashMap<>();
		retrieved.put("possibleValues", possibleValues);
		retrieved.put("defaultValues", defaultValues);
		retrieved.put("description", description);
		retrieved.put("type", type.toString());
		retrieved.put("editable", editable);
		retrieved.put("multiValue", multiValue);

		return new ConfigOption(retrieved);
	}

	public static ConfigOption createConfigOption(String description, TYPE type, boolean editable, boolean multiValue) {
		List<Object> possibleValues = new ArrayList<>();
		List<Object> defaultValues = new ArrayList<>();

		if (type == TYPE.BOOL_CONFIG) {
			possibleValues.add(true);
			possibleValues.add(false);

			editable = false;
		}

		return createConfigOption(description, type, editable, multiValue, defaultValues, possibleValues);
	}

	private void buildConfigOption() {
		// overwrite values
		if (retrieved == null || retrieved.get("possibleValues") == null) {
			put("possibleValues", new ArrayList<>());
		} else {
			put("possibleValues", retrieved.get("possibleValues"));
		}

		if (retrieved == null || retrieved.get("defaultValues") == null) {
			put("defaultValues", new ArrayList<>());
		} else {
			put("defaultValues", retrieved.get("defaultValues"));
		}

		if (retrieved == null || retrieved.get("description") == null) {
			put("description", "");
		} else {
			put("description", retrieved.get("description"));
		}

		buildType();

		buildSelectionMode();

		if (retrieved == null) {
			put("editable", true);
		} else if (retrieved.get("editable") == null) {
			put("editable", false);
		} else {
			put("editable", retrieved.get("editable"));
		}
	}

	private void buildType() {
		if (retrieved == null || retrieved.get("type") == null) {
			Logging.debug(this, "set default UnicodeConfig");
			put("type", "UnicodeConfig");
			type = TYPE.UNICODE_CONFIG;
		} else {
			if (retrieved.get("type") == null) {
				put("type", TYPE.UNDEFINED_CONFIG.toString());
			} else {
				put("type", retrieved.get("type"));
			}

			if (get("type").equals(TYPE.BOOL_CONFIG.toString()) || "BoolProductProperty".equals(get("type"))) {
				type = TYPE.BOOL_CONFIG;
			} else {
				type = TYPE.UNICODE_CONFIG;
			}
		}
	}

	private void buildSelectionMode() {
		if (retrieved == null) {
			put("selectionMode", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if (retrieved.get("multiValue") == null) {
			put("selectionMode", ListSelectionModel.SINGLE_SELECTION);
		} else if (Boolean.TRUE.equals(retrieved.get("multiValue"))) {
			put("selectionMode", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			put("selectionMode", ListSelectionModel.SINGLE_SELECTION);
		}
	}

	public List<Object> getPossibleValues() {
		return (List<Object>) get("possibleValues");
	}

	public List<Object> getDefaultValues() {
		return (List<Object>) get("defaultValues");
	}

	public void setDefaultValues(List<Object> values) {
		put("defaultValues", values);
	}

	public int getSelectionMode() {
		return (Integer) get("selectionMode");
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
}
