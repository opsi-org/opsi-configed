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
import de.uib.configed.configed;
import de.uib.configed.type.ConfigOption;
import de.uib.opsicommand.Executioner;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;

public class UserConfigProducing {
	boolean notUsingDefaultUser;

	String configserver;
	Collection<String> existingDepots;
	Collection<String> existingHostgroups;
	Collection<String> existingProductgroups;

	Map<String, List<Object>> serverconfigValuesMap;
	Map<String, de.uib.utilities.table.ListCellOptions> configOptionsMap;

	ArrayList<Object> readyObjects;
	

	public UserConfigProducing(
			// command tools

			// data used for configuring
			boolean notUsingDefaultUser,

			String configserver,

			Collection<String> existingDepots, Collection<String> existingHostgroups,
			Collection<String> existingProductgroups,

			// data. on which changes are based
			Map<String, List<Object>> serverconfigValuesMap,
			Map<String, de.uib.utilities.table.ListCellOptions> configOptionsMap

	) {
		this.notUsingDefaultUser = notUsingDefaultUser;
		this.configserver = configserver;

		this.existingDepots = existingDepots;
		this.existingHostgroups = existingHostgroups;
		this.existingProductgroups = existingProductgroups;

		this.serverconfigValuesMap = serverconfigValuesMap;
		this.configOptionsMap = configOptionsMap;

		logging.info(this, "create with existing collections depots, hostgroups, productgroups " + existingDepots.size()
				+ " - " + existingHostgroups.size() + " - " + existingProductgroups.size());
	}

	public ArrayList<Object> produce() {
		readyObjects = new ArrayList<>();

		java.util.Set<String> userparts = new TreeSet<>();
		java.util.Set<String> roleparts = new TreeSet<>();
		produceRoleAndUserParts(userparts, roleparts);

		if (ConfigedMain.USER == null) {
			ConfigedMain.USER = configed.user;
		}

		logging.info(this, "we have got logged in user " + ConfigedMain.USER + " and configure based on it "
				+ notUsingDefaultUser);

		if (notUsingDefaultUser && ConfigedMain.USER != null && !userparts.contains(ConfigedMain.USER)) {
			logging.info(this, "supply logged in user");
			userparts.add(ConfigedMain.USER);
			String propertyclass = UserConfig.START_USER_KEY + ConfigedMain.USER + '}';

			if (!PersistenceController.PROPERTYCLASSES_SERVER.containsKey(propertyclass)) {
				logging.info(this, "createUserPropertySubclass for logged in user " + ConfigedMain.USER);
				PersistenceController.PROPERTYCLASSES_SERVER.put(propertyclass, "");
			}

		}

		supplyAllPermissionEntries(userparts, roleparts);

		return readyObjects;
	}

	/*
	 * public UserConfig getUserConfig()
	 * {
	 * return new UserConfig (userToAdd);
	 * }
	 */

	private void produceRoleAndUserParts(java.util.Set<String> userNames, java.util.Set<String> roleNames) {
		logging.info(this, "produceRoleAndUserParts for " + userNames + " resp. " + roleNames);
		// String generalRoleKey = defaultRolePart();
		final String roleBranchPart = UserConfig.KEY_USER_ROLE_ROOT;
		final String startRoleKey = roleBranchPart + ".{";

		for (String key : serverconfigValuesMap.keySet()) {
			if (!(key.startsWith(UserConfig.KEY_USER_ROOT)))
				continue;

			
			// UserConfig.KEY_USER_ROOT " + key);
			if (key.startsWith(roleBranchPart)) {
				String rolenameBefore = key.substring(0, startRoleKey.length());
				String rolename = key.substring(rolenameBefore.length());
				int lenOfRoleName = rolename.indexOf("}");

				if (lenOfRoleName > 0) {
					rolename = rolename.substring(0, lenOfRoleName);

					logging.info(this, "role branch with rolename " + rolename);

					roleNames.add(rolename);

					String propertyclass = startRoleKey + rolename + '}';

					

					if (!PersistenceController.PROPERTYCLASSES_SERVER.containsKey(propertyclass)) {
						logging.info(this, "createRolePropertySubclass for role  " + rolename);
						PersistenceController.PROPERTYCLASSES_SERVER.put(propertyclass, "");
					}

				} else {
					logging.warning(this, "rolePart without proper rolename found " + key);
				}
			} else {

				if (key.startsWith(UserConfig.ALL_USER_KEY_START)) {
					logging.info(this,
							"not delivered in this collection " + key.substring(UserConfig.START_USER_KEY.length()));
					// not delivered in this collection
				} else {
					String username = UserConfig.getUserFromKey(key);
					logging.debug(this, "produceRoleAndUserParts userpart start  " + username);

					if (username != null) {
						userNames.add(username);
						logging.debug(this, "usernames, add " + username + " for key " + key);
						String propertyclass = UserConfig.START_USER_KEY + username + '}';

						if (!PersistenceController.PROPERTYCLASSES_SERVER.containsKey(propertyclass)) {

							logging.info(this, "createUserPropertySubclass for user  " + username);
							PersistenceController.PROPERTYCLASSES_SERVER.put(propertyclass, "");
						}

					} else
						logging.warning(this, "username not specified in key " + key);

				}

			}
		}

		logging.info(this, "all roleNames " + roleNames);
		logging.info(this, "all userNames " + userNames);
	}

	private void supplyConfigPermissionList(final String configKeyUseList, final boolean initialValue, // activate this feature by default?
			final String configKeyList, final List<Object> selectedValues, final Set<Object> oldPossibleValues,
			final Set<Object> currentPossibleValuesListed)
	// produces readyObjects for this configKey(List)
	{
		logging.info(this, "supplyConfigPermissionList, configKeyUseList: " + configKeyUseList);
		Map<String, Object> item = null; // item variable for adding items to readyObjects

		logging.info(this, "supplyConfigPermissionList  configKey " + configKeyUseList);

		if (serverconfigValuesMap.get(configKeyUseList) == null) {
			logging.info(this, "supplyPermissionList. serverconfigValuesMap has no value for key " + configKeyUseList);

			item = PersistenceController.createJSONBoolConfig(configKeyUseList, initialValue,
					"the primary value setting is " + initialValue);
			readyObjects.add(Executioner.jsonMap(item));
		}

		logging.info(this, "supplyPermissionList  configKey " + configKeyList);

		logging.debug(this, "serverconfigValuesMap.get( configKeyList ) " + serverconfigValuesMap.get(configKeyList));
		logging.debug(this, "selectedValues  " + selectedValues);

		logging.debug(this, "currentPossibleValuesListed " + currentPossibleValuesListed);
		logging.debug(this, "oldPossibleValues " + oldPossibleValues);

		if ((serverconfigValuesMap.get(configKeyList) == null)
				|| !serverconfigValuesMap.get(configKeyList).equals(selectedValues)
				|| !currentPossibleValuesListed.equals(oldPossibleValues)) // includes the case that there was no value
																																																		// for the configKey at all
		{
			logging.info(this, "supplyPermissionList initialization or change");

			logging.info(this,
					"supplyPermissionList add to currentPossibleValuesListed " + currentPossibleValuesListed);
			ArrayList<Object> listOptions = new ArrayList<>(currentPossibleValuesListed);

			logging.info(this, "supplyPermissionList products arraylist " + listOptions);

			item = PersistenceController.createNOMitem(ConfigOption.UNICODE_TYPE);

			item.put("ident", configKeyList);
			item.put("editable", false);
			item.put("multiValue", true);

			item.put("description",
					"the primary value setting is an empty selection list, but all existing items as option");
			item.put("defaultValues", Executioner.jsonArray(selectedValues));
			item.put("possibleValues", Executioner.jsonArray(listOptions));

			readyObjects.add(Executioner.jsonMap(item));
		}

	}

	/** we call up the cascade of default role, other roles, and the users */
	protected void supplyAllPermissionEntries(java.util.Set<String> userParts, java.util.Set<String> roleParts) {
		logging.info(this, "supplyAllPermissionEntries start");

		// separateServerConfigsTreeSection( UserConfig.KEY_USER_ROOT + "." +
		
		// separateServerConfigsTreeSection( userPart().substring( 0,
		// userPart().length()-1 ) );

		logging.info(this, "supplyAllPermissionEntries all roles " + roleParts);
		logging.info(this, "supplyAllPermissionEntries first for default role,  " + UserConfig.DEFAULT_ROLE_NAME);

		// (1) the default role
		logging.info(this, "supplyAllPermissionEntries UserConfig.getArcheoConfig( " + UserConfig.getArcheoConfig());

		UserConfig defaultUserConfig = new UserConfig(UserConfig.DEFAULT_ROLE_NAME);
		String rolenameStartkey = UserConfig.KEY_USER_ROOT + "." + UserConfig.ROLE + ".{" + UserConfig.DEFAULT_ROLE_NAME
				+ "}.";
		supplyPermissionEntriesForAUser(UserConfig.DEFAULT_ROLE_NAME, rolenameStartkey, false,
				UserConfig.getArcheoConfig(), defaultUserConfig);

		logging.info(this, "supplyAllPermissionEntries defaultUserConfig " + defaultUserConfig);

		// ---System.exit(0);

		Map<String, UserConfig> roleConfigs = new HashMap<>();
		Map<String, UserConfig> userConfigs = new HashMap<>();

		roleConfigs.put(UserConfig.DEFAULT_ROLE_NAME, defaultUserConfig);

		Set<String> extraRoleParts = new HashSet<>(roleParts);
		extraRoleParts.remove(UserConfig.DEFAULT_ROLE_NAME);

		logging.info(this, "supplyAllPermissionEntries extraRoleParts " + extraRoleParts);

		for (String rolename : extraRoleParts) {
			rolenameStartkey = UserConfig.KEY_USER_ROOT + "." + UserConfig.ROLE + ".{" + rolename + "}.";
			
			UserConfig roleConfig = new UserConfig(rolename);
			supplyPermissionEntriesForAUser(rolename, rolenameStartkey, false, defaultUserConfig, roleConfig);

			roleConfigs.put(rolename, roleConfig);
		}

		logging.info(this, "supplyAllPermissionEntries roleConfigs.size() " + roleConfigs.size());
		logging.info(this, "supplyAllPermissionEntries readyObjects for roleparts " + readyObjects.size());
		logging.info(this, "supplyAllPermissionEntries for userparts " + userParts);

		for (String username : userParts)
		// String username = "admindepot1"; //testing
		{
			String roleToPlay = UserConfig.DEFAULT_ROLE_NAME;
			String usernameStartkey = UserConfig.KEY_USER_ROOT + ".{" + username + "}.";
			UserConfig userConfig = new UserConfig(username);
			userConfigs.put(username, userConfig);
			String roleKey = usernameStartkey + UserConfig.HAS_ROLE_ATTRIBUT;

			logging.info(this,
					"supplyAllPermissionEntries usernameStartkey " + usernameStartkey + " roleKey " + roleKey);

			List<Object> values = serverconfigValuesMap.get(roleKey);

			logging.info(this,
					"supplyAllPermissionEntries got values " + values + " for role from serverconfigValuesMap ");

			String configuredRole = null;
			boolean followConfiguredRole = false;
			logging.info(this, "supplyAllPermissionEntries has role " + values);
			
			
			
			// values.get(0) ).equals( UserConfig.NONE_PROTOTYPE) ));

			if (values == null || values.isEmpty() // || !(values.get(0) instanceof String )
					|| (((String) values.get(0)).equals(UserConfig.NONE_PROTOTYPE))) {
				logging.info(this, "no role specified for user " + username);
			} else {
				// we have got some value
				configuredRole = "" + values.get(0);

				logging.info(this, "supplyAllPermissionEntries configuredRole " + configuredRole);
				logging.info(this, "supplyAllPermissionEntries roleConfigs " + roleConfigs);

				if (roleConfigs.containsKey(configuredRole)) {
					roleToPlay = configuredRole;
					followConfiguredRole = true;
				}
			}

			logging.info(this,
					" for user " + username + " followConfiguredRole " + followConfiguredRole + ": " + roleToPlay);

			// update role selection
			ArrayList<Object> selectedValuesRole = new ArrayList<>();
			if (configuredRole != null)
				selectedValuesRole.add(configuredRole);
			else
				selectedValuesRole.add(UserConfig.NONE_PROTOTYPE);

			Set<String> possibleValuesSet = new HashSet<>(roleParts);
			if (configuredRole != null)
				possibleValuesSet.add(configuredRole);
			possibleValuesSet.add(UserConfig.NONE_PROTOTYPE);

			ArrayList<Object> possibleValuesRole = new ArrayList<>(possibleValuesSet);

			Map<String, Object> itemRole = PersistenceController.createJSONConfig(ConfigOption.TYPE.UnicodeConfig,
					roleKey, "which role should determine this users configuration", false, // editable
					false, // multivalue
					selectedValuesRole, // defaultValues enry
					possibleValuesRole);

			logging.info(this, "supplyAllPermissionEntries possibleValuesRole, roleParts " + " " + possibleValuesRole
					+ ", " + roleParts);

			// if( !( possibleValuesSet.equals( roleParts ) ) )
			readyObjects.add(Executioner.jsonMap(itemRole));

			UserConfig roleConfig = roleConfigs.get(roleToPlay);
			// is defaultConfig if roleToPlay does not exist

			supplyPermissionEntriesForAUser(username, usernameStartkey, followConfiguredRole, roleConfig, userConfig);
		}

		logging.info(this, "readyObjects for userparts " + readyObjects.size());

		if (notUsingDefaultUser)
			UserConfig.setCurrentConfig(userConfigs.get(ConfigedMain.USER));
		else
			UserConfig.setCurrentConfig(defaultUserConfig);

	}

	private void supplyPermissionEntriesForAUser(final String username, final String startkey,
			final Boolean prototypeObligatory, final UserConfig prototypeConfig, UserConfig userConfig) {
		logging.info(this, "supplyPermissionEntriesForAUser for user " + username + " with startkey " + startkey);
		logging.info(this, "supplyPermissionEntriesForAUser for user, prototypeConfig " + prototypeConfig);

		int countReadyObjectsOnStart = readyObjects.size();

		Map<String, Object> item = null;

		String configKey = null;

		// Boolean valued

		logging.info(this,
				"supplyPermissionEntriesForAUser UserConfig.getUserBoolKeys( " + UserConfig.getUserBoolKeys());

		for (String partkey : UserConfig.getUserBoolKeys())
		// includes at the moment ssh permission keys and opsi permission keys
		{
			configKey = startkey + partkey;
			// String configKey = startkey +
			// UserOpsipermission.PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY ;
			logging.info(this,
					"supplyPermissionEntriesForAUser boolean configKey " + configKey + " -- partkey " + partkey);

			List<Object> values = serverconfigValuesMap.get(configKey);
			Boolean value = null;

			logging.info(this,
					"supplyPermissionEntriesForAUser bool configKey " + configKey + " -- partkey " + partkey);
			logging.info(this, "supplyPermissionEntriesForAUser bool configKey has values " + values);

			if (values == null || values.isEmpty() || !(values.get(0) instanceof Boolean)
			// there is no formally correct value)
					|| (prototypeObligatory
							&& !((Boolean) values.get(0).equals(prototypeConfig.getBooleanValue(partkey)))
					// the specific values differs from prototype values and must be corrected
					))

			{
				logging.info(this,
						"supplyPermissionEntriesForAUser. serverconfigValuesMap has no value for key " + configKey);
				value = prototypeConfig.getBooleanValue(partkey);

				userConfig.setBooleanValue(partkey, value);
				item = PersistenceController.createJSONBoolConfig(configKey, value,
						"the primary value setting is based on the user group");

				readyObjects.add(Executioner.jsonMap(item));
			} else
				value = (Boolean) values.get(0);

			userConfig.setBooleanValue(partkey, value);
		}

		logging.info(this, "supplyPermissionEntriesForAUser, readyObjects bool keys for user named " + username + " "
				+ readyObjects);

		
		

		// single String valued
		logging.info(this, "supplyPermissionEntriesForAUser UserConfig.getUserStringValueKeys "
				+ UserConfig.getUserStringValueKeys());
		logging.info(this, "supplyPermissionEntriesForAUser UserConfig.getUserStringValueKeys_withoutRole "
				+ UserConfig.getUserStringValueKeys_withoutRole());

		// role entry, will be removed for the next run, if not obligatory
		if (!prototypeObligatory) {
			configKey = startkey + UserConfig.HAS_ROLE_ATTRIBUT;
			logging.info(this, "configkey " + configKey);
			List<Object> values = serverconfigValuesMap.get(configKey);

			if (values == null || values.isEmpty() || !((String) values.get(0)).equals(UserConfig.NONE_PROTOTYPE)) {
				ArrayList<Object> selectedValuesRole = new ArrayList<>();
				selectedValuesRole.add(UserConfig.NONE_PROTOTYPE);

				Map<String, Object> itemRole = PersistenceController.createJSONConfig(ConfigOption.TYPE.UnicodeConfig,
						configKey, "which role should determine this users configuration", false, // editable
						false, // multivalue
						selectedValuesRole, // defaultValues enry
						selectedValuesRole);

				readyObjects.add(Executioner.jsonMap(itemRole));
			}
		}

		for (String partkey : UserConfig.getUserStringValueKeys_withoutRole()) {

			configKey = startkey + partkey;
			// String configKey = startkey +
			// UserOpsipermission.PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY ;

			List<Object> values = serverconfigValuesMap.get(configKey);

			logging.info(this, "supplyPermissionEntriesForAUser configKey " + configKey + " -- partkey " + partkey);
			logging.info(this, "supplyPermissionEntriesForAUser configKey has size 1 values " + values);

			if (values == null

					|| (prototypeObligatory && !(values.equals(prototypeConfig.getValues(partkey))))

			) {
				logging.info(this,
						"supplyPermissionEntriesForAUser. serverconfigValuesMap gives not valid value for key "
								+ configKey);
				values = prototypeConfig.getValues(partkey);

				userConfig.setValues(partkey, values);

				item = PersistenceController.createJSONConfig(ConfigOption.TYPE.UnicodeConfig, configKey, configKey, // description
						false, // editable
						false, // multivalue
						values, values // possibleValues

				);

				readyObjects.add(Executioner.jsonMap(item));

			}
			
			// configKey + " -- partkey " + partkey);
			
		}

		// Stringlist valued

		logging.info(this,
				"supplyPermissionEntriesForAUser UserConfig.getUserListKeys( " + UserConfig.getUserListKeys());

		/*
		 * for( String partkey : UserConfig.getUserListKeys() )
		 * //handles for opsi permission keys the associated lists, i.e. the
		 * selectedValues
		 * //mulitvalue here true!
		 * {
		 * configKey = startkey + partkey;
		 * // String configKey = startkey +
		 * UserOpsipermission.PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY ;
		 * 
		 * 
		 * List<Object> values = serverconfigValuesMap.get( configKey );
		 * 
		 * logging.info(this, "supplyPermissionEntriesForAUser list configKey " +
		 * configKey + " -- partkey " + partkey);
		 * logging.info(this,
		 * "supplyPermissionEntriesForAUser list configKey has values " + values);
		 * logging.info(this, "supplyPermissionEntriesForAUser list prototype values " +
		 * prototypeConfig.getValues( partkey ) );
		 * 
		 * 
		 * if (
		 * values == null
		 * 
		 * || (prototypeObligatory && !( values.equals( prototypeConfig.getValues(
		 * partkey ) ) )
		 * )
		 * 
		 * )
		 * 
		 * {
		 * logging.info(this,
		 * "supplyPermissionEntriesForAUser. serverconfigValuesMap gives not valid value for key "
		 * + configKey);
		 * values = prototypeConfig.getValues( partkey );
		 * 
		 * item = PersistenceController.createJSONConfig(
		 * ConfigOption.TYPE.UnicodeConfig,
		 * configKey,
		 * configKey, //description
		 * false, //editable
		 * true, //multivalue
		 * values,
		 * values //possibleValues
		 * 
		 * );
		 * 
		 * 
		 * readyObjects.add( Executioner.jsonMap(item) ) ;
		 * 
		 * }
		 * 
		 * userConfig.setValues ( partkey, values );
		 * 
		 * configKey + " -- partkey " + partkey);
		 * 
		 * }
		 */

		logging.info(this, "supplyPermissionEntriesForAUser  user config " + userConfig);

		logging.info(this,
				"supplyPermissionEntriesForAUser, readyObjects list keys for " + username + " " + readyObjects.size());
		logging.info(this, "supplyPermissionEntriesForAUser UserConfig " + userConfig);

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

		logging.info(this, "configKeyUseList " + configKeyUseList + ", configKeyList " + configKeyList);
		possibleValuesDepot = new ArrayList<>();
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
			// oldPossibleValues = new TreeSet<>();
		}

		logging.info(this, "oldPossibleValuesDepot " + oldPossibleValuesDepot);

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

		logging.info(this,
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
		LinkedHashSet<Object> currentPossibleValuesHostgroupListed = null;

		configKeyUseList = startkey + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED;
		partkey = UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE;
		configKeyList = startkey + partkey;

		logging.info(this, "configKeyUseList " + configKeyUseList + ", configKeyList " + configKeyList);

		defaultvalueForRestrictionUsage = prototypeConfig
				.getBooleanValue(UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED);

		currentPossibleValuesHostgroupListed = new LinkedHashSet<>();
		possibleValuesHostgroup = new ArrayList<>();

		if (prototypeObligatory || serverconfigValuesMap.get(configKeyList) == null) {
			selectedValuesHostgroup = prototypeConfig.getValues(partkey);
		} else {
			selectedValuesHostgroup = serverconfigValuesMap.get(configKeyList);
		}

		logging.info(this, "selectedValuesHostgroup for user " + username + ": " + selectedValuesHostgroup);

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

		logging.info(this,
				"hostGroups selectedValuesHostgroup before supplying for " + username + ": " + selectedValuesHostgroup);
		logging.info(this, "hostGroups oldPossibleValuesHostgroupListed before supplying for " + username + ": "
				+ oldPossibleValuesHostgroup);
		logging.info(this,
				"hostgroups possibleValuesHostgroup before supplying for " + username + ": " + possibleValuesHostgroup);
		logging.info(this, "hostgroups currentPossibleValuesHostgroupListed before supplying for " + username + ": "
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

		logging.info(this, "configKeyUseList " + configKeyUseList + ", configKeyList " + configKeyList);

		defaultvalueForRestrictionUsage = prototypeConfig
				.getBooleanValue(UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED);

		currentPossibleValuesProductgroupsListed = new LinkedHashSet<>();
		possibleValuesProductgroups = new ArrayList<>();

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

		logging.info(this, "productGroups selectedValuesProductgroups before supplying " + selectedValuesProductgroups);
		logging.info(this, "productGroups oldPossibleValuesProductgroupsListed before supplying "
				+ oldPossibleValuesProductgroups);
		logging.info(this, "productGroups currentPossibleValuesProductgroupsListed before supplying "
				+ currentPossibleValuesProductgroupsListed);

		supplyConfigPermissionList(configKeyUseList, // final
				// activate this feature by default?
				defaultvalueForRestrictionUsage, configKeyList, // final
				selectedValuesProductgroups, // final
				oldPossibleValuesProductgroups, // final
				currentPossibleValuesProductgroupsListed);

		logging.info(this, "supplyPermissionEntriesForAUser username " + username);
		logging.info(this, "supplyPermissionEntriesForAUser countReadyObjectsOnStart " + countReadyObjectsOnStart);
		logging.info(this, "supplyPermissionEntriesForAUser readyObjects.size() " + readyObjects.size());
		
		

		if (countReadyObjectsOnStart == readyObjects.size()) {
			logging.info(this,
					"supplyPermissionEntriesForAUser added no object(s) for saving, for username " + username);
		} else {
			logging.info(this, "supplyPermissionEntriesForAUser added object(s) for saving, for username " + username
					+ ": " + (readyObjects.size() - 1));
			List<Object> timeVal = Globals.getNowTimeListValue("set by role prototype");

			/*
			 * ConfigOption itemModifyTime = PersistenceController.createConfig(
			 * ConfigOption.TYPE.UnicodeConfig,
			 * startkey + UserConfig.MODIFICATION_INFO_KEY,
			 * "last modification time for entries of this user",
			 * false, false,
			 * timeVal,
			 * timeVal
			 * );
			 * 
			 */
			Map<String, Object> itemModifyTime = PersistenceController.createNOMitem(ConfigOption.UNICODE_TYPE);

			itemModifyTime.put("ident", startkey + UserConfig.MODIFICATION_INFO_KEY);
			itemModifyTime.put("editable", false);
			itemModifyTime.put("multiValue", false);

			itemModifyTime.put("description", "last modification time for entries of this user");

			itemModifyTime.put("defaultValues", timeVal);
			itemModifyTime.put("possibleValues", timeVal);

			logging.info(this, "modi time " + itemModifyTime);

			// --System.exit(0);

			readyObjects.add(Executioner.jsonMap(itemModifyTime));
		}

	}

	public static void main(String[] args) {
		String theServer = "";

		logging.setSuppressConsole(false);
		logging.debug("UserConfigProducing");

		PersistenceController persist = PersistenceControllerFactory.getNewPersistenceController(theServer, "user",
				"test");

		UserConfigProducing up = new UserConfigProducing(false, // boolean notUsingDefaultUser,

				theServer, // String configserver,
				persist.getHostInfoCollections().getDepotNamesList(), // Collection<String> existingDepots,
				persist.getHostGroupIds(), // Collection<String> existingHostgroups,
				persist.getProductGroups().keySet(), // Collection<String> existingProductgroups,

				// data. on which changes are based
				persist.getConfigDefaultValues(), // Map<String, List<Object>> serverconfigValuesMap,
				persist.getConfigOptions()// Map<String, de.uib.utilities.table.ListCellOptions> configOptionsMap

		);

		ArrayList<Object> newData = up.produce();
		logging.debug("UserConfigProducing: newData " + newData);

	}

}
