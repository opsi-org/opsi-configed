/**
 * ConfigedMain description: The main controller of the program copyright:
 * Copyright (c) 2000-2022 organization: uib.de
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 * Copyright (c) 2000-2022 uib.de
 *
 * @author D. Oertel, R. Roeder, J. Schneider, A. Sucher, N. Otto
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
*/

package de.uib.configed;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
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
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.groupaction.ActivatedGroupModel;
import de.uib.configed.groupaction.FGroupActions;
import de.uib.configed.gui.ClientSelectionDialog;
import de.uib.configed.gui.DPassword;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.FDialogRemoteControl;
import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.FShowListWithComboSelect;
import de.uib.configed.gui.FStartWakeOnLan;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.FWakeClients;
import de.uib.configed.gui.GroupnameChoice;
import de.uib.configed.gui.HostsStatusPanel;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.gui.NewClientDialog;
import de.uib.configed.gui.SavedSearchesDialog;
import de.uib.configed.gui.ssh.SSHCommandControlDialog;
import de.uib.configed.gui.ssh.SSHConfigDialog;
import de.uib.configed.guidata.DependenciesModel;
import de.uib.configed.guidata.IFInstallationStateTableModel;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.guidata.InstallationStateTableModelFiltered;
import de.uib.configed.guidata.ListMerger;
import de.uib.configed.productaction.FProductActions;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.tree.GroupNode;
import de.uib.configed.tree.IconNode;
import de.uib.configed.type.DateExtendedByVars;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.messagebus.Messagebus;
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.JSONthroughHTTPS;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandNeedParameter;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsicommand.sshcommand.SSHConnectTerminal;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.datachanges.AdditionalconfigurationUpdateCollection;
import de.uib.opsidatamodel.datachanges.HostUpdateCollection;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.utilities.DataChangedKeeper;
import de.uib.utilities.logging.LogEvent;
import de.uib.utilities.logging.LogEventObserver;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.DataLoadingObservable;
import de.uib.utilities.savedstates.SavedStates;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;
import de.uib.utilities.swing.CheckedDocument;
import de.uib.utilities.swing.FEditText;
import de.uib.utilities.swing.list.ListCellRendererByIndex;
import de.uib.utilities.swing.tabbedpane.TabClient;
import de.uib.utilities.swing.tabbedpane.TabController;
import de.uib.utilities.swing.tabbedpane.TabbedFrame;
import de.uib.utilities.table.gui.BooleanIconTableCellRenderer;
import de.uib.utilities.table.gui.ConnectionStatusTableCellRenderer;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.ExternalSource;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.provider.RowsProvider;
import de.uib.utilities.table.provider.TableProvider;
import de.uib.utilities.thread.WaitCursor;

public class ConfigedMain implements ListSelectionListener, TabController, LogEventObserver {
	public static final int VIEW_CLIENTS = 0;
	public static final int VIEW_LOCALBOOT_PRODUCTS = 1;
	public static final int VIEW_NETBOOT_PRODUCTS = 2;
	public static final int VIEW_NETWORK_CONFIGURATION = 3;
	public static final int VIEW_HARDWARE_INFO = 4;
	public static final int VIEW_SOFTWARE_INFO = 5;
	public static final int VIEW_LOG = 6;
	public static final int VIEW_PRODUCT_PROPERTIES = 7;
	public static final int VIEW_HOST_PROPERTIES = 8;

	// Dashboard and other features for opsi 4.3 enabled?
	public static final boolean THEMES = true;

	static final String TEST_ACCESS_RESTRICTED_HOST_GROUP = null;

	static final String TEMPGROUPNAME = "";

	private static GuiStrategyForLoadingData strategyForLoadingData;

	private static MainFrame mainFrame;
	public static DPassword dPassword;

	public static String host;
	public static String user;
	public static String password;

	public static EditingTarget editingTarget = EditingTarget.CLIENTS;

	AbstractPersistenceController persist;

	// global table providers for licence management
	TableProvider licencePoolTableProvider;
	TableProvider licenceOptionsTableProvider;
	TableProvider licenceContractsTableProvider;
	TableProvider softwarelicencesTableProvider;

	public TableProvider globalProductsTableProvider;

	protected GeneralDataChangedKeeper generalDataChangedKeeper;
	protected ClientInfoDataChangedKeeper clientInfoDataChangedKeeper;
	protected HostConfigsDataChangedKeeper hostConfigsDataChangedKeeper;

	protected DependenciesModel dependenciesModel;

	protected IFInstallationStateTableModel istmForSelectedClientsLocalboot;
	protected IFInstallationStateTableModel istmForSelectedClientsNetboot;
	protected String firstSelectedClient;
	private String[] selectedClients = new String[] {};
	List<String> saveSelectedClients;
	List<String> preSaveSelectedClients;

	// we do not work with selection

	protected Map<String, TreePath> activeTreeNodes;
	protected List<TreePath> activePaths;
	// is nearly activeTreeNodes.values() , but there may be multiple paths where
	// only one entry to activeTreeNode remains ----

	protected Set<String> clientsFilteredByTree;
	protected TreePath groupPathActivatedByTree;
	protected ActivatedGroupModel activatedGroupModel;

	protected String[] objectIds = new String[] {};
	protected String[] selectedDepots = new String[] {};
	protected String[] oldSelectedDepots;
	protected List<String> selectedDepotsV = new ArrayList<>();

	protected boolean anyDataChanged;

	protected String clientInDepot;
	protected HostInfo hostInfo = new HostInfo();

	// tells if a group of client is loaded via GroupManager (and not by direct
	// selection)
	protected boolean groupLoading;

	protected boolean changeListByToggleShowSelection;
	protected boolean hostgroupChanged;
	protected String groupname = TEMPGROUPNAME;
	private String appTitle = Globals.APPNAME;

	protected FTextArea fAskSaveChangedText;
	protected FTextArea fAskSaveProductConfiguration;

	private SavedSearchesDialog savedSearchesDialog;
	private ClientSelectionDialog clientSelectionDialog;
	private FShowList fShowReachableInfo;

	// null serves als marker that we were not editing products
	protected String productEdited;

	// the properties for one product and all selected clients
	protected Collection<Map<String, Object>> productProperties;
	protected UpdateCollection updateCollection = new UpdateCollection(new ArrayList<>());
	protected Map<String, ProductpropertiesUpdateCollection> clientProductpropertiesUpdateCollections;
	/*
	 * for each product:
	 * a collection of all clients
	 * that contains name value pairs with changed data
	 */

	protected ProductpropertiesUpdateCollection clientProductpropertiesUpdateCollection;
	protected ProductpropertiesUpdateCollection depotProductpropertiesUpdateCollection;

	protected AdditionalconfigurationUpdateCollection additionalconfigurationUpdateCollection;

	protected HostUpdateCollection hostUpdateCollection;

	protected Map<String /* client */, Map<String /* product */, Map<String /* propertykey */, String/* propertyvalue */>>> collectChangedLocalbootStates = new HashMap<>();

	protected Map<String /* client */, Map<String /* product */, Map<String /* propertykey */, String/* propertyvalue */>>> collectChangedNetbootStates = new HashMap<>();

	// a map of products, product --> list of used as an indicator that a product is in the depot
	protected Map<String, List<String>> possibleActions = new HashMap<>();

	protected Map<String, ListMerger> mergedProductProperties;

	private Map<String, Boolean> displayFieldsLocalbootProducts;
	private Map<String, Boolean> displayFieldsNetbootProducts;

	protected Set<String> depotsOfSelectedClients;
	protected Set<String> allowedClients;

	protected List<String> localbootProductnames;
	protected List<String> netbootProductnames;
	protected List<Map<String, List<Map<String, Object>>>> hwAuditConfig;

	// marker variables for requests for reload when clientlist changes
	private Map<String, List<Map<String, String>>> localbootStatesAndActions;
	private boolean localbootStatesAndActionsUPDATE;
	private Map<String, List<Map<String, String>>> netbootStatesAndActions;
	private Map<String, Map<String, Object>> hostConfigs;

	// collection of retrieved software audit and hardware maps

	private Map<String, Map<String, List<Map<String, Object>>>> hwInfoClientmap;

	protected String myServer;
	protected String opsiDefaultDomain;
	protected List<String> editableDomains;
	protected boolean multiDepot;

	private WaitCursor waitCursorInitGui;

	JTableSelectionPanel selectionPanel;

	ClientTree treeClients;

	Map<String, Map<String, String>> productGroups;
	Map<String, Set<String>> productGroupMembers;

	DepotsList depotsList;
	Map<String, Map<String, Object>> depots;
	List<String> depotNamesLinked;
	private boolean depotsListSelectionChanged;
	private String depotRepresentative;
	ListSelectionListener depotsListSelectionListener;

	private ReachableUpdater reachableUpdater = new ReachableUpdater(0);

	private List<JFrame> allFrames;

	public TabbedFrame licencesFrame;

	private FGroupActions groupActionFrame;
	private FProductActions productActionFrame;

	ControlPanelEnterLicence controlPanelEnterLicence;
	ControlPanelEditLicences controlPanelEditLicences;
	AbstractControlMultiTablePanel controlPanelAssignToLPools;
	ControlPanelLicencesStatistics controlPanelLicencesStatistics;
	ControlPanelLicencesUsage controlPanelLicencesUsage;
	ControlPanelLicencesReconciliation controlPanelLicencesReconciliation;

	List<AbstractControlMultiTablePanel> allControlMultiTablePanels;

	private Dashboard dashboard;

	MyDialogRemoteControl dialogRemoteControl;
	Map<String, RemoteControl> remoteControls;

	private int clientCount;
	private boolean firstDepotListChange = true;

	private final Dimension licencesInitDimension = new Dimension(1200, 800);

	protected int viewIndex = VIEW_CLIENTS;
	protected int saveClientsViewIndex = VIEW_CLIENTS;
	protected int saveDepotsViewIndex = VIEW_PRODUCT_PROPERTIES;
	protected int saveServerViewIndex = VIEW_NETWORK_CONFIGURATION;

	protected Map<String, Object> reachableInfo = new HashMap<>();
	protected Map<String, String> sessionInfo = new HashMap<>();

	protected Map<String, String> logfiles;

	public Map<String, Boolean> hostDisplayFields;

	LicensingInfoMap licensingInfoMap;

	public enum LicencesTabStatus {
		LICENCEPOOL, ENTER_LICENCE, EDIT_LICENCE, USAGE, RECONCILIATION, STATISTICS
	}

	protected Map<LicencesTabStatus, TabClient> licencesPanels = new EnumMap<>(LicencesTabStatus.class);
	protected LicencesTabStatus licencesStatus;

	protected Map<LicencesTabStatus, String> licencesPanelsTabNames = new EnumMap<>(LicencesTabStatus.class);

	private boolean dataReady;

	private boolean filterClientList;

	public enum EditingTarget {
		CLIENTS, DEPOTS, SERVER
	}
	// with this enum type we build a state model, which target shall be edited

	private int buildPclistTableModelCounter;

	private int reloadCounter;

	private Messagebus messagebus;

	private Set<String> connectedHostsByMessagebus;

	boolean sessioninfoFinished;

	public ConfigedMain(String host, String user, String password, String sshKey, String sshKeyPass) {
		if (ConfigedMain.host == null) {
			ConfigedMain.host = host;
		}
		if (ConfigedMain.user == null) {
			ConfigedMain.user = user;
		}
		if (ConfigedMain.password == null) {
			ConfigedMain.password = password;
		}

		SSHConnectionInfo.getInstance().setHost(host);
		SSHConnectionInfo.getInstance().setUser(user);
		SSHConnectionInfo.getInstance().setPassw(password);
		if (sshKey == null) {
			SSHConnectionInfo.getInstance().useKeyfile(false, "", "");
		} else {
			SSHConnectionInfo.getInstance().useKeyfile(true, sshKey, sshKeyPass);
		}

		Logging.registLogEventObserver(this);
	}

	public static MainFrame getMainFrame() {
		return mainFrame;
	}

	// TabController Interface
	@Override
	public LicencesTabStatus getStartTabState() {
		return LicencesTabStatus.LICENCEPOOL;
	}

	@Override
	public TabClient getClient(LicencesTabStatus state) {
		return licencesPanels.get(state);
	}

	@Override
	public void addClient(LicencesTabStatus status, TabClient panel) {
		licencesPanels.put(status, panel);
		licencesFrame.addTab(status, licencesPanelsTabNames.get(status), (JComponent) panel);
	}

	@Override
	public LicencesTabStatus reactToStateChangeRequest(LicencesTabStatus newState) {
		Logging.debug(this, "reactToStateChangeRequest( newState: " + newState + "), current state " + licencesStatus);
		if (newState != licencesStatus && getClient(licencesStatus).mayLeave()) {
			licencesStatus = newState;

			if (getClient(licencesStatus) != null) {
				licencesPanels.get(licencesStatus).reset();
			}
			// otherwise we return the old status

		}

		return licencesStatus;
	}

	public static ConfigedMain.EditingTarget getEditingTarget() {
		return editingTarget;
	}

	protected void initGui() {
		Logging.info(this, "initGui");

		displayFieldsLocalbootProducts = new LinkedHashMap<>(
				persist.getProductOnClientsDisplayFieldsLocalbootProducts());
		displayFieldsNetbootProducts = new LinkedHashMap<>(persist.getProductOnClientsDisplayFieldsNetbootProducts());
		// initialization by defaults, it can be edited afterwards

		initTree();

		allFrames = new ArrayList<>();

		initSpecialTableProviders();

		initMainFrame();

		activatedGroupModel = new ActivatedGroupModel(mainFrame.getHostsStatusInfo());

		setEditingTarget(EditingTarget.CLIENTS);

		anyDataChanged = false;

		waitCursorInitGui = new WaitCursor(mainFrame.getContentPane(), mainFrame.getCursor(), "initGui");

		preloadData();

		// restrict visibility of clients to some group

		setRebuiltClientListTableModel();

		// \u0009 is tab
		Logging.debug(this, "initialTreeActivation\u0009");

		SwingUtilities.invokeLater(() -> {
			initialTreeActivation();
			if (strategyForLoadingData != null) {
				strategyForLoadingData.actAfterWaiting();
			}
		});

		reachableUpdater.setInterval(Configed.getRefreshMinutes());

		setReachableInfo(selectedClients);
	}

	private static String getSavedStatesDefaultLocation() {
		String result = "";

		if (System.getenv(Logging.WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) != null) {
			// Windows
			result = System.getenv(Logging.WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) + File.separator + "opsi.org"
					+ File.separator + "configed";
		} else {
			result = System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY) + File.separator + ".configed";
		}

		return result;
	}

	private static String getSavedStatesDirectoryName(String locationName) {
		return locationName + File.separator + host.replace(":", "_");
	}

	private void initSavedStates() {
		File savedStatesDir = null;
		boolean success = true;

		if (Configed.savedStatesLocationName != null) {
			Logging.info(this, "trying to write saved states to " + Configed.savedStatesLocationName);
			try {
				String directoryName = getSavedStatesDirectoryName(Configed.savedStatesLocationName);
				savedStatesDir = new File(directoryName);
				Logging.info(this, "writing saved states, created file " + savedStatesDir);

				if (!savedStatesDir.exists() && !savedStatesDir.mkdirs()) {
					Logging.warning(this, "mkdirs for saved states failed, for File " + savedStatesDir);
				}

				Logging.info(this, "writing saved states, got dirs");

				if (!savedStatesDir.setWritable(true, true)) {
					Logging.warning(this, "setting file savedStatesDir writable failed");
				}

				Logging.info(this, "writing saved states, set writable, success: " + success);
				Configed.savedStates = new SavedStates(
						new File(savedStatesDir.toString() + File.separator + Configed.SAVED_STATES_FILENAME));
			} catch (Exception ex) {
				Logging.warning(this, "saved states exception " + ex);
				success = false;
				Logging.error(this, "cannot not write saved states into " + Configed.savedStatesLocationName);
			}
		}

		if (Configed.savedStatesLocationName == null || Configed.savedStates == null || !success) {
			Logging.info(this, "writing saved states to " + getSavedStatesDefaultLocation());
			savedStatesDir = new File(getSavedStatesDirectoryName(getSavedStatesDefaultLocation()));

			if (!savedStatesDir.exists() && !savedStatesDir.mkdirs()) {
				Logging.warning(this, "mkdirs for saved states failed, in savedStatesDefaultLocation");
			}

			if (!savedStatesDir.setWritable(true, true)) {
				Logging.warning(this, "setting file savedStatesDir writable failed");
			}

			Configed.savedStates = new SavedStates(
					new File(savedStatesDir.toString() + File.separator + Configed.SAVED_STATES_FILENAME));
		}

		try {
			Configed.savedStates.load();
		} catch (IOException iox) {
			Logging.warning(this, "saved states file could not be loaded", iox);
		}

		Integer oldUsageCount = Integer.valueOf(Configed.savedStates.saveUsageCount.deserialize());
		Configed.savedStates.saveUsageCount.serialize(oldUsageCount + 1);
	}

	private List<String> readLocallySavedServerNames() {
		List<String> result = new ArrayList<>();
		TreeMap<java.sql.Timestamp, String> sortingmap = new TreeMap<>();
		File savedStatesLocation = null;
		// the following is nearly a double of initSavedStates

		boolean success = true;

		if (Configed.savedStatesLocationName != null) {
			Logging.info(this, "trying to find saved states in " + Configed.savedStatesLocationName);
			try {
				savedStatesLocation = new File(Configed.savedStatesLocationName);
				savedStatesLocation.mkdirs();
				success = success && savedStatesLocation.setReadable(true);
			} catch (Exception ex) {
				success = false;
			}
		}

		if (!success) {
			Logging.warning(this, "cannot not find saved states in " + Configed.savedStatesLocationName);
		}

		if (Configed.savedStatesLocationName == null || (!success)) {
			Logging.info(this, "searching saved states in " + getSavedStatesDefaultLocation());
			Configed.savedStatesLocationName = getSavedStatesDefaultLocation();
			savedStatesLocation = new File(getSavedStatesDefaultLocation());
			savedStatesLocation.mkdirs();
		}

		Logging.info(this, "saved states location " + savedStatesLocation);

		File[] subdirs = null;

		try {
			subdirs = savedStatesLocation.listFiles(File::isDirectory);

			for (File folder : subdirs) {
				File checkFile = new File(folder + File.separator + Configed.SAVED_STATES_FILENAME);
				String folderPath = folder.getPath();
				String elementname = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);

				if (elementname.lastIndexOf("_") > -1) {
					elementname = elementname.replace("_", ":");
				}

				sortingmap.put(new java.sql.Timestamp(checkFile.lastModified()), elementname);
			}
		} catch (SecurityException ex) {
			Logging.warning("could not read file: " + ex);
		}

		for (Date date : sortingmap.descendingKeySet()) {
			result.add(sortingmap.get(date));
		}

		Logging.info(this, "readLocallySavedServerNames  result " + result);

		return result;
	}

	private void setSSHallowedHosts() {
		Set<String> sshAllowedHosts = new HashSet<>();

		if (persist.isDepotsFullPermission()) {
			Logging.info(this, "set ssh allowed hosts " + host);
			sshAllowedHosts.add(host);
			sshAllowedHosts.addAll(persist.getHostInfoCollections().getDepots().keySet());
		} else {
			sshAllowedHosts.addAll(persist.getDepotPropertiesForPermittedDepots().keySet());
		}

		SSHCommandFactory.getInstance(this).setAllowedHosts(sshAllowedHosts);
		Logging.info(this, "ssh allowed hosts" + sshAllowedHosts);
	}

	public void initDashInfo() {
		if (!JSONthroughHTTPS.isOpsi43()) {
			Logging.info(this, "initDashInfo not enabled");
			return;
		}

		Logging.info(this, "initDashboard " + dashboard);
		if (dashboard == null) {
			dashboard = new Dashboard();
			dashboard.initAndShowGUI();
		} else {
			dashboard.show();
		}
	}

	private void initMessagebus() {
		if (connectMessagebus()) {
			messagebus.makeStandardChannelSubscriptions(this);
		} else {
			Logging.error(this, "could not connect to messagebus...");
		}

	}

	public void addClientToTable(String clientId) {
		// TODO TO IMPLEMENT; WHAT TO DO WHEN MESSAGEBUS HAS EVENT OF CLIENT ADDED
	}

	public void removeClientFromTable(String clientId) {
		// TODO TO IMPLEMENT; WHAT TO DO WHEN MESSAGEBUS HAS EVENT OF CLIENT DELETED
	}

	public void addClientToConnectedList(String clientId) {
		connectedHostsByMessagebus.add(clientId);
		updateConnectionStatusInTable(clientId);
	}

	public void removeClientFromConnectedList(String clientId) {
		connectedHostsByMessagebus.remove(clientId);
		updateConnectionStatusInTable(clientId);
	}

	public boolean connectMessagebus() {
		if (messagebus == null) {
			messagebus = new Messagebus();
		}

		try {

			Logging.info(this, "connect to messagebus");
			return messagebus.connect();
		} catch (InterruptedException e) {
			Logging.error(this, "could not connect to messagebus", e);
			Thread.currentThread().interrupt();
		}

		return false;
	}

	public void connectTerminal() {
		messagebus.connectTerminal();
	}

	public void loadDataAndGo() {

		Logging.clearErrorList();

		// errors are already handled in login
		Logging.info(this, " we got persist " + persist);

		setSSHallowedHosts();

		Logging.info(this, "call initData");
		initData();
		initSavedStates();
		oldSelectedDepots = Configed.savedStates.saveDepotSelection.deserialize();

		// too early, raises a NPE, if the user entry does not exist

		persist.syncTables();

		strategyForLoadingData = new GuiStrategyForLoadingData(dPassword);

		((DataLoadingObservable) persist).registerDataLoadingObserver(strategyForLoadingData);

		strategyForLoadingData.startWaiting();

		new Thread() {
			@Override
			public void run() {
				initGui();

				waitCursorInitGui.stop();
				checkErrorList();

				strategyForLoadingData.setReady();
				strategyForLoadingData.actAfterWaiting();

				mainFrame.toFront();
			}
		}.start();
	}

	public void init() {
		Logging.debug(this, "init");

		// we start with a language

		if (reachableUpdater != null) {
			reachableUpdater.setInterval(0);
		}

		InstallationStateTableModel.restartColumnDict();
		SWAuditEntry.setLocale();

		List<String> savedServers = readLocallySavedServerNames();

		login(savedServers);
	}

	protected void initData() {

		dependenciesModel = new DependenciesModel(persist);
		generalDataChangedKeeper = new GeneralDataChangedKeeper();
		clientInfoDataChangedKeeper = new ClientInfoDataChangedKeeper();
		hostConfigsDataChangedKeeper = new HostConfigsDataChangedKeeper();
		allControlMultiTablePanels = new ArrayList<>();

		connectedHostsByMessagebus = persist.getMessagebusConnectedClients();

		if (JSONthroughHTTPS.isOpsi43()) {
			initMessagebus();
		}
	}

	protected void initSpecialTableProviders() {
		List<String> columnNames = new ArrayList<>();

		columnNames.add("productId");
		columnNames.add("productName");

		// from OpsiPackage.appendValues
		columnNames.add(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE);
		columnNames.add(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
		columnNames.add(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
		columnNames.add(OpsiPackage.SERVICE_KEY_LOCKED);

		List<String> classNames = new ArrayList<>();
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		globalProductsTableProvider = new DefaultTableProvider(
				new ExternalSource(columnNames, classNames, new RowsProvider() {
					@Override
					public void requestReload() {
						persist.productDataRequestRefresh();
					}

					@Override
					public List<List<Object>> getRows() {
						return persist.getProductRows();
					}
				}));
	}

	// sets dataReady = true when finished
	protected void preloadData() {
		WaitCursor waitCursor = new WaitCursor(mainFrame.getContentPane(), "preloadData");

		persist.retrieveOpsiModules();

		if (depotRepresentative == null) {
			depotRepresentative = myServer;
		}
		persist.setDepot(depotRepresentative);
		persist.depotChange();

		opsiDefaultDomain = persist.getOpsiDefaultDomain();
		editableDomains = persist.getDomains();
		if (!editableDomains.contains(opsiDefaultDomain)) {
			editableDomains.add(opsiDefaultDomain);
		}

		localbootProductnames = persist.getAllLocalbootProductNames();
		netbootProductnames = persist.getAllNetbootProductNames();
		persist.getProductIds();

		persist.productGroupsRequestRefresh();

		hostDisplayFields = persist.getHostDisplayFields();
		persist.getProductOnClientsDisplayFieldsNetbootProducts();
		persist.getProductOnClientsDisplayFieldsLocalbootProducts();
		persist.configOptionsRequestRefresh();

		if (savedSearchesDialog != null) {
			savedSearchesDialog.resetModel();
		}

		productGroups = persist.getProductGroups();
		productGroupMembers = persist.getFProductGroup2Members();

		hwAuditConfig = persist
				.getOpsiHWAuditConf(Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry());
		mainFrame.initHardwareInfo(hwAuditConfig);
		Logging.info(this, "preloadData, hw classes " + persist.getAllHwClassNames());
		mainFrame.updateHostCheckboxenText();

		persist.retrieveProducts();

		possibleActions = persist.getPossibleActions(depotRepresentative);
		persist.retrieveProductPropertyDefinitions();
		persist.retrieveProductDependencies();

		persist.retrieveDepotProductProperties();

		persist.getInstalledSoftwareInformation();

		dataReady = true;
		waitCursor.stop();
		mainFrame.enableAfterLoading();
	}

	public void setGroupLoading(boolean b) {
		groupLoading = b;
	}

	public void toggleColumnIPAddress() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);
		persist.getHostDisplayFields().put(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}
	}

	public void toggleColumnSystemUUID() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL);
		persist.getHostDisplayFields().put(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}
	}

	public void toggleColumnHardwareAddress() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL);
		persist.getHostDisplayFields().put(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}
	}

	public void setColumnSessionInfo(boolean b) {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		if (visible == b) {
			return;
		}

		Logging.info(this, "setColumnSessionInfo " + b);
		persist.getHostDisplayFields().put(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL, b);

		mainFrame.combinedMenuItemSessionInfoColumn.show(b);
		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}
	}

	public void toggleColumnSessionInfo() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);

		setColumnSessionInfo(!visible);

		mainFrame.combinedMenuItemSessionInfoColumn.show(!visible);
	}

	public void toggleColumnInventoryNumber() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL);
		persist.getHostDisplayFields().put(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemInventoryNumberColumn.show(!visible);
	}

	public void toggleColumnCreated() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CREATED_DISPLAY_FIELD_LABEL);
		persist.getHostDisplayFields().put(HostInfo.CREATED_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemCreatedColumn.show(!visible);
	}

	public void toggleColumnWANactive() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);
		persist.getHostDisplayFields().put(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemWANactiveColumn.show(!visible);
	}

	public void toggleColumnUEFIactive() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);
		persist.getHostDisplayFields().put(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemUefiBootColumn.show(!visible);
	}

	public void toggleColumnInstallByShutdownActive() {
		Boolean visible = persist.getHostDisplayFields().get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);
		if (visible == null) {
			JOptionPane.showMessageDialog(mainFrame, "An older configed is running in the network", "Information",
					JOptionPane.OK_OPTION);
			// == null can occur if an old configed runs somewhere
		} else {
			persist.getHostDisplayFields().put(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL, !visible);

			setRebuiltClientListTableModel(false);
			selectionPanel.initSortKeys();
			if (getSelectedClients().length > 0) {
				selectionPanel.moveToValue(getSelectedClients()[0], 0);
			}

			mainFrame.combinedMenuItemInstallByShutdownColumn.show(!visible);
		}
	}

	public void toggleColumnDepot() {
		boolean visible = persist.getHostDisplayFields().get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL);
		persist.getHostDisplayFields().put(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemDepotColumn.show(!visible);
	}

	public boolean treeViewAllowed() {
		return true;

	}

	public void handleGroupActionRequest() {
		if (persist.isWithLocalImaging()) {
			startGroupActionFrame();
		} else {
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", "not activated", true,
					new String[] { Configed.getResourceValue("buttonOK") }, 200, 200);

			f.setVisible(true);
		}
	}

	private void startGroupActionFrame() {
		Logging.info(this, "startGroupActionFrame clientsFilteredByTree " + activatedGroupModel.getAssociatedClients()
				+ " active " + activatedGroupModel.isActive());

		if (!activatedGroupModel.isActive()) {
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", "no group selected", true,
					new String[] { Configed.getResourceValue("buttonOK") }, 200, 200);

			f.setVisible(true);

			return;
		}

		if (groupActionFrame == null) {
			groupActionFrame = new FGroupActions(this, persist);
			groupActionFrame.setSize(licencesInitDimension);
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
			productActionFrame = new FProductActions(this, persist);
			productActionFrame.setSize(licencesInitDimension);
			productActionFrame.centerOnParent();
			allFrames.add(productActionFrame);
		}

		productActionFrame.start();
	}

	public void handleLicencesManagementRequest() {
		Logging.info(this, "handleLicencesManagementRequest called");
		persist.retrieveOpsiModules();

		if (persist.isWithLicenceManagement()) {
			toggleLicencesFrame();
		} else {
			de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText
					.callInstanceWith(Configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));

		}
	}

	public void toggleLicencesFrame() {
		if (licencesFrame == null) {
			initLicencesFrame();
			allFrames.add(licencesFrame);
			licencesFrame.setSize(licencesInitDimension);
			licencesFrame.setVisible(true);
			mainFrame.visualizeLicencesFramesActive(true);
			return;
		}

		Logging.info(this, "toggleLicencesFrame is visible" + licencesFrame.isVisible());
		licencesFrame.setVisible(true);
		mainFrame.visualizeLicencesFramesActive(licencesFrame.isVisible());
	}

	public void setEditingTarget(EditingTarget t) {
		Logging.info(this, "setEditingTarget " + t);
		editingTarget = t;
		mainFrame.visualizeEditingTarget(t);
		// what else to do:
		switch (t) {
		case CLIENTS:
			Logging.debug(this, "setEditingTarget preSaveSelectedClients " + preSaveSelectedClients);

			mainFrame.setConfigPanesEnabled(true);
			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.jPanel_HostProperties")), false);
			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), false);
			mainFrame.setVisualViewIndex(saveClientsViewIndex);

			Logging.debug(this, "setEditingTarget preSaveSelectedClients " + preSaveSelectedClients);

			if (!reachableUpdater.isInterrupted()) {
				reachableUpdater.interrupt();
			}

			if (preSaveSelectedClients != null && !preSaveSelectedClients.isEmpty()) {
				setSelectedClientsOnPanel(preSaveSelectedClients.toArray(new String[] {}));
			}

			break;
		case DEPOTS:
			Logging.info(this, "setEditingTarget  DEPOTS");

			if (!reachableUpdater.isInterrupted()) {
				reachableUpdater.interrupt();
			}

			initServer();
			mainFrame.setConfigPanesEnabled(false);

			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.jPanel_HostProperties")), true);
			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), true);

			Logging.info(this, "setEditingTarget  call setVisualIndex  saved " + saveDepotsViewIndex + " resp. "
					+ mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

			mainFrame.setVisualViewIndex(saveDepotsViewIndex);

			break;
		case SERVER:
			if (!reachableUpdater.isInterrupted()) {
				reachableUpdater.interrupt();
			}

			initServer();
			mainFrame.setConfigPanesEnabled(false);

			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig")), true);

			mainFrame.setVisualViewIndex(saveServerViewIndex);
			break;
		default:
			break;
		}

		resetView(viewIndex);
	}

	protected void actOnListSelection() {
		Logging.info(this, "actOnListSelection");

		checkSaveAll(true);

		checkErrorList();

		Logging.info(this, "selectionPanel.getSelectedValues().size(): " + selectionPanel.getSelectedValues().size());

		boolean groupingExists = selectionPanel.getSelectedValues().size() > 1;

		// when initializing the program the frame may not exist
		if (mainFrame != null) {
			Logging.info(this, "ListSelectionListener valueChanged selectionPanel.isSelectionEmpty() "
					+ selectionPanel.isSelectionEmpty());

			mainFrame.saveGroupSetEnabled(groupingExists);

			if (selectionPanel.isSelectionEmpty()) {
				setSelectedClients((List<String>) null);
				setSelectedClientsArray(new String[0]);
			} else {
				setSelectedClients(selectionPanel.getSelectedValues());
			}

			clientInDepot = "";

			hostInfo.initialize();

			Map<String, HostInfo> pcinfos = persist.getHostInfoCollections().getMapOfPCInfoMaps();

			Logging.info(this,
					"actOnListSelection, produce hostInfo  getSelectedClients().length " + getSelectedClients().length);

			if (getSelectedClients().length > 0) {
				String selClient = getSelectedClients()[0];
				hostInfo.setBy(pcinfos.get(selClient).getMap());

				Logging.debug(this, "actOnListSelection, produce hostInfo first selClient " + selClient);
				Logging.debug(this, "actOnListSelection, produce hostInfo  " + hostInfo);

				HostInfo secondInfo = new HostInfo();

				// test

				for (int i = 1; i < getSelectedClients().length; i++) {
					selClient = getSelectedClients()[i];
					secondInfo.setBy(pcinfos.get(selClient).getMap());
					hostInfo.combineWith(secondInfo);
				}
			}

			mainFrame.setClientInfoediting(getSelectedClients().length == 1);

			// initialize the following method
			depotsOfSelectedClients = null;
			depotsOfSelectedClients = getDepotsOfSelectedClients();
			Iterator<String> selectedDepotsIterator = depotsOfSelectedClients.iterator();
			StringBuilder depotsAdded = new StringBuilder("");

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

			if (getSelectedClients().length == 1) {
				mainFrame.setClientID(getSelectedClients()[0]);
			} else {
				mainFrame.setClientID("");
			}

			hostInfo.resetGui(mainFrame);

			mainFrame.enableMenuItemsForClients(getSelectedClients().length);

			Logging.info(this, "actOnListSelection update hosts status selectedClients " + getSelectedClients().length
					+ " as well as " + selectionPanel.getSelectedValues().size());

			mainFrame.getHostsStatusInfo().updateValues(clientCount, getSelectedClients().length,
					getSelectedClientsStringWithMaxLength(HostsStatusPanel.MAX_CLIENT_NAMES_IN_FIELD), clientInDepot);

			activatedGroupModel.setActive(getSelectedClients().length <= 0);

			// request reloading of client list depending data

			requestRefreshDataForClientSelection();
		}
	}

	// ListSelectionListener for client list
	@Override
	public void valueChanged(ListSelectionEvent e) {

		// Ignore extra messages.
		if (e.getValueIsAdjusting()) {
			return;
		}

		actOnListSelection();
	}

	// we call this after we have a PersistenceController
	protected void initMainFrame() {

		myServer = persist.getHostInfoCollections().getConfigServer();

		// create depotsList
		depotsList = new DepotsList(persist);

		if (depotsListSelectionListener == null) {
			Logging.info(this, "create depotsListSelectionListener");
			depotsListSelectionListener = new ListSelectionListener() {
				private int counter;

				@Override
				public void valueChanged(ListSelectionEvent e) {
					counter++;
					Logging.info(this, "============ depotSelection event count  " + counter);

					if (!e.getValueIsAdjusting()) {
						depotsListValueChanged();
					}
				}
			};
		}

		fetchDepots();

		depotsList.addListSelectionListener(depotsListSelectionListener);
		depotsList.setInfo(depots);

		// String[] oldSelectedDepots =

		if (oldSelectedDepots.length == 0) {
			depotsList.setSelectedValue(myServer, true);
		} else {
			ArrayList<Integer> savedSelectedDepots = new ArrayList<>();
			// we collect the indices of the old depots in the current list

			for (int i = 0; i < oldSelectedDepots.length; i++) {
				for (int j = 0; j < depotsList.getModel().getSize(); j++) {
					if ((depotsList.getModel().getElementAt(j)).equals(oldSelectedDepots[i])) {
						savedSelectedDepots.add(j);
					}
				}
			}

			if (!savedSelectedDepots.isEmpty()) {
				int[] depotsToSelect = new int[savedSelectedDepots.size()];
				for (int j = 0; j < depotsToSelect.length; j++) {
					// conversion to int
					depotsToSelect[j] = savedSelectedDepots.get(j);
				}

				depotsList.setSelectedIndices(depotsToSelect);
			} else {
				// if none of the old selected depots is in the list we select the config server
				depotsList.setSelectedValue(myServer, true);
			}
		}

		// we correct the result of the first selection
		depotsListSelectionChanged = false;

		if (!THEMES) {
			depotsList.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

		// create client selection panel
		selectionPanel = new JTableSelectionPanel(this) {

			@Override
			protected void keyPressedOnTable(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					startRemoteControlForSelectedClients();
				} else if (e.getKeyCode() == KeyEvent.VK_F10) {
					Logging.debug(this, "keypressed: f10");
					mainFrame.showPopupClients();
				}
			}

		};

		selectionPanel.setModel(buildClientListTableModel(true));
		setSelectionPanelCols();

		selectionPanel.initSortKeys();

		mainFrame = new MainFrame(this, selectionPanel, depotsList, treeClients, multiDepot);

		// setting the similar global values as well

		Globals.container1 = mainFrame;

		mainFrame.enableMenuItemsForClients(0);

		// rearranging visual components
		mainFrame.validate();

		// set splitpanes before making the frame visible
		mainFrame.initSplitPanes();

		// center the frame:
		locateAndDisplay();

		// init visual states
		Logging.debug(this, "mainframe nearly initialized");

		mainFrame.saveGroupSetEnabled(false);
	}

	private void locateAndDisplay() {
		Rectangle screenRectangle = dPassword.getGraphicsConfiguration().getBounds();
		int distance = Math.min(screenRectangle.width, screenRectangle.height) / 10;

		Logging.info(this, "set size and location of mainFrame");

		// weird formula for size
		mainFrame.setSize(screenRectangle.width - distance, screenRectangle.height - distance);

		// Center mainFrame on screen of configed.fProgress
		mainFrame.setLocation((int) (screenRectangle.getCenterX() - mainFrame.getSize().getWidth() / 2),
				(int) (screenRectangle.getCenterY() - mainFrame.getSize().getHeight() / 2));

		Logging.info(this, "setting mainframe visible");
		mainFrame.setVisible(true);
	}

	protected void initLicencesFrame() {
		long startmillis = System.currentTimeMillis();
		Logging.info(this, "initLicencesFrame start ");
		WaitCursor waitCursor = new WaitCursor(mainFrame.getContentPane(), mainFrame.getCursor(), "initLicencesFrame");
		// general

		licencesFrame = new TabbedFrame(this);

		Globals.frame1 = licencesFrame;
		Globals.container1 = licencesFrame.getContentPane();

		licencesFrame.setGlobals(Globals.getMap());
		licencesFrame.setTitle(
				Globals.APPNAME + "  " + myServer + ":  " + Configed.getResourceValue("ConfigedMain.Licences"));

		licencesStatus = getStartTabState();

		// global table providers
		List<String> columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		List<String> classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		licencePoolTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, () -> (Map) persist.getLicencepools()));

		persist.retrieveRelationsAuditSoftwareToLicencePools();

		columnNames = new ArrayList<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		licenceOptionsTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, () -> persist.getRelationsSoftwareL2LPool()));

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

		licenceContractsTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, () -> (Map) persist.getLicenceContracts()));

		columnNames = new ArrayList<>();
		columnNames.add(LicenceEntry.ID_KEY);
		columnNames.add(LicenceEntry.LICENCE_CONTRACT_ID_KEY);
		columnNames.add(LicenceEntry.TYPE_KEY);
		columnNames.add(LicenceEntry.MAX_INSTALLATIONS_KEY);
		columnNames.add(LicenceEntry.BOUND_TO_HOST_KEY);
		columnNames.add(LicenceEntry.EXPIRATION_DATE_KEY);

		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("de.uib.utilities.ExtendedInteger");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		softwarelicencesTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, () -> (Map) persist.getSoftwareLicences()));

		// panelAssignToLPools
		licencesPanelsTabNames.put(LicencesTabStatus.LICENCEPOOL,
				Configed.getResourceValue("ConfigedMain.Licences.TabLicencepools"));
		controlPanelAssignToLPools = new ControlPanelAssignToLPools(persist, this);
		addClient(LicencesTabStatus.LICENCEPOOL, controlPanelAssignToLPools.getTabClient());
		allControlMultiTablePanels.add(controlPanelAssignToLPools);

		// panelEnterLicence
		licencesPanelsTabNames.put(LicencesTabStatus.ENTER_LICENCE,
				Configed.getResourceValue("ConfigedMain.Licences.TabNewLicence"));
		controlPanelEnterLicence = new ControlPanelEnterLicence(persist, this);
		addClient(LicencesTabStatus.ENTER_LICENCE, controlPanelEnterLicence.getTabClient());
		allControlMultiTablePanels.add(controlPanelEnterLicence);

		// panelEditLicence
		licencesPanelsTabNames.put(LicencesTabStatus.EDIT_LICENCE,
				Configed.getResourceValue("ConfigedMain.Licences.TabEditLicence"));
		controlPanelEditLicences = new ControlPanelEditLicences(persist, this);
		addClient(LicencesTabStatus.EDIT_LICENCE, controlPanelEditLicences.getTabClient());
		allControlMultiTablePanels.add(controlPanelEditLicences);

		// panelUsage
		licencesPanelsTabNames.put(LicencesTabStatus.USAGE,
				Configed.getResourceValue("ConfigedMain.Licences.TabLicenceUsage"));
		controlPanelLicencesUsage = new ControlPanelLicencesUsage(persist, this);
		addClient(LicencesTabStatus.USAGE, controlPanelLicencesUsage.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesUsage);

		// panelReconciliation
		licencesPanelsTabNames.put(LicencesTabStatus.RECONCILIATION,
				Configed.getResourceValue("ConfigedMain.Licences.TabLicenceReconciliation"));
		controlPanelLicencesReconciliation = new ControlPanelLicencesReconciliation(persist);
		addClient(LicencesTabStatus.RECONCILIATION, controlPanelLicencesReconciliation.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesReconciliation);

		// panelStatistics
		licencesPanelsTabNames.put(LicencesTabStatus.STATISTICS,
				Configed.getResourceValue("ConfigedMain.Licences.TabStatistics"));
		controlPanelLicencesStatistics = new ControlPanelLicencesStatistics(persist);
		addClient(LicencesTabStatus.STATISTICS, controlPanelLicencesStatistics.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesStatistics);

		licencesFrame.start();

		Logging.info(this, "set size and location of licencesFrame");

		licencesFrame.setSize(licencesInitDimension);

		// Center on mainFrame
		licencesFrame.setLocationRelativeTo(mainFrame);

		waitCursor.stop();

		long endmillis = System.currentTimeMillis();
		Logging.info(this, "initLicencesFrame  diff " + (endmillis - startmillis));
	}

	// returns true if we have a PersistenceController and are connected
	protected void login(List<String> savedServers) {

		Logging.debug(this, " create password dialog ");
		dPassword = new DPassword(this);

		// set list of saved servers
		if (savedServers != null && !savedServers.isEmpty()) {
			dPassword.setServers(savedServers);
		}

		// check if we started with preferred values
		if (host != null && !host.equals("")) {
			dPassword.setHost(host);
		}

		if (user != null) {
			dPassword.setUser(user);
		}

		if (password != null) {
			dPassword.setPassword(password);
		}

		if (persist == null || persist.getConnectionState().getState() != ConnectionState.CONNECTED) {
			Logging.info(this, "become interactive");

			dPassword.setAlwaysOnTop(true);
			dPassword.setVisible(true);
			// dpass will give back control and call loadDataAndGo
		}

		// This must be called last, so that loading frame for connection is called last
		// and on top of the login-frame
		if (host != null && user != null && password != null) {
			// Auto login
			Logging.info(this, "start with given credentials");

			dPassword.tryConnecting();
		}

	}

	public AbstractPersistenceController getPersistenceController() {
		return persist;
	}

	public void setPersistenceController(AbstractPersistenceController persis) {
		persist = persis;
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

	protected Map<String, Boolean> produceClientListForDepots(String[] depots, Set<String> allowedClients) {
		Logging.info(this, " producePcListForDepots " + Arrays.toString(depots) + " running with allowedClients "
				+ allowedClients);
		Map<String, Boolean> m = persist.getHostInfoCollections().getClientListForDepots(depots, allowedClients);

		if (m != null) {
			clientCount = m.size();
		}

		if (mainFrame != null) {
			mainFrame.getHostsStatusInfo().updateValues(clientCount, null, null, null);
			// persist.getHostInfoCollections().getCountClients() > 0
			// but we are testing:

			selectionPanel.setMissingDataPanel(persist.getHostInfoCollections().getCountClients() == 0);
		}

		return m;
	}

	protected Map<String, Boolean> filterMap(Map<String, Boolean> map0, Set<String> filterset) {
		HashMap<String, Boolean> result = new HashMap<>();

		if (filterset == null) {
			return result;
		}

		Iterator<String> iter = filterset.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (map0.containsKey(key)) {
				result.put(key, map0.get(key));
			}
		}

		return result;
	}

	protected TableModel buildClientListTableModel(boolean rebuildTree) {
		Logging.debug(this, " --------- buildPclistTableModel rebuildTree " + rebuildTree);
		DefaultTableModel model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};

		Map<String, Boolean> pclist = new HashMap<>();
		// dont make pclist global and clear it here
		// since pclist is bound to a variable in persistencecontroller
		// which will be cleared

		Set<String> permittedHostGroups = null;

		if (!persist.accessToHostgroupsOnlyIfExplicitlyStated()) {
			Logging.info(this, " --------- buildPclistTableModel not full hostgroups permission");
			permittedHostGroups = persist.getHostgroupsPermitted();
		}

		Map<String, Boolean> unfilteredList = produceClientListForDepots(getSelectedDepots(), null);

		Logging.debug(this, " unfilteredList ");

		buildPclistTableModelCounter++;
		Logging.info(this,
				"buildPclistTableModel, counter " + buildPclistTableModelCounter + "   rebuildTree  " + rebuildTree);

		if (rebuildTree) {
			Logging.info(this, "------------ buildPclistTableModel, rebuildTree " + rebuildTree);

			unfilteredList = produceClientListForDepots(getSelectedDepots(), null);
			String[] allPCs = new TreeMap<>(unfilteredList).keySet().toArray(new String[] {});

			Logging.debug(this, "buildPclistTableModel, rebuildTree, allPCs  " + Arrays.toString(allPCs));

			treeClients.clear();
			treeClients.setClientInfo(persist.getHostInfoCollections().getMapOfAllPCInfoMaps());

			treeClients.produceTreeForALL(allPCs);

			treeClients.produceAndLinkGroups(persist.getHostGroups());

			Logging.info(this, "------------ buildPclistTableModel, permittedHostGroups " + permittedHostGroups);
			Logging.info(this, "------------ buildPclistTableModel, allPCs " + allPCs.length);
			allowedClients = treeClients.associateClientsToGroups(allPCs, persist.getFObject2Groups(),
					permittedHostGroups);

			if (allowedClients != null) {
				Logging.info(this, "------------ buildPclistTableModel, allowedClients " + allowedClients.size());
			}

		}

		// changes the produced unfilteredList
		if (allowedClients != null) {
			unfilteredList = produceClientListForDepots(getSelectedDepots(), allowedClients);

			Logging.info(this, " unfilteredList " + unfilteredList.size());

			buildPclistTableModelCounter++;
			Logging.info(this, "buildPclistTableModel, counter " + buildPclistTableModelCounter + "   rebuildTree  "
					+ rebuildTree);

			if (rebuildTree) {
				Logging.info(this, "------------ buildPclistTableModel, rebuildTree " + rebuildTree);
				String[] allPCs = new TreeMap<>(unfilteredList).keySet().toArray(new String[] {});

				Logging.debug(this, "buildPclistTableModel, rebuildTree, allPCs  " + Arrays.toString(allPCs));

				treeClients.clear();
				treeClients.setClientInfo(persist.getHostInfoCollections().getMapOfAllPCInfoMaps());

				treeClients.produceTreeForALL(allPCs);

				Logging.info(this, "----------- buildPclistTableModel, directly allowed groups "
						+ treeClients.getDirectlyAllowedGroups());
				treeClients.produceAndLinkGroups(persist.getHostGroups());

				Logging.info(this, "------------ buildPclistTableModel, allPCs (2) " + allPCs.length);

				// we got already allowedClients, therefore don't need the parameter
				// hostgroupsPermitted
				treeClients.associateClientsToGroups(allPCs, persist.getFObject2Groups(), null);

				Logging.info(this, "tree produced");
			}
		}

		Map<String, Boolean> pclist0 = filterMap(unfilteredList, clientsFilteredByTree);

		Logging.info(this, " filterClientList " + filterClientList);

		if (filterClientList) {

			if (pclist0 != null) {

				Logging.info(this,
						"buildPclistTableModel with filterCLientList " + "selected pcs " + getSelectedClients().length);

				for (Entry<String, Boolean> pcEntry : pclist0.entrySet()) {

					for (int j = 0; j < getSelectedClients().length; j++) {

						if (getSelectedClients()[j].equals(pcEntry.getKey())) {

							pclist.put(pcEntry.getKey(), pcEntry.getValue());
							break;
						}
					}
				}

			}
		} else {
			pclist = pclist0;
		}

		// building table model
		Map<String, HostInfo> pcinfos = persist.getHostInfoCollections().getMapOfPCInfoMaps();

		if (pclist != null) {
			hostDisplayFields = persist.getHostDisplayFields();

			// test

			for (Map.Entry<String, Boolean> entry : hostDisplayFields.entrySet()) {
				if (Boolean.TRUE.equals(entry.getValue())) {
					model.addColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel." + entry.getKey()));
				}
			}

			Logging.info(this, "buildPclistTableModel host_displayFields " + hostDisplayFields);

			for (String clientName : pclist.keySet()) {

				HostInfo pcinfo = pcinfos.get(clientName);
				if (pcinfo == null) {
					pcinfo = new HostInfo();
				}

				Map<String, Object> rowmap = pcinfo.getDisplayRowMap0();

				String sessionValue = "";
				if (sessionInfo.get(clientName) != null) {
					sessionValue = "" + sessionInfo.get(clientName);
				}

				rowmap.put(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL, sessionValue);
				rowmap.put(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, getConnectionInfoForClient(clientName));

				List<Object> rowItems = new ArrayList<>();

				for (Entry<String, Boolean> entry : hostDisplayFields.entrySet()) {
					if (Boolean.TRUE.equals(entry.getValue())) {
						rowItems.add(rowmap.get(entry.getKey()));
					}
				}

				if (clientName.equals("fscnoteb1.uib.local")) {
					Logging.info(this, "*** host_displayFields size, content " + hostDisplayFields.size() + ": "
							+ hostDisplayFields);
					Logging.info(this, "*** rowmap size, content " + rowmap.size() + ": " + rowmap);
					Logging.info(this, "*** rowItems size, content " + rowItems.size() + ": " + rowItems);
				}

				model.addRow(rowItems.toArray());
			}
		}

		Logging.info(this, "buildPclistTableModel, model column count " + model.getColumnCount());

		return model;
	}

	/**
	 * selects a client
	 *
	 * @param clientName
	 */
	public void setClient(String clientName) {
		Logging.info(this, "setClient " + clientName);

		if (clientName == null) {
			setSelectedClientsOnPanel(new String[] {});
		} else {
			setSelectedClientsOnPanel(new String[] { clientName });
			// implies:

			actOnListSelection();
		}
	}

	public void setClients(String[] clientNames) {
		Logging.info(this, "setClients " + clientNames);

		if (clientNames == null) {
			setSelectedClientsOnPanel(new String[] {});
		} else {
			setSelectedClientsOnPanel(clientNames);
			// implies:

			actOnListSelection();
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

		if (!treeClients.groupNodesExists() || treeClients.getGroupNode(groupname) == null) {
			Logging.warning("no group " + groupname);
			return false;
		}

		GroupNode node = treeClients.getGroupNode(groupname);
		TreePath path = treeClients.getPathToNode(node);

		activateGroupByTree(preferringOldSelection, node, path);

		Logging.info(this, "expand activated  path " + path);
		treeClients.expandPath(path);

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

		setSelectedClientsCollectionOnPanel(clientsFilteredByTree);
	}

	protected void requestRefreshDataForClientSelection() {
		Logging.info(this, "requestRefreshDataForClientSelection");
		requestReloadStatesAndActions();
		hostConfigs = null;
		persist.getEmptyLogfiles();

		if (mainFrame.controllerHWinfoMultiClients != null) {
			mainFrame.controllerHWinfoMultiClients.requestResetFilter();
		}
	}

	public void requestReloadStatesAndActions() {
		requestReloadStatesAndActions(false);
	}

	public void requestReloadStatesAndActions(boolean onlyUpdate) {
		Logging.info(this, "requestReloadStatesAndActions , only updating " + onlyUpdate);

		persist.productpropertiesRequestRefresh();

		if (onlyUpdate) {
			localbootStatesAndActionsUPDATE = true;
		} else {
			localbootStatesAndActions = null;
		}

		netbootStatesAndActions = null;
	}

	public String[] getSelectedClients() {
		return selectedClients;
	}

	protected void setSelectedClientsArray(String[] a) {
		if (a == null) {
			return;
		}

		Logging.info(this, "setSelectedClientsArray " + a.length);
		Logging.info(this, "selectedClients up to now size " + Logging.getSize(selectedClients));

		selectedClients = a;
		if (selectedClients.length == 0) {
			firstSelectedClient = "";
		} else {
			firstSelectedClient = selectedClients[0];
		}

		Logging.info(this, "setSelectedClientsArray produced firstSelectedClient " + firstSelectedClient);

		mainFrame.getHostsStatusInfo().updateValues(clientCount, getSelectedClients().length,
				getSelectedClientsString(), clientInDepot);
	}

	/**
	 * transports the selected values of the selection panel to the outer world
	 */
	public List<String> getSelectedClientsInTable() {
		if (selectionPanel == null) {
			return new ArrayList<>();
		}

		return selectionPanel.getSelectedValues();
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

		// init
		setSelectedClientsArray(new String[] {});

		if (!clientNames.isEmpty()) {
			setSelectedClientsArray(clientNames.toArray(new String[clientNames.size()]));
		}

		treeClients.produceActiveParents(getSelectedClients());

		clearLogPage();
		clearSoftwareInfoPage();

		if (getViewIndex() != VIEW_CLIENTS) {
			// change in selection not via clientpage (i.e. via tree)

			Logging.debug(this, "getSelectedClients  " + Arrays.toString(getSelectedClients())
					+ " ,  getViewIndex, viewClients: " + getViewIndex() + ", " + VIEW_CLIENTS);
			int newViewIndex = getViewIndex();
			resetView(newViewIndex);
		}
	}

	public Map<String, TreePath> getActiveTreeNodes() {
		return activeTreeNodes;
	}

	public List<TreePath> getActivePaths() {
		return activePaths;
	}

	public boolean isFilterClientList() {
		return filterClientList;
	}

	public void toggleFilterClientList() {
		changeListByToggleShowSelection = true;
		Logging.info(this, "toggleFilterClientList   " + filterClientList);
		setFilterClientList(!filterClientList);
		changeListByToggleShowSelection = false;
	}

	public void invertClientselection() {
		selectionPanel.removeListSelectionListener(this);
		boolean oldFilterClientList = filterClientList;
		if (filterClientList) {
			toggleFilterClientList();
		}

		Logging.info(this, "invertClientselection selected " + selectionPanel.getSelectedValues());
		List<String> selectedValues = new ArrayList<>(selectionPanel.getInvertedSet());

		String[] selected = selectedValues.toArray(new String[selectedValues.size()]);

		Logging.info(this, "new selection " + Arrays.toString(selected));

		selectionPanel.setSelectedValues(selected);
		setSelectedClientsArray(selected);

		if (oldFilterClientList) {
			toggleFilterClientList();
		}

		selectionPanel.addListSelectionListener(this);
	}

	private void setSelectionPanelCols() {
		Logging.info(this, "setSelectionPanelCols ");

		final int ICON_COLUMN_MAX_WIDTH = 100;

		if (Boolean.TRUE.equals(persist.getHostDisplayFields().get(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL))) {
			int col = selectionPanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL));

			TableColumn column = selectionPanel.getColumnModel().getColumn(col);

			column.setMaxWidth(ICON_COLUMN_MAX_WIDTH);

			column.setCellRenderer(new ConnectionStatusTableCellRenderer());
		}

		if (Boolean.TRUE.equals(persist.getHostDisplayFields().get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL))) {

			List<String> columns = new ArrayList<>();
			for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++) {
				columns.add(selectionPanel.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);

			int col = selectionPanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col " + col);

			Logging.info(this, "showAndSave found col " + col);

			if (col > -1) {
				TableColumn column = selectionPanel.getColumnModel().getColumn(col);
				Logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue());
				column.setMaxWidth(ICON_COLUMN_MAX_WIDTH);

				// column.setCellRenderer(new

				column.setCellRenderer(new BooleanIconTableCellRenderer(
						Globals.createImageIcon("images/checked_withoutbox.png", ""), null));

			}
		}

		if (Boolean.TRUE.equals(persist.getHostDisplayFields().get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL))) {

			List<String> columns = new ArrayList<>();
			for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++) {
				columns.add(selectionPanel.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);

			int col = selectionPanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col " + col);

			if (col > -1) {
				TableColumn column = selectionPanel.getColumnModel().getColumn(col);
				Logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue());
				column.setMaxWidth(ICON_COLUMN_MAX_WIDTH);

				column.setCellRenderer(new BooleanIconTableCellRenderer(
						Globals.createImageIcon("images/checked_withoutbox.png", ""), null));
			}

		}

		if (Boolean.TRUE
				.equals(persist.getHostDisplayFields().get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL))) {

			List<String> columns = new ArrayList<>();

			for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++) {
				columns.add(selectionPanel.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);

			int col = selectionPanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col " + col);

			if (col > -1) {
				TableColumn column = selectionPanel.getColumnModel().getColumn(col);
				Logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue());
				column.setMaxWidth(ICON_COLUMN_MAX_WIDTH);

				column.setCellRenderer(new BooleanIconTableCellRenderer(
						Globals.createImageIcon("images/checked_withoutbox.png", ""), null));
			}

		}

	}

	protected void setRebuiltClientListTableModel() {
		setRebuiltClientListTableModel(true);
	}

	protected void setRebuiltClientListTableModel(boolean restoreSortKeys) {
		Logging.info(this, "setRebuiltClientListTableModel, we have selected Set : " + selectionPanel.getSelectedSet());

		setRebuiltClientListTableModel(restoreSortKeys, true, selectionPanel.getSelectedSet());
	}

	protected void setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree,
			Set<String> selectValues) {
		Logging.info(this,
				"setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree, Set selectValues)  : "
						+ restoreSortKeys + ", " + rebuildTree + ",  selectValues.size() "
						+ Logging.getSize(selectValues));

		List<String> valuesToSelect = null;
		if (selectValues != null) {
			valuesToSelect = new ArrayList<>(selectValues);
		}

		List<? extends RowSorter.SortKey> saveSortKeys = selectionPanel.getSortKeys();

		Logging.info(this, " setRebuiltClientListTableModel--- set model new, selected "
				+ selectionPanel.getSelectedValues().size());

		TableModel tm = buildClientListTableModel(rebuildTree);
		Logging.info(this,
				"setRebuiltClientListTableModel --- got model selected " + selectionPanel.getSelectedValues().size());

		selectionPanel.removeListSelectionListener(this);
		int[] columnWidths = getTableColumnWidths(selectionPanel.getTable());

		selectionPanel.setModel(tm);

		setTableColumnWidths(selectionPanel.getTable(), columnWidths);
		selectionPanel.addListSelectionListener(this);

		selectionPanel.initColumnNames();
		Logging.debug(this, " --- model set  ");
		setSelectionPanelCols();

		if (restoreSortKeys) {
			selectionPanel.setSortKeys(saveSortKeys);
		}

		Logging.info(this, "setRebuiltClientListTableModel set selected values in setRebuiltClientListTableModel() "
				+ Logging.getSize(valuesToSelect));
		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel"
				+ Logging.getSize(selectionPanel.getSelectedValues()));

		setSelectionPanelCols();

		// did lose the selection since last setting
		setSelectedClientsCollectionOnPanel(valuesToSelect);

		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel "
				+ Logging.getSize(selectionPanel.getSelectedValues()));

		reloadCounter++;
		Logging.info(this, "setRebuiltClientListTableModel  reloadCounter " + reloadCounter);
	}

	public void setFilterClientList(boolean b) {

		filterClientList = b;
		setRebuiltClientListTableModel();

	}

	protected String getSelectedClientsString() {
		return getSelectedClientsStringWithMaxLength(null);
	}

	protected String getSelectedClientsStringWithMaxLength(Integer max) {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		int stop = getSelectedClients().length;
		if (max != null && stop > max) {
			stop = max;
		}

		for (int i = 0; i < stop - 1; i++) {
			result.append(getSelectedClients()[i]);

			result.append(";\n");
		}

		result.append(getSelectedClients()[stop - 1]);

		if (max != null && getSelectedClients().length > max) {
			result.append(" ... ");
		}

		return result.toString();
	}

	protected Set<String> getDepotsOfSelectedClients() {
		if (depotsOfSelectedClients == null) {
			depotsOfSelectedClients = new TreeSet<>();
		}

		for (int i = 0; i < getSelectedClients().length; i++) {
			if (persist.getHostInfoCollections().getMapPcBelongsToDepot().get(getSelectedClients()[i]) != null) {
				depotsOfSelectedClients
						.add(persist.getHostInfoCollections().getMapPcBelongsToDepot().get(getSelectedClients()[i]));
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
		productProperties = new ArrayList<>(getSelectedClients().length);

		if ((getSelectedClients().length > 0) && (possibleActions.get(productEdited) != null)) {

			Map<String, Object> productPropertiesFor1Client = persist.getProductProperties(getSelectedClients()[0],
					productEdited);

			if (productPropertiesFor1Client != null) {
				productProperties.add(productPropertiesFor1Client);

				Iterator<String> iter = productPropertiesFor1Client.keySet().iterator();
				while (iter.hasNext()) {
					// get next key - value - pair
					String key = iter.next();

					List<?> value = (List<?>) productPropertiesFor1Client.get(key);

					// create a merger for it
					ListMerger merger = new ListMerger(value);
					mergedProductProperties.put(key, merger);
				}

				// merge the other clients
				for (int i = 1; i < getSelectedClients().length; i++) {
					productPropertiesFor1Client = persist.getProductProperties(getSelectedClients()[i], productEdited);

					productProperties.add(productPropertiesFor1Client);

					iter = productPropertiesFor1Client.keySet().iterator();

					while (iter.hasNext()) {
						String key = iter.next();
						List<?> value = (List<?>) productPropertiesFor1Client.get(key);

						if (mergedProductProperties.get(key) == null) {
							// we need a new property. it is not common

							ListMerger merger = new ListMerger(value);

							merger.setHavingNoCommonValue();
							mergedProductProperties.put(key, merger);
						} else {
							ListMerger merger = mergedProductProperties.get(key);

							ListMerger mergedValue = merger.merge(value);

							// on merging we check if the value is the same as before
							mergedProductProperties.put(key, mergedValue);
						}
					}
				}
			}
		}

	}

	protected void clearProductEditing() {
		mainFrame.panelLocalbootProductSettings.clearEditing();
		mainFrame.panelNetbootProductSettings.clearEditing();
	}

	protected void clearListEditors() {
		mainFrame.panelLocalbootProductSettings.clearListEditors();
		mainFrame.panelNetbootProductSettings.clearListEditors();
	}

	public void setProductEdited(String productname) {
		// called from ProductSettings

		Logging.debug(this, "setProductEdited " + productname);

		productEdited = productname;

		if (clientProductpropertiesUpdateCollection != null) {
			try {
				updateCollection.remove(clientProductpropertiesUpdateCollection);
			} catch (Exception ex) {
				Logging.error(this, "removing ProductpropertiesUpdateCollection from updateCollection failed", ex);
			}
		}
		clientProductpropertiesUpdateCollection = null;

		if (clientProductpropertiesUpdateCollections.get(productEdited) == null) {
			// have we got already a clientProductpropertiesUpdateCollection for this
			// product?
			// if not, we produce one

			clientProductpropertiesUpdateCollection = new ProductpropertiesUpdateCollection(this, persist,
					getSelectedClients(), productEdited);

			clientProductpropertiesUpdateCollections.put(productEdited, clientProductpropertiesUpdateCollection);
			addToGlobalUpdateCollection(clientProductpropertiesUpdateCollection);

		} else {
			clientProductpropertiesUpdateCollection = clientProductpropertiesUpdateCollections.get(productEdited);
		}

		collectTheProductProperties(productEdited);

		dependenciesModel.setActualProduct(productEdited);

		Logging.debug(this, " --- mergedProductProperties " + mergedProductProperties);

		Logging.debug(this, "setProductEdited " + productname + " client specific properties "
				+ persist.hasClientSpecificProperties(productname));

		mainFrame.panelLocalbootProductSettings.initEditing(productEdited, persist.getProductTitle(productEdited),
				persist.getProductInfo(productEdited), persist.getProductHint(productEdited),
				persist.getProductVersion(productEdited) + Globals.ProductPackageVersionSeparator.FOR_DISPLAY
						+ persist.getProductPackageVersion(productEdited) + "   "
						+ persist.getProductLockedInfo(productEdited),
				// List of the properties map of all selected clients
				productProperties,
				// these properties merged to one map
				mergedProductProperties,

				// editmappanelx
				persist.getProductPropertyOptionsMap(productEdited),

				clientProductpropertiesUpdateCollection);

		mainFrame.panelNetbootProductSettings.initEditing(productEdited, persist.getProductTitle(productEdited),
				persist.getProductInfo(productEdited), persist.getProductHint(productEdited),
				persist.getProductVersion(productEdited) + Globals.ProductPackageVersionSeparator.FOR_DISPLAY
						+ persist.getProductPackageVersion(productEdited) + "   "
						+ persist.getProductLockedInfo(productEdited),
				// array of the properties map of all selected clients
				productProperties,
				// these properties merged to one map
				mergedProductProperties,

				// editmappanelx
				persist.getProductPropertyOptionsMap(productEdited),

				clientProductpropertiesUpdateCollection);
	}

	public int getViewIndex() {
		return viewIndex;
	}

	public void treeClientsMouseAction(boolean clicked, MouseEvent mouseEvent) {
		Logging.debug(this, "treeClients_mouseAction");

		if (!treeClients.isEnabled()) {
			return;
		}

		if (mouseEvent.getButton() != MouseEvent.BUTTON1) {
			return;
		}

		int mouseRow = treeClients.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
		TreePath mousePath = treeClients.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

		DefaultMutableTreeNode mouseNode = null;

		if (mouseRow != -1) {
			mouseNode = (DefaultMutableTreeNode) mousePath.getLastPathComponent();

			if (!mouseNode.getAllowsChildren()) {

				// we manage the active nodes

				if (clicked) {
					// on client activation a group activation is ended
					if ((activePaths.size() == 1)
							&& ((DefaultMutableTreeNode) activePaths.get(0).getLastPathComponent())
									.getAllowsChildren()) {
						clearTree();
					} else {
						if ((mouseEvent.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK) {

							clearTree();

							TreePath[] selTreePaths = treeClients.getSelectionPaths();

							for (int i = 0; i < selTreePaths.length; i++) {
								DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) selTreePaths[i]
										.getLastPathComponent();
								activeTreeNodes.put((String) selNode.getUserObject(), selTreePaths[i]);
								activePaths.add(selTreePaths[i]);
								treeClients.collectParentIDsFrom(selNode);
							}
						} else {
							clearTree();
						}
					}

					activateClientByTree((String) mouseNode.getUserObject(), mousePath);

					setRebuiltClientListTableModel(true, false, clientsFilteredByTree);

					Logging.info(this,
							" treeClients_mouseAction getSelectedClients().length " + getSelectedClients().length);

					if (getSelectedClients().length == 1) {
						mainFrame.getHostsStatusInfo().setGroupName(mouseNode.getParent().toString());
					} else {
						mainFrame.getHostsStatusInfo().setGroupName("");
					}

					mainFrame.getHostsStatusInfo().updateValues(clientCount, getSelectedClients().length,
							getSelectedClientsString(), clientInDepot);

					// restore keys, do not rebuild tree, select clientsFilteredByTree
				}
			} else {
				if (clicked) {
					activateGroupByTree(false, mouseNode, mousePath);
					// fails!!
				}
			}

		}

		if (mouseEvent.getClickCount() == 2) {
			Logging.debug(this, "treeClients: double click on tree row " + mouseRow + " getting path " + mousePath);
			Logging.debug(this, "treeClients: mouseNode instanceof GroupNode " + mouseNode + " "
					+ (mouseNode instanceof GroupNode));

			if (mouseNode instanceof GroupNode) {
				setGroup(mouseNode.toString());
			}
		}

	}

	public boolean treeClientsSelectAction(TreePath newSelectedPath) {
		Logging.info(this, "treeClientsSelectAction");

		DefaultMutableTreeNode selectedNode = ((DefaultMutableTreeNode) newSelectedPath.getLastPathComponent());
		Logging.info(this, "treeClientsSelectAction selected node " + selectedNode);

		if (!selectedNode.getAllowsChildren()) {
			setClientByTree(selectedNode.toString(), newSelectedPath);
		}

		return true;
	}

	protected void initTree() {
		Logging.debug(this, "initTree");
		activeTreeNodes = new HashMap<>();
		activePaths = new ArrayList<>();

		treeClients = new ClientTree(this);
		persist.getHostInfoCollections().setTree(treeClients);

	}

	private void setClientByTree(String nodeObject, TreePath pathToNode) {
		clearTree();

		activateClientByTree(nodeObject, pathToNode);

		setRebuiltClientListTableModel(true, false, clientsFilteredByTree);

		if (getSelectedClients().length == 1) {
			mainFrame.getHostsStatusInfo().setGroupName(

					pathToNode.getPathComponent(pathToNode.getPathCount() - 1).toString());
		} else {
			mainFrame.getHostsStatusInfo().setGroupName("");
		}

		mainFrame.getHostsStatusInfo().updateValues(clientCount, getSelectedClients().length,
				getSelectedClientsString(), clientInDepot);

	}

	private void activateClientByTree(String nodeObject, TreePath pathToNode) {
		Logging.info(this, "activateClientByTree, nodeObject: " + nodeObject + ", pathToNode: " + pathToNode);
		activeTreeNodes.put(nodeObject, pathToNode);
		activePaths.add(pathToNode);

		treeClients.collectParentIDsFrom((DefaultMutableTreeNode) pathToNode.getLastPathComponent());

		treeClients.repaint();

		Logging.debug(this, "activateClientByTree, activeTreeNodes " + activeTreeNodes);
		clientsFilteredByTree = activeTreeNodes.keySet();
		Logging.debug(this, "activateClientByTree, clientsFilteredByTree " + clientsFilteredByTree);

		// since we select based on the tree view we disable the filter
		if (filterClientList) {
			mainFrame.toggleClientFilterAction();
		}

	}

	public void clearTree() {
		activeTreeNodes.clear();
		activePaths.clear();
		treeClients.initActiveParents();
	}

	private void setGroupByTree(DefaultMutableTreeNode node, TreePath pathToNode) {
		Logging.info(this, "setGroupByTree node, pathToNode " + node + ", " + pathToNode);
		clearTree();
		activeTreeNodes.put((String) node.getUserObject(), pathToNode);
		activePaths.add(pathToNode);

		clientsFilteredByTree = treeClients.collectLeafs(node);
		treeClients.repaint();

	}

	public void activateGroupByTree(boolean preferringOldSelection, DefaultMutableTreeNode node, TreePath pathToNode) {
		Logging.info(this, "activateGroupByTree, node: " + node + ", pathToNode : " + pathToNode);

		setGroupByTree(node, pathToNode);

		// intended for reload, we cancel activating group
		if (preferringOldSelection && selectionPanel.getSelectedSet() != null
				&& !selectionPanel.getSelectedSet().isEmpty()) {
			return;
		}

		setRebuiltClientListTableModel(true, false, null);
		// with this, a selected client remains selected (but in bottom line, the group
		// seems activated, not the client)

		groupPathActivatedByTree = pathToNode;

		try {
			activatedGroupModel.setNode("" + node, node, pathToNode);
			activatedGroupModel.setDescription(treeClients.getGroups().get("" + node).get("description"));
			activatedGroupModel.setAssociatedClients(clientsFilteredByTree);
			activatedGroupModel.setActive(true);
		} catch (Exception ex) {
			Logging.info(this, "activateGroupByTree, node: " + node + " exception : " + ex);
		}

	}

	public TreePath getGroupPathActivatedByTree() {
		return groupPathActivatedByTree;
	}

	public ActivatedGroupModel getActivatedGroupModel() {
		return activatedGroupModel;
	}

	public Set<String> getActiveParents() {
		if (treeClients == null) {
			return new HashSet<>();
		}

		return treeClients.getActiveParents();
	}

	public boolean addGroup(de.uib.utilities.datastructure.StringValuedRelationElement newGroup) {
		return persist.addGroup(newGroup);
	}

	public boolean updateGroup(String groupId, Map<String, String> groupInfo) {
		return persist.updateGroup(groupId, groupInfo);
	}

	public boolean deleteGroup(String groupId) {
		return persist.deleteGroup(groupId);
	}

	public boolean removeHostGroupElements(List<Object2GroupEntry> entries) {
		return persist.removeHostGroupElements(entries);
	}

	public boolean removeObject2Group(String objectId, String groupId) {
		return persist.removeObject2Group(objectId, groupId);
	}

	public boolean addObject2Group(String objectId, String groupId) {
		return persist.addObject2Group(objectId, groupId);
	}

	public boolean setProductGroup(String groupId, String description, Set<String> productSet) {
		return persist.setProductGroup(groupId, description, productSet);
	}

	private void depotsListValueChanged() {
		if (firstDepotListChange) {
			firstDepotListChange = false;
			return;
		}

		Logging.info(this, "----    depotsList selection changed");

		changeDepotSelection();

		if (depotsList.getModel().getSize() > 1 && mainFrame != null) {
			mainFrame.setChangedDepotSelectionActive(true);
		}

		// when running after the first run, we deactivate buttons

		depotsListSelectionChanged = true;
		depotsOfSelectedClients = null;

		selectedDepots = depotsList.getSelectedValuesList().toArray(new String[0]);
		selectedDepotsV = new ArrayList<>(depotsList.getSelectedValuesList());

		Logging.debug(this, "--------------------  selectedDepotsV         " + selectedDepotsV);

		Configed.savedStates.saveDepotSelection.serialize(selectedDepots);

		try {
			Logging.info(this, " depotsList_valueChanged, omitted initialTreeActivation");

			if (selectionPanel != null) {
				initialTreeActivation();
			}
		} catch (Exception ex) {
			Logging.error(this, "Tree cannot be activated", ex);
		}

	}

	private boolean checkSynchronous(Set<String> depots) {

		if (depots.size() > 1 && !persist.areDepotsSynchronous(depots)) {
			JOptionPane.showMessageDialog(mainFrame, Configed.getResourceValue("ConfigedMain.notSynchronous.text"),
					Configed.getResourceValue("ConfigedMain.notSynchronous.title"), JOptionPane.OK_OPTION);

			return false;
		}

		return true;
	}

	protected boolean setDepotRepresentative() {
		Logging.debug(this, "setDepotRepresentative");

		boolean result = true;

		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			if (depotRepresentative == null) {
				depotRepresentative = myServer;
			}
		} else {

			depotsOfSelectedClients = getDepotsOfSelectedClients();

			Logging.info(this, "depots of selected clients:" + depotsOfSelectedClients);

			String oldRepresentative = depotRepresentative;

			Logging.debug(this, "setDepotRepresentative(), old representative: " + depotRepresentative + " should be ");

			if (!checkSynchronous(depotsOfSelectedClients)) {
				result = false;
			} else {
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

				Logging.debug(this, " --------------- depotRepresentative " + depotRepresentative);

				Logging.info(this,
						"setDepotRepresentative  change depotRepresentative " + " up to now " + oldRepresentative
								+ " new " + depotRepresentative + " equal "
								+ oldRepresentative.equals(depotRepresentative));

				if (!oldRepresentative.equals(depotRepresentative)) {
					Logging.info(this, " new depotRepresentative " + depotRepresentative);
					persist.setDepot(depotRepresentative);

					// everything
					persist.depotChange();
				}
			}
		}

		return result;
	}

	protected List<String> getLocalbootProductDisplayFieldsList() {
		List<String> result = new ArrayList<>();
		Iterator<String> iter = displayFieldsLocalbootProducts.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (Boolean.TRUE.equals(displayFieldsLocalbootProducts.get(key))) {
				result.add(key);
			}
		}

		return result;
	}

	protected List<String> getNetbootProductDisplayFieldsList() {
		List<String> result = new ArrayList<>();
		Iterator<String> iter = displayFieldsNetbootProducts.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (Boolean.TRUE.equals(displayFieldsNetbootProducts.get(key))) {
				result.add(key);
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
		Logging.debug(this, "checkOneClientSelected() selectedClients " + Arrays.toString(getSelectedClients()));
		boolean result = true;

		if (getSelectedClients().length != 1) {
			result = false;
		}

		return result;
	}

	protected boolean setLocalbootProductsPage() {
		Logging.debug(this, "setLocalbootProductsPage() with filter "
				+ Configed.savedStates.saveLocalbootproductFilter.deserialize());

		if (!setDepotRepresentative()) {
			return false;
		}

		try {
			clearProductEditing();

			// null,

			// we reload since at the moment we do not track changes if anyDataChanged
			if (localbootStatesAndActions == null || istmForSelectedClientsLocalboot == null
					|| localbootStatesAndActionsUPDATE) {
				localbootStatesAndActionsUPDATE = false;

				localbootStatesAndActions = persist.getMapOfLocalbootProductStatesAndActions(getSelectedClients());

				istmForSelectedClientsLocalboot = null;
			}

			clientProductpropertiesUpdateCollections = new HashMap<>();
			mainFrame.panelLocalbootProductSettings.initAllProperties();

			Logging.debug(this, "setLocalbootProductsPage,  depotRepresentative:" + depotRepresentative);
			possibleActions = persist.getPossibleActions(depotRepresentative);

			// we retrieve the properties for all clients and products

			// it is necessary to do this before resetting selection below (*) since there a
			// listener is triggered
			// which loads the productProperties for each client separately

			persist.retrieveProductProperties(selectionPanel.getSelectedValues());

			Set<String> oldProductSelection = mainFrame.panelLocalbootProductSettings.getSelectedIDs();
			List<? extends RowSorter.SortKey> currentSortKeysLocalbootProducts = mainFrame.panelLocalbootProductSettings
					.getSortKeys();

			Logging.info(this, "setLocalbootProductsPage: oldProductSelection " + oldProductSelection);

			Logging.debug(this,
					"setLocalbootProductsPage: collectChangedLocalbootStates " + collectChangedLocalbootStates);

			if (istmForSelectedClientsLocalboot == null) {
				// we rebuild only if we reloaded
				istmForSelectedClientsLocalboot = new InstallationStateTableModelFiltered(getSelectedClients(), this,
						collectChangedLocalbootStates, persist.getAllLocalbootProductNames(depotRepresentative),
						localbootStatesAndActions, possibleActions, // persist.getPossibleActions(depotRepresentative),
						persist.getProductGlobalInfos(depotRepresentative), getLocalbootProductDisplayFieldsList(),
						Configed.savedStates.saveLocalbootproductFilter

				);
			}

			try {
				int[] columnWidths = getTableColumnWidths(mainFrame.panelLocalbootProductSettings.tableProducts);
				mainFrame.panelLocalbootProductSettings.setTableModel(istmForSelectedClientsLocalboot);
				mainFrame.panelLocalbootProductSettings.setSortKeys(currentSortKeysLocalbootProducts);

				mainFrame.panelLocalbootProductSettings.setGroupsData(productGroups, productGroupMembers);

				Logging.info(this, "resetFilter " + Configed.savedStates.saveLocalbootproductFilter.deserialize());

				Set<String> savedFilter = Configed.savedStates.saveLocalbootproductFilter.deserialize();

				(mainFrame.panelLocalbootProductSettings).reduceToSet(savedFilter);

				Logging.info(this, "setLocalbootProductsPage oldProductSelection -----------  " + oldProductSelection);
				mainFrame.panelLocalbootProductSettings.setSelection(oldProductSelection); // (*)

				mainFrame.panelLocalbootProductSettings.setSearchFields(
						InstallationStateTableModel.localizeColumns(getLocalbootProductDisplayFieldsList()));

				setTableColumnWidths(mainFrame.panelLocalbootProductSettings.tableProducts, columnWidths);
			} catch (Exception ex) {
				Logging.warning("setLocalbootInstallationStateTableModel, exception occurred: " + ex.getMessage(), ex);
			}

			return true;
		} catch (Exception ex) {
			Logging.error("Error in setLocalbootProductsPage: " + ex, ex);
			return false;
		}

	}

	protected boolean setNetbootProductsPage() {

		if (!setDepotRepresentative()) {
			return false;
		}

		try {
			clearProductEditing();

			long startmillis = System.currentTimeMillis();
			Logging.debug(this,
					"setLocalbootProductsPage, # getMapOfNetbootProductStatesAndActions(selectedClients)  start "
							+ startmillis);

			if (netbootStatesAndActions == null) {
				// we reload since at the moment we do not track changes if anyDataChanged
				netbootStatesAndActions = persist.getMapOfNetbootProductStatesAndActions(getSelectedClients());

				istmForSelectedClientsNetboot = null;
			}
			long endmillis = System.currentTimeMillis();
			Logging.debug(this,
					"setNetbootProductsPage, # getMapOfNetbootProductStatesAndActions(selectedClients)  end "
							+ endmillis + " diff " + (endmillis - startmillis));

			List<? extends RowSorter.SortKey> currentSortKeysNetbootProducts = mainFrame.panelNetbootProductSettings
					.getSortKeys();

			clientProductpropertiesUpdateCollections = new HashMap<>();
			mainFrame.panelLocalbootProductSettings.initAllProperties();

			possibleActions = persist.getPossibleActions(depotRepresentative);

			Set<String> oldProductSelection = mainFrame.panelNetbootProductSettings.getSelectedIDs();

			// we retrieve the properties for all clients and products

			persist.retrieveProductProperties(selectionPanel.getSelectedValues());

			if (istmForSelectedClientsNetboot == null) {
				// we rebuild only if we reloaded
				istmForSelectedClientsNetboot = new InstallationStateTableModelFiltered(// Netbootproducts(
						getSelectedClients(), this, collectChangedNetbootStates,
						persist.getAllNetbootProductNames(depotRepresentative), netbootStatesAndActions,
						possibleActions, persist.getProductGlobalInfos(depotRepresentative),
						getNetbootProductDisplayFieldsList(), Configed.savedStates.saveNetbootproductFilter);
			}

			try {
				int[] columnWidths = getTableColumnWidths(mainFrame.panelNetbootProductSettings.tableProducts);
				mainFrame.panelNetbootProductSettings.setTableModel(istmForSelectedClientsNetboot);

				mainFrame.panelNetbootProductSettings.setSortKeys(currentSortKeysNetbootProducts);

				mainFrame.panelNetbootProductSettings.setGroupsData(productGroups, productGroupMembers);

				Logging.info(this, "resetFilter " + Configed.savedStates.saveLocalbootproductFilter.deserialize());

				Set<String> savedFilter = Configed.savedStates.saveNetbootproductFilter.deserialize();

				mainFrame.panelNetbootProductSettings.reduceToSet(savedFilter);

				mainFrame.panelNetbootProductSettings.setSelection(oldProductSelection);

				setTableColumnWidths(mainFrame.panelNetbootProductSettings.tableProducts, columnWidths);
			} catch (Exception ex) {
				Logging.error(" setNetbootInstallationStateTableModel,  exception Occurred", ex);
			}

			return true;
		} catch (Exception ex) {
			Logging.error("Error in setNetbootProductsPage: " + ex, ex);
			return false;
		}

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
		HashMap<String, Object> mergedMap = new HashMap<>();
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

	protected boolean setProductPropertiesPage() {
		Logging.debug(this, "setProductPropertiesPage");

		if (editingTarget == EditingTarget.DEPOTS) {
			int saveSelectedRow = mainFrame.panelProductProperties.paneProducts.getSelectedRow();
			mainFrame.panelProductProperties.paneProducts.reset();

			if (mainFrame.panelProductProperties.paneProducts.getTableModel().getRowCount() > 0) {
				if (saveSelectedRow == -1 || mainFrame.panelProductProperties.paneProducts.getTableModel()
						.getRowCount() <= saveSelectedRow) {
					mainFrame.panelProductProperties.paneProducts.setSelectedRow(0);
				} else {
					mainFrame.panelProductProperties.paneProducts.setSelectedRow(saveSelectedRow);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	protected boolean setHostPropertiesPage() {
		Logging.debug(this, "setHostPropertiesPage");

		try {
			if (editingTarget == EditingTarget.DEPOTS) {
				Map<String, Map<String, Object>> depotPropertiesForPermittedDepots = persist
						.getDepotPropertiesForPermittedDepots();

				if (hostUpdateCollection != null) {
					updateCollection.remove(hostUpdateCollection);
				}

				hostUpdateCollection = new HostUpdateCollection(persist);
				addToGlobalUpdateCollection(hostUpdateCollection);

				mainFrame.panelHostProperties.initMultipleHostsEditing(
						Configed.getResourceValue("PanelHostProperties.SelectHost"),
						new DefaultComboBoxModel<>(depotPropertiesForPermittedDepots.keySet().toArray(new String[0])),
						depotPropertiesForPermittedDepots, hostUpdateCollection,
						AbstractPersistenceController.KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT);
			}

			return true;
		} catch (Exception ex) {
			Logging.error("Error in setHostPropertiesPage: " + ex, ex);
			return false;
		}
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

		for (String key : keysForDeleting) {
			m.remove(key);
		}
	}

	public boolean setNetworkconfigurationPage() {
		Logging.info(this, "setNetworkconfigurationPage ");
		Logging.info(this,
				"setNetworkconfigurationPage  getSelectedClients() " + Arrays.toString(getSelectedClients()));

		try {
			if (editingTarget == EditingTarget.SERVER) {
				objectIds = new String[] { myServer };
			} else {
				objectIds = getSelectedClients();
			}

			if (additionalconfigurationUpdateCollection != null) {
				try {
					updateCollection.remove(additionalconfigurationUpdateCollection);
				} catch (Exception ex) {
					Logging.error(this, "removing additionalconfigurationUpdateCollection from updateCollection failed",
							ex);
				}
			}
			additionalconfigurationUpdateCollection = new AdditionalconfigurationUpdateCollection(persist, objectIds);
			addToGlobalUpdateCollection(additionalconfigurationUpdateCollection);

			if (editingTarget == EditingTarget.SERVER) {
				List<Map<String, List<Object>>> additionalConfigs = new ArrayList<>(1);

				Map<String, List<Object>> defaultValuesMap = persist.getConfigDefaultValues();

				additionalConfigs.add(defaultValuesMap);

				additionalconfigurationUpdateCollection.setMasterConfig(true);

				mainFrame.panelHostConfig.initEditing("  " + myServer + " (configuration server)",
						additionalConfigs.get(0), persist.getConfigOptions(), additionalConfigs,
						additionalconfigurationUpdateCollection, true,
						// editableOptions
						AbstractPersistenceController.PROPERTY_CLASSES_SERVER);
			} else {
				List<Map<String, Object>> additionalConfigs = new ArrayList<>(getSelectedClients().length);

				if (hostConfigs == null) {

					// serves as marker
					hostConfigs = new HashMap<>();

					for (String client : getSelectedClients()) {
						hostConfigs.put(client, persist.getConfigs().get(client));
					}
				}

				Logging.info(this, "additionalConfig fetch for " + Arrays.toString(getSelectedClients()));

				for (int i = 0; i < getSelectedClients().length; i++) {
					additionalConfigs.add(persist.getConfig(getSelectedClients()[i]));
					// with server defaults
				}

				Map<String, Object> mergedVisualMap = mergeMaps(additionalConfigs);

				Map<String, de.uib.utilities.table.ListCellOptions> configOptions = persist.getConfigOptions();

				removeKeysStartingWith(mergedVisualMap,
						AbstractPersistenceController.CONFIG_KEY_STARTERS_NOT_FOR_CLIENTS);

				mainFrame.panelHostConfig.initEditing("  " + getSelectedClientsString(), mergedVisualMap, configOptions,
						additionalConfigs, additionalconfigurationUpdateCollection, false, // editableOptions
						AbstractPersistenceController.PROPERTYCLASSES_CLIENT);

			}

			return true;
		} catch (Exception ex) {

			Logging.error("Error in setNetworkConfigurationPage: " + ex, ex);
			return false;
		}

	}

	public static void setProgressComponentStopWaiting() {
		if (strategyForLoadingData != null) {
			try {
				strategyForLoadingData.stopWaiting();
				strategyForLoadingData = null;
			} catch (Exception ex) {
				Logging.debug("Exception " + ex);
			}
		}
	}

	protected void checkHwInfo() {
		if (hwInfoClientmap == null) {
			hwInfoClientmap = new HashMap<>();
		}
	}

	public void clearHwInfo() {
		checkHwInfo();
		hwInfoClientmap.clear();
	}

	protected boolean setHardwareInfoPage() {
		Logging.info(this, "setHardwareInfoPage for, clients count " + getSelectedClients().length);

		try {
			if (firstSelectedClient == null || getSelectedClients().length == 0) {
				mainFrame.setHardwareInfoNotPossible(Configed.getResourceValue("MainFrame.noClientSelected1"), null);
			} else if (getSelectedClients().length > 1) {
				if (!PersistenceControllerFactory.sqlAndGetRows) {
					mainFrame.setHardwareInfoNotPossible(Configed.getResourceValue("MainFrame.backendSQLrequired1"),
							Configed.getResourceValue("MainFrame.backendSQLrequired2"));
				} else {
					mainFrame.setHardwareInfoMultiClients(getSelectedClients());
				}
			} else {
				checkHwInfo();
				Map<String, List<Map<String, Object>>> hwInfo = hwInfoClientmap.get(firstSelectedClient);
				if (hwInfo == null) {
					hwInfo = persist.getHardwareInfo(firstSelectedClient, true);
					hwInfoClientmap.put(firstSelectedClient, hwInfo);
				}
				mainFrame.setHardwareInfo(hwInfo);
			}

			return true;
		} catch (Exception ex) {

			Logging.error("Error in setHardwareInfoPage: " + ex, ex);
			return false;
		}
	}

	protected void clearSoftwareInfoPage() {
		mainFrame.setSoftwareAuditNullInfo("");
	}

	public void clearSwInfo() {
		// TODO, check what clearHwInfo does...
	}

	protected boolean setSoftwareInfoPage() {
		Logging.info(this, "setSoftwareInfoPage() firstSelectedClient, checkOneClientSelected " + firstSelectedClient
				+ ", " + checkOneClientSelected());

		try {
			if (firstSelectedClient == null || !checkOneClientSelected()) {
				mainFrame.setSoftwareAudit();
			} else {
				// retrieve data and check with softwaretable
				persist.getSoftwareAudit(firstSelectedClient);

				mainFrame.setSoftwareAudit(firstSelectedClient);
			}

			return true;
		} catch (Exception ex) {
			Logging.error("Error in setSoftwareInfoPage: " + ex, ex);
			return false;
		}
	}

	protected void clearLogPage() {
		mainFrame.setLogfilePanel(new HashMap<>());
	}

	public boolean updateLogPage(String logtype) {

		try {
			Logging.debug(this, "updatelogpage");

			if (!checkOneClientSelected()) {
				return false;
			}

			persist.getLogfiles(firstSelectedClient, logtype);
		} catch (Exception ex) {
			Logging.error("Error in updateLogPage: " + ex, ex);
			return false;
		}
		return true;
	}

	public boolean logfileExists(String logtype) {
		return logfiles != null && logfiles.get(logtype) != null && !logfiles.get(logtype).equals("")
				&& !logfiles.get(logtype).equals(Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
	}

	public Map<String, String> getLogfilesUpdating(String logtypeToUpdate) {
		Logging.info(this, "getLogfilesUpdating " + logtypeToUpdate);

		if (!checkOneClientSelected()) {
			for (int i = 0; i < Globals.getLogTypes().length; i++) {
				logfiles.put(Globals.getLogType(i), Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
			}

			mainFrame.setLogfilePanel(logfiles);
		} else {

			try {

				WaitCursor waitCursor = new WaitCursor(ConfigedMain.getMainFrame(), "getLogfilesUpdating");
				logfiles = persist.getLogfiles(firstSelectedClient, logtypeToUpdate);
				waitCursor.stop();

				Logging.debug(this, "log pages set");
			} catch (Exception ex) {

				Logging.error("Error in setLogPage: " + ex, ex);
			}
		}

		return logfiles;
	}

	protected boolean setLogPage() {

		Logging.debug(this, "setLogPage(), selected clients: " + Arrays.toString(getSelectedClients()));

		try {
			logfiles = persist.getEmptyLogfiles();
			mainFrame.setUpdatedLogfilePanel("instlog");
			mainFrame.setLogview("instlog");

			return true;
		} catch (Exception ex) {
			Logging.error("Error in setLogPage: " + ex, ex);
			return false;
		}
	}

	// extra tasks not done by resetView
	protected boolean setView(int viewIndex) {
		if (viewIndex == VIEW_CLIENTS) {
			checkErrorList();

			depotsList.setEnabled(true);
		}

		return true;
	}

	public boolean resetView(int viewIndex) {
		Logging.info(this, "resetView to " + viewIndex + "  getSelectedClients " + getSelectedClients().length);

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
			result = setNetworkconfigurationPage();
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

		return result;

	}

	public void setVisualViewIndex(int i) {
		mainFrame.setVisualViewIndex(i);
	}

	public void setViewIndex(int visualViewIndex) {
		int oldViewIndex = viewIndex;

		Logging.info(this, " visualViewIndex " + visualViewIndex + ", (old) viewIndex " + viewIndex);

		boolean problem = false;

		dependenciesModel.setActualProduct("");

		// if we are leaving some tab we check first if we possibly have to save
		// something

		Logging.info(this, "setViewIndex anyDataChanged " + anyDataChanged);

		if (anyDataChanged && (viewIndex == VIEW_LOCALBOOT_PRODUCTS || viewIndex == VIEW_NETBOOT_PRODUCTS)) {
			if (depotsListSelectionChanged) {
				requestReloadStatesAndActions();
			} else {
				requestReloadStatesAndActions(true);
			}
		}

		saveIfIndicated();

		// we will only leave view 0 if a PC is selected

		// check if change of view index to the value of visualViewIndex can be allowed
		if (visualViewIndex != VIEW_CLIENTS && (!((visualViewIndex == VIEW_CLIENTS)
				|| ((visualViewIndex == VIEW_NETWORK_CONFIGURATION) && (editingTarget == EditingTarget.SERVER))
				|| ((visualViewIndex == VIEW_HOST_PROPERTIES) && (editingTarget == EditingTarget.DEPOTS))))) {

			Logging.debug(this, " selected clients " + Arrays.toString(getSelectedClients()));

			// should not occur
			if (getSelectedClients() == null) {
				Logging.debug(this, " getSelectedClients()  null");

				problem = true;
				JOptionPane.showMessageDialog(mainFrame, Configed.getResourceValue("ConfigedMain.pleaseSelectPc.text"),
						Configed.getResourceValue("ConfigedMain.pleaseSelectPc.title"), JOptionPane.OK_OPTION);
				viewIndex = VIEW_CLIENTS;
			}
		}

		if (!problem && dataReady) {
			// we have loaded the data

			viewIndex = visualViewIndex;

			if (viewIndex != VIEW_CLIENTS) {
				depotsList.setEnabled(false);
			}

			Logging.debug(this, "switch to viewIndex " + viewIndex);

			boolean result = true;

			setView(viewIndex);
			// tasks only needed when primarily called
			result = resetView(viewIndex);
			// task needed also when recalled

			if (!result) {
				viewIndex = oldViewIndex;
				Logging.debug(" tab index could not be changed");
			}

			if (viewIndex == VIEW_CLIENTS) {
				if (reachableUpdater.isInterrupted()) {
					reachableUpdater.interrupt();
				}

				mainFrame.enableMenuItemsForClients(getSelectedClients().length);
			} else {
				mainFrame.enableMenuItemsForClients(-1);
			}

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

			if (result) {
				clearListEditors();
			}
			// dont keep product editing across views
		}
	}

	protected void updateLocalbootProductStates() {
		// localboot products
		Logging.info(this, "updateProductStates: collectChangedLocalbootStates  " + collectChangedLocalbootStates);

		if (collectChangedLocalbootStates != null && collectChangedLocalbootStates.keySet() != null
				&& !collectChangedLocalbootStates.keySet().isEmpty()) {

			Iterator<String> it0 = collectChangedLocalbootStates.keySet().iterator();

			while (it0.hasNext()) {
				String client = it0.next();
				Map<String, Map<String, String>> clientValues = collectChangedLocalbootStates.get(client);

				Logging.debug(this, "updateProductStates, collectChangedLocalbootStates , client " + client + " values "
						+ clientValues);

				if (clientValues.keySet() == null || clientValues.keySet().isEmpty()) {
					continue;
				}

				Iterator<String> it1 = clientValues.keySet().iterator();
				while (it1.hasNext()) {
					String product = it1.next();

					Map<String, String> productValues = clientValues.get(product);

					persist.updateProductOnClient(client, product, OpsiPackage.TYPE_LOCALBOOT, productValues);
				}
			}

			// send the collected items
			persist.updateProductOnClients();
		}
	}

	protected void updateNetbootProductStates() {
		// netboot products
		Logging.debug(this, "collectChangedNetbootStates  " + collectChangedNetbootStates);
		if (collectChangedNetbootStates != null && collectChangedNetbootStates.keySet() != null
				&& !collectChangedNetbootStates.keySet().isEmpty()) {
			Iterator<String> it0 = collectChangedNetbootStates.keySet().iterator();

			while (it0.hasNext()) {
				String client = it0.next();
				Map<String, Map<String, String>> clientValues = collectChangedNetbootStates.get(client);

				if (clientValues.keySet() == null || clientValues.keySet().isEmpty()) {
					continue;
				}

				Iterator<String> it1 = clientValues.keySet().iterator();
				while (it1.hasNext()) {
					String product = it1.next();
					Map<String, String> productValues = clientValues.get(product);

					persist.updateProductOnClient(client, product, OpsiPackage.TYPE_NETBOOT, productValues);
				}
			}

			// send the collected items
			persist.updateProductOnClients();
		}

		if (istmForSelectedClientsNetboot != null) {
			istmForSelectedClientsNetboot.clearCollectChangedStates();
		}

		if (istmForSelectedClientsLocalboot != null) {
			istmForSelectedClientsLocalboot.clearCollectChangedStates();
		}
	}

	protected void updateProductStates() {
		updateLocalbootProductStates();
		updateNetbootProductStates();
	}

	public void initServer() {
		checkSaveAll(true);
		preSaveSelectedClients = saveSelectedClients;
		Logging.debug(this, "initServer() preSaveSelectedClients " + preSaveSelectedClients);
		setSelectedClients((List<String>) null);
		Logging.debug(this, "set selected values in initServer()");
	}

	public String[] getSelectedDepots() {
		return selectedDepots;
	}

	public List<String> getSelectedDepotsV() {
		return selectedDepotsV;
	}

	public Set<String> getAllowedClients() {
		return allowedClients;
	}

	public List<String> getAccessedDepots() {
		List<String> accessedDepots = new ArrayList<>();
		for (String depot : selectedDepotsV) {
			if (persist.hasDepotPermission(depot)) {
				accessedDepots.add(depot);
			}
		}

		return accessedDepots;
	}

	public List<String> getProductNames() {
		return localbootProductnames;
	}

	public List<String> getAllProductNames() {
		List<String> productnames = new ArrayList<>(localbootProductnames);
		productnames.addAll(netbootProductnames);

		Logging.info(this, "productnames " + productnames);

		return productnames;
	}

	protected String[] getDepotArray() {
		if (depots == null) {
			return new String[] {};
		}

		return depots.keySet().toArray(new String[0]);
	}

	protected void fetchDepots() {
		Logging.info(this, "fetchDepots");

		// for testing activated in 4.0.6.0.9
		if (depotsList.getListSelectionListeners().length > 0) {
			depotsList.removeListSelectionListener(depotsListSelectionListener);
		}

		depotsList.getSelectionModel().setValueIsAdjusting(true);

		String[] depotsListSelectedValues = getSelectedDepots();

		depots = persist.getHostInfoCollections().getDepots();

		depotNamesLinked = persist.getHostInfoCollections().getDepotNamesList();

		Logging.debug(this, "fetchDepots sorted depots " + depotNamesLinked);

		if (depots.size() == 1) {
			multiDepot = false;
		} else {
			multiDepot = true;
		}

		Logging.debug(this, "we have multidepot " + multiDepot);

		depotsList.setListData(getLinkedDepots());

		Logging.debug(this, " ----------  selected after fetch " + getSelectedDepots().length);

		boolean[] depotsListIsSelected = new boolean[depotsList.getModel().getSize()];

		for (int j = 0; j < depotsListSelectedValues.length; j++) {
			// collect all indices where the value had been selected
			depotsList.setSelectedValue(depotsListSelectedValues[j], false);
			if (depotsList.getSelectedIndex() > -1) {
				depotsListIsSelected[depotsList.getSelectedIndex()] = true;
			}
		}

		for (int i = 0; i < depotsListIsSelected.length; i++) {
			// combine the selections to a new selection
			if (depotsListIsSelected[i]) {
				depotsList.addSelectionInterval(i, i);
			}
		}

		if (mainFrame != null) {
			mainFrame.setChangedDepotSelectionActive(false);
		}

		depotsList.getSelectionModel().setValueIsAdjusting(false);
		depotsListSelectionChanged = false;
		depotsList.addListSelectionListener(depotsListSelectionListener);
	}

	public List<String> getLinkedDepots() {
		return new ArrayList<>(depotNamesLinked);
	}

	public String getConfigserver() {
		return myServer;
	}

	public void reloadLicensesData() {
		Logging.info(this, " reloadLicensesData _______________________________ ");
		if (dataReady) {
			persist.licencesUsageRequestRefresh();
			persist.relationsAuditSoftwareToLicencePoolsRequestRefresh();
			persist.reconciliationInfoRequestRefresh();

			Iterator<AbstractControlMultiTablePanel> iter = allControlMultiTablePanels.iterator();
			while (iter.hasNext()) {
				AbstractControlMultiTablePanel cmtp = iter.next();
				if (cmtp != null) {
					for (int i = 0; i < cmtp.getTablePanes().size(); i++) {
						PanelGenEditTable p = cmtp.getTablePanes().get(i);
						p.reload();
					}
				}
			}

		}
	}

	private void refreshClientListKeepingGroup() {
		// dont do anything if we did not finish another thread for this
		if (dataReady) {
			String oldGroupSelection = activatedGroupModel.getGroupName();
			Logging.info(this, " ==== refreshClientListKeepingGroup oldGroupSelection " + oldGroupSelection);

			refreshClientList();
			activateGroup(true, oldGroupSelection);
		}
	}

	private void changeDepotSelection() {
		Logging.info(this, "changeDepotSelection");
		if (mainFrame != null) {
			// by starting a thread the visual marker of changing in progress works
			SwingUtilities.invokeLater(() -> {
				mainFrame.setChangedDepotSelectionActive(true);
				refreshClientListKeepingGroup();
				mainFrame.setChangedDepotSelectionActive(false);
			});
		} else {
			refreshClientListKeepingGroup();
		}

		depotsListSelectionChanged = false;
	}

	public void reload() {
		if (mainFrame != null) {
			mainFrame.setChangedDepotSelectionActive(false);
			SwingUtilities.invokeLater(this::reloadData);
		} else {
			reloadData();
		}
	}

	private void reloadData() {
		checkSaveAll(true);
		int saveViewIndex = getViewIndex();

		Logging.info(this, " reloadData _______________________________  saveViewIndex " + saveViewIndex);

		// stop all old waiting threads if there should be any left
		WaitCursor.stopAll();

		List<String> selValuesList = selectionPanel.getSelectedValues();

		Logging.info(this, "reloadData, selValuesList.size " + selValuesList.size());

		String[] savedSelectedValues = selValuesList.toArray(new String[selValuesList.size()]);

		if (selectionPanel != null) {
			// deactivate temporarily listening to list selection events
			selectionPanel.removeListSelectionListener(this);
		}

		// dont do anything if we did not finish another thread for this
		if (dataReady) {
			allowedClients = null;

			de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText.reset();

			persist.requestReloadOpsiDefaultDomain();
			persist.userConfigurationRequestReload();
			persist.checkConfiguration();

			persist.opsiInformationRequestRefresh();
			persist.hwAuditConfRequestRefresh();
			persist.client2HwRowsRequestRefresh();

			Logging.info(this, "call installedSoftwareInformationRequestRefresh()");
			persist.installedSoftwareInformationRequestRefresh();
			persist.softwareAuditOnClientsRequestRefresh();

			persist.productDataRequestRefresh();

			Logging.info(this, "reloadData _1");

			// calls again persist.productDataRequestRefresh()
			mainFrame.panelProductProperties.paneProducts.reload();
			Logging.info(this, "reloadData _2");

			// if variable modelDataValid in GenTableModel has no function , the following
			// statement is sufficient:

			// only for licenses, will be handled in another method

			persist.configOptionsRequestRefresh();

			if (mainFrame != null && mainFrame.fDialogOpsiLicensingInfo != null) {
				mainFrame.fDialogOpsiLicensingInfo.thePanel.reload();
			}

			requestRefreshDataForClientSelection();

			reloadHosts();

			persist.fProductGroup2MembersRequestRefresh();
			persist.auditHardwareOnHostRequestRefresh();

			// clearing softwareMap in OpsiDataBackend
			de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataBackend.getInstance().setReloadRequested();

			clearSwInfo();
			clearHwInfo();

			// sets dataReady
			preloadData();

			Logging.info(this, " in reload, we are in thread " + Thread.currentThread());

			setRebuiltClientListTableModel();

			if (mainFrame.controllerHWinfoMultiClients != null) {
				mainFrame.controllerHWinfoMultiClients.rebuildModel();
			}

			fetchDepots();

			// configuratio
			persist.getHostInfoCollections().getAllDepots();

			// we do this again since we reloaded the configuration
			persist.checkConfiguration();
		}

		// sets visual view index, therefore:
		setEditingTarget(editingTarget);

		// if depot selection changed, we adapt the clients
		NavigableSet<String> clientsLeft = new TreeSet<>();

		for (String client : savedSelectedValues) {
			if (persist.getHostInfoCollections().getMapPcBelongsToDepot().get(client) != null) {
				String clientDepot = persist.getHostInfoCollections().getMapPcBelongsToDepot().get(client);

				if (selectedDepotsV.contains(clientDepot)) {
					clientsLeft.add(client);
				}
			}
		}

		Logging.info(this, "reloadData, selected clients now " + Logging.getSize(clientsLeft));

		// no action before gui initialized
		if (selectionPanel != null) {
			// reactivate selection listener

			Logging.debug(this, " reset the values, particularly in list ");

			selectionPanel.addListSelectionListener(this);
			setSelectedClientsCollectionOnPanel(clientsLeft);

			// no list select item is provided
			if (clientsLeft.isEmpty()) {
				selectionPanel.fireListSelectionEmpty(this);
			}
		}

		Logging.info(this, "reloadData, selected clients now, after resetting " + Logging.getSize(selectedClients));

		mainFrame.reloadServerMenu();
	}

	public HostsStatusInfo getHostsStatusInfo() {
		return mainFrame.getHostsStatusInfo();
	}

	public TableModel getSelectedClientsTableModel() {
		return selectionPanel.getSelectedRowsModel();
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
									Configed.getResourceValue("MainFrame.SaveChangedValue.YES") });
					fAskSaveProductConfiguration
							.setMessage(Configed.getResourceValue("ConfigedMain.reminderSaveConfig"));

					fAskSaveProductConfiguration.setSize(new Dimension(300, 220));
				}

				fAskSaveProductConfiguration.setLocationRelativeTo(mainFrame);
				fAskSaveProductConfiguration.setVisible(true);

				result = (fAskSaveProductConfiguration.getResult() == 2);

				fAskSaveProductConfiguration.setVisible(false);
			}

			return result;
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
	}

	/* ============================================ */

	public HostConfigsDataChangedKeeper getHostConfigsDataChangedKeeper() {
		return hostConfigsDataChangedKeeper;
	}

	/*
	 * ============================================
	 * inner class userconfigDataChangedKeeper
	 * ===========================================
	 */
	public class HostConfigsDataChangedKeeper extends GeneralDataChangedKeeper {
		// TODO remove? Because it actually does not change anything
		public HostConfigsDataChangedKeeper() {
			super();
		}
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
		Map<String, Map<String, String>> source;

		// we use this, but it would not override, therefore we perform a cast
		// it does not guarantee that the values of the map are maps!

		@Override
		public void dataHaveChanged(Object source1) {
			this.source = (Map<String, Map<String, String>>) source1;

			Logging.debug(this, "dataHaveChanged source " + source);

			if (source == null) {
				Logging.info(this, "dataHaveChanged null");
			} else {
				for (Entry<String, Map<String, String>> clientEntry : source.entrySet()) {
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
									Configed.getResourceValue("MainFrame.SaveChangedValue.YES") });
					fAskSaveChangedText.setMessage(Configed.getResourceValue("MainFrame.SaveChangedValue"));
					fAskSaveChangedText.setSize(new Dimension(300, 220));
				}

				fAskSaveChangedText.setLocationRelativeTo(mainFrame);
				fAskSaveChangedText.setVisible(true);
				result = (fAskSaveChangedText.getResult() == 2);

				fAskSaveChangedText.setVisible(false);
			}

			return result;
		}

		public void save() {
			Logging.info(this, "save , dataChanged " + dataChanged + " source " + source);
			if (this.dataChanged && source != null && getSelectedClients() != null) {
				Logging.info(this, "save for clients " + getSelectedClients().length);

				for (String client : getSelectedClients()) {
					hostInfo.showAndSaveInternally(selectionPanel, mainFrame, persist, client, source.get(client));
				}
				persist.updateHosts();

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

	// if data are changed then save or - after asking - abandon changes
	protected void saveIfIndicated() {
		Logging.info(this, "---------------- saveIfIndicated : anyDataChanged, " + anyDataChanged);

		if (!anyDataChanged) {
			return;
		}

		if (clientInfoDataChangedKeeper.askSave()) {
			clientInfoDataChangedKeeper.save();
		} else {
			// reset to old values
			hostInfo.resetGui(mainFrame);
		}
		clientInfoDataChangedKeeper.unsetDataChanged();

		if (generalDataChangedKeeper.askSave()) {
			generalDataChangedKeeper.save();
		} else {
			generalDataChangedKeeper.cancel();
		}
		generalDataChangedKeeper.unsetDataChanged();

		if (hostConfigsDataChangedKeeper.askSave()) {
			hostConfigsDataChangedKeeper.save();
		} else {
			hostConfigsDataChangedKeeper.cancel();
		}
		hostConfigsDataChangedKeeper.unsetDataChanged();

		setDataChanged(false, true);
		clearUpdateCollectionAndTell();
	}

	// save if not otherwise stated
	public void checkSaveAll(boolean ask) {
		Logging.debug(this, "----------------  checkSaveAll: anyDataChanged, ask  " + anyDataChanged + ", " + ask);

		if (anyDataChanged) {
			// without showing, but must be on first place since we run in this method again
			setDataChanged(false, false);

			if (ask) {
				if (clientInfoDataChangedKeeper.askSave()) {
					clientInfoDataChangedKeeper.save();
				} else {
					// reset to old values
					hostInfo.resetGui(mainFrame);
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
		private boolean suspended;
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
			while (true) {
				Logging.debug(this, " " + " suspended , editingTarget, viewIndex " +

						suspended + ", " + editingTarget + ", " + viewIndex

				);

				if (!suspended && editingTarget == EditingTarget.CLIENTS && viewIndex == VIEW_CLIENTS) {
					try {
						// we catch exceptions especially if we are on some updating process for the
						// model

						if (Boolean.TRUE.equals(persist.getHostDisplayFields().get("clientConnected"))) {
							Map<String, Object> saveReachableInfo = reachableInfo;

							reachableInfo = persist.reachableInfo(null);
							// update column

							reachableInfo = persist.reachableInfo(null);

							AbstractTableModel model = selectionPanel.getTableModel();

							int col = model.findColumn(
									Configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));

							for (int row = 0; row < model.getRowCount(); row++) {
								String clientId = (String) model.getValueAt(row, 0);
								Boolean newInfo = (Boolean) reachableInfo.get(clientId);

								if (newInfo != null) {
									if (saveReachableInfo.get(clientId) == null) {
										model.setValueAt(newInfo, row, col);
									} else if (model.getValueAt(row, col) != null
											&& !model.getValueAt(row, col).equals(newInfo)) {

										model.setValueAt(newInfo, row, col);

										model.fireTableRowsUpdated(row, row);
										// if ordered by col the order does not change although the value changes
										// is necessary
									}
								}
							}
						}
					} catch (Exception ex) {
						Logging.info(this, "we could not update the model");
					}
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
		final boolean onlySelectedClients = (selectedClients != null && selectedClients.length > 0);

		final String[] selClients = selectedClients;

		Logging.info(this, "we have sel clients " + selClients.length);

		// we put this into a thread since it may never end in case of a name resolving
		// problem
		new Thread() {
			@Override
			public void run() {

				if (fShowReachableInfo == null) {
					fShowReachableInfo = new FShowList(null, Globals.APPNAME, false,
							new String[] { Configed.getResourceValue("ConfigedMain.reachableInfoCancel") }, 350, 100);
				}

				fShowReachableInfo.setMessage(Configed.getResourceValue("ConfigedMain.reachableInfoRequested"));

				fShowReachableInfo.setAlwaysOnTop(true);
				fShowReachableInfo.setSize(Globals.REACHABLE_INFO_FRAME_WIDTH, Globals.REACHABLE_INFO_FRAME_HEIGHT);
				fShowReachableInfo.setLocationRelativeTo(ConfigedMain.getMainFrame());
				fShowReachableInfo.setVisible(true);
				fShowReachableInfo.toFront();
				reachableInfo = new HashMap<>();

				if (onlySelectedClients) {
					Logging.info(this, "we have sel clients " + selClients.length);
					reachableInfo.putAll(persist.reachableInfo(getSelectedClients()));
				} else {
					reachableInfo = persist.reachableInfo(null);
				}

				fShowReachableInfo.setVisible(false);

				mainFrame.iconButtonReachableInfo.setEnabled(true);

				setReachableInfo(selClients);
			}
		}.start();
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

		AbstractTableModel model = selectionPanel.getTableModel();

		int col = model.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));

		for (int row = 0; row < model.getRowCount(); row++) {

			if (model.getValueAt(row, 0).equals(clientName)) {
				model.setValueAt(getConnectionInfoForClient(clientName), row, col);

				model.fireTableCellUpdated(row, col);
				return;
			}
		}
		Logging.warning(this, "could not update connectionStatus for client " + clientName + ": Not found");
	}

	private void setReachableInfo(String[] selClients) {
		// update column
		if (Boolean.TRUE.equals(persist.getHostDisplayFields().get("clientConnected"))) {
			AbstractTableModel model = selectionPanel.getTableModel();

			int col = model.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));

			for (int row = 0; row < model.getRowCount(); row++) {

				String clientId = (String) model.getValueAt(row, 0);

				model.setValueAt(getConnectionInfoForClient(clientId), row, col);
			}

			model.fireTableDataChanged();

			setSelectedClientsOnPanel(selClients);
		}
	}

	public void getSessionInfo() {
		final boolean onlySelectedClients = (selectedClients != null) && (selectedClients.length > 0);

		final String[] selClients = selectedClients;
		sessioninfoFinished = false;

		Logging.info(this, "getSessionInfo start, onlySelectedClients " + onlySelectedClients);

		mainFrame.iconButtonSessionInfo.setWaitingState(true);

		// no old values kept
		sessionInfo = new HashMap<>();

		try {
			// leave the Event dispatching thread
			new Thread() {
				@Override
				public void run() {
					// disable the button
					try {
						SwingUtilities.invokeAndWait(() -> mainFrame.iconButtonSessionInfo.setEnabled(false));
					} catch (InvocationTargetException ex) {
						Logging.info(this,
								"invocation target or interrupt ex at  iconButtonSessionInfo.setEnabled(false) " + ex);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}

					// handling the main perspective
					final int MAX_WAIT_SECONDS = 600;

					new Thread() {
						@Override
						public void run() {
							int waitSecs = 0;

							Logging.info(this, "counting thread started");
							while (!sessioninfoFinished && waitSecs <= MAX_WAIT_SECONDS) {
								Logging.debug(this, "wait secs for session infoi " + waitSecs);
								try {
									sleep(1000);
								} catch (InterruptedException iex) {
									Logging.info(this, "interrupt at " + waitSecs);
									Thread.currentThread().interrupt();
								}
								waitSecs++;
							}

							// finishing the task
							SwingUtilities.invokeLater(() -> {
								Logging.info(this, "when sessioninfoFinished");
								mainFrame.iconButtonSessionInfo.setWaitingState(false);

								mainFrame.iconButtonSessionInfo.setEnabled(true);

								// update column
								if (Boolean.TRUE.equals(persist.getHostDisplayFields()
										.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL))) {
									AbstractTableModel model = selectionPanel.getTableModel();

									int col = model.findColumn(Configed
											.getResourceValue("ConfigedMain.pclistTableModel.clientSessionInfo"));

									for (int row = 0; row < model.getRowCount(); row++) {
										String clientId = (String) model.getValueAt(row, 0);

										model.setValueAt(sessionInfo.get(clientId), row, col);
									}

									model.fireTableDataChanged();
									setSelectedClientsOnPanel(selClients);
								}
							});
						}
					}.start();

					// fetch the data in a separated thread
					new Thread() {
						@Override
						public void run() {
							Logging.info(this, "thread started");

							if (onlySelectedClients) {
								sessionInfo.putAll(persist.sessionInfo(getSelectedClients()));
							} else {
								sessionInfo = persist.sessionInfo(null);
							}

							sessioninfoFinished = true;
						}
					}.start();
				}
			}.start();
		} catch (Exception ex) {
			Logging.info(this, "getSessionInfo Exception " + ex);
		}
	}

	public String getBackendInfos() {
		return persist.getBackendInfos();
	}

	public Map<String, RemoteControl> getRemoteControls() {
		return persist.getRemoteControls();
	}

	public void resetProductsForSelectedClients(boolean withDependencies, boolean resetLocalbootProducts,
			boolean resetNetbootProducts) {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		String confirmInfo = "";

		if (resetLocalbootProducts && resetNetbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetProducts.question");
		} else if (resetLocalbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetLocalbootProducts.question");
		} else if (resetNetbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetNetbootProducts.question");
		}

		if (!confirmActionForSelectedClients(confirmInfo)) {
			return;
		}

		if (resetLocalbootProducts) {
			persist.resetLocalbootProducts(getSelectedClients(), withDependencies);
		}

		if (resetNetbootProducts) {
			persist.resetNetbootProducts(getSelectedClients(), withDependencies);
		}

		requestReloadStatesAndActions(true);
	}

	public boolean freeAllPossibleLicencesForSelectedClients() {
		Logging.info(this, "freeAllPossibleLicencesForSelectedClients, count " + getSelectedClients().length);

		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return true;
		}

		if (!confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.confirmFreeLicences.question"))) {
			return false;
		}

		for (String client : getSelectedClients()) {
			Map<String, List<LicenceUsageEntry>> fClient2LicencesUsageList = persist.getFClient2LicencesUsageList();

			for (LicenceUsageEntry m : fClient2LicencesUsageList.get(client)) {
				persist.addDeletionLicenceUsage(client, m.getLicenceId(), m.getLicencepool());
			}
		}

		return persist.executeCollectedDeletionsLicenceUsage();
	}

	public void setEditableDomains(List<String> editableDomains) {
		this.editableDomains = editableDomains;
	}

	public void callNewClientDialog() {
		Collections.sort(localbootProductnames);
		List<String> vLocalbootProducts = new ArrayList<>(localbootProductnames);
		Collections.sort(netbootProductnames);
		List<String> vNetbootProducts = new ArrayList<>(netbootProductnames);

		NewClientDialog.getInstance(this, getLinkedDepots()).setVisible(true);
		NewClientDialog.getInstance().setGroupList(new ArrayList<>(persist.getHostGroupIds()));
		NewClientDialog.getInstance().setProductNetbootList(vNetbootProducts);
		NewClientDialog.getInstance().setProductLocalbootList(vLocalbootProducts);

		NewClientDialog.getInstance().setDomains(editableDomains);

		NewClientDialog.getInstance().useConfigDefaults(persist.isInstallByShutdownConfigured(myServer),
				persist.isUefiConfigured(myServer), persist.isWanConfigured(myServer));

		NewClientDialog.getInstance().setHostNames(persist.getHostInfoCollections().getOpsiHostNames());
	}

	public void callChangeClientIDDialog() {
		if (getSelectedClients() == null || getSelectedClients().length != 1) {
			return;
		}

		FEditText fEdit = new FEditText(getSelectedClients()[0]) {
			@Override
			protected void commit() {
				super.commit();

				String newID = getText();

				if (persist.getHostInfoCollections().getOpsiHostNames().contains(newID)) {
					FTextArea fHostExistsInfo = new FTextArea(
							getMainFrame(), Configed.getResourceValue("FGeneralDialog.title.information") + " ("
									+ Globals.APPNAME + ") ",
							true, new String[] { Configed.getResourceValue("FGeneralDialog.ok") });

					StringBuilder message = new StringBuilder();
					message.append(Configed.getResourceValue("ConfigedMain.hostExists"));
					message.append(" \"");
					message.append(newID);
					message.append("\" \n");

					fHostExistsInfo.setMessage(message.toString());
					fHostExistsInfo.setLocationRelativeTo(getMainFrame());
					fHostExistsInfo.setAlwaysOnTop(true);
					fHostExistsInfo.setVisible(true);

					return;
				}

				Logging.debug(this, "new name " + newID);

				persist.renameClient(getSelectedClients()[0], newID);

				refreshClientList(newID);
			}
		};

		fEdit.init();
		fEdit.setTitle(Configed.getResourceValue("ConfigedMain.fChangeClientID.title") + " (" + Globals.APPNAME + ")");
		fEdit.setSize(Globals.WIDTH_FRAME_RENAME_CLIENT, Globals.HEIGHT_FRAME_RENAME_CLIENT);
		fEdit.setLocationRelativeTo(ConfigedMain.getMainFrame());
		fEdit.setSingleLine(true);
		fEdit.setModal(true);
		fEdit.setAlwaysOnTop(true);
		fEdit.setVisible(true);
	}

	public void callChangeDepotDialog() {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		FShowListWithComboSelect fChangeDepotForClients = new FShowListWithComboSelect(mainFrame,
				Globals.APPNAME + " " + Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.title"), true,
				Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.newDepot"), getDepotArray(),
				new String[] { Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.OptionNO"),
						Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.OptionYES") });

		fChangeDepotForClients.setLineWrap(false);

		StringBuilder messageBuffer = new StringBuilder(
				"\n" + Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.Moving") + ": \n\n");

		for (int i = 0; i < getSelectedClients().length; i++) {
			messageBuffer.append(getSelectedClients()[i]);
			messageBuffer.append("     (from: ");
			try {
				messageBuffer
						.append(persist.getHostInfoCollections().getMapPcBelongsToDepot().get(getSelectedClients()[i]));
			} catch (Exception e) {
				Logging.warning(this, "changeDepot for " + getSelectedClients()[i] + " " + e);
			}

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

		if (targetDepot == null || targetDepot.equals("")) {
			return;
		}

		Logging.debug(this, " start moving to another depot");
		persist.getHostInfoCollections().setDepotForClients(getSelectedClients(), targetDepot);
		checkErrorList();
		refreshClientListKeepingGroup();
	}

	protected void initialTreeActivation(final String groupName) {
		Logging.info(this, "initialTreeActivation");
		treeClients.expandPath(treeClients.getPathToALL());

		String oldGroupSelection = groupName;
		if (oldGroupSelection == null) {
			oldGroupSelection = Configed.savedStates.saveGroupSelection.deserialize();
		}

		if (oldGroupSelection != null && activateGroup(true, oldGroupSelection)) {
			Logging.info(this, "old group reset " + oldGroupSelection);
		} else {
			activateGroup(true, ClientTree.ALL_CLIENTS_NAME);
		}
	}

	protected void initialTreeActivation() {
		initialTreeActivation(null);
	}

	protected void refreshClientListActivateALL() {
		Logging.info(this, "refreshClientListActivateALL");
		refreshClientList();
		activateGroup(true, ClientTree.ALL_CLIENTS_NAME);
	}

	protected void refreshClientList() {
		Logging.info(this, "refreshClientList");

		produceClientListForDepots(getSelectedDepots(), allowedClients);

		setRebuiltClientListTableModel();
	}

	protected void refreshClientList(String selectClient) {
		Logging.info(this, "refreshClientList " + selectClient);
		refreshClientListActivateALL();

		if (selectClient != null) {
			Logging.debug(this, "set client refreshClientList");
			setClient(selectClient);
		}
	}

	protected void refreshClientList(String[] selectClients) {
		Logging.info(this, "refreshClientList " + selectClients);
		refreshClientListActivateALL();

		if (selectClients != null) {
			Logging.debug(this, "set client refreshClientList");
			setClients(selectClients);
		}
	}

	protected void refreshClientList(boolean resetSelection) {
		Logging.info(this, "refreshClientList  resetSelecton " + resetSelection);
		refreshClientListActivateALL();

		if (resetSelection) {
			setClientGroup();
		}
	}

	public void reloadHosts() {
		persist.getHostInfoCollections().opsiHostsRequestRefresh();
		persist.hostConfigsRequestRefresh();
		persist.hostGroupsRequestRefresh();
		persist.fObject2GroupsRequestRefresh();
		persist.fGroup2MembersRequestRefresh();
		refreshClientListKeepingGroup();
	}

	public void createClients(List<List<Object>> clients) {
		if (persist.createClients(clients)) {
			Logging.debug(this, "createClients" + clients);
			checkErrorList();

			String[] createdClientNames = clients.stream().map(v -> (String) v.get(0) + "." + v.get(1))
					.collect(Collectors.toList()).toArray(new String[clients.size()]);

			persist.getHostInfoCollections().addOpsiHostNames(createdClientNames);
			persist.fObject2GroupsRequestRefresh();

			refreshClientListActivateALL();
			setClients(createdClientNames);
		}
	}

	public void createClient(final String hostname, final String domainname, final String depotID,
			final String description, final String inventorynumber, final String notes, final String ipaddress,
			final String systemUUID, final String macaddress, final boolean shutdownInstall, final boolean uefiBoot,
			final boolean wanConfig, final String group, final String productNetboot, final String productLocalboot) {

		Logging.debug(this,
				"createClient " + hostname + ", " + domainname + ", " + depotID + ", " + description + ", "
						+ inventorynumber + ", " + notes + shutdownInstall + ", " + uefiBoot + ", " + wanConfig + ", "
						+ group + ", " + productNetboot + ", " + productLocalboot);

		if (persist.createClient(hostname, domainname, depotID, description, inventorynumber, notes, ipaddress,
				systemUUID, macaddress, shutdownInstall, uefiBoot, wanConfig, group, productNetboot,
				productLocalboot)) {
			String newClientID = hostname + "." + domainname;

			checkErrorList();
			persist.getHostInfoCollections().addOpsiHostName(newClientID);
			persist.fObject2GroupsRequestRefresh();

			refreshClientList();

			// Activate group of created Client (and the group of all clients if no group
			// specified)
			if (!activateGroup(false, group)) {
				activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
			}

			// Sets the client on the table
			setClient(newClientID);
		}
	}

	private abstract static class AbstractErrorListProducer extends Thread {
		String title;

		AbstractErrorListProducer(String specificPartOfTitle) {
			String part = specificPartOfTitle;
			title = Globals.APPNAME + ":  " + part;
		}

		protected abstract List<String> getErrors();

		@Override
		public void run() {
			// final
			FShowList fListFeedback = new FShowList(mainFrame, title, false, new String[] { "ok" }, 800, 200);
			fListFeedback.setFont(Globals.defaultFont);
			fListFeedback.setMessage("");
			fListFeedback.setButtonsEnabled(true);
			Cursor oldCursor = fListFeedback.getCursor();
			fListFeedback.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			fListFeedback.setVisible(true);
			fListFeedback.glassTransparency(true, 800, 200, 0.04F);

			List<String> errors = getErrors();

			if (!errors.isEmpty()) {
				fListFeedback.setLines(errors);
				fListFeedback.setCursor(oldCursor);
				fListFeedback.setButtonsEnabled(true);

				fListFeedback.setVisible(true);
			} else {
				fListFeedback.leave();
			}
		}
	}

	public void wakeUp(final String[] clients, String startInfo) {
		if (clients == null) {
			return;
		}

		Logging.info(this, "wakeUp " + clients.length + " clients: " + startInfo);
		if (clients.length == 0) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoWakeClients") + " " + startInfo) {
			@Override
			protected List<String> getErrors() {
				return persist.wakeOnLan(clients);
			}
		}.start();
	}

	public void wakeSelectedClients() {
		wakeUp(getSelectedClients(), "");
	}

	public void wakeUpWithDelay(final int delaySecs, final String[] clients, String startInfo) {
		if (clients == null) {
			return;
		}

		Logging.info(this, "wakeUpWithDelay " + clients.length + " clients: " + startInfo + " delay secs " + delaySecs);

		if (clients.length == 0) {
			return;
		}

		final FWakeClients result = new FWakeClients(mainFrame,
				Globals.APPNAME + ": " + Configed.getResourceValue("FWakeClients.title") + " " + startInfo, persist);

		new Thread() {
			@Override
			public void run() {
				result.act(clients, delaySecs);
			}
		}.start();
	}

	public void wakeSelectedClientsWithDelay(final int delaySecs) {
		wakeUpWithDelay(delaySecs, getSelectedClients(), "");
	}

	public void deletePackageCachesOfSelectedClients() {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoDeletePackageCaches")) {
			@Override
			protected List<String> getErrors() {
				return persist.deletePackageCaches(getSelectedClients());
			}
		}.start();
	}

	public void fireOpsiclientdEventOnSelectedClients(final String event) {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		new AbstractErrorListProducer("opsiclientd " + event) {
			@Override
			protected List<String> getErrors() {
				return persist.fireOpsiclientdEventOnClients(event, getSelectedClients());
			}
		}.start();
	}

	public void showPopupOnSelectedClients(final String message, final Float seconds) {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoPopup") + " " + message) {
			@Override
			protected List<String> getErrors() {
				return persist.showPopupOnClients(message, getSelectedClients(), seconds);
			}

		}.start();
	}

	private void initSavedSearchesDialog() {
		if (savedSearchesDialog == null) {
			Logging.debug(this, "create SavedSearchesDialog");
			savedSearchesDialog = new SavedSearchesDialog() {
				@Override
				protected void commit() {
					super.commit();
					List<String> result = (List<String>) super.getValue();
					Logging.info(this, "commit result == null " + (result == null));
					if (result != null) {
						Logging.info(this, "result size " + result.size());
						selectionPanel.setSelectedValues(result);
					}
				}

				@Override
				protected void removeSavedSearch(String name) {
					persist.deleteSavedSearch(name);
					super.removeSavedSearch(name);
				}

				@Override
				protected void reloadAction() {
					persist.configOptionsRequestRefresh();
					persist.auditHardwareOnHostRequestRefresh();
					resetModel();
				}

				@Override
				protected void addElement() {
					callClientSelectionDialog();
				}

				@Override
				protected void editSearch(String name) {
					callClientSelectionDialog();
					clientSelectionDialog.loadSearch(name);
				}
			};
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

	private class MyDialogRemoteControl extends FDialogRemoteControl {
		public void appendLog(final String s) {
			SwingUtilities.invokeLater(() -> {
				if (s == null) {
					loggingArea.setText("");
				} else {
					loggingArea.append(s);
					loggingArea.setCaretPosition(loggingArea.getText().length());
				}
			});
		}

		@Override
		public void commit() {
			super.commit();
			setVisible(true);

			Logging.debug(this, "getSelectedValue " + getSelectedList());

			appendLog(null);

			if (!getSelectedList().isEmpty()) {
				final String selected = "" + getSelectedList().get(0);

				for (int j = 0; j < getSelectedClients().length; j++) {
					final String targetClient = getSelectedClients()[j];

					new Thread() {
						@Override
						public void run() {
							String cmd = getValue(selected);

							de.uib.utilities.script.Interpreter trans = new de.uib.utilities.script.Interpreter(
									new String[] { "%host%", "%hostname%", "%ipaddress%", "%inventorynumber%",
											"%hardwareaddress%", "%opsihostkey%", "%depotid%", "%configserverid%" });

							trans.setCommand(cmd);

							HashMap<String, String> values = new HashMap<>();
							values.put("%host%", targetClient);
							String hostName = targetClient;
							Logging.info(this, " targetClient " + targetClient);
							if (targetClient.indexOf(".") > 0) {
								String[] parts = targetClient.split("\\.");
								Logging.info(this, " targetClient " + Arrays.toString(parts));
								hostName = parts[0];
							}

							values.put("%hostname%", hostName);

							HostInfo pcInfo = persist.getHostInfoCollections().getMapOfPCInfoMaps().get(targetClient);
							values.put("%ipaddress%", pcInfo.getIpAddress());
							values.put("%hardwareaddress%", pcInfo.getMacAddress());
							values.put("%inventorynumber%", pcInfo.getInventoryNumber());
							values.put("%opsihostkey%", pcInfo.getHostKey());
							values.put("%depotid%", pcInfo.getInDepot());

							String configServerId = myServer;
							if (myServer == null || myServer.equals("")) {
								myServer = "localhost";
							}
							values.put("%configserverid%", configServerId);

							trans.setValues(values);

							cmd = trans.interpret();

							List<String> parts = de.uib.utilities.script.Interpreter.splitToList(cmd);

							try {
								Logging.debug(this,
										"startRemoteControlForSelectedClients, cmd: " + cmd + " splitted to " + parts);

								ProcessBuilder pb = new ProcessBuilder(parts);
								pb.redirectErrorStream(true);

								Process proc = pb.start();

								BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

								String line = null;
								while ((line = br.readLine()) != null) {
									appendLog(selected + " on " + targetClient + " >" + line + "\n");
								}
							} catch (Exception ex) {
								Logging.error("Runtime error for command >>" + cmd + "<<, : " + ex, ex);
							}
						}
					}.start();
				}
			}
		}
	}

	public void startRemoteControlForSelectedClients() {
		if (dialogRemoteControl == null) {
			dialogRemoteControl = new MyDialogRemoteControl();
		}

		if (remoteControls != getRemoteControls()) {
			remoteControls = getRemoteControls();

			Logging.debug(this, "remoteControls " + remoteControls);

			Map<String, String> entries = new LinkedHashMap<>();
			Map<String, String> tooltips = new LinkedHashMap<>();
			Map<String, String> rcCommands = new HashMap<>();
			Map<String, Boolean> commandsEditable = new HashMap<>();

			for (Entry<String, RemoteControl> entry : remoteControls.entrySet()) {
				entries.put(entry.getKey(), entry.getKey());
				RemoteControl rc = entry.getValue();
				if (rc.getDescription() != null && rc.getDescription().length() > 0) {
					tooltips.put(entry.getKey(), rc.getDescription());
				} else {
					tooltips.put(entry.getKey(), rc.getCommand());
				}
				rcCommands.put(entry.getKey(), rc.getCommand());
				Boolean editable = Boolean.valueOf(rc.getEditable());

				commandsEditable.put(entry.getKey(), editable);
			}

			dialogRemoteControl.setMeanings(rcCommands);
			dialogRemoteControl.setEditable(commandsEditable);

			// we want to present a sorted list of the keys
			List<String> sortedKeys = new ArrayList<>(remoteControls.keySet());
			sortedKeys.sort(Comparator.comparing(String::toString));
			dialogRemoteControl.setListModel(new DefaultComboBoxModel<>(sortedKeys.toArray(new String[0])));

			dialogRemoteControl.setCellRenderer(new ListCellRendererByIndex(entries, tooltips, null, false, ""));

			dialogRemoteControl
					.setTitle(Globals.APPNAME + ":  " + Configed.getResourceValue("MainFrame.jMenuRemoteControl"));
			dialogRemoteControl.setModal(false);
			dialogRemoteControl.init();
		}

		dialogRemoteControl.resetValue();

		dialogRemoteControl.setSize(MainFrame.F_WIDTH, mainFrame.getHeight() / 2);
		dialogRemoteControl.setLocationRelativeTo(mainFrame);

		dialogRemoteControl.setVisible(true);
		dialogRemoteControl.setDividerLocation(0.8);
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
		final ConfigedMain m = this;
		new Thread() {
			@Override
			public void run() {
				if (command.needParameter()) {
					((SSHCommandNeedParameter) command).startParameterGui(m);
				} else {
					new SSHConnectExec(m, command);
				}
			}
		}.start();
	}

	/**
	 * Starts the config dialog
	 */
	public void startSSHConfigDialog() {
		SSHConfigDialog.getInstance(this);
	}

	public SSHConfigDialog getSSHConfigDialog() {
		return SSHConfigDialog.getInstance(this);
	}

	/** Starts the control dialog */
	public void startSSHControlDialog() {
		SSHCommandControlDialog.getInstance(this);
	}

	/** Starts the terminal */
	public void startSSHOpsiServerTerminal() {
		final ConfigedMain m = this;

		new Thread(() -> new SSHConnectTerminal(m)).start();
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
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		if (confirmActionForSelectedClients(
				Configed.getResourceValue("ConfigedMain.ConfirmShutdownClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoShutdownClients")) {
				@Override
				protected List<String> getErrors() {
					return persist.shutdownClients(getSelectedClients());
				}
			}.start();
		}
	}

	public void rebootSelectedClients() {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		if (confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmRebootClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoRebootClients")) {
				@Override
				protected List<String> getErrors() {
					return persist.rebootClients(getSelectedClients());
				}
			}.start();
		}
	}

	public void deleteSelectedClients() {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		if (!confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmDeleteClients.question"))) {
			return;
		}

		persist.deleteClients(getSelectedClients());

		if (isFilterClientList()) {
			mainFrame.toggleClientFilterAction();
		}

		refreshClientListKeepingGroup();
		selectionPanel.clearSelection();
	}

	public void copySelectedClient() {
		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			return;
		}

		Optional<HostInfo> selectedClient = persist.getHostInfoCollections().getMapOfPCInfoMaps().values().stream()
				.filter(hostValues -> hostValues.getName().equals(getSelectedClients()[0])).findFirst();

		if (selectedClient.isPresent()) {
			JPanel additionalPane = new JPanel();
			additionalPane.setOpaque(false);
			GroupLayout additionalPaneLayout = new GroupLayout(additionalPane);
			additionalPane.setLayout(additionalPaneLayout);

			JLabel jLabelHostname = new JLabel(Configed.getResourceValue("ConfigedMain.jLabelHostname"));
			JTextField jTextHostname = new JTextField(new CheckedDocument(new char[] { '-', '0', '1', '2', '3', '4',
					'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
					'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' }, -1), "", 17);
			jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));
			CopySuffixAddition copySuffixAddition = new CopySuffixAddition(selectedClient.get().getName());
			jTextHostname.setText(copySuffixAddition.add());

			additionalPaneLayout.setHorizontalGroup(additionalPaneLayout
					.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE).addComponent(jLabelHostname)
					.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE).addComponent(jTextHostname));
			additionalPaneLayout.setVerticalGroup(additionalPaneLayout.createSequentialGroup()
					.addGap(Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2)
					.addComponent(jLabelHostname)
					.addGap(Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2)
					.addComponent(jTextHostname));

			additionalPane.add(jLabelHostname);
			additionalPane.add(jTextHostname);
			additionalPane.setVisible(true);

			FTextArea fAskCopyClient = new FTextArea(getMainFrame(),
					Configed.getResourceValue("MainFrame.jMenuCopyClient") + " (" + Globals.APPNAME + ") ", true,
					new String[] { Configed.getResourceValue("FGeneralDialog.no"),
							Configed.getResourceValue("FGeneralDialog.yes") },
					null, Globals.DEFAULT_FTEXTAREA_WIDTH, 230, additionalPane);

			StringBuilder message = new StringBuilder("");
			message.append(Configed.getResourceValue("ConfigedMain.confirmCopyClient"));
			message.append("\n\n");
			message.append(selectedClient.get().getName());

			fAskCopyClient.setMessage(message.toString());
			fAskCopyClient.setLocationRelativeTo(getMainFrame());
			fAskCopyClient.setAlwaysOnTop(true);
			fAskCopyClient.setVisible(true);

			if (fAskCopyClient.getResult() == 2) {
				String newClientName = jTextHostname.getText();

				if (newClientName.isEmpty()) {
					return;
				}

				HostInfo clientToCopy = selectedClient.get();
				String[] splittedClientName = clientToCopy.getName().split("\\.");
				String newClientNameWithDomain = newClientName + "." + splittedClientName[1] + "."
						+ splittedClientName[2];

				// if client already exists ask if they want to override
				if (persist.getHostInfoCollections().getOpsiHostNames().contains(newClientNameWithDomain)) {
					boolean overwriteExistingHost = ask2OverwriteExistingHost(newClientNameWithDomain);

					if (!overwriteExistingHost) {
						return;
					}
				}

				CopyClient copyClient = new CopyClient(clientToCopy, newClientName);

				Logging.info(this, "copy client with new name " + newClientName);
				copyClient.copy();

				refreshClientList(newClientName);
			}
		}
	}

	private static boolean ask2OverwriteExistingHost(String host) {
		FTextArea fAskOverwriteExsitingHost = new FTextArea(getMainFrame(),
				Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question") + " (" + Globals.APPNAME
						+ ") ",
				true, new String[] { Configed.getResourceValue("FGeneralDialog.no"),
						Configed.getResourceValue("FGeneralDialog.yes") });

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

	public void callSaveGroupDialog() {
		List<String> groupList = persist.getHostGroupIds();
		Collections.sort(groupList);
		List<String> groupSelectionIds = new ArrayList<>(groupList);

		int i = groupSelectionIds.indexOf(groupname);

		if (i < 0) {
			groupSelectionIds.add(0, TEMPGROUPNAME);
			i = 0;
		}

		GroupnameChoice choiceDialog = new GroupnameChoice(Configed.getResourceValue("ConfigedMain.saveGroup"),
				groupSelectionIds, i);

		if (choiceDialog.getResult() == 1 && !choiceDialog.getResultString().equals("")) {

			IconNode newGroupNode = treeClients.makeSubgroupAt(null);
			if (newGroupNode == null) {
				return;
			}

			String newGroupName = choiceDialog.getResultString();

			TreePath newGroupPath = treeClients.getPathToGROUPS().pathByAddingChild(newGroupNode);

			for (int j = 0; j < getSelectedClients().length; j++) {
				treeClients.copyClientTo(getSelectedClients()[j], null, newGroupName, newGroupNode, newGroupPath);
			}

			treeClients.makeVisible(newGroupPath);
			treeClients.repaint();
		}
	}

	public void callDeleteGroupDialog() {
		List<String> groupList = persist.getHostGroupIds();
		Collections.sort(groupList);
		List<String> groupSelectionIds = new ArrayList<>(groupList);

		int i = groupSelectionIds.indexOf(groupname);

		GroupnameChoice choiceDialog = new GroupnameChoice(Configed.getResourceValue("ConfigedMain.deleteGroup"),
				groupSelectionIds, i);

		if (choiceDialog.getResult() == 1 && !choiceDialog.getResultString().equals("")) {
			persist.deleteGroup(choiceDialog.getResultString());
			persist.hostGroupsRequestRefresh();
			if (clientSelectionDialog != null) {
				clientSelectionDialog.refreshGroups();
			}
		}
	}

	public void setGroupname(String name) {
		if (name == null || name.equals("")) {
			groupname = TEMPGROUPNAME;
		} else {
			groupname = name;
		}
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
			clientSelectionDialog = new ClientSelectionDialog(this, selectionPanel, savedSearchesDialog);
		}

		clientSelectionDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		clientSelectionDialog.setVisible(true);
	}

	public void clearSelectionOnPanel() {
		selectionPanel.clearSelection();
	}

	private void setSelectedClientsOnPanel(String[] selected) {
		if (selected != null) {
			Logging.info(this, " setSelectedClientsOnPanel clients count " + selected.length);
		} else {
			Logging.info(this, " setSelectedClientsOnPanel selected null");
		}

		selectionPanel.removeListSelectionListener(this);
		selectionPanel.setSelectedValues(selected);
		setSelectedClientsArray(selected);
		selectionPanel.addListSelectionListener(this);
	}

	private void setSelectedClientsCollectionOnPanel(Collection<String> selected) {
		if (selected != null) {
			Logging.info(this, "setSelectedClientsCollectionOnPanel clients count " + selected.size());
		}

		selectionPanel.setSelectedValues(selected);

		Logging.info(this, "setSelectedClientsCollectionOnPanel   selectionPanel.getSelectedValues().size() "
				+ selectionPanel.getSelectedValues().size());

		if (selected == null) {
			setSelectedClientsArray(new String[0]);
		} else {
			setSelectedClientsArray(selected.toArray(new String[selected.size()]));
		}

	}

	public void setSelectedClientsCollectionOnPanel(Collection<String> selected, boolean renewFilter) {
		boolean saveFilterClientList = false;

		if (renewFilter) {
			saveFilterClientList = isFilterClientList();
			if (saveFilterClientList) {
				setFilterClientList(false);
			}
		}

		setSelectedClientsCollectionOnPanel(selected);

		if (renewFilter && saveFilterClientList) {
			setFilterClientList(true);
		}
	}

	public void selectClientsByFailedAtSomeTimeAgo(String arg) {
		SelectionManager manager = new SelectionManager(null);

		if (arg == null || arg.equals("")) {
			manager.setSearch(de.uib.opsidatamodel.SavedSearches.SEARCH_FAILED_AT_ANY_TIME);
		} else {
			String timeAgo = DateExtendedByVars.dayValueOf(arg);
			String test = String.format(de.uib.opsidatamodel.SavedSearches.SEARCH_FAILED_BY_TIMES, timeAgo);

			Logging.info(this, "selectClientsByFailedAtSomeTimeAgo  test " + test);
			manager.setSearch(test);
		}

		List<String> result = manager.selectClients();

		setSelectedClientsCollectionOnPanel(result, true);

	}

	public void selectClientsNotCurrentProductInstalled(List<String> selectedProducts,
			boolean includeClientsWithBrokenInstallation) {
		Logging.debug(this, "selectClientsNotCurrentProductInstalled, products " + selectedProducts);
		if (selectedProducts == null || selectedProducts.size() != 1) {
			return;
		}

		String productId = selectedProducts.get(0);
		String productVersion = persist.getProductVersion(productId);
		String packageVersion = persist.getProductPackageVersion(productId);

		Logging.debug(this, "selectClientsNotCurrentProductInstalled product " + productId + ", " + productVersion
				+ ", " + packageVersion);

		List<String> clientsToSelect = persist.getClientsWithOtherProductVersion(productId, productVersion,
				packageVersion, includeClientsWithBrokenInstallation);

		Logging.info(this, "selectClientsNotCurrentProductInstalled clients found globally " + clientsToSelect.size());

		clientsToSelect.retainAll(selectionPanel.getColumnValues(0));

		Logging.info(this, "selectClientsNotCurrentProductInstalled clients found for displayed client list "
				+ clientsToSelect.size());

		setSelectedClientsCollectionOnPanel(clientsToSelect, true);
		setRebuiltClientListTableModel();
	}

	public void selectClientsWithFailedProduct(List<String> selectedProducts) {
		Logging.debug(this, "selectClientsWithFailedProduct, products " + selectedProducts);
		if (selectedProducts == null || selectedProducts.size() != 1) {
			return;
		}

		SelectionManager manager = new SelectionManager(null);

		String test = String.format(de.uib.opsidatamodel.SavedSearches.SEARCH_FAILED_PRODUCT, selectedProducts.get(0));

		manager.setSearch(test);

		List<String> result = manager.selectClients();

		Logging.info(this, "selected: " + result);
		setSelectedClientsCollectionOnPanel(result, true);
	}

	public void setClientGroup() {
		boolean wasFiltered = false;

		// no group selection on the filtered list
		if (filterClientList) {
			setFilterClientList(false);
			wasFiltered = true;
		}

		selectionPanel.clearSelection();

		Map<String, Boolean> clientList = produceClientListForDepots(getSelectedDepots(), null);
		Logging.debug(this, "setClientGroup pclist " + clientList);

		if (clientList != null) {
			TreeSet<String> selectedList = new TreeSet<>();
			for (Entry<String, Boolean> ob : clientList.entrySet()) {
				if (Boolean.TRUE.equals(ob.getValue())) {
					selectedList.add(ob.getKey());
				}
			}

			Logging.debug(this, "set selected values in setClientGroup " + selectedList);
			setSelectedClientsCollectionOnPanel(selectedList, true);
		}

		if (wasFiltered) {
			filterClientList = true;
			setRebuiltClientListTableModel();
		}
	}

	// interface LogEventObserver
	@Override
	public void logEventOccurred(LogEvent event) {

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

	protected void saveConfigs() {
		Logging.info(this, " --------  saveConfigs ");

		updateProductStates();
		Logging.debug(this, "saveConfigs: collectChangedLocalbootStates " + collectChangedLocalbootStates);

		Logging.info(this,
				" ------- we should now start working on the update collection of size  " + updateCollection.size());

		updateCollection.doCall();
		checkErrorList();

		clearUpdateCollectionAndTell();
	}

	private void clearUpdateCollectionAndTell() {
		Logging.info(this, " --- we clear the update collection " + updateCollection.getClass());

		updateCollection.clearElements();
	}

	protected void checkErrorList() {
		Logging.checkErrorList(mainFrame);
	}

	protected boolean checkSavedLicencesFrame() {
		if (allControlMultiTablePanels == null) {
			return true;
		}

		boolean change = false;
		boolean result = false;
		Iterator<AbstractControlMultiTablePanel> iter = allControlMultiTablePanels.iterator();
		while (!change && iter.hasNext()) {
			AbstractControlMultiTablePanel cmtp = iter.next();
			if (cmtp != null) {
				Iterator<PanelGenEditTable> iterP = cmtp.tablePanes.iterator();
				while (!change && iterP.hasNext()) {
					PanelGenEditTable p = iterP.next();
					change = p.isDataChanged();
				}
			}
		}

		if (change) {
			int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.Licences.AllowLeaveApp"),
					Configed.getResourceValue("ConfigedMain.Licences.AllowLeaveApp.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION) {
				result = true;
			}
		} else {
			result = true;
		}

		return result;
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

		if (dPassword != null) {
			dPassword.setVisible(false);
			dPassword.dispose();
		}

		if (!checkSavedLicencesFrame()) {
			licencesFrame.setVisible(true);
			result = false;
		}

		Logging.info(this, "close instance result " + result);

		return result;
	}

	public void finishApp(boolean checkdirty, int exitcode) {
		if (closeInstance(checkdirty)) {
			Configed.endApp(exitcode);
		}
	}
}
