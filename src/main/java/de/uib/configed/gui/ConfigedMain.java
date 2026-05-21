/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.app.Main;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.core.infrastructure.HostData;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.gui.data.DependenciesModel;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.features.terminal.TerminalController;
import de.uib.configed.gui.features.tree.AbstractGroupTree;
import de.uib.configed.gui.features.tree.ClientTree;
import de.uib.configed.gui.features.tree.GroupNode;
import de.uib.configed.gui.features.tree.GroupTreeTransferHandler;
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.gui.share.WindowsPositionManager;
import de.uib.configed.gui.share.swing.ButtonTabComponent;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.userprefs.UserPreferences;

public class ConfigedMain {
	private static final Pattern backslashPattern = Pattern.compile("[\\[\\]\\s]", Pattern.UNICODE_CHARACTER_CLASS);

	private static final int ICON_COLUMN_MAX_WIDTH = 100;

	private static MainFrame mainFrame;
	private static LoginDialog loginDialog;

	private static EditingTarget editingTarget = EditingTarget.CLIENTS;

	private OpsiServiceNOMPersistenceController persistenceController;

	private DependenciesModel dependenciesModel;

	private List<String> selectedClients = new ArrayList<>();

	private Set<String> clientsFilteredByTree = new HashSet<>();

	private HostInfo hostInfo = new HostInfo();

	// collection of retrieved software audit and hardware maps

	private ClientTablePanel clientTablePanel;

	private ClientSearch clientSearch;

	private ClientTree clientTree;
	private ProductTree productTree;

	private DepotsList depotsList;
	private Map<String, Map<String, Object>> depots;
	private String depotRepresentative;

	private DepotListSelectionListener depotListSelectionListener;

	public enum EditingTarget {
		CLIENTS, DEPOTS, SERVER, DASHBOARD, OPSI_MODULES, HEALTH_CHECK, LICENSE_MANAGEMENT
	}
	// with this enum type we build a state model, which target shall be edited

	private ConnectedHostsManager connectedHostsManager;

	private InitialDataLoader initialDataLoader;

	public ConfigedMain() {
		Logging.info(this, "ConfigedMain constructor called");
		TerminalController.setConfigedMain(this);
	}

	public static MainFrame getMainFrame() {
		return mainFrame;
	}

	public static JFrame getVisibleFrame() {
		return mainFrame != null ? mainFrame : loginDialog;
	}

	public static LoginDialog getLoginDialog() {
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
		// clientTablePanel.getClientTable().updateModel(buildClientListTableModel(true));
		clientTablePanel.initColumnNames();

		// clientTablePanel.getClientTable().initSortKeys();

		startMainFrame(this, clientTablePanel, depotsList, clientTree, productTree);

		connectTreesWithTables();

		initTabComponents();

		initialTreeActivation();

		registerMessagebusListeners();

		if (Messagebus.getInstance().getWebSocket().isOpen()) {
			// Fake opening event on registering listener since this listener
			// does not know yet if it's open
			mainFrame.getMainPanelManager().getHostsStatusPanel().onOpen(null);
		} else {
			Logging.warning(this, "Messagebus is not open, but should be on start");
		}

		Logging.debug(this, "initialTreeActivation");

		mainFrame.getMainPanelManager().getClientConfiguration().getClientInfoPanel().updateClientCheckboxText();
	}

	private void connectTreesWithTables() {
		GroupTreeTransferHandler clientTransferHandler = new GroupTreeTransferHandler(clientTree);
		clientTree.setTransferHandler(clientTransferHandler);
		// clientTablePanel.getClientTable().setTransferHandler(clientTransferHandler);

		GroupTreeTransferHandler productTransferHandler = new GroupTreeTransferHandler(productTree);
		productTree.setTransferHandler(productTransferHandler);
		mainFrame.getMainPanelManager().getClientConfiguration().getPanelLocalbootProductSettings().getProductTable()
				.setTransferHandler(productTransferHandler);
		mainFrame.getMainPanelManager().getClientConfiguration().getPanelNetbootProductSettings().getProductTable()
				.setTransferHandler(productTransferHandler);
	}

	public ProductTree getProductTree() {
		return productTree;
	}

	public void initTabComponents() {
		ButtonTabComponent depotComp = (ButtonTabComponent) mainFrame.getMainPanelManager().getTabbedPane()
				.getTabComponentAt(0);
		depotComp.showButton(depots.size() != depotsList.getSelectedValuesList().size());

		ButtonTabComponent clientComp = (ButtonTabComponent) ConfigedMain.getMainFrame().getMainPanelManager()
				.getTabbedPane().getTabComponentAt(1);
		clientComp.showButton(clientTree.getSelectionPaths() == null
				|| !ClientTree.ALL_CLIENTS_NAME.equals(clientTree.getSelectionPath().getLastPathComponent().toString())
				|| clientTree.getSelectionPaths().length > 1);

		ButtonTabComponent productComp = (ButtonTabComponent) ConfigedMain.getMainFrame().getMainPanelManager()
				.getTabbedPane().getTabComponentAt(2);
		productComp.showButton(productTree.getSelectionPaths() == null
				|| !Configed.getResourceValue("ProductTree.allProducts")
						.equals(productTree.getSelectionPath().getLastPathComponent().toString())
				|| productTree.getSelectionPaths().length > 1);
	}

	public void registerMessagebusListeners() {
		Messagebus.getInstance().getWebSocket().registerListener(connectedHostsManager);
		Messagebus.getInstance().getWebSocket().registerListener(mainFrame.getMainPanelManager().getHostsStatusPanel());
		// Messagebus.getInstance().getWebSocket().registerListener(clientTablePanel.getClientTable());
		Messagebus.getInstance().getWebSocket()
				.registerListener(mainFrame.getMainPanelManager().getClientConfiguration().getProductPageManager());
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
		ServerActionManager.initData(this, persistenceController);
		Messagebus.initMessagebus(this);

		clientSearch = new ClientSearch();

		initialDataLoader = new InitialDataLoader(this);
		initialDataLoader.execute();
	}

	protected void preloadData() {
		if (depotRepresentative == null) {
			depotRepresentative = persistenceController.getDataServices().hostInfoCollections.getConfigServer();
		}

		persistenceController.getDataServices().depot.setDepot(depotRepresentative);

		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		executor.runInParallel(() -> {
			persistenceController.getDataServices().product.retrieveProductIdsAndDefaultStatesPD();
			persistenceController.getDataServices().product.retrieveAllProductPropertyDefinitionsPD();
			persistenceController.getDataServices().product.retrieveAllProductDependenciesPD();
		});
		executor.runInParallel(() -> persistenceController.getDataServices().product
				.retrieveProductOnClientsDisplayFieldsLocalbootProducts());
		executor.runInParallel(() -> persistenceController.getDataServices().group.retrieveAllGroupsPD());
		executor.runInParallel(() -> persistenceController.getDataServices().group.retrieveAllObject2GroupsPD());
		executor.runInParallel(
				() -> persistenceController.getDataServices().product.retrieveDepotProductPropertiesPD());
		executor.waitForCompletion();

		ExtraFrameController.reloadDialogs();
	}

	public void toggleColumn(String column) {
		Map<String, Boolean> fields = persistenceController.getDataServices().host.getHostDisplayFields();
		fields.put(column, !fields.get(column));
		setRebuiltClientListTableModel(true, false);

		// We need to make first selected visible again after resetting sortKeys
		// clientTablePanel.getClientTable().moveToFirstSelected();

		clientTablePanel.initColumnNames();
	}

	public void handleGroupActionRequest() {
		if (!persistenceController.getDataServices().module.isOpsiModuleActive(OpsiModule.LOCAL_IMAGING)) {
			Logging.error(this,
					"this should not happen: group actions are not available since the module \"local_imaging\" is not available");
		} else {
			ExtraFrameController.startGroupActionFrame(this);
		}
	}

	public static boolean setEditingTarget(EditingTarget newEditingTarget) {
		Logging.info("setEditingTarget ", newEditingTarget);

		if (!ChangedDataManager.checkSaveAll(true)) {
			Logging.info("stop changing editingTarget, unsaved data");
			return false;
		}

		if (newEditingTarget == editingTarget) {
			Logging.info("stop setting editingTarget, it remains the same");
			return false;
		}

		if (mainFrame.showPanel(newEditingTarget)) {
			editingTarget = newEditingTarget;
		}

		return true;
	}

	public void actOnListSelection() {
		Logging.info(this, "actOnListSelection");

		Logging.checkErrorList();

		Logging.info(this, "ListSelectionListener valueChanged getSelectedRowCount() ",
				clientTablePanel.getTableComponent().model.getSelectedRows().size());

		Set<String> clientsSelectedInTable = getSelectedSet();
		Logging.info(this, "setSelectedClients clientNames size ", clientsSelectedInTable.size());

		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTY_STATES.toString());

		Logging.info(this, "setSelectedClientsArray ", clientsSelectedInTable.size());
		Logging.info(this, "selectedClients was before ", selectedClients.size());

		selectedClients = new ArrayList<>(clientsSelectedInTable);

		clientTree.produceActiveParents();

		mainFrame.getMainPanelManager().getClientConfiguration().stateChanged(null);

		hostInfo.resetValues();

		updateHostInfo();

		mainFrame.getMainPanelManager().getClientConfiguration().getClientInfoPanel()
				.setClientInfoEditing(selectedClients.size() == 1, selectedClients.isEmpty());

		if (selectedClients.size() == 1) {
			mainFrame.getMainPanelManager().getClientConfiguration().getClientInfoPanel()
					.setClientID(selectedClients.get(0));
		} else {
			mainFrame.getMainPanelManager().getClientConfiguration().getClientInfoPanel().setClientID("");
		}

		hostInfo.resetGui();

		Logging.info(this, "actOnListSelection update hosts status selectedClients ", selectedClients.size(),
				" as well as ", clientTablePanel.getTableComponent().model.getSelectedRows().size());

		mainFrame.getMainPanelManager().getHostsStatusPanel().updateValues(
				clientTablePanel.getTableComponent().model.getSelectedRows().size(), selectedClients, hostInfo);

		clientTree.updateSelectedObjectsInTable();
	}

	private void updateHostInfo() {
		Map<String, HostInfo> pcinfos = persistenceController.getDataServices().hostInfoCollections
				.getMapOfPCInfoMaps();

		Logging.info(this, "updateHostInfo, produce hostInfo  selectedClients.length ", selectedClients.size());

		if (!selectedClients.isEmpty() && !pcinfos.isEmpty()) {
			hostInfo.setValues(pcinfos.get(selectedClients.get(0)).getMap());

			Logging.debug(this, "updateHostInfo, produce hostInfo first selClient ", selectedClients.get(0));
			Logging.debug(this, "updateHostInfo, produce hostInfo  ", hostInfo);

			HostInfo secondInfo = new HostInfo();

			selectedClients.stream().skip(1).forEach((String clientId) -> {
				secondInfo.setValues(pcinfos.get(clientId).getMap());
				hostInfo.combineWith(secondInfo);
			});
		}
	}

	private void initDepots() {
		// create depotsList
		depotsList = new DepotsList(this);

		Logging.info(this, "create depotsListSelectionListener");
		depotListSelectionListener = new DepotListSelectionListener(this, depotsList, initialDataLoader);
		depotsList.addListSelectionListener(depotListSelectionListener);

		fetchDepots();

		depotsList.setInfo(depots);
		List<String> oldSelectedDepots = List.of(backslashPattern
				.matcher(Configed.getSavedStates().getProperty("selectedDepots",
						persistenceController.getDataServices().hostInfoCollections.getConfigServer()))
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
		if (WindowsPositionManager
				.isOnAnyScreen(WindowsPositionManager.getWindowBounds(WindowsPositionManager.MAIN_WINDOW))) {
			WindowsPositionManager.loadWindowProperties(mainFrame, WindowsPositionManager.MAIN_WINDOW);
		} else {
			locateFrame();
		}

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
	public static void setupLoginDialog(HostData hostData) {
		loginDialog = new LoginDialog(hostData);
	}

	public void setPersistenceController(OpsiServiceNOMPersistenceController persistenceController) {
		this.persistenceController = persistenceController;
	}

	public DependenciesModel getDependenciesModel() {
		return dependenciesModel;
	}

	private TableModel buildClientListTableModel(boolean rebuildTree) {
		Logging.debug(this, "buildPclistTableModel rebuildTree ", rebuildTree);

		Logging.info(this, " producePcListForDepots ", depotsList.getSelectedValuesList(),
				" running with allowedClients ", getAllowedClients());

		// We need to create a copy since we manipulate the set later
		Set<String> clientsForDepots = new TreeSet<>(persistenceController.getDataServices().hostInfoCollections
				.getClientsForDepots(depotsList.getSelectedValuesList(), getAllowedClients()));

		if (mainFrame != null) {
			mainFrame.getMainPanelManager().getHostsStatusPanel().updateAllClientsCount(clientsForDepots.size());
			clientTablePanel.updateTable();
		}

		Logging.debug(this, " unfilteredList ");

		Logging.info(this, "buildPclistTableModel, rebuildTree  ", rebuildTree);

		if (rebuildTree) {
			rebuildTree();
		}

		clientsForDepots.retainAll(clientsFilteredByTree);

		Logging.info(this, " clientTable isFilteredMode ", clientTablePanel.isFilteredMode());

		if (clientTablePanel.isFilteredMode()) {
			Logging.info(this, "buildPclistTableModel with filterCLientList, number of selected pcs ",
					selectedClients.size());

			// selected clients that are in the pclist0
			clientsForDepots.retainAll(selectedClients);
		}

		clientTablePanel.getTableComponent()
				.dispatch(new GenericTableViewMsg.ChangeOriginalSnapshot(getOriginalSnapshot(clientsForDepots)));

		// building table model
		return buildTableModel(clientsForDepots);
	}

	private TableModel buildTableModel(Set<String> clientIds) {
		DefaultTableModel model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};

		Map<String, HostInfo> pcinfos = persistenceController.getDataServices().hostInfoCollections
				.getMapOfPCInfoMaps();

		List<String> displayFields = new ArrayList<>();
		for (Entry<String, Boolean> entry : persistenceController.getDataServices().host.getHostDisplayFields()
				.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {
				model.addColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel." + entry.getKey()));
				displayFields.add(entry.getKey());
			}
		}

		UserPreferences.set(UserPreferences.CLIENTS_TABLE_DISPLAY_FIELDS, String.join(",", displayFields));

		Logging.info(this, "buildPclistTableModel host_displayFields ",
				persistenceController.getDataServices().host.getHostDisplayFields());

		for (String clientId : clientIds) {
			HostInfo pcinfo = pcinfos.getOrDefault(clientId, new HostInfo());

			Map<String, Object> rowmap = pcinfo.getDisplayRowMap();
			rowmap.put(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL,
					persistenceController.getDataServices().host.getSessionInfo().getOrDefault(clientId, ""));
			rowmap.put(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, isHostConnected(clientId));

			List<Object> rowItems = persistenceController.getDataServices().host.getHostDisplayFields().entrySet()
					.stream().filter(entry -> Boolean.TRUE.equals(entry.getValue()))
					.map(entry -> rowmap.get(entry.getKey())).toList();

			model.addRow(rowItems.toArray());
		}

		Logging.info(this, "buildPclistTableModel, model column count ", model.getColumnCount());

		// Clear the selected clients because we have a new model with no selected clients
		selectedClients.clear();
		if (mainFrame != null) {
			// Update the info on the bottom with new data
			mainFrame.getMainPanelManager().getHostsStatusPanel().updateSelectedClientsCount(selectedClients.size(),
					clientIds.size());
		}
		return model;
	}

	private List<Map<String, Object>> getOriginalSnapshot(Set<String> clientIds) {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, HostInfo> pcinfos = persistenceController.getDataServices().hostInfoCollections
				.getMapOfPCInfoMaps();

		Map<String, Boolean> setOfDisplayFields = persistenceController.getDataServices().host.getHostDisplayFields();
		for (String clientId : clientIds) {
			HostInfo pcinfo = pcinfos.getOrDefault(clientId, new HostInfo());

			Map<String, Object> rowmap = pcinfo.getDisplayRowMap();
			rowmap.put(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL,
					persistenceController.getDataServices().host.getSessionInfo().getOrDefault(clientId, ""));
			rowmap.put(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, isHostConnected(clientId));

			Map<String, Object> filteredMap = rowmap.entrySet().stream()
					.filter(entry -> setOfDisplayFields.get(entry.getKey()).equals(Boolean.TRUE))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

			result.add(filteredMap);
		}

		return result;
	}

	private void rebuildTree() {
		clientTree.clear();
		clientTree.build();
	}

	public void setClients(Collection<String> clientNames) {
		Logging.info(this, "setClients ", clientNames);
		clientTablePanel.setSelectedValues(clientNames);
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
			clientsFilteredByTree.addAll(filterIds);
		}
		setRebuiltClientListTableModel(true, false, selectedIds);
	}

	public List<String> getSelectedClients() {
		return selectedClients;
	}

	public void setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree) {
		Logging.info(this, "setRebuiltClientListTableModel, we have selected Set : ", getSelectedSet());

		setRebuiltClientListTableModel(restoreSortKeys, rebuildTree, getSelectedSet());
	}

	private void setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree,
			Set<String> selectValues) {
		Logging.info(this,
				"setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree, Set selectValues)  : ",
				restoreSortKeys, ", ", rebuildTree, ",  selectValues.size() ", Logging.getSize(selectValues));

		Logging.info(this, "setRebuiltClientListTableModel save sort keys ");
		// List<Pair<String, SortOrder>> sortKeyNames = clientTablePanel.getClientTable().getSortedNames();

		Logging.info(this, " setRebuiltClientListTableModel--- set model new, selected ",
				clientTablePanel.getTableComponent().model.getSelectedRows().size());

		TableModel tm = buildClientListTableModel(rebuildTree);
		Logging.info(this, "setRebuiltClientListTableModel --- got model selected ",
				clientTablePanel.getTableComponent().model.getSelectedRows().size());

		// int[] columnWidths = ConfigedUtilityMethods.getTableColumnWidths(clientTablePanel.getClientTable());

		// We want to deactivate the listener here, since we want it to react only later
		// when
		// the values are selected. We only reactivate the listener if it was active
		// before.
		// boolean listenerDeactivated = clientTablePanel.deactivateListSelectionListener();
		// clientTablePanel.getClientTable().updateModel(tm);
		// if (listenerDeactivated) {
		// clientTablePanel.activateListSelectionListener();
		// }

		// ConfigedUtilityMethods.setTableColumnWidths(clientTablePanel.getClientTable(), columnWidths);

		Logging.debug(this, " --- model set  ");

		// setSelectionPanelCols();

		// if (restoreSortKeys) {
		// 	clientTablePanel.getClientTable().setSortedByNames(sortKeyNames);
		// }

		Logging.info(this, "setRebuiltClientListTableModel set selected values in setRebuiltClientListTableModel() ",
				Logging.getSize(selectValues));
		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel",
				clientTablePanel.getTableComponent().model.getSelectedRows().size());

		// clientTablePanel.restoreFilter();
		// did lose the selection since last setting
		// clientTablePanel.setSelectedValues(selectValues);

		mainFrame.getMainPanelManager().getHostsStatusPanel().updateValues(
				clientTablePanel.getTableComponent().model.getRows().size(),
				selectValues != null ? new ArrayList<>(selectValues) : new ArrayList<>(), hostInfo);

		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel ",
				Logging.getSize(getSelectedSet()));

		Logging.info(this, "setRebuiltClientListTableModel");
	}

	public Set<String> getDepotsOfSelectedClients() {
		Map<String, String> mapPcBelongsToDepot = persistenceController.getDataServices().hostInfoCollections
				.getMapPcBelongsToDepot();

		return selectedClients.stream().map(mapPcBelongsToDepot::get).filter(Objects::nonNull)
				.collect(Collectors.toCollection(TreeSet::new));
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
		}
	}

	public void treeClientsSelectAction(TreePath[] selTreePaths) {
		clientTablePanel.setFilterMark(false);

		clientsFilteredByTree.clear();

		if (selTreePaths == null || selTreePaths.length == 0) {
			setRebuiltClientListTableModel(true, false, Set.of());
			return;
		}

		Set<String> selectedValues = new HashSet<>();
		List<DefaultMutableTreeNode> filteredNodes = clientTree
				.filterMostSpecificNodes(clientTree.extractNodes(selTreePaths));

		for (DefaultMutableTreeNode node : filteredNodes) {
			if (node.getAllowsChildren()) {
				AbstractGroupTree.addAllDescendants(node, clientsFilteredByTree);
			} else {
				String value = node.getUserObject().toString();
				clientsFilteredByTree.add(value);
				selectedValues.add(value);
			}
		}

		if (selTreePaths.length == 1) {
			treeClientsSelectAction(selTreePaths[0]);
		} else {
			Logging.info(this, "treeClientsSelectAction selTreePaths length: ", selTreePaths.length);
			setRebuiltClientListTableModel(true, false, selectedValues);
		}
	}

	private void initTree() {
		Logging.debug(this, "initTree");

		clientTree = new ClientTree(this);
		productTree = new ProductTree(this);
		persistenceController.getDataServices().hostInfoCollections.setTree(clientTree);
	}

	private void setGroupByTree(DefaultMutableTreeNode node) {
		Logging.info(this, "setGroupByTree, node ", node);

		if (node == null) {
			Logging.info(this, "Target node not found — possibly deleted or not selected. Defaulting to '",
					ClientTree.ALL_CLIENTS_NAME, "'");
			node = clientTree.getGroupNode(ClientTree.ALL_CLIENTS_NAME);
		}

		clientTree.initActiveParents();
		// Get all leaves from the node which should be a group
		clientsFilteredByTree.clear();
		AbstractGroupTree.addAllDescendants(node, clientsFilteredByTree);

		clientTree.repaint();
	}

	private void activateGroupByTree(boolean preferringOldSelection, DefaultMutableTreeNode node) {
		Logging.info(this, "activateGroupByTree, node: ", node);

		setGroupByTree(node);
		Set<String> selectValues = null;
		// intended for reload, we cancel activating group
		if (preferringOldSelection && !getSelectedSet().isEmpty()) {
			selectValues = getSelectedSet();
		}

		setRebuiltClientListTableModel(true, false, selectValues);
		// with this, a selected client remains selected (but in bottom line, the group
		// seems activated, not the client)

		// since we select based on the tree view we disable the filter
		deactivateFilter();
	}

	public void deactivateFilter() {
		Logging.info(this, "deactivate filter", clientTablePanel.isFilteredMode());
		if (clientTablePanel.isFilteredMode()) {
			setRebuiltClientListTableModel(true, false);
		}
	}

	public boolean checkSynchronous(Set<String> depots) {
		if (depots.size() > 1 && !persistenceController.getDataServices().depot.areDepotsSynchronous(depots)) {
			JOptionPane.showMessageDialog(mainFrame, Configed.getResourceValue("ConfigedMain.notSynchronous.text"),
					Configed.getResourceValue("ConfigedMain.notSynchronous.title"), JOptionPane.OK_OPTION);

			return false;
		}

		return true;
	}

	public void setDepotRepresentative() {
		Logging.debug(this, "setDepotRepresentative");

		List<String> selectedDepots = getSelectedDepots();
		String oldRepresentative = depotRepresentative;

		String configServer = persistenceController.getDataServices().hostInfoCollections.getConfigServer();
		Set<String> clientDepots = selectedClients.isEmpty() ? Set.of() : getDepotsOfSelectedClients();

		Logging.info(this, "Selected depots: " + selectedDepots);
		Logging.info(this, "Depots of selected clients: " + clientDepots);

		String newRepresentative = Stream
				.concat(selectedDepots.stream().filter(clientDepots::contains), Stream.of(configServer)).findFirst()
				.orElse(configServer);

		Logging.debug(this, "Old representative: " + oldRepresentative + ", new: " + newRepresentative);

		if (!Objects.equals(oldRepresentative, newRepresentative)) {
			depotRepresentative = newRepresentative;
			Logging.info(this, "Depot representative changed to " + depotRepresentative);

			persistenceController.getDataServices().depot.setDepot(depotRepresentative);
			persistenceController.reloadData(ReloadEvent.DEPOT_CHANGE_RELOAD.toString());
		} else {
			Logging.debug(this, "Depot representative unchanged.");
		}
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
			return new String[0];
		}

		return depots.keySet().toArray(new String[0]);
	}

	private void fetchDepots() {
		Logging.info(this, "fetchDepots");

		Logging.debug(this, "fetchDepots sorted depots ",
				persistenceController.getDataServices().hostInfoCollections.getDepotNamesList());

		depots = persistenceController.getDataServices().hostInfoCollections.getDepots();
		List<String> oldSelection = depotsList.getSelectedValuesList();

		// Setting the list data will remove old selection. To prevent doing events
		// twice
		// we set the flag that value is adjusting, because we will set the selected
		// values again.
		// Both actions will then be united into one event only
		depotsList.setValueIsAdjusting(true);
		depotsList.setListData(persistenceController.getDataServices().hostInfoCollections.getDepotNamesList());
		depotsList.setSelectedValues(oldSelection);
		depotsList.setValueIsAdjusting(false);

		Logging.debug(this, "selected after fetch ", getSelectedDepots().size());
	}

	public void refreshClientListKeepingGroup() {
		// dont do anything if we did not finish another thread for this
		String oldGroupSelection = getSelectedGroupName();
		Logging.info(this, " refreshClientListKeepingGroup oldGroupSelection ", oldGroupSelection);

		Map<String, Map<String, Object>> nodes = clientTree.getExpandedAndSelectedNodes();
		setRebuiltClientListTableModel(true, true);
		activateGroup(true, oldGroupSelection);
		clientTree.expandAndSelectNodes(nodes);
	}

	private void saveSelectedGroupName() {
		String groupName = getSelectedGroupName();
		if (groupName != null) {
			Configed.getSavedStates().setProperty("groupname", groupName);
			Logging.info(this, "saveSelectedGroupName ", groupName);
		} else {
			Logging.info(this, "saveSelectedGroupName, no group selected");
			Configed.getSavedStates().remove("groupname");
		}
	}

	public String getSelectedGroupName() {
		if (clientTree.getSelectionPath() == null) {
			return null;
		} else {
			String groupName = getGroupNameForPath(clientTree.getSelectionPath());

			for (int i = 1; i < clientTree.getSelectionCount(); i++) {
				TreePath path = clientTree.getSelectionPaths()[i];
				String groupName2 = getGroupNameForPath(path);
				if (!groupName.equals(groupName2)) {
					Logging.info(this, "getSelectedGroupName, multiple groups selected: ", groupName, " and ",
							groupName2);
					// multiple groups selected
					return null;
				}
			}
			return groupName;
		}
	}

	private static String getGroupNameForPath(TreePath path) {
		if (((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()) {
			return ((DefaultMutableTreeNode) path.getLastPathComponent()).toString();
		} else {
			return ((DefaultMutableTreeNode) path.getPathComponent(path.getPathCount() - 2)).toString();
		}
	}

	public void reload() {
		mainFrame.activateLoadingPane(Configed.getResourceValue("MainFrame.jMenuFileReload") + " ...");
		SwingUtilities.invokeLater(this::reloadData);
	}

	private void reloadData() {
		if (!ChangedDataManager.checkSaveAll(true)) {
			mainFrame.deactivateLoadingPane();
			return;
		}

		Set<String> selValuesList = getSelectedSet();
		// Logging.info(this, "reloadData, selValuesList.size ", clientTablePanel.getClientTable().getSelectedRowCount());

		Set<String> selectedLocalbootProducts = mainFrame.getMainPanelManager().getClientConfiguration()
				.getPanelLocalbootProductSettings().getProductTable().getSelectedIDs();
		Set<String> selectedNetbootProducts = mainFrame.getMainPanelManager().getClientConfiguration()
				.getPanelNetbootProductSettings().getProductTable().getSelectedIDs();
		clientTablePanel.deactivateListSelectionListener();
		depotsList.removeListSelectionListener(depotListSelectionListener);

		persistenceController.reloadData(CacheIdentifier.ALL_DATA.toString());
		persistenceController.getDataServices().userRoles.checkConfigurationPD();

		preloadData();

		mainFrame.resetData();

		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTY_STATES.toString());

		mainFrame.getMainPanelManager().getClientConfiguration().getClientInfoPanel().updateClientCheckboxText();

		Logging.info(this, " in reload, we are in thread ", Thread.currentThread());

		productTree.reInitTree();
		clientTree.reInitTree();
		treeClientsSelectAction(clientTree.getSelectionPaths());
		fetchDepots();

		// if depot selection changed, we adapt the clients
		List<String> clientsLeft = getClientSelectionBasedOnDepotSelection(selValuesList);

		Logging.info(this, "reloadData, selected clients now ", Logging.getSize(clientsLeft));

		Logging.debug(this, " reset the values, particularly in list ");

		clientTablePanel.activateListSelectionListener();
		clientTablePanel.restoreFilter();
		clientTablePanel.setSelectedValues(clientsLeft);
		clientTree.produceActiveParents();
		clientTree.updateSelectedObjectsInTable();

		mainFrame.getMainPanelManager().getClientConfiguration().getPanelLocalbootProductSettings().getProductTable()
				.setPendingSelection(selectedLocalbootProducts);
		mainFrame.getMainPanelManager().getClientConfiguration().getPanelNetbootProductSettings().getProductTable()
				.setPendingSelection(selectedNetbootProducts);
		productTree.produceActiveParents();
		productTree.updateSelectedObjectsInTable();

		depotsList.addListSelectionListener(depotListSelectionListener);

		Logging.info(this, "reloadData, selected clients now, after resetting ", Logging.getSize(selectedClients));
		mainFrame.reloadServerConsoleMenu();

		mainFrame.deactivateLoadingPane();

		updatePage();

		initTabComponents();

		ExtraFrameController.resetCompleteWinProductsPanel();
	}

	private static void updatePage() {
		// We want to reset and reload the page that is being shown now...
		EditingTarget t = editingTarget;
		editingTarget = null;
		setEditingTarget(t);

		// We need to update the client configuration since it will not be done
		// automatically in the method setEditingTarget!
		if (t == EditingTarget.CLIENTS) {
			mainFrame.getMainPanelManager().getClientConfiguration().stateChanged(null);
		}
	}

	public ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
	}

	public void initialTreeActivation() {
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

	public void reloadHosts() {
		mainFrame.activateLoadingCursor();
		List<String> clientsLeft = getClientSelectionBasedOnDepotSelection(getSelectedSet());
		persistenceController.reloadData(ReloadEvent.HOST_DATA_RELOAD.toString());
		refreshClientListKeepingGroup();
		updateHostInfo();

		hostInfo.resetGui();
		// clientTablePanel.restoreFilter();
		this.selectedClients = clientsLeft;
		mainFrame.getMainPanelManager().getHostsStatusPanel().updateValues(
				clientTablePanel.getTableComponent().model.getRows().size(), this.selectedClients, hostInfo);
		clientTablePanel.setSelectedValues(this.selectedClients);

		mainFrame.deactivateLoadingCursor();
	}

	public Set<String> getSelectedSet() {
		Set<String> result = new HashSet<>();
		List<RowData> rows = clientTablePanel.getTableComponent().model.getRows();
		Set<Integer> selectedRows = clientTablePanel.getTableComponent().model.getSelectedRows();
		for (int i = 0; i < rows.size(); i++) {
			if (selectedRows.contains(i)) {
				RowData row = rows.get(i);
				String clientName = row.getValue(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, String.class);
				result.add(clientName);
			}
		}
		return result;
		// return clientTablePanel.getClientTable().getSelectedSet();
	}

	private List<String> getClientSelectionBasedOnDepotSelection(Set<String> selValuesList) {
		return selValuesList.stream().filter(client -> depotsList.getSelectedValuesList().contains(
				persistenceController.getDataServices().hostInfoCollections.getMapPcBelongsToDepot().get(client)))
				.toList();
	}

	public void invertSelection() {
		// List<String> previouslySelectedClients = getSelectedClients();
		// List<String> clientsToSelect = new ArrayList<>();
		// int rowCount = clientTablePanel.getTableModel().getRowCount();
		// for (int i = 0; i < rowCount; i++) {
		// 	String clientName = (String) clientTablePanel.getTableModel().getValueAt(i, 0);
		// 	if (!previouslySelectedClients.contains(clientName)) {
		// 		clientsToSelect.add(clientName);
		// 	}
		// }
		// setClients(clientsToSelect);
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

			// We set editing target because after restarting the configed, we will show
			// this panel!
			editingTarget = EditingTarget.CLIENTS;
		}

		boolean result = true;

		if (mainFrame != null) {
			result = mainFrame.getMainPanelManager().checkSavedLicenses();
			if (result) {
				mainFrame.setVisible(false);
				mainFrame.dispose();
				mainFrame = null;
			}
		}

		return result;
	}

	public void saveAndQuit() {
		Logging.info(this, "saveAndQuit");
		saveSelectedGroupName();

		finishApp(!persistenceController.getDataServices().userRoles.isGlobalReadOnly(), 0);
	}

	public static void finishApp(boolean checkdirty, int exitcode) {
		if (closeInstance(checkdirty)) {
			Main.endApp(exitcode);
		}
	}
}
