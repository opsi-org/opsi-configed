/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ListSelectionModel;

import de.uib.utils.logging.Logging;
import de.uib.utils.table.ListCellOptions;

// has a problem with type of defaultValues
public class ConfigOption extends RetrievedMap implements ListCellOptions {
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

	// UndefinedConfig should not occur

	public static final String BOOL_TYPE = TYPE.BOOL_CONFIG.toString();
	public static final String UNICODE_TYPE = TYPE.UNICODE_CONFIG.toString();
	public static final String UNDEFINED_TYPE = TYPE.UNDEFINED_CONFIG.toString();

	private TYPE type;

	public ConfigOption(Map<String, Object> object) {
		super(object);
		build();
	}

	private ConfigOption() {
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

		if (type != TYPE.BOOL_CONFIG) {
			put("nullable", false);
		}
	}

	private void buildType() {
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
		return type != TYPE.BOOL_CONFIG;
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

	@Override
	public ListCellOptions deepCopy() {
		ConfigOption configOption = new ConfigOption();
		configOption.put("type", type);
		configOption.put("description", get("description"));
		configOption.put("possibleValues", get("possibleValues"));
		configOption.put("defaultValues", get("defaultValues"));
		configOption.put("editable", get("editable"));
		configOption.put("selectionMode", get("selectionMode"));
		configOption.put("nullable", get("nullable"));
		return configOption;
	}
}
