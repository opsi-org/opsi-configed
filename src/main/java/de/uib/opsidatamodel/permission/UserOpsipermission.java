package de.uib.opsidatamodel.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import de.uib.utilities.logging.logging;

public class UserOpsipermission extends UserConfigModule {
	// public enum HostTypeOfPrivilege{ ALL, CLIENT, DEPOT, SERVER }
	// permit restrictable action
	
	
	

	
	

	public enum ActionPrivilege {
		READ_ONLY, READ_WRITE
	}

	public static String CONFIGKEY_STR_USER = "user";
	public static String CONFIGKEY_STR_HOST = "host";
	public static String CONFIGKEY_STR_PRODUCT = "product";
	public static String CONFIGKEY_STR_PRIVILEGE = "privilege";
	public static String CONFIGKEY_STR_DEPOT = "depotaccess";
	public static String CONFIGKEY_STR_DEPOTLIST = "depots";
	public static String CONFIGKEY_STR_HOSTGROUP = "groupaccess";
	public static String CONFIGKEY_STR_HOSTGROUPLIST = "hostgroups";
	public static String CONFIGKEY_STR_PRODUCTGROUP = "groupaccess";
	public static String CONFIGKEY_STR_PRODUCTGROUPLIST = "productgroups";
	public static String CONFIGKEY_STR_ACCESSCONTROLLED = "configured";
	public static String CONFIGKEY_STR_SERVER = "opsiserver";
	public static String CONFIGKEY_STR_READWRITE = "write";
	public static String CONFIGKEY_STR_ALLHOSTS = "all";
	public static String CONFIGKEY_STR_CREATECLIENT = "createclient";
	public static String CONFIGKEY_STR_READONLY = "registered_readonly";

	public static final String PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY = CONFIGKEY_STR_PRIVILEGE + "."
			+ CONFIGKEY_STR_HOST + "." + CONFIGKEY_STR_ALLHOSTS + "." + CONFIGKEY_STR_READONLY;
	// privilege.host.all.readonly : boolean
	public static final String PARTKEY_USER_PRIVILEGE_SERVER_READWRITE = CONFIGKEY_STR_PRIVILEGE + "."
			+ CONFIGKEY_STR_HOST + "." + CONFIGKEY_STR_SERVER + "." + CONFIGKEY_STR_READWRITE;
	// privilege.host.opsiserver.readwrite boolean

	public static final String PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED = CONFIGKEY_STR_PRIVILEGE + "."
			+ CONFIGKEY_STR_HOST + "." + CONFIGKEY_STR_DEPOT + "." + CONFIGKEY_STR_ACCESSCONTROLLED;
	
	public static final String PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE = CONFIGKEY_STR_PRIVILEGE + "."
			+ CONFIGKEY_STR_HOST + "." + CONFIGKEY_STR_DEPOT + "." + CONFIGKEY_STR_DEPOTLIST;
	// privilege.host.depotaccess.depots : multivalue

	public static final String PARTKEY_USER_PRIVILEGE_CREATECLIENT = CONFIGKEY_STR_PRIVILEGE + "." + CONFIGKEY_STR_HOST
			+ "." + CONFIGKEY_STR_CREATECLIENT;

	public static final String PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED = CONFIGKEY_STR_PRIVILEGE + "."
			+ CONFIGKEY_STR_HOST + "." + CONFIGKEY_STR_HOSTGROUP + "." + CONFIGKEY_STR_ACCESSCONTROLLED;
	

	public static final String PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE = CONFIGKEY_STR_PRIVILEGE + "."
			+ CONFIGKEY_STR_HOST + "." + CONFIGKEY_STR_HOSTGROUP + "." + CONFIGKEY_STR_HOSTGROUPLIST;
	// privilege.host.groupaccess.hostgroups : multivalue

	public static final String PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED = CONFIGKEY_STR_PRIVILEGE
			+ "." + CONFIGKEY_STR_PRODUCT + "." + CONFIGKEY_STR_PRODUCTGROUP + "." + CONFIGKEY_STR_ACCESSCONTROLLED;
	

	public static final String PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE = CONFIGKEY_STR_PRIVILEGE + "."
			+ CONFIGKEY_STR_PRODUCT + "." + CONFIGKEY_STR_PRODUCTGROUP + "." + CONFIGKEY_STR_PRODUCTGROUPLIST;
	// privilege.product.groupaccess.productgroups : multivalue

	public static final LinkedHashSet<String> BOOL_KEYS;
	static {
		BOOL_KEYS = new LinkedHashSet<>();
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_SERVER_READWRITE);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED);
		BOOL_KEYS.add(PARTKEY_USER_PRIVILEGE_CREATECLIENT);

		logging.info(" UserOpsipermission BOOL_KEYS " + BOOL_KEYS);

	}

	public static final HashSet<String> LIST_KEYS;
	public static final HashMap<String, String> CORRESPONDENCE_TO_LIST_KEYS;
	static {
		CORRESPONDENCE_TO_LIST_KEYS = new HashMap<>();
		LIST_KEYS = new HashSet<>();

		CORRESPONDENCE_TO_LIST_KEYS.put(PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED,
				PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE);
		CORRESPONDENCE_TO_LIST_KEYS.put(PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED,
				PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE);
		CORRESPONDENCE_TO_LIST_KEYS.put(PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED,
				PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE);

		for (String key : CORRESPONDENCE_TO_LIST_KEYS.keySet()) {
			LIST_KEYS.add(CORRESPONDENCE_TO_LIST_KEYS.get(key));
		}
		logging.info(" CORRESPONDENCE_TO_LIST_KEYS " + CORRESPONDENCE_TO_LIST_KEYS);

		logging.info(" UserOpsipermission LIST_KEYS " + LIST_KEYS);

	}

	public static final UserOpsipermission DEFAULT;

	static {
		logging.info("init ARCHEO for UserOpsipermission");
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
		logging.info(this, "created for username " + uname + " with " + booleanMap + " -- " + valuesMap);

	}

}
