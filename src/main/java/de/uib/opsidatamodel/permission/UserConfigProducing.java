/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

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
import de.uib.configed.type.ConfigOption;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.ListCellOptions;

public class UserConfigProducing {
	private boolean notUsingDefaultUser;

	private String configserver;
	private Collection<String> existingDepots;
	private Collection<String> existingHostgroups;
	private Collection<String> existingProductgroups;

	private Map<String, List<Object>> serverconfigValuesMap;
	private Map<String, ListCellOptions> configOptionsMap;

	private List<Object> readyObjects;

	public UserConfigProducing(boolean notUsingDefaultUser, String configserver, Collection<String> existingDepots,
			Collection<String> existingHostgroups, Collection<String> existingProductgroups,
			Map<String, List<Object>> serverconfigValuesMap, Map<String, ListCellOptions> configOptionsMap) {
		this.notUsingDefaultUser = notUsingDefaultUser;
		this.configserver = configserver;
		this.existingDepots = existingDepots;
		this.existingHostgroups = existingHostgroups;
		this.existingProductgroups = existingProductgroups;
		this.serverconfigValuesMap = serverconfigValuesMap;
		this.configOptionsMap = configOptionsMap;

		Logging.info(this.getClass(), "create with existing collections depots, hostgroups, productgroups "
				+ existingDepots.size() + " - " + existingHostgroups.size() + " - " + existingProductgroups.size());
	}

	public List<Object> produce() {
		readyObjects = new ArrayList<>();

		Set<String> userparts = new TreeSet<>();
		Set<String> roleparts = new TreeSet<>();
		produceRoleAndUserParts(userparts, roleparts);

		Logging.info(this, "we have got logged in user " + ConfigedMain.getUser() + " and configure based on it "
				+ notUsingDefaultUser);

		if (notUsingDefaultUser && ConfigedMain.getUser() != null
				&& !serverconfigValuesMap.containsKey(ConfigedMain.getUser())) {
			Logging.info(this, "supply logged in user");
			userparts.add(ConfigedMain.getUser());
			createPropertySubclass(ConfigedMain.getUser(), UserConfig.CONFIGKEY_STR_USER);
		}

		supplyAllPermissionEntries(userparts, roleparts);

		return readyObjects;
	}

	private void produceRoleAndUserParts(Set<String> userNames, Set<String> roleNames) {
		Logging.info(this, "produceRoleAndUserParts for " + userNames + " resp. " + roleNames);

		for (String key : serverconfigValuesMap.keySet()) {
			if (!(key.startsWith(UserConfig.KEY_USER_ROOT))) {
				continue;
			}

			if (key.startsWith(UserConfig.KEY_USER_ROLE_ROOT)) {
				String roleName = produceRolePart(key);
				if (roleName != null) {
					Logging.info(this, "role branch with rolename " + roleName);
					roleNames.add(roleName);
					createPropertySubclass(roleName, UserConfig.ROLE);
				}
			} else if (key.startsWith(UserConfig.ALL_USER_KEY_START)) {
				Logging.info(this, "not delivered in this collection " + key);
			} else {
				String userName = produceUserPart(key);
				if (userName != null) {
					Logging.debug(this, "usernames, add " + userName + " for key " + key);
					userNames.add(userName);
					createPropertySubclass(userName, UserConfig.CONFIGKEY_STR_USER);
				} else {
					Logging.warning(this, "username not specified in key " + key);
				}
			}
		}

		Logging.info(this, "all roleNames " + roleNames);
		Logging.info(this, "all userNames " + userNames);
	}

	private String produceRolePart(String roleKey) {
		final String startRoleKey = UserConfig.KEY_USER_ROLE_ROOT + ".{";
		final String roleNameBefore = roleKey.substring(0, startRoleKey.length());
		String roleName = roleKey.substring(roleNameBefore.length());
		final int lenOfRoleName = roleName.indexOf("}");

		if (lenOfRoleName > 0) {
			return roleName.substring(0, lenOfRoleName);
		} else {
			Logging.warning(this, "rolePart without proper rolename found " + roleKey);
		}

		return null;
	}

	private String produceUserPart(String userKey) {
		String username = UserConfig.getUserFromKey(userKey);
		Logging.debug(this, "produceUserPart userpart start  " + username);
		return username;
	}

	private void createPropertySubclass(String property, String propertyType) {
		final String propertyclass = UserConfig.START_USER_KEY + property + '}';
		OpsiServiceNOMPersistenceController.getPropertyClassesServer().computeIfAbsent(propertyclass, (String arg) -> {
			Logging.info(this, "createPropertySubclass for " + propertyType + " " + property);
			return "";
		});
	}

	private void supplyConfigPermissionList(final String configKeyUseList, final boolean initialValue,
			final String configKeyList, final List<Object> selectedValues, final Set<Object> oldPossibleValues,
			final Set<Object> currentPossibleValuesListed) {
		// produces readyObjects for this configKey(List)

		Logging.info(this, "supplyConfigPermissionList, configKeyUseList: " + configKeyUseList);

		// item variable for adding items to readyObjects
		Map<String, Object> item = null;

		if (configKeyUseList != null && serverconfigValuesMap.get(configKeyUseList) == null) {
			Logging.info(this,
					"supplyConfigPermissionList. serverconfigValuesMap has no value for key " + configKeyUseList);
			item = Utils.createNOMBoolConfig(configKeyUseList, initialValue,
					"the primary value setting is " + initialValue);
			readyObjects.add(item);
		}

		Logging.info(this, "supplyConfigPermissionList configKey " + configKeyList);
		Logging.debug(this, "serverconfigValuesMap.get(configKeyList) " + serverconfigValuesMap.get(configKeyList));
		Logging.debug(this, "selectedValues " + selectedValues);
		Logging.debug(this, "currentPossibleValuesListed " + currentPossibleValuesListed);
		Logging.debug(this, "oldPossibleValues " + oldPossibleValues);

		if (serverconfigValuesMap.get(configKeyList) == null
				|| !serverconfigValuesMap.get(configKeyList).equals(selectedValues)
				|| !currentPossibleValuesListed.equals(oldPossibleValues)) {
			Logging.info(this, "supplyConfigPermissionList initialization or change");
			Logging.info(this,
					"supplyConfigPermissionList add to currentPossibleValuesListed " + currentPossibleValuesListed);

			List<Object> listOptions = new ArrayList<>(currentPossibleValuesListed);
			Logging.info(this, "supplyConfigPermissionList products List " + listOptions);

			item = Utils.createNOMitem(ConfigOption.UNICODE_TYPE);
			item.put("ident", configKeyList);
			item.put("editable", false);
			item.put("multiValue", true);
			item.put("description",
					"the primary value setting is an empty selection list, but all existing items as option");
			item.put("defaultValues", selectedValues);
			item.put("possibleValues", listOptions);
			readyObjects.add(item);
		}
	}

	/** we call up the cascade of default role, other roles, and the users */
	private void supplyAllPermissionEntries(Set<String> userParts, Set<String> roleParts) {
		Logging.info(this, "supplyAllPermissionEntries start");
		Logging.info(this, "supplyAllPermissionEntries all roles " + roleParts);
		Logging.info(this, "supplyAllPermissionEntries first for default role,  " + UserConfig.DEFAULT_ROLE_NAME);
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
			UserConfig roleConfig = new UserConfig(rolename);
			roleConfigs.put(rolename, roleConfig);
			rolenameStartkey = UserConfig.KEY_USER_ROOT + "." + UserConfig.ROLE + ".{" + rolename + "}.";
			supplyPermissionEntriesForAUser(rolename, rolenameStartkey, false, defaultUserConfig, roleConfig);
		}

		Logging.info(this, "supplyAllPermissionEntries roleConfigs.size() " + roleConfigs.size());
		Logging.info(this, "supplyAllPermissionEntries readyObjects for roleparts " + readyObjects.size());
		Logging.info(this, "supplyAllPermissionEntries for userparts " + userParts);

		for (String userName : userParts) {
			UserConfig userConfig = new UserConfig(userName);
			userConfigs.put(userName, userConfig);

			String roleToPlay = UserConfig.DEFAULT_ROLE_NAME;
			String userNameStartKey = UserConfig.KEY_USER_ROOT + ".{" + userName + "}.";
			String roleKey = userNameStartKey + UserConfig.HAS_ROLE_ATTRIBUT;

			Logging.info(this,
					"supplyAllPermissionEntries usernameStartkey " + userNameStartKey + " roleKey " + roleKey);

			List<Object> values = serverconfigValuesMap.get(roleKey);

			Logging.info(this,
					"supplyAllPermissionEntries got values " + values + " for role from serverconfigValuesMap ");

			String configuredRole = null;
			boolean followConfiguredRole = false;
			Logging.info(this, "supplyAllPermissionEntries has role " + values);

			if (values == null || values.isEmpty()) {
				// update role selection because we don't have one
				readyObjects.add(createDefaultItemRole(roleKey, configuredRole, roleParts));
			} else if (!((String) values.get(0)).equals(UserConfig.NONE_PROTOTYPE)) {
				configuredRole = "" + values.get(0);

				Logging.info(this, "supplyAllPermissionEntries configuredRole " + configuredRole);
				Logging.info(this, "supplyAllPermissionEntries roleConfigs " + roleConfigs);

				if (roleConfigs.containsKey(configuredRole)) {
					roleToPlay = configuredRole;
					followConfiguredRole = true;
				}
			} else {
				Logging.info(this, "no role specified for user " + userName);
			}

			Logging.info(this,
					" for user " + userName + " followConfiguredRole " + followConfiguredRole + ": " + roleToPlay);

			UserConfig roleConfig = roleConfigs.get(roleToPlay);
			supplyPermissionEntriesForAUser(userName, userNameStartKey, followConfiguredRole, roleConfig, userConfig);
		}

		Logging.info(this, "readyObjects for userparts " + readyObjects.size());

		if (notUsingDefaultUser) {
			UserConfig.setCurrentConfig(userConfigs.get(ConfigedMain.getUser()));
		} else {
			UserConfig.setCurrentConfig(defaultUserConfig);
		}
	}

	private Map<String, Object> createDefaultItemRole(String roleKey, String configuredRole, Set<String> roleParts) {
		List<Object> selectedValuesRole = new ArrayList<>();
		selectedValuesRole.add(UserConfig.NONE_PROTOTYPE);

		Set<String> possibleValuesSet = new HashSet<>(roleParts);
		possibleValuesSet.add(configuredRole);
		possibleValuesSet.add(UserConfig.NONE_PROTOTYPE);

		List<Object> possibleValuesRole = new ArrayList<>(possibleValuesSet);
		Logging.info(this, "supplyAllPermissionEntries possibleValuesRole, roleParts " + " " + possibleValuesRole + ", "
				+ roleParts);

		return Utils.createNOMConfig(ConfigOption.TYPE.UNICODE_CONFIG, roleKey,
				"which role should determine this users configuration", false, false, selectedValuesRole,
				possibleValuesRole);
	}

	private void supplyPermissionEntriesForAUser(final String username, final String startKey,
			final boolean prototypeObligatory, final UserConfig prototypeConfig, UserConfig userConfig) {
		Logging.info(this, "supplyPermissionEntriesForAUser for user " + username + " with startkey " + startKey);
		Logging.info(this, "supplyPermissionEntriesForAUser for user, prototypeConfig " + prototypeConfig);

		int countReadyObjectsOnStart = readyObjects.size();

		Logging.info(this,
				"supplyPermissionEntriesForAUser UserConfig.getUserBoolKeys( " + UserConfig.getUserBoolKeys());

		updateUserConfigBooleanValues(userConfig, prototypeConfig, prototypeObligatory, startKey);

		Logging.info(this, "supplyPermissionEntriesForAUser, readyObjects bool keys for user named " + username + " "
				+ readyObjects);
		Logging.info(this, "supplyPermissionEntriesForAUser UserConfig.getUserStringValueKeys "
				+ UserConfig.getUserStringValueKeys());
		Logging.info(this, "supplyPermissionEntriesForAUser UserConfig.getUserStringValueKeys_withoutRole "
				+ UserConfig.getUserStringValueKeysWithoutRole());

		// role entry, will be removed for the next run, if not obligatory
		if (!prototypeObligatory) {
			String configKey = startKey + UserConfig.HAS_ROLE_ATTRIBUT;
			Logging.info(this, "configkey " + configKey);
			List<Object> values = serverconfigValuesMap.get(configKey);

			if (values == null || values.isEmpty() || values.get(0) == null
					|| !((String) values.get(0)).equals(UserConfig.NONE_PROTOTYPE)) {
				Logging.info(this,
						"supplyPermissionEntriesForAUser. serverconfigValuesMap has no value for key " + configKey);

				List<Object> selectedValuesRole = new ArrayList<>();
				selectedValuesRole.add(UserConfig.NONE_PROTOTYPE);

				Map<String, Object> itemRole = Utils.createNOMConfig(ConfigOption.TYPE.UNICODE_CONFIG, configKey,
						"which role should determine this users configuration", false, false, selectedValuesRole,
						selectedValuesRole);
				readyObjects.add(itemRole);
			}
		}

		updateUserConfigUserStringValuesWithoutKeyRoles(userConfig, prototypeConfig, prototypeObligatory, startKey);

		Logging.info(this,
				"supplyPermissionEntriesForAUser UserConfig.getUserListKeys( " + UserConfig.getUserListKeys());
		Logging.info(this, "supplyPermissionEntriesForAUser  user config " + userConfig);
		Logging.info(this,
				"supplyPermissionEntriesForAUser, readyObjects list keys for " + username + " " + readyObjects.size());
		Logging.info(this, "supplyPermissionEntriesForAUser UserConfig " + userConfig);

		updateConfigListItem(UserServerConsoleConfig.KEY_TERMINAL_ACCESS_FORBIDDEN,
				UserServerConsoleConfig.FORBIDDEN_OPTIONS, userConfig, prototypeConfig, prototypeObligatory, startKey);
		updateDepots(userConfig, prototypeConfig, prototypeObligatory, startKey);
		updateHostGroups(userConfig, prototypeConfig, prototypeObligatory, startKey, username);
		updateProductGroups(userConfig, prototypeConfig, prototypeObligatory, startKey);

		Logging.info(this, "supplyPermissionEntriesForAUser username " + username);
		Logging.info(this, "supplyPermissionEntriesForAUser countReadyObjectsOnStart " + countReadyObjectsOnStart);
		Logging.info(this, "supplyPermissionEntriesForAUser readyObjects.size() " + readyObjects.size());

		if (countReadyObjectsOnStart == readyObjects.size()) {
			Logging.info(this,
					"supplyPermissionEntriesForAUser added no object(s) for saving, for username " + username);
		} else {
			Logging.info(this, "supplyPermissionEntriesForAUser added object(s) for saving, for username " + username
					+ ": " + (readyObjects.size() - 1));
			List<Object> timeVal = Utils.getNowTimeListValue("set by role prototype");

			Map<String, Object> itemModifyTime = Utils.createNOMitem(ConfigOption.UNICODE_TYPE);
			itemModifyTime.put("ident", startKey + UserConfig.MODIFICATION_INFO_KEY);
			itemModifyTime.put("editable", false);
			itemModifyTime.put("multiValue", false);
			itemModifyTime.put("description", "last modification time for entries of this user");
			itemModifyTime.put("defaultValues", timeVal);
			itemModifyTime.put("possibleValues", timeVal);

			Logging.info(this, "modi time " + itemModifyTime);
			readyObjects.add(itemModifyTime);
		}
	}

	private void updateDepots(UserConfig userConfig, UserConfig prototypeConfig, boolean prototypeObligatory,
			String startKey) {
		List<Object> selectedValuesDepot = null;
		List<Object> possibleValuesDepot = null;
		Set<Object> oldPossibleValuesDepot = null;
		Set<Object> currentPossibleValuesDepotListed = null;

		String configKeyUseList = startKey + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;
		String partkey = UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;
		String configKeyList = startKey + partkey;

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
				"updateDepots currentPossibleValuesDepotListed before supplying " + currentPossibleValuesDepotListed);

		supplyConfigPermissionList(configKeyUseList, defaultvalueForRestrictionUsage, configKeyList,
				selectedValuesDepot, oldPossibleValuesDepot, currentPossibleValuesDepotListed);
	}

	private void updateHostGroups(UserConfig userConfig, UserConfig prototypeConfig, boolean prototypeObligatory,
			String startKey, String username) {
		List<Object> selectedValuesHostgroup = null;
		List<Object> possibleValuesHostgroup = null;
		Set<Object> oldPossibleValuesHostgroup = null;
		Set<Object> currentPossibleValuesHostgroupListed = null;

		String configKeyUseList = startKey
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED;
		String partkey = UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE;
		String configKeyList = startKey + partkey;

		Logging.info(this, "configKeyUseList " + configKeyUseList + ", configKeyList " + configKeyList);

		boolean defaultvalueForRestrictionUsage = prototypeConfig
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

		Logging.info(this, "updateHostGroups selectedValuesHostgroup before supplying for " + username + ": "
				+ selectedValuesHostgroup);
		Logging.info(this, "updateHostGroups oldPossibleValuesHostgroupListed before supplying for " + username + ": "
				+ oldPossibleValuesHostgroup);
		Logging.info(this, "updateHostGroups possibleValuesHostgroup before supplying for " + username + ": "
				+ possibleValuesHostgroup);
		Logging.info(this, "updateHostGroups currentPossibleValuesHostgroupListed before supplying for " + username
				+ ": " + currentPossibleValuesHostgroupListed);

		supplyConfigPermissionList(configKeyUseList, defaultvalueForRestrictionUsage, configKeyList,
				selectedValuesHostgroup, oldPossibleValuesHostgroup, currentPossibleValuesHostgroupListed);
	}

	private void updateProductGroups(UserConfig userConfig, UserConfig prototypeConfig, boolean prototypeObligatory,
			String startKey) {
		List<Object> selectedValuesProductgroups = null;
		List<Object> possibleValuesProductgroups = null;
		Set<Object> oldPossibleValuesProductgroups = null;
		Set<Object> currentPossibleValuesProductgroupsListed = null;

		String configKeyUseList = startKey
				+ UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED;
		String partkey = UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE;
		String configKeyList = startKey + partkey;

		Logging.info(this, "configKeyUseList " + configKeyUseList + ", configKeyList " + configKeyList);

		boolean defaultvalueForRestrictionUsage = prototypeConfig
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

		Logging.info(this,
				"updateProductGroups selectedValuesProductgroups before supplying " + selectedValuesProductgroups);
		Logging.info(this, "updateProductGroups oldPossibleValuesProductgroupsListed before supplying "
				+ oldPossibleValuesProductgroups);
		Logging.info(this, "updateProductGroups currentPossibleValuesProductgroupsListed before supplying "
				+ currentPossibleValuesProductgroupsListed);

		supplyConfigPermissionList(configKeyUseList, defaultvalueForRestrictionUsage, configKeyList,
				selectedValuesProductgroups, oldPossibleValuesProductgroups, currentPossibleValuesProductgroupsListed);
	}

	private void updateConfigListItem(String partkey, List<Object> possibleValues, UserConfig userConfig,
			UserConfig prototypeConfig, boolean prototypeObligatory, String startKey) {
		// Methode zum initialisieren von Listenwerten
		// analog zu updateProductGroups, ignoriert aber das configure-config 
		// und setzt übergegebene possibleValues

		List<Object> selectedValuesProductgroups = null;
		List<Object> possibleValuesProductgroups = null;
		Set<Object> oldPossibleValuesProductgroups = null;
		Set<Object> currentPossibleValuesProductgroupsListed = null;

		String configKeyConfigured = null;
		String configKeyList = startKey + partkey;

		Logging.info(this, "updateTerminalConfig, configKeyConfigured " + configKeyConfigured + ", configKeyList "
				+ configKeyList);

		boolean defaultvalueForRestrictionUsage = false;

		if (prototypeObligatory || serverconfigValuesMap.get(configKeyList) == null) {
			selectedValuesProductgroups = prototypeConfig.getValues(partkey);
			Logging.info(this,
					"updateTerminalConfig, selectedValuesProductgroups from prototype " + selectedValuesProductgroups);
		} else {
			selectedValuesProductgroups = serverconfigValuesMap.get(configKeyList);
			Logging.info(this,
					"updateTerminalConfig, selectedValuesProductgroups from server " + selectedValuesProductgroups);
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
			Set<Object> posVals = new TreeSet<>(possibleValues);
			currentPossibleValuesProductgroupsListed.addAll(posVals);
		}

		userConfig.setPossibleValues(partkey, new ArrayList<>(currentPossibleValuesProductgroupsListed));

		Logging.info(this,
				"updateProductGroups selectedValuesProductgroups before supplying " + selectedValuesProductgroups);
		Logging.info(this, "updateProductGroups oldPossibleValuesProductgroupsListed before supplying "
				+ oldPossibleValuesProductgroups);
		Logging.info(this, "updateProductGroups currentPossibleValuesProductgroupsListed before supplying "
				+ currentPossibleValuesProductgroupsListed);

		supplyConfigPermissionList(configKeyConfigured, defaultvalueForRestrictionUsage, configKeyList,
				selectedValuesProductgroups, oldPossibleValuesProductgroups, currentPossibleValuesProductgroupsListed);
	}

	private void updateUserConfigBooleanValues(UserConfig userConfig, UserConfig prototypeConfig,
			boolean prototypeObligatory, String startKey) {
		for (String partKey : UserConfig.getUserBoolKeys()) {
			String configKey = startKey + partKey;
			List<Object> values = serverconfigValuesMap.get(configKey);
			Boolean value = null;

			Logging.info(this, "fillUserConfigBooleanValues bool configKey " + configKey + " -- partkey " + partKey);
			Logging.info(this, "fillUserConfigBooleanValues bool configKey has values " + values);

			// there is no formally correct value.
			// the specific values differs from prototype values and must be corrected.
			if (containsValidBoolean(values, prototypeObligatory, prototypeConfig.getBooleanValue(partKey))) {
				Logging.info(this,
						"fillUserConfigBooleanValues serverconfigValuesMap has no value for key " + configKey);

				value = prototypeConfig.getBooleanValue(partKey);
				userConfig.setBooleanValue(partKey, value);

				Map<String, Object> item = Utils.createNOMBoolConfig(configKey, value,
						"the primary value setting is based on the user group");
				readyObjects.add(item);
			} else {
				value = (Boolean) values.get(0);
			}

			userConfig.setBooleanValue(partKey, value);
		}
	}

	private void updateUserConfigUserStringValuesWithoutKeyRoles(UserConfig userConfig, UserConfig prototypeConfig,
			boolean prototypeObligatory, String startKey) {
		for (String partkey : UserConfig.getUserStringValueKeysWithoutRole()) {
			String configKey = startKey + partkey;
			List<Object> values = serverconfigValuesMap.get(configKey);

			Logging.info(this, "updateUserConfigUserStringValuesWithoutKeyRoles configKey " + configKey + " -- partkey "
					+ partkey);
			Logging.info(this, "updateUserConfigUserStringValuesWithoutKeyRoles configKey has size 1 values " + values);

			// We don't want to change the 'modified' value based on userroles (when prototypeObligatory is true)
			if (values == null || (prototypeObligatory && !partkey.equals(UserConfig.MODIFICATION_INFO_KEY)
					&& !(values.equals(prototypeConfig.getValues(partkey))))) {
				Logging.info(this,
						"updateUserConfigUserStringValuesWithoutKeyRoles serverconfigValuesMap gives not valid value for key "
								+ configKey);

				values = prototypeConfig.getValues(partkey);
				userConfig.setValues(partkey, values);

				Map<String, Object> item = Utils.createNOMConfig(ConfigOption.TYPE.UNICODE_CONFIG, configKey, configKey,
						false, false, values, values);
				readyObjects.add(item);
			}
		}
	}

	private static boolean containsValidBoolean(List<Object> values, boolean prototypeObligatory,
			Boolean prototypeConfigValue) {
		if (values == null || values.isEmpty() || !(values.get(0) instanceof Boolean)) {
			return true;
		}

		return prototypeObligatory && !values.get(0).equals(prototypeConfigValue);
	}
}
