/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableModel;

import com.formdev.flatlaf.FlatLaf;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.EditingTarget;
import de.uib.configed.FCreditsDialog;
import de.uib.configed.Globals;
import de.uib.configed.dashboard.LicenseDisplayer;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.hwinfopage.ControllerHWinfoMultiClients;
import de.uib.configed.gui.hwinfopage.PanelHWInfo;
import de.uib.configed.gui.productpage.PanelProductProperties;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.gui.swinfopage.PanelSWInfo;
import de.uib.configed.gui.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.HostInfo;
import de.uib.messages.Messages;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.modulelicense.LicensingInfoDialog;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserSshConfig;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.savedstates.UserPreferences;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.FEditObject;
import de.uib.utilities.swing.FEditTextWithExtra;
import de.uib.utilities.swing.JTextEditorField;
import de.uib.utilities.swing.JTextHideField;
import de.uib.utilities.swing.SeparatedDocument;
import de.uib.utilities.table.AbstractExportTable;
import de.uib.utilities.table.ClientTableExporterToCSV;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import utils.PopupMouseListener;
import utils.Utils;

public class MainFrame extends JFrame implements WindowListener, KeyListener, MouseListener, ComponentListener {
	private static final int DIVIDER_LOCATION_CENTRAL_PANE = 300;

	public static final int F_WIDTH = 800;

	// todo rework 
	private static final int F_WIDTH_RIGHTHANDED = 200;

	public static final String ITEM_ADD_CLIENT = "add client";
	public static final String ITEM_DELETE_CLIENT = "remove client";
	public static final String ITEM_FREE_LICENSES = "free licenses for client";

	private static JRadioButtonMenuItem[] rbLoglevelItems = new JRadioButtonMenuItem[Logging.LEVEL_SECRET + 1];

	private String oldNotes;

	private Map<String, Map<String, String>> changedClientInfos;

	private AbstractExportTable exportTable;

	private ConfigedMain configedMain;

	// menu system
	private Map<String, List<JMenuItem>> menuItemsHost;

	private JMenu jMenuFile;
	private JMenuItem jMenuFileSaveConfigurations;

	private JMenu jMenuClients = new JMenu();

	private JMenuItem jMenuDirectWOL = new JMenuItem();
	private JMenuItem jMenuNewScheduledWOL = new JMenuItem();

	private JMenuItem jMenuShowScheduledWOL = new JMenuItem();
	private JMenuItem jMenuShowPopupMessage = new JMenuItem();
	private JMenuItem jMenuRequestSessionInfo = new JMenuItem();
	private JMenuItem jMenuShutdownClient = new JMenuItem();
	private JMenuItem jMenuRebootClient = new JMenuItem();
	private JMenuItem jMenuChangeDepot = new JMenuItem();
	private JMenuItem jMenuChangeClientID = new JMenuItem();

	private JMenuItem jMenuAddClient = new JMenuItem();
	private JMenuItem jMenuDeleteClient = new JMenuItem();
	private JMenuItem jMenuCopyClient = new JMenuItem();

	private JMenu jMenuResetProducts = new JMenu();

	private JMenuItem jMenuFreeLicenses = new JMenuItem();
	private JMenuItem jMenuDeletePackageCaches = new JMenuItem();

	private JMenuItem jMenuResetProductOnClientWithStates = new JMenuItem();
	private JMenuItem jMenuResetProductOnClient = new JMenuItem();
	private JMenuItem jMenuResetLocalbootProductOnClientWithStates = new JMenuItem();
	private JMenuItem jMenuResetLocalbootProductOnClient = new JMenuItem();
	private JMenuItem jMenuResetNetbootProductOnClientWithStates = new JMenuItem();
	private JMenuItem jMenuResetNetbootProductOnClient = new JMenuItem();

	private JMenu jMenuServer = new JMenu();

	private JMenuItem jMenuSSHConnection = new JMenuItem();

	private Map<String, Integer> labelledDelays;

	private Map<String, String> searchedTimeSpans;
	private Map<String, String> searchedTimeSpansText;

	private JMenu jMenuShowColumns = new JMenu();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowCreatedColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowWANactiveColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowIPAddressColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowInventoryNumberColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowSystemUUIDColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowHardwareAddressColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowSessionInfoColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowUefiBoot = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowInstallByShutdown = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowDepotColumn = new JCheckBoxMenuItem();
	private JMenuItem jMenuRemoteControl = new JMenuItem();

	private JMenuItem[] clientMenuItemsDependOnSelectionCount = new JMenuItem[] { jMenuResetProducts, jMenuDeleteClient,
			jMenuResetProducts, jMenuFreeLicenses, jMenuShowPopupMessage, jMenuRequestSessionInfo,
			jMenuDeletePackageCaches, jMenuRebootClient, jMenuShutdownClient, jMenuChangeDepot, jMenuRemoteControl };

	private JMenu jMenuClientselection = new JMenu();
	private JMenuItem jMenuClientselectionGetGroup = new JMenuItem();
	private JMenuItem jMenuClientselectionGetSavedSearch = new JMenuItem();
	private JMenuItem jMenuClientselectionProductNotUptodate = new JMenuItem();
	private JMenuItem jMenuClientselectionProductNotUptodateOrBroken = new JMenuItem();
	private JMenuItem jMenuClientselectionFailedProduct = new JMenuItem();
	private JMenu jMenuClientselectionFailedInPeriod = new JMenu();

	private JCheckBoxMenuItem jMenuClientselectionToggleClientFilter = new JCheckBoxMenuItem();

	private JMenu jMenuFrames = new JMenu();
	private JMenuItem jMenuFrameWorkOnGroups = new JMenuItem();
	private JMenuItem jMenuFrameWorkOnProducts = new JMenuItem();
	private JMenuItem jMenuFrameDashboard = new JMenuItem();
	private JMenuItem jMenuFrameLicenses = new JMenuItem();
	private JMenuItem jMenuFrameShowDialogs = new JMenuItem();
	private JMenuItem jMenuFrameTerminal = new JMenuItem();

	private JMenu jMenuHelp = new JMenu();
	private JMenuItem jMenuHelpInternalConfiguration = new JMenuItem();
	private JMenuItem jMenuHelpAbout = new JMenuItem();
	private JMenuItem jMenuHelpCredits = new JMenuItem();
	private JMenuItem jMenuHelpOpsiVersion = new JMenuItem();
	private JMenuItem jMenuHelpOpsiModuleInformation = new JMenuItem();
	private JMenuItem jMenuHelpCheckHealth = new JMenuItem();

	private JPopupMenu popupClients = new JPopupMenu();

	private JMenu popupResetProducts = new JMenu(Configed.getResourceValue("MainFrame.jMenuResetProducts"));
	private JMenuItem popupResetLocalbootProductOnClientWithStates = new JMenuItem();
	private JMenuItem popupResetLocalbootProductOnClient = new JMenuItem();
	private JMenuItem popupResetNetbootProductOnClientWithStates = new JMenuItem();
	private JMenuItem popupResetNetbootProductOnClient = new JMenuItem();
	private JMenuItem popupResetProductOnClientWithStates = new JMenuItem();
	private JMenuItem popupResetProductOnClient = new JMenuItem();

	private JMenuItem popupAddClient = new JMenuItem();
	private JMenuItem popupCopyClient = new JMenuItem();
	private JMenuItem popupDeleteClient = new JMenuItem();
	private JMenuItem popupFreeLicenses = new JMenuItem();
	private JMenuItem popupDeletePackageCaches = new JMenuItem();
	private JMenu popupWakeOnLan = new JMenu(Configed.getResourceValue("MainFrame.jMenuWakeOnLan"));
	private JMenuItem popupWakeOnLanDirect = new JMenuItem();
	private JMenuItem popupWakeOnLanScheduler = new JMenuItem();

	private JMenuItem popupShowPopupMessage = new JMenuItem();
	private JMenuItem popupRequestSessionInfo = new JMenuItem();
	private JMenuItem popupShutdownClient = new JMenuItem();
	private JMenuItem popupRebootClient = new JMenuItem();
	private JMenuItem popupChangeDepot = new JMenuItem();
	private JMenuItem popupChangeClientID = new JMenuItem();
	private JMenuItem popupRemoteControl = new JMenuItem();

	private JMenuItem[] clientPopupsDependOnSelectionCount = new JMenuItem[] { popupResetProducts, popupDeleteClient,
			popupResetProducts, popupFreeLicenses, popupShowPopupMessage, popupRequestSessionInfo,
			popupDeletePackageCaches, popupRebootClient, popupShutdownClient, popupChangeDepot, popupRemoteControl };

	private JMenu popupShowColumns = new JMenu();
	private JCheckBoxMenuItem popupShowCreatedColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowWANactiveColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowIPAddressColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowSystemUUIDColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowHardwareAddressColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowSessionInfoColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowInventoryNumberColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowUefiBoot = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowInstallByShutdownColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowDepotColumn = new JCheckBoxMenuItem();

	private JMenuItem popupSelectionGetGroup = new JMenuItem();
	private JMenuItem popupSelectionGetSavedSearch = new JMenuItem();

	private JCheckBoxMenuItem popupSelectionToggleClientFilter = new JCheckBoxMenuItem();

	private JMenuItem popupRebuildClientList = new JMenuItem(Configed.getResourceValue("PopupMenuTrait.reload"),
			Utils.createImageIcon("images/reload16.png", ""));
	private JMenuItem popupCreatePdf = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"),
			Utils.createImageIcon("images/acrobat_reader16.png", ""));

	private JButton jButtonServerConfiguration;
	private JButton jButtonDepotsConfiguration;
	private JButton jButtonClientsConfiguration;

	private JButton jButtonLicenses;
	private JButton jButtonOpsiLicenses;

	private IconButton iconButtonReload;
	private IconButton iconButtonReloadLicenses;
	private IconButton iconButtonNewClient;
	private IconButton iconButtonSetGroup;
	private IconButton iconButtonSaveConfiguration;
	private IconButton iconButtonToggleClientFilter;

	private IconButton iconButtonReachableInfo;
	private IconButton iconButtonSessionInfo;

	private BorderLayout borderLayout1 = new BorderLayout();
	private JTabbedPane jTabbedPaneConfigPanes;
	private JSplitPane panelClientSelection;

	private HostsStatusPanel statusPane;

	private PanelProductSettings panelLocalbootProductSettings;
	private PanelProductSettings panelNetbootProductSettings;
	private PanelHostConfig panelHostConfig;
	private PanelHostProperties panelHostProperties;
	private PanelProductProperties panelProductProperties;

	private PanelHWInfo panelHWInfo;
	private JPanel showHardwareLogNotFound;
	private ControllerHWinfoMultiClients controllerHWinfoMultiClients;
	private JPanel showHardwareLogMultiClientReport;
	private JPanel showHardwareLogParentOfNotFoundPanel;
	private JPanel showHardwareLog;
	private JLabel labelNoSoftware;

	private PanelSWInfo panelSWInfo;
	private JPanel showSoftwareLogNotFound;
	private PanelSWMultiClientReport showSoftwareLogMultiClientReport;
	private JPanel showSoftwareLog;

	private PanelTabbedDocuments showLogfiles;

	private LicensingInfoDialog fDialogOpsiLicensingInfo;
	private LicensingInfoMap licensingInfoMap;

	private ClientTable panelClientlist;

	private JLabel labelHostID;
	private CheckedLabel cbInstallByShutdown;
	private CheckedLabel cbUefiBoot;
	private CheckedLabel cbWANConfig;

	private JTextEditorField jTextFieldDescription;
	private JTextEditorField jTextFieldInventoryNumber;
	private JTextArea jTextAreaNotes;
	private JTextEditorField systemUUIDField;
	private JTextEditorField macAddressField;
	private JTextEditorField ipAddressField;
	private JTextEditorField jTextFieldOneTimePassword;
	private JTextHideField jTextFieldHostKey;

	private GlassPane glassPane;

	private boolean multidepot;

	private DepotListPresenter depotListPresenter;

	private ClientTree treeClients;
	private ProductTree treeProducts;

	private JPanel clientPane;

	private LicenseDisplayer licenseDisplayer;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public MainFrame(ConfigedMain main, ClientTable panelClientlist, DepotsList depotsList, ClientTree treeClients) {
		// we handle it in the window listener method
		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.multidepot = persistenceController.getHostInfoCollections().getDepots().size() != 1;

		this.panelClientlist = panelClientlist;

		exportTable = new ExporterToCSV(panelClientlist.getTable());

		this.treeClients = treeClients;
		treeProducts = new ProductTree();

		depotListPresenter = new DepotListPresenter(depotsList, multidepot);

		this.configedMain = main;

		guiInit();
		initData();
	}

	@Override
	public void setVisible(boolean b) {
		Logging.info(this, "setVisible from MainFrame " + b);
		super.setVisible(b);
	}

	private void initData() {
		statusPane.updateValues(0, null, null, null);
	}

	public HostsStatusPanel getHostsStatusPanel() {
		return statusPane;
	}

	// This shall be called after MainFrame is made visible
	public void initSplitPanes() {
		panelClientSelection.setDividerLocation(0.8);
		panelLocalbootProductSettings.setDividerLocation(0.8);
		panelNetbootProductSettings.setDividerLocation(0.8);
		panelProductProperties.setDividerLocation(0.8);
	}

	// ------------------------------------------------------------------------------------------
	// configure interaction
	// ------------------------------------------------------------------------------------------
	// menus

	private void setupMenuLists() {
		menuItemsHost = new LinkedHashMap<>();
		menuItemsHost.put(ITEM_ADD_CLIENT, new ArrayList<>());
		menuItemsHost.put(ITEM_DELETE_CLIENT, new ArrayList<>());
		menuItemsHost.put(ITEM_FREE_LICENSES, new ArrayList<>());
	}

	private void setupMenuFile() {
		jMenuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));

		JMenuItem jMenuFileExit = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileExit"));
		jMenuFileExit.addActionListener((ActionEvent e) -> exitAction());

		jMenuFileSaveConfigurations = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileSaveConfigurations"));
		jMenuFileSaveConfigurations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSaveConfigurations.addActionListener((ActionEvent e) -> saveAction());

		JMenuItem jMenuFileReload = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileReload"));

		jMenuFileReload.addActionListener((ActionEvent e) -> {
			reloadAction();
			if (iconButtonReloadLicenses.isEnabled()) {
				reloadLicensesAction();
			}
		});

		JMenuItem jMenuFileLogout = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileLogout"));
		jMenuFileLogout.addActionListener((ActionEvent e) -> logout());

		jMenuFile.add(jMenuFileSaveConfigurations);
		jMenuFile.add(jMenuFileReload);
		jMenuFile.add(Messages.createJMenuLanguages(this::restartConfiged));
		jMenuFile.add(createJMenuTheme(this::restartConfiged));
		jMenuFile.add(jMenuFileLogout);
		jMenuFile.add(jMenuFileExit);
	}

	public static JMenu createJMenuTheme(Runnable runnable) {
		JMenu jMenuTheme = new JMenu(Configed.getResourceValue("theme"));
		ButtonGroup groupThemes = new ButtonGroup();
		String selectedTheme = Messages.getSelectedTheme();

		for (final String theme : Messages.getAvailableThemes()) {
			JMenuItem themeItem = new JRadioButtonMenuItem(Messages.getThemeTranslation(theme));
			Logging.debug("selectedTheme " + theme);
			themeItem.setSelected(selectedTheme.equals(theme));
			jMenuTheme.add(themeItem);
			groupThemes.add(themeItem);

			themeItem.addActionListener((ActionEvent e) -> {
				UserPreferences.set(UserPreferences.THEME, theme);
				Messages.setTheme(theme);
				Main.setOpsiLaf();

				runnable.run();
			});
		}

		return jMenuTheme;
	}

	private void logout() {
		ConfigedMain.setHost(null);
		ConfigedMain.setUser(null);
		ConfigedMain.setPassword(null);
		CacheManager.getInstance().clearAllCachedData();
		SSHCommandFactory.destroyInstance();
		Configed.getSavedStates().removeAll();
		ConfigedMain.requestLicensesFrameReload();
		restartConfiged();
	}

	private void restartConfiged() {
		configedMain.closeInstance(true);
		new Thread() {
			@Override
			public void run() {
				Configed.startConfiged();
			}
		}.start();
	}

	private void initMenuData() {
		labelledDelays = new LinkedHashMap<>();
		labelledDelays.put("0 sec", 0);
		labelledDelays.put("5 sec", 5);
		labelledDelays.put("20 sec", 20);
		labelledDelays.put("1 min", 60);
		labelledDelays.put("2 min", 120);
		labelledDelays.put("10 min", 600);
		labelledDelays.put("20 min", 1200);
		labelledDelays.put("1 h", 3600);

		searchedTimeSpans = new LinkedHashMap<>();

		final String TODAY = "today";
		final String SINCE_YESTERDAY = "since yesterday";
		final String LAST_3_DAYS = "last 3 days";
		final String LAST_7_DAYS = "last 7 days";
		final String LAST_MONTH = "last month";
		final String ANY_TIME = "at any time";

		searchedTimeSpans.put(TODAY, "%minus0%");
		searchedTimeSpans.put(SINCE_YESTERDAY, "%minus1%");
		searchedTimeSpans.put(LAST_3_DAYS, "%minus2%");
		searchedTimeSpans.put(LAST_7_DAYS, "%minus7%");
		searchedTimeSpans.put(LAST_MONTH, "%minus31%");
		searchedTimeSpans.put(ANY_TIME, "");

		searchedTimeSpansText = new LinkedHashMap<>();

		searchedTimeSpansText.put(TODAY, Configed.getResourceValue("MainFrame.TODAY"));
		searchedTimeSpansText.put(SINCE_YESTERDAY, Configed.getResourceValue("MainFrame.SINCE_YESTERDAY"));
		searchedTimeSpansText.put(LAST_3_DAYS, Configed.getResourceValue("MainFrame.LAST_3_DAYS"));
		searchedTimeSpansText.put(LAST_7_DAYS, Configed.getResourceValue("MainFrame.LAST_7_DAYS"));
		searchedTimeSpansText.put(LAST_MONTH, Configed.getResourceValue("MainFrame.LAST_MONTH"));
		searchedTimeSpansText.put(ANY_TIME, Configed.getResourceValue("MainFrame.ANY_TIME"));
	}

	private void setupMenuClients() {
		jMenuClients.setText(Configed.getResourceValue("MainFrame.jMenuClients"));

		jMenuClients.addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(MenuEvent arg0) {
				// Nothing to do.
			}

			@Override
			public void menuDeselected(MenuEvent arg0) {
				// Nothing to do.
			}

			@Override
			public void menuSelected(MenuEvent arg0) {
				enableMenuItemsForClients();
			}
		});

		jCheckBoxMenuItemShowCreatedColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		jCheckBoxMenuItemShowCreatedColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CREATED_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowCreatedColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CREATED_DISPLAY_FIELD_LABEL);
			popupShowCreatedColumn.setSelected(jCheckBoxMenuItemShowCreatedColumn.isSelected());
		});

		jCheckBoxMenuItemShowWANactiveColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		jCheckBoxMenuItemShowWANactiveColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowWANactiveColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);
			popupShowWANactiveColumn.setSelected(jCheckBoxMenuItemShowWANactiveColumn.isSelected());
		});

		jCheckBoxMenuItemShowIPAddressColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		jCheckBoxMenuItemShowIPAddressColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowIPAddressColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);
			popupShowIPAddressColumn.setSelected(jCheckBoxMenuItemShowIPAddressColumn.isSelected());
		});

		jCheckBoxMenuItemShowSystemUUIDColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowSystemUUIDColumn"));
		jCheckBoxMenuItemShowSystemUUIDColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowSystemUUIDColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL);
			popupShowSystemUUIDColumn.setSelected(jCheckBoxMenuItemShowSystemUUIDColumn.isSelected());
		});

		jCheckBoxMenuItemShowHardwareAddressColumn
				.setText(Configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		jCheckBoxMenuItemShowHardwareAddressColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowHardwareAddressColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL);
			popupShowHardwareAddressColumn.setSelected(jCheckBoxMenuItemShowHardwareAddressColumn.isSelected());
		});

		jCheckBoxMenuItemShowSessionInfoColumn
				.setText(Configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		jCheckBoxMenuItemShowSessionInfoColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowSessionInfoColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
			popupShowSessionInfoColumn.setSelected(jCheckBoxMenuItemShowSessionInfoColumn.isSelected());
		});

		jCheckBoxMenuItemShowInventoryNumberColumn
				.setText(Configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		jCheckBoxMenuItemShowInventoryNumberColumn.setSelected(
				configedMain.getHostDisplayFields().get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowInventoryNumberColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL);
			popupShowInventoryNumberColumn.setSelected(jCheckBoxMenuItemShowInventoryNumberColumn.isSelected());
		});

		jCheckBoxMenuItemShowUefiBoot.setText(Configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		jCheckBoxMenuItemShowUefiBoot
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowUefiBoot.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);
			popupShowUefiBoot.setSelected(jCheckBoxMenuItemShowUefiBoot.isSelected());
		});

		jCheckBoxMenuItemShowInstallByShutdown
				.setText(Configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		jCheckBoxMenuItemShowInstallByShutdown.setSelected(
				configedMain.getHostDisplayFields().get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowInstallByShutdown.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);
			popupShowInstallByShutdownColumn.setSelected(jCheckBoxMenuItemShowInstallByShutdown.isSelected());
		});

		jCheckBoxMenuItemShowDepotColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		jCheckBoxMenuItemShowDepotColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));
		jCheckBoxMenuItemShowDepotColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL);
			popupShowDepotColumn.setSelected(jCheckBoxMenuItemShowDepotColumn.isSelected());
		});

		jMenuChangeDepot.setText(Configed.getResourceValue("MainFrame.jMenuChangeDepot"));

		jMenuChangeDepot.addActionListener((ActionEvent e) -> changeDepotAction());

		jMenuChangeClientID.setText(Configed.getResourceValue("MainFrame.jMenuChangeClientID"));

		jMenuChangeClientID.addActionListener((ActionEvent e) -> changeClientIDAction());

		jMenuResetProducts.setText(Configed.getResourceValue("MainFrame.jMenuResetProducts"));

		jMenuResetProductOnClientWithStates
				.setText(Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithStates"));

		jMenuResetProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, true, true));

		jMenuResetProductOnClient
				.setText(Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithoutStates"));

		jMenuResetProductOnClient.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, true, true));

		jMenuResetLocalbootProductOnClientWithStates
				.setText(Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithStates"));

		jMenuResetLocalbootProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, true, false));

		jMenuResetLocalbootProductOnClient
				.setText(Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithoutStates"));

		jMenuResetLocalbootProductOnClient
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, true, false));

		jMenuResetNetbootProductOnClientWithStates
				.setText(Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithStates"));

		jMenuResetNetbootProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, false, true));

		jMenuResetNetbootProductOnClient
				.setText(Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithoutStates"));

		jMenuResetNetbootProductOnClient
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, false, true));

		jMenuAddClient.setText(Configed.getResourceValue("MainFrame.jMenuAddClient"));
		jMenuAddClient.addActionListener((ActionEvent e) -> addClientAction());

		menuItemsHost.get(ITEM_ADD_CLIENT).add(jMenuAddClient);

		JMenu jMenuWakeOnLan = new JMenu(Configed.getResourceValue("MainFrame.jMenuWakeOnLan"));

		jMenuDirectWOL.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.direct"));
		jMenuDirectWOL.addActionListener((ActionEvent e) -> wakeOnLanAction());

		jMenuWakeOnLan.add(jMenuDirectWOL);

		jMenuNewScheduledWOL.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler"));
		jMenuNewScheduledWOL.addActionListener((ActionEvent e) -> {
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(Configed.getResourceValue("FStartWakeOnLan.title"),
					configedMain);
			fStartWakeOnLan.setLocationRelativeTo(this);

			fStartWakeOnLan.setVisible(true);
			fStartWakeOnLan.setPredefinedDelays(labelledDelays);

			fStartWakeOnLan.setClients();
		});

		jMenuWakeOnLan.add(jMenuNewScheduledWOL);

		jMenuWakeOnLan.addSeparator();

		jMenuShowScheduledWOL.setEnabled(false);
		jMenuShowScheduledWOL.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.showRunning"));
		jMenuShowScheduledWOL.addActionListener(
				(ActionEvent e) -> executeCommandOnInstances("arrange", FEditObject.runningInstances.getAll()));

		jMenuWakeOnLan.add(jMenuShowScheduledWOL);

		jMenuDeletePackageCaches.setText(Configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
		jMenuDeletePackageCaches.addActionListener((ActionEvent e) -> deletePackageCachesAction());

		JMenu jMenuOpsiClientdEvent = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : persistenceController.getConfigDataService().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItem(event);

			item.addActionListener((ActionEvent e) -> fireOpsiclientdEventAction(event));

			jMenuOpsiClientdEvent.add(item);
		}

		jMenuShowPopupMessage.setText(Configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
		jMenuShowPopupMessage.addActionListener((ActionEvent e) -> showPopupOnClientsAction());

		jMenuShutdownClient.setText(Configed.getResourceValue("MainFrame.jMenuShutdownClient"));
		jMenuShutdownClient.addActionListener((ActionEvent e) -> shutdownClientsAction());

		jMenuRequestSessionInfo.setText(Configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
		jMenuRequestSessionInfo.addActionListener((ActionEvent e) -> {
			configedMain.setColumnSessionInfo(true);
			getSessionInfo();
		});

		jMenuRebootClient.setText(Configed.getResourceValue("MainFrame.jMenuRebootClient"));
		jMenuRebootClient.addActionListener((ActionEvent e) -> rebootClientsAction());

		jMenuDeleteClient.setText(Configed.getResourceValue("MainFrame.jMenuDeleteClient"));
		jMenuDeleteClient.addActionListener((ActionEvent e) -> deleteClientAction());

		jMenuCopyClient.setText(Configed.getResourceValue("MainFrame.jMenuCopyClient"));
		jMenuCopyClient.addActionListener((ActionEvent e) -> copyClientAction());

		menuItemsHost.get(ITEM_DELETE_CLIENT).add(jMenuDeleteClient);

		jMenuFreeLicenses.setText(Configed.getResourceValue("MainFrame.jMenuFreeLicenses"));
		jMenuFreeLicenses.addActionListener((ActionEvent e) -> freeLicensesAction());

		menuItemsHost.get(ITEM_FREE_LICENSES).add(jMenuFreeLicenses);

		jMenuRemoteControl.setText(Configed.getResourceValue("MainFrame.jMenuRemoteControl"));
		jMenuRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

		// produces a global reaction when pressing space
		jMenuRemoteControl.addActionListener((ActionEvent e) -> panelClientlist.startRemoteControlForSelectedClients());

		jMenuClients.add(jMenuWakeOnLan);
		jMenuClients.add(jMenuOpsiClientdEvent);
		jMenuClients.add(jMenuShowPopupMessage);
		jMenuClients.add(jMenuRequestSessionInfo);
		jMenuClients.add(jMenuDeletePackageCaches);

		jMenuClients.addSeparator();

		jMenuClients.add(jMenuShutdownClient);
		jMenuClients.add(jMenuRebootClient);
		jMenuClients.add(jMenuRemoteControl);

		jMenuClients.addSeparator();

		jMenuClients.add(jMenuAddClient);
		if (ServerFacade.isOpsi43()) {
			jMenuClients.add(jMenuCopyClient);
		}
		jMenuClients.add(jMenuDeleteClient);

		jMenuResetProducts.add(jMenuResetLocalbootProductOnClientWithStates);
		jMenuResetProducts.add(jMenuResetLocalbootProductOnClient);
		jMenuResetProducts.add(jMenuResetNetbootProductOnClientWithStates);
		jMenuResetProducts.add(jMenuResetNetbootProductOnClient);
		jMenuResetProducts.add(jMenuResetProductOnClientWithStates);
		jMenuResetProducts.add(jMenuResetProductOnClient);

		jMenuClients.add(jMenuResetProducts);

		jMenuClients.add(jMenuFreeLicenses);
		jMenuClients.add(jMenuChangeClientID);
		if (multidepot) {
			jMenuClients.add(jMenuChangeDepot);
		}
		jMenuClients.addSeparator();

		// --

		jMenuShowColumns.setText(Configed.getResourceValue("ConfigedMain.columnVisibility"));

		jMenuShowColumns.add(jCheckBoxMenuItemShowWANactiveColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowIPAddressColumn);
		if (ServerFacade.isOpsi43()) {
			jMenuShowColumns.add(jCheckBoxMenuItemShowSystemUUIDColumn);
		}
		jMenuShowColumns.add(jCheckBoxMenuItemShowHardwareAddressColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowSessionInfoColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowInventoryNumberColumn);
		jMenuShowColumns.add(jCheckBoxMenuItemShowCreatedColumn);

		if (!ServerFacade.isOpsi43()) {
			jMenuShowColumns.add(jCheckBoxMenuItemShowUefiBoot);
		}

		jMenuShowColumns.add(jCheckBoxMenuItemShowInstallByShutdown);
		jMenuShowColumns.add(jCheckBoxMenuItemShowDepotColumn);

		jMenuClients.add(jMenuShowColumns);
	}

	public void updateSSHConnectedInfoMenu(String status) {
		String connectiondata = SSHConnectionInfo.getInstance().getUser() + "@"
				+ SSHConnectionInfo.getInstance().getHost();

		jMenuSSHConnection.setText(connectiondata.trim() + " " + status);
	}

	public void reloadServerMenu() {
		setupMenuServer();
	}

	/**
	 * Get existing (sorted) sshcommands and build the menu "server-konsole"
	 * (include config, control and terminal dialog) also check the depot
	 * configs for setting the field editable (or not)
	 **/
	private void setupMenuServer() {
		Logging.info(this, "setupMenuServer ");
		final SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);
		SSHConnectionInfo connectionInfo = SSHConnectionInfo.getInstance();

		JMenu menuOpsi = new JMenu(SSHCommandFactory.PARENT_OPSI);

		jMenuServer.removeAll();
		jMenuServer.setText(SSHCommandFactory.PARENT_NULL);
		boolean isReadOnly = PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly();

		Logging.info(this, "setupMenuServer add configpage");
		JMenuItem jMenuSSHConfig = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuSSHConfig"));
		jMenuSSHConfig.addActionListener((ActionEvent e) -> startSSHConfigAction());

		jMenuSSHConnection.setEnabled(false);

		String connectionState;

		if (Configed.isSSHConnectionOnStart()) {
			connectionState = factory.testConnection(connectionInfo.getUser(), connectionInfo.getHost());
		} else {
			connectionState = factory.getConnectionState();
		}

		updateSSHConnectedInfoMenu(connectionState);

		Logging.info(this, "setupMenuServer add commandcontrol");
		JMenuItem jMenuSSHCommandControl = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuSSHCommandControl"));
		jMenuSSHCommandControl.addActionListener((ActionEvent e) -> startSSHControlAction());
		// SSHCommandControlDialog

		jMenuServer.add(jMenuSSHConnection);
		jMenuServer.add(jMenuSSHConfig);
		jMenuServer.add(jMenuSSHCommandControl);
		jMenuServer.addSeparator();

		Logging.info(this, "setupMenuServer getCurrentUserConfig " + UserConfig.getCurrentUserConfig());

		boolean commandsAreDeactivated = UserConfig.getCurrentUserConfig() == null
				|| UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDS_ACTIVE) == null
				|| !UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDS_ACTIVE);
		Logging.info(this, "setupMenuServer commandsAreDeactivated " + commandsAreDeactivated);

		factory.retrieveSSHCommandListRequestRefresh();
		factory.retrieveSSHCommandList();
		Map<String, List<SSHCommandTemplate>> sortedComs = factory.getSSHCommandMapSortedByParent();

		Logging.debug(this, "setupMenuServer add commands to menu commands sortedComs " + sortedComs);
		boolean firstParentGroup = true;
		boolean commandsExist = false;
		for (Entry<String, List<SSHCommandTemplate>> entry : sortedComs.entrySet()) {
			String parentMenuName = entry.getKey();
			List<SSHCommandTemplate> listCom = new LinkedList<>(entry.getValue());
			Collections.sort(listCom);
			JMenu parentMenu = new JMenu(parentMenuName);

			Logging.info(this, "ssh parent menu text " + parentMenuName);
			if (parentMenuName.equals(SSHCommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS)) {
				parentMenu.setText("");
				parentMenu.setIcon(Utils.createImageIcon("images/burger_menu_09.png", "..."));
			} else if ((parentMenuName.equals(SSHCommandFactory.PARENT_NULL))) {
				// Do nothing in that case
			} else {
				firstParentGroup = false;
			}

			for (final SSHCommandTemplate com : listCom) {
				commandsExist = true;
				JMenuItem jMenuItem = new JMenuItem();
				jMenuItem.setText(com.getMenuText());
				Logging.info(this, "ssh command menuitem text " + com.getMenuText());
				jMenuItem.setToolTipText(com.getToolTipText());
				jMenuItem.addActionListener((ActionEvent e) -> jMenuItemAction(factory, com));

				if (parentMenuName.equals(SSHCommandFactory.PARENT_NULL)) {
					jMenuServer.add(jMenuItem);
				} else {
					parentMenu.add(jMenuItem);
					if (parentMenuName.equals(SSHCommandFactory.PARENT_OPSI)) {
						menuOpsi = parentMenu;
						jMenuServer.add(menuOpsi);
					} else {
						jMenuServer.add(parentMenu);
					}
				}
				jMenuItem.setEnabled(!isReadOnly && !commandsAreDeactivated);
			}
			if (firstParentGroup && commandsExist) {
				jMenuServer.addSeparator();
			}

			firstParentGroup = false;
		}
		if (menuOpsi.getSubElements().length != 0) {
			menuOpsi.addSeparator();
		}

		List<SSHCommand> commands = factory.getSSHCommandParameterList();
		Logging.info(this, "setupMenuServer add parameterDialogs to opsi commands" + commands);
		for (final SSHCommand command : commands) {
			JMenuItem jMenuOpsiCommand = new JMenuItem();
			jMenuOpsiCommand.setText(command.getMenuText());
			jMenuOpsiCommand.setToolTipText(command.getToolTipText());
			jMenuOpsiCommand.addActionListener((ActionEvent e) -> jMenuOptionCommandAction(factory, command));
			if (!jMenuServer.isMenuComponent(menuOpsi)) {
				jMenuServer.add(menuOpsi);
			}
			menuOpsi.add(jMenuOpsiCommand);
			jMenuOpsiCommand.setEnabled(!isReadOnly && !commandsAreDeactivated);
		}

		Logging.info(this, "setupMenuServer create/read command menu configs");

		boolean userConfigExists = UserConfig.getCurrentUserConfig() != null;

		jMenuSSHConfig.setEnabled(userConfigExists
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_CONFIG_ACTIVE));

		Logging.info(this, "setupMenuServer create/read command menu configs current user config "
				+ UserConfig.getCurrentUserConfig());
		jMenuSSHCommandControl.setEnabled(userConfigExists
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDCONTROL_ACTIVE));

		jMenuSSHCommandControl.setEnabled(true);

		jMenuServer.setEnabled(userConfigExists && !isReadOnly
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_MENU_ACTIVE));
	}

	private void jMenuItemAction(SSHCommandFactory factory, SSHCommandTemplate com) {
		if (factory.getConnectionState().equals(SSHCommandFactory.NOT_CONNECTED)) {
			Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message") + " "
					+ factory.getConnectionState());
		} else if (factory.getConnectionState().equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED)) {
			Logging.error(this, Configed.getResourceValue("SSHConnection.connected_not_allowed.message"));
		} else if (factory.getConnectionState().equals(SSHCommandFactory.UNKNOWN)) {
			Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message") + " "
					+ factory.getConnectionState());
		} else {
			// Create new instance of the same command, so that further
			// modifications would not affect the original command.
			final SSHCommandTemplate c = new SSHCommandTemplate(com);
			remoteSSHExecAction(c);
		}
	}

	private void jMenuOptionCommandAction(SSHCommandFactory factory, SSHCommand command) {
		if (factory.getConnectionState().equals(SSHCommandFactory.NOT_CONNECTED)) {
			Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message") + " "
					+ factory.getConnectionState());
		} else if (factory.getConnectionState().equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED)) {
			Logging.error(this, Configed.getResourceValue("SSHConnection.connected_not_allowed.message"));
		} else if (factory.getConnectionState().equals(SSHCommandFactory.UNKNOWN)) {
			Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message") + " "
					+ factory.getConnectionState());
		} else {
			remoteSSHExecAction(command);
		}
	}

	private void setupMenuGrouping() {
		jMenuClientselection.setText(Configed.getResourceValue("MainFrame.jMenuClientselection"));

		jMenuClientselectionGetGroup.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jMenuClientselectionGetGroup.addActionListener((ActionEvent e) -> callSelectionDialog());

		jMenuClientselectionGetSavedSearch
				.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		jMenuClientselectionGetSavedSearch
				.addActionListener((ActionEvent e) -> configedMain.clientSelectionGetSavedSearch());

		jMenuClientselectionProductNotUptodate
				.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersion"));
		jMenuClientselectionProductNotUptodate.addActionListener((ActionEvent e) -> groupByNotCurrentProductVersion());

		jMenuClientselectionProductNotUptodateOrBroken.setText(Configed
				.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersionOrUnknownState"));
		jMenuClientselectionProductNotUptodateOrBroken
				.addActionListener((ActionEvent e) -> groupByNotCurrentProductVersionOrBrokenInstallation());

		jMenuClientselectionFailedProduct
				.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedForProduct"));
		jMenuClientselectionFailedProduct.addActionListener((ActionEvent e) -> groupByFailedProduct());

		jMenuClientselectionFailedInPeriod
				.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedInTimespan"));

		for (Entry<String, String> entry : searchedTimeSpansText.entrySet()) {
			JMenuItem item = new JMenuItem(entry.getValue());

			item.addActionListener((ActionEvent e) -> configedMain
					.selectClientsByFailedAtSomeTimeAgo(searchedTimeSpans.get(entry.getKey())));

			jMenuClientselectionFailedInPeriod.add(item);
		}

		jMenuClientselectionToggleClientFilter
				.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionToggleClientFilter"));
		jMenuClientselectionToggleClientFilter.setState(false);
		jMenuClientselectionToggleClientFilter.addActionListener((ActionEvent e) -> toggleClientFilterAction());

		jMenuClientselection.add(jMenuClientselectionGetGroup);
		jMenuClientselection.add(jMenuClientselectionGetSavedSearch);

		jMenuClientselection.addSeparator();

		jMenuClientselection.add(jMenuClientselectionProductNotUptodate);
		jMenuClientselection.add(jMenuClientselectionProductNotUptodateOrBroken);
		jMenuClientselection.add(jMenuClientselectionFailedProduct);
		jMenuClientselection.add(jMenuClientselectionFailedInPeriod);

		// ----------
		jMenuClientselection.addSeparator();
		jMenuClientselection.add(jMenuClientselectionToggleClientFilter);
	}

	private void setupMenuFrames() {
		jMenuFrames.setText(Configed.getResourceValue("MainFrame.jMenuFrames"));

		jMenuFrameWorkOnGroups.setText(Configed.getResourceValue("MainFrame.jMenuFrameWorkOnGroups"));
		jMenuFrameWorkOnGroups.setEnabled(persistenceController.getModuleDataService().isWithLocalImagingPD());
		jMenuFrameWorkOnGroups.addActionListener(event -> configedMain.handleGroupActionRequest());

		jMenuFrameWorkOnProducts.setText(Configed.getResourceValue("MainFrame.jMenuFrameWorkOnProducts"));
		jMenuFrameWorkOnProducts.addActionListener(event -> configedMain.handleProductActionRequest());

		jMenuFrameDashboard.setText(Configed.getResourceValue("Dashboard.title"));
		jMenuFrameDashboard.addActionListener(event -> configedMain.initDashInfo());

		jMenuFrameLicenses.setText(Configed.getResourceValue("MainFrame.jMenuFrameLicenses"));
		jMenuFrameLicenses.setEnabled(false);
		jMenuFrameLicenses.addActionListener(event -> configedMain.handleLicensesManagementRequest());

		jMenuFrameShowDialogs.setText(Configed.getResourceValue("MainFrame.jMenuFrameShowDialogs"));
		jMenuFrameShowDialogs.setEnabled(false);
		jMenuFrameShowDialogs.addActionListener((ActionEvent e) -> {
			Logging.info(this, "actionPerformed");
			executeCommandOnInstances("arrange", FEditObject.runningInstances.getAll());
		});

		jMenuFrameTerminal.setText(Configed.getResourceValue("Terminal.title"));
		jMenuFrameTerminal.addActionListener((ActionEvent e) -> {
			configedMain.initMessagebus();
			TerminalFrame terminal = new TerminalFrame();
			terminal.setMessagebus(configedMain.getMessagebus());
			terminal.display();
		});

		jMenuFrames.add(jMenuFrameWorkOnGroups);
		jMenuFrames.add(jMenuFrameWorkOnProducts);
		if (ServerFacade.isOpsi43()) {
			jMenuFrames.add(jMenuFrameDashboard);
		}
		jMenuFrames.add(jMenuFrameLicenses);
		if (ServerFacade.isOpsi43()) {
			jMenuFrames.add(jMenuFrameTerminal);
		}
		jMenuFrames.addSeparator();
		jMenuFrames.add(jMenuFrameShowDialogs);
	}

	public static void addHelpLinks(JMenu jMenuHelp) {
		JMenuItem jMenuHelpDoc = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuDoc"));
		jMenuHelpDoc.addActionListener(actionEvent -> Utils.showExternalDocument(Globals.OPSI_DOC_PAGE));
		jMenuHelp.add(jMenuHelpDoc);

		JMenuItem jMenuHelpForum = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuForum"));
		jMenuHelpForum.addActionListener(actionEvent -> Utils.showExternalDocument(Globals.OPSI_FORUM_PAGE));
		jMenuHelp.add(jMenuHelpForum);

		JMenuItem jMenuHelpSupport = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuSupport"));
		jMenuHelpSupport.addActionListener(actionEvent -> Utils.showExternalDocument(Globals.OPSI_SUPPORT_PAGE));
		jMenuHelp.add(jMenuHelpSupport);
	}

	private void setupMenuHelp() {
		jMenuHelp.setText(Configed.getResourceValue("MainFrame.jMenuHelp"));

		addHelpLinks(jMenuHelp);

		jMenuHelp.addSeparator();

		jMenuHelpOpsiVersion.setText(
				Configed.getResourceValue("MainFrame.jMenuHelpOpsiService") + ": " + ServerFacade.getServerVersion());
		jMenuHelpOpsiVersion.setEnabled(false);

		jMenuHelp.add(jMenuHelpOpsiVersion);

		jMenuHelpOpsiModuleInformation.setText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jMenuHelpOpsiModuleInformation.addActionListener((ActionEvent e) -> showOpsiModules());

		jMenuHelp.add(jMenuHelpOpsiModuleInformation);

		jMenuHelpInternalConfiguration.setText(Configed.getResourceValue("MainFrame.jMenuHelpInternalConfiguration"));
		jMenuHelpInternalConfiguration.addActionListener((ActionEvent e) -> showBackendConfigurationAction());

		if (!ServerFacade.isOpsi43()) {
			jMenuHelp.add(jMenuHelpInternalConfiguration);
		}

		addLogfileMenus(jMenuHelp, this);

		jMenuHelpCheckHealth.setText(Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		jMenuHelpCheckHealth.addActionListener((ActionEvent e) -> showHealthDataAction());

		if (ServerFacade.isOpsi43()) {
			jMenuHelp.add(jMenuHelpCheckHealth);
		}

		jMenuHelp.addSeparator();

		jMenuHelpCredits.setText(Configed.getResourceValue("MainFrame.jMenuHelpCredits"));
		jMenuHelpCredits.addActionListener((ActionEvent e) -> FCreditsDialog.display(this));
		jMenuHelp.add(jMenuHelpCredits);

		jMenuHelpAbout.setText(Configed.getResourceValue("MainFrame.jMenuHelpAbout"));
		jMenuHelpAbout.addActionListener((ActionEvent e) -> Utils.showAboutAction(this));
		jMenuHelp.add(jMenuHelpAbout);
	}

	public static void addLogfileMenus(JMenu jMenuHelp, JFrame centerFrame) {
		JMenu jMenuHelpLoglevel = new JMenu(Configed.getResourceValue("MainFrame.jMenuLoglevel"));

		for (int i = Logging.LEVEL_NONE; i <= Logging.LEVEL_SECRET; i++) {
			rbLoglevelItems[i] = new JRadioButtonMenuItem(
					"[" + i + "] " + Logging.levelText(i).toLowerCase(Locale.ROOT));

			jMenuHelpLoglevel.add(rbLoglevelItems[i]);
			if (i == Logging.getLogLevelConsole()) {
				rbLoglevelItems[i].setSelected(true);
			}

			rbLoglevelItems[i].addActionListener(MainFrame::applyLoglevel);
		}

		jMenuHelp.add(jMenuHelpLoglevel);

		JMenuItem jMenuHelpLogfileLocation = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuHelpLogfileLocation"));
		jMenuHelpLogfileLocation.addActionListener((ActionEvent e) -> showLogfileLocationAction(centerFrame));

		jMenuHelp.add(jMenuHelpLogfileLocation);
	}

	// ------------------------------------------------------------------------------------------
	// icon pane
	private void setupIcons1() {
		iconButtonReload = new IconButton(Configed.getResourceValue("MainFrame.jMenuFileReload"), "images/reload.gif",
				"images/reload_over.gif", "");
		iconButtonReload.setFocusable(false);

		iconButtonReloadLicenses = new IconButton(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"),
				"images/reload_licenses.png", "images/reload_licenses_over.png", "", true);
		iconButtonReloadLicenses.setFocusable(false);
		iconButtonReloadLicenses.setVisible(false);

		iconButtonNewClient = new IconButton(Configed.getResourceValue("MainFrame.iconButtonNewClient"),
				"images/newClient.gif", "images/newClient_over.gif", "");
		iconButtonNewClient.setFocusable(false);

		iconButtonSetGroup = new IconButton(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"),
				"images/setGroup.gif", "images/setGroup_over.gif", "");
		iconButtonSetGroup.setFocusable(false);

		iconButtonSaveConfiguration = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
				"images/apply.png", "", "images/apply_disabled.png", false);
		iconButtonSaveConfiguration.setFocusable(false);

		iconButtonToggleClientFilter = new IconButton(
				Configed.getResourceValue("MainFrame.iconButtonToggleClientFilter"),
				"images/view-filter_disabled-32.png", "images/view-filter_over-32.png", "images/view-filter-32.png",
				true);
		iconButtonToggleClientFilter.setFocusable(false);

		iconButtonReachableInfo = new IconButton(Configed.getResourceValue("MainFrame.iconButtonReachableInfo"),
				"images/new_networkconnection.png", "images/new_networkconnection.png",
				"images/new_networkconnection.png", configedMain.getHostDisplayFields().get("clientConnected"));
		iconButtonReachableInfo.setFocusable(false);

		iconButtonSessionInfo = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSessionInfo"),
				"images/system-users-query.png", "images/system-users-query_over.png",
				"images/system-users-query_over.png",
				configedMain.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));
		iconButtonSessionInfo.setFocusable(false);
		iconButtonSessionInfo.setEnabled(true);

		iconButtonReload.addActionListener((ActionEvent e) -> reloadAction());

		iconButtonReloadLicenses.addActionListener((ActionEvent e) -> reloadLicensesAction());

		iconButtonNewClient.addActionListener((ActionEvent e) -> addClientAction());

		iconButtonSetGroup.addActionListener((ActionEvent e) -> callSelectionDialog());

		iconButtonSaveConfiguration.addActionListener((ActionEvent e) -> saveAction());

		iconButtonToggleClientFilter.addActionListener((ActionEvent e) -> toggleClientFilterAction());

		iconButtonReachableInfo.addActionListener((ActionEvent e) -> getReachableInfo());

		iconButtonSessionInfo.addActionListener((ActionEvent e) -> {
			configedMain.setColumnSessionInfo(true);
			getSessionInfo();
		});
	}

	// ------------------------------------------------------------------------------------------
	// context menus

	private void setupPopupMenuClientsTab() {
		popupShowCreatedColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		popupShowCreatedColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CREATED_DISPLAY_FIELD_LABEL));
		popupShowCreatedColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CREATED_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowCreatedColumn.setSelected(popupShowCreatedColumn.isSelected());
		});

		popupShowWANactiveColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		popupShowWANactiveColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

		popupShowWANactiveColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowWANactiveColumn.setSelected(popupShowWANactiveColumn.isSelected());
		});

		popupShowIPAddressColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		popupShowIPAddressColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));
		popupShowIPAddressColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowIPAddressColumn.setSelected(popupShowIPAddressColumn.isSelected());
		});

		popupShowSystemUUIDColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowSystemUUIDColumn"));
		popupShowSystemUUIDColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL));
		popupShowSystemUUIDColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowSystemUUIDColumn.setSelected(popupShowSystemUUIDColumn.isSelected());
		});

		popupShowHardwareAddressColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		popupShowHardwareAddressColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));
		popupShowHardwareAddressColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowHardwareAddressColumn.setSelected(popupShowHardwareAddressColumn.isSelected());
		});

		popupShowSessionInfoColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		popupShowSessionInfoColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));
		popupShowSessionInfoColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowSessionInfoColumn.setSelected(popupShowSessionInfoColumn.isSelected());
		});

		popupShowInventoryNumberColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		popupShowInventoryNumberColumn.setSelected(
				configedMain.getHostDisplayFields().get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));
		popupShowInventoryNumberColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowInventoryNumberColumn.setSelected(popupShowInventoryNumberColumn.isSelected());
		});

		popupShowUefiBoot.setText(Configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		popupShowUefiBoot
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));
		popupShowUefiBoot.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowUefiBoot.setSelected(popupShowUefiBoot.isSelected());
		});

		popupShowInstallByShutdownColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		popupShowInstallByShutdownColumn.setSelected(
				configedMain.getHostDisplayFields().get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));
		popupShowInstallByShutdownColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowInstallByShutdown.setSelected(popupShowInstallByShutdownColumn.isSelected());
		});

		popupShowDepotColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		popupShowDepotColumn
				.setSelected(configedMain.getHostDisplayFields().get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));
		popupShowDepotColumn.addActionListener((ActionEvent e) -> {
			configedMain.toggleColumn(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL);
			jCheckBoxMenuItemShowDepotColumn.setSelected(popupShowDepotColumn.isSelected());
		});

		popupChangeDepot.setText(Configed.getResourceValue("MainFrame.jMenuChangeDepot"));

		popupChangeDepot.addActionListener((ActionEvent e) -> changeDepotAction());

		popupChangeClientID.setText(Configed.getResourceValue("MainFrame.jMenuChangeClientID"));

		popupChangeClientID.addActionListener((ActionEvent e) -> changeClientIDAction());

		popupResetLocalbootProductOnClientWithStates
				.setText(Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithStates"));

		popupResetLocalbootProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, true, false));

		popupResetLocalbootProductOnClient
				.setText(Configed.getResourceValue("MainFrame.jMenuResetLocalbootProductOnClientWithoutStates"));

		popupResetLocalbootProductOnClient
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, true, false));

		popupResetNetbootProductOnClientWithStates
				.setText(Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithStates"));

		popupResetNetbootProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, false, true));

		popupResetNetbootProductOnClient
				.setText(Configed.getResourceValue("MainFrame.jMenuResetNetbootProductOnClientWithoutStates"));

		popupResetNetbootProductOnClient
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, false, true));

		popupResetProductOnClientWithStates
				.setText(Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithStates"));

		popupResetProductOnClientWithStates
				.addActionListener((ActionEvent e) -> resetProductOnClientAction(true, true, true));

		popupResetProductOnClient
				.setText(Configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithoutStates"));

		popupResetProductOnClient.addActionListener((ActionEvent e) -> resetProductOnClientAction(false, true, true));

		popupAddClient.setText(Configed.getResourceValue("MainFrame.jMenuAddClient"));

		popupAddClient.addActionListener((ActionEvent e) -> addClientAction());

		menuItemsHost.get(ITEM_ADD_CLIENT).add(popupAddClient);

		popupWakeOnLan.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan"));

		popupWakeOnLanDirect.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.direct"));
		popupWakeOnLanDirect.addActionListener((ActionEvent e) -> wakeOnLanAction());
		popupWakeOnLan.add(popupWakeOnLanDirect);

		popupWakeOnLanScheduler.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler"));
		popupWakeOnLanScheduler.addActionListener((ActionEvent e) -> {
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(Configed.getResourceValue("FStartWakeOnLan.title"),
					configedMain);
			fStartWakeOnLan.setLocationRelativeTo(this);

			fStartWakeOnLan.setVisible(true);
			fStartWakeOnLan.setPredefinedDelays(labelledDelays);

			fStartWakeOnLan.setClients();
		});
		popupWakeOnLan.add(popupWakeOnLanScheduler);

		popupDeletePackageCaches.setText(Configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
		popupDeletePackageCaches.addActionListener((ActionEvent e) -> deletePackageCachesAction());

		popupShowPopupMessage.setText(Configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
		popupShowPopupMessage.addActionListener((ActionEvent e) -> showPopupOnClientsAction());

		popupRequestSessionInfo.setText(Configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
		popupRequestSessionInfo.addActionListener((ActionEvent e) -> {
			configedMain.setColumnSessionInfo(true);
			getSessionInfo();
		});

		popupShutdownClient.setText(Configed.getResourceValue("MainFrame.jMenuShutdownClient"));
		popupShutdownClient.addActionListener((ActionEvent e) -> shutdownClientsAction());

		popupRebootClient.setText(Configed.getResourceValue("MainFrame.jMenuRebootClient"));
		popupRebootClient.addActionListener((ActionEvent e) -> rebootClientsAction());

		popupDeleteClient.setText(Configed.getResourceValue("MainFrame.jMenuDeleteClient"));
		popupDeleteClient.addActionListener((ActionEvent e) -> deleteClientAction());

		menuItemsHost.get(ITEM_DELETE_CLIENT).add(popupDeleteClient);

		popupCopyClient.setText(Configed.getResourceValue("MainFrame.jMenuCopyClient"));
		popupCopyClient.addActionListener((ActionEvent e) -> copyClientAction());

		menuItemsHost.get(ITEM_DELETE_CLIENT).add(popupCopyClient);

		popupFreeLicenses.setText(Configed.getResourceValue("MainFrame.jMenuFreeLicenses"));
		popupFreeLicenses.addActionListener((ActionEvent e) -> freeLicensesAction());

		menuItemsHost.get(ITEM_FREE_LICENSES).add(popupFreeLicenses);

		popupRemoteControl.setText(Configed.getResourceValue("MainFrame.jMenuRemoteControl"));

		popupRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		popupRemoteControl.addActionListener((ActionEvent e) -> panelClientlist.startRemoteControlForSelectedClients());

		popupSelectionGetGroup.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		popupSelectionGetGroup.addActionListener((ActionEvent e) -> callSelectionDialog());

		popupSelectionGetSavedSearch.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		popupSelectionGetSavedSearch.addActionListener((ActionEvent e) -> configedMain.clientSelectionGetSavedSearch());

		// pdf generating
		popupCreatePdf.setText(Configed.getResourceValue("FGeneralDialog.pdf"));
		popupCreatePdf.addActionListener((ActionEvent e) -> createPdf());
		//

		popupSelectionToggleClientFilter
				.setText(Configed.getResourceValue("MainFrame.jMenuClientselectionToggleClientFilter"));
		popupSelectionToggleClientFilter.setState(false);

		popupSelectionToggleClientFilter.addActionListener((ActionEvent e) -> toggleClientFilterAction());

		popupRebuildClientList.addActionListener((ActionEvent e) -> configedMain.reloadHosts());

		// ----

		popupClients.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// Nothing to do.
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				// Nothing to do.
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				enableMenuItemsForClients();
			}
		});

		popupClients.add(popupWakeOnLan);

		JMenu menuPopupOpsiClientdEvent = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : persistenceController.getConfigDataService().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItem(event);

			item.addActionListener((ActionEvent e) -> fireOpsiclientdEventAction(event));

			menuPopupOpsiClientdEvent.add(item);
		}

		popupClients.add(menuPopupOpsiClientdEvent);
		popupClients.add(popupShowPopupMessage);
		popupClients.add(popupRequestSessionInfo);
		popupClients.add(popupDeletePackageCaches);

		popupClients.addSeparator();

		popupClients.add(popupShutdownClient);
		popupClients.add(popupRebootClient);
		popupClients.add(popupRemoteControl);

		popupClients.addSeparator();

		// --
		popupClients.add(popupAddClient);
		if (ServerFacade.isOpsi43()) {
			popupClients.add(popupCopyClient);
		}
		popupClients.add(popupDeleteClient);

		popupResetProducts.add(popupResetLocalbootProductOnClientWithStates);
		popupResetProducts.add(popupResetLocalbootProductOnClient);
		popupResetProducts.add(popupResetNetbootProductOnClientWithStates);
		popupResetProducts.add(popupResetNetbootProductOnClient);
		popupResetProducts.add(popupResetProductOnClientWithStates);
		popupResetProducts.add(popupResetProductOnClient);
		popupClients.add(popupResetProducts);

		popupClients.add(popupFreeLicenses);
		popupClients.add(popupChangeClientID);
		if (multidepot) {
			popupClients.add(popupChangeDepot);
		}

		// ----
		popupClients.addSeparator();
		popupClients.add(popupSelectionGetGroup);
		popupClients.add(popupSelectionGetSavedSearch);

		popupClients.addSeparator();
		popupClients.add(popupSelectionToggleClientFilter);

		popupClients.add(popupRebuildClientList);
		popupClients.add(popupCreatePdf);

		exportTable.addMenuItemsTo(popupClients);

		ClientTableExporterToCSV clientTableExporter = new ClientTableExporterToCSV(panelClientlist.getTable());
		clientTableExporter.addMenuItemsTo(popupClients);

		// ----
		popupClients.addSeparator();

		popupShowColumns.setText(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		popupShowColumns.add(popupShowWANactiveColumn);
		popupShowColumns.add(popupShowIPAddressColumn);
		if (ServerFacade.isOpsi43()) {
			popupShowColumns.add(popupShowSystemUUIDColumn);
		}
		popupShowColumns.add(popupShowHardwareAddressColumn);
		popupShowColumns.add(popupShowSessionInfoColumn);
		popupShowColumns.add(popupShowInventoryNumberColumn);
		popupShowColumns.add(popupShowCreatedColumn);

		if (!ServerFacade.isOpsi43()) {
			popupShowColumns.add(popupShowUefiBoot);
		}

		popupShowColumns.add(popupShowInstallByShutdownColumn);
		popupShowColumns.add(popupShowDepotColumn);

		popupClients.add(popupShowColumns);
	}

	private static void applyLoglevel(ActionEvent actionEvent) {
		for (int i = Logging.LEVEL_NONE; i <= Logging.LEVEL_SECRET; i++) {
			if (actionEvent.getSource() == rbLoglevelItems[i]) {
				rbLoglevelItems[i].setSelected(true);
				Logging.setLogLevel(i);
			} else {
				if (rbLoglevelItems[i] != null) {
					rbLoglevelItems[i].setSelected(false);
				}
			}
		}
	}

	private void createPdf() {
		TableModel tm = configedMain.getSelectedClientsTableModel();
		JTable jTable = new JTable(tm);

		Map<String, String> metaData = new HashMap<>();
		String title = Configed.getResourceValue("MainFrame.ClientList");

		if (statusPane.getGroupName().length() != 0) {
			title = title + ": " + statusPane.getGroupName();
		}
		metaData.put("header", title);
		title = "";
		if (statusPane.getInvolvedDepots().length() != 0) {
			title = title + "Depot(s) : " + statusPane.getInvolvedDepots();
		}

		metaData.put("title", title);
		metaData.put("subject", "report of table");
		metaData.put("keywords", "");

		ExporterToPDF pdfExportTable = new ExporterToPDF(panelClientlist.getTable());

		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(null, jTable.getSelectedRowCount() != 0);
	}

	// ------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------

	public void updateHostCheckboxenText() {
		if (persistenceController.getModuleDataService().isWithUEFIPD()) {
			cbUefiBoot.setText(Configed.getResourceValue("NewClientDialog.boottype"));
		} else {
			cbUefiBoot.setText(Configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			cbUefiBoot.setEnabled(false);
		}

		if (persistenceController.getModuleDataService().isWithWANPD()) {
			cbWANConfig.setText(Configed.getResourceValue("NewClientDialog.wanConfig"));
		} else {
			cbWANConfig.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
			cbWANConfig.setEnabled(false);
		}
	}

	private void guiInit() {
		this.addWindowListener(this);

		this.setIconImage(Utils.getMainIcon());

		JPanel allPanel = new JPanel();
		allPanel.addComponentListener(this);
		allPanel.setLayout(borderLayout1);

		getContentPane().add(allPanel);

		initMenuData();

		setupMenuLists();

		setupMenuFile();
		setupMenuGrouping();
		setupMenuClients();
		setupMenuServer();
		setupMenuFrames();
		setupMenuHelp();

		JMenuBar jMenuBar = new JMenuBar();
		jMenuBar.add(jMenuFile);
		jMenuBar.add(jMenuClientselection);
		jMenuBar.add(jMenuClients);
		jMenuBar.add(jMenuServer);
		jMenuBar.add(jMenuFrames);
		jMenuBar.add(jMenuHelp);

		this.setJMenuBar(jMenuBar);

		setupPopupMenuClientsTab();

		// clientPane
		labelHostID = new JLabel();

		labelHostID.setFont(labelHostID.getFont().deriveFont(Font.BOLD));

		JLabel labelClientDescription = new JLabel(Configed.getResourceValue("MainFrame.jLabelDescription"));
		labelClientDescription.setPreferredSize(Globals.BUTTON_DIMENSION);
		JLabel labelClientInventoryNumber = new JLabel(Configed.getResourceValue("MainFrame.jLabelInventoryNumber"));
		labelClientInventoryNumber.setPreferredSize(Globals.BUTTON_DIMENSION);
		JLabel labelClientNotes = new JLabel(Configed.getResourceValue("MainFrame.jLabelNotes"));
		JLabel labelClientSystemUUID = new JLabel(Configed.getResourceValue("MainFrame.jLabelSystemUUID"));
		labelClientSystemUUID.setVisible(ServerFacade.isOpsi43());
		JLabel labelClientMacAddress = new JLabel(Configed.getResourceValue("MainFrame.jLabelMacAddress"));
		JLabel labelClientIPAddress = new JLabel(Configed.getResourceValue("MainFrame.jLabelIPAddress"));
		JLabel labelOneTimePassword = new JLabel(Configed.getResourceValue("MainFrame.jLabelOneTimePassword"));
		JLabel labelOpsiHostKey = new JLabel("opsiHostKey");

		jTextFieldDescription = new JTextEditorField("");
		jTextFieldDescription.setEditable(true);
		jTextFieldDescription.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);
		jTextFieldDescription.addKeyListener(this);
		jTextFieldDescription.addMouseListener(this);

		jTextFieldInventoryNumber = new JTextEditorField("");
		jTextFieldInventoryNumber.setEditable(true);
		jTextFieldInventoryNumber.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);
		jTextFieldInventoryNumber.addKeyListener(this);
		jTextFieldInventoryNumber.addMouseListener(this);

		jTextAreaNotes = new JTextArea();

		jTextAreaNotes.setEditable(true);
		jTextAreaNotes.setLineWrap(true);
		jTextAreaNotes.setWrapStyleWord(true);

		jTextAreaNotes.addKeyListener(this);
		jTextAreaNotes.addMouseListener(this);

		JScrollPane scrollpaneNotes = new JScrollPane(jTextAreaNotes);
		scrollpaneNotes.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);
		scrollpaneNotes.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpaneNotes.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		systemUUIDField = new JTextEditorField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3',
				'4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36, Character.MIN_VALUE, 36, true),
				"", 36);

		systemUUIDField.addKeyListener(this);
		systemUUIDField.addMouseListener(this);
		systemUUIDField.setVisible(ServerFacade.isOpsi43());

		macAddressField = new JTextEditorField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3',
				'4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':', 2, true), "", 17);

		macAddressField.addKeyListener(this);
		macAddressField.addMouseListener(this);

		ipAddressField = new JTextEditorField(
				new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
						'.', 'a', 'b', 'c', 'd', 'e', 'f', ':' }, 28, Character.MIN_VALUE, 4, false),
				"", 24);
		ipAddressField.addKeyListener(this);
		ipAddressField.addMouseListener(this);

		final Icon unselectedIcon;
		final Icon selectedIcon;
		final Icon nullIcon;

		unselectedIcon = Utils.createImageIcon("images/checked_not.png", "");
		selectedIcon = Utils.createImageIcon("images/checked.png", "");
		nullIcon = Utils.createImageIcon("images/checked_box_mixed.png", "");

		cbUefiBoot = new CheckedLabel(Configed.getResourceValue("NewClientDialog.boottype"), selectedIcon,
				unselectedIcon, nullIcon, false);
		if (!ServerFacade.isOpsi43()) {
			cbUefiBoot.addActionListener(event -> uefiBootAction());
		}

		cbWANConfig = new CheckedLabel(Configed.getResourceValue("NewClientDialog.wan_not_activated"), selectedIcon,
				unselectedIcon, nullIcon, false);
		cbWANConfig.setEnabled(true);
		cbWANConfig.addActionListener(event -> wanConfigAction());

		cbInstallByShutdown = new CheckedLabel(Configed.getResourceValue("NewClientDialog.installByShutdown"),
				selectedIcon, unselectedIcon, nullIcon, false);
		cbInstallByShutdown.setEnabled(true);
		cbInstallByShutdown.addActionListener(event -> installByShutdownAction());

		updateHostCheckboxenText();

		jTextFieldOneTimePassword = new JTextEditorField("");
		jTextFieldOneTimePassword.addKeyListener(this);
		jTextFieldOneTimePassword.addMouseListener(this);

		jTextFieldHostKey = new JTextHideField();

		clientPane = new JPanel();

		GroupLayout layoutClientPane = new GroupLayout(clientPane);
		clientPane.setLayout(layoutClientPane);
		layoutClientPane.setHorizontalGroup(layoutClientPane.createParallelGroup()

				/////// HOST
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE)
						.addComponent(labelHostID, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE))

				/////// DESCRIPTION
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(jTextFieldDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// INVENTORY NUMBER
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(jTextFieldInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// SYSTEM UUID
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientSystemUUID, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(systemUUIDField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
						Globals.FIRST_LABEL_WIDTH)

				/////// MAC ADDRESS
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientMacAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(macAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
						Globals.FIRST_LABEL_WIDTH)

				/////// IP ADDRESS
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientIPAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(ipAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
						Globals.FIRST_LABEL_WIDTH)

				/////// INSTALL BY SHUTDOWN
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(cbInstallByShutdown, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// UEFI BOOT
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(cbUefiBoot,
						0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// WAN CONFIG
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(cbWANConfig, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// ONE TIME PASSWORD
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(jTextFieldOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				////// opsiHostKey
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelOpsiHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(jTextFieldHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// NOTES
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(scrollpaneNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutClientPane.setVerticalGroup(layoutClientPane.createSequentialGroup()
				/////// HOST
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelHostID, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// DESCRIPTION
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldDescription, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// INVENTORY NUMBER
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldInventoryNumber, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// SYSTEM UUID
				.addGap(ServerFacade.isOpsi43() ? Globals.MIN_GAP_SIZE : 0,
						ServerFacade.isOpsi43() ? Globals.MIN_GAP_SIZE : 0,
						ServerFacade.isOpsi43() ? Globals.MIN_GAP_SIZE : 0)
				.addComponent(labelClientSystemUUID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(systemUUIDField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// MAC ADDRESS
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(macAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// IP ADDRESS
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientIPAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(ipAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				////// INSTALL BY SHUTDOWN
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(cbInstallByShutdown, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// UEFI BOOT & WAN Config
				.addComponent(cbUefiBoot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(cbWANConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// ONE TIME PASSWORD
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelOneTimePassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldOneTimePassword, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				////// opsiHostKey
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelOpsiHostKey, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldHostKey, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// NOTES
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpaneNotes, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		JScrollPane scrollpaneTreeClients = new JScrollPane();
		scrollpaneTreeClients.getViewport().add(treeClients);
		scrollpaneTreeClients.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setPreferredSize(treeClients.getMaximumSize());

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimum() "
				+ scrollpaneTreeClients.getVerticalScrollBar().getMinimum());

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() "
				+ scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize());

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() "
				+ scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize());

		JScrollPane scrollpaneTreeProducts = new JScrollPane();
		scrollpaneTreeProducts.getViewport().add(treeProducts);
		scrollpaneTreeProducts.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeProducts.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeProducts.setPreferredSize(treeProducts.getMaximumSize());

		JTabbedPane jTabbedPaneClientSelection = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		jTabbedPaneClientSelection.addTab("Depots", depotListPresenter);
		jTabbedPaneClientSelection.addTab("Clients", scrollpaneTreeClients);
		jTabbedPaneClientSelection.addTab("Produkte", scrollpaneTreeProducts);

		jTabbedPaneClientSelection.setSelectedIndex(1);
		jTabbedPaneClientSelection.setBorder(new EmptyBorder(0, Globals.MIN_GAP_SIZE, 0, 0));

		jButtonServerConfiguration = new JButton(Utils.createImageIcon("images/opsiconsole_deselected.png", ""));
		jButtonServerConfiguration.setSelectedIcon(Utils.createImageIcon("images/opsiconsole.png", ""));
		jButtonServerConfiguration.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));
		jButtonServerConfiguration.setFocusable(false);

		jButtonDepotsConfiguration = new JButton(Utils.createImageIcon("images/opsidepots_deselected.png", ""));
		jButtonDepotsConfiguration.setSelectedIcon(Utils.createImageIcon("images/opsidepots.png", ""));
		jButtonDepotsConfiguration.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelDepotsConfiguration"));
		jButtonDepotsConfiguration.setFocusable(false);

		jButtonClientsConfiguration = new JButton(Utils.createImageIcon("images/opsiclients_deselected.png", ""));
		jButtonClientsConfiguration.setSelectedIcon(Utils.createImageIcon("images/opsiclients.png", ""));
		jButtonClientsConfiguration.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonClientsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
		jButtonClientsConfiguration.setFocusable(false);

		jButtonLicenses = new JButton(Utils.createImageIcon("images/licenses_deselected.png", ""));
		jButtonLicenses.setEnabled(false);
		jButtonLicenses.setSelectedIcon(Utils.createImageIcon("images/licenses.png", ""));
		jButtonLicenses.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelLicenses"));
		jButtonLicenses.setFocusable(false);

		jButtonServerConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.SERVER));
		jButtonDepotsConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.DEPOTS));
		jButtonClientsConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.CLIENTS));
		jButtonLicenses.addActionListener(event -> configedMain.handleLicensesManagementRequest());

		JButton jButtonWorkOnGroups = new JButton(Utils.createImageIcon("images/group_all_unselected_40.png", ""));
		jButtonWorkOnGroups.setSelectedIcon(Utils.createImageIcon("images/group_all_selected_40.png", ""));
		jButtonWorkOnGroups.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonWorkOnGroups.setToolTipText(Configed.getResourceValue("MainFrame.jMenuFrameWorkOnGroups"));
		jButtonWorkOnGroups.setFocusable(false);

		jButtonWorkOnGroups.setEnabled(persistenceController.getModuleDataService().isWithLocalImagingPD());
		jButtonWorkOnGroups.addActionListener(event -> configedMain.handleGroupActionRequest());

		JButton jButtonWorkOnProducts = new JButton(Utils.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setSelectedIcon(Utils.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonWorkOnProducts.setToolTipText(Configed.getResourceValue("MainFrame.labelWorkOnProducts"));
		jButtonWorkOnProducts.setFocusable(false);

		jButtonWorkOnProducts.addActionListener(event -> configedMain.handleProductActionRequest());

		JButton jButtonDashboard = new JButton(Utils.createImageIcon("images/dash_unselected.png", ""));
		jButtonDashboard.setSelectedIcon(Utils.createImageIcon("images/dash_selected.png", ""));
		jButtonDashboard.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));
		jButtonDashboard.setFocusable(false);

		jButtonDashboard.setEnabled(ServerFacade.isOpsi43());
		jButtonDashboard.setVisible(ServerFacade.isOpsi43());
		jButtonDashboard.addActionListener(event -> configedMain.initDashInfo());

		if (persistenceController.getModuleDataService().isOpsiLicensingAvailablePD()
				&& persistenceController.getModuleDataService().isOpsiUserAdminPD() && licensingInfoMap == null) {
			licensingInfoMap = LicensingInfoMap.getInstance(
					persistenceController.getModuleDataService().getOpsiLicensingInfoOpsiAdminPD(),
					persistenceController.getConfigDataService().getConfigDefaultValuesPD(),
					!LicensingInfoDialog.isExtendedView());

			switch (licensingInfoMap.getWarningLevel()) {
			case LicensingInfoMap.STATE_OVER_LIMIT:
				jButtonOpsiLicenses = new JButton(Utils.createImageIcon("images/opsi-licenses-error-small.png", ""));
				break;
			case LicensingInfoMap.STATE_CLOSE_TO_LIMIT:
				jButtonOpsiLicenses = new JButton(Utils.createImageIcon("images/opsi-licenses-warning-small.png", ""));
				break;

			case LicensingInfoMap.STATE_OKAY:
				jButtonOpsiLicenses = new JButton(Utils.createImageIcon("images/opsi-licenses.png", ""));
				break;

			default:
				Logging.warning(this, "unexpected warninglevel: " + licensingInfoMap.getWarningLevel());
				break;
			}
		} else {
			jButtonOpsiLicenses = new JButton(Utils.createImageIcon("images/opsi-licenses.png", ""));
		}

		jButtonOpsiLicenses.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonOpsiLicenses.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jButtonOpsiLicenses.addActionListener(e -> showOpsiModules());
		jButtonOpsiLicenses.setFocusable(false);

		JPanel iconPaneTargets = new JPanel();

		GroupLayout layoutIconPaneTargets = new GroupLayout(iconPaneTargets);
		iconPaneTargets.setLayout(layoutIconPaneTargets);

		layoutIconPaneTargets.setHorizontalGroup(layoutIconPaneTargets.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		layoutIconPaneTargets.setVerticalGroup(layoutIconPaneTargets.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE));

		JPanel iconPaneExtraFrames = new JPanel();

		GroupLayout layoutIconPaneExtraFrames = new GroupLayout(iconPaneExtraFrames);
		iconPaneExtraFrames.setLayout(layoutIconPaneExtraFrames);

		layoutIconPaneExtraFrames.setHorizontalGroup(layoutIconPaneExtraFrames.createSequentialGroup()
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonDashboard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(ServerFacade.isOpsi43() ? Globals.GAP_SIZE : 0, ServerFacade.isOpsi43() ? Globals.GAP_SIZE : 0,
						ServerFacade.isOpsi43() ? Globals.GAP_SIZE : 0)
				.addComponent(jButtonOpsiLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(jButtonLicenses, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		layoutIconPaneExtraFrames.setVerticalGroup(layoutIconPaneExtraFrames.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonDashboard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonOpsiLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE));

		JPanel iconsTopRight = new JPanel();

		GroupLayout layoutIconPane0 = new GroupLayout(iconsTopRight);
		iconsTopRight.setLayout(layoutIconPane0);

		layoutIconPane0.setHorizontalGroup(
				layoutIconPane0.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE));

		layoutIconPane0.setVerticalGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		setupIcons1();
		JPanel iconsTopLeft = new JPanel();

		GroupLayout layoutIconPane1 = new GroupLayout(iconsTopLeft);
		iconsTopLeft.setLayout(layoutIconPane1);

		layoutIconPane1
				.setHorizontalGroup(
						layoutIconPane1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE).addComponent(iconButtonSessionInfo,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE));

		layoutIconPane1.setVerticalGroup(layoutIconPane1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonSessionInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		JPanel iconBarPane = new JPanel();
		iconBarPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;

		c.gridx = 0;
		c.gridy = 0;
		iconBarPane.add(iconsTopLeft, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 0;

		String logoPath;

		if (FlatLaf.isLafDark()) {
			logoPath = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_ohne_Text_quer_neg.png";
		} else {
			logoPath = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_kurz_quer.png";
		}

		iconBarPane.add(new JLabel(Utils.createImageIcon(logoPath, null, 150, 50)), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 2;
		c.gridy = 0;
		iconBarPane.add(iconsTopRight, c);

		jTabbedPaneConfigPanes = new JTabbedPane();
		jTabbedPaneConfigPanes.setBorder(new EmptyBorder(0, 0, 0, Globals.MIN_GAP_SIZE));

		JSplitPane centralPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, jTabbedPaneClientSelection,
				jTabbedPaneConfigPanes);
		centralPane.setDividerLocation(DIVIDER_LOCATION_CENTRAL_PANE);

		// statusPane

		statusPane = new HostsStatusPanel();

		allPanel.add(iconBarPane, BorderLayout.NORTH);
		allPanel.add(centralPane, BorderLayout.CENTER);
		allPanel.add(statusPane, BorderLayout.SOUTH);

		// tab panes

		jTabbedPaneConfigPanes.addChangeListener((ChangeEvent e) -> {
			// report state change request to
			int visualIndex = jTabbedPaneConfigPanes.getSelectedIndex();

			// report state change request to controller

			Logging.info(this, "stateChanged of tabbedPane, visualIndex " + visualIndex);
			configedMain.setViewIndex(visualIndex);

			// retrieve the state index finally produced by main
			int newStateIndex = configedMain.getViewIndex();

			// if the controller did not accept the new index set it back
			// observe that we get a recursion since we initiate another state change
			// the recursion breaks since main.setViewIndex does not yield a different value
			if (visualIndex != newStateIndex) {
				jTabbedPaneConfigPanes.setSelectedIndex(newStateIndex);
			}
		});

		// --- panel_Clientselection

		panelClientlist.addMouseListener(new PopupMouseListener(popupClients));

		panelClientSelection = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelClientlist, clientPane);

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.panel_Clientselection"), null,
				panelClientSelection, null, ConfigedMain.VIEW_CLIENTS);

		panelLocalbootProductSettings = new PanelProductSettings(
				Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), configedMain,
				configedMain.getDisplayFieldsLocalbootProducts());
		treeProducts.setLocalbootPanel(panelLocalbootProductSettings);

		panelNetbootProductSettings = new PanelProductSettings(
				Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), configedMain,
				configedMain.getDisplayFieldsNetbootProducts());
		treeProducts.setNetbootPanel(panelNetbootProductSettings);

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), null,
				panelLocalbootProductSettings, null, ConfigedMain.VIEW_LOCALBOOT_PRODUCTS);

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), null,
				panelNetbootProductSettings, null, ConfigedMain.VIEW_NETBOOT_PRODUCTS);

		panelHostConfig = new PanelHostConfig(configedMain);

		panelHostConfig.registerDataChangedObserver(configedMain.getHostConfigsDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), null,
				panelHostConfig, null, ConfigedMain.VIEW_NETWORK_CONFIGURATION);

		showHardwareLog = new JPanel();

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog"), null,
				showHardwareLog, null, ConfigedMain.VIEW_HARDWARE_INFO);

		initSoftWareInfo();
		initHardwareInfo();

		labelNoSoftware = new JLabel();

		showSoftwareLogNotFound = new JPanel(new FlowLayout());
		showSoftwareLogNotFound.add(labelNoSoftware);

		showSoftwareLog = showSoftwareLogNotFound;

		showSoftwareLogMultiClientReport = new PanelSWMultiClientReport();
		SwExporter swExporter = new SwExporter(showSoftwareLogMultiClientReport, panelSWInfo, configedMain);
		showSoftwareLogMultiClientReport.setActionListenerForStart(swExporter);

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog"), null,
				showSoftwareLog, null, ConfigedMain.VIEW_SOFTWARE_INFO);

		showLogfiles = new PanelTabbedDocuments(Utils.getLogTypes(),
				Configed.getResourceValue("MainFrame.DefaultTextForLogfiles"), configedMain) {
			@Override
			public void loadDocument(String logtype) {
				super.loadDocument(logtype);
				Logging.info(this, "loadDocument logtype " + logtype);
				setUpdatedLogfilePanel(logtype);
			}
		};

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_logfiles"), null, showLogfiles,
				null, ConfigedMain.VIEW_LOG);

		showLogfiles.addChangeListener((ChangeEvent e) -> {
			Logging.debug(this, " new logfiles tabindex " + showLogfiles.getSelectedIndex());

			String logtype = Utils.getLogType(showLogfiles.getSelectedIndex());

			// logfile empty?
			if (!configedMain.logfileExists(logtype)) {
				setUpdatedLogfilePanel(logtype);
			}
		});

		panelProductProperties = new PanelProductProperties(configedMain);

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"), null,
				panelProductProperties, null, ConfigedMain.VIEW_PRODUCT_PROPERTIES);

		Logging.info(this,
				"added tab  " + Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties") + " index "
						+ jTabbedPaneConfigPanes
								.indexOfTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		panelHostProperties = new PanelHostProperties();
		panelHostProperties.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties"), null,
				panelHostProperties, null, ConfigedMain.VIEW_HOST_PROPERTIES);

		Logging.info(this, "added tab  " + Configed.getResourceValue("MainFrame.jPanel_HostProperties") + " index "
				+ jTabbedPaneConfigPanes.indexOfTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties")));

		jTabbedPaneConfigPanes.setSelectedIndex(0);

		setTitle(configedMain.getAppTitle());

		glassPane = new GlassPane();
		setGlassPane(glassPane);
	}

	public void showPopupClients() {
		popupClients.show(panelClientlist, -1, -1);
	}

	public void setConfigPanesEnabled(boolean b) {
		for (int i = 0; i < jTabbedPaneConfigPanes.getTabCount(); i++) {
			jTabbedPaneConfigPanes.setEnabledAt(i, b);
		}
	}

	public void setVisualViewIndex(int i) {
		if (i >= 0 && i < jTabbedPaneConfigPanes.getTabCount()) {
			jTabbedPaneConfigPanes.setSelectedIndex(i);
		}
	}

	public void setConfigPaneEnabled(int tabindex, boolean b) {
		jTabbedPaneConfigPanes.setEnabledAt(tabindex, b);
	}

	public int getTabIndex(String tabname) {
		return jTabbedPaneConfigPanes.indexOfTab(tabname);
	}

	// -- helper methods for interaction
	public void saveConfigurationsSetEnabled(boolean b) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService().isGlobalReadOnly()
				&& b) {
			return;
		}

		Logging.debug(this, "saveConfigurationsSetEnabled " + b);

		jMenuFileSaveConfigurations.setEnabled(b);
		iconButtonSaveConfiguration.setEnabled(b);
	}

	// ----------------------------------------------------------------------------------------
	// action methods for visual interactions

	private void wakeOnLanAction() {
		configedMain.wakeSelectedClients();
	}

	private void deletePackageCachesAction() {
		configedMain.deletePackageCachesOfSelectedClients();
	}

	private void fireOpsiclientdEventAction(String event) {
		configedMain.fireOpsiclientdEventOnSelectedClients(event);
	}

	private void showPopupOnClientsAction() {
		FEditTextWithExtra fText = new FEditTextWithExtra("", Configed.getResourceValue("MainFrame.writePopupMessage"),
				Configed.getResourceValue("MainFrame.writePopupDuration")) {
			@Override
			protected void commit() {
				super.commit();
				Float duration = 0F;
				if (!getExtra().isEmpty()) {
					duration = Float.parseFloat(getExtra());
				}
				configedMain.showPopupOnSelectedClients(getText(), duration);
			}
		};

		fText.setTitle(Configed.getResourceValue("MainFrame.popupFrameTitle"));
		fText.init();
		fText.setLocationRelativeTo(this);
		fText.setVisible(true);
	}

	private void shutdownClientsAction() {
		configedMain.shutdownSelectedClients();
	}

	private void rebootClientsAction() {
		configedMain.rebootSelectedClients();
	}

	private void deleteClientAction() {
		configedMain.deleteSelectedClients();
	}

	private void copyClientAction() {
		configedMain.copySelectedClient();
	}

	private void freeLicensesAction() {
		Logging.info(this, "freeLicensesAction ");
		configedMain.freeAllPossibleLicensesForSelectedClients();
	}

	/**
	 * Calls method from configedMain to start the execution of given command
	 *
	 * @param SSHCommand command
	 */
	private void remoteSSHExecAction(SSHCommand command) {
		Logging.debug(this, "jMenuRemoteSSHExecAction");
		configedMain.startSSHOpsiServerExec(command);
	}

	/**
	 * Calls method from configedMain to start the config dialog
	 */
	private void startSSHConfigAction() {
		Logging.debug(this, "jMenuSSHConfigAction");
		configedMain.startSSHConfigDialog();
	}

	/**
	 * Calls method from configedMain to start the command control dialog
	 */
	private void startSSHControlAction() {
		Logging.debug(this, "jMenuSSHControlAction");
		configedMain.startSSHControlDialog();
	}

	public void setClientFilterAction(boolean b) {
		if (configedMain.isFilterClientList() != b) {
			toggleClientFilterAction();
		}
	}

	public void toggleClientFilterAction() {
		toggleClientFilterAction(true);
	}

	public void toggleClientFilterAction(boolean rebuildClientListTableModel) {
		configedMain.toggleFilterClientList(rebuildClientListTableModel);
		jMenuClientselectionToggleClientFilter.setState(configedMain.isFilterClientList());
		popupSelectionToggleClientFilter.setState(configedMain.isFilterClientList());
		iconButtonToggleClientFilter.setSelected(configedMain.isFilterClientList());
		panelClientlist.setFilterMark(configedMain.isFilterClientList());
	}

	private void exitAction() {
		configedMain.finishApp(true, 0);
	}

	private void saveAction() {
		configedMain.checkSaveAll(false);
	}

	private void getSessionInfo() {
		configedMain.getSessionInfo();
	}

	private void getReachableInfo() {
		iconButtonReachableInfo.setEnabled(false);

		SwingUtilities.invokeLater(configedMain::getReachableInfo);
	}

	private void callSelectionDialog() {
		configedMain.callClientSelectionDialog();
	}

	private void groupByNotCurrentProductVersion() {
		String products = getProduct(new ArrayList<>(new TreeSet<>(configedMain.getProductNames())));
		configedMain.selectClientsNotCurrentProductInstalled(products, false);
	}

	private void groupByNotCurrentProductVersionOrBrokenInstallation() {
		String products = getProduct(new ArrayList<>(new TreeSet<>(configedMain.getProductNames())));
		configedMain.selectClientsNotCurrentProductInstalled(products, true);
	}

	private void groupByFailedProduct() {
		String products = getProduct(new ArrayList<>(new TreeSet<>(configedMain.getProductNames())));
		configedMain.selectClientsWithFailedProduct(products);
	}

	private String getProduct(List<String> completeList) {
		FSelectionList fProductSelectionList = new FSelectionList(this,
				Configed.getResourceValue("MainFrame.productSelection"), true, new String[] { "", "" }, new Icon[] {
						Utils.createImageIcon("images/cancel.png", ""), Utils.createImageIcon("images/apply.png", "") },
				F_WIDTH / 2, 600);
		fProductSelectionList.setListData(completeList);
		fProductSelectionList.setVisible(true);
		return fProductSelectionList.getResult() == 2 ? fProductSelectionList.getSelectedValue() : "";
	}

	private void reloadAction() {
		configedMain.reload();
	}

	public void activateLoadingPane(String infoText) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> {
				glassPane.activate(true);
				glassPane.setInfoText(infoText);
			});
		} else {
			glassPane.activate(true);
			glassPane.setInfoText(infoText);
		}
	}

	public void deactivateLoadingPane() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> glassPane.activate(false));
		} else {
			glassPane.activate(false);
		}
	}

	public void activateLoadingCursor() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> setCursor(Globals.WAIT_CURSOR));
		} else {
			setCursor(Globals.WAIT_CURSOR);
		}
	}

	public void deactivateLoadingCursor() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> setCursor(null));
		} else {
			setCursor(null);
		}
	}

	private void reloadLicensesAction() {
		activateLoadingPane(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData") + " ...");
		new Thread() {
			@Override
			public void run() {
				configedMain.reloadLicensesData();
				ConfigedMain.getLicensesFrame().setVisible(true);
				deactivateLoadingPane();
			}
		}.start();
	}

	private void checkMenuItemsDisabling() {
		if (menuItemsHost == null) {
			Logging.info(this, "checkMenuItemsDisabling: menuItemsHost not yet enabled");
			return;
		}

		List<String> disabledClientMenuEntries = persistenceController.getConfigDataService()
				.getDisabledClientMenuEntries();

		if (disabledClientMenuEntries != null) {
			for (String menuActionType : disabledClientMenuEntries) {
				for (JMenuItem menuItem : menuItemsHost.get(menuActionType)) {
					Logging.debug(this, "disable " + menuActionType + ", " + menuItem);
					menuItem.setEnabled(false);
				}
			}

			iconButtonNewClient.setEnabled(!disabledClientMenuEntries.contains(ITEM_ADD_CLIENT));

			if (!persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD()) {
				jMenuAddClient.setEnabled(false);
				jMenuCopyClient.setEnabled(false);
				popupAddClient.setEnabled(false);
				popupCopyClient.setEnabled(false);
				iconButtonNewClient.setVisible(false);
			}
		}
	}

	private void enableMenuItemsForClients() {
		int countSelectedClients = configedMain.getSelectedClients().size();
		Logging.debug(this, " enableMenuItemsForClients, countSelectedClients " + countSelectedClients);

		for (JMenuItem jMenuItem : clientMenuItemsDependOnSelectionCount) {
			jMenuItem.setEnabled(countSelectedClients >= 1);
		}

		for (JMenuItem jMenuItem : clientPopupsDependOnSelectionCount) {
			jMenuItem.setEnabled(countSelectedClients >= 1);
		}

		jMenuChangeClientID.setEnabled(countSelectedClients == 1);
		jMenuCopyClient.setEnabled(countSelectedClients == 1);
		popupChangeClientID.setEnabled(countSelectedClients == 1);
		popupCopyClient.setEnabled(countSelectedClients == 1);

		checkMenuItemsDisabling();
	}

	private void resetProductOnClientAction(boolean withProductProperties, boolean resetLocalbootProducts,
			boolean resetNetbootProducts) {
		configedMain.resetProductsForSelectedClients(withProductProperties, resetLocalbootProducts,
				resetNetbootProducts);
	}

	private void addClientAction() {
		configedMain.callNewClientDialog();
	}

	private void changeClientIDAction() {
		configedMain.callChangeClientIDDialog();
	}

	private void changeDepotAction() {
		configedMain.callChangeDepotDialog();
	}

	private void showBackendConfigurationAction() {
		FEditorPane backendInfoDialog = new FEditorPane(this,
				Globals.APPNAME + ":  " + Configed.getResourceValue("MainFrame.InfoInternalConfiguration"), false,
				new String[] { Configed.getResourceValue("buttonClose") }, 800, 600);
		backendInfoDialog.insertHTMLTable(configedMain.getBackendInfos(), "");

		backendInfoDialog.setVisible(true);
	}

	private static void showLogfileLocationAction(JFrame centerFrame) {
		FTextArea info = new FTextArea(centerFrame, Configed.getResourceValue("MainFrame.showLogFileInfoTitle"), false,
				new String[] { Configed.getResourceValue("buttonClose"),
						Configed.getResourceValue("MainFrame.showLogFileCopyToClipboard"),
						Configed.getResourceValue("MainFrame.showLogFileOpen") },
				Globals.WIDTH_INFO_LOG_FILE, Globals.HEIGHT_INFO_LOG_FILE) {
			@Override
			public void doAction2() {
				getTextComponent().copy();
			}

			@Override
			public void doAction3() {
				try {
					Desktop.getDesktop().open(new File(Logging.getCurrentLogfilePath()));
				} catch (IOException e) {
					Logging.error("cannot open: " + Logging.getCurrentLogfilePath() + " :\n " + e);
				}
				super.doAction2();
			}
		};

		StringBuilder message = new StringBuilder();

		message.append(Configed.getResourceValue("MainFrame.showLogFileInfoText"));
		message.append("\n\n");
		message.append(Logging.getCurrentLogfilePath());

		info.setMessage(message.toString());

		info.getTextComponent().setSelectionEnd(message.toString().length());
		info.getTextComponent()
				.setSelectionStart(message.toString().length() - Logging.getCurrentLogfilePath().length());

		info.setVisible(true);
	}

	private void showHealthDataAction() {
		if (!persistenceController.getHealthDataService().isHealthDataAlreadyLoaded()) {
			activateLoadingPane(Configed.getResourceValue("HealthCheckDialog.loadData"));
		}

		HealthCheckDataLoader healthCheckDataLoader = new HealthCheckDataLoader();
		healthCheckDataLoader.execute();
	}

	private void showOpsiModules() {
		if (!persistenceController.getModuleDataService().isOpsiLicensingAvailablePD()
				|| !persistenceController.getModuleDataService().isOpsiUserAdminPD()) {
			StringBuilder message = new StringBuilder();
			Map<String, Object> modulesInfo = persistenceController.getModuleDataService().getOpsiModulesInfosPD();

			int count = 0;
			for (Entry<String, Object> modulesInfoEntry : modulesInfo.entrySet()) {
				count++;
				message.append("\n " + modulesInfoEntry.getKey() + ": " + modulesInfoEntry.getValue());
			}

			FTextArea f = new FTextArea(this, Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"),
					message.toString(), true, new String[] { Configed.getResourceValue("buttonClose") }, 300,
					50 + count * 25);

			f.setVisible(true);
		} else {
			callOpsiLicensingInfo();
		}
	}

	private void callOpsiLicensingInfo() {
		if (fDialogOpsiLicensingInfo == null) {
			fDialogOpsiLicensingInfo = new LicensingInfoDialog(this,
					Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), false,
					new String[] { Configed.getResourceValue("buttonClose") }, 1, 900, 700, true);
		} else {
			fDialogOpsiLicensingInfo.setLocationRelativeTo(this);
			fDialogOpsiLicensingInfo.setVisible(true);
		}
	}

	private void uefiBootAction() {
		Logging.info(this, "actionPerformed on cbUefiBoot");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_UEFI_BOOT_KEY, cbUefiBoot.isSelected().toString());
			configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}
	}

	private void wanConfigAction() {
		Logging.info(this, "actionPerformed on cbWANConfig");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_WAN_CONFIG_KEY, cbWANConfig.isSelected().toString());
			configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}
	}

	private void installByShutdownAction() {
		Logging.info(this, "actionPerformed on cbInstallByShutdown");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY, cbInstallByShutdown.isSelected().toString());
			configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}
	}

	// ----------------------------------------------------------------------------------------

	/* WindowListener implementation */
	@Override
	public void windowClosing(WindowEvent e) {
		configedMain.finishApp(true, 0);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowClosed(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowActivated(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowDeactivated(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowIconified(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowDeiconified(WindowEvent e) {
		/* Not needed */}

	private Map<String, String> getChangedClientInfoFor(String client) {
		if (changedClientInfos == null) {
			changedClientInfos = new HashMap<>();
		}

		return changedClientInfos.computeIfAbsent(client, arg -> new HashMap<>());
	}

	// ComponentListener implementation
	@Override
	public void componentHidden(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentMoved(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentShown(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentResized(ComponentEvent e) {
		Logging.debug(this, "componentResized");

		moveDivider1(panelClientSelection, clientPane, (int) (F_WIDTH_RIGHTHANDED * 0.2), 200,
				(int) (F_WIDTH_RIGHTHANDED * 1.5));
		Logging.debug(this, "componentResized ready");
	}

	private static void moveDivider1(JSplitPane splitpane, JComponent rightpane, int minRightWidth, int minLeftWidth,
			int maxRightWidth) {
		if (splitpane == null || rightpane == null) {
			return;
		}

		int dividerLocation = splitpane.getDividerLocation();

		int sizeOfRightPanel = (int) rightpane.getSize().getWidth();
		int missingSpace = minRightWidth - sizeOfRightPanel;
		if (missingSpace > 0 && dividerLocation > minLeftWidth) {
			splitpane.setDividerLocation(dividerLocation - missingSpace);
		}

		if (sizeOfRightPanel > maxRightWidth) {
			splitpane.setDividerLocation(dividerLocation + (sizeOfRightPanel - maxRightWidth));
		}
	}

	private void arrangeWs(Set<JDialog> frames) {
		// problem: https://bugs.openjdk.java.net/browse/JDK-7074504
		// Can iconify, but not deiconify a modal JDialog

		if (frames == null) {
			return;
		}

		int transpose = 20;

		for (Window f : frames) {
			transpose = transpose + Globals.LINE_HEIGHT;

			if (f != null) {
				f.setVisible(true);
				f.setLocation(getLocation().x + transpose, getLocation().y + transpose);
			}
		}
	}

	public void instancesChanged(Set<?> instances) {
		boolean existJDialogInstances = instances != null && !instances.isEmpty();

		if (jMenuShowScheduledWOL != null) {
			jMenuShowScheduledWOL.setEnabled(existJDialogInstances);
		}
		if (jMenuFrameShowDialogs != null) {
			jMenuFrameShowDialogs.setEnabled(existJDialogInstances);
		}
	}

	private void executeCommandOnInstances(String command, Set<JDialog> instances) {
		Logging.info(this, "executeCommandOnInstances " + command + " for count instances " + instances.size());
		if ("arrange".equals(command)) {
			arrangeWs(instances);
		}
	}

	private void reactToHostDataChange(InputEvent e) {
		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

			if (e.getSource() == jTextFieldDescription) {
				Logging.info(this,
						"key released on textfielddescription ischangedtext " + jTextFieldDescription.isChangedText());
				if (jTextFieldDescription.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_DESCRIPTION_KEY, jTextFieldDescription.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_DESCRIPTION_KEY);
				}
			} else if (e.getSource() == jTextFieldInventoryNumber) {
				if (jTextFieldInventoryNumber.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, jTextFieldInventoryNumber.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_INVENTORY_NUMBER_KEY);
				}
			} else if (e.getSource() == jTextFieldOneTimePassword) {
				if (jTextFieldOneTimePassword.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY, jTextFieldOneTimePassword.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY);
				}
			} else if (e.getSource() == jTextAreaNotes) {
				if (!jTextAreaNotes.getText().equals(oldNotes)) {
					changedClientInfo.put(HostInfo.CLIENT_NOTES_KEY, jTextAreaNotes.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_NOTES_KEY);
				}
			} else if (e.getSource() == systemUUIDField) {
				Logging.debug(this, " keyPressed on systemUUIDField, text " + systemUUIDField.getText());

				if (systemUUIDField.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, systemUUIDField.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_SYSTEM_UUID_KEY);
				}
			} else if (e.getSource() == macAddressField) {
				Logging.debug(this, " keyPressed on macAddressField, text " + macAddressField.getText());

				if (macAddressField.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macAddressField.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_MAC_ADRESS_KEY);
				}
			} else if (e.getSource() == ipAddressField) {
				Logging.debug(this, " keyPressed on ipAddressField, text " + ipAddressField.getText());

				if (ipAddressField.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipAddressField.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_IP_ADDRESS_KEY);
				}
			} else {
				Logging.warning(this, "unexpected source in reactToHostDataChange: " + e.getSource());
			}
		}
	}

	// MouseListener implementation
	@Override
	public void mouseClicked(MouseEvent e) {
		Logging.debug(this, "mouse clicked " + configedMain.getSelectedClients());

		reactToHostDataChange(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}

	// KeyListener implementation
	@Override
	public void keyPressed(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyReleased(KeyEvent e) {
		Logging.debug(this, "key released " + configedMain.getSelectedClients());

		reactToHostDataChange(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	public void startLicenseDisplayer() {
		if (licenseDisplayer == null) {
			try {
				licenseDisplayer = new LicenseDisplayer();
				licenseDisplayer.setConfigedMain(configedMain);
				licenseDisplayer.initAndShowGUI();
			} catch (IOException ioE) {
				Logging.warning(this, "Unable to open FXML file.", ioE);
			}
		} else {
			licenseDisplayer.display();
		}
	}

	public void enableAfterLoading() {
		jButtonLicenses.setEnabled(true);
		jMenuFrameLicenses.setEnabled(true);
	}

	public void visualizeLicensesFramesActive(boolean b) {
		jButtonLicenses.setSelected(b);
		iconButtonReloadLicenses.setVisible(true);
		iconButtonReloadLicenses.setEnabled(true);
	}

	public void visualizeEditingTarget(EditingTarget t) {
		switch (t) {
		case CLIENTS:
			jButtonClientsConfiguration.setSelected(true);
			jButtonDepotsConfiguration.setSelected(false);
			jButtonServerConfiguration.setSelected(false);

			break;

		case DEPOTS:
			jButtonDepotsConfiguration.setSelected(true);
			jButtonServerConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);

			break;

		case SERVER:
			jButtonServerConfiguration.setSelected(true);
			jButtonDepotsConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);

			break;

		default:
			break;
		}
	}

	private void initHardwareInfo() {
		if (panelHWInfo == null) {
			panelHWInfo = new PanelHWInfo(configedMain) {
				@Override
				protected void reload() {
					super.reload();
					// otherwise we get a wait cursor only in table component
					configedMain.resetView(ConfigedMain.VIEW_HARDWARE_INFO);
				}
			};
		}
	}

	private void showHardwareInfo() {
		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog")),
				showHardwareLog);

		showHardwareLog.repaint();
	}

	public void setHardwareInfoNotPossible(String label) {
		Logging.info(this, "setHardwareInfoNotPossible");

		if (showHardwareLogNotFound == null || showHardwareLogParentOfNotFoundPanel == null) {
			showHardwareLogNotFound = new JPanel();
			showHardwareLogNotFound.add(new JLabel(label));
			showHardwareLogParentOfNotFoundPanel = new JPanel();

			showHardwareLogParentOfNotFoundPanel.setLayout(new BorderLayout());
			showHardwareLogParentOfNotFoundPanel.add(showHardwareLogNotFound);
		}

		showHardwareLog = showHardwareLogParentOfNotFoundPanel;
		showHardwareInfo();
	}

	public void setHardwareInfoMultiClients() {
		if (showHardwareLogMultiClientReport == null || controllerHWinfoMultiClients == null) {
			controllerHWinfoMultiClients = new ControllerHWinfoMultiClients(configedMain);
			showHardwareLogMultiClientReport = controllerHWinfoMultiClients.getPanel();
		}

		Logging.info(this, "setHardwareInfoMultiClients ");

		controllerHWinfoMultiClients.setFilter();
		showHardwareLog = showHardwareLogMultiClientReport;

		showHardwareInfo();
	}

	public void setHardwareInfo(Map<String, List<Map<String, Object>>> hardwareInfo) {
		panelHWInfo.setHardwareInfo(hardwareInfo);

		showHardwareLog = panelHWInfo;
		showHardwareInfo();
	}

	private void showSoftwareInfo() {
		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog")),
				showSoftwareLog);

		SwingUtilities.invokeLater(() -> ConfigedMain.getMainFrame().repaint());
	}

	public void setSoftwareAudit() {
		if (configedMain.getSelectedClients() != null && configedMain.getSelectedClients().size() > 1) {
			Logging.info(this, "setSoftwareAudit for clients " + configedMain.getSelectedClients().size());

			showSoftwareLog = showSoftwareLogMultiClientReport;
			showSoftwareInfo();
		} else {
			// handled by the following methods
			labelNoSoftware.setText(Configed.getResourceValue("MainFrame.TabRequiresClientSelected"));
			showSoftwareLog = showSoftwareLogNotFound;
			showSoftwareInfo();
		}
	}

	private void initSoftWareInfo() {
		panelSWInfo = new PanelSWInfo(configedMain) {
			@Override
			protected void reload() {
				super.reload();
				persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
				configedMain.resetView(ConfigedMain.VIEW_SOFTWARE_INFO);
			}
		};
	}

	public void setSoftwareAudit(String hostId) {
		labelNoSoftware.setText(Configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));

		Logging.debug(this, "setSoftwareAudit for " + hostId);
		panelSWInfo.setAskForOverwrite(true);
		panelSWInfo.setHost(hostId);
		panelSWInfo.updateModel();

		showSoftwareLog = panelSWInfo;

		showSoftwareInfo();
	}

	public void setUpdatedLogfilePanel(String logtype) {
		Logging.info(this, "setUpdatedLogfilePanel " + logtype);
		setLogfilePanel(configedMain.getLogfilesUpdating(logtype));
	}

	public void setLogfilePanel(final Map<String, String> logs) {
		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(Configed.getResourceValue("MainFrame.jPanel_logfiles")),
				showLogfiles);

		showLogfiles.setDocuments(logs, statusPane.getSelectedClientNames());
	}

	public void setLogview(String logtype) {
		int i = Arrays.asList(Utils.getLogTypes()).indexOf(logtype);
		if (i < 0) {
			return;
		}
		showLogfiles.setSelectedIndex(i);
	}

	// client field editing
	public void setClientDescriptionText(String s) {
		jTextFieldDescription.setText(s);
		jTextFieldDescription.setCaretPosition(0);
	}

	public void setClientInventoryNumberText(String s) {
		jTextFieldInventoryNumber.setText(s);
		jTextFieldInventoryNumber.setCaretPosition(0);
	}

	public void setClientOneTimePasswordText(String s) {
		jTextFieldOneTimePassword.setText(s);
		jTextFieldOneTimePassword.setCaretPosition(0);
	}

	public void setClientNotesText(String s) {
		jTextAreaNotes.setText(s);
		jTextAreaNotes.setCaretPosition(0);
		oldNotes = s;
	}

	public void setClientMacAddress(String s) {
		macAddressField.setText(s);
	}

	public void setClientSystemUUID(String s) {
		systemUUIDField.setText(s);
	}

	public void setClientIpAddress(String s) {
		ipAddressField.setText(s);
	}

	public void setUefiBoot(Boolean b) {
		Logging.info(this, "setUefiBoot " + b);

		if (ServerFacade.isOpsi43()) {
			cbUefiBoot.setSelected(
					persistenceController.getConfigDataService().isUEFI43(configedMain.getSelectedClients()));
		} else {
			cbUefiBoot.setSelected(b);
		}
	}

	public void setWANConfig(Boolean b) {
		Logging.info(this, "setWANConfig " + b);
		cbWANConfig.setSelected(b);
	}

	public void setOpsiHostKey(String s) {
		Logging.info(this, "setOpsiHostKey " + s);
		jTextFieldHostKey.setText(s);
	}

	public void setShutdownInstall(Boolean b) {
		Logging.info(this, "setShutdownInstall " + b);
		cbInstallByShutdown.setSelected(b);
	}

	public void setClientID(String s) {
		labelHostID.setText(s);
	}

	public void setClientInfoediting(boolean singleClient) {
		// singleClient is primarily conceived as toggle: true for single host, false
		// for multi hosts editing

		// mix with global read only flag
		boolean gb = !PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly();

		// resulting toggle for multi hosts editing
		boolean b1 = false;
		if (singleClient && gb) {
			b1 = true;
		}

		jTextFieldDescription.setEnabled(singleClient);
		jTextFieldDescription.setEditable(b1);
		jTextFieldInventoryNumber.setEnabled(singleClient);
		jTextFieldInventoryNumber.setEditable(b1);
		jTextFieldOneTimePassword.setEnabled(singleClient);
		jTextFieldOneTimePassword.setEditable(b1);
		jTextAreaNotes.setEnabled(singleClient);
		jTextAreaNotes.setEditable(b1);
		systemUUIDField.setEnabled(singleClient);
		systemUUIDField.setEditable(b1);
		macAddressField.setEnabled(singleClient);
		macAddressField.setEditable(b1);
		ipAddressField.setEnabled(singleClient);
		ipAddressField.setEditable(b1);

		// multi host editing allowed
		cbUefiBoot.setEnabled(gb && persistenceController.getModuleDataService().isWithUEFIPD());
		if (ServerFacade.isOpsi43()) {
			cbUefiBoot.disableSelection();
		}

		cbWANConfig.setEnabled(gb && persistenceController.getModuleDataService().isWithWANPD());
		cbInstallByShutdown.setEnabled(gb);

		jTextFieldHostKey.setMultiValue(!singleClient);
		jTextFieldHostKey.setEnabled(singleClient);

		if (singleClient) {
			jTextFieldDescription.setToolTipText(null);
			jTextFieldInventoryNumber.setToolTipText(null);
			jTextFieldOneTimePassword.setToolTipText(null);
			jTextAreaNotes.setToolTipText(null);
		} else {
			jTextFieldDescription
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldInventoryNumber
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldOneTimePassword
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextAreaNotes.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
		}
	}

	public IconButton getIconButtonReachableInfo() {
		return iconButtonReachableInfo;
	}

	public IconButton getIconButtonSessionInfo() {
		return iconButtonSessionInfo;
	}

	public PanelProductSettings getPanelLocalbootProductSettings() {
		return panelLocalbootProductSettings;
	}

	public PanelProductSettings getPanelNetbootProductSettings() {
		return panelNetbootProductSettings;
	}

	public PanelHostConfig getPanelHostConfig() {
		return panelHostConfig;
	}

	public PanelHostProperties getPanelHostProperties() {
		return panelHostProperties;
	}

	public PanelProductProperties getPanelProductProperties() {
		return panelProductProperties;
	}

	public ControllerHWinfoMultiClients getControllerHWinfoMultiClients() {
		return controllerHWinfoMultiClients;
	}

	public LicensingInfoDialog getFDialogOpsiLicensingInfo() {
		return fDialogOpsiLicensingInfo;
	}
}
