
/**
 *   PersistenceController
 *   description: abstract methods for retrieving and setting data
 *
 *  copyright:     Copyright (c) 2000-2021
 *  organization: uib.de
 * @author  R. Roeder
 */

package de.uib.opsidatamodel;

// This file has dos format (use "dos2unix" command in terminal to transfere to unix)
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;

import de.uib.configed.ControlDash;
import de.uib.configed.configed;
import de.uib.configed.type.AdditionalQuery;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.DatedRowList;
import de.uib.configed.type.MetaConfig;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.SavedSearch;
import de.uib.configed.type.licences.LicenceContractEntry;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.configed.type.licences.LicenceStatisticsRow;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.Executioner;
//import de.uib.opsidatamodel.permission.*;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.logging;
import de.uib.utilities.observer.DataLoadingObservable;
import de.uib.utilities.observer.DataLoadingObserver;
import de.uib.utilities.observer.DataRefreshedObservable;
import de.uib.utilities.observer.DataRefreshedObserver;

public abstract class PersistenceController implements DataRefreshedObservable, DataLoadingObservable {
	public static final String CLIENT_GLOBAL_SEPARATOR = "/";

	public static final Set<String> KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT = new HashSet<>();
	{
		KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT.add("type");
		KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT.add("id");
	}

	// constants for building hw queries
	public final String hwInfo_CONFIG = "HARDWARE_CONFIG_";
	public final String hwInfo_DEVICE = "HARDWARE_DEVICE_";
	public final String hostIdField = ".hostId";
	public final String hardwareIdField = ".hardware_id";
	public final String lastseenColName = "lastseen";
	public final String lastseenVisibleColName = "HOST.last_scan_time";

	public static final String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT = "configed.productonclient_displayfields_localboot";
	public static final String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT = "configed.productonclient_displayfields_netboot";
	public static final String KEY_HOST_DISPLAYFIELDS = "configed.host_displayfields";
	public static final String KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation = "configed.license_inventory_extradisplayfields";

	public static final String KEY_SHOW_DASH_ON_PROGRAMSTART = ControlDash.CONFIG_KEY + ".show_dash_on_loaddata";
	public static final Boolean DEFAULTVALUE_SHOW_DASH_ON_PROGRAMSTART = false;
	public static final String KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT = ControlDash.CONFIG_KEY
			+ ".show_dash_for_showlicenses";
	public static final Boolean DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT = false;

	public static final String KEY_SEARCH_BY_SQL = "configed.search_by_sql";
	public static final Boolean DEFAULTVALUE_SEARCH_BY_SQL = true; // combines with question if mysql backend is working

	public static final String KEY_DISABLED_CLIENT_ACTIONS = "configed.host_actions_disabled";

	public static final String KEY_OPSICLIENTD_EXTRA_EVENTS = "configed.opsiclientd_events";
	public static final String OPSI_CLIENTD_EVENT_on_demand = "on_demand";
	public static final String OPSI_CLIENTD_EVENT_silent_install = "silent_install";

	public static final String KEY_PRODUCT_SORT_ALGORITHM = "product_sort_algorithm";

	public static final String KEY_CHOICES_FOR_WOL_DELAY = "wol_delays_sec";

	public static final String localImageRestoreProductKey = "opsi-local-image-restore";
	public static final String localImagesListPropertyKey = "imagefiles_list";
	public static final String localImageToRestorePropertyKey = "imagefile";

	public static final String CONFIG_DEPOT_ID = "clientconfig.depot.id";
	public static final String KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN = "opsiclientd.event_on_shutdown.active";
	public static final Boolean DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN = false;

	public static final String KEY_SSH_DEFAULTWINUSER = "configed.ssh.deploy-client-agent.default.user";
	public static final String KEY_SSH_DEFAULTWINUSER_defaultvalue = "Administrator";
	public static final String KEY_SSH_DEFAULTWINPW = "configed.ssh.deploy-client-agent.default.password";
	public static final String KEY_SSH_DEFAULTWINPW_defaultvalue = "";

	public static final String configedWORKBENCH_key = "configed.workbench.default";
	public static String configedWORKBENCH_defaultvalue = "/var/lib/opsi/workbench/";
	public static String packageServerDirectoryS = configedWORKBENCH_defaultvalue;

	public static final String configedGIVENDOMAINS_key = "configed.domains_given";

	// wan meta configuration
	public static final String WAN_PARTKEY = "wan_";
	public static final String WAN_CONFIGURED_PARTKEY = "wan_mode_on";
	public static final String NOT_WAN_CONFIGURED_PARTKEY = "wan_mode_off";
	protected Map<String, java.util.List<Object>> wanConfiguration;
	protected Map<String, java.util.List<Object>> notWanConfiguration;
	// keys for default wan configuration
	public static final String CONFIG_CLIENTD_EVENT_GUISTARTUP = "opsiclientd.event_gui_startup.active";
	public static final String CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN = "opsiclientd.event_gui_startup{user_logged_in}.active";
	public static final String CONFIG_CLIENTD_EVENT_NET_CONNECTION = "opsiclientd.event_net_connection.active";
	public static final String CONFIG_CLIENTD_EVENT_TIMER = "opsiclientd.event_timer.active";

	public static final String CONFIG_DHCPD_FILENAME = "clientconfig.dhcpd.filename";
	public static final String EFI_DHCPD_FILENAME = "linux/pxelinux.cfg/elilo.efi";
	// the current real value, but it is not necessary to configure it:
	// public static final String EFI_DHCPD_FILENAME_SHIM =
	// "linux/pxelinux.cfg/shimx64.efi.signed";
	// not more used:
	// public static final String EFI_DHCPD_FILENAME_X86 =
	// "linux/pxelinux.cfg/elilo-x86.efi";
	public static final String EFI_DHCPD_NOT = "";

	// public static final String HOST_KEY_UEFI_BOOT = "uefi";
	// public static final String ELILO_STRING = "elilo";
	// public static final String SHIM_STRING = "shim";
	public static final String EFI_STRING = "efi";

	public static final String KEY_USER_ROOT = "user"; // UserConfig.CONFIGKEY_STR_USER;

	public static final String KEY_USER_ROLE_ROOT = KEY_USER_ROOT + "." + "role";// UserConfig.
	public static final String ALL_USER_KEY_START = KEY_USER_ROOT + ".{}.";// UserConfig.

	public static final String KEY_USER_REGISTER = KEY_USER_ROOT + ".{}.register"; // boolean
	public static Boolean KEY_USER_REGISTER_VALUE = null;

	public static final String DEPOT_SELECTION_NODEPOTS = configed
			.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS");
	public static final String DEPOT_SELECTION_ALL = configed
			.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_ALL");
	public static final String DEPOT_SELECTION_ALL_WHERE_INSTALLED = configed
			.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_ALL_WHERE_INSTALLED");

	public static final java.util.List BOOLEAN_VALUES = new ArrayList<Boolean>();
	static {
		BOOLEAN_VALUES.add(true);
		BOOLEAN_VALUES.add(false);
	}

	public static TreeMap<String, String> PROPERTYCLASSES_SERVER;
	static {
		PROPERTYCLASSES_SERVER = new TreeMap<String, String>();
		PROPERTYCLASSES_SERVER.put("", "general configuration items");
		PROPERTYCLASSES_SERVER.put("clientconfig", "network configuration");
		PROPERTYCLASSES_SERVER.put(de.uib.opsidatamodel.modulelicense.LicensingInfoMap.CONFIG_KEY,
				"opsi module status display");
		PROPERTYCLASSES_SERVER.put(ControlDash.CONFIG_KEY, "dash configuration");
		PROPERTYCLASSES_SERVER.put(AdditionalQuery.CONFIG_KEY,
				"<html><p>sql queries can be defined here<br />- for purposes other than are fulfilled by the standard tables</p></html>");
		PROPERTYCLASSES_SERVER.put(MetaConfig.CONFIG_KEY, "default configuration for other properties");
		PROPERTYCLASSES_SERVER.put(SavedSearch.CONFIG_KEY,
				"<html><p>saved search configurations ,<br />do not edit here <br />- editing via the search form</p></html>");
		PROPERTYCLASSES_SERVER.put(RemoteControl.CONFIG_KEY,
				"<html><p>remote control calls,<br />i.e. calls to tools on the local computer<br />typically targeting at a selected client</p></html>");
		PROPERTYCLASSES_SERVER.put(OpsiHwAuditDeviceClass.CONFIG_KEY,
				"<html><p>configuration for hw overview table,<br />- best editing via the helper function<br />at the hw overview table!)</p></html>");
		PROPERTYCLASSES_SERVER.put("opsiclientd", "<html>entries for the opsiclientd.conf</html>");
		// PROPERTYCLASSES_SERVER.put( "opsi-local-image", "" );
		PROPERTYCLASSES_SERVER.put("opsi-script", "<html>parameters for opsi-script on a client</html>");
		PROPERTYCLASSES_SERVER.put("software-on-demand",
				"<html>software on demand configuration,<br />not client specific</html>");
		// PROPERTYCLASSES_SERVER.put( KEY_USER_ROOT, "<html>user privileges
		// configuration,<br />not client specific</html>");
		// PROPERTYCLASSES_SERVER.put( KEY_USER_ROLE_ROOT, "<html>user role
		// configuration,<br />not client specific</html>");
		PROPERTYCLASSES_SERVER.put(KEY_USER_ROOT,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.userPrivilegesConfiguration.ToolTip"));
		PROPERTYCLASSES_SERVER.put(KEY_USER_ROLE_ROOT,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.roleConfiguration.ToolTip"));
	}

	public static TreeMap<String, String> PROPERTYCLASSES_CLIENT;
	static {
		PROPERTYCLASSES_CLIENT = new TreeMap<String, String>();
		PROPERTYCLASSES_CLIENT.put("", "general configuration items");
		PROPERTYCLASSES_CLIENT.put("clientconfig", "network configuration");
		// PROPERTYCLASSES_CLIENT.put( SavedSearch.CONFIG_KEY, "<html></p>saved search
		// configurations ,<br />do not edit!</p></html>" );
		// PROPERTYCLASSES_CLIENT.put( RemoteControl.CONFIG_KEY, "<html><p>remote
		// control call,<br />not client specific</p></html>" );
		PROPERTYCLASSES_CLIENT.put("opsiclientd", "<html>entries for the opsiclientd.conf</html>");
		PROPERTYCLASSES_CLIENT.put("opsi-script", "<html>parameters for opsi-script on a client</html>");
		// PROPERTYCLASSES_CLIENT.put( "opsi-local-image", "" );
		PROPERTYCLASSES_CLIENT.put("software-on-demand",
				"<html>software on demand configuration,<br />not client specific</html>");
		// PROPERTYCLASSES_CLIENT.put( OpsiPermission.CONFIGKEY_STR_USER, "<html>user
		// privileges configuration,<br />not client specific</html>");
	}

	public static TreeMap<String, String> PROPERTY_EDITOPTIONS_CLIENT;
	static {
		PROPERTY_EDITOPTIONS_CLIENT = new TreeMap<String, String>();

	}

	public static TreeMap<String, String> PROPERTY_EDITOPTIONS_SERVER;
	static {
		PROPERTY_EDITOPTIONS_SERVER = new TreeMap<String, String>();

	}

	public static Set<String> CONFIG_KEYSTARTERS_NOT_FOR_CLIENTS;
	static {
		CONFIG_KEYSTARTERS_NOT_FOR_CLIENTS = new HashSet<String>(PROPERTYCLASSES_SERVER.keySet());
		CONFIG_KEYSTARTERS_NOT_FOR_CLIENTS.removeAll(PROPERTYCLASSES_CLIENT.keySet());
		CONFIG_KEYSTARTERS_NOT_FOR_CLIENTS.add(KEY_PRODUCT_SORT_ALGORITHM);
		CONFIG_KEYSTARTERS_NOT_FOR_CLIENTS.add("configed");
	}

	/**
	 * This creation method constructs a new Controller instance and lets a
	 * static variable point to it When next time we need a Controller we can
	 * choose if we take the already constructed one - returned from the static
	 * method getPersistenceController - or construct a new one public static
	 * PersistenceController getNewPersistenceController (String server, String
	 * user, String password) { return null; } public static
	 * PersistenceController getPersistenceController () { return null; }
	 */

	public Executioner exec;

	protected final Map<String, Executioner> execs = new HashMap<String, Executioner>();

	public abstract void userConfigurationRequestReload();

	// public abstract void checkFragileUserRegistration();

	public abstract void addRoleConfig(String name, String rolename);

	public abstract void addUserConfig(String name, String rolename);

	public abstract void checkConfiguration();

	// protected abstract boolean sourceAccept();

	public abstract boolean canCallMySQL();

	/* error handling convenience methods */
	// public abstract List getErrorList ();

	// public abstract void clearErrorList ();

	/* ============================ */
	public abstract Executioner retrieveWorkingExec(String depot);

	protected abstract boolean makeConnection();

	public abstract String getOpsiCACert();

	/* connection state handling */
	public abstract ConnectionState getConnectionState();

	public abstract void setConnectionState(ConnectionState state);

	// public abstract void checkReadOnly();

	public abstract LinkedHashMap<String, Map<String, Object>> getDepotPropertiesForPermittedDepots();

	public boolean hasUserPrivilegesData() {
		// user has roles
		// a role has privileges
		// a privilege is implemented by conditions referring to targets
		return false;
	}

	public abstract void checkPermissions();

	public abstract boolean isGlobalReadOnly();

	public abstract boolean isServerFullPermission();

	public abstract boolean isCreateClientPermission();

	public abstract boolean isDepotsFullPermission();

	public abstract boolean getDepotPermission(String depotId);

	public abstract boolean accessToHostgroupsOnlyIfExplicitlyStated();

	public abstract Set<String> getHostgroupsPermitted();

	public abstract boolean getHostgroupPermission(String hostgroupId);

	public abstract boolean isProductgroupsFullPermission();

	public abstract boolean getProductgroupPermission(String productgroupId);

	/* ============================ */
	/* data retrieving and setting */

	public void syncTables() {
	}

	public void cleanUpAuditSoftware() {
		logging.error(this, "cleanUpAuditSoftware not implemented");
	}

	// ---------------------------------------------------------------
	// implementation of observer patterns
	// offer observing of data refreshed announcements
	protected java.util.List<DataRefreshedObserver> dataRefreshedObservers;

	public void registerDataRefreshedObserver(DataRefreshedObserver ob) {
		if (dataRefreshedObservers == null)
			dataRefreshedObservers = new ArrayList<DataRefreshedObserver>();
		dataRefreshedObservers.add(ob);
	}

	public void unregisterDataRefreshedObserver(DataRefreshedObserver ob) {
		if (dataRefreshedObservers != null)
			dataRefreshedObservers.remove(ob);
	}

	public void notifyDataRefreshedObservers(Object mesg) {
		if (dataRefreshedObservers == null)
			return;

		for (DataRefreshedObserver ob : dataRefreshedObservers) {
			ob.gotNotification(mesg);
		}
	}

	// offer observing of data loading
	protected java.util.List<DataLoadingObserver> dataLoadingObservers;

	public void registerDataLoadingObserver(DataLoadingObserver ob) {
		if (dataLoadingObservers == null)
			dataLoadingObservers = new ArrayList<DataLoadingObserver>();
		dataLoadingObservers.add(ob);
	}

	public void unregisterDataLoadingObserver(DataLoadingObserver ob) {
		if (dataLoadingObservers != null)
			dataLoadingObservers.remove(ob);
	}

	public void notifyDataLoadingObservers(Object mesg) {
		if (dataLoadingObservers == null)
			return;

		for (DataLoadingObserver ob : dataLoadingObservers) {
			ob.gotNotification(mesg);
		}
	}

	// ---------------------------------------------------------------

	/* server related */
	public abstract boolean installPackage(String filename);

	public abstract boolean setRights(String path);

	/* relating to the PC list */

	public abstract java.util.List<Map<java.lang.String, java.lang.Object>> HOST_read();

	public abstract HostInfoCollections getHostInfoCollections();

	public abstract java.util.List<String> getClientsWithOtherProductVersion(String productId, String productVersion,
			String packageVersion, boolean includeFailedInstallations);

	// public abstract String[] getClientsWithFailed();

	// public abstract Map<String, String> getProductVersion(String productId,
	// String depotID);

	public abstract boolean areDepotsSynchronous(Set depots);

	public abstract Boolean isInstallByShutdownConfigured(String host);

	public abstract Boolean isWanConfigured(String host);

	public abstract Boolean isUefiConfigured(String host);

	public abstract boolean createClients(Vector<Vector<Object>> clients);

	public abstract boolean createClient(String hostname, String domainname, String depotId, String description,
			String inventorynumber, String notes, String ipaddress, String macaddress, boolean shutdownInstall,
			boolean uefiBoot, boolean wan, String group, String productNetboot, String productLocalboot);

	public abstract boolean configureInstallByShutdown(String clientId, boolean shutdownInstal);

	public abstract boolean configureUefiBoot(String clientId, boolean uefiBoot);

	public abstract boolean setWANConfigs(String clientId, boolean wan);

	public abstract boolean renameClient(String hostname, String newHostname);

	public abstract void deleteClient(String hostId);

	public abstract void deleteClients(String[] hostIds);

	public abstract java.util.List<String> deletePackageCaches(String[] hostIds);

	// public abstract void wakeOnLan (String hostId);

	public abstract java.util.List<String> wakeOnLan(String[] hostIds);

	public abstract java.util.List<String> wakeOnLan(java.util.Set<String> hostIds,
			Map<String, java.util.List<String>> hostSeparationByDepot, Map<String, Executioner> execsByDepot);

	public abstract java.util.List<String> fireOpsiclientdEventOnClients(String event, String[] clientIds);

	public abstract java.util.List<String> showPopupOnClients(String message, String[] clientIds, Float seconds);

	public abstract java.util.List<String> shutdownClients(String[] clientIds);

	public abstract java.util.List<String> rebootClients(String[] clientIds);

	public abstract Map<String, Object> reachableInfo(String[] clientIds);

	public abstract Map<String, Integer> getInstalledOsOverview();

	public abstract Map<String, Object> getLicensingInfo();

	public abstract List<Map<String, Object>> getModules();

	public abstract Map<String, String> sessionInfo(String[] clientIds);

	// executes all updates collected by setHostDescription ...
	public abstract void updateHosts();

	public abstract void setHostDescription(String hostId, String description);

	public abstract void setClientInventoryNumber(String hostId, String inventoryNumber);

	public abstract void setClientOneTimePassword(String hostId, String oneTimePassword);

	public abstract void setHostNotes(String hostId, String notes);

	public abstract String getMacAddress(String hostId);

	public abstract void setMacAddress(String hostId, String address);

	public abstract void setIpAddress(String hostId, String address);

	// group handling
	public abstract Map<String, Map<String, String>> getProductGroups();

	public abstract void productGroupsRequestRefresh();

	public abstract Map<String, Map<String, String>> getHostGroups();

	public abstract void hostGroupsRequestRefresh();

	// public abstract void clientsWithFailedRequestRefresh();

	public abstract void fObject2GroupsRequestRefresh();

	public abstract Map<String, Set<String>> getFObject2Groups();

	public abstract void fGroup2MembersRequestRefresh();

	public abstract void fProductGroup2MembersRequestRefresh();

	public abstract Map<String, Set<String>> getFGroup2Members();

	public abstract Map<String, Set<String>> getFProductGroup2Members();

	public abstract boolean addHosts2Group(java.util.List<String> objectIds, String groupId);

	public abstract boolean addObject2Group(String objectId, String groupId);

	public abstract boolean removeObject2Group(String objectId, String groupId);

	public abstract boolean removeHostGroupElements(java.util.List<Object2GroupEntry> entries);

	public abstract boolean addGroup(StringValuedRelationElement newgroup);

	public abstract boolean deleteGroup(String groupId);

	public abstract boolean updateGroup(String groupId, Map<String, String> updateInfo);

	public abstract boolean setProductGroup(String groupId, String description, Set<String> products);

	public abstract List<String> getHostGroupIds();

	// public abstract void populateHostGroupFromSearch(String savedSearch, String
	// groupName);

	public abstract Map<String, java.util.List<String>> getHostSeparationByDepots(String[] hostIds);

	// deprecated
	// public abstract boolean writeGroup (String groupname, String[] groupmembers);

	// public abstract String getPcInfo( String hostId );

	// public abstract boolean existsEntry (String pcname);

	/* software info */
	public abstract ArrayList<String> getSoftwareList();

	public abstract TreeMap<String, Integer> getSoftware2Number();

	public abstract Map getSoftwareInfo(String clientId);

	public abstract void fillClient2Software(java.util.List<String> clients);

	public abstract void softwareAuditOnClientsRequestRefresh();

	// public abstract List<Map<String, Object>> getSoftwareAuditOnClients();

	public abstract Map<String, java.util.List<SWAuditClientEntry>> getClient2Software();

	public abstract DatedRowList getSoftwareAudit(String clientId);

	public abstract String getLastSoftwareAuditModification(String clientId);

	public abstract Map<String, Map/* <String, String> */> retrieveSoftwareAuditData(String clientId);

	/* hardware info */
	public abstract List<Map<String, Object>> getOpsiHWAuditConf();

	public abstract List<Map<String, Object>> getOpsiHWAuditConf(String locale);

	public abstract List<String> getAllHwClassNames();

	public abstract Map<String, OpsiHwAuditDeviceClass> getHwAuditDeviceClasses();

	public abstract void hwAuditConfRequestRefresh();

	public abstract Object getHardwareInfo(String clientId, boolean asHTMLtable);

	public abstract void auditHardwareOnHostRequestRefresh();

	public abstract List<Map<String, Object>> getHardwareOnClient();

	/* multiclient hwinfo */

	public abstract Vector<String> getHwInfoClassNames();

	public abstract Vector<String> getHostColumnNames();

	public abstract Vector<String> getClient2HwRowsColumnNames();

	public abstract Vector<String> getClient2HwRowsJavaclassNames();

	public abstract void client2HwRowsRequestRefresh();

	public abstract Map<String, Map<String, Object>> getClient2HwRows(String[] hosts);

	public abstract boolean saveHwColumnConfig(Map<String, Map<String, Boolean>> updateItems);

	/* log files */
	public abstract String[] getLogtypes();

	public abstract Map<String, String> getEmptyLogfiles();

	public abstract Map<String, String> getLogfiles(String clientId, String logtype);

	public abstract Map<String, String> getLogfiles(String clientId);

	/* list of boot images */
	// public abstract Vector getInstallImages();

	// product related

	// public abstract void depotProductPropertiesRequestRefresh();

	public abstract void depotChange();

	public abstract void productDataRequestRefresh();

	/* listings of all products and their properties */

	public abstract List<String> getAllProductNames(String depotId);

	public abstract List<String> getProvidedLocalbootProducts(String depotId);

	public abstract List<String> getProvidedNetbootProducts(String depotId);

	public abstract List<String> getAllLocalbootProductNames(String depotId);

	public abstract List<String> getAllLocalbootProductNames();

	// public abstract void localbootProductNamesRequestRefresh();

	public abstract List<String> getAllDepotsWithIdenticalProductStock(String depot);

	// deprecated
	public abstract List<String> getAllNetbootProductNames();

	public abstract List<String> getAllNetbootProductNames(String depotId);

	public abstract Vector<String> getWinProducts(String depotId, String depotProductDirectory);

	// public abstract void retrieveProductsAllDepots();

	public abstract void retrieveProducts();

	public abstract Map<String, java.util.List<String>> getPossibleActions(String depotId);

	public abstract Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos();

	public abstract Object2Product2VersionList getDepot2LocalbootProducts();

	public abstract Object2Product2VersionList getDepot2NetbootProducts();

	// public abstract void retrieveProductGlobalInfos();

	public abstract Map<String, Map<String, Object>> getProductGlobalInfos(String depotId); // (productId -> (infoKey ->
																							// info))

	public abstract Vector<Vector<Object>> getProductRows();

	public abstract Map<String, Map<String, java.util.List<String>>> getProduct2VersionInfo2Depots();

	public abstract TreeSet<String> getProductIds();

	public abstract Map<String, Map<String, String>> getProductDefaultStates();

	// public abstract List getProductDependencies ( String productname);
	public abstract Map<String, List<Map<String, String>>> getProductDependencies(String depotId);

	public abstract void retrieveProductDependencies();

	public abstract Set<String> extendToDependentProducts(final Set<String> startProductSet, final String depot);

	// intersection of the values of the clients
	public abstract List<String> getCommonProductPropertyValues(java.util.List<String> clients, String product,
			String property);

	public abstract void productPropertyDefinitionsRequestRefresh();

	public abstract void retrieveProductPropertyDefinitions();

	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getProductPropertyOptionsMap(String productId);

	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getProductPropertyOptionsMap(String depotId,
			String productId);

	// public abstract Map getProductPropertyValuesMap (String productname);

	// public abstract Map getProductPropertyDescriptionsMap (String productname);

	// public abstract Map getProductPropertyDefaultsMap (String productname);

	public abstract String getProductTitle(String product);

	public abstract String getProductInfo(String product);

	public abstract String getProductHint(String product);

	public abstract String getProductVersion(String product);

	public abstract String getProductPackageVersion(String product);

	public abstract String getProductLockedInfo(String product);

	public abstract String getProductTimestamp(String product);

	/* PC specific listings of products and their states and updatings */

	// public abstract List[] getClientsLocalbootProductNames(String[] clientIds);

	// public abstract List[] getClientsNetbootProductNames(String[] clientIds);

	// methods requires java 8:
	// public abstract Map getProductStatesNOMSortedByClientId();
	// public abstract Map getProductStatesNOMSorted(String sortKey);

	// public abstract Map getMapOfProductStates (String clientId);

	// public abstract Map getMapOfProductActions (String clientId);

	public abstract Map<String, java.util.List<Map<String, String>>> getMapOfProductStatesAndActions(
			String[] clientIds);

	// public abstract Map getMapOfLocalbootProductStatesAndActions (String[]
	// clientIds,
	// Map currentMap);

	public abstract Map<String, java.util.List<Map<String, String>>> getMapOfLocalbootProductStatesAndActions(
			String[] clientIds);

	public abstract Map<String, java.util.List<Map<String, String>>> getMapOfNetbootProductStatesAndActions(
			String[] clientIds);

	// collecting update items
	public abstract boolean updateProductOnClient(String pcname, String productname, int producttype, Map updateValues);

	// send the collected items
	public abstract boolean updateProductOnClients();

	// update for the whole set of clients
	public abstract boolean updateProductOnClients(Set<String> clients, String productName, int productType,
			Map<String, String> changedValues);

	public abstract boolean resetLocalbootProducts(String[] selectedClients, boolean withDependencies);

	public abstract Map<String, String> getProductPreRequirements(String depotId, String productname);

	public abstract Map<String, String> getProductRequirements(String depotId, String productname);

	public abstract Map<String, String> getProductPostRequirements(String depotId, String productname);

	public abstract Map<String, String> getProductDeinstallRequirements(String depotId, String productname);

	/* pc and product specific */
	// public abstract void retrieveProductproperties (List clientNames);
	public abstract void productpropertiesRequestRefresh();

	public abstract void retrieveProductproperties(List<String> clientNames);

	public abstract Boolean hasClientSpecificProperties(String productname);

	public abstract Map<String, Boolean> getProductHavingClientSpecificProperties();

	public abstract Map<String, Map<String, ConfigName2ConfigValue>> getDepot2product2properties();

	public abstract Map<String, ConfigName2ConfigValue> getDefaultProductProperties(String depotId);

	public abstract void retrieveDepotProductProperties();

	public abstract Map<String, Object> getProductproperties(String pcname, String productname);

	// public abstract void setProductproperties(String pcname, String productname,
	// Map properties,
	// java.util.List updateCollection, java.util.List deleteCollection);
	public abstract void setProductproperties(String pcname, String productname, Map properties);

	// public abstract void setProductproperties( java.util.List updateCollection,
	// java.util.List deleteCollection );
	public abstract void setProductproperties();

	public abstract void setCommonProductPropertyValue(Set<String> clientNames, String productName, String propertyName,
			java.util.List<String> values);

	/* information about the service */
	// public abstract void mapOfMethodSignaturesRequestRefresh(); we dont need
	// update this

	public abstract List<String> getMethodSignature(String methodname);

	public abstract String getBackendInfos();

	/* network and additional settings, for network objects */

	// public abstract java.util.List getServers();

	// public abstract Map getNetworkConfiguration (String objectId);

	public abstract void hostConfigsRequestRefresh();

	// public abstract void hostConfigsRequestRefresh(String[] clients);

	// public abstract void hostConfigsCheck(String[] clients);
	// retrieve host configs if not existing

	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getConfigOptions();

	public abstract Map<String, java.util.List<Object>> getConfigDefaultValues();

	public abstract Boolean getGlobalBooleanConfigValue(String key, Boolean defaultVal);

	public abstract void setGlobalBooleanConfigValue(String key, Boolean val, String description);

	protected abstract boolean setHostBooleanConfigValue(String key, String hostName, boolean val);

	// protected abstract boolean getHostBooleanConfigValue( String key, String
	// hostName, Boolean defaultVal );

	public abstract Map<String, Map<String, Object>> getConfigs();

	public abstract Map<String, Object> getConfig(String objectId);

	// public abstract Map getAdditionalConfiguration (String objectId);

	// public abstract void setNetworkConfiguration (String objectId, Map settings);

	public abstract void setHostValues(Map settings);

	public abstract void setAdditionalConfiguration(String objectId, ConfigName2ConfigValue settings);

	public abstract void setAdditionalConfiguration(boolean determineConfigOptions);

	public abstract void setConfig(Map<String, java.util.List<Object>> settings);

	public abstract void setConfig();

	// public abstract void setConfig(boolean restrictToMissing);

	public abstract void configOptionsRequestRefresh();

	public abstract Map<String, RemoteControl> getRemoteControls();

	public abstract SavedSearches getSavedSearches();

	public abstract void deleteSavedSearch(String name);

	public abstract void saveSearch(SavedSearch ob);

	public abstract java.util.List<String> getServerConfigStrings(String key);

	public abstract void requestReloadOpsiDefaultDomain();

	public abstract String getOpsiDefaultDomain();

	public abstract Vector<String> getDomains();

	public abstract void writeDomains(java.util.ArrayList<Object> domains);

	public abstract void setDepot(String depotId);

	public abstract String getDepot();

	// public abstract String getDepot();

	public abstract Map<String, SWAuditEntry> getInstalledSoftwareInformation();

	public abstract Map<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing();

	public abstract Map<String, Map<String, String>> getInstalledSoftwareName2SWinfo();

	public abstract TreeMap<String, Set<String>> getName2SWIdents();

	public abstract void installedSoftwareInformationRequestRefresh();

	public abstract String getSWident(Integer i);

	/* licences */
	public abstract Map<String, LicenceContractEntry> getLicenceContracts();

	public abstract TreeMap<String, TreeSet<String>> getLicenceContractsExpired();
	// date in sql time format, contrad ID

	public abstract TreeMap<String, TreeSet<String>> getLicenceContractsToNotify();
	// date in sql time format, contrad ID

	// returns the ID of the edited data record
	public abstract String editLicenceContract(String licenseContractId, String partner, String conclusionDate,
			String notificationDate, String expirationDate, String notes);

	public abstract boolean deleteLicenceContract(String licenseContractId);

	public abstract Map<String, LicencepoolEntry> getLicencepools();

	// returns the ID of the edited data record
	public abstract String editLicencePool(String licensePoolId, String description);

	public abstract boolean deleteLicencePool(String licensePoolId);

	public abstract Map<String, LicenceEntry> getSoftwareLicences();

	// returns the ID of the edited data record
	public abstract String editSoftwareLicence(String softwareLicenseId, String licenceContractId, String licenceType,
			String maxInstallations, String boundToHost, String expirationDate);

	public abstract boolean deleteSoftwareLicence(String softwareLicenseId);

	public abstract Map<String, Map> getRelationsSoftwareL2LPool();

	// returns the ID of the edited data record
	public abstract String editRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId,
			String licenseKey);

	public abstract boolean deleteRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId);

	public abstract Map<String, Map<String, String>> getRelationsProductId2LPool();

	// returns an ID of the edited data record
	public abstract String editRelationProductId2LPool(String productId, String licensePoolId);

	public abstract boolean deleteRelationProductId2LPool(String productId, String licensePoolId);

	public abstract void retrieveRelationsAuditSoftwareToLicencePools();

	public abstract void relations_windowsSoftwareId2LPool_requestRefresh();

	public abstract void relations_auditSoftwareToLicencePools_requestRefresh();

	public abstract List<String> getSoftwareListByLicencePool(String licencePoolId);

	public abstract List<String> getUnknownSoftwareListForLicencePool(String licencePoolId);

	public abstract TreeSet<Object> getSoftwareWithoutAssociatedLicencePool();

	public abstract Map<String, String> getFSoftware2LicencePool();

	public abstract String getFSoftware2LicencePool(String softwareIdent);

	public abstract void setFSoftware2LicencePool(String softwareIdent, String licencePoolId);

	// public abstract List getLicencePool2WindowsSoftwareIDs(String licensePoolId);

	// public abstract Map<String, Map> getRelationsWindowsSoftwareId2LPool();

	public abstract boolean removeAssociations(String licensePoolId, List<String> softwareIds);

	public abstract boolean setWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign);

	public abstract boolean addWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign);

	/*
	 * returns the ID of the edited data record
	 * public abstract String editRelationWindowsSoftwareId2LPool(
	 * String windowsSoftwareId,
	 * String licensePoolId {
	 * 
	 * );
	 * 
	 */

	public abstract String editPool2AuditSoftware(String softwareID, String licensePoolID_old,
			String licensePoolID_new);

	public abstract Map<String, LicenceStatisticsRow> getLicenceStatistics();

	public abstract void licencesUsageRequestRefresh();

	public abstract Map<String, java.util.List<LicenceUsageEntry>> getFClient2LicencesUsageList();

	public abstract Map<String, LicenceUsageEntry> getLicencesUsage();

	public abstract String getLicenceUsage(String hostId, String licensePoolId);

	public abstract String editLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId,
			String licenseKey, String notes);

	public abstract boolean deleteLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId);

	// collecting deletion items
	public abstract void addDeletionLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId);

	// send the collected items
	public abstract boolean executeCollectedDeletionsLicenceUsage();

	public abstract void reconciliationInfoRequestRefresh();

	public abstract Map<String, Map<String, Object>> getLicencesReconciliation();

	public abstract String editLicencesReconciliation(String clientId, String licensePoolId);

	public abstract boolean deleteLicencesReconciliation(String clientId, String licensePoolId);

	// configurations and algorithms
	public abstract LinkedHashMap<String, Boolean> getProductOnClients_displayFieldsLocalbootProducts();

	public abstract LinkedHashMap<String, Boolean> getProductOnClients_displayFieldsNetbootProducts();

	public abstract LinkedHashMap<String, Boolean> getHost_displayFields();

	// menu configuration
	public abstract java.util.List<String> getDisabledClientMenuEntries();

	public abstract java.util.List<String> getOpsiclientdExtraEvents();

	// table sources
	// public abstract class AllProductsTableSource implements
	// de.uib.utilities.table.provider.TableSource;

	// opsi module information
	public static int CLIENT_COUNT_WARNING_LIMIT = 10;
	public static int CLIENT_COUNT_TOLERANCE_LIMIT = 50;

	public abstract void opsiInformationRequestRefresh();

	public abstract Date getOpsiExpiresDate();

	public abstract void retrieveOpsiModules();

	public abstract void showLicInfoWarnings();

	public abstract Map<String, Object> getOpsiModulesInfos();

	public abstract String getOpsiLicensingInfoVersion();

	public abstract void opsiLicensingInfoRequestRefresh();

	public abstract JSONObject getOpsiLicensingInfo();

	public abstract String getCustomer();

	public abstract boolean isWithLocalImaging();

	// public abstract boolean isWithScalability1();

	public abstract boolean isWithLicenceManagement();

	public abstract boolean isWithMySQL();

	public abstract boolean isWithUEFI();

	public abstract boolean isWithWAN();

	public abstract boolean isWithLinuxAgent();

	public abstract boolean isWithUserRoles();

	public abstract boolean applyUserSpecializedConfig();

	public abstract String getOpsiVersion();

	public abstract boolean handleVersionOlderThan(String minRequiredVersion);

	public abstract java.util.List<Map<java.lang.String, java.lang.Object>> retrieveCommandList();

	public abstract boolean doActionSSHCommand(String method, List<Object> jsonObjects);

	public abstract boolean createSSHCommand(List<Object> jsonObjects);

	public abstract boolean updateSSHCommand(List<Object> jsonObjects);

	public abstract boolean deleteSSHCommand(List<String> jsonObjects);

	public abstract boolean checkSSHCommandMethod(String method);

	// json generating

	public static Map<String, Object> createNOMitem(String type) {
		Map<String, Object> item = new HashMap<String, Object>();
		item.put("type", type);

		return item;
	}

	public static ConfigOption createConfig(ConfigOption.TYPE type, String key, String description, boolean editable,
			boolean multiValue, java.util.List<Object> defaultValues, java.util.List<Object> possibleValues) {
		Map<String, Object> item = createNOMitem(type.toString());

		item.put("ident", key.toLowerCase());
		item.put("description", description);
		item.put("editable", editable);
		item.put("multiValue", multiValue);

		item.put("defaultValues", defaultValues);

		item.put("possibleValues", possibleValues);

		return new ConfigOption(item);
	}

	public static Map<String, Object> createJSONConfig(ConfigOption.TYPE type, String key, String description,
			boolean editable, boolean multiValue, java.util.List<Object> defaultValues,
			java.util.List<Object> possibleValues) {

		Map<String, Object> item = createNOMitem(type.toString());

		item.put("id", key.toLowerCase());
		item.put("description", description);
		item.put("editable", editable);
		item.put("multiValue", multiValue);

		item.put("defaultValues", Executioner.jsonArray(defaultValues));

		item.put("possibleValues", Executioner.jsonArray(possibleValues));

		return item;
	}

	public static ConfigOption createBoolConfig(String key, Boolean value, String description) {
		java.util.List<Object> defaultValues = new ArrayList<Object>();
		defaultValues.add(value);

		java.util.List<Object> possibleValues = new ArrayList<Object>();
		possibleValues.add(true);
		possibleValues.add(false);

		return createConfig(ConfigOption.TYPE.BoolConfig, key, description, false, false, defaultValues,
				possibleValues);
	}

	public static Map<String, Object> createJSONBoolConfig(String key, Boolean value, String description) {
		java.util.List<Object> defaultValues = new ArrayList<Object>();
		defaultValues.add(value);

		java.util.List<Object> possibleValues = new ArrayList<Object>();
		possibleValues.add(true);
		possibleValues.add(false);

		return createJSONConfig(ConfigOption.TYPE.BoolConfig, key, description, false, false, defaultValues,
				possibleValues);
	}

}
