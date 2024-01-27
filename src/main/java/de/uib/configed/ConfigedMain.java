/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.java_websocket.handshake.ServerHandshake;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.Main;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.groupaction.ActivatedGroupModel;
import de.uib.configed.groupaction.FGroupActions;
import de.uib.configed.gui.ClientSelectionDialog;
import de.uib.configed.gui.ClientTable;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.FShowListWithComboSelect;
import de.uib.configed.gui.FStartWakeOnLan;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.FWakeClients;
import de.uib.configed.gui.HostsStatusPanel;
import de.uib.configed.gui.LoginDialog;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.gui.NewClientDialog;
import de.uib.configed.gui.SavedSearchesDialog;
import de.uib.configed.gui.licenses.LicensesFrame;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.gui.ssh.SSHCommandControlDialog;
import de.uib.configed.gui.ssh.SSHConfigDialog;
import de.uib.configed.guidata.DependenciesModel;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.guidata.InstallationStateUpdateManager;
import de.uib.configed.guidata.ListMerger;
import de.uib.configed.productaction.FProductActions;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.tree.GroupNode;
import de.uib.configed.type.DateExtendedByVars;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.licenses.LicenseEntry;
import de.uib.configed.type.licenses.LicenseUsageEntry;
import de.uib.messagebus.Messagebus;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandNeedParameter;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.datachanges.AdditionalconfigurationUpdateCollection;
import de.uib.opsidatamodel.datachanges.HostUpdateCollection;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.productstate.ActionSequence;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.DataChangedKeeper;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedDocument;
import de.uib.utilities.swing.FEditText;
import de.uib.utilities.swing.tabbedpane.TabClient;
import de.uib.utilities.table.ListCellOptions;
import de.uib.utilities.table.gui.BooleanIconTableCellRenderer;
import de.uib.utilities.table.gui.ConnectionStatusTableCellRenderer;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import utils.ProductPackageVersionSeparator;
import utils.Utils;

public class ConfigedMain implements ListSelectionListener, MessagebusListener {
	private static final Pattern backslashPattern = Pattern.compile("[\\[\\]\\s]", Pattern.UNICODE_CHARACTER_CLASS);

	public static final int VIEW_CLIENTS = 0;
	public static final int VIEW_LOCALBOOT_PRODUCTS = 1;
	public static final int VIEW_NETBOOT_PRODUCTS = 2;
	public static final int VIEW_NETWORK_CONFIGURATION = 3;
	public static final int VIEW_HARDWARE_INFO = 4;
	public static final int VIEW_SOFTWARE_INFO = 5;
	public static final int VIEW_LOG = 6;
	public static final int VIEW_PRODUCT_PROPERTIES = 7;
	public static final int VIEW_HOST_PROPERTIES = 8;

	private static final int ICON_COLUMN_MAX_WIDTH = 100;

	private static final Dimension LICENSES_DIMENSION = new Dimension(1200, 900);

	private static LicensesFrame licensesFrame;
	private static MainFrame mainFrame;
	private static LoginDialog loginDialog;

	private static String host;
	private static String user;
	private static String password;

	private static EditingTarget editingTarget = EditingTarget.CLIENTS;

	private OpsiServiceNOMPersistenceController persistenceController;

	// global table providers for license management
	protected DefaultTableProvider licensePoolTableProvider;
	protected DefaultTableProvider licenseOptionsTableProvider;
	protected DefaultTableProvider licenseContractsTableProvider;
	protected DefaultTableProvider softwarelicensesTableProvider;

	private GeneralDataChangedKeeper generalDataChangedKeeper;
	private ClientInfoDataChangedKeeper clientInfoDataChangedKeeper;
	private GeneralDataChangedKeeper hostConfigsDataChangedKeeper;

	private DependenciesModel dependenciesModel;

	private String firstSelectedClient;
	private List<String> selectedClients = new ArrayList<>();
	private List<String> saveSelectedClients;
	private List<String> preSaveSelectedClients;

	private Set<String> clientsFilteredByTree = new HashSet<>();
	private TreePath groupPathActivatedByTree;
	private ActivatedGroupModel activatedGroupModel;

	private boolean anyDataChanged;

	private String clientInDepot;
	private HostInfo hostInfo = new HostInfo();

	private String appTitle = Globals.APPNAME;

	private FTextArea fAskSaveChangedText;
	private FTextArea fAskSaveProductConfiguration;

	private SavedSearchesDialog savedSearchesDialog;
	private ClientSelectionDialog clientSelectionDialog;

	// the properties for one product and all selected clients
	private Collection<Map<String, Object>> productProperties;
	private UpdateCollection updateCollection = new UpdateCollection(new ArrayList<>());
	private Map<String, ProductpropertiesUpdateCollection> clientProductpropertiesUpdateCollections;
	/*
	 * for each product:
	 * a collection of all clients
	 * that contains name value pairs with changed data
	 */

	private ProductpropertiesUpdateCollection clientProductpropertiesUpdateCollection;

	private AdditionalconfigurationUpdateCollection additionalconfigurationUpdateCollection;

	private HostUpdateCollection hostUpdateCollection;

	private InstallationStateUpdateManager updateManager;

	// Map<client, <product, <propertykey, propertyvalue>>>
	private Map<String, Map<String, Map<String, String>>> collectChangedLocalbootStates = new HashMap<>();
	private Map<String, Map<String, Map<String, String>>> collectChangedNetbootStates = new HashMap<>();

	// a map of products, product --> list of used as an indicator that a product is in the depot
	private Map<String, List<String>> possibleActions = new HashMap<>();

	private Map<String, ListMerger> mergedProductProperties;

	private Map<String, Boolean> displayFieldsLocalbootProducts;
	private Map<String, Boolean> displayFieldsNetbootProducts;

	private Set<String> depotsOfSelectedClients;
	private Set<String> allowedClients;

	private List<String> localbootProductnames;
	private List<String> netbootProductnames;

	// marker variables for requests for reload when clientlist changes
	private boolean localbootStatesAndActionsUpdate = true;
	private boolean netbootStatesAndActionsUpdate = true;

	// collection of retrieved software audit and hardware maps

	private String myServer;

	private ClientTable clientTable;

	private ClientTree clientTree;

	private Map<String, Map<String, String>> productGroups;
	private Map<String, Set<String>> productGroupMembers;

	private DepotsList depotsList;
	private Map<String, Map<String, Object>> depots;
	private List<String> depotNamesLinked;
	private String depotRepresentative;
	private ListSelectionListener depotsListSelectionListener;

	private ReachableUpdater reachableUpdater = new ReachableUpdater(0);

	private List<JFrame> allFrames;

	private FGroupActions groupActionFrame;
	private FProductActions productActionFrame;

	private List<AbstractControlMultiTablePanel> allControlMultiTablePanels;

	private Dashboard dashboard;

	private int clientCount;

	private int viewIndex = VIEW_CLIENTS;
	private int saveClientsViewIndex = VIEW_CLIENTS;
	private int saveDepotsViewIndex = VIEW_PRODUCT_PROPERTIES;
	private int saveServerViewIndex = VIEW_NETWORK_CONFIGURATION;

	private Map<String, Object> reachableInfo = new HashMap<>();
	private Map<String, String> sessionInfo = new HashMap<>();

	private Map<String, String> logfiles = new HashMap<>();

	private Map<String, Boolean> hostDisplayFields;

	public enum LicensesTabStatus {
		LICENSEPOOL, ENTER_LICENSE, EDIT_LICENSE, USAGE, RECONCILIATION, STATISTICS
	}

	private Map<LicensesTabStatus, TabClient> licensesPanels = new EnumMap<>(LicensesTabStatus.class);
	private LicensesTabStatus licensesStatus;

	private Map<LicensesTabStatus, String> licensesPanelsTabNames = new EnumMap<>(LicensesTabStatus.class);

	private boolean filterClientList;

	public enum EditingTarget {
		CLIENTS, DEPOTS, SERVER
	}
	// with this enum type we build a state model, which target shall be edited

	private int buildPclistTableModelCounter;

	private int reloadCounter;

	private Messagebus messagebus;

	private Set<String> connectedHostsByMessagebus;

	private SSHConfigDialog sshConfigDialog;
	private SSHCommandControlDialog sshCommandControlDialog;
	private NewClientDialog newClientDialog;

	private boolean isAllLicenseDataReloaded;
	private boolean isInitialLicenseDataLoading;

	private InitialDataLoader initialDataLoader;

	public ConfigedMain(String host, String user, String password, String sshKey, String sshKeyPass) {
		if (ConfigedMain.host == null) {
			setHost(host);
		}
		if (ConfigedMain.user == null) {
			setUser(user);
		}
		if (ConfigedMain.password == null) {
			setPassword(password);
		}

		SSHConnectionInfo.getInstance().setHost(host);
		SSHConnectionInfo.getInstance().setUser(user);
		SSHConnectionInfo.getInstance().setPassw(password);
		if (sshKey == null) {
			SSHConnectionInfo.getInstance().useKeyfile(false, "", "");
		} else {
			SSHConnectionInfo.getInstance().useKeyfile(true, sshKey, sshKeyPass);
		}

		Logging.registerConfigedMain(this);
	}

	public static MainFrame getMainFrame() {
		return mainFrame;
	}

	public LoginDialog getLoginDialog() {
		return loginDialog;
	}

	private void addClient(LicensesTabStatus status, TabClient panel) {
		licensesPanels.put(status, panel);
		licensesFrame.addTab(status, licensesPanelsTabNames.get(status), (JComponent) panel);
	}

	public LicensesTabStatus reactToStateChangeRequest(LicensesTabStatus newState) {
		Logging.debug(this, "reactToStateChangeRequest( newState: " + newState + "), current state " + licensesStatus);
		if (newState != licensesStatus && licensesPanels.get(licensesStatus).mayLeave()) {
			licensesStatus = newState;

			if (licensesPanels.get(licensesStatus) != null) {
				licensesPanels.get(licensesStatus).reset();
			}
			// otherwise we return the old status
		}

		return licensesStatus;
	}

	public static ConfigedMain.EditingTarget getEditingTarget() {
		return editingTarget;
	}

	protected void initGui() {
		Logging.info(this, "initGui");

		displayFieldsLocalbootProducts = new LinkedHashMap<>(
				persistenceController.getProductDataService().getProductOnClientsDisplayFieldsLocalbootProducts());
		displayFieldsNetbootProducts = new LinkedHashMap<>(
				persistenceController.getProductDataService().getProductOnClientsDisplayFieldsNetbootProducts());
		// initialization by defaults, it can be edited afterwards

		initTree();

		allFrames = new ArrayList<>();

		initMainFrame();

		updateManager = new InstallationStateUpdateManager(this,
				mainFrame.getPanelLocalbootProductSettings().getTableProducts(),
				mainFrame.getPanelNetbootProductSettings().getTableProducts());

		activatedGroupModel = new ActivatedGroupModel(mainFrame.getHostsStatusPanel());

		initialTreeActivation();

		Logging.info(this, "Is messagebus null? " + (messagebus == null));

		if (messagebus != null) {
			messagebus.getWebSocket().registerListener(this);
			messagebus.getWebSocket().registerListener(mainFrame.getHostsStatusPanel());

			if (messagebus.getWebSocket().isOpen()) {
				// Fake opening event on registering listener since this listener
				// does not know yet if it's open
				mainFrame.getHostsStatusPanel().onOpen(null);
			} else {
				Logging.warning(this, "Messagebus is not open, but should be on start");
			}
		}

		setEditingTarget(EditingTarget.CLIENTS);

		anyDataChanged = false;

		Logging.debug(this, "initialTreeActivation");

		reachableUpdater.setInterval(Configed.getRefreshMinutes());

		mainFrame.updateHostCheckboxenText();
		mainFrame.enableAfterLoading();
	}

	private List<String> readLocallySavedServerNames() {
		List<String> result = new ArrayList<>();
		TreeMap<Timestamp, String> sortingmap = new TreeMap<>();
		File savedStatesLocation = null;
		// the following is nearly a double of initSavedStates

		boolean success = true;

		if (Configed.getSavedStatesLocationName() != null) {
			Logging.info(this, "trying to find saved states in " + Configed.getSavedStatesLocationName());

			savedStatesLocation = new File(Configed.getSavedStatesLocationName());
			savedStatesLocation.mkdirs();
			success = savedStatesLocation.setReadable(true);
		}

		if (!success) {
			Logging.warning(this, "cannot not find saved states in " + Configed.getSavedStatesLocationName());
		}

		if (Configed.getSavedStatesLocationName() == null || !success) {
			Logging.info(this, "searching saved states in " + Utils.getSavedStatesDefaultLocation());
			savedStatesLocation = new File(Utils.getSavedStatesDefaultLocation());
			savedStatesLocation.mkdirs();
		}

		Logging.info(this, "saved states location " + savedStatesLocation);

		File[] subdirs = null;

		if (savedStatesLocation != null) {
			subdirs = savedStatesLocation.listFiles(File::isDirectory);

			for (File folder : subdirs) {
				File checkFile = new File(folder + File.separator + Configed.SAVED_STATES_FILENAME);
				String folderPath = folder.getPath();
				String elementname = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);

				if (elementname.lastIndexOf("_") > -1) {
					elementname = elementname.replace("_", ":");
				}

				sortingmap.put(new Timestamp(checkFile.lastModified()), elementname);
			}
		}

		for (Date date : sortingmap.descendingKeySet()) {
			result.add(sortingmap.get(date));
		}

		Logging.info(this, "readLocallySavedServerNames  result " + result);

		return result;
	}

	private void setSSHallowedHosts() {
		Set<String> sshAllowedHosts = new HashSet<>();

		if (persistenceController.getUserRolesConfigDataService().hasDepotsFullPermissionPD()) {
			Logging.info(this, "set ssh allowed hosts " + host);
			sshAllowedHosts.add(host);
			sshAllowedHosts.addAll(persistenceController.getHostInfoCollections().getDepots().keySet());
		} else {
			sshAllowedHosts.addAll(
					persistenceController.getDepotDataService().getDepotPropertiesForPermittedDepots().keySet());
		}

		SSHCommandFactory.getInstance(this).setAllowedHosts(sshAllowedHosts);
		Logging.info(this, "ssh allowed hosts" + sshAllowedHosts);
	}

	public void initDashInfo() {
		if (!ServerFacade.isOpsi43()) {
			Logging.info(this, "initDashInfo not enabled");
			return;
		}

		Logging.info(this, "initDashboard " + dashboard);
		if (dashboard == null) {
			dashboard = new Dashboard(this);
			dashboard.initAndShowGUI();
		} else {
			dashboard.show();
		}
	}

	public boolean initMessagebus() {
		if (messagebus == null) {
			messagebus = new Messagebus();
		}

		if (!messagebus.isConnected()) {
			try {
				Logging.info(this, "connecting to messagebus");
				messagebus.connect();
				Logging.info(this, "connected to messagebus");
			} catch (InterruptedException e) {
				Logging.error(this, "could not connect to messagebus", e);
				Thread.currentThread().interrupt();
			}
		}
		return messagebus.isConnected();
	}

	public Messagebus getMessagebus() {
		return messagebus;
	}

	public void addClientToTable(String clientId) {
		if (persistenceController.getHostInfoCollections().getOpsiHostNames().contains(clientId)
				|| getViewIndex() != VIEW_CLIENTS) {
			return;
		}

		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		SwingUtilities.invokeLater(() -> {
			List<String> selectedValues = clientTable.getSelectedValues();
			clientTable.clearSelection();
			refreshClientListKeepingGroup();
			setClients(selectedValues);
		});
	}

	public void removeClientFromTable(String clientId) {
		if (!persistenceController.getHostInfoCollections().getOpsiHostNames().contains(clientId)
				|| getViewIndex() != VIEW_CLIENTS) {
			return;
		}

		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		SwingUtilities.invokeLater(this::refreshClientListKeepingGroup);
	}

	public void updateProductTableForClient(String clientId, String productType) {
		Logging.devel("updateProductsTableForClient " + clientId + " " + productType);
		int selectedView = getViewIndex();
		Logging.devel(productType);
		if (selectedView == VIEW_LOCALBOOT_PRODUCTS) {
			List<String> attributes = getLocalbootStateAndActionsAttributes();
			updateManager.updateProductTableForClient(clientId, attributes);
		} else if (selectedView == VIEW_NETBOOT_PRODUCTS) {
			List<String> attributes = getAttributesFromProductDisplayFields(getNetbootProductDisplayFieldsList());
			// Remove uneeded attributes
			attributes.remove(ProductState.KEY_PRODUCT_PRIORITY);
			attributes.add(ProductState.key2servicekey.get(ProductState.KEY_LAST_STATE_CHANGE));

			updateManager.updateProductTableForClient(clientId, attributes);
		} else {
			Logging.info(this, "in updateProduct nothing to update because Tab for productType " + productType
					+ "not open or configed not yet initialized");
		}
	}

	public void addClientToConnectedList(String clientId) {
		connectedHostsByMessagebus.add(clientId);
		updateConnectionStatusInTable(clientId);
	}

	public void removeClientFromConnectedList(String clientId) {
		connectedHostsByMessagebus.remove(clientId);
		updateConnectionStatusInTable(clientId);
	}

	public void loadDataAndGo() {
		Logging.clearErrorList();

		// errors are already handled in login
		Logging.info(this, " we got persist " + persistenceController);

		setSSHallowedHosts();

		Logging.info(this, "call initData");
		initData();

		initialDataLoader = new InitialDataLoader(this);
		initialDataLoader.execute();
	}

	public void init() {
		Logging.debug(this, "init");

		// we start with a language

		InstallationStateTableModel.restartColumnDict();

		List<String> savedServers = readLocallySavedServerNames();

		setupLoginDialog(savedServers);
	}

	private void initData() {
		dependenciesModel = new DependenciesModel();
		generalDataChangedKeeper = new GeneralDataChangedKeeper();
		clientInfoDataChangedKeeper = new ClientInfoDataChangedKeeper();
		hostConfigsDataChangedKeeper = new GeneralDataChangedKeeper();
		allControlMultiTablePanels = new ArrayList<>();

		if (ServerFacade.isOpsi43()) {
			initMessagebus();
		}
	}

	protected void preloadData() {
		persistenceController.getModuleDataService().retrieveOpsiModules();
		myServer = persistenceController.getHostInfoCollections().getConfigServer();

		if (depotRepresentative == null) {
			depotRepresentative = myServer;
		}

		persistenceController.getDepotDataService().setDepot(depotRepresentative);

		localbootProductnames = persistenceController.getProductDataService().getAllLocalbootProductNames();
		netbootProductnames = persistenceController.getProductDataService().getAllNetbootProductNames();
		persistenceController.getProductDataService().retrieveProductIdsAndDefaultStatesPD();

		hostDisplayFields = persistenceController.getHostDataService().getHostDisplayFields();
		persistenceController.getProductDataService().retrieveProductOnClientsDisplayFieldsNetbootProducts();
		persistenceController.getProductDataService().retrieveProductOnClientsDisplayFieldsLocalbootProducts();

		if (savedSearchesDialog != null) {
			savedSearchesDialog.resetModel();
		}

		// Load all group data in this method to only call one method!
		persistenceController.getGroupDataService().retrieveAllGroupsPD();
		persistenceController.getGroupDataService().retrieveAllObject2GroupsPD();

		productGroups = persistenceController.getGroupDataService().getProductGroupsPD();
		fillterPermittedProductGroups(productGroups.keySet());
		productGroupMembers = persistenceController.getGroupDataService().getFProductGroup2Members();

		persistenceController.getDepotDataService().retrieveProductsPD();

		possibleActions = persistenceController.getProductDataService().getPossibleActionsPD(depotRepresentative);
		persistenceController.getProductDataService().retrieveAllProductPropertyDefinitionsPD();
		persistenceController.getProductDataService().retrieveAllProductDependenciesPD();
		persistenceController.getProductDataService().retrieveDepotProductPropertiesPD();

		connectedHostsByMessagebus = persistenceController.getHostDataService().getMessagebusConnectedClients();
	}

	private void fillterPermittedProductGroups(Set<String> productGroups) {
		if (!persistenceController.getUserRolesConfigDataService().hasProductGroupsFullPermissionPD()) {
			Set<String> permittedProductGroups = persistenceController.getUserRolesConfigDataService()
					.getPermittedProductGroupsPD();
			productGroups.retainAll(permittedProductGroups);
		}
	}

	public void setColumnSessionInfo(boolean b) {
		boolean visible = persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		if (visible != b) {
			toggleColumn(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		}

		Logging.info(this, "setColumnSessionInfo " + b);
	}

	public void toggleColumn(String column) {
		boolean visible = persistenceController.getHostDataService().getHostDisplayFields().get(column);
		persistenceController.getHostDataService().getHostDisplayFields().put(column, !visible);

		setRebuiltClientListTableModel(false);
		clientTable.initSortKeys();

		// Todo this is called before in "setRebuiltClientListTableModel". Maybe make it unnecessary
		clientTable.moveToFirstSelected();
	}

	public void handleGroupActionRequest() {
		if (persistenceController.getModuleDataService().isWithLocalImagingPD()) {
			startGroupActionFrame();
		} else {
			Logging.error(this,
					"this should not happen: group actions are not available since the module \"local_imaging\" is not available");
		}
	}

	private void startGroupActionFrame() {
		Logging.info(this, "startGroupActionFrame clientsFilteredByTree " + activatedGroupModel.getAssociatedClients()
				+ " active " + activatedGroupModel.isActive());

		if (!activatedGroupModel.isActive()) {
			FTextArea f = new FTextArea(mainFrame, Configed.getResourceValue("information"),
					Configed.getResourceValue("ConfigedMain.noGroupSelected"), true,
					new String[] { Configed.getResourceValue("buttonClose") }, 400, 200);

			f.setVisible(true);

			return;
		}

		if (groupActionFrame == null) {
			groupActionFrame = new FGroupActions(this);
			groupActionFrame.setSize(LICENSES_DIMENSION);
			groupActionFrame.centerOnParent();

			allFrames.add(groupActionFrame);
		}

		groupActionFrame.start();
	}

	public void handleProductActionRequest() {
		startProductActionFrame();
	}

	private void startProductActionFrame() {
		Logging.info(this, "startProductActionFrame ");

		if (productActionFrame == null) {
			productActionFrame = new FProductActions(this);
			productActionFrame.setSize(LICENSES_DIMENSION);
			productActionFrame.centerOnParent();
			allFrames.add(productActionFrame);
		}

		productActionFrame.start();
	}

	public void handleLicensesManagementRequest() {
		// show Loading pane only when something needs to be loaded from server
		if (persistenceController.getModuleDataService().isWithLicenseManagementPD() && licensesFrame == null) {
			mainFrame.activateLoadingPane(Configed.getResourceValue("ConfigedMain.Licenses.Loading"));
		}
		new Thread() {
			@Override
			public void run() {
				Logging.info(this, "handleLicensesManagementRequest called");
				persistenceController.getModuleDataService().retrieveOpsiModules();

				if (persistenceController.getModuleDataService().isWithLicenseManagementPD()) {
					toggleLicensesFrame();
				} else {
					FOpsiLicenseMissingText
							.callInstanceWith(Configed.getResourceValue("ConfigedMain.LicensemanagementNotActive"));
				}

				if (Boolean.TRUE.equals(persistenceController.getConfigDataService().getGlobalBooleanConfigValue(
						OpsiServiceNOMPersistenceController.KEY_SHOW_DASH_FOR_LICENSEMANAGEMENT,
						OpsiServiceNOMPersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENSEMANAGEMENT))) {
					// Starting JavaFX-Thread by creating a new JFXPanel, but not
					// using it since it is not needed.
					new JFXPanel();

					Platform.runLater(mainFrame::startLicenseDisplayer);
				}

				mainFrame.deactivateLoadingPane();
			}
		}.start();
	}

	public void toggleLicensesFrame() {
		if (licensesFrame == null) {
			isInitialLicenseDataLoading = true;
			initLicensesFrame();
			allFrames.add(licensesFrame);
			isInitialLicenseDataLoading = false;
		}

		Logging.info(this, "toggleLicensesFrame is visible" + licensesFrame.isVisible());
		licensesFrame.setLocationRelativeTo(mainFrame);
		licensesFrame.setVisible(true);
		mainFrame.visualizeLicensesFramesActive(true);
	}

	public void setEditingTarget(EditingTarget t) {
		Logging.info(this, "setEditingTarget " + t);
		editingTarget = t;
		mainFrame.visualizeEditingTarget(t);
		int previousViewIndex = getViewIndex();
		// what else to do:
		switch (t) {
		case CLIENTS:
			setEditingClients();
			break;
		case DEPOTS:
			setEditingDepots();
			break;
		case SERVER:
			setEditingServer();
			break;
		default:
			break;
		}
		if (getViewIndex() == previousViewIndex) {
			resetView(viewIndex);
		}
	}

	private void setEditingClients() {
		Logging.debug(this, "setEditingTarget preSaveSelectedClients " + preSaveSelectedClients);

		clientTree.setEnabled(true);
		depotsList.setEnabled(true);
		depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		mainFrame.setConfigPanesEnabled(true);
		mainFrame.setConfigPaneEnabled(
				mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.jPanel_HostProperties")), false);
		mainFrame.setConfigPaneEnabled(
				mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), false);
		mainFrame.setVisualViewIndex(saveClientsViewIndex);

		Logging.debug(this, "setEditingTarget preSaveSelectedClients " + preSaveSelectedClients);

		if (preSaveSelectedClients != null && !preSaveSelectedClients.isEmpty()) {
			clientTable.setSelectedValues(preSaveSelectedClients);
		}
	}

	private void setEditingDepots() {
		Logging.info(this, "setEditingTarget  DEPOTS");

		depotsList.setEnabled(true);
		depotsList.requestFocus();
		depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		clientTree.setEnabled(false);

		initServer();
		mainFrame.setConfigPanesEnabled(false);
		mainFrame.setConfigPaneEnabled(
				mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.jPanel_HostProperties")), true);
		mainFrame.setConfigPaneEnabled(
				mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), true);
		mainFrame.setConfigPaneEnabled(
				mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig")),
				ServerFacade.isOpsi43());

		Logging.info(this, "setEditingTarget  call setVisualIndex  saved " + saveDepotsViewIndex + " resp. "
				+ mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		mainFrame.setVisualViewIndex(saveDepotsViewIndex);
	}

	private void setEditingServer() {
		clientTree.setEnabled(false);

		initServer();
		mainFrame.setConfigPanesEnabled(false);
		mainFrame.setConfigPaneEnabled(
				mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig")), true);

		mainFrame.setVisualViewIndex(saveServerViewIndex);
	}

	public void actOnListSelection() {
		Logging.info(this, "actOnListSelection");

		checkSaveAll(true);
		checkErrorList();

		Logging.info(this, "selectionPanel.getSelectedValues().size(): " + clientTable.getSelectedValues().size());

		// when initializing the program the frame may not exist
		if (mainFrame != null) {
			Logging.info(this, "ListSelectionListener valueChanged selectionPanel.isSelectionEmpty() "
					+ clientTable.isSelectionEmpty());
			setSelectedClients(clientTable.getSelectedValues());

			clientInDepot = "";

			hostInfo.initialize();

			updateHostInfo();

			mainFrame.setClientInfoediting(getSelectedClients().size() == 1);

			// initialize the following method
			depotsOfSelectedClients = null;
			depotsOfSelectedClients = getDepotsOfSelectedClients();
			Iterator<String> selectedDepotsIterator = depotsOfSelectedClients.iterator();
			StringBuilder depotsAdded = new StringBuilder();

			String singleDepot = "";

			if (selectedDepotsIterator.hasNext()) {
				singleDepot = selectedDepotsIterator.next();
				depotsAdded.append(singleDepot);
			}

			while (selectedDepotsIterator.hasNext()) {
				String appS = selectedDepotsIterator.next();
				depotsAdded.append(";\n");
				depotsAdded.append(appS);
			}

			clientInDepot = depotsAdded.toString();

			if (getSelectedClients().size() == 1) {
				mainFrame.setClientID(getSelectedClients().get(0));
			} else {
				mainFrame.setClientID("");
			}

			hostInfo.resetGui();

			Logging.info(this, "actOnListSelection update hosts status selectedClients " + getSelectedClients().size()
					+ " as well as " + clientTable.getSelectedValues().size());

			mainFrame.getHostsStatusPanel().updateValues(clientCount, getSelectedClients().size(),
					getSelectedClientsStringWithMaxLength(HostsStatusPanel.MAX_CLIENT_NAMES_IN_FIELD), clientInDepot);

			activatedGroupModel.setActive(getSelectedClients().isEmpty());

			// request reloading of client list depending data

			requestRefreshDataForClientSelection();
		}
	}

	private void updateHostInfo() {
		Map<String, HostInfo> pcinfos = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps();

		Logging.info(this,
				"updateHostInfo, produce hostInfo  getSelectedClients().length " + getSelectedClients().size());

		if (!getSelectedClients().isEmpty()) {
			hostInfo.setBy(pcinfos.get(getSelectedClients().get(0)).getMap());

			Logging.debug(this, "updateHostInfo, produce hostInfo first selClient " + getSelectedClients().get(0));
			Logging.debug(this, "updateHostInfo, produce hostInfo  " + hostInfo);

			HostInfo secondInfo = new HostInfo();

			for (int i = 1; i < getSelectedClients().size(); i++) {
				secondInfo.setBy(pcinfos.get(getSelectedClients().get(i)).getMap());
				hostInfo.combineWith(secondInfo);
			}
		}
	}

	// ListSelectionListener for client list
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			actOnListSelection();
		}
	}

	// we call this after we have a PersistenceController
	private void initMainFrame() {
		initDepots();

		// create client selection panel
		clientTable = new ClientTable(this);

		clientTable.setModel(buildClientListTableModel(true));
		setSelectionPanelCols();

		clientTable.initSortKeys();

		startMainFrame(this, clientTable, depotsList, clientTree);
	}

	private void initDepots() {
		// create depotsList
		depotsList = new DepotsList();

		if (depotsListSelectionListener == null) {
			Logging.info(this, "create depotsListSelectionListener");
			depotsListSelectionListener = new ListSelectionListener() {
				private int counter;

				@Override
				public void valueChanged(ListSelectionEvent e) {
					counter++;
					Logging.info(this, "depotSelection event count  " + counter);

					if (!e.getValueIsAdjusting()) {
						depotsListValueChanged();
					}
				}
			};
		}

		depotsList.addListSelectionListener(depotsListSelectionListener);

		fetchDepots();

		depotsList.setInfo(depots);
		List<String> oldSelectedDepots = Arrays.asList(backslashPattern
				.matcher(Configed.getSavedStates().getProperty("selectedDepots", "")).replaceAll("").split(","));
		if (oldSelectedDepots.isEmpty()) {
			depotsList.setSelectedValue(myServer, true);
		} else {
			depotsList.setSelectedValues(oldSelectedDepots);
		}
	}

	private static void startMainFrame(ConfigedMain configedMain, ClientTable selectionPanel, DepotsList depotsList,
			ClientTree treeClients) {
		mainFrame = new MainFrame(configedMain, selectionPanel, depotsList, treeClients);

		// rearranging visual components
		mainFrame.validate();

		// center the frame:
		locateFrame();

		// init visual states
		Logging.debug(configedMain, "mainframe nearly initialized");
	}

	private static void locateFrame() {
		Rectangle screenRectangle = loginDialog.getGraphicsConfiguration().getBounds();
		int distance = Math.min(screenRectangle.width, screenRectangle.height) / 10;

		Logging.info("set size and location of mainFrame");

		// weird formula for size
		mainFrame.setSize(screenRectangle.width - distance, screenRectangle.height - distance);

		// Center mainFrame on screen of configed.fProgress
		mainFrame.setLocation((int) (screenRectangle.getCenterX() - mainFrame.getSize().getWidth() / 2),
				(int) (screenRectangle.getCenterY() - mainFrame.getSize().getHeight() / 2));
	}

	private void initLicensesFrame() {
		long startmillis = System.currentTimeMillis();
		Logging.info(this, "initLicensesFrame start ");
		initTableData();
		startLicensesFrame();
		long endmillis = System.currentTimeMillis();
		Logging.info(this, "initLicensesFrame  diff " + (endmillis - startmillis));
	}

	private void initTableData() {
		licensesStatus = LicensesTabStatus.LICENSEPOOL;

		// global table providers
		List<String> columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		List<String> classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		licensePoolTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!isAllLicenseDataReloaded()) {
							persistenceController.reloadData(ReloadEvent.LICENSE_POOL_DATA_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getLicenseDataService().getLicensePoolsPD();
					}
				}));

		columnNames = new ArrayList<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		licenseOptionsTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!isAllLicenseDataReloaded()) {
							persistenceController
									.reloadData(ReloadEvent.SOFTWARE_LICENSE_TO_LICENSE_POOL_DATA_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return persistenceController.getLicenseDataService().getRelationsSoftwareL2LPool();
					}
				}));

		columnNames = new ArrayList<>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		licenseContractsTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!isAllLicenseDataReloaded()) {
							persistenceController.reloadData(ReloadEvent.LICENSE_CONTRACT_DATA_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getLicenseDataService().getLicenseContractsPD();
					}
				}));

		columnNames = new ArrayList<>();
		columnNames.add(LicenseEntry.ID_KEY);
		columnNames.add(LicenseEntry.LICENSE_CONTRACT_ID_KEY);
		columnNames.add(LicenseEntry.TYPE_KEY);
		columnNames.add(LicenseEntry.MAX_INSTALLATIONS_KEY);
		columnNames.add(LicenseEntry.BOUND_TO_HOST_KEY);
		columnNames.add(LicenseEntry.EXPIRATION_DATE_KEY);

		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("de.uib.utilities.ExtendedInteger");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		softwarelicensesTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!isAllLicenseDataReloaded()) {
							persistenceController.reloadData(CacheIdentifier.LICENSES.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getLicenseDataService().getLicensesPD();
					}
				}));
	}

	private void startLicensesFrame() {
		licensesFrame = new LicensesFrame(this);
		Utils.setMasterFrame(licensesFrame);
		licensesFrame.setIconImage(Utils.getMainIcon());
		licensesFrame.setTitle(myServer + ":  " + Configed.getResourceValue("ConfigedMain.Licenses"));

		// panelAssignToLPools
		licensesPanelsTabNames.put(LicensesTabStatus.LICENSEPOOL,
				Configed.getResourceValue("ConfigedMain.Licenses.TabLicensepools"));
		ControlPanelAssignToLPools controlPanelAssignToLPools = new ControlPanelAssignToLPools(this);
		addClient(LicensesTabStatus.LICENSEPOOL, controlPanelAssignToLPools.getTabClient());
		allControlMultiTablePanels.add(controlPanelAssignToLPools);

		// panelEnterLicense
		licensesPanelsTabNames.put(LicensesTabStatus.ENTER_LICENSE,
				Configed.getResourceValue("ConfigedMain.Licenses.TabNewLicense"));
		ControlPanelEnterLicense controlPanelEnterLicense = new ControlPanelEnterLicense(this);
		addClient(LicensesTabStatus.ENTER_LICENSE, controlPanelEnterLicense.getTabClient());
		allControlMultiTablePanels.add(controlPanelEnterLicense);

		// panelEditLicense
		licensesPanelsTabNames.put(LicensesTabStatus.EDIT_LICENSE,
				Configed.getResourceValue("ConfigedMain.Licenses.TabEditLicense"));
		ControlPanelEditLicenses controlPanelEditLicenses = new ControlPanelEditLicenses(this);
		addClient(LicensesTabStatus.EDIT_LICENSE, controlPanelEditLicenses.getTabClient());
		allControlMultiTablePanels.add(controlPanelEditLicenses);

		// panelUsage
		licensesPanelsTabNames.put(LicensesTabStatus.USAGE,
				Configed.getResourceValue("ConfigedMain.Licenses.TabLicenseUsage"));
		ControlPanelLicensesUsage controlPanelLicensesUsage = new ControlPanelLicensesUsage(this);
		addClient(LicensesTabStatus.USAGE, controlPanelLicensesUsage.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicensesUsage);

		// panelReconciliation
		licensesPanelsTabNames.put(LicensesTabStatus.RECONCILIATION,
				Configed.getResourceValue("ConfigedMain.Licenses.TabLicenseReconciliation"));
		ControlPanelLicensesReconciliation controlPanelLicensesReconciliation = new ControlPanelLicensesReconciliation(
				this);
		addClient(LicensesTabStatus.RECONCILIATION, controlPanelLicensesReconciliation.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicensesReconciliation);

		// panelStatistics
		licensesPanelsTabNames.put(LicensesTabStatus.STATISTICS,
				Configed.getResourceValue("ConfigedMain.Licenses.TabStatistics"));
		ControlPanelLicensesStatistics controlPanelLicensesStatistics = new ControlPanelLicensesStatistics(this);
		addClient(LicensesTabStatus.STATISTICS, controlPanelLicensesStatistics.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicensesStatistics);

		licensesFrame.start();

		Logging.info(this, "set size and location of licensesFrame");

		licensesFrame.setSize(LICENSES_DIMENSION);
	}

	// returns true if we have a PersistenceController and are connected
	private void setupLoginDialog(List<String> savedServers) {
		Logging.debug(this, " create password dialog ");
		loginDialog = new LoginDialog(this);

		// set list of saved servers
		if (!savedServers.isEmpty()) {
			loginDialog.setServers(savedServers);
		}

		// check if we started with preferred values
		if (host != null && !host.isEmpty()) {
			loginDialog.setHost(host);
		}

		if (user != null) {
			loginDialog.setUser(user);
		}

		if (password != null) {
			loginDialog.setPassword(password);
		}

		Logging.info(this, "become interactive");

		loginDialog.setVisible(true);

		// This must be called last, so that loading frame for connection is called last
		// and on top of the login-frame
		if (host != null && user != null && password != null) {
			// Auto login
			Logging.info(this, "start with given credentials");

			loginDialog.tryConnecting();
		}
	}

	public void setPersistenceController(OpsiServiceNOMPersistenceController persis) {
		persistenceController = persis;
	}

	public void setAppTitle(String s) {
		appTitle = s;
	}

	public String getAppTitle() {
		return appTitle;
	}

	public DependenciesModel getDependenciesModel() {
		return dependenciesModel;
	}

	private Set<String> produceClientSetForDepots(Set<String> allowedClients) {
		Logging.info(this, " producePcListForDepots " + depotsList.getSelectedValuesList()
				+ " running with allowedClients " + allowedClients);
		Set<String> m = persistenceController.getHostInfoCollections()
				.getClientsForDepots(depotsList.getSelectedValuesList(), allowedClients);

		clientCount = m.size();

		if (mainFrame != null) {
			mainFrame.getHostsStatusPanel().updateValues(clientCount, null, null, null);

			if (persistenceController.getHostInfoCollections().getCountClients() == 0) {
				clientTable.setMissingDataPanel();
			} else {
				clientTable.setDataPanel();
			}
		}

		return m;
	}

	private TableModel buildClientListTableModel(boolean rebuildTree) {
		Logging.debug(this, "buildPclistTableModel rebuildTree " + rebuildTree);

		Set<String> clientsForTableModel = produceClientSetForDepots(null);

		Logging.debug(this, " unfilteredList ");

		buildPclistTableModelCounter++;
		Logging.info(this,
				"buildPclistTableModel, counter " + buildPclistTableModelCounter + "   rebuildTree  " + rebuildTree);

		if (rebuildTree) {
			Set<String> permittedHostGroups = null;

			if (!persistenceController.getUserRolesConfigDataService().isAccessToHostgroupsOnlyIfExplicitlyStatedPD()) {
				Logging.info(this, "buildPclistTableModel not full hostgroups permission");
				permittedHostGroups = persistenceController.getUserRolesConfigDataService().getHostGroupsPermitted();
			}

			rebuildTree(new TreeSet<>(clientsForTableModel), permittedHostGroups);
		}

		// changes the produced unfilteredList
		if (allowedClients != null) {
			clientsForTableModel = produceClientSetForDepots(allowedClients);

			Logging.info(this, " clientsForTableModel " + clientsForTableModel.size());

			buildPclistTableModelCounter++;
			Logging.info(this, "buildPclistTableModel, counter " + buildPclistTableModelCounter + "   rebuildTree  "
					+ rebuildTree);

			if (rebuildTree) {
				rebuildTree(new TreeSet<>(clientsForTableModel), null);
			}
		}

		clientsForTableModel.retainAll(clientsFilteredByTree);

		Logging.info(this, " filterClientList " + filterClientList);

		if (filterClientList) {
			Logging.info(this, "buildPclistTableModel with filterCLientList, number of selected pcs "
					+ getSelectedClients().size());

			// selected clients that are in the pclist0
			clientsForTableModel.retainAll(getSelectedClients());
		}

		// building table model
		return buildTableModel(clientsForTableModel);
	}

	private TableModel buildTableModel(Set<String> clientIds) {
		DefaultTableModel model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};

		Map<String, HostInfo> pcinfos = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps();

		hostDisplayFields = persistenceController.getHostDataService().getHostDisplayFields();

		for (Entry<String, Boolean> entry : hostDisplayFields.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {
				model.addColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel." + entry.getKey()));
			}
		}

		Logging.info(this, "buildPclistTableModel host_displayFields " + hostDisplayFields);

		for (String clientId : clientIds) {
			HostInfo pcinfo = pcinfos.get(clientId);
			if (pcinfo == null) {
				pcinfo = new HostInfo();
			}

			Map<String, Object> rowmap = pcinfo.getDisplayRowMap0();

			String sessionValue = "";
			if (sessionInfo.get(clientId) != null) {
				sessionValue = "" + sessionInfo.get(clientId);
			}

			rowmap.put(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL, sessionValue);
			rowmap.put(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, getConnectionInfoForClient(clientId));

			List<Object> rowItems = new ArrayList<>();

			for (Entry<String, Boolean> entry : hostDisplayFields.entrySet()) {
				if (Boolean.TRUE.equals(entry.getValue())) {
					rowItems.add(rowmap.get(entry.getKey()));
				}
			}

			model.addRow(rowItems.toArray());
		}

		Logging.info(this, "buildPclistTableModel, model column count " + model.getColumnCount());

		return model;
	}

	private void rebuildTree(Collection<String> allPCs, Set<String> permittedHostGroups) {
		Logging.debug(this, "buildPclistTableModel, rebuildTree, allPCs  " + allPCs);

		clientTree.clear();
		clientTree.setClientInfo(persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps());

		clientTree.produceTreeForALL(allPCs);

		clientTree.produceAndLinkGroups(persistenceController.getGroupDataService().getHostGroupsPD());

		Logging.info(this, "buildPclistTableModel, permittedHostGroups " + permittedHostGroups);
		Logging.info(this, "buildPclistTableModel, allPCs " + allPCs.size());
		allowedClients = clientTree.associateClientsToGroups(allPCs,
				persistenceController.getGroupDataService().getFObject2GroupsPD(), permittedHostGroups);

		if (allowedClients != null) {
			Logging.info(this, "buildPclistTableModel, allowedClients " + allowedClients.size());
		}
	}

	public void setClient(String clientName) {
		setClients(Collections.singletonList(clientName));
	}

	public void setClients(List<String> clientNames) {
		Logging.info(this, "setClients " + clientNames);
		if (clientNames == null) {
			clientTable.setSelectedValues(new ArrayList<>());
		} else {
			clientTable.setSelectedValues(clientNames);
		}
	}

	/**
	 * activates a group
	 *
	 * @param groupname
	 */
	public boolean activateGroup(boolean preferringOldSelection, String groupname) {
		Logging.info(this, "activateGroup  " + groupname);
		if (groupname == null) {
			return false;
		}

		if (!clientTree.groupNodesExists() || clientTree.getGroupNode(groupname) == null) {
			Logging.warning("no group " + groupname);
			return false;
		}

		GroupNode node = clientTree.getGroupNode(groupname);
		TreePath path = clientTree.getPathToNode(node);

		activateGroupByTree(preferringOldSelection, node, path);

		Logging.info(this, "expand activated  path " + path);
		clientTree.expandPath(path);

		return true;
	}

	/**
	 * activates a group and selects all clients
	 *
	 * @param groupname
	 */
	public void setGroup(String groupname) {
		Logging.info(this, "setGroup " + groupname);
		if (!activateGroup(true, groupname)) {
			return;
		}

		clientTable.setSelectedValues(clientsFilteredByTree);
	}

	private void requestRefreshDataForClientSelection() {
		Logging.info(this, "requestRefreshDataForClientSelection");
		requestReloadStatesAndActions();
		if (mainFrame.getControllerHWinfoMultiClients() != null) {
			mainFrame.getControllerHWinfoMultiClients().requestResetFilter();
		}
	}

	public void requestReloadStatesAndActions() {
		Logging.info(this, "requestReloadStatesAndActions");
		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTIES.toString());
		localbootStatesAndActionsUpdate = true;
		netbootStatesAndActionsUpdate = true;
	}

	public List<String> getSelectedClients() {
		return selectedClients;
	}

	private void setSelectedClientsArray(Collection<String> a) {
		if (a == null) {
			return;
		}

		Logging.info(this, "setSelectedClientsArray " + a.size());
		Logging.info(this, "selectedClients up to now size " + Logging.getSize(selectedClients));

		selectedClients = new ArrayList<>(a);
		if (selectedClients.isEmpty()) {
			firstSelectedClient = "";
		} else {
			firstSelectedClient = selectedClients.get(0);
		}

		Logging.info(this, "setSelectedClientsArray produced firstSelectedClient " + firstSelectedClient);

		mainFrame.getHostsStatusPanel().updateValues(clientCount, getSelectedClients().size(),
				getSelectedClientsString(), clientInDepot);
	}

	/**
	 * transports the selected values of the selection panel to the outer world
	 */
	public List<String> getSelectedClientsInTable() {
		if (clientTable == null) {
			return new ArrayList<>();
		}

		return clientTable.getSelectedValues();
	}

	private void setSelectedClients(List<String> clientNames) {
		if (clientNames == null) {
			Logging.info(this, "setSelectedClients clientNames null");
		} else {
			Logging.info(this, "setSelectedClients clientNames size " + clientNames.size());
		}

		if (clientNames == null) {
			return;
		}

		if (clientNames.equals(saveSelectedClients)) {
			Logging.info(this, "setSelectedClients clientNames.equals(saveSelectedClients)");
		}

		saveSelectedClients = clientNames;

		requestRefreshDataForClientSelection();

		setSelectedClientsArray(clientNames);

		clientTree.produceActiveParents(getSelectedClients());

		if (getViewIndex() != VIEW_CLIENTS) {
			// change in selection not via clientpage (i.e. via tree)

			Logging.debug(this, "getSelectedClients  " + getSelectedClients() + " ,  getViewIndex, viewClients: "
					+ getViewIndex() + ", " + VIEW_CLIENTS);
			int newViewIndex = getViewIndex();
			resetView(newViewIndex);
		}
	}

	public boolean isFilterClientList() {
		return filterClientList;
	}

	public void toggleFilterClientList(boolean rebuildClientListTableModel) {
		Logging.info(this, "toggleFilterClientList   " + filterClientList + " rebuild client list table model "
				+ rebuildClientListTableModel);

		filterClientList = !filterClientList;
		if (rebuildClientListTableModel) {
			setRebuiltClientListTableModel(true, false, clientTable.getSelectedSet());
		}
	}

	private void setSelectionPanelCols() {
		Logging.info(this, "setSelectionPanelCols ");

		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL))) {
			int col = clientTable.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL));

			TableColumn column = clientTable.getColumnModel().getColumn(col);

			column.setMaxWidth(ICON_COLUMN_MAX_WIDTH);

			column.setCellRenderer(new ConnectionStatusTableCellRenderer());
		}

		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL))) {
			List<String> columns = new ArrayList<>();
			for (int i = 0; i < clientTable.getTableModel().getColumnCount(); i++) {
				columns.add(clientTable.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);

			int col = clientTable.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col " + col);

			Logging.info(this, "showAndSave found col " + col);

			initSelectionPanelColumn(col);
		}

		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL))) {
			List<String> columns = new ArrayList<>();
			for (int i = 0; i < clientTable.getTableModel().getColumnCount(); i++) {
				columns.add(clientTable.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);

			int col = clientTable.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col " + col);

			initSelectionPanelColumn(col);
		}

		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL))) {
			List<String> columns = new ArrayList<>();

			for (int i = 0; i < clientTable.getTableModel().getColumnCount(); i++) {
				columns.add(clientTable.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);

			int col = clientTable.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col " + col);

			initSelectionPanelColumn(col);
		}
	}

	private void initSelectionPanelColumn(int col) {
		if (col > -1) {
			TableColumn column = clientTable.getColumnModel().getColumn(col);
			Logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue());
			column.setMaxWidth(ICON_COLUMN_MAX_WIDTH);
			column.setCellRenderer(
					new BooleanIconTableCellRenderer(Utils.createImageIcon("images/checked_withoutbox.png", ""), null));
		}
	}

	private void setRebuiltClientListTableModel(boolean restoreSortKeys) {
		Logging.info(this, "setRebuiltClientListTableModel, we have selected Set : " + clientTable.getSelectedSet());

		setRebuiltClientListTableModel(restoreSortKeys, true, clientTable.getSelectedSet());
	}

	private void setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree,
			Set<String> selectValues) {
		if (mainFrame != null) {
			mainFrame.activateLoadingCursor();
		}
		Logging.info(this,
				"setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree, Set selectValues)  : "
						+ restoreSortKeys + ", " + rebuildTree + ",  selectValues.size() "
						+ Logging.getSize(selectValues));

		List<? extends SortKey> saveSortKeys = clientTable.getSortKeys();

		Logging.info(this,
				" setRebuiltClientListTableModel--- set model new, selected " + clientTable.getSelectedValues().size());

		TableModel tm = buildClientListTableModel(rebuildTree);
		Logging.info(this,
				"setRebuiltClientListTableModel --- got model selected " + clientTable.getSelectedValues().size());

		int[] columnWidths = getTableColumnWidths(clientTable.getTable());

		clientTable.deactivateListSelectionListener();
		clientTable.setModel(tm);
		clientTable.activateListSelectionListener();

		setTableColumnWidths(clientTable.getTable(), columnWidths);

		clientTable.initColumnNames();
		Logging.debug(this, " --- model set  ");

		setSelectionPanelCols();

		if (restoreSortKeys) {
			clientTable.setSortKeys(saveSortKeys);
		}

		Logging.info(this, "setRebuiltClientListTableModel set selected values in setRebuiltClientListTableModel() "
				+ Logging.getSize(selectValues));
		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel"
				+ Logging.getSize(clientTable.getSelectedValues()));

		// did lose the selection since last setting
		clientTable.setSelectedValues(selectValues);

		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel "
				+ Logging.getSize(clientTable.getSelectedValues()));

		reloadCounter++;
		Logging.info(this, "setRebuiltClientListTableModel  reloadCounter " + reloadCounter);
		if (mainFrame != null) {
			mainFrame.deactivateLoadingCursor();
		}
	}

	private String getSelectedClientsString() {
		return getSelectedClientsStringWithMaxLength(null);
	}

	private String getSelectedClientsStringWithMaxLength(Integer max) {
		return Utils.getListStringRepresentation(getSelectedClients(), max);
	}

	private String getSelectedDepotsString() {
		return Utils.getListStringRepresentation(depotsList.getSelectedValuesList(), null);
	}

	private Set<String> getDepotsOfSelectedClients() {
		if (depotsOfSelectedClients == null) {
			depotsOfSelectedClients = new TreeSet<>();
		}

		for (String selectedClient : getSelectedClients()) {
			if (persistenceController.getHostInfoCollections().getMapPcBelongsToDepot().get(selectedClient) != null) {
				depotsOfSelectedClients.add(
						persistenceController.getHostInfoCollections().getMapPcBelongsToDepot().get(selectedClient));
			}
		}

		return depotsOfSelectedClients;
	}

	private void collectTheProductProperties(String productEdited) {
		// we build
		// --
		// -- the map of the merged product properties from combining the properties of
		// all selected clients

		Logging.info(this, "collectTheProductProperties for " + productEdited);
		mergedProductProperties = new HashMap<>();
		productProperties = new ArrayList<>(getSelectedClients().size());

		if (!getSelectedClients().isEmpty() && possibleActions.get(productEdited) != null) {
			Map<String, Object> productPropertiesFor1Client = persistenceController.getProductDataService()
					.getProductPropertiesPD(getSelectedClients().get(0), productEdited);

			if (productPropertiesFor1Client != null) {
				productProperties.add(productPropertiesFor1Client);

				for (Entry<String, Object> productProperty : productPropertiesFor1Client.entrySet()) {
					// create a merger for product property
					ListMerger merger = new ListMerger((List<?>) productProperty.getValue());

					mergedProductProperties.put(productProperty.getKey(), merger);
				}

				// merge the other clients
				mergeOtherClients(productEdited);
			}
		}
	}

	private void mergeOtherClients(String productEdited) {
		for (int i = 1; i < getSelectedClients().size(); i++) {
			String selectedClient = getSelectedClients().get(i);

			Map<String, Object> productPropertiesFor1Client = persistenceController.getProductDataService()
					.getProductPropertiesPD(selectedClient, productEdited);

			productProperties.add(productPropertiesFor1Client);

			for (Entry<String, Object> productProperty : productPropertiesFor1Client.entrySet()) {
				List<?> value = (List<?>) productProperty.getValue();

				if (mergedProductProperties.get(productProperty.getValue()) == null) {
					// we need a new property. it is not common

					ListMerger merger = new ListMerger(value);

					merger.setHavingNoCommonValue();
					mergedProductProperties.put(productProperty.getKey(), merger);
				} else {
					ListMerger merger = mergedProductProperties.get(productProperty.getKey());

					ListMerger mergedValue = merger.merge(value);

					// on merging we check if the value is the same as before
					mergedProductProperties.put(productProperty.getKey(), mergedValue);
				}
			}
		}
	}

	private static void clearListEditors() {
		mainFrame.getPanelLocalbootProductSettings().clearListEditors();
		mainFrame.getPanelNetbootProductSettings().clearListEditors();
	}

	public void setProductEdited(String productname) {
		// called from ProductSettings

		Logging.debug(this, "setProductEdited " + productname);

		if (clientProductpropertiesUpdateCollection != null) {
			updateCollection.remove(clientProductpropertiesUpdateCollection);
		}
		clientProductpropertiesUpdateCollection = null;

		if (clientProductpropertiesUpdateCollections.get(productname) == null) {
			// have we got already a clientProductpropertiesUpdateCollection for this
			// product?
			// if not, we produce one

			clientProductpropertiesUpdateCollection = new ProductpropertiesUpdateCollection(getSelectedClients(),
					productname);

			clientProductpropertiesUpdateCollections.put(productname, clientProductpropertiesUpdateCollection);
			addToGlobalUpdateCollection(clientProductpropertiesUpdateCollection);
		} else {
			clientProductpropertiesUpdateCollection = clientProductpropertiesUpdateCollections.get(productname);
		}

		collectTheProductProperties(productname);

		dependenciesModel.setActualProduct(productname);

		Logging.debug(this, " --- mergedProductProperties " + mergedProductProperties);

		Logging.debug(this, "setProductEdited " + productname + " client specific properties "
				+ persistenceController.getProductDataService().hasClientSpecificProperties(productname));

		mainFrame.getPanelLocalbootProductSettings().initEditing(productname,
				persistenceController.getProductDataService().getProductTitle(productname),
				persistenceController.getProductDataService().getProductInfo(productname),
				persistenceController.getProductDataService().getProductHint(productname),
				persistenceController.getProductDataService().getProductVersion(productname)
						+ ProductPackageVersionSeparator.FOR_DISPLAY
						+ persistenceController.getProductDataService().getProductPackageVersion(productname) + "   "
						+ persistenceController.getProductDataService().getProductLockedInfo(productname),
				// List of the properties map of all selected clients
				productProperties,
				// these properties merged to one map
				mergedProductProperties,

				// editmappanelx
				persistenceController.getProductDataService().getProductPropertyOptionsMap(productname),

				clientProductpropertiesUpdateCollection);

		mainFrame.getPanelNetbootProductSettings().initEditing(productname,
				persistenceController.getProductDataService().getProductTitle(productname),
				persistenceController.getProductDataService().getProductInfo(productname),
				persistenceController.getProductDataService().getProductHint(productname),
				persistenceController.getProductDataService().getProductVersion(productname)
						+ ProductPackageVersionSeparator.FOR_DISPLAY
						+ persistenceController.getProductDataService().getProductPackageVersion(productname) + "   "
						+ persistenceController.getProductDataService().getProductLockedInfo(productname),
				// array of the properties map of all selected clients
				productProperties,
				// these properties merged to one map
				mergedProductProperties,

				// editmappanelx
				persistenceController.getProductDataService().getProductPropertyOptionsMap(productname),

				clientProductpropertiesUpdateCollection);
	}

	public int getViewIndex() {
		return viewIndex;
	}

	private boolean treeClientsSelectAction(TreePath newSelectedPath) {
		Logging.info(this, "treeClientsSelectAction");

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) newSelectedPath.getLastPathComponent();
		Logging.info(this, "treeClientsSelectAction selected node " + selectedNode);

		if (selectedNode.getAllowsChildren()) {
			activateGroupByTree(false, selectedNode, newSelectedPath);
		} else {
			setClientByTree(selectedNode, newSelectedPath);
		}

		return true;
	}

	public void treeClientsSelectAction(TreePath[] selTreePaths) {
		clearTree();

		clientsFilteredByTree.clear();
		if (selTreePaths != null) {
			for (TreePath selectionPath : selTreePaths) {
				clientsFilteredByTree.add(selectionPath.getLastPathComponent().toString());
			}
		}

		if (selTreePaths == null) {
			setRebuiltClientListTableModel(true, false, clientsFilteredByTree);
			mainFrame.getHostsStatusPanel().setGroupName("");
			mainFrame.getHostsStatusPanel().updateValues(clientCount, getSelectedClients().size(),
					getSelectedClientsString(), clientInDepot);
		} else if (selTreePaths.length == 1) {
			treeClientsSelectAction(selTreePaths[0]);
		} else {
			Logging.info(this, "treeClientsSelectAction selTreePaths: " + selTreePaths.length);
			for (TreePath selectedTreePath : selTreePaths) {
				DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) selectedTreePath.getLastPathComponent();

				if (selNode.getAllowsChildren()) {
					continue;
				}

				clientTree.collectParentIDsFrom(selNode);
			}

			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selTreePaths[selTreePaths.length - 1]
					.getLastPathComponent();

			setClientByTree(selectedNode, selTreePaths[selTreePaths.length - 1]);
		}
	}

	private void setGroupNameForNode(DefaultMutableTreeNode selectedNode) {
		if (getSelectedClients().size() == 1 && selectedNode.getParent() != null) {
			mainFrame.getHostsStatusPanel().setGroupName(selectedNode.getParent().toString());
		} else {
			mainFrame.getHostsStatusPanel().setGroupName("");
		}
	}

	private void initTree() {
		Logging.debug(this, "initTree");

		clientTree = new ClientTree(this);
		persistenceController.getHostInfoCollections().setTree(clientTree);
	}

	private void setClientByTree(DefaultMutableTreeNode selectedNode, TreePath pathToNode) {
		activateClientByTree(pathToNode);
		setRebuiltClientListTableModel(true, false, clientsFilteredByTree);

		setGroupNameForNode(selectedNode);

		mainFrame.getHostsStatusPanel().updateValues(clientCount, getSelectedClients().size(),
				getSelectedClientsString(), clientInDepot);
	}

	private void activateClientByTree(TreePath pathToNode) {
		Logging.info(this, "activateClientByTree, pathToNode: " + pathToNode);

		clientTree.collectParentIDsFrom((DefaultMutableTreeNode) pathToNode.getLastPathComponent());

		clientTree.repaint();

		// since we select based on the tree view we disable the filter
		if (filterClientList) {
			mainFrame.toggleClientFilterAction(false);
		}
	}

	public void clearTree() {
		clientTree.initActiveParents();
	}

	private void setGroupByTree(DefaultMutableTreeNode node) {
		Logging.info(this, "setGroupByTree, node " + node);
		clearTree();

		// Get all leaves from the node which should be a group
		clientsFilteredByTree.clear();
		Enumeration<TreeNode> e = node.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();

			if (!element.getAllowsChildren()) {
				String nodeinfo = (String) element.getUserObject();
				clientsFilteredByTree.add(nodeinfo);
			}
		}

		clientTree.repaint();
	}

	private void activateGroupByTree(boolean preferringOldSelection, DefaultMutableTreeNode node, TreePath pathToNode) {
		Logging.info(this, "activateGroupByTree, node: " + node + ", pathToNode : " + pathToNode);

		setGroupByTree(node);

		// intended for reload, we cancel activating group
		if (preferringOldSelection && !clientTable.getSelectedSet().isEmpty()) {
			return;
		}

		setRebuiltClientListTableModel(true, false, null);
		// with this, a selected client remains selected (but in bottom line, the group
		// seems activated, not the client)

		groupPathActivatedByTree = pathToNode;

		activatedGroupModel.setNode("" + node);
		activatedGroupModel.setDescription(clientTree.getGroups().get("" + node).get("description"));
		activatedGroupModel.setAssociatedClients(clientsFilteredByTree);
		activatedGroupModel.setActive(true);

		// since we select based on the tree view we disable the filter
		if (filterClientList) {
			mainFrame.toggleClientFilterAction();
		}
	}

	public TreePath getGroupPathActivatedByTree() {
		return groupPathActivatedByTree;
	}

	public ActivatedGroupModel getActivatedGroupModel() {
		return activatedGroupModel;
	}

	public Set<String> getActiveParents() {
		if (clientTree == null) {
			return new HashSet<>();
		}

		return clientTree.getActiveParents();
	}

	public boolean addGroup(StringValuedRelationElement newGroup) {
		return persistenceController.getGroupDataService().addGroup(newGroup);
	}

	public boolean updateGroup(String groupId, Map<String, String> groupInfo) {
		return persistenceController.getGroupDataService().updateGroup(groupId, groupInfo);
	}

	public boolean deleteGroup(String groupId) {
		return persistenceController.getGroupDataService().deleteGroup(groupId);
	}

	public boolean removeHostGroupElements(List<Object2GroupEntry> entries) {
		return persistenceController.getGroupDataService().removeHostGroupElements(entries);
	}

	public boolean removeObject2Group(String objectId, String groupId) {
		return persistenceController.getGroupDataService().removeObject2Group(objectId, groupId);
	}

	public boolean addObject2Group(String objectId, String groupId) {
		return persistenceController.getGroupDataService().addObject2Group(objectId, groupId);
	}

	public boolean setProductGroup(String groupId, String description, Set<String> productSet) {
		return persistenceController.getGroupDataService().setProductGroup(groupId, description, productSet);
	}

	private void depotsListValueChanged() {
		Logging.info(this, "depotsList selection changed");

		if (initialDataLoader.isDataLoaded()) {
			mainFrame.activateLoadingCursor();
		}

		// when running after the first run, we deactivate buttons

		depotsOfSelectedClients = null;
		if (initialDataLoader.isDataLoaded()) {
			refreshClientListKeepingGroup();
		}
		Configed.getSavedStates().setProperty("selectedDepots", depotsList.getSelectedValuesList().toString());

		Logging.info(this, " depotsList_valueChanged, omitted initialTreeActivation");

		if (initialDataLoader.isDataLoaded()) {
			initialTreeActivation();
			clientTable.clearSelection();
		}

		setViewIndex(getViewIndex());

		if (initialDataLoader.isDataLoaded()) {
			mainFrame.deactivateLoadingCursor();
		}
	}

	private boolean checkSynchronous(Set<String> depots) {
		if (depots.size() > 1 && !persistenceController.getDepotDataService().areDepotsSynchronous(depots)) {
			JOptionPane.showMessageDialog(mainFrame, Configed.getResourceValue("ConfigedMain.notSynchronous.text"),
					Configed.getResourceValue("ConfigedMain.notSynchronous.title"), JOptionPane.OK_OPTION);

			return false;
		}

		return true;
	}

	private boolean setDepotRepresentative() {
		Logging.debug(this, "setDepotRepresentative");

		if (getSelectedClients().isEmpty()) {
			if (depotRepresentative == null) {
				depotRepresentative = myServer;
			}

			return true;
		}

		depotsOfSelectedClients = getDepotsOfSelectedClients();

		Logging.info(this, "depots of selected clients:" + depotsOfSelectedClients);

		Logging.debug(this, "setDepotRepresentative(), old representative: " + depotRepresentative + " should be ");

		if (!checkSynchronous(depotsOfSelectedClients)) {
			return false;
		}

		String oldRepresentative = depotRepresentative;

		Logging.debug(this, "setDepotRepresentative  start  " + " up to now " + oldRepresentative + " old"
				+ depotRepresentative + " equal " + oldRepresentative.equals(depotRepresentative));

		depotRepresentative = null;

		Logging.info(this, "setDepotRepresentative depotsOfSelectedClients " + depotsOfSelectedClients);

		Iterator<String> depotsIterator = depotsOfSelectedClients.iterator();

		if (!depotsIterator.hasNext()) {
			depotRepresentative = myServer;
			Logging.debug(this,
					"setDepotRepresentative  without next change depotRepresentative " + " up to now "
							+ oldRepresentative + " new " + depotRepresentative + " equal "
							+ oldRepresentative.equals(depotRepresentative));
		} else {
			depotRepresentative = depotsIterator.next();

			while (!depotRepresentative.equals(myServer) && depotsIterator.hasNext()) {
				String depot = depotsIterator.next();
				if (depot.equals(myServer)) {
					depotRepresentative = myServer;
				}
			}
		}

		Logging.debug(this, "depotRepresentative: " + depotRepresentative);

		Logging.info(this, "setDepotRepresentative  change depotRepresentative " + " up to now " + oldRepresentative
				+ " new " + depotRepresentative + " equal " + oldRepresentative.equals(depotRepresentative));

		if (!oldRepresentative.equals(depotRepresentative)) {
			Logging.info(this, " new depotRepresentative " + depotRepresentative);
			persistenceController.getDepotDataService().setDepot(depotRepresentative);

			// everything
			persistenceController.reloadData(ReloadEvent.DEPOT_CHANGE_RELOAD.toString());
		}

		return true;
	}

	private List<String> getLocalbootProductDisplayFieldsList() {
		List<String> result = new ArrayList<>();
		for (Entry<String, Boolean> productDisplay : displayFieldsLocalbootProducts.entrySet()) {
			if (Boolean.TRUE.equals(productDisplay.getValue())) {
				result.add(productDisplay.getKey());
			}
		}

		return result;
	}

	private List<String> getNetbootProductDisplayFieldsList() {
		List<String> result = new ArrayList<>();

		for (Entry<String, Boolean> productDisplay : displayFieldsNetbootProducts.entrySet()) {
			if (Boolean.TRUE.equals(productDisplay.getValue())) {
				result.add(productDisplay.getKey());
			}
		}

		return result;
	}

	public Map<String, Boolean> getDisplayFieldsNetbootProducts() {
		return displayFieldsNetbootProducts;
	}

	public Map<String, Boolean> getDisplayFieldsLocalbootProducts() {
		return displayFieldsLocalbootProducts;
	}

	// enables and disables tabs depending if they make sense in other cases
	private boolean checkOneClientSelected() {
		Logging.debug(this, "checkOneClientSelected() selectedClients " + getSelectedClients());
		return getSelectedClients().size() == 1;
	}

	private boolean setProductsPage(boolean updateStatesAndActions, List<String> attributes, String productServerString,
			PanelProductSettings panelProductSettings, List<String> productNames, List<String> displayFields,
			String savedStateObjectTag) {
		if (!setDepotRepresentative()) {
			return false;
		}
		Map<String, List<Map<String, String>>> statesAndActions = null;
		if (updateStatesAndActions) {
			statesAndActions = persistenceController.getProductDataService()
					.getMapOfProductStatesAndActions(getSelectedClients(), attributes, productServerString);
			updateStatesAndActions = true;
		}

		clientProductpropertiesUpdateCollections = new HashMap<>();
		panelProductSettings.initAllProperties();

		Logging.debug(this, "setLocalbootProductsPage,  depotRepresentative:" + depotRepresentative);
		possibleActions = persistenceController.getProductDataService().getPossibleActionsPD(depotRepresentative);

		// we retrieve the properties for all clients and products

		// it is necessary to do this before resetting selection below (*) since there a
		// listener is triggered
		// which loads the productProperties for each client separately

		persistenceController.getProductDataService().retrieveProductPropertiesPD(clientTable.getSelectedValues());

		Set<String> oldProductSelection = panelProductSettings.getSelectedIDs();

		List<? extends SortKey> currentSortKeysProducts = panelProductSettings.getSortKeys();

		Logging.info(this, "setLocalbootProductsPage: oldProductSelection " + oldProductSelection);

		Logging.debug(this, "setLocalbootProductsPage: collectChangedLocalbootStates " + collectChangedLocalbootStates);

		int[] columnWidths = getTableColumnWidths(panelProductSettings.getTableProducts());

		if (updateStatesAndActions) {
			InstallationStateTableModel istmForSelectedClients = new InstallationStateTableModel(getSelectedClients(),
					this, collectChangedLocalbootStates, productNames, statesAndActions, possibleActions,
					persistenceController.getProductDataService().getProductGlobalInfosPD(depotRepresentative),
					displayFields, savedStateObjectTag);

			panelProductSettings.setTableModel(istmForSelectedClients);
		}

		panelProductSettings.setSortKeys(currentSortKeysProducts);

		Logging.info(this, "resetFilter " + Configed.getSavedStates()
				.getProperty(savedStateObjectTag + "." + InstallationStateTableModel.STATE_TABLE_FILTERS_PROPERTY));

		Logging.info(this, "resetFilter " + Configed.getSavedStates()
				.getProperty(savedStateObjectTag + "." + InstallationStateTableModel.STATE_TABLE_FILTERS_PROPERTY));

		Set<String> savedFilter = null;

		if (Configed.getSavedStates().getProperty(
				savedStateObjectTag + "." + InstallationStateTableModel.STATE_TABLE_FILTERS_PROPERTY) != null) {
			savedFilter = new HashSet<>(
					Arrays.asList(backslashPattern
							.matcher(Configed.getSavedStates()
									.getProperty(savedStateObjectTag + "."
											+ InstallationStateTableModel.STATE_TABLE_FILTERS_PROPERTY))
							.replaceAll("").split(",")));
		}
		panelProductSettings.setGroupsData(productGroups, productGroupMembers);
		panelProductSettings.reduceToSet(savedFilter);

		Logging.info(this, "setLocalbootProductsPage oldProductSelection: " + oldProductSelection);
		panelProductSettings.setSelection(oldProductSelection);
		panelProductSettings.updateSearchFields();
		setTableColumnWidths(panelProductSettings.getTableProducts(), columnWidths);

		return true;
	}

	private boolean setLocalbootProductsPage() {
		return setProductsPage(localbootStatesAndActionsUpdate, getLocalbootStateAndActionsAttributes(),
				OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING, mainFrame.getPanelLocalbootProductSettings(),
				localbootProductnames, getLocalbootProductDisplayFieldsList(), "localbootProducts");
	}

	private boolean setNetbootProductsPage() {
		return setProductsPage(netbootStatesAndActionsUpdate, Collections.emptyList(),
				OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING, mainFrame.getPanelNetbootProductSettings(),
				netbootProductnames, getNetbootProductDisplayFieldsList(), "netbootProducts");
	}

	private List<String> getLocalbootStateAndActionsAttributes() {
		List<String> attributes = getAttributesFromProductDisplayFields(getLocalbootProductDisplayFieldsList());

		// Position is something different in opsi 4.3 than before...
		if (getLocalbootProductDisplayFieldsList().contains(ProductState.KEY_POSITION)) {
			attributes.remove(ProductState.KEY_POSITION);

			if (ServerFacade.isOpsi43()) {
				attributes.add(ActionSequence.KEY);
			}
		}

		if (getLocalbootProductDisplayFieldsList().contains(ProductState.KEY_INSTALLATION_INFO)) {
			attributes.add(ProductState.key2servicekey.get(ProductState.KEY_ACTION_PROGRESS));
			attributes.add(ProductState.key2servicekey.get(ProductState.KEY_LAST_ACTION));
		}

		// Remove uneeded attributes
		attributes.remove(ProductState.KEY_PRODUCT_PRIORITY);

		attributes.add(ProductState.key2servicekey.get(ProductState.KEY_LAST_STATE_CHANGE));
		return attributes;
	}

	private static List<String> getAttributesFromProductDisplayFields(List<String> productDisplayFields) {
		List<String> attributes = new ArrayList<>();
		for (String v : productDisplayFields) {
			if (ProductState.KEY_VERSION_INFO.equals(v)) {
				attributes.add(ProductState.key2servicekey.get(ProductState.KEY_PACKAGE_VERSION));
				attributes.add(ProductState.key2servicekey.get(ProductState.KEY_PRODUCT_VERSION));
				continue;
			}
			if (ProductState.KEY_INSTALLATION_INFO.equals(v)) {
				attributes.add(ProductState.key2servicekey.get(ProductState.KEY_ACTION_RESULT));
			}
			if (ProductState.key2servicekey.containsKey(v)) {
				attributes.add(ProductState.key2servicekey.get(v));
			}
		}

		return attributes;
	}

	private static int[] getTableColumnWidths(JTable table) {
		TableColumnModel columnModel = table.getColumnModel();
		int[] columnWidths = new int[columnModel.getColumnCount()];

		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			columnWidths[i] = columnModel.getColumn(i).getWidth();
		}

		return columnWidths;
	}

	// only has an effect if number of table columns not changed
	private static void setTableColumnWidths(JTable table, int[] columnWidths) {
		// Only do it if number of columns didn't change
		if (columnWidths.length == table.getColumnModel().getColumnCount()) {
			for (int i = 0; i < columnWidths.length; i++) {
				table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
				table.getColumnModel().getColumn(i).setWidth(columnWidths[i]);
			}
		}
	}

	private static Map<String, Object> mergeMaps(List<Map<String, Object>> collection) {
		Map<String, Object> mergedMap = new HashMap<>();
		if (collection == null || collection.isEmpty()) {
			return mergedMap;
		}

		Map<String, Object> mergeIn = collection.get(0);

		for (Entry<String, Object> entry : mergeIn.entrySet()) {
			List<?> value = (List<?>) entry.getValue();
			ListMerger merger = new ListMerger(value);
			mergedMap.put(entry.getKey(), merger);
		}

		// merge the other maps
		for (int i = 1; i < collection.size(); i++) {
			mergeIn = collection.get(i);

			for (Entry<String, Object> mergeInEntry : mergeIn.entrySet()) {
				List<?> value = (List<?>) mergeInEntry.getValue();

				if (mergedMap.get(mergeInEntry.getKey()) == null) {
					ListMerger merger = new ListMerger(value);
					merger.setHavingNoCommonValue();
					mergedMap.put(mergeInEntry.getKey(), merger);
				} else {
					ListMerger merger = (ListMerger) mergedMap.get(mergeInEntry.getKey());
					ListMerger mergedValue = merger.merge(value);
					mergedMap.put(mergeInEntry.getKey(), mergedValue);
				}
			}
		}

		return mergedMap;
	}

	private boolean setProductPropertiesPage() {
		if (editingTarget != EditingTarget.DEPOTS) {
			return false;
		}

		Logging.debug(this, "setProductPropertiesPage");
		mainFrame.getPanelProductProperties().setProductProperties();
		depotsList.setEnabled(true);
		depotsList.requestFocus();
		depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		return true;
	}

	private boolean setHostPropertiesPage() {
		if (editingTarget != EditingTarget.DEPOTS) {
			return false;
		}

		Logging.debug(this, "setHostPropertiesPage");

		depotsList.setEnabled(true);
		depotsList.requestFocus();
		depotsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		Map<String, Map<String, Object>> depotPropertiesForPermittedDepots = persistenceController.getDepotDataService()
				.getDepotPropertiesForPermittedDepots();

		if (hostUpdateCollection != null) {
			updateCollection.remove(hostUpdateCollection);
		}

		hostUpdateCollection = new HostUpdateCollection();
		addToGlobalUpdateCollection(hostUpdateCollection);

		String depot = "";
		if (!depotsList.getSelectedValuesList().isEmpty()) {
			depot = depotsList.getSelectedValuesList().get(0);
		}

		mainFrame.getPanelHostProperties().initMultipleHostsEditing(depot, depotPropertiesForPermittedDepots,
				hostUpdateCollection, OpsiServiceNOMPersistenceController.KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT);

		return true;
	}

	private static void removeKeysStartingWith(Map<String, ? extends Object> m, Set<String> keystartersStrNotWanted) {
		Set<String> keysForDeleting = new HashSet<>();

		for (String start : keystartersStrNotWanted) {
			for (String key : m.keySet()) {
				if (key.startsWith(start)) {
					keysForDeleting.add(key);
				}
			}
		}

		m.keySet().removeAll(keysForDeleting);
	}

	@SuppressWarnings({ "unchecked" })
	public boolean setNetworkConfigurationPage() {
		Logging.info(this, "setNetworkconfigurationPage ");
		Logging.info(this, "setNetworkconfigurationPage  getSelectedClients() " + getSelectedClients());

		List<String> objectIds = new ArrayList<>();
		if (editingTarget == EditingTarget.SERVER) {
			objectIds.add(myServer);
		} else if (editingTarget == EditingTarget.DEPOTS) {
			objectIds.addAll(depotsList.getSelectedValuesList());
		} else {
			objectIds.addAll(getSelectedClients());
		}

		if (additionalconfigurationUpdateCollection != null) {
			updateCollection.remove(additionalconfigurationUpdateCollection);
		}
		additionalconfigurationUpdateCollection = new AdditionalconfigurationUpdateCollection(objectIds);
		addToGlobalUpdateCollection(additionalconfigurationUpdateCollection);

		depotsList.setEnabled(false);

		if (editingTarget == EditingTarget.SERVER) {
			List<Map<String, List<Object>>> additionalConfigs = new ArrayList<>(1);
			Map<String, List<Object>> defaultValuesMap = persistenceController.getConfigDataService()
					.getConfigDefaultValuesPD();
			additionalConfigs.add(defaultValuesMap);
			additionalconfigurationUpdateCollection.setMasterConfig(true);
			mainFrame.getPanelHostConfig().initEditing("  " + myServer + " (configuration server)",
					additionalConfigs.get(0), persistenceController.getConfigDataService().getConfigListCellOptionsPD(),
					additionalConfigs, additionalconfigurationUpdateCollection, true,
					OpsiServiceNOMPersistenceController.getPropertyClassesServer());
		} else if (editingTarget == EditingTarget.DEPOTS) {
			depotsList.setEnabled(true);
			depotsList.requestFocus();
			depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			List<Map<String, Object>> additionalConfigs = produceAdditionalConfigs(getSelectedDepots());
			Map<String, Object> mergedVisualMap = mergeMaps(additionalConfigs);
			removeKeysStartingWith(mergedVisualMap,
					OpsiServiceNOMPersistenceController.getConfigKeyStartersNotForClients());
			Map<String, Object> originalMap = mergeMaps(
					persistenceController.getConfigDataService().getHostsConfigsWithoutDefaults(getSelectedDepots()));
			mainFrame.getPanelHostConfig().initEditing(getSelectedDepotsString(), mergedVisualMap,
					persistenceController.getConfigDataService().getConfigListCellOptionsPD(), additionalConfigs,
					additionalconfigurationUpdateCollection, false,
					OpsiServiceNOMPersistenceController.getPropertyClassesClient(), originalMap, false);
		} else {
			List<Map<String, Object>> additionalConfigs = produceAdditionalConfigs(getSelectedClients());
			Map<String, Object> mergedVisualMap = mergeMaps(additionalConfigs);
			removeKeysStartingWith(mergedVisualMap,
					OpsiServiceNOMPersistenceController.getConfigKeyStartersNotForClients());
			Map<String, ListCellOptions> configListCellOptions = deepCopyConfigListCellOptions(
					persistenceController.getConfigDataService().getConfigListCellOptionsPD());
			if (ServerFacade.isOpsi43() && !getSelectedClients().isEmpty()) {
				List<String> depotIds = new ArrayList<>();
				depotIds.add(persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps()
						.get(getSelectedClients().get(0)).getInDepot());
				Map<String, Object> defaultValues = persistenceController.getConfigDataService()
						.getHostsConfigsWithDefaults(depotIds).get(0);
				for (Entry<String, ListCellOptions> entry : configListCellOptions.entrySet()) {
					configListCellOptions.get(entry.getKey())
							.setDefaultValues((List<Object>) defaultValues.get(entry.getKey()));
				}
			}
			Map<String, Object> originalMap = mergeMaps(
					persistenceController.getConfigDataService().getHostsConfigsWithoutDefaults(getSelectedClients()));
			mainFrame.getPanelHostConfig().initEditing(getSelectedClientsString(), mergedVisualMap,
					configListCellOptions, additionalConfigs, additionalconfigurationUpdateCollection, false,
					OpsiServiceNOMPersistenceController.getPropertyClassesClient(), originalMap, true);
		}

		return true;
	}

	private static Map<String, ListCellOptions> deepCopyConfigListCellOptions(
			Map<String, ListCellOptions> originalMap) {
		Map<String, ListCellOptions> copy = new HashMap<>();
		for (Entry<String, ListCellOptions> entry : originalMap.entrySet()) {
			copy.put(entry.getKey(), entry.getValue().deepCopy());
		}
		return copy;
	}

	private List<Map<String, Object>> produceAdditionalConfigs(List<String> list) {
		List<Map<String, Object>> additionalConfigs = new ArrayList<>(list.size());
		if (list.isEmpty()) {
			return additionalConfigs;
		}
		Logging.info(this, "additionalConfig fetch for " + list);
		if (ServerFacade.isOpsi43()) {
			additionalConfigs = persistenceController.getConfigDataService().getHostsConfigsWithDefaults(list);
		} else {
			for (String item : list) {
				additionalConfigs.add(persistenceController.getConfigDataService().getHostConfig(item));
				// with server defaults
			}
		}
		return additionalConfigs;
	}

	private boolean setHardwareInfoPage() {
		Logging.info(this, "setHardwareInfoPage for, clients count " + getSelectedClients().size());

		if (firstSelectedClient == null || getSelectedClients().isEmpty()) {
			mainFrame.setHardwareInfoNotPossible(Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
		} else if (getSelectedClients().size() > 1) {
			if (persistenceController.getModuleDataService().canCallMySQLPD()) {
				mainFrame.setHardwareInfoMultiClients();
			} else {
				mainFrame.setHardwareInfoNotPossible(Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
			}
		} else {
			mainFrame.setHardwareInfo(
					persistenceController.getHardwareDataService().getHardwareInfo(firstSelectedClient));
		}

		return true;
	}

	private boolean setSoftwareInfoPage() {
		Logging.info(this, "setSoftwareInfoPage() firstSelectedClient, checkOneClientSelected " + firstSelectedClient
				+ ", " + checkOneClientSelected());

		if (firstSelectedClient == null || !checkOneClientSelected()) {
			mainFrame.setSoftwareAudit();
		} else {
			mainFrame.setSoftwareAudit(firstSelectedClient);
		}

		return true;
	}

	public boolean logfileExists(String logtype) {
		return logfiles != null && logfiles.get(logtype) != null && !logfiles.get(logtype).isEmpty()
				&& !logfiles.get(logtype).equals(Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
	}

	public Map<String, String> getLogfilesUpdating(String logtypeToUpdate) {
		Logging.info(this, "getLogfilesUpdating " + logtypeToUpdate);

		if (!checkOneClientSelected()) {
			for (String logType : Utils.getLogTypes()) {
				logfiles.put(logType, Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
			}
		} else {
			mainFrame.activateLoadingCursor();
			logfiles = persistenceController.getLogDataService().getLogfile(firstSelectedClient, logtypeToUpdate);
			mainFrame.deactivateLoadingCursor();
			Logging.debug(this, "log pages set");
		}

		return logfiles;
	}

	private boolean setLogPage() {
		Logging.debug(this, "setLogPage(), selected clients: " + getSelectedClients());
		mainFrame.setUpdatedLogfilePanel("instlog");
		mainFrame.setLogview("instlog");
		return true;
	}

	public boolean resetView(int viewIndex) {
		Logging.info(this, "resetView to " + viewIndex + "  getSelectedClients " + getSelectedClients().size());
		mainFrame.activateLoadingCursor();
		boolean result = true;

		switch (viewIndex) {
		case VIEW_CLIENTS:
			break;

		case VIEW_LOCALBOOT_PRODUCTS:
			result = setLocalbootProductsPage();
			break;

		case VIEW_NETBOOT_PRODUCTS:
			result = setNetbootProductsPage();
			break;

		case VIEW_NETWORK_CONFIGURATION:
			result = setNetworkConfigurationPage();
			break;

		case VIEW_HARDWARE_INFO:
			result = setHardwareInfoPage();
			break;

		case VIEW_SOFTWARE_INFO:
			result = setSoftwareInfoPage();
			break;

		case VIEW_LOG:
			result = setLogPage();
			break;

		case VIEW_PRODUCT_PROPERTIES:
			result = setProductPropertiesPage();
			break;

		case VIEW_HOST_PROPERTIES:
			result = setHostPropertiesPage();
			break;

		default:
			Logging.warning(this, "resetting View failed, no index for viewIndex: '" + viewIndex + "' found");
			break;
		}

		mainFrame.deactivateLoadingCursor();
		return result;
	}

	public void setVisualViewIndex(int i) {
		mainFrame.setVisualViewIndex(i);
	}

	public void setViewIndex(int visualViewIndex) {
		int oldViewIndex = viewIndex;

		Logging.info(this, "visualViewIndex " + visualViewIndex + ", (old) viewIndex " + viewIndex);
		Logging.info(this, "setViewIndex anyDataChanged " + anyDataChanged);

		checkSaveAll(true);

		if (initialDataLoader.isDataLoaded()) {
			viewIndex = visualViewIndex;
			depotsList.setEnabled(viewIndex == VIEW_CLIENTS);

			Logging.debug(this, "switch to viewIndex " + viewIndex);
			boolean result = resetView(viewIndex);

			if (!result) {
				viewIndex = oldViewIndex;
				Logging.debug(" tab index could not be changed");
			}

			saveCurrentViewIndex();
			if (result) {
				clearListEditors();
			}
		}
	}

	private void saveCurrentViewIndex() {
		switch (editingTarget) {
		case CLIENTS:
			saveClientsViewIndex = viewIndex;
			break;

		case DEPOTS:
			saveDepotsViewIndex = viewIndex;
			break;

		case SERVER:
			saveServerViewIndex = viewIndex;
			break;
		}
	}

	public void initServer() {
		checkSaveAll(true);
		preSaveSelectedClients = saveSelectedClients;
	}

	public List<String> getSelectedDepots() {
		return depotsList.getSelectedValuesList();
	}

	public Set<String> getAllowedClients() {
		return allowedClients;
	}

	public List<String> getProductNames() {
		return localbootProductnames;
	}

	protected String[] getDepotArray() {
		if (depots == null) {
			return new String[] {};
		}

		return depots.keySet().toArray(new String[0]);
	}

	private void fetchDepots() {
		Logging.info(this, "fetchDepots");

		depotNamesLinked = persistenceController.getHostInfoCollections().getDepotNamesList();
		Logging.debug(this, "fetchDepots sorted depots " + depotNamesLinked);

		depots = persistenceController.getHostInfoCollections().getDepots();
		List<String> oldSelection = depotsList.getSelectedValuesList();

		// Setting the list data will remove old selection. To prevent doing events twice
		// we set the flag that value is adjusting, because we will set the selected values again.
		// Both actions will then be united into one event only
		depotsList.setValueIsAdjusting(true);
		depotsList.setListData(getLinkedDepots());
		depotsList.setSelectedValues(oldSelection);
		depotsList.setValueIsAdjusting(false);

		Logging.debug(this, "selected after fetch " + getSelectedDepots().size());
	}

	public List<String> getLinkedDepots() {
		return new ArrayList<>(depotNamesLinked);
	}

	public String getConfigserver() {
		return myServer;
	}

	public void reloadLicensesData() {
		Logging.info(this, "reloadLicensesData");
		if (initialDataLoader.isDataLoaded()) {
			persistenceController.reloadData(ReloadEvent.LICENSE_DATA_RELOAD.toString());
			isAllLicenseDataReloaded = true;

			for (AbstractControlMultiTablePanel cmtp : allControlMultiTablePanels) {
				for (PanelGenEditTable p : cmtp.getTablePanes()) {
					p.reload();
				}
			}
			isAllLicenseDataReloaded = false;
		}
	}

	public boolean isAllLicenseDataReloaded() {
		return isAllLicenseDataReloaded;
	}

	public boolean isInitialLicenseDataLoading() {
		return isInitialLicenseDataLoading;
	}

	private void refreshClientListKeepingGroup() {
		// dont do anything if we did not finish another thread for this
		String oldGroupSelection = activatedGroupModel.getGroupName();
		Logging.info(this, " refreshClientListKeepingGroup oldGroupSelection " + oldGroupSelection);

		setRebuiltClientListTableModel(true);
		activateGroup(true, oldGroupSelection);
	}

	public void reload() {
		mainFrame.activateLoadingPane(Configed.getResourceValue("MainFrame.jMenuFileReload") + " ...");
		SwingUtilities.invokeLater(this::reloadData);
	}

	private void reloadData() {
		checkSaveAll(true);

		int saveViewIndex = getViewIndex();
		Logging.info(this, " reloadData saveViewIndex " + saveViewIndex);
		List<String> selValuesList = clientTable.getSelectedValues();
		Logging.info(this, "reloadData, selValuesList.size " + selValuesList.size());
		// dont do anything if we did not finish another thread for this
		if (initialDataLoader.isDataLoaded()) {
			clientTable.deactivateListSelectionListener();
			allowedClients = null;

			persistenceController.reloadData(CacheIdentifier.ALL_DATA.toString());
			persistenceController.getUserRolesConfigDataService().checkConfigurationPD();
			preloadData();

			FOpsiLicenseMissingText.reset();
			mainFrame.getPanelProductProperties().reload();
			if (mainFrame.getFDialogOpsiLicensingInfo() != null) {
				mainFrame.getFDialogOpsiLicensingInfo().reload();
			}

			requestRefreshDataForClientSelection();

			mainFrame.updateHostCheckboxenText();
			mainFrame.enableAfterLoading();

			Logging.info(this, " in reload, we are in thread " + Thread.currentThread());

			if (mainFrame.getControllerHWinfoMultiClients() != null) {
				mainFrame.getControllerHWinfoMultiClients().rebuildModel();
			}

			fetchDepots();
			setEditingTarget(editingTarget);

			// if depot selection changed, we adapt the clients
			NavigableSet<String> clientsLeft = new TreeSet<>();
			for (String client : selValuesList) {
				String depotForClient = persistenceController.getHostInfoCollections().getMapPcBelongsToDepot()
						.get(client);

				if (depotForClient != null && depotsList.getSelectedValuesList().contains(depotForClient)) {
					clientsLeft.add(client);
				}
			}

			Logging.info(this, "reloadData, selected clients now " + Logging.getSize(clientsLeft));

			Logging.debug(this, " reset the values, particularly in list ");
			clientTable.activateListSelectionListener();
			clientTable.setSelectedValues(clientsLeft);

			Logging.info(this, "reloadData, selected clients now, after resetting " + Logging.getSize(selectedClients));
			mainFrame.reloadServerMenu();
			updateHostInfo();
			hostInfo.resetGui();
		}

		mainFrame.deactivateLoadingPane();
	}

	public HostsStatusPanel getHostsStatusInfo() {
		return mainFrame.getHostsStatusPanel();
	}

	public TableModel getSelectedClientsTableModel() {
		return clientTable.getSelectedRowsModel();
	}

	public void addToGlobalUpdateCollection(UpdateCollection newCollection) {
		updateCollection.add(newCollection);
	}

	public GeneralDataChangedKeeper getGeneralDataChangedKeeper() {
		return generalDataChangedKeeper;
	}

	/*
	 * ============================================
	 * inner class generalDataChangedKeeper
	 * ===========================================
	 */
	public class GeneralDataChangedKeeper extends DataChangedKeeper {
		@Override
		public void dataHaveChanged(Object source) {
			super.dataHaveChanged(source);
			Logging.info(this, "dataHaveChanged from " + source);

			// anyDataChanged in ConfigedMain
			setDataChanged(super.isDataChanged());
		}

		public boolean askSave() {
			boolean result = false;
			if (this.dataChanged) {
				if (fAskSaveProductConfiguration == null) {
					fAskSaveProductConfiguration = new FTextArea(mainFrame, Globals.APPNAME, true,
							new String[] { Configed.getResourceValue("MainFrame.SaveChangedValue.NO"),
									Configed.getResourceValue("buttonYES") });
					fAskSaveProductConfiguration
							.setMessage(Configed.getResourceValue("ConfigedMain.reminderSaveConfig"));

					fAskSaveProductConfiguration.setSize(new Dimension(300, 220));
				}

				fAskSaveProductConfiguration.setLocationRelativeTo(mainFrame);
				fAskSaveProductConfiguration.setVisible(true);

				result = fAskSaveProductConfiguration.getResult() == 2;

				fAskSaveProductConfiguration.setVisible(false);
			}

			return result;
		}

		private void saveConfigs() {
			Logging.info(this, "saveConfigs ");

			updateProductStates();
			Logging.debug(this, "saveConfigs: collectChangedLocalbootStates " + collectChangedLocalbootStates);

			Logging.info(this,
					"we should now start working on the update collection of size  " + updateCollection.size());

			updateCollection.doCall();
			checkErrorList();

			Logging.info(this, "we clear the update collection " + updateCollection.getClass());

			updateCollection.clearElements();
		}

		public void save() {
			if (this.dataChanged) {
				saveConfigs();
			}

			this.dataChanged = false;
		}

		public void cancel() {
			Logging.info(this, "cancel");
			this.dataChanged = false;

			updateCollection.cancel();
		}

		private void updateProductStates() {
			updateManager.updateProductStates(collectChangedLocalbootStates, OpsiPackage.TYPE_LOCALBOOT);

			updateManager.updateProductStates(collectChangedNetbootStates, OpsiPackage.TYPE_NETBOOT);
		}
	}

	/* ============================================ */

	public GeneralDataChangedKeeper getHostConfigsDataChangedKeeper() {
		return hostConfigsDataChangedKeeper;
	}

	/* ============================================ */

	public ClientInfoDataChangedKeeper getClientInfoDataChangedKeeper() {
		return clientInfoDataChangedKeeper;
	}

	/*
	 * ============================================
	 * inner class ClientInfoDataChangedKeeper
	 */
	public class ClientInfoDataChangedKeeper extends DataChangedKeeper {
		Map<?, ?> source;

		// we use this, but it would not override, therefore we perform a cast
		// it does not guarantee that the values of the map are maps!

		@Override
		public void dataHaveChanged(Object source1) {
			this.source = (Map<?, ?>) source1;

			Logging.debug(this, "dataHaveChanged source " + source);

			if (source == null) {
				Logging.info(this, "dataHaveChanged null");
			} else {
				for (Entry<?, ?> clientEntry : source.entrySet()) {
					Logging.debug(this, "dataHaveChanged for client " + clientEntry.getKey() + " with values"
							+ clientEntry.getValue());
				}
			}

			super.dataHaveChanged(source);

			Logging.debug(this, "dataHaveChanged dataChanged " + dataChanged);

			setDataChanged(super.isDataChanged());

			Logging.debug(this, "dataHaveChanged dataChanged " + dataChanged);

			// anyDataChanged in ConfigedMain

			Logging.info(this, "dataHaveChanged dataChanged " + dataChanged);
		}

		public boolean askSave() {
			boolean result = false;
			if (this.dataChanged) {
				if (fAskSaveChangedText == null) {
					fAskSaveChangedText = new FTextArea(mainFrame, Globals.APPNAME, true,
							new String[] { Configed.getResourceValue("MainFrame.SaveChangedValue.NO"),
									Configed.getResourceValue("buttonYES") });
					fAskSaveChangedText.setMessage(Configed.getResourceValue("MainFrame.SaveChangedValue"));
					fAskSaveChangedText.setSize(new Dimension(300, 220));
				}

				fAskSaveChangedText.setLocationRelativeTo(mainFrame);
				fAskSaveChangedText.setVisible(true);
				result = fAskSaveChangedText.getResult() == 2;

				fAskSaveChangedText.setVisible(false);
			}

			return result;
		}

		public void save() {
			Logging.info(this, "save , dataChanged " + dataChanged + " source " + source);
			if (this.dataChanged && source != null && getSelectedClients() != null) {
				Logging.info(this, "save for clients " + getSelectedClients().size());

				for (String client : getSelectedClients()) {
					hostInfo.showAndSaveInternally(clientTable, client, (Map<?, ?>) source.get(client));
				}
				persistenceController.getHostDataService().updateHosts();

				source.clear();
				// we have to clear the map instead of nulling,
				// since otherwise changedClientInfo in MainFrame keep its value
				// such producing wrong values for other clients
			}

			this.dataChanged = false;
		}
	}

	/* ============================================ */

	public void setDataChanged(boolean b) {
		setDataChanged(b, true);
	}

	private void setDataChanged(boolean b, boolean show) {
		Logging.info(this, "setDataChanged " + b + ", showing " + show);
		anyDataChanged = b;

		if (show && mainFrame != null) {
			mainFrame.saveConfigurationsSetEnabled(b);
		}
	}

	public void cancelChanges() {
		Logging.info(this, "cancelChanges ");
		setDataChanged(false);
		generalDataChangedKeeper.cancel();
	}

	public int checkClose() {
		int result = 0;

		if (anyDataChanged) {
			result = JOptionPane.showOptionDialog(mainFrame,
					Configed.getResourceValue("ConfigedMain.saveBeforeCloseText"),
					Globals.APPNAME + " " + Configed.getResourceValue("ConfigedMain.saveBeforeCloseTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		}

		Logging.debug(this, "checkClose result " + result);
		return result;
	}

	// save if not otherwise stated
	public void checkSaveAll(boolean ask) {
		Logging.debug(this, "checkSaveAll: anyDataChanged, ask  " + anyDataChanged + ", " + ask);

		if (anyDataChanged) {
			// without showing, but must be on first place since we run in this method again
			setDataChanged(false, false);

			if (ask) {
				if (clientInfoDataChangedKeeper.askSave()) {
					clientInfoDataChangedKeeper.save();
				} else {
					// reset to old values
					hostInfo.resetGui();
				}
			} else {
				clientInfoDataChangedKeeper.save();
			}

			if (!ask || generalDataChangedKeeper.askSave()) {
				generalDataChangedKeeper.save();
			}

			if (!ask || hostConfigsDataChangedKeeper.askSave()) {
				hostConfigsDataChangedKeeper.save();
			} else {
				hostConfigsDataChangedKeeper.cancel();
			}

			setDataChanged(false, true);
		}
	}

	private class ReachableUpdater extends Thread {
		private int interval;

		ReachableUpdater(Integer interval) {
			super();
			setInterval(interval);
		}

		public final void setInterval(Integer interval) {
			int oldInterval = this.interval;

			if (interval == null) {
				this.interval = 0;
			} else {
				this.interval = interval;
			}

			if (oldInterval == 0 && this.interval > 0) {
				start();
			}
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				Logging.debug(this, " editingTarget, viewIndex " + editingTarget + ", " + viewIndex);

				if (viewIndex == VIEW_CLIENTS && Boolean.TRUE.equals(
						persistenceController.getHostDataService().getHostDisplayFields().get("clientConnected"))) {
					reachableInfo = persistenceController.getHostDataService().reachableInfo(null);

					setReachableInfo();
				}

				try {
					int millisecs = interval * 60 * 1000;
					Logging.debug(this, "Thread going to sleep for ms " + millisecs);
					sleep(millisecs);
				} catch (InterruptedException ex) {
					Logging.info(this, "Thread interrupted ");
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void getReachableInfo() {
		// we put this into a thread since it may never end in case of a name resolving
		// problem
		new Thread() {
			@Override
			public void run() {
				FShowList fShowReachableInfo = createReachableInfoDialog();
				fShowReachableInfo.setVisible(true);
				fShowReachableInfo.toFront();

				if (selectedClients != null && !selectedClients.isEmpty()) {
					Logging.info(this, "we have sel clients " + selectedClients.size());
					reachableInfo = persistenceController.getHostDataService().reachableInfo(getSelectedClients());
				} else {
					Logging.info(this, "we don't have selected clients, so we check reachable for all clients");
					reachableInfo = persistenceController.getHostDataService().reachableInfo(null);
				}

				fShowReachableInfo.setVisible(false);

				mainFrame.getIconButtonReachableInfo().setEnabled(true);

				setReachableInfo();
			}
		}.start();
	}

	private static FShowList createReachableInfoDialog() {
		FShowList fShowReachableInfo = new FShowList(null, Globals.APPNAME, false,
				new String[] { Configed.getResourceValue("buttonClose") }, 350, 100);
		fShowReachableInfo.setMessage(Configed.getResourceValue("ConfigedMain.reachableInfoRequested"));
		fShowReachableInfo.setAlwaysOnTop(true);
		fShowReachableInfo.setSize(Globals.REACHABLE_INFO_FRAME_WIDTH, Globals.REACHABLE_INFO_FRAME_HEIGHT);
		fShowReachableInfo.setLocationRelativeTo(ConfigedMain.getMainFrame());
		return fShowReachableInfo;
	}

	/*
	 * gets the connection String for the client, depending on whether connected
	 * to the messagebus, or reachable or not
	 */
	private Object getConnectionInfoForClient(String clientName) {
		if (connectedHostsByMessagebus.contains(clientName)) {
			return ConnectionStatusTableCellRenderer.CONNECTED_BY_MESSAGEBUS;
		} else {
			return getConnectionInfoStateForBoolean(reachableInfo.get(clientName));
		}
	}

	private static String getConnectionInfoStateForBoolean(Object b) {
		if (!(b instanceof Boolean)) {
			return ConnectionStatusTableCellRenderer.UNKNOWN;
		} else if (Boolean.TRUE.equals(b)) {
			return ConnectionStatusTableCellRenderer.REACHABLE;
		} else {
			return ConnectionStatusTableCellRenderer.NOT_REACHABLE;
		}
	}

	private void updateConnectionStatusInTable(String clientName) {
		AbstractTableModel model = clientTable.getTableModel();

		int col = model.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));

		for (int row = 0; row < model.getRowCount(); row++) {
			if (model.getValueAt(row, 0).equals(clientName)) {
				model.setValueAt(getConnectionInfoForClient(clientName), row, col);

				model.fireTableCellUpdated(row, col);

				Logging.info(this, "connectionStatus for client " + clientName + " updated in table");
				return;
			}
		}
		Logging.info(this,
				"could not update connectionStatus for client " + clientName + ": not in list of shown table");
	}

	private void setReachableInfo() {
		// update column
		if (Boolean.TRUE
				.equals(persistenceController.getHostDataService().getHostDisplayFields().get("clientConnected"))) {
			AbstractTableModel model = clientTable.getTableModel();

			int col = model.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));

			for (int row = 0; row < model.getRowCount(); row++) {
				String clientId = (String) model.getValueAt(row, 0);

				model.setValueAt(getConnectionInfoForClient(clientId), row, col);
			}

			model.fireTableDataChanged();

			clientTable.setSelectedValues(selectedClients);
		}
	}

	public void getSessionInfo() {
		mainFrame.setCursor(Globals.WAIT_CURSOR);
		mainFrame.getIconButtonSessionInfo().setEnabled(false);
		SessionInfoRetriever infoRetriever = new SessionInfoRetriever(this);
		infoRetriever.setOnlySelectedClients(selectedClients != null && !selectedClients.isEmpty());
		infoRetriever.execute();
	}

	public ClientTable getClientTable() {
		return clientTable;
	}

	public void setSessionInfo(Map<String, String> sessionInfo) {
		this.sessionInfo = sessionInfo;
	}

	@SuppressWarnings({ "java:S1874" })
	public String getBackendInfos() {
		return persistenceController.getConfigDataService().getBackendInfos();
	}

	public void resetProductsForSelectedClients(boolean withDependencies, boolean resetLocalbootProducts,
			boolean resetNetbootProducts) {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		String confirmInfo;

		if (resetLocalbootProducts && resetNetbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetProducts.question");
		} else if (resetLocalbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetLocalbootProducts.question");
		} else if (resetNetbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetNetbootProducts.question");
		} else {
			Logging.warning(this, "cannot reset products because they're neither localboot nor netboot");
			return;
		}

		if (!confirmActionForSelectedClients(confirmInfo)) {
			return;
		}

		mainFrame.activateLoadingCursor();

		if (resetLocalbootProducts) {
			persistenceController.getProductDataService().resetLocalbootProducts(getSelectedClients(),
					withDependencies);
		}

		if (resetNetbootProducts) {
			persistenceController.getProductDataService().resetNetbootProducts(getSelectedClients(), withDependencies);
		}

		requestReloadStatesAndActions();
		mainFrame.deactivateLoadingCursor();
	}

	public boolean freeAllPossibleLicensesForSelectedClients() {
		Logging.info(this, "freeAllPossibleLicensesForSelectedClients, count " + getSelectedClients().size());

		if (getSelectedClients().isEmpty()) {
			return true;
		}

		if (!confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.confirmFreeLicenses.question"))) {
			return false;
		}

		for (String client : getSelectedClients()) {
			Map<String, List<LicenseUsageEntry>> fClient2LicensesUsageList = persistenceController
					.getLicenseDataService().getFClient2LicensesUsageListPD();

			for (LicenseUsageEntry m : fClient2LicensesUsageList.get(client)) {
				persistenceController.getLicenseDataService().addDeletionLicenseUsage(client, m.getLicenseId(),
						m.getLicensePool());
			}
		}

		return persistenceController.getLicenseDataService().executeCollectedDeletionsLicenseUsage();
	}

	public void callNewClientDialog() {
		Collections.sort(netbootProductnames);

		if (newClientDialog == null) {
			newClientDialog = new NewClientDialog(this, getLinkedDepots());
		}

		newClientDialog.setProductNetbootList(netbootProductnames);
		newClientDialog.useConfigDefaults(
				persistenceController.getConfigDataService().isInstallByShutdownConfigured(myServer),
				persistenceController.getConfigDataService().isUefiConfigured(myServer),
				persistenceController.getConfigDataService().isWanConfigured(myServer));
		newClientDialog.setHostNames(persistenceController.getHostInfoCollections().getOpsiHostNames());
		newClientDialog.setLocationRelativeTo(getMainFrame());
		newClientDialog.setVisible(true);
	}

	public void callChangeClientIDDialog() {
		if (getSelectedClients().size() != 1) {
			return;
		}

		FEditText fEdit = new FEditText(getSelectedClients().get(0)) {
			@Override
			protected void commit() {
				super.commit();

				String newID = getText();

				if (persistenceController.getHostInfoCollections().getOpsiHostNames().contains(newID)) {
					showInformationHostExistsAlready(newID);
				}

				Logging.debug(this, "new name " + newID);

				persistenceController.getHostDataService().renameClient(getSelectedClients().get(0), newID);

				refreshClientListActivateALL();
				Logging.debug(this, "set client refreshClientList");
				setClient(newID);
			}
		};

		fEdit.init();
		fEdit.setTitle(Configed.getResourceValue("MainFrame.jMenuChangeClientID"));
		fEdit.setSize(Globals.WIDTH_FRAME_RENAME_CLIENT, Globals.HEIGHT_FRAME_RENAME_CLIENT);
		fEdit.setLocationRelativeTo(ConfigedMain.getMainFrame());
		fEdit.setSingleLine(true);
		fEdit.setModal(true);
		fEdit.setAlwaysOnTop(true);
		fEdit.setVisible(true);
	}

	private static void showInformationHostExistsAlready(String clientId) {
		FTextArea fHostExistsInfo = new FTextArea(getMainFrame(),
				Configed.getResourceValue("FGeneralDialog.title.information"), true,
				new String[] { Configed.getResourceValue("buttonClose") });

		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("ConfigedMain.hostExists"));
		message.append(" \"");
		message.append(clientId);
		message.append("\" \n");

		fHostExistsInfo.setMessage(message.toString());
		fHostExistsInfo.setLocationRelativeTo(getMainFrame());
		fHostExistsInfo.setAlwaysOnTop(true);
		fHostExistsInfo.setVisible(true);
	}

	public void callChangeDepotDialog() {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		FShowListWithComboSelect fChangeDepotForClients = new FShowListWithComboSelect(mainFrame,
				Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.title"), true,
				Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.newDepot"), getDepotArray(),
				new String[] { Configed.getResourceValue("buttonClose"), Configed.getResourceValue("buttonOK") });

		fChangeDepotForClients.setLineWrap(false);

		StringBuilder messageBuffer = new StringBuilder(
				"\n" + Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.Moving") + ": \n\n");

		for (String selectedClient : getSelectedClients()) {
			messageBuffer.append(selectedClient);
			messageBuffer.append("     (from: ");
			messageBuffer.append(
					persistenceController.getHostInfoCollections().getMapPcBelongsToDepot().get(selectedClient));

			messageBuffer.append(") ");

			messageBuffer.append("\n");
		}

		fChangeDepotForClients.setSize(new Dimension(400, 250));
		fChangeDepotForClients.setMessage(messageBuffer.toString());

		fChangeDepotForClients.setVisible(true);

		if (fChangeDepotForClients.getResult() != 2) {
			return;
		}

		final String targetDepot = (String) fChangeDepotForClients.getChoice();

		if (targetDepot == null || targetDepot.isEmpty()) {
			return;
		}

		Logging.debug(this, " start moving to another depot");
		persistenceController.getHostInfoCollections().setDepotForClients(getSelectedClients(), targetDepot);
		checkErrorList();
		refreshClientListKeepingGroup();
	}

	private void initialTreeActivation() {
		Logging.info(this, "initialTreeActivation");

		TreePath pathToSelect;
		String oldGroupSelection = Configed.getSavedStates().getProperty("groupname");

		if (oldGroupSelection != null && clientTree.getGroupNode(oldGroupSelection) != null) {
			pathToSelect = clientTree.getPathToNode(clientTree.getGroupNode(oldGroupSelection));
			Logging.info(this, "old group reset " + oldGroupSelection);
		} else {
			pathToSelect = clientTree.getPathToALL();
		}

		clientTree.expandPath(pathToSelect);
		clientTree.setSelectionPath(pathToSelect);
	}

	private void refreshClientListActivateALL() {
		Logging.info(this, "refreshClientListActivateALL");
		setRebuiltClientListTableModel(true);
		activateGroup(true, ClientTree.ALL_CLIENTS_NAME);
	}

	public void reloadHosts() {
		mainFrame.activateLoadingCursor();
		persistenceController.reloadData(ReloadEvent.HOST_DATA_RELOAD.toString());
		refreshClientListKeepingGroup();
		updateHostInfo();
		hostInfo.resetGui();

		mainFrame.deactivateLoadingCursor();
	}

	public void createClients(List<List<Object>> clients) {
		List<String> createdClientNames = clients.stream().map(v -> (String) v.get(0) + "." + v.get(1))
				.collect(Collectors.toList());
		persistenceController.getHostInfoCollections().addOpsiHostNames(createdClientNames);
		if (persistenceController.getHostDataService().createClients(clients)) {
			Logging.debug(this, "createClients" + clients);
			checkErrorList();

			persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());

			setRebuiltClientListTableModel(true);
			activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
			setClients(createdClientNames);
		} else {
			persistenceController.getHostInfoCollections().removeOpsiHostNames(createdClientNames);
		}
	}

	public void createClient(final String hostname, final String domainname, final String depotID,
			final String description, final String inventorynumber, final String notes, final String ipaddress,
			final String systemUUID, final String macaddress, final boolean shutdownInstall, final boolean uefiBoot,
			final boolean wanConfig, final String[] groups, final String productNetboot) {
		Logging.debug(this,
				"createClient " + hostname + ", " + domainname + ", " + depotID + ", " + description + ", "
						+ inventorynumber + ", " + notes + shutdownInstall + ", " + uefiBoot + ", " + wanConfig + ", "
						+ Arrays.toString(groups) + ", " + productNetboot);

		String newClientID = hostname + "." + domainname;

		persistenceController.getHostInfoCollections().addOpsiHostName(newClientID);

		if (persistenceController.getHostDataService().createClient(hostname, domainname, depotID, description,
				inventorynumber, notes, ipaddress, systemUUID, macaddress, shutdownInstall, uefiBoot, wanConfig, groups,
				productNetboot)) {
			checkErrorList();
			persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());

			setRebuiltClientListTableModel(true);

			if (groups.length == 0 || groups.length > 1 || !activateGroup(false, groups[0])) {
				activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
			}

			// Sets the client on the table
			setClient(newClientID);
		} else {
			persistenceController.getHostInfoCollections().removeOpsiHostName(newClientID);
		}
	}

	public void wakeUp(final List<String> clients, String startInfo) {
		if (clients == null) {
			return;
		}

		Logging.info(this, "wakeUp " + clients.size() + " clients: " + startInfo);
		if (clients.isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoWakeClients") + " " + startInfo) {
			@Override
			protected List<String> getErrors() {
				List<String> errors;

				if (ServerFacade.isOpsi43()) {
					errors = persistenceController.getRPCMethodExecutor().wakeOnLanOpsi43(clients);
				} else {
					errors = persistenceController.getRPCMethodExecutor().wakeOnLan(clients);
				}

				return errors;
			}
		}.start();
	}

	public void wakeSelectedClients() {
		wakeUp(getSelectedClients(), "");
	}

	public void wakeUpWithDelay(final int delaySecs, final List<String> clients, String startInfo) {
		if (clients == null) {
			return;
		}

		Logging.info(this, "wakeUpWithDelay " + clients.size() + " clients: " + startInfo + " delay secs " + delaySecs);

		if (clients.isEmpty()) {
			return;
		}

		final FWakeClients result = new FWakeClients(mainFrame,
				Configed.getResourceValue("FWakeClients.title") + " " + startInfo);

		new Thread() {
			@Override
			public void run() {
				result.act(clients, delaySecs);
			}
		}.start();
	}

	public void deletePackageCachesOfSelectedClients() {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoDeletePackageCaches")) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().deletePackageCaches(getSelectedClients());
			}
		}.start();
	}

	public void fireOpsiclientdEventOnSelectedClients(final String event) {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		new AbstractErrorListProducer("opsiclientd " + event) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().fireOpsiclientdEventOnClients(event,
						getSelectedClients());
			}
		}.start();
	}

	public void processActionRequestsAllProducts() {
		processActionRequests(Collections.emptySet());
	}

	public void processActionRequestsSelectedProducts() {
		processActionRequests(mainFrame.getPanelLocalbootProductSettings().getSelectedIDs());
	}

	private void processActionRequests(Set<String> products) {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		checkSaveAll(false);

		new AbstractErrorListProducer("opsiclientd processActionRequests") {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().processActionRequests(getSelectedClients(),
						products);
			}
		}.start();
	}

	public void showPopupOnSelectedClients(final String message, final Float seconds) {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoPopup") + " " + message) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().showPopupOnClients(message, getSelectedClients(),
						seconds);
			}
		}.start();
	}

	private void initSavedSearchesDialog() {
		if (savedSearchesDialog == null) {
			Logging.debug(this, "create SavedSearchesDialog");
			savedSearchesDialog = new SavedSearchesDialog(clientTable, this);
			savedSearchesDialog.init(new Dimension(300, 400));
		}
		savedSearchesDialog.start();
	}

	public void clientSelectionGetSavedSearch() {
		Logging.debug(this, "clientSelectionGetSavedSearch");
		initSavedSearchesDialog();

		savedSearchesDialog.setLocationRelativeTo(mainFrame);
		savedSearchesDialog.setVisible(true);
	}

	public void reloadServerMenu() {
		mainFrame.reloadServerMenu();
	}

	/**
	 * Starts the execution of command
	 *
	 * @param command
	 */
	public void startSSHOpsiServerExec(final SSHCommand command) {
		Logging.info(this, "startSSHOpsiServerExec isReadOnly false");
		final ConfigedMain configedMain = this;
		new Thread() {
			@Override
			public void run() {
				if (command.needParameter()) {
					((SSHCommandNeedParameter) command).startParameterGui(configedMain);
				} else {
					new SSHConnectExec(configedMain, command);
				}
			}
		}.start();
	}

	/**
	 * Starts the config dialog
	 */
	public void startSSHConfigDialog() {
		if (sshConfigDialog == null) {
			sshConfigDialog = new SSHConfigDialog(this);
		}
		sshConfigDialog.setVisible(true);
		sshConfigDialog.checkComponents();
	}

	/** Starts the control dialog */
	public void startSSHControlDialog() {
		if (sshCommandControlDialog == null) {
			sshCommandControlDialog = new SSHCommandControlDialog(this);
		}
		sshCommandControlDialog.setVisible(true);
	}

	private boolean confirmActionForSelectedClients(String confirmInfo) {
		FShowList fConfirmActionForClients = new FShowList(mainFrame, Globals.APPNAME, true,
				new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") }, 350,
				400);

		fConfirmActionForClients.setMessage(confirmInfo + "\n\n" + getSelectedClientsString().replace(";", ""));

		fConfirmActionForClients.setLocationRelativeTo(ConfigedMain.getMainFrame());
		fConfirmActionForClients.setAlwaysOnTop(true);
		fConfirmActionForClients.setVisible(true);

		return fConfirmActionForClients.getResult() == 2;
	}

	public void shutdownSelectedClients() {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		if (confirmActionForSelectedClients(
				Configed.getResourceValue("ConfigedMain.ConfirmShutdownClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoShutdownClients")) {
				@Override
				protected List<String> getErrors() {
					return persistenceController.getRPCMethodExecutor().shutdownClients(getSelectedClients());
				}
			}.start();
		}
	}

	public void rebootSelectedClients() {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		if (confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmRebootClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoRebootClients")) {
				@Override
				protected List<String> getErrors() {
					return persistenceController.getRPCMethodExecutor().rebootClients(getSelectedClients());
				}
			}.start();
		}
	}

	public void deleteSelectedClients() {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		if (!confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmDeleteClients.question"))) {
			return;
		}

		persistenceController.getHostDataService().deleteClients(getSelectedClients());

		if (isFilterClientList()) {
			mainFrame.toggleClientFilterAction();
		}

		refreshClientListKeepingGroup();
		clientTable.clearSelection();
	}

	public void copySelectedClient() {
		if (getSelectedClients().isEmpty()) {
			return;
		}

		Optional<HostInfo> selectedClient = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps().values()
				.stream().filter(hostValues -> hostValues.getName().equals(getSelectedClients().get(0))).findFirst();

		if (!selectedClient.isPresent()) {
			return;
		}

		JPanel additionalPane = new JPanel();
		GroupLayout additionalPaneLayout = new GroupLayout(additionalPane);
		additionalPane.setLayout(additionalPaneLayout);

		JLabel jLabelHostname = new JLabel(Configed.getResourceValue("ConfigedMain.jLabelHostname"));
		JTextField jTextHostname = new JTextField(new CheckedDocument(
				new char[] { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
						'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' },
				-1), "", 17);
		jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));
		CopySuffixAddition copySuffixAddition = new CopySuffixAddition(selectedClient.get().getName());
		jTextHostname.setText(copySuffixAddition.add());

		additionalPaneLayout.setHorizontalGroup(
				additionalPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(Globals.GAP_SIZE)
						.addComponent(jLabelHostname).addGap(Globals.GAP_SIZE).addComponent(jTextHostname));
		additionalPaneLayout.setVerticalGroup(additionalPaneLayout.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2)
				.addComponent(jLabelHostname)
				.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2)
				.addComponent(jTextHostname));

		FTextArea fAskCopyClient = new FTextArea(getMainFrame(), Configed.getResourceValue("MainFrame.jMenuCopyClient"),
				true, new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") },
				null, Globals.DEFAULT_FTEXTAREA_WIDTH, 230, additionalPane);

		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("ConfigedMain.confirmCopyClient"));
		message.append("\n\n");
		message.append(selectedClient.get().getName());

		fAskCopyClient.setMessage(message.toString());
		fAskCopyClient.setLocationRelativeTo(getMainFrame());
		fAskCopyClient.setAlwaysOnTop(true);
		fAskCopyClient.setVisible(true);

		if (fAskCopyClient.getResult() == 2) {
			mainFrame.activateLoadingCursor();
			String newClientName = jTextHostname.getText();
			boolean proceed = true;
			if (newClientName.isEmpty()) {
				proceed = false;
			}

			HostInfo clientToCopy = selectedClient.get();
			String newClientNameWithDomain = newClientName + "."
					+ Utils.getDomainFromClientName(clientToCopy.getName());
			if (persistenceController.getHostInfoCollections().getOpsiHostNames().contains(newClientNameWithDomain)) {
				boolean overwriteExistingHost = ask2OverwriteExistingHost(newClientNameWithDomain);
				if (!overwriteExistingHost) {
					proceed = false;
				}
			}

			Logging.info(this, "copy client with new name " + newClientName);
			if (proceed) {
				persistenceController.getHostInfoCollections().addOpsiHostName(newClientNameWithDomain);
				CopyClient copyClient = new CopyClient(clientToCopy, newClientName);
				copyClient.copy();

				setRebuiltClientListTableModel(true);
				activateGroup(false, activatedGroupModel.getGroupName());
				setClient(newClientNameWithDomain);
			}
			mainFrame.deactivateLoadingCursor();
		}
	}

	private static boolean ask2OverwriteExistingHost(String host) {
		FTextArea fAskOverwriteExsitingHost = new FTextArea(getMainFrame(),
				Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question"), true,
				new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") });

		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message0"));
		message.append(" \"");
		message.append(host);
		message.append("\" \n");
		message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message1"));

		fAskOverwriteExsitingHost.setMessage(message.toString());
		fAskOverwriteExsitingHost.setLocationRelativeTo(getMainFrame());
		fAskOverwriteExsitingHost.setAlwaysOnTop(true);
		fAskOverwriteExsitingHost.setVisible(true);

		return fAskOverwriteExsitingHost.getResult() == 2;
	}

	public void callNewClientSelectionDialog() {
		if (clientSelectionDialog != null) {
			clientSelectionDialog.leave();
			clientSelectionDialog = null;
		}
		callClientSelectionDialog();
	}

	public void callClientSelectionDialog() {
		initSavedSearchesDialog();

		if (clientSelectionDialog == null) {
			clientSelectionDialog = new ClientSelectionDialog(this, clientTable, savedSearchesDialog);
		}

		clientSelectionDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		clientSelectionDialog.setVisible(true);
	}

	public void loadSearch(String name) {
		clientSelectionDialog.loadSearch(name);
	}

	public void setSelectedClients(Collection<String> clientsToSelect) {
		clientTable.setSelectedValues(clientsToSelect);
	}

	public void selectClientsByFailedAtSomeTimeAgo(String arg) {
		SelectionManager manager = new SelectionManager(null);

		if (arg == null || arg.isEmpty()) {
			manager.setSearch(SavedSearches.SEARCH_FAILED_AT_ANY_TIME);
		} else {
			String timeAgo = DateExtendedByVars.interpretVar(arg);
			String test = String.format(SavedSearches.SEARCH_FAILED_BY_TIMES, timeAgo);

			Logging.info(this, "selectClientsByFailedAtSomeTimeAgo  test " + test);
			manager.setSearch(test);
		}

		List<String> result = manager.selectClients();

		clientTable.setSelectedValues(result);
	}

	public void selectClientsNotCurrentProductInstalled(String selectedProduct,
			boolean includeClientsWithBrokenInstallation) {
		Logging.debug(this, "selectClientsNotCurrentProductInstalled, products " + selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		String productVersion = persistenceController.getProductDataService().getProductVersion(selectedProduct);
		String packageVersion = persistenceController.getProductDataService().getProductPackageVersion(selectedProduct);

		Logging.debug(this, "selectClientsNotCurrentProductInstalled product " + selectedProduct + ", " + productVersion
				+ ", " + packageVersion);

		List<String> clientsToSelect = persistenceController.getHostDataService().getClientsWithOtherProductVersion(
				selectedProduct, productVersion, packageVersion, includeClientsWithBrokenInstallation);

		Logging.info(this, "selectClientsNotCurrentProductInstalled clients found globally " + clientsToSelect.size());

		clientsToSelect.retainAll(clientTable.getColumnValues(0));

		Logging.info(this, "selectClientsNotCurrentProductInstalled clients found for displayed client list "
				+ clientsToSelect.size());

		clientTable.setSelectedValues(clientsToSelect);
	}

	public void selectClientsWithFailedProduct(String selectedProduct) {
		Logging.debug(this, "selectClientsWithFailedProduct, products " + selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		SelectionManager manager = new SelectionManager(null);

		String test = String.format(SavedSearches.SEARCH_FAILED_PRODUCT, selectedProduct);

		manager.setSearch(test);

		List<String> result = manager.selectClients();

		Logging.info(this, "selected: " + result);
		clientTable.setSelectedValues(result);
	}

	public void logEventOccurred() {
		if (allFrames == null) {
			return;
		}

		boolean found = false;

		for (JFrame f : allFrames) {
			if (f != null) {
				Logging.debug(this, "log event occurred in frame f , is focused " + f.isFocused() + " " + f);

				Logging.checkErrorList(f);
				found = true;
				break;
			} else {
				Logging.warning(this, "a frame is null here");
			}
		}

		if (!found) {
			Logging.checkErrorList(mainFrame);
		}
	}

	protected void checkErrorList() {
		Logging.checkErrorList(mainFrame);
	}

	private boolean checkSavedLicensesFrame() {
		if (allControlMultiTablePanels == null) {
			return true;
		}

		boolean change = false;
		boolean result = false;
		Iterator<AbstractControlMultiTablePanel> iter = allControlMultiTablePanels.iterator();
		while (!change && iter.hasNext()) {
			AbstractControlMultiTablePanel cmtp = iter.next();
			Iterator<PanelGenEditTable> iterP = cmtp.tablePanes.iterator();
			while (!change && iterP.hasNext()) {
				PanelGenEditTable p = iterP.next();
				change = p.isDataChanged();
			}
		}

		if (change) {
			int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.AllowLeaveApp"),
					Configed.getResourceValue("ConfigedMain.Licenses.AllowLeaveApp.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION) {
				result = true;
			}
		} else {
			result = true;
		}

		return result;
	}

	public static JFrame getFrame() {
		if (mainFrame != null) {
			return mainFrame;
		} else if (loginDialog != null) {
			return loginDialog;
		} else {
			Logging.error(ConfigedMain.class, "This should not happen... Both mainFrame and loginDialog are null");
			return null;
		}
	}

	public boolean closeInstance(boolean checkdirty) {
		Logging.info(this, "start closing instance, checkdirty " + checkdirty);

		boolean result = true;

		if (!FStartWakeOnLan.runningInstances.isEmpty()) {
			result = FStartWakeOnLan.runningInstances.askStop();
		}

		if (!result) {
			return false;
		}

		if (checkdirty) {
			int closeCheckResult = checkClose();
			result = closeCheckResult == JOptionPane.YES_OPTION || closeCheckResult == JOptionPane.NO_OPTION;

			if (!result) {
				return result;
			}

			if (closeCheckResult == JOptionPane.YES_OPTION) {
				checkSaveAll(false);
			}
		}

		if (mainFrame != null) {
			mainFrame.setVisible(false);
			mainFrame.dispose();
			mainFrame = null;
		}

		if (loginDialog != null) {
			loginDialog.setVisible(false);
			loginDialog.dispose();
		}

		if (!checkSavedLicensesFrame()) {
			licensesFrame.setVisible(true);
			result = false;
		}

		Logging.info(this, "close instance result " + result);

		return result;
	}

	public void finishApp(boolean checkdirty, int exitcode) {
		if (closeInstance(checkdirty)) {
			Main.endApp(exitcode);
		}
	}

	public static void requestLicensesFrameReload() {
		licensesFrame = null;
	}

	public static LicensesFrame getLicensesFrame() {
		return licensesFrame;
	}

	public Map<String, Boolean> getHostDisplayFields() {
		return hostDisplayFields;
	}

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		ConfigedMain.host = host;
	}

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		ConfigedMain.user = user;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		ConfigedMain.password = password;
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// Not required to implement.
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		// Sleep for a little because otherwise we cannot get the needed data from the server.
		Utils.threadSleep(this, 5);

		if (!WebSocketEvent.GENERAL_EVENT.toString().equals(message.get("type")) && !message.containsKey("event")) {
			return;
		}

		String eventType = (String) message.get("event");
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> eventData = objectMapper.convertValue(message.get("data"),
				new TypeReference<Map<String, Object>>() {
				});

		if (WebSocketEvent.PRODUCT_ON_CLIENT_CREATED.toString().equals(eventType)
				|| WebSocketEvent.PRODUCT_ON_CLIENT_UPDATED.toString().equals(eventType)
				|| WebSocketEvent.PRODUCT_ON_CLIENT_DELETED.toString().equals(eventType)) {
			updateManager.updateProduct(eventData);
		} else if (WebSocketEvent.HOST_CONNECTED.toString().equals(eventType)) {
			addClientToConnectedList((String) eventData.get("id"));
		} else if (WebSocketEvent.HOST_DISCONNECTED.toString().equals(eventType)) {
			removeClientFromConnectedList((String) eventData.get("id"));
		} else if (WebSocketEvent.HOST_CREATED.toString().equals(eventType)) {
			addClientToTable((String) eventData.get("id"));
		} else if (WebSocketEvent.HOST_DELETED.toString().equals(eventType)) {
			removeClientFromTable((String) eventData.get("id"));
		} else {
			// Other events are handled by other listeners.
		}
	}
}
