/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiHwAuditDevicePropertyType;
import de.uib.configed.type.OpsiHwAuditDevicePropertyTypes;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.RetrievedMap;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.SavedSearch;
import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.configed.type.licences.LicenceContractEntry;
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
import de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.ModulePermissionValue;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.opsidatamodel.permission.UserOpsipermission;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.ExtendedDate;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.datapanel.MapTableModel;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.observer.DataRefreshedObserver;
import de.uib.utilities.table.ListCellOptions;

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
public class OpsiserviceNOMPersistenceController {
	private static final String EMPTYFIELD = "-";
	private static final List<String> NONE_LIST = new ArrayList<>() {
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
	public static String configedWorkbenchDefaultValue = "/var/lib/opsi/workbench/";
	public static String packageServerDirectoryS = configedWorkbenchDefaultValue;

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

	private static final String BACKEND_LICENSING_INFO_METHOD_NAME = "backend_getLicensingInfo";

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
	private static final int CLIENT_COUNT_WARNING_LIMIT = 10;
	private static final int CLIENT_COUNT_TOLERANCE_LIMIT = 50;

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

	private List<DataRefreshedObserver> dataRefreshedObservers;

	public AbstractExecutioner exec;

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

	private String theDepot = "";

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

	private DataStubNOM dataStub;

	private Boolean acceptMySQL;

	// package visibility, the constructor is called by PersistenceControllerFactory
	OpsiserviceNOMPersistenceController(String server, String user, String password) {
		Logging.info(this.getClass(), "start construction, \nconnect to " + server + " as " + user);
		this.connectionServer = server;
		this.user = user;

		Logging.debug(this.getClass(), "create");

		hostInfoCollections = new HostInfoCollections(this);

		exec = new ServerFacade(server, user, password);

		hwAuditConf = new HashMap<>();

		dataStub = new DataStubNOM(this);
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
	// implementation of observer patterns
	// offer observing of data refreshed announcements

	public void registerDataRefreshedObserver(DataRefreshedObserver ob) {
		if (dataRefreshedObservers == null) {
			dataRefreshedObservers = new ArrayList<>();
		}
		dataRefreshedObservers.add(ob);
	}

	public void notifyDataRefreshedObservers(Object mesg) {
		if (dataRefreshedObservers == null) {
			return;
		}

		for (DataRefreshedObserver ob : dataRefreshedObservers) {
			ob.gotNotification(mesg);
		}
	}

	public Map<String, List<Object>> getWanConfiguration() {
		return wanConfiguration;
	}

	public Map<String, List<Object>> getNotWanConfiguration() {
		return notWanConfiguration;
	}

	// ---------------------------------------------------------------

	public boolean canCallMySQL() {
		if (acceptMySQL == null) {
			acceptMySQL = dataStub.canCallMySQL();
		}

		return acceptMySQL;
	}

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

	// final in order to avoid deactiviating by override
	private final boolean setAgainUserRegistration(final boolean userRegisterValueFromConfigs) {
		Logging.info(this, "setAgainUserRegistration, userRoles can be used " + withUserRoles);

		boolean resultVal = userRegisterValueFromConfigs;

		if (!withUserRoles) {
			return resultVal;
		}

		Boolean locallySavedValueUserRegister = null;
		if (Configed.savedStates == null) {
			Logging.trace(this, "savedStates.saveRegisterUser not initialized");
		} else {
			locallySavedValueUserRegister = Boolean.parseBoolean(Configed.savedStates.getProperty(KEY_USER_REGISTER));
			Logging.info(this, "setAgainUserRegistration, userRegister was activated " + locallySavedValueUserRegister);

			if (userRegisterValueFromConfigs) {
				if (locallySavedValueUserRegister == null || !locallySavedValueUserRegister) {
					// we save true
					Configed.savedStates.setProperty(KEY_USER_REGISTER, "true");
				}
			} else {
				if (locallySavedValueUserRegister != null && locallySavedValueUserRegister) {
					// if true was locally saved but is not the value from service then we ask
					Logging.warning(this, "setAgainUserRegistration, it seems that user check has been deactivated");

					FTextArea dialog = new FTextArea(ConfigedMain.getMainFrame(),
							Configed.getResourceValue("RegisterUserWarning.dialog.title"),

							true,
							new String[] { Configed.getResourceValue("RegisterUserWarning.dialog.button1"),
									Configed.getResourceValue("RegisterUserWarning.dialog.button2"),
									Configed.getResourceValue("RegisterUserWarning.dialog.button3") },
							new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
									Globals.createImageIcon("images/edit-delete.png", ""),
									Globals.createImageIcon("images/executing_command_red_16.png", "") },
							500, 200);
					StringBuilder msg = new StringBuilder(

							Configed.getResourceValue("RegisterUserWarning.dialog.info1"));
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.info2"));
					msg.append("\n");
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option1"));
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option2"));
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option3"));

					dialog.setMessage("" + msg);
					dialog.setVisible(true);
					int result = dialog.getResult();
					Logging.info(this, "setAgainUserRegistration, reaction via option " + dialog.getResult());

					switch (result) {
					case 1:
						Logging.info(this, "setAgainUserRegistration ignore ");
						break;

					case 2:
						Logging.info(this, "setAgainUserRegistration remove warning locally ");
						// remove from store
						Configed.savedStates.remove(KEY_USER_REGISTER);
						break;

					case 3:
						Logging.info(this, "setAgainUserRegistration reactivate user check ");
						resultVal = true;
						break;

					default:
						Logging.warning(this, "no case found for result in setAgainUserRegistration");
						break;
					}
				}
			}
		}

		return resultVal;
	}

	private String userPart() {
		if (userConfigPart != null) {
			return userConfigPart;
		}

		if (applyUserSpecializedConfig()) {
			userConfigPart = KEY_USER_ROOT + ".{" + user + "}.";
		} else {
			userConfigPart = UserConfig.KEY_USER_ROLE_ROOT + ".{" + UserConfig.DEFAULT_ROLE_NAME + "}.";
		}

		Logging.info(this, "userConfigPart initialized, " + userConfigPart);

		return userConfigPart;
	}

	public final void checkConfiguration() {
		retrieveOpsiModules();
		Logging.info(this, "checkConfiguration, modules " + opsiModules);

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

		globalReadOnly = checkReadOnlyBySystemuser();

		serverFullPermission = !globalReadOnly;
		depotsFullPermission = true;
		hostgroupsOnlyIfExplicitlyStated = false;
		productgroupsFullPermission = true;
		createClientPermission = true;

		keyUserRegisterValue = isUserRegisterActivated();
		boolean correctedUserRegisterVal = setAgainUserRegistration(keyUserRegisterValue);

		boolean setUserRegisterVal = !keyUserRegisterValue && correctedUserRegisterVal;

		if (setUserRegisterVal) {
			keyUserRegisterValue = true;
		}

		if (Boolean.TRUE.equals(keyUserRegisterValue)) {
			keyUserRegisterValue = checkUserRolesModule();
		}

		if (serverPropertyMap.get(KEY_USER_REGISTER) == null || setUserRegisterVal) {
			List<Object> readyObjects = new ArrayList<>();
			Map<String, Object> item = createJSONBoolConfig(KEY_USER_REGISTER, keyUserRegisterValue,
					"without given values the primary value setting is false");
			readyObjects.add(item);

			OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { readyObjects });

			exec.doCall(omc);
		}

		applyUserSpecializedConfig();

		List<Object> readyConfigObjects = new UserConfigProducing(applyUserSpecializedConfig(),
				getHostInfoCollections().getConfigServer(), getHostInfoCollections().getDepotNamesList(),
				getHostGroupIds(), getProductGroups().keySet(), getConfigDefaultValues(), getConfigOptions()).produce();

		if (readyConfigObjects == null) {
			Logging.warning(this, "readyObjects for userparts " + null);
		} else {

			if (!readyConfigObjects.isEmpty()) {

				OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { readyConfigObjects });

				exec.doCall(omc);
			}

			Logging.info(this, "readyObjects for userparts " + readyConfigObjects.size());
		}

		checkPermissions();

		if (serverFullPermission) {
			checkStandardConfigs();
		}
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
		// set by executioner

		Logging.info(this, "trying to make connection");
		boolean result = false;
		try {
			result = exec1.doCall(new OpsiMethodCall("accessControl_authenticated", new String[] {}));

			if (!result) {
				Logging.info(this, "connection does not work");
			}

		} catch (ClassCastException ex) {
			Logging.info(this, "JSONthroughHTTPS failed to make connection: " + ex);
		}

		result = result && getConnectionState().getState() == ConnectionState.CONNECTED;
		Logging.info(this, "tried to make connection result " + result);
		return result;
	}

	public String getOpsiCACert() {
		OpsiMethodCall omc = new OpsiMethodCall("getOpsiCACert", new Object[0]);
		return exec.getStringResult(omc);
	}

	public boolean usesMultiFactorAuthentication() {
		return isMultiFactorAuthenticationEnabled;
	}

	public void checkMultiFactorAuthentication() {
		isMultiFactorAuthenticationEnabled = ServerFacade.isOpsi43() && getOTPSecret(ConfigedMain.user) != null;
	}

	private String getOTPSecret(String userId) {
		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("id", userId);
		OpsiMethodCall omc = new OpsiMethodCall("user_getObjects", new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> result = exec.getListOfMaps(omc);

		if (result.isEmpty()) {
			return null;
		}

		Map<String, Object> userDetails = result.get(0);
		String otpSecret = null;

		if (userDetails.containsKey("otpSecret")) {
			otpSecret = (String) userDetails.get("otpSecret");
		}

		return otpSecret;
	}

	// we delegate method calls to the executioner
	public ConnectionState getConnectionState() {
		return exec.getConnectionState();
	}

	public boolean isGlobalReadOnly() {
		return globalReadOnly;
	}

	private boolean checkReadOnlyBySystemuser() {
		boolean result = false;

		Logging.info(this, "checkReadOnly");
		if (exec.getBooleanResult(new OpsiMethodCall("accessControl_userIsReadOnlyUser", new String[] {}))) {
			result = true;
			Logging.info(this, "checkReadOnly " + globalReadOnly);
		}

		return result;
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

	private boolean checkFullPermission(Set<String> permittedEntities, final String keyUseList, final String keyList,
			final Map<String, List<Object>> serverPropertyMap) {
		Logging.info(this, "checkFullPermission  key name,  defaultResult true " + keyUseList);

		boolean fullPermission = true;

		if (serverPropertyMap.get(keyUseList) != null) {
			fullPermission = !(Boolean) (serverPropertyMap.get(keyUseList).get(0));
			// we don't give full permission if the config doesn't exist

			// we didn't configure anything, therefore we revoke the setting
			if (serverPropertyMap.get(keyList) == null) {
				fullPermission = true;
				Logging.info(this, "checkFullPermission not configured keyList " + keyList);
			}
		}

		Logging.info(this, "checkFullPermission  key for list,  fullPermission " + keyList + ", " + fullPermission);

		// we didn't configure anything, therefore we revoke the setting
		if (!fullPermission && serverPropertyMap.get(keyList) != null) {
			for (Object val : serverPropertyMap.get(keyList)) {
				permittedEntities.add((String) val);
			}
		}

		Logging.info(this, "checkFullPermission   result " + fullPermission);
		Logging.info(this, "checkFullPermission   produced list " + permittedEntities);

		return fullPermission;
	}

	private void checkPermissions() {
		UserOpsipermission.ActionPrivilege serverActionPermission;

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

		// variable for simplifying the use of the map
		String configKey = null;

		// already specified via systemuser group
		if (!globalReadOnly) {
			// lookup if we have a config for it and set it though not set by group
			configKey = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY;
			Logging.info(this, "checkPermissions  configKey " + configKey);
			globalReadOnly = serverPropertyMap.get(configKey) != null
					&& (Boolean) serverPropertyMap.get(configKey).get(0);
		}

		Logging.info(this, " checkPermissions globalReadOnly " + globalReadOnly);

		if (globalReadOnly) {
			serverActionPermission = UserOpsipermission.ActionPrivilege.READ_ONLY;
		} else {
			// is default!!
			boolean mayWriteOnOpsiserver = true;

			configKey = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_SERVER_READWRITE;
			Logging.info(this, "checkPermissions  configKey " + configKey);

			if (serverPropertyMap.get(configKey) != null) {
				Logging.info(this, " checkPermissions  value  " + (serverPropertyMap.get(configKey).get(0)));
				mayWriteOnOpsiserver = (Boolean) ((serverPropertyMap.get(configKey)).get(0));
			}

			Logging.info(this, " checkPermissions mayWriteOnOpsiserver " + mayWriteOnOpsiserver);
			if (mayWriteOnOpsiserver) {
				serverActionPermission = UserOpsipermission.ActionPrivilege.READ_WRITE;
			} else {
				serverActionPermission = UserOpsipermission.ActionPrivilege.READ_ONLY;
			}
		}

		serverFullPermission = serverActionPermission == UserOpsipermission.ActionPrivilege.READ_WRITE;

		configKey = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_CREATECLIENT;
		Logging.info(this, " checkPermissions key " + configKey);
		if (serverPropertyMap.get(configKey) != null && withUserRoles) {
			Logging.info(this, " checkPermissions  value  " + (serverPropertyMap.get(configKey).get(0)));
			createClientPermission = (Boolean) ((serverPropertyMap.get(configKey)).get(0));
		}

		String configKeyUseList = null;
		String configKeyList = null;

		configKeyUseList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;
		depotsPermitted = new HashSet<>();

		depotsFullPermission = checkFullPermission(depotsPermitted,
				// true,
				configKeyUseList, configKeyList, serverPropertyMap);
		Logging.info(this,
				"checkPermissions depotsFullPermission (false means, depots must be specified " + depotsFullPermission);
		Logging.info(this, "checkPermissions depotsPermitted " + depotsPermitted);

		configKeyUseList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE;
		hostgroupsPermitted = new HashSet<>();

		// false, //not only as specified but always
		hostgroupsOnlyIfExplicitlyStated = checkFullPermission(hostgroupsPermitted, configKeyUseList, configKeyList,
				serverPropertyMap);

		if (hostgroupsOnlyIfExplicitlyStated) {
			hostgroupsPermitted = null;
		}

		Logging.info(this, "checkPermissions hostgroupsPermitted " + hostgroupsPermitted);

		configKeyUseList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE;
		Set<String> productgroupsPermitted = new HashSet<>();

		productgroupsFullPermission = checkFullPermission(productgroupsPermitted, configKeyUseList, configKeyList,
				serverPropertyMap);

		permittedProducts = null;

		if (!productgroupsFullPermission) {
			permittedProducts = new TreeSet<>();

			for (String group : productgroupsPermitted) {
				Set<String> products = getFProductGroup2Members().get(group);
				if (products != null) {
					permittedProducts.addAll(products);
				}
			}
		}

		Logging.info(this, "checkPermissions permittedProducts " + permittedProducts);
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
		String method = "depot_installPackage";

		Logging.notice(this, method);
		// TODO is "true" necessary?
		boolean result = exec.doCall(new OpsiMethodCall(method, new Object[] { filename, true }));
		Logging.info(this, "installPackage result " + result);

		return result;
	}

	public boolean setRights(String path) {
		String method = "setRights";
		Logging.info(this, "setRights for path " + path);

		String[] args = new String[] { path };

		if (path == null) {
			args = new String[] {};
		}

		return exec.doCall(new OpsiMethodCall(method, args));
	}

	public List<Map<String, Object>> hostRead() {
		String[] callAttributes = new String[] {};
		Map<?, ?> callFilter = new HashMap<>();

		TimeCheck timer = new TimeCheck(this, "HOST_read").start();
		Logging.notice(this, "host_getObjects");
		List<Map<String, Object>> opsiHosts = exec
				.getListOfMaps(new OpsiMethodCall("host_getObjects", new Object[] { callAttributes, callFilter }));
		timer.stop();

		return opsiHosts;
	}

	public List<String> getClientsWithOtherProductVersion(String productId, String productVersion,
			String packageVersion, boolean includeFailedInstallations) {
		String[] callAttributes = new String[] {};

		HashMap<String, String> callFilter = new HashMap<>();
		callFilter.put(OpsiPackage.DB_KEY_PRODUCT_ID, productId);
		callFilter.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);

		List<Map<String, Object>> retrievedList = retrieveListOfMapsNOM(callAttributes, callFilter,
				"productOnClient_getObjects");

		List<String> result = new ArrayList<>();

		for (Map<String, Object> m : retrievedList) {
			String client = (String) m.get("clientId");

			String clientProductVersion = (String) m.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
			String clientPackageVersion = (String) m.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);

			Object clientProductState = m.get(ProductState.KEY_INSTALLATION_STATUS);
			if (
			// has state unknown, probably because of a failed installation)
			(includeFailedInstallations
					&& InstallationStatus.getLabel(InstallationStatus.UNKNOWN).equals(clientProductState)) ||
			// has wrong product version
					(InstallationStatus.getLabel(InstallationStatus.INSTALLED).equals(clientProductState)
							&& ((!POJOReMapper.equalsNull(clientProductVersion)
									&& !productVersion.equals(clientProductVersion))
									|| (!POJOReMapper.equalsNull(clientPackageVersion)
											&& !packageVersion.equals(clientPackageVersion))))) {
				Logging.debug("getClientsWithOtherProductVersion hit " + m);

				result.add(client);
			}
		}

		Logging.info(this, "getClientsWithOtherProductVersion globally " + result.size());

		// should be done otherwere by preselection of depots

		return result;
	}

	public boolean areDepotsSynchronous(Iterable<String> depots) {
		String lastIdent = null;

		for (String depot : depots) {
			String callReturnType = "dict";
			Map<String, String> callFilter = new HashMap<>();
			callFilter.put("depotId", depot);

			List<Map<String, Object>> products = exec.getListOfMaps(
					new OpsiMethodCall("productOnDepot_getIdents", new Object[] { callReturnType, callFilter }));
			List<String> productIdents = new ArrayList<>();

			for (Map<String, Object> product : products) {
				productIdents.add(product.get("productId") + ";" + product.get("productVersion") + ";"
						+ product.get("packageVersion"));
			}

			Collections.sort(productIdents);
			String ident = String.join("|", productIdents);

			if (lastIdent != null && !ident.equals(lastIdent)) {
				return false;
			}

			lastIdent = ident;
		}

		return true;
	}

	private Map<String, ConfigOption> extractSubConfigOptionsByInitial(final String s) {
		HashMap<String, ConfigOption> result = new HashMap<>();
		getConfigOptions();
		for (Entry<String, ConfigOption> configOption : configOptions.entrySet()) {
			if (configOption.getKey().startsWith(s) && configOption.getKey().length() > s.length()) {
				String xKey = configOption.getKey().substring(s.length());
				result.put(xKey, configOption.getValue());
			}
		}

		return result;
	}

	private static List<Map<String, Object>> buildWANConfigOptions(List<Map<String, Object>> readyObjects) {
		// NOT_WAN meta configs
		Map<String, Object> item = createJSONBoolConfig(
				CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + "." + CONFIG_CLIENTD_EVENT_GUISTARTUP, true,
				"meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = createJSONBoolConfig(
				CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + "." + CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN,
				true, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = createJSONBoolConfig(
				CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + "." + CONFIG_CLIENTD_EVENT_NET_CONNECTION, false,
				"meta configuration for default not wan behaviour");

		readyObjects.add(item);

		item = createJSONBoolConfig(CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + "." + CONFIG_CLIENTD_EVENT_TIMER,
				false, "meta configuration for default not wan behaviour");

		readyObjects.add(item);

		return readyObjects;
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
		} else if (getConfigDefaultValues().get(CONFIG_DHCPD_FILENAME) != null
				&& !((List<?>) getConfigDefaultValues().get(CONFIG_DHCPD_FILENAME)).isEmpty()) {
			String configValue = (String) ((List<?>) getConfigDefaultValues().get(CONFIG_DHCPD_FILENAME)).get(0);

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

	private Map<String, ConfigOption> getWANConfigOptions() {
		Map<String, ConfigOption> allWanConfigOptions = extractSubConfigOptionsByInitial(
				CONFIG_KEY + "." + WAN_PARTKEY);

		Logging.info(this, " getWANConfigOptions   " + allWanConfigOptions);

		Map<String, ConfigOption> notWanConfigOptions = extractSubConfigOptionsByInitial(
				CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + ".");

		notWanConfiguration = new HashMap<>();
		wanConfiguration = new HashMap<>();

		List<Object> values = null;

		for (Entry<String, ConfigOption> notWanConfigOption : notWanConfigOptions.entrySet()) {
			if (notWanConfigOption.getValue().getType() != ConfigOption.TYPE.BOOL_CONFIG) {
				notWanConfiguration.put(notWanConfigOption.getKey(), null);
				wanConfiguration.put(notWanConfigOption.getKey(), null);
			} else {
				Boolean b = (Boolean) notWanConfigOption.getValue().getDefaultValues().get(0);

				values = new ArrayList<>();
				values.add(b);
				notWanConfiguration.put(notWanConfigOption.getKey(), values);

				values = new ArrayList<>();
				values.add(!b);
				wanConfiguration.put(notWanConfigOption.getKey(), values);
			}
		}

		Logging.info(this, "getWANConfigOptions wanConfiguration " + wanConfiguration);
		Logging.info(this, "getWANConfigOptions notWanConfiguration  " + notWanConfiguration);

		return allWanConfigOptions;
	}

	private List<Map<String, Object>> addWANConfigStates(String clientId, boolean wan,
			List<Map<String, Object>> jsonObjects) {
		getWANConfigOptions();

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

		OpsiMethodCall omc = new OpsiMethodCall("configState_updateObjects", new Object[] { jsonObjects });
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

			OpsiMethodCall omc = new OpsiMethodCall("configState_updateObjects", new Object[] { jsonObjects });
			result = exec.doCall(omc);
		} else {
			values.add(EFI_DHCPD_NOT);

			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			jsonObjects.add(createUefiJSONEntry(clientId, EFI_DHCPD_NOT));

			OpsiMethodCall omc = new OpsiMethodCall("configState_updateObjects", new Object[] { jsonObjects });
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
			boolean wanConfig = Boolean.parseBoolean((String) client.get(12));
			boolean uefiBoot = Boolean.parseBoolean((String) client.get(13));
			boolean shutdownInstall = Boolean.parseBoolean((String) client.get(14));

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

		OpsiMethodCall omc = new OpsiMethodCall("host_createObjects", new Object[] { clientsJsonObject });
		boolean result = exec.doCall(omc);

		if (result) {
			if (!configStatesJsonObject.isEmpty()) {
				omc = new OpsiMethodCall("configState_updateObjects", new Object[] { configStatesJsonObject });
				result = exec.doCall(omc);
			}

			if (!groupsJsonObject.isEmpty()) {
				omc = new OpsiMethodCall("objectToGroup_createObjects", new Object[] { groupsJsonObject });
				result = exec.doCall(omc);
			}

			if (!productsNetbootJsonObject.isEmpty()) {
				omc = new OpsiMethodCall("productOnClient_createObjects", new Object[] { productsNetbootJsonObject });
				result = exec.doCall(omc);
			}
		}

		return result;
	}

	public boolean createClient(String hostname, String domainname, String depotId, String description,
			String inventorynumber, String notes, String ipaddress, String systemUUID, String macaddress,
			boolean shutdownInstall, boolean uefiBoot, boolean wanConfig, String group, String productNetboot,
			String productLocalboot) {
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

		OpsiMethodCall omc = new OpsiMethodCall("host_createObjects", new Object[] { hostItem });
		result = exec.doCall(omc);

		HostInfo hostInfo = new HostInfo(hostItem);

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

			omc = new OpsiMethodCall("configState_updateObjects", new Object[] { jsonObjects });

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
			omc = new OpsiMethodCall("objectToGroup_createObjects", new Object[] { jsonObjects });
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
			omc = new OpsiMethodCall("productOnClient_createObjects", new Object[] { jsonObjects });
			result = exec.doCall(omc);
		}

		if (result && productLocalboot != null && !productLocalboot.isEmpty()) {
			Logging.info(this, "createClient" + " productLocalboot " + productLocalboot);
			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			Map<String, Object> itemProducts = createNOMitem("ProductOnClient");
			itemProducts.put(OpsiPackage.DB_KEY_PRODUCT_ID, productLocalboot);
			itemProducts.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
			itemProducts.put("clientId", newClientId);
			itemProducts.put(ProductState.key2servicekey.get(ProductState.KEY_ACTION_REQUEST), "setup");
			jsonObjects.add(itemProducts);
			omc = new OpsiMethodCall("productOnClient_createObjects", new Object[] { jsonObjects });
			result = exec.doCall(omc);
		}

		if (result) {
			if (depotId == null || depotId.isEmpty()) {
				depotId = getHostInfoCollections().getConfigServer();
			}
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

		OpsiMethodCall omc = new OpsiMethodCall("host_renameOpsiClient", new String[] { hostname, newHostname });
		hostInfoCollections.opsiHostsRequestRefresh();
		return exec.doCall(omc);
	}

	public void deleteClients(String[] hostIds) {
		if (globalReadOnly) {
			return;
		}

		for (String hostId : hostIds) {
			OpsiMethodCall omc = new OpsiMethodCall("host_delete", new String[] { hostId });
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
		OpsiMethodCall omc = new OpsiMethodCall("hostControlSafe_opsiclientdRpc",
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
				OpsiMethodCall omc = new OpsiMethodCall("hostControl_start",
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
					OpsiMethodCall omc = new OpsiMethodCall("hostControl_start",
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
			OpsiMethodCall omc = new OpsiMethodCall("hostControl_start", new Object[] { hostIds });

			response = exec1.getMapResult(omc);
		}

		return collectErrorsFromResponsesByHost(response, "wakeOnLan");
	}

	public List<String> fireOpsiclientdEventOnClients(String event, String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall("hostControl_fireEvent", new Object[] { event, clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "fireOpsiclientdEventOnClients");
	}

	public List<String> showPopupOnClients(String message, String[] clientIds, Float seconds) {
		OpsiMethodCall omc;

		if (seconds == 0) {
			omc = new OpsiMethodCall("hostControl_showPopup", new Object[] { message, clientIds });
		} else {
			omc = new OpsiMethodCall("hostControl_showPopup",
					new Object[] { message, clientIds, "True", "True", seconds });
		}

		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "showPopupOnClients");

	}

	public List<String> shutdownClients(String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall("hostControl_shutdown", new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "shutdownClients");
	}

	public List<String> rebootClients(String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall("hostControl_reboot", new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "rebootClients");
	}

	public Map<String, Object> reachableInfo(String[] clientIds) {
		Logging.info(this, "reachableInfo ");
		Object[] callParameters = new Object[] {};

		String methodName = "hostControl_reachable";
		if (clientIds != null) {
			Logging.info(this, "reachableInfo for clientIds " + clientIds.length);
			callParameters = new Object[] { clientIds };
			methodName = "hostControlSafe_reachable";
		}

		// background call, do not show waiting info
		return exec.getMapResult(new OpsiMethodCall(methodName, callParameters, OpsiMethodCall.BACKGROUND_DEFAULT));
	}

	public Map<String, Integer> getInstalledOsOverview() {
		Logging.info(this, "getInstalledOsOverview");

		Map<String, Object> producedLicencingInfo;

		if (isOpsiUserAdmin() && licencingInfoOpsiAdmin != null) {
			producedLicencingInfo = POJOReMapper.remap(getOpsiLicencingInfoOpsiAdmin().get("result"),
					new TypeReference<Map<String, Object>>() {
					});
		} else {
			producedLicencingInfo = getOpsiLicencingInfoNoOpsiAdmin();
		}

		return POJOReMapper.remap(producedLicencingInfo.get("client_numbers"),
				new TypeReference<Map<String, Integer>>() {
				});
	}

	private Map<String, Object> getOpsiLicencingInfoNoOpsiAdmin() {
		Logging.info(this, "getLicensingInfoNoOpsiAdmin");

		if (licencingInfoOpsiAdmin == null && isOpsiLicencingAvailable()) {
			Object[] callParameters = {};
			OpsiMethodCall omc = new OpsiMethodCall(BACKEND_LICENSING_INFO_METHOD_NAME, callParameters,
					OpsiMethodCall.BACKGROUND_DEFAULT);

			licencingInfoNoOpsiAdmin = exec.getMapResult(omc);
		}

		return licencingInfoNoOpsiAdmin;
	}

	public List<Map<String, Object>> getModules() {
		Logging.info(this, "getModules");

		Map<String, Object> producedLicencingInfo;

		if (isOpsiUserAdmin() && licencingInfoOpsiAdmin != null) {
			producedLicencingInfo = POJOReMapper.remap(getOpsiLicencingInfoOpsiAdmin().get("result"),
					new TypeReference<Map<String, Object>>() {
					});
		} else {
			producedLicencingInfo = getOpsiLicencingInfoNoOpsiAdmin();
		}

		return POJOReMapper.remap(producedLicencingInfo.get("licenses"),
				new TypeReference<List<Map<String, Object>>>() {
				});
	}

	public Map<String, String> sessionInfo(String[] clientIds) {
		Map<String, String> result = new HashMap<>();

		Object[] callParameters = new Object[] {};
		if (clientIds != null && clientIds.length > 0) {
			callParameters = new Object[] { clientIds };
		}
		String methodname = "hostControl_getActiveSessions";

		Map<String, Object> result0 = exec.getResponses(exec
				.retrieveResponse(new OpsiMethodCall(methodname, callParameters, OpsiMethodCall.BACKGROUND_DEFAULT)));

		for (Entry<String, Object> resultEntry : result0.entrySet()) {
			StringBuilder value = new StringBuilder();

			if (resultEntry.getValue() instanceof String) {
				String errorStr = (String) resultEntry.getValue();
				value = new StringBuilder("no response");
				if (errorStr.indexOf("Opsi timeout") > -1) {
					int i = errorStr.indexOf("(");
					if (i > -1) {
						value.append("   " + errorStr.substring(i + 1, errorStr.length() - 1));
					} else {
						value.append(" (opsi timeout)");
					}
				} else if (errorStr.indexOf(methodname) > -1) {
					value.append("  (" + methodname + " not valid)");
				} else if (errorStr.indexOf("Name or service not known") > -1) {
					value.append(" (name or service not known)");
				} else {
					Logging.notice(this, "unexpected output occured in session Info");
				}
			} else if (resultEntry.getValue() instanceof List) {
				List<?> sessionlist = (List<?>) resultEntry.getValue();
				for (Object element : sessionlist) {
					Map<String, Object> session = POJOReMapper.remap(element, new TypeReference<Map<String, Object>>() {
					});

					String username = "" + session.get("UserName");
					String logondomain = "" + session.get("LogonDomain");

					if (!value.toString().isEmpty()) {
						value.append("; ");
					}

					value.append(username + " (" + logondomain + "\\" + username + ")");
				}
			} else {
				Logging.warning(this, "resultEntry's value is neither a String nor a List");
			}

			result.put(resultEntry.getKey(), value.toString());
		}

		return result;
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

		OpsiMethodCall omc = new OpsiMethodCall("host_updateObjects", new Object[] { updates.toArray() });

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

	public Map<String, Map<String, String>> getProductGroups() {
		if (productGroups != null) {
			return productGroups;
		}

		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);

		Map<String, Map<String, String>> result = exec.getStringMappedObjectsByKey(
				new OpsiMethodCall("group_getObjects", new Object[] { callAttributes, callFilter }), "ident",
				new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" });

		productGroups = result;

		return productGroups;
	}

	public void productGroupsRequestRefresh() {
		productGroups = null;
	}

	public Map<String, Map<String, String>> getHostGroups() {
		if (hostGroups != null) {
			return hostGroups;
		}

		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);

		hostGroups = new HostGroups(exec.getStringMappedObjectsByKey(
				new OpsiMethodCall("group_getObjects", new Object[] { callAttributes, callFilter }), "ident",
				new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" }), this);

		Logging.debug(this, "getHostGroups " + hostGroups);

		hostGroups = hostGroups.addSpecialGroups();
		Logging.debug(this, "getHostGroups " + hostGroups);
		hostGroups.alterToWorkingVersion();

		Logging.debug(this, "getHostGroups rebuilt" + hostGroups);

		return hostGroups;
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

	private static Map<String, Set<String>> projectToFunction(Map<String, Map<String, String>> mappedRelation,
			String originVar, String imageVar) {
		Map<String, Set<String>> result = new TreeMap<>();

		Iterator<String> iter = mappedRelation.keySet().iterator();

		while (iter.hasNext()) {
			String key = iter.next();

			Map<String, String> relation = mappedRelation.get(key);
			String originValue = relation.get(originVar);
			String imageValue = relation.get(imageVar);
			if (imageValue != null) {
				Set<String> assignedSet = result.computeIfAbsent(originValue, arg -> new TreeSet<>());
				assignedSet.add(imageValue);
			}
		}

		return result;
	}

	public Map<String, Set<String>> getFGroup2Members() {
		if (fGroup2Members == null) {
			fGroup2Members = retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP, "clientId");
		}

		return fGroup2Members;
	}

	public Map<String, Set<String>> getFProductGroup2Members() {
		if (fProductGroup2Members == null) {
			fProductGroup2Members = retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP, "productId");
		}

		return fProductGroup2Members;
	}

	// returns the function that yields for a given groupId all objects which belong
	// to the group
	private Map<String, Set<String>> retrieveFGroup2Members(String groupType, String memberIdName) {
		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("groupType", groupType);

		Map<String, Map<String, String>> mappedRelations =

				exec.getStringMappedObjectsByKey(
						new OpsiMethodCall("objectToGroup_getObjects", new Object[] { callAttributes, callFilter }),
						"ident", new String[] { "objectId", "groupId" }, new String[] { memberIdName, "groupId" });

		return projectToFunction(mappedRelations, "groupId", memberIdName);
	}

	public void fObject2GroupsRequestRefresh() {
		fObject2Groups = null;
	}

	// returns the function that yields for a given clientId all groups to which the
	// client belongs
	public Map<String, Set<String>> getFObject2Groups() {
		if (fObject2Groups == null) {
			Map<String, Map<String, String>> mappedRelations =

					exec.getStringMappedObjectsByKey(new OpsiMethodCall("objectToGroup_getObjects", new String[] {}),
							"ident", new String[] { "objectId", "groupId" }, new String[] { "clientId", "groupId" },
							ClientTree.getTranslationsFromPersistentNames());

			fObject2Groups = projectToFunction(mappedRelations, "clientId", "groupId");

		}

		return fObject2Groups;
	}

	public boolean addHosts2Group(List<String> objectIds, String groupId) {
		if (globalReadOnly) {
			return false;
		}

		Logging.info(this, "addHosts2Group hosts " + objectIds);

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);

		List<Map<String, Object>> jsonObjects = new ArrayList<>();

		for (String ob : objectIds) {
			Map<String, Object> item = createNOMitem(Object2GroupEntry.TYPE_NAME);
			item.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			item.put(Object2GroupEntry.MEMBER_KEY, ob);
			item.put(Object2GroupEntry.GROUP_ID_KEY, persistentGroupId);
			jsonObjects.add(item);
		}

		Logging.info(this, "addHosts2Group persistentGroupId " + persistentGroupId);

		OpsiMethodCall omc = new OpsiMethodCall("objectToGroup_createObjects", new Object[] { jsonObjects });

		return exec.doCall(omc);
	}

	public boolean addObject2Group(String objectId, String groupId) {
		if (globalReadOnly) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		Logging.debug(this, "addObject2Group persistentGroupId " + persistentGroupId);
		OpsiMethodCall omc = new OpsiMethodCall("objectToGroup_create",
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
			OpsiMethodCall omc = new OpsiMethodCall("objectToGroup_deleteObjects",
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
		OpsiMethodCall omc = new OpsiMethodCall("objectToGroup_delete",
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

		OpsiMethodCall omc = new OpsiMethodCall("group_createHostGroup",
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

		OpsiMethodCall omc = new OpsiMethodCall("group_delete", new String[] { groupId });
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

		OpsiMethodCall omc = new OpsiMethodCall("group_updateObject", new Object[] { updateInfo });
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

		OpsiMethodCall omc = new OpsiMethodCall("group_createObjects", new Object[] { new Object[] { map } });
		result = exec.doCall(omc);

		HashSet<String> inNewSetnotInOriSet = new HashSet<>(productSet);
		HashSet<String> inOriSetnotInNewSet = new HashSet<>();

		if (getFProductGroup2Members().get(groupId) != null) {
			Set<String> oriSet = getFProductGroup2Members().get(groupId);
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
			result = result
					&& exec.doCall(new OpsiMethodCall("objectToGroup_deleteObjects", new Object[] { object2Groups }));
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
			result = result
					&& exec.doCall(new OpsiMethodCall("objectToGroup_createObjects", new Object[] { object2Groups }));
		}

		if (result) {
			getFProductGroup2Members().put(groupId, productSet);
		}

		return result;
	}

	public List<String> getHostGroupIds() {
		Set<String> groups = getHostGroups().keySet();
		groups.remove(ClientTree.DIRECTORY_NAME);

		return new ArrayList<>(groups);
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

	// partial version of produceHwAuditDeviceClasses()
	private static List<String> produceHwClasses(List<Map<String, List<Map<String, Object>>>> hwAuditConf) {
		List<String> result = new ArrayList<>();

		for (Map<String, List<Map<String, Object>>> hwAuditClass : hwAuditConf) {

			String hwClass = (String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.OPSI_KEY);

			result.add(hwClass);
		}

		return result;
	}

	private List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConf() {
		if (hwAuditConf == null) {
			Logging.warning("hwAuditConf is null in getOpsiHWAuditConf");
			return new ArrayList<>();
		} else if (!hwAuditConf.containsKey("")) {
			hwAuditConf.put("",
					exec.getListOfMapsOfListsOfMaps(new OpsiMethodCall("auditHardware_getConfig", new String[] {})));
			if (hwAuditConf.get("") == null) {
				Logging.warning(this, "got no hardware config");
			}
		} else {
			// hwAuditConf already contains key "" and is initialized
		}

		return hwAuditConf.get("");
	}

	public List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConf(String locale) {
		return hwAuditConf.computeIfAbsent(locale, s -> exec
				.getListOfMapsOfListsOfMaps(new OpsiMethodCall("auditHardware_getConfig", new String[] { locale })));
	}

	public List<String> getAllHwClassNames() {
		if (opsiHwClassNames == null) {
			opsiHwClassNames = produceHwClasses(getOpsiHWAuditConf());
		}

		Logging.info(this, "getAllHwClassNames, hw classes " + opsiHwClassNames);

		return opsiHwClassNames;
	}

	public Map<String, OpsiHwAuditDeviceClass> getHwAuditDeviceClasses() {
		if (hwAuditDeviceClasses == null) {
			produceHwAuditDeviceClasses();
		}

		return hwAuditDeviceClasses;
	}

	public void softwareAuditOnClientsRequestRefresh() {
		Logging.info(this, "softwareAuditOnClientsRequestRefresh");
		dataStub.softwareAuditOnClientsRequestRefresh();
	}

	public void fillClient2Software(List<String> clients) {
		dataStub.fillClient2Software(clients);
	}

	public Map<String, List<SWAuditClientEntry>> getClient2Software() {
		return dataStub.getClient2Software();
	}

	/*
	 * the method is only additionally called because of the retry mechanism
	 */
	public void getSoftwareAudit(String clientId) {
		dataStub.fillClient2Software(clientId);

		List<SWAuditClientEntry> entries = dataStub.getClient2Software().get(clientId);

		if (entries == null) {
			return;
		}

		for (SWAuditClientEntry entry : entries) {
			if (entry.getSWid() != null) {
				int i = entry.getSWid();

				Logging.debug(this, "getSoftwareAudit,  ID " + i + " for client entry " + entry);
				if (i == -1) {
					Logging.info(this, "getSoftwareAudit,  not found client entry " + entry);
					int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
							Configed.getResourceValue("PersistenceController.reloadSoftwareInformation.message") + " "
									+ entry.getSWident()
									+ Configed.getResourceValue(
											"PersistenceController.reloadSoftwareInformation.question")
									+ Configed.getResourceValue("PersistenceController.reloadSoftwareInformation.info"),
							Configed.getResourceValue("PersistenceController.reloadSoftwareInformation.title"),
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

					switch (returnedOption) {
					case JOptionPane.NO_OPTION:
						break;

					case JOptionPane.YES_OPTION:
						installedSoftwareInformationRequestRefresh();
						softwareAuditOnClientsRequestRefresh();
						return;

					case JOptionPane.CANCEL_OPTION:
						return;

					default:
						Logging.warning(this, "no case found for returnedOption in getSoftwareAuditOnce");
						break;
					}
				}
			}
		}
	}

	public String getLastSoftwareAuditModification(String clientId) {
		String result = "";

		if (clientId != null && !clientId.isEmpty() && dataStub.getClient2Software() != null
				&& dataStub.getClient2Software().get(clientId) != null
				&& !dataStub.getClient2Software().get(clientId).isEmpty()) {
			result = dataStub.getClient2Software().get(clientId).get(0).getLastModification();
		}

		return result;
	}

	public Map<String, Map<String, Object>> retrieveSoftwareAuditData(String clientId) {
		Map<String, Map<String, Object>> result = new TreeMap<>();

		if (clientId == null || clientId.isEmpty()) {
			return result;
		}

		dataStub.fillClient2Software(clientId);

		List<SWAuditClientEntry> entries = dataStub.getClient2Software().get(clientId);

		if (entries == null) {
			return result;
		}

		for (SWAuditClientEntry entry : entries) {
			if (entry.getSWid() != null && entry.getSWid() != -1) {
				result.put("" + entry.getSWid(),
						entry.getExpandedMap(getInstalledSoftwareInformation(), getSWident(entry.getSWid())));
			}
		}

		return result;
	}

	public Map<String, List<Map<String, Object>>> getHardwareInfo(String clientId) {
		if (clientId == null) {
			return new HashMap<>();
		}

		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put(HOST_KEY, clientId);

		List<Map<String, Object>> hardwareInfos = exec.getListOfMaps(
				new OpsiMethodCall("auditHardwareOnHost_getObjects", new Object[] { callAttributes, callFilter }));

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime scanTime = LocalDateTime.parse("2000-01-01 00:00:00", timeFormatter);
		Map<String, List<Map<String, Object>>> result = new HashMap<>();
		for (Map<String, Object> hardwareInfo : hardwareInfos) {
			if (result.containsKey(hardwareInfo.get("hardwareClass"))) {
				List<Map<String, Object>> hardwareClassInfos = result.get(hardwareInfo.get("hardwareClass"));
				hardwareClassInfos.add(hardwareInfo);
			} else {
				List<Map<String, Object>> hardwareClassInfos = new ArrayList<>();
				hardwareClassInfos.add(hardwareInfo);
				result.put((String) hardwareInfo.get("hardwareClass"), hardwareClassInfos);
			}
			Object lastSeenStr = hardwareInfo.get("lastseen");
			LocalDateTime lastSeen = scanTime;
			if (lastSeenStr != null) {
				lastSeen = LocalDateTime.parse(lastSeenStr.toString(), timeFormatter);
			}
			if (scanTime.compareTo(lastSeen) < 0) {
				scanTime = lastSeen;
			}
		}

		List<Map<String, Object>> scanProperties = new ArrayList<>();
		Map<String, Object> scanProperty = new HashMap<>();
		scanProperty.put("scantime", scanTime.format(timeFormatter));
		scanProperties.add(scanProperty);
		result.put("SCANPROPERTIES", scanProperties);

		if (result.size() > 1) {
			return result;
		}

		return new HashMap<>();
	}

	public void auditHardwareOnHostRequestRefresh() {
		relationsAuditHardwareOnHost = null;
	}

	public List<Map<String, Object>> getHardwareOnClient() {
		if (relationsAuditHardwareOnHost == null) {
			Map<String, String> filterMap = new HashMap<>();
			filterMap.put("state", "1");
			relationsAuditHardwareOnHost = exec.getListOfMaps(
					new OpsiMethodCall("auditHardwareOnHost_getHashes", new Object[] { new String[0], filterMap }));
		}

		return relationsAuditHardwareOnHost;
	}

	/* multiclient hwinfo */

	public List<String> getClient2HwRowsColumnNames() {
		retrieveClient2HwRowsColumnNames();
		return client2HwRowsColumnNames;
	}

	public List<String> getClient2HwRowsJavaclassNames() {
		retrieveClient2HwRowsColumnNames();
		return client2HwRowsJavaclassNames;
	}

	private void produceHwAuditDeviceClasses() {
		hwAuditDeviceClasses = new TreeMap<>();

		if (getOpsiHWAuditConf().isEmpty()) {
			Logging.error(this, "no hwaudit config found ");
			return;
		}

		for (Map<String, List<Map<String, Object>>> hwAuditClass : getOpsiHWAuditConf()) {
			if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) == null
					|| hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) == null
			//|| !(hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) instanceof Map)
			/*|| !(hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) instanceof List)*/) {
				Logging.warning(this, "getAllHwClassNames illegal hw config item, having hwAuditClass.get Class "
						+ hwAuditClass.get("Class"));
				if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) != null) {
					Logging.warning(this,
							"getAllHwClassNames illegal hw config item,  hwAuditClass.get Class is of class "
									+ hwAuditClass.get("Class").getClass());
				}
				if (hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) != null) {
					Logging.warning(this,
							"getAllHwClassNames illegal hw config item,  hwAuditClass.get Values is of class "
									+ hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY).getClass());
				}

				continue;
			}
			String hwClass = (String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.OPSI_KEY);

			OpsiHwAuditDevicePropertyType firstSeen = new OpsiHwAuditDevicePropertyType(hwClass);
			firstSeen.setOpsiDbColumnName(OpsiHwAuditDeviceClass.FIRST_SEEN_COL_NAME);
			firstSeen.setOpsiDbColumnType("timestamp");
			OpsiHwAuditDevicePropertyType lastSeen = new OpsiHwAuditDevicePropertyType(hwClass);
			lastSeen.setOpsiDbColumnName(OpsiHwAuditDeviceClass.LAST_SEEN_COL_NAME);
			lastSeen.setOpsiDbColumnType("timestamp");

			OpsiHwAuditDeviceClass hwAuditDeviceClass = new OpsiHwAuditDeviceClass(hwClass);
			hwAuditDeviceClasses.put(hwClass, hwAuditDeviceClass);

			hwAuditDeviceClass.setLinuxQuery((String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.LINUX_KEY));
			hwAuditDeviceClass.setWmiQuery((String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.WMI_KEY));

			Logging.info(this, "hw audit class " + hwClass);

			for (Object m : (List<?>) hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY)) {
				if (!(m instanceof Map)) {
					Logging.warning(this, "getAllHwClassNames illegal VALUES item, m " + m);
					continue;
				}

				Map<?, ?> ma = (Map<?, ?>) m;

				if ("i".equals(ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY))) {
					OpsiHwAuditDevicePropertyType devProperty = new OpsiHwAuditDevicePropertyType(hwClass);
					devProperty.setOpsiDbColumnName((String) ma.get(OpsiHwAuditDeviceClass.OPSI_KEY));
					devProperty.setOpsiDbColumnType((String) ma.get(OpsiHwAuditDeviceClass.TYPE_KEY));

					hwAuditDeviceClass.addHostRelatedProperty(devProperty);
					hwAuditDeviceClass.setHostConfigKey((OpsiHwAuditDeviceClass.CONFIG_KEY + "." + hwClass + "_"
							+ OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE).toLowerCase(Locale.ROOT));

				} else if ("g".equals(ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY))) {
					OpsiHwAuditDevicePropertyType devProperty = new OpsiHwAuditDevicePropertyType(hwClass);
					devProperty.setOpsiDbColumnName((String) ma.get(OpsiHwAuditDeviceClass.OPSI_KEY));
					devProperty.setOpsiDbColumnType((String) ma.get(OpsiHwAuditDeviceClass.TYPE_KEY));

					hwAuditDeviceClass.addHwItemRelatedProperty(devProperty);
					hwAuditDeviceClass.setHwItemConfigKey((OpsiHwAuditDeviceClass.CONFIG_KEY + "." + hwClass + "_"
							+ OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE).toLowerCase(Locale.ROOT));
				} else {
					Logging.warning(this, "getAllHwClassNames illegal value for key " + OpsiHwAuditDeviceClass.SCOPE_KEY
							+ " " + ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY));
				}
			}

			hwAuditDeviceClass.addHostRelatedProperty(firstSeen);
			hwAuditDeviceClass.addHostRelatedProperty(lastSeen);

			Logging.info(this, "hw audit class " + hwAuditDeviceClass);
		}

		Logging.info(this, "produceHwAuditDeviceClasses hwAuditDeviceClasses size " + hwAuditDeviceClasses.size());
	}

	private String cutClassName(String columnName) {
		String result = null;

		if (!columnName.startsWith("HOST") && columnName.startsWith(HW_INFO_CONFIG)) {
			result = columnName.substring(HW_INFO_CONFIG.length());
			result = result.substring(0, result.indexOf('.'));
		} else if (columnName.startsWith(HW_INFO_DEVICE)) {
			result = columnName.substring(HW_INFO_DEVICE.length());
			result = result.substring(0, result.indexOf('.'));
		} else {
			Logging.warning(this, "cutClassName " + "unexpected columnName " + columnName);
		}

		return result;
	}

	private void retrieveClient2HwRowsColumnNames() {
		getConfigOptions();

		Logging.info(this, "retrieveClient2HwRowsColumnNames " + "client2HwRowsColumnNames == null "
				+ (client2HwRowsColumnNames == null));
		if (client2HwRowsColumnNames == null || hwInfoClassNames == null || client2HwRowsJavaclassNames == null) {
			hostColumnNames = new ArrayList<>();

			// todo make static variables
			hostColumnNames.add("HOST.hostId");
			hostColumnNames.add("HOST.description");
			hostColumnNames.add("HOST.hardwareAdress");
			hostColumnNames.add(LAST_SEEN_VISIBLE_COL_NAME);

			getConfigOptions();
			// there is produced client2HwRowsColumnNames

			client2HwRowsColumnNames = new ArrayList<>(hostColumnNames);

			for (Entry<String, OpsiHwAuditDeviceClass> hwClass : hwAuditDeviceClasses.entrySet()) {
				OpsiHwAuditDeviceClass hwAuditDeviceClass = hwClass.getValue();

				for (OpsiHwAuditDevicePropertyType deviceProperty : hwAuditDeviceClass.getDeviceHostProperties()) {
					if (deviceProperty.getDisplayed() != null && deviceProperty.getDisplayed()) {
						String col = HW_INFO_CONFIG + hwClass.getKey() + "." + deviceProperty.getOpsiDbColumnName();
						client2HwRowsColumnNames.add(col);
					}
				}

				for (OpsiHwAuditDevicePropertyType deviceProperty : hwAuditDeviceClass.getDeviceHwItemProperties()) {
					if (deviceProperty.getDisplayed() != null && deviceProperty.getDisplayed()) {
						String col = HW_INFO_DEVICE + hwClass.getKey() + "." + deviceProperty.getOpsiDbColumnName();
						client2HwRowsColumnNames.add(col);
					}
				}
			}
			client2HwRowsJavaclassNames = new ArrayList<>();
			Set<String> hwInfoClasses = new HashSet<>();

			for (String columnName : client2HwRowsColumnNames) {
				Logging.info(this, "retrieveClient2HwRowsColumnNames col " + columnName);
				client2HwRowsJavaclassNames.add("java.lang.String");
				String className = cutClassName(columnName);
				if (className != null) {
					hwInfoClasses.add(className);
				}
			}

			hwInfoClassNames = new ArrayList<>(hwInfoClasses);

			Logging.info(this, "retrieveClient2HwRowsColumnNames hwInfoClassNames " + hwInfoClassNames);

		}
	}

	public List<String> getHwInfoClassNames() {
		retrieveClient2HwRowsColumnNames();
		Logging.info(this, "getHwInfoClassNames " + hwInfoClassNames);
		return hwInfoClassNames;
	}

	public void client2HwRowsRequestRefresh() {
		hostColumnNames = null;
		client2HwRowsColumnNames = null;
		dataStub.client2HwRowsRequestRefresh();
	}

	public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts) {
		Map<String, Map<String, Object>> client2HwRows = dataStub.getClient2HwRows(hosts);

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
		getConfigOptions();

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
		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { readyObjects });

		return exec.doCall(omc);

	}

	public Map<String, String> getEmptyLogfiles() {
		logfiles = new HashMap<>();
		String[] logtypes = Globals.getLogTypes();

		for (int i = 0; i < logtypes.length; i++) {
			logfiles.put(logtypes[i], "");
		}

		return logfiles;
	}

	public Map<String, String> getLogfiles(String clientId, String logtype) {

		if (logfiles == null) {
			getEmptyLogfiles();
		}

		int i = Arrays.asList(Globals.getLogTypes()).indexOf(logtype);
		if (i < 0) {
			Logging.error("illegal logtype: " + logtype);
			return logfiles;
		}

		Logging.debug(this, "getLogfile logtye " + logtype);

		String[] logtypes = Globals.getLogTypes();

		String s = "";

		Logging.debug(this, "OpsiMethodCall log_read " + logtypes[i] + " max size " + Globals.getMaxLogSize(i));

		try {
			if (Globals.getMaxLogSize(i) == 0) {
				s = exec.getStringResult(new OpsiMethodCall("log_read", new String[] { logtype, clientId }));
			} else {
				s = exec.getStringResult(new OpsiMethodCall("log_read",
						new String[] { logtype, clientId, String.valueOf(Globals.getMaxLogSize(i)) }));
			}

		} catch (OutOfMemoryError e) {
			s = "--- file too big for showing, enlarge java memory  ---";
			Logging.debug(this, "thrown exception: " + e);
		}

		logfiles.put(logtype, s);

		return logfiles;
	}

	public void depotChange() {
		Logging.info(this, "depotChange");
		productGlobalInfos = null;
		possibleActions = null;
		productIds = null;
		netbootProductNames = null;
		localbootProductNames = null;
		retrieveProducts();
		retrieveProductPropertyDefinitions();
		getProductGlobalInfos(theDepot);

	}

	public void productDataRequestRefresh() {
		dataStub.productDataRequestRefresh();
		productpropertiesRequestRefresh();
		depot2product2properties = null;
		productGroups = null;
		depotChange();
	}

	public List<String> getAllProductNames(String depotId) {
		String callReturnType = "dict";
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("depotId", depotId);

		OpsiMethodCall omc = new OpsiMethodCall("productOnDepot_getIdents",
				new Object[] { callReturnType, callFilter });
		List<Map<String, Object>> result = exec.getListOfMaps(omc);

		return result.stream().map(v -> (String) v.get("productId")).collect(Collectors.toList());
	}

	public List<String> getProvidedLocalbootProducts(String depotId) {
		String callReturnType = "dict";
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("depotId", depotId);
		callFilter.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);

		OpsiMethodCall omc = new OpsiMethodCall("productOnDepot_getIdents",
				new Object[] { callReturnType, callFilter });
		List<Map<String, Object>> result = exec.getListOfMaps(omc);

		return result.stream().map(v -> (String) v.get("productId")).collect(Collectors.toList());
	}

	public List<String> getProvidedNetbootProducts(String depotId) {
		String callReturnType = "dict";
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("depotId", depotId);
		callFilter.put("productType", OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);

		OpsiMethodCall omc = new OpsiMethodCall("productOnDepot_getIdents",
				new Object[] { callReturnType, callFilter });
		List<Map<String, Object>> result = exec.getListOfMaps(omc);

		return result.stream().map(v -> (String) v.get("productId")).collect(Collectors.toList());
	}

	public List<String> getAllLocalbootProductNames(String depotId) {
		Logging.debug(this, "getAllLocalbootProductNames for depot " + depotId);
		Logging.info(this, "getAllLocalbootProductNames, producing " + (localbootProductNames == null));
		if (localbootProductNames == null) {
			Map<String, List<String>> productOrderingResult = exec
					.getMapOfStringLists(new OpsiMethodCall("getProductOrdering", new String[] { depotId }));

			List<String> sortedProducts = productOrderingResult.get("sorted");
			if (sortedProducts == null) {
				sortedProducts = new ArrayList<>();
			}

			List<String> notSortedProducts = productOrderingResult.get("not_sorted");
			if (notSortedProducts == null) {
				notSortedProducts = new ArrayList<>();
			}

			Logging.info(this, "not ordered " + (notSortedProducts.size() - sortedProducts.size()) + "");

			notSortedProducts.removeAll(sortedProducts);
			Logging.info(this, "missing: " + notSortedProducts);

			localbootProductNames = sortedProducts;
			localbootProductNames.addAll(notSortedProducts);

			// we don't have a productsgroupsFullPermission)
			if (permittedProducts != null) {
				localbootProductNames.retainAll(permittedProducts);
			}
		}

		Logging.info(this, "localbootProductNames sorted, size " + localbootProductNames.size());

		return new ArrayList<>(localbootProductNames);
	}

	public List<String> getAllLocalbootProductNames() {
		return getAllLocalbootProductNames(theDepot);
	}

	public void retrieveProducts() {
		retrieveDepotProducts(theDepot);
	}

	private void retrieveDepotProducts(String depotId) {
		Logging.debug(this, "retrieveDepotProducts for " + depotId);

		if (dataStub.getDepot2NetbootProducts().get(depotId) != null) {
			netbootProductNames = new ArrayList<>(dataStub.getDepot2NetbootProducts().get(depotId).keySet());
		} else {
			netbootProductNames = new ArrayList<>();
		}

		// we don't have a productsgroupsFullPermission)
		if (permittedProducts != null) {
			netbootProductNames.retainAll(permittedProducts);
		}

		// for localboot products, we have to look for ordering information
		localbootProductNames = getAllLocalbootProductNames(depotId);
	}

	public List<String> getAllDepotsWithIdenticalProductStock(String depot) {
		List<String> result = new ArrayList<>();

		TreeSet<OpsiPackage> first = dataStub.getDepot2Packages().get(depot);
		Logging.info(this, "getAllDepotsWithIdenticalProductStock " + first);

		for (String testdepot : getHostInfoCollections().getAllDepots().keySet()) {
			if (depot.equals(testdepot) || (first == null && dataStub.getDepot2Packages().get(testdepot) == null)
					|| (first != null && first.equals(dataStub.getDepot2Packages().get(testdepot)))) {
				result.add(testdepot);
			}
		}
		Logging.info(this, "getAllDepotsWithIdenticalProductStock  result " + result);

		return result;
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

	public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos() {
		return dataStub.getProduct2versionInfo2infos();
	}

	public Object2Product2VersionList getDepot2LocalbootProducts() {
		return dataStub.getDepot2LocalbootProducts();
	}

	public Object2Product2VersionList getDepot2NetbootProducts() {
		return dataStub.getDepot2NetbootProducts();
	}

	private void retrieveProductGlobalInfos(String depotId) {

		Logging.info(this, "retrieveProductGlobalInfos , depot " + depotId);

		productGlobalInfos = new HashMap<>();
		possibleActions = new HashMap<>();

		for (String productId : dataStub.getProduct2versionInfo2infos().keySet()) {
			if (dataStub.getProduct2versionInfo2infos().get(productId) == null) {
				Logging.warning(this, "retrieveProductGlobalInfos productId == null for product " + productId);
			}

			if (dataStub.getProduct2versionInfo2infos().get(productId) != null) {
				String versionInfo = null;
				Map<String, OpsiProductInfo> productAllInfos = dataStub.getProduct2versionInfo2infos().get(productId);

				// look for associated product on depot info
				HashMap<String, List<String>> product2VersionList = dataStub.getDepot2LocalbootProducts().get(depotId);
				if (product2VersionList != null && product2VersionList.get(productId) != null
						&& !product2VersionList.get(productId).isEmpty()) {
					versionInfo = product2VersionList.get(productId).get(0);
				}

				if (versionInfo == null) {
					product2VersionList = dataStub.getDepot2NetbootProducts().get(depotId);

					if (product2VersionList != null && product2VersionList.get(productId) != null
							&& !product2VersionList.get(productId).isEmpty()) {
						versionInfo = product2VersionList.get(productId).get(0);
					}
				}

				// if found go on

				if (versionInfo != null && productAllInfos.get(versionInfo) != null) {
					OpsiProductInfo productInfo = productAllInfos.get(versionInfo);

					possibleActions.put(productId, productInfo.getPossibleActions());

					Map<String, Object> aProductInfo = new HashMap<>();

					aProductInfo.put("actions", productInfo.getPossibleActions());

					aProductInfo.put(ProductState.KEY_PRODUCT_ID, productId
					// productInfo.getProductId()
					);
					aProductInfo.put(ProductState.KEY_VERSION_INFO,
							Globals.ProductPackageVersionSeparator.formatKeyForDisplay(productInfo.getVersionInfo()));

					aProductInfo.put(ProductState.KEY_PRODUCT_PRIORITY, productInfo.getPriority());

					aProductInfo.put(ProductState.KEY_PRODUCT_NAME,
							// OpsiProductInfo.SERVICEkeyPRODUCT_NAME,
							productInfo.getProductName());

					aProductInfo.put(OpsiProductInfo.SERVICE_KEY_PRODUCT_DESCRIPTION, productInfo.getDescription());

					aProductInfo.put(OpsiProductInfo.SERVICE_KEY_PRODUCT_ADVICE, productInfo.getAdvice());

					aProductInfo.put(ProductState.KEY_PRODUCT_VERSION, productInfo.getProductVersion());

					aProductInfo.put(ProductState.KEY_PACKAGE_VERSION, productInfo.getPackageVersion());

					aProductInfo.put(OpsiPackage.SERVICE_KEY_LOCKED, productInfo.getLockedInfo());

					Logging.debug(this, "productInfo " + aProductInfo);

					productGlobalInfos.put(productId, aProductInfo);
				}
			}
		}

		Logging.info(this, "retrieveProductGlobalInfos  found number  " + productGlobalInfos.size());
	}

	private void checkProductGlobalInfos(String depotId) {
		Logging.info(this, "checkProductGlobalInfos for Depot " + depotId);
		if (!theDepot.equals(depotId)) {
			Logging.warning(this, "depot irregular, preset " + theDepot);
		}
		if (depotId == null || depotId.isEmpty()) {
			Logging.notice(this, "checkProductGlobalInfos called for no depot");
		}
		Logging.debug(this, "checkProductGlobalInfos depotId " + depotId + " productGlobaInfos  = null "
				+ (productGlobalInfos == null) + " possibleActions = null " + (possibleActions == null));
		if (possibleActions == null || productGlobalInfos == null || theDepot == null || !theDepot.equals(depotId)) {
			retrieveProductGlobalInfos(depotId);
		}
	}

	// map with key productId
	public Map<String, List<String>> getPossibleActions(String depotId) {
		Logging.debug(this, "getPossibleActions depot irregular " + !theDepot.equals(depotId));
		checkProductGlobalInfos(depotId);
		return possibleActions;
	}

	public Map<String, List<Map<String, String>>> getMapOfProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfProductStatesAndActions for : " + Arrays.toString(clientIds));

		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}

		return getProductStatesNOM(clientIds);
	}

	private Map<String, List<Map<String, String>>> getLocalBootProductStates(String[] clientIds) {
		return getLocalBootProductStatesNOM(clientIds);
	}

	private Map<String, List<Map<String, String>>> getProductStatesNOM(String[] clientIds) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));

		List<Map<String, Object>> productOnClients = exec.getListOfMaps(
				new OpsiMethodCall("productOnClient_getHashes", new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		for (Map<String, Object> m : productOnClients) {
			String client = (String) m.get("clientId");

			result.computeIfAbsent(client, arg -> new ArrayList<>())
					.add(new ProductState(POJOReMapper.giveEmptyForNull(m), true));
		}
		return result;
	}

	private Map<String, List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));
		callFilter.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);

		List<Map<String, Object>> productOnClients = exec.getListOfMaps(
				new OpsiMethodCall("productOnClient_getHashes", new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		for (Map<String, Object> m : productOnClients) {

			String client = (String) m.get("clientId");
			List<Map<String, String>> states1Client = result.computeIfAbsent(client, arg -> new ArrayList<>());

			Map<String, String> aState = new ProductState(POJOReMapper.giveEmptyForNull(m), true);
			states1Client.add(aState);
		}

		return result;
	}

	@SuppressWarnings("java:S1168")
	public Map<String, List<Map<String, String>>> getMapOfLocalbootProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfLocalbootProductStatesAndActions for : " + Arrays.toString(clientIds));

		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}

		return getLocalBootProductStates(clientIds);
	}

	private Map<String, List<Map<String, String>>> getNetBootProductStatesNOM(String[] clientIds) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));
		callFilter.put("productType", OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);

		List<Map<String, Object>> productOnClients = exec.getListOfMaps(
				new OpsiMethodCall("productOnClient_getHashes", new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		for (Map<String, Object> m : productOnClients) {

			String client = (String) m.get("clientId");
			result.computeIfAbsent(client, arg -> new ArrayList<>())
					.add(new ProductState(POJOReMapper.giveEmptyForNull(m), true));

		}
		return result;
	}

	@SuppressWarnings("java:S1168")
	public Map<String, List<Map<String, String>>> getMapOfNetbootProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfNetbootProductStatesAndActions for : " + Arrays.toString(clientIds));

		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}

		return getNetBootProductStatesNOM(clientIds);
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

			OpsiMethodCall omc = new OpsiMethodCall("productOnClient_updateObjects", new Object[] { updateItems });

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

		boolean result = true;

		List<Map<String, Object>> deleteProductItems = new ArrayList<>();

		for (int i = 0; i < selectedClients.length; i++) {
			for (String product : localbootProductNames) {
				Map<String, Object> productOnClientItem = createNOMitem("ProductOnClient");
				productOnClientItem.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
				productOnClientItem.put("clientId", selectedClients[i]);
				productOnClientItem.put("productId", product);

				deleteProductItems.add(productOnClientItem);
			}
		}

		Logging.info(this, "resetLocalbootProducts deleteProductItems.size " + deleteProductItems.size());

		result = resetProducts(deleteProductItems, withDependencies);

		Logging.debug(this, "resetLocalbootProducts result " + result);

		return result;
	}

	public boolean resetNetbootProducts(String[] selectedClients, boolean withDependencies) {
		if (globalReadOnly) {
			return false;
		}

		boolean result = true;

		List<Map<String, Object>> deleteProductItems = new ArrayList<>();

		for (int i = 0; i < selectedClients.length; i++) {
			for (String product : netbootProductNames) {
				Map<String, Object> productOnClientItem = createNOMitem("ProductOnClient");
				productOnClientItem.put("productType", OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
				productOnClientItem.put("clientId", selectedClients[i]);
				productOnClientItem.put("productId", product);

				deleteProductItems.add(productOnClientItem);
			}
		}

		Logging.info(this, "resetNetbootProducts deleteProductItems.size " + deleteProductItems.size());

		result = resetProducts(deleteProductItems, withDependencies);

		Logging.debug(this, "resetNetbootProducts result " + result);

		return result;
	}

	private boolean resetProducts(Collection<Map<String, Object>> productItems, boolean withDependencies) {
		if (globalReadOnly) {
			return false;
		}

		boolean result = true;

		Logging.info(this, "resetProducts productItems.size " + productItems.size());

		if (!productItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall("productOnClient_deleteObjects",
					new Object[] { productItems.toArray() });

			result = exec.doCall(omc);

			Logging.debug(this, "resetProducts result " + result);

			if (result && withDependencies) {
				omc = new OpsiMethodCall("productPropertyState_delete",
						new Object[] { productItems.stream().map(p -> p.get("productId")).toArray(), "*",
								productItems.stream().map(p -> p.get("clientId")).toArray() });

				result = exec.doCall(omc);
			}
		}

		Logging.debug(this, "resetProducts result " + result);

		return result;
	}

	public void retrieveProductDependencies() {
		dataStub.getDepot2product2dependencyInfos();
	}

	public Map<String, Map<String, Object>> getProductGlobalInfos(String depotId) {
		checkProductGlobalInfos(depotId);
		return productGlobalInfos;
	}

	public Map<String, String> getProductInfos(String productId, String clientId) {

		String[] callAttributes = new String[] {};

		HashMap<String, String> callFilter = new HashMap<>();
		callFilter.put(OpsiPackage.DB_KEY_PRODUCT_ID, productId);
		callFilter.put("clientId", clientId);

		Map<String, Object> retrievedMap = retrieveListOfMapsNOM(callAttributes, callFilter,
				"productOnClient_getHashes").get(0);

		return new ProductState(POJOReMapper.giveEmptyForNull(retrievedMap), true);
	}

	public Map<String, Map<String, String>> getProductDefaultStates() {
		if (productIds == null) {
			getProductIds();
		}

		Logging.debug(this, "getProductDefaultStates, count " + productDefaultStates.size());
		return productDefaultStates;
	}

	public List<List<Object>> getProductRows() {
		return dataStub.getProductRows();
	}

	public Map<String, Map<String, List<String>>> getProduct2VersionInfo2Depots() {
		return dataStub.getProduct2VersionInfo2Depots();
	}

	public NavigableSet<String> getProductIds() {
		dataStub.getProduct2versionInfo2infos();

		if (productIds == null) {
			productIds = new TreeSet<>();
			productDefaultStates = new TreeMap<>();

			for (String productId : dataStub.getProduct2versionInfo2infos().keySet()) {
				productIds.add(productId);
				ProductState productDefault = new ProductState(null);
				productDefault.put("productId", productId);
				productDefaultStates.put(productId, productDefault);
			}

			Logging.info(this, "getProductIds size / names " + productIds.size() + " / ... ");
		}

		return productIds;
	}

	private List<Map<String, String>> getProductDependencies(String depotId, String productId) {
		List<Map<String, String>> result = null;

		if (dataStub.getDepot2product2dependencyInfos().get(depotId) != null) {
			result = dataStub.getDepot2product2dependencyInfos().get(depotId).get(productId);
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

		if (dataStub.getDepot2product2dependencyInfos().get(depotId) != null) {
			result = dataStub.getDepot2product2dependencyInfos().get(depotId);
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

	public Set<String> getMessagebusConnectedClients() {

		// no messagebus available if not at least opsi 4.3
		if (!ServerFacade.isOpsi43()) {
			return new HashSet<>();
		}

		Logging.info(this, "get clients connected with messagebus");
		OpsiMethodCall omc = new OpsiMethodCall("host_getMessagebusConnectedIds", new Object[] {});

		return new HashSet<>(exec.getStringListResult(omc));
	}

	public Boolean hasClientSpecificProperties(String productname) {
		return productHavingClientSpecificProperties.get(productname);
	}

	public List<Map<String, Object>> retrieveListOfMapsNOM(String methodName) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		return retrieveListOfMapsNOM(callAttributes, callFilter, methodName);
	}

	public List<Map<String, Object>> retrieveListOfMapsNOM(String[] callAttributes, Map<?, ?> callFilter,
			String methodName) {
		List<Map<String, Object>> retrieved = exec
				.getListOfMaps(new OpsiMethodCall(methodName, new Object[] { callAttributes, callFilter }));

		Logging.debug(this, "retrieveListOfMapsNOM: " + retrieved);
		return retrieved;
	}

	public List<Map<String, Object>> retrieveListOfMapsNOM(String methodName, Object[] data) {
		List<Map<String, Object>> retrieved = exec.getListOfMaps(new OpsiMethodCall(methodName, data));
		Logging.debug(this, "retrieveListOfMapsNOM: " + retrieved);
		return retrieved;
	}

	public Map<String, Object> retrieveMapNOM(String methodName, Object[] data) {
		Map<String, Object> retrieved = exec.getMapResult(new OpsiMethodCall(methodName, data));
		Logging.debug(this, "retrieveMapNOM: " + retrieved);
		return retrieved;
	}

	/**
	 * Collects the common property values of some product for a client
	 * collection,<br \> needed for local imaging handling <br \>
	 *
	 * @param List<String> clients -
	 * @param String       product
	 * @param String       property
	 */
	public List<String> getCommonProductPropertyValues(List<String> clients, String product, String property) {
		Logging.info(this, "getCommonProductPropertyValues for product, property, clients " + product + ", " + property
				+ "  -- " + clients);
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("objectId", clients);
		callFilter.put("productId", product);
		callFilter.put("propertyId", property);
		List<Map<String, Object>> properties = retrieveListOfMapsNOM(callAttributes, callFilter,
				"productPropertyState_getObjects");

		Set<String> resultSet = new HashSet<>();

		boolean starting = true;

		for (Map<String, Object> map : properties) {
			Object retrievedValues = ((JSONArray) map.get("values")).toList();

			List<?> valueList = (List<?>) retrievedValues;

			Set<String> values = new HashSet<>();

			for (int i = 0; i < valueList.size(); i++) {
				values.add((String) valueList.get(i));
			}

			if (starting) {
				resultSet = values;
				starting = false;
			} else {
				resultSet.retainAll(values);
			}
		}

		Logging.info(this, "getCommonProductPropertyValues " + resultSet);

		return new ArrayList<>(resultSet);
	}

	/**
	 * This method collects properties for all selected clients and all
	 * products,<br \> as a sideeffect, it produces the depot specific default
	 * values <br \>
	 *
	 * @param clientNames -
	 */
	public void retrieveProductProperties(List<String> clientNames) {
		retrieveProductProperties(new HashSet<>(clientNames));
	}

	public Map<String, Map<String, ConfigName2ConfigValue>> getDepot2product2properties() {
		retrieveDepotProductProperties();
		return depot2product2properties;
	}

	public Map<String, ConfigName2ConfigValue> getDefaultProductProperties(String depotId) {
		Logging.debug(this, "getDefaultProductProperties for depot " + depotId);
		retrieveDepotProductProperties();
		if (depot2product2properties == null) {
			Logging.error("no product properties ");
			return new HashMap<>();
		} else {
			if (depot2product2properties.get(depotId) == null) {
				// initializing state
				return new HashMap<>();
			}

			if (!depot2product2properties.get(depotId).isEmpty()) {
				Logging.info(this, "getDefaultProductProperties for depotId " + depotId + " starts with "
						+ new ArrayList<>(depot2product2properties.get(depotId).keySet()).get(0));
			}

			return depot2product2properties.get(depotId);
		}
	}

	public void retrieveDepotProductProperties() {
		if (depot2product2properties != null) {
			return;
		}

		Logging.info(this, "retrieveDepotProductProperties, build depot2product2properties");

		depot2product2properties = new HashMap<>();

		// depot missing ??

		List<Map<String, Object>> retrieved = dataStub
				.getProductPropertyDepotStates(hostInfoCollections.getDepots().keySet());

		for (Map<String, Object> map : retrieved) {
			String host = (String) map.get("objectId");

			if (!hostInfoCollections.getDepots().keySet().contains(host)) {
				Logging.warning(this, "should be a productPropertyState for a depot, but host " + host);
				continue;
			}

			Map<String, ConfigName2ConfigValue> productproperties1Host = depot2product2properties.computeIfAbsent(host,
					arg -> new HashMap<>());

			ConfigName2ConfigValue properties = productproperties1Host.computeIfAbsent(
					(String) map.get(OpsiPackage.DB_KEY_PRODUCT_ID),
					arg -> new ConfigName2ConfigValue(new HashMap<>()));

			properties.put((String) map.get("propertyId"), new JSONArray((List<?>) map.get("values")).toList());
			properties.getRetrieved().put((String) map.get("propertyId"),
					new JSONArray((List<?>) map.get("values")).toList());

			Logging.debug(this,
					"retrieveDepotProductProperties product properties " + map.get(OpsiPackage.DB_KEY_PRODUCT_ID));
		}

	}

	/**
	 * This method collects properties for all selected clients and all
	 * products,<br \> as a sideeffect, it produces the depot specific default
	 * values <br \>
	 *
	 * @param clientNames -
	 */
	private void retrieveProductProperties(final Set<String> clientNames) {

		boolean existing = true;

		if (productProperties == null) {
			existing = false;
		} else {
			for (String client : clientNames) {
				if (productProperties.get(client) == null) {
					existing = false;
					break;
				}
			}
		}

		if (existing) {
			return;
		}

		productProperties = new HashMap<>();
		Map<String, Map<String, Map<String, Object>>> productPropertiesRetrieved = new HashMap<>();

		dataStub.fillProductPropertyStates(clientNames);
		List<Map<String, Object>> retrieved = dataStub.getProductPropertyStates();

		Set<String> productsWithProductPropertyStates = new HashSet<>();

		for (Map<String, Object> map : retrieved) {
			String host = (String) map.get("objectId");

			productsWithProductPropertyStates.add((String) map.get("productId"));

			Map<String, Map<String, Object>> productproperties1Client = productPropertiesRetrieved.computeIfAbsent(host,
					s -> new HashMap<>());

			Map<String, Object> properties = productproperties1Client.computeIfAbsent((String) map.get("productId"),
					s -> new HashMap<>());

			properties.put((String) map.get("propertyId"),
					POJOReMapper.remap(map.get("values"), new TypeReference<List<Object>>() {
					}));
		}

		Logging.info(this,
				" retrieveProductproperties  productsWithProductPropertyStates " + productsWithProductPropertyStates);

		Map<String, ConfigName2ConfigValue> defaultProperties = getDefaultProductProperties(theDepot);
		Map<String, Map<String, Object>> defaultPropertiesRetrieved = new HashMap<>();
		if (!defaultProperties.isEmpty()) {
			for (Entry<String, ConfigName2ConfigValue> defaultProperty : defaultProperties.entrySet()) {
				defaultPropertiesRetrieved.put(defaultProperty.getKey(), defaultProperty.getValue());
			}
		}

		Set<String> products = defaultPropertiesRetrieved.keySet();

		Set<String> productsHavingSpecificProperties = new TreeSet<>(products);

		for (String host : clientNames) {
			HashMap<String, ConfigName2ConfigValue> productproperties1Client = new HashMap<>();
			productProperties.put(host, productproperties1Client);

			Map<String, Map<String, Object>> retrievedProperties = productPropertiesRetrieved.get(host);
			if (retrievedProperties == null) {
				retrievedProperties = defaultPropertiesRetrieved;
				productsHavingSpecificProperties.clear();
			}

			for (String product : products) {
				Map<String, Object> retrievedProperties1Product = retrievedProperties.get(product);
				// complete set of default values
				Map<String, Object> properties1Product = new HashMap<>(defaultPropertiesRetrieved.get(product));

				if (retrievedProperties1Product == null) {
					productsHavingSpecificProperties.remove(product);
				} else {
					for (Entry<String, Object> retrievedProperty : retrievedProperties1Product.entrySet()) {
						properties1Product.put(retrievedProperty.getKey(), retrievedProperty.getValue());
					}
				}

				ConfigName2ConfigValue state = new ConfigName2ConfigValue(properties1Product, null);
				productproperties1Client.put(product, state);
			}
		}

		Logging.info(this,
				" retrieveProductproperties productsHavingSpecificProperties " + productsHavingSpecificProperties);

		Map<String, ConfigName2ConfigValue> depotValues = getDefaultProductProperties(theDepot);

		productHavingClientSpecificProperties = new HashMap<>();

		for (String product : products) {
			if (productPropertyDefinitions != null && productPropertyDefinitions.get(product) != null) {
				ConfigName2ConfigValue productPropertyConfig = depotValues.get(product);

				Iterator<String> iterProperties = productPropertyDefinitions.get(product).keySet().iterator();
				while (iterProperties.hasNext()) {
					String property = iterProperties.next();

					if (productPropertyConfig.isEmpty() || productPropertyConfig.get(property) == null) {
						productPropertyDefinitions.get(product).get(property).setDefaultValues(new ArrayList<>());
					} else {
						productPropertyDefinitions.get(product).get(property)
								.setDefaultValues((List) productPropertyConfig.get(property));
					}
				}
			}

			productHavingClientSpecificProperties.put(product, productsHavingSpecificProperties.contains(product));
		}
	}

	/**
	 * @param pcname      - if it changes productproperties should have been set
	 *                    to null.
	 * @param productname
	 */
	public Map<String, Object> getProductProperties(String pcname, String productname) {
		Logging.debug(this, "getProductProperties for product, host " + productname + ", " + pcname);

		Set<String> pcs = new TreeSet<>();
		pcs.add(pcname);
		retrieveProductProperties(pcs);

		if (productProperties.get(pcname) == null || productProperties.get(pcname).get(productname) == null) {
			return new HashMap<>();
		}

		return productProperties.get(pcname).get(productname);
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

		if (updateCollection != null && !updateCollection.isEmpty() && exec
				.doCall(new OpsiMethodCall("productPropertyState_updateObjects", new Object[] { updateCollection }))) {
			updateCollection.clear();
		}

		if (deleteCollection != null && !deleteCollection.isEmpty() && exec
				.doCall(new OpsiMethodCall("productPropertyState_deleteObjects", new Object[] { deleteCollection }))) {
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

		if (dataStub.getDepot2Product2PropertyDefinitions().get(depotId) == null) {
			result = new HashMap<>();
			Logging.info("getProductPropertyOptionsMap: no productproperty definitions for depot " + depotId);
		} else {
			result = dataStub.getDepot2Product2PropertyDefinitions().get(depotId).get(productId);
		}

		if (result == null) {
			Logging.info("getProductPropertyOptionsMap: no productproperty definitions  for depot, product " + depotId
					+ ", " + productId);
			result = new HashMap<>();
		}

		return result;
	}

	public Map<String, ListCellOptions> getProductPropertyOptionsMap(String productId) {
		retrieveProductPropertyDefinitions();
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
		dataStub.productPropertyDefinitionsRequestRefresh();
		productPropertyDefinitions = null;
	}

	public void retrieveProductPropertyDefinitions() {
		productPropertyDefinitions = dataStub.getDepot2Product2PropertyDefinitions().get(theDepot);
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

				if ((requirementType.equals(NAME_REQUIREMENT_TYPE_NEUTRAL)
						|| requirementType.equals(NAME_REQUIREMENT_TYPE_BEFORE)
						|| requirementType.equals(NAME_REQUIREMENT_TYPE_AFTER))
						&& ((aDependency.get("action")).equals(ActionRequest.getLabel(ActionRequest.SETUP))
								|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.ONCE))
								|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.ALWAYS))
								|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.CUSTOM)))
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
		dataStub.productPropertyStatesRequestRefresh();
		productProperties = null;
	}

	// lazy initializing
	private List<String> getMethodSignature(String methodname) {
		if (mapOfMethodSignatures == null) {
			List<Object> methodsList = exec.getListResult(new OpsiMethodCall("backend_getInterface", new Object[] {}));

			if (!methodsList.isEmpty()) {
				mapOfMethodSignatures = new HashMap<>();

				Iterator<Object> iter = methodsList.iterator();
				while (iter.hasNext()) {
					Map<String, Object> listEntry = exec.getMapFromItem(iter.next());

					String name = (String) listEntry.get("name");
					List<String> signature = new ArrayList<>();

					// should never result
					List<Object> signature1 = exec.getListFromItem(listEntry.get("params").toString());

					// to null
					for (int i = 0; i < signature1.size(); i++) {
						String element = (String) signature1.get(i);

						if (element != null && element.length() > 0 && element.charAt(0) == '*') {
							signature.add(element.substring(1));
						} else {
							signature.add(element);
						}

						Logging.debug(this, "mapOfMethodSignatures  " + i + ":: " + name + ": " + signature);
					}
					mapOfMethodSignatures.put(name, signature);
				}
			}
		}

		Logging.debug(this, "mapOfMethodSignatures " + mapOfMethodSignatures);

		if (mapOfMethodSignatures.get(methodname) == null) {
			return NONE_LIST;
		}

		return mapOfMethodSignatures.get(methodname);
	}

	// Will not be called in opsi 4.3 (or later) because we don't need backend-infos any more
	public String getBackendInfos() {
		String bgColor0 = "#dedeff";
		String bgColor1 = "#ffffff";
		String bgColor = "";

		String titleSize = "14px";
		String fontSizeBig = "10px";
		String fontSizeSmall = "8px";
		// are not evaluated at this moment

		OpsiMethodCall omc = new OpsiMethodCall("getBackendInfos_listOfHashes", new String[] {});

		List<Object> list = exec.getListResult(omc);

		StringBuilder buf = new StringBuilder("");

		Map<String, List<Map<String, Object>>> backends = new HashMap<>();

		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> listEntry = exec.getMapFromItem(list.get(i));

			String backendName = "UNKNOWN";

			if (listEntry.containsKey("name")) {
				backendName = (String) listEntry.get("name");
			}

			if (!backends.containsKey(backendName)) {
				backends.put(backendName, new ArrayList<>());
			}

			backends.get(backendName).add(listEntry);
		}

		buf.append("<table border='0' cellspacing='0' cellpadding='0'>\n");

		Iterator<String> backendIterator = backends.keySet().iterator();
		while (backendIterator.hasNext()) {
			String backendName = backendIterator.next();

			buf.append("<tr><td bgcolor='#fbeca5' color='#000000'  width='100%'  colspan='3'  align='left'>");
			buf.append("<font size='" + titleSize + "'><b>" + backendName + "</b></font></td></tr>");

			List<Map<String, Object>> backendEntries = backends.get(backendName);

			for (int i = 0; i < backendEntries.size(); i++) {
				Map<String, Object> listEntry = backendEntries.get(i);

				Iterator<String> eIt = listEntry.keySet().iterator();

				boolean entryIsEven = false;

				while (eIt.hasNext()) {
					String key = eIt.next();
					if ("name".equals(key)) {
						continue;
					}

					entryIsEven = !entryIsEven;
					if (entryIsEven) {
						bgColor = bgColor0;
					} else {
						bgColor = bgColor1;
					}

					Object value = listEntry.get(key);
					buf.append("<tr height='8px'>");
					buf.append("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
							+ fontSizeBig + "'>" + key + "</font></td>");

					if ("config".equals(key)) {
						buf.append("<td colspan='2'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
								+ fontSizeBig + "'>&nbsp;</font></td>");
						buf.append("</tr>");

						Map<String, Object> configItems = exec.getMapFromItem(value);

						if (!configItems.isEmpty()) {
							Iterator<String> configItemsIterator = configItems.keySet().iterator();

							while (configItemsIterator.hasNext()) {
								String configKey = configItemsIterator.next();

								Object jO = configItems.get(configKey);

								String configVal = "";

								configVal = jO.toString();

								buf.append("<td bgcolor='" + bgColor + "'>&nbsp;</td>");
								buf.append("<td width='200px'  bgcolor='" + bgColor
										+ "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configKey
										+ "</font></td>");
								buf.append("<td width='200px'  bgcolor='" + bgColor
										+ "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configVal
										+ "</font></td>");
								buf.append("</tr>");
							}
						}
					} else {
						buf.append("<td width='300px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
								+ fontSizeBig + "'>" + value + "</font></td>");
						buf.append("</tr>");
					}
				}
				buf.append("<tr height='10px'><td bgcolor='" + bgColor + "' colspan='3'></td></tr>");
			}

			buf.append(
					"<tr><td bgcolor='#ffffff' color='#000000' width='100%' height='30px' colspan='3'>&nbsp;</td></tr>");
		}

		buf.append("</table>\n");

		return buf.toString();
	}

	public Map<String, ListCellOptions> getConfigOptions() {
		getHwAuditDeviceClasses();

		if (configListCellOptions == null || configOptions == null || configDefaultValues == null) {
			Logging.debug(this, "getConfigOptions() work");

			List<Map<String, Object>> deleteItems = new ArrayList<>();

			boolean tryIt = true;

			int tryOnceMoreCounter = 0;
			final int STOP_REPEATING_AT_THIS = 1;

			while (tryIt) {
				tryIt = false;
				tryOnceMoreCounter++;

				configOptions = new HashMap<>();
				configListCellOptions = new HashMap<>();
				configDefaultValues = new HashMap<>();

				remoteControls = new RemoteControls();
				savedSearches = new SavedSearches();

				OpsiHwAuditDevicePropertyTypes hwAuditDevicePropertyTypes = new OpsiHwAuditDevicePropertyTypes(
						hwAuditDeviceClasses);

				// metaConfig for wan configuration is rebuilt in
				// getWANConfigOptions

				List<Map<String, Object>> retrievedList = retrieveListOfMapsNOM("config_getObjects");

				Logging.info(this, "configOptions retrieved ");

				for (Map<String, Object> configItem : retrievedList) {
					// map to java type
					for (Entry<String, Object> configItemEntry : configItem.entrySet()) {
						if (configItemEntry.getValue() instanceof JSONArray) {
							configItem.put(configItemEntry.getKey(), ((JSONArray) configItemEntry.getValue()).toList());
						}
					}

					String key = (String) configItem.get("ident");

					// build a ConfigOption from the retrieved item

					// eliminate key produced by old version for role branch

					String pseudouserProducedByOldVersion = KEY_USER_ROOT + ".{"
							+ UserConfig.ROLE.substring(1, UserConfig.ROLE.length());
					//

					if (key != null && key.startsWith(pseudouserProducedByOldVersion)) {
						Logging.warning(this, "user entry " + key
								+ " produced by a still somewhere running old configed version , please delete user entry "
								+ pseudouserProducedByOldVersion);

						deleteItems.add(configItem);

						Logging.info(this, "deleteItem " + configItem);

						continue;
					}

					ConfigOption configOption = new ConfigOption(configItem);

					configOptions.put(key, configOption);

					configListCellOptions.put(key, configOption);

					if (configOption.getDefaultValues() == null) {
						Logging.warning(this, "default values missing for config  " + key);

						if (tryOnceMoreCounter <= STOP_REPEATING_AT_THIS) {
							tryIt = true;
							Logging.warning(this,
									"repeat loading the values , we repeated  " + tryOnceMoreCounter + " times");

							Globals.threadSleep(this, 1000);
							break;
						}
					}

					configDefaultValues.put(key, configOption.getDefaultValues());

					if (configOption.getDefaultValues() != null && !configOption.getDefaultValues().isEmpty()) {
						remoteControls.checkIn(key, "" + configOption.getDefaultValues().get(0));
						savedSearches.checkIn(key, "" + configOption.getDefaultValues().get(0));
						hwAuditDevicePropertyTypes.checkIn(key, configOption.getDefaultValues());
					}
				}

				Logging.debug(this,
						" getConfigOptions produced hwAuditDevicePropertyTypes " + hwAuditDevicePropertyTypes);
			}

			Logging.info(this, "{ole deleteItems " + deleteItems.size());

			if (!deleteItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall("config_deleteObjects", new Object[] { deleteItems.toArray() });

				if (exec.doCall(omc)) {
					deleteItems.clear();
				}
			}

			getWANConfigOptions();
			Logging.debug(this, "getConfigOptions() work finished");
		}

		return configListCellOptions;
	}

	public Map<String, RemoteControl> getRemoteControls() {
		getConfigOptions();
		return remoteControls;
	}

	public SavedSearches getSavedSearches() {
		getConfigOptions();
		return savedSearches;
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

		OpsiMethodCall omc = new OpsiMethodCall("configState_updateObjects", new Object[] { jsonObjects });

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
		Object obj = getConfigOptions().get(key);

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

	public Map<String, List<Object>> getConfigDefaultValues() {
		getConfigOptions();
		return configDefaultValues;
	}

	public void configOptionsRequestRefresh() {
		Logging.info(this, "configOptionsRequestRefresh");
		configOptions = null;
	}

	public void hostConfigsRequestRefresh() {
		dataStub.hostConfigsRequestRefresh();
	}

	public Map<String, Map<String, Object>> getConfigs() {
		return dataStub.getConfigs();
	}

	public Map<String, Object> getConfig(String objectId) {
		getConfigOptions();

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

		exec.doCall(new OpsiMethodCall("host_createObjects", new Object[] { hostMaps }));
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

		if (Globals.checkCollection(this, "configStateCollection", configStateCollection)
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
				OpsiMethodCall omc = new OpsiMethodCall("configState_deleteObjects",
						new Object[] { deleteConfigStateItems.toArray() });

				if (exec.doCall(omc)) {
					deleteConfigStateItems.clear();
					configStateCollection.removeAll(doneList);
				}
			}

			List<Object> existingConfigIds = exec
					.getListResult(new OpsiMethodCall("config_getIdents", new Object[] {}));
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
				OpsiMethodCall omc = new OpsiMethodCall("config_createObjects", new Object[] { createItems.toArray() });
				exec.doCall(omc);
				configsChanged = true;
			}

			if (configsChanged) {
				configOptionsRequestRefresh();
				getConfigOptions();
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
				exec.doCall(new OpsiMethodCall("config_updateObjects", new Object[] { callsConfigCollection }));
			}

			// do call

			// now we can set the values and clear the collected update items
			exec.doCall(new OpsiMethodCall("configState_updateObjects",
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
					.getListResult(new OpsiMethodCall("config_getIdents", new Object[] {}));

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
				OpsiMethodCall omc = new OpsiMethodCall("config_createObjects", new Object[] { createItems.toArray() });
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
				exec.doCall(new OpsiMethodCall("config_deleteObjects", new Object[] { callsConfigDeleteCollection }));
				configOptionsRequestRefresh();
				// because of referential integrity
				hostConfigsRequestRefresh();
			}

			Logging.debug(this, "setConfig() callsConfigUpdateCollection " + callsConfigUpdateCollection);

			if (!callsConfigUpdateCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall("config_updateObjects", new Object[] { callsConfigUpdateCollection }));
				configOptionsRequestRefresh();
			}

			getConfigOptions();
			configCollection.clear();

			Logging.info(this, "setConfig(),  configCollection result: " + configCollection);
		}
	}

	/**
	 * delivers the default domain if it is not existing it retrieves it from
	 * servide
	 */
	public String getOpsiDefaultDomain() {
		retrieveOpsiDefaultDomain();
		return opsiDefaultDomain;
	}

	/**
	 * signals that the default domain shall be reloaded from service
	 */
	public void requestReloadOpsiDefaultDomain() {
		opsiDefaultDomain = null;
	}

	/**
	 * retrieves default domain from service
	 */
	private void retrieveOpsiDefaultDomain() {
		if (opsiDefaultDomain == null) {
			Object[] params = new Object[] {};
			opsiDefaultDomain = exec.getStringResult(new OpsiMethodCall("getDomain", params));
		}
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

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { readyObjects });

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

	public Map<String, SWAuditEntry> getInstalledSoftwareInformation() {

		Logging.info(this, "getInstalledSoftwareInformation");

		return dataStub.getInstalledSoftwareInformation();
	}

	public NavigableMap<String, Set<String>> getName2SWIdents() {
		return dataStub.getName2SWIdents();
	}

	public Map<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing() {

		return dataStub.getInstalledSoftwareInformationForLicensing();
	}

	// only software relevant of the items for licensing
	public NavigableMap<String, Map<String, String>> getInstalledSoftwareName2SWinfo() {
		return dataStub.getInstalledSoftwareName2SWinfo();
	}

	public void installedSoftwareInformationRequestRefresh() {
		Logging.info(this, " call installedSoftwareInformationRequestRefresh()");
		dataStub.installedSoftwareInformationRequestRefresh();
	}

	public String getSWident(Integer i) {
		return dataStub.getSWident(i);
	}

	public List<String> getSoftwareList() {
		return dataStub.getSoftwareList();
	}

	public NavigableMap<String, Integer> getSoftware2Number() {
		return dataStub.getSoftware2Number();
	}

	// without internal caching
	public Map<String, LicenceContractEntry> getLicenceContracts() {
		dataStub.licenceContractsRequestRefresh();
		return dataStub.getLicenceContracts();
	}

	// date in sql time format, contrad ID
	public NavigableMap<String, NavigableSet<String>> getLicenceContractsExpired() {
		dataStub.licenceContractsRequestRefresh();
		return dataStub.getLicenceContractsToNotify();
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
			OpsiMethodCall omc = new OpsiMethodCall("licenseContract_create", new String[] { licenseContractId, "",
					notes, partner, conclusionDate, notificationDate, expirationDate });

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
			OpsiMethodCall omc = new OpsiMethodCall("licenseContract_delete", new String[] { licenseContractId });
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
			OpsiMethodCall omc = new OpsiMethodCall("licensePool_create", new String[] { licensePoolId, description });

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
			OpsiMethodCall omc = new OpsiMethodCall("licensePool_delete", new Object[] { licensePoolId });
			return exec.doCall(omc);
			// comes too late
		}

		return false;
	}

	// without internal caching
	public Map<String, LicenceEntry> getSoftwareLicences() {
		Map<String, LicenceEntry> softwareLicences = new HashMap<>();

		if (withLicenceManagement) {
			dataStub.licencesRequestRefresh();
			softwareLicences = dataStub.getLicences();
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
			String methodName = "softwareLicense_";

			switch (licenceType) {
			case LicenceEntry.VOLUME:
				methodName = methodName + "createVolume";
				break;
			case LicenceEntry.OEM:
				methodName = methodName + "createOEM";
				break;
			case LicenceEntry.CONCURRENT:
				methodName = methodName + "createConcurrent";
				break;
			case LicenceEntry.RETAIL:
				methodName = methodName + "createRetail";
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
			OpsiMethodCall omc = new OpsiMethodCall("softwareLicense_delete", new Object[] { softwareLicenseId });
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
			List<Map<String, Object>> softwareL2LPools = exec.getListOfMaps(new OpsiMethodCall(
					"softwareLicenseToLicensePool_getObjects", new Object[] { callAttributes, callFilter }));

			for (Map<String, Object> softwareL2LPool : softwareL2LPools) {
				softwareL2LPool.remove("ident");
				softwareL2LPool.remove("type");

				rowsSoftwareL2LPool
						.put(Globals.pseudokey(new String[] { (String) softwareL2LPool.get("softwareLicenseId"),
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
			OpsiMethodCall omc = new OpsiMethodCall("softwareLicenseToLicensePool_create",
					new String[] { softwareLicenseId, licensePoolId, licenseKey });

			if (!exec.doCall(omc)) {
				Logging.error(this, "cannot create softwarelicense to licensepool relation");
				return "";
			}
		}

		return Globals.pseudokey(new String[] { softwareLicenseId, licensePoolId });
	}

	public boolean deleteRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId) {
		if (!serverFullPermission) {
			return false;
		}

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("softwareLicenseFromLicensePool_delete",
					new String[] { softwareLicenseId, licensePoolId });

			return exec.doCall(omc);
		}

		return false;
	}

	// without internal caching
	public Map<String, Map<String, String>> getRelationsProductId2LPool() {
		HashMap<String, Map<String, String>> rowsLicencePoolXOpsiProduct = new HashMap<>();

		if (withLicenceManagement) {
			dataStub.licencePoolXOpsiProductRequestRefresh();
			dataStub.getLicencePoolXOpsiProduct();
			Logging.info(this, "licencePoolXOpsiProduct size " + dataStub.getLicencePoolXOpsiProduct().size());

			for (StringValuedRelationElement element : dataStub.getLicencePoolXOpsiProduct()) {
				rowsLicencePoolXOpsiProduct
						.put(Globals.pseudokey(new String[] { element.get(LicencePoolXOpsiProduct.LICENCE_POOL_KEY),
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

			if (exec.doCall(new OpsiMethodCall("licensePool_updateObject", new Object[] { licensePool }))) {
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

			return exec.doCall(new OpsiMethodCall("licensePool_updateObject", new Object[] { licensePool }));
		}

		return false;
	}

	private Map<String, Object> getLicensePool(String licensePoolId) {
		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("id", licensePoolId);
		return exec
				.getListOfMaps(
						new OpsiMethodCall("licensePool_getObjects", new Object[] { callAttributes, callFilter }))
				.get(0);
	}

	public void retrieveRelationsAuditSoftwareToLicencePools() {
		Logging.info(this,
				"retrieveRelationsAuditSoftwareToLicencePools start " + (relationsAuditSoftwareToLicencePools != null));

		if (relationsAuditSoftwareToLicencePools == null) {
			dataStub.auditSoftwareXLicencePoolRequestRefresh();
		} else {
			return;
		}

		relationsAuditSoftwareToLicencePools = dataStub.getAuditSoftwareXLicencePool();

		// function softwareIdent --> pool
		fSoftware2LicencePool = new HashMap<>();
		// function pool --> list of assigned software
		fLicencePool2SoftwareList = new HashMap<>();
		// function pool --> list of assigned software
		fLicencePool2UnknownSoftwareList = new HashMap<>();

		softwareWithoutAssociatedLicencePool = new TreeSet<>(getInstalledSoftwareInformationForLicensing().keySet());

		if (!withLicenceManagement) {
			return;
		}

		for (StringValuedRelationElement retrieved : relationsAuditSoftwareToLicencePools) {
			SWAuditEntry entry = new SWAuditEntry(retrieved);
			String licencePoolKEY = retrieved.get(LicencepoolEntry.ID_SERVICE_KEY);
			String swKEY = entry.getIdent();

			// build row for software table
			LinkedHashMap<String, String> row = new LinkedHashMap<>();

			for (String colName : SWAuditEntry.getDisplayKeys()) {
				row.put(colName, entry.get(colName));

			}

			// build fSoftware2LicencePool
			if (fSoftware2LicencePool.get(swKEY) != null && !fSoftware2LicencePool.get(swKEY).equals(licencePoolKEY)) {
				Logging.error("software with ident \"" + swKEY + "\" has assigned license pool "
						+ fSoftware2LicencePool.get(swKEY) + " as well as " + licencePoolKEY);
			}
			fSoftware2LicencePool.put(swKEY, licencePoolKEY);

			// build fLicencePool2SoftwareList
			if (fLicencePool2SoftwareList.get(licencePoolKEY) == null) {
				fLicencePool2SoftwareList.put(licencePoolKEY, new ArrayList<>());
			}

			List<String> softwareIds = fLicencePool2SoftwareList.get(licencePoolKEY);
			if (softwareIds.indexOf(swKEY) == -1) {
				if (getInstalledSoftwareInformationForLicensing().get(swKEY) == null) {
					Logging.warning(this, "license pool " + licencePoolKEY
							+ " is assigned to a not listed software with ID " + swKEY + " data row " + row);
					// we serve the fLicencePool2UnknownSoftwareList only in case that a key is
					// found
					List<String> unknownSoftwareIds = fLicencePool2UnknownSoftwareList.computeIfAbsent(licencePoolKEY,
							s -> new ArrayList<>());
					unknownSoftwareIds.add(swKEY);
				} else {
					softwareIds.add(swKEY);
					softwareWithoutAssociatedLicencePool.remove(swKEY);
				}
			}
		}

		Logging.info(this, "retrieveRelationsAuditSoftwareToLicencePools,  softwareWithoutAssociatedLicencePool "
				+ softwareWithoutAssociatedLicencePool.size());
	}

	public NavigableSet<Object> getSoftwareWithoutAssociatedLicencePool() {
		if (softwareWithoutAssociatedLicencePool == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}

		return softwareWithoutAssociatedLicencePool;
	}

	public void relationsAuditSoftwareToLicencePoolsRequestRefresh() {
		relationsAuditSoftwareToLicencePools = null;
		softwareWithoutAssociatedLicencePool = null;
		fLicencePool2SoftwareList = null;
		fLicencePool2UnknownSoftwareList = null;
	}

	public List<String> getSoftwareListByLicencePool(String licencePoolId) {
		if (fLicencePool2SoftwareList == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}

		List<String> result = fLicencePool2SoftwareList.get(licencePoolId);
		if (result == null) {
			result = new ArrayList<>();
		}
		return result;
	}

	public List<String> getUnknownSoftwareListForLicencePool(String licencePoolId) {
		if (fLicencePool2UnknownSoftwareList == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}

		List<String> result = fLicencePool2UnknownSoftwareList.get(licencePoolId);
		if (result == null) {
			result = new ArrayList<>();
		}
		return result;
	}

	public Map<String, String> getFSoftware2LicencePool() {
		if (fSoftware2LicencePool == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}
		return fSoftware2LicencePool;
	}

	public String getFSoftware2LicencePool(String softwareIdent) {
		if (fSoftware2LicencePool == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}
		return fSoftware2LicencePool.get(softwareIdent);
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

			OpsiMethodCall omc = new OpsiMethodCall("auditSoftwareToLicensePool_deleteObjects",
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
			Map<String, SWAuditEntry> instSwI = getInstalledSoftwareInformationForLicensing();

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
					OpsiMethodCall omc = new OpsiMethodCall("auditSoftwareToLicensePool_deleteObjects",
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

			OpsiMethodCall omc = new OpsiMethodCall("auditSoftwareToLicensePool_createObjects",
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
					if (getName2SWIdents().get(swName) == null) {
						getName2SWIdents().put(swName, new TreeSet<>());
					}
					getName2SWIdents().get(swName).add(ident);

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

				OpsiMethodCall omc = new OpsiMethodCall("auditSoftwareToLicensePool_createObjects",
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
		getConfigOptions();

		return Globals.takeAsStringList(configDefaultValues.get(key));
	}

	public Map<String, LicencepoolEntry> getLicencepools() {
		dataStub.licencepoolsRequestRefresh();
		return dataStub.getLicencepools();
	}

	// poolId -> LicenceStatisticsRow
	private Map<String, LicenceStatisticsRow> produceLicenceStatistics() {
		// side effects of this method: rowsLicencesReconciliation
		Logging.info(this, "produceLicenceStatistics === ");

		Map<String, List<String>> licencePool2listOfUsingClientsSWInvent = new HashMap<>();

		Map<String, Set<String>> licencePool2setOfUsingClientsSWInvent = new HashMap<>();

		// result
		Map<String, Integer> licencePoolUsagecountSWInvent = new HashMap<>();

		// now we have audit software on client data for all clients
		fillClient2Software(getHostInfoCollections().getOpsiHostNames());
		AuditSoftwareXLicencePool auditSoftwareXLicencePool = dataStub.getAuditSoftwareXLicencePool();

		Map<String, Set<String>> swId2clients = dataStub.getSoftwareIdent2clients();

		if (withLicenceManagement) {
			Map<String, LicencepoolEntry> licencePools = dataStub.getLicencepools();

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
						String pseudokey = Globals.pseudokey(new String[] { clientEntry.getKey(), pool });
						rowsLicencesReconciliation.put(pseudokey, rowMap);
					}
				}
			}

			getInstalledSoftwareInformationForLicensing();

			retrieveRelationsAuditSoftwareToLicencePools();

			// idents
			for (String softwareIdent : getInstalledSoftwareInformationForLicensing().keySet()) {
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
						String pseudokey = Globals.pseudokey(new String[] { client, licencePoolId });

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
		Map<String, LicencepoolEntry> licencePools = dataStub.getLicencepools();

		// table SOFTWARE_LICENSE
		Logging.info(this, " licences ");

		// table SOFTWARE_LICENSE_TO_LICENSE_POOL
		Logging.info(this, " licence usabilities ");
		List<LicenceUsableForEntry> licenceUsabilities = dataStub.getLicenceUsabilities();

		// table LICENSE_ON_CLIENT
		Logging.info(this, " licence usages ");
		List<LicenceUsageEntry> licenceUsages = dataStub.getLicenceUsages();

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
				count = dataStub.getLicences().get(licenceId).getMaxInstallations();
				pool2allowedUsagesCount.put(pool, count);
			} else {
				ExtendedInteger result = count.add(dataStub.getLicences().get(licenceId).getMaxInstallations());
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
						String pseudokey = Globals.pseudokey(new String[] { client, licencePoolId });

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

	public Map<String, LicenceUsageEntry> getLicencesUsage() {
		retrieveLicencesUsage();
		return rowsLicencesUsage;
	}

	public Map<String, List<LicenceUsageEntry>> getFClient2LicencesUsageList() {
		retrieveLicencesUsage();
		return fClient2LicencesUsageList;
	}

	private void retrieveLicencesUsage() {
		Logging.info(this, "retrieveLicencesUsage with refresh " + (rowsLicencesUsage == null));

		if (rowsLicencesUsage == null) {
			dataStub.licenceUsagesRequestRefresh();
		} else {
			return;
		}

		if (!withLicenceManagement) {
			return;
		}

		rowsLicencesUsage = new HashMap<>();
		fClient2LicencesUsageList = new HashMap<>();

		for (LicenceUsageEntry m : dataStub.getLicenceUsages()) {
			rowsLicencesUsage.put(m.getPseudoKey(), m);

			List<LicenceUsageEntry> licencesUsagesForClient = fClient2LicencesUsageList.computeIfAbsent(m.getClientId(),
					s -> new ArrayList<>());
			licencesUsagesForClient.add(m);
		}
	}

	// retrieves the used software licence - or tries to reserve one - for the given
	// host and licence pool
	public String getLicenceUsage(String hostId, String licensePoolId) {
		String result = null;
		Map<String, Object> resultMap = null;

		if (withLicenceManagement) {
			OpsiMethodCall omc0 = new OpsiMethodCall("licenseOnClient_getOrCreateObject",
					new String[] { hostId, licensePoolId });

			resultMap = exec.getMapResult(omc0);

			if (!resultMap.isEmpty()) {
				result = Globals.pseudokey(new String[] { "" + resultMap.get(HOST_KEY),
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
			OpsiMethodCall omc = new OpsiMethodCall("licenseOnClient_create",
					new String[] { softwareLicenseId, licensePoolId, hostId, licenseKey, notes });

			resultMap = exec.getMapResult(omc);

			if (!resultMap.isEmpty()) {
				result = Globals.pseudokey(new String[] { "" + resultMap.get(HOST_KEY),
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

		OpsiMethodCall omc = new OpsiMethodCall("licenseOnClient_deleteObjects", new Object[] { jsonPreparedList });

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
			OpsiMethodCall omc = new OpsiMethodCall("licenseOnClient_delete",
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
		dataStub.installedSoftwareInformationRequestRefresh();

		relationsAuditSoftwareToLicencePools = null;

		dataStub.softwareAuditOnClientsRequestRefresh();
		dataStub.licencepoolsRequestRefresh();
		dataStub.licencesRequestRefresh();
		dataStub.licenceUsabilitiesRequestRefresh();
		dataStub.licenceUsagesRequestRefresh();
		hostInfoCollections.opsiHostsRequestRefresh();
	}

	public Map<String, Map<String, Object>> getLicencesReconciliation() {
		getLicenceStatistics();
		return rowsLicencesReconciliation;
	}

	@SuppressWarnings("java:S1168")
	private List<String> produceProductOnClientDisplayfieldsLocalboot() {
		if (globalReadOnly) {
			return null;
		}

		List<String> result = getDefaultValuesProductOnClientDisplayFields();

		List<String> possibleValues = getPossibleValuesProductOnClientDisplayFields();

		// create config for service
		Map<String, Object> item = createNOMitem("UnicodeConfig");
		item.put("ident", KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT);
		item.put("description", "");
		item.put("defaultValues", result);
		item.put("possibleValues", possibleValues);
		item.put("editable", false);
		item.put("multiValue", true);

		Logging.info(this, "produceProductOnClientDisplayfields_localboot");

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { item });

		exec.doCall(omc);

		return result;
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

		Map<String, Object> itemRole = OpsiserviceNOMPersistenceController.createJSONConfig(
				ConfigOption.TYPE.UNICODE_CONFIG, configkey, "which role should determine this configuration", false,
				false, selectedValuesRole, selectedValuesRole);

		readyObjects.add(itemRole);

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { readyObjects });

		exec.doCall(omc);

		configDefaultValues.put(configkey, selectedValuesRole);
	}

	public void userConfigurationRequestReload() {
		Logging.info(this, "userConfigurationRequestReload");
		keyUserRegisterValue = null;
	}

	private final boolean isUserRegisterActivated() {
		boolean result = false;

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();
		// dont do anything if we have not got the config
		if (serverPropertyMap.get(KEY_USER_REGISTER) != null && !serverPropertyMap.get(KEY_USER_REGISTER).isEmpty()) {
			result = (Boolean) ((List<?>) serverPropertyMap.get(KEY_USER_REGISTER)).get(0);
		}
		return result;
	}

	private final boolean checkUserRolesModule() {
		if (Boolean.TRUE.equals(keyUserRegisterValue) && !withUserRoles) {
			keyUserRegisterValue = false;

			SwingUtilities.invokeLater(this::callOpsiLicenceMissingText);
		}

		return keyUserRegisterValue;
	}

	private void callOpsiLicenceMissingText() {
		StringBuilder info = new StringBuilder();
		info.append(Configed.getResourceValue("Permission.modules.missing_user_roles") + "\n");
		info.append(Configed.getResourceValue("Permission.modules.missing_user_roles.1") + "\n");
		info.append(Configed.getResourceValue("Permission.modules.missing_user_roles.2") + "\n");
		info.append(KEY_USER_REGISTER + " " + Configed.getResourceValue("Permission.modules.missing_user_roles.3"));
		info.append("\n");

		Logging.warning(this, " user role administration configured but not permitted by the modules file " + info);

		FOpsiLicenseMissingText.callInstanceWith(info.toString());
	}

	public Map<String, Boolean> getProductOnClientsDisplayFieldsLocalbootProducts() {
		if (productOnClientsDisplayFieldsLocalbootProducts == null) {
			Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

			Logging.debug(this,
					"getProductOnClients_displayFieldsLocalbootProducts()  configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT "
							+ configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT));

			List<String> configuredByService = Globals
					.takeAsStringList(serverPropertyMap.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT));

			List<?> possibleValuesAccordingToService = new ArrayList<>();

			if (configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT) != null) {
				possibleValuesAccordingToService = (List<?>) configOptions
						.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT).get("possibleValues");
			}

			Logging.debug(this, "getProductOnClients_displayFieldsLocalbootProducts() possibleValuesAccordingToService "
					+ possibleValuesAccordingToService);

			if (configuredByService.isEmpty() || !((new HashSet<>(getPossibleValuesProductOnClientDisplayFields()))
					.equals(new HashSet<>(possibleValuesAccordingToService)))) {
				// we did not initialize server property
				configuredByService = produceProductOnClientDisplayfieldsLocalboot();
			}

			productOnClientsDisplayFieldsLocalbootProducts = new LinkedHashMap<>();

			if (configuredByService == null) {
				Logging.warning(this, "configuredByService is null");
				return productOnClientsDisplayFieldsLocalbootProducts;
			}

			// key names from ProductState
			productOnClientsDisplayFieldsLocalbootProducts.put("productId", true);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_PRODUCT_NAME,
					configuredByService.indexOf(ProductState.KEY_PRODUCT_NAME) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_TARGET_CONFIGURATION,
					configuredByService.indexOf(ProductState.KEY_TARGET_CONFIGURATION) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_INSTALLATION_STATUS, true);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_INSTALLATION_INFO,
					configuredByService.indexOf(ProductState.KEY_INSTALLATION_INFO) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_ACTION_REQUEST, true);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_PRODUCT_PRIORITY,
					configuredByService.indexOf(ProductState.KEY_PRODUCT_PRIORITY) > -1);
			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_POSITION,
					configuredByService.indexOf(ProductState.KEY_POSITION) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_LAST_STATE_CHANGE,
					configuredByService.indexOf(ProductState.KEY_LAST_STATE_CHANGE) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_VERSION_INFO, true);

		}

		return productOnClientsDisplayFieldsLocalbootProducts;
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

		OpsiMethodCall omc = new OpsiMethodCall("config_deleteObjects", new Object[] { readyObjects });

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

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { readyObjects });

		exec.doCall(omc);
	}

	private static List<String> getPossibleValuesProductOnClientDisplayFields() {
		List<String> possibleValues = new ArrayList<>();
		possibleValues.add("productId");
		possibleValues.add(ProductState.KEY_PRODUCT_NAME);
		possibleValues.add(ProductState.KEY_INSTALLATION_STATUS);
		possibleValues.add(ProductState.KEY_INSTALLATION_INFO);
		possibleValues.add(ProductState.KEY_ACTION_REQUEST);
		possibleValues.add(ProductState.KEY_PRODUCT_PRIORITY);
		possibleValues.add(ProductState.KEY_POSITION);
		possibleValues.add(ProductState.KEY_LAST_STATE_CHANGE);
		possibleValues.add(ProductState.KEY_TARGET_CONFIGURATION);
		possibleValues.add(ProductState.KEY_VERSION_INFO);

		return possibleValues;
	}

	private static List<String> getDefaultValuesProductOnClientDisplayFields() {
		List<String> result = new ArrayList<>();

		result.add("productId");

		result.add(ProductState.KEY_INSTALLATION_STATUS);
		result.add(ProductState.KEY_INSTALLATION_INFO);
		result.add(ProductState.KEY_ACTION_REQUEST);
		result.add(ProductState.KEY_VERSION_INFO);

		return result;
	}

	private List<String> produceProductOnClientDisplayfieldsNetboot() {
		List<String> result = getDefaultValuesProductOnClientDisplayFields();
		List<String> possibleValues = getPossibleValuesProductOnClientDisplayFields();

		// create config for service
		Map<String, Object> item = createNOMitem("UnicodeConfig");
		item.put("ident", KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT);
		item.put("description", "");
		item.put("defaultValues", result);
		item.put("possibleValues", possibleValues);
		item.put("editable", false);
		item.put("multiValue", true);

		Logging.info(this, "produceProductOnClientDisplayfields_netboot");

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { item });

		exec.doCall(omc);

		return result;
	}

	public Map<String, Boolean> getProductOnClientsDisplayFieldsNetbootProducts() {
		if (productOnClientsDisplayFieldsNetbootProducts == null) {
			Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

			List<String> configuredByService = Globals
					.takeAsStringList(serverPropertyMap.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT));

			List<?> possibleValuesAccordingToService = new ArrayList<>();

			if (configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT) != null) {
				possibleValuesAccordingToService = (List<?>) configOptions
						.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT).get("possibleValues");
			}

			if (configuredByService.isEmpty() || !((new HashSet<>(getPossibleValuesProductOnClientDisplayFields()))
					.equals(new HashSet<>(possibleValuesAccordingToService)))) {
				// we did not initialize server property
				configuredByService = produceProductOnClientDisplayfieldsNetboot();
			}

			productOnClientsDisplayFieldsNetbootProducts = new LinkedHashMap<>();

			// key names from ProductState
			productOnClientsDisplayFieldsNetbootProducts.put("productId", true);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_PRODUCT_NAME,
					configuredByService.indexOf(ProductState.KEY_PRODUCT_NAME) > -1);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_TARGET_CONFIGURATION, false);
			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_INSTALLATION_STATUS, true);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_INSTALLATION_INFO,
					configuredByService.indexOf(ProductState.KEY_INSTALLATION_INFO) > -1);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_ACTION_REQUEST, true);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_LAST_STATE_CHANGE,
					configuredByService.indexOf(ProductState.KEY_LAST_STATE_CHANGE) > -1);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_VERSION_INFO, true);
		}

		return productOnClientsDisplayFieldsNetbootProducts;
	}

	private List<String> produceHostDisplayFields(List<String> givenList) {
		List<String> result = null;
		Logging.info(this,
				"produceHost_displayFields configOptions.get(key) " + configOptions.get(KEY_HOST_DISPLAYFIELDS));

		List<String> possibleValues = new ArrayList<>();
		possibleValues.add(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.LAST_SEEN_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CREATED_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL);

		List<String> defaultValues = new ArrayList<>();
		defaultValues.add(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL);
		defaultValues.add(HostInfo.CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL);
		defaultValues.add(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);
		defaultValues.add(HostInfo.LAST_SEEN_DISPLAY_FIELD_LABEL);
		defaultValues.add(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);

		if (givenList == null || givenList.isEmpty()) {
			result = defaultValues;

			Logging.info(this, "givenList is null or empty: " + givenList);

			// create config for service
			Map<String, Object> item = createNOMitem("UnicodeConfig");
			item.put("ident", KEY_HOST_DISPLAYFIELDS);
			item.put("description", "");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", false);
			item.put("multiValue", true);

			OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { item });

			exec.doCall(omc);
		} else {
			result = givenList;
			// but not if we want to change the default values:
		}

		return result;
	}

	public Map<String, Boolean> getHostDisplayFields() {
		if (hostDisplayFields == null) {
			Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

			List<String> configuredByService = Globals.takeAsStringList(serverPropertyMap.get(KEY_HOST_DISPLAYFIELDS));

			// check if have to initialize the server property
			configuredByService = produceHostDisplayFields(configuredByService);

			hostDisplayFields = new LinkedHashMap<>();
			hostDisplayFields.put(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, true);
			// always shown, we put it here because of ordering and repeat the statement
			// after the loop if it has been set to false

			for (String field : HostInfo.ORDERING_DISPLAY_FIELDS) {
				hostDisplayFields.put(field, configuredByService.indexOf(field) > -1);
			}

			hostDisplayFields.put(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, true);
		}

		return hostDisplayFields;
	}

	public List<String> getDisabledClientMenuEntries() {
		getConfigOptions();
		return Globals.takeAsStringList(configDefaultValues.get(KEY_DISABLED_CLIENT_ACTIONS));
	}

	public List<String> getOpsiclientdExtraEvents() {
		Logging.debug(this, "getOpsiclientdExtraEvents");
		getConfigOptions();
		if (configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS) == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
		}

		List<String> result = Globals.takeAsStringList(configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS));
		Logging.debug(this, "getOpsiclientdExtraEvents() " + result);
		return result;
	}

	private static Map<String, Object> produceConfigEntry(String nomType, String key, Object value,
			String description) {
		return produceConfigEntry(nomType, key, value, description, true);
	}

	private static Map<String, Object> produceConfigEntry(String nomType, String key, Object value, String description,
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

	private boolean checkStandardConfigs() {
		boolean result = getConfigOptions() != null;
		Logging.info(this, "checkStandardConfigs, already there " + result);

		if (!result) {
			return false;
		}

		List<Object> defaultValues;
		List<Object> possibleValues;
		Map<String, Object> item;
		String key;
		List<Map<String, Object>> readyObjects = new ArrayList<>();

		// list of domains for new clients
		key = CONFIGED_GIVEN_DOMAINS_KEY;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(getOpsiDefaultDomain());

			possibleValues = new ArrayList<>();
			possibleValues.add(getOpsiDefaultDomain());

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// search by sql if possible
		key = KEY_SEARCH_BY_SQL;

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			item = createJSONBoolConfig(key, DEFAULTVALUE_SEARCH_BY_SQL,
					"Use SQL calls for search if SQL backend is active");
			readyObjects.add(item);
		}

		// global value for install_by_shutdown

		key = KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN;

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			item = createJSONBoolConfig(key, DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN,
					"Use install by shutdown if possible");
			readyObjects.add(item);
		}

		// product_sort_algorithm
		// will not be used in opsi 4.3
		if (!ServerFacade.isOpsi43()) {
			key = KEY_PRODUCT_SORT_ALGORITHM;
			// defaultValues
			defaultValues = configDefaultValues.get(key);
			Logging.info(this, "checkStandardConfigs:  from server product_sort_algorithm " + defaultValues);

			if (defaultValues == null) {
				Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

				defaultValues = new ArrayList<>();
				defaultValues.add("algorithm1");

				possibleValues = new ArrayList<>();
				possibleValues.add("algorithm1");
				possibleValues.add("algorithm2");

				// create config for service
				item = createNOMitem("UnicodeConfig");
				item.put("ident", key);
				item.put("description", "algorithm1 = dependencies first; algorithm2 = priorities first");
				item.put("defaultValues", defaultValues);

				item.put("possibleValues", possibleValues);
				item.put("editable", false);
				item.put("multiValue", false);

				readyObjects.add(item);
			}
		}

		// extra display fields in licencing

		key = KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION;

		// defaultValues

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			// key not yet configured
			defaultValues = new ArrayList<>();
			// example for standard configuration other than empty
			// extra columns for licence management, page licences reconciliation
			possibleValues = new ArrayList<>();
			possibleValues.add("description");
			possibleValues.add("inventoryNumber");
			possibleValues.add("notes");
			possibleValues.add("ipAddress");
			possibleValues.add("lastSeen");

			// create config for service
			item = createNOMitem("UnicodeConfig");
			item.put("ident", key);
			item.put("description",
					Configed.getResourceValue("ConfigedMain.Licences.TabLicenceReconciliation.ExtraHostFields"));
			item.put("defaultValues", defaultValues);

			item.put("possibleValues", possibleValues);
			item.put("editable", false);
			item.put("multiValue", true);

			readyObjects.add(item);
		}

		// remote controls
		String command;
		String description;

		// ping_linux
		key = RemoteControl.CONFIG_KEY + "." + "ping_linux";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "xterm +hold -e ping %host%";
			description = "ping, started in a Linux environment";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, true,
					"(command may be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// ping_windows
		key = RemoteControl.CONFIG_KEY + "." + "ping_windows";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "cmd.exe /c start ping %host%";
			description = "ping, started in a Windows terminal";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, true,
					"(command may be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, linux
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_linux";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "firefox https://%host%:4441/info.html";
			description = "opsiclientd  timeline, called from a Linux environment, firefox recommended";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, false,
					"(command may not be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, windows
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_windows";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "cmd.exe /c start https://%host%:4441/info.html";
			description = "opsiclientd  timeline, called rfrom a Windows environment";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, false,
					"(command may not be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// additional queries
		String query;
		StringBuilder qbuf;
		key = CONFIG_KEY_SUPPLEMENTARY_QUERY + "." + "hosts_with_products";

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			qbuf = new StringBuilder("select");
			qbuf.append(" hostId, productId, installationStatus from ");
			qbuf.append(" HOST, PRODUCT_ON_CLIENT ");
			qbuf.append(" WHERE HOST.hostId  = PRODUCT_ON_CLIENT.clientId ");
			qbuf.append(" AND =  installationStatus='installed' ");
			qbuf.append(" order by hostId, productId ");

			query = qbuf.toString();
			description = "all hosts and their installed products";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, query, description));

			readyObjects
					.add(produceConfigEntry("BoolConfig", key + "." + EDITABLE_KEY, false, "(command may be edited)"));
			// description entry
			readyObjects.add(produceConfigEntry("UnicodeConfig", key + "." + DESCRIPTION_KEY, description, ""));
		}

		// WAN_CONFIGURATION
		// does it exist?

		Map<String, ConfigOption> wanConfigOptions = getWANConfigOptions();
		if (wanConfigOptions == null || wanConfigOptions.isEmpty()) {
			Logging.info(this, "build default wanConfigOptions");
			readyObjects = buildWANConfigOptions(readyObjects);
		}

		// saved searches

		key = SavedSearch.CONFIG_KEY + "." + "product_failed";

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			StringBuilder val = new StringBuilder();
			val.append("{ \"version\" : \"2\", ");
			val.append("\"data\" : {");
			val.append(" \"element\" : null, ");
			val.append(" \"elementPath\" : null,");
			val.append(" \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, ");
			val.append(
					" \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] ");
			val.append("} }");

			String value = val.toString();

			description = "any product failed";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, value, description));

			// description entry
			readyObjects
					.add(produceConfigEntry("UnicodeConfig", key + "." + SavedSearch.DESCRIPTION_KEY, description, ""));
		}

		// configuration of host menus

		key = KEY_DISABLED_CLIENT_ACTIONS;

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_DISABLED_CLIENT_ACTIONS);
			// key not yet configured
			defaultValues = new ArrayList<>();
			configDefaultValues.put(key, defaultValues);

			possibleValues = new ArrayList<>();
			possibleValues.add(MainFrame.ITEM_ADD_CLIENT);
			possibleValues.add(MainFrame.ITEM_DELETE_CLIENT);
			possibleValues.add(MainFrame.ITEM_FREE_LICENCES);

			item = createNOMitem("UnicodeConfig");
			item.put("id", key);
			item.put("description", "");
			item.put("defaultValues", defaultValues);

			item.put("possibleValues", possibleValues);
			item.put("editable", false);
			item.put("multiValue", true);

			readyObjects.add(item);
		}

		key = KEY_SSH_DEFAULTWINUSER;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_SSH_DEFAULTWINUSER);
			readyObjects.add(produceConfigEntry("UnicodeConfig", key, KEY_SSH_DEFAULTWINUSER_DEFAULT_VALUE,
					"default windows username for deploy-client-agent-script"));
		}

		key = KEY_SSH_DEFAULTWINPW;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_SSH_DEFAULTWINPW);
			readyObjects.add(produceConfigEntry("UnicodeConfig", key, KEY_SSH_DEFAULTWINPW_DEFAULT_VALUE,
					"default windows password for deploy-client-agent-script"));
		}

		key = CONFIGED_WORKBENCH_KEY;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add(produceConfigEntry("UnicodeConfig", key, configedWorkbenchDefaultValue,
					"default path to opsiproducts"));
		} else {
			Logging.info(this, "checkStandardConfigs set WORKBENCH_defaultvalue to " + (String) defaultValues.get(0));
			configedWorkbenchDefaultValue = (String) defaultValues.get(0);
		}

		// configuration of opsiclientd extra events

		key = KEY_OPSICLIENTD_EXTRA_EVENTS;

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
			// key not yet configured
			defaultValues = new ArrayList<>();

			defaultValues.add(OPSI_CLIENTD_EVENT_ON_DEMAND);

			configDefaultValues.put(key, defaultValues);

			possibleValues = new ArrayList<>();

			possibleValues.add(OPSI_CLIENTD_EVENT_ON_DEMAND);
			possibleValues.add(OPSI_CLIENTD_EVENT_SILENT_INSTALL);

			item = createNOMitem("UnicodeConfig");
			item.put("id", key);
			item.put("description", "");
			item.put("defaultValues", defaultValues);

			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(item);
		}

		// for warnings for opsi licences

		// percentage number of clients
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// absolute number of clients
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// days limit warning
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// modules disabled for warnings
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.DISABLE_WARNING_FOR_MODULES;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();

			possibleValues = new ArrayList<>();

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", defaultValues);
			item.put("possibleValues", possibleValues);
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(item);

			configDefaultValues.put(key, defaultValues);
		}

		// add metaconfigs

		// Update configs if there are some to update
		if (!readyObjects.isEmpty()) {
			Logging.notice(this, "There are " + readyObjects.size() + "configurations to update, so we do this now:");

			OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", new Object[] { readyObjects });

			exec.doCall(omc);
		} else {
			Logging.notice(this, "there are no configurations to update");
		}

		List<Map<String, Object>> defaultUserConfigsObsolete = new ArrayList<>();

		// delete obsolete configs

		for (Entry<String, List<Object>> configEntry : configDefaultValues.entrySet()) {
			if (configEntry.getKey().startsWith(ALL_USER_KEY_START + "ssh")) {
				defaultValues = configEntry.getValue();

				if (defaultValues != null) {
					// still existing
					Logging.info(this, "handling ssh config key at old location " + configEntry.getKey());
					Map<String, Object> config = new HashMap<>();

					config.put("id", configEntry.getKey());

					String type = "BoolConfig";
					config.put("type", type);

					defaultUserConfigsObsolete.add(config);
				}
			}
		}

		for (Entry<String, List<Object>> configEntry : configDefaultValues.entrySet()) {
			if (configEntry.getKey().startsWith(ALL_USER_KEY_START + "{ole.")) {
				defaultValues = configEntry.getValue();

				if (defaultValues != null) {
					// still existing
					Logging.info(this, "removing unwillingly generated entry  " + configEntry.getKey());
					Map<String, Object> config = new HashMap<>();

					config.put("id", configEntry.getKey());

					String type = "BoolConfig";
					config.put("type", type);

					defaultUserConfigsObsolete.add(config);
				}
			}
		}

		Logging.info(this, "defaultUserConfigsObsolete " + defaultUserConfigsObsolete);

		if (!defaultUserConfigsObsolete.isEmpty()) {
			exec.doCall(new OpsiMethodCall("config_deleteObjects", new Object[] { defaultUserConfigsObsolete }));
		}

		return true;
	}

	private ExtendedInteger calculateModulePermission(ExtendedInteger globalMaxClients,
			final Integer specialMaxClientNumber) {
		Logging.info(this, "calculateModulePermission globalMaxClients " + globalMaxClients + " specialMaxClientNumber "
				+ specialMaxClientNumber);
		Integer maxClients = null;

		if (specialMaxClientNumber != null) {
			int compareResult = globalMaxClients.compareTo(specialMaxClientNumber);
			Logging.info(this, "calculateModulePermission compareResult " + compareResult);

			// the global max client count is reduced, a real warning and error limit exists
			if (compareResult < 0) {

				maxClients = specialMaxClientNumber;
				globalMaxClients = new ExtendedInteger(maxClients);
			} else {
				maxClients = specialMaxClientNumber;
			}
		}

		Logging.info(this, "calculateModulePermission returns " + maxClients);

		if (maxClients == null) {
			return globalMaxClients;
		} else {
			return new ExtendedInteger(maxClients);
		}
	}

	// opsi module information
	public void opsiInformationRequestRefresh() {
		opsiInformation = new HashMap<>();
	}

	public Map<String, Object> getOpsiModulesInfos() {
		retrieveOpsiModules();
		return opsiModulesDisplayInfo;
	}

	public boolean isOpsiLicencingAvailable() {
		retrieveOpsiLicensingInfoVersion();
		return isOpsiLicencingAvailable;
	}

	private void retrieveOpsiLicensingInfoVersion() {
		if (!hasOpsiLicencingBeenChecked) {
			Logging.info(this, "retrieveOpsiLicensingInfoVersion getMethodSignature( backend_getLicensingInfo "
					+ getMethodSignature(BACKEND_LICENSING_INFO_METHOD_NAME));

			if (getMethodSignature(BACKEND_LICENSING_INFO_METHOD_NAME) == NONE_LIST) {
				Logging.info(this,
						"method " + BACKEND_LICENSING_INFO_METHOD_NAME + " not existing in this opsi service");
				isOpsiLicencingAvailable = false;
			} else {
				isOpsiLicencingAvailable = true;
			}

			hasOpsiLicencingBeenChecked = true;
		}
	}

	public boolean isOpsiUserAdmin() {

		if (!hasIsOpisUserAdminBeenChecked) {
			retrieveIsOpsiUserAdmin();
		}

		return isOpsiUserAdmin;
	}

	private void retrieveIsOpsiUserAdmin() {
		OpsiMethodCall omc = new OpsiMethodCall("accessControl_userIsAdmin", new Object[] {});

		Map<String, Object> json = exec.retrieveResponse(omc);

		if (json.containsKey("result") && json.get("result") != null) {
			isOpsiUserAdmin = (Boolean) json.get("result");
		} else {
			Logging.warning(this, "cannot check if user is admin, fallback to false...");

			isOpsiUserAdmin = false;
		}

		hasIsOpisUserAdminBeenChecked = true;
	}

	public final void opsiLicencingInfoRequestRefresh() {
		licencingInfoOpsiAdmin = null;
		licencingInfoNoOpsiAdmin = null;
		licInfoMap = null;
		LicensingInfoMap.requestRefresh();
		Logging.info(this, "request worked");
	}

	// is not allowed to be overriden in order to prevent changes
	public final Map<String, Object> getOpsiLicencingInfoOpsiAdmin() {
		if (licencingInfoOpsiAdmin == null && isOpsiLicencingAvailable() && isOpsiUserAdmin()) {
			OpsiMethodCall omc = new OpsiMethodCall(BACKEND_LICENSING_INFO_METHOD_NAME,
					new Object[] { true, false, true, false });

			licencingInfoOpsiAdmin = exec.retrieveResponse(omc);
		}

		return licencingInfoOpsiAdmin;
	}

	private Map<String, Object> produceOpsiInformation() {
		if (!opsiInformation.isEmpty()) {
			// we are initialized
			return opsiInformation;
		}

		String methodName = "backend_info";

		if (ServerFacade.isOpsi43()) {
			methodName = BACKEND_LICENSING_INFO_METHOD_NAME;
		}

		OpsiMethodCall omc = new OpsiMethodCall(methodName, new String[] {});
		opsiInformation = new HashMap<>();

		// method does not exist before opsi 3.4
		if (getMethodSignature(methodName) != NONE_LIST) {
			opsiInformation = exec.getMapResult(omc);
		}

		return opsiInformation;
	}

	private void produceOpsiModulesInfo() {
		// has the actual signal if a module is activ
		opsiModules = new HashMap<>();

		// opsiinformation which delivers the service information on checked modules

		// displaying to the user

		getHostInfoCollections().retrieveOpsiHosts();
		Logging.info(this,
				"getOverLimitModuleList() "
						+ LicensingInfoMap.getInstance(getOpsiLicencingInfoOpsiAdmin(), getConfigDefaultValues(), true)
								.getCurrentOverLimitModuleList());

		licInfoMap = LicensingInfoMap.getInstance(getOpsiLicencingInfoOpsiAdmin(), getConfigDefaultValues(),
				!FGeneralDialogLicensingInfo.extendedView);

		List<String> availableModules = licInfoMap.getAvailableModules();

		for (String mod : licInfoMap.getModules()) {
			opsiModules.put(mod, availableModules.indexOf(mod) != -1);
		}

		Logging.info(this, "opsiModules result " + opsiModules);

		withLicenceManagement = opsiModules.get("license_management") != null && opsiModules.get("license_management");
		withLocalImaging = opsiModules.get("local_imaging") != null && opsiModules.get("local_imaging");

		withMySQL = canCallMySQL();
		withUEFI = opsiModules.get("uefi") != null && opsiModules.get("uefi");
		withWAN = opsiModules.get("vpn") != null && opsiModules.get("vpn");
		withUserRoles = opsiModules.get("userroles") != null && opsiModules.get("userroles");

		Logging.info(this, "produceOpsiModulesInfo withUserRoles " + withUserRoles);
		Logging.info(this, "produceOpsiModulesInfo withUEFI " + withUEFI);
		Logging.info(this, "produceOpsiModulesInfo withWAN " + withWAN);
		Logging.info(this, "produceOpsiModulesInfo withLicenceManagement " + withLicenceManagement);
		Logging.info(this, "produceOpsiModulesInfo withMySQL " + withMySQL);

		// sets value to true if we use the mysql backend and informs that we are
		// underlicensed
	}

	private void produceOpsiModulesInfoClassicOpsi43() {
		produceOpsiInformation();

		// keeps the info for displaying to the user
		opsiModulesDisplayInfo = new HashMap<>();

		HashMap<String, ModulePermissionValue> opsiModulesPermissions = new HashMap<>();
		// has the actual signal if a module is active
		opsiModules = new HashMap<>();

		final List<String> missingModulesPermissionInfo = new ArrayList<>();

		// prepare the user info
		Map<String, Object> opsiModulesInfo = exec.getMapFromItem(opsiInformation.get("modules"));
		Logging.info(this, "opsi module information " + opsiModulesInfo);

		ExtendedDate validUntil = ExtendedDate.INFINITE;

		// analyse the real module info
		Map<String, Object> opsiCountModules = exec.getMapFromItem(opsiInformation.get("modules"));
		opsiCountModules.keySet()
				.removeAll(exec.getListFromItem(((JSONArray) opsiInformation.get("obsolete_modules")).toString()));
		getHostInfoCollections().retrieveOpsiHosts();

		ExtendedInteger globalMaxClients = ExtendedInteger.INFINITE;

		int countClients = hostInfoCollections.getCountClients();

		LocalDateTime today = LocalDateTime.now();

		Logging.info(this, "opsiModulesInfo " + opsiModulesInfo);

		// read in modules
		for (Entry<String, Object> opsiModuleInfo : opsiModulesInfo.entrySet()) {
			Logging.info(this, "module from opsiModulesInfo, key " + opsiModuleInfo);
			Map<String, Object> opsiModuleData = POJOReMapper.remap(opsiModuleInfo.getValue(),
					new TypeReference<Map<String, Object>>() {
					});
			ModulePermissionValue modulePermission = new ModulePermissionValue(opsiModuleData.get("available"),
					validUntil);

			Logging.info(this, "handle modules key, modulePermission  " + modulePermission);
			Boolean permissionCheck = modulePermission.getBoolean();
			opsiModulesPermissions.put(opsiModuleInfo.getKey(), modulePermission);
			if (permissionCheck != null) {
				opsiModules.put(opsiModuleInfo.getKey(), permissionCheck);
			}

			if (opsiModuleData.get("available") != null) {
				opsiModulesDisplayInfo.put(opsiModuleInfo.getKey(), opsiModuleData.get("available"));
			}
		}

		Logging.info(this, "modules resulting step 0  " + opsiModules);

		// existing
		for (Entry<String, Object> opsiCountModule : opsiCountModules.entrySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(opsiCountModule.getKey());
			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission was " + modulePermission);
			Map<String, Object> opsiModuleData = POJOReMapper.remap(opsiCountModule.getValue(),
					new TypeReference<Map<String, Object>>() {
					});

			if ("free".equals(opsiModuleData.get("state"))) {
				continue;
			}

			modulePermission = new ModulePermissionValue(opsiModuleData.get("client_number"), validUntil);

			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission set " + modulePermission);
			// replace value got from modulesInfo
			opsiModulesPermissions.put(opsiCountModule.getKey(), modulePermission);

			if (opsiModuleData.get("client_number") != null) {
				opsiModulesDisplayInfo.put(opsiCountModule.getKey(), opsiModuleData.get("client_number"));
			}
		}

		Logging.info(this, "modules resulting step 1 " + opsiModules);
		Logging.info(this, "countModules is  " + opsiCountModules);

		// set values for modules checked by configed
		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			if (modulePermission.getBoolean() != null) {
				opsiModules.put(key, modulePermission.getBoolean());
				Logging.info(this,
						" retrieveOpsiModules, set opsiModules for key " + key + ": " + modulePermission.getBoolean());
			} else {
				opsiModules.put(key, true);
				Logging.info(this, " retrieveOpsiModules " + key + " " + maxClientsForThisModule.getNumber());

				if (maxClientsForThisModule.equals(ExtendedInteger.ZERO)) {
					opsiModules.put(key, false);
				} else {

					Integer warningLimit = null;
					Integer stopLimit = null;

					Logging.info(this,
							" retrieveOpsiModules " + key + " up to now globalMaxClients " + globalMaxClients);

					Logging.info(this, " retrieveOpsiModules " + key + " maxClientsForThisModule.getNumber "
							+ maxClientsForThisModule.getNumber());

					globalMaxClients = calculateModulePermission(globalMaxClients, maxClientsForThisModule.getNumber());

					Logging.info(this,
							" retrieveOpsiModules " + key + " result:  globalMaxClients is " + globalMaxClients);

					Integer newGlobalLimit = globalMaxClients.getNumber();

					// global limit is changed by this module a real warning
					// and error limit exists
					if (newGlobalLimit != null) {
						warningLimit = newGlobalLimit - CLIENT_COUNT_WARNING_LIMIT;
						stopLimit = newGlobalLimit + CLIENT_COUNT_TOLERANCE_LIMIT;
					}

					Logging.info(this, " retrieveOpsiModules " + key + " old  warningLimit " + warningLimit
							+ " stopLimit " + stopLimit);

					if (stopLimit != null && hostInfoCollections.getCountClients() > stopLimit) {
						opsiModules.put(key, false);
					} else {
						if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
							LocalDateTime expiresDate = expiresForThisModule.getDate();

							if (today.isAfter(expiresDate)) {
								opsiModules.put(key, false);
							}
						}
					}
				}
			}
		}

		Logging.info(this, "modules resulting step 2  " + opsiModules);
		Logging.info(this, "count Modules is  " + opsiCountModules);

		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			int countClientsInThisBlock = countClients;

			// tests

			Logging.info(this, "check module " + key + " problem on start " + (!(opsiModules.get(key))));
			boolean problemToIndicate = true;
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxAllowedClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			Logging.info(this, "check  module " + key + " maxAllowedClientsForThisModule "
					+ maxAllowedClientsForThisModule + " expiresForThisModule " + expiresForThisModule);

			if (maxAllowedClientsForThisModule.equals(ExtendedInteger.ZERO)) {
				problemToIndicate = false;
			}

			if (problemToIndicate
					&& ("linux_agent".equals(key) || ("userroles".equals(key) && !isUserRegisterActivated()))) {
				problemToIndicate = false;
			}

			Logging.info(this, "check module " + key + "  problemToIndicate " + problemToIndicate);

			if (problemToIndicate) {
				Logging.info(this, "retrieveOpsiModules " + key + " , maxClients " + maxAllowedClientsForThisModule
						+ " count " + countClientsInThisBlock);

				if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
					LocalDateTime noticeDate = expiresForThisModule.getDate().minusDays(14);

					if (today.isAfter(noticeDate)) {
						missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);
					}
				}

				if (!ExtendedInteger.INFINITE.equals(maxAllowedClientsForThisModule)) {
					int startWarningCount = maxAllowedClientsForThisModule.getNumber() - CLIENT_COUNT_WARNING_LIMIT;
					int stopCount = maxAllowedClientsForThisModule.getNumber() + CLIENT_COUNT_TOLERANCE_LIMIT;

					if (countClientsInThisBlock > stopCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " stopCount " + stopCount + " count clients "
								+ countClients);

						String warningText =

								String.format(
										// locale.
										Configed.getResourceValue("Permission.modules.clientcount.error"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);

						Logging.warning(this, warningText);
					} else if (countClientsInThisBlock > startWarningCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " startWarningCount " + startWarningCount
								+ " count clients " + countClients);

						String warningText =

								String.format(
										// locale,
										Configed.getResourceValue("Permission.modules.clientcount.warning"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);
						Logging.warning(this, warningText);
					} else {
						// Do nothing when countClientsInThisBlock <= startWarningCount
					}
				}
			}
		}

		Logging.info(this, "modules resulting  " + opsiModules);
		Logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos " + missingModulesPermissionInfo);

		// Will be called only, when info empty
		callOpsiLicenceMissingModules(missingModulesPermissionInfo);

		withLicenceManagement = opsiModules.get("license_management") != null && opsiModules.get("license_management");
		withLocalImaging = opsiModules.get("local_imaging") != null && opsiModules.get("local_imaging");

		withMySQL = canCallMySQL();
		withUEFI = opsiModules.get("uefi") != null && opsiModules.get("uefi");
		withWAN = opsiModules.get("vpn") != null && opsiModules.get("vpn");
		withUserRoles = opsiModules.get("userroles") != null && opsiModules.get("userroles");

		Logging.info(this, "retrieveOpsiModules opsiCountModules " + opsiCountModules);
		Logging.info(this, "retrieveOpsiModules opsiModulesPermissions " + opsiModulesPermissions);
		Logging.info(this, "retrieveOpsiModules opsiModules " + opsiModules);
	}

	private void callOpsiLicenceMissingModules(List<String> missingModulesPermissionInfo) {
		if (!missingModulesPermissionInfo.isEmpty()) {

			SwingUtilities.invokeLater(() -> {
				StringBuilder info = new StringBuilder();

				info.append(Configed.getResourceValue("Permission.modules.clientcount.2"));
				info.append(":\n");
				for (String moduleInfo : missingModulesPermissionInfo) {
					info.append(moduleInfo + "\n");
				}

				Logging.info(this, "missingModules " + info);
				FOpsiLicenseMissingText.callInstanceWith(info.toString());
			});
		}
	}

	private void produceOpsiModulesInfoClassic() {
		produceOpsiInformation();

		// keeps the info for displaying to the user
		opsiModulesDisplayInfo = new HashMap<>();

		HashMap<String, ModulePermissionValue> opsiModulesPermissions = new HashMap<>();
		// has the actual signal if a module is active
		opsiModules = new HashMap<>();

		String opsiVersion = (String) opsiInformation.get("opsiVersion");
		Logging.info(this, "opsi version information " + opsiVersion);

		final List<String> missingModulesPermissionInfo = new ArrayList<>();

		// prepare the user info
		Map<String, Object> opsiModulesInfo = exec.getMapFromItem(opsiInformation.get("modules"));

		opsiModulesInfo.remove("signature");
		Logging.info(this, "opsi module information " + opsiModulesInfo);
		opsiModulesInfo.remove("valid");

		opsiModulesDisplayInfo = new HashMap<>(opsiModulesInfo);

		ExtendedDate validUntil = ExtendedDate.INFINITE;

		// analyse the real module info
		Map<String, Object> opsiCountModules = exec.getMapFromItem(opsiInformation.get("realmodules"));
		getHostInfoCollections().retrieveOpsiHosts();

		ExtendedInteger globalMaxClients = ExtendedInteger.INFINITE;

		int countClients = hostInfoCollections.getCountClients();

		LocalDateTime today = LocalDateTime.now();

		Logging.info(this, "opsiModulesInfo " + opsiModulesInfo);

		// read in modules
		for (Entry<String, Object> opsiModuleInfo : opsiModulesInfo.entrySet()) {
			Logging.info(this, "module from opsiModulesInfo, key " + opsiModuleInfo.getKey());
			ModulePermissionValue modulePermission = new ModulePermissionValue(opsiModuleInfo.getValue(), validUntil);

			Logging.info(this, "handle modules key, modulePermission  " + modulePermission);
			Boolean permissionCheck = modulePermission.getBoolean();
			opsiModulesPermissions.put(opsiModuleInfo.getKey(), modulePermission);
			if (permissionCheck != null) {
				opsiModules.put(opsiModuleInfo.getKey(), permissionCheck);
			}
		}

		Logging.info(this, "modules resulting step 0  " + opsiModules);

		// existing
		for (Entry<String, Object> opsiCountModule : opsiCountModules.entrySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(opsiCountModule.getKey());
			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission was " + modulePermission);

			modulePermission = new ModulePermissionValue(opsiCountModule.getValue(), validUntil);

			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission set " + modulePermission);
			// replace value got from modulesInfo
			opsiModulesPermissions.put(opsiCountModule.getKey(), modulePermission);

			if (opsiCountModule.getValue() != null) {
				opsiModulesDisplayInfo.put(opsiCountModule.getKey(), opsiCountModule.getValue());
			}
		}

		Logging.info(this, "modules resulting step 1 " + opsiModules);
		Logging.info(this, "countModules is  " + opsiCountModules);

		// set values for modules checked by configed
		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			if (modulePermission.getBoolean() != null) {
				opsiModules.put(key, modulePermission.getBoolean());
				Logging.info(this,
						" retrieveOpsiModules, set opsiModules for key " + key + ": " + modulePermission.getBoolean());
			} else {
				opsiModules.put(key, true);
				Logging.info(this, " retrieveOpsiModules " + key + " " + maxClientsForThisModule.getNumber());

				if (maxClientsForThisModule.equals(ExtendedInteger.ZERO)) {
					opsiModules.put(key, false);
				} else {

					Integer warningLimit = null;
					Integer stopLimit = null;

					Logging.info(this,
							" retrieveOpsiModules " + key + " up to now globalMaxClients " + globalMaxClients);

					Logging.info(this, " retrieveOpsiModules " + key + " maxClientsForThisModule.getNumber "
							+ maxClientsForThisModule.getNumber());

					globalMaxClients = calculateModulePermission(globalMaxClients, maxClientsForThisModule.getNumber());

					Logging.info(this,
							" retrieveOpsiModules " + key + " result:  globalMaxClients is " + globalMaxClients);

					Integer newGlobalLimit = globalMaxClients.getNumber();

					// global limit is changed by this module a real warning
					// and error limit exists
					if (newGlobalLimit != null) {
						warningLimit = newGlobalLimit - CLIENT_COUNT_WARNING_LIMIT;
						stopLimit = newGlobalLimit + CLIENT_COUNT_TOLERANCE_LIMIT;
					}

					Logging.info(this, " retrieveOpsiModules " + key + " old  warningLimit " + warningLimit
							+ " stopLimit " + stopLimit);

					if (stopLimit != null && hostInfoCollections.getCountClients() > stopLimit) {
						opsiModules.put(key, false);
					} else {
						if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
							LocalDateTime expiresDate = expiresForThisModule.getDate();

							if (today.isAfter(expiresDate)) {
								opsiModules.put(key, false);
							}
						}
					}
				}
			}
		}

		Logging.info(this, "modules resulting step 2  " + opsiModules);
		Logging.info(this, "count Modules is  " + opsiCountModules);

		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			int countClientsInThisBlock = countClients;

			// tests

			Logging.info(this, "check module " + key + " problem on start " + (!(opsiModules.get(key))));
			boolean problemToIndicate = true;
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxAllowedClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			Logging.info(this, "check  module " + key + " maxAllowedClientsForThisModule "
					+ maxAllowedClientsForThisModule + " expiresForThisModule " + expiresForThisModule);

			if (maxAllowedClientsForThisModule.equals(ExtendedInteger.ZERO)) {
				problemToIndicate = false;
			}

			if (problemToIndicate
					&& ("linux_agent".equals(key) || ("userroles".equals(key) && !isUserRegisterActivated()))) {
				problemToIndicate = false;
			}

			Logging.info(this, "check module " + key + "  problemToIndicate " + problemToIndicate);

			if (problemToIndicate) {
				Logging.info(this, "retrieveOpsiModules " + key + " , maxClients " + maxAllowedClientsForThisModule
						+ " count " + countClientsInThisBlock);

				if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
					LocalDateTime noticeDate = expiresForThisModule.getDate().minusDays(14);

					if (today.isAfter(noticeDate)) {
						missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);
					}
				}

				if (!ExtendedInteger.INFINITE.equals(maxAllowedClientsForThisModule)) {
					int startWarningCount = maxAllowedClientsForThisModule.getNumber() - CLIENT_COUNT_WARNING_LIMIT;
					int stopCount = maxAllowedClientsForThisModule.getNumber() + CLIENT_COUNT_TOLERANCE_LIMIT;

					if (countClientsInThisBlock > stopCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " stopCount " + stopCount + " count clients "
								+ countClients);

						String warningText =

								String.format(
										// locale.
										Configed.getResourceValue("Permission.modules.clientcount.error"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);

						Logging.warning(this, warningText);
					} else if (countClientsInThisBlock > startWarningCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " startWarningCount " + startWarningCount
								+ " count clients " + countClients);

						String warningText =

								String.format(Configed.getResourceValue("Permission.modules.clientcount.warning"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);
						Logging.warning(this, warningText);
					} else {
						// countClientsInThisBlock small enough, so nothing to do
					}
				}
			}
		}

		Logging.info(this, "modules resulting  " + opsiModules);
		Logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos " + missingModulesPermissionInfo);

		// Will be called only when info empty
		callOpsiLicenceMissingModules(missingModulesPermissionInfo);

		withLicenceManagement = (opsiModules.get("license_management") != null)
				&& opsiModules.get("license_management");
		withLocalImaging = (opsiModules.get("local_imaging") != null) && opsiModules.get("local_imaging");

		withMySQL = canCallMySQL();
		withUEFI = (opsiModules.get("uefi") != null) && opsiModules.get("uefi");
		withWAN = (opsiModules.get("vpn") != null) && opsiModules.get("vpn");
		withUserRoles = (opsiModules.get("userroles") != null) && opsiModules.get("userroles");

		Logging.info(this, "retrieveOpsiModules opsiCountModules " + opsiCountModules);
		Logging.info(this, "retrieveOpsiModules opsiModulesPermissions " + opsiModulesPermissions);
		Logging.info(this, "retrieveOpsiModules opsiModules " + opsiModules);
	}

	public final void retrieveOpsiModules() {
		Logging.info(this, "retrieveOpsiModules ");

		licencingInfoOpsiAdmin = getOpsiLicencingInfoOpsiAdmin();

		// probably old opsi service version
		if (licencingInfoOpsiAdmin == null) {
			if (ServerFacade.isOpsi43()) {
				produceOpsiModulesInfoClassicOpsi43();
			} else {
				produceOpsiModulesInfoClassic();
			}
		} else {
			produceOpsiModulesInfo();
		}

		Logging.info(this, " withMySQL " + withMySQL);
		Logging.info(this, " withUserRoles " + withUserRoles);
	}

	public boolean isWithLocalImaging() {
		retrieveOpsiModules();
		return withLocalImaging;
	}

	public boolean isWithUEFI() {
		return withUEFI;
	}

	public boolean isWithWAN() {
		return withWAN;
	}

	public boolean isWithLicenceManagement() {
		return withLicenceManagement;
	}

	private boolean applyUserSpecializedConfig() {
		if (applyUserSpecializedConfig != null) {
			return applyUserSpecializedConfig;
		}

		applyUserSpecializedConfig = withUserRoles && keyUserRegisterValue;
		Logging.info(this, "applyUserSpecializedConfig initialized, " + applyUserSpecializedConfig);

		return applyUserSpecializedConfig;
	}

	/**
	 * Test if sshcommand methods exists
	 *
	 * @param method name
	 * @return True if exists
	 */
	public boolean checkSSHCommandMethod(String method) {
		// method does not exist before opsi 3.4
		if (getMethodSignature(method) != NONE_LIST) {
			Logging.info(this, "checkSSHCommandMethod " + method + " exists");
			return true;
		}
		Logging.info(this, "checkSSHCommandMethod " + method + " does not exists");
		return false;
	}

	public List<Map<String, Object>> getOpsiconfdConfigHealth() {
		return retrieveHealthDetails("opsiconfd_config");
	}

	public List<Map<String, Object>> getDiskUsageHealth() {
		return retrieveHealthDetails("disk_usage");
	}

	public List<Map<String, Object>> getDepotHealth() {
		return retrieveHealthDetails("depotservers");
	}

	public List<Map<String, Object>> getSystemPackageHealth() {
		return retrieveHealthDetails("system_packages");
	}

	public List<Map<String, Object>> getProductOnDepotsHealth() {
		return retrieveHealthDetails("product_on_depots");
	}

	public List<Map<String, Object>> getProductOnClientsHealth() {
		return retrieveHealthDetails("product_on_clients");
	}

	public List<Map<String, Object>> getLicenseHealth() {
		return retrieveHealthDetails("opsi_licenses");
	}

	public List<Map<String, Object>> getDeprecatedCalls() {
		return retrieveHealthDetails("deprecated_calls");
	}

	private List<Map<String, Object>> retrieveHealthDetails(String checkId) {
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
			healthData = dataStub.checkHealth();
		}

		return healthData;
	}

	public boolean isHealthDataAlreadyLoaded() {
		return healthData != null;
	}

	public Map<String, Object> getDiagnosticData() {
		if (diagnosticData == null) {
			diagnosticData = dataStub.getDiagnosticData();
		}

		return diagnosticData;
	}

	/**
	 * Exec the python-opsi command "SSHCommand_getObjects"
	 *
	 * @return command objects
	 */
	public List<Map<String, Object>> retrieveCommandList() {
		Logging.info(this, "retrieveCommandList ");

		List<Map<String, Object>> sshCommands = exec.getListOfMaps(
				new OpsiMethodCall("SSHCommand_getObjects", new Object[] { /* callAttributes, callFilter */ }));
		Logging.debug(this, "retrieveCommandList commands " + sshCommands);
		return sshCommands;
	}

	/**
	 * Exec a python-opsi command
	 *
	 * @param method      name
	 * @param jsonObjects to do sth
	 * @return result true if everything is ok
	 */
	private boolean doActionSSHCommand(String method, List<Object> jsonObjects) {
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
		OpsiMethodCall omc = new OpsiMethodCall("SSHCommand_deleteObjects", new Object[] { jsonObjects });
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
		return doActionSSHCommand("SSHCommand_createObjects", jsonObjects);
	}

	/**
	 * Exec the python-opsi command "SSHCommand_updateObjects"
	 *
	 * @param jsonObjects to update
	 * @return result true if successfull
	 */
	public boolean updateSSHCommand(List<Object> jsonObjects) {
		return doActionSSHCommand("SSHCommand_updateObjects", jsonObjects);
	}
}
