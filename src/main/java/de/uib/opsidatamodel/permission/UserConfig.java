package de.uib.opsidatamodel.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import de.uib.utilities.logging.Logging;

/*

	concept originally designed eg. as follows 

		cf.
		de.uib.configed.type.user.UserXRole.getRole()

	if GLOBAL_READONLY (can be specified by user group or in config)
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
	public static String CONFIGKEY_STR_USER = "user";

	public static final String ROLE = "role";

	public static final String KEY_USER_ROOT = CONFIGKEY_STR_USER;
	public static final String KEY_USER_ROLE_ROOT = KEY_USER_ROOT + "." + ROLE;
	public static final String ALL_USER_KEY_START = KEY_USER_ROOT + ".{}.";

	public static final String START_USER_KEY = UserConfig.KEY_USER_ROOT + ".{";

	public static final String DEFAULT_ROLE_NAME = "default";
	public static final String ARCHEO_ROLE_NAME = "archeo";
	public static final String NONE_PROTOTYPE = "";

	public static final String roleBranchPart = KEY_USER_ROLE_ROOT;

	public static final String HAS_ROLE_ATTRIBUT = "has_role";
	public static final String MODIFICATION_INFO_KEY = "modified";

	public static final List<Object> EMPTY_LIST = new ArrayList<>();
	public static final List<Object> BOOLEAN_POSSIBLE_VALUES = new ArrayList<>();
	static {
		BOOLEAN_POSSIBLE_VALUES.add(true);
		BOOLEAN_POSSIBLE_VALUES.add(false);
	}

	public static final List<Object> ZERO_TIME;
	static {
		ZERO_TIME = new ArrayList<>();
		ZERO_TIME.add("0000-00-00 00:00:00");
	}

	protected String username;

	protected UserConfig prototypeConfig;

	protected Map<String, Boolean> booleanMap;
	protected Map<String, List<Object>> valuesMap;
	protected Map<String, List<Object>> possibleValuesMap;

	private static LinkedHashSet<String> USER_BOOL_KEYS;

	public static LinkedHashSet<String> getUserBoolKeys() {
		if (USER_BOOL_KEYS == null) {
			USER_BOOL_KEYS = new LinkedHashSet<>();

			Logging.info("addAll ssh bool keys");
			USER_BOOL_KEYS.addAll(UserSshConfig.BOOL_KEYS);
			Logging.info("addAll opsipermission bool keys");
			USER_BOOL_KEYS.addAll(UserOpsipermission.BOOL_KEYS);
		}

		return USER_BOOL_KEYS;

	}

	private static LinkedHashSet<String> USER_LIST_KEYS;

	private static LinkedHashSet<String> USER_STRINGVALUE_KEYS;

	private static LinkedHashSet<String> USER_STRINGVALUE_KEYS_WITHOUT_ROLE;

	public static LinkedHashSet<String> getUserStringValueKeys() {
		if (USER_STRINGVALUE_KEYS == null) {
			USER_STRINGVALUE_KEYS = new LinkedHashSet<>(getUserStringValueKeys_withoutRole());
			USER_STRINGVALUE_KEYS.add(HAS_ROLE_ATTRIBUT);
		}

		return USER_STRINGVALUE_KEYS;
	}

	public static LinkedHashSet<String> getUserStringValueKeys_withoutRole() {
		if (USER_STRINGVALUE_KEYS_WITHOUT_ROLE == null) {
			USER_STRINGVALUE_KEYS_WITHOUT_ROLE = new LinkedHashSet<>();
			USER_STRINGVALUE_KEYS_WITHOUT_ROLE.add(MODIFICATION_INFO_KEY);
		}

		return USER_STRINGVALUE_KEYS_WITHOUT_ROLE;

	}

	public static LinkedHashSet<String> getUserListKeys() {

		if (USER_LIST_KEYS == null) {
			USER_LIST_KEYS = new LinkedHashSet<>();

			USER_LIST_KEYS.addAll(UserSshConfig.LIST_KEYS);
			USER_LIST_KEYS.addAll(UserOpsipermission.LIST_KEYS);
		}

		return USER_LIST_KEYS;
	}

	// default UserConfig Objects
	private static UserConfig ARCHEO_PROTOTYPE_CONFIG;

	public static final UserConfig getArcheoConfig() {
		Logging.info("getArcheoConfig");
		if (ARCHEO_PROTOTYPE_CONFIG == null) {
			ARCHEO_PROTOTYPE_CONFIG = new UserConfig(ARCHEO_ROLE_NAME);
			ARCHEO_PROTOTYPE_CONFIG.setValues(HAS_ROLE_ATTRIBUT, EMPTY_LIST);
		}

		getUserBoolKeys();
		getUserListKeys();

		ARCHEO_PROTOTYPE_CONFIG.booleanMap.putAll(UserSshConfig.DEFAULT.booleanMap);
		ARCHEO_PROTOTYPE_CONFIG.booleanMap.putAll(UserOpsipermission.DEFAULT.booleanMap);

		ARCHEO_PROTOTYPE_CONFIG.valuesMap.putAll(UserOpsipermission.DEFAULT.valuesMap);
		ARCHEO_PROTOTYPE_CONFIG.possibleValuesMap.putAll(UserOpsipermission.DEFAULT.possibleValuesMap);

		ARCHEO_PROTOTYPE_CONFIG.setValues(MODIFICATION_INFO_KEY, ZERO_TIME);

		return ARCHEO_PROTOTYPE_CONFIG;
	}

	public UserConfig(String userName) {
		Logging.info(this, "create for " + userName);
		this.username = userName;
		booleanMap = new LinkedHashMap<>();
		valuesMap = new LinkedHashMap<>();
		possibleValuesMap = new LinkedHashMap<>();
	}

	public String getUserName() {
		return username;
	}

	public boolean hasBooleanConfig(String key) {
		return USER_BOOL_KEYS.contains(key);
	}

	public boolean hasListConfig(String key) {
		return USER_LIST_KEYS.contains(key);
	}

	public void setBooleanValue(String key, Boolean val) {
		if (!getUserBoolKeys().contains(key)) {
			Logging.error("UserConfig.USER_BOOL_KEYS " + UserConfig.USER_BOOL_KEYS);
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
		if (!USER_BOOL_KEYS.contains(key)) {
			Logging.error("UserConfig.USER_BOOL_KEYS " + UserConfig.USER_BOOL_KEYS);
			Logging.error("UserConfig : illegal key " + key);
			return false;
		}

		if (booleanMap.get(key) != null) {
			return booleanMap.get(key);
		}

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

		return booleanMap.get(key);
	}

	public List<Object> getValues(String key) {
		if (valuesMap.get(key) == null)
			return new ArrayList<>();

		return valuesMap.get(key);
	}

	public List<Object> getPossibleValues(String key) {
		if (hasBooleanConfig(key))
			return BOOLEAN_POSSIBLE_VALUES;

		if (possibleValuesMap.get(key) == null)
			return new ArrayList<>();

		return possibleValuesMap.get(key);
	}

	public String getUser() {
		return username;
	}

	public void setUser(String uname) {
		username = uname;
	}

	public UserConfig getPrototype() {
		if (prototypeConfig == null)
			return ARCHEO_PROTOTYPE_CONFIG;
		return prototypeConfig;
	}

	public void setPrototype(UserConfig prototype) {
		this.prototypeConfig = prototype;
	}

	private static UserConfig currentConfig;

	public static UserConfig getCurrentUserConfig() {
		if (currentConfig == null)
			return ARCHEO_PROTOTYPE_CONFIG;

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

	public static String getPartKeyFromUserconfigKey(String key) {
		String partKey = null;

		String user = getUserFromKey(key);

		if (user != null) {
			String userStart = UserConfig.KEY_USER_ROOT + ".{" + user + ".}.";
			partKey = key.substring(userStart.length());

			return partKey;
		}

		return partKey;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": user " + username + ":: " + booleanMap + " :: " + valuesMap;
	}

}
