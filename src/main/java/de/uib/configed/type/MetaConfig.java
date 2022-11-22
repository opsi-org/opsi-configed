package de.uib.configed.type;

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

	public class InvalidTypeException extends Exception {
	}

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

	public MetaConfig(
			String keyName, String propertyName, String description,
			boolean editable, boolean multiValue,
			java.util.List<Object> values,
			java.util.List<Object> possibleValues,
			ConfigOption.TYPE type)

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

	/*
	 * 
	 * public ArrayList<Object> addObjectsForService( ArrayList<Object> readyObjects
	 * )
	 * {
	 * if (readyObjects == null)
	 * readyObjects = new ArrayList<Object>();
	 * 
	 * java.util.List<Object> defaultValues = new ArrayList<Object> ();
	 * defaultValues.add(propertyName);
	 * java.util.List<Object> propertyPossibleValues = new ArrayList<Object>();
	 * propertyPossibleValues.add(propertyName);
	 * 
	 * 
	 * Map<String, Object> itemPropertyName
	 * = createConfig(
	 * ConfigOption.TYPE.UnicodeConfig,
	 * "metaconfig for " + propertynameMetaKeyName,
	 * true,
	 * false,
	 * defaultValues,
	 * propertyPossibleValues
	 * );
	 * 
	 * readyObjects.add( exec.jsonMap(itemPropertyName) );
	 * 
	 * 
	 * defaultValues = new ArrayList<Object> ();
	 * defaultValues.add(propertyType.toString());
	 * propertyPossibleValues = new ArrayList<Object>();
	 * propertyPossibleValues.add(propertyType.toString());
	 * 
	 * Map<String, Object> itemPropertyType
	 * = createConfig(
	 * ConfigOption.TYPE.UnicodeConfig,
	 * "metaconfig for " + typeMetaKeyName,
	 * false,
	 * false,
	 * defaultValues,
	 * propertyPossibleValues
	 * );
	 * 
	 * 
	 * 
	 * readyObjects.add( exec.jsonMap(itemPropertyType) );
	 * 
	 * 
	 * Map<String, Object> itemValues
	 * = createConfig(
	 * ConfigOption.TYPE.UnicodeConfig,
	 * "metaconfig for " + valueMetaKeyName,
	 * true,
	 * true,
	 * values,
	 * values
	 * );
	 * 
	 * readyObjects.add( exec.jsonMap(itemValues) );
	 * 
	 * 
	 * Map<String, Object> itemPossibleValues
	 * = createConfig(
	 * ConfigOption.TYPE.UnicodeConfig,
	 * "metaconfig for " + possibleValuesMetaKeyName,
	 * true,
	 * true,
	 * possibleValues,
	 * possibleValues
	 * );
	 * 
	 * readyObjects.add( exec.jsonMap(itemPossibleValues) );
	 * 
	 * 
	 * Map<String, Object> itemEditable
	 * = createBoolConfig(
	 * isEditableMetaKeyName,
	 * editable,
	 * "metaconfig for " + isEditableMetaKeyName
	 * );
	 * 
	 * 
	 * readyObjects.add( exec.jsonMap(itemEditable) );
	 * 
	 * 
	 * Map<String, Object> itemMultiValues
	 * = createBoolConfig(
	 * isMultiValueMetaKeyName,
	 * multiValue,
	 * "metaconfig for " + isMultiValueMetaKeyName
	 * );
	 * 
	 * 
	 * readyObjects.add( exec.jsonMap(itemMultiValues) );
	 * }
	 */

}
