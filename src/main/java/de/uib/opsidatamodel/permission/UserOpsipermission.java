package de.uib.opsidatamodel.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.uib.utilities.logging.Logging;

public class UserOpsipermission extends UserConfigModule {

	// permit restrictable action

	public enum ActionPrivilege {
		READ_ONLY, READ_WRITE
	}

	private static final String CONFIG_KEY_STR_HOST = "host";
	private static final String CONFIG_KEY_STR_PRODUCT = "product";
	private static final String CONFIG_KEY_STR_PRIVILEGE = "privilege";
	private static final String CONFIG_KEY_STR_DEPOT = "depotaccess";
	private static final String CONFIG_KEY_STR_DEPOTLIST = "depots";
	private static final String CONFIG_KEY_STR_HOSTGROUP = "groupaccess";
	private static final String CONFIG_KEY_STR_HOSTGROUPLIST = "hostgroups";
	private static final String CONFIG_KEY_STR_PRODUCTGROUP = "groupaccess";
	private static final String CONFIG_KEY_STR_PRODUCTGROUPLIST = "productgroups";
	private static final String CONFIG_KEY_STR_ACCESSCONTROLLED = "configured";
	private static final String CONFIG_KEY_STR_SERVER = "opsiserver";
	private static final String CONFIG_KEY_STR_READWRITE = "write";
	private static final String CONFIG_KEY_STR_ALLHOSTS = "all";
	private static final String CONFIG_KEY_STR_CREATECLIENT = "createclient";
	private static final String CONFIG_KEY_STR_READONLY = "registered_readonly";

	public static final String PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY = CONFIG_KEY_STR_PRIVILEGE + "."
			+ CONFIG_KEY_STR_HOST + "." + CONFIG_KEY_STR_ALLHOSTS + "." + CONFIG_KEY_STR_READONLY;
	// privilege.host.all.readonly : boolean
	public static final String PARTKEY_USER_PRIVILEGE_SERVER_READWRITE = CONFIG_KEY_STR_PRIVILEGE + "."
			+ CONFIG_KEY_STR_HOST + "." + CONFIG_KEY_STR_SERVER + "." + CONFIG_KEY_STR_READWRITE;
	// privilege.host.opsiserver.readwrite boolean

	public static final String PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED = CONFIG_KEY_STR_PRIVILEGE + "."
			+ CONFIG_KEY_STR_HOST + "." + CONFIG_KEY_STR_DEPOT + "." + CONFIG_KEY_STR_ACCESSCONTROLLED;

	public static final String PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE = CONFIG_KEY_STR_PRIVILEGE + "."
			+ CONFIG_KEY_STR_HOST + "." + CONFIG_KEY_STR_DEPOT + "." + CONFIG_KEY_STR_DEPOTLIST;
	// privilege.host.depotaccess.depots : multivalue

	public static final String PARTKEY_USER_PRIVILEGE_CREATECLIENT = CONFIG_KEY_STR_PRIVILEGE + "."
			+ CONFIG_KEY_STR_HOST + "." + CONFIG_KEY_STR_CREATECLIENT;

	public static final String PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED = CONFIG_KEY_STR_PRIVILEGE + "."
			+ CONFIG_KEY_STR_HOST + "." + CONFIG_KEY_STR_HOSTGROUP + "." + CONFIG_KEY_STR_ACCESSCONTROLLED;

	public static final String PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE = CONFIG_KEY_STR_PRIVILEGE + "."
			+ CONFIG_KEY_STR_HOST + "." + CONFIG_KEY_STR_HOSTGROUP + "." + CONFIG_KEY_STR_HOSTGROUPLIST;
	// privilege.host.groupaccess.hostgroups : multivalue

	public static final String PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED = CONFIG_KEY_STR_PRIVILEGE
			+ "." + CONFIG_KEY_STR_PRODUCT + "." + CONFIG_KEY_STR_PRODUCTGROUP + "." + CONFIG_KEY_STR_ACCESSCONTROLLED;

	public static final String PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE = CONFIG_KEY_STR_PRIVILEGE + "."
			+ CONFIG_KEY_STR_PRODUCT + "." + CONFIG_KEY_STR_PRODUCTGROUP + "." + CONFIG_KEY_STR_PRODUCTGROUPLIST;
	// privilege.product.groupaccess.productgroups : multivalue

	public static final Set<String> BOOL_KEYS;
	static {
		BOOL_KEYS = new LinkedHashSet<>();
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_SERVER_READWRITE);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_CREATECLIENT);

		Logging.info(" UserOpsipermission BOOL_KEYS " + BOOL_KEYS);

	}

	public static final Set<String> LIST_KEYS;
	private static final Map<String, String> CORRESPONDENCE_TO_LIST_KEYS;
	static {
		CORRESPONDENCE_TO_LIST_KEYS = new HashMap<>();
		LIST_KEYS = new HashSet<>();

		CORRESPONDENCE_TO_LIST_KEYS.put(PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED,
				PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE);
		CORRESPONDENCE_TO_LIST_KEYS.put(PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED,
				PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE);
		CORRESPONDENCE_TO_LIST_KEYS.put(PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED,
				PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE);

		for (String value : CORRESPONDENCE_TO_LIST_KEYS.values()) {
			LIST_KEYS.add(value);
		}
		Logging.info(" CORRESPONDENCE_TO_LIST_KEYS " + CORRESPONDENCE_TO_LIST_KEYS);

		Logging.info(" UserOpsipermission LIST_KEYS " + LIST_KEYS);

	}

	public static final UserOpsipermission DEFAULT;

	static {
		Logging.info("init ARCHEO for UserOpsipermission");
		DEFAULT = new UserOpsipermission(UserConfig.ARCHEO_ROLE_NAME);

		DEFAULT.setBooleanValue(PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY, false);
		DEFAULT.setBooleanValue(PARTKEY_USER_PRIVILEGE_SERVER_READWRITE, true);
		DEFAULT.setBooleanValue(PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED, false);
		DEFAULT.setBooleanValue(PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED, false);
		DEFAULT.setBooleanValue(PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED, false);
		DEFAULT.setBooleanValue(PARTKEY_USER_PRIVILEGE_CREATECLIENT, true);
		DEFAULT.setValues(PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE, new ArrayList<>());
		DEFAULT.setValues(PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE, new ArrayList<>());
		DEFAULT.setValues(PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE, new ArrayList<>());
		DEFAULT.setPossibleValues(PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE, new ArrayList<>());
		DEFAULT.setPossibleValues(PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE, new ArrayList<>());
		DEFAULT.setPossibleValues(PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE, new ArrayList<>());
	}

	public UserOpsipermission(String uname) {
		super(uname);
		Logging.info(this,
				"created for username " + uname + " with " + super.getBooleanMap() + " -- " + super.getValuesMap());
	}
}
