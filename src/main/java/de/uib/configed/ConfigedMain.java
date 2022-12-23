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
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

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
import de.uib.configed.gui.productpage.PanelGroupedProductSettings;
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
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandNeedParameter;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsicommand.sshcommand.SSHConnectTerminal;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.datachanges.AdditionalconfigurationUpdateCollection;
import de.uib.opsidatamodel.datachanges.HostUpdateCollection;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.utilities.DataChangedKeeper;
import de.uib.utilities.logging.LogEvent;
import de.uib.utilities.logging.LogEventObserver;
import de.uib.utilities.logging.logging;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;
import de.uib.utilities.swing.FEditText;
import de.uib.utilities.swing.FLoadingWaiter;
import de.uib.utilities.swing.list.ListCellRendererByIndex;
import de.uib.utilities.swing.tabbedpane.TabClient;
import de.uib.utilities.swing.tabbedpane.TabController;
import de.uib.utilities.swing.tabbedpane.TabbedFrame;
import de.uib.utilities.table.gui.BooleanIconTableCellRenderer;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.ExternalSource;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.provider.RowsProvider;
import de.uib.utilities.table.provider.TableProvider;
import de.uib.utilities.thread.WaitCursor;

public class ConfigedMain implements ListSelectionListener, TabController, LogEventObserver

{

	public static final int VIEW_CLIENTS = 0;
	public static final int VIEW_LOCALBOOT_PRODUCTS = 1;
	public static final int VIEW_NETBOOT_PRODUCTS = 2;
	public static final int VIEW_NETWORK_CONFIGURATION = 3;
	public static final int VIEW_HARDWARE_INFO = 4;
	public static final int VIEW_SOFTWARE_INFO = 5;
	public static final int VIEW_LOG = 6;
	public static final int VIEW_PRODUCT_PROPERTIES = 7;
	public static final int VIEW_HOST_PROPERTIES = 8;

	// Dashboard disabled
	public static final boolean DASH_ENABLED = true;

	static final String TEST_ACCESS_RESTRICTED_HOST_GROUP = null;

	static final String TEMPGROUPNAME = "";

	PersistenceController persist;

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
	protected String firstSelectedClient = null;
	private String[] selectedClients = new String[] {};
	List<String> saveSelectedClients = null;
	List<String> preSaveSelectedClients = null;

	// protected TreePath[] selectedTreePaths = new TreePath[]{}; // not used since
	// we do not work with selection

	protected Map<String, TreePath> activeTreeNodes;
	protected ArrayList<TreePath> activePaths;
	// is nearly activeTreeNodes.values() , but there may be multiple paths where
	// only one entry to activeTreeNode remains ----

	protected Set<String> clientsFilteredByTree;
	protected TreePath groupPathActivatedByTree;
	protected ActivatedGroupModel activatedGroupModel;

	protected String[] objectIds = new String[] {};
	protected String[] selectedDepots = new String[] {};
	protected String[] oldSelectedDepots;
	protected Vector<String> selectedDepotsV = new Vector<>();

	protected boolean anyDataChanged = false;

	protected String clientInDepot;
	protected HostInfo hostInfo = new HostInfo();

	protected boolean groupLoading = false; // tells if a group of client is loaded via GroupManager (and not by direct
											// selection)
	protected boolean changeListByToggleShowSelection = false;
	protected boolean hostgroupChanged = false;
	protected String groupname = TEMPGROUPNAME;
	private String appTitle = Globals.APPNAME;

	protected FTextArea fAskSaveChangedText;
	protected FTextArea fAskSaveProductConfiguration;

	private SavedSearchesDialog savedSearchesDialog;
	private ClientSelectionDialog clientSelectionDialog;
	private FShowList fShowReachableInfo;

	protected String productEdited = null; // null serves als marker that we were not editing products
	protected Collection productProperties; // the properties for one product and all selected clients
	protected de.uib.opsidatamodel.datachanges.UpdateCollection updateCollection = new UpdateCollection(new Vector<>());
	protected HashMap clientProductpropertiesUpdateCollections;
	/*
	 * for each product:
	 * a collection of all clients
	 * that contains name value pairs with changed data
	 */

	protected ProductpropertiesUpdateCollection clientProductpropertiesUpdateCollection;
	protected ProductpropertiesUpdateCollection depotProductpropertiesUpdateCollection;

	protected AdditionalconfigurationUpdateCollection additionalconfigurationUpdateCollection;

	protected HostUpdateCollection hostUpdateCollection;

	protected Map collectChangedStates = new HashMap<>(); // a map of clients, for each occurring client there is a map of
															// products, where for each occurring product there may be an
															// entry stateTypeKey - value

	protected Map<String /* client */, Map<String /* product */, Map<String /* propertykey */, String/* propertyvalue */>>> collectChangedLocalbootStates = new HashMap<>();

	protected Map<String /* client */, Map<String /* product */, Map<String /* propertykey */, String/* propertyvalue */>>> collectChangedNetbootStates = new HashMap<>();

	protected Map<String, List<String>> possibleActions = new HashMap<>(); // a
																			// map
																			// of
																			// products,
																			// product
																			// -->
																			// list
																			// of

	// used
	// as
	// an
	// indicator
	// that
	// a
	// product
	// is
	// in
	// the
	// depot

	protected Map mergedProductProperties = null;

	public Map<String, Boolean> displayFieldsLocalbootProducts;
	public Map<String, Boolean> displayFieldsNetbootProducts;

	protected Set depotsOfSelectedClients = null;
	protected Set<String> allowedClients = null;

	

	protected List<String> localbootProductnames;
	protected List<String> netbootProductnames;
	protected List hwAuditConfig;

	// marker variables for requests for reload when clientlist changes
	private Map<String, List<Map<String, String>>> localbootStatesAndActions = null;
	private boolean localbootStatesAndActionsUPDATE = false;
	private Map<String, List<Map<String, String>>> netbootStatesAndActions = null;
	private Map<String, Map<String, Object>> hostConfigs = null;

	// collection of retrieved software audit and hardware maps

	private Map<String, Object> hwInfoClientmap;

	// public static final ActionRequestDisplay ardDefault = new

	// public static final ActionRequestDisplay ardNetboot = new

	protected String myServer;
	protected String opsiDefaultDomain;
	protected Vector<String> editableDomains;
	protected boolean multiDepot = false;

	private WaitCursor waitCursorInitGui;

	JTableSelectionPanel selectionPanel;

	de.uib.configed.tree.ClientTree treeClients;

	Map<String, Map<String, String>> productGroups;
	Map<String, Set<String>> productGroupMembers;

	DepotsList depotsList;
	Map<String, Map<String, Object>> depots;
	List<String> depotNamesLinked;
	int[] depotsList_selectedIndices_lastFetched;
	boolean depotsList_selectionChanged;
	private String depotRepresentative;
	ListSelectionListener depotsListSelectionListener;

	private MainFrame mainFrame;

	private ReachableUpdater reachableUpdater = new ReachableUpdater(0);

	private ArrayList<JFrame> allFrames;

	public TabbedFrame licencesFrame;

	public FGroupActions groupActionFrame;
	public FProductActions productActionFrame;

	ControlPanelEnterLicence controlPanelEnterLicence;
	ControlPanelEditLicences controlPanelEditLicences;
	ControlMultiTablePanel controlPanelAssignToLPools;
	ControlPanelLicencesStatistics controlPanelLicencesStatistics;
	ControlPanelLicencesUsage controlPanelLicencesUsage;
	ControlPanelLicencesReconciliation controlPanelLicencesReconciliation;

	Vector<ControlMultiTablePanel> allControlMultiTablePanels;

	private Dashboard dashboard;

	public static DPassword dpass;
	MyDialogRemoteControl dialogRemoteControl;
	Map<String, RemoteControl> remoteControls;

	private int clientCount = 0;
	private boolean firstDepotListChange = true;

	public final Dimension licencesInitDimension = new Dimension(1200, 800);

	protected int viewIndex = VIEW_CLIENTS;
	protected int saveClientsViewIndex = VIEW_CLIENTS;
	protected int saveDepotsViewIndex = VIEW_PRODUCT_PROPERTIES;
	protected int saveServerViewIndex = VIEW_NETWORK_CONFIGURATION;

	// public enum MainTabState {CLIENT_SELECTION, LOCALBOOT_PRODUCTS,
	// NETBOOT_PRODUCTS, NETWORK_CONFIG, HARDWARE_INVENT, SOFTWARE_INVENT};

	protected Map<String, Object> reachableInfo = new HashMap<>();
	protected Map<String, String> sessionInfo = new HashMap<>();

	protected Map<String, String> logfiles;

	public Map<String, Boolean> host_displayFields;

	public enum LicencesTabStatus {
		LICENCEPOOL, ENTER_LICENCE, EDIT_LICENCE, USAGE, RECONCILIATION, STATISTICS
	}

	protected Map<LicencesTabStatus, TabClient> licencesPanels = new EnumMap<>(LicencesTabStatus.class);
	protected LicencesTabStatus licencesStatus;

	protected Map<LicencesTabStatus, String> licencesPanelsTabNames = new EnumMap<>(LicencesTabStatus.class);

	// ==================================================================
	// TabController Interface
	@Override
	public Enum getStartTabState() {
		return LicencesTabStatus.LICENCEPOOL;
	}

	@Override
	public TabClient getClient(Enum state) {
		return licencesPanels.get(state);
	}

	@Override
	public void addClient(Enum status, TabClient panel) {
		licencesPanels.put((LicencesTabStatus) status, panel);
		licencesFrame.addTab(status, licencesPanelsTabNames.get(status), (JComponent) panel);
	}

	@Override
	public void removeClient(Enum status) {
		licencesFrame.removeTab(status);
	}

	@Override
	public Enum reactToStateChangeRequest(Enum newState) {
		logging.debug(this, "reactToStateChangeRequest( newState: " + (LicencesTabStatus) newState + "), current state "
				+ licencesStatus);
		if (newState != licencesStatus) {
			if (getClient(licencesStatus).mayLeave()) {
				licencesStatus = (LicencesTabStatus) newState;

				if (getClient(licencesStatus) != null) {
					licencesPanels.get(licencesStatus).reset();
				}
			}

			// otherwise we return the old status

		}

		return licencesStatus;
	}

	@Override
	public boolean exit() {
		if (licencesFrame != null) {
			licencesFrame.setVisible(false);// !
			mainFrame.visualizeLicencesFramesActive(false);
		}
		return true;
	}

	// ==================================================================

	private boolean dataReady = false;

	private boolean filterClientList = false;

	public static String HOST = null;
	public static String USER = null;
	public static String PASSWORD = null;
	public static String SSHKEY = null;
	public static String SSHKEYPASS = null;

	public enum EditingTarget {
		CLIENTS, DEPOTS, SERVER
	}
	// with this enum type we build a state model, which target shall be edited

	
	public static EditingTarget editingTarget = EditingTarget.CLIENTS;

	public ConfigedMain(String host, String user, String password) {
		if (HOST == null) {
			HOST = host;
		}
		if (USER == null) {
			USER = user;
		}
		if (PASSWORD == null) {
			PASSWORD = password;
		}
		SSHKEY = configed.sshkey;
		SSHKEYPASS = configed.sshkeypassphrase;

		SSHConnectionInfo.getInstance().setHost(HOST);
		SSHConnectionInfo.getInstance().setUser(USER);
		SSHConnectionInfo.getInstance().setPassw(PASSWORD);
		SSHConnectionInfo.getInstance().useKeyfile(SSHKEY != null ? true : false, SSHKEY != null ? SSHKEY : "",
				SSHKEY != null ? SSHKEYPASS : "");
		logging.registLogEventObserver(this);
	}

	public static ConfigedMain.EditingTarget getEditingTarget() {
		return editingTarget;
	}

	protected void initGui() {
		logging.info(this, "initGui");

		// persist.getProductOnClients_displayFieldsLocalbootProducts());
		displayFieldsLocalbootProducts = new LinkedHashMap<>(
				persist.getProductOnClients_displayFieldsLocalbootProducts());
		displayFieldsNetbootProducts = new LinkedHashMap<>(persist.getProductOnClients_displayFieldsNetbootProducts());
		// initialization by defaults, it can be edited afterwards

		initTree();

		allFrames = new ArrayList<>();

		initSpecialTableProviders();

		initMainFrame();

		activatedGroupModel = new ActivatedGroupModel(mainFrame.getHostsStatusInfo());

		setEditingTarget(EditingTarget.CLIENTS);

		anyDataChanged = false;

		waitCursorInitGui = new WaitCursor(mainFrame.retrieveBasePane(), mainFrame.getCursor(), "initGui");

		preloadData();

		// restrict visibility of clients to some group

		setRebuiltClientListTableModel();

		logging.debug(this, "initialTreeActivation\u0009"); // \u0009 is tab

		SwingUtilities.invokeLater(() -> {
			initialTreeActivation();
			if (configed.fProgress != null) {
				configed.fProgress.actAfterWaiting();
			}

			if (Boolean.TRUE
					.equals(persist.getGlobalBooleanConfigValue(PersistenceController.KEY_SHOW_DASH_ON_PROGRAMSTART,
							PersistenceController.DEFAULTVALUE_SHOW_DASH_ON_PROGRAMSTART))) {
				initDashInfo();
			}
		});

		reachableUpdater.setInterval(configed.refreshMinutes);
	}

	private String getSavedStatesDefaultLocation() {
		String result = "";

		if (System.getenv(logging.windowsEnvVariableAppDataDirectory) != null) {
			// Windows
			result = System.getenv(logging.windowsEnvVariableAppDataDirectory) + File.separator + "opsi.org"
					+ File.separator + "configed";
		} else {
			result = System.getProperty(logging.envVariableForUserDirectory) + File.separator + ".configed";
		}

		return result;
	}

	private String getSavedStatesDirectoryName(String locationName) {
		return locationName + File.separator + HOST.replace(":", "_");
	}

	private void initSavedStates() {
		File savedStatesDir = null;
		boolean success = true;

		if (configed.savedStatesLocationName != null) {
			logging.info(this, "trying to write saved states to " + configed.savedStatesLocationName);
			try {
				String directoryName = getSavedStatesDirectoryName(configed.savedStatesLocationName);
				savedStatesDir = new File(directoryName);
				logging.info(this, "writing saved states, created file " + success);
				savedStatesDir.mkdirs();
				logging.info(this, "writing saved states, got dirs " + success);
				savedStatesDir.setWritable(true, true);
				logging.info(this, "writing saved states, set writable ");
				configed.savedStates = new de.uib.utilities.savedstates.SavedStates(
						new File(savedStatesDir.toString() + File.separator + configed.savedStatesFilename));
			} catch (Exception ex) {
				logging.warning(this, "saved states exception " + ex);
				success = false;
			}
		}

		if (!success) {
			logging.error(this, "cannot not write saved states into " + configed.savedStatesLocationName);
		}

		if (configed.savedStatesLocationName == null || configed.savedStates == null || (!success)) {
			logging.info(this, "writing saved states to " + getSavedStatesDefaultLocation());
			savedStatesDir = new File(getSavedStatesDirectoryName(getSavedStatesDefaultLocation()));
			savedStatesDir.mkdirs();
			savedStatesDir.setWritable(true, true);
			configed.savedStates = new de.uib.utilities.savedstates.SavedStates(
					new File(savedStatesDir.toString() + File.separator + configed.savedStatesFilename));
		}

		try {
			configed.savedStates.load();
		} catch (IOException iox) {
			logging.info(this, "saved states file could not be loaded");
		}

		Integer oldUsageCount = Integer.valueOf(configed.savedStates.saveUsageCount.deserialize());
		configed.savedStates.saveUsageCount.serialize(oldUsageCount + 1);
	}

	private Vector<String> readLocallySavedServerNames() {
		Vector<String> result = new Vector<>();
		TreeMap<java.sql.Timestamp, String> sortingmap = new TreeMap<>();
		File savedStatesLocation = null;
		// the following is nearly a double of initSavedStates

		boolean success = true;

		if (configed.savedStatesLocationName != null) {
			logging.info(this, "trying to find saved states in " + configed.savedStatesLocationName);
			try {
				savedStatesLocation = new File(configed.savedStatesLocationName);
				savedStatesLocation.mkdirs();
				success = success && savedStatesLocation.setReadable(true);
			} catch (Exception ex) {
				success = false;
			}
		}

		if (!success) {
			logging.warning(this, "cannot not find saved states in " + configed.savedStatesLocationName);
		}

		if (configed.savedStatesLocationName == null || (!success)) {
			logging.info(this, "searching saved states in " + getSavedStatesDefaultLocation());
			configed.savedStatesLocationName = getSavedStatesDefaultLocation();
			savedStatesLocation = new File(getSavedStatesDefaultLocation());
			savedStatesLocation.mkdirs();
		}

		logging.info(this, "saved states location " + savedStatesLocation);

		File[] subdirs = null;

		try {
			subdirs = savedStatesLocation.listFiles(File::isDirectory);

			for (File folder : subdirs) {
				File checkFile = new File(folder + File.separator + configed.savedStatesFilename);
				String folderPath = folder.getPath();
				String elementname = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);

				// revert encryption of ":"
				if (elementname.lastIndexOf("_") > -1) {
					elementname = elementname.replace("_", ":");
				}

				sortingmap.put(new java.sql.Timestamp(checkFile.lastModified()), elementname);
			}
		} catch (SecurityException ex) {
			logging.warning("could not read file: " + ex);
		}

		for (Date date : sortingmap.descendingKeySet()) {
			result.add(sortingmap.get(date));
		}

		logging.info(this, "readLocallySavedServerNames  result " + result);

		return result;
	}

	private void setSSHallowedHosts() {
		Set<String> sshAllowedHosts = new HashSet<>();

		if (persist.isDepotsFullPermission()) {
			logging.info(this, "set ssh allowed hosts " + HOST);
			sshAllowedHosts.add(HOST);
			sshAllowedHosts.addAll(persist.getHostInfoCollections().getDepots().keySet());
		} else {
			sshAllowedHosts.addAll(persist.getDepotPropertiesForPermittedDepots().keySet());
		}

		SSHCommandFactory.getInstance(this).setAllowedHosts(sshAllowedHosts);
		logging.info(this, "ssh allowed hosts" + sshAllowedHosts);
	}

	public void initDashInfo() {
		if (!DASH_ENABLED) {
			logging.info(this, "initDashInfo not enabled");
			return;
		}

		logging.info(this, "initDashboard " + dashboard);
		if (dashboard == null) {
			dashboard = new Dashboard();
			dashboard.initAndShowGUI();
		} else {
			dashboard.show();
		}

		// if (controlDash == null )
		
		// else

	}

	// public void showDashInfo()
	// {
	// if (controlDash != null)

	// }

	public void loadDataAndGo() {
		dpass.setVisible(false);

		logging.clearErrorList();

		// errors are already handled in login
		logging.info(this, " we got persist " + persist);

		setSSHallowedHosts();

		logging.info(this, "call initData");
		initData();
		initSavedStates();
		oldSelectedDepots = configed.savedStates.saveDepotSelection.deserialize();

		Date opsiExpiresDate = persist.getOpsiExpiresDate();

		logging.info(this, " opsi modules file expires " + opsiExpiresDate);

		// too early, raises a NPE, if the user entry does not exist

		persist.syncTables();

		configed.fProgress = new FLoadingWaiter(dpass,
				Globals.APPNAME + " " + configed.getResourceValue("FWaitProgress.title"));
		((de.uib.utilities.observer.DataLoadingObservable) persist).registerDataLoadingObserver(configed.fProgress);

		if (opsiExpiresDate != null) {
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTime(new Date());

			Calendar noticeCal = Calendar.getInstance();
			noticeCal.setTime(opsiExpiresDate);

			noticeCal.add(Calendar.DAY_OF_MONTH, -14);

			if (nowCal.after(noticeCal)) {
				logging.info(this, "show notice of expiring module");
				FTextArea fMessage = new FTextArea(mainFrame, configed.getResourceValue("Permission.modules.title"),
						false, new String[] { "ok" }, 350, 150);
				fMessage.setMessage(configed.getResourceValue("Permission.modules.expires") + "\n" + opsiExpiresDate);
				fMessage.setVisible(true);
			}
		}

		configed.fProgress.startWaiting();

		new Thread() {
			@Override
			public void run() {
				initGui();

				mainFrame.initFirstSplitPane();

				waitCursorInitGui.stop();
				checkErrorList();
				if (configed.fProgress == null) {
					logging.warning(this, "configed.fProgress == null although it should have been started");
				} else {
					configed.fProgress.setReady();
					configed.fProgress.actAfterWaiting();
				}

				/*
				 * //try to ensure that we get the mainframe
				 * SwingUtilities.invokeLater(new Runnable(){
				 * public void run()
				 * {
				 * logging.info(this, "ensuring mainFrame is visible");
				 * mainFrame.setVisible(true);
				 * 
				 * if (configed.isApplet)
				 * {
				 * try
				 * {
				 * Thread.sleep(5000);
				 * }
				 * catch(InterruptedException ex)
				 * {
				 * }
				 * 
				 * adjustSize(10, appletHost.getSize().width-1, appletHost.getSize().height -
				 * 20);
				 * }
				 * }
				 * }
				 * );
				 */

			}
		}.start();

		/*
		 * if (reachableUpdater != null)
		 * {
		 * reachableUpdater.setSuspended(false);
		 * }
		 */

	}

	public void init() {
		logging.debug(this, "init");

		/*
		 * try{
		 * Thread.sleep(1000);
		 * }
		 * catch(InterruptedException ex)
		 * {
		 * }
		 */

		// we start with a language

		if (reachableUpdater != null) {
			reachableUpdater.setInterval(0);
		}

		InstallationStateTableModel.restartColumnDict();
		SWAuditEntry.setLocale();

		Vector<String> savedServers = readLocallySavedServerNames();

		login(savedServers);
	}

	// private void adjustSize(int waitMs, int newW, int newH) {
	// try {

	// } catch (InterruptedException ex) {
	// } ;

	// }

	protected void initData() {
		
		
		dependenciesModel = new DependenciesModel(persist);
		generalDataChangedKeeper = new GeneralDataChangedKeeper();
		clientInfoDataChangedKeeper = new ClientInfoDataChangedKeeper();
		hostConfigsDataChangedKeeper = new HostConfigsDataChangedKeeper();
		allControlMultiTablePanels = new Vector<>();

	}

	protected void initSpecialTableProviders() {
		Vector<String> columnNames = new Vector<>();

		columnNames.add("productId");
		columnNames.add("productName");

		// columnNames.add("depotId");

		// from OpsiPackage.appendValues
		columnNames.add(OpsiPackage.SERVICEkeyPRODUCT_TYPE);
		columnNames.add(OpsiPackage.SERVICEkeyPRODUCT_VERSION);
		columnNames.add(OpsiPackage.SERVICEkeyPACKAGE_VERSION);
		columnNames.add(OpsiPackage.SERVICEkeyLOCKED);

		Vector<String> classNames = new Vector<>();
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
					public Vector<Vector<Object>> getRows() {
						return persist.getProductRows();
					}
				}));
	}

	protected void preloadData()
	// sets dataReady = true when finished
	{
		WaitCursor waitCursor = new WaitCursor(mainFrame.retrieveBasePane(), "preloadData");

		persist.retrieveOpsiModules();
		

		if (depotRepresentative == null)
			depotRepresentative = myServer;
		persist.setDepot(depotRepresentative);
		persist.depotChange();

		opsiDefaultDomain = persist.getOpsiDefaultDomain();
		editableDomains = persist.getDomains();
		if (!editableDomains.contains(opsiDefaultDomain))
			editableDomains.add(opsiDefaultDomain);

		// System.exit(0);

		localbootProductnames = persist.getAllLocalbootProductNames();
		netbootProductnames = persist.getAllNetbootProductNames();
		persist.getProductIds();

		persist.productGroupsRequestRefresh();

		host_displayFields = persist.getHost_displayFields();
		persist.getProductOnClients_displayFieldsNetbootProducts();
		persist.getProductOnClients_displayFieldsLocalbootProducts();
		persist.configOptionsRequestRefresh();

		if (savedSearchesDialog != null) {
			savedSearchesDialog.resetModel();
		}

		// System.exit(0);

		productGroups = persist.getProductGroups();
		productGroupMembers = persist.getFProductGroup2Members();

		hwAuditConfig = persist
				.getOpsiHWAuditConf(Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry());
		mainFrame.initHardwareInfo(hwAuditConfig);
		logging.info(this, "preloadData, hw classes " + persist.getAllHwClassNames());
		mainFrame.updateHostCheckboxenText();

		persist.retrieveProducts();

		possibleActions = persist.getPossibleActions(depotRepresentative);
		persist.retrieveProductPropertyDefinitions();
		persist.retrieveProductDependencies();

		/*
		 * test
		 * List<String> startProductList = Arrays.asList( new String[]
		 * {"00_testprod_rupert"});
		 * List<String> result = persist.extendToDependentProducts(
		 * startProductList, "mydepot.uib.local" );
		 * 
		 * logging.info(this, " extendToDependentProducts " + result);
		 * System.exit(0);
		 */

		persist.retrieveDepotProductProperties();

		persist.getInstalledSoftwareInformation();

		// persist.getClient2Software();

		dataReady = true;
		waitCursor.stop();
		mainFrame.enableAfterLoading();

		persist.showLicInfoWarnings();
	}

	public void setGroupLoading(boolean b) {
		groupLoading = b;
	}

	public void toggleColumnIPAddress() {
		boolean visible = persist.getHost_displayFields().get(HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL);
		persist.getHost_displayFields().put(HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.showIPAddressColumn(!visible);
	}

	public void toggleColumnHardwareAddress() {
		boolean visible = persist.getHost_displayFields().get(HostInfo.clientMacAddress_DISPLAY_FIELD_LABEL);
		persist.getHost_displayFields().put(HostInfo.clientMacAddress_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

	}

	public void setColumnSessionInfo(boolean b) {
		boolean visible = persist.getHost_displayFields().get(HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL);
		if (visible == b) {
			return;
		}

		logging.info(this, "setColumnSessionInfo " + b);
		persist.getHost_displayFields().put(HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL, b);

		mainFrame.combinedMenuItemSessionInfoColumn.show(b);
		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}
	}

	public void toggleColumnSessionInfo() {
		boolean visible = persist.getHost_displayFields().get(HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL);

		setColumnSessionInfo(!visible);

		/*
		 * persist.getHost_displayFields().put(
		 * HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL, !visible);
		 * 
		 * mainFrame.iconButtonSessionInfo.setEnabled(!visible);
		 * 
		 * setRebuiltClientListTableModel(false, false);
		 * selectionPanel.initSortKeys();
		 * if (getSelectedClients().length > 0)
		 * selectionPanel.moveToValue(getSelectedClients()[0], 0);
		 * 
		 */

		mainFrame.combinedMenuItemSessionInfoColumn.show(!visible);
	}

	public void toggleColumnInventoryNumber() {
		boolean visible = persist.getHost_displayFields().get(HostInfo.clientInventoryNumber_DISPLAY_FIELD_LABEL);
		persist.getHost_displayFields().put(HostInfo.clientInventoryNumber_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemInventoryNumberColumn.show(!visible);
	}

	public void toggleColumnCreated() {
		boolean visible = persist.getHost_displayFields().get(HostInfo.created_DISPLAY_FIELD_LABEL);
		persist.getHost_displayFields().put(HostInfo.created_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		// mainFrame.showCreatedColumn( !visible );
		mainFrame.combinedMenuItemCreatedColumn.show(!visible);
	}

	public void toggleColumnWANactive() {
		boolean visible = persist.getHost_displayFields().get(HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL);
		persist.getHost_displayFields().put(HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemWANactiveColumn.show(!visible);
	}

	public void toggleColumnUEFIactive() {
		boolean visible = persist.getHost_displayFields().get(HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL);
		persist.getHost_displayFields().put(HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemUefiBootColumn.show(!visible);
	}

	public void toggleColumnInstallByShutdownActive() {
		Boolean visible = persist.getHost_displayFields().get(HostInfo.clientInstallByShutdown_DISPLAY_FIELD_LABEL);
		if (visible == null) {
			JOptionPane.showMessageDialog(mainFrame, "An older configed is running in the network", "Information",
					JOptionPane.OK_OPTION);
			// == null can occur if an old configed runs somewhere
		} else {
			persist.getHost_displayFields().put(HostInfo.clientInstallByShutdown_DISPLAY_FIELD_LABEL, !visible);

			setRebuiltClientListTableModel(false);
			selectionPanel.initSortKeys();
			if (getSelectedClients().length > 0) {
				selectionPanel.moveToValue(getSelectedClients()[0], 0);
			}

			mainFrame.combinedMenuItemInstallByShutdownColumn.show(!visible);
		}
	}

	public void toggleColumnDepot() {
		boolean visible = persist.getHost_displayFields().get(HostInfo.depotOfClient_DISPLAY_FIELD_LABEL);
		persist.getHost_displayFields().put(HostInfo.depotOfClient_DISPLAY_FIELD_LABEL, !visible);

		setRebuiltClientListTableModel(false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0) {
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
		}

		mainFrame.combinedMenuItemDepotColumn.show(!visible);
	}

	public boolean treeViewAllowed() {
		return true;

		/*
		 * if ( getOpsiModules() != null && getOpsiModules().get("treeview") != null
		 * && (Boolean) getOpsiModules().get("treeview"))
		 * 
		 * return true;
		 * 
		 * else
		 * 
		 * {
		 * 
		 * 
		 * FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information",
		 * true, 1);
		 * f.setMessage(configed.getResourceValue("ConfigedMain.TreeViewNotActive"));
		 * f.setSize(new Dimension(400, 400));
		 * f.setVisible(true);
		 * return false;
		 * }
		 */
	}

	public void handleGroupActionRequest() {
		if (persist.isWithLocalImaging()) {
			startGroupActionFrame();
		} else {
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", false);
			f.setMessage("not activated");// configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));
			f.setSize(new Dimension(400, 400));
			f.setVisible(true);
		}
	}

	private void startGroupActionFrame() {
		logging.info(this, "startGroupActionFrame clientsFilteredByTree " + activatedGroupModel.getAssociatedClients()
				+ " active " + activatedGroupModel.isActive());

		if (!activatedGroupModel.isActive()) {
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", false);
			f.setMessage("no group selected");// configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));
			f.setSize(new Dimension(400, 400));
			f.setVisible(true);

			return;
		}

		if (groupActionFrame == null) {
			groupActionFrame = new FGroupActions(this, persist, mainFrame);
			groupActionFrame.setSize(licencesInitDimension);
			groupActionFrame.centerOnParent();

			allFrames.add(groupActionFrame);
		}

		groupActionFrame.start();// !groupActionFrame.isVisible());
	}

	public void handleProductActionRequest() {
		startProductActionFrame();

		/*
		 * if ( persist.isWithLocalImaging())
		 * {
		 * startProductActionFrame();
		 * }
		 * 
		 * else
		 * 
		 * {
		 * FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information",
		 * true, 1);
		 * f.setMessage( "not activated");//configed.getResourceValue(
		 * "ConfigedMain.LicencemanagementNotActive"));
		 * f.setSize(new Dimension(400, 400));
		 * f.setVisible(true);
		 * }
		 */
	}

	private void startProductActionFrame() {
		logging.info(this, "startProductActionFrame ");

		if (productActionFrame == null) {
			productActionFrame = new FProductActions(this, persist, mainFrame);
			productActionFrame.setSize(licencesInitDimension);
			productActionFrame.centerOnParent();
			allFrames.add(productActionFrame);
		}

		productActionFrame.start();// !productActionFrame.isVisible());
	}

	// private void test4AllClients() {
	// List<String> allHosts =

	
	// int i = 0;

	// for (final String host : allHosts) {
	// i++;

	// try {
	// Thread.sleep(500);
	// } catch (Exception ex) {
	// }
	// SwingUtilities.invokeLater(new Thread() {
	// public void run() {

	// }
	// });
	// }
	// }

	public void handleLicencesManagementRequest() {
		logging.info(this, "handleLicencesManagementRequest called");
		persist.retrieveOpsiModules();

		if (persist.isWithLicenceManagement()) {
			toggleLicencesFrame();
		} else {
			de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText
					.callInstanceWith(configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));

			/*
			 * 
			 * FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information",
			 * false, 0);
			 * f.setMessage(configed.getResourceValue(
			 * "ConfigedMain.LicencemanagementNotActive"));
			 * f.setSize(new Dimension(400, 250));
			 * f.setVisible(true);
			 * 
			 */
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

		logging.info(this, "toggleLicencesFrame is visible" + licencesFrame.isVisible());
		licencesFrame.setVisible(true);// !licencesFrame.isVisible());
		mainFrame.visualizeLicencesFramesActive(licencesFrame.isVisible());
	}

	public void setEditingTarget(EditingTarget t) {
		logging.info(this, "setEditingTarget " + t);
		editingTarget = t;
		mainFrame.visualizeEditingTarget(t);
		// what else to do:
		switch (t) {
		case CLIENTS:
			logging.debug(this, "setEditingTarget preSaveSelectedClients " + preSaveSelectedClients);

			mainFrame.setConfigPanesEnabled(true);
			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_HostProperties")), false);
			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), false);
			mainFrame.setVisualViewIndex(saveClientsViewIndex);

			logging.debug(this, "setEditingTarget preSaveSelectedClients " + preSaveSelectedClients);

			if (!reachableUpdater.isInterrupted())
				reachableUpdater.interrupt();

			if (preSaveSelectedClients != null && !preSaveSelectedClients.isEmpty())
				setSelectedClientsOnPanel(preSaveSelectedClients.toArray(new String[] {}));

			break;
		case DEPOTS:
			logging.info(this, "setEditingTarget  DEPOTS");

			if (!reachableUpdater.isInterrupted())
				reachableUpdater.interrupt();

			initServer();
			mainFrame.setConfigPanesEnabled(false);

			// (configed.getResourceValue("MainFrame.jPanel_NetworkConfig") ) );
			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_HostProperties")), true);
			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), true);
			// mainFrame.setVisualViewIndex(mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_HostProperties")));

			logging.info(this, "setEditingTarget  call setVisualIndex  saved " + saveDepotsViewIndex + " resp. "
					+ mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

			mainFrame.setVisualViewIndex(saveDepotsViewIndex);
			// mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));
			break;
		case SERVER:
			if (!reachableUpdater.isInterrupted())
				reachableUpdater.interrupt();

			initServer();
			mainFrame.setConfigPanesEnabled(false);

			// (configed.getResourceValue("MainFrame.jPanel_NetworkConfig") ) );
			mainFrame.setConfigPaneEnabled(
					mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_NetworkConfig")), true);
			// mainFrame.setConfigPaneEnabled
			// (mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")),

			mainFrame.setVisualViewIndex(saveServerViewIndex);
			break;
		default:
			break;
		}

		resetView(viewIndex);
	}

	int shutdowncount = 0;

	// private void stoppAt(String location) {

	// FTextArea fMessage = new FTextArea(mainFrame, "stopp", true, new String[] {
	// "ok" }, 350, 150);
	// fMessage.setMessage("stop and look at\n" + this.getClass().getName() + " :\n"
	// + location);

	// }

	protected void actOnListSelection() {
		logging.info(this, "actOnListSelection");

		/*
		 * JOptionPane.showMessageDialog( mainFrame,
		 * "list selection value changed", //"not synchronous",
		 * "debug",
		 * JOptionPane.OK_OPTION);
		 */

		checkSaveAll(true);

		checkErrorList();

		/*
		 * if (!groupLoading 
		 * manager choices are not more valid
		 * && !changeListByToggleShowSelection) // and not by the show selection toggle
		 * {
		 * 
		 * logging.info(this, "empty select values");
		 * }
		 */

		logging.info(this, "selectionPanel.getSelectedValues().size(): " + selectionPanel.getSelectedValues().size());

		boolean groupingExists = selectionPanel.getSelectedValues().size() > 1;

		if (mainFrame != null) // when initializing the program the frame may not exist
		{
			logging.info(this, "ListSelectionListener valueChanged selectionPanel.isSelectionEmpty() "
					+ selectionPanel.isSelectionEmpty());

			mainFrame.saveGroupSetEnabled(groupingExists);

			if (selectionPanel.isSelectionEmpty()) {
				setSelectedClients((List) null);
				setSelectedClientsArray(new String[] {});

				// mainFrame.setClientSelectionText("");
			} else {
				setSelectedClients(selectionPanel.getSelectedValues());

			}

			clientInDepot = "";

			// if (viewIndex == viewClients)
			hostInfo.initialize();

			Map<String, HostInfo> pcinfos = persist.getHostInfoCollections().getMapOfPCInfoMaps();

			/*
			 * for (String selClient : getSelectedClients())
			 * {
			 * if (pcinfos.get(selClient) == null)
			 * {
			 * logging.info(this, "addOnListSelection pcinfo map  null for " + selClient);
			 * //should only occur when a nonexistent client is requested
			 * }
			 * else
			 * {
			 * 
			 * hostInfo.produceFrom( pcinfos.get(selClient).getMap() ) ;
			 * }
			 */

			logging.info(this,
					"actOnListSelection, produce hostInfo  getSelectedClients().length " + getSelectedClients().length);

			if (getSelectedClients().length > 0) {
				String selClient = getSelectedClients()[0];
				hostInfo.setBy(pcinfos.get(selClient).getMap());

				logging.debug(this, "actOnListSelection, produce hostInfo first selClient " + selClient);
				logging.debug(this, "actOnListSelection, produce hostInfo  " + hostInfo);

				HostInfo secondInfo = new HostInfo();

				// test

				for (int i = 1; i < getSelectedClients().length; i++) {
					selClient = getSelectedClients()[i];
					secondInfo.setBy(pcinfos.get(selClient).getMap());
					hostInfo.combineWith(secondInfo);
				}
			}

			mainFrame.setClientInfoediting(getSelectedClients().length == 1);

			/*
			 * if (getSelectedClients().length == 1)
			 * {
			 * try
			 * {
			 * String selClient = getSelectedClients()[0];
			 * 
			 * // if (viewIndex == viewClients)
			 * // {
			 * logging.debug(this, "selClient " + selClient + " infos from map " + ((Object)
			 * pcinfos));
			 * logging.info(this, "addOnListSelection selClient " + selClient);
			 * logging.info(this, "addOnListSelection pcinfo map  " +
			 * pcinfos.get(selClient));
			 * if (pcinfos.get(selClient) == null)
			 * {
			 * logging.info(this, "addOnListSelection pcinfo map  null for " + selClient);
			 * //should only occur when a nonexistent client is requested
			 * }
			 * else
			 * {
			 * 
			 * hostInfo.produceFrom( pcinfos.get(selClient).getMap() ) ;
			 * }
			 * // }
			 * 
			 * }
			 * catch (Exception ex)
			 * {
			 * logging.warning("valueChanged in ConfigedMain " + ex);
			 * 
			 * // "error occurred  "Ljava.lang.string cannot be cast do java.lang.string"
			 * }
			 * mainFrame.setClientInfoediting(true);
			 * }
			 * else
			 * {
			 * mainFrame.setClientInfoediting(false);
			 * }
			 */

			depotsOfSelectedClients = null; // initialize the following method
			Set depots = getDepotsOfSelectedClients();
			Iterator iter = depots.iterator();
			StringBuilder depotsAdded = new StringBuilder("");

			String singleDepot = "";

			if (iter.hasNext()) {
				singleDepot = (String) iter.next();
				depotsAdded.append(singleDepot);
			}

			while (iter.hasNext()) {
				String appS = (String) iter.next();
				depotsAdded.append(";\n");
				depotsAdded.append(appS);
			}

			clientInDepot = depotsAdded.toString();

			// if (viewIndex == viewClients)
			// {

			if (getSelectedClients().length == 1) {
				mainFrame.setClientID(getSelectedClients()[0]);
			} else {
				mainFrame.setClientID("");
			}

			hostInfo.resetGui(mainFrame);

			mainFrame.enableMenuItemsForClients(getSelectedClients().length);
			// }

			logging.info(this, "actOnListSelection update hosts status selectedClients " + getSelectedClients().length
					+ " as well as " + selectionPanel.getSelectedValues().size());
			// + ": " + getSelectedClientsString());

			// mainFrame.getHostsStatusInfo().updateValues(null,
			// selectionPanel.getSelectedValues().size(), getSelectedClientsString(),

			mainFrame.getHostsStatusInfo().updateValues(clientCount, getSelectedClients().length,
					getSelectedClientsStringWithMaxLength(HostsStatusPanel.maxClientnamesInField), clientInDepot);

			// if (getSelectedClients().length > 0)

			// else

			activatedGroupModel.setActive(getSelectedClients().length <= 0);

			/*
			 * TreePath first = adaptTreeSelection();
			 * 
			 * if (first != null)
			 * treeClients.scrollPathToVisible(first);
			 */

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

	// private String showIntArray(int[] a) {
	// if (a == null || a.length == 0)
	// return " ";

	
	// for (int n = 0; n < a.length; n++) {
	// b.append(" " + a[n]);
	// }

	// }

	// we call this after we have a PersistenceController
	protected void initMainFrame() {

		/*
		 * List<String> serverList = persist.getServers();
		 * if (serverList == null || serverList.size() < 1)
		 * {
		 * logging.error(" server list empty" );
		 * serverList = new ArrayList<>();
		 * serverList.add("");
		 * }
		 */
		myServer = persist.getHostInfoCollections().getConfigServer();

		// create depotsList
		depotsList = new DepotsList(persist);

		if (depotsListSelectionListener == null) {
			logging.info(this, "create depotsListSelectionListener");
			depotsListSelectionListener = new ListSelectionListener() {
				private int counter = 0;

				@Override
				public void valueChanged(ListSelectionEvent e) {
					counter++;
					logging.info(this, "============ depotSelection event count  " + counter);

					if (!e.getValueIsAdjusting()) {
						depotsList_valueChanged();
					}
				}
			};
		}

		fetchDepots();

		depotsList.addListSelectionListener(depotsListSelectionListener);
		depotsList.setInfo(depots);

		// String[] oldSelectedDepots =

		if (oldSelectedDepots == null) {
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
					depotsToSelect[j] = savedSelectedDepots.get(j); // conversion to int
				}

				depotsList.setSelectedIndices(depotsToSelect);
			} else
			// if none of the old selected depots is in the list we select the config server
			{
				depotsList.setSelectedValue(myServer, true);
			}

		}

		// mainFrame.setChangedDepotSelectionActive(false) //too early
		depotsList_selectedIndices_lastFetched = depotsList.getSelectedIndices();

		// we correct the result of the first selection
		depotsList_selectionChanged = false;
		depotsList.setBackground(Globals.backgroundWhite);

		// create client selection panel
		selectionPanel = new JTableSelectionPanel(this) {

			@Override
			protected void keyPressedOnTable(KeyEvent e) {

				// " + KeyEvent.ALT_DOWN_MASK);
				if (e.getKeyCode() == KeyEvent.VK_SPACE)
				// && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK))
				{

					startRemoteControlForSelectedClients();

				} else if (e.getKeyCode() == KeyEvent.VK_F10) {
					logging.debug(this, "keypressed: f10");
					mainFrame.showPopupClients();
				}

			}

		};

		selectionPanel.setModel(buildClientListTableModel(true));
		setSelectionPanelCols();

		selectionPanel.initSortKeys();

		mainFrame = new MainFrame(this, selectionPanel, depotsList, treeClients, multiDepot);

		// for passing it to message frames everywhere
		Globals.mainFrame = mainFrame;

		// setting the similar global values as well

		Globals.container1 = mainFrame;
		Globals.mainContainer = mainFrame;

		/*
		 * Map<String, Boolean> itemsToDisable = new HashMap<>();
		 * 
		 * itemsToDisable.put(MainFrame.ITEM_ADD_CLIENT, true);
		 * itemsToDisable.put(MainFrame.ITEM_DELETE_CLIENT, true);
		 * 
		 * mainFrame.setDisabledMenuItems(itemsToDisable);
		 */

		mainFrame.enableMenuItemsForClients(0);

		

		// rearranging visual components

		mainFrame.validate();

		// center the frame:
		
		locateAndDisplay();

		// init visual states
		logging.debug(this, "mainframe nearly initialized");

		mainFrame.saveGroupSetEnabled(false);

	}

	private void locateAndDisplay() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		int wTaken = MainFrame.F_WIDTH;
		int hTaken = MainFrame.F_HEIGHT;

		for (int i = 0; i < gs.length; i++) {
			DisplayMode dm = gs[i].getDisplayMode();
			logging.info(this, "display width " + i + ": " + dm.getWidth());
			if (dm.getWidth() > wTaken) {
				wTaken = dm.getWidth();
				hTaken = dm.getHeight();
			}
			logging.info(this, "display height " + i + ": " + dm.getHeight());
		}

		logging.info(this, "locateAndDisplay, startSizing with screen width, height " + wTaken + ", " + hTaken);

		String savedX = configed.savedStates.saveMainLocationX.deserialize();
		String savedY = configed.savedStates.saveMainLocationY.deserialize();

		String savedWidth = configed.savedStates.saveMainLocationWidth.deserialize();
		String savedHeight = configed.savedStates.saveMainLocationHeight.deserialize();

		logging.info(this, "locateAndDisplay, call buildLocation startX, startY, startWidth, startHeight, got " + savedX
				+ ", " + savedY + ", " + savedWidth + ", " + savedHeight);

		Globals.startX = null;
		Globals.startY = null;

		if (savedX != null && savedY != null) {
			Globals.startX = Integer.valueOf(savedX);
			Globals.startY = Integer.valueOf(savedY);

			Globals.startWidth = Integer.valueOf(savedWidth);
			Globals.startHeight = Integer.valueOf(savedHeight);
		}

		Rectangle screenRectangle = configed.fProgress.getGraphicsConfiguration().getBounds();

		logging.info(this, "set size and location of mainFrame");

		// weird formula for size
		mainFrame.setSize(screenRectangle.width * 19 / 20 - 100, screenRectangle.height * 19 / 20 - 100);

		// Center mainFrame on screen of configed.fProgress
		mainFrame.setLocation((int) (screenRectangle.getCenterX() - mainFrame.getSize().getWidth() / 2),
				(int) (screenRectangle.getCenterY() - mainFrame.getSize().getHeight() / 2));

		logging.info(this, "setting mainframe visible");
		mainFrame.setVisible(true);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				try {
					logging.info(this, "locateAndDisplay, setting mainframe visible in EventQueue");

					// mainFrame.panel_Clientselection.setDividerLocation(dim.width - 100);

					//mainFrame.setSize(dim.width + 2, dim.height); // corrects splitpane
					//mainFrame.panel_Clientselection.setDividerLocation(mainFrame.getWidth() - 100);

					mainFrame.setSavingFramePosition(true);

				} catch (Exception ex) {
					logging.info(this, "sizing or locating error " + ex);
				}
			}
		});

	}

	protected void initLicencesFrame() {
		long startmillis = System.currentTimeMillis();
		logging.info(this, "initLicencesFrame start " + new Date(startmillis));
		WaitCursor waitCursor = new WaitCursor(mainFrame.retrieveBasePane(), mainFrame.getCursor(),
				"initLicencesFrame");
		// general

		licencesFrame = new TabbedFrame(mainFrame, this);

		Globals.frame1 = licencesFrame;
		Globals.container1 = licencesFrame.getContentPane();

		/*
		 * is set beforehand:
		 * else
		 * {
		 * Globals.container1 = appletHost.getContentPane();
		 * }
		 */

		licencesFrame.setGlobals(Globals.getMap());
		licencesFrame.setTitle(
				Globals.APPNAME + "  " + myServer + ":  " + configed.getResourceValue("ConfigedMain.Licences"));

		licencesStatus = (LicencesTabStatus) ((TabController) this).getStartTabState();

		// global table providers
		Vector<String> columnNames = new Vector<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		Vector<String> classNames = new Vector<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		licencePoolTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, () -> (Map) persist.getLicencepools()));

		persist.retrieveRelationsAuditSoftwareToLicencePools();

		columnNames = new Vector<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		classNames = new Vector<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		licenceOptionsTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, () -> persist.getRelationsSoftwareL2LPool()));

		columnNames = new Vector<>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");
		classNames = new Vector<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		licenceContractsTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, () -> (Map) persist.getLicenceContracts()));

		columnNames = new Vector<>();
		columnNames.add(LicenceEntry.idKEY);
		columnNames.add(LicenceEntry.licenceContractIdKEY);
		columnNames.add(LicenceEntry.typeKEY);
		columnNames.add(LicenceEntry.maxInstallationsKEY);
		columnNames.add(LicenceEntry.boundToHostKEY);
		columnNames.add(LicenceEntry.expirationDateKEY);

		classNames = new Vector<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("de.uib.utilities.ExtendedInteger"); // classNames.add("java.lang.Integer");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		softwarelicencesTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, classNames, () -> (Map) persist.getSoftwareLicences()));

		// panelAssignToLPools
		licencesPanelsTabNames.put(LicencesTabStatus.LICENCEPOOL,
				configed.getResourceValue("ConfigedMain.Licences.TabLicencepools"));
		controlPanelAssignToLPools = new ControlPanelAssignToLPools(persist, this);
		addClient(LicencesTabStatus.LICENCEPOOL, controlPanelAssignToLPools.getTabClient());
		allControlMultiTablePanels.add(controlPanelAssignToLPools);

		// panelEnterLicence
		licencesPanelsTabNames.put(LicencesTabStatus.ENTER_LICENCE,
				configed.getResourceValue("ConfigedMain.Licences.TabNewLicence"));
		controlPanelEnterLicence = new ControlPanelEnterLicence(persist, this);
		addClient(LicencesTabStatus.ENTER_LICENCE, controlPanelEnterLicence.getTabClient());
		allControlMultiTablePanels.add(controlPanelEnterLicence);

		// panelEditLicence
		licencesPanelsTabNames.put(LicencesTabStatus.EDIT_LICENCE,
				configed.getResourceValue("ConfigedMain.Licences.TabEditLicence"));
		controlPanelEditLicences = new ControlPanelEditLicences(persist, this);
		addClient(LicencesTabStatus.EDIT_LICENCE, controlPanelEditLicences.getTabClient());
		allControlMultiTablePanels.add(controlPanelEditLicences);

		// panelUsage
		licencesPanelsTabNames.put(LicencesTabStatus.USAGE,
				configed.getResourceValue("ConfigedMain.Licences.TabLicenceUsage"));
		controlPanelLicencesUsage = new ControlPanelLicencesUsage(persist, this);
		addClient(LicencesTabStatus.USAGE, controlPanelLicencesUsage.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesUsage);

		// panelReconciliation
		licencesPanelsTabNames.put(LicencesTabStatus.RECONCILIATION,
				configed.getResourceValue("ConfigedMain.Licences.TabLicenceReconciliation"));
		controlPanelLicencesReconciliation = new ControlPanelLicencesReconciliation(persist, this);
		addClient(LicencesTabStatus.RECONCILIATION, controlPanelLicencesReconciliation.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesReconciliation);

		// panelStatistics
		licencesPanelsTabNames.put(LicencesTabStatus.STATISTICS,
				configed.getResourceValue("ConfigedMain.Licences.TabStatistics"));
		controlPanelLicencesStatistics = new ControlPanelLicencesStatistics(persist);
		addClient(LicencesTabStatus.STATISTICS, controlPanelLicencesStatistics.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesStatistics);

		licencesFrame.start();

		/*
		 * licencesFrame.setSize(licencesInitDimension);
		 * 
		 * //center the frame:
		 * Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		 * Dimension frameSize = licencesFrame.getSize();
		 * if (frameSize.height > screenSize.height)
		 * {
		 * frameSize.height = screenSize.height;
		 * }
		 * if (frameSize.width > screenSize.width)
		 * {
		 * frameSize.width = screenSize.width;
		 * }
		 * licencesFrame.setLocation((screenSize.width - frameSize.width) / 2,
		 * (screenSize.height - frameSize.height) / 2);
		 */

		licencesFrame.setSize(licencesInitDimension);
		final Rectangle dim = Globals.buildLocation(licencesInitDimension.width, licencesInitDimension.height, 0, 0);
		licencesFrame.setLocation(dim.x, dim.y);

		waitCursor.stop();

		long endmillis = System.currentTimeMillis();
		logging.info(this, "initLicencesFrame " + new Date(endmillis) + " diff " + (endmillis - startmillis));
	}

	protected void login(Vector<String> savedServers)
	// returns true if we have a PersistenceController and are connected
	{
		/*
		 * if (configed.isApplet)
		 * // we should be authenticated, try connection and produce persistence
		 * controller
		 * {
		 * persist = DPassword.producePersistenceController(host);
		 * return true;
		 * }
		 */

		logging.debug(this, " create password dialog ");
		dpass = new DPassword(this);

		// set list of saved servers
		if (savedServers != null && !savedServers.isEmpty()) {
			dpass.setServers(savedServers);
		}

		// check if we started with preferred values
		if (HOST != null && !HOST.equals("")) {
			dpass.setHost(HOST);
		}
		if (USER != null) {
			dpass.setUser(USER);
		}
		if (PASSWORD != null) {
			dpass.setPassword(PASSWORD);
		}

		if (((HOST != null && USER != null && PASSWORD != null))) {
			// Auto login
			logging.info(this, "start with given credentials");

			dpass.tryConnecting();
		}

		if (persist == null || persist.getConnectionState().getState() != ConnectionState.CONNECTED) {
			logging.info(this, "become interactive");

			dpass.setAlwaysOnTop(true);
			dpass.setVisible(true);
			// dpass will give back control and call loadDataAndGo
		}

		/*
		 * int countWait = 0;
		 * while
		 * (
		 * !(PersistenceControllerFactory.getConnectionState().getState() ==
		 * ConnectionState.CONNECTED)
		 * )
		 * {
		 * try
		 * {
		 * Thread.sleep (1000);
		 * countWait++;
		 * logging.info(this, "countWait " + countWait + " waited,  we are in state " +
		 * " " + PersistenceControllerFactory.getConnectionState());
		 * 
		 * }
		 * catch (InterruptedException ex)
		 * {
		 * }
		 * }
		 * 
		 * 
		 * 
		 * if ( persist != null && persist.getConnectionState().getState() ==
		 * ConnectionState.CONNECTED )
		 * {
		 * dpass.setVisible(false);
		 * result = true;
		 * }
		 * 
		 * 
		 * logging.info(this, "------ we are connected in ConfigedMain");
		 */
	}

	public PersistenceController getPersistenceController() {
		return persist;
	}

	public void setPersistenceController(PersistenceController persis) {
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

	// private String formatDate(String original) {
	// if (original != null) {
	// String[] ls = original.split("");
	// if (ls.length >= 15)
	// return ls[1] + ls[2] + ls[3] + ls[4] + '-' + ls[5] + ls[6] + '-' + ls[7] +
	// ls[8] + ' ' + ls[9] + ls[10]
	// + ':' + ls[11] + ls[12] + ':' + ls[13] + ls[14];
	// else

	// } else
	// return "";
	// }

	protected Map<String, Boolean> produceClientListForDepots(String[] depots, Set<String> allowedClients) {
		logging.info(this, " producePcListForDepots " + logging.getStrings(depots) + " running with allowedClients "
				+ allowedClients);
		Map<String, Boolean> m = persist.getHostInfoCollections().getClientListForDepots(depots, allowedClients);

		if (m != null) {
			clientCount = m.size();
		}

		if (mainFrame != null) {
			mainFrame.getHostsStatusInfo().updateValues(clientCount, null, null, null);
			// persist.getHostInfoCollections().getCountClients() > 0
			// but we are testing:
			// selectionPanel.setMissingDataPanel( pcCount < 100);
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

	/*
	 * private TreePath adaptTreeSelection()
	 * {
	 * //building tree selection paths
	 * ArrayList<TreePath> selpathlist = new ArrayList<>();
	 * 
	 * TreePath firstPath = null;
	 * 
	 * for ( int j = 0; j < getSelectedClients().length; j++)
	 * {
	 * 
	 * 
	 * ArrayList<TreePath> listforclient =
	 * treeClients.getInvertedTreePaths(getSelectedClients()[j]);
	 * 
	 * 
	 * 
	 * for ( int k = 0; k < listforclient.size(); k++)
	 * {
	 * if (firstPath == null);
	 * firstPath = listforclient.get(k);
	 * 
	 * selpathlist.add(listforclient.get(k));
	 * }
	 * }
	 * //selectedTreePaths = selpathlist.toArray(new TreePath[selpathlist.size()]);
	 * 
	 * 
	 * return firstPath;
	 * }
	 */

	// private void buildClientList(

	private int buildPclistTableModelCounter = 0;

	protected TableModel buildClientListTableModel(boolean rebuildTree) {
		logging.debug(this, " --------- buildPclistTableModel rebuildTree " + rebuildTree);
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
			logging.info(this, " --------- buildPclistTableModel not full hostgroups permission");

			permittedHostGroups = persist.getHostgroupsPermitted();
		}

		Map<String, Boolean> unfilteredList = produceClientListForDepots(getSelectedDepots(), null);

		logging.debug(this, " unfilteredList ");
		// + unfilteredList);

		buildPclistTableModelCounter++;
		logging.info(this,
				"buildPclistTableModel, counter " + buildPclistTableModelCounter + "   rebuildTree  " + rebuildTree);

		if (rebuildTree) {
			logging.info(this, "------------ buildPclistTableModel, rebuildTree " + rebuildTree);
			
			unfilteredList = produceClientListForDepots(getSelectedDepots(), null);
			String[] allPCs = new TreeMap<>(unfilteredList).keySet().toArray(new String[] {});

			logging.debug(this, "buildPclistTableModel, rebuildTree, allPCs  " + logging.getStrings(allPCs));

			treeClients.clear();
			treeClients.setClientInfo(persist.getHostInfoCollections().getMapOfAllPCInfoMaps());

			treeClients.produceTreeForALL(allPCs);

			// String[]{"fscnoteb1.uib.local"});
			treeClients.produceAndLinkGroups(persist.getHostGroups());

			logging.info(this, "------------ buildPclistTableModel, permittedHostGroups " + permittedHostGroups);
			logging.info(this, "------------ buildPclistTableModel, allPCs " + allPCs.length);
			allowedClients = treeClients.associateClientsToGroups(allPCs, persist.getFObject2Groups(),
					permittedHostGroups);

			// treeClients.associateClientsToGroups( allPCs, persist.getFObject2Groups() );
			if (allowedClients != null)
				logging.info(this, "------------ buildPclistTableModel, allowedClients " + allowedClients.size());

		}

		if (allowedClients != null)
		// changes the produced unfilteredList
		{
			unfilteredList = produceClientListForDepots(getSelectedDepots(), allowedClients);

			logging.info(this, " unfilteredList " + unfilteredList.size());
			// + unfilteredList);

			buildPclistTableModelCounter++;
			logging.info(this, "buildPclistTableModel, counter " + buildPclistTableModelCounter + "   rebuildTree  "
					+ rebuildTree);

			if (rebuildTree) {
				logging.info(this, "------------ buildPclistTableModel, rebuildTree " + rebuildTree);
				String[] allPCs = new TreeMap<>(unfilteredList).keySet().toArray(new String[] {});

				logging.debug(this, "buildPclistTableModel, rebuildTree, allPCs  " + logging.getStrings(allPCs));

				treeClients.clear();
				treeClients.setClientInfo(persist.getHostInfoCollections().getMapOfAllPCInfoMaps());

				treeClients.produceTreeForALL(allPCs);

				// String[]{"fscnoteb1.uib.local"});

				logging.info(this, "----------- buildPclistTableModel, directly allowed groups "
						+ treeClients.getDirectlyAllowedGroups());
				treeClients.produceAndLinkGroups(persist.getHostGroups());

				logging.info(this, "------------ buildPclistTableModel, allPCs (2) " + allPCs.length);

				treeClients.associateClientsToGroups(allPCs, persist.getFObject2Groups(), null // we got already
																								// allowedClients,
																								// therefore don't need
																								// the parameter
																								// hostgroupsPermitted
				);
				// treeClients.associateClientsToGroups( allPCs, persist.getFObject2Groups() );

				logging.info(this, "tree produced");
				// System.exit(0);
			}
		}

		Map<String, Boolean> pclist0 = filterMap(unfilteredList, clientsFilteredByTree);

		logging.info(this, " filterClientList " + filterClientList);

		if (filterClientList) {

			

			if (pclist0 != null) {
				Object[] pcEntries = pclist0.entrySet().toArray();

				logging.info(this,
						"buildPclistTableModel with filterCLientList " + "selected pcs " + getSelectedClients().length);

				for (int i = 0; i < pcEntries.length; i++) {
					Map.Entry ob = (Map.Entry) pcEntries[i];

					String key = (String) ob.getKey();
					for (int j = 0; j < getSelectedClients().length; j++) {

						// getSelectedClients()[j] + " ... key " );

						if (getSelectedClients()[j].equals(key)) {

							pclist.put(key, (Boolean) ob.getValue());
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
			host_displayFields = persist.getHost_displayFields();

			// test
			// host_displayFields.put("install by shutdown", true);
			// host_displayFields.put(HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL, true);
			// host_displayFields.put(HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL, true);
			// host_displayFields.put(HostInfo.depotOfClient_DISPLAY_FIELD_LABEL, true);

			for (Map.Entry<String, Boolean> entry : host_displayFields.entrySet()) {
				if (Boolean.TRUE.equals(entry.getValue())) {
					model.addColumn(configed.getResourceValue("ConfigedMain.pclistTableModel." + entry.getKey()));
				}
			}

			logging.info(this, "buildPclistTableModel host_displayFields " + host_displayFields);
			/*
			 * int countDisplayFields = 0;
			 * for (String field : host_displayFields.keySet())
			 * {
			 * if ( host_displayFields.get( field ) )
			 * countDisplayFields++;
			 * }
			 * logging.info(this, "buildPclistTableModel host_displayFields count " +
			 * countDisplayFields);
			 */

			Object[] pcs = pclist.entrySet().toArray();
			for (int i = 0; i < pcs.length; i++) {
				Map.Entry ob = (Map.Entry) pcs[i];
				Object key = ob.getKey();

				HostInfo pcinfo = pcinfos.get(key);
				if (pcinfo == null)
					pcinfo = new HostInfo();

				HashMap<String, Object> rowmap = pcinfo.getDisplayRowMap0();

				String sessionValue = "";
				if (sessionInfo.get(key) != null)
					sessionValue = "" + sessionInfo.get(key);;

				rowmap.put(HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL, sessionValue);
				rowmap.put(HostInfo.clientConnected_DISPLAY_FIELD_LABEL, (Boolean) reachableInfo.get(key));

				ArrayList<Object> rowItems = new ArrayList<>();

				for (String field : host_displayFields.keySet()) {

					// host_displayFields.get(field)), rowmap.get(field) "
					// + field + ", " + host_displayFields.get(field) + ", " + rowmap.get(field) );
					if (Boolean.TRUE.equals(host_displayFields.get(field))) {
						rowItems.add(rowmap.get(field));
					}
				}

				if (key.equals("fscnoteb1.uib.local")) {
					logging.info(this, "*** host_displayFields size, content " + host_displayFields.size() + ": "
							+ host_displayFields);
					logging.info(this, "*** rowmap size, content " + rowmap.size() + ": " + rowmap);
					logging.info(this, "*** rowItems size, content " + rowItems.size() + ": " + rowItems);
				}

				model.addRow(rowItems.toArray());
			}
		}

		logging.info(this, "buildPclistTableModel, model column count " + model.getColumnCount());

		return model;
	}

	/**
	 * selects a client
	 *
	 * @param clientName
	 */
	public void setClient(String clientName) {
		logging.info(this, "setClient " + clientName);

		if (clientName == null)
			setSelectedClientsOnPanel(new String[] {});
		else {
			setSelectedClientsOnPanel(new String[] { clientName });
			// implies:
			// setSelectedClientsArray(new String[]{clientname});
			actOnListSelection();
		}
	}

	public void setClients(String[] clientNames) {
		logging.info(this, "setClients " + clientNames);

		if (clientNames == null)
			setSelectedClientsOnPanel(new String[] {});
		else {
			setSelectedClientsOnPanel(clientNames);
			// implies:
			// setSelectedClientsArray(new String[]{clientname});
			actOnListSelection();
		}
	}

	/**
	 * activates a group
	 *
	 * @param groupname
	 */
	/*
	 * public boolean activateGroup(String groupname)
	 * {
	 * 
	 * return activateGroupWithModelRebuild(groupname, true);
	 * }
	 */
	public boolean activateGroup(String groupname) {
		logging.info(this, "activateGroup  " + groupname);
		if (groupname == null) {
			return false;
		}

		if (!treeClients.groupNodesExists() || treeClients.getGroupNode(groupname) == null) {
			logging.warning("no group " + groupname);
			return false;
		}

		GroupNode node = treeClients.getGroupNode(groupname);
		TreePath path = treeClients.getPathToNode(node);

		activateGroupByTree(false, node, path);

		logging.info(this, "expand activated  path " + path);
		treeClients.expandPath(path);

		// System.exit(0);

		return true;
	}

	/**
	 * activates a group and selects all clients
	 *
	 * @param groupname
	 */
	public void setGroup(String groupname) {
		logging.info(this, "setGroup " + groupname);
		if (!activateGroup(groupname)) {
			return;
		}

		setSelectedClientsCollectionOnPanel(clientsFilteredByTree);
	}

	protected void requestRefreshDataForClientSelection() {
		logging.info(this, "requestRefreshDataForClientSelection");
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
		logging.info(this, "requestReloadStatesAndActions , only updating " + onlyUpdate);
		// currentSortKeysLocalbootProducts =
		// mainFrame.panel_LocalbootProductsettings.getSortKeys();
		// currentSortKeysNetbootProducts =
		// mainFrame.panel_NetbootProductsettings.getSortKeys();

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
		if (a == null)
			return;

		logging.info(this, "setSelectedClientsArray " + a.length);
		logging.info(this, "selectedClients up to now size " + logging.getSize(selectedClients));

		selectedClients = a;
		if (selectedClients.length == 0)
			firstSelectedClient = "";
		else
			firstSelectedClient = selectedClients[0];

		logging.info(this, "setSelectedClientsArray produced firstSelectedClient " + firstSelectedClient);

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
			logging.info(this, "setSelectedClients clientNames null");
		} else {
			logging.info(this, "setSelectedClients clientNames size " + clientNames.size());
		}

		if (clientNames == null) {
			return;
		}

		if (clientNames.equals(saveSelectedClients)) {
			logging.info(this, "setSelectedClients clientNames.equals(saveSelectedClients)");

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

		if (getViewIndex() != VIEW_CLIENTS) // change in selection not via clientpage (i.e. via tree)
		{
			logging.debug(this, "getSelectedClients  " + logging.getStrings(getSelectedClients())
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

	public boolean getFilterClientList() {
		return filterClientList;
	}

	public void toggleFilterClientList() {
		changeListByToggleShowSelection = true;
		logging.info(this, "toggleFilterClientList   " + filterClientList);
		setFilterClientList(!filterClientList);
		changeListByToggleShowSelection = false;
	}

	public void invertClientselection() {
		selectionPanel.removeListSelectionListener(this);
		boolean oldFilterClientList = filterClientList;
		if (filterClientList)
			toggleFilterClientList();

		logging.info(this, "invertClientselection selected " + selectionPanel.getSelectedValues());
		List<String> selectedValues = new ArrayList<>(selectionPanel.getInvertedSet());

		String[] selected = selectedValues.toArray(new String[selectedValues.size()]);

		logging.info(this, "new selection " + Arrays.toString(selected));

		selectionPanel.setSelectedValues(selected);
		setSelectedClientsArray(selected);

		if (oldFilterClientList)
			toggleFilterClientList();

		selectionPanel.addListSelectionListener(this);
	}

	private void setSelectionPanelCols() {
		logging.info(this, "setSelectionPanelCols ");

		final int iconColumnMaxWidth = 100;
		final int iconColumnPrefWidth = 70;

		if (persist.getHost_displayFields().get(HostInfo.clientConnected_DISPLAY_FIELD_LABEL)) {
			int col = selectionPanel.getTableModel().findColumn(configed
					.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.clientConnected_DISPLAY_FIELD_LABEL));

			javax.swing.table.TableColumn column = selectionPanel.getColumnModel().getColumn(col);

			column.setMaxWidth(iconColumnMaxWidth);
			column.setPreferredWidth(iconColumnPrefWidth);

			column.setCellRenderer(new BooleanIconTableCellRenderer(
					// Globals.createImageIcon("images/ok22.png", ""),
					Globals.createImageIcon("images/new_network-connect2.png", ""),
					Globals.createImageIcon("images/new_network-disconnect.png", ""),
					Globals.createImageIcon("images/new_network-empty22.png", ""), false));

			// new

		}

		if (persist.getHost_displayFields().get(HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL)) {

			Vector<String> columns = new Vector<>();
			for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++) {
				columns.add(selectionPanel.getTableModel().getColumnName(i));
			}
			logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL);

			int col = selectionPanel.getTableModel().findColumn(configed
					.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL));

			logging.info(this, "setSelectionPanelCols ,  found col " + col);

			logging.info(this, "showAndSave found col " + col);

			if (col > -1) {
				javax.swing.table.TableColumn column = selectionPanel.getColumnModel().getColumn(col);
				logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue());
				column.setMaxWidth(iconColumnMaxWidth);
				column.setPreferredWidth(iconColumnPrefWidth);
				// System.exit(2);
				// column.setCellRenderer(new

				column.setCellRenderer(new BooleanIconTableCellRenderer(
						Globals.createImageIcon("images/checked_withoutbox.png", ""), null // Globals.createImageIcon("images/checked_box.png",
																																					// "")
				));

			}
		}

		if (persist.getHost_displayFields().get(HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL)) {

			Vector<String> columns = new Vector<>();
			for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++) {
				columns.add(selectionPanel.getTableModel().getColumnName(i));
			}
			logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL);

			int col = selectionPanel.getTableModel().findColumn(configed
					.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL));

			logging.info(this, "setSelectionPanelCols ,  found col " + col);

			if (col > -1) {
				javax.swing.table.TableColumn column = selectionPanel.getColumnModel().getColumn(col);
				logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue());
				column.setMaxWidth(iconColumnMaxWidth);
				column.setPreferredWidth(iconColumnPrefWidth);

				// column.setCellRenderer(new

				column.setCellRenderer(new BooleanIconTableCellRenderer(
						Globals.createImageIcon("images/checked_withoutbox.png", ""), null // Globals.createImageIcon("images/checked_box.png",
																																					// "")
				));
			}

		}

		if (persist.getHost_displayFields().get(HostInfo.clientInstallByShutdown_DISPLAY_FIELD_LABEL)) {

			Vector<String> columns = new Vector<>();

			for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++) {
				columns.add(selectionPanel.getTableModel().getColumnName(i));
			}
			logging.info(this, "showAndSave columns are " + columns + ", search for "
					+ HostInfo.clientInstallByShutdown_DISPLAY_FIELD_LABEL);

			int col = selectionPanel.getTableModel().findColumn(configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.clientInstallByShutdown_DISPLAY_FIELD_LABEL));

			logging.info(this, "setSelectionPanelCols ,  found col " + col);

			if (col > -1) {
				javax.swing.table.TableColumn column = selectionPanel.getColumnModel().getColumn(col);
				logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue());
				column.setMaxWidth(iconColumnMaxWidth);
				column.setPreferredWidth(iconColumnPrefWidth);

				// column.setCellRenderer(new

				column.setCellRenderer(new BooleanIconTableCellRenderer(
						Globals.createImageIcon("images/checked_withoutbox.png", ""), null // Globals.createImageIcon("images/checked_box.png",
																																					// "")
				));
			}

		}

	}

	protected void setRebuiltClientListTableModel() {
		setRebuiltClientListTableModel(true);
	}

	protected void setRebuiltClientListTableModel(boolean restoreSortKeys) {
		logging.info(this, "setRebuiltClientListTableModel, we have selected Set : " + selectionPanel.getSelectedSet());

		setRebuiltClientListTableModel(restoreSortKeys, true, selectionPanel.getSelectedSet());
	}

	private int reloadCounter = 0;

	protected void setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree,
			Set<String> selectValues) {
		logging.info(this,
				"setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree, Set selectValues)  : "
						+ restoreSortKeys + ", " + rebuildTree + ",  selectValues.size() "
						+ logging.getSize(selectValues));

		List<String> valuesToSelect = null;
		if (selectValues != null)
			valuesToSelect = new ArrayList<>(selectValues);

		// if (valuesToSelect == null)
		
		List<? extends RowSorter.SortKey> saveSortKeys = selectionPanel.getSortKeys();

		logging.info(this, " setRebuiltClientListTableModel--- set model new, selected "
				+ selectionPanel.getSelectedValues().size());
		// setSelectedClientsOnPanel(new String[]{});
		TableModel tm = buildClientListTableModel(rebuildTree);
		logging.info(this,
				"setRebuiltClientListTableModel --- got model selected " + selectionPanel.getSelectedValues().size());
		selectionPanel.removeListSelectionListener(this);
		// setSelectedClientsOnPanel(new String[]{});

		selectionPanel.setModel(tm);

		selectionPanel.addListSelectionListener(this);

		selectionPanel.initColumnNames();
		logging.debug(this, " --- model set  ");
		setSelectionPanelCols();

		if (restoreSortKeys)
			selectionPanel.setSortKeys(saveSortKeys);

		logging.info(this, "setRebuiltClientListTableModel set selected values in setRebuiltClientListTableModel() "
				+ logging.getSize(valuesToSelect));
		logging.info(this, "setRebuiltClientListTableModel selected in selection panel"
				+ logging.getSize(selectionPanel.getSelectedValues()));

		setSelectionPanelCols();

		setSelectedClientsCollectionOnPanel(valuesToSelect); // did lose the selection since last setting

		logging.info(this, "setRebuiltClientListTableModel selected in selection panel "
				+ logging.getSize(selectionPanel.getSelectedValues()));

		reloadCounter++;
		logging.info(this, "setRebuiltClientListTableModel  reloadCounter " + reloadCounter);
	}

	public void setFilterClientList(boolean b) {

		// value: " + b);
		filterClientList = b;
		setRebuiltClientListTableModel();

	}

	protected String getSelectedClientsString() {
		return getSelectedClientsStringWithMaxLength(null);
	}

	protected String getSelectedClientsStringWithMaxLength(Integer max) {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return "";

		StringBuffer result = new StringBuffer();
		int stop = getSelectedClients().length;
		if (max != null && stop > max)
			stop = max;

		for (int i = 0; i < stop - 1; i++) {
			result.append(getSelectedClients()[i]);
			// result.append ("; ");
			result.append(";\n");
		}

		result.append(getSelectedClients()[stop - 1]);

		if (max != null && getSelectedClients().length > max)
			result.append(" ... ");

		return result.toString();
	}

	protected Set getDepotsOfSelectedClients() {
		if (depotsOfSelectedClients == null) {
			depotsOfSelectedClients = new TreeSet<>();

			for (int i = 0; i < getSelectedClients().length; i++) {

				// "persist.getHostInfoCollections().getMapPcBelongsToDepot() " +

				if (persist.getHostInfoCollections().getMapPcBelongsToDepot().get(getSelectedClients()[i]) != null)
					depotsOfSelectedClients.add(
							persist.getHostInfoCollections().getMapPcBelongsToDepot().get(getSelectedClients()[i]));
			}
		}

		return depotsOfSelectedClients;
	}

	private void collectTheProductProperties(String productEdited)
	// we build
	// --
	// -- the map of the merged product properties from combining the properties of
	// all selected clients

	{
		logging.info(this, "collectTheProductProperties for " + productEdited);
		mergedProductProperties = new HashMap<>();
		productProperties = new ArrayList<>(getSelectedClients().length);

		if ((getSelectedClients().length > 0) && (possibleActions.get(productEdited) != null)) {
			// getSelectedClients()[0]

			Map<String, Object> productPropertiesFor1Client = persist.getProductproperties(getSelectedClients()[0],
					productEdited);

			if (productPropertiesFor1Client != null) {
				productProperties.add(productPropertiesFor1Client);

				Iterator iter = productPropertiesFor1Client.keySet().iterator();
				while (iter.hasNext()) {
					// get next key - value - pair
					String key = (String) iter.next();

					// productPropertiesFor1Client.get(key) " + productPropertiesFor1Client.get(key)
					// + " class: " + productPropertiesFor1Client.get(key).getClass() );

					List value = (List) productPropertiesFor1Client.get(key);

					// create a merger for it
					ListMerger merger = new ListMerger(value);
					mergedProductProperties.put(key, merger);
				}

				// merge the other clients
				for (int i = 1; i < getSelectedClients().length; i++) {
					productPropertiesFor1Client = persist.getProductproperties(getSelectedClients()[i], productEdited);

					productProperties.add(productPropertiesFor1Client);

					iter = productPropertiesFor1Client.keySet().iterator();

					while (iter.hasNext()) {
						String key = (String) iter.next();
						List value = (List) productPropertiesFor1Client.get(key);

						if (mergedProductProperties.get(key) == null)
						// we need a new property. it is not common
						{
							ListMerger merger = new ListMerger(value);

							merger.setHavingNoCommonValue();
							mergedProductProperties.put(key, merger);
						} else {
							Object merger = mergedProductProperties.get(key);

							ListMerger mergedValue = ((ListMerger) merger).merge(value);

							// + (mergedValue == ListMerger.NO_COMMON_VALUE));

							// on merging we check if the value is the same as before
							mergedProductProperties.put(key, mergedValue);
						}
					}
				}
			}
		}

	}

	protected void clearProductEditing() {
		mainFrame.panel_LocalbootProductsettings.clearEditing();
		mainFrame.panel_NetbootProductsettings.clearEditing();
	}

	protected void clearListEditors() {
		mainFrame.panel_LocalbootProductsettings.clearListEditors();
		mainFrame.panel_NetbootProductsettings.clearListEditors();
	}

	public void setProductEdited(String productname)
	// called from ProductSettings
	{
		logging.debug(this, "setProductEdited " + productname);

		productEdited = productname;

		if (clientProductpropertiesUpdateCollection != null) {
			try {
				updateCollection.remove(clientProductpropertiesUpdateCollection);
			} catch (Exception ex) {
			}
		}
		clientProductpropertiesUpdateCollection = null;

		if (clientProductpropertiesUpdateCollections.get(productEdited) == null)
		// have we got already a clientProductpropertiesUpdateCollection for this
		// product?
		// if not, we produce one
		{
			clientProductpropertiesUpdateCollection = new ProductpropertiesUpdateCollection(this, persist,
					getSelectedClients(), productEdited);

			clientProductpropertiesUpdateCollections.put(productEdited, clientProductpropertiesUpdateCollection);
			addToGlobalUpdateCollection(clientProductpropertiesUpdateCollection);

		} else {
			clientProductpropertiesUpdateCollection = (ProductpropertiesUpdateCollection) clientProductpropertiesUpdateCollections
					.get(productEdited);
		}

		collectTheProductProperties(productEdited);

		dependenciesModel.setActualProduct(productEdited);

		logging.debug(this, " --- mergedProductProperties " + mergedProductProperties);

		// " + persist.getProductPropertyValuesMap(productEdited));

		/*
		 * transcode product property descriptions
		 * Map productpropertyDescriptionsMap =
		 * persist.getProductPropertyDescriptionsMap(productEdited);
		 * if ( productpropertyDescriptionsMap != null &&
		 * !configed.get_serverCharset_equals_vm_charset() )
		 * {
		 * Iterator properties = productpropertyDescriptionsMap.keySet().iterator();
		 * while ( properties.hasNext())
		 * {
		 * String property = (String) properties.next();
		 * String val = (String) productpropertyDescriptionsMap.get (property) ;
		 * if (val != null && !val.equals(""))
		 * {
		 * productpropertyDescriptionsMap.put ( property,
		 * configed.encodeStringFromService( val ));
		 * }
		 * }
		 * }
		 */

		// ": " + persist.getProductPropertyOptionsMap(productEdited));

		logging.debug(this, "setProductEdited " + productname + " client specific properties "
				+ persist.hasClientSpecificProperties(productname));

		mainFrame.panel_LocalbootProductsettings.initEditing(productEdited, persist.getProductTitle(productEdited),
				persist.getProductInfo(productEdited), persist.getProductHint(productEdited),
				persist.getProductVersion(productEdited) + Globals.ProductPackageVersionSeparator.forDisplay()
						+ persist.getProductPackageVersion(productEdited) + "   "
						+ persist.getProductLockedInfo(productEdited),
				persist.getProductTimestamp(productEdited),

				persist.getProductHavingClientSpecificProperties(), // persist.hasClientSpecificProperties(productname),
				productProperties, // arraylist of the properties map of all selected clients
				mergedProductProperties, // these properties merged to one map

				// editmappanelx
				persist.getProductPropertyOptionsMap(productEdited),

				/*
				 * //editmappanel
				 * persist.getProductPropertyValuesMap(productEdited),
				 * persist.getProductPropertyDescriptionsMap(productEdited),
				 * persist.getProductPropertyDefaultsMap(productEdited),
				 */

				clientProductpropertiesUpdateCollection);

		mainFrame.panel_NetbootProductsettings.initEditing(productEdited, persist.getProductTitle(productEdited),
				persist.getProductInfo(productEdited), persist.getProductHint(productEdited),
				persist.getProductVersion(productEdited) + Globals.ProductPackageVersionSeparator.forDisplay()
						+ persist.getProductPackageVersion(productEdited) + "   "
						+ persist.getProductLockedInfo(productEdited),
				persist.getProductTimestamp(productEdited),

				persist.getProductHavingClientSpecificProperties(), // persist.hasClientSpecificProperties(productname),
				productProperties, // array of the properties map of all selected clients
				mergedProductProperties, // these properties merged to one map

				// editmappanelx
				persist.getProductPropertyOptionsMap(productEdited),

				/*
				 * //editmappanel
				 * persist.getProductPropertyValuesMap(productEdited),
				 * persist.getProductPropertyDescriptionsMap(productEdited),
				 * persist.getProductPropertyDefaultsMap(productEdited),
				 */

				clientProductpropertiesUpdateCollection);

	}

	public int getViewIndex() {
		return viewIndex;
	}

	public void treeClients_mouseAction(boolean clicked, MouseEvent mouseEvent) {
		logging.debug(this, "treeClients_mouseAction");

		if (!treeClients.isEnabled())
			return;

		if (mouseEvent.getButton() != MouseEvent.BUTTON1)
			return;

		int mouseRow = treeClients.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
		TreePath mousePath = treeClients.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

		DefaultMutableTreeNode mouseNode = null;

		if (mouseRow != -1) {

			// getting path " + mousePath);

			mouseNode = (DefaultMutableTreeNode) mousePath.getLastPathComponent();

			if (!mouseNode.getAllowsChildren()) {

				// we manage the active nodes

				if (clicked) {
					if ((activePaths.size() == 1)
							&& ((DefaultMutableTreeNode) activePaths.get(0).getLastPathComponent()).getAllowsChildren())
					// on client activation a group activation is ended
					{
						clearTree();
					}

					else {
						if ((mouseEvent.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) {

							clearTree();

							TreePath[] selTreePaths = treeClients.getSelectionPaths();

							for (int i = 0; i < selTreePaths.length; i++) {
								DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) selTreePaths[i]
										.getLastPathComponent();
								activeTreeNodes.put((String) selNode.getUserObject(), selTreePaths[i]);
								activePaths.add(selTreePaths[i]);
								treeClients.collectParentIDsFrom(selNode);
							}
						}

						else if ((mouseEvent.getModifiersEx()
								& MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {

						} else {

							clearTree();
						}
					}

					activateClientByTree((String) mouseNode.getUserObject(), mousePath);

					setRebuiltClientListTableModel(true, false, clientsFilteredByTree);

					logging.info(this,
							" treeClients_mouseAction getSelectedClients().length " + getSelectedClients().length);

					if (getSelectedClients().length == 1) {
						mainFrame.getHostsStatusInfo()
								.setGroupName(((DefaultMutableTreeNode) mouseNode).getParent().toString());
					} else {
						mainFrame.getHostsStatusInfo().setGroupName("");
					}

					mainFrame.getHostsStatusInfo().updateValues(clientCount, getSelectedClients().length,
							getSelectedClientsString(), clientInDepot);

					// restore keys, do not rebuild tree, select clientsFilteredByTree

					// treeClients.setSelectionPaths(activePaths.toArray(new TreePath[]{}));

				}

			} else {
				if (clicked) {
					activateGroupByTree(false, mouseNode, mousePath);

					// fails!!
				}
			}

		}

		if (mouseEvent.getClickCount() == 2) {
			logging.debug(this, "treeClients: double click on tree row " + mouseRow + " getting path " + mousePath);
			logging.debug(this, "treeClients: mouseNode instanceof GroupNode " + mouseNode + " "
					+ (mouseNode instanceof GroupNode));

			if (mouseNode instanceof GroupNode) {

				setGroup(mouseNode.toString());

			}
		}

	}

	public boolean treeClients_selectAction(TreePath newSelectedPath) {
		logging.info(this, "treeClients_selectAction");

		DefaultMutableTreeNode selectedNode = ((DefaultMutableTreeNode) newSelectedPath.getLastPathComponent());
		logging.info(this, "treeClients_selectAction selected node " + selectedNode);

		if (!selectedNode.getAllowsChildren()) {
			setClientByTree(selectedNode.toString(), newSelectedPath);
		}

		return true;
	}

	protected void initTree() {
		logging.debug(this, "initTree");
		activeTreeNodes = new HashMap<>();
		activePaths = new ArrayList<>();

		treeClients = new de.uib.configed.tree.ClientTree(this);
		persist.getHostInfoCollections().setTree(treeClients);

	}

	private void setClientByTree(String nodeObject, TreePath pathToNode) {
		// WaitCursor waitCursor = new WaitCursor(
		// Globals.mainContainer, "setClientByTree" );

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
		logging.info(this, "activateClientByTree, nodeObject: " + nodeObject + ", pathToNode: " + pathToNode);
		activeTreeNodes.put(nodeObject, pathToNode);
		activePaths.add(pathToNode);

		treeClients.collectParentIDsFrom((DefaultMutableTreeNode) pathToNode.getLastPathComponent());

		treeClients.repaint();

		logging.debug(this, "activateClientByTree, activeTreeNodes " + activeTreeNodes);
		clientsFilteredByTree = activeTreeNodes.keySet();
		logging.debug(this, "activateClientByTree, clientsFilteredByTree " + clientsFilteredByTree);

		// since we select based on the tree view we disable the filter
		if (filterClientList)
			mainFrame.toggleClientFilterAction();

	}

	public void clearTree() {

		activeTreeNodes.clear();
		activePaths.clear();
		treeClients.initActiveParents();
	}

	private void setGroupByTree(DefaultMutableTreeNode node, TreePath pathToNode) {
		logging.info(this, "setGroupByTree node, pathToNode " + node + ", " + pathToNode);
		clearTree();
		activeTreeNodes.put((String) node.getUserObject(), pathToNode);
		activePaths.add(pathToNode);

		clientsFilteredByTree = treeClients.collectLeafs(node);
		treeClients.repaint();

	}

	public void activateGroupByTree(boolean preferringOldSelection, DefaultMutableTreeNode node, TreePath pathToNode) {
		logging.info(this, "activateGroupByTree, node: " + node + ", pathToNode : " + pathToNode);

		setGroupByTree(node, pathToNode);

		if (preferringOldSelection // intended for reload, we cancel activating group
				&& selectionPanel.getSelectedSet() != null && !selectionPanel.getSelectedSet().isEmpty()) {
			return;
		}

		setRebuiltClientListTableModel(true, false, null);
		// with this, a selected client remains selected (but in bottom line, the group
		// seems activated, not the client)

		groupPathActivatedByTree = pathToNode;

		// mainFrame.setGroupName("" + node ); //+ " (" + pathToNode + ")");

		try {
			activatedGroupModel.setNode("" + node, node, pathToNode);
			activatedGroupModel.setDescription(treeClients.getGroups().get("" + node).get("description"));
			activatedGroupModel.setAssociatedClients(clientsFilteredByTree);
			activatedGroupModel.setActive(true);
		} catch (Exception ex) {
			logging.info(this, "activateGroupByTree, node: " + node + " exception : " + ex);
		}

	}

	public TreePath getGroupPathActivatedByTree() {
		return groupPathActivatedByTree;
	}

	public ActivatedGroupModel getActivatedGroupModel() {
		return activatedGroupModel;
	}

	public Set<String> getActiveParents() {
		if (treeClients == null)
			return new HashSet<>();

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

	public void treeClients_selectedValueChanged(TreeSelectionEvent e) {
		logging.debug(this, "treeClients_selectedValueChanged");
	}

	public boolean setProductGroup(String groupId, String description, Set<String> productSet) {
		return persist.setProductGroup(groupId, description, productSet);
	}

	private void depotsList_valueChanged() {
		if (firstDepotListChange) {
			firstDepotListChange = false;
			return;
		}

		logging.info(this, "----    depotsList selection changed");

		changeDepotSelection();

		

		/*
		 * if (getSelectedDepots().length == 0)
		 * // if nothing is selected set config server as depot
		 * {
		 * 
		 * 
		 * }
		 */

		if (depotsList.getModel().getSize() > 1) {
			if (mainFrame != null)
				mainFrame.setChangedDepotSelectionActive(true);

			// when running after the first run, we deactivate buttons
		}

		depotsList_selectionChanged = true;
		depotsOfSelectedClients = null;

		selectedDepots = depotsList.getSelectedValuesList().toArray(new String[0]);// new String[depotsList.getSelectedValues().length];
		selectedDepotsV = new Vector<>(depotsList.getSelectedValuesList());

		logging.debug(this, "--------------------  selectedDepotsV         " + selectedDepotsV);

		// configed.savedStates.setProperty("selectedDepots",

		configed.savedStates.saveDepotSelection.serialize(selectedDepots);

		/*
		 * logging.info(this, "--------------------saveSelectedDepotsV  " +
		 * saveSelectedDepotsV);
		 * boolean changedSelectedDepotsV =
		 * !selectedDepotsV.equals(saveSelectedDepotsV);
		 * logging.info(this, " selectedDepotsV not changed  " +
		 * !changedSelectedDepotsV);
		 * 
		 * if (!changedSelectedDepotsV)
		 * return;
		 */

		/*
		 * test of check of synchronicity
		 * TreeSet depotSet = new TreeSet<>();
		 * for (int i = 0; i < getSelectedDepots().length; i++)
		 * { depotSet.add(getSelectedDepots()[i] ); }
		 * persist.areDepotsSynchronous(depotSet);
		 */

		/*
		 * SwingUtilities.invokeLater(new Runnable(){
		 * 
		 * public void run()
		 * {
		 * initialTreeActivation();
		 * }
		 * }
		 * );;
		 */

		try {
			logging.info(this, " depotsList_valueChanged, omitted initialTreeActivation");

			if (selectionPanel != null)
				initialTreeActivation();

		} catch (Exception ex) {
			logging.error(this, "Tree cannot be activated", ex);
		}

	}

	private boolean checkSynchronous(java.util.Set depots) {
		// if (depots == null)

		if (depots.size() > 1 && !persist.areDepotsSynchronous(depots)) {
			JOptionPane.showMessageDialog(mainFrame, configed.getResourceValue("ConfigedMain.notSynchronous.text"), // "not
					// synchronous",
					configed.getResourceValue("ConfigedMain.notSynchronous.title"), JOptionPane.OK_OPTION);

			return false;
		}

		return true;
	}

	protected boolean setDepotRepresentative() {
		logging.debug(this, "setDepotRepresentative");

		boolean result = true;

		if (getSelectedClients() == null || getSelectedClients().length == 0) {
			if (depotRepresentative == null)
				depotRepresentative = myServer;
		} else {
			Set depots = getDepotsOfSelectedClients();
			logging.info(this, "depots of selected clients:" + depots);

			String oldRepresentative = depotRepresentative;

			logging.debug(this, "setDepotRepresentative(), old representative: " + depotRepresentative + " should be ");
			// + persist.getDepot() );

			if (!checkSynchronous(depots))
				result = false;

			else {
				logging.debug(this, "setDepotRepresentative  start  " + " up to now " + oldRepresentative + " old"
						+ depotRepresentative + " equal " + oldRepresentative.equals(depotRepresentative));

				depotRepresentative = null;

				logging.info(this, "setDepotRepresentative depotsOfSelectedClients " + getDepotsOfSelectedClients());

				Iterator depotsIterator = getDepotsOfSelectedClients().iterator();

				if (!depotsIterator.hasNext()) {
					depotRepresentative = myServer;

					logging.debug(this,
							"setDepotRepresentative  without next change depotRepresentative " + " up to now "
									+ oldRepresentative + " new " + depotRepresentative + " equal "
									+ oldRepresentative.equals(depotRepresentative));

				} else {

					depotRepresentative = (String) depotsIterator.next();

					while (!depotRepresentative.equals(myServer) && depotsIterator.hasNext()) {
						String depot = (String) depotsIterator.next();
						if (depot.equals(myServer)) {
							depotRepresentative = myServer;
						}
					}
				}

				logging.debug(this, " --------------- depotRepresentative " + depotRepresentative);

				logging.info(this,
						"setDepotRepresentative  change depotRepresentative " + " up to now " + oldRepresentative
								+ " new " + depotRepresentative + " equal "
								+ oldRepresentative.equals(depotRepresentative));

				if (!oldRepresentative.equals(depotRepresentative)) {
					logging.info(this, " new depotRepresentative " + depotRepresentative);
					persist.setDepot(depotRepresentative);

					// everything
					persist.depotChange();

				}

			}

		}

		return result;
	}

	protected ArrayList<String> getLocalbootProductDisplayFieldsList() {
		ArrayList<String> result = new ArrayList<>();
		Iterator iter = displayFieldsLocalbootProducts.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (displayFieldsLocalbootProducts.get(key))
				result.add(key);
		}

		return result;
	}

	protected ArrayList<String> getNetbootProductDisplayFieldsList() {
		ArrayList<String> result = new ArrayList<>();
		Iterator iter = displayFieldsNetbootProducts.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (displayFieldsNetbootProducts.get(key))
				result.add(key);
		}

		return result;
	}

	public Map<String, Boolean> getDisplayFieldsNetbootProducts() {
		return displayFieldsNetbootProducts;
	}

	public Map<String, Boolean> getDisplayFieldsLocalbootProducts() {
		return displayFieldsLocalbootProducts;
	}

	private boolean checkOneClientSelected()
	// enables and disables tabs depending if they make sense in other cases
	{
		logging.debug(this, "checkOneClientSelected() selectedClients " + logging.getStrings(getSelectedClients()));
		boolean result = true;

		if (getSelectedClients().length != 1) {
			/*
			 * JOptionPane.showMessageDialog(mainFrame,
			 * configed.getResourceValue("ConfigedMain.pleaseSelectOneClient.text"),
			 * configed.getResourceValue("ConfigedMain.pleaseSelectOneClient.title"),
			 * JOptionPane.OK_OPTION);
			 */
			result = false;
		}

		return result;
	}

	private void freeMemoryFromSearchData() {
		if (clientSelectionDialog != null) {

		}
	}

	protected boolean setLocalbootProductsPage() {
		logging.debug(this, "setLocalbootProductsPage() with filter "
				+ configed.savedStates.saveLocalbootproductFilter.deserialize());

		Set<String> savedFilter = (Set<String>) configed.savedStates.saveLocalbootproductFilter.deserialize();
		// , localboot display fields list " + getLocalbootProductDisplayFieldsList()

		// + " reload : " + (localbootStatesAndActions == null) );

		if (!setDepotRepresentative())
			return false;

		

		try {
			// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

			clearProductEditing();

			// null,

			if (localbootStatesAndActions == null || istmForSelectedClientsLocalboot == null
					|| localbootStatesAndActionsUPDATE)
			// we reload since at the moment we do not track changes if anyDataChanged
			{
				localbootStatesAndActionsUPDATE = false;

				// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

				localbootStatesAndActions = persist.getMapOfLocalbootProductStatesAndActions(getSelectedClients());

				istmForSelectedClientsLocalboot = null;
			}

			clientProductpropertiesUpdateCollections = new HashMap<>();
			mainFrame.panel_LocalbootProductsettings.initAllProperties();

			logging.debug(this, "setLocalbootProductsPage,  depotRepresentative:" + depotRepresentative);
			possibleActions = persist.getPossibleActions(depotRepresentative);

			// we retrieve the properties for all clients and products

			// it is necessary to do this before resetting selection below (*) since there a
			// listener is triggered
			// which loads the productProperties for each client separately

			persist.retrieveProductproperties(selectionPanel.getSelectedValues());

			Set<String> oldProductSelection = mainFrame.panel_LocalbootProductsettings.getSelectedIDs();
			List<? extends RowSorter.SortKey> currentSortKeysLocalbootProducts = mainFrame.panel_LocalbootProductsettings
					.getSortKeys();

			logging.info(this, "setLocalbootProductsPage: oldProductSelection " + oldProductSelection);

			logging.debug(this,
					"setLocalbootProductsPage: collectChangedLocalbootStates " + collectChangedLocalbootStates);

			if (istmForSelectedClientsLocalboot == null) {
				// we rebuild only if we reloaded
				istmForSelectedClientsLocalboot = new InstallationStateTableModelFiltered(getSelectedClients(), this,
						collectChangedLocalbootStates, persist.getAllLocalbootProductNames(depotRepresentative),
						localbootStatesAndActions, possibleActions, // persist.getPossibleActions(depotRepresentative),
						persist.getProductGlobalInfos(depotRepresentative),
						// new ArrayList<>(productDisplayFields.keySet())
						getLocalbootProductDisplayFieldsList(), configed.savedStates.saveLocalbootproductFilter

				);
			}

			try {
				mainFrame.panel_LocalbootProductsettings
						.setTableModel((InstallationStateTableModelFiltered) istmForSelectedClientsLocalboot);
				mainFrame.panel_LocalbootProductsettings.setSortKeys(currentSortKeysLocalbootProducts);

				mainFrame.panel_LocalbootProductsettings.setGroupsData(productGroups, productGroupMembers);

				logging.info(this, "resetFilter " + configed.savedStates.saveLocalbootproductFilter.deserialize());
				// ( (InstallationStateTableModelFiltered) istmForSelectedClientsLocalboot

				((PanelGroupedProductSettings) mainFrame.panel_LocalbootProductsettings).reduceToSet(savedFilter);

				logging.info(this, "setLocalbootProductsPage oldProductSelection -----------  " + oldProductSelection);
				mainFrame.panel_LocalbootProductsettings.setSelection(oldProductSelection); // (*)

				mainFrame.panel_LocalbootProductsettings
						.setSearchFields(de.uib.configed.guidata.InstallationStateTableModel
								.localizeColumns(getLocalbootProductDisplayFieldsList()));

			} catch (Exception ex) {
				logging.warning("setLocalbootInstallationStateTableModel, exception occurred: " + ex.getMessage(), ex);
			}

			return true;
		} catch (Exception ex) {
			// if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setLocalbootProductsPage: " + ex, ex);
			return false;
		}

	}

	protected boolean setNetbootProductsPage() {

		Set<String> savedFilter = (Set<String>) configed.savedStates.saveNetbootproductFilter.deserialize();

		if (!setDepotRepresentative())
			return false;

		freeMemoryFromSearchData();

		

		try {
			// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

			clearProductEditing();

			long startmillis = System.currentTimeMillis();
			logging.debug(this,
					"setLocalbootProductsPage, # getMapOfNetbootProductStatesAndActions(selectedClients)  start "
							+ startmillis);

			if (netbootStatesAndActions == null)
			// we reload since at the moment we do not track changes if anyDataChanged
			{
				// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

				netbootStatesAndActions = persist.getMapOfNetbootProductStatesAndActions(getSelectedClients());

				istmForSelectedClientsNetboot = null;
			}
			long endmillis = System.currentTimeMillis();
			logging.debug(this,
					"setNetbootProductsPage, # getMapOfNetbootProductStatesAndActions(selectedClients)  end "
							+ endmillis + " diff " + (endmillis - startmillis));

			List<? extends RowSorter.SortKey> currentSortKeysNetbootProducts = mainFrame.panel_NetbootProductsettings
					.getSortKeys();

			clientProductpropertiesUpdateCollections = new HashMap<>();
			mainFrame.panel_LocalbootProductsettings.initAllProperties();

			possibleActions = persist.getPossibleActions(depotRepresentative);

			Set<String> oldProductSelection = mainFrame.panel_NetbootProductsettings.getSelectedIDs();

			// we retrieve the properties for all clients and products

			persist.retrieveProductproperties(selectionPanel.getSelectedValues());

			if (istmForSelectedClientsNetboot == null) {
				// we rebuild only if we reloaded
				istmForSelectedClientsNetboot = new InstallationStateTableModelFiltered(// Netbootproducts(
						getSelectedClients(), this, collectChangedNetbootStates,
						persist.getAllNetbootProductNames(depotRepresentative), netbootStatesAndActions,
						possibleActions, // persist.getPossibleActions(depotRepresentative),
						persist.getProductGlobalInfos(depotRepresentative), getNetbootProductDisplayFieldsList(),
						configed.savedStates.saveNetbootproductFilter);
			}

			try {
				mainFrame.panel_NetbootProductsettings.setTableModel(istmForSelectedClientsNetboot);

				mainFrame.panel_NetbootProductsettings.setSortKeys(currentSortKeysNetbootProducts);

				mainFrame.panel_NetbootProductsettings.setGroupsData(productGroups, productGroupMembers);

				logging.info(this, "resetFilter " + configed.savedStates.saveLocalbootproductFilter.deserialize());
				// ( (InstallationStateTableModelFiltered) istmForSelectedClientsNetboot

				((PanelGroupedProductSettings) mainFrame.panel_NetbootProductsettings).reduceToSet(savedFilter);

				mainFrame.panel_NetbootProductsettings.setSelection(oldProductSelection); // (*)

			} catch (Exception ex) {
				logging.error(" setNetbootInstallationStateTableModel,  exception Occurred", ex);
			}

			return true;
		} catch (Exception ex) {
			// if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setNetbootProductsPage: " + ex, ex);
			return false;
		}

	}

	private Map<String, Object> mergeMaps(ArrayList<Map<String, Object>> collection) {
		HashMap<String, Object> mergedMap = new HashMap<>();
		if (collection == null || collection.isEmpty())
			return mergedMap;

		Map<String, Object> mergeIn = collection.get(0);

		for (String key : mergeIn.keySet()) {
			List value = (List) mergeIn.get(key);
			ListMerger merger = new ListMerger(value);
			mergedMap.put(key, merger);
		}

		// merge the other maps
		for (int i = 1; i < collection.size(); i++) {
			mergeIn = collection.get(i);

			for (String key : mergeIn.keySet()) {
				List value = (List) mergeIn.get(key);

				if (mergedMap.get(key) == null)
				// new property
				{
					ListMerger merger = new ListMerger(value);
					merger.setHavingNoCommonValue();
					mergedMap.put(key, merger);
				}

				else {
					ListMerger merger = (ListMerger) mergedMap.get(key);
					ListMerger mergedValue = merger.merge(value);
					mergedMap.put(key, mergedValue);
				}
			}
		}

		return mergedMap;
	}

	protected boolean setProductPropertiesPage() {
		logging.debug(this, "setProductPropertiesPage");

		if (editingTarget == EditingTarget.DEPOTS
		// ||
		// editingTarget == EditingTarget.SERVER
		)

		{
			int saveSelectedRow = mainFrame.panel_ProductProperties.paneProducts.getSelectedRow();
			mainFrame.panel_ProductProperties.paneProducts.reset();

			/*
			 * if (
			 * mainFrame.panel_ProductProperties.paneProducts.getTableModel().getRowCount()>
			 * 0
			 * && mainFrame.panel_ProductProperties.paneProducts.getSelectedRowCount() == 0
			 * )
			 * {
			 * mainFrame.panel_ProductProperties.paneProducts.setSelectedRow(0);
			 * }
			 */

			/*
			 * missing product
			 * if (depotProductpropertiesUpdateCollection != null)
			 * {
			 * try {updateCollection.remove (depotProductpropertiesUpdateCollection);}
			 * catch (Exception ex) {}
			 * }
			 * depotProductpropertiesUpdateCollection = new
			 * ProductpropertiesUpdateCollection(this, persist, objectIds, product);
			 * updateCollection.add(depotProductpropertiesUpdateCollection);
			 * 
			 */

			if (mainFrame.panel_ProductProperties.paneProducts.getTableModel().getRowCount() > 0) {
				if (saveSelectedRow == -1 || mainFrame.panel_ProductProperties.paneProducts.getTableModel()
						.getRowCount() <= saveSelectedRow) {
					mainFrame.panel_ProductProperties.paneProducts.setSelectedRow(0);
				} else {
					mainFrame.panel_ProductProperties.paneProducts.setSelectedRow(saveSelectedRow);
				}
			}

			// mainFrame.panel_ProductProperties.refresh();
			return true;
		} else
			return false;
	}

	protected boolean setHostPropertiesPage() {
		logging.debug(this, "setHostPropertiesPage");
		

		try {
			if (editingTarget == EditingTarget.DEPOTS) {
				// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

				// Map<String, List<Object>> defaultValuesMap =

				LinkedHashMap<String, Map<String, Object>> depotPropertiesForPermittedDepots = persist
						.getDepotPropertiesForPermittedDepots();

				if (hostUpdateCollection != null) {
					try {
						updateCollection.remove(hostUpdateCollection);
					} catch (Exception ex) {
					}
				}
				hostUpdateCollection = new HostUpdateCollection(persist);
				addToGlobalUpdateCollection(hostUpdateCollection);

				mainFrame.panel_HostProperties.initMultipleHostsEditing(
						configed.getResourceValue("PanelHostProperties.SelectHost"),
						new DefaultComboBoxModel<>(new Vector<>(depotPropertiesForPermittedDepots.keySet())),
						depotPropertiesForPermittedDepots, hostUpdateCollection,
						PersistenceController.KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT

				);

			}

			return true;
		} catch (Exception ex) {
			// if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setHostPropertiesPage: " + ex, ex);
			return false;
		}

	}

	private void removeKeysStartingWith(Map<String, ? extends Object> m, Set<String> keystartersStrNotWanted) {
		Set<String> keysForDeleting = new HashSet<>();

		for (String start : keystartersStrNotWanted) {
			for (String key : m.keySet()) {
				if (key.startsWith(start))
					keysForDeleting.add(key);
			}
		}

		for (String key : keysForDeleting) {
			m.remove(key);
		}
	}

	public boolean setNetworkconfigurationPage() {
		logging.info(this, "setNetworkconfigurationPage ");// + Arrays.asList(saveSelectedClients));
		logging.info(this,
				"setNetworkconfigurationPage  getSelectedClients() " + Arrays.toString(getSelectedClients()));
		

		try {
			if (editingTarget == EditingTarget.SERVER)
				objectIds = new String[] { myServer };
			else
				objectIds = getSelectedClients();

			/*
			 * if (getSelectedClients().length == 0)
			 * objectIds = new String[] {myServer};
			 * else
			 * objectIds = getSelectedClients();
			 */

			if (additionalconfigurationUpdateCollection != null) {
				try {
					updateCollection.remove(additionalconfigurationUpdateCollection);
				} catch (Exception ex) {
				}
			}
			additionalconfigurationUpdateCollection = new AdditionalconfigurationUpdateCollection(persist, objectIds);
			addToGlobalUpdateCollection(additionalconfigurationUpdateCollection);

			if (editingTarget == EditingTarget.SERVER) {
				// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

				ArrayList additionalConfigs = new ArrayList<>(1);

				Map<String, List<Object>> defaultValuesMap = persist.getConfigDefaultValues();

				additionalConfigs.add(defaultValuesMap);

				additionalconfigurationUpdateCollection.setMasterConfig(true);

				mainFrame.panel_HostConfig.initEditing(
						// configed.getResourceValue("MainFrame.jLabel_AdditionalConfig") + ":"
						"  " + myServer + " (configuration server)", (Map) additionalConfigs.get(0),
						persist.getConfigOptions(), additionalConfigs, additionalconfigurationUpdateCollection, true,
						// editableOptions
						PersistenceController.PROPERTYCLASSES_SERVER);

			}

			else {

				// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

				ArrayList<Map<String, Object>> additionalConfigs = new ArrayList<>(getSelectedClients().length);

				if (hostConfigs == null) {
					hostConfigs = new HashMap<>(); // serves as marker

					for (String client : getSelectedClients()) {
						hostConfigs.put(client, persist.getConfigs().get(client));
					}
				}

				logging.info(this, "additionalConfig fetch for " + Arrays.toString(getSelectedClients()));

				for (int i = 0; i < getSelectedClients().length; i++) {
					additionalConfigs.add(persist.getConfig(getSelectedClients()[i]));

					// persist.getConfig(getSelectedClients()[i]));
					// with server defaults
				}

				Map<String, Object> mergedVisualMap = mergeMaps(additionalConfigs);

				Map<String, de.uib.utilities.table.ListCellOptions> configOptions = persist.getConfigOptions();

				// removeKeysStartingWith(configOptions, "user");
				// removeKeysStartingWith(mergedVisualMap, "user");
				// removeKeysStartingWith(mergedVisualMap, "configed");
				removeKeysStartingWith(mergedVisualMap, persist.CONFIG_KEYSTARTERS_NOT_FOR_CLIENTS);

				mainFrame.panel_HostConfig.initEditing("  " + getSelectedClientsString(), // "",
						mergedVisualMap, configOptions, additionalConfigs, additionalconfigurationUpdateCollection,
						false, // editableOptions
						persist.PROPERTYCLASSES_CLIENT);

				/*
				 * if (!checkOneClientSelected())
				 * {
				 * viewIndex = viewClients;
				 * }
				 * else
				 * {
				 * waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),
				 * mainFrame.getCursor() );
				 * 
				 * 
				 * ArrayList additionalConfigs = new ArrayList (1);
				 * additionalConfigs.add
				 * (persist.getConfig(selectedClients[0]));//persist.getAdditionalConfiguration(
				 * selectedClients[0]));
				 * 
				 * mainFrame.initNetworkconfigEditing (
				 * selectedClients[0],
				 * (Map) additionalConfigs.get(0),
				 * persist.getConfigOptions(),
				 * additionalConfigs,
				 * additionalconfigurationUpdateCollection);
				 * 
				 * waitCursor.stop();
				 * }
				 */

			}

			return true;
		} catch (Exception ex) {
			// if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setNetworkConfigurationPage: " + ex, ex);
			return false;
		}

	}

	protected void checkHwInfo() {
		if (hwInfoClientmap == null)
			hwInfoClientmap = new HashMap<>();
	}

	public void clearHwInfo() {
		checkHwInfo();
		hwInfoClientmap.clear();
	}

	protected boolean setHardwareInfoPage() {
		logging.info(this, "setHardwareInfoPage for, clients count " + getSelectedClients().length);

		

		try {
			// waitCursor = new WaitCursor(mainFrame.retrieveBasePane(),

			if (firstSelectedClient == null || getSelectedClients().length == 0) {
				mainFrame.setHardwareInfoNotPossible(configed.getResourceValue("MainFrame.noClientSelected1"), null);
			} else if (getSelectedClients().length > 1) {
				if (!PersistenceControllerFactory.sqlAndGetRows) {
					mainFrame.setHardwareInfoNotPossible(configed.getResourceValue("MainFrame.backendSQLrequired1"),
							configed.getResourceValue("MainFrame.backendSQLrequired2"));
				} else {
					mainFrame.setHardwareInfoMultiClients(getSelectedClients());
				}
			} else {
				checkHwInfo();
				Object hwInfo = hwInfoClientmap.get(firstSelectedClient);
				if (hwInfo == null) {
					hwInfo = persist.getHardwareInfo(firstSelectedClient, true);
					hwInfoClientmap.put(firstSelectedClient, hwInfo);
				}
				mainFrame.setHardwareInfo(hwInfo);
			}

			return true;
		} catch (Exception ex) {
			// if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setHardwareInfoPage: " + ex, ex);
			return false;
		}
	}

	protected void clearSoftwareInfoPage() {
		mainFrame.setSoftwareAuditNullInfo("");
	}

	protected void checkSwInfo() {
		/*
		 * if (swInfoClientmap == null)
		 * swInfoClientmap = new HashMap<>();
		 */
	}

	public void clearSwInfo() {
		/*
		 * checkSwInfo();
		 * swInfoClientmap.clear();
		 */
	}

	protected boolean setSoftwareInfoPage() {
		logging.info(this, "setSoftwareInfoPage() firstSelectedClient, checkOneClientSelected " + firstSelectedClient
				+ ", " + checkOneClientSelected());
		

		try {
			// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

			if (firstSelectedClient == null || !checkOneClientSelected())
				mainFrame.setSoftwareAudit();
			else {
				persist.getSoftwareAudit(firstSelectedClient); // retrieve data and check with softwaretable
				mainFrame.setSoftwareAudit(firstSelectedClient);
			}

			return true;
		} catch (Exception ex) {
			// if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setSoftwareInfoPage: " + ex, ex);
			return false;
		}
	}

	protected void clearLogPage() {
		mainFrame.setLogfilePanel(new HashMap<>());
	}

	public boolean updateLogPage(String logtype) {
		

		try {
			logging.debug(this, "updatelogpage");

			if (!checkOneClientSelected())
				return false;

			// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

			persist.getLogfiles(firstSelectedClient, logtype);

		} catch (Exception ex) {
			// if (waitCursor != null) waitCursor.stop();
			logging.error("Error in updateLogPage: " + ex, ex);

			return false;
		}
		return true;
	}

	public boolean logfileExists(String logtype) {
		if (logfiles == null || logfiles.get(logtype) == null || logfiles.get(logtype).equals("")
				|| logfiles.get(logtype).equals(configed.getResourceValue("MainFrame.TabActiveForSingleClient")))
			return false;

		return true;
	}

	public Map<String, String> getLogfilesUpdating(String logtypeToUpdate) {
		logging.info(this, "getLogfilesUpdating " + logtypeToUpdate);

		if (!checkOneClientSelected()) {
			for (int i = 0; i < persist.getLogtypes().length; i++) {
				logfiles.put(persist.getLogtypes()[i], configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
			}

			mainFrame.setLogfilePanel(logfiles);
		} else {

			try {
				// waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

				WaitCursor waitCursor = new WaitCursor(Globals.mainContainer, "getLogfilesUpdating");
				logfiles = persist.getLogfiles(firstSelectedClient, logtypeToUpdate);
				waitCursor.stop();

				logging.debug(this, "log pages set");
			} catch (Exception ex) {
				// if (waitCursor != null) waitCursor.stop();
				logging.error("Error in setLogPage: " + ex, ex);
			}
		}

		return logfiles;
	}

	protected boolean setLogPage() {
		

		logging.debug(this, "setLogPage(), selected clients: " + logging.getStrings(getSelectedClients()));

		try {
			/*
			 * if (!checkOneClientSelected())
			 * {
			 * viewIndex = viewClients;
			 * }
			 * else
			 */
			{
				logfiles = persist.getEmptyLogfiles();
				mainFrame.setUpdatedLogfilePanel("instlog");
				mainFrame.setLogview("instlog");
			}

			return true;

		} catch (Exception ex) {
			// if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setLogPage: " + ex, ex);
			return false;
		}
	}

	protected boolean setView(int viewIndex)
	// extra tasks not done by resetView
	{
		if (viewIndex == VIEW_CLIENTS) {
			checkErrorList();

			depotsList.setEnabled(true);
		}

		return true;
	}

	/*
	 * public boolean checkedResetView(int viewIndex)
	 * {
	 * checkSaveAll(true);
	 * return resetView(viewIndex);6
	 * }
	 */

	public boolean resetView(int viewIndex) {

		logging.info(this, "resetView to " + viewIndex + "  getSelectedClients " + getSelectedClients().length);

		boolean result = true;

		switch (viewIndex) {
		case VIEW_CLIENTS: {
			break;
		}

		case VIEW_LOCALBOOT_PRODUCTS: {
			result = setLocalbootProductsPage();
			break;
		}

		case VIEW_NETBOOT_PRODUCTS: {
			result = setNetbootProductsPage();
			break;
		}

		case VIEW_NETWORK_CONFIGURATION: {
			result = setNetworkconfigurationPage();
			break;
		}

		case VIEW_HARDWARE_INFO: {
			result = setHardwareInfoPage();
			break;
		}

		case VIEW_SOFTWARE_INFO: {
			result = setSoftwareInfoPage();
			break;
		}

		case VIEW_LOG: {
			result = setLogPage();
			break;
		}

		case VIEW_PRODUCT_PROPERTIES: {
			result = setProductPropertiesPage();

			break;
		}

		case VIEW_HOST_PROPERTIES: {
			result = setHostPropertiesPage();
			break;
		}

		}

		return result;

	}

	public void setVisualViewIndex(int i) {
		mainFrame.setVisualViewIndex(i);
	}

	public void setViewIndex(int visualViewIndex) {
		int oldViewIndex = viewIndex;

		logging.info(this, " visualViewIndex " + visualViewIndex + ", (old) viewIndex " + viewIndex);

		/*
		 * if (visualViewIndex != viewClients)
		 * {
		 * if( clientSelectionDialog != null )
		 * clientSelectionDialog.setEnabled(false);
		 * }
		 * else
		 * {
		 * if( clientSelectionDialog != null )
		 * clientSelectionDialog.setEnabled(true);
		 * }
		 */

		boolean problem = false;

		dependenciesModel.setActualProduct("");

		// if we are leaving some tab we check first if we possibly have to save
		// something

		logging.info(this, "setViewIndex anyDataChanged " + anyDataChanged);

		if (anyDataChanged && (viewIndex == VIEW_LOCALBOOT_PRODUCTS || viewIndex == VIEW_NETBOOT_PRODUCTS)) {
			if (depotsList_selectionChanged) {
				requestReloadStatesAndActions();
			} else {
				requestReloadStatesAndActions(true);
			}
		}

		saveIfIndicated();

		// we will only leave view 0 if a PC is selected

		// check if change of view index to the value of visualViewIndex can be allowed
		if (visualViewIndex != VIEW_CLIENTS) {

			if (!((visualViewIndex == VIEW_CLIENTS)
					|| ((visualViewIndex == VIEW_NETWORK_CONFIGURATION) && (editingTarget == EditingTarget.SERVER))
					|| ((visualViewIndex == VIEW_HOST_PROPERTIES) && (editingTarget == EditingTarget.DEPOTS)))) {
				logging.debug(this, " selected clients " + logging.getStrings(getSelectedClients()));

				if (getSelectedClients() == null)
				// should not occur
				{
					logging.debug(this, " getSelectedClients()  null");

					problem = true;
					JOptionPane.showMessageDialog(mainFrame,
							configed.getResourceValue("ConfigedMain.pleaseSelectPc.text"),
							configed.getResourceValue("ConfigedMain.pleaseSelectPc.title"), JOptionPane.OK_OPTION);
					viewIndex = VIEW_CLIENTS;
				}

				/*
				 * else
				 * {
				 * if (!setDepotRepresentative())
				 * {
				 * //handled in setLocalbootProductsPage/setNetbootProductsPage
				 * problem = true;
				 * viewIndex = viewClients;
				 * }
				 * }
				 */
			}

		}

		if (!problem && dataReady) // we have loaded the data
		{
			viewIndex = visualViewIndex;

			if (viewIndex != VIEW_CLIENTS) {

				depotsList.setEnabled(false);

			}

			/*
			 * if (viewIndex != 1)
			 * {
			 * mainFrame.saveConfigurationsSetEnabled(false);
			 * }
			 */

			logging.debug(this, "switch to viewIndex " + viewIndex);

			boolean result = true;

			setView(viewIndex);
			// tasks only needed when primarily called
			result = resetView(viewIndex);
			// task needed also when recalled

			if (!result) {
				viewIndex = oldViewIndex;
				logging.debug(" tab index could not be changed");
			}

			if (viewIndex == VIEW_CLIENTS) {
				if (reachableUpdater.isInterrupted())
					reachableUpdater.interrupt();

				mainFrame.enableMenuItemsForClients(getSelectedClients().length);
			}

			else
				mainFrame.enableMenuItemsForClients(-1);

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

			if (result)
				clearListEditors();
			// dont keep product editing across views
		}
	}

	protected void updateProductStates() {
		// localboot products
		logging.info(this, "updateProductStates: collectChangedLocalbootStates  " + collectChangedLocalbootStates);

		if (collectChangedLocalbootStates != null && collectChangedLocalbootStates.keySet() != null
				&& !collectChangedLocalbootStates.keySet().isEmpty()) {

			Iterator it0 = collectChangedLocalbootStates.keySet().iterator();

			while (it0.hasNext()) {
				String client = (String) it0.next();
				Map<String, Map<String, String>> clientValues = (Map<String, Map<String, String>>) collectChangedLocalbootStates
						.get(client);

				logging.debug(this, "updateProductStates, collectChangedLocalbootStates , client " + client + " values "
						+ clientValues);

				if (clientValues.keySet() == null || clientValues.keySet().isEmpty())
					continue;

				Iterator it1 = clientValues.keySet().iterator();
				while (it1.hasNext()) {
					String product = (String) it1.next();

					// client " + client + " product " + product);
					Map<String, String> productValues = (Map<String, String>) clientValues.get(product);

					persist.updateProductOnClient(client, product, OpsiPackage.TYPE_LOCALBOOT, productValues);
				}
			}

			persist.updateProductOnClients(); // send the collected items

		}

		// netboot products
		logging.debug(this, "collectChangedNetbootStates  " + collectChangedNetbootStates);
		if (collectChangedNetbootStates != null && collectChangedNetbootStates.keySet() != null
				&& !collectChangedNetbootStates.keySet().isEmpty()) {
			Iterator it0 = collectChangedNetbootStates.keySet().iterator();

			while (it0.hasNext()) {
				String client = (String) it0.next();
				Map<String, Map<String, String>> clientValues = (Map<String, Map<String, String>>) collectChangedNetbootStates
						.get(client);

				if (clientValues.keySet() == null || clientValues.keySet().isEmpty())
					continue;

				Iterator it1 = clientValues.keySet().iterator();
				while (it1.hasNext()) {
					String product = (String) it1.next();
					Map productValues = (Map) clientValues.get(product);

					persist.updateProductOnClient(client, product, OpsiPackage.TYPE_NETBOOT, productValues);
				}
			}

			persist.updateProductOnClients(); // send the collected items

		}

		if (istmForSelectedClientsNetboot != null)
			istmForSelectedClientsNetboot.clearCollectChangedStates();

		if (istmForSelectedClientsLocalboot != null)
			istmForSelectedClientsLocalboot.clearCollectChangedStates();
	}

	public void initServer() {
		checkSaveAll(true);
		preSaveSelectedClients = saveSelectedClients;
		logging.debug(this, "initServer() preSaveSelectedClients " + preSaveSelectedClients);
		setSelectedClients((List) null);
		logging.debug(this, "set selected values in initServer()");
		// setSelectedClientsOnPanel(new String[0]);

	}

	public String[] getSelectedDepots() {
		return selectedDepots;
	}

	public Vector<String> getSelectedDepotsV() {
		return selectedDepotsV;
	}

	public Set<String> getAllowedClients() {
		return allowedClients;
	}

	public List<String> getAccessedDepots() {
		ArrayList<String> accessedDepots = new ArrayList<>();
		for (String depot : selectedDepotsV) {
			if (persist.getDepotPermission(depot))
				accessedDepots.add(depot);
		}

		return accessedDepots;
	}

	public List<String> getProductNames() {
		return localbootProductnames;
	}

	public List<String> getAllProductNames() {
		List<String> productnames = new ArrayList<>(localbootProductnames);
		productnames.addAll(netbootProductnames);

		logging.info(this, "productnames " + productnames);

		return productnames;
	}

	protected String[] getDepotArray() {
		if (depots == null)
			return new String[] {};

		return new ArrayList<>(depots.keySet()).toArray(new String[] {});
	}

	protected void fetchDepots() {
		logging.info(this, "fetchDepots");

		// for testing activated in 4.0.6.0.9
		if (depotsList.getListSelectionListeners().length > 0)
			depotsList.removeListSelectionListener(depotsListSelectionListener);

		depotsList.getSelectionModel().setValueIsAdjusting(true);

		String[] depotsList_selectedValues = getSelectedDepots();

		depots = persist.getHostInfoCollections().getDepots();

		depotNamesLinked = persist.getHostInfoCollections().getDepotNamesList();

		/*
		 * 
		 * new LinkedList<>();
		 * depotNamesLinked.add(myServer);
		 * 
		 * Map<String, Object> configServerRecord = new HashMap<>();
		 * configServerRecord.put("id", myServer);
		 * configServerRecord.put("description", "opsi config server");
		 * 
		 * 
		 * if (depots == null || depots.size() < 1)
		 * {
		 * logging.warning(" depots list empty, selecting main configuration server" );
		 * depots = new HashMap<>();
		 * depots.put(myServer, configServerRecord);
		 * }
		 * else
		 * {
		 * depots.put(myServer, configServerRecord);
		 * logging.debug(this, "fetchDepots depots.keySet() " +depots.keySet());
		 * TreeSet<String> depotIdsSorted = new TreeSet<>( depots.keySet() );
		 * depotIdsSorted.remove(myServer);
		 * for (String depotId : depotIdsSorted)
		 * {
		 * depotNamesLinked.add(depotId);
		 * }
		 * }
		 */

		logging.debug(this, "fetchDepots sorted depots " + depotNamesLinked);

		if (depots.size() == 1)
			multiDepot = false;
		else
			multiDepot = true;

		logging.debug(this, "we have multidepot " + multiDepot);

		depotsList.setListData(getLinkedDepots());

		logging.debug(this, " ----------  selected after fetch " + getSelectedDepots().length);

		boolean[] depotsList_isSelected = new boolean[depotsList.getModel().getSize()];

		for (int j = 0; j < depotsList_selectedValues.length; j++) {
			// collect all indices where the value had been selected
			depotsList.setSelectedValue(depotsList_selectedValues[j], false);
			if (depotsList.getSelectedIndex() > -1)
				depotsList_isSelected[depotsList.getSelectedIndex()] = true;
		}

		for (int i = 0; i < depotsList_isSelected.length; i++) {
			// combine the selections to a new selection
			if (depotsList_isSelected[i])
				depotsList.addSelectionInterval(i, i);
		}

		depotsList_selectedIndices_lastFetched = depotsList.getSelectedIndices();

		if (mainFrame != null)
			mainFrame.setChangedDepotSelectionActive(false);

		depotsList.getSelectionModel().setValueIsAdjusting(false);
		depotsList_selectionChanged = false;
		depotsList.addListSelectionListener(depotsListSelectionListener);

	}

	public Vector<String> getLinkedDepots() {
		return new Vector<>(depotNamesLinked);
	}

	public String getConfigserver() {
		return myServer;
	}

	public void reloadLicensesData() {
		logging.info(this, " reloadLicensesData _______________________________ ");
		if (dataReady) {

			persist.licencesUsageRequestRefresh();
			persist.relations_auditSoftwareToLicencePools_requestRefresh();
			persist.reconciliationInfoRequestRefresh();

			// WaitCursor waitCursor = new WaitCursor( mainFrame.retrieveBasePane(),

			// persist.getRelationsWindowsSoftwareId2LPool();

			Iterator iter = allControlMultiTablePanels.iterator();
			while (iter.hasNext()) {
				ControlMultiTablePanel cmtp = (ControlMultiTablePanel) iter.next();
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
		if (dataReady)
		// dont do anything if we did not finish another thread for this
		{

			String oldGroupSelection = activatedGroupModel.getGroupName();
			logging.info(this, " ==== refreshClientListKeepingGroup oldGroupSelection " + oldGroupSelection);

			refreshClientList();
			activateGroup(oldGroupSelection);

			freeMemoryFromSearchData(); // we have to observe the changed client selection

		}

	}

	private void changeDepotSelection() {
		logging.info(this, "changeDepotSelection");
		if (mainFrame != null) {
			// by starting a thread the visual marker of changing in progress works
			SwingUtilities.invokeLater(new Thread() {
				@Override
				public void run() {
					mainFrame.setChangedDepotSelectionActive(true);
					refreshClientListKeepingGroup();
					mainFrame.setChangedDepotSelectionActive(false);
				}
			});
		} else
			refreshClientListKeepingGroup();

		depotsList_selectionChanged = false;
	}

	public void reload() {
		if (mainFrame != null) {
			mainFrame.setChangedDepotSelectionActive(false);
			SwingUtilities.invokeLater(new Thread() {
				@Override
				public void run() {
					reloadData();
				}
			});
		} else
			reloadData();
	}

	private void reloadData() {
		checkSaveAll(true);
		int saveViewIndex = getViewIndex();

		logging.info(this, " reloadData _______________________________  saveViewIndex " + saveViewIndex);

		WaitCursor.stopAll(); // stop all old waiting threads if there should be any left
		// WaitCursor waitCursor = new WaitCursor(mainFrame.retrieveBasePane(),

		ArrayList<String> selValuesList = selectionPanel.getSelectedValues();

		logging.info(this, "reloadData, selValuesList.size " + selValuesList.size());

		String[] savedSelectedValues = selValuesList.toArray(new String[selValuesList.size()]);

		if (selectionPanel != null)
			selectionPanel.removeListSelectionListener(this); // deactivate temporarily listening to list selection
																// events

		

		if (dataReady)
		// dont do anything if we did not finish another thread for this
		{
			/*
			 * if( clientSelectionDialog != null)
			 * {
			 * clientSelectionDialog.leave();
			 * clientSelectionDialog = null;
			 * }
			 */

			allowedClients = null;

			de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText.reset();

			persist.requestReloadOpsiDefaultDomain();
			persist.userConfigurationRequestReload();
			persist.checkConfiguration();

			// mainFrame.panel_ProductProperties.paneProducts.requestReload();

			persist.opsiInformationRequestRefresh();
			persist.hwAuditConfRequestRefresh();
			persist.client2HwRowsRequestRefresh();

			logging.info(this, "call installedSoftwareInformationRequestRefresh()");
			persist.installedSoftwareInformationRequestRefresh();
			persist.softwareAuditOnClientsRequestRefresh();

			persist.productDataRequestRefresh();

			
			// if (saveViewIndex == viewProductProperties)

			logging.info(this, "reloadData _1");
			// mainFrame.panel_ProductProperties.paneProducts.requestReload();
			// calls again persist.productDataRequestRefresh()
			mainFrame.panel_ProductProperties.paneProducts.reload();
			logging.info(this, "reloadData _2");

			// if variable modelDataValid in GenTableModel has no function , the following
			// statement is sufficient:

			// mainFrame.panel_ProductProperties.paneProducts.reset();

			// only for licenses, will be handled in another method
			// persist.relations_auditSoftwareToLicencePools_requestRefresh();

			persist.configOptionsRequestRefresh();

			if (mainFrame != null && mainFrame.fDialogOpsiLicensingInfo != null
			// && mainFrame.fDialogOpsiLicensingInfo.isVisible()
			)
				mainFrame.fDialogOpsiLicensingInfo.thePanel.reload();

			requestRefreshDataForClientSelection();

			reloadHosts();
			// includes:

			// _/

			// _/

			// _/

			// _/

			// _/
			// persist.fObject2GroupsRequestRefresh();
			// _/
			// persist.fGroup2MembersRequestRefresh();

			persist.fProductGroup2MembersRequestRefresh();
			persist.auditHardwareOnHostRequestRefresh();

			// clearing softwareMap in OpsiDataBackend
			de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataBackend.getInstance().setReloadRequested();

			clearSwInfo();
			clearHwInfo();

			System.gc();

			preloadData(); // sets dataReady

			logging.info(this, " in reload, we are in thread " + Thread.currentThread());
			setRebuiltClientListTableModel();

			if (mainFrame.controllerHWinfoMultiClients != null)
				mainFrame.controllerHWinfoMultiClients.rebuildModel();

			// for licenses
			/*
			 * persist.getRelationsWindowsSoftwareId2LPool();
			 * 
			 * Iterator iter = allControlMultiTablePanels.iterator();
			 * while (iter.hasNext())
			 * {
			 * ControlMultiTablePanel cmtp = (ControlMultiTablePanel) iter.next();
			 * if (cmtp != null)
			 * {
			 * for (int i = 0; i < cmtp.getTablePanes().size(); i++)
			 * {
			 * PanelGenEditTable p = cmtp.getTablePanes().get(i);
			 * p.reload();
			 * }
			 * }
			 * }
			 */
			fetchDepots();

			// configuratio
			persist.getHostInfoCollections().getAllDepots();
			persist.checkConfiguration(); // we do this again since we reloaded the configuration

		}

		setEditingTarget(editingTarget); // sets visual view index, therefore:
		mainFrame.setVisualViewIndex(saveViewIndex);

		// if depot selection changed, we adapt the clients
		TreeSet<String> clientsLeft = new TreeSet<>();

		for (String client : savedSelectedValues) {

			if (persist.getHostInfoCollections().getMapPcBelongsToDepot().get(client) != null) {
				String clientDepot = persist.getHostInfoCollections().getMapPcBelongsToDepot().get(client);

				if (selectedDepotsV.contains(clientDepot))
					clientsLeft.add(client);
			}
		}

		logging.info(this, "reloadData, selected clients now " + logging.getSize(clientsLeft));

		if (selectionPanel != null) // no action before gui initialized
		{
			// reactivate selection listener

			logging.debug(this, " reset the values, particularly in list ");

			selectionPanel.addListSelectionListener(this);
			setSelectedClientsCollectionOnPanel(clientsLeft);

			if (clientsLeft.isEmpty()) // no list select item is provided
				selectionPanel.fireListSelectionEmpty(this);

		}

		logging.info(this, "reloadData, selected clients now, after resetting " + logging.getSize(selectedClients));

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
			logging.info(this, "dataHaveChanged from " + source);
			setDataChanged(super.isDataChanged()); // anyDataChanged in ConfigedMain
		}

		public boolean askSave() {
			boolean result = false;
			if (this.dataChanged) {
				if (fAskSaveProductConfiguration == null) {
					fAskSaveProductConfiguration = new FTextArea(mainFrame, Globals.APPNAME, true,
							new String[] { configed.getResourceValue("MainFrame.SaveChangedValue.YES"),
									configed.getResourceValue("MainFrame.SaveChangedValue.NO") });
					fAskSaveProductConfiguration
							.setMessage(configed.getResourceValue("ConfigedMain.reminderSaveConfig"));

					fAskSaveProductConfiguration.setSize(new Dimension(300, 200));

				}

				fAskSaveProductConfiguration.setVisible(true);

				/*GeneralDataChang
				 * }
				 */

				result = (fAskSaveProductConfiguration.getResult() == 1);

				fAskSaveProductConfiguration.setVisible(false);

			}

			return result;
		}

		public void save() {
			if (this.dataChanged) // || (updateCollection.accumulatedSize() > 0)
			{
				saveConfigs();
			}

			this.dataChanged = false;

		}

		public void cancel() {
			logging.info(this, "cancel");
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
		public HostConfigsDataChangedKeeper() {
			
			super();

			/*
			 * setActingOnSource( source ->
			 * {
			 * logging.info(this, "changed source " + source );
			 * logging.info(this, "is Stsring " + (source instanceof String) );
			 * String[] parts = source.toString().split(",");
			 * for (String line: parts)
			 * {
			 * logging.info(this, "line " + line);
			 * };
			 * }
			 * 
			 * );
			 */

			// JOptionPane.showMessageDialog(null, "" + source, "alert",
			// JOptionPane.INFORMATION_MESSAGE) );
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

		// public void dataHaveChanged(Map<String, Map<String, String>> source)
		// we use this, but it would not override, therefore we perform a cast
		// it does not guarantee that the values of the map are maps!

		@Override
		public void dataHaveChanged(Object source1) {

			this.source = (Map<String, Map<String, String>>) source1;

			// public void dataHaveChanged(Map<String, Map<String, String>> source)
			// {
			

			logging.debug(this, "dataHaveChanged source " + source);

			if (source == null)
				logging.info(this, "dataHaveChanged null");
			else {
				for (String client : source.keySet()) {
					logging.debug(this, "dataHaveChanged for client " + client + source.get(client));
				}
			}

			super.dataHaveChanged(source);

			logging.debug(this, "dataHaveChanged dataChanged " + dataChanged);

			setDataChanged(super.isDataChanged());

			logging.debug(this, "dataHaveChanged dataChanged " + dataChanged);

			/*
			 * if (
			 * (this.source) == null
			 * ||
			 * (this.source).isEmpty()
			 * )
			 * {
			 * logging.debug(this, "source empty, we set datachanged false, source " +
			 * source);
			 * setDataChanged(false);
			 * }
			 */

			; // anyDataChanged in ConfigedMain

			logging.info(this, "dataHaveChanged dataChanged " + dataChanged);
		}

		public boolean askSave() {
			boolean result = false;
			if (this.dataChanged) {
				if (fAskSaveChangedText == null) {
					fAskSaveChangedText = new FTextArea(mainFrame, Globals.APPNAME, true,
							new String[] { configed.getResourceValue("MainFrame.SaveChangedValue.YES"),
									configed.getResourceValue("MainFrame.SaveChangedValue.NO") });
					fAskSaveChangedText.setMessage(configed.getResourceValue("MainFrame.SaveChangedValue"));
					fAskSaveChangedText.setSize(new Dimension(300, 200));
				}

				fAskSaveChangedText.setVisible(true);
				result = (fAskSaveChangedText.getResult() == 1);

				fAskSaveChangedText.setVisible(false);

			}

			return result;
		}

		public void save() {
			logging.info(this, "save , dataChanged " + dataChanged + " source " + source);
			if (this.dataChanged && source != null && getSelectedClients() != null
			// && getSelectedClients().length == 1 // we handle only one client
			) {

				

				logging.info(this, "save for clients " + getSelectedClients().length);

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
		logging.info(this, "setDataChanged " + b + ", showing " + show);
		anyDataChanged = b;

		if (show)
			if (mainFrame != null)
				mainFrame.saveConfigurationsSetEnabled(b);
	}

	public void cancelChanges() {
		logging.info(this, "cancelChanges ");
		setDataChanged(false);
		generalDataChangedKeeper.cancel();
	}

	public int checkClose()
	// 0 save changes
	// 1 lose changes
	// 2 cancel closing
	{
		int result = 0;

		if (anyDataChanged) {
			int returnedOption = JOptionPane.showOptionDialog(mainFrame,
					configed.getResourceValue("ConfigedMain.saveBeforeCloseText"),
					Globals.APPNAME + " " + configed.getResourceValue("ConfigedMain.saveBeforeCloseTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

			switch (returnedOption) {
			case JOptionPane.YES_OPTION: {
				result = 0;
				break;
			}

			case JOptionPane.NO_OPTION: {
				result = 1;
				break;
			}
			case JOptionPane.CANCEL_OPTION: {
				result = 2;
				break;
			}
			}
		}

		logging.debug(this, "checkClose result " + result);
		return result;
	}

	// if data are changed then save or - after asking - abandon changes
	protected void saveIfIndicated() {
		logging.info(this, "---------------- saveIfIndicated : anyDataChanged, " + anyDataChanged);

		if (!anyDataChanged)
			return;

		if (clientInfoDataChangedKeeper.askSave()) {
			clientInfoDataChangedKeeper.save();
		} else // reset to old values
		{
			hostInfo.resetGui(mainFrame);
		}
		clientInfoDataChangedKeeper.unsetDataChanged();

		if (generalDataChangedKeeper.askSave())
			generalDataChangedKeeper.save();
		else
			generalDataChangedKeeper.cancel();
		generalDataChangedKeeper.unsetDataChanged();

		if (hostConfigsDataChangedKeeper.askSave())
			hostConfigsDataChangedKeeper.save();
		else
			hostConfigsDataChangedKeeper.cancel();
		hostConfigsDataChangedKeeper.unsetDataChanged();

		setDataChanged(false, true);
		clearUpdateCollectionAndTell();
	}

	// save if not otherwise stated
	public void checkSaveAll(boolean ask) {
		logging.debug(this, "----------------  checkSaveAll: anyDataChanged, ask  " + anyDataChanged + ", " + ask);

		if (anyDataChanged) {
			setDataChanged(false, false); // without showing, but must be on first place since we run in this method
											// again

			if (ask) {
				if (clientInfoDataChangedKeeper.askSave()) {
					clientInfoDataChangedKeeper.save();
				} else // reset to old values
				{
					hostInfo.resetGui(mainFrame);
				}
			} else {
				clientInfoDataChangedKeeper.save();
			}

			if (!ask || generalDataChangedKeeper.askSave())
				generalDataChangedKeeper.save();

			if (!ask || hostConfigsDataChangedKeeper.askSave())
				hostConfigsDataChangedKeeper.save();

			setDataChanged(false, true);
		}
	}

	private class ReachableUpdater extends Thread {
		private boolean suspended = false;
		private int interval = 0;

		ReachableUpdater(Integer interval) {
			super();
			setInterval(interval);
		}

		public void setInterval(Integer interval) {
			int oldInterval = this.interval;

			if (interval == null)
				this.interval = 0;
			else
				this.interval = interval;

			if (oldInterval == 0 && this.interval > 0)
				start();
		}

		boolean isSuspended() {
			return suspended;
		}

		void setSuspended(boolean b) {
			suspended = b;
		}

		int getInterval() {
			return interval;
		}

		@Override
		public void run() {
			while (true) {
				logging.debug(this, " " + " suspended , editingTarget, viewIndex " +

						suspended + ", " + editingTarget + ", " + viewIndex

				);

				
				if (!suspended && editingTarget == EditingTarget.CLIENTS && viewIndex == VIEW_CLIENTS) {

					try {
						// we catch exceptions especially if we are on some updating process for the
						// model

						if (persist.getHost_displayFields().get("clientConnected")) {
							Map<String, Object> saveReachableInfo = reachableInfo;

							reachableInfo = persist.reachableInfo(null);
							// update column

							reachableInfo = persist.reachableInfo(null);

							javax.swing.table.AbstractTableModel model = selectionPanel.getTableModel();

							int col = model.findColumn(
									configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));

							for (int row = 0; row < model.getRowCount(); row++) {
								String clientId = (String) model.getValueAt(row, 0);
								Boolean newInfo = (Boolean) reachableInfo.get(clientId);

								/*
								 * could be sufficient:
								 * if (!model.getValueAt(row, col).equals(newInfo))
								 * {
								 * 
								 * model.setValueAt(newInfo, row, col);
								 * 
								 * model.fireTableRowsUpdated(row, row);
								 * }
								 * 
								 */

								if (newInfo != null) {

									if (saveReachableInfo.get(clientId) == null) {
										
										model.setValueAt(newInfo, row, col);
									}

									else if (model.getValueAt(row, col) != null
											&& !model.getValueAt(row, col).equals(newInfo)) {
										
										model.setValueAt(newInfo, row, col);

										model.fireTableRowsUpdated(row, row);
										// if ordered by col the order does not change although the value changes
										// if the other way is wanted a global fireTableDataChanged (see below)
										// is necessary
									}
								}

							}
						}

						/*
						 * 
						 * List selectedClients = getSelectedClientsInTable();
						 * if (changed) model.fireTableDataChanged();
						 * 
						 * if (selectedClients != null && selectedClients.size() > 0)
						 * setSelectedClientsOnPanel(selectedClients);
						 * 
						 */
					} catch (Exception ex) {
						logging.info(this, "we could not update the model");
					}

				}

				try {

					int millisecs = interval * 60 * 1000;
					logging.debug(this, "Thread going to sleep for ms " + millisecs);
					sleep(millisecs);
				} catch (InterruptedException ex) {
					logging.info(this, "Thread interrupted ");
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void getReachableInfo() {
		getReachableInfo((selectedClients != null) && (selectedClients.length > 0));
	}

	public void getReachableInfo(final boolean onlySelectedClients) {
		final String[] selClients = selectedClients;
		logging.info(this, "we have sel clients " + selClients.length);

		// we put this into a thread since it may never end in case of a name resolving
		// problem
		new Thread() {
			@Override
			public void run() {

				if (fShowReachableInfo == null) {
					fShowReachableInfo = new FShowList(null, Globals.APPNAME, false,
							new String[] { configed.getResourceValue("ConfigedMain.reachableInfoCancel") }, 350, 100);
					fShowReachableInfo.centerOn(Globals.mainContainer);
				}

				fShowReachableInfo.setMessage(configed.getResourceValue("ConfigedMain.reachableInfoRequested"));

				fShowReachableInfo.setAlwaysOnTop(true);
				fShowReachableInfo.setVisible(true);
				fShowReachableInfo.glassTransparency(true, 200, 100, 0.005f);
				fShowReachableInfo.toFront();
				reachableInfo = new HashMap<>();

				if (onlySelectedClients) {
					logging.info(this, "we have sel clients " + selClients.length);

					reachableInfo.putAll(persist.reachableInfo(getSelectedClients()));
				} else {
					reachableInfo = persist.reachableInfo(null);
				}
				// it occurs that the rpc does not return

				try {
					sleep(2000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				fShowReachableInfo.setVisible(false);

				mainFrame.iconButtonReachableInfo.setEnabled(true);

				// update column
				if (persist.getHost_displayFields().get("clientConnected")) {
					javax.swing.table.AbstractTableModel model = selectionPanel.getTableModel();

					int col = model
							.findColumn(configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));

					for (int row = 0; row < model.getRowCount(); row++) {
						String clientId = (String) model.getValueAt(row, 0);

						// " old value was " + model.getValueAt(row, col) );

						model.setValueAt(reachableInfo.get(clientId), row, col);
					}

					model.fireTableDataChanged();

					setSelectedClientsOnPanel(selClients);

				}
			}
		}.start();

	}

	public void getSessionInfo() {
		getSessionInfo((selectedClients != null) && (selectedClients.length > 0));
	}

	boolean sessioninfoFinished;

	public void getSessionInfo(final boolean onlySelectedClients) {

		final String[] selClients = selectedClients;
		sessioninfoFinished = false;

		logging.info(this, "getSessionInfo start, onlySelectedClients " + onlySelectedClients);

		mainFrame.iconButtonSessionInfo.setWaitingState(true);

		sessionInfo = new HashMap<>(); // no old values kept

		try {
			// leave the Event dispatching thread
			new Thread() {
				@Override
				public void run() {
					// disable the button
					try {
						SwingUtilities.invokeAndWait(() -> mainFrame.iconButtonSessionInfo.setEnabled(false));
					} catch (InvocationTargetException ex) {
						logging.info(this,
								"invocation target or interrupt ex at  iconButtonSessionInfo.setEnabled(false) " + ex);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}

					// handling the main perspective
					final int maxWaitSecs = 600;

					new Thread() {
						@Override
						public void run() {
							int waitSecs = 0;

							logging.info(this, "counting thread started");
							while (!sessioninfoFinished && waitSecs <= maxWaitSecs) {
								logging.debug(this, "wait secs for session infoi " + waitSecs);
								try {
									sleep(1000);
								} catch (InterruptedException iex) {
									logging.info(this, "interrupt at " + waitSecs);

								}
								waitSecs++;
							}

							// finishing the task
							SwingUtilities.invokeLater(() -> {
								logging.info(this, "when sessioninfoFinished");
								mainFrame.iconButtonSessionInfo.setWaitingState(false);

								mainFrame.iconButtonSessionInfo.setEnabled(true);

								// update column
								if (persist.getHost_displayFields().get("clientSessionInfo")) {
									javax.swing.table.AbstractTableModel model = selectionPanel.getTableModel();

									int col = model.findColumn(configed
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
							logging.info(this, "thread started");

							if (onlySelectedClients) {
								sessionInfo.putAll(persist.sessionInfo(getSelectedClients()));
							}

							else

								sessionInfo = persist.sessionInfo(null); // new String[]{"vbrupertwin7-64.uib.local"});

							sessioninfoFinished = true;

						}

					}.start();
				}
			}.start();
		} catch (Exception ex) {
			logging.info(this, "getSessionInfo Exception " + ex);
		}

	}

	public String getBackendInfos() {
		return persist.getBackendInfos();
	}

	public String getOpsiVersion() {
		return persist.getOpsiVersion();
	}

	public Map<String, RemoteControl> getRemoteControls() {
		return persist.getRemoteControls();
	}

	public void resetProductsForSelectedClients(boolean withDependencies) {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		if (!confirmActionForSelectedClients(configed.getResourceValue("ConfigedMain.confirmResetProducts.question")))
			return;

		

		persist.resetLocalbootProducts(getSelectedClients(), withDependencies);

		requestReloadStatesAndActions(true);

	}

	public boolean freeAllPossibleLicencesForSelectedClients() {
		logging.info(this, "freeAllPossibleLicencesForSelectedClients, count " + getSelectedClients().length);

		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return true;

		if (!confirmActionForSelectedClients(configed.getResourceValue("ConfigedMain.confirmFreeLicences.question"))) {
			return false;
		}

		for (String client : getSelectedClients()) {

			Map<String, List<LicenceUsageEntry>> fClient2LicencesUsageList = persist.getFClient2LicencesUsageList();

			// client " + fClient2LicencesUsageList.get(client).size());

			for (LicenceUsageEntry m : fClient2LicencesUsageList.get(client)) {

				persist.addDeletionLicenceUsage(client, m.getLicenceId(), m.getLicencepool());
			}
		}

		return persist.executeCollectedDeletionsLicenceUsage();

	}

	public void setEditableDomains(Vector<String> editableDomains) {
		this.editableDomains = editableDomains;
	}

	public void callNewClientDialog() {

		/*
		 * if (!NewClientDialog.getInstance(this).macAddressFieldIsSet())
		 * {
		 * 
		 * logging.debug(this,
		 * " --------------------------  signature of createClient  " +
		 * ((List)persist.getMethodSignature("createClient")));
		 * 
		 * if ( ((List)persist.getMethodSignature("createClient")).contains(
		 * "hardwareAddress") )
		 * NewClientDialog.getInstance(this).setMacAddressFieldVisible(true);
		 * else
		 * NewClientDialog.getInstance(this).setMacAddressFieldVisible(false);
		 * }
		 */
		

		

		Collections.sort(localbootProductnames);
		Vector<String> vLocalbootProducts = new Vector<>(localbootProductnames);
		Collections.sort(netbootProductnames);
		Vector<String> vNetbootProducts = new Vector<>(netbootProductnames);

		NewClientDialog.getInstance(this, getLinkedDepots()).setVisible(true);
		NewClientDialog.getInstance().setGroupList(new Vector<>(persist.getHostGroupIds()));
		NewClientDialog.getInstance().setProductNetbootList(vNetbootProducts);
		NewClientDialog.getInstance().setProductLocalbootList(vLocalbootProducts);

		NewClientDialog.getInstance().setDomains(editableDomains);

		NewClientDialog.getInstance().useConfigDefaults(persist.isInstallByShutdownConfigured(myServer),
				persist.isUefiConfigured(myServer), persist.isWanConfigured(myServer));

		NewClientDialog.getInstance().setHostNames(persist.getHostInfoCollections().getOpsiHostNames());

		// try // in an applet context this may cause a security problem
		// {

		// }
		// catch (Exception ex)
		// {
		// }

	}

	public void callChangeClientIDDialog() {
		if (getSelectedClients() == null || getSelectedClients().length != 1)
			return;

		FEditText fEdit = new FEditText(getSelectedClients()[0]) {
			@Override
			protected void commit() {
				super.commit();

				String newID = getText();

				logging.debug(this, "new name " + newID);

				persist.renameClient(getSelectedClients()[0], newID);

				refreshClientList(newID);
			}
		};

		fEdit.init();
		fEdit.setTitle(configed.getResourceValue("ConfigedMain.fChangeClientID.title") + " (" + Globals.APPNAME + ")");
		fEdit.setSize(250, 120);
		fEdit.centerOn(Globals.mainContainer);
		fEdit.setSingleLine(true);
		fEdit.setModal(true);
		fEdit.setAlwaysOnTop(true);
		fEdit.setVisible(true);

	}

	public void callChangeDepotDialog() {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		FShowListWithComboSelect fChangeDepotForClients = new FShowListWithComboSelect(mainFrame,
				Globals.APPNAME + " " + configed.getResourceValue("ConfigedMain.fChangeDepotForClients.title"), true,
				configed.getResourceValue("ConfigedMain.fChangeDepotForClients.newDepot"), getDepotArray(),
				new String[] { configed.getResourceValue("ConfigedMain.fChangeDepotForClients.OptionNO"),
						configed.getResourceValue("ConfigedMain.fChangeDepotForClients.OptionYES") });

		fChangeDepotForClients.setLineWrap(false);

		StringBuffer messageBuffer = new StringBuffer(
				"\n" + configed.getResourceValue("ConfigedMain.fChangeDepotForClients.Moving") + ": \n\n");

		for (int i = 0; i < getSelectedClients().length; i++) {
			messageBuffer.append(getSelectedClients()[i]);
			messageBuffer.append("     (from: ");
			try {
				messageBuffer.append(persist.getHostInfoCollections().getMapPcBelongsToDepot()
						.get(getSelectedClients()[i]).toString());
			} catch (Exception e) {
				logging.warning(this, "changeDepot for " + getSelectedClients()[i] + " " + e);
			}

			messageBuffer.append(") ");

			// result.append ("; ");
			messageBuffer.append("\n");
		}

		fChangeDepotForClients.setSize(new Dimension(400, 250));
		fChangeDepotForClients.setMessage(messageBuffer.toString());

		fChangeDepotForClients.setVisible(true);

		if (fChangeDepotForClients.getResult() != 2)
			return;

		final String targetDepot = (String) fChangeDepotForClients.getChoice();

		if (targetDepot == null || targetDepot.equals(""))
			return;

		

		logging.debug(this, " start moving to another depot");

		persist.getHostInfoCollections().setDepotForClients(getSelectedClients(), targetDepot);

		checkErrorList();

		refreshClientListKeepingGroup();

	}

	/*
	 * transferred to persistencecontroller
	 * protected void setDepotForClients(String[] clients, String depotId)
	 * {
	 * Map<String, Object> values = new HashMap<>();
	 * ArrayList depots = new ArrayList<>();
	 * values.put("clientconfig.depot.id", depots);
	 * ConfigName2ConfigValue config = new ConfigName2ConfigValue(null);
	 * 
	 * depots.add(depotId);
	 * config.put("clientconfig.depot.id", depots);
	 * for (int i = 0; i < clients.length; i++)
	 * {
	 * //collect data
	 * logging.debug(this, "setDepotForClients, client " + clients[i] +
	 * ", configState " + config);
	 * persist.setAdditionalConfiguration(clients[i], config);
	 * }
	 * //send data
	 * persist.setAdditionalConfiguration(false);
	 * 
	 * }
	 */

	/*
	 * protected void setDepotForClient(final String clientId, final String depotId)
	 * {
	 * if (clientId != null && depotId != null && !clientId.equals("") &&
	 * !depotId.equals(""))
	 * {
	 * Map networkConfig = persist.getNetworkConfiguration(clientId);
	 * networkConfig.put("depotId", depotId);
	 * persist.setNetworkConfiguration(clientId, networkConfig);
	 * }
	 * }
	 */

	/*
	 * protected void restrictClientAccess()
	 * {
	 * logging.info(this, "restrict client access start");
	 * allowedClients = null;
	 * 
	 * if (TEST_ACCESS_RESTRICTED_HOST_GROUP == null)
	 * return;
	 * 
	 * Set<String> acceptedHosts = new HashSet<>();
	 * 
	 * 
	 * for (String client :
	 * persist.getHostInfoCollections().getFNode2Treeparents().keySet())
	 * {
	 * 
	 * //+ persist.getHostInfoCollections().getFNode2Treeparents().get( client ));
	 * if (persist.getHostInfoCollections().getFNode2Treeparents().get( client
	 * ).contains ( TEST_ACCESS_RESTRICTED_HOST_GROUP ) )
	 * {
	 * acceptedHosts.add( client );
	 * 
	 * treeClients.getSimpleTreePaths( client ) );
	 * }
	 * 
	 * }
	 * 
	 * 
	 * logging.info(this, "restrict client access " + acceptedHosts);
	 * if (acceptedHosts.size() > 0)
	 * allowedClients = acceptedHosts;
	 * }
	 */

	protected void initialTreeActivation(final String groupName) {
		logging.info(this, "initialTreeActivation");
		treeClients.expandPath(treeClients.getPathToALL());

		String oldGroupSelection = groupName;
		if (oldGroupSelection == null)
			oldGroupSelection = configed.savedStates.saveGroupSelection.deserialize();

		if (oldGroupSelection != null && activateGroup(oldGroupSelection))
			logging.info(this, "old group reset " + oldGroupSelection);
		else
			activateGroup(ClientTree.ALL_NAME);
	}

	protected void initialTreeActivation() {
		initialTreeActivation(null);
	}

	protected void refreshClientListActivateALL() {
		logging.info(this, "refreshClientListActivateALL");
		refreshClientList();
		activateGroup(ClientTree.ALL_NAME);
	}

	protected void refreshClientList() {
		logging.info(this, "refreshClientList");

		

		produceClientListForDepots(getSelectedDepots(), allowedClients);

		setRebuiltClientListTableModel();

	}

	protected void refreshClientList(String selectClient) {
		logging.info(this, "refreshClientList " + selectClient);
		refreshClientListActivateALL();

		if (selectClient != null) {
			logging.debug(this, "set client refreshClientList");

			setClient(selectClient);
		}
	}

	protected void refreshClientList(String[] selectClients) {
		logging.info(this, "refreshClientList " + selectClients);
		refreshClientListActivateALL();

		if (selectClients != null) {
			logging.debug(this, "set client refreshClientList");
			setClients(selectClients);
		}
	}

	protected void refreshClientList(boolean resetSelection) {
		logging.info(this, "refreshClientList  resetSelecton " + resetSelection);
		refreshClientListActivateALL();

		if (resetSelection)
			setClientGroup();
	}

	public void reloadHosts() {
		persist.getHostInfoCollections().opsiHostsRequestRefresh();
		persist.hostConfigsRequestRefresh();
		persist.hostGroupsRequestRefresh();
		persist.fObject2GroupsRequestRefresh();
		persist.fGroup2MembersRequestRefresh(); // ??
		refreshClientListKeepingGroup();
	}

	public void setInstallByShutdownProductPropertyValue(boolean newStatus) {
		String product = "opsi-client-agent";
		String activate = "off";
		String setup = "setup";
		if (newStatus)
			activate = "on";

		// ArrayList<String> shutdown_value = ( (ArrayList)
		// persist.getCommonProductPropertyValues( new
		// ArrayList(Arrays.asList(clientname)) , product, "on_shutdown_install" ) );

		// if ( (shutdown_value.get(0) != null) &&
		// !(shutdown_value.get(0).equals(aktivate)) )
		{
			persist.setCommonProductPropertyValue(new HashSet<>(Arrays.asList(getSelectedClients())), product,
					"on_shutdown_install", Arrays.asList(activate));

			// if (status== setup)
			// set.status= none

			Map<String, String> productValues = new HashMap<>();
			productValues.put("actionRequest", setup);

			for (String clientNames : getSelectedClients()) {
				persist.updateProductOnClient(clientNames, product, OpsiPackage.TYPE_LOCALBOOT, productValues);
			}
			persist.updateProductOnClients();
		}
	}

	public void setInstallByShutdownProductPropertyValue(String clientNames, boolean status) {
		String product = "opsi-client-agent";
		String activate = "off";
		String setup = "setup";
		if (status)
			activate = "on";

		// ArrayList<String> shutdown_value = ( (ArrayList)
		// persist.getCommonProductPropertyValues( new
		// ArrayList(Arrays.asList(clientname)) , product, "on_shutdown_install" ) );

		// if ( (shutdown_value.get(0) != null) &&
		// !(shutdown_value.get(0).equals(aktivate)) )
		{
			persist.setCommonProductPropertyValue(new HashSet<>(Arrays.asList(clientNames)), product,
					"on_shutdown_install", Arrays.asList(activate));

			// if (status== setup)
			// set.status= none

			Map<String, String> productValues = new HashMap<>();
			productValues.put("actionRequest", setup);

			persist.updateProductOnClient(clientNames, product, OpsiPackage.TYPE_LOCALBOOT, productValues);
			persist.updateProductOnClients();
		}
	}

	public void createClients(Vector<Vector<Object>> clients) {
		if (persist.createClients(clients)) {

			logging.debug(this, "createClients" + clients);
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
			final String macaddress, final boolean shutdownInstall, final boolean uefiBoot, final boolean wanConfig,
			final String group, final String productNetboot, final String productLocalboot) {

		logging.debug(this,
				"createClient " + hostname + ", " + domainname + ", " + depotID + ", " + description + ", "
						+ inventorynumber + ", " + notes + shutdownInstall + ", " + uefiBoot + ", " + wanConfig + ", "
						+ group + ", " + productNetboot + ", " + productLocalboot);

		if (persist.createClient(hostname, domainname, depotID, description, inventorynumber, notes, ipaddress,
				macaddress, shutdownInstall, uefiBoot, wanConfig, group, productNetboot, productLocalboot)) {
			String newClientID = hostname + "." + domainname;

			checkErrorList();
			persist.getHostInfoCollections().addOpsiHostName(newClientID);
			persist.fObject2GroupsRequestRefresh();

			refreshClientList();

			// Activate group of created Client (and the group of all clients if no group specified)
			if (!activateGroup(group))
				activateGroup(ClientTree.ALL_NAME);

			// Sets the client on the table
			setClient(newClientID);
		}
	}

	private abstract class ErrorListProducer extends Thread {
		String title;

		ErrorListProducer(String specificPartOfTitle) {
			String part = specificPartOfTitle;
			// int maxlen = 30;
			// if (part.length() > maxlen)
			// part = part.substring(0, maxlen) + " ...";

			title = Globals.APPNAME + ":  " +
			// " (" + part + ")";
					part;
		}

		protected abstract List getErrors();

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
			fListFeedback.glassTransparency(true, 800, 200, 0.04f);

			// SwingUtilities.invokeLater( new Thread(){
			// public void run(){

			List errors = getErrors();

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
		if (clients == null)
			return;

		logging.info(this, "wakeUp " + clients.length + " clients: " + startInfo);
		if (clients.length == 0)
			return;

		new ErrorListProducer(configed.getResourceValue("ConfigedMain.infoWakeClients") + " " + startInfo) {
			@Override
			protected List getErrors() {
				return persist.wakeOnLan(clients);
			}
		}.start();

	}

	public void wakeSelectedClients() {
		wakeUp(getSelectedClients(), "");
	}

	public void wakeUpWithDelay(final int delaySecs, final String[] clients, String startInfo) {
		if (clients == null)
			return;

		logging.info(this, "wakeUpWithDelay " + clients.length + " clients: " + startInfo + " delay secs " + delaySecs);

		if (clients.length == 0)
			return;

		final FWakeClients result = new FWakeClients(mainFrame,
				Globals.APPNAME + ": " + configed.getResourceValue("FWakeClients.title") + " " + startInfo, persist);

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

	/*
	 * new Thread(){
	 * public void run()
	 * {
	 * 
	 * FShowList fListFeedback = new FShowList(mainFrame,
	 * "wake on lan",
	 * false, 0);
	 * fListFeedback.setFont(Globals.defaultFont);
	 * fListFeedback.setMessage("");
	 * fListFeedback.setButtonsEnabled(false);
	 * Cursor oldCursor = fListFeedback.getCursor();
	 * //fListFeedback.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	 * fListFeedback.setVisible(true);
	 * fListFeedback.glassTransparency(true, 1000, 200, 0.04f);
	 * 
	 * for (int i = 0; i < getSelectedClients().length; i++)
	 * {
	 * fListFeedback.appendLine("trying to start " + getSelectedClients()[i]);
	 * persist.wakeOnLan(new String[] {getSelectedClients()[i]} );
	 * try
	 * {
	 * Thread.sleep(1000 * delaySecs);
	 * }
	 * catch(InterruptedException ies)
	 * {
	 * }
	 * }
	 * 
	 * 
	 * 
	 * 
	 * }
	 * }.start();
	 * 
	 */

	public void deletePackageCachesOfSelectedClients() {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		new ErrorListProducer(configed.getResourceValue("ConfigedMain.infoDeletePackageCaches")) {
			@Override
			protected List getErrors() {
				return persist.deletePackageCaches(getSelectedClients());
			}
		}.start();

	}

	public void fireOpsiclientdEventOnSelectedClients(final String event) {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		/*
		 * without feed back other than general waiting bar
		 * 
		 * new Thread(){
		 * 
		 * @Override
		 * public void run()
		 * {
		 * persist.fireOpsiclientdEventOnClients(event, getSelectedClients());
		 * }
		 * }.start();
		 */

		new ErrorListProducer(
				// configed.getResourceValue("ConfigedMain.infoFireEvent") + event
				"opsiclientd " + event) {
			@Override
			protected List getErrors() {
				return persist.fireOpsiclientdEventOnClients(event, getSelectedClients());
			}
		}.start();

	}

	public void showPopupOnSelectedClients(final String message, final Float seconds) {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		new ErrorListProducer(configed.getResourceValue("ConfigedMain.infoPopup") + " " + message) {
			@Override
			protected List getErrors() {
				return persist.showPopupOnClients(message, getSelectedClients(), seconds);
			}

		}.start();

	}

	private void initSavedSearchesDialog() {
		if (savedSearchesDialog == null) {
			logging.debug(this, "create SavedSearchesDialog");
			savedSearchesDialog = new SavedSearchesDialog()

			{
				@Override
				protected void commit() {
					super.commit();
					List<String> result = (List<String>) super.getValue();
					logging.info(this, "commit result == null " + (result == null));
					if (result != null) {
						logging.info(this, "result size " + result.size());
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
		logging.debug(this, "clientSelectionGetSavedSearch");
		initSavedSearchesDialog();

		try {
			java.awt.Point pointField = Globals.container1.getLocationOnScreen();
			savedSearchesDialog.setLocation((int) pointField.getX() + 30, (int) pointField.getY() + 20);
		} catch (IllegalComponentStateException ex) {
			logging.info(this, "clientSelectionGetSavedSearch " + ex);
		}

		savedSearchesDialog.setVisible(true);

		/*
		 * Object value = savedSearchesDialog.getValue();
		 * logging.debug( this, "clientSelectionGetSavedSearch value "+value );
		 * if( value == null )
		 * return;
		 * setSelectedClientsOnPanel( (List<String>) value );
		 */

	}

	private class MyDialogRemoteControl extends FDialogRemoteControl {
		public void appendLog(final String s) {
			SwingUtilities.invokeLater(() -> {
				if (s == null)
					loggingArea.setText("");
				else {
					loggingArea.append(s);
					loggingArea.setCaretPosition(loggingArea.getText().length());
				}
			});
		}

		@Override
		public void commit() {
			super.commit();
			setVisible(true);

			logging.debug(this, "getSelectedValue " + getSelectedList());

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

							HashMap<String, Object> values = new HashMap<>();
							values.put("%host%", targetClient);
							String hostName = targetClient;
							logging.info(this, " targetClient " + targetClient);
							if (targetClient.indexOf(".") > 0) {
								String[] parts = targetClient.split("\\.");
								logging.info(this, " targetClient " + Arrays.toString(parts));
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

							// System.exit(0);

							try {
								logging.debug(this,
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
								logging.error("Runtime error for command >>" + cmd + "<<, : " + ex, ex);
							}
						}
					}.start();
				}

			}
		}
	}

	public void startRemoteControlForSelectedClients() {
		if (dialogRemoteControl == null)
			dialogRemoteControl = new MyDialogRemoteControl();

		if (remoteControls != getRemoteControls()) {
			remoteControls = getRemoteControls();

			logging.debug(this, "remoteControls " + remoteControls);

			Map<String, String> entries = new LinkedHashMap<>();
			Map<String, String> tooltips = new LinkedHashMap<>();
			Map<String, String> rcCommands = new HashMap<>();
			Map<String, Boolean> commandsEditable = new HashMap<>();

			for (String key : remoteControls.keySet()) {
				entries.put(key, key);
				RemoteControl rc = remoteControls.get(key);
				if (rc.getDescription() != null && rc.getDescription().length() > 0)
					tooltips.put(key, rc.getDescription());
				else
					tooltips.put(key, rc.getCommand());
				rcCommands.put(key, rc.getCommand());
				Boolean editable = Boolean.valueOf(rc.getEditable());

				// + ", " + editable);
				commandsEditable.put(key, editable);

			}

			// + tooltips );

			dialogRemoteControl.setMeanings(rcCommands);
			dialogRemoteControl.setEditable(commandsEditable);

			dialogRemoteControl
					.setListModel(new DefaultComboBoxModel<>(new Vector<>(new TreeSet<>(remoteControls.keySet()))
					// new String[]{"xx"}
					));

			dialogRemoteControl.setCellRenderer(new ListCellRendererByIndex(entries, tooltips, null, -1, false, ""));

			dialogRemoteControl
					.setTitle(Globals.APPNAME + ":  " + configed.getResourceValue("MainFrame.jMenuRemoteControl"));
			dialogRemoteControl.setModal(false);
			dialogRemoteControl.init();

		}

		dialogRemoteControl.resetValue();

		dialogRemoteControl.setLocation((int) mainFrame.getX() + 40, (int) mainFrame.getY() + 40);
		dialogRemoteControl.setSize(MainFrame.F_WIDTH, mainFrame.getHeight() / 2);

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
		// if (!(Globals.isGlobalReadOnly()))
		// {
		logging.info(this, "startSSHOpsiServerExec isReadOnly false");
		final ConfigedMain m = this;
		new Thread() {
			@Override
			public void run() {
				if (command.needParameter())
					((SSHCommandNeedParameter) command).startParameterGui(m);
				else
					new SSHConnectExec(m, command);
				// if (!(Globals.isGlobalReadOnly()))
				// {
				// if (command instanceof SSHCommand_Template)
				// exec_template((SSHCommand_Template) command);
				// else if (command.isMultiCommand()) exec_list((SSHMultiCommand)command);

				// }
			}
		}.start();
		// }
	}

	/**
	 * Starts the config dialog
	 */
	public void startSSHConfigDialog() {
		SSHConfigDialog.getInstance(mainFrame, this);
	}

	public SSHConfigDialog getSSHConfigDialog() {
		return SSHConfigDialog.getInstance(mainFrame, this);
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
				new String[] { configed.getResourceValue("buttonNO"), configed.getResourceValue("buttonYES") }, 350,
				400);

		fConfirmActionForClients.setMessage(confirmInfo + "\n\n" + getSelectedClientsString().replaceAll(";", ""));

		fConfirmActionForClients.centerOn(Globals.mainContainer);
		fConfirmActionForClients.setAlwaysOnTop(true);
		fConfirmActionForClients.setVisible(true);

		if (fConfirmActionForClients.getResult() != 2)
			return false;

		return true;
	}

	public void shutdownSelectedClients() {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		if (confirmActionForSelectedClients(
				configed.getResourceValue("ConfigedMain.ConfirmShutdownClients.question"))) {
			new ErrorListProducer(configed.getResourceValue("ConfigedMain.infoShutdownClients")) {
				@Override
				protected List getErrors() {
					return persist.shutdownClients(getSelectedClients());
				}

			}.start();
		}
	}

	public void rebootSelectedClients() {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		if (confirmActionForSelectedClients(configed.getResourceValue("ConfigedMain.ConfirmRebootClients.question"))) {
			new ErrorListProducer(configed.getResourceValue("ConfigedMain.infoRebootClients")) {
				@Override
				protected List getErrors() {
					return persist.rebootClients(getSelectedClients());
				}
			}.start();
		}
	}

	public void deleteSelectedClients() {
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		if (!confirmActionForSelectedClients(configed.getResourceValue("ConfigedMain.ConfirmDeleteClients.question")))
			return;

		

		persist.deleteClients(getSelectedClients());

		if (getFilterClientList()) {
			mainFrame.toggleClientFilterAction();

		}

		refreshClientListKeepingGroup();

		selectionPanel.clearSelection();

	}

	public void callSaveGroupDialog() {
		List groupList = persist.getHostGroupIds();
		Collections.sort(groupList);
		Vector groupSelectionIds = new Vector<>(groupList);

		int i = groupSelectionIds.indexOf(groupname);

		if (i < 0) {
			groupSelectionIds.insertElementAt(TEMPGROUPNAME, 0);
			i = 0;
		}

		GroupnameChoice choiceDialog = new GroupnameChoice(configed.getResourceValue("ConfigedMain.saveGroup"), this,
				groupSelectionIds, i);

		if (choiceDialog.getResult() == 1 && !choiceDialog.getResultString().equals("")) {
			String newGroupName = choiceDialog.getResultString();
			IconNode newGroupNode = treeClients.makeSubgroupAt(null);
			if (newGroupNode == null)
				return;

			TreePath newGroupPath = treeClients.getPathToGROUPS().pathByAddingChild(newGroupNode);

			for (int j = 0; j < getSelectedClients().length; j++) {
				treeClients.copyClientTo(getSelectedClients()[j], null, // from nowhere
						newGroupName, newGroupNode, newGroupPath);
			}

			treeClients.makeVisible(newGroupPath);
			treeClients.repaint();

		}
	}

	public void callDeleteGroupDialog() {
		List<String> groupList = persist.getHostGroupIds();
		Collections.sort(groupList);
		Vector<String> groupSelectionIds = new Vector<>(groupList);

		int i = groupSelectionIds.indexOf(groupname);

		GroupnameChoice choiceDialog = new GroupnameChoice(configed.getResourceValue("ConfigedMain.deleteGroup"), this,
				groupSelectionIds, i);

		if (choiceDialog.getResult() == 1 && !choiceDialog.getResultString().equals("")) {
			persist.deleteGroup(choiceDialog.getResultString());
			persist.hostGroupsRequestRefresh();
			if (clientSelectionDialog != null)
				clientSelectionDialog.refreshGroups();
		}
	}

	public void setGroupname(String name) {
		if (name == null || name.equals(""))
			groupname = TEMPGROUPNAME;
		else
			groupname = name;
	}

	public void callNewClientSelectionDialog() {
		java.awt.Point oldLocation = null;
		if (clientSelectionDialog != null) {
			oldLocation = clientSelectionDialog.getLocationOnScreen();
			logging.info(this, "callNewClientSelectionDialog, old location " + oldLocation);

			clientSelectionDialog.leave();
			clientSelectionDialog = null;
		}
		callClientSelectionDialog();

		if (oldLocation != null)
			clientSelectionDialog.setLocation(oldLocation.x, oldLocation.y);
	}

	public void callClientSelectionDialog() {
		initSavedSearchesDialog();

		/*
		 * if( clientSelectionDialog == null ||
		 * clientSelectionDialog.isReloadRequested())
		 * {
		 * if (clientSelectionDialog != null)
		 * {
		 * clientSelectionDialog.leave();
		 * }
		 * 
		 * clientSelectionDialog = new ClientSelectionDialog(this, selectionPanel,
		 * savedSearchesDialog);
		 * }
		 */

		if (clientSelectionDialog == null) {
			clientSelectionDialog = new ClientSelectionDialog(this, selectionPanel, savedSearchesDialog);
		}

		clientSelectionDialog.centerOn(Globals.mainContainer);
		clientSelectionDialog.setVisible(true);

		/*
		 * try
		 * {
		 * clientSelectionDialog.setAlwaysOnTop(true);
		 * }
		 * catch (SecurityException ex)
		 * {
		 * logging.debug(this, "callClientSelectionDialog " + ex);
		 * }
		 */
	}

	public void clearSelectionOnPanel() {
		selectionPanel.clearSelection();
	}

	private void setSelectedClientsOnPanel(String[] selected) {
		if (selected != null) {
			logging.info(this, " setSelectedClientsOnPanel clients count " + selected.length);
		} else
			logging.info(this, " setSelectedClientsOnPanel selected null");

		selectionPanel.removeListSelectionListener(this);
		selectionPanel.setSelectedValues(selected);
		setSelectedClientsArray(selected);
		selectionPanel.addListSelectionListener(this);
	}

	private void setSelectedClientsCollectionOnPanel(Collection<String> selected) {
		if (selected != null) {
			logging.info(this, "setSelectedClientsCollectionOnPanel clients count " + selected.size());
		}

		selectionPanel.setSelectedValues(selected);

		logging.info(this, "setSelectedClientsCollectionOnPanel   selectionPanel.getSelectedValues().size() "
				+ selectionPanel.getSelectedValues().size());

		if (selected == null)
			setSelectedClientsArray(new String[0]);
		else
			setSelectedClientsArray(selected.toArray(new String[selected.size()]));

	}

	public void setSelectedClientsCollectionOnPanel(Collection<String> selected, boolean renewFilter) {
		boolean saveFilterClientList = false;

		if (renewFilter) {
			saveFilterClientList = getFilterClientList();
			if (saveFilterClientList)
				setFilterClientList(false);
		}

		setSelectedClientsCollectionOnPanel(selected);

		if (renewFilter && saveFilterClientList)
			setFilterClientList(true);

	}

	public void selectClientsByFailedAtSomeTimeAgo(String arg) {
		de.uib.configed.clientselection.SelectionManager manager = new de.uib.configed.clientselection.SelectionManager(
				null);

		if (arg == null || arg.equals("")) {
			manager.setSearch(de.uib.opsidatamodel.SavedSearches.SEARCHfailedAtAnyTimeS);
		} else {
			String timeAgo = DateExtendedByVars.dayValueOf(arg);
			String test = String.format(de.uib.opsidatamodel.SavedSearches.SEARCHfailedByTimeS, timeAgo);

			logging.info(this, "selectClientsByFailedAtSomeTimeAgo  test " + test);
			manager.setSearch(test);
		}

		List<String> result = manager.selectClients();

		setSelectedClientsCollectionOnPanel(result, true);

	}

	public void selectClientsNotCurrentProductInstalled(List<String> selectedProducts,
			boolean includeClientsWithBrokenInstallation) {
		logging.debug(this, "selectClientsNotCurrentProductInstalled, products " + selectedProducts);
		if (selectedProducts == null || selectedProducts.size() != 1)
			return;

		String productId = selectedProducts.get(0);
		String productVersion = persist.getProductVersion(productId);
		String packageVersion = persist.getProductPackageVersion(productId);

		logging.debug(this, "selectClientsNotCurrentProductInstalled product " + productId + ", " + productVersion
				+ ", " + packageVersion);

		List<String> clientsToSelect = persist.getClientsWithOtherProductVersion(productId, productVersion,
				packageVersion, includeClientsWithBrokenInstallation);

		logging.info(this, "selectClientsNotCurrentProductInstalled clients found globally " + clientsToSelect.size());

		// clientsToSelect.retainAll( producePcListForDepots( getSelectedDepots()

		// selected depots " + clientsToSelect.size());

		clientsToSelect.retainAll(selectionPanel.getColumnValues(0));

		logging.info(this, "selectClientsNotCurrentProductInstalled clients found for displayed client list "
				+ clientsToSelect.size());

		setSelectedClientsCollectionOnPanel(clientsToSelect, true);
		setRebuiltClientListTableModel();
	}

	public void selectClientsWithFailedProduct(List<String> selectedProducts) {
		logging.debug(this, "selectClientsWithFailedProduct, products " + selectedProducts);
		if (selectedProducts == null || selectedProducts.size() != 1)
			return;

		de.uib.configed.clientselection.SelectionManager manager = new de.uib.configed.clientselection.SelectionManager(
				null);

		String test = String.format(de.uib.opsidatamodel.SavedSearches.SEARCHfailedProduct, selectedProducts.get(0));

		manager.setSearch(test);

		List<String> result = manager.selectClients();

		logging.info(this, "selected: " + result);
		setSelectedClientsCollectionOnPanel(result, true);
	}

	public void setClientGroup() {
		boolean wasFiltered = false;
		// String[] saveSelectedClients = getSelectedClients();

		if (filterClientList) // no group selection on the filtered list
		{
			setFilterClientList(false);
			wasFiltered = true;
		}

		selectionPanel.clearSelection();

		// for (int j=0; j < depotsList.getSelectedValues().length ; j++)

		Map<String, Boolean> clientList = produceClientListForDepots(getSelectedDepots(), null);
		logging.debug(this, "setClientGroup pclist " + clientList);

		if (clientList != null) {
			Object[] clients = clientList.entrySet().toArray();
			TreeSet<String> selectedList = new TreeSet<>();
			for (int i = 0; i < clients.length; i++) {
				Map.Entry ob = (Map.Entry) clients[i];

				// +ob.getValue() );

				if (Boolean.TRUE.equals((Boolean) ob.getValue())) {

					// modelIndex " + selectionPanel.convertRowIndexToModel(i) + " viewIndex " +

					selectedList.add((String) ob.getKey());
					// lsm.addSelectionInterval(selectionPanel.convertRowIndexToView(i),

					// lsm.addSelectionInterval (selectionPanel.getModelToSortedView()[i],
					// selectionPanel.getModelToSortedView()[i]);
				}
			}

			logging.debug(this, "set selected values in setClientGroup " + selectedList);
			setSelectedClientsCollectionOnPanel(selectedList, true);
		}

		if (wasFiltered) {
			filterClientList = true;

			setRebuiltClientListTableModel();

		}

	}

	/*
	 * public void loadClientGroup (String groupId, String productId, String
	 * installationStatus, String actionRequest, String productVersion, String
	 * packageVersion, Map hwFilter)
	 * // retrieves the clients fitting to the conjunction of criteria
	 * // deselects everything if all strings are null or empty
	 * {
	 * String actionRequest_querystring = "";
	 * if (!actionRequest.equals(""))
	 * actionRequest_querystring = actionRequest; //ActionRequest.getIDString(
	 * ActionRequest.getTypeFromDisplayString (actionRequest, ardDefault)) ;
	 * 
	 * //Map pclist =
	 * persist.getHostInfoCollections().getPcListForDepots(getSelectedDepots());
	 * 
	 * pclist);
	 * persist.makeClientSelection("", depotsList.getSelectedValues(),
	 * groupId, productId,
	 * installationStatus, actionRequest_querystring,
	 * productVersion, packageVersion, hwFilter);
	 * 
	 * setClientGroup();
	 * 
	 * checkErrorList();
	 * }
	 */

	// interface LogEventObserver
	@Override
	public void logEventOccurred(LogEvent event) {
		boolean found = false;

		if (allFrames == null)
			return;

		for (JFrame f : allFrames) {
			logging.debug(this, "log event occurred in frame f , is focused " + f.isFocused() + " " + f);
			if (f != null)// && f.isFocused())
			{
				logging.checkErrorList(f);
				found = true;
				break;
			}

		}

		if (!found) {
			logging.checkErrorList(mainFrame);
		}

		/*
		 * if (licencesFrame != null && licencesFrame.isFocused())
		 * {
		 * logging.checkErrorList(licencesFrame);
		 * }
		 * else
		 * {
		 * logging.checkErrorList(mainFrame);
		 * }
		 */

	}

	protected void saveConfigs() {

		logging.info(this, " --------  saveConfigs ");

		updateProductStates();
		logging.debug(this, "saveConfigs: collectChangedLocalbootStates " + collectChangedLocalbootStates);

		logging.info(this,
				" ------- we should now start working on the update collection of size  " + updateCollection.size());

		updateCollection.doCall();
		checkErrorList();

		// System.exit(0);

		clearUpdateCollectionAndTell();
	}

	private void clearUpdateCollectionAndTell() {
		logging.info(this, " --- we clear the update collection " + updateCollection.getClass());

		updateCollection.clearElements();
	}

	protected void checkErrorList() {
		logging.checkErrorList(mainFrame);
	}

	protected boolean checkSavedLicencesFrame() {
		if (allControlMultiTablePanels == null)
			return true;

		boolean change = false;
		boolean result = false;
		Iterator iter = allControlMultiTablePanels.iterator();
		while (!change && iter.hasNext()) {
			ControlMultiTablePanel cmtp = (ControlMultiTablePanel) iter.next();
			if (cmtp != null) {
				Iterator iterP = cmtp.tablePanes.iterator();
				while (!change && iterP.hasNext()) {
					PanelGenEditTable p = (PanelGenEditTable) iterP.next();
					change = p.isDataChanged();
				}
			}
		}

		if (change) {
			int returnedOption = JOptionPane.showOptionDialog(Globals.mainContainer,
					configed.getResourceValue("ConfigedMain.Licences.AllowLeaveApp"),
					configed.getResourceValue("ConfigedMain.Licences.AllowLeaveApp.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION) {
				result = true;
			}

		} else
			result = true;

		return result;

	}

	public boolean closeInstance(boolean checkdirty) {
		logging.info(this, "start closing instance, checkdirty " + checkdirty);

		boolean result = true;

		if (!FStartWakeOnLan.runningInstances.isEmpty()) {
			result = FStartWakeOnLan.runningInstances.askStop();
		}
		if (!result)
			return false;

		if (checkdirty) {
			int closeCheckResult = checkClose();
			result = (closeCheckResult < 2);
			if (!result)
				return result;

			if (closeCheckResult == 0)
				checkSaveAll(false);
		}

		if (mainFrame != null) {

			mainFrame.setVisible(false);
			mainFrame.dispose();
			mainFrame = null;
		}

		if (dpass != null) {
			dpass.setVisible(false);
			dpass.dispose();
		}

		if (!checkSavedLicencesFrame()) {
			licencesFrame.setVisible(true);
			result = false;
		}

		logging.info(this, "close instance result " + result);

		return result;
	}

	public void finishApp(boolean checkdirty, int exitcode) {
		if (closeInstance(checkdirty))
			configed.endApp(exitcode);
	}

}
