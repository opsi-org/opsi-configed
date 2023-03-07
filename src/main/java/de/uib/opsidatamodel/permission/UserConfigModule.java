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

	private Set<String> boolKeys;
	private Set<String> listKeys;

	private Map<String, Boolean> booleanMap;
	private Map<String, List<Object>> valuesMap;
	private Map<String, List<Object>> possibleValuesMap;

	protected UserConfigModule(String userName) {
		this(userName, null);
	}

	protected UserConfigModule(String userName, UserConfigModule prototype) {
		this.username = userName;

		Logging.info(this, "create UserConfigModule for user named " + username + " with prototype  " + prototype);

		booleanMap = new LinkedHashMap<>();
		boolKeys = new LinkedHashSet<>();
		valuesMap = new LinkedHashMap<>();
		possibleValuesMap = new LinkedHashMap<>();
		listKeys = new LinkedHashSet<>();

		if (prototype != null) {
			booleanMap.putAll(prototype.booleanMap);

			extractKeys(prototype.booleanMap, boolKeys);

			valuesMap.putAll(prototype.valuesMap);

			extractKeys(prototype.valuesMap, listKeys);
		}

		Logging.info(this, "for user " + userName + " we got by prototype " + booleanMap + " -- " + valuesMap);
		Logging.info(this, "for user " + userName + " bool keys " + boolKeys + " -- list keys " + listKeys);

	}

	@Override
	public String toString() {
		return getClass().getName() + ": user " + username + ":: " + booleanMap + " :: " + valuesMap;
	}

	public boolean withBooleanConfig(String key) {
		return boolKeys.contains(key);
	}

	public boolean withListConfig(String key) {
		return listKeys.contains(key);
	}

	public Boolean getBooleanValue(String key) {
		if (booleanMap.get(key) != null) {
			return booleanMap.get(key);
		}

		return UserConfig.getArcheoConfig().getBooleanValue(key);
	}

	public List<Object> getValues(String key) {
		if (valuesMap.get(key) == null) {
			return new ArrayList<>();
		}

		return valuesMap.get(key);
	}

	public List<Object> getPossibleValues(String key) {
		if (possibleValuesMap.get(key) == null) {
			return new ArrayList<>();
		}

		return valuesMap.get(key);
	}

	public void setBooleanValue(String key, Boolean val) {
		Logging.info(this, "for user " + username + " setBooleanValue " + key + " : " + val);
		booleanMap.put(key, val);
	}

	public Map<String, Boolean> getBooleanMap() {
		return booleanMap;
	}

	private static void extractKeys(final Map<String, ? extends Object> map, Set<String> result) {
		for (String key : map.keySet()) {
			result.add(key);
		}
	}

	public void setValues(String key, List<Object> values) {
		Logging.info(this, "for user " + username + ", key " + key + " setValues " + values);
		Logging.info(this, "we have list_keys " + listKeys);
		Logging.info(this, "we have bool_keys " + boolKeys);

		if (!listKeys.contains(key)) {
			Logging.info("UserOpsiPermisson : still missing key " + key);
		}

		valuesMap.put(key, values);
	}

	public Map<String, List<Object>> getValuesMap() {
		return valuesMap;
	}

	public void setPossibleValues(String key, List<Object> possibleValues) {
		Logging.info(this, "for user " + username + ", key " + key + " setPossibleValues " + possibleValues);
		Logging.info(this, "we have list_keys " + listKeys);
		Logging.info(this, "we have bool_keys " + boolKeys);

		if (!listKeys.contains(key)) {
			Logging.info("UserOpsiPermisson : still missing key " + key);
		}

		possibleValuesMap.put(key, possibleValues);
	}

	public Map<String, List<Object>> getPossibleValuesMap() {
		return possibleValuesMap;
	}

}
