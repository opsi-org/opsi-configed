package de.uib.opsidatamodel.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.utilities.logging.Logging;

public class UserConfigModule {
	protected String username;

	public LinkedHashSet<String> bool_keys;
	public LinkedHashSet<String> list_keys;

	public Map<String, Boolean> booleanMap;
	public Map<String, List<Object>> valuesMap;
	public Map<String, List<Object>> possibleValuesMap;

	protected UserConfigModule(String userName) {
		this(userName, null);
	}

	protected UserConfigModule(String userName, UserConfigModule prototype) {
		this.username = userName;

		Logging.info(this, "create UserConfigModule for user named " + username + " with prototype  " + prototype);

		booleanMap = new LinkedHashMap<>();
		bool_keys = new LinkedHashSet<>();
		valuesMap = new LinkedHashMap<>();
		possibleValuesMap = new LinkedHashMap<>();
		list_keys = new LinkedHashSet<>();

		if (prototype != null) {
			booleanMap.putAll(prototype.booleanMap);

			extractKeys(prototype.booleanMap, bool_keys);

			valuesMap.putAll(prototype.valuesMap);

			extractKeys(prototype.valuesMap, list_keys);
		}

		Logging.info(this, "for user " + userName + " we got by prototype " + booleanMap + " -- " + valuesMap);
		Logging.info(this, "for user " + userName + " bool keys " + bool_keys + " -- list keys " + list_keys);

	}

	@Override
	public String toString() {
		return getClass().getName() + ": user " + username + ":: " + booleanMap + " :: " + valuesMap;
	}

	public boolean withBooleanConfig(String key) {
		return bool_keys.contains(key);
	}

	public boolean withListConfig(String key) {
		return list_keys.contains(key);
	}

	public Boolean getBooleanValue(String key) {
		if (booleanMap.get(key) != null)
			return booleanMap.get(key);

		return UserConfig.getArcheoConfig().getBooleanValue(key);
	}

	public List<Object> getValues(String key) {
		if (valuesMap.get(key) == null)
			return new ArrayList<>();

		return valuesMap.get(key);
	}

	public List<Object> getPossibleValues(String key) {
		if (possibleValuesMap.get(key) == null)
			return new ArrayList<>();

		return valuesMap.get(key);
	}

	public void setBooleanValue(String key, Boolean val) {

		Logging.info(this, "for user " + username + " setBooleanValue " + key + " : " + val);
		booleanMap.put(key, val);

	}

	private void extractKeys(final Map<String, ? extends Object> map, Set<String> result) {
		for (String key : map.keySet()) {
			result.add(key);
		}
	}

	public void setValues(String key, List<Object> values) {
		Logging.info(this, "for user " + username + ", key " + key + " setValues " + values);
		Logging.info(this, "we have list_keys " + list_keys);
		Logging.info(this, "we have bool_keys " + bool_keys);

		if (!list_keys.contains(key))
			Logging.info("UserOpsiPermisson : still missing key " + key);

		valuesMap.put(key, values);

	}

	public void setPossibleValues(String key, List<Object> possibleValues) {
		Logging.info(this, "for user " + username + ", key " + key + " setPossibleValues " + possibleValues);
		Logging.info(this, "we have list_keys " + list_keys);
		Logging.info(this, "we have bool_keys " + bool_keys);

		if (!list_keys.contains(key))
			Logging.info("UserOpsiPermisson : still missing key " + key);

		possibleValuesMap.put(key, possibleValues);

	}

}
