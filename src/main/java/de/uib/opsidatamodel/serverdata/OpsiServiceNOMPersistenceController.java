/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import de.uib.configed.productaction.FCompleteWinProducts;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SavedSearch;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.serverdata.dataservice.CommandDataService;
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
import de.uib.opsidatamodel.serverdata.dataservice.SoftwareDataService;
import de.uib.opsidatamodel.serverdata.dataservice.UserDataService;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.opsidatamodel.serverdata.reload.ReloadDispatcher;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.opsidatamodel.serverdata.reload.handler.ConfigOptionsDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.DefaultDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.DepotChangeReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.DepotProductPropertiesDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.HardwareConfDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.HostDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.InstalledSoftwareDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.LicenseContractDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.LicenseDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.LicensePoolDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.OpsiHostDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.OpsiLicenseReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.ProductDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.RelationsASWToLPDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.SoftwareLicense2LicensePoolDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.StatisticsDataReloadHandler;
import de.uib.utils.logging.Logging;

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
 * {@link AbstractPOJOExecutioner} component that transmits requests to the opsi
 * server and receives the responses. There are several classes which implement
 * the {@link AbstractPOJOExecutioner} methods in different ways dependent on
 * the used means and protocols.
 */
public class OpsiServiceNOMPersistenceController {
	public static final Set<String> KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT = Set.of("type", "id");

	public static final String CONFIG_KEY_SUPPLEMENTARY_QUERY = "configed.query_supplementary";

	@SuppressWarnings({ "java:S103" })
	public static final String KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION = "configed.license_inventory_extradisplayfields";

	public static final String CONTROL_DASH_CONFIG_KEY = "configed.dash_config";
	public static final String CONFIG_KEY = "configed.meta_config";

	public static final String KEY_SHOW_DASH_FOR_LICENSEMANAGEMENT = CONTROL_DASH_CONFIG_KEY
			+ ".show_dash_for_showlicenses";

	public static final Boolean DEFAULTVALUE_SHOW_DASH_FOR_LICENSEMANAGEMENT = false;

	public static final String OPSI_CLIENTD_EVENT_ON_DEMAND = "on_demand";

	public static final String LOCAL_IMAGE_RESTORE_PRODUCT_KEY = "opsi-local-image-restore";
	public static final String LOCAL_IMAGE_LIST_PROPERTY_KEY = "imagefiles_list";
	public static final String LOCAL_IMAGE_TO_RESTORE_PROPERTY_KEY = "imagefile";

	public static final String CONFIG_DEPOT_ID = "clientconfig.depot.id";
	public static final String KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN = "opsiclientd.event_on_shutdown.active";

	public static final String CONFIGED_GIVEN_DOMAINS_KEY = "configed.domains_given";

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

	public static final String HOST_KEY = "hostId";

	public static final String CONFIG_STATE_TYPE = "ConfigState";

	public static final String OBJECT_ID = "objectId";
	public static final String CONFIG_ID = "configId";
	public static final String VALUES_ID = "values";

	// opsi module information

	private static NavigableMap<String, String> propertyClassesServer;
	private static NavigableMap<String, String> propertyClassesClient;
	private static Set<String> configKeyStartersNotForClients;

	private FCompleteWinProducts panelCompleteWinProducts;

	private String user;

	private AbstractPOJOExecutioner exec;

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
	private CommandDataService sshCommandDataService;
	private UserDataService userDataService;
	private RPCMethodExecutor rpcMethodExecutor;
	private ReloadDispatcher reloadDispatcher;

	private String triggeredEvent;

	OpsiServiceNOMPersistenceController(String server, String user, String password, String otp) {
		Logging.info(this.getClass(), "start construction, \nconnect to " + server + " as " + user);
		this.user = user;

		Logging.debug(this.getClass(), "create");

		init();

		exec = new ServerFacade(server, user, password, otp);
		userRolesConfigDataService = new UserRolesConfigDataService(exec, this);
		configDataService = new ConfigDataService(exec, this);
		depotDataService = new DepotDataService(exec);
		groupDataService = new GroupDataService(exec, this);
		hardwareDataService = new HardwareDataService(exec);
		healthDataService = new HealthDataService(exec);
		hostDataService = new HostDataService(exec, this);
		licenseDataService = new LicenseDataService(exec);
		logDataService = new LogDataService(exec);
		moduleDataService = new ModuleDataService(exec);
		productDataService = new ProductDataService(exec, this);
		softwareDataService = new SoftwareDataService(exec, this);
		sshCommandDataService = new CommandDataService(exec);
		userDataService = new UserDataService(exec);
		rpcMethodExecutor = new RPCMethodExecutor(exec, this);
		hostInfoCollections = new HostInfoCollections(this);

		configDataService.setUserRolesConfigDataService(userRolesConfigDataService);

		depotDataService.setUserRolesConfigDataService(userRolesConfigDataService);
		depotDataService.setProductDataService(productDataService);
		depotDataService.setHostInfoCollections(hostInfoCollections);

		groupDataService.setUserRolesConfigDataService(userRolesConfigDataService);

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

	public CommandDataService getSSHCommandDataService() {
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

	@SuppressWarnings({ "java:S103", "java:S138" })
	private void registerReloadHandlers() {
		reloadDispatcher = new ReloadDispatcher();

		HostDataReloadHandler hostDataReloadHandler = new HostDataReloadHandler();
		hostDataReloadHandler.setConfigDataService(configDataService);
		hostDataReloadHandler.setGroupDataService(groupDataService);
		hostDataReloadHandler.setHostInfoCollections(hostInfoCollections);
		reloadDispatcher.registerHandler(ReloadEvent.HOST_DATA_RELOAD.toString(), hostDataReloadHandler);

		ConfigOptionsDataReloadHandler configOptionsDataReloadHandler = new ConfigOptionsDataReloadHandler();
		configOptionsDataReloadHandler.setConfigDataService(configDataService);
		reloadDispatcher.registerHandler(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString(), configOptionsDataReloadHandler);

		HardwareConfDataReloadHandler hardwareConfDataReloadHandler = new HardwareConfDataReloadHandler();
		hardwareConfDataReloadHandler.setConfigDataService(configDataService);
		hardwareConfDataReloadHandler.setHardwareDataService(hardwareDataService);
		reloadDispatcher.registerHandler(ReloadEvent.HARDWARE_CONF_RELOAD.toString(), hardwareConfDataReloadHandler);

		InstalledSoftwareDataReloadHandler installedSoftwareDataReloadHandler = new InstalledSoftwareDataReloadHandler();
		installedSoftwareDataReloadHandler.setSoftwareDataService(softwareDataService);
		reloadDispatcher.registerHandler(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString(),
				installedSoftwareDataReloadHandler);

		LicenseDataReloadHandler licenseDataReloadHandler = new LicenseDataReloadHandler();
		licenseDataReloadHandler.setLicenseDataService(licenseDataService);
		licenseDataReloadHandler.setHostInfoCollections(hostInfoCollections);
		licenseDataReloadHandler.setSoftwareDataService(softwareDataService);
		reloadDispatcher.registerHandler(ReloadEvent.LICENSE_DATA_RELOAD.toString(), licenseDataReloadHandler);

		OpsiLicenseReloadHandler opsiLicenseReloadHandler = new OpsiLicenseReloadHandler();
		opsiLicenseReloadHandler.setModuleDataService(moduleDataService);
		reloadDispatcher.registerHandler(ReloadEvent.OPSI_LICENSE_RELOAD.toString(), opsiLicenseReloadHandler);

		ProductDataReloadHandler productDataReloadHandler = new ProductDataReloadHandler();
		productDataReloadHandler.setGroupDataService(groupDataService);
		productDataReloadHandler.setProductDataService(productDataService);
		reloadDispatcher.registerHandler(ReloadEvent.PRODUCT_DATA_RELOAD.toString(), productDataReloadHandler);

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

		LicenseContractDataReloadHandler licenseContractDataReloadHandler = new LicenseContractDataReloadHandler();
		licenseContractDataReloadHandler.setLicenseDataService(licenseDataService);
		reloadDispatcher.registerHandler(ReloadEvent.LICENSE_CONTRACT_DATA_RELOAD.toString(),
				licenseContractDataReloadHandler);

		LicensePoolDataReloadHandler licensePoolDataReloadHandler = new LicensePoolDataReloadHandler();
		licensePoolDataReloadHandler.setLicenseDataService(licenseDataService);
		reloadDispatcher.registerHandler(ReloadEvent.LICENSE_POOL_DATA_RELOAD.toString(), licensePoolDataReloadHandler);

		@SuppressWarnings({ "java:S103" })
		SoftwareLicense2LicensePoolDataReloadHandler softwareLicense2LicensePoolDataReloadHandler = new SoftwareLicense2LicensePoolDataReloadHandler();
		softwareLicense2LicensePoolDataReloadHandler.setLicenseDataService(licenseDataService);
		reloadDispatcher.registerHandler(ReloadEvent.SOFTWARE_LICENSE_TO_LICENSE_POOL_DATA_RELOAD.toString(),
				softwareLicense2LicensePoolDataReloadHandler);

		StatisticsDataReloadHandler statisticsDataReloadHandler = new StatisticsDataReloadHandler();
		statisticsDataReloadHandler.setSoftwareDataService(softwareDataService);
		reloadDispatcher.registerHandler(ReloadEvent.STATISTICS_DATA_RELOAD.toString(), statisticsDataReloadHandler);

		DepotProductPropertiesDataReloadHandler depotProductPropertiesDataReloadHandler = new DepotProductPropertiesDataReloadHandler();
		depotProductPropertiesDataReloadHandler.setProductDataService(productDataService);
		reloadDispatcher.registerHandler(ReloadEvent.DEPOT_PRODUCT_PROPERTIES_DATA_RELOAD.toString(),
				depotProductPropertiesDataReloadHandler);

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
		reloadDispatcher.registerHandler(CacheIdentifier.HOST_CONFIGS.toString(), defaultDataReloadHandler);
		reloadDispatcher.registerHandler(CacheIdentifier.ALL_DATA.toString(), defaultDataReloadHandler);
		reloadDispatcher.registerHandler(CacheIdentifier.LICENSES.toString(), defaultDataReloadHandler);
	}

	public void reloadData(String event) {
		triggeredEvent = event;
		reloadDispatcher.dispatch(event);
	}

	public String getTriggeredEvent() {
		return triggeredEvent;
	}

	public void registerPanelCompleteWinProducts(FCompleteWinProducts panelCompleteWinProducts) {
		this.panelCompleteWinProducts = panelCompleteWinProducts;
	}

	public void notifyPanelCompleteWinProducts() {
		if (panelCompleteWinProducts != null) {
			panelCompleteWinProducts.evaluateWinProducts();
		}
	}

	private static void init() {
		propertyClassesServer = new TreeMap<>();
		propertyClassesServer.put("", "HostConfigNodeRenderer.mainNode");
		propertyClassesServer.put("clientconfig", "HostConfigNodeRenderer.clientconfig.Tooltip");
		propertyClassesServer.put(LicensingInfoMap.CONFIG_KEY, "HostConfigNodeRenderer.licensing.Tooltip");
		propertyClassesServer.put(CONTROL_DASH_CONFIG_KEY, "HostConfigNodeRenderer.configed.dash_config.Tooltip");
		propertyClassesServer.put(CONFIG_KEY_SUPPLEMENTARY_QUERY,
				"HostConfigNodeRenderer.configed.query_supplementary");
		propertyClassesServer.put(CONFIG_KEY, "HostConfigNodeRenderer.configed.meta_config");
		propertyClassesServer.put(SavedSearch.CONFIG_KEY, "HostConfigNodeRenderer.configed.saved_search");
		propertyClassesServer.put(RemoteControl.CONFIG_KEY, "HostConfigNodeRenderer.configed.remote_control");
		propertyClassesServer.put("opsiclientd", "HostConfigNodeRenderer.opsiclientd.Tooltip");

		propertyClassesServer.put("opsi-script", "HostConfigNodeRenderer.opsi_script.Tooltip");
		propertyClassesServer.put("software-on-demand", "HostConfigNodeRenderer.software_on_demand.Tooltip");
		propertyClassesServer.put(KEY_USER_ROOT,
				"EditMapPanelGroupedForHostConfigs.userPrivilegesConfiguration.ToolTip");
		propertyClassesServer.put(KEY_USER_ROLE_ROOT, "EditMapPanelGroupedForHostConfigs.roleConfiguration.ToolTip");

		propertyClassesClient = new TreeMap<>();
		propertyClassesClient.put("", "HostConfigNodeRenderer.mainNode");
		propertyClassesClient.put("clientconfig", "HostConfigNodeRenderer.clientconfig.Tooltip");
		propertyClassesClient.put("opsiclientd", "HostConfigNodeRenderer.opsiclientd.Tooltip");
		propertyClassesClient.put("opsi-script", "HostConfigNodeRenderer.opsi_script.Tooltip");
		propertyClassesClient.put("software-on-demand", "HostConfigNodeRenderer.software_on_demand.Tooltip");

		configKeyStartersNotForClients = new HashSet<>(propertyClassesServer.keySet());
		configKeyStartersNotForClients.removeAll(propertyClassesClient.keySet());
		configKeyStartersNotForClients.add("configed");
	}

	public boolean makeConnection() {
		Logging.info(this, "trying to make connection");
		boolean result = exec.doCall(new OpsiMethodCall(RPCMethodName.ACCESS_CONTROL_AUTHENTICATED, new String[] {}));

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

	public AbstractPOJOExecutioner getExecutioner() {
		return exec;
	}

	public String getUser() {
		return user;
	}

	public static NavigableMap<String, String> getPropertyClassesServer() {
		return propertyClassesServer;
	}

	public static NavigableMap<String, String> getPropertyClassesClient() {
		return propertyClassesClient;
	}

	public static Set<String> getConfigKeyStartersNotForClients() {
		return configKeyStartersNotForClients;
	}
}
