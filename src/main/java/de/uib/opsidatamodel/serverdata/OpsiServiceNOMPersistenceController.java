/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.Configed;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.productaction.PanelCompleteWinProducts;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiHwAuditDevicePropertyType;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.RetrievedMap;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.SavedSearch;
import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.configed.type.licences.LicencePoolXOpsiProduct;
import de.uib.configed.type.licences.LicenceStatisticsRow;
import de.uib.configed.type.licences.LicenceUsableForEntry;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.connectx.SmbConnect;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.HostGroups;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.RemoteControls;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.datapanel.MapTableModel;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ListCellOptions;
import utils.Utils;

/**
 * PersistenceController implementation for the New Object Model (opsi 4.0)
 * description: instances of PersistenceController give access to proxy objects
 * which mediate access to remote objects (and buffer the data) The
 * PersistenceController retrieves its data from a server that is compatible
 * with the opsi data server resp. its stub (proxy) It has a Executioner
 * component that transmits requests to the opsi server and receives the
 * responses. There are several classes which implement the Executioner methods
 * in different ways dependent on the used means and protocols
 */
public class OpsiServiceNOMPersistenceController {
	private static final String EMPTYFIELD = "-";
	protected static final List<String> NONE_LIST = new ArrayList<>() {
		@Override
		public int size() {
			return -1;
		}
	};

	public static final Set<String> KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT = new HashSet<>();
	static {
		KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT.add("type");
		KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT.add("id");
	}

	public static final String CONFIG_KEY_SUPPLEMENTARY_QUERY = "configed.query_supplementary";
	public static final String DESCRIPTION_KEY = "description";
	public static final String EDITABLE_KEY = "editable";

	// constants for building hw queries
	public static final String HW_INFO_CONFIG = "HARDWARE_CONFIG_";
	public static final String HW_INFO_DEVICE = "HARDWARE_DEVICE_";
	public static final String LAST_SEEN_VISIBLE_COL_NAME = "HOST.last_scan_time";

	public static final String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT = "configed.productonclient_displayfields_localboot";
	public static final String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT = "configed.productonclient_displayfields_netboot";
	public static final String KEY_HOST_DISPLAYFIELDS = "configed.host_displayfields";
	public static final String KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION = "configed.license_inventory_extradisplayfields";

	public static final String CONTROL_DASH_CONFIG_KEY = "configed.dash_config";
	public static final String CONFIG_KEY = "configed.meta_config";

	public static final String KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT = CONTROL_DASH_CONFIG_KEY
			+ ".show_dash_for_showlicenses";

	public static final Boolean DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT = false;

	public static final String KEY_SEARCH_BY_SQL = "configed.search_by_sql";

	// combines with question if mysql backend is working
	public static final Boolean DEFAULTVALUE_SEARCH_BY_SQL = true;

	public static final String KEY_DISABLED_CLIENT_ACTIONS = "configed.host_actions_disabled";

	public static final String KEY_OPSICLIENTD_EXTRA_EVENTS = "configed.opsiclientd_events";
	public static final String OPSI_CLIENTD_EVENT_ON_DEMAND = "on_demand";
	public static final String OPSI_CLIENTD_EVENT_SILENT_INSTALL = "silent_install";

	public static final String KEY_PRODUCT_SORT_ALGORITHM = "product_sort_algorithm";

	public static final String LOCAL_IMAGE_RESTORE_PRODUCT_KEY = "opsi-local-image-restore";
	public static final String LOCAL_IMAGE_LIST_PROPERTY_KEY = "imagefiles_list";
	public static final String LOCAL_IMAGE_TO_RESTORE_PROPERTY_KEY = "imagefile";

	public static final String CONFIG_DEPOT_ID = "clientconfig.depot.id";
	public static final String KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN = "opsiclientd.event_on_shutdown.active";
	public static final Boolean DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN = false;

	public static final String KEY_SSH_DEFAULTWINUSER = "configed.ssh.deploy-client-agent.default.user";
	public static final String KEY_SSH_DEFAULTWINUSER_DEFAULT_VALUE = "Administrator";
	public static final String KEY_SSH_DEFAULTWINPW = "configed.ssh.deploy-client-agent.default.password";
	public static final String KEY_SSH_DEFAULTWINPW_DEFAULT_VALUE = "";

	public static final String CONFIGED_WORKBENCH_KEY = "configed.workbench.default";
	private static String configedWorkbenchDefaultValue = "/var/lib/opsi/workbench/";
	private static String packageServerDirectoryS = configedWorkbenchDefaultValue;

	public static final String CONFIGED_GIVEN_DOMAINS_KEY = "configed.domains_given";

	// keys for default wan configuration
	public static final String CONFIG_CLIENTD_EVENT_GUISTARTUP = "opsiclientd.event_gui_startup.active";
	public static final String CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN = "opsiclientd.event_gui_startup{user_logged_in}.active";
	public static final String CONFIG_CLIENTD_EVENT_NET_CONNECTION = "opsiclientd.event_net_connection.active";
	public static final String CONFIG_CLIENTD_EVENT_TIMER = "opsiclientd.event_timer.active";

	public static final String CONFIG_DHCPD_FILENAME = "clientconfig.dhcpd.filename";
	public static final String EFI_DHCPD_FILENAME = "linux/pxelinux.cfg/elilo.efi";
	// the current real value, but it is not necessary to configure it:

	// not more used:

	public static final String EFI_DHCPD_NOT = "";

	public static final String EFI_STRING = "efi";

	public static final String KEY_USER_ROOT = "user";

	public static final String KEY_USER_ROLE_ROOT = KEY_USER_ROOT + "." + "role";// UserConfig.
	public static final String ALL_USER_KEY_START = KEY_USER_ROOT + ".{}.";// UserConfig.

	public static final String KEY_USER_REGISTER = KEY_USER_ROOT + ".{}.register"; // boolean

	public static final String DEPOT_SELECTION_NODEPOTS = Configed
			.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS");
	public static final String DEPOT_SELECTION_ALL = Configed
			.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_ALL");
	public static final String DEPOT_SELECTION_ALL_WHERE_INSTALLED = Configed
			.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_ALL_WHERE_INSTALLED");

	public static final String HOST_KEY = "hostId";

	private static final RPCMethodName BACKEND_LICENSING_INFO_METHOD_NAME = RPCMethodName.BACKEND_GET_LICENSING_INFO;

	private static final String NAME_REQUIREMENT_TYPE_BEFORE = "before";
	private static final String NAME_REQUIREMENT_TYPE_AFTER = "after";
	private static final String NAME_REQUIREMENT_TYPE_NEUTRAL = "";
	private static final String NAME_REQUIREMENT_TYPE_ON_DEINSTALL = "on_deinstall";

	public static final String CONFIG_STATE_TYPE = "ConfigState";

	public static final String OBJECT_ID = "objectId";
	public static final String CONFIG_ID = "configId";
	public static final String VALUES_ID = "values";

	private static Boolean keyUserRegisterValue;

	public static final NavigableMap<String, String> PROPERTY_CLASSES_SERVER = new TreeMap<>();
	static {
		PROPERTY_CLASSES_SERVER.put("", "general configuration items");
		PROPERTY_CLASSES_SERVER.put("clientconfig", "network configuration");
		PROPERTY_CLASSES_SERVER.put(LicensingInfoMap.CONFIG_KEY, "opsi module status display");
		PROPERTY_CLASSES_SERVER.put(CONTROL_DASH_CONFIG_KEY, "dash configuration");
		PROPERTY_CLASSES_SERVER.put(CONFIG_KEY_SUPPLEMENTARY_QUERY,
				"<html><p>sql queries can be defined here<br />- for purposes other than are fulfilled by the standard tables</p></html>");
		PROPERTY_CLASSES_SERVER.put(CONFIG_KEY, "default configuration for other properties");
		PROPERTY_CLASSES_SERVER.put(SavedSearch.CONFIG_KEY,
				"<html><p>saved search configurations ,<br />do not edit here <br />- editing via the search form</p></html>");
		PROPERTY_CLASSES_SERVER.put(RemoteControl.CONFIG_KEY,
				"<html><p>remote control calls,<br />i.e. calls to tools on the local computer<br />typically targeting at a selected client</p></html>");
		PROPERTY_CLASSES_SERVER.put(OpsiHwAuditDeviceClass.CONFIG_KEY,
				"<html><p>configuration for hw overview table,<br />- best editing via the helper function<br />at the hw overview table!)</p></html>");
		PROPERTY_CLASSES_SERVER.put("opsiclientd", "<html>entries for the opsiclientd.conf</html>");

		PROPERTY_CLASSES_SERVER.put("opsi-script", "<html>parameters for opsi-script on a client</html>");
		PROPERTY_CLASSES_SERVER.put("software-on-demand",
				"<html>software on demand configuration,<br />not client specific</html>");
		PROPERTY_CLASSES_SERVER.put(KEY_USER_ROOT,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.userPrivilegesConfiguration.ToolTip"));
		PROPERTY_CLASSES_SERVER.put(KEY_USER_ROLE_ROOT,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.roleConfiguration.ToolTip"));
	}

	public static final NavigableMap<String, String> PROPERTYCLASSES_CLIENT = new TreeMap<>();
	static {
		PROPERTYCLASSES_CLIENT.put("", "general configuration items");
		PROPERTYCLASSES_CLIENT.put("clientconfig", "network configuration");
		PROPERTYCLASSES_CLIENT.put("opsiclientd", "<html>entries for the opsiclientd.conf</html>");
		PROPERTYCLASSES_CLIENT.put("opsi-script", "<html>parameters for opsi-script on a client</html>");

		PROPERTYCLASSES_CLIENT.put("software-on-demand",
				"<html>software on demand configuration,<br />not client specific</html>");
	}

	public static final Set<String> CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS;
	static {
		CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS = new HashSet<>(PROPERTY_CLASSES_SERVER.keySet());
		CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS.removeAll(PROPERTYCLASSES_CLIENT.keySet());
		CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS.add(KEY_PRODUCT_SORT_ALGORITHM);
		CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS.add("configed");
	}

	// opsi module information
	// wan meta configuration
	public static final String WAN_PARTKEY = "wan_";
	public static final String NOT_WAN_CONFIGURED_PARTKEY = "wan_mode_off";

	private Map<String, List<Object>> wanConfiguration;
	private Map<String, List<Object>> notWanConfiguration;

	/**
	 * This creation method constructs a new Controller instance and lets a
	 * static variable point to it When next time we need a Controller we can
	 * choose if we take the already constructed one - returned from the static
	 * method getPersistenceController - or construct a new one public static
	 * PersistenceController getNewPersistenceController (String server, String
	 * user, String password) { return null; } public static
	 * PersistenceController getPersistenceController () { return null; }
	 */

	private PanelCompleteWinProducts panelCompleteWinProducts;

	private AbstractExecutioner exec;

	/* data for checking permissions */
	private boolean globalReadOnly;

	private boolean serverFullPermission;

	private boolean createClientPermission;

	private boolean depotsFullPermission;
	private Set<String> depotsPermitted;

	private boolean hostgroupsOnlyIfExplicitlyStated;
	private Set<String> hostgroupsPermitted;

	private boolean productgroupsFullPermission;

	/* ------------------------------------------ */

	private String connectionServer;
	private String user;
	private String userConfigPart;
	private Boolean applyUserSpecializedConfig;

	private Map<String, List<String>> mapOfMethodSignatures;

	private Map<String, Map<String, Object>> productGlobalInfos;

	private Map<String, Map<String, ConfigName2ConfigValue>> productProperties;
	// (pcname -> (productname -> (propertyname -> propertyvalue))) NOM
	private Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties;
	private Map<String, Boolean> productHavingClientSpecificProperties;

	// for depot
	private Map<String, Map<String, ListCellOptions>> productPropertyDefinitions;

	private HostInfoCollections hostInfoCollections;

	protected String theDepot = "";

	private String opsiDefaultDomain;

	private Set<String> permittedProducts;

	private List<String> localbootProductNames;
	private List<String> netbootProductNames;

	private Map<String, List<String>> possibleActions; // product-->possibleActions

	// key --> rowmap for auditSoftware

	private List<Map<String, Object>> relationsAuditHardwareOnHost;

	private AuditSoftwareXLicencePool relationsAuditSoftwareToLicencePools;

	// function softwareIdent --> pool
	private Map<String, String> fSoftware2LicencePool;

	// function pool --> list of assigned software
	private Map<String, List<String>> fLicencePool2SoftwareList;

	// function pool --> list of assigned software which is not in software table
	private Map<String, List<String>> fLicencePool2UnknownSoftwareList;

	private NavigableSet<Object> softwareWithoutAssociatedLicencePool;

	// map key -> rowmap
	private Map<String, LicenceUsageEntry> rowsLicencesUsage;

	// function host -> list of used licences
	private Map<String, List<LicenceUsageEntry>> fClient2LicencesUsageList;

	private Map<String, Map<String, Object>> rowsLicencesReconciliation;

	private Map<String, List<Map<String, List<Map<String, Object>>>>> hwAuditConf;

	private List<String> opsiHwClassNames;
	private Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses;

	private List<String> hostColumnNames;
	private List<String> client2HwRowsColumnNames;
	private List<String> client2HwRowsJavaclassNames;
	private List<String> hwInfoClassNames;

	private Map<String, Map<String, String>> productGroups;

	private HostGroups hostGroups;

	private Map<String, Set<String>> fObject2Groups;

	private Map<String, Set<String>> fGroup2Members;

	private Map<String, Set<String>> fProductGroup2Members;

	private Map<String, String> logfiles;

	private List<Map<String, Object>> updateProductOnClientItems;

	private List<LicenceUsageEntry> itemsDeletionLicenceUsage;

	private Map<String, Object> opsiInformation = new HashMap<>();
	private Map<String, Object> licencingInfoOpsiAdmin;
	private Map<String, Object> licencingInfoNoOpsiAdmin;
	private LicensingInfoMap licInfoMap;

	private boolean hasOpsiLicencingBeenChecked;
	private boolean isOpsiLicencingAvailable;

	private boolean hasIsOpisUserAdminBeenChecked;
	private boolean isOpsiUserAdmin;
	private boolean isMultiFactorAuthenticationEnabled;

	// the infos that are displayed in the gui
	private Map<String, Object> opsiModulesDisplayInfo;

	// the resulting info about permission
	private Map<String, Boolean> opsiModules;

	private boolean withLicenceManagement;
	private boolean withLocalImaging;

	private boolean withMySQL;
	private boolean withUEFI;
	private boolean withWAN;

	private boolean withUserRoles;

	// for internal use, for external cast to:
	private Map<String, ConfigOption> configOptions;
	private Map<String, ListCellOptions> configListCellOptions;
	private Map<String, List<Object>> configDefaultValues;

	private RemoteControls remoteControls;
	private SavedSearches savedSearches;

	private Map<String, Boolean> productOnClientsDisplayFieldsNetbootProducts;
	private Map<String, Boolean> productOnClientsDisplayFieldsLocalbootProducts;
	private Map<String, Boolean> hostDisplayFields;

	private List<Map<String, Object>> configStateCollection;
	private List<Map<String, Object>> deleteConfigStateItems;
	private List<Map<String, Object>> configCollection;

	private List<Map<String, Object>> productPropertyStateUpdateCollection;
	private List<Map<String, Object>> productPropertyStateDeleteCollection;

	private Map<String, Map<String, Object>> hostUpdates;

	private NavigableSet<String> productIds;
	private Map<String, Map<String, String>> productDefaultStates;

	private List<Map<String, Object>> healthData;
	private Map<String, Object> diagnosticData;

	private Boolean acceptMySQL;

	private DataUpdater dataUpdater;

	private VolatileDataRetriever volatileDataRetriever;
	private PersistentDataRetriever persistentDataRetriever;

	// package visibility, the constructor is called by PersistenceControllerFactory
	OpsiServiceNOMPersistenceController(String server, String user, String password) {
		Logging.info(this.getClass(), "start construction, \nconnect to " + server + " as " + user);
		this.connectionServer = server;
		this.user = user;

		Logging.debug(this.getClass(), "create");

		// hostInfoCollections = new HostInfoCollections(this);
		exec = new ServerFacade(server, user, password);
		dataUpdater = new DataUpdater(exec);
		volatileDataRetriever = new VolatileDataRetriever(exec);
		persistentDataRetriever = new PersistentDataRetriever(exec, this);
		hwAuditConf = new HashMap<>();
	}

	public DataUpdater getDataUpdater() {
		return dataUpdater;
	}

	public VolatileDataRetriever getVolatileDataRetriever() {
		return volatileDataRetriever;
	}

	public PersistentDataRetriever getPersistentDataRetriever() {
		return persistentDataRetriever;
	}

	public static Map<String, Object> createNOMitem(String type) {
		Map<String, Object> item = new HashMap<>();
		item.put("type", type);

		return item;
	}

	public static Map<String, Object> createJSONConfig(ConfigOption.TYPE type, String key, String description,
			boolean editable, boolean multiValue, List<Object> defaultValues, List<Object> possibleValues) {

		Map<String, Object> item = createNOMitem(type.toString());

		item.put("id", key.toLowerCase(Locale.ROOT));
		item.put("description", description);
		item.put("editable", editable);
		item.put("multiValue", multiValue);

		item.put("defaultValues", defaultValues);

		item.put("possibleValues", possibleValues);

		return item;
	}

	public static Map<String, Object> createJSONBoolConfig(String key, Boolean value, String description) {
		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(value);

		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(true);
		possibleValues.add(false);

		return createJSONConfig(ConfigOption.TYPE.BOOL_CONFIG, key, description, false, false, defaultValues,
				possibleValues);
	}

	// ---------------------------------------------------------------
	// Registering and notifying panelCompleteWinProducts
	// TODO change how messages are shown there

	public void registerPanelCompleteWinProducts(PanelCompleteWinProducts panelCompleteWinProducts) {
		this.panelCompleteWinProducts = panelCompleteWinProducts;
	}

	public void notifyPanelCompleteWinProducts() {
		if (panelCompleteWinProducts != null) {
			panelCompleteWinProducts.evaluateWinProducts();
		}
	}

	public Map<String, List<Object>> getWanConfiguration() {
		return wanConfiguration;
	}

	public Map<String, List<Object>> getNotWanConfiguration() {
		return notWanConfiguration;
	}

	// ---------------------------------------------------------------

	public HostInfoCollections getHostInfoCollections() {
		return hostInfoCollections;
	}

	public static Boolean interpretAsBoolean(Object ob, Boolean defaultValue) {
		if (ob == null) {
			return defaultValue;
		}

		if (ob instanceof Boolean) {
			return (Boolean) ob;
		}

		if (ob instanceof Integer) {
			return ((Integer) ob) == 1;
		}

		if (ob instanceof String) {
			return "1".equals(ob);
		}

		Logging.warning("could not find boolean in interpretAsBoolean, returning false");

		// not foreseen value
		return false;
	}

	public AbstractExecutioner retrieveWorkingExec(String depot) {

		Logging.debug(this, "retrieveWorkingExec , compare depotname " + depot + " to config server "
				+ hostInfoCollections.getConfigServer() + " ( named as " + connectionServer + ")");

		if (depot.equals(hostInfoCollections.getConfigServer())) {
			Logging.debug(this, "retrieveWorkingExec for config server");
			return exec;
		}

		String password = (String) getHostInfoCollections().getDepots().get(depot).get(HostInfo.HOST_KEY_KEY);

		AbstractExecutioner exec1 = new ServerFacade(depot, depot, password);

		if (makeConnection(exec1)) {
			Logging.info(this, "retrieveWorkingExec new for server " + depot);
			return exec1;
		}

		Logging.info(this, "no connection to server " + depot);

		return AbstractExecutioner.getNoneExecutioner();
	}

	public boolean makeConnection() {
		return makeConnection(exec);
	}

	private boolean makeConnection(AbstractExecutioner exec1) {
		Logging.info(this, "trying to make connection");
		boolean result = exec1.doCall(new OpsiMethodCall(RPCMethodName.ACCESS_CONTROL_AUTHENTICATED, new String[] {}));

		if (!result) {
			Logging.info(this, "connection does not work");
		}

		result = result && getConnectionState().getState() == ConnectionState.CONNECTED;
		Logging.info(this, "tried to make connection result " + result);
		return result;
	}

	// we delegate method calls to the executioner
	public ConnectionState getConnectionState() {
		return exec.getConnectionState();
	}

	public boolean isGlobalReadOnly() {
		return globalReadOnly;
	}

	public Map<String, Map<String, Object>> getDepotPropertiesForPermittedDepots() {
		Map<String, Map<String, Object>> depotProperties = getHostInfoCollections().getAllDepots();
		LinkedHashMap<String, Map<String, Object>> depotPropertiesForPermittedDepots = new LinkedHashMap<>();

		String configServer = getHostInfoCollections().getConfigServer();
		if (hasDepotPermission(configServer)) {
			depotPropertiesForPermittedDepots.put(configServer, depotProperties.get(configServer));
		}

		for (Entry<String, Map<String, Object>> depotProperty : depotProperties.entrySet()) {
			if (!depotProperty.getKey().equals(configServer) && hasDepotPermission(depotProperty.getKey())) {
				depotPropertiesForPermittedDepots.put(depotProperty.getKey(), depotProperty.getValue());
			}
		}

		return depotPropertiesForPermittedDepots;
	}

	public boolean isServerFullPermission() {
		return serverFullPermission;
	}

	public boolean isCreateClientPermission() {
		return createClientPermission;
	}

	public boolean isDepotsFullPermission() {
		return depotsFullPermission;
	}

	public boolean hasDepotPermission(String depotId) {
		if (depotsFullPermission) {
			return true;
		}

		boolean result = false;

		if (depotsPermitted != null) {
			result = depotsPermitted.contains(depotId);
		}

		return result;
	}

	public boolean accessToHostgroupsOnlyIfExplicitlyStated() {
		return hostgroupsOnlyIfExplicitlyStated;
	}

	public Set<String> getHostgroupsPermitted() {
		Set<String> result = null;
		if (!hostgroupsOnlyIfExplicitlyStated) {
			result = hostgroupsPermitted;
		}

		Logging.info(this, "getHostgroupsPermitted " + result);

		return result;
	}

	public boolean installPackage(String filename) {
		boolean result = exec
				.doCall(new OpsiMethodCall(RPCMethodName.DEPOT_INSTALL_PACKAGE, new Object[] { filename, true }));
		Logging.info(this, "installPackage result " + result);
		return result;
	}

	public boolean setRights(String path) {
		Logging.info(this, "setRights for path " + path);
		String[] args = new String[] { path };
		if (path == null) {
			args = new String[] {};
		}
		return exec.doCall(new OpsiMethodCall(RPCMethodName.SET_RIGHTS, args));
	}

	public Boolean isInstallByShutdownConfigured(String host) {
		return getHostBooleanConfigValue(KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN, host, true, false);
	}

	public Boolean isWanConfigured(String host) {
		Logging.info(this, " isWanConfigured wanConfiguration  " + wanConfiguration + " for host " + host);
		return findBooleanConfigurationComparingToDefaults(host, wanConfiguration);
	}

	public Boolean isUefiConfigured(String hostname) {
		Boolean result = false;

		if (getConfigs().get(hostname) != null && getConfigs().get(hostname).get(CONFIG_DHCPD_FILENAME) != null
				&& !((List<?>) getConfigs().get(hostname).get(CONFIG_DHCPD_FILENAME)).isEmpty()) {
			String configValue = (String) ((List<?>) getConfigs().get(hostname).get(CONFIG_DHCPD_FILENAME)).get(0);

			if (configValue.indexOf(EFI_STRING) >= 0) {
				// something similar should work, but not this:

				result = true;
			}
		} else if (persistentDataRetriever.getConfigDefaultValues().get(CONFIG_DHCPD_FILENAME) != null
				&& !((List<?>) persistentDataRetriever.getConfigDefaultValues().get(CONFIG_DHCPD_FILENAME)).isEmpty()) {
			String configValue = (String) ((List<?>) persistentDataRetriever.getConfigDefaultValues()
					.get(CONFIG_DHCPD_FILENAME)).get(0);

			if (configValue.indexOf(EFI_STRING) >= 0) {
				// something similar should work, but not this:
				result = true;
			}
		} else {
			// No UEFI configuration
		}

		return result;
	}

	private Boolean valueFromConfigStateAsExpected(Map<String, Object> configs, String configKey, boolean expectValue) {
		Logging.debug(this, "valueFromConfigStateAsExpected configKey " + configKey);
		boolean result = false;

		if (configs != null && configs.get(configKey) != null && !((List<?>) (configs.get(configKey))).isEmpty()) {
			Logging.debug(this, "valueFromConfigStateAsExpected configKey, values " + configKey + ", valueList "
					+ configs.get(configKey) + " expected " + expectValue);

			Object value = ((List<?>) configs.get(configKey)).get(0);

			if (value instanceof Boolean) {
				if (((Boolean) value).equals(expectValue)) {
					result = true;
				}
			} else if (value instanceof String) {
				if (((String) value).equalsIgnoreCase("" + expectValue)) {
					result = true;
				}
			} else {
				Logging.error(this, "it is not a boolean and not a string, how to handle it ? " + " value " + value);
			}

			Logging.debug(this, "valueFromConfigStateAsExpected " + result);

		}
		return result;
	}

	public boolean configureInstallByShutdown(String clientId, boolean shutdownInstall) {
		return setHostBooleanConfigValue(KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN, clientId, shutdownInstall);
	}

	// for checking if WAN default configuration is set
	public boolean findBooleanConfigurationComparingToDefaults(String host,
			Map<String, List<Object>> defaultConfiguration) {
		boolean tested = false;
		for (Entry<String, List<Object>> configuration : defaultConfiguration.entrySet()) {
			tested = valueFromConfigStateAsExpected(getConfig(host), configuration.getKey(),
					(Boolean) (configuration.getValue().get(0)));
			if (!tested) {
				break;
			}
		}

		return tested;
	}

	private List<Map<String, Object>> addWANConfigStates(String clientId, boolean wan,
			List<Map<String, Object>> jsonObjects) {
		persistentDataRetriever.getWANConfigOptions();

		Logging.debug(this,
				"addWANConfigState  wanConfiguration " + wanConfiguration + "\n " + wanConfiguration.size());
		Logging.debug(this, "addWANConfigState  wanConfiguration.keySet() " + wanConfiguration.keySet() + "\n "
				+ wanConfiguration.keySet().size());

		Logging.debug(this,
				"addWANConfigState  notWanConfiguration " + notWanConfiguration + "\n " + notWanConfiguration.size());
		Logging.debug(this, "addWANConfigState  notWanConfiguration.keySet() " + notWanConfiguration.keySet() + "\n "
				+ notWanConfiguration.keySet().size());

		setConfig(notWanConfiguration);
		Logging.info(this, "set notWanConfiguration members where no entry exists");
		// send to opsiserver only new configs
		setConfig(true);

		Map<String, List<Object>> specifiedConfiguration;

		if (wan) {
			specifiedConfiguration = wanConfiguration;
		} else {
			specifiedConfiguration = notWanConfiguration;
		}

		if (jsonObjects == null) {
			jsonObjects = new ArrayList<>();
		}

		for (Entry<String, List<Object>> config : specifiedConfiguration.entrySet()) {
			Logging.info(this, "addWANConfigState configId " + config.getKey());
			Map<String, Object> item = createNOMitem(CONFIG_STATE_TYPE);

			item.put(CONFIG_ID, config.getKey());

			Logging.info(this, "addWANConfigState values " + config.getValue());

			item.put(VALUES_ID, config.getValue());

			item.put(OBJECT_ID, clientId);

			Logging.info(this, "addWANConfigState configId, item " + config.getKey() + ", " + item);

			// locally, hopefully the RPC call will work
			if (getConfigs().get(clientId) == null) {
				Logging.info(this, "addWANConfigState; until now, no config(State) existed for client " + clientId
						+ " no local update");
				getConfigs().put(clientId, new HashMap<>());
			}

			getConfigs().get(clientId).put(config.getKey(), config.getValue());

			// prepare for JSON RPC
			jsonObjects.add(item);
		}

		return jsonObjects;
	}

	public boolean setWANConfigs(String clientId, boolean wan) {
		boolean result = false;
		Logging.info(this, "setWANConfigs " + clientId + " . " + wan);

		List<Map<String, Object>> jsonObjects = addWANConfigStates(clientId, wan, null);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
				new Object[] { jsonObjects });
		result = exec.doCall(omc);

		return result;
	}

	private static Map<String, Object> createUefiJSONEntry(String clientId, String val) {
		Map<String, Object> item = createNOMitem("ConfigState");
		List<String> values = new ArrayList<>();
		values.add(val);
		item.put("objectId", clientId);
		item.put("values", values);
		item.put("configId", CONFIG_DHCPD_FILENAME);

		return item;
	}

	public boolean configureUefiBoot(String clientId, boolean uefiBoot) {
		boolean result = false;

		Logging.info(this, "configureUefiBoot, clientId " + clientId + " " + uefiBoot);

		List<String> values = new ArrayList<>();

		if (uefiBoot) {
			values.add(EFI_DHCPD_FILENAME);

			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			jsonObjects.add(createUefiJSONEntry(clientId, EFI_DHCPD_FILENAME));

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
					new Object[] { jsonObjects });
			result = exec.doCall(omc);
		} else {
			values.add(EFI_DHCPD_NOT);

			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			jsonObjects.add(createUefiJSONEntry(clientId, EFI_DHCPD_NOT));

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
					new Object[] { jsonObjects });
			result = exec.doCall(omc);
		}

		// locally
		if (result) {
			if (getConfigs().get(clientId) == null) {
				getConfigs().put(clientId, new HashMap<>());
			}

			Logging.info(this,
					"configureUefiBoot, configs for clientId " + clientId + " " + getConfigs().get(clientId));
			getConfigs().get(clientId).put(CONFIG_DHCPD_FILENAME, values);
		}

		return result;
	}

	public boolean createClients(Iterable<List<Object>> clients) {
		List<Map<String, Object>> clientsJsonObject = new ArrayList<>();
		List<Map<String, Object>> productsNetbootJsonObject = new ArrayList<>();
		List<Map<String, Object>> groupsJsonObject = new ArrayList<>();
		List<Map<String, Object>> configStatesJsonObject = new ArrayList<>();

		for (List<Object> client : clients) {
			String hostname = (String) client.get(0);
			String domainname = (String) client.get(1);
			String depotId = (String) client.get(2);
			String description = (String) client.get(3);
			String inventorynumber = (String) client.get(4);
			String notes = (String) client.get(5);
			String systemUUID = (String) client.get(6);
			String macaddress = (String) client.get(7);
			String ipaddress = (String) client.get(8);
			String group = (String) client.get(9);
			String productNetboot = (String) client.get(10);
			boolean wanConfig = Boolean.parseBoolean((String) client.get(11));
			boolean uefiBoot = Boolean.parseBoolean((String) client.get(12));
			boolean shutdownInstall = Boolean.parseBoolean((String) client.get(13));

			String newClientId = hostname + "." + domainname;

			Map<String, Object> hostItem = createNOMitem(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT);
			hostItem.put(HostInfo.HOSTNAME_KEY, newClientId);
			hostItem.put(HostInfo.CLIENT_DESCRIPTION_KEY, description);
			hostItem.put(HostInfo.CLIENT_NOTES_KEY, notes);
			hostItem.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, systemUUID);
			hostItem.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macaddress);
			hostItem.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipaddress);
			hostItem.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventorynumber);

			clientsJsonObject.add(hostItem);

			Map<String, Object> itemDepot = createNOMitem(CONFIG_STATE_TYPE);
			List<String> valuesDepot = new ArrayList<>();
			valuesDepot.add(depotId);
			itemDepot.put(OBJECT_ID, newClientId);
			itemDepot.put(VALUES_ID, valuesDepot);
			itemDepot.put(CONFIG_ID, CONFIG_DEPOT_ID);

			configStatesJsonObject.add(itemDepot);

			if (uefiBoot) {
				configStatesJsonObject.add(createUefiJSONEntry(newClientId, EFI_DHCPD_FILENAME));
			}

			if (wanConfig) {
				configStatesJsonObject = addWANConfigStates(newClientId, true, configStatesJsonObject);
			}

			if (shutdownInstall) {
				List<Object> valuesShI = new ArrayList<>();
				valuesShI.add(true);

				Map<String, Object> itemShI = createNOMitem(CONFIG_STATE_TYPE);
				itemShI.put(OBJECT_ID, newClientId);
				itemShI.put(VALUES_ID, valuesShI);
				itemShI.put(CONFIG_ID, KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN);

				Logging.info(this, "create client, config item for shutdownInstall " + itemShI);

				configStatesJsonObject.add(itemShI);
			}

			if (group != null && !group.isEmpty()) {
				Logging.info(this, "createClient" + " group " + group);
				Map<String, Object> itemGroup = createNOMitem(Object2GroupEntry.TYPE_NAME);
				itemGroup.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
				itemGroup.put(Object2GroupEntry.GROUP_ID_KEY, group);
				itemGroup.put(Object2GroupEntry.MEMBER_KEY, newClientId);
				groupsJsonObject.add(itemGroup);
			}

			if (productNetboot != null && !productNetboot.isEmpty()) {
				Logging.info(this, "createClient" + " productNetboot " + productNetboot);
				Map<String, Object> itemProducts = createNOMitem("ProductOnClient");
				itemProducts.put(OpsiPackage.DB_KEY_PRODUCT_ID, productNetboot);
				itemProducts.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
				itemProducts.put("clientId", newClientId);
				itemProducts.put(ProductState.key2servicekey.get(ProductState.KEY_ACTION_REQUEST), "setup");
				productsNetbootJsonObject.add(itemProducts);
			}

			HostInfo hostInfo = new HostInfo(hostItem);
			if (depotId == null || depotId.isEmpty()) {
				depotId = getHostInfoCollections().getConfigServer();
			}
			hostInfo.setInDepot(depotId);
			hostInfo.setUefiBoot(uefiBoot);
			hostInfo.setWanConfig(wanConfig);
			hostInfo.setShutdownInstall(shutdownInstall);

			hostInfoCollections.setLocalHostInfo(newClientId, depotId, hostInfo);
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CREATE_OBJECTS, new Object[] { clientsJsonObject });
		boolean result = exec.doCall(omc);

		if (result) {
			if (!configStatesJsonObject.isEmpty()) {
				omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
						new Object[] { configStatesJsonObject });
				result = exec.doCall(omc);
			}

			if (!groupsJsonObject.isEmpty()) {
				omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS,
						new Object[] { groupsJsonObject });
				result = exec.doCall(omc);
			}

			if (!productsNetbootJsonObject.isEmpty()) {
				omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_CREATE_OBJECTS,
						new Object[] { productsNetbootJsonObject });
				result = exec.doCall(omc);
			}
		}

		return result;
	}

	public boolean createClient(String hostname, String domainname, String depotId, String description,
			String inventorynumber, String notes, String ipaddress, String systemUUID, String macaddress,
			boolean shutdownInstall, boolean uefiBoot, boolean wanConfig, String group, String productNetboot) {
		if (!hasDepotPermission(depotId)) {
			return false;
		}

		boolean result = false;

		if (inventorynumber == null) {
			inventorynumber = "";
		}

		if (description == null) {
			description = "";
		}

		if (notes == null) {
			notes = "";
		}

		if (ipaddress.isEmpty()) {
			ipaddress = null;
			// null works, "" does not in the opsi call
		}

		if (group == null) {
			group = "";
		}

		String newClientId = hostname + "." + domainname;

		Map<String, Object> hostItem = createNOMitem(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT);
		hostItem.put(HostInfo.HOSTNAME_KEY, newClientId);
		hostItem.put(HostInfo.CLIENT_DESCRIPTION_KEY, description);
		hostItem.put(HostInfo.CLIENT_NOTES_KEY, notes);
		hostItem.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, systemUUID);
		hostItem.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macaddress);
		hostItem.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipaddress);
		hostItem.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventorynumber);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CREATE_OBJECTS, new Object[] { hostItem });
		result = exec.doCall(omc);

		if (result) {
			List<Map<String, Object>> jsonObjects = new ArrayList<>();

			Map<String, Object> itemDepot = createNOMitem(CONFIG_STATE_TYPE);
			List<String> valuesDepot = new ArrayList<>();
			valuesDepot.add(depotId);
			itemDepot.put(OBJECT_ID, newClientId);
			itemDepot.put(VALUES_ID, valuesDepot);
			itemDepot.put(CONFIG_ID, CONFIG_DEPOT_ID);

			jsonObjects.add(itemDepot);

			if (uefiBoot) {
				jsonObjects.add(createUefiJSONEntry(newClientId, EFI_DHCPD_FILENAME));
			}

			if (wanConfig) {
				jsonObjects = addWANConfigStates(newClientId, true, jsonObjects);
			}

			if (shutdownInstall) {
				List<Object> valuesShI = new ArrayList<>();
				valuesShI.add(true);

				Map<String, Object> itemShI = createNOMitem(CONFIG_STATE_TYPE);
				itemShI.put(OBJECT_ID, newClientId);
				itemShI.put(VALUES_ID, valuesShI);
				itemShI.put(CONFIG_ID, KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN);

				Logging.info(this, "create client, config item for shutdownInstall " + itemShI);

				jsonObjects.add(itemShI);
			}

			omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS, new Object[] { jsonObjects });

			result = exec.doCall(omc);
		}

		if (result && group != null && !group.isEmpty()) {
			Logging.info(this, "createClient" + " group " + group);
			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			Map<String, Object> itemGroup = createNOMitem(Object2GroupEntry.TYPE_NAME);
			itemGroup.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			itemGroup.put(Object2GroupEntry.GROUP_ID_KEY, group);
			itemGroup.put(Object2GroupEntry.MEMBER_KEY, newClientId);
			jsonObjects.add(itemGroup);
			omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS, new Object[] { jsonObjects });
			result = exec.doCall(omc);
		}

		if (result && productNetboot != null && !productNetboot.isEmpty()) {
			Logging.info(this, "createClient" + " productNetboot " + productNetboot);
			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			Map<String, Object> itemProducts = createNOMitem("ProductOnClient");
			itemProducts.put(OpsiPackage.DB_KEY_PRODUCT_ID, productNetboot);
			itemProducts.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
			itemProducts.put("clientId", newClientId);
			itemProducts.put(ProductState.key2servicekey.get(ProductState.KEY_ACTION_REQUEST), "setup");
			jsonObjects.add(itemProducts);
			omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_CREATE_OBJECTS, new Object[] { jsonObjects });
			result = exec.doCall(omc);
		}

		if (result) {
			if (depotId == null || depotId.isEmpty()) {
				depotId = getHostInfoCollections().getConfigServer();
			}
			HostInfo hostInfo = new HostInfo(hostItem);
			hostInfo.setInDepot(depotId);
			hostInfo.setUefiBoot(uefiBoot);
			hostInfo.setWanConfig(wanConfig);
			hostInfo.setShutdownInstall(shutdownInstall);
			hostInfoCollections.setLocalHostInfo(newClientId, depotId, hostInfo);

			Logging.info(this, " createClient hostInfo " + hostInfo);
		}

		return result;
	}

	public boolean renameClient(String hostname, String newHostname) {
		if (globalReadOnly) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_RENAME_OPSI_CLIENT,
				new String[] { hostname, newHostname });
		hostInfoCollections.opsiHostsRequestRefresh();
		return exec.doCall(omc);
	}

	public void deleteClients(String[] hostIds) {
		if (globalReadOnly) {
			return;
		}

		for (String hostId : hostIds) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_DELETE, new String[] { hostId });
			exec.doCall(omc);
		}
		hostInfoCollections.opsiHostsRequestRefresh();
	}

	// hostControl methods
	private List<String> collectErrorsFromResponsesByHost(Map<String, Object> responses, String callingMethodName) {
		List<String> errors = new ArrayList<>();

		for (Entry<String, Object> response : responses.entrySet()) {
			Map<String, Object> jO = POJOReMapper.remap(response.getValue(), new TypeReference<Map<String, Object>>() {
			});
			String error = exec.getErrorFromResponse(jO);

			if (error != null) {
				error = response.getKey() + ":\t" + error;
				Logging.info(callingMethodName + ",  " + error);
				errors.add(error);
			}
		}

		return errors;
	}

	public List<String> deletePackageCaches(String[] hostIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SAFE_OPSICLIENTD_RPC,
				new Object[] { "cacheService_deleteCache", new Object[] {}, hostIds });

		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "deleteCache");
	}

	public Map<String, List<String>> getHostSeparationByDepots(String[] hostIds) {
		Map<String, Set<String>> hostSeparationByDepots = new HashMap<>();

		for (String hostId : hostIds) {
			String depotId = getHostInfoCollections().getMapPcBelongsToDepot().get(hostId);

			hostSeparationByDepots.computeIfAbsent(depotId, arg -> new HashSet<>()).add(hostId);
		}

		Map<String, List<String>> result = new HashMap<>();
		for (Entry<String, Set<String>> hostSeparationEntry : hostSeparationByDepots.entrySet()) {
			result.put(hostSeparationEntry.getKey(), new ArrayList<>(hostSeparationEntry.getValue()));
		}

		return result;
	}

	public List<String> wakeOnLan(String[] hostIds) {
		return wakeOnLan(getHostSeparationByDepots(hostIds));
	}

	private List<String> wakeOnLan(Map<String, List<String>> hostSeparationByDepot) {
		Map<String, Object> responses = new HashMap<>();

		Map<String, AbstractExecutioner> executionerForDepots = new HashMap<>();

		for (Entry<String, List<String>> hostSeparationEntry : hostSeparationByDepot.entrySet()) {
			Logging.info(this,
					"from depot " + hostSeparationEntry.getKey() + " we have hosts " + hostSeparationEntry.getValue());

			AbstractExecutioner exec1 = executionerForDepots.get(hostSeparationEntry.getKey());

			Logging.info(this, "working exec for depot " + hostSeparationEntry.getKey() + " " + (exec1 != null));

			if (exec1 == null) {
				exec1 = retrieveWorkingExec(hostSeparationEntry.getKey());
			}

			if (exec1 != null && exec1 != AbstractExecutioner.getNoneExecutioner()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_START,
						new Object[] { hostSeparationEntry.getValue().toArray(new String[0]) });

				Map<String, Object> responses1 = exec1.getMapResult(omc);
				responses.putAll(responses1);
			}
		}

		return collectErrorsFromResponsesByHost(responses, "wakeOnLan");
	}

	public List<String> wakeOnLan(Set<String> hostIds, Map<String, List<String>> hostSeparationByDepot,
			Map<String, AbstractExecutioner> execsByDepot) {
		Map<String, Object> responses = new HashMap<>();

		for (Entry<String, List<String>> hostSeparationEntry : hostSeparationByDepot.entrySet()) {
			if (hostSeparationEntry.getValue() != null && !hostSeparationEntry.getValue().isEmpty()) {
				Set<String> hostsToWake = new HashSet<>(hostIds);
				hostsToWake.retainAll(hostSeparationEntry.getValue());

				if (execsByDepot.get(hostSeparationEntry.getKey()) != null
						&& execsByDepot.get(hostSeparationEntry.getKey()) != AbstractExecutioner.getNoneExecutioner()
						&& !hostsToWake.isEmpty()) {
					Logging.debug(this, "wakeOnLan execute for " + hostsToWake);
					OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_START,
							new Object[] { hostsToWake.toArray(new String[0]) });

					Map<String, Object> responses1 = execsByDepot.get(hostSeparationEntry.getKey()).getMapResult(omc);
					responses.putAll(responses1);
				}
			}

		}

		return collectErrorsFromResponsesByHost(responses, "wakeOnLan");
	}

	public List<String> wakeOnLanOpsi43(String[] hostIds) {
		Map<String, Object> response = new HashMap<>();

		AbstractExecutioner exec1 = retrieveWorkingExec(getHostInfoCollections().getConfigServer());

		Logging.info(this,
				"working exec for config server " + getHostInfoCollections().getConfigServer() + " " + (exec1 != null));

		if (exec1 != null && exec1 != AbstractExecutioner.getNoneExecutioner()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_START, new Object[] { hostIds });

			response = exec1.getMapResult(omc);
		}

		return collectErrorsFromResponsesByHost(response, "wakeOnLan");
	}

	public List<String> fireOpsiclientdEventOnClients(String event, String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_FIRE_EVENT,
				new Object[] { event, clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "fireOpsiclientdEventOnClients");
	}

	public List<String> showPopupOnClients(String message, String[] clientIds, Float seconds) {
		OpsiMethodCall omc;

		if (seconds == 0) {
			omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SHOW_POPUP, new Object[] { message, clientIds });
		} else {
			omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SHOW_POPUP,
					new Object[] { message, clientIds, "True", "True", seconds });
		}

		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "showPopupOnClients");

	}

	public List<String> shutdownClients(String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SHUTDOWN, new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "shutdownClients");
	}

	public List<String> rebootClients(String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_REBOOT, new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "rebootClients");
	}

	public Map<String, Object> reachableInfo(String[] clientIds) {
		Logging.info(this, "reachableInfo ");
		Object[] callParameters = new Object[] {};

		RPCMethodName methodName = RPCMethodName.HOST_CONTROL_REACHABLE;
		if (clientIds != null) {
			Logging.info(this, "reachableInfo for clientIds " + clientIds.length);
			callParameters = new Object[] { clientIds };
			methodName = RPCMethodName.HOST_CONTROL_SAFE_REACHABLE;
		}

		// background call, do not show waiting info
		return exec.getMapResult(new OpsiMethodCall(methodName, callParameters, OpsiMethodCall.BACKGROUND_DEFAULT));
	}

	public Map<String, Integer> getInstalledOsOverview() {
		Logging.info(this, "getInstalledOsOverview");

		Map<String, Object> producedLicencingInfo;

		if (persistentDataRetriever.isOpsiUserAdmin() && licencingInfoOpsiAdmin != null) {
			producedLicencingInfo = POJOReMapper.remap(
					persistentDataRetriever.getOpsiLicencingInfoOpsiAdmin().get("result"),
					new TypeReference<Map<String, Object>>() {
					});
		} else {
			producedLicencingInfo = persistentDataRetriever.getOpsiLicencingInfoNoOpsiAdmin();
		}

		return POJOReMapper.remap(producedLicencingInfo.get("client_numbers"),
				new TypeReference<Map<String, Integer>>() {
				});

	}

	public List<Map<String, Object>> getModules() {
		Logging.info(this, "getModules");

		Map<String, Object> producedLicencingInfo;

		if (persistentDataRetriever.isOpsiUserAdmin() && licencingInfoOpsiAdmin != null) {
			producedLicencingInfo = POJOReMapper.remap(
					persistentDataRetriever.getOpsiLicencingInfoOpsiAdmin().get("result"),
					new TypeReference<Map<String, Object>>() {
					});
		} else {
			producedLicencingInfo = persistentDataRetriever.getOpsiLicencingInfoNoOpsiAdmin();
		}

		return POJOReMapper.remap(producedLicencingInfo.get("licenses"),
				new TypeReference<List<Map<String, Object>>>() {
				});

	}

	// executes all updates collected by setHostDescription ...
	public void updateHosts() {
		if (globalReadOnly) {
			return;
		}

		// checkHostPermission is done in updateHost

		if (hostUpdates == null) {
			return;
		}

		List<Map<String, Object>> updates = new ArrayList<>();
		for (Map<String, Object> hostUpdateValue : hostUpdates.values()) {
			updates.add(hostUpdateValue);
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_UPDATE_OBJECTS, new Object[] { updates.toArray() });

		if (exec.doCall(omc)) {
			hostUpdates.clear();
		}
	}

	private void updateHost(String hostId, String property, String value) {
		if (hostUpdates == null) {
			hostUpdates = new HashMap<>();
		}

		Map<String, Object> hostUpdateMap = hostUpdates.get(hostId);

		if (hostUpdateMap == null) {
			hostUpdateMap = new HashMap<>();
		}

		hostUpdateMap.put("ident", hostId);
		hostUpdateMap.put(HostInfo.HOST_TYPE_KEY, HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT);
		hostUpdateMap.put(property, value);

		hostUpdates.put(hostId, hostUpdateMap);
	}

	public void setHostDescription(String hostId, String description) {
		updateHost(hostId, HostInfo.CLIENT_DESCRIPTION_KEY, description);
	}

	public void setClientInventoryNumber(String hostId, String inventoryNumber) {
		updateHost(hostId, HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventoryNumber);
	}

	public void setClientOneTimePassword(String hostId, String oneTimePassword) {
		updateHost(hostId, HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY, oneTimePassword);
	}

	public void setHostNotes(String hostId, String notes) {
		updateHost(hostId, HostInfo.CLIENT_NOTES_KEY, notes);
	}

	public void setSystemUUID(String hostId, String uuid) {
		updateHost(hostId, HostInfo.CLIENT_SYSTEM_UUID_KEY, uuid);
	}

	public void setMacAddress(String hostId, String address) {
		updateHost(hostId, HostInfo.CLIENT_MAC_ADRESS_KEY, address);
	}

	public void setIpAddress(String hostId, String address) {
		updateHost(hostId, HostInfo.CLIENT_IP_ADDRESS_KEY, address);
	}

	public void hostGroupsRequestRefresh() {
		hostGroups = null;
	}

	public void fGroup2MembersRequestRefresh() {
		fGroup2Members = null;
	}

	public void fProductGroup2MembersRequestRefresh() {
		fProductGroup2Members = null;
	}

	public void fObject2GroupsRequestRefresh() {
		fObject2Groups = null;
	}

	public boolean addHosts2Group(List<String> objectIds, String groupId) {
		if (globalReadOnly) {
			return false;
		}

		Logging.info(this, "addHosts2Group hosts " + objectIds + " group " + groupId);
		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		List<Map<String, Object>> data = new ArrayList<>();

		for (String ob : objectIds) {
			Map<String, Object> item = createNOMitem(Object2GroupEntry.TYPE_NAME);
			item.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			item.put(Object2GroupEntry.MEMBER_KEY, ob);
			item.put(Object2GroupEntry.GROUP_ID_KEY, persistentGroupId);
			data.add(item);
		}

		Logging.info(this, "addHosts2Group persistentGroupId " + persistentGroupId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS, new Object[] { data });
		return exec.doCall(omc);
	}

	public boolean addHost2Groups(String objectId, List<String> groupIds) {
		if (globalReadOnly) {
			return false;
		}

		Logging.info(this, "addHost2Groups host " + objectId + " groups " + groupIds);
		List<Map<String, Object>> data = new ArrayList<>();

		for (String groupId : groupIds) {
			String persistentGroupId = ClientTree.translateToPersistentName(groupId);
			Map<String, Object> item = createNOMitem(Object2GroupEntry.TYPE_NAME);
			item.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			item.put(Object2GroupEntry.MEMBER_KEY, objectId);
			item.put(Object2GroupEntry.GROUP_ID_KEY, persistentGroupId);
			data.add(item);
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS, new Object[] { data });
		return exec.doCall(omc);
	}

	public boolean addObject2Group(String objectId, String groupId) {
		if (globalReadOnly) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		Logging.debug(this, "addObject2Group persistentGroupId " + persistentGroupId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE,
				new String[] { Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId, objectId });

		return exec.doCall(omc);
	}

	public boolean removeHostGroupElements(Iterable<Object2GroupEntry> entries) {
		if (globalReadOnly) {
			return false;
		}

		List<Map<String, Object>> deleteItems = new ArrayList<>();
		for (Object2GroupEntry entry : entries) {
			Map<String, Object> deleteItem = createNOMitem(Object2GroupEntry.TYPE_NAME);
			deleteItem.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			deleteItem.put(Object2GroupEntry.GROUP_ID_KEY, entry.getGroupId());
			deleteItem.put(Object2GroupEntry.MEMBER_KEY, entry.getMember());

			deleteItems.add(deleteItem);
		}

		boolean result = true;
		if (!deleteItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_DELETE_OBJECTS,
					new Object[] { deleteItems.toArray() });

			if (exec.doCall(omc)) {
				deleteItems.clear();
			} else {
				result = false;
			}
		}

		return result;
	}

	public boolean removeObject2Group(String objectId, String groupId) {
		if (globalReadOnly) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_DELETE,
				new String[] { Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId, objectId });

		return exec.doCall(omc);
	}

	public boolean addGroup(StringValuedRelationElement newgroup) {
		return addGroup(newgroup, true);
	}

	public boolean addGroup(StringValuedRelationElement newgroup, boolean requestRefresh) {
		if (!serverFullPermission) {
			return false;
		}

		Logging.debug(this, "addGroup : " + newgroup + " requestRefresh " + requestRefresh);

		String id = newgroup.get("groupId");
		String parentId = newgroup.get("parentGroupId");
		if (parentId == null || parentId.equals(ClientTree.ALL_GROUPS_NAME)) {
			parentId = null;
		}

		parentId = ClientTree.translateToPersistentName(parentId);

		if (id.equalsIgnoreCase(parentId)) {
			Logging.error(this, "Cannot add group as child to itself, group ID " + id);
			return false;
		}

		String description = newgroup.get("description");
		String notes = "";

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_CREATE_HOST_GROUP,
				new Object[] { id, description, notes, parentId });
		boolean result = exec.doCall(omc);
		if (result) {
			hostGroupsRequestRefresh();
		}

		return result;

	}

	public boolean deleteGroup(String groupId) {
		if (!serverFullPermission) {
			return false;
		}

		if (groupId == null) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_DELETE, new String[] { groupId });
		boolean result = exec.doCall(omc);

		if (result) {
			hostGroupsRequestRefresh();
		}

		return result;
	}

	public boolean updateGroup(String groupId, Map<String, String> updateInfo) {
		if (!serverFullPermission) {
			return false;
		}

		if (groupId == null) {
			return false;
		}

		if (updateInfo == null) {
			updateInfo = new HashMap<>();
		}

		updateInfo.put("ident", groupId);
		updateInfo.put("type", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);

		if (updateInfo.get("parentGroupId").equals(ClientTree.ALL_GROUPS_NAME)) {
			updateInfo.put("parentGroupId", "null");
		}

		String parentGroupId = updateInfo.get("parentGroupId");
		parentGroupId = ClientTree.translateToPersistentName(parentGroupId);
		updateInfo.put("parentGroupId", parentGroupId);

		Logging.debug(this, "updateGroup " + parentGroupId);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_UPDATE_OBJECT, new Object[] { updateInfo });
		return exec.doCall(omc);
	}

	public boolean setProductGroup(String groupId, String description, Set<String> productSet) {
		if (!serverFullPermission) {
			return false;
		}

		Logging.debug(this, "setProductGroup: groupId " + groupId);
		if (groupId == null) {
			return false;
		}

		Logging.info(this, "setProductGroup: groupId " + groupId + " should have members " + productSet);

		boolean result = true;

		Map<String, String> map = new HashMap<>();

		map.put("id", groupId);
		map.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);

		if (description != null) {
			map.put("description", description);
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_CREATE_OBJECTS,
				new Object[] { new Object[] { map } });
		result = exec.doCall(omc);

		HashSet<String> inNewSetnotInOriSet = new HashSet<>(productSet);
		HashSet<String> inOriSetnotInNewSet = new HashSet<>();

		if (persistentDataRetriever.getFProductGroup2Members().get(groupId) != null) {
			Set<String> oriSet = persistentDataRetriever.getFProductGroup2Members().get(groupId);
			Logging.debug(this, "setProductGroup: oriSet " + oriSet);
			inOriSetnotInNewSet = new HashSet<>(oriSet);
			inOriSetnotInNewSet.removeAll(productSet);
			inNewSetnotInOriSet.removeAll(oriSet);
		}

		Logging.info(this, "setProductGroup: inOriSetnotInNewSet, inNewSetnotInOriSet. " + inOriSetnotInNewSet + ", "
				+ inNewSetnotInOriSet);

		final Map<String, String> typingObject = new HashMap<>();
		typingObject.put("groupType", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);
		typingObject.put("type", Object2GroupEntry.TYPE_NAME);

		List<Map<String, String>> object2Groups = new ArrayList<>();
		for (String objectId : inOriSetnotInNewSet) {
			Map<String, String> m = new HashMap<>(typingObject);
			m.put("groupId", groupId);
			m.put("objectId", objectId);
			object2Groups.add(m);
		}

		Logging.debug(this, "delete objects " + object2Groups);

		if (!object2Groups.isEmpty()) {
			result = result && exec.doCall(
					new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_DELETE_OBJECTS, new Object[] { object2Groups }));
		}

		object2Groups.clear();
		for (String objectId : inNewSetnotInOriSet) {
			Map<String, String> m = new HashMap<>(typingObject);
			m.put("groupId", groupId);
			m.put("objectId", objectId);
			object2Groups.add(m);
		}

		Logging.debug(this, "create new objects " + object2Groups);

		if (!object2Groups.isEmpty()) {
			result = result && exec.doCall(
					new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS, new Object[] { object2Groups }));
		}

		if (result) {
			persistentDataRetriever.getFProductGroup2Members().put(groupId, productSet);
		}

		return result;
	}

	public void hwAuditConfRequestRefresh() {
		hwAuditConf.clear();
		hwAuditDeviceClasses = null;
		client2HwRowsColumnNames = null;
		hwInfoClassNames = null;

		if (opsiHwClassNames != null) {
			opsiHwClassNames.clear();
		}
		opsiHwClassNames = null;
	}

	public void softwareAuditOnClientsRequestRefresh() {
		Logging.info(this, "softwareAuditOnClientsRequestRefresh");
		// persistentDataRetriever.softwareAuditOnClientsRequestRefresh();
	}

	public String getLastSoftwareAuditModification(Map<String, List<SWAuditClientEntry>> entries, String clientId) {
		String result = "";

		if (entries == null || entries.isEmpty()) {
			return result;
		}

		List<SWAuditClientEntry> swAuditClientEntries = entries.get(clientId);
		if (!entries.isEmpty() && swAuditClientEntries != null && !swAuditClientEntries.isEmpty()) {
			result = swAuditClientEntries.get(0).getLastModification();
		}

		return result;
	}

	public Map<String, Map<String, Object>> retrieveSoftwareAuditData(Map<String, List<SWAuditClientEntry>> entries,
			String clientId) {
		Map<String, Map<String, Object>> result = new TreeMap<>();

		if (entries == null || entries.isEmpty()) {
			return result;
		}

		List<SWAuditClientEntry> swAuditClientEntries = entries.get(clientId);
		for (SWAuditClientEntry entry : swAuditClientEntries) {
			if (entry.getSWid() != null && entry.getSWid() != -1) {
				result.put("" + entry.getSWid(),
						entry.getExpandedMap(persistentDataRetriever.getInstalledSoftwareInformation(),
								persistentDataRetriever.getSWident(entry.getSWid())));
			}
		}

		return result;
	}

	public void auditHardwareOnHostRequestRefresh() {
		relationsAuditHardwareOnHost = null;
	}

	/* multiclient hwinfo */

	public void client2HwRowsRequestRefresh() {
		hostColumnNames = null;
		client2HwRowsColumnNames = null;
		// persistentDataRetriever.client2HwRowsRequestRefresh();
	}

	public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts) {
		Map<String, Map<String, Object>> client2HwRows = persistentDataRetriever.getClient2HwRows(hosts);

		for (String host : hosts) {
			Logging.info(this, "getClient2HwRows host " + host);

			if (client2HwRows.get(host) == null) {
				Logging.info(this, "getClient2HwRows for host " + host + " is null");
			}
		}

		return client2HwRows;
	}

	private Map<String, Object> produceHwAuditColumnConfig(String configKey,
			List<OpsiHwAuditDevicePropertyType> deviceProperties, Map<String, Boolean> tableConfigUpdates) {
		List<Object> oldDefaultValues = new ArrayList<>();

		if (configOptions.get(configKey) != null) {
			oldDefaultValues = configOptions.get(configKey).getDefaultValues();
		}

		Logging.info(this, "produceHwAuditColumnConfig " + oldDefaultValues);

		List<Object> possibleValues = new ArrayList<>();
		for (OpsiHwAuditDevicePropertyType deviceProperty : deviceProperties) {
			possibleValues.add(deviceProperty.getOpsiDbColumnName());
		}

		Logging.info(this, "produceConfig, possibleValues " + possibleValues);

		List<Object> newDefaultValues = new ArrayList<>();
		for (Object value : possibleValues) {
			if (oldDefaultValues.contains(value)) {
				// was in default values and no change, or value is in (old) default values and
				// set again
				if (tableConfigUpdates.get(value) == null || Boolean.TRUE.equals(tableConfigUpdates.get(value))) {
					newDefaultValues.add(value);
				}
			} else if (tableConfigUpdates.get(value) != null && tableConfigUpdates.get(value)) {
				// change, value is now configured
				newDefaultValues.add(value);
			} else {
				// value is contained nowhere
			}
		}

		Map<String, Object> configItem = createJSONConfig(ConfigOption.TYPE.UNICODE_CONFIG, configKey, // key
				"", false, // editable
				true, // multivalue
				newDefaultValues, possibleValues);

		Logging.info(this, "produceConfig, created an item " + configItem);

		return configItem;
	}

	public boolean saveHwColumnConfig(Map<String, Map<String, Boolean>> updateItems) {
		persistentDataRetriever.getConfigOptions();

		List<Object> readyObjects = new ArrayList<>();

		for (Entry<String, OpsiHwAuditDeviceClass> hwClass : hwAuditDeviceClasses.entrySet()) {
			OpsiHwAuditDeviceClass hwAuditDeviceClass = hwAuditDeviceClasses.get(hwClass.getKey());

			// case hostAssignedTableType
			String configKey = hwAuditDeviceClass.getHostConfigKey();
			String configIdent = hwClass.getKey() + "_" + OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE;

			Logging.debug(this, " saveHwColumnConfig for HOST configIdent " + configIdent);

			Map<String, Boolean> tableConfigUpdates = updateItems.get(configIdent.toUpperCase(Locale.ROOT));

			if (tableConfigUpdates != null) {
				Logging.info(this,
						" saveHwColumnConfig tableConfigUpdates  for the host configIdent,  " + tableConfigUpdates);
			}

			// we have got updates for this table configuration
			if (tableConfigUpdates != null) {
				Map<String, Object> configItem = produceHwAuditColumnConfig(configKey,
						hwAuditDeviceClass.getDeviceHostProperties(), tableConfigUpdates);

				readyObjects.add(configItem);

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				// save the data locally, we hope that the upload later will work as well

				// now, we have got them in a view model

				Logging.info(this,
						"saveHwColumnConfig, locally saving " + " key " + hwAuditDeviceClass.getHwItemConfigKey());

				Logging.info(this,
						"saveHwColumnConfig, old configOption for key" + " " + hwAuditDeviceClass.getHostConfigKey()
								+ " " + configOptions.get(hwAuditDeviceClass.getHostConfigKey()));

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				ConfigOption configOption = new ConfigOption(configItem);

				configOptions.put(hwAuditDeviceClass.getHostConfigKey(), configOption);
			}

			// case hwItemAssignedTableType
			configKey = hwAuditDeviceClass.getHwItemConfigKey();
			configIdent = hwClass.getKey() + "_" + OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE;

			Logging.debug(this, " saveHwColumnConfig for HW configIdent " + configIdent);

			tableConfigUpdates = updateItems.get(configIdent.toUpperCase(Locale.ROOT));

			if (tableConfigUpdates != null) {
				Logging.info(this,
						" saveHwColumnConfig tableConfigUpdates  for the hw configIdent,  " + tableConfigUpdates);
			}

			// we have got updates for this table configuration
			if (tableConfigUpdates != null) {

				Map<String, Object> configItem = produceHwAuditColumnConfig(configKey,
						hwAuditDeviceClass.getDeviceHwItemProperties(), tableConfigUpdates);

				readyObjects.add(configItem);

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				// save the data locally, we hope that the upload later will work as well
				// now, we have got them in a view model

				Logging.info(this, "saveHwColumnConfig, produce a ConfigOption from configItem " + configItem);

				Logging.info(this,
						"saveHwColumnConfig, locally saving " + " key " + hwAuditDeviceClass.getHwItemConfigKey());

				Logging.info(this,
						"saveHwColumnConfig, we had configOption for key" + " "
								+ hwAuditDeviceClass.getHwItemConfigKey() + " "
								+ configOptions.get(hwAuditDeviceClass.getHwItemConfigKey()));

				ConfigOption configOption = new ConfigOption(configItem);

				configOptions.put(hwAuditDeviceClass.getHostConfigKey(), configOption);

			}
		}

		Logging.info(this, "saveHwColumnConfig readyObjects " + readyObjects.size());
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

		return exec.doCall(omc);

	}

	public void depotChange() {
		Logging.info(this, "depotChange");
		productGlobalInfos = null;
		possibleActions = null;
		productIds = null;
		netbootProductNames = null;
		localbootProductNames = null;
		retrieveProducts();
		// retrieveProductPropertyDefinitions();
		persistentDataRetriever.getProductGlobalInfos(theDepot);
	}

	public void productDataRequestRefresh() {
		//persistentDataRetriever.productDataRequestRefresh();
		productpropertiesRequestRefresh();
		depot2product2properties = null;
		productGroups = null;
	}

	public void retrieveProducts() {
		retrieveDepotProducts(theDepot);
	}

	private void retrieveDepotProducts(String depotId) {
		Logging.debug(this, "retrieveDepotProducts for " + depotId);

		if (persistentDataRetriever.getDepot2NetbootProducts().get(depotId) != null) {
			netbootProductNames = new ArrayList<>(
					persistentDataRetriever.getDepot2NetbootProducts().get(depotId).keySet());
		} else {
			netbootProductNames = new ArrayList<>();
		}

		// we don't have a productsgroupsFullPermission)
		if (permittedProducts != null) {
			netbootProductNames.retainAll(permittedProducts);
		}

		// for localboot products, we have to look for ordering information
		localbootProductNames = persistentDataRetriever.getAllLocalbootProductNames(depotId);
	}

	public List<String> getAllDepotsWithIdenticalProductStock(String depot) {
		List<String> result = new ArrayList<>();

		TreeSet<OpsiPackage> originalProductStock = persistentDataRetriever.getDepot2Packages().get(depot);
		Logging.info(this, "getAllDepotsWithIdenticalProductStock " + originalProductStock);

		for (String testDepot : getHostInfoCollections().getAllDepots().keySet()) {
			if (depot.equals(testDepot) || areProductStocksIdentical(originalProductStock,
					persistentDataRetriever.getDepot2Packages().get(testDepot))) {
				result.add(testDepot);
			}
		}
		Logging.info(this, "getAllDepotsWithIdenticalProductStock  result " + result);

		return result;
	}

	private static boolean areProductStocksIdentical(TreeSet<OpsiPackage> firstProductStock,
			TreeSet<OpsiPackage> secondProductStock) {
		return (firstProductStock == null && secondProductStock == null)
				|| (firstProductStock != null && firstProductStock.equals(secondProductStock));
	}

	public List<String> getAllNetbootProductNames(String depotId) {
		if (netbootProductNames == null) {
			retrieveDepotProducts(depotId);
		}
		return new ArrayList<>(netbootProductNames);
	}

	public List<String> getAllNetbootProductNames() {
		return getAllNetbootProductNames(theDepot);
	}

	public List<String> getWinProducts(String depotId, String depotProductDirectory) {
		List<String> winProducts = new ArrayList<>();
		if (depotProductDirectory == null) {
			return winProducts;
		}

		boolean smbMounted = new File(depotProductDirectory).exists();

		for (String product : new TreeSet<>(getAllNetbootProductNames(depotId))) {
			if (!smbMounted
					|| new File(
							depotProductDirectory + File.separator + product + File.separator + SmbConnect.DIRECTORY_PE)
									.exists()
					|| new File(depotProductDirectory + File.separator + product + File.separator
							+ SmbConnect.DIRECTORY_I368).exists()) {
				winProducts.add(product);
			}
		}

		return winProducts;
	}

	private void updateProductOnClient(String pcname, String productname, int producttype,
			Map<String, String> updateValues, List<Map<String, Object>> updateItems) {
		Map<String, Object> values = new HashMap<>();

		values.put("productType", OpsiPackage.giveProductType(producttype));
		values.put("type", "ProductOnClient");
		values.put("clientId", pcname);
		values.put("productId", productname);
		values.putAll(updateValues);

		Logging.debug(this, "updateProductOnClient, values " + values);
		updateItems.add(values);
	}

	public void updateProductOnClient(String pcname, String productname, int producttype,
			Map<String, String> updateValues) {
		if (updateProductOnClientItems == null) {
			updateProductOnClientItems = new ArrayList<>();
		}

		updateProductOnClient(pcname, productname, producttype, updateValues, updateProductOnClientItems);
	}

	// hopefully we get only updateItems for allowed clients
	private boolean updateProductOnClients(List<Map<String, Object>> updateItems) {
		Logging.info(this, "updateProductOnClients ");

		if (globalReadOnly) {
			return false;
		}

		boolean result = false;

		if (updateItems != null && !updateItems.isEmpty()) {
			Logging.info(this, "updateProductOnClients  updateItems.size " + updateItems.size());
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_UPDATE_OBJECTS,
					new Object[] { updateItems });
			result = exec.doCall(omc);
			// at any rate
			updateItems.clear();
		}

		return result;
	}

	public boolean updateProductOnClients() {
		return updateProductOnClients(updateProductOnClientItems);
	}

	public boolean updateProductOnClients(Set<String> clients, String productName, int productType,
			Map<String, String> changedValues) {
		List<Map<String, Object>> updateCollection = new ArrayList<>();

		// collect updates for all clients
		for (String client : clients) {
			updateProductOnClient(client, productName, productType, changedValues, updateCollection);
		}

		// execute
		return updateProductOnClients(updateCollection);
	}

	public boolean resetLocalbootProducts(String[] selectedClients, boolean withDependencies) {
		if (globalReadOnly) {
			return false;
		}

		List<Map<String, Object>> deleteProductItems = produceDeleteProductItems(selectedClients,
				OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
		Logging.info(this, "resetLocalbootProducts deleteProductItems.size " + deleteProductItems.size());
		boolean result = resetProducts(deleteProductItems, withDependencies);
		Logging.debug(this, "resetLocalbootProducts result " + result);
		return result;
	}

	public boolean resetNetbootProducts(String[] selectedClients, boolean withDependencies) {
		if (globalReadOnly) {
			return false;
		}

		List<Map<String, Object>> deleteProductItems = produceDeleteProductItems(selectedClients,
				OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
		Logging.info(this, "resetNetbootProducts deleteProductItems.size " + deleteProductItems.size());
		boolean result = resetProducts(deleteProductItems, withDependencies);
		Logging.debug(this, "resetNetbootProducts result " + result);
		return result;
	}

	private List<Map<String, Object>> produceDeleteProductItems(String[] selectedClients, String productType) {
		List<Map<String, Object>> deleteProductItems = new ArrayList<>();
		List<Map<String, Object>> modifiedProductsOnClients = retrieveModifiedProductsOnClients(
				Arrays.asList(selectedClients));

		for (final String clientId : selectedClients) {
			List<String> modifiedProductsOnClient = modifiedProductsOnClients.stream()
					.filter(m -> clientId.equals(m.get("clientId"))).map(m -> (String) m.get("productId"))
					.collect(Collectors.toList());
			for (final String product : modifiedProductsOnClient) {
				Map<String, Object> productOnClientItem = createNOMitem("ProductOnClient");
				productOnClientItem.put("productType", productType);
				productOnClientItem.put("clientId", clientId);
				productOnClientItem.put("productId", product);
				deleteProductItems.add(productOnClientItem);
			}
		}

		return deleteProductItems;
	}

	private List<Map<String, Object>> retrieveModifiedProductsOnClients(List<String> clientIds) {
		String[] callAttributes = new String[] {};
		HashMap<String, Object> callFilter = new HashMap<>();
		callFilter.put("clientId", clientIds);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		return exec.getListOfMaps(omc);
	}

	private boolean resetProducts(Collection<Map<String, Object>> productItems, boolean withDependencies) {
		if (globalReadOnly) {
			return false;
		}

		boolean result = true;

		Logging.info(this, "resetProducts productItems.size " + productItems.size());

		if (!productItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_DELETE_OBJECTS,
					new Object[] { productItems.toArray() });

			result = exec.doCall(omc);

			Logging.debug(this, "resetProducts result " + result);

			if (result && withDependencies) {
				omc = new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_DELETE,
						new Object[] { productItems.stream().map(p -> p.get("productId")).toArray(), "*",
								productItems.stream().map(p -> p.get("clientId")).toArray() });

				result = exec.doCall(omc);
			}
		}

		Logging.debug(this, "resetProducts result " + result);

		return result;
	}

	private List<Map<String, String>> getProductDependencies(String depotId, String productId) {
		List<Map<String, String>> result = null;

		if (persistentDataRetriever.getDepot2product2dependencyInfos().get(depotId) != null) {
			result = persistentDataRetriever.getDepot2product2dependencyInfos().get(depotId).get(productId);
		}

		if (result == null) {
			result = new ArrayList<>();
		}

		Logging.debug(this,
				"getProductDependencies for depot, product " + depotId + ", " + productId + " , result " + result);
		return result;
	}

	public Map<String, List<Map<String, String>>> getProductDependencies(String depotId) {
		Map<String, List<Map<String, String>>> result = null;

		if (persistentDataRetriever.getDepot2product2dependencyInfos().get(depotId) != null) {
			result = persistentDataRetriever.getDepot2product2dependencyInfos().get(depotId);
		} else {
			result = new HashMap<>();
		}

		return result;
	}

	public Set<String> extendToDependentProducts(final Set<String> startProductSet, final String depot) {
		HashSet<String> notHandled = new HashSet<>(startProductSet);
		HashSet<String> endResultSet = new HashSet<>(startProductSet);
		HashSet<String> startResultSet = null;

		while (!notHandled.isEmpty()) {
			startResultSet = new HashSet<>(endResultSet);

			for (String prod : notHandled) {
				Logging.info(this, " extendToDependentProducts prod " + prod);
				for (Map<String, String> m : getProductDependencies(depot, prod)) {
					Logging.info(this, " extendToDependentProducts m " + m.get("requiredProductId"));
					endResultSet.add(m.get("requiredProductId"));
				}
			}

			notHandled = new HashSet<>(endResultSet);
			notHandled.removeAll(startResultSet);
		}

		return new TreeSet<>(endResultSet);
	}

	public Boolean hasClientSpecificProperties(String productname) {
		return productHavingClientSpecificProperties.get(productname);
	}

	public List<Map<String, Object>> retrieveListOfMapsNOM(RPCMethodName methodName) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		return retrieveListOfMapsNOM(callAttributes, callFilter, methodName);
	}

	public List<Map<String, Object>> retrieveListOfMapsNOM(String[] callAttributes, Map<?, ?> callFilter,
			RPCMethodName methodName) {
		List<Map<String, Object>> retrieved = exec
				.getListOfMaps(new OpsiMethodCall(methodName, new Object[] { callAttributes, callFilter }));
		Logging.debug(this, "retrieveListOfMapsNOM: " + retrieved);
		return retrieved;
	}

	public List<Map<String, Object>> retrieveListOfMapsNOM(RPCMethodName methodName, Object[] data) {
		List<Map<String, Object>> retrieved = exec.getListOfMaps(new OpsiMethodCall(methodName, data));
		Logging.debug(this, "retrieveListOfMapsNOM: " + retrieved);
		return retrieved;
	}

	public Map<String, Object> retrieveMapNOM(RPCMethodName methodName, Object[] data) {
		Map<String, Object> retrieved = exec.getMapResult(new OpsiMethodCall(methodName, data));
		Logging.debug(this, "retrieveMapNOM: " + retrieved);
		return retrieved;
	}

	// collect productPropertyState updates and deletions
	public void setProductProperties(String pcname, String productname, Map<?, ?> properties,
			List<Map<String, Object>> updateCollection, List<Map<String, Object>> deleteCollection) {
		if (!(properties instanceof ConfigName2ConfigValue)) {
			Logging.warning(this, "! properties instanceof ConfigName2ConfigValue ");
			return;
		}

		Iterator<?> propertiesKeyIterator = properties.keySet().iterator();

		while (propertiesKeyIterator.hasNext()) {
			String propertyId = (String) propertiesKeyIterator.next();

			List<?> newValue = (List<?>) properties.get(propertyId);

			Map<String, Object> retrievedConfig = ((RetrievedMap) properties).getRetrieved();
			Object oldValue = retrievedConfig == null ? null : retrievedConfig.get(propertyId);

			if (newValue != oldValue) {
				Map<String, Object> state = new HashMap<>();
				state.put("type", "ProductPropertyState");
				state.put("objectId", pcname);
				state.put("productId", productname);
				state.put("propertyId", propertyId);

				if (newValue == null || newValue.equals(MapTableModel.nullLIST)) {
					Logging.debug(this, "setProductProperties,  requested deletion " + properties.get(propertyId));
					deleteState(state, deleteCollection, retrievedConfig, propertyId);
				} else {
					Logging.debug(this, "setProductProperties,  requested update " + properties.get(propertyId)
							+ " for oldValue " + oldValue);

					state.put("values", newValue);
					updateState(state, updateCollection, retrievedConfig, propertyId, properties.get(propertyId));
				}
			}
		}
	}

	private static void deleteState(Map<String, Object> state, List<Map<String, Object>> deleteCollection,
			Map<String, Object> retrievedConfig, String propertyId) {
		deleteCollection.add(state);

		// we hope that the update works and directly update the retrievedConfig
		if (retrievedConfig != null) {
			retrievedConfig.remove(propertyId);
		}
	}

	private void updateState(Map<String, Object> state, List<Map<String, Object>> updateCollection,
			Map<String, Object> retrievedConfig, String propertyId, Object propertyValue) {

		Logging.debug(this, "setProductProperties,  we have new state " + state);
		updateCollection.add(state);

		// we hope that the update works and directly update the retrievedConfig
		if (retrievedConfig != null) {
			retrievedConfig.put(propertyId, propertyValue);
		}
	}

	// collect productPropertyState updates and deletions in standard lists
	public void setProductProperties(String pcname, String productname, Map<?, ?> properties) {
		// old version

		if (productPropertyStateUpdateCollection == null) {
			productPropertyStateUpdateCollection = new ArrayList<>();
		}

		if (productPropertyStateDeleteCollection == null) {
			productPropertyStateDeleteCollection = new ArrayList<>();
		}

		setProductProperties(pcname, productname, properties, productPropertyStateUpdateCollection,
				productPropertyStateDeleteCollection);
	}

	// send productPropertyState updates and clear the collections for standard
	// collections
	public void setProductProperties() {
		setProductProperties(productPropertyStateUpdateCollection, productPropertyStateDeleteCollection);
	}

	// send productPropertyState updates and clear the collections
	private void setProductProperties(List<Map<String, Object>> updateCollection, List<?> deleteCollection) {
		Logging.debug(this, "setProductproperties() ");

		if (globalReadOnly) {
			return;
		}

		if (updateCollection != null && !updateCollection.isEmpty()
				&& exec.doCall(new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_UPDATE_OBJECTS,
						new Object[] { updateCollection }))) {
			updateCollection.clear();
		}

		if (deleteCollection != null && !deleteCollection.isEmpty()
				&& exec.doCall(new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_DELETE_OBJECTS,
						new Object[] { deleteCollection }))) {
			deleteCollection.clear();
		}
	}

	public void setCommonProductPropertyValue(Iterable<String> clientNames, String productName, String propertyName,
			List<String> values) {
		List<Map<String, Object>> updateCollection = new ArrayList<>();
		List<Map<String, Object>> deleteCollection = new ArrayList<>();

		// collect updates for all clients
		for (String client : clientNames) {
			Map<String, Object> newdata = new ConfigName2ConfigValue(null);

			newdata.put(propertyName, values);

			// collect the updates
			setProductProperties(client, productName, newdata, updateCollection, deleteCollection);
		}
		// execute updates
		setProductProperties(updateCollection, deleteCollection);
	}

	public Map<String, ListCellOptions> getProductPropertyOptionsMap(String depotId, String productId) {
		Map<String, ListCellOptions> result = null;

		if (persistentDataRetriever.getDepot2Product2PropertyDefinitions().get(depotId) == null) {
			result = new HashMap<>();
			Logging.info("getProductPropertyOptionsMap: no productproperty definitions for depot " + depotId);
		} else {
			result = persistentDataRetriever.getDepot2Product2PropertyDefinitions().get(depotId).get(productId);
		}

		if (result == null) {
			Logging.info("getProductPropertyOptionsMap: no productproperty definitions  for depot, product " + depotId
					+ ", " + productId);
			result = new HashMap<>();
		}

		return result;
	}

	public Map<String, ListCellOptions> getProductPropertyOptionsMap(String productId) {
		// retrieveProductPropertyDefinitions();
		Map<String, ListCellOptions> result;

		if (productPropertyDefinitions == null) {
			result = new HashMap<>();
		} else {
			result = productPropertyDefinitions.get(productId);
			if (result == null) {
				result = new HashMap<>();
			}
		}

		return result;
	}

	public void productPropertyDefinitionsRequestRefresh() {
		//persistentDataRetriever.productPropertyDefinitionsRequestRefresh();
		productPropertyDefinitions = null;
	}

	public String getProductTitle(String product) {
		Logging.info(this, "getProductTitle for product " + product + " on depot " + theDepot);
		Logging.info(this, "getProductTitle for productGlobalsInfos found number " + productGlobalInfos.size());
		Logging.info(this, "getProductTitle, productInfos " + productGlobalInfos.get(product));
		Object result = productGlobalInfos.get(product).get(ProductState.KEY_PRODUCT_NAME);
		Logging.info(this, "getProductTitle for product " + result);

		String resultS = null;
		if (result == null) {
			resultS = EMPTYFIELD;
		} else {
			resultS = "" + result;
		}
		return resultS;
	}

	public String getProductInfo(String product) {
		String result = "" + productGlobalInfos.get(product).get(OpsiProductInfo.SERVICE_KEY_PRODUCT_DESCRIPTION);
		Logging.debug(this, " getProductInfo for product " + product + ": " + result);

		return result;
	}

	public String getProductHint(String product) {
		return (String) productGlobalInfos.get(product).get(OpsiProductInfo.SERVICE_KEY_PRODUCT_ADVICE);
	}

	public String getProductVersion(String product) {
		String result = (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);

		if (result == null) {
			result = EMPTYFIELD;
		}

		Logging.debug(this, "getProductVersion which? " + result + " for product: " + product);

		return result;
	}

	public String getProductPackageVersion(String product) {
		return (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
	}

	public String getProductLockedInfo(String product) {
		return (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_LOCKED);
	}

	private Map<String, String> getProductRequirements(String depotId, String productname, String requirementType) {
		Map<String, String> result = new HashMap<>();

		String depot = null;
		if (depotId == null) {
			depot = theDepot;
		} else {
			depot = depotId;
		}

		Logging.debug(this,
				"getProductRequirements productname, requirementType  " + productname + ", " + requirementType);

		List<Map<String, String>> dependenciesFor1product = getProductDependencies(depot, productname);

		if (dependenciesFor1product == null) {
			return result;
		}

		for (Map<String, String> aDependency : dependenciesFor1product) {
			Logging.debug(this, " dependency map : " + aDependency);

			if (requirementType.equals(NAME_REQUIREMENT_TYPE_ON_DEINSTALL)
					// we demand information for this type,
					// this is not specified by type in the dependency map
					// but only by the action value
					&& aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.UNINSTALL))) {
				result.put(aDependency.get("requiredProductId"),
						aDependency.get("requiredInstallationStatus") + ":" + aDependency.get("requiredAction"));
			} else {
				Logging.debug(this, " dependency map : ");

				boolean hasRequirementType = requirementType.equals(NAME_REQUIREMENT_TYPE_NEUTRAL)
						|| requirementType.equals(NAME_REQUIREMENT_TYPE_BEFORE)
						|| requirementType.equals(NAME_REQUIREMENT_TYPE_AFTER);
				boolean hasActionRequest = aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.SETUP))
						|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.ONCE))
						|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.ALWAYS))
						|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.CUSTOM));

				if (hasRequirementType && hasActionRequest
						&& aDependency.get("requirementType").equals(requirementType)) {
					result.put(aDependency.get("requiredProductId"),
							aDependency.get("requiredInstallationStatus") + ":" + aDependency.get("requiredAction"));
				}
			}
		}

		Logging.debug(this, "getProductRequirements depot, productname, requirementType  " + depotId + ", "
				+ productname + ", " + requirementType);
		Logging.info(this, "getProductRequirements " + result);

		return result;
	}

	public Map<String, String> getProductPreRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_BEFORE);
	}

	public Map<String, String> getProductRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_NEUTRAL);
	}

	public Map<String, String> getProductPostRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_AFTER);
	}

	public Map<String, String> getProductDeinstallRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_ON_DEINSTALL);
	}

	public void productpropertiesRequestRefresh() {
		//persistentDataRetriever.productPropertyStatesRequestRefresh();
		productProperties = null;
	}

	private boolean setHostBooleanConfigValue(String configId, String hostName, boolean val) {
		Logging.info(this, "setHostBooleanConfigValue " + hostName + " configId " + configId + " val " + val);

		List<Object> values = new ArrayList<>();
		values.add(val);

		Map<String, Object> item = createNOMitem(CONFIG_STATE_TYPE);
		item.put(OBJECT_ID, hostName);
		item.put(VALUES_ID, values);
		item.put(CONFIG_ID, configId);

		List<Map<String, Object>> jsonObjects = new ArrayList<>();
		jsonObjects.add(item);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
				new Object[] { jsonObjects });

		return exec.doCall(omc);
	}

	private Boolean getHostBooleanConfigValue(String key, String hostName, boolean useGlobalFallback,
			Boolean defaultVal) {

		Logging.debug(this, "getHostBooleanConfigValue key '" + key + "', host '" + hostName + "'");
		Boolean value = null;

		Map<String, Object> hostConfig = getConfigs().get(hostName);
		if (hostConfig != null && hostConfig.get(key) != null && !((List<?>) (hostConfig.get(key))).isEmpty()) {
			value = interpretAsBoolean(((List<?>) hostConfig.get(key)).get(0), (Boolean) null);
			Logging.debug(this,
					"getHostBooleanConfigValue key '" + key + "', host '" + hostName + "', value: " + value);
			if (value != null) {
				return value;
			}
		}

		if (useGlobalFallback) {
			value = getGlobalBooleanConfigValue(key, null);
			if (value != null) {
				Logging.debug(this,
						"getHostBooleanConfigValue key '" + key + "', host '" + hostName + "', global value: " + value);
				return value;
			}
		}
		Logging.info(this, "getHostBooleanConfigValue key '" + key + "', host '" + hostName
				+ "', returning default value: " + defaultVal);
		return defaultVal;
	}

	public Boolean getGlobalBooleanConfigValue(String key, Boolean defaultVal) {
		Boolean val = defaultVal;
		Object obj = persistentDataRetriever.getConfigOptions().get(key);

		Logging.debug(this, "getGlobalBooleanConfigValue '" + key + "'='" + obj + "'");
		if (obj == null) {
			Logging.warning(this, "getGlobalBooleanConfigValue '" + key + "' is null, returning default value: " + val);
			return val;
		}

		ConfigOption option = (ConfigOption) obj;
		if (option.getType() != ConfigOption.TYPE.BOOL_CONFIG) {
			Logging.warning(this, "getGlobalBooleanConfigValue type of '" + key + "' should be boolean, but is "
					+ option.getType() + ", returning default value: " + val);
			return val;

		}

		List<Object> values = option.getDefaultValues();
		Logging.debug(this, "getGlobalBooleanConfigValue '" + key + "' defaultValues: " + values);
		if (values != null && !values.isEmpty()) {
			val = (Boolean) values.get(0);
		}

		return val;
	}

	public void configOptionsRequestRefresh() {
		Logging.info(this, "configOptionsRequestRefresh");
		configOptions = null;
	}

	public void hostConfigsRequestRefresh() {
		//persistentDataRetriever.hostConfigsRequestRefresh();
	}

	public Map<String, Map<String, Object>> getConfigs() {
		return persistentDataRetriever.getConfigs();
	}

	public Map<String, Object> getConfig(String objectId) {
		persistentDataRetriever.getConfigOptions();

		Map<String, Object> retrieved = getConfigs().get(objectId);

		return new ConfigName2ConfigValue(retrieved, configOptions);
	}

	public void setHostValues(Map<String, Object> settings) {
		if (globalReadOnly) {
			return;
		}

		List<Map<String, Object>> hostMaps = new ArrayList<>();

		Map<String, Object> corrected = new HashMap<>();
		for (Entry<String, Object> setting : settings.entrySet()) {
			if (setting.getValue() instanceof String && "".equals(((String) setting.getValue()).trim())) {
				corrected.put(setting.getKey(), JSONObject.NULL);
			} else {
				corrected.put(setting.getKey(), setting.getValue());
			}
		}

		hostMaps.add(corrected);

		exec.doCall(new OpsiMethodCall(RPCMethodName.HOST_CREATE_OBJECTS, new Object[] { hostMaps }));
	}

	// collect config state updates
	public void setAdditionalConfiguration(String objectId, ConfigName2ConfigValue settings) {
		if (configStateCollection == null) {
			configStateCollection = new ArrayList<>();
		}

		Set<String> currentKeys = settings.keySet();
		Logging.info(this, "setAdditionalConfigurations current keySet size: " + currentKeys.size());
		if (settings.getRetrieved() != null) {
			Set<String> retrievedKeys = settings.getRetrieved().keySet();

			Logging.info(this, "setAdditionalConfigurations retrieved keys size  " + retrievedKeys.size());

			Set<String> removedKeys = new HashSet<>(retrievedKeys);
			removedKeys.removeAll(currentKeys);
			Logging.info(this, "setAdditionalConfigurations removed " + removedKeys);

			if (!removedKeys.isEmpty()) {
				if (deleteConfigStateItems == null) {
					deleteConfigStateItems = new ArrayList<>();
				}

				for (Object key : removedKeys) {
					String ident = "" + key + ";" + objectId;

					Map<String, Object> item = createNOMitem("ConfigState");
					item.put("ident", ident);
					deleteConfigStateItems.add(item);
				}
			}
		}

		for (Map.Entry<String, Object> entry : settings.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			Map<String, Object> state = new HashMap<>();

			state.put("type", "ConfigState");
			state.put("objectId", objectId);
			state.put("configId", key);
			state.put("values", value);

			Map<String, Object> retrievedConfig = settings.getRetrieved();
			Object oldValue = null;

			if (retrievedConfig != null) {
				oldValue = retrievedConfig.get(key);
			}

			if (value != oldValue) {
				configStateCollection.add(state);

				// we hope that the update works and directly update the retrievedConfig
				if (retrievedConfig != null) {
					retrievedConfig.put(key, value);
				}
			}
		}
	}

	// send config updates and clear the collection
	public void setAdditionalConfiguration() {
		if (globalReadOnly) {
			return;
		}

		if (Utils.checkCollection(this, "configStateCollection", configStateCollection)
				&& !configStateCollection.isEmpty()) {
			boolean configsChanged = false;

			if (deleteConfigStateItems == null) {
				deleteConfigStateItems = new ArrayList<>();
			}

			// add configId where necessary
			Set<String> usedConfigIds = new HashSet<>();
			Map<String, String> typesOfUsedConfigIds = new HashMap<>();

			List<Object> doneList = new ArrayList<>();

			for (Map<String, Object> configState : configStateCollection) {
				String ident = (String) configState.get("configId");
				usedConfigIds.add(ident);

				List<?> valueList = (List<?>) configState.get("values");

				if (!valueList.isEmpty() && valueList.get(0) instanceof Boolean) {
					typesOfUsedConfigIds.put(ident, "BoolConfig");
				} else {
					typesOfUsedConfigIds.put(ident, "UnicodeConfig");
				}

				if (valueList.equals(MapTableModel.nullLIST)) {
					Map<String, Object> item = createNOMitem("ConfigState");
					item.put("objectId", configState.get("objectId"));
					item.put("configId", configState.get("configId"));

					deleteConfigStateItems.add(item);

					doneList.add(configState);
				}
			}
			Logging.debug(this, "setAdditionalConfiguration(), usedConfigIds: " + usedConfigIds);

			Logging.debug(this, "setAdditionalConfiguration(), deleteConfigStateItems  " + deleteConfigStateItems);
			// not used
			if (!deleteConfigStateItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_DELETE_OBJECTS,
						new Object[] { deleteConfigStateItems.toArray() });

				if (exec.doCall(omc)) {
					deleteConfigStateItems.clear();
					configStateCollection.removeAll(doneList);
				}
			}

			List<Object> existingConfigIds = exec
					.getListResult(new OpsiMethodCall(RPCMethodName.CONFIG_GET_IDENTS, new Object[] {}));
			Logging.debug(this, "setAdditionalConfiguration(), existingConfigIds: " + existingConfigIds.size());

			Set<String> missingConfigIds = new HashSet<>(usedConfigIds);
			missingConfigIds.removeAll(existingConfigIds);

			Logging.debug(this, "setAdditionalConfiguration(), missingConfigIds: " + missingConfigIds);
			List<Map<String, Object>> createItems = new ArrayList<>();
			for (String missingId : missingConfigIds) {
				Map<String, Object> item = createNOMitem(typesOfUsedConfigIds.get(missingId));
				item.put("ident", missingId);
				createItems.add(item);
			}

			if (!createItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_CREATE_OBJECTS,
						new Object[] { createItems.toArray() });
				exec.doCall(omc);
				configsChanged = true;
			}

			if (configsChanged) {
				configOptionsRequestRefresh();
				persistentDataRetriever.getConfigOptions();
			}

			// build calls

			List<Map<String, Object>> callsConfigName2ConfigValueCollection = new ArrayList<>();
			List<Map<String, Object>> callsConfigCollection = new ArrayList<>();

			for (Map<String, Object> state : configStateCollection) {

				state.put("values", state.get("values"));
				callsConfigName2ConfigValueCollection.add(state);
			}

			Logging.debug(this, "callsConfigCollection " + callsConfigCollection);
			if (!callsConfigCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS,
						new Object[] { callsConfigCollection }));
			}

			// do call

			// now we can set the values and clear the collected update items
			exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_STATE_UPDATE_OBJECTS,
					new Object[] { callsConfigName2ConfigValueCollection }));

			// at any rate:
			configStateCollection.clear();
		}
	}

	// collect config updates
	public void setConfig(Map<String, List<Object>> settings) {
		Logging.debug(this, "setConfig settings " + settings);
		if (configCollection == null) {
			configCollection = new ArrayList<>();
		}

		for (Entry<String, List<Object>> setting : settings.entrySet()) {
			Logging.debug(this, "setConfig,  key, settings.get(key): " + setting.getKey() + ", " + setting.getValue());

			if (setting.getValue() != null) {
				Logging.debug(this, "setConfig,  settings.get(key), settings.get(key).getClass().getName(): "
						+ setting.getValue() + " , " + setting.getValue().getClass().getName());

				if (setting.getValue() instanceof List) {
					List<Object> oldValue = null;

					if (configOptions.get(setting.getKey()) != null) {
						oldValue = configOptions.get(setting.getKey()).getDefaultValues();
					}

					Logging.info(this, "setConfig, key, oldValue: " + setting.getKey() + ", " + oldValue);

					List<Object> valueList = setting.getValue();

					if (valueList != null && !valueList.equals(oldValue)) {
						Map<String, Object> config = new HashMap<>();

						config.put("ident", setting.getKey());

						String type = "UnicodeConfig";

						Logging.debug(this, "setConfig, key,  configOptions.get(key):  " + setting.getKey() + ", "
								+ configOptions.get(setting.getKey()));
						if (configOptions.get(setting.getKey()) != null) {
							type = (String) configOptions.get(setting.getKey()).get("type");
						} else {
							if (!valueList.isEmpty() && valueList.get(0) instanceof Boolean) {
								type = "BoolConfig";
							}
						}

						config.put("type", type);

						config.put("defaultValues", valueList);

						List<Object> possibleValues = null;
						if (configOptions.get(setting.getKey()) == null) {
							possibleValues = new ArrayList<>();
							if (type.equals(ConfigOption.BOOL_TYPE)) {
								possibleValues.add(true);
								possibleValues.add(false);
							}
						} else {
							possibleValues = configOptions.get(setting.getKey()).getPossibleValues();
						}

						for (Object item : valueList) {
							if (possibleValues.indexOf(item) == -1) {
								possibleValues.add(item);
							}
						}

						config.put("possibleValues", possibleValues);

						configCollection.add(config);
					}
				} else {
					Logging.error("setConfig,  setting.getKey(), setting.getValue(): " + setting.getKey() + ", "
							+ setting.getValue() + " \nUnexpected type");
				}
			}
		}
	}

	// send config updates and clear the collection
	public void setConfig() {
		setConfig(false);
	}

	// send config updates, possibly not updating existing

	private void setConfig(boolean restrictToMissing) {
		Logging.info(this, "setConfig(),  configCollection null " + (configCollection == null));
		if (configCollection != null) {
			Logging.info(this, "setConfig(),  configCollection size  " + configCollection.size());
		}

		if (globalReadOnly) {
			return;
		}

		if (configCollection != null && !configCollection.isEmpty()) {
			// add configId where necessary
			List<String> usedConfigIds = new ArrayList<>();
			Map<String, String> typesOfUsedConfigIds = new HashMap<>();
			for (Map<String, Object> config : configCollection) {
				String ident = (String) config.get("ident");
				usedConfigIds.add(ident);
				typesOfUsedConfigIds.put(ident, (String) config.get("type"));
			}

			Logging.debug(this, "setConfig(), usedConfigIds: " + usedConfigIds);

			List<Object> existingConfigIds = exec
					.getListResult(new OpsiMethodCall(RPCMethodName.CONFIG_GET_IDENTS, new Object[] {}));

			Logging.info(this, "setConfig(), existingConfigIds: " + existingConfigIds.size());

			List<String> missingConfigIds = new ArrayList<>(usedConfigIds);
			for (Object configId : existingConfigIds) {
				missingConfigIds.remove(configId);
			}
			Logging.info(this, "setConfig(), missingConfigIds: " + missingConfigIds);
			List<Map<String, Object>> createItems = new ArrayList<>();
			for (String missingId : missingConfigIds) {
				Map<String, Object> item = createNOMitem(typesOfUsedConfigIds.get(missingId));
				item.put("ident", missingId);
				createItems.add(item);
			}

			if (!createItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_CREATE_OBJECTS,
						new Object[] { createItems.toArray() });
				exec.doCall(omc);
			}

			// remap to JSON types
			List<Map<String, Object>> callsConfigUpdateCollection = new ArrayList<>();
			List<Map<String, Object>> callsConfigDeleteCollection = new ArrayList<>();

			for (Map<String, Object> callConfig : configCollection) {

				if (callConfig.get("defaultValues") == MapTableModel.nullLIST) {
					callsConfigDeleteCollection.add(callConfig);
				} else {
					Logging.debug(this, "setConfig config with ident " + callConfig.get("ident"));

					boolean isMissing = missingConfigIds.contains(callConfig.get("ident"));

					if (!restrictToMissing || isMissing) {
						callConfig.put("defaultValues", callConfig.get("defaultValues"));
						callConfig.put("possibleValues", callConfig.get("possibleValues"));
						callsConfigUpdateCollection.add(callConfig);
					}
				}
			}

			Logging.debug(this, "setConfig() callsConfigUpdateCollection " + callsConfigUpdateCollection);

			if (!callsConfigDeleteCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS,
						new Object[] { callsConfigDeleteCollection }));
				configOptionsRequestRefresh();
				// because of referential integrity
				hostConfigsRequestRefresh();
			}

			Logging.debug(this, "setConfig() callsConfigUpdateCollection " + callsConfigUpdateCollection);

			if (!callsConfigUpdateCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS,
						new Object[] { callsConfigUpdateCollection }));
				configOptionsRequestRefresh();
			}

			persistentDataRetriever.getConfigOptions();
			configCollection.clear();

			Logging.info(this, "setConfig(),  configCollection result: " + configCollection);
		}
	}

	/**
	 * signals that the default domain shall be reloaded from service
	 */
	public void requestReloadOpsiDefaultDomain() {
		opsiDefaultDomain = null;
	}

	public List<String> getDomains() {
		List<String> result = new ArrayList<>();

		if (configDefaultValues.get(CONFIGED_GIVEN_DOMAINS_KEY) == null) {
			Logging.info(this, "no values found for   " + CONFIGED_GIVEN_DOMAINS_KEY);
		} else {
			Logging.info(this, "getDomains " + configDefaultValues.get(CONFIGED_GIVEN_DOMAINS_KEY));

			HashMap<String, Integer> numberedValues = new HashMap<>();
			TreeSet<String> orderedValues = new TreeSet<>();
			TreeSet<String> unorderedValues = new TreeSet<>();

			for (Object item : configDefaultValues.get(CONFIGED_GIVEN_DOMAINS_KEY)) {
				String entry = (String) item;
				int p = entry.indexOf(":");
				if (p == -1 || p == 0) {
					unorderedValues.add(entry);
				} else if (p > 0) {
					// the only regular case
					int orderNumber = -1;
					try {
						orderNumber = Integer.valueOf(entry.substring(0, p));
						String value = entry.substring(p + 1);
						if (numberedValues.get(value) == null || orderNumber < numberedValues.get(value)) {
							orderedValues.add(entry);
							numberedValues.put(value, orderNumber);
						}
					} catch (NumberFormatException x) {
						Logging.warning(this, "illegal order format for domain entry: " + entry);
						unorderedValues.add(entry);
					}
				} else {
					Logging.warning(this, "p has unexpected value " + p);
				}
			}

			for (String entry : orderedValues) {
				int p = entry.indexOf(":");
				result.add(entry.substring(p + 1));
			}

			unorderedValues.removeAll(result);

			for (String entry : unorderedValues) {
				result.add(entry);
			}
		}

		Logging.info(this, "getDomains " + result);
		return result;
	}

	public void writeDomains(List<Object> domains) {
		String key = CONFIGED_GIVEN_DOMAINS_KEY;
		Map<String, Object> item = createNOMitem("UnicodeConfig");

		item.put("ident", key);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", domains);
		item.put("possibleValues", domains);
		item.put("editable", true);
		item.put("multiValue", true);

		List<Map<String, Object>> readyObjects = new ArrayList<>();
		readyObjects.add(item);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

		exec.doCall(omc);

		configDefaultValues.put(key, domains);
	}

	public void setDepot(String depotId) {
		Logging.info(this, "setDepot: " + depotId);
		theDepot = depotId;
	}

	public String getDepot() {
		return theDepot;
	}

	public void installedSoftwareInformationRequestRefresh() {
		Logging.info(this, " call installedSoftwareInformationRequestRefresh()");
		//persistentDataRetriever.installedSoftwareInformationRequestRefresh();
	}

	// returns the ID of the edited data record
	public String editLicenceContract(String licenseContractId, String partner, String conclusionDate,
			String notificationDate, String expirationDate, String notes) {
		if (!serverFullPermission) {
			return "";
		}
		String result = "";

		Logging.debug(this, "editLicenceContract " + licenseContractId);

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_CONTRACT_CREATE, new String[] {
					licenseContractId, "", notes, partner, conclusionDate, notificationDate, expirationDate });

			// the method gives the first letter instead of the complete string as return
			// value, therefore we set it in a shortcut:

			if (exec.doCall(omc)) {
				result = licenseContractId;
			} else {
				Logging.error(this, "could not create license " + licenseContractId);
			}
		}

		Logging.debug(this, "editLicenceContract result " + result);

		return result;
	}

	public boolean deleteLicenceContract(String licenseContractId) {
		if (!serverFullPermission) {
			return false;
		}

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_CONTRACT_DELETE,
					new String[] { licenseContractId });
			return exec.doCall(omc);
		}

		return false;
	}

	// returns the ID of the edited data record
	public String editLicencePool(String licensePoolId, String description) {
		if (!serverFullPermission) {
			return "";
		}

		String result = "";

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_CREATE,
					new String[] { licensePoolId, description });

			if (exec.doCall(omc)) {
				result = licensePoolId;
			} else {
				Logging.warning(this, "could not create licensepool " + licensePoolId);
			}
		}

		return result;
	}

	public boolean deleteLicencePool(String licensePoolId) {
		Logging.info(this, "deleteLicencePool " + licensePoolId);

		if (!serverFullPermission) {
			return false;
		}

		if (withLicenceManagement) {
			// does not get reach into the crucial data structures
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_DELETE, new Object[] { licensePoolId });
			return exec.doCall(omc);
			// comes too late
		}

		return false;
	}

	// without internal caching
	public Map<String, LicenceEntry> getSoftwareLicences() {
		Map<String, LicenceEntry> softwareLicences = new HashMap<>();

		if (withLicenceManagement) {
			//persistentDataRetriever.licencesRequestRefresh();
			softwareLicences = persistentDataRetriever.getLicences();
		}
		return softwareLicences;
	}

	// returns the ID of the edited data record
	public String editSoftwareLicence(String softwareLicenseId, String licenceContractId, String licenceType,
			String maxInstallations, String boundToHost, String expirationDate) {
		if (!serverFullPermission) {
			return "";
		}

		String result = "";

		if (withLicenceManagement) {
			RPCMethodName methodName = null;
			switch (licenceType) {
			case LicenceEntry.VOLUME:
				methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_VOLUME;
				break;
			case LicenceEntry.OEM:
				methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_OEM;
				break;
			case LicenceEntry.CONCURRENT:
				methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_CONCURRENT;
				break;
			case LicenceEntry.RETAIL:
				methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_RETAIL;
				break;
			default:
				Logging.notice(this, "encountered UNKNOWN license type");
				break;
			}

			// The jsonRPC-calls would fail sometimes if we use empty / blank Strings...
			if (maxInstallations != null && maxInstallations.isBlank()) {
				maxInstallations = null;
			}

			if (boundToHost != null && boundToHost.isBlank()) {
				boundToHost = null;
			}

			if (expirationDate != null && expirationDate.isBlank()) {
				expirationDate = null;
			}

			OpsiMethodCall omc = new OpsiMethodCall(methodName, new String[] { softwareLicenseId, licenceContractId,
					maxInstallations, boundToHost, expirationDate });

			if (exec.doCall(omc)) {
				result = softwareLicenseId;
			} else {
				Logging.error(this, "could not execute " + methodName + "  with softwareLicenseId " + softwareLicenseId
						+ " and licenseContractId " + licenceContractId);
			}
		}

		return result;
	}

	public boolean deleteSoftwareLicence(String softwareLicenseId) {
		if (!serverFullPermission) {
			return false;
		}

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_DELETE,
					new Object[] { softwareLicenseId });
			return exec.doCall(omc);
		}

		return false;
	}

	// without internal caching; legacy license method
	public Map<String, Map<String, Object>> getRelationsSoftwareL2LPool() {
		Map<String, Map<String, Object>> rowsSoftwareL2LPool = new HashMap<>();

		if (withLicenceManagement) {
			List<String> callAttributes = new ArrayList<>();
			Map<String, Object> callFilter = new HashMap<>();
			List<Map<String, Object>> softwareL2LPools = exec
					.getListOfMaps(new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_GET_OBJECTS,
							new Object[] { callAttributes, callFilter }));

			for (Map<String, Object> softwareL2LPool : softwareL2LPools) {
				softwareL2LPool.remove("ident");
				softwareL2LPool.remove("type");

				rowsSoftwareL2LPool
						.put(Utils.pseudokey(new String[] { (String) softwareL2LPool.get("softwareLicenseId"),
								(String) softwareL2LPool.get("licensePoolId") }), softwareL2LPool);
			}
		}

		return rowsSoftwareL2LPool;
	}

	public String editRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId, String licenseKey) {
		if (!serverFullPermission) {
			return "";
		}

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_CREATE,
					new String[] { softwareLicenseId, licensePoolId, licenseKey });

			if (!exec.doCall(omc)) {
				Logging.error(this, "cannot create softwarelicense to licensepool relation");
				return "";
			}
		}

		return Utils.pseudokey(new String[] { softwareLicenseId, licensePoolId });
	}

	public boolean deleteRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId) {
		if (!serverFullPermission) {
			return false;
		}

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_FROM_LICENSE_POOL_DELETE,
					new String[] { softwareLicenseId, licensePoolId });

			return exec.doCall(omc);
		}

		return false;
	}

	// without internal caching
	public Map<String, Map<String, String>> getRelationsProductId2LPool() {
		HashMap<String, Map<String, String>> rowsLicencePoolXOpsiProduct = new HashMap<>();

		if (withLicenceManagement) {
			//persistentDataRetriever.licencePoolXOpsiProductRequestRefresh();
			persistentDataRetriever.getLicencePoolXOpsiProduct();
			Logging.info(this,
					"licencePoolXOpsiProduct size " + persistentDataRetriever.getLicencePoolXOpsiProduct().size());

			for (StringValuedRelationElement element : persistentDataRetriever.getLicencePoolXOpsiProduct()) {
				rowsLicencePoolXOpsiProduct
						.put(Utils.pseudokey(new String[] { element.get(LicencePoolXOpsiProduct.LICENCE_POOL_KEY),
								element.get(LicencePoolXOpsiProduct.PRODUCT_ID_KEY) }), element);
			}
		}

		Logging.info(this, "rowsLicencePoolXOpsiProduct size " + rowsLicencePoolXOpsiProduct.size());

		return rowsLicencePoolXOpsiProduct;
	}

	public String editRelationProductId2LPool(String productId, String licensePoolId) {
		if (!serverFullPermission) {
			return "";
		}

		String result = "";

		if (withLicenceManagement) {
			Map<String, Object> licensePool = getLicensePool(licensePoolId);

			// Replace old product list with actualized list
			List<Object> licensePoolProductIds = new ArrayList<>((List<?>) licensePool.get("productIds"));
			licensePoolProductIds.add(productId);
			licensePool.put("productIds", licensePoolProductIds);

			if (exec.doCall(
					new OpsiMethodCall(RPCMethodName.LICENSE_POOL_UPDATE_OBJECT, new Object[] { licensePool }))) {
				result = licensePoolId;
			} else {
				Logging.error(this, "could not update product " + productId + " to licensepool " + licensePoolId);
			}
		}

		return result;
	}

	public boolean deleteRelationProductId2LPool(String productId, String licensePoolId) {
		if (!serverFullPermission) {
			return false;
		}

		if (withLicenceManagement) {
			Map<String, Object> licensePool = getLicensePool(licensePoolId);
			// Replace old product list with actualized list
			List<Object> licensePoolProductIds = new ArrayList<>((List<?>) licensePool.get("productIds"));
			licensePoolProductIds.remove(productId);
			licensePool.put("productIds", licensePoolProductIds);

			return exec
					.doCall(new OpsiMethodCall(RPCMethodName.LICENSE_POOL_UPDATE_OBJECT, new Object[] { licensePool }));
		}

		return false;
	}

	public Map<String, Object> getLicensePool(String licensePoolId) {
		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("id", licensePoolId);
		return exec.getListOfMaps(
				new OpsiMethodCall(RPCMethodName.LICENSE_POOL_GET_OBJECTS, new Object[] { callAttributes, callFilter }))
				.get(0);
	}

	public void relationsAuditSoftwareToLicencePoolsRequestRefresh() {
		relationsAuditSoftwareToLicencePools = null;
		softwareWithoutAssociatedLicencePool = null;
		fLicencePool2SoftwareList = null;
		fLicencePool2UnknownSoftwareList = null;
	}

	public void setFSoftware2LicencePool(String softwareIdent, String licencePoolId) {
		fSoftware2LicencePool.put(softwareIdent, licencePoolId);
	}

	public boolean removeAssociations(String licencePoolId, List<String> softwareIds) {
		Logging.info(this, "removeAssociations licensePoolId, softwareIds " + licencePoolId + ", " + softwareIds);

		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (licencePoolId == null || softwareIds == null) {
			return result;
		}

		if (withLicenceManagement) {
			List<Map<String, String>> deleteItems = new ArrayList<>();

			for (String swIdent : softwareIds) {
				Map<String, String> item = new HashMap<>();
				item.put("ident", swIdent + ";" + licencePoolId);
				item.put("type", "AuditSoftwareToLicensePool");
				deleteItems.add(item);
			}

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_DELETE_OBJECTS,
					new Object[] { deleteItems.toArray() });
			result = exec.doCall(omc);

			if (result) {
				for (String swIdent : softwareIds) {
					fSoftware2LicencePool.remove(swIdent);
				}

				if (fLicencePool2SoftwareList.get(licencePoolId) != null) {
					fLicencePool2SoftwareList.get(licencePoolId).removeAll(softwareIds);
				}

				if (fLicencePool2UnknownSoftwareList.get(licencePoolId) != null) {
					fLicencePool2UnknownSoftwareList.get(licencePoolId).removeAll(softwareIds);
				}
			}
		}

		return result;
	}

	public boolean setWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign) {
		return setWindowsSoftwareIds2LPool(licensePoolId, softwareToAssign, false);
	}

	public boolean addWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign) {
		return setWindowsSoftwareIds2LPool(licensePoolId, softwareToAssign, true);
	}

	private boolean setWindowsSoftwareIds2LPool(String licensePoolId, final List<String> softwareToAssign,
			boolean onlyAdding) {
		Logging.debug(this, "setWindowsSoftwareIds2LPool  licensePoolId,  softwareToAssign:" + licensePoolId + " , "
				+ softwareToAssign);

		if (!serverFullPermission) {
			return false;
		}

		boolean result = true;

		if (withLicenceManagement) {
			Map<String, SWAuditEntry> instSwI = persistentDataRetriever.getInstalledSoftwareInformationForLicensing();

			List<String> oldEntries = fLicencePool2SoftwareList.computeIfAbsent(licensePoolId,
					arg -> new ArrayList<>());

			List<String> oldEntriesTruely = new ArrayList<>(oldEntries);
			List<String> softwareToAssignTruely = new ArrayList<>(softwareToAssign);

			Set<String> entriesToRemove = new HashSet<>();

			// we work only with real changes
			softwareToAssignTruely.removeAll(oldEntries);
			oldEntriesTruely.removeAll(softwareToAssign);

			Logging.info(this, "setWindowsSoftwareIds2LPool softwareToAssignTruely " + softwareToAssignTruely);
			Logging.info(this, "setWindowsSoftwareIds2LPool oldEntriesTruely " + oldEntriesTruely);

			if (!onlyAdding) {
				ArrayList<Map<String, String>> deleteItems = new ArrayList<>();

				for (String swIdent : oldEntriesTruely) {
					// software exists in audit software
					if (instSwI.get(swIdent) != null) {
						entriesToRemove.add(swIdent);
						Map<String, String> item = new HashMap<>();
						item.put("ident", swIdent + ";" + licensePoolId);
						item.put("type", "AuditSoftwareToLicensePool");
						deleteItems.add(item);

						Logging.info(this, "" + instSwI.get(swIdent));
					}
				}
				Logging.info(this, "entriesToRemove " + entriesToRemove);
				Logging.info(this, "deleteItems " + deleteItems);

				if (!deleteItems.isEmpty()) {
					OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_DELETE_OBJECTS,
							new Object[] { deleteItems.toArray() });
					result = exec.doCall(omc);
				}

				if (!result) {
					return false;
				} else {
					// do it locally
					for (String swIdent : entriesToRemove) {
						instSwI.remove(swIdent);
					}
				}

			}

			ArrayList<Map<String, String>> createItems = new ArrayList<>();

			for (String swIdent : softwareToAssignTruely) {
				Map<String, String> item = new HashMap<>();
				item.put("ident", swIdent + ";" + licensePoolId);
				item.put("type", "AuditSoftwareToLicensePool");
				createItems.add(item);
			}

			Logging.info(this, "setWindowsSoftwareIds2LPool, createItems " + createItems);

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_CREATE_OBJECTS,
					new Object[] { createItems.toArray() });

			result = exec.doCall(omc);

			// we build the correct data locally
			if (result) {
				HashSet<String> intermediateSet = new HashSet<>(fLicencePool2SoftwareList.get(licensePoolId));
				intermediateSet.removeAll(entriesToRemove);
				intermediateSet.addAll(softwareToAssign);
				// dont delete old entries but avoid double entries
				List<String> newList = new ArrayList<>(intermediateSet);
				fLicencePool2SoftwareList.put(licensePoolId, newList);

				softwareWithoutAssociatedLicencePool.addAll(entriesToRemove);
				softwareWithoutAssociatedLicencePool.removeAll(softwareToAssign);

				Logging.info(this, "setWindowsSoftwareIds2LPool licencePool, fLicencePool2SoftwareList " + licensePoolId
						+ " : " + fLicencePool2SoftwareList.get(licensePoolId));

				for (String ident : newList) {
					// give zero length parts as ""
					String[] parts = ident.split(";", -1);
					String swName = parts[1];
					if (persistentDataRetriever.getName2SWIdents().get(swName) == null) {
						persistentDataRetriever.getName2SWIdents().put(swName, new TreeSet<>());
					}
					persistentDataRetriever.getName2SWIdents().get(swName).add(ident);

					Logging.info(this,
							"setWindowsSoftwareIds2LPool, collecting all idents for a name (even if not belonging to the pool), add ident "
									+ ident + " to set for name " + swName);
				}
			}
		}

		return result;
	}

	// we have got a SW from software table, therefore we do not serve the unknown
	// software list
	public String editPool2AuditSoftware(String softwareID, String licensePoolIDOld, String licencePoolIDNew) {
		if (!serverFullPermission) {
			return "";
		}

		String result = "";

		boolean ok = false;
		Logging.info(this, "editPool2AuditSoftware ");

		if (withLicenceManagement) {
			if (licensePoolIDOld != null && !licensePoolIDOld.equals(FSoftwarename2LicencePool.VALUE_NO_LICENCE_POOL)) {
				// there was an association, we delete it)

				List<String> swIds = new ArrayList<>();
				swIds.add(softwareID);
				ok = removeAssociations(licensePoolIDOld, swIds);

				if (!ok) {
					Logging.warning(this, "editPool2AuditSoftware " + " failed");
				}
			}

			if (FSoftwarename2LicencePool.VALUE_NO_LICENCE_POOL.equals(licencePoolIDNew)) {
				// nothing to do, we deleted the entry
				ok = true;
			} else {
				List<Map<String, Object>> readyObjects = new ArrayList<>();
				Map<String, Object> item;

				Map<String, String> swMap = AuditSoftwareXLicencePool.produceMapFromSWident(softwareID);
				swMap.put(LicencepoolEntry.ID_SERVICE_KEY, licencePoolIDNew);

				item = createNOMitem("AuditSoftwareToLicensePool");
				item.putAll(swMap);
				// create the edited entry

				readyObjects.add(item);

				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_CREATE_OBJECTS,
						new Object[] { readyObjects }

				);
				Logging.info(this, "editPool2AuditSoftware call " + omc);
				if (exec.doCall(omc)) {
					ok = true;
				} else {
					Logging.warning(this, "editPool2AuditSoftware " + omc + " failed");
				}
			}

			Logging.info(this, "editPool2AuditSoftware ok " + ok);

			if (ok) {
				Logging.info(this, "fSoftware2LicencePool == null " + (fSoftware2LicencePool == null));

				if (fSoftware2LicencePool != null) {
					Logging.info(this,
							"fSoftware2LicencePool.get( softwareID ) " + fSoftware2LicencePool.get(softwareID));
					fSoftware2LicencePool.put(softwareID, licencePoolIDNew);
				}
				List<String> fLicencePoolSoftwareList = fLicencePool2SoftwareList.computeIfAbsent(licencePoolIDNew,
						arg -> new ArrayList<>());

				Logging.info(this, "fLicencePool2SoftwareList.get( licencePoolIDNew ) " + fLicencePoolSoftwareList);

				fLicencePoolSoftwareList.add(softwareID);
			}

			return result;
		}

		return "???";
	}

	public List<String> getServerConfigStrings(String key) {
		persistentDataRetriever.getConfigOptions();
		return takeAsStringList(configDefaultValues.get(key));
	}

	public Map<String, LicencepoolEntry> getLicencepools() {
		//persistentDataRetriever.licencepoolsRequestRefresh();
		return persistentDataRetriever.getLicencepools();
	}

	// poolId -> LicenceStatisticsRow
	private Map<String, LicenceStatisticsRow> produceLicenceStatistics() {
		// side effects of this method: rowsLicencesReconciliation
		Logging.info(this, "produceLicenceStatistics === ");

		Map<String, List<String>> licencePool2listOfUsingClientsSWInvent = new HashMap<>();

		Map<String, Set<String>> licencePool2setOfUsingClientsSWInvent = new HashMap<>();

		// result
		Map<String, Integer> licencePoolUsagecountSWInvent = new HashMap<>();

		AuditSoftwareXLicencePool auditSoftwareXLicencePool = persistentDataRetriever.getAuditSoftwareXLicencePool();

		Map<String, Set<String>> swId2clients = persistentDataRetriever
				.getSoftwareIdent2clients(getHostInfoCollections().getOpsiHostNames());

		if (withLicenceManagement) {
			Map<String, LicencepoolEntry> licencePools = persistentDataRetriever.getLicencepools();

			if (rowsLicencesReconciliation == null) {
				rowsLicencesReconciliation = new HashMap<>();

				List<String> extraHostFields = getServerConfigStrings(
						KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION);

				Map<String, HostInfo> clientMap = hostInfoCollections.getMapOfAllPCInfoMaps();

				for (Entry<String, HostInfo> clientEntry : clientMap.entrySet()) {
					for (String pool : licencePools.keySet()) {
						HashMap<String, Object> rowMap = new HashMap<>();

						rowMap.put(HOST_KEY, clientEntry.getKey());

						for (String fieldName : extraHostFields) {
							rowMap.put(fieldName, clientEntry.getValue().getMap().get(fieldName));
						}

						rowMap.put("licensePoolId", pool);
						rowMap.put("used_by_opsi", false);
						rowMap.put("SWinventory_used", false);
						String pseudokey = Utils.pseudokey(new String[] { clientEntry.getKey(), pool });
						rowsLicencesReconciliation.put(pseudokey, rowMap);
					}
				}
			}

			persistentDataRetriever.getInstalledSoftwareInformationForLicensing();

			persistentDataRetriever.retrieveRelationsAuditSoftwareToLicencePools();

			// idents
			for (String softwareIdent : persistentDataRetriever.getInstalledSoftwareInformationForLicensing()
					.keySet()) {
				String licencePoolId = fSoftware2LicencePool.get(softwareIdent);

				if (licencePoolId != null) {
					List<String> listOfUsingClients = licencePool2listOfUsingClientsSWInvent
							.computeIfAbsent(licencePoolId, s -> new ArrayList<>());
					Set<String> setOfUsingClients = licencePool2setOfUsingClientsSWInvent.computeIfAbsent(licencePoolId,
							s -> new HashSet<>());

					Logging.debug(this,
							"software " + softwareIdent + " installed on " + swId2clients.get(softwareIdent));

					if (swId2clients.get(softwareIdent) == null) {
						continue;
					}

					for (String client : swId2clients.get(softwareIdent)) {
						listOfUsingClients.add(client);
						setOfUsingClients.add(client);
					}

					licencePoolUsagecountSWInvent.put(licencePoolId, setOfUsingClients.size());

					for (String client : swId2clients.get(softwareIdent)) {
						String pseudokey = Utils.pseudokey(new String[] { client, licencePoolId });

						if (rowsLicencesReconciliation.get(pseudokey) == null) {
							Logging.warning(
									"client " + client + " or license pool ID " + licencePoolId + " do not exist");
						} else {
							rowsLicencesReconciliation.get(pseudokey).put("SWinventory_used", true);
						}
					}
				}
			}
		}

		// ------------------ retrieve data for statistics

		// table LICENSE_POOL
		Map<String, LicencepoolEntry> licencePools = persistentDataRetriever.getLicencepools();

		// table SOFTWARE_LICENSE
		Logging.info(this, " licences ");

		// table SOFTWARE_LICENSE_TO_LICENSE_POOL
		Logging.info(this, " licence usabilities ");
		List<LicenceUsableForEntry> licenceUsabilities = persistentDataRetriever.getLicenceUsabilities();

		// table LICENSE_ON_CLIENT
		Logging.info(this, " licence usages ");
		List<LicenceUsageEntry> licenceUsages = persistentDataRetriever.getLicenceUsages();

		// software usage according to audit
		// tables
		// AUDIT_SOFTWARE_TO_LICENSE_POOL
		// SOFTWARE_CONFIG
		// leads to getSoftwareAuditOnClients()

		// ----------------- set up data structure

		TreeMap<String, ExtendedInteger> pool2allowedUsagesCount = new TreeMap<>();

		for (LicenceUsableForEntry licenceUsability : licenceUsabilities) {
			String pool = licenceUsability.getLicencePoolId();
			String licenceId = licenceUsability.getLicenceId();

			// value up this step
			ExtendedInteger count = pool2allowedUsagesCount.get(pool);

			// not yet initialized
			if (count == null) {
				count = persistentDataRetriever.getLicences().get(licenceId).getMaxInstallations();
				pool2allowedUsagesCount.put(pool, count);
			} else {
				ExtendedInteger result = count
						.add(persistentDataRetriever.getLicences().get(licenceId).getMaxInstallations());
				pool2allowedUsagesCount.put(pool, result);
			}
		}

		Logging.debug(this, " pool2allowedUsagesCount " + pool2allowedUsagesCount);

		TreeMap<String, Integer> pool2opsiUsagesCount = new TreeMap<>();

		Map<String, Set<String>> pool2opsiUsages = new TreeMap<>();

		for (LicenceUsageEntry licenceUsage : licenceUsages) {
			String pool = licenceUsage.getLicencepool();
			Integer usageCount = pool2opsiUsagesCount.computeIfAbsent(pool, s -> Integer.valueOf(0));
			Set<String> usingClients = pool2opsiUsages.computeIfAbsent(pool, s -> new TreeSet<>());

			String clientId = licenceUsage.getClientId();

			if (clientId != null) {
				usageCount = usageCount + 1;
				pool2opsiUsagesCount.put(pool, usageCount);
				usingClients.add(clientId);
			}
		}

		// all used licences for pools

		Logging.info(this, "  retrieveStatistics  collect pool2installationsCount");

		TreeMap<String, TreeSet<String>> pool2clients = new TreeMap<>();
		// we take Set since we count only one usage per client

		TreeMap<String, Integer> pool2installationsCount = new TreeMap<>();

		// require this licencepool
		// add the clients which have this software installed

		for (StringValuedRelationElement swXpool : auditSoftwareXLicencePool) {
			Logging.debug(this, " retrieveStatistics1 relationElement  " + swXpool);
			String pool = swXpool.get(LicencepoolEntry.ID_SERVICE_KEY);

			TreeSet<String> clientsServedByPool = pool2clients.computeIfAbsent(pool, s -> new TreeSet<>());

			String swIdent = swXpool.get(AuditSoftwareXLicencePool.SW_ID);

			Logging.debug(this, " retrieveStatistics1 swIdent " + swIdent);

			if (swId2clients.get(swIdent) != null) {
				Logging.debug(this, "pool " + pool + " serves clients " + swId2clients.get(swIdent));
				clientsServedByPool.addAll(swId2clients.get(swIdent));
			}
		}

		for (Entry<String, TreeSet<String>> poolEntry : pool2clients.entrySet()) {
			pool2installationsCount.put(poolEntry.getKey(), poolEntry.getValue().size());
		}

		Map<String, LicenceStatisticsRow> rowsLicenceStatistics = new TreeMap<>();

		if (withLicenceManagement) {
			for (String licencePoolId : licencePools.keySet()) {
				LicenceStatisticsRow rowMap = new LicenceStatisticsRow(licencePoolId);
				rowsLicenceStatistics.put(licencePoolId, rowMap);

				rowMap.setAllowedUsagesCount(pool2allowedUsagesCount.get(licencePoolId));
				rowMap.setOpsiUsagesCount(pool2opsiUsagesCount.get(licencePoolId));
				rowMap.setSWauditUsagesCount(pool2installationsCount.get(licencePoolId));

				Set<String> listOfUsingClients = pool2opsiUsages.get(licencePoolId);

				Logging.debug(this, "pool  " + licencePoolId + " used_by_opsi on clients : " + listOfUsingClients);

				if (listOfUsingClients != null) {
					for (String client : listOfUsingClients) {
						String pseudokey = Utils.pseudokey(new String[] { client, licencePoolId });

						if (rowsLicencesReconciliation.get(pseudokey) == null) {
							Logging.warning(
									"client " + client + " or license pool ID " + licencePoolId + " do not exist");
						} else {
							rowsLicencesReconciliation.get(pseudokey).put("used_by_opsi", true);
						}
					}
				}
			}
		}

		Logging.debug(this, "rowsLicenceStatistics " + rowsLicenceStatistics);

		return rowsLicenceStatistics;
	}

	public Map<String, LicenceStatisticsRow> getLicenceStatistics() {
		return produceLicenceStatistics();
	}

	public void licencesUsageRequestRefresh() {
		rowsLicencesUsage = null;
		fClient2LicencesUsageList = null;
	}

	// retrieves the used software licence - or tries to reserve one - for the given
	// host and licence pool
	public String getLicenceUsage(String hostId, String licensePoolId) {
		String result = null;
		Map<String, Object> resultMap = null;

		if (withLicenceManagement) {
			OpsiMethodCall omc0 = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_GET_OR_CREATE_OBJECT,
					new String[] { hostId, licensePoolId });

			resultMap = exec.getMapResult(omc0);

			if (!resultMap.isEmpty()) {
				result = Utils.pseudokey(new String[] { "" + resultMap.get(HOST_KEY),
						"" + resultMap.get("softwareLicenseId"), "" + resultMap.get("licensePoolId") });
			}
		}

		return result;
	}

	public String editLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId, String licenseKey,
			String notes) {
		if (!serverFullPermission) {
			return null;
		}

		String result = null;
		Map<String, Object> resultMap = null;

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_CREATE,
					new String[] { softwareLicenseId, licensePoolId, hostId, licenseKey, notes });

			resultMap = exec.getMapResult(omc);

			if (!resultMap.isEmpty()) {
				result = Utils.pseudokey(new String[] { "" + resultMap.get(HOST_KEY),
						"" + resultMap.get("softwareLicenseId"), "" + resultMap.get("licensePoolId") });
			}
		}

		return result;
	}

	public void addDeletionLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (itemsDeletionLicenceUsage == null) {
			itemsDeletionLicenceUsage = new ArrayList<>();
		}

		addDeletionLicenceUsage(hostId, softwareLicenseId, licensePoolId, itemsDeletionLicenceUsage);
	}

	private void addDeletionLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId,
			List<LicenceUsageEntry> deletionItems) {
		if (deletionItems == null) {
			return;
		}

		if (!serverFullPermission) {
			return;
		}

		if (!withLicenceManagement) {
			return;
		}

		LicenceUsageEntry deletionItem = new LicenceUsageEntry(hostId, softwareLicenseId, licensePoolId, "", "");
		deletionItems.add(deletionItem);
	}

	public boolean executeCollectedDeletionsLicenceUsage() {
		Logging.info(this, "executeCollectedDeletionsLicenceUsage itemsDeletionLicenceUsage == null "
				+ (itemsDeletionLicenceUsage == null));
		if (itemsDeletionLicenceUsage == null) {
			return true;
		}

		if (!serverFullPermission) {
			return false;
		}

		if (!withLicenceManagement) {
			return false;
		}

		boolean result = false;

		List<Map<String, Object>> jsonPreparedList = new ArrayList<>();
		for (LicenceUsageEntry item : itemsDeletionLicenceUsage) {
			jsonPreparedList.add(item.getNOMobject());
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_DELETE_OBJECTS,
				new Object[] { jsonPreparedList });

		result = exec.doCall(omc);

		if (result) {
			for (LicenceUsageEntry item : itemsDeletionLicenceUsage) {
				String key = item.getPseudoKey();
				String hostX = item.getClientId();

				LicenceUsageEntry rowmap = rowsLicencesUsage.get(key);
				rowsLicencesUsage.remove(key);
				fClient2LicencesUsageList.get(hostX).remove(rowmap);

				Logging.debug(this,
						"deleteLicenceUsage check fClient2LicencesUsageList " + fClient2LicencesUsageList.get(hostX));
			}
		}

		itemsDeletionLicenceUsage.clear();

		return result;
	}

	public boolean deleteLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_DELETE,
					new String[] { softwareLicenseId, licensePoolId, hostId });

			result = exec.doCall(omc);

			if (result) {
				String key = LicenceUsageEntry.produceKey(hostId, licensePoolId, softwareLicenseId);
				LicenceUsageEntry rowmap = rowsLicencesUsage.get(key);
				rowsLicencesUsage.remove(key);
				fClient2LicencesUsageList.get(hostId).remove(rowmap);
			}

			Logging.info(this,
					"deleteLicenceUsage check fClient2LicencesUsageList " + fClient2LicencesUsageList.get(hostId));
		}

		return result;
	}

	public void reconciliationInfoRequestRefresh() {
		Logging.info(this, "reconciliationInfoRequestRefresh");
		rowsLicencesReconciliation = null;
		Logging.info(this, "reconciliationInfoRequestRefresh installedSoftwareInformationRequestRefresh()");
		// persistentDataRetriever.installedSoftwareInformationRequestRefresh();

		relationsAuditSoftwareToLicencePools = null;

		// persistentDataRetriever.softwareAuditOnClientsRequestRefresh();
		// persistentDataRetriever.licencepoolsRequestRefresh();
		// persistentDataRetriever.licencesRequestRefresh();
		// persistentDataRetriever.licenceUsabilitiesRequestRefresh();
		// persistentDataRetriever.licenceUsagesRequestRefresh();
		hostInfoCollections.opsiHostsRequestRefresh();
	}

	public Map<String, Map<String, Object>> getLicencesReconciliation() {
		getLicenceStatistics();
		return rowsLicencesReconciliation;
	}

	public void addRoleConfig(String name, String rolename) {
		String configkey = UserConfig.KEY_USER_ROLE_ROOT + ".{" + name + "}." + UserConfig.HAS_ROLE_ATTRIBUT;
		addRoleAndUserConfig(configkey, rolename);
	}

	public void addUserConfig(String name, String rolename) {
		String configkey = UserConfig.START_USER_KEY + name + "}." + UserConfig.HAS_ROLE_ATTRIBUT;
		addRoleAndUserConfig(configkey, rolename);
	}

	private void addRoleAndUserConfig(String configkey, String rolename) {
		List<Map<String, Object>> readyObjects = new ArrayList<>();
		String role = rolename;

		if (role == null) {
			role = UserConfig.NONE_PROTOTYPE;
		}

		List<Object> selectedValuesRole = new ArrayList<>();
		selectedValuesRole.add(role);

		Map<String, Object> itemRole = OpsiServiceNOMPersistenceController.createJSONConfig(
				ConfigOption.TYPE.UNICODE_CONFIG, configkey, "which role should determine this configuration", false,
				false, selectedValuesRole, selectedValuesRole);

		readyObjects.add(itemRole);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

		exec.doCall(omc);

		configDefaultValues.put(configkey, selectedValuesRole);
	}

	public void userConfigurationRequestReload() {
		Logging.info(this, "userConfigurationRequestReload");
		keyUserRegisterValue = null;
	}

	public void deleteSavedSearch(String name) {
		Logging.debug(this, "deleteSavedSearch " + name);

		List<Map<String, Object>> readyObjects = new ArrayList<>();
		Map<String, Object> item;

		item = createNOMitem("UnicodeConfig");
		item.put("id", SavedSearch.CONFIG_KEY + "." + name);
		readyObjects.add(item);

		item = createNOMitem("UnicodeConfig");
		item.put("id", SavedSearch.CONFIG_KEY + "." + name + "." + SavedSearch.DESCRIPTION_KEY);
		readyObjects.add(item);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS, new Object[] { readyObjects });

		exec.doCall(omc);
		savedSearches.remove(name);
	}

	public void saveSearch(SavedSearch ob) {
		Logging.debug(this, "saveSearch " + ob);

		List<Object> readyObjects = new ArrayList<>();
		// entry of serialization string
		readyObjects.add(produceConfigEntry("UnicodeConfig", SavedSearch.CONFIG_KEY + "." + ob.getName(),
				ob.getSerialization(), ob.getDescription(), false));
		// description entry
		readyObjects.add(produceConfigEntry("UnicodeConfig",
				SavedSearch.CONFIG_KEY + "." + ob.getName() + "." + SavedSearch.DESCRIPTION_KEY, ob.getDescription(),
				"", true));

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { readyObjects });

		exec.doCall(omc);
	}

	public Map<String, Object> produceConfigEntry(String nomType, String key, Object value, String description) {
		return produceConfigEntry(nomType, key, value, description, true);
	}

	public Map<String, Object> produceConfigEntry(String nomType, String key, Object value, String description,
			boolean editable) {
		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(value);

		// defaultValues
		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(value);

		// create config for service
		Map<String, Object> item;

		item = createNOMitem(nomType);
		item.put("ident", key);
		item.put("description", description);
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		item.put("editable", editable);
		item.put("multiValue", false);

		return item;
	}

	public List<String> getDisabledClientMenuEntries() {
		persistentDataRetriever.getConfigOptions();
		return takeAsStringList(configDefaultValues.get(KEY_DISABLED_CLIENT_ACTIONS));
	}

	public List<String> getOpsiclientdExtraEvents() {
		Logging.debug(this, "getOpsiclientdExtraEvents");
		persistentDataRetriever.getConfigOptions();
		if (configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS) == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
		}

		List<String> result = takeAsStringList(configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS));
		Logging.debug(this, "getOpsiclientdExtraEvents() " + result);
		return result;
	}

	public static List<String> takeAsStringList(List<Object> list) {
		List<String> result = new ArrayList<>();

		if (list == null) {
			return result;
		}

		for (Object val : list) {
			result.add((String) val);
		}

		return result;
	}

	// opsi module information
	public void opsiInformationRequestRefresh() {
		opsiInformation = new HashMap<>();
	}

	public final void opsiLicencingInfoRequestRefresh() {
		licencingInfoOpsiAdmin = null;
		licencingInfoNoOpsiAdmin = null;
		licInfoMap = null;
		LicensingInfoMap.requestRefresh();
		Logging.info(this, "request worked");
	}

	/**
	 * Test if sshcommand methods exists
	 *
	 * @param method name
	 * @return True if exists
	 */
	public boolean checkSSHCommandMethod(RPCMethodName method) {
		// method does not exist before opsi 3.4
		if (persistentDataRetriever.getMethodSignature(method) != NONE_LIST) {
			Logging.info(this, "checkSSHCommandMethod " + method + " exists");
			return true;
		}
		Logging.info(this, "checkSSHCommandMethod " + method + " does not exists");
		return false;
	}

	public List<Map<String, Object>> retrieveHealthDetails(String checkId) {
		List<Map<String, Object>> result = new ArrayList<>();

		for (Map<String, Object> data : checkHealth()) {
			if (((String) data.get("check_id")).equals(checkId)) {
				result = POJOReMapper.remap(data.get("partial_results"),
						new TypeReference<List<Map<String, Object>>>() {
						});
				break;
			}
		}

		return result;
	}

	public List<Map<String, Object>> checkHealth() {
		if (!isHealthDataAlreadyLoaded()) {
			healthData = persistentDataRetriever.checkHealth();
		}

		return healthData;
	}

	public boolean isHealthDataAlreadyLoaded() {
		return healthData != null;
	}

	public Map<String, Object> getDiagnosticData() {
		if (diagnosticData == null) {
			diagnosticData = persistentDataRetriever.getDiagnosticData();
		}

		return diagnosticData;
	}

	/**
	 * Exec a python-opsi command
	 *
	 * @param method      name
	 * @param jsonObjects to do sth
	 * @return result true if everything is ok
	 */
	private boolean doActionSSHCommand(RPCMethodName method, List<Object> jsonObjects) {
		Logging.info(this, "doActionSSHCommand method " + method);
		if (globalReadOnly) {
			return false;
		}
		OpsiMethodCall omc = new OpsiMethodCall(method, new Object[] { jsonObjects });
		boolean result = exec.doCall(omc);
		Logging.info(this, "doActionSSHCommand method " + method + " result " + result);
		return result;
	}

	/**
	 * Exec the python-opsi command "SSHCommand_deleteObjects"
	 *
	 * @param jsonObjects to remove
	 * @return result true if successfull
	 */
	public boolean deleteSSHCommand(List<String> jsonObjects) {
		// Strings not object!
		Logging.info(this, "deleteSSHCommand ");
		if (globalReadOnly) {
			return false;
		}
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SSH_COMMAND_DELETE_OBJECTS, new Object[] { jsonObjects });
		boolean result = exec.doCall(omc);
		Logging.info(this, "deleteSSHCommand result " + result);
		return result;
	}

	/**
	 * Exec the python-opsi command "SSHCommand_createObjects"
	 *
	 * @param jsonObjects to create
	 * @return result true if successfull
	 */
	public boolean createSSHCommand(List<Object> jsonObjects) {
		return doActionSSHCommand(RPCMethodName.SSH_COMMAND_CREATE_OBJECTS, jsonObjects);
	}

	/**
	 * Exec the python-opsi command "SSHCommand_updateObjects"
	 *
	 * @param jsonObjects to update
	 * @return result true if successfull
	 */
	public boolean updateSSHCommand(List<Object> jsonObjects) {
		return doActionSSHCommand(RPCMethodName.SSH_COMMAND_UPDATE_OBJECTS, jsonObjects);
	}

	public static String getConfigedWorkbenchDefaultValue() {
		return configedWorkbenchDefaultValue;
	}

	public static void setConfigedWorkbenchDefaultValue(String defaultWorkbenchValue) {
		OpsiServiceNOMPersistenceController.configedWorkbenchDefaultValue = defaultWorkbenchValue;
	}

	public static String getPackageServerDirectoryS() {
		return packageServerDirectoryS;
	}

	public static void setPackageServerDirectoryS(String packageServerDirectoryS) {
		OpsiServiceNOMPersistenceController.packageServerDirectoryS = packageServerDirectoryS;
	}

	public AbstractExecutioner getExecutioner() {
		return exec;
	}
}
