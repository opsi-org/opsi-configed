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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.java_websocket.handshake.ServerHandshake;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.Main;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.groupaction.ActivatedGroupModel;
import de.uib.configed.groupaction.FGroupActions;
import de.uib.configed.gui.ClientSelectionDialog;
import de.uib.configed.gui.ClientTable;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.FShowListWithComboSelect;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.HostsStatusPanel;
import de.uib.configed.gui.LoginDialog;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.gui.NewClientDialog;
import de.uib.configed.gui.SavedSearchesDialog;
import de.uib.configed.guidata.DependenciesModel;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.productaction.FCompleteWinProducts;
import de.uib.configed.serverconsole.CommandControlDialog;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.tree.GroupNode;
import de.uib.configed.tree.ProductTree;
import de.uib.configed.type.DateExtendedByVars;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.licenses.LicenseUsageEntry;
import de.uib.messagebus.Messagebus;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.DataChangedKeeper;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.CheckedDocument;
import de.uib.utils.swing.FEditText;
import de.uib.utils.table.gui.BooleanIconTableCellRenderer;
import de.uib.utils.userprefs.UserPreferences;

public class ConfigedMain implements MessagebusListener {
	private static final Pattern backslashPattern = Pattern.compile("[\\[\\]\\s]", Pattern.UNICODE_CHARACTER_CLASS);

	private static final int ICON_COLUMN_MAX_WIDTH = 100;

	private static MainFrame mainFrame;
	private static LoginDialog loginDialog;

	private static String host;
	private static String user;
	private static String password;
	private static String otp;

	private static EditingTarget editingTarget = EditingTarget.CLIENTS;

	private OpsiServiceNOMPersistenceController persistenceController;

	private GeneralDataChangedKeeper generalDataChangedKeeper;
	private ClientInfoDataChangedKeeper clientInfoDataChangedKeeper;
	private GeneralDataChangedKeeper hostConfigsDataChangedKeeper;

	private DependenciesModel dependenciesModel;

	private List<String> selectedClients = new ArrayList<>();
	private List<String> saveSelectedClients;

	private Set<String> clientsFilteredByTree = new HashSet<>();
	private ActivatedGroupModel activatedGroupModel;

	private boolean anyDataChanged;

	private String clientInDepot;
	private HostInfo hostInfo = new HostInfo();

	private FTextArea fAskSaveChangedText;
	private FTextArea fAskSaveProductConfiguration;

	private SavedSearchesDialog savedSearchesDialog;
	private ClientSelectionDialog clientSelectionDialog;

	private UpdateCollection updateCollection = new UpdateCollection();

	private Set<String> allowedClients;

	// collection of retrieved software audit and hardware maps

	private ClientTable clientTable;

	private ClientTree clientTree;
	private ProductTree productTree;

	private DepotsList depotsList;
	private Map<String, Map<String, Object>> depots;
	private String depotRepresentative;

	private List<JFrame> allFrames;

	private FGroupActions groupActionFrame;
	private FCompleteWinProducts productActionFrame;

	private int clientCount;

	private Map<String, String> sessionInfo = new HashMap<>();

	private Map<String, String> logfiles = new HashMap<>();

	public enum EditingTarget {
		CLIENTS, DEPOTS, SERVER, DASHBOARD, OPSI_MODULES, HEALTH_CHECK, LICENSE_MANAGEMENT
	}
	// with this enum type we build a state model, which target shall be edited

	private int buildPclistTableModelCounter;

	private int reloadCounter;

	private Messagebus messagebus;

	private Set<String> connectedHostsByMessagebus;

	private CommandControlDialog commandControlDialog;
	private NewClientDialog newClientDialog;

	private InitialDataLoader initialDataLoader;

	public ConfigedMain(String host, String user, String password, String otp) {
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

		Logging.registerConfigedMain(this);
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

		initDepots();
		initTree();

		allFrames = new ArrayList<>();

		// create client selection panel
		clientTable = new ClientTable(this);

		clientTable.setModel(buildClientListTableModel(true));
		setSelectionPanelCols();

		clientTable.initSortKeys();

		startMainFrame(this, clientTable, depotsList, clientTree, productTree);

		activatedGroupModel = new ActivatedGroupModel(mainFrame.getHostsStatusPanel());

		initialTreeActivation();

		messagebus.getWebSocket().registerListener(this);
		messagebus.getWebSocket().registerListener(mainFrame.getHostsStatusPanel());

		if (messagebus.getWebSocket().isOpen()) {
			// Fake opening event on registering listener since this listener
			// does not know yet if it's open
			mainFrame.getHostsStatusPanel().onOpen(null);
		} else {
			Logging.warning(this, "Messagebus is not open, but should be on start");
		}

		anyDataChanged = false;

		Logging.debug(this, "initialTreeActivation");

		mainFrame.getClientConfiguration().getClientInfoPanel().updateClientCheckboxText();
	}

	private List<String> readLocallySavedServerNames() {
		List<String> result = new ArrayList<>();
		TreeMap<Timestamp, String> sortingmap = new TreeMap<>();
		File savedStatesLocation = null;
		// the following is nearly a double of initSavedStates

		boolean success = true;

		if (Configed.getSavedStatesLocationName() != null) {
			Logging.info(this, "trying to find saved states in ", Configed.getSavedStatesLocationName());

			savedStatesLocation = new File(Configed.getSavedStatesLocationName());
			savedStatesLocation.mkdirs();
			success = savedStatesLocation.setReadable(true);
		}

		if (!success) {
			Logging.warning(this, "cannot not find saved states in ", Configed.getSavedStatesLocationName());
		}

		if (Configed.getSavedStatesLocationName() == null || !success) {
			Logging.info(this, "searching saved states in ", Utils.getSavedStatesDefaultLocation());
			savedStatesLocation = new File(Utils.getSavedStatesDefaultLocation());
			savedStatesLocation.mkdirs();
		}

		Logging.info(this, "saved states location ", savedStatesLocation);

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

		Logging.info(this, "readLocallySavedServerNames  result ", result);

		return result;
	}

	public boolean initMessagebus() {
		if (messagebus == null) {
			messagebus = new Messagebus(this);
		}

		if (!messagebus.isConnected()) {
			try {
				Logging.info(this, "connecting to messagebus");
				messagebus.connect();
				Logging.info(this, "connected to messagebus");
			} catch (InterruptedException e) {
				Logging.error(this, e, "could not connect to messagebus");
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
				|| mainFrame.getClientConfiguration().getSelectedIndex() != 0) {
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
				|| mainFrame.getClientConfiguration().getSelectedIndex() != 0) {
			return;
		}

		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());

		SwingUtilities.invokeLater(this::refreshClientListKeepingGroup);
	}

	public Set<String> getConnectedClientsByMessagebus() {
		return connectedHostsByMessagebus;
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
		Logging.info(this, " we got persist ", persistenceController);
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

		initMessagebus();
	}

	protected void preloadData() {
		persistenceController.getModuleDataService().retrieveOpsiModules();

		if (depotRepresentative == null) {
			depotRepresentative = persistenceController.getHostInfoCollections().getConfigServer();
		}

		persistenceController.getDepotDataService().setDepot(depotRepresentative);

		persistenceController.getProductDataService().retrieveProductIdsAndDefaultStatesPD();

		persistenceController.getProductDataService().retrieveProductOnClientsDisplayFieldsNetbootProducts();
		persistenceController.getProductDataService().retrieveProductOnClientsDisplayFieldsLocalbootProducts();

		if (savedSearchesDialog != null) {
			savedSearchesDialog.resetModel();
		}

		// Load all group data in this method to only call one method!
		persistenceController.getGroupDataService().retrieveAllGroupsPD();
		persistenceController.getGroupDataService().retrieveAllObject2GroupsPD();

		Map<String, Map<String, String>> productGroups = persistenceController.getGroupDataService()
				.getProductGroupsPD();
		fillterPermittedProductGroups(productGroups.keySet());

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

	private void setColumnSessionInfo(boolean b) {
		boolean visible = persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		if (visible != b) {
			toggleColumn(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		}

		Logging.info(this, "setColumnSessionInfo ", b);
	}

	public void toggleColumn(String column) {
		boolean visible = persistenceController.getHostDataService().getHostDisplayFields().get(column);
		persistenceController.getHostDataService().getHostDisplayFields().put(column, !visible);

		setRebuiltClientListTableModel(false);
		clientTable.initSortKeys();

		// We need to make first selected visible again after resetting sortKeys
		clientTable.moveToFirstSelected();
	}

	public void handleGroupActionRequest() {
		if (persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.LOCAL_IMAGING)) {
			startGroupActionFrame();
		} else {
			Logging.error(this,
					"this should not happen: group actions are not available since the module \"local_imaging\" is not available");
		}
	}

	private void startGroupActionFrame() {
		Logging.info(this, "startGroupActionFrame clientsFilteredByTree ", activatedGroupModel.getAssociatedClients(),
				" active ", activatedGroupModel.isActive());

		if (!activatedGroupModel.isActive()) {
			FTextArea f = new FTextArea(mainFrame, Configed.getResourceValue("information"),
					Configed.getResourceValue("ConfigedMain.noGroupSelected"), true,
					new String[] { Configed.getResourceValue("buttonClose") }, 400, 200);

			f.setVisible(true);

			return;
		}

		if (groupActionFrame == null) {
			groupActionFrame = new FGroupActions(this);
			groupActionFrame.setSize(1000, 300);
			groupActionFrame.setLocationRelativeTo(ConfigedMain.getMainFrame());

			allFrames.add(groupActionFrame);
		}

		groupActionFrame.start();
	}

	public void startProductActionFrame() {
		Logging.info(this, "startProductActionFrame ");

		if (productActionFrame == null) {
			productActionFrame = new FCompleteWinProducts();
			productActionFrame.setLocationRelativeTo(ConfigedMain.getMainFrame());
			allFrames.add(productActionFrame);
		}

		productActionFrame.start();
	}

	public void setEditingTarget(EditingTarget newEditingTarget) {
		Logging.info(this, "setEditingTarget ", newEditingTarget);
		checkSaveAll(true);
		if (newEditingTarget == editingTarget) {
			Logging.info(this, "stop setting editingTarget, it remains the same");
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

		checkSaveAll(true);
		Logging.checkErrorList(mainFrame);

		Logging.info(this, "selectionPanel.getSelectedValues().size(): ", clientTable.getSelectedValues().size());

		// when initializing the program the frame may not exist
		if (mainFrame != null) {
			Logging.info(this, "ListSelectionListener valueChanged selectionPanel.isSelectionEmpty() ",
					clientTable.isSelectionEmpty());
			setSelectedClients(clientTable.getSelectedValues());

			clientInDepot = "";

			hostInfo.initialize();

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
			} else {
				mainFrame.getClientConfiguration().getClientInfoPanel().setClientID("");
			}

			hostInfo.resetGui();

			Logging.info(this, "actOnListSelection update hosts status selectedClients ", selectedClients.size(),
					" as well as ", clientTable.getSelectedValues().size());

			mainFrame.getHostsStatusPanel().updateValues(clientCount, selectedClients.size(),
					Utils.getListStringRepresentation(selectedClients, HostsStatusPanel.MAX_CLIENT_NAMES_IN_FIELD),
					clientInDepot);

			activatedGroupModel.setActive(selectedClients.isEmpty());
		}

		clientTree.updateSelectedObjectsInTable();
	}

	private void updateHostInfo() {
		Map<String, HostInfo> pcinfos = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps();

		Logging.info(this, "updateHostInfo, produce hostInfo  selectedClients.length ", selectedClients.size());

		if (!selectedClients.isEmpty()) {
			hostInfo.setBy(pcinfos.get(selectedClients.get(0)).getMap());

			Logging.debug(this, "updateHostInfo, produce hostInfo first selClient ", selectedClients.get(0));
			Logging.debug(this, "updateHostInfo, produce hostInfo  ", hostInfo);

			HostInfo secondInfo = new HostInfo();

			for (int i = 1; i < selectedClients.size(); i++) {
				secondInfo.setBy(pcinfos.get(selectedClients.get(i)).getMap());
				hostInfo.combineWith(secondInfo);
			}
		}
	}

	private void initDepots() {
		// create depotsList
		depotsList = new DepotsList(this);

		Logging.info(this, "create depotsListSelectionListener");
		ListSelectionListener depotsListSelectionListener = new ListSelectionListener() {
			private int counter;

			@Override
			public void valueChanged(ListSelectionEvent e) {
				counter++;
				Logging.info(this, "depotSelection event count  ", counter);

				if (!e.getValueIsAdjusting()) {
					depotsListValueChanged();
				}
			}
		};
		depotsList.addListSelectionListener(depotsListSelectionListener);
		// TODO: add right click manager for depots items to open context menu

		fetchDepots();

		depotsList.setInfo(depots);
		List<String> oldSelectedDepots = Arrays
				.asList(backslashPattern
						.matcher(Configed.getSavedStates().getProperty("selectedDepots",
								persistenceController.getHostInfoCollections().getConfigServer()))
						.replaceAll("").split(","));
		depotsList.setSelectedValues(oldSelectedDepots);
	}

	private static void startMainFrame(ConfigedMain configedMain, ClientTable clientTable, DepotsList depotsList,
			ClientTree clientTree, ProductTree productTree) {
		mainFrame = new MainFrame(configedMain, clientTable, depotsList, clientTree, productTree);
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

		if (otp != null) {
			loginDialog.setOTP(otp);
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
			mainFrame.getHostsStatusPanel().updateValues(clientCount, null, null, null);
			clientTable.updateTable();
		}

		return m;
	}

	private TableModel buildClientListTableModel(boolean rebuildTree) {
		Logging.debug(this, "buildPclistTableModel rebuildTree ", rebuildTree);

		Set<String> clientsForTableModel = produceClientSetForDepots(null);

		Logging.debug(this, " unfilteredList ");

		buildPclistTableModelCounter++;
		Logging.info(this, "buildPclistTableModel, counter ", buildPclistTableModelCounter, "   rebuildTree  ",
				rebuildTree);

		Set<String> permittedHostGroups = null;
		if (!persistenceController.getUserRolesConfigDataService().isAccessToHostgroupsOnlyIfExplicitlyStatedPD()) {
			Logging.info(this, "buildPclistTableModel not full hostgroups permission");
			permittedHostGroups = persistenceController.getUserRolesConfigDataService().getHostGroupsPermitted();
		}

		if (rebuildTree) {
			rebuildTree(new TreeSet<>(clientsForTableModel), permittedHostGroups);
		}

		// changes the produced unfilteredList
		if (allowedClients != null) {
			clientsForTableModel = produceClientSetForDepots(allowedClients);

			Logging.info(this, " clientsForTableModel ", clientsForTableModel.size());

			buildPclistTableModelCounter++;
			Logging.info(this, "buildPclistTableModel, counter ", buildPclistTableModelCounter, "   rebuildTree  ",
					rebuildTree);

			if (rebuildTree) {
				rebuildTree(new TreeSet<>(clientsForTableModel), permittedHostGroups);
			}
		}

		clientsForTableModel.retainAll(clientsFilteredByTree);

		Logging.info(this, " clientTable isFilteredMode ", clientTable.isFilteredMode());

		if (clientTable.isFilteredMode()) {
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
			rowmap.put(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, connectedHostsByMessagebus.contains(clientId));

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
		Logging.debug(this, "buildPclistTableModel, rebuildTree, allPCs  ", allPCs);

		clientTree.clear();

		clientTree.produceTreeForALL(allPCs);

		clientTree.produceAndLinkGroups(persistenceController.getGroupDataService().getHostGroupsPD(),
				permittedHostGroups);

		Logging.info(this, "buildPclistTableModel, permittedHostGroups ", permittedHostGroups);
		Logging.info(this, "buildPclistTableModel, allPCs ", allPCs.size());
		allowedClients = clientTree.associateClientsToGroups(allPCs,
				persistenceController.getGroupDataService().getFObject2GroupsPD(), permittedHostGroups);

		if (allowedClients != null) {
			Logging.info(this, "buildPclistTableModel, allowedClients ", allowedClients.size());
		}
	}

	public void setClient(String clientName) {
		setClients(Collections.singletonList(clientName));
	}

	public void setClients(List<String> clientNames) {
		Logging.info(this, "setClients ", clientNames);
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

		clientTable.setSelectedValues(clientsFilteredByTree);
	}

	public void requestReloadStatesAndActions() {
		Logging.info(this, "requestReloadStatesAndActions");
		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTIES.toString());
	}

	public List<String> getSelectedClients() {
		return selectedClients;
	}

	private void setSelectedClients(List<String> clientNames) {
		Logging.info(this, "setSelectedClients clientNames size ", clientNames.size());

		if (clientNames.equals(saveSelectedClients)) {
			Logging.info(this, "setSelectedClients clientNames.equals(saveSelectedClients)");
		}

		saveSelectedClients = clientNames;

		requestReloadStatesAndActions();

		Logging.info(this, "setSelectedClientsArray ", clientNames.size());
		Logging.info(this, "selectedClients was before ", selectedClients.size());

		selectedClients = new ArrayList<>(clientNames);

		clientTree.produceActiveParents();

		// With a new client the view should be updated, but only when we are in the Client configuration
		if (editingTarget == EditingTarget.CLIENTS) {
			// change in selection not via clientpage (i.e. via tree)
			mainFrame.getClientConfiguration().stateChanged(null);
		}
	}

	public void toggleFilterClientList(boolean rebuildClientListTableModel) {
		Logging.info(this, "toggleFilterClientList, rebuild client list table model ", rebuildClientListTableModel);

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

			column.setCellRenderer(
					new BooleanIconTableCellRenderer(Icons.getIntellijIcon("checkmark", Globals.OPSI_OK), null));
		}

		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL))) {
			List<String> columns = new ArrayList<>();
			for (int i = 0; i < clientTable.getTableModel().getColumnCount(); i++) {
				columns.add(clientTable.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are ", columns, ", search for ",
					HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);

			int col = clientTable.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col ", col);

			Logging.info(this, "showAndSave found col ", col);

			initSelectionPanelColumn(col);
		}

		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL))) {
			List<String> columns = new ArrayList<>();
			for (int i = 0; i < clientTable.getTableModel().getColumnCount(); i++) {
				columns.add(clientTable.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are ", columns, ", search for ",
					HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);

			int col = clientTable.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col ", col);

			initSelectionPanelColumn(col);
		}

		if (Boolean.TRUE.equals(persistenceController.getHostDataService().getHostDisplayFields()
				.get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL))) {
			List<String> columns = new ArrayList<>();

			for (int i = 0; i < clientTable.getTableModel().getColumnCount(); i++) {
				columns.add(clientTable.getTableModel().getColumnName(i));
			}
			Logging.info(this, "showAndSave columns are ", columns, ", search for ",
					HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);

			int col = clientTable.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

			Logging.info(this, "setSelectionPanelCols ,  found col ", col);

			initSelectionPanelColumn(col);
		}
	}

	private void initSelectionPanelColumn(int col) {
		if (col > -1) {
			TableColumn column = clientTable.getColumnModel().getColumn(col);
			Logging.info(this, "setSelectionPanelCols  column ", column.getHeaderValue());
			column.setMaxWidth(ICON_COLUMN_MAX_WIDTH);
			column.setCellRenderer(new BooleanIconTableCellRenderer(Icons.getIntellijIcon("checkmark"), null));
		}
	}

	private void setRebuiltClientListTableModel(boolean restoreSortKeys) {
		Logging.info(this, "setRebuiltClientListTableModel, we have selected Set : ", clientTable.getSelectedSet());

		setRebuiltClientListTableModel(restoreSortKeys, true, clientTable.getSelectedSet());
	}

	private void setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree,
			Set<String> selectValues) {
		Logging.info(this,
				"setRebuiltClientListTableModel(boolean restoreSortKeys, boolean rebuildTree, Set selectValues)  : ",
				restoreSortKeys, ", ", rebuildTree, ",  selectValues.size() ", Logging.getSize(selectValues));

		List<? extends SortKey> saveSortKeys = clientTable.getSortKeys();

		Logging.info(this, " setRebuiltClientListTableModel--- set model new, selected ",
				clientTable.getSelectedValues().size());

		TableModel tm = buildClientListTableModel(rebuildTree);
		Logging.info(this, "setRebuiltClientListTableModel --- got model selected ",
				clientTable.getSelectedValues().size());

		int[] columnWidths = ConfigedUtilityMethods.getTableColumnWidths(clientTable.getTable());

		// We want to deactivate the listener here, since we want it to react only later when 
		// the values are selected. We only reactivate the listener if it was active before.
		boolean listenerDeactivated = clientTable.deactivateListSelectionListener();
		clientTable.setModel(tm);
		if (listenerDeactivated) {
			clientTable.activateListSelectionListener();
		}

		ConfigedUtilityMethods.setTableColumnWidths(clientTable.getTable(), columnWidths);

		clientTable.initColumnNames();
		Logging.debug(this, " --- model set  ");

		setSelectionPanelCols();

		if (restoreSortKeys) {
			clientTable.setSortKeys(saveSortKeys);
		}

		Logging.info(this, "setRebuiltClientListTableModel set selected values in setRebuiltClientListTableModel() ",
				Logging.getSize(selectValues));
		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel",
				Logging.getSize(clientTable.getSelectedValues()));

		// did lose the selection since last setting
		clientTable.setSelectedValues(selectValues);

		Logging.info(this, "setRebuiltClientListTableModel selected in selection panel ",
				Logging.getSize(clientTable.getSelectedValues()));

		reloadCounter++;
		Logging.info(this, "setRebuiltClientListTableModel  reloadCounter ", reloadCounter);
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
			setClientByTree(selectedNode, newSelectedPath);
		}
	}

	public void treeClientsSelectAction(TreePath[] selTreePaths) {
		clientTable.setFilterMark(false);

		clientsFilteredByTree.clear();
		if (selTreePaths != null) {
			for (TreePath selectionPath : selTreePaths) {
				clientsFilteredByTree.add(selectionPath.getLastPathComponent().toString());
			}
		}

		if (selTreePaths == null) {
			setRebuiltClientListTableModel(true, false, clientsFilteredByTree);
			mainFrame.getHostsStatusPanel().setGroupName("");
			mainFrame.getHostsStatusPanel().updateValues(clientCount, selectedClients.size(),
					Utils.getListStringRepresentation(selectedClients, null), clientInDepot);
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

	private void setClientByTree(DefaultMutableTreeNode selectedNode, TreePath pathToNode) {
		activateClientByTree(pathToNode);
		setRebuiltClientListTableModel(true, false, clientsFilteredByTree);

		setGroupNameForNode(selectedNode);

		mainFrame.getHostsStatusPanel().updateValues(clientCount, selectedClients.size(),
				Utils.getListStringRepresentation(selectedClients, null), clientInDepot);
	}

	private void activateClientByTree(TreePath pathToNode) {
		Logging.info(this, "activateClientByTree, pathToNode: ", pathToNode);

		// since we select based on the tree view we disable the filter
		if (clientTable.isFilteredMode()) {
			toggleFilterClientList(false);
		}
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
		if (preferringOldSelection && !clientTable.getSelectedSet().isEmpty()) {
			selectValues = clientTable.getSelectedSet();
		}

		setRebuiltClientListTableModel(true, false, selectValues);
		// with this, a selected client remains selected (but in bottom line, the group
		// seems activated, not the client)

		activatedGroupModel.setNode("" + node);
		activatedGroupModel.setDescription(clientTree.getGroups().get("" + node).get("description"));
		activatedGroupModel.setAssociatedClients(clientsFilteredByTree);
		activatedGroupModel.setActive(true);

		// since we select based on the tree view we disable the filter
		if (clientTable.isFilteredMode()) {
			toggleFilterClientList(true);
		}
	}

	public ActivatedGroupModel getActivatedGroupModel() {
		return activatedGroupModel;
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

	public boolean setDepotRepresentative() {
		Logging.debug(this, "setDepotRepresentative");

		if (getSelectedClients().isEmpty()) {
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

	public List<Map<String, Object>> produceAdditionalConfigs(List<String> list) {
		Logging.info(this, "additionalConfig fetch for ", list);

		if (list.isEmpty()) {
			return new ArrayList<>();
		} else {
			return persistenceController.getConfigDataService().getHostsConfigsWithDefaults(list);
		}
	}

	public boolean logfileExists(String logtype) {
		return logfiles != null && logfiles.get(logtype) != null && !logfiles.get(logtype).isEmpty()
				&& !logfiles.get(logtype).equals(Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
	}

	public Map<String, String> getLogfilesUpdating(String logtypeToUpdate) {
		Logging.info(this, "getLogfilesUpdating ", logtypeToUpdate);

		if (selectedClients.size() == 1) {
			logfiles = persistenceController.getLogDataService().getLogfile(selectedClients.get(0), logtypeToUpdate);
			Logging.debug(this, "log pages set");
		} else {
			for (String logType : Utils.getLogTypes()) {
				logfiles.put(logType, Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
			}
		}

		return logfiles;
	}

	public List<String> getSelectedDepots() {
		return depotsList.getSelectedValuesList();
	}

	public Set<String> getAllowedClients() {
		return allowedClients;
	}

	private String[] getDepotArray() {
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

	private void refreshClientListKeepingGroup() {
		// dont do anything if we did not finish another thread for this
		String oldGroupSelection = activatedGroupModel.getGroupName();
		Logging.info(this, " refreshClientListKeepingGroup oldGroupSelection ", oldGroupSelection);

		setRebuiltClientListTableModel(true, true, clientTable.getSelectedSet());
		activateGroup(true, oldGroupSelection);
	}

	public void reload() {
		mainFrame.activateLoadingPane(Configed.getResourceValue("MainFrame.jMenuFileReload") + " ...");
		SwingUtilities.invokeLater(this::reloadData);
	}

	private void reloadData() {
		checkSaveAll(true);

		List<String> selValuesList = clientTable.getSelectedValues();
		Logging.info(this, "reloadData, selValuesList.size ", selValuesList.size());

		clientTable.deactivateListSelectionListener();
		allowedClients = null;

		persistenceController.reloadData(CacheIdentifier.ALL_DATA.toString());
		persistenceController.getUserRolesConfigDataService().checkConfigurationPD();

		preloadData();

		FOpsiLicenseMissingText.reset();

		mainFrame.resetData();

		requestReloadStatesAndActions();

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
		clientTable.setSelectedValues(clientsLeft);
		clientTable.activateListSelectionListener();

		Logging.info(this, "reloadData, selected clients now, after resetting ", Logging.getSize(selectedClients));
		mainFrame.reloadServerConsoleMenu();

		updateHostInfo();

		hostInfo.resetGui();

		mainFrame.deactivateLoadingPane();

		// We want to reset and reload the page that is being shown now...
		EditingTarget t = editingTarget;
		editingTarget = null;
		setEditingTarget(t);
	}

	public void addToGlobalUpdateCollection(UpdateCollection newCollection) {
		updateCollection.add(newCollection);
	}

	public void removeFromGlobalUpdateCollection(UpdateCollection newCollection) {
		updateCollection.remove(newCollection);
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
			Logging.info(this, "dataHaveChanged from ", source);

			// anyDataChanged in ConfigedMain
			setDataChanged(super.isDataChanged());
		}

		public boolean askSave() {
			boolean result = false;
			if (this.dataChanged) {
				if (fAskSaveProductConfiguration == null) {
					fAskSaveProductConfiguration = new FTextArea(mainFrame, Globals.APPNAME, true, new String[] {
							Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") });
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

			Logging.info(this, "we should now start working on the update collection of size  ",
					updateCollection.size());

			updateCollection.doCall();
			Logging.checkErrorList(mainFrame);

			Logging.info(this, "we clear the update collection ", updateCollection.getClass());

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
			mainFrame.getClientConfiguration().getProductPageManager().updateProductStates();
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

			Logging.debug(this, "dataHaveChanged source ", source);

			if (source == null) {
				Logging.info(this, "dataHaveChanged null");
			} else {
				for (Entry<?, ?> clientEntry : source.entrySet()) {
					Logging.debug(this, "dataHaveChanged for client ", clientEntry.getKey(), " with values",
							clientEntry.getValue());
				}
			}

			super.dataHaveChanged(source);

			Logging.debug(this, "dataHaveChanged dataChanged ", dataChanged);

			setDataChanged(super.isDataChanged());

			Logging.debug(this, "dataHaveChanged dataChanged ", dataChanged);

			// anyDataChanged in ConfigedMain

			Logging.info(this, "dataHaveChanged dataChanged ", dataChanged);
		}

		public boolean askSave() {
			boolean result = false;
			if (this.dataChanged) {
				if (fAskSaveChangedText == null) {
					fAskSaveChangedText = new FTextArea(mainFrame, Globals.APPNAME, true, new String[] {
							Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") });
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
			Logging.info(this, "save , dataChanged ", dataChanged, " source ", source);
			if (this.dataChanged && source != null && selectedClients != null) {
				Logging.info(this, "save for clients ", selectedClients.size());

				for (String client : selectedClients) {
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
		Logging.info(this, "setDataChanged ", b, ", showing ", show);
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
			result = JOptionPane.showConfirmDialog(mainFrame,
					Configed.getResourceValue("ConfigedMain.saveBeforeCloseText"),
					Configed.getResourceValue("ConfigedMain.saveBeforeCloseTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		}

		Logging.debug(this, "checkClose result ", result);
		return result;
	}

	// save if not otherwise stated
	public void checkSaveAll(boolean ask) {
		Logging.debug(this, "checkSaveAll: anyDataChanged, ask  ", anyDataChanged, ", ", ask);

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

	private void updateConnectionStatusInTable(String clientName) {
		AbstractTableModel model = clientTable.getTableModel();

		int col = model.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected"));

		for (int row = 0; row < model.getRowCount(); row++) {
			if (model.getValueAt(row, 0).equals(clientName)) {
				model.setValueAt(connectedHostsByMessagebus.contains(clientName), row, col);

				model.fireTableCellUpdated(row, col);

				Logging.info(this, "connectionStatus for client ", clientName, " updated in table");
				return;
			}
		}
		Logging.info(this, "could not update connectionStatus for client ", clientName, ": not in list of shown table");
	}

	public void getSessionInfo() {
		mainFrame.setCursor(Globals.WAIT_CURSOR);
		setColumnSessionInfo(true);
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

	public void resetProductsForSelectedClients(boolean withDependencies, boolean resetLocalbootProducts,
			boolean resetNetbootProducts) {
		String confirmInfoMessage = getConfirmInfoMessage(resetLocalbootProducts, resetNetbootProducts);
		if (selectedClients.isEmpty() || confirmInfoMessage.isEmpty()
				|| !confirmActionForSelectedClients(confirmInfoMessage)) {
			return;
		}

		mainFrame.activateLoadingCursor();

		persistenceController.getProductDataService().resetProducts(selectedClients, withDependencies,
				resetLocalbootProducts ? OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING
						: OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);

		requestReloadStatesAndActions();

		mainFrame.getClientConfiguration().updateProductTab();

		mainFrame.deactivateLoadingCursor();
	}

	private String getConfirmInfoMessage(boolean resetLocalbootProducts, boolean resetNetbootProducts) {
		String confirmInfo = "";
		if (resetLocalbootProducts && resetNetbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetProducts.question");
		} else if (resetLocalbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetLocalbootProducts.question");
		} else if (resetNetbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetNetbootProducts.question");
		} else {
			Logging.warning(this, "cannot reset products because they're neither localboot nor netboot");
		}
		return confirmInfo;
	}

	public boolean freeAllPossibleLicensesForSelectedClients() {
		Logging.info(this, "freeAllPossibleLicensesForSelectedClients, count ", selectedClients.size());

		if (selectedClients.isEmpty()) {
			return true;
		}

		if (!confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.confirmFreeLicenses.question"))) {
			return false;
		}

		for (String client : selectedClients) {
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
		if (newClientDialog == null) {
			newClientDialog = new NewClientDialog(this);
		}

		newClientDialog.setDefaultValues();
		newClientDialog.setLocationRelativeTo(getMainFrame());
		newClientDialog.setVisible(true);
	}

	public void callChangeClientIDDialog() {
		if (selectedClients.size() != 1) {
			return;
		}

		FEditText fEdit = new FEditText(selectedClients.get(0)) {
			@Override
			protected void commit() {
				super.commit();

				String newID = getText();

				if (persistenceController.getHostInfoCollections().getOpsiHostNames().contains(newID)) {
					showInformationHostExistsAlready(newID);
				}

				Logging.debug(this, "new name ", newID);

				persistenceController.getHostDataService().renameClient(selectedClients.get(0), newID);

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
		if (selectedClients.isEmpty()) {
			return;
		}

		FShowListWithComboSelect fChangeDepotForClients = new FShowListWithComboSelect(mainFrame,
				Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.title"), true,
				Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.newDepot"), getDepotArray(),
				new String[] { Configed.getResourceValue("buttonClose"), Configed.getResourceValue("buttonOK") });

		fChangeDepotForClients.setLineWrap(false);

		StringBuilder messageBuffer = new StringBuilder(
				"\n" + Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.Moving") + ": \n\n");

		for (String selectedClient : selectedClients) {
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
		persistenceController.getHostInfoCollections().setDepotForClients(selectedClients, targetDepot);
		Logging.checkErrorList(mainFrame);
		refreshClientListKeepingGroup();
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
		List<String> createdClientNames = clients.stream().map(v -> (String) v.get(0) + "." + v.get(1)).toList();
		persistenceController.getHostInfoCollections().addOpsiHostNames(createdClientNames);
		if (persistenceController.getHostDataService().createClients(clients)) {
			Logging.debug(this, "createClients", clients);
			Logging.checkErrorList(mainFrame);

			persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());

			setRebuiltClientListTableModel(true);
			activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
			setClients(createdClientNames);
		} else {
			persistenceController.getHostInfoCollections().removeOpsiHostNames(createdClientNames);
		}
	}

	public void createClient(String newClientID, final String[] groups) {
		Logging.checkErrorList(mainFrame);
		persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());

		setRebuiltClientListTableModel(true);

		if (groups.length == 0 || groups.length > 1 || !activateGroup(false, groups[0])) {
			activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
		}

		// Sets the client on the table
		setClient(newClientID);
	}

	public void wakeSelectedClients() {
		if (selectedClients == null) {
			return;
		}

		Logging.info(this, "wakeUp ", selectedClients.size());
		if (selectedClients.isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoWakeClients")) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().wakeOnLanOpsi43(selectedClients);
			}
		}.start();
	}

	public void deletePackageCachesOfSelectedClients() {
		if (selectedClients.isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoDeletePackageCaches")) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().deletePackageCaches(selectedClients);
			}
		}.start();
	}

	public void fireOpsiclientdEventOnSelectedClients(final String event) {
		if (selectedClients.isEmpty()) {
			return;
		}

		new AbstractErrorListProducer("opsiclientd " + event) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().fireOpsiclientdEventOnClients(event,
						selectedClients);
			}
		}.start();
	}

	public void processActionRequestsAllProducts() {
		processActionRequests(Collections.emptySet());
	}

	public void processActionRequestsSelectedProducts() {
		processActionRequests(mainFrame.getClientConfiguration().getPanelLocalbootProductSettings().getSelectedIDs());
	}

	private void processActionRequests(Set<String> products) {
		if (selectedClients.isEmpty()) {
			return;
		}

		checkSaveAll(false);

		new AbstractErrorListProducer("opsiclientd processActionRequests") {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().processActionRequests(selectedClients, products);
			}
		}.start();
	}

	public void showPopupOnSelectedClients(final String message, final Float seconds) {
		if (selectedClients.isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoPopup") + " " + message) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().showPopupOnClients(message, selectedClients,
						seconds);
			}
		}.start();
	}

	private void initSavedSearchesDialog() {
		if (savedSearchesDialog == null) {
			Logging.debug(this, "create SavedSearchesDialog");
			savedSearchesDialog = new SavedSearchesDialog(clientTable, this);
			savedSearchesDialog.setPreferredScrollPaneSize(new Dimension(300, 400));
			savedSearchesDialog.init();
		}
	}

	public void clientSelectionGetSavedSearch() {
		Logging.debug(this, "clientSelectionGetSavedSearch");
		initSavedSearchesDialog();

		savedSearchesDialog.setLocationRelativeTo(mainFrame);
		savedSearchesDialog.setVisible(true);
	}

	public void startControlDialog() {
		if (commandControlDialog == null) {
			commandControlDialog = new CommandControlDialog(this);
		}
		commandControlDialog.setVisible(true);
	}

	private boolean confirmActionForSelectedClients(String confirmInfo) {
		FShowList fConfirmActionForClients = new FShowList(mainFrame, Globals.APPNAME, true,
				new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") }, 350,
				400);

		fConfirmActionForClients.setMessage(
				confirmInfo + "\n\n" + Utils.getListStringRepresentation(selectedClients, null).replace(";", ""));

		fConfirmActionForClients.setLocationRelativeTo(ConfigedMain.getMainFrame());
		fConfirmActionForClients.setAlwaysOnTop(true);
		fConfirmActionForClients.setVisible(true);

		return fConfirmActionForClients.getResult() == 2;
	}

	public void shutdownSelectedClients() {
		if (selectedClients.isEmpty()) {
			return;
		}

		if (confirmActionForSelectedClients(
				Configed.getResourceValue("ConfigedMain.ConfirmShutdownClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoShutdownClients")) {
				@Override
				protected List<String> getErrors() {
					return persistenceController.getRPCMethodExecutor().shutdownClients(selectedClients);
				}
			}.start();
		}
	}

	public void rebootSelectedClients() {
		if (selectedClients.isEmpty()) {
			return;
		}

		if (confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmRebootClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoRebootClients")) {
				@Override
				protected List<String> getErrors() {
					return persistenceController.getRPCMethodExecutor().rebootClients(selectedClients);
				}
			}.start();
		}
	}

	public void deleteSelectedClients() {
		if (selectedClients.isEmpty()) {
			return;
		}

		if (!confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmDeleteClients.question"))) {
			return;
		}

		persistenceController.getHostDataService().deleteClients(selectedClients);

		if (clientTable.isFilteredMode()) {
			toggleFilterClientList(true);
		}

		refreshClientListKeepingGroup();
	}

	public void copySelectedClient() {
		if (selectedClients.isEmpty()) {
			return;
		}

		Optional<HostInfo> selectedClient = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps().values()
				.stream().filter(hostValues -> hostValues.getName().equals(selectedClients.get(0))).findFirst();

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
				Globals.DEFAULT_FTEXTAREA_WIDTH, 230, additionalPane);

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

			Logging.info(this, "copy client with new name ", newClientName);
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

		if (!getConnectedClientsByMessagebus().contains(connectToHost) && !"Configserver".equals(connectToHost)) {
			Logging.info(this, type, " shell access feature is only supported for clients connected with messagebus");
			JOptionPane.showMessageDialog(mainFrame,
					Configed.getResourceValue("ConfigedMain.openTerminalOn" + type + "Feature.message"));
			return;
		}
		TerminalFrame terminalFrame = new TerminalFrame(this);
		terminalFrame.setMessagebus(messagebus);
		terminalFrame.setSession(connectToHost);
		terminalFrame.display();
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

			Logging.info(this, "selectClientsByFailedAtSomeTimeAgo  test ", test);
			manager.setSearch(test);
		}

		List<String> result = manager.selectClients();

		clientTable.setSelectedValues(result);
	}

	public void selectClientsNotCurrentProductInstalled(String selectedProduct,
			boolean includeClientsWithBrokenInstallation) {
		Logging.debug(this, "selectClientsNotCurrentProductInstalled, products ", selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		String productVersion = persistenceController.getProductDataService().getProductVersion(selectedProduct);
		String packageVersion = persistenceController.getProductDataService().getProductPackageVersion(selectedProduct);

		Logging.debug(this, "selectClientsNotCurrentProductInstalled product ", selectedProduct, ", ", productVersion,
				", ", packageVersion);

		List<String> clientsToSelect = persistenceController.getHostDataService().getClientsWithOtherProductVersion(
				selectedProduct, productVersion, packageVersion, includeClientsWithBrokenInstallation);

		Logging.info(this, "selectClientsNotCurrentProductInstalled clients found globally ", clientsToSelect.size());

		clientsToSelect.retainAll(clientTable.getColumnValues(0));

		Logging.info(this, "selectClientsNotCurrentProductInstalled clients found for displayed client list ",
				clientsToSelect.size());

		clientTable.setSelectedValues(clientsToSelect);
	}

	public void selectClientsWithFailedProduct(String selectedProduct) {
		Logging.debug(this, "selectClientsWithFailedProduct, products ", selectedProduct);
		if (selectedProduct == null || selectedProduct.isEmpty()) {
			return;
		}

		SelectionManager manager = new SelectionManager(null);

		String test = String.format(SavedSearches.SEARCH_FAILED_PRODUCT, selectedProduct);

		manager.setSearch(test);

		List<String> result = manager.selectClients();

		Logging.info(this, "selected: ", result);
		clientTable.setSelectedValues(result);
	}

	public void logEventOccurred() {
		if (allFrames == null) {
			return;
		}

		boolean found = false;

		for (JFrame f : allFrames) {
			if (f != null) {
				Logging.debug(this, "log event occurred in frame f , is focused ", f.isFocused(), " ", f);

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
		Logging.info(this, "start closing instance, checkdirty ", checkdirty);

		if (checkdirty) {
			int closeCheckResult = checkClose();

			if (closeCheckResult == JOptionPane.YES_OPTION) {
				checkSaveAll(false);
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

	public void finishApp(boolean checkdirty, int exitcode) {
		if (closeInstance(checkdirty)) {
			Main.endApp(exitcode);
		}
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

	public static void setOTP(String otp) {
		ConfigedMain.otp = otp;
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

		if (WebSocketEvent.HOST_CONNECTED.toString().equals(eventType)) {
			addClientToConnectedList((String) ((Map<?, ?>) eventData.get("host")).get("id"));
		} else if (WebSocketEvent.HOST_DISCONNECTED.toString().equals(eventType)) {
			removeClientFromConnectedList((String) ((Map<?, ?>) eventData.get("host")).get("id"));
		} else if (WebSocketEvent.HOST_CREATED.toString().equals(eventType)) {
			addClientToTable((String) eventData.get("id"));
		} else if (WebSocketEvent.HOST_DELETED.toString().equals(eventType)) {
			removeClientFromTable((String) eventData.get("id"));
		} else {
			// Other events are handled by other listeners.
		}
	}
}
