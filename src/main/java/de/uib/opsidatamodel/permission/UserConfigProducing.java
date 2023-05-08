package de.uib.opsidatamodel.permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.ConfigOption;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;

public class UserConfigProducing {
	private boolean notUsingDefaultUser;

	String configserver;
	Collection<String> existingDepots;
	Collection<String> existingHostgroups;
	Collection<String> existingProductgroups;

	Map<String, List<Object>> serverconfigValuesMap;
	Map<String, de.uib.utilities.table.ListCellOptions> configOptionsMap;

	List<Object> readyObjects;

	public UserConfigProducing(
			// command tools

			// data used for configuring
			boolean notUsingDefaultUser,

			String configserver,

			Collection<String> existingDepots, Collection<String> existingHostgroups,
			Collection<String> existingProductgroups,

			// data. on which changes are based
			Map<String, List<Object>> serverconfigValuesMap,
			Map<String, de.uib.utilities.table.ListCellOptions> configOptionsMap) {
		this.notUsingDefaultUser = notUsingDefaultUser;
		this.configserver = configserver;

		this.existingDepots = existingDepots;
		this.existingHostgroups = existingHostgroups;
		this.existingProductgroups = existingProductgroups;

		this.serverconfigValuesMap = serverconfigValuesMap;
		this.configOptionsMap = configOptionsMap;

		Logging.info(this, "create with existing collections depots, hostgroups, productgroups " + existingDepots.size()
				+ " - " + existingHostgroups.size() + " - " + existingProductgroups.size());
	}

	public List<Object> produce() {
		readyObjects = new ArrayList<>();

		Set<String> userparts = new TreeSet<>();
		Set<String> roleparts = new TreeSet<>();
		produceRoleAndUserParts(userparts, roleparts);

		Logging.info(this, "we have got logged in user " + ConfigedMain.user + " and configure based on it "
				+ notUsingDefaultUser);

		if (notUsingDefaultUser && ConfigedMain.user != null && !serverconfigValuesMap.containsKey(ConfigedMain.user)) {
			Logging.info(this, "supply logged in user");
			userparts.add(ConfigedMain.user);
			String propertyclass = UserConfig.START_USER_KEY + ConfigedMain.user + '}';

			AbstractPersistenceController.PROPERTY_CLASSES_SERVER.computeIfAbsent(propertyclass, (String arg) -> {
				Logging.info(this, "createUserPropertySubclass for logged in user " + ConfigedMain.user);
				return "";
			});
		}

		supplyAllPermissionEntries(userparts, roleparts);

		return readyObjects;
	}

	private void produceRoleAndUserParts(Set<String> userNames, Set<String> roleNames) {
		Logging.info(this, "produceRoleAndUserParts for " + userNames + " resp. " + roleNames);

		final String roleBranchPart = UserConfig.KEY_USER_ROLE_ROOT;
		final String startRoleKey = roleBranchPart + ".{";

		for (String key : serverconfigValuesMap.keySet()) {
			if (!(key.startsWith(UserConfig.KEY_USER_ROOT))) {
				continue;
			}

			if (key.startsWith(roleBranchPart)) {
				String rolenameBefore = key.substring(0, startRoleKey.length());
				String rolename = key.substring(rolenameBefore.length());
				int lenOfRoleName = rolename.indexOf("}");

				if (lenOfRoleName > 0) {
					rolename = rolename.substring(0, lenOfRoleName);

					Logging.info(this, "role branch with rolename " + rolename);

					roleNames.add(rolename);

					String propertyclass = startRoleKey + rolename + '}';

					final String role = rolename;
					AbstractPersistenceController.PROPERTY_CLASSES_SERVER.computeIfAbsent(propertyclass,
							(String arg) -> {
								Logging.info(this, "createRolePropertySubclass for role  " + role);
								return "";
							});
				} else {
					Logging.warning(this, "rolePart without proper rolename found " + key);
				}
			} else {

				if (key.startsWith(UserConfig.ALL_USER_KEY_START)) {
					Logging.info(this,
							"not delivered in this collection " + key.substring(UserConfig.START_USER_KEY.length()));
					// not delivered in this collection
				} else {
					String username = UserConfig.getUserFromKey(key);
					Logging.debug(this, "produceRoleAndUserParts userpart start  " + username);

					if (username != null) {
						userNames.add(username);
						Logging.debug(this, "usernames, add " + username + " for key " + key);
						String propertyclass = UserConfig.START_USER_KEY + username + '}';

						AbstractPersistenceController.PROPERTY_CLASSES_SERVER.computeIfAbsent(propertyclass,
								(String arg) -> {
									Logging.info(this, "createUserPropertySubclass for user  " + username);
									return "";
								});

					} else {
						Logging.warning(this, "username not specified in key " + key);
					}
				}
			}
		}

		Logging.info(this, "all roleNames " + roleNames);
		Logging.info(this, "all userNames " + userNames);
	}

	private void supplyConfigPermissionList(final String configKeyUseList, final boolean initialValue,
			final String configKeyList, final List<Object> selectedValues, final Set<Object> oldPossibleValues,
			final Set<Object> currentPossibleValuesListed) {
		// produces readyObjects for this configKey(List)

		Logging.info(this, "supplyConfigPermissionList, configKeyUseList: " + configKeyUseList);

		// item variable for adding items to readyObjects
		Map<String, Object> item = null;

		Logging.info(this, "supplyConfigPermissionList  configKey " + configKeyUseList);

		if (serverconfigValuesMap.get(configKeyUseList) == null) {
			Logging.info(this, "supplyPermissionList. serverconfigValuesMap has no value for key " + configKeyUseList);
			item = AbstractPersistenceController.createJSONBoolConfig(configKeyUseList, initialValue,
					"the primary value setting is " + initialValue);
			readyObjects.add(AbstractExecutioner.jsonMap(item));
		}

		Logging.info(this, "supplyPermissionList  configKey " + configKeyList);

		Logging.debug(this, "serverconfigValuesMap.get( configKeyList ) " + serverconfigValuesMap.get(configKeyList));
		Logging.debug(this, "selectedValues  " + selectedValues);

		Logging.debug(this, "currentPossibleValuesListed " + currentPossibleValuesListed);
		Logging.debug(this, "oldPossibleValues " + oldPossibleValues);

		if ((serverconfigValuesMap.get(configKeyList) == null)
				|| !serverconfigValuesMap.get(configKeyList).equals(selectedValues)
				|| !currentPossibleValuesListed.equals(oldPossibleValues)) {

			// for the configKey at all
			Logging.info(this, "supplyPermissionList initialization or change");

			Logging.info(this,
					"supplyPermissionList add to currentPossibleValuesListed " + currentPossibleValuesListed);
			List<Object> listOptions = new ArrayList<>(currentPossibleValuesListed);

			Logging.info(this, "supplyPermissionList products List " + listOptions);

			item = AbstractPersistenceController.createNOMitem(ConfigOption.UNICODE_TYPE);

			item.put("ident", configKeyList);
			item.put("editable", false);
			item.put("multiValue", true);

			item.put("description",
					"the primary value setting is an empty selection list, but all existing items as option");
			item.put("defaultValues", AbstractExecutioner.jsonArray(selectedValues));
			item.put("possibleValues", AbstractExecutioner.jsonArray(listOptions));

			readyObjects.add(AbstractExecutioner.jsonMap(item));
		}
	}

	/** we call up the cascade of default role, other roles, and the users */
	protected void supplyAllPermissionEntries(Set<String> userParts, Set<String> roleParts) {
		Logging.info(this, "supplyAllPermissionEntries start");

		Logging.info(this, "supplyAllPermissionEntries all roles " + roleParts);
		Logging.info(this, "supplyAllPermissionEntries first for default role,  " + UserConfig.DEFAULT_ROLE_NAME);

		// (1) the default role
		Logging.info(this, "supplyAllPermissionEntries UserConfig.getArcheoConfig( " + UserConfig.getArcheoConfig());

		UserConfig defaultUserConfig = new UserConfig(UserConfig.DEFAULT_ROLE_NAME);
		String rolenameStartkey = UserConfig.KEY_USER_ROOT + "." + UserConfig.ROLE + ".{" + UserConfig.DEFAULT_ROLE_NAME
				+ "}.";
		supplyPermissionEntriesForAUser(UserConfig.DEFAULT_ROLE_NAME, rolenameStartkey, false,
				UserConfig.getArcheoConfig(), defaultUserConfig);

		Logging.info(this, "supplyAllPermissionEntries defaultUserConfig " + defaultUserConfig);

		Map<String, UserConfig> roleConfigs = new HashMap<>();
		Map<String, UserConfig> userConfigs = new HashMap<>();

		roleConfigs.put(UserConfig.DEFAULT_ROLE_NAME, defaultUserConfig);

		Set<String> extraRoleParts = new HashSet<>(roleParts);
		extraRoleParts.remove(UserConfig.DEFAULT_ROLE_NAME);

		Logging.info(this, "supplyAllPermissionEntries extraRoleParts " + extraRoleParts);

		for (String rolename : extraRoleParts) {
			rolenameStartkey = UserConfig.KEY_USER_ROOT + "." + UserConfig.ROLE + ".{" + rolename + "}.";

			UserConfig roleConfig = new UserConfig(rolename);
			supplyPermissionEntriesForAUser(rolename, rolenameStartkey, false, defaultUserConfig, roleConfig);

			roleConfigs.put(rolename, roleConfig);
		}

		Logging.info(this, "supplyAllPermissionEntries roleConfigs.size() " + roleConfigs.size());
		Logging.info(this, "supplyAllPermissionEntries readyObjects for roleparts " + readyObjects.size());
		Logging.info(this, "supplyAllPermissionEntries for userparts " + userParts);

		for (String username : userParts) {
			String roleToPlay = UserConfig.DEFAULT_ROLE_NAME;
			String usernameStartkey = UserConfig.KEY_USER_ROOT + ".{" + username + "}.";
			UserConfig userConfig = new UserConfig(username);
			userConfigs.put(username, userConfig);
			String roleKey = usernameStartkey + UserConfig.HAS_ROLE_ATTRIBUT;

			Logging.info(this,
					"supplyAllPermissionEntries usernameStartkey " + usernameStartkey + " roleKey " + roleKey);

			List<Object> values = serverconfigValuesMap.get(roleKey);

			Logging.info(this,
					"supplyAllPermissionEntries got values " + values + " for role from serverconfigValuesMap ");

			String configuredRole = null;
			boolean followConfiguredRole = false;
			Logging.info(this, "supplyAllPermissionEntries has role " + values);

			if (values == null || values.isEmpty()) {
				// update role selection because we don't have one
				List<Object> selectedValuesRole = new ArrayList<>();
				selectedValuesRole.add(UserConfig.NONE_PROTOTYPE);

				Set<String> possibleValuesSet = new HashSet<>(roleParts);
				possibleValuesSet.add(configuredRole);

				possibleValuesSet.add(UserConfig.NONE_PROTOTYPE);

				List<Object> possibleValuesRole = new ArrayList<>(possibleValuesSet);

				Map<String, Object> itemRole = AbstractPersistenceController.createJSONConfig(
						ConfigOption.TYPE.UNICODE_CONFIG, roleKey,
						"which role should determine this users configuration", false, false, selectedValuesRole,
						possibleValuesRole);

				Logging.info(this, "supplyAllPermissionEntries possibleValuesRole, roleParts " + " "
						+ possibleValuesRole + ", " + roleParts);

				readyObjects.add(AbstractExecutioner.jsonMap(itemRole));
			} else if (!((String) values.get(0)).equals(UserConfig.NONE_PROTOTYPE)) {

				// we have got some value
				configuredRole = "" + values.get(0);

				Logging.info(this, "supplyAllPermissionEntries configuredRole " + configuredRole);
				Logging.info(this, "supplyAllPermissionEntries roleConfigs " + roleConfigs);

				if (roleConfigs.containsKey(configuredRole)) {
					roleToPlay = configuredRole;
					followConfiguredRole = true;
				}
			} else {
				Logging.info(this, "no role specified for user " + username);
			}

			Logging.info(this,
					" for user " + username + " followConfiguredRole " + followConfiguredRole + ": " + roleToPlay);

			UserConfig roleConfig = roleConfigs.get(roleToPlay);
			// is defaultConfig if roleToPlay does not exist

			supplyPermissionEntriesForAUser(username, usernameStartkey, followConfiguredRole, roleConfig, userConfig);
		}

		Logging.info(this, "readyObjects for userparts " + readyObjects.size());

		if (notUsingDefaultUser) {
			UserConfig.setCurrentConfig(userConfigs.get(ConfigedMain.user));
		} else {
			UserConfig.setCurrentConfig(defaultUserConfig);
		}
	}

	private void supplyPermissionEntriesForAUser(final String username, final String startkey,
			final boolean prototypeObligatory, final UserConfig prototypeConfig, UserConfig userConfig) {
		Logging.info(this, "supplyPermissionEntriesForAUser for user " + username + " with startkey " + startkey);
		Logging.info(this, "supplyPermissionEntriesForAUser for user, prototypeConfig " + prototypeConfig);

		int countReadyObjectsOnStart = readyObjects.size();

		Map<String, Object> item = null;

		String configKey = null;

		// Boolean valued

		Logging.info(this,
				"supplyPermissionEntriesForAUser UserConfig.getUserBoolKeys( " + UserConfig.getUserBoolKeys());

		for (String partkey : UserConfig.getUserBoolKeys()) {
			// includes at the moment ssh permission keys and opsi permission keys

			configKey = startkey + partkey;

			Logging.info(this,
					"supplyPermissionEntriesForAUser boolean configKey " + configKey + " -- partkey " + partkey);

			List<Object> values = serverconfigValuesMap.get(configKey);
			Boolean value = null;

			Logging.info(this,
					"supplyPermissionEntriesForAUser bool configKey " + configKey + " -- partkey " + partkey);
			Logging.info(this, "supplyPermissionEntriesForAUser bool configKey has values " + values);

			// there is no formally correct value)
			// the specific values differs from prototype values and must be corrected
			if (values == null || values.isEmpty() || !(values.get(0) instanceof Boolean)
					|| (prototypeObligatory && !values.get(0).equals(prototypeConfig.getBooleanValue(partkey)))) {
				Logging.info(this,
						"supplyPermissionEntriesForAUser. serverconfigValuesMap has no value for key " + configKey);
				value = prototypeConfig.getBooleanValue(partkey);

				userConfig.setBooleanValue(partkey, value);
				item = AbstractPersistenceController.createJSONBoolConfig(configKey, value,
						"the primary value setting is based on the user group");

				readyObjects.add(AbstractExecutioner.jsonMap(item));
			} else {
				value = (Boolean) values.get(0);
			}

			userConfig.setBooleanValue(partkey, value);
		}

		Logging.info(this, "supplyPermissionEntriesForAUser, readyObjects bool keys for user named " + username + " "
				+ readyObjects);

		// single String valued
		Logging.info(this, "supplyPermissionEntriesForAUser UserConfig.getUserStringValueKeys "
				+ UserConfig.getUserStringValueKeys());
		Logging.info(this, "supplyPermissionEntriesForAUser UserConfig.getUserStringValueKeys_withoutRole "
				+ UserConfig.getUserStringValueKeysWithoutRole());

		// role entry, will be removed for the next run, if not obligatory
		if (!prototypeObligatory) {
			configKey = startkey + UserConfig.HAS_ROLE_ATTRIBUT;
			Logging.info(this, "configkey " + configKey);
			List<Object> values = serverconfigValuesMap.get(configKey);

			if (values == null || values.isEmpty() || values.get(0) == null
					|| !((String) values.get(0)).equals(UserConfig.NONE_PROTOTYPE)) {

				Logging.info(this,
						"supplyPermissionEntriesForAUser. serverconfigValuesMap has no value for key " + configKey);

				List<Object> selectedValuesRole = new ArrayList<>();
				selectedValuesRole.add(UserConfig.NONE_PROTOTYPE);

				Map<String, Object> itemRole = AbstractPersistenceController.createJSONConfig(
						ConfigOption.TYPE.UNICODE_CONFIG, configKey,
						"which role should determine this users configuration", false, false, selectedValuesRole,
						selectedValuesRole);

				readyObjects.add(AbstractExecutioner.jsonMap(itemRole));
			}
		}

		for (String partkey : UserConfig.getUserStringValueKeysWithoutRole()) {

			configKey = startkey + partkey;
			// String configKey = startkey +

			List<Object> values = serverconfigValuesMap.get(configKey);

			Logging.info(this, "supplyPermissionEntriesForAUser configKey " + configKey + " -- partkey " + partkey);
			Logging.info(this, "supplyPermissionEntriesForAUser configKey has size 1 values " + values);

			// We don't want to change the 'modified' value based on userroles (when prototypeObligatory is true)
			if (values == null || (prototypeObligatory && !partkey.equals(UserConfig.MODIFICATION_INFO_KEY)
					&& !(values.equals(prototypeConfig.getValues(partkey))))) {
				Logging.info(this,
						"supplyPermissionEntriesForAUser. serverconfigValuesMap gives not valid value for key "
								+ configKey);

				values = prototypeConfig.getValues(partkey);

				userConfig.setValues(partkey, values);

				item = AbstractPersistenceController.createJSONConfig(ConfigOption.TYPE.UNICODE_CONFIG, configKey,
						configKey, false, false, values, values);

				// TODO
				readyObjects.add(AbstractExecutioner.jsonMap(item));
			}
		}

		// Stringlist valued

		Logging.info(this,
				"supplyPermissionEntriesForAUser UserConfig.getUserListKeys( " + UserConfig.getUserListKeys());

		Logging.info(this, "supplyPermissionEntriesForAUser  user config " + userConfig);

		Logging.info(this,
				"supplyPermissionEntriesForAUser, readyObjects list keys for " + username + " " + readyObjects.size());
		Logging.info(this, "supplyPermissionEntriesForAUser UserConfig " + userConfig);

		// == update all list entries

		// == update depots

		List<Object> selectedValuesDepot = null;
		List<Object> possibleValuesDepot = null;
		Set<Object> oldPossibleValuesDepot = null;
		LinkedHashSet<Object> currentPossibleValuesDepotListed = null;

		String configKeyUseList = startkey + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;

		String partkey = UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;
		String configKeyList = startkey + partkey;

		boolean defaultvalueForRestrictionUsage = prototypeConfig
				.getBooleanValue(UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED);

		Logging.info(this, "configKeyUseList " + configKeyUseList + ", configKeyList " + configKeyList);
		currentPossibleValuesDepotListed = new LinkedHashSet<>();

		if (prototypeObligatory || serverconfigValuesMap.get(configKeyList) == null) {
			selectedValuesDepot = prototypeConfig.getValues(partkey);
		} else {
			selectedValuesDepot = serverconfigValuesMap.get(configKeyList);
		}

		userConfig.setValues(partkey, selectedValuesDepot);

		if (configOptionsMap.get(configKeyList) == null
				|| configOptionsMap.get(configKeyList).getPossibleValues() == null) {
			oldPossibleValuesDepot = new TreeSet<>();
		} else {
			oldPossibleValuesDepot = new HashSet<>(configOptionsMap.get(configKeyList).getPossibleValues());
		}

		Logging.info(this, "oldPossibleValuesDepot " + oldPossibleValuesDepot);

		if (prototypeObligatory) {
			possibleValuesDepot = prototypeConfig.getPossibleValues(partkey);
			currentPossibleValuesDepotListed = new LinkedHashSet<>(possibleValuesDepot);

		} else {

			currentPossibleValuesDepotListed.add(configserver);

			Set<Object> posVals = new TreeSet<>();
			posVals.addAll(existingDepots);
			posVals.addAll(oldPossibleValuesDepot);

			currentPossibleValuesDepotListed = new LinkedHashSet<>(posVals);
		}

		userConfig.setPossibleValues(partkey, new ArrayList<>(currentPossibleValuesDepotListed));

		Logging.info(this,
				"depots currentPossibleValuesDepotListed before supplying " + currentPossibleValuesDepotListed);

		supplyConfigPermissionList(configKeyUseList, // final
				// activate this feature by default?
				defaultvalueForRestrictionUsage, configKeyList, // final
				selectedValuesDepot, // final
				oldPossibleValuesDepot, // final
				currentPossibleValuesDepotListed);

		// == update hostgroups

		List<Object> selectedValuesHostgroup = null;
		List<Object> possibleValuesHostgroup = null;
		Set<Object> oldPossibleValuesHostgroup = null;
		Set<Object> currentPossibleValuesHostgroupListed = null;

		configKeyUseList = startkey + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED;
		partkey = UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE;
		configKeyList = startkey + partkey;

		Logging.info(this, "configKeyUseList " + configKeyUseList + ", configKeyList " + configKeyList);

		defaultvalueForRestrictionUsage = prototypeConfig
				.getBooleanValue(UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED);

		possibleValuesHostgroup = new ArrayList<>();

		if (prototypeObligatory || serverconfigValuesMap.get(configKeyList) == null) {
			selectedValuesHostgroup = prototypeConfig.getValues(partkey);
		} else {
			selectedValuesHostgroup = serverconfigValuesMap.get(configKeyList);
		}

		Logging.info(this, "selectedValuesHostgroup for user " + username + ": " + selectedValuesHostgroup);

		userConfig.setValues(partkey, selectedValuesHostgroup);

		if (configOptionsMap.get(configKeyList) == null
				|| configOptionsMap.get(configKeyList).getPossibleValues() == null) {
			oldPossibleValuesHostgroup = new TreeSet<>();
		} else {
			oldPossibleValuesHostgroup = new HashSet<>(configOptionsMap.get(configKeyList).getPossibleValues());
		}

		if (prototypeObligatory) {
			possibleValuesHostgroup = prototypeConfig.getPossibleValues(partkey);
			currentPossibleValuesHostgroupListed = new LinkedHashSet<>(possibleValuesHostgroup);
		} else {
			Set<Object> posVals = new TreeSet<>(existingHostgroups);
			posVals.addAll(oldPossibleValuesHostgroup);
			currentPossibleValuesHostgroupListed = new LinkedHashSet<>(posVals);
		}

		userConfig.setPossibleValues(partkey, new ArrayList<>(currentPossibleValuesHostgroupListed));

		Logging.info(this,
				"hostGroups selectedValuesHostgroup before supplying for " + username + ": " + selectedValuesHostgroup);
		Logging.info(this, "hostGroups oldPossibleValuesHostgroupListed before supplying for " + username + ": "
				+ oldPossibleValuesHostgroup);
		Logging.info(this,
				"hostgroups possibleValuesHostgroup before supplying for " + username + ": " + possibleValuesHostgroup);
		Logging.info(this, "hostgroups currentPossibleValuesHostgroupListed before supplying for " + username + ": "
				+ currentPossibleValuesHostgroupListed);

		supplyConfigPermissionList(configKeyUseList, // final
				// activate this feature by default?
				defaultvalueForRestrictionUsage, configKeyList, // final
				selectedValuesHostgroup, // final
				oldPossibleValuesHostgroup, // final
				currentPossibleValuesHostgroupListed);

		// == update productgroups possible values

		List<Object> selectedValuesProductgroups = null;
		List<Object> possibleValuesProductgroups = null;
		Set<Object> oldPossibleValuesProductgroups = null;
		LinkedHashSet<Object> currentPossibleValuesProductgroupsListed = null;

		configKeyUseList = startkey + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED;
		partkey = UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE;
		configKeyList = startkey + partkey;

		Logging.info(this, "configKeyUseList " + configKeyUseList + ", configKeyList " + configKeyList);

		defaultvalueForRestrictionUsage = prototypeConfig
				.getBooleanValue(UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED);

		if (prototypeObligatory || serverconfigValuesMap.get(configKeyList) == null) {
			selectedValuesProductgroups = prototypeConfig.getValues(partkey);
		} else {
			selectedValuesProductgroups = serverconfigValuesMap.get(configKeyList);
		}

		userConfig.setValues(partkey, selectedValuesProductgroups);

		if (configOptionsMap.get(configKeyList) == null
				|| configOptionsMap.get(configKeyList).getPossibleValues() == null) {
			oldPossibleValuesProductgroups = new TreeSet<>();
		} else {
			oldPossibleValuesProductgroups = new HashSet<>(configOptionsMap.get(configKeyList).getPossibleValues());
		}

		currentPossibleValuesProductgroupsListed = new LinkedHashSet<>();

		if (prototypeObligatory) {
			possibleValuesProductgroups = prototypeConfig.getPossibleValues(partkey);
			currentPossibleValuesProductgroupsListed.addAll(possibleValuesProductgroups);
		} else {
			Set<Object> posVals = new TreeSet<>(existingProductgroups);
			currentPossibleValuesProductgroupsListed.addAll(posVals);
		}

		userConfig.setPossibleValues(partkey, new ArrayList<>(currentPossibleValuesProductgroupsListed));

		Logging.info(this, "productGroups selectedValuesProductgroups before supplying " + selectedValuesProductgroups);
		Logging.info(this, "productGroups oldPossibleValuesProductgroupsListed before supplying "
				+ oldPossibleValuesProductgroups);
		Logging.info(this, "productGroups currentPossibleValuesProductgroupsListed before supplying "
				+ currentPossibleValuesProductgroupsListed);

		supplyConfigPermissionList(configKeyUseList, // final
				// activate this feature by default?
				defaultvalueForRestrictionUsage, configKeyList, // final
				selectedValuesProductgroups, // final
				oldPossibleValuesProductgroups, // final
				currentPossibleValuesProductgroupsListed);

		Logging.info(this, "supplyPermissionEntriesForAUser username " + username);
		Logging.info(this, "supplyPermissionEntriesForAUser countReadyObjectsOnStart " + countReadyObjectsOnStart);
		Logging.info(this, "supplyPermissionEntriesForAUser readyObjects.size() " + readyObjects.size());

		if (countReadyObjectsOnStart == readyObjects.size()) {
			Logging.info(this,
					"supplyPermissionEntriesForAUser added no object(s) for saving, for username " + username);
		} else {
			Logging.info(this, "supplyPermissionEntriesForAUser added object(s) for saving, for username " + username
					+ ": " + (readyObjects.size() - 1));
			List<Object> timeVal = Globals.getNowTimeListValue("set by role prototype");

			Map<String, Object> itemModifyTime = AbstractPersistenceController.createNOMitem(ConfigOption.UNICODE_TYPE);

			itemModifyTime.put("ident", startkey + UserConfig.MODIFICATION_INFO_KEY);
			itemModifyTime.put("editable", false);
			itemModifyTime.put("multiValue", false);

			itemModifyTime.put("description", "last modification time for entries of this user");

			itemModifyTime.put("defaultValues", timeVal);
			itemModifyTime.put("possibleValues", timeVal);

			Logging.info(this, "modi time " + itemModifyTime);

			// TODO
			readyObjects.add(AbstractExecutioner.jsonMap(itemModifyTime));
		}
	}
}
