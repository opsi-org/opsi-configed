/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.permission;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.utilities.logging.Logging;

public class UserConfigModule {
	private String username;

	private Set<String> boolKeys;
	private Set<String> listKeys;

	private Map<String, Boolean> booleanMap;
	private Map<String, List<Object>> valuesMap;
	private Map<String, List<Object>> possibleValuesMap;

	protected UserConfigModule(String userName) {
		this.username = userName;

		Logging.info(this, "create UserConfigModule for user named " + username);

		booleanMap = new LinkedHashMap<>();
		boolKeys = new LinkedHashSet<>();
		valuesMap = new LinkedHashMap<>();
		possibleValuesMap = new LinkedHashMap<>();
		listKeys = new LinkedHashSet<>();

		Logging.info(this, "for user " + userName + " we got by prototype " + booleanMap + " -- " + valuesMap);
		Logging.info(this, "for user " + userName + " bool keys " + boolKeys + " -- list keys " + listKeys);

	}

	@Override
	public String toString() {
		return getClass().getName() + ": user " + username + ":: " + booleanMap + " :: " + valuesMap;
	}

	public void setBooleanValue(String key, Boolean val) {
		Logging.info(this, "for user " + username + " setBooleanValue " + key + " : " + val);
		booleanMap.put(key, val);
	}

	public Map<String, Boolean> getBooleanMap() {
		return booleanMap;
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
