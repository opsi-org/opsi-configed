/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata;

import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import de.uib.configed.core.domain.modulelicense.LicensingInfoMap;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;
import de.uib.configed.core.domain.serverdata.reload.ReloadDispatcher;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.core.domain.serverdata.reload.handler.ConfigOptionsDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.DefaultDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.DepotChangeReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.DepotProductPropertiesDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.HardwareConfDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.HostDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.InstalledSoftwareDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.LicenseContractDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.LicenseDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.LicenseOnClientDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.LicensePoolDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.OpsiHostDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.OpsiLicenseReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.ProductDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.RelationsASWToLPDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.SoftwareLicense2LicensePoolDataReloadHandler;
import de.uib.configed.core.domain.serverdata.reload.handler.StatisticsDataReloadHandler;
import de.uib.configed.core.infrastructure.AbstractPOJOExecutioner;
import de.uib.configed.core.infrastructure.ConnectionState;
import de.uib.configed.core.infrastructure.ServerFacade;
import de.uib.configed.gui.features.productaction.CompleteWinProductsDialog;
import de.uib.configed.gui.type.RemoteControl;
import de.uib.configed.gui.type.SavedSearch;
import de.uib.configed.share.logging.Logging;

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

	@SuppressWarnings({ "java:S103" })
	public static final String KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION = "configed.license_inventory_extradisplayfields";

	public static final String CONTROL_DASH_CONFIG_KEY = "configed.dash_config";

	public static final String CONFIG_KEY_MSG_OF_DAY_DEVICE = "message_of_the_day.device.message";
	public static final String CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL = "message_of_the_day.device.message_valid_until";
	public static final String CONFIG_KEY_MSG_OF_DAY_USER = "message_of_the_day.user.message";
	public static final String CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL = "message_of_the_day.user.message_valid_until";

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

	private CompleteWinProductsDialog completeWinProductsPanel;

	private ServerFacade exec;

	private DataServices dataServices;

	private ReloadDispatcher reloadDispatcher;

	private String triggeredEvent;

	OpsiServiceNOMPersistenceController(String server, String user, String password, String otp, boolean useSSO) {
		Logging.info(this, "start construction, \nconnect to ", server, " as ", user, " sso ", useSSO);

		if (server == null || server.isEmpty()) {
			Logging.error(this.getClass(), "no server given");
			return;
		}

		if (useSSO) {
			Logging.info(this.getClass(), "OSNOM try sso/saml");
		} else if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
			Logging.error(this, "No user or password given.");
		} else {
			// Nothing to do here, we just continue logging in
		}

		Logging.debug(this, "create");

		init();
		ParallelTaskExecutor.allowNewTasks(true);
		if (useSSO) {
			Logging.info("ONOMPC useSSO true");
			exec = new ServerFacade(server);
			exec.setUseSSO(true);
		} else {
			Logging.info("ONOMPC useSSO false server ", server, " user ", user);
			exec = new ServerFacade(server, user, password, otp);
			exec.setUseSSO(false);
		}

		Logging.info(this, "connection state ", exec.getConnectionState());

		dataServices = new DataServices(exec, this);

		registerReloadHandlers();
	}

	public DataServices getDataServices() {
		return dataServices;
	}

	@SuppressWarnings({ "java:S103" })
	private void registerReloadHandlers() {
		reloadDispatcher = new ReloadDispatcher();

		HostDataReloadHandler hostDataReloadHandler = new HostDataReloadHandler();
		hostDataReloadHandler.setConfigDataService(dataServices.config);
		hostDataReloadHandler.setGroupDataService(dataServices.group);
		hostDataReloadHandler.setHostInfoCollections(dataServices.hostInfoCollections);
		reloadDispatcher.registerHandler(ReloadEvent.HOST_DATA_RELOAD.toString(), hostDataReloadHandler);

		ConfigOptionsDataReloadHandler configOptionsDataReloadHandler = new ConfigOptionsDataReloadHandler();
		configOptionsDataReloadHandler.setConfigDataService(dataServices.config);
		reloadDispatcher.registerHandler(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString(), configOptionsDataReloadHandler);

		HardwareConfDataReloadHandler hardwareConfDataReloadHandler = new HardwareConfDataReloadHandler();
		hardwareConfDataReloadHandler.setConfigDataService(dataServices.config);
		hardwareConfDataReloadHandler.setHardwareDataService(dataServices.hardware);
		reloadDispatcher.registerHandler(ReloadEvent.HARDWARE_CONF_RELOAD.toString(), hardwareConfDataReloadHandler);

		InstalledSoftwareDataReloadHandler installedSoftwareDataReloadHandler = new InstalledSoftwareDataReloadHandler();
		installedSoftwareDataReloadHandler.setSoftwareDataService(dataServices.software);
		reloadDispatcher.registerHandler(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString(),
				installedSoftwareDataReloadHandler);

		LicenseDataReloadHandler licenseDataReloadHandler = new LicenseDataReloadHandler();
		licenseDataReloadHandler.setLicenseDataService(dataServices.license);
		licenseDataReloadHandler.setHostInfoCollections(dataServices.hostInfoCollections);
		licenseDataReloadHandler.setSoftwareDataService(dataServices.software);
		reloadDispatcher.registerHandler(ReloadEvent.LICENSE_DATA_RELOAD.toString(), licenseDataReloadHandler);

		OpsiLicenseReloadHandler opsiLicenseReloadHandler = new OpsiLicenseReloadHandler();
		opsiLicenseReloadHandler.setModuleDataService(dataServices.module);
		reloadDispatcher.registerHandler(ReloadEvent.OPSI_LICENSE_RELOAD.toString(), opsiLicenseReloadHandler);

		ProductDataReloadHandler productDataReloadHandler = new ProductDataReloadHandler();
		productDataReloadHandler.setGroupDataService(dataServices.group);
		productDataReloadHandler.setProductDataService(dataServices.product);
		reloadDispatcher.registerHandler(ReloadEvent.PRODUCT_DATA_RELOAD.toString(), productDataReloadHandler);

		DepotChangeReloadHandler depotChangeReloadHandler = new DepotChangeReloadHandler();
		depotChangeReloadHandler.setDepotDataService(dataServices.depot);
		depotChangeReloadHandler.setProductDataService(dataServices.product);
		reloadDispatcher.registerHandler(ReloadEvent.DEPOT_CHANGE_RELOAD.toString(), depotChangeReloadHandler);

		RelationsASWToLPDataReloadHandler relationsASWToLPDataReloadHandler = new RelationsASWToLPDataReloadHandler();
		relationsASWToLPDataReloadHandler.setSoftwareDataService(dataServices.software);
		reloadDispatcher.registerHandler(ReloadEvent.ASW_TO_LP_RELATIONS_DATA_RELOAD.toString(),
				relationsASWToLPDataReloadHandler);

		OpsiHostDataReloadHandler opsiHostDataReloadHandler = new OpsiHostDataReloadHandler();
		opsiHostDataReloadHandler.setHostInfoCollections(dataServices.hostInfoCollections);
		reloadDispatcher.registerHandler(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString(), opsiHostDataReloadHandler);

		LicenseContractDataReloadHandler licenseContractDataReloadHandler = new LicenseContractDataReloadHandler();
		licenseContractDataReloadHandler.setLicenseDataService(dataServices.license);
		reloadDispatcher.registerHandler(ReloadEvent.LICENSE_CONTRACT_DATA_RELOAD.toString(),
				licenseContractDataReloadHandler);

		LicensePoolDataReloadHandler licensePoolDataReloadHandler = new LicensePoolDataReloadHandler();
		licensePoolDataReloadHandler.setLicenseDataService(dataServices.license);
		reloadDispatcher.registerHandler(ReloadEvent.LICENSE_POOL_DATA_RELOAD.toString(), licensePoolDataReloadHandler);

		@SuppressWarnings({ "java:S103" })
		SoftwareLicense2LicensePoolDataReloadHandler softwareLicense2LicensePoolDataReloadHandler = new SoftwareLicense2LicensePoolDataReloadHandler();
		softwareLicense2LicensePoolDataReloadHandler.setLicenseDataService(dataServices.license);
		reloadDispatcher.registerHandler(ReloadEvent.SOFTWARE_LICENSE_TO_LICENSE_POOL_DATA_RELOAD.toString(),
				softwareLicense2LicensePoolDataReloadHandler);

		LicenseOnClientDataReloadHandler licenseOnClientDataReloadHandler = new LicenseOnClientDataReloadHandler();
		licenseOnClientDataReloadHandler.setLicenseDataService(dataServices.license);
		reloadDispatcher.registerHandler(ReloadEvent.LICENSE_ON_CLIENT_DATA_RELOAD.toString(),
				licenseOnClientDataReloadHandler);

		StatisticsDataReloadHandler statisticsDataReloadHandler = new StatisticsDataReloadHandler();
		statisticsDataReloadHandler.setSoftwareDataService(dataServices.software);
		reloadDispatcher.registerHandler(ReloadEvent.STATISTICS_DATA_RELOAD.toString(), statisticsDataReloadHandler);

		DepotProductPropertiesDataReloadHandler depotProductPropertiesDataReloadHandler = new DepotProductPropertiesDataReloadHandler();
		depotProductPropertiesDataReloadHandler.setProductDataService(dataServices.product);
		reloadDispatcher.registerHandler(ReloadEvent.DEPOT_PRODUCT_PROPERTIES_DATA_RELOAD.toString(),
				depotProductPropertiesDataReloadHandler);

		DefaultDataReloadHandler defaultDataReloadHandler = new DefaultDataReloadHandler();
		defaultDataReloadHandler.setGroupDataService(dataServices.group);
		defaultDataReloadHandler.setHardwareDataService(dataServices.hardware);
		defaultDataReloadHandler.setConfigDataService(dataServices.config);
		defaultDataReloadHandler.setLicenseDataService(dataServices.license);
		reloadDispatcher.registerHandler(CacheIdentifier.LICENSE_USAGE.toString(), defaultDataReloadHandler);
		reloadDispatcher.registerHandler(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST.toString(),
				defaultDataReloadHandler);
		reloadDispatcher.registerHandler(CacheIdentifier.FHOST_TO_GROUPS.toString(), defaultDataReloadHandler);
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

	public void registerPanelCompleteWinProducts(CompleteWinProductsDialog completeWinProductsPanel) {
		this.completeWinProductsPanel = completeWinProductsPanel;
	}

	public void notifyPanelCompleteWinProducts() {
		if (completeWinProductsPanel != null) {
			completeWinProductsPanel.evaluateWinProducts();
		}
	}

	private static void init() {
		propertyClassesServer = new TreeMap<>();
		propertyClassesServer.put("", "HostConfigNodeRenderer.mainNode");
		propertyClassesServer.put("clientconfig", "HostConfigNodeRenderer.clientconfig.Tooltip");
		propertyClassesServer.put(LicensingInfoMap.CONFIG_KEY, "HostConfigNodeRenderer.licensing.Tooltip");
		propertyClassesServer.put(CONTROL_DASH_CONFIG_KEY, "HostConfigNodeRenderer.configed.dash_config.Tooltip");
		propertyClassesServer.put(SavedSearch.CONFIG_KEY, "HostConfigNodeRenderer.configed.saved_search");
		propertyClassesServer.put(RemoteControl.CONFIG_KEY, "HostConfigNodeRenderer.configed.remote_control");
		propertyClassesServer.put("netboot", "HostConfigNodeRenderer.netboot.Tooltip");
		propertyClassesServer.put("opsiclientd", "HostConfigNodeRenderer.opsiclientd.Tooltip");
		propertyClassesServer.put("opsi-script", "HostConfigNodeRenderer.opsi_script.Tooltip");
		propertyClassesServer.put("software-on-demand", "HostConfigNodeRenderer.software_on_demand.Tooltip");
		propertyClassesServer.put(KEY_USER_ROOT,
				"EditMapPanelGroupedForHostConfigs.userPrivilegesConfiguration.ToolTip");
		propertyClassesServer.put(KEY_USER_ROLE_ROOT, "EditMapPanelGroupedForHostConfigs.roleConfiguration.ToolTip");

		propertyClassesClient = new TreeMap<>();
		propertyClassesClient.put("", "HostConfigNodeRenderer.mainNode");
		propertyClassesClient.put("clientconfig", "HostConfigNodeRenderer.clientconfig.Tooltip");
		propertyClassesClient.put("netboot", "HostConfigNodeRenderer.netboot.Tooltip");
		propertyClassesClient.put("opsiclientd", "HostConfigNodeRenderer.opsiclientd.Tooltip");
		propertyClassesClient.put("opsi-script", "HostConfigNodeRenderer.opsi_script.Tooltip");
		propertyClassesClient.put("software-on-demand", "HostConfigNodeRenderer.software_on_demand.Tooltip");

		configKeyStartersNotForClients = new HashSet<>(propertyClassesServer.keySet());
		configKeyStartersNotForClients.removeAll(propertyClassesClient.keySet());
		configKeyStartersNotForClients.add("configed");
	}

	public boolean makeConnection() {
		Logging.info(this, "trying to make connection");
		boolean result = exec.doCall(RPCMethodName.ACCESS_CONTROL_AUTHENTICATED);

		if (!result) {
			Logging.info(this, "connection does not work");
		}

		result = result && getConnectionState().getState() == ConnectionState.CONNECTED;
		Logging.info(this, "tried to make connection result ", result);
		return result;
	}

	public ConnectionState getConnectionState() {
		return exec.getConnectionState();
	}

	public ServerFacade getExecutioner() {
		return exec;
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
