package de.uib.configed.type;

import java.util.List;

//currently only the CONFIG_KEY is used

public abstract class MetaConfig {
	// we have eight class members to define a property (a config) by a meta-config:
	// giving four meta configs

	protected String metakeynameForPropertyName;
	protected String metakeynameForPropertyValue;
	protected String metakeynameForPropertyDefaultValues;
	protected String metakeynameForPropertyType;
	protected String metakeynameForPropertyEditable;
	protected String metakeynameForPropertyMultivalue;
	protected String metakeynameForDescription;

	protected String propertyName;
	protected Object propertyValue;
	protected String description;

	protected ConfigOption.TYPE propertyType;

	public class InvalidTypeException extends Exception {}

	public static final String CONFIG_KEY = "configed.meta_config";
	public static final String PROPERTY_KEY_PART = "propertyname";
	public static final String VALUE_KEY_PART = "defaultvalue";
	public static final String POSSIBLE_VALUES_KEY_PART = "possiblevalues";
	public static final String EDITABLE_KEY_PART = "editable";
	public static final String MULTIVALUE_KEY_PART = "multivalue";
	public static final String TYPE_KEY_PART = "type";

	protected String baseEntryKeyName;

	String baseMetaKeyName;
	String propertynameMetaKeyName;
	String isEditableMetaKeyName;
	String isMultiValueMetaKeyName;
	String valueMetaKeyName;
	String possibleValuesMetaKeyName;
	String typeMetaKeyName;

	public MetaConfig(String keyName, String propertyName, String description, boolean editable, boolean multiValue,
			List<Object> values, List<Object> possibleValues, ConfigOption.TYPE type)

	{
		baseMetaKeyName = CONFIG_KEY + ". " + keyName;
		propertynameMetaKeyName = baseEntryKeyName + "." + PROPERTY_KEY_PART;
		isEditableMetaKeyName = baseEntryKeyName + "." + EDITABLE_KEY_PART;
		isMultiValueMetaKeyName = baseEntryKeyName + "." + MULTIVALUE_KEY_PART;
		valueMetaKeyName = baseEntryKeyName + "." + VALUE_KEY_PART;
		possibleValuesMetaKeyName = baseEntryKeyName + "." + POSSIBLE_VALUES_KEY_PART;
		typeMetaKeyName = baseEntryKeyName + "." + TYPE_KEY_PART;
		propertyType = type;
	}

}
