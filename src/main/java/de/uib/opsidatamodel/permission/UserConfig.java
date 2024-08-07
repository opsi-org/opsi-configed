/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.utils.logging.Logging;

/*

	concept originally designed eg. as follows 

		cf.
		de.uib.configed.type.user.UserXRole.getRole()

	private if GLOBAL_READONLY (can be specified by user group or in config)
		everything readonly (ignoring a config  SERVER_READWRITE)

		if DEPOTACCESS_ONLY_AS_SPECIFIED: (default general access!)
			access only to specified depots

	else
		if SERVER_READWRITE false or not set:
			write access to server

		if DEPOTACCESS_AS_SPECIFIED (default general access!)
			access only to specified depots
			(of course, the user could change this in case of server_readwrite)
	*/

public class UserConfig {
	public static final String CONFIGKEY_STR_USER = "user";

	public static final String ROLE = "role";

	public static final String KEY_USER_ROOT = CONFIGKEY_STR_USER;
	public static final String KEY_USER_ROLE_ROOT = KEY_USER_ROOT + "." + ROLE;
	public static final String ALL_USER_KEY_START = KEY_USER_ROOT + ".{}.";

	public static final String START_USER_KEY = UserConfig.KEY_USER_ROOT + ".{";

	public static final String DEFAULT_ROLE_NAME = "default";
	public static final String ARCHEO_ROLE_NAME = "archeo";
	public static final String NONE_PROTOTYPE = "";

	public static final String HAS_ROLE_ATTRIBUT = "has_role";
	public static final String MODIFICATION_INFO_KEY = "modified";

	private static final List<Object> EMPTY_LIST = new ArrayList<>();
	private static final List<Object> BOOLEAN_POSSIBLE_VALUES = new ArrayList<>();
	static {
		BOOLEAN_POSSIBLE_VALUES.add(true);
		BOOLEAN_POSSIBLE_VALUES.add(false);
	}

	private static final List<Object> ZERO_TIME;
	static {
		ZERO_TIME = new ArrayList<>();
		ZERO_TIME.add("0000-00-00 00:00:00");
	}

	private static Set<String> userBoolKeys;

	private static Set<String> userListKeys;

	private static Set<String> userStringValueKeys;

	private static Set<String> userStringValueKeysWithoutRole;

	// default UserConfig Objects
	private static UserConfig archeoPrototypeConfig;

	private static UserConfig currentConfig;

	private String username;

	private Map<String, Boolean> booleanMap;
	private Map<String, List<Object>> valuesMap;
	private Map<String, List<Object>> possibleValuesMap;

	public UserConfig(String userName) {
		Logging.info(this.getClass(), "create for " + userName);
		this.username = userName;
		booleanMap = new LinkedHashMap<>();
		valuesMap = new LinkedHashMap<>();
		possibleValuesMap = new LinkedHashMap<>();
	}

	public static Set<String> getUserBoolKeys() {
		if (userBoolKeys == null) {
			userBoolKeys = new LinkedHashSet<>();

			Logging.info("addAll bool keys");
			userBoolKeys.addAll(UserServerConsoleConfig.BOOL_KEYS);
			Logging.info("addAll opsipermission bool keys");
			userBoolKeys.addAll(UserOpsipermission.BOOL_KEYS);
		}

		return userBoolKeys;
	}

	public static Set<String> getUserStringValueKeys() {
		if (userStringValueKeys == null) {
			userStringValueKeys = new LinkedHashSet<>(getUserStringValueKeysWithoutRole());
			userStringValueKeys.add(HAS_ROLE_ATTRIBUT);
		}
		return userStringValueKeys;
	}

	public static Set<String> getUserStringValueKeysWithoutRole() {
		if (userStringValueKeysWithoutRole == null) {
			userStringValueKeysWithoutRole = new LinkedHashSet<>();
			userStringValueKeysWithoutRole.add(MODIFICATION_INFO_KEY);
		}

		return userStringValueKeysWithoutRole;
	}

	public static Set<String> getUserListKeys() {
		if (userListKeys == null) {
			userListKeys = new LinkedHashSet<>();

			userListKeys.addAll(UserOpsipermission.LIST_KEYS);
			userListKeys.addAll(UserServerConsoleConfig.LIST_KEYS);
		}

		return userListKeys;
	}

	public static final UserConfig getArcheoConfig() {
		Logging.info("getArcheoConfig");
		if (archeoPrototypeConfig == null) {
			archeoPrototypeConfig = new UserConfig(ARCHEO_ROLE_NAME);
			archeoPrototypeConfig.setValues(HAS_ROLE_ATTRIBUT, EMPTY_LIST);
		}

		getUserBoolKeys();
		getUserListKeys();

		archeoPrototypeConfig.booleanMap.putAll(UserOpsipermission.DEFAULT.getBooleanMap());
		archeoPrototypeConfig.booleanMap.putAll(UserServerConsoleConfig.DEFAULT.getBooleanMap());

		archeoPrototypeConfig.valuesMap.putAll(UserOpsipermission.DEFAULT.getValuesMap());
		archeoPrototypeConfig.valuesMap.putAll(UserServerConsoleConfig.DEFAULT.getValuesMap());
		archeoPrototypeConfig.possibleValuesMap.putAll(UserOpsipermission.DEFAULT.getPossibleValuesMap());
		archeoPrototypeConfig.possibleValuesMap.putAll(UserServerConsoleConfig.DEFAULT.getPossibleValuesMap());

		archeoPrototypeConfig.setValues(MODIFICATION_INFO_KEY, ZERO_TIME);

		return archeoPrototypeConfig;
	}

	public String getUserName() {
		return username;
	}

	public boolean hasBooleanConfig(String key) {
		return userBoolKeys.contains(key);
	}

	public void setBooleanValue(String key, Boolean val) {
		if (!getUserBoolKeys().contains(key)) {
			Logging.error("UserConfig.USER_BOOL_KEYS " + UserConfig.userBoolKeys);
			Logging.error("UserConfig : illegal key " + key);
		}
		booleanMap.put(key, val);
	}

	public void setValues(String key, List<Object> values) {
		valuesMap.put(key, values);
	}

	public void setPossibleValues(String key, List<Object> possibleValues) {
		possibleValuesMap.put(key, possibleValues);
	}

	public Boolean getBooleanValue(String key) {
		if (!userBoolKeys.contains(key)) {
			Logging.error("UserConfig.USER_BOOL_KEYS " + UserConfig.userBoolKeys);
			Logging.error("UserConfig : illegal key " + key);
			return false;
		}

		if (booleanMap.get(key) == null) {
			if (!getArcheoConfig().hasBooleanConfig(key)) {
				Logging.warning(this, "UserConfig : no default value for key " + key + " for user " + username);
				return false;
			} else {
				boolean val = false;
				if (username.equals(getArcheoConfig().getUserName())) {
					Logging.warning(this, "UserConfig : setting value for key " + key + " for default user ");
				} else {
					Logging.warning(this, "UserConfig : setting value for key " + key + " for user " + username
							+ " to default value " + getArcheoConfig().getBooleanValue(key));
					val = getArcheoConfig().getBooleanValue(key);
				}
				booleanMap.put(key, val);
			}
		}

		return booleanMap.get(key);
	}

	public List<Object> getValues(String key) {
		if (valuesMap.get(key) == null) {
			return new ArrayList<>();
		}

		return valuesMap.get(key);
	}

	public List<Object> getPossibleValues(String key) {
		if (hasBooleanConfig(key)) {
			return BOOLEAN_POSSIBLE_VALUES;
		}

		if (possibleValuesMap.get(key) == null) {
			return new ArrayList<>();
		}

		return possibleValuesMap.get(key);
	}

	public static UserConfig getCurrentUserConfig() {
		if (currentConfig == null) {
			return archeoPrototypeConfig;
		}
		return currentConfig;
	}

	public static void setCurrentConfig(UserConfig userConfig) {
		currentConfig = userConfig;
	}

	public static String getUserFromKey(String key) {
		String result = null;

		if (key.startsWith(START_USER_KEY)) {
			result = key.substring(START_USER_KEY.length());
			int userNameLength = result.indexOf("}");

			if (userNameLength > 0) {
				result = result.substring(0, userNameLength);
			}
			return result;
		}
		return result;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": user " + username + ":: " + booleanMap + " :: " + valuesMap;
	}
}
