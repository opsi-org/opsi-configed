
/**
 *   PersistenceController
 *   description: abstract methods for retrieving and setting data
 *
 *  copyright:     Copyright (c) 2000-2021
 *  organization: uib.de
 * @author  R. Roeder
 */

package de.uib.opsidatamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONObject;

import de.uib.configed.Configed;
import de.uib.configed.type.AbstractMetaConfig;
import de.uib.configed.type.AdditionalQuery;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.DatedRowList;
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
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.ConnectionState;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.DataLoadingObservable;
import de.uib.utilities.observer.DataLoadingObserver;
import de.uib.utilities.observer.DataRefreshedObservable;
import de.uib.utilities.observer.DataRefreshedObserver;

public abstract class AbstractPersistenceController implements DataRefreshedObservable, DataLoadingObservable {
	public static final String CLIENT_GLOBAL_SEPARATOR = "/";

	public static final Set<String> KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT = new HashSet<>();
	static {
		KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT.add("type");
		KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT.add("id");
	}

	// constants for building hw queries
	public static final String HW_INFO_CONFIG = "HARDWARE_CONFIG_";
	public static final String HW_INFO_DEVICE = "HARDWARE_DEVICE_";
	public static final String HOST_ID_FIELD = ".hostId";
	public static final String HARDWARE_ID_FIELD = ".hardware_id";
	public static final String LAST_SEEN_COL_NAME = "lastseen";
	public static final String LAST_SEEN_VISIBLE_COL_NAME = "HOST.last_scan_time";

	public static final String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT = "configed.productonclient_displayfields_localboot";
	public static final String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT = "configed.productonclient_displayfields_netboot";
	public static final String KEY_HOST_DISPLAYFIELDS = "configed.host_displayfields";
	public static final String KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION = "configed.license_inventory_extradisplayfields";

	public static final String CONTROL_DASH_CONFIG_KEY = "configed.dash_config";

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

	public static final String KEY_CHOICES_FOR_WOL_DELAY = "wol_delays_sec";

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

	public static final NavigableMap<String, String> PROPERTY_CLASSES_SERVER = new TreeMap<>();
	static {
		PROPERTY_CLASSES_SERVER.put("", "general configuration items");
		PROPERTY_CLASSES_SERVER.put("clientconfig", "network configuration");
		PROPERTY_CLASSES_SERVER.put(de.uib.opsidatamodel.modulelicense.LicensingInfoMap.CONFIG_KEY,
				"opsi module status display");
		PROPERTY_CLASSES_SERVER.put(CONTROL_DASH_CONFIG_KEY, "dash configuration");
		PROPERTY_CLASSES_SERVER.put(AdditionalQuery.CONFIG_KEY,
				"<html><p>sql queries can be defined here<br />- for purposes other than are fulfilled by the standard tables</p></html>");
		PROPERTY_CLASSES_SERVER.put(AbstractMetaConfig.CONFIG_KEY, "default configuration for other properties");
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
	public static final int CLIENT_COUNT_WARNING_LIMIT = 10;
	public static final int CLIENT_COUNT_TOLERANCE_LIMIT = 50;

	// wan meta configuration
	public static final String WAN_PARTKEY = "wan_";
	public static final String WAN_CONFIGURED_PARTKEY = "wan_mode_on";
	public static final String NOT_WAN_CONFIGURED_PARTKEY = "wan_mode_off";

	protected Map<String, List<Object>> wanConfiguration;
	protected Map<String, List<Object>> notWanConfiguration;

	/**
	 * This creation method constructs a new Controller instance and lets a
	 * static variable point to it When next time we need a Controller we can
	 * choose if we take the already constructed one - returned from the static
	 * method getPersistenceController - or construct a new one public static
	 * PersistenceController getNewPersistenceController (String server, String
	 * user, String password) { return null; } public static
	 * PersistenceController getPersistenceController () { return null; }
	 */

	protected List<DataRefreshedObserver> dataRefreshedObservers;

	public AbstractExecutioner exec;

	protected final Map<String, AbstractExecutioner> execs = new HashMap<>();

	// offer observing of data loading
	protected List<DataLoadingObserver> dataLoadingObservers;

	public abstract void userConfigurationRequestReload();

	public abstract void addRoleConfig(String name, String rolename);

	public abstract void addUserConfig(String name, String rolename);

	public abstract void checkConfiguration();

	public abstract boolean canCallMySQL();

	/* error handling convenience methods */

	/* ============================ */
	public abstract AbstractExecutioner retrieveWorkingExec(String depot);

	protected abstract boolean makeConnection();

	public abstract String getOpsiCACert();

	/* connection state handling */
	public abstract ConnectionState getConnectionState();

	public abstract void setConnectionState(ConnectionState state);

	public abstract Map<String, Map<String, Object>> getDepotPropertiesForPermittedDepots();

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

	public abstract boolean hasDepotPermission(String depotId);

	public abstract boolean accessToHostgroupsOnlyIfExplicitlyStated();

	public abstract Set<String> getHostgroupsPermitted();

	public abstract boolean hasHostgroupPermission(String hostgroupId);

	public abstract boolean isProductgroupsFullPermission();

	public abstract boolean hasProductgroupPermission(String productgroupId);

	/* ============================ */
	/* data retrieving and setting */

	public void syncTables() {
	}

	public void cleanUpAuditSoftware() {
		Logging.error(this, "cleanUpAuditSoftware not implemented");
	}

	// ---------------------------------------------------------------
	// implementation of observer patterns
	// offer observing of data refreshed announcements

	@Override
	public void registerDataRefreshedObserver(DataRefreshedObserver ob) {
		if (dataRefreshedObservers == null) {
			dataRefreshedObservers = new ArrayList<>();
		}
		dataRefreshedObservers.add(ob);
	}

	@Override
	public void unregisterDataRefreshedObserver(DataRefreshedObserver ob) {
		if (dataRefreshedObservers != null) {
			dataRefreshedObservers.remove(ob);
		}
	}

	@Override
	public void notifyDataRefreshedObservers(Object mesg) {
		if (dataRefreshedObservers == null) {
			return;
		}

		for (DataRefreshedObserver ob : dataRefreshedObservers) {
			ob.gotNotification(mesg);
		}
	}

	@Override
	public void registerDataLoadingObserver(DataLoadingObserver ob) {
		if (dataLoadingObservers == null) {
			dataLoadingObservers = new ArrayList<>();
		}
		dataLoadingObservers.add(ob);
	}

	@Override
	public void unregisterDataLoadingObserver(DataLoadingObserver ob) {
		if (dataLoadingObservers != null) {
			dataLoadingObservers.remove(ob);
		}
	}

	@Override
	public void notifyDataLoadingObservers(Object mesg) {
		if (dataLoadingObservers == null) {
			return;
		}

		for (DataLoadingObserver ob : dataLoadingObservers) {
			ob.gotNotification(mesg);
		}
	}

	// ---------------------------------------------------------------

	/* server related */
	public abstract boolean installPackage(String filename);

	public abstract boolean setRights(String path);

	/* relating to the PC list */

	public abstract List<Map<java.lang.String, java.lang.Object>> hostRead();

	public abstract HostInfoCollections getHostInfoCollections();

	public abstract List<String> getClientsWithOtherProductVersion(String productId, String productVersion,
			String packageVersion, boolean includeFailedInstallations);

	public abstract boolean areDepotsSynchronous(Set<String> depots);

	public abstract Boolean isInstallByShutdownConfigured(String host);

	public abstract Boolean isWanConfigured(String host);

	public abstract Boolean isUefiConfigured(String host);

	public abstract boolean createClients(List<List<Object>> clients);

	public abstract boolean createClient(String hostname, String domainname, String depotId, String description,
			String inventorynumber, String notes, String ipaddress, String systemUUID, String macaddress,
			boolean shutdownInstall, boolean uefiBoot, boolean wan, String group, String productNetboot,
			String productLocalboot);

	public abstract boolean configureInstallByShutdown(String clientId, boolean shutdownInstal);

	public abstract boolean configureUefiBoot(String clientId, boolean uefiBoot);

	public abstract boolean setWANConfigs(String clientId, boolean wan);

	public abstract boolean renameClient(String hostname, String newHostname);

	public abstract void deleteClient(String hostId);

	public abstract void deleteClients(String[] hostIds);

	public abstract List<String> deletePackageCaches(String[] hostIds);

	public abstract List<String> wakeOnLan(String[] hostIds);

	public abstract List<String> wakeOnLan(Set<String> hostIds, Map<String, List<String>> hostSeparationByDepot,
			Map<String, AbstractExecutioner> execsByDepot);

	public abstract List<String> fireOpsiclientdEventOnClients(String event, String[] clientIds);

	public abstract List<String> showPopupOnClients(String message, String[] clientIds, Float seconds);

	public abstract List<String> shutdownClients(String[] clientIds);

	public abstract List<String> rebootClients(String[] clientIds);

	public abstract Map<String, Object> reachableInfo(String[] clientIds);

	public abstract Map<String, Integer> getInstalledOsOverview();

	public abstract List<Map<String, Object>> getModules();

	public abstract Map<String, String> sessionInfo(String[] clientIds);

	// executes all updates collected by setHostDescription ...
	public abstract void updateHosts();

	public abstract void setHostDescription(String hostId, String description);

	public abstract void setClientInventoryNumber(String hostId, String inventoryNumber);

	public abstract void setClientOneTimePassword(String hostId, String oneTimePassword);

	public abstract void setHostNotes(String hostId, String notes);

	public abstract String getMacAddress(String hostId);

	public abstract void setSystemUUID(String hostId, String uuid);

	public abstract void setMacAddress(String hostId, String address);

	public abstract void setIpAddress(String hostId, String address);

	// group handling
	public abstract Map<String, Map<String, String>> getProductGroups();

	public abstract void productGroupsRequestRefresh();

	public abstract Map<String, Map<String, String>> getHostGroups();

	public abstract void hostGroupsRequestRefresh();

	public abstract void fObject2GroupsRequestRefresh();

	public abstract Map<String, Set<String>> getFObject2Groups();

	public abstract void fGroup2MembersRequestRefresh();

	public abstract void fProductGroup2MembersRequestRefresh();

	public abstract Map<String, Set<String>> getFGroup2Members();

	public abstract Map<String, Set<String>> getFProductGroup2Members();

	public abstract boolean addHosts2Group(List<String> objectIds, String groupId);

	public abstract boolean addObject2Group(String objectId, String groupId);

	public abstract boolean removeObject2Group(String objectId, String groupId);

	public abstract boolean removeHostGroupElements(List<Object2GroupEntry> entries);

	public abstract boolean addGroup(StringValuedRelationElement newgroup);

	public abstract boolean deleteGroup(String groupId);

	public abstract boolean updateGroup(String groupId, Map<String, String> updateInfo);

	public abstract boolean setProductGroup(String groupId, String description, Set<String> products);

	public abstract List<String> getHostGroupIds();

	public abstract Map<String, List<String>> getHostSeparationByDepots(String[] hostIds);

	// deprecated

	/* software info */
	public abstract List<String> getSoftwareList();

	public abstract NavigableMap<String, Integer> getSoftware2Number();

	public abstract void fillClient2Software(List<String> clients);

	public abstract void softwareAuditOnClientsRequestRefresh();

	public abstract Map<String, List<SWAuditClientEntry>> getClient2Software();

	public abstract DatedRowList getSoftwareAudit(String clientId);

	public abstract String getLastSoftwareAuditModification(String clientId);

	public abstract Map<String, Map<String, Object>> retrieveSoftwareAuditData(String clientId);

	/* hardware info */
	public abstract List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConf();

	public abstract List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConf(String locale);

	public abstract List<String> getAllHwClassNames();

	public abstract Map<String, OpsiHwAuditDeviceClass> getHwAuditDeviceClasses();

	public abstract void hwAuditConfRequestRefresh();

	public abstract Map<String, List<Map<String, Object>>> getHardwareInfo(String clientId, boolean asHTMLtable);

	public abstract void auditHardwareOnHostRequestRefresh();

	public abstract List<Map<String, Object>> getHardwareOnClient();

	/* multiclient hwinfo */

	public abstract List<String> getHwInfoClassNames();

	public abstract List<String> getHostColumnNames();

	public abstract List<String> getClient2HwRowsColumnNames();

	public abstract List<String> getClient2HwRowsJavaclassNames();

	public abstract void client2HwRowsRequestRefresh();

	public abstract Map<String, Map<String, Object>> getClient2HwRows(String[] hosts);

	public abstract boolean saveHwColumnConfig(Map<String, Map<String, Boolean>> updateItems);

	/* log files */

	public abstract Map<String, String> getEmptyLogfiles();

	public abstract Map<String, String> getLogfiles(String clientId, String logtype);

	public abstract Map<String, String> getLogfiles(String clientId);

	/* list of boot images */

	// product related

	public abstract void depotChange();

	public abstract void productDataRequestRefresh();

	/* listings of all products and their properties */

	public abstract List<String> getAllProductNames(String depotId);

	public abstract List<String> getProvidedLocalbootProducts(String depotId);

	public abstract List<String> getProvidedNetbootProducts(String depotId);

	public abstract List<String> getAllLocalbootProductNames(String depotId);

	public abstract List<String> getAllLocalbootProductNames();

	public abstract List<String> getAllDepotsWithIdenticalProductStock(String depot);

	// deprecated
	public abstract List<String> getAllNetbootProductNames();

	public abstract List<String> getAllNetbootProductNames(String depotId);

	public abstract List<String> getWinProducts(String depotId, String depotProductDirectory);

	public abstract void retrieveProducts();

	public abstract Map<String, List<String>> getPossibleActions(String depotId);

	public abstract Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos();

	public abstract Object2Product2VersionList getDepot2LocalbootProducts();

	public abstract Object2Product2VersionList getDepot2NetbootProducts();

	// (productId -> (infoKey -> info))
	public abstract Map<String, Map<String, Object>> getProductGlobalInfos(String depotId);

	public abstract List<List<Object>> getProductRows();

	public abstract Map<String, Map<String, List<String>>> getProduct2VersionInfo2Depots();

	public abstract NavigableSet<String> getProductIds();

	public abstract Map<String, Map<String, String>> getProductDefaultStates();

	public abstract Map<String, List<Map<String, String>>> getProductDependencies(String depotId);

	public abstract void retrieveProductDependencies();

	public abstract Set<String> extendToDependentProducts(final Set<String> startProductSet, final String depot);

	public abstract Set<String> getMessagebusConnectedClients();

	// intersection of the values of the clients
	public abstract List<String> getCommonProductPropertyValues(List<String> clients, String product, String property);

	public abstract void productPropertyDefinitionsRequestRefresh();

	public abstract void retrieveProductPropertyDefinitions();

	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getProductPropertyOptionsMap(String productId);

	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getProductPropertyOptionsMap(String depotId,
			String productId);

	public abstract String getProductTitle(String product);

	public abstract String getProductInfo(String product);

	public abstract String getProductHint(String product);

	public abstract String getProductVersion(String product);

	public abstract String getProductPackageVersion(String product);

	public abstract String getProductLockedInfo(String product);

	/* PC specific listings of products and their states and updatings */

	// methods requires java 8:

	public abstract Map<String, List<Map<String, String>>> getMapOfProductStatesAndActions(String[] clientIds);

	// clientIds,

	public abstract Map<String, List<Map<String, String>>> getMapOfLocalbootProductStatesAndActions(String[] clientIds);

	public abstract Map<String, List<Map<String, String>>> getMapOfNetbootProductStatesAndActions(String[] clientIds);

	// collecting update items
	public abstract boolean updateProductOnClient(String pcname, String productname, int producttype,
			Map<String, String> updateValues);

	// send the collected items
	public abstract boolean updateProductOnClients();

	// update for the whole set of clients
	public abstract boolean updateProductOnClients(Set<String> clients, String productName, int productType,
			Map<String, String> changedValues);

	public abstract boolean resetLocalbootProducts(String[] selectedClients, boolean withDependencies);

	public abstract boolean resetNetbootProducts(String[] selectedClients, boolean withDependencies);

	public abstract boolean resetProducts(List<Map<String, Object>> productItems, boolean withDependencies);

	public abstract Map<String, String> getProductPreRequirements(String depotId, String productname);

	public abstract Map<String, String> getProductRequirements(String depotId, String productname);

	public abstract Map<String, String> getProductPostRequirements(String depotId, String productname);

	public abstract Map<String, String> getProductDeinstallRequirements(String depotId, String productname);

	/* pc and product specific */

	public abstract void productpropertiesRequestRefresh();

	public abstract void retrieveProductProperties(List<String> clientNames);

	public abstract Boolean hasClientSpecificProperties(String productname);

	public abstract Map<String, Boolean> getProductHavingClientSpecificProperties();

	public abstract Map<String, Map<String, ConfigName2ConfigValue>> getDepot2product2properties();

	public abstract Map<String, ConfigName2ConfigValue> getDefaultProductProperties(String depotId);

	public abstract void retrieveDepotProductProperties();

	public abstract Map<String, Object> getProductProperties(String pcname, String productname);

	// Map properties,

	public abstract void setProductProperties(String pcname, String productname, Map<?, ?> properties);

	public abstract void setProductProperties();

	public abstract void setCommonProductPropertyValue(Set<String> clientNames, String productName, String propertyName,
			List<String> values);

	/* information about the service */

	// update this

	public abstract List<String> getMethodSignature(String methodname);

	public abstract String getBackendInfos();

	/* network and additional settings, for network objects */

	public abstract void hostConfigsRequestRefresh();

	// retrieve host configs if not existing

	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getConfigOptions();

	public abstract Map<String, List<Object>> getConfigDefaultValues();

	public abstract Boolean getGlobalBooleanConfigValue(String key, Boolean defaultVal);

	public abstract void setGlobalBooleanConfigValue(String key, Boolean val, String description);

	protected abstract boolean setHostBooleanConfigValue(String key, String hostName, boolean val);

	public abstract Map<String, Map<String, Object>> getConfigs();

	public abstract Map<String, Object> getConfig(String objectId);

	public abstract void setHostValues(Map<String, Object> settings);

	public abstract void setAdditionalConfiguration(String objectId, ConfigName2ConfigValue settings);

	public abstract void setAdditionalConfiguration(boolean determineConfigOptions);

	public abstract void setConfig(Map<String, List<Object>> settings);

	public abstract void setConfig();

	public abstract void configOptionsRequestRefresh();

	public abstract Map<String, RemoteControl> getRemoteControls();

	public abstract SavedSearches getSavedSearches();

	public abstract void deleteSavedSearch(String name);

	public abstract void saveSearch(SavedSearch ob);

	public abstract List<String> getServerConfigStrings(String key);

	public abstract void requestReloadOpsiDefaultDomain();

	public abstract String getOpsiDefaultDomain();

	public abstract List<String> getDomains();

	public abstract void writeDomains(List<Object> domains);

	public abstract void setDepot(String depotId);

	public abstract String getDepot();

	public abstract Map<String, SWAuditEntry> getInstalledSoftwareInformation();

	public abstract Map<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing();

	public abstract Map<String, Map<String, String>> getInstalledSoftwareName2SWinfo();

	public abstract NavigableMap<String, Set<String>> getName2SWIdents();

	public abstract void installedSoftwareInformationRequestRefresh();

	public abstract String getSWident(Integer i);

	/* licences */
	public abstract Map<String, LicenceContractEntry> getLicenceContracts();

	public abstract NavigableMap<String, NavigableSet<String>> getLicenceContractsExpired();
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

	public abstract Map<String, Map<String, Object>> getRelationsSoftwareL2LPool();

	// returns the ID of the edited data record
	public abstract String editRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId,
			String licenseKey);

	public abstract boolean deleteRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId);

	public abstract Map<String, Map<String, String>> getRelationsProductId2LPool();

	// returns an ID of the edited data record
	public abstract String editRelationProductId2LPool(String productId, String licensePoolId);

	public abstract boolean deleteRelationProductId2LPool(String productId, String licensePoolId);

	public abstract void retrieveRelationsAuditSoftwareToLicencePools();

	public abstract void relationsAuditSoftwareToLicencePoolsRequestRefresh();

	public abstract List<String> getSoftwareListByLicencePool(String licencePoolId);

	public abstract List<String> getUnknownSoftwareListForLicencePool(String licencePoolId);

	public abstract NavigableSet<Object> getSoftwareWithoutAssociatedLicencePool();

	public abstract Map<String, String> getFSoftware2LicencePool();

	public abstract String getFSoftware2LicencePool(String softwareIdent);

	public abstract void setFSoftware2LicencePool(String softwareIdent, String licencePoolId);

	public abstract boolean removeAssociations(String licensePoolId, List<String> softwareIds);

	public abstract boolean setWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign);

	public abstract boolean addWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign);

	public abstract String editPool2AuditSoftware(String softwareID, String licensePoolIDOld, String licensePoolIDNew);

	public abstract Map<String, LicenceStatisticsRow> getLicenceStatistics();

	public abstract void licencesUsageRequestRefresh();

	public abstract Map<String, List<LicenceUsageEntry>> getFClient2LicencesUsageList();

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
	public abstract Map<String, Boolean> getProductOnClientsDisplayFieldsLocalbootProducts();

	public abstract Map<String, Boolean> getProductOnClientsDisplayFieldsNetbootProducts();

	public abstract Map<String, Boolean> getHostDisplayFields();

	// menu configuration
	public abstract List<String> getDisabledClientMenuEntries();

	public abstract List<String> getOpsiclientdExtraEvents();

	// table sources

	public abstract void opsiInformationRequestRefresh();

	public abstract void retrieveOpsiModules();

	public abstract Map<String, Object> getOpsiModulesInfos();

	public abstract boolean isOpsiLicencingAvailable();

	public abstract boolean isOpsiUserAdmin();

	public abstract void opsiLicencingInfoRequestRefresh();

	public abstract JSONObject getOpsiLicencingInfoOpsiAdmin();

	public abstract Map<String, Object> getOpsiLicencingInfoNoOpsiAdmin();

	public abstract String getCustomer();

	public abstract boolean isWithLocalImaging();

	public abstract boolean isWithLicenceManagement();

	public abstract boolean isWithMySQL();

	public abstract boolean isWithUEFI();

	public abstract boolean isWithWAN();

	public abstract boolean isWithLinuxAgent();

	public abstract boolean isWithUserRoles();

	public abstract boolean applyUserSpecializedConfig();

	public abstract List<Map<java.lang.String, java.lang.Object>> retrieveCommandList();

	public abstract boolean doActionSSHCommand(String method, List<Object> jsonObjects);

	public abstract boolean createSSHCommand(List<Object> jsonObjects);

	public abstract boolean updateSSHCommand(List<Object> jsonObjects);

	public abstract boolean deleteSSHCommand(List<String> jsonObjects);

	public abstract boolean checkSSHCommandMethod(String method);

	public abstract List<Map<String, Object>> getOpsiconfdConfigHealth();

	public abstract List<Map<String, Object>> getDiskUsageHealth();

	public abstract List<Map<String, Object>> getDepotHealth();

	public abstract List<Map<String, Object>> getSystemPackageHealth();

	public abstract List<Map<String, Object>> getProductOnDepotsHealth();

	public abstract List<Map<String, Object>> getProductOnClientsHealth();

	public abstract List<Map<String, Object>> getLicenseHealth();

	public abstract List<Map<String, Object>> getDeprecatedCalls();

	public abstract List<Map<String, Object>> checkHealth();

	// json generating

	public static Map<String, Object> createNOMitem(String type) {
		Map<String, Object> item = new HashMap<>();
		item.put("type", type);

		return item;
	}

	public static ConfigOption createConfig(ConfigOption.TYPE type, String key, String description, boolean editable,
			boolean multiValue, List<Object> defaultValues, List<Object> possibleValues) {
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
			boolean editable, boolean multiValue, List<Object> defaultValues, List<Object> possibleValues) {

		Map<String, Object> item = createNOMitem(type.toString());

		item.put("id", key.toLowerCase());
		item.put("description", description);
		item.put("editable", editable);
		item.put("multiValue", multiValue);

		item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));

		item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));

		return item;
	}

	public static ConfigOption createBoolConfig(String key, Boolean value, String description) {
		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(value);

		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(true);
		possibleValues.add(false);

		return createConfig(ConfigOption.TYPE.BOOL_CONFIG, key, description, false, false, defaultValues,
				possibleValues);
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

}
