/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import de.uib.configed.Configed;
import de.uib.configed.productaction.PanelCompleteWinProducts;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SavedSearch;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.serverdata.dataservice.ConfigDataService;
import de.uib.opsidatamodel.serverdata.dataservice.DepotDataService;
import de.uib.opsidatamodel.serverdata.dataservice.GroupDataService;
import de.uib.opsidatamodel.serverdata.dataservice.HardwareDataService;
import de.uib.opsidatamodel.serverdata.dataservice.HealthDataService;
import de.uib.opsidatamodel.serverdata.dataservice.HostDataService;
import de.uib.opsidatamodel.serverdata.dataservice.LicenseDataService;
import de.uib.opsidatamodel.serverdata.dataservice.LogDataService;
import de.uib.opsidatamodel.serverdata.dataservice.ModuleDataService;
import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;
import de.uib.opsidatamodel.serverdata.dataservice.SSHCommandDataService;
import de.uib.opsidatamodel.serverdata.dataservice.SoftwareDataService;
import de.uib.opsidatamodel.serverdata.dataservice.UserDataService;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.opsidatamodel.serverdata.reload.ReloadDispatcher;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.opsidatamodel.serverdata.reload.handler.ClientHardwareDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.ConfigOptionsDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.DefaultDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.DepotChangeReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.EssentialDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.HardwareConfDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.HostConfigDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.HostDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.InstalledSoftwareDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.LicenseDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.OpsiHostDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.OpsiLicenseReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.ProductDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.ReconciliationDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.RelationsASWToLPDataReloadHandler;
import de.uib.utilities.logging.Logging;

/**
 * Provides methods for accessing classes, that provides methods working with
 * server data. Each class (usually ending in {@code DataService}) provides
 * methods working with different kind of data.
 * <p>
 * {@link OpsiServiceNOMPersistenceController} is implementation for the New
 * Object Model (opsi 4.0). Instances of
 * {@link OpsiServiceNOMPersistenceController} give access to proxy objects
 * which mediate access to remote objects (and buffer the data) The
 * {@link OpsiServiceNOMPersistenceController} retrieves its data from a server
 * that is compatible with the opsi data server resp. its stub (proxy) It has a
 * {@link AbstractExecutioner} component that transmits requests to the opsi
 * server and receives the responses. There are several classes which implement
 * the {@link AbstractExecutioner} methods in different ways dependent on the
 * used means and protocols.
 */
public class OpsiServiceNOMPersistenceController {

	public static final List<String> NONE_LIST = new ArrayList<>() {
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

	public static final String KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION = "configed.license_inventory_extradisplayfields";

	public static final String CONTROL_DASH_CONFIG_KEY = "configed.dash_config";
	public static final String CONFIG_KEY = "configed.meta_config";

	public static final String KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT = CONTROL_DASH_CONFIG_KEY
			+ ".show_dash_for_showlicenses";

	public static final Boolean DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT = false;

	public static final String KEY_SEARCH_BY_SQL = "configed.search_by_sql";

	// combines with question if mysql backend is working
	public static final Boolean DEFAULTVALUE_SEARCH_BY_SQL = true;

	public static final String OPSI_CLIENTD_EVENT_ON_DEMAND = "on_demand";

	public static final String KEY_PRODUCT_SORT_ALGORITHM = "product_sort_algorithm";

	public static final String LOCAL_IMAGE_RESTORE_PRODUCT_KEY = "opsi-local-image-restore";
	public static final String LOCAL_IMAGE_LIST_PROPERTY_KEY = "imagefiles_list";
	public static final String LOCAL_IMAGE_TO_RESTORE_PROPERTY_KEY = "imagefile";

	public static final String CONFIG_DEPOT_ID = "clientconfig.depot.id";
	public static final String KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN = "opsiclientd.event_on_shutdown.active";

	public static final String KEY_SSH_DEFAULTWINUSER = "configed.ssh.deploy-client-agent.default.user";
	public static final String KEY_SSH_DEFAULTWINUSER_DEFAULT_VALUE = "Administrator";
	public static final String KEY_SSH_DEFAULTWINPW = "configed.ssh.deploy-client-agent.default.password";
	public static final String KEY_SSH_DEFAULTWINPW_DEFAULT_VALUE = "";

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

	public static final String CONFIG_STATE_TYPE = "ConfigState";

	public static final String OBJECT_ID = "objectId";
	public static final String CONFIG_ID = "configId";
	public static final String VALUES_ID = "values";

	public static final NavigableMap<String, String> PROPERTY_CLASSES_SERVER = new TreeMap<>();
	static {
		PROPERTY_CLASSES_SERVER.put("", "HostConfigNodeRenderer.mainNode.Tooltip");
		PROPERTY_CLASSES_SERVER.put("clientconfig", "HostConfigNodeRenderer.clientconfig.Tooltip");
		PROPERTY_CLASSES_SERVER.put(LicensingInfoMap.CONFIG_KEY, "HostConfigNodeRenderer.licensing.Tooltip");
		PROPERTY_CLASSES_SERVER.put(CONTROL_DASH_CONFIG_KEY,
				"HostConfigNodeRenderer.configed.dash_configuration.Tooltip");
		PROPERTY_CLASSES_SERVER.put(CONFIG_KEY_SUPPLEMENTARY_QUERY,
				"<html><p>sql queries can be defined here<br />- for purposes other than are fulfilled by the standard tables</p></html>");
		PROPERTY_CLASSES_SERVER.put(CONFIG_KEY, "default configuration for other properties");
		PROPERTY_CLASSES_SERVER.put(SavedSearch.CONFIG_KEY,
				"<html><p>saved search configurations ,<br />do not edit here <br />- editing via the search form</p></html>");
		PROPERTY_CLASSES_SERVER.put(RemoteControl.CONFIG_KEY,
				"<html><p>remote control calls,<br />i.e. calls to tools on the local computer<br />typically targeting at a selected client</p></html>");
		PROPERTY_CLASSES_SERVER.put(OpsiHwAuditDeviceClass.CONFIG_KEY,
				"HostConfigNodeRenderer.configed.usecolumns_hwaudit");
		PROPERTY_CLASSES_SERVER.put("opsiclientd", "HostConfigNodeRenderer.opsiclientd.Tooltip");

		PROPERTY_CLASSES_SERVER.put("opsi-script", "HostConfigNodeRenderer.opsi_script.Tooltip");
		PROPERTY_CLASSES_SERVER.put("software-on-demand", "HostConfigNodeRenderer.software_on_demand.Tooltip");
		PROPERTY_CLASSES_SERVER.put(KEY_USER_ROOT,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.userPrivilegesConfiguration.ToolTip"));
		PROPERTY_CLASSES_SERVER.put(KEY_USER_ROLE_ROOT,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.roleConfiguration.ToolTip"));
	}

	public static final NavigableMap<String, String> PROPERTY_CLASSES_CLIENT = new TreeMap<>();
	static {
		PROPERTY_CLASSES_CLIENT.put("", "HostConfigNodeRenderer.mainNode.Tooltip");
		PROPERTY_CLASSES_CLIENT.put("clientconfig", "HostConfigNodeRenderer.clientconfig.Tooltip");
		PROPERTY_CLASSES_CLIENT.put("opsiclientd", "HostConfigNodeRenderer.opsiclientd.Tooltip");
		PROPERTY_CLASSES_CLIENT.put("opsi-script", "HostConfigNodeRenderer.opsi_script.Tooltip");
		PROPERTY_CLASSES_CLIENT.put("software-on-demand", "HostConfigNodeRenderer.software_on_demand.Tooltip");
	}

	public static final Set<String> CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS;
	static {
		CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS = new HashSet<>(PROPERTY_CLASSES_SERVER.keySet());
		CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS.removeAll(PROPERTY_CLASSES_CLIENT.keySet());
		CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS.add(KEY_PRODUCT_SORT_ALGORITHM);
		CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS.add("configed");
	}

	// opsi module information
	// wan meta configuration
	public static final String WAN_PARTKEY = "wan_";
	public static final String NOT_WAN_CONFIGURED_PARTKEY = "wan_mode_off";

	private PanelCompleteWinProducts panelCompleteWinProducts;

	private String user;

	private AbstractExecutioner exec;

	private String connectionServer;
	private HostInfoCollections hostInfoCollections;

	private ConfigDataService configDataService;
	private UserRolesConfigDataService userRolesConfigDataService;
	private DepotDataService depotDataService;
	private GroupDataService groupDataService;
	private HardwareDataService hardwareDataService;
	private HealthDataService healthDataService;
	private HostDataService hostDataService;
	private LicenseDataService licenseDataService;
	private LogDataService logDataService;
	private ModuleDataService moduleDataService;
	private ProductDataService productDataService;
	private SoftwareDataService softwareDataService;
	private SSHCommandDataService sshCommandDataService;
	private UserDataService userDataService;
	private RPCMethodExecutor rpcMethodExecutor;
	private ReloadDispatcher reloadDispatcher;

	OpsiServiceNOMPersistenceController(String server, String user, String password) {
		Logging.info(this.getClass(), "start construction, \nconnect to " + server + " as " + user);
		this.connectionServer = server;
		this.user = user;

		Logging.debug(this.getClass(), "create");

		exec = new ServerFacade(server, user, password);
		userRolesConfigDataService = new UserRolesConfigDataService(exec, this);
		configDataService = new ConfigDataService(exec, this);
		depotDataService = new DepotDataService(exec);
		groupDataService = new GroupDataService(exec, this);
		hardwareDataService = new HardwareDataService(exec, this);
		healthDataService = new HealthDataService(exec);
		hostDataService = new HostDataService(exec, this);
		licenseDataService = new LicenseDataService(exec);
		logDataService = new LogDataService(exec);
		moduleDataService = new ModuleDataService(exec);
		productDataService = new ProductDataService(exec, this);
		softwareDataService = new SoftwareDataService(exec, this);
		sshCommandDataService = new SSHCommandDataService(exec);
		userDataService = new UserDataService(exec);
		rpcMethodExecutor = new RPCMethodExecutor(exec, this);
		hostInfoCollections = new HostInfoCollections(this);

		configDataService.setHardwareDataService(hardwareDataService);
		configDataService.setUserRolesConfigDataService(userRolesConfigDataService);

		userRolesConfigDataService.setConfigDataService(configDataService);
		userRolesConfigDataService.setGroupDataService(groupDataService);
		userRolesConfigDataService.setHostInfoCollections(hostInfoCollections);
		userRolesConfigDataService.setModuleDataService(moduleDataService);

		depotDataService.setUserRolesConfigDataService(userRolesConfigDataService);
		depotDataService.setProductDataService(productDataService);
		depotDataService.setHostInfoCollections(hostInfoCollections);

		groupDataService.setUserRolesConfigDataService(userRolesConfigDataService);

		hardwareDataService.setConfigDataService(configDataService);
		hardwareDataService.setHostInfoCollections(hostInfoCollections);

		hostDataService.setConfigDataService(configDataService);
		hostDataService.setHostInfoCollections(hostInfoCollections);
		hostDataService.setHostInfoCollections(hostInfoCollections);
		hostDataService.setUserRolesConfigDataService(userRolesConfigDataService);

		licenseDataService.setUserRolesConfigDataService(userRolesConfigDataService);
		licenseDataService.setModuleDataService(moduleDataService);

		moduleDataService.setUserRolesConfigDataService(userRolesConfigDataService);
		moduleDataService.setHostInfoCollections(hostInfoCollections);

		productDataService.setConfigDataService(configDataService);
		productDataService.setDepotDataService(depotDataService);
		productDataService.setHostInfoCollections(hostInfoCollections);
		productDataService.setUserRolesConfigDataService(userRolesConfigDataService);

		softwareDataService.setModuleDataService(moduleDataService);
		softwareDataService.setLicenseDataService(licenseDataService);
		softwareDataService.setUserRolesConfigDataService(userRolesConfigDataService);
		softwareDataService.setHostInfoCollections(hostInfoCollections);

		sshCommandDataService.setModuleDataService(moduleDataService);
		sshCommandDataService.setUserRolesConfigDataService(userRolesConfigDataService);

		rpcMethodExecutor.setHostDataService(hostDataService);
		rpcMethodExecutor.setHostInfoCollections(hostInfoCollections);

		registerReloadHandlers();
	}

	public ConfigDataService getConfigDataService() {
		return configDataService;
	}

	public UserRolesConfigDataService getUserRolesConfigDataService() {
		return userRolesConfigDataService;
	}

	public DepotDataService getDepotDataService() {
		return depotDataService;
	}

	public GroupDataService getGroupDataService() {
		return groupDataService;
	}

	public HardwareDataService getHardwareDataService() {
		return hardwareDataService;
	}

	public HealthDataService getHealthDataService() {
		return healthDataService;
	}

	public HostDataService getHostDataService() {
		return hostDataService;
	}

	public LicenseDataService getLicenseDataService() {
		return licenseDataService;
	}

	public LogDataService getLogDataService() {
		return logDataService;
	}

	public ModuleDataService getModuleDataService() {
		return moduleDataService;
	}

	public ProductDataService getProductDataService() {
		return productDataService;
	}

	public SoftwareDataService getSoftwareDataService() {
		return softwareDataService;
	}

	public SSHCommandDataService getSSHCommandDataService() {
		return sshCommandDataService;
	}

	public UserDataService getUserDataService() {
		return userDataService;
	}

	public HostInfoCollections getHostInfoCollections() {
		return hostInfoCollections;
	}

	public RPCMethodExecutor getRPCMethodExecutor() {
		return rpcMethodExecutor;
	}

	private void registerReloadHandlers() {
		reloadDispatcher = new ReloadDispatcher();

		EssentialDataReloadHandler essentialDataReloadHandler = new EssentialDataReloadHandler();
		essentialDataReloadHandler.setConfigDataService(configDataService);
		essentialDataReloadHandler.setUserRolesConfigDataService(userRolesConfigDataService);
		essentialDataReloadHandler.setDepotDataService(depotDataService);
		essentialDataReloadHandler.setGroupDataService(groupDataService);
		essentialDataReloadHandler.setHardwareDataService(hardwareDataService);
		essentialDataReloadHandler.setHostDataService(hostDataService);
		essentialDataReloadHandler.setModuleDataService(moduleDataService);
		essentialDataReloadHandler.setProductDataService(productDataService);
		essentialDataReloadHandler.setHostInfoCollections(hostInfoCollections);
		reloadDispatcher.registerHandler(ReloadEvent.ESSENTIAL_DATA_RELOAD.toString(), essentialDataReloadHandler);

		HostDataReloadHandler hostDataReloadHandler = new HostDataReloadHandler();
		hostDataReloadHandler.setConfigDataService(configDataService);
		hostDataReloadHandler.setGroupDataService(groupDataService);
		hostDataReloadHandler.setHostInfoCollections(hostInfoCollections);
		reloadDispatcher.registerHandler(ReloadEvent.HOST_DATA_RELOAD.toString(), hostDataReloadHandler);

		ClientHardwareDataReloadHandler clientHardwareDataReloadHandler = new ClientHardwareDataReloadHandler();
		clientHardwareDataReloadHandler.setHardwareDataService(hardwareDataService);
		reloadDispatcher.registerHandler(ReloadEvent.CLIENT_HARDWARE_RELOAD.toString(),
				clientHardwareDataReloadHandler);

		ConfigOptionsDataReloadHandler configOptionsDataReloadHandler = new ConfigOptionsDataReloadHandler();
		configOptionsDataReloadHandler.setConfigDataService(configDataService);
		reloadDispatcher.registerHandler(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString(), configOptionsDataReloadHandler);

		HardwareConfDataReloadHandler hardwareConfDataReloadHandler = new HardwareConfDataReloadHandler();
		hardwareConfDataReloadHandler.setConfigDataService(configDataService);
		hardwareConfDataReloadHandler.setHardwareDataService(hardwareDataService);
		reloadDispatcher.registerHandler(ReloadEvent.HARDWARE_CONF_RELOAD.toString(), hardwareConfDataReloadHandler);

		HostConfigDataReloadHandler hostConfigDataReloadHandler = new HostConfigDataReloadHandler();
		hostConfigDataReloadHandler.setConfigDataService(configDataService);
		reloadDispatcher.registerHandler(ReloadEvent.HOST_CONFIG_RELOAD.toString(), hostConfigDataReloadHandler);

		InstalledSoftwareDataReloadHandler installedSoftwareDataReloadHandler = new InstalledSoftwareDataReloadHandler();
		installedSoftwareDataReloadHandler.setSoftwareDataService(softwareDataService);
		reloadDispatcher.registerHandler(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString(),
				installedSoftwareDataReloadHandler);

		LicenseDataReloadHandler licenseDataReloadHandler = new LicenseDataReloadHandler();
		licenseDataReloadHandler.setLicenseDataService(licenseDataService);
		licenseDataReloadHandler.setHostInfoCollections(hostInfoCollections);
		reloadDispatcher.registerHandler(ReloadEvent.LICENSE_DATA_RELOAD.toString(), licenseDataReloadHandler);

		OpsiLicenseReloadHandler opsiLicenseReloadHandler = new OpsiLicenseReloadHandler();
		opsiLicenseReloadHandler.setModuleDataService(moduleDataService);
		reloadDispatcher.registerHandler(ReloadEvent.OPSI_LICENSE_RELOAD.toString(), opsiLicenseReloadHandler);

		ProductDataReloadHandler productDataReloadHandler = new ProductDataReloadHandler();
		productDataReloadHandler.setGroupDataService(groupDataService);
		productDataReloadHandler.setProductDataService(productDataService);
		reloadDispatcher.registerHandler(ReloadEvent.PRODUCT_DATA_RELOAD.toString(), productDataReloadHandler);

		ReconciliationDataReloadHandler reconciliationDataReloadHandler = new ReconciliationDataReloadHandler();
		reconciliationDataReloadHandler.setLicenseDataService(licenseDataService);
		reconciliationDataReloadHandler.setSoftwareDataService(softwareDataService);
		reconciliationDataReloadHandler.setHostInfoCollections(hostInfoCollections);
		reloadDispatcher.registerHandler(ReloadEvent.RECONCILIATION_INFO_RELOAD.toString(),
				reconciliationDataReloadHandler);

		DepotChangeReloadHandler depotChangeReloadHandler = new DepotChangeReloadHandler();
		depotChangeReloadHandler.setDepotDataService(depotDataService);
		depotChangeReloadHandler.setProductDataService(productDataService);
		reloadDispatcher.registerHandler(ReloadEvent.DEPOT_CHANGE_RELOAD.toString(), depotChangeReloadHandler);

		RelationsASWToLPDataReloadHandler relationsASWToLPDataReloadHandler = new RelationsASWToLPDataReloadHandler();
		relationsASWToLPDataReloadHandler.setSoftwareDataService(softwareDataService);
		reloadDispatcher.registerHandler(ReloadEvent.ASW_TO_LP_RELATIONS_DATA_RELOAD.toString(),
				relationsASWToLPDataReloadHandler);

		OpsiHostDataReloadHandler opsiHostDataReloadHandler = new OpsiHostDataReloadHandler();
		opsiHostDataReloadHandler.setHostInfoCollections(hostInfoCollections);
		reloadDispatcher.registerHandler(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString(), opsiHostDataReloadHandler);

		DefaultDataReloadHandler defaultDataReloadHandler = new DefaultDataReloadHandler();
		defaultDataReloadHandler.setGroupDataService(groupDataService);
		defaultDataReloadHandler.setHardwareDataService(hardwareDataService);
		defaultDataReloadHandler.setConfigDataService(configDataService);
		defaultDataReloadHandler.setLicenseDataService(licenseDataService);
		reloadDispatcher.registerHandler(CacheIdentifier.LICENSE_USAGE.toString(), defaultDataReloadHandler);
		reloadDispatcher.registerHandler(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST.toString(),
				defaultDataReloadHandler);
		reloadDispatcher.registerHandler(CacheIdentifier.FOBJECT_TO_GROUPS.toString(), defaultDataReloadHandler);
		reloadDispatcher.registerHandler(CacheIdentifier.HOST_GROUPS.toString(), defaultDataReloadHandler);
		reloadDispatcher.registerHandler(CacheIdentifier.PRODUCT_PROPERTIES.toString(), defaultDataReloadHandler);
	}

	public void reloadData(String event) {
		reloadDispatcher.dispatch(event);
	}

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

	public AbstractExecutioner retrieveWorkingExec(String depot) {
		Logging.debug(this, "retrieveWorkingExec , compare depotname " + depot + " to config server "
				+ hostInfoCollections.getConfigServer() + " ( named as " + connectionServer + ")");

		if (depot.equals(hostInfoCollections.getConfigServer())) {
			Logging.debug(this, "retrieveWorkingExec for config server");
			return exec;
		}

		String password = (String) hostInfoCollections.getDepots().get(depot).get(HostInfo.HOST_KEY_KEY);
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

	public ConnectionState getConnectionState() {
		return exec.getConnectionState();
	}

	public AbstractExecutioner getExecutioner() {
		return exec;
	}

	public String getUser() {
		return user;
	}
}
