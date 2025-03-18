/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.Main;
import de.uib.configed.groupaction.ActivatedGroupModel;
import de.uib.configed.gui.ClientTablePanel;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.LoginDialog;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.guidata.DependenciesModel;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.tree.GroupNode;
import de.uib.configed.tree.ProductTree;
import de.uib.configed.type.HostInfo;
import de.uib.messagebus.Messagebus;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.ParallelTaskExecutor;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.ButtonTabComponent;
import de.uib.utils.table.gui.BooleanIconTableCellRenderer;
import de.uib.utils.userprefs.UserPreferences;

public class ConfigedMain {
	private static final Pattern backslashPattern = Pattern.compile("[\\[\\]\\s]", Pattern.UNICODE_CHARACTER_CLASS);

	private static final int ICON_COLUMN_MAX_WIDTH = 100;

	private static MainFrame mainFrame;
	private static LoginDialog loginDialog;

	private static String host;
	private static String user;
	private static String password;
	private static String otp;
	private static boolean useSSO;

	private static EditingTarget editingTarget = EditingTarget.CLIENTS;

	private OpsiServiceNOMPersistenceController persistenceController;

	private DependenciesModel dependenciesModel;

	private List<String> selectedClients = new ArrayList<>();

	private Set<String> clientsFilteredByTree = new HashSet<>();
	private ActivatedGroupModel activatedGroupModel;

	private String clientInDepot = "";
	private HostInfo hostInfo = new HostInfo();

	// collection of retrieved software audit and hardware maps

	private ClientTablePanel clientTablePanel;

	private ClientSearch clientSearch;

	private ClientTree clientTree;
	private ProductTree productTree;

	private DepotsList depotsList;
	private Map<String, Map<String, Object>> depots;
	private String depotRepresentative;

	private int clientCount;

	private Map<String, String> sessionInfo = new HashMap<>();

	public enum EditingTarget {
		CLIENTS, DEPOTS, SERVER, DASHBOARD, OPSI_MODULES, HEALTH_CHECK, LICENSE_MANAGEMENT
	}
	// with this enum type we build a state model, which target shall be edited

	private ConnectedHostsManager connectedHostsManager;

	private InitialDataLoader initialDataLoader;

	private ListSelectionListener depotsListSelectionListener = new ListSelectionListener() {
		private int counter;

		@Override
		public void valueChanged(ListSelectionEvent e) {
			counter++;
			Logging.info(this, "depotSelection event count  ", counter);

			if (!e.getValueIsAdjusting()) {
				depotsListValueChanged();
			}
		}

		private void depotsListValueChanged() {
			Logging.info(this, "depotsList selection changed");

			Configed.getSavedStates().setProperty("selectedDepots", depotsList.getSelectedValuesList().toString());

			Logging.info(this, " depotsList_valueChanged, omitted initialTreeActivation");

			// when running after the first run, we deactivate buttons
			if (initialDataLoader.isDataLoaded()) {
				initialTreeActivation();

				productTree.reInitTree();
				refreshClientListKeepingGroup();

				ButtonTabComponent comp = (ButtonTabComponent) mainFrame.getTabbedPane().getTabComponentAt(0);
				comp.showButton(depots.size() != depotsList.getSelectedValuesList().size());
			}
		}
	};

	public ConfigedMain(String host, String user, String password, String otp, boolean useSSO) {
		if (ConfigedMain.host == null) {
			setHost(host);
		}
		if (ConfigedMain.user == null) {
			setUser(user);
		}
		if (ConfigedMain.password == null) {
			setPassword(password);
		}
		if (ConfigedMain.otp == null) {
			setOTP(otp);
		}
		if (!ConfigedMain.useSSO) {
			setUseSSO(useSSO);
		}
	}

	public static MainFrame getMainFrame() {
		return mainFrame;
	}

	public LoginDialog getLoginDialog() {
		return loginDialog;
	}

	public static ConfigedMain.EditingTarget getEditingTarget() {
		return editingTarget;
	}

	protected void initGui() {
		Logging.info(this, "initGui");
		connectedHostsManager = new ConnectedHostsManager(this);

		initDepots();
		initTree();

		// create client selection panel
		clientTablePanel = new ClientTablePanel(this);

		// set table model and update the column selection in search accordingly
		clientTablePanel.getClientTable().updateModel(buildClientListTableModel(true));
		clientTablePanel.initColumnNames();

		setSelectionPanelCols();

		clientTablePanel.getClientTable().initSortKeys();

		startMainFrame(this, clientTablePanel, depotsList, clientTree, productTree);

		initTabComponents();

		activatedGroupModel = new ActivatedGroupModel(mainFrame.getHostsStatusPanel());

		initialTreeActivation();

		registerMessagebusListeners();

		if (Messagebus.getInstance().getWebSocket().isOpen()) {
			// Fake opening event on registering listener since this listener
			// does not know yet if it's open
			mainFrame.getHostsStatusPanel().onOpen(null);
		} else {
			Logging.warning(this, "Messagebus is not open, but should be on start");
		}

		Logging.debug(this, "initialTreeActivation");

		mainFrame.getClientConfiguration().getClientInfoPanel().updateClientCheckboxText();
	}

	private void initTabComponents() {
		ButtonTabComponent depotComp = (ButtonTabComponent) mainFrame.getTabbedPane().getTabComponentAt(0);
		depotComp.showButton(depots.size() != depotsList.getSelectedValuesList().size());

		ButtonTabComponent clientComp = (ButtonTabComponent) ConfigedMain.getMainFrame().getTabbedPane()
				.getTabComponentAt(1);
		clientComp.showButton(clientTree.getSelectionPaths() == null
				|| !ClientTree.ALL_CLIENTS_NAME.equals(clientTree.getSelectionPath().getLastPathComponent().toString())
				|| clientTree.getSelectionPaths().length > 1);

		ButtonTabComponent productComp = (ButtonTabComponent) ConfigedMain.getMainFrame().getTabbedPane()
				.getTabComponentAt(2);
		productComp.showButton(productTree.getSelectionPaths() == null
				|| !Configed.getResourceValue("ProductTree.allProducts")
						.equals(productTree.getSelectionPath().getLastPathComponent().toString())
				|| productTree.getSelectionPaths().length > 1);
	}

	public void registerMessagebusListeners() {
		Messagebus.getInstance().getWebSocket().registerListener(connectedHostsManager);
		Messagebus.getInstance().getWebSocket().registerListener(mainFrame.getHostsStatusPanel());
		Messagebus.getInstance().getWebSocket().registerListener(clientTablePanel.getClientTable());
	}

	public ClientSearch getClientSearch() {
		return clientSearch;
	}

	public boolean isHostConnected(String hostId) {
		return connectedHostsManager.isHostConnected(hostId);
	}

	public void loadDataAndGo() {
		Logging.clearErrorList();

		// errors are already handled in login
		Logging.info(this, " we got persist ", persistenceController);
		Logging.info(this, "initialize the data");

		dependenciesModel = new DependenciesModel();
		// Init data for these manager classes so they can work
		ChangedDataManager.initData(this, hostInfo);
		ServerActionManager.initData(this);
		Messagebus.initMessagebus(this);

		clientSearch = new ClientSearch();

		initialDataLoader = new InitialDataLoader(this);
		initialDataLoader.execute();
	}

	public void init() {
		Logging.debug(this, "init");

		// we start with a language

		InstallationStateTableModel.restartColumnDict();

		setupLoginDialog(this);
	}

	protected void preloadData() {
		if (depotRepresentative == null) {
			depotRepresentative = persistenceController.getHostInfoCollections().getConfigServer();
		}

		persistenceController.getDepotDataService().setDepot(depotRepresentative);

		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		executor.runInParallel(() -> {
			persistenceController.getProductDataService().retrieveProductIdsAndDefaultStatesPD();
			persistenceController.getProductDataService().retrieveAllProductPropertyDefinitionsPD();
			persistenceController.getProductDataService().retrieveAllProductDependenciesPD();
		});
		executor.runInParallel(() -> persistenceController.getProductDataService()
				.retrieveProductOnClientsDisplayFieldsLocalbootProducts());
		executor.runInParallel(() -> persistenceController.getGroupDataService().retrieveAllGroupsPD());
		executor.runInParallel(() -> persistenceController.getGroupDataService().retrieveAllObject2GroupsPD());
		executor.runInParallel(() -> persistenceController.getProductDataService().retrieveDepotProductPropertiesPD());
		executor.runInParallel(() -> persistenceController.getHealthDataService().retrieveHostsWithHealthCheck());
		executor.waitForCompletion();

		ExtraFrameController.reloadDialogs();
	}

	public void toggleColumn(String column) {
		boolean visible = persistenceController.getHostDataService().getHostDisplayFields().get(column);
		persistenceController.getHostDataService().getHostDisplayFields().put(column, !visible);

		setRebuiltClientListTableModel(false, false);
		clientTablePanel.getClientTable().initSortKeys();

		// We need to make first selected visible again after resetting sortKeys
		clientTablePanel.getClientTable().moveToFirstSelected();

		clientTablePanel.initColumnNames();
	}

	public void handleGroupActionRequest() {
		if (!persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.LOCAL_IMAGING)) {
			Logging.error(this,
					"this should not happen: group actions are not available since the module \"local_imaging\" is not available");
		} else if (!activatedGroupModel.isActive()) {
			JOptionPane.showMessageDialog(mainFrame, Configed.getResourceValue("ConfigedMain.noGroupSelected"),
					Configed.getResourceValue("error"), JOptionPane.ERROR_MESSAGE);
		} else {
			ExtraFrameController.startGroupActionFrame(this);
		}
	}

	public static void setEditingTarget(EditingTarget newEditingTarget) {
		Logging.info("setEditingTarget ", newEditingTarget);
		ChangedDataManager.checkSaveAll(true);
		if (newEditingTarget == editingTarget) {
			Logging.info("stop setting editingTarget, it remains the same");
			return;
		}

		editingTarget = newEditingTarget;

		switch (editingTarget) {
		case CLIENTS:
			mainFrame.showClientConfiguration();
			break;

		case DEPOTS:
			mainFrame.showDepotConfiguration();
			break;

		case SERVER:
			mainFrame.showServerConfiguration();
			break;

		case DASHBOARD:
			mainFrame.showDashboard();
			break;

		case OPSI_MODULES:
			mainFrame.showOpsiModules();
			break;

		case HEALTH_CHECK:
			mainFrame.showHealthDataAction();
			break;

		case LICENSE_MANAGEMENT:
			mainFrame.startLicensingManagement();
			break;
		}
	}

	public void actOnListSelection() {
		Logging.info(this, "actOnListSelection");

		ChangedDataManager.checkSaveAll(true);
		Logging.checkErrorList();

		Logging.info(this, "ListSelectionListener valueChanged getSelectedRowCount() ",
				clientTablePanel.getClientTable().getSelectedRowCount());

		Set<String> clientsSelectedInTable = clientTablePanel.getClientTable().getSelectedSet();
		Logging.info(this, "setSelectedClients clientNames size ", clientsSelectedInTable.size());

		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTIES.toString());

		Logging.info(this, "setSelectedClientsArray ", clientsSelectedInTable.size());
		Logging.info(this, "selectedClients was before ", selectedClients.size());

		selectedClients = new ArrayList<>(clientsSelectedInTable);

		clientTree.produceActiveParents();

		// change in selection not via clientpage (i.e. via tree)
		mainFrame.getClientConfiguration().stateChanged(null);

		clientInDepot = "";

		hostInfo.resetValues();

		updateHostInfo();

		mainFrame.getClientConfiguration().getClientInfoPanel().setClientInfoEditing(selectedClients.size() == 1,
				selectedClients.isEmpty());

		// initialize the following method
		Iterator<String> selectedDepotsIterator = getDepotsOfSelectedClients().iterator();
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

		if (selectedClients.size() == 1) {
			mainFrame.getClientConfiguration().getClientInfoPanel().setClientID(selectedClients.get(0));
			mainFrame.getClientConfiguration().getClientInfoPanel()
					.updateHealthCheckActiveCheckBoxStatus(selectedClients.get(0));
		} else {
			mainFrame.getClientConfiguration().getClientInfoPanel().setClientID("");
			mainFrame.getClientConfiguration().getClientInfoPanel().updateHealthCheckActiveCheckBoxStatus(null);
		}

		hostInfo.resetGui();

		Logging.info(this, "actOnListSelection update hosts status selectedClients ", selectedClients.size(),
				" as well as ", clientTablePanel.getClientTable().getSelectedRowCount());

		mainFrame.getHostsStatusPanel().updateValues(clientCount, selectedClients, clientInDepot);

		activatedGroupModel.setActive(selectedClients.isEmpty());

		clientTree.updateSelectedObjectsInTable();
	}

	private void updateHostInfo() {
		Map<String, HostInfo> pcinfos = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps();

		Logging.info(this, "updateHostInfo, produce hostInfo  selectedClients.length ", selectedClients.size());

		if (!selectedClients.isEmpty()) {
			hostInfo.setValues(pcinfos.get(selectedClients.get(0)).getMap());

			Logging.debug(this, "updateHostInfo, produce hostInfo first selClient ", selectedClients.get(0));
			Logging.debug(this, "updateHostInfo, produce hostInfo  ", hostInfo);

			HostInfo secondInfo = new HostInfo();

			for (int i = 1; i < selectedClients.size(); i++) {
				secondInfo.setValues(pcinfos.get(selectedClients.get(i)).getMap());
				hostInfo.combineWith(secondInfo);
			}
		}
	}

	private void initDepots() {
		// create depotsList
		depotsList = new DepotsList(this);

		Logging.info(this, "create depotsListSelectionListener");
		depotsList.addListSelectionListener(depotsListSelectionListener);

		fetchDepots();

		depotsList.setInfo(depots);
		List<String> oldSelectedDepots = Arrays
				.asList(backslashPattern
						.matcher(Configed.getSavedStates().getProperty("selectedDepots",
								persistenceController.getHostInfoCollections().getConfigServer()))
						.replaceAll("").split(","));
		depotsList.setSelectedValues(oldSelectedDepots);
	}

	private static void startMainFrame(ConfigedMain configedMain, ClientTablePanel clientTablePanel,
			DepotsList depotsList, ClientTree clientTree, ProductTree productTree) {
		mainFrame = new MainFrame(configedMain, clientTablePanel, depotsList, clientTree, productTree);
		Utils.setMasterFrame(mainFrame);

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

	// returns true if we have a PersistenceController and are connected
	private static void setupLoginDialog(ConfigedMain configedMain) {
		Logging.debug(" create password dialog ");
		loginDialog = new LoginDialog(configedMain);

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

		if (otp != null) {
			loginDialog.setOTP(otp);
		}

		Logging.info("become interactive");
		Logging.info("using sso ? ", useSSO);
		loginDialog.setVisible(true);

		if (host == null) {
			Logging.info("host is not set (yet)");
		}
		if (!useSSO && (user == null || password == null)) {
			Logging.info("user or password not given (yet)");
		} else {
			// This must be called last, so that loading frame for connection is called last
			// and on top of the login-frame
			Logging.info("loginDialog tryConnecting with sso ", useSSO);
			loginDialog.tryConnectingDependOnServer(useSSO);
		}
	}

	public void setPersistenceController(OpsiServiceNOMPersistenceController persistenceController) {
		this.persistenceController = persistenceController;
	}

	public DependenciesModel getDependenciesModel() {
		return dependenciesModel;
	}

	private Set<String> produceClientSetForDepots(Set<String> allowedClients) {
		Logging.info(this, " producePcListForDepots ", depotsList.getSelectedValuesList(),
				" running with allowedClients ", allowedClients);
		Set<String> m = persistenceController.getHostInfoCollections()
				.getClientsForDepots(depotsList.getSelectedValuesList(), allowedClients);

		clientCount = m.size();

		if (mainFrame != null) {
			mainFrame.getHostsStatusPanel().updateValues(clientCount, selectedClients, clientInDepot);
			clientTablePanel.updateTable();
		}

		return m;
	}

	private TableModel buildClientListTableModel(boolean rebuildTree) {
		Logging.debug(this, "buildPclistTableModel rebuildTree ", rebuildTree);

		Set<String> clientsForTableModel = produceClientSetForDepots(null);

		Logging.debug(this, " unfilteredList ");

		Logging.info(this, "buildPclistTableModel, rebuildTree  ", rebuildTree);

		Set<String> permittedHostGroups = null;
		if (!persistenceController.getUserRolesConfigDataService().isAccessToHostgroupsOnlyIfExplicitlyStatedPD()) {
			Logging.info(this, "buildPclistTableModel not full hostgroups permission");
			permittedHostGroups = persistenceController.getUserRolesConfigDataService().getHostGroupsPermitted();
		}

		if (rebuildTree) {
			rebuildTree(new TreeSet<>(clientsForTableModel), permittedHostGroups);
		}

		// changes the produced unfilteredList
		if (getAllowedClients() != null) {
			clientsForTableModel = produceClientSetForDepots(getAllowedClients());

			Logging.info(this, " clientsForTableModel ", clientsForTableModel.size());

			Logging.info(this, "buildPclistTableModel, rebuildTree  ", rebuildTree);

			if (rebuildTree) {
				rebuildTree(new TreeSet<>(clientsForTableModel), permittedHostGroups);
			}
		}

		clientsForTableModel.retainAll(clientsFilteredByTree);

		Logging.info(this, " clientTable isFilteredMode ", clientTablePanel.isFilteredMode());

		if (clientTablePanel.isFilteredMode()) {
			Logging.info(this, "buildPclistTableModel with filterCLientList, number of selected pcs ",
					selectedClients.size());

			// selected clients that are in the pclist0
			clientsForTableModel.retainAll(selectedClients);
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

		List<String> displayFields = new ArrayList<>();
		for (Entry<String, Boolean> entry : persistenceController.getHostDataService().getHostDisplayFields()
				.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {
				model.addColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel." + entry.getKey()));
				displayFields.add(entry.getKey());
			}
		}

		UserPreferences.set(UserPreferences.CLIENTS_TABLE_DISPLAY_FIELDS, String.join(",", displayFields));

		Logging.info(this, "buildPclistTableModel host_displayFields ",
				persistenceController.getHostDataService().getHostDisplayFields());

		Set<Object> hostsWithActiveHealthCheck = persistenceController.getHealthDataService()
				.getHostsWithActiveHealthCheck();
		for (String clientId : clientIds) {
			HostInfo pcinfo = pcinfos.get(clientId);
			if (pcinfo == null) {
				pcinfo = new HostInfo();
			}

			Map<String, Object> rowmap = pcinfo.getDisplayRowMap0();

			String sessionValue = "";
			if (sessionInfo.get(clientId) != null) {
				sessionValue = sessionInfo.get(clientId);
			}

			rowmap.put(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL, sessionValue);
			rowmap.put(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, isHostConnected(clientId));
			rowmap.put(HostInfo.HEALTH_CHECK_ACTIVE_FIELD_LABEL, hostsWithActiveHealthCheck.contains(clientId));

			List<Object> rowItems = new ArrayList<>();

			for (Entry<String, Boolean> entry : persistenceController.getHostDataService().getHostDisplayFields()
					.entrySet()) {
				if (Boolean.TRUE.equals(entry.getValue())) {
					rowItems.add(rowmap.get(entry.getKey()));
				}
			}

			model.addRow(rowItems.toArray());
		}

		Logging.info(this, "buildPclistTableModel, model column count ", model.getColumnCount());

		return model;
	}

	private void rebuildTree(Collection<String> allPCs, Set<String> permittedHostGroups) {
		clientTree.clear();
		clientTree.build(allPCs, permittedHostGroups);
	}

	public void setClient(String clientName) {
		setClients(Collections.singleton(clientName));
	}

	public void setClients(Collection<String> clientNames) {
		Logging.info(this, "setClients ", clientNames);
		if (clientNames == null) {
			clientTablePanel.setSelectedValues(Collections.emptySet());
		} else {
			clientTablePanel.setSelectedValues(clientNames);
		}
	}

	/**
	 * activates a group
	 *
	 * @param groupname
	 */
	public boolean activateGroup(boolean preferringOldSelection, String groupname) {
		Logging.info(this, "activateGroup  ", groupname);
		if (groupname == null) {
			return false;
		}

		if (clientTree.getGroupNode(groupname) == null) {
			Logging.warning("no group ", groupname);
			return false;
		}

		GroupNode node = clientTree.getGroupNode(groupname);
		TreePath path = clientTree.getPathToNode(node);

		activateGroupByTree(preferringOldSelection, node);

		Logging.info(this, "expand activated  path ", path);
		clientTree.setSelectionPath(path);
		clientTree.expandPath(path);

		return true;
	}

	/**
	 * activates a group and selects all clients
	 *
	 * @param groupname
	 */
	public void setGroupAndSelect(String groupname) {
		Logging.info(this, "setGroup ", groupname);
		if (!activateGroup(true, groupname)) {
			return;
		}

		clientTablePanel.setSelectedValues(clientsFilteredByTree);
	}

	public void setClientsFilteredAndSelected(Set<String> filterIds, Set<String> selectedIds) {
		clientsFilteredByTree.clear();
		if (filterIds != null) {
			for (String filterId : filterIds) {
				clientsFilteredByTree.add(filterId);
			}
		}
		setRebuiltClientListTableModel(true, false, selectedIds);
	}

	public List<String> getSelectedClients() {
		return selectedClients;
	}

	private void setSelectionPanelCols() {
		BooleanIconTableCellRenderer defaultCheckMarkCellRenderer = new BooleanIconTableCellRenderer(
				Icons.getIntellijIcon("checkmark", null), null);
		BooleanIconTableCellRenderer opsiCheckMarkCellRenderer = new BooleanIconTableCellRenderer(
				Icons.getIntellijIcon("checkmark", Globals.OPSI_OK), null);

		configureColumn(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, opsiCheckMarkCellRenderer);
		configureColumn(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL, defaultCheckMarkCellRenderer);
		configureColumn(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL, defaultCheckMarkCellRenderer);
		configureColumn(HostInfo.HEALTH_CHECK_ACTIVE_FIELD_LABEL, defaultCheckMarkCellRenderer);
	}

	private void configureColumn(String fieldLabel, TableCellRenderer renderer) {
		if (Boolean.FALSE.equals(persistenceController.getHostDataService().getHostDisplayFields().get(fieldLabel))) {
			Logging.info(this, "configureColumn, column is hidden " + fieldLabel);
			return;
		}

		String colPropertyName = Configed.getResourceValue("ConfigedMain.pclistTableModel." + fieldLabel);
		int col = clientTablePanel.getTableModel().findColumn(colPropertyName);
		if (col == -1) {
			Logging.info(this, "configureColumn, column not found " + colPropertyName);
			return;
		}

		TableColumn column = clientTablePanel.getClientTable().getColumnModel().getColumn(col);
		column.setMaxWidth(ICON_COLUMN_MAX_WIDTH);
		column.setCellRenderer(renderer);
		Logging.info(this, "configureColumn, found column ", col);
	}

	public void setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree) {
		Logging.info(this, "setRebuiltClientListTableModel, we have selected Set : ",
				clientTablePanel.getClientTable().getSelectedSet());

		setRebuiltClientListTableModel(restoreSortKeys, rebuildTree,
				clientTablePanel.getClientTable().getSelectedSet());
	}

	private void setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree,
			Set<String> selectValues) {
		Logging.info(this,
				"setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree, Set selectValues)  : ",
				restoreSortKeys, ", ", rebuildTree, ",  selectValues.size() ", Logging.getSize(selectValues));

		List<? extends SortKey> saveSortKeys = clientTablePanel.getClientTable().getRowSorter().getSortKeys();

		Logging.info(this, " setRebuiltClientListTableModel--- set model new, selected ",
				clientTablePanel.getClientTable().getSelectedRowCount());

		TableModel tm = buildClientListTableModel(rebuildTree);
		Logging.info(this, "setRebuiltClientListTableModel --- got model selected ",
				clientTablePanel.getClientTable().getSelectedRowCount());

		int[] columnWidths = ConfigedUtilityMethods.getTableColumnWidths(clientTablePanel.getClientTable());

		// We want to deactivate the listener here, since we want it to react only later when
		// the values are selected. We only reactivate the listener if it was active before.
		boolean listenerDeactivated = clientTablePanel.deactivateListSelectionListener();
		clientTablePanel.getClientTable().updateModel(tm);
		if (listenerDeactivated) {
			clientTablePanel.activateListSelectionListener();
		}

		ConfigedUtilityMethods.setTableColumnWidths(clientTablePanel.getClientTable(), columnWidths);

		Logging.debug(this, " --- model set  ");

		setSelectionPanelCols();

		if (restoreSortKeys) {
			clientTablePanel.getClientTable().getRowSorter().setSortKeys(saveSortKeys);
		}

		Logging.info(this, "setRebuiltClientListTableModel set selected values in setRebuiltClientListTableModel() ",
				Logging.getSize(selectValues));
		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel",
				clientTablePanel.getClientTable().getSelectedRowCount());

		// did lose the selection since last setting
		clientTablePanel.setSelectedValues(selectValues);

		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel ",
				Logging.getSize(clientTablePanel.getClientTable().getSelectedSet()));

		Logging.info(this, "setRebuiltClientListTableModel");
	}

	private Set<String> getDepotsOfSelectedClients() {
		Set<String> depotsOfSelectedClients = new TreeSet<>();

		for (String selectedClient : selectedClients) {
			if (persistenceController.getHostInfoCollections().getMapPcBelongsToDepot().get(selectedClient) != null) {
				depotsOfSelectedClients.add(
						persistenceController.getHostInfoCollections().getMapPcBelongsToDepot().get(selectedClient));
			}
		}

		return depotsOfSelectedClients;
	}

	private void treeClientsSelectAction(TreePath newSelectedPath) {
		Logging.info(this, "treeClientsSelectAction");

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) newSelectedPath.getLastPathComponent();
		Logging.info(this, "treeClientsSelectAction selected node ", selectedNode);

		if (selectedNode.getAllowsChildren()) {
			activateGroupByTree(false, selectedNode);
			clientTree.updateSelectedObjectsInTable();
		} else {
			// Activate client
			setRebuiltClientListTableModel(true, false, clientsFilteredByTree);
			setGroupNameForNode(selectedNode);
			mainFrame.getHostsStatusPanel().updateValues(clientCount, selectedClients, clientInDepot);

		}
	}

	public void treeClientsSelectAction(TreePath[] selTreePaths) {
		clientTablePanel.setFilterMark(false);

		clientsFilteredByTree.clear();
		if (selTreePaths != null) {
			for (TreePath selectionPath : selTreePaths) {
				clientsFilteredByTree.add(selectionPath.getLastPathComponent().toString());
			}
		}

		if (selTreePaths == null) {
			setRebuiltClientListTableModel(true, false, clientsFilteredByTree);
			mainFrame.getHostsStatusPanel().setGroupName("");
			mainFrame.getHostsStatusPanel().updateValues(clientCount, selectedClients, clientInDepot);
		} else if (selTreePaths.length == 1) {
			treeClientsSelectAction(selTreePaths[0]);
		} else {
			Logging.info(this, "treeClientsSelectAction selTreePaths: ", selTreePaths.length);
			setRebuiltClientListTableModel(true, false, clientsFilteredByTree);
		}
	}

	private void setGroupNameForNode(DefaultMutableTreeNode selectedNode) {
		if (selectedClients.size() == 1 && selectedNode.getParent() != null) {
			mainFrame.getHostsStatusPanel().setGroupName(selectedNode.getParent().toString());
		} else {
			mainFrame.getHostsStatusPanel().setGroupName("");
		}
	}

	private void initTree() {
		Logging.debug(this, "initTree");

		clientTree = new ClientTree(this);
		productTree = new ProductTree(this);
		persistenceController.getHostInfoCollections().setTree(clientTree);
	}

	private void setGroupByTree(DefaultMutableTreeNode node) {
		Logging.info(this, "setGroupByTree, node ", node);

		clientTree.initActiveParents();
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

	private void activateGroupByTree(boolean preferringOldSelection, DefaultMutableTreeNode node) {
		Logging.info(this, "activateGroupByTree, node: ", node);

		setGroupByTree(node);
		Set<String> selectValues = null;
		// intended for reload, we cancel activating group
		if (preferringOldSelection && !clientTablePanel.getClientTable().getSelectedSet().isEmpty()) {
			selectValues = clientTablePanel.getClientTable().getSelectedSet();
		}

		setRebuiltClientListTableModel(true, false, selectValues);
		// with this, a selected client remains selected (but in bottom line, the group
		// seems activated, not the client)

		activatedGroupModel.setNode("" + node);
		activatedGroupModel.setDescription(clientTree.getGroups().get("" + node).get("description"));
		activatedGroupModel.setAssociatedClients(clientsFilteredByTree);
		activatedGroupModel.setActive(true);

		// since we select based on the tree view we disable the filter
		deactivateFilter();
	}

	public void deactivateFilter() {
		Logging.info(this, "deactivate filter", clientTablePanel.isFilteredMode());
		if (clientTablePanel.isFilteredMode()) {
			setRebuiltClientListTableModel(true, false);
		}
	}

	public ActivatedGroupModel getActivatedGroupModel() {
		return activatedGroupModel;
	}

	private boolean checkSynchronous(Set<String> depots) {
		if (depots.size() > 1 && !persistenceController.getDepotDataService().areDepotsSynchronous(depots)) {
			JOptionPane.showMessageDialog(mainFrame, Configed.getResourceValue("ConfigedMain.notSynchronous.text"),
					Configed.getResourceValue("ConfigedMain.notSynchronous.title"), JOptionPane.OK_OPTION);

			return false;
		}

		return true;
	}

	public boolean setDepotRepresentative() {
		Logging.debug(this, "setDepotRepresentative");

		if (selectedClients.isEmpty()) {
			depotRepresentative = persistenceController.getHostInfoCollections().getConfigServer();

			return true;
		}

		Set<String> depotsOfSelectedClients = getDepotsOfSelectedClients();

		Logging.info(this, "depots of selected clients:", depotsOfSelectedClients);

		Logging.debug(this, "setDepotRepresentative(), old representative: ", depotRepresentative, " should be ");

		if (!checkSynchronous(depotsOfSelectedClients)) {
			return false;
		}

		String oldRepresentative = depotRepresentative;

		Logging.debug(this, "setDepotRepresentative  start   up to now ", oldRepresentative, " old",
				depotRepresentative, " equal ", oldRepresentative.equals(depotRepresentative));

		Logging.info(this, "setDepotRepresentative depotsOfSelectedClients ", depotsOfSelectedClients);

		Iterator<String> depotsIterator = depotsOfSelectedClients.iterator();

		if (!depotsIterator.hasNext()) {
			depotRepresentative = persistenceController.getHostInfoCollections().getConfigServer();
			Logging.debug(this, "setDepotRepresentative  without next change depotRepresentative ", " up to now ",
					oldRepresentative, " new ", depotRepresentative, " equal ",
					oldRepresentative.equals(depotRepresentative));
		} else {
			depotRepresentative = depotsIterator.next();

			while (depotsIterator.hasNext()) {
				String depot = depotsIterator.next();
				if (depot.equals(persistenceController.getHostInfoCollections().getConfigServer())) {
					depotRepresentative = depot;
					break;
				}
			}
		}

		Logging.debug(this, "depotRepresentative: ", depotRepresentative);

		Logging.info(this, "setDepotRepresentative  change depotRepresentative ", " up to now ", oldRepresentative,
				" new ", depotRepresentative, " equal ", oldRepresentative.equals(depotRepresentative));

		if (!oldRepresentative.equals(depotRepresentative)) {
			Logging.info(this, " new depotRepresentative ", depotRepresentative);
			persistenceController.getDepotDataService().setDepot(depotRepresentative);

			// everything
			persistenceController.reloadData(ReloadEvent.DEPOT_CHANGE_RELOAD.toString());
		}

		return true;
	}

	public String getDepotRepresentative() {
		return depotRepresentative;
	}

	public List<String> getSelectedDepots() {
		return depotsList.getSelectedValuesList();
	}

	public void selectAllDepots() {
		depotsList.setSelectedValues(depots.keySet());
	}

	public void activateAllProductsGroup() {
		GroupNode node = productTree.getGroupNode(Configed.getResourceValue("ProductTree.allProducts"));
		TreePath path = productTree.getPathToNode(node);
		productTree.setSelectionPath(path);
		productTree.expandPath(path);
	}

	public Set<String> getAllowedClients() {
		return clientTree.getAllowedClients();
	}

	public String[] getDepotArray() {
		if (depots == null) {
			return new String[] {};
		}

		return depots.keySet().toArray(new String[0]);
	}

	private void fetchDepots() {
		Logging.info(this, "fetchDepots");

		Logging.debug(this, "fetchDepots sorted depots ",
				persistenceController.getHostInfoCollections().getDepotNamesList());

		depots = persistenceController.getHostInfoCollections().getDepots();
		List<String> oldSelection = depotsList.getSelectedValuesList();

		// Setting the list data will remove old selection. To prevent doing events twice
		// we set the flag that value is adjusting, because we will set the selected values again.
		// Both actions will then be united into one event only
		depotsList.setValueIsAdjusting(true);
		depotsList.setListData(persistenceController.getHostInfoCollections().getDepotNamesList());
		depotsList.setSelectedValues(oldSelection);
		depotsList.setValueIsAdjusting(false);

		Logging.debug(this, "selected after fetch ", getSelectedDepots().size());
	}

	public void refreshClientListKeepingGroup() {
		// dont do anything if we did not finish another thread for this
		String oldGroupSelection = activatedGroupModel.getGroupName();
		Logging.info(this, " refreshClientListKeepingGroup oldGroupSelection ", oldGroupSelection);

		Map<String, Map<String, Object>> nodes = clientTree.getExpandedAndSelectedNodes();
		setRebuiltClientListTableModel(true, true);
		activateGroup(true, oldGroupSelection);
		clientTree.expandAndSelectNodes(nodes);
	}

	public void reload() {
		mainFrame.activateLoadingPane(Configed.getResourceValue("MainFrame.jMenuFileReload") + " ...");
		SwingUtilities.invokeLater(this::reloadData);
	}

	private void reloadData() {
		ChangedDataManager.checkSaveAll(true);

		Set<String> selValuesList = clientTablePanel.getClientTable().getSelectedSet();
		Logging.info(this, "reloadData, selValuesList.size ", clientTablePanel.getClientTable().getSelectedRowCount());

		String selectedGroup = getActivatedGroupModel().getGroupName();
		Set<String> selectedLocalbootProducts = mainFrame.getClientConfiguration().getPanelLocalbootProductSettings()
				.getProductTable().getSelectedIDs();
		Set<String> selectedNetbootProducts = mainFrame.getClientConfiguration().getPanelNetbootProductSettings()
				.getProductTable().getSelectedIDs();
		clientTablePanel.deactivateListSelectionListener();
		depotsList.removeListSelectionListener(depotsListSelectionListener);

		persistenceController.reloadData(CacheIdentifier.ALL_DATA.toString());
		persistenceController.getUserRolesConfigDataService().checkConfigurationPD();

		preloadData();

		mainFrame.resetData();

		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTIES.toString());

		mainFrame.getClientConfiguration().getClientInfoPanel().updateClientCheckboxText();

		Logging.info(this, " in reload, we are in thread ", Thread.currentThread());

		productTree.reInitTree();
		clientTree.reInitTree();
		fetchDepots();

		// if depot selection changed, we adapt the clients
		Set<String> clientsLeft = new TreeSet<>();
		for (String client : selValuesList) {
			String depotForClient = persistenceController.getHostInfoCollections().getMapPcBelongsToDepot().get(client);

			if (depotForClient != null && depotsList.getSelectedValuesList().contains(depotForClient)) {
				clientsLeft.add(client);
			}
		}

		Logging.info(this, "reloadData, selected clients now ", Logging.getSize(clientsLeft));

		Logging.debug(this, " reset the values, particularly in list ");

		activateGroupByTree(true, clientTree.getGroupNode(selectedGroup));
		clientTablePanel.setSelectedValues(clientsLeft);
		clientTablePanel.activateListSelectionListener();
		clientTree.produceActiveParents();
		clientTree.updateSelectedObjectsInTable();

		mainFrame.getClientConfiguration().getPanelLocalbootProductSettings().getProductTable()
				.setSelection(selectedLocalbootProducts);
		mainFrame.getClientConfiguration().getPanelNetbootProductSettings().getProductTable()
				.setSelection(selectedNetbootProducts);
		productTree.produceActiveParents();
		productTree.updateSelectedObjectsInTable();

		depotsList.addListSelectionListener(depotsListSelectionListener);

		Logging.info(this, "reloadData, selected clients now, after resetting ", Logging.getSize(selectedClients));
		mainFrame.reloadServerConsoleMenu();

		updateHostInfo();

		hostInfo.resetGui();

		mainFrame.deactivateLoadingPane();

		updatePage();

		initTabComponents();
	}

	private static void updatePage() {
		// We want to reset and reload the page that is being shown now...
		EditingTarget t = editingTarget;
		editingTarget = null;
		setEditingTarget(t);

		// We need to update the client configuration since it will not be done
		// automatically in the method setEditingTarget!
		if (t == EditingTarget.CLIENTS) {
			mainFrame.getClientConfiguration().stateChanged(null);
		}
	}

	public ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
	}

	public void setSessionInfo(Map<String, String> sessionInfo) {
		this.sessionInfo = sessionInfo;
	}

	private void initialTreeActivation() {
		Logging.info(this, "initialTreeActivation");

		TreePath pathToSelect = null;
		String oldGroupSelection = Configed.getSavedStates().getProperty("groupname");

		if (oldGroupSelection != null && clientTree.getGroupNode(oldGroupSelection) != null) {
			pathToSelect = clientTree.getPathToNode(clientTree.getGroupNode(oldGroupSelection));
			Logging.info(this, "old group reset ", oldGroupSelection);
		}

		// the old path selection may not exist any more
		if (pathToSelect == null || pathToSelect.getPathCount() <= 1) {
			pathToSelect = clientTree.getPathToALL();
		}

		clientTree.expandPath(pathToSelect);
		clientTree.setSelectionPath(pathToSelect);
	}

	public void refreshClientListActivateALL() {
		Logging.info(this, "refreshClientListActivateALL");
		setRebuiltClientListTableModel(true, true);
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

	public void openTerminalOnClient() {
		openTerminalOnHost("Client");
	}

	public void openTerminalOnDepot() {
		openTerminalOnHost("ConfigserverOrDepot");
	}

	private void openTerminalOnHost(String type) {
		if (!"Client".equals(type) && !"ConfigserverOrDepot".equals(type)) {
			throw new IllegalArgumentException("type must be either 'Client' or 'Depot'");
		}
		String connectToHost = ("Client".equals(type)) ? selectedClients.get(0) : depotsList.getSelectedValue();
		if (connectToHost == null) {
			throw new IllegalArgumentException("host must not be null. (type: " + type + ")");
		}
		if ("ConfigserverOrDepot".equals(type)
				&& connectToHost.equals(persistenceController.getHostInfoCollections().getConfigServer())) {
			connectToHost = "Configserver";
		}

		if (!isHostConnected(connectToHost) && !"Configserver".equals(connectToHost)) {
			Logging.info(this, type, " shell access feature is only supported for clients connected with messagebus");
			JOptionPane.showMessageDialog(mainFrame,
					Configed.getResourceValue("ConfigedMain.openTerminalOn" + type + "Feature.message"));
			return;
		}
		TerminalFrame terminalFrame = new TerminalFrame(this);
		terminalFrame.setMessagebus(Messagebus.getInstance());
		terminalFrame.setSession(connectToHost);
		terminalFrame.display();
	}

	public void invertSelection() {
		List<String> previouslySelectedClients = getSelectedClients();
		List<String> clientsToSelect = new ArrayList<>();
		int rowCount = clientTablePanel.getTableModel().getRowCount();
		for (int i = 0; i < rowCount; i++) {
			String clientName = (String) clientTablePanel.getTableModel().getValueAt(i, 0);
			if (!previouslySelectedClients.contains(clientName)) {
				clientsToSelect.add(clientName);
			}
		}
		setClients(clientsToSelect);
	}

	public static boolean closeInstance(boolean checkdirty) {
		Logging.info("start closing instance, checkdirty ", checkdirty);

		if (checkdirty) {
			int closeCheckResult = ChangedDataManager.checkClose();

			if (closeCheckResult == JOptionPane.YES_OPTION) {
				ChangedDataManager.checkSaveAll(false);
			} else if (closeCheckResult != JOptionPane.NO_OPTION) {
				return false;
			} else {
				// Do when closing without option
			}

			// We set editing target because after restarting the configed, we will show this panel!
			editingTarget = EditingTarget.CLIENTS;
		}

		boolean result = true;

		if (mainFrame != null) {
			result = mainFrame.checkSaveLicenses();
			if (result) {
				mainFrame.setVisible(false);
				mainFrame.dispose();
				mainFrame = null;
			}
		}

		return result;
	}

	public static void finishApp(boolean checkdirty, int exitcode) {
		if (closeInstance(checkdirty)) {
			Main.endApp(exitcode);
		}
	}

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		Logging.trace("Setting host from ", ConfigedMain.host, "to", host);
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

	public static void setOTP(String otp) {
		ConfigedMain.otp = otp;
	}

	public static void setUseSSO(boolean useSSO) {
		ConfigedMain.useSSO = useSSO;
	}
}
