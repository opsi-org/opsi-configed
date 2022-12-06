package de.uib.configed.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;

/**
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2022 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.CopyrightInfos;
import de.uib.configed.Globals;
import de.uib.configed.HostsStatusInfo;
import de.uib.configed.configed;
import de.uib.configed.dashboard.LicenseDisplayer;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.hwinfopage.ControllerHWinfoMultiClients;
import de.uib.configed.gui.productpage.PanelGroupedProductSettings;
import de.uib.configed.gui.productpage.PanelProductProperties;
import de.uib.configed.gui.swinfopage.PanelSWInfo;
import de.uib.configed.gui.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.HostInfo;
import de.uib.messages.Messages;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommand_Template;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserSshConfig;
import de.uib.utilities.logging.logging;
import de.uib.utilities.observer.RunningInstancesObserver;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;
import de.uib.utilities.swing.ActivityPanel;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.FEditList;
import de.uib.utilities.swing.FEditObject;
import de.uib.utilities.swing.FEditTextWithExtra;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.JTextEditorField;
import de.uib.utilities.swing.JTextHideField;
import de.uib.utilities.swing.SeparatedDocument;
import de.uib.utilities.swing.TitledPanel;
import de.uib.utilities.swing.VerticalPositioner;
import de.uib.utilities.table.ExportTable;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.thread.WaitCursor;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

//import de.uib.utilities.StringvaluedObject;

public class MainFrame extends JFrame implements WindowListener, KeyListener, MouseListener, ActionListener,
		RunningInstancesObserver<JDialog>, ComponentListener {

	protected int dividerLocationCentralPane = 300;
	protected int minHSizeTreePanel = 150;

	public static final int fwidth = 800;
	public static final int fheight = 600;

	private static final int fwidth_righthanded = 200;// was fwidth - fwidth_lefthanded;

	private static final int dividerLocationClientTreeMultidepot = 200;
	private static final int dividerLocationClientTreeSingledepot = 50;

	// final int widthColumnServer = 110; //130;

	// protected String oldDescription;
	// protected String oldInventoryNumber;
	// protected String oldOneTimePassword;
	protected String oldNotes;
	// protected String oldMacAddress;

	protected Map<String, Map<String, String>> changedClientInfos;

	protected ExportTable exportTable;

	ConfigedMain main;

	public SizeListeningPanel allPane;

	private boolean savingFramePosition = false;

	// menu system

	public static final String ITEM_ADD_CLIENT = "add client";
	public static final String ITEM_DELETE_CLIENT = "remove client";
	public static final String ITEM_FREE_LICENCES = "free licences for client";

	private Map<String, java.util.List<JMenuItem>> menuItemsHost;

	// Map<String, java.util.List<JMenuItem>> menuItemsOpsiclientdExtraEvent = new
	// HashMap<String, java.util.List<JMenuItem>>();

	JMenuBar jMenuBar1 = new JMenuBar();

	JMenu jMenuFile;
	JMenuItem jMenuFileExit;
	JMenuItem jMenuFileSaveConfigurations;
	JMenuItem jMenuFileReload;
	JMenuItem jMenuFileLanguage;

	JMenu jMenuClients = new JMenu();
	JMenuItem jMenuResetProductOnClientWithStates = new JMenuItem();
	JMenuItem jMenuResetProductOnClient = new JMenuItem();
	JMenuItem jMenuAddClient = new JMenuItem();
	JMenuItem jMenuDeleteClient = new JMenuItem();
	JMenuItem jMenuFreeLicences = new JMenuItem();
	JMenuItem jMenuDeletePackageCaches = new JMenuItem();
	JMenu jMenuWakeOnLan;
	// JMenu jMenuScheduledWOL;
	JMenuItem jMenuDirectWOL = new JMenuItem();
	JMenuItem jMenuNewScheduledWOL = new JMenuItem();
	JMenuItem jMenuShowScheduledWOL = new JMenuItem();
	JMenuItem jMenuOpsiClientdEvent;
	JMenuItem jMenuShowPopupMessage = new JMenuItem();
	JMenuItem jMenuRequestSessionInfo = new JMenuItem();
	JMenuItem jMenuShutdownClient = new JMenuItem();
	JMenuItem jMenuRebootClient = new JMenuItem();
	JMenuItem jMenuChangeDepot = new JMenuItem();
	JMenuItem jMenuChangeClientID = new JMenuItem();

	JMenu jMenuServer = new JMenu();
	// JMenuItem jMenuRemoteTerminal = new JMenuItem();
	JMenuItem jMenuSSHConfig = new JMenuItem();
	JMenuItem jMenuSSHConnection = new JMenuItem();
	JMenuItem jMenuSSHCommandControl = new JMenuItem();

	LinkedHashMap<String, Integer> labelledDelays;

	private Map<String, String> searchedTimeSpans;
	private Map<String, String> searchedTimeSpansText;

	// JCheckBoxMenuItem jCheckBoxMenuItem_displayClientList = new
	// JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItem_showCreatedColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showWANactiveColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showIPAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showInventoryNumberColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showHardwareAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showSessionInfoColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showUefiBoot = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showInstallByShutdown = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showDepotColumn = new JCheckBoxMenuItem();
	JMenuItem jMenuRemoteControl = new JMenuItem();

	JMenuItem[] clientMenuItemsDependOnSelectionCount = new JMenuItem[] { jMenuResetProductOnClientWithStates,
			jMenuResetProductOnClient, jMenuAddClient, jMenuDeleteClient, jMenuFreeLicences,
			// jMenuWakeOnLan,
			// jMenuShowPopupMessage,
			// jMenuRequestSessionInfo,
			// jMenuShutdownClient,
			// jMenuRebootClient,
			// jMenuOpsiClientdEvent,
			jMenuChangeDepot, jMenuChangeClientID,
			// jMenuRemoteControl
	};

	JMenu jMenuClientselection = new JMenu();
	JMenuItem jMenuClientselectionGetGroup = new JMenuItem();
	JMenuItem jMenuClientselectionGetSavedSearch = new JMenuItem();
	JMenuItem jMenuClientselectionProductNotUptodate = new JMenuItem();
	JMenuItem jMenuClientselectionProductNotUptodateOrBroken = new JMenuItem();
	JMenuItem jMenuClientselectionFailedProduct = new JMenuItem();
	JMenu jMenuClientselectionFailedInPeriod = new JMenu();
	// JMenuItem jMenuClientselectionSaveGroup = new JMenuItem();
	// JMenuItem jMenuClientselectionDeleteGroup = new JMenuItem();
	JMenuItem jMenuClientselectionDeselect = new JMenuItem();
	JCheckBoxMenuItem jMenuClientselectionToggleClientFilter = new JCheckBoxMenuItem();

	JMenu jMenuFrames = new JMenu();
	JMenuItem jMenuFrameLicences = new JMenuItem();
	JMenuItem jMenuFrameWorkOnProducts = new JMenuItem();
	JMenuItem jMenuFrameWorkOnGroups = new JMenuItem();
	JMenuItem jMenuFrameShowDialogs = new JMenuItem();

	JMenu jMenuHelp = new JMenu();
	JMenuItem jMenuHelpSupport = new JMenuItem();
	JMenuItem jMenuHelpDoc = new JMenuItem();
	JMenuItem jMenuHelpDocSpecial = new JMenuItem();
	JMenuItem jMenuHelpForum = new JMenuItem();
	JMenuItem jMenuHelpInternalConfiguration = new JMenuItem();
	JMenuItem jMenuHelpAbout = new JMenuItem();
	JMenuItem jMenuHelpOpsiVersion = new JMenuItem();
	JMenuItem jMenuHelpOpsiModuleInformation = new JMenuItem();
	JMenuItem jMenuHelpServerInfoPage = new JMenuItem();
	JMenu jMenuHelpLoglevel = new JMenu();
	JMenuItem jMenuHelpLogfileLocation = new JMenuItem();

	JRadioButtonMenuItem[] rbLoglevelItems = new JRadioButtonMenuItem[logging.LEVEL_SECRET + 1];

	JPopupMenu popupClients = new JPopupMenu();
	JMenuItemFormatted popupResetProductOnClientWithStates = new JMenuItemFormatted();
	JMenuItemFormatted popupResetProductOnClient = new JMenuItemFormatted();
	JMenuItemFormatted popupAddClient = new JMenuItemFormatted();
	JMenuItemFormatted popupDeleteClient = new JMenuItemFormatted();
	JMenuItemFormatted popupFreeLicences = new JMenuItemFormatted();
	JMenuItemFormatted popupDeletePackageCaches = new JMenuItemFormatted();
	JMenu popupWakeOnLan = new JMenu(configed.getResourceValue("MainFrame.jMenuWakeOnLan"));
	JMenuItemFormatted popupWakeOnLanDirect = new JMenuItemFormatted();
	JMenuItemFormatted popupWakeOnLanScheduler = new JMenuItemFormatted();

	// JMenu subOpsiClientdEvent = new JMenu();
	JMenu menuPopupOpsiClientdEvent = new JMenu(configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));
	JMenuItemFormatted popupShowPopupMessage = new JMenuItemFormatted();
	JMenuItemFormatted popupRequestSessionInfo = new JMenuItemFormatted();
	JMenuItemFormatted popupShutdownClient = new JMenuItemFormatted();
	JMenuItemFormatted popupRebootClient = new JMenuItemFormatted();
	JMenuItemFormatted popupChangeDepot = new JMenuItemFormatted();
	JMenuItemFormatted popupChangeClientID = new JMenuItemFormatted();
	JMenuItemFormatted popupRemoteControl = new JMenuItemFormatted();

	JMenuItemFormatted[] clientPopupsDependOnSelectionCount = new JMenuItemFormatted[] { popupResetProductOnClient,
			popupAddClient, popupDeleteClient, popupFreeLicences,
			// popupWakeOnLan,
			popupShowPopupMessage, popupRequestSessionInfo, popupDeletePackageCaches, popupRebootClient,
			popupShutdownClient,
			// menuPopupOpsiClientdEvent,
			popupChangeDepot, popupChangeClientID, popupRemoteControl };

	// JCheckBoxMenuItem popupDisplayClientList = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowCreatedColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowWANactiveColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowIPAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowHardwareAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowSessionInfoColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowInventoryNumberColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowUefiBoot = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowInstallByShutdownColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowDepotColumn = new JCheckBoxMenuItem();

	JMenuItemFormatted popupSelectionGetGroup = new JMenuItemFormatted();
	JMenuItemFormatted popupSelectionGetSavedSearch = new JMenuItemFormatted();
	// JMenuItemFormatted popupSelectionSaveGroup = new JMenuItemFormatted();
	// JMenuItemFormatted popupSelectionDeleteGroup = new JMenuItemFormatted();
	JMenuItemFormatted popupSelectionDeselect = new JMenuItemFormatted();
	JCheckBoxMenuItem popupSelectionToggleClientFilter = new JCheckBoxMenuItem();

	JMenuItemFormatted popupRebuildClientList = new JMenuItemFormatted(
			configed.getResourceValue("PopupMenuTrait.reload"),
			de.uib.configed.Globals.createImageIcon("images/reload16.png", ""));
	JMenuItemFormatted popupCreatePdf = new JMenuItemFormatted(configed.getResourceValue("FGeneralDialog.pdf"),
			de.uib.configed.Globals.createImageIcon("images/acrobat_reader16.png", ""));

	JPopupMenu popupLocalbootProducts = new JPopupMenu();
	JPopupMenu popupNetbootProducts = new JPopupMenu();
	JPopupMenu popupHardwareAudit = new JPopupMenu();
	JPopupMenu popupSoftwareAudit = new JPopupMenu();
	JPopupMenu popupNetworkConfig = new JPopupMenu();
	JPopupMenu popupLogfiles = new JPopupMenu();

	JPopupMenu popupDepotList = new JPopupMenu();
	JMenuItemFormatted popupCommitChangedDepotSelection = new JMenuItemFormatted();
	JMenuItemFormatted popupCancelChangedDepotSelection = new JMenuItemFormatted();

	JPanel iconBarPane;

	JPanel iconPane0;

	JPanel iconPaneTargets;
	JButton jButtonServerConfiguration;
	JButton jButtonDepotsConfiguration;
	JButton jButtonClientsConfiguration;
	JButton jButtonWorkOnGroups;
	JButton jButtonWorkOnProducts;

	JPanel iconPaneExtraFrames;

	JButton jButtonDash;
	JButton jButtonLicences;

	JPanel iconPane1;
	// JButton buttonWindowStack; may be it will get a revival at a different place
	IconButton iconButtonReload;
	IconButton iconButtonReloadLicenses;
	IconButton iconButtonNewClient;
	IconButton iconButtonSaveGroup;/* gibts nicht **/
	IconButton iconButtonSetGroup;
	IconButton iconButtonSaveConfiguration;
	IconButton iconButtonCancelChanges;
	IconButton iconButtonToggleClientFilter;
	public IconButton iconButtonReachableInfo;
	public IconButton iconButtonSessionInfo;

	public CombinedMenuItem combinedMenuItemCreatedColumn = new CombinedMenuItem(jCheckBoxMenuItem_showCreatedColumn,
			popupShowCreatedColumn);

	public CombinedMenuItem combinedMenuItemWANactiveColumn = new CombinedMenuItem(
			jCheckBoxMenuItem_showWANactiveColumn, popupShowWANactiveColumn);

	public CombinedMenuItem combinedMenuItemIPAddressColumn = new CombinedMenuItem(
			jCheckBoxMenuItem_showIPAddressColumn, popupShowIPAddressColumn);

	public CombinedMenuItem combinedMenuItemHardwareAddressColumn = new CombinedMenuItem(
			jCheckBoxMenuItem_showHardwareAddressColumn, popupShowHardwareAddressColumn);

	public CombinedMenuItem combinedMenuItemSessionInfoColumn = new CombinedMenuItem(
			jCheckBoxMenuItem_showSessionInfoColumn, popupShowSessionInfoColumn);

	public CombinedMenuItem combinedMenuItemInventoryNumberColumn = new CombinedMenuItem(
			jCheckBoxMenuItem_showInventoryNumberColumn, popupShowInventoryNumberColumn);

	public CombinedMenuItem combinedMenuItemUefiBootColumn = new CombinedMenuItem(jCheckBoxMenuItem_showUefiBoot,
			popupShowUefiBoot);

	public CombinedMenuItem combinedMenuItemInstallByShutdownColumn = new CombinedMenuItem(
			jCheckBoxMenuItem_showInstallByShutdown, popupShowInstallByShutdownColumn);

	public CombinedMenuItem combinedMenuItemDepotColumn = new CombinedMenuItem(jCheckBoxMenuItem_showDepotColumn,
			popupShowDepotColumn);

	JPanel proceeding;

	/*
	 * protected IconButton buttonCommitChangedDepotSelection;
	 * protected IconButton buttonCancelChangedDepotSelection;
	 */

	protected JButton buttonSelectDepotsWithEqualProperties;
	protected JButton buttonSelectDepotsAll;

	BorderLayout borderLayout1 = new BorderLayout();
	GroupLayout contentLayout;
	JTabbedPane jTabbedPaneConfigPanes = new JTabbedPane(); // new ClippedTitleTabbedPane();
	public JSplitPane panel_Clientselection; // = new JSplitPane();

	private HostsStatusPanel statusPane;

	public PanelGroupedProductSettings panel_LocalbootProductsettings;
	public PanelGroupedProductSettings panel_NetbootProductsettings;
	public PanelHostConfig panel_HostConfig;
	public PanelHostProperties panel_HostProperties;
	public PanelProductProperties panel_ProductProperties;

	// PanelJSONData showHardwareLog_version1 = new PanelJSONData();
	de.uib.configed.gui.hwinfopage.PanelHWInfo showHardwareLog_version2;
	// de.uib.configed.gui.hwinfopage.PanelHWInfo
	TitledPanel showHardwareLog_NotFound;
	public ControllerHWinfoMultiClients controllerHWinfoMultiClients;
	JPanel showHardwareLog_MultiClientReport;
	JPanel showHardwareLogParentOfNotFoundPanel;
	JPanel showHardwareLog;
	JLabel labelNoSoftware;
	Panelreinst panelReinstmgr = new Panelreinst();

	PanelSWInfo panelSWInfo;
	JPanel showSoftwareLog_NotFound;
	PanelSWMultiClientReport showSoftwareLog_MultiClientReport;
	JPanel showSoftwareLog;

	PanelTabbedDocuments showLogfiles;

	JPanel jPanel_Schalterstellung;

	public de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo fDialogOpsiLicensingInfo;

	// protected ProductInfoPane localbootProductInfo;
	// protected ProductInfoPane netbootProductInfo;

	// EditMapPanel localboot_productPropertiesPanel;
	// EditMapPanel netboot_productPropertiesPanel;

	JTextField jTextFieldConfigdir = new JTextField();
	JButton jButtonFileChooserConfigdir = new JButton();
	JPanel jPanel3 = new JPanel();
	// JLabel jLabel_Clientname = new JLabel();
	JCheckBox jCheckBoxSorted = new JCheckBox();
	JButton jButtonSaveList = new JButton();
	JPanel jPanel_ButtonSaveList = new JPanel();
	String[] options = new String[] { "off", "on", "setup" };
	JComboBox<String> jComboBoxProductValues = new JComboBox<>(options);

	JLabel jLabel_property = new JLabel();
	ButtonGroup buttonGroupRequired = new ButtonGroup();
	JRadioButton jRadioRequiredAll = new JRadioButton();
	JRadioButton jRadioRequiredOff = new JRadioButton();

	private static boolean settingSchalter = false;
	JButton jBtnAllOff = new JButton();
	// JButton jBtnCopyTemplate = new JButton();

	// JButton jBtnRefresh = new JButton();

	JTableSelectionPanel panelClientlist;
	boolean shiftpressed = false;
	// TableColumnModel clientlistColumnModel;
	JLabel jLabel_Hostinfos = new JLabel();
	// FlowLayout flowLayout1 = new FlowLayout();
	JLabel jLabelPath = new JLabel();
	// JLabel jLabelDepot = new
	// JLabel(configed.getResourceValue("MainFrame.jLabelDepot"));//"Depot(s): ");
	JTextArea jFieldInDepot;
	JLabel labelHost;
	JLabel labelHostID;
	CheckedLabel cbInstallByShutdown;
	CheckedLabel cbUefiBoot;
	CheckedLabel cbWANConfig;

	// JPanel paneldeprecatedInstallByShutdown;
	// JLabel jLabel_InstallByShutdown;
	// JButton btnAktivateInstallByShutdown;
	// JButton btnDeaktivateInstallByShutdown;

	JTextEditorField jTextFieldDescription;
	JTextEditorField jTextFieldInventoryNumber;
	JTextArea jTextAreaNotes;
	JTextEditorField macAddressField;
	JTextEditorField ipAddressField;
	JTextEditorField jTextFieldOneTimePassword;
	JTextHideField jTextFieldHostKey;
	JScrollPane scrollpaneNotes;

	JPopupMenu jPopupMenu = new JPopupMenu();

	protected FShowList fListSelectedClients;
	// protected FTextArea fAskSaveChangedText;

	JPanel jPanelChooseDomain;
	// JTabbedPane jTabbedPaneConfigPanes; // cf. above
	JPanel panelTreeClientSelection;
	JPanel jPanelProductsConfig;

	// DepotsList depotslist;
	boolean multidepot = false;
	// JScrollPane scrollpaneDepotslist;

	DepotListPresenter depotListPresenter;

	ClientTree treeClients;
	JScrollPane scrollpaneTreeClients;

	JPanel clientPane;
	Containership csClientPane;

	int splitterPanelClientSelection = 0;
	int prefClientPaneW = 100;
	int clientPaneW;

	// ComponentListener clientPaneComponentListener;

	public Container baseContainer;

	private LicenseDash licenseDash;

	class GlassPane extends JComponent {
		GlassPane() {
			super();
			logging.debug(this, "glass pane initialized");
			super.setVisible(true);
			setOpaque(true);
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					logging.debug(this, "key typed on glass pane");
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					logging.info(this, "mouse on glass pane");
				}
			});

		}

		@Override
		public void paintComponent(Graphics g) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5));

			g.setColor(new Color(230, 230, 250));
			g.fillRect(0, 0, getWidth(), getHeight());
		}

	}

	GlassPane glass;

	public MainFrame(ConfigedMain main, JTableSelectionPanel selectionPanel, DepotsList depotsList,
			ClientTree treeClients, boolean multidepot) {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // we handle it in the window listener method

		this.multidepot = multidepot;

		panelClientlist = selectionPanel;
		// selectionPanel.setPreferredSize(new Dimension(fwidth_lefthanded, fheight));
		// clientlistColumnModel = selectionPanel.getColumnModel();

		exportTable = new ExporterToCSV(selectionPanel.getTable());

		this.treeClients = treeClients;

		depotListPresenter = new DepotListPresenter(depotsList, multidepot, main.getPersistenceController());
		// this.depotslist = depotsList;

		this.main = main;
		addComponentListener(this);

		baseContainer = this.getContentPane();

		de.uib.utilities.Globals.masterFrame = baseContainer;

		glass = new GlassPane();

		guiInit();
		initData();

		UIManager.put("OptionPane.yesButtonText", configed.getResourceValue("UIManager.yesButtonText"));
		UIManager.put("OptionPane.noButtonText", configed.getResourceValue("UIManager.noButtonText"));
		UIManager.put("OptionPane.cancelButtonText", configed.getResourceValue("UIManager.cancelButtonText"));

		FEditObject.runningInstances.addObserver((RunningInstancesObserver<JDialog>) this);

	}

	@Override
	public void setVisible(boolean b) {
		logging.info(this, "setVisible from MainFrame " + b);
		super.setVisible(b);
	}

	private void initData() {
		statusPane.updateValues(0, null, null, null);
	}

	public HostsStatusInfo getHostsStatusInfo() {
		return statusPane;
	}

	public Container retrieveBasePane()
	// for setting cursor
	{
		return baseContainer;
	}

	public void initFirstSplitPane() {
		panel_Clientselection.setDividerLocation(0.8);
	}

	public void repairSizes() {
		// repair sizes when the frame is resized

		if (panel_Clientselection == null)
			return;

		splitterPanelClientSelection = panel_Clientselection.getSize().width - clientPaneW;

		// clientPane.removeComponentListener(clientPaneComponentListener);
		// panel_Clientselection.setDividerLocation(splitterPanelClientSelection);

		moveDivider1(panel_Clientselection, clientPane, (int) (fwidth_righthanded * 0.2), 200,
				(int) (fwidth_righthanded * 1.5));

		// clientPane.addComponentListener(clientPaneComponentListener);

		// moveDivider2(panel_LocalbootProductsettings, localbootProductInfo, 200);

		// moveDivider2(panel_NetbootProductsettings, netbootProductInfo, 200);

		// moveDivider(panel_LocalbootProductsettings, localbootProductInfo,
		// (int)fwidth_righthanded/2 + 40, 130, fwidth_righthanded_compi + 80);

		// moveDivider(panel_NetbootProductsettings, netbootProductInfo,
		// (int)fwidth_righthanded/2 + 40, 130, fwidth_righthanded_compi + 80);
	}

	private void moveDivider1(JSplitPane splitpane, JComponent rightpane, int min_right_width, int min_left_width,
			int max_right_width) {
		if (splitpane == null || rightpane == null)
			return;

		int dividerLocation = splitpane.getDividerLocation();
		// dividerLocation initially was (fwidth_lefthanded + splitterLeftRight);
		int sizeOfRightPanel = (int) rightpane.getSize().getWidth();
		int missingSpace = min_right_width - sizeOfRightPanel;
		if (missingSpace > 0 && dividerLocation > min_left_width) {
			splitpane.setDividerLocation(dividerLocation - missingSpace);
			// logging.debug (" reset divider location ");
		}

		// logging.info(this, "moveDivider1 ");

		if (sizeOfRightPanel > max_right_width) {
			splitpane.setDividerLocation(dividerLocation + (sizeOfRightPanel - max_right_width));
		}

	}

	public class SizeListeningPanel extends JPanel implements ComponentListener {
		SizeListeningPanel() {
			addComponentListener(this);
		}
		// ComponentListener implementation

		public void componentHidden(ComponentEvent e) {
		}

		public void componentMoved(ComponentEvent e) {
		}

		public void componentResized(ComponentEvent e) {
			logging.debug(this, "componentResized");

			try {
				repairSizes();
			} catch (Exception ex) {
				logging.info(this, "componentResized " + ex);
			}
			logging.debug(this, "componentResized ready");

		}

		public void componentShown(ComponentEvent e) {
		}

		private void moveDivider1(JSplitPane splitpane, JComponent rightpane, int min_right_width, int min_left_width,
				int max_right_width) {
			if (splitpane == null || rightpane == null)
				return;

			int dividerLocation = splitpane.getDividerLocation();
			// dividerLocation initially was (fwidth_lefthanded + splitterLeftRight);
			int sizeOfRightPanel = (int) rightpane.getSize().getWidth();
			int missingSpace = min_right_width - sizeOfRightPanel;
			if (missingSpace > 0 && dividerLocation > min_left_width) {
				splitpane.setDividerLocation(dividerLocation - missingSpace);
				// logging.debug (" reset divider location ");
			}

			// logging.info(this, "moveDivider1 ");

			if (sizeOfRightPanel > max_right_width) {
				splitpane.setDividerLocation(dividerLocation + (sizeOfRightPanel - max_right_width));
			}

		}

		public void repairSizes() {
			// repair sizes when the frame is resized

			if (panel_Clientselection == null)
				return;

			splitterPanelClientSelection = panel_Clientselection.getSize().width - clientPaneW;

			// clientPane.removeComponentListener(clientPaneComponentListener);
			// panel_Clientselection.setDividerLocation(splitterPanelClientSelection);

			moveDivider1(panel_Clientselection, clientPane, (int) (fwidth_righthanded * 0.2), 200,
					(int) (fwidth_righthanded * 1.5));

			// clientPane.addComponentListener(clientPaneComponentListener);

			// moveDivider2(panel_LocalbootProductsettings, localbootProductInfo, 200);

			// moveDivider2(panel_NetbootProductsettings, netbootProductInfo, 200);

			// moveDivider(panel_LocalbootProductsettings, localbootProductInfo,
			// (int)fwidth_righthanded/2 + 40, 130, fwidth_righthanded_compi + 80);

			// moveDivider(panel_NetbootProductsettings, netbootProductInfo,
			// (int)fwidth_righthanded/2 + 40, 130, fwidth_righthanded_compi + 80);
		}

	}

	// ------------------------------------------------------------------------------------------
	// configure interaction
	// ------------------------------------------------------------------------------------------
	// menus

	private void setupMenuLists() {

		menuItemsHost = new HashMap<String, java.util.List<JMenuItem>>();
		menuItemsHost.put(ITEM_ADD_CLIENT, new ArrayList<JMenuItem>());
		menuItemsHost.put(ITEM_DELETE_CLIENT, new ArrayList<JMenuItem>());
		menuItemsHost.put(ITEM_FREE_LICENCES, new ArrayList<JMenuItem>());

		/*
		 * menuItemsOpsiclientdExtraEvent = new HashMap<String,
		 * java.util.List<JMenuItem>>();
		 * if (main.getPersistenceController().getOpsiclientdExtraEvents() != null)
		 * {
		 * for (String event :
		 * main.getPersistenceController().getOpsiclientdExtraEvents())
		 * {
		 * menuItemsOpsiclientdExtraEvent.put(event, new ArrayList<JMenuItem>());
		 * }
		 * }
		 */
	}

	private void setupMenuFile() {
		jMenuFile = new JMenu();
		jMenuFileExit = new JMenuItem();
		jMenuFileSaveConfigurations = new JMenuItem();
		jMenuFileReload = new JMenuItem();
		jMenuFileLanguage = new JMenu(); // submenu

		jMenuFile.setText(configed.getResourceValue("MainFrame.jMenuFile"));

		jMenuFileExit.setText(configed.getResourceValue("MainFrame.jMenuFileExit"));
		jMenuFileExit.addActionListener((ActionEvent e) -> exitAction());

		jMenuFileSaveConfigurations.setText(configed.getResourceValue("MainFrame.jMenuFileSaveConfigurations"));
		jMenuFileSaveConfigurations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSaveConfigurations.addActionListener((ActionEvent e) -> saveAction());

		jMenuFileReload.setText(configed.getResourceValue("MainFrame.jMenuFileReload"));
		// jMenuFileReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

		jMenuFileReload.addActionListener((ActionEvent e) -> {
			reloadAction();
			if (iconButtonReloadLicenses.isEnabled())
				reloadLicensesAction();
		});

		jMenuFileLanguage.setText(configed.getResourceValue("MainFrame.jMenuFileChooseLanguage"));
		ButtonGroup groupLanguages = new ButtonGroup();

		String selectedLocale = Messages.getSelectedLocale();
		logging.debug(this, "selectedLocale " + selectedLocale);

		for (final String localeName : Messages.getLocaleInfo().keySet()) {
			ImageIcon localeIcon = null;
			String imageIconName = Messages.getLocaleInfo().get(localeName);
			if (imageIconName != null && imageIconName.length() > 0) {
				try {
					localeIcon = new ImageIcon(Messages.class.getResource(imageIconName));
				} catch (Exception ex) {
					logging.info(this, "icon not found: " + imageIconName + ", " + ex);
				}
			}

			JMenuItem menuItem = new JRadioButtonMenuItem(localeName, localeIcon);
			logging.debug(this, "selectedLocale " + selectedLocale);
			menuItem.setSelected(selectedLocale.equals(localeName));
			jMenuFileLanguage.add(menuItem);
			groupLanguages.add(menuItem);

			menuItem.addActionListener((ActionEvent e) -> {
				main.closeInstance(true);
				de.uib.messages.Messages.setLocale(localeName);
				new Thread() {
					@Override
					public void run() {
						configed.startWithLocale();
					}
				}.start();

				// we put it into to special thread to avoid invokeAndWait runtime error
			});
		}

		/*
		 * jMenuFileLanguage.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * //logging.debug( " action event on jMenuFileReload ");
		 * main.closeInstance(true);
		 * de.uib.messages.Messages.setLocale("tr");
		 * configed.startWithLocale();
		 * }
		 * });
		 */

		jMenuFile.add(jMenuFileSaveConfigurations);
		jMenuFile.add(jMenuFileReload);
		jMenuFile.add(jMenuFileLanguage);

		jMenuFile.add(jMenuFileExit);
	}

	private void initMenuData() {
		labelledDelays = new LinkedHashMap<String, Integer>();
		labelledDelays.put("0 sec", 0);
		labelledDelays.put("5 sec", 5);
		labelledDelays.put("20 sec", 20);
		labelledDelays.put("1 min", 60);
		labelledDelays.put("2 min", 120);
		labelledDelays.put("10 min", 600);
		labelledDelays.put("20 min", 1200);
		labelledDelays.put("1 h", 3600);

		searchedTimeSpans = new LinkedHashMap<String, String>();

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

		searchedTimeSpansText = new LinkedHashMap<String, String>();

		searchedTimeSpansText.put(TODAY, configed.getResourceValue("MainFrame.TODAY"));
		searchedTimeSpansText.put(SINCE_YESTERDAY, configed.getResourceValue("MainFrame.SINCE_YESTERDAY"));
		searchedTimeSpansText.put(LAST_3_DAYS, configed.getResourceValue("MainFrame.LAST_3_DAYS"));
		searchedTimeSpansText.put(LAST_7_DAYS, configed.getResourceValue("MainFrame.LAST_7_DAYS"));
		searchedTimeSpansText.put(LAST_MONTH, configed.getResourceValue("MainFrame.LAST_MONTH"));
		searchedTimeSpansText.put(ANY_TIME, configed.getResourceValue("MainFrame.ANY_TIME"));

	}

	private void setupMenuClients() {
		jMenuClients.setText(configed.getResourceValue("MainFrame.jMenuClients"));

		jCheckBoxMenuItem_showCreatedColumn.setText(configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		combinedMenuItemCreatedColumn.show(main.host_displayFields.get(HostInfo.created_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showCreatedColumn.addItemListener((ItemEvent e) -> main.toggleColumnCreated());

		jCheckBoxMenuItem_showWANactiveColumn.setText(configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		combinedMenuItemWANactiveColumn.show(main.host_displayFields.get(HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showWANactiveColumn.addItemListener((ItemEvent e) -> main.toggleColumnWANactive());

		jCheckBoxMenuItem_showIPAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		combinedMenuItemIPAddressColumn.show(main.host_displayFields.get(HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showIPAddressColumn.addItemListener((ItemEvent e) -> main.toggleColumnIPAddress());

		jCheckBoxMenuItem_showHardwareAddressColumn
				.setText(configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		combinedMenuItemHardwareAddressColumn
				.show(main.host_displayFields.get(HostInfo.clientMacAddress_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showHardwareAddressColumn
				.addItemListener((ItemEvent e) -> main.toggleColumnHardwareAddress());

		jCheckBoxMenuItem_showSessionInfoColumn
				.setText(configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		combinedMenuItemSessionInfoColumn
				.show(main.host_displayFields.get(HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showSessionInfoColumn.addItemListener((ItemEvent e) -> {
			logging.info(this, "toggleColumnSessionInfo by CheckBoxMenuItem");
			main.toggleColumnSessionInfo();

		});

		jCheckBoxMenuItem_showInventoryNumberColumn
				.setText(configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		combinedMenuItemInventoryNumberColumn
				.show(main.host_displayFields.get(HostInfo.clientInventoryNumber_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showInventoryNumberColumn
				.addItemListener((ItemEvent e) -> main.toggleColumnInventoryNumber());

		jCheckBoxMenuItem_showUefiBoot.setText(configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		combinedMenuItemUefiBootColumn.show(main.host_displayFields.get(HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showUefiBoot.addItemListener((ItemEvent e) -> main.toggleColumnUEFIactive());

		jCheckBoxMenuItem_showInstallByShutdown
				.setText(configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		combinedMenuItemUefiBootColumn
				.show(main.host_displayFields.get(HostInfo.clientInstallByShutdown_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showInstallByShutdown
				.addItemListener((ItemEvent e) -> main.toggleColumnInstallByShutdownActive());

		jCheckBoxMenuItem_showDepotColumn.setText(configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		combinedMenuItemDepotColumn.show(main.host_displayFields.get(HostInfo.depotOfClient_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItem_showDepotColumn.addItemListener((ItemEvent e) -> main.toggleColumnDepot());

		/*
		 * jCheckBoxMenuItem_displayClientList.setText(configed.getResourceValue(
		 * "MainFrame.jMenuShowSelectedClients"));
		 * 
		 * setClientSelectionText(""); // creates fListSelectedClients with empty
		 * content
		 * jCheckBoxMenuItem_displayClientList.addItemListener(new ItemListener()
		 * {
		 * public void itemStateChanged (ItemEvent e)
		 * {
		 * if (fListSelectedClients != null)
		 * fListSelectedClients.setVisible(jCheckBoxMenuItem_displayClientList.
		 * isSelected());
		 * }
		 * });
		 */

		jMenuChangeDepot.setText(configed.getResourceValue("MainFrame.jMenuChangeDepot"));

		jMenuChangeDepot.addActionListener((ActionEvent e) -> changeDepotAction());

		jMenuChangeClientID.setText(configed.getResourceValue("MainFrame.jMenuChangeClientID"));

		jMenuChangeClientID.addActionListener((ActionEvent e) -> changeClientIDAction());

		jMenuResetProductOnClientWithStates
				.setText(configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithStates"));

		jMenuResetProductOnClientWithStates.addActionListener((ActionEvent e) -> resetProductOnClientAction(true));

		jMenuResetProductOnClient
				.setText(configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithoutStates"));

		jMenuResetProductOnClient.addActionListener((ActionEvent e) -> resetProductOnClientAction(false));

		jMenuAddClient.setText(configed.getResourceValue("MainFrame.jMenuAddClient"));
		jMenuAddClient.addActionListener((ActionEvent e) -> addClientAction());

		menuItemsHost.get(ITEM_ADD_CLIENT).add(jMenuAddClient);

		jMenuWakeOnLan = new JMenu(configed.getResourceValue("MainFrame.jMenuWakeOnLan"));

		jMenuDirectWOL.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan.direct"));
		jMenuDirectWOL.addActionListener((ActionEvent e) -> wakeOnLanAction());

		jMenuWakeOnLan.add(jMenuDirectWOL);

		/*
		 * jMenuScheduledWOL = new JMenu(
		 * configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler")
		 * );
		 * 
		 * jMenuWakeOnLan.add(jMenuScheduledWOL);
		 */

		jMenuNewScheduledWOL.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler"));
		final MainFrame f = this;
		jMenuNewScheduledWOL.addActionListener((ActionEvent e) -> {
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(f,
					Globals.APPNAME + ": " + configed.getResourceValue("FStartWakeOnLan.title"), main);
			fStartWakeOnLan.centerOn(f);
			// fStartWakeOnLan.setup();
			fStartWakeOnLan.setVisible(true);
			fStartWakeOnLan.setPredefinedDelays(labelledDelays);
			// logging.info(this, "hostSeparationByDepots "
			// main.getPersistenceController().getHostSeparationByDepots(
			// main.getSelectedClients() ) );

			fStartWakeOnLan.setClients();

		});

		jMenuWakeOnLan.add(jMenuNewScheduledWOL);

		jMenuWakeOnLan.addSeparator();

		jMenuShowScheduledWOL.setEnabled(false);
		jMenuShowScheduledWOL.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan.showRunning"));
		jMenuShowScheduledWOL.addActionListener((ActionEvent e) -> {
			logging.info(this, "actionPerformed");
			executeCommandOnInstances("arrange", FEditObject.runningInstances.getAll());
		});

		jMenuWakeOnLan.add(jMenuShowScheduledWOL);
		// jMenuScheduledWOL.addSeparator();

		/*
		 * jMenuWakeOnLan.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * wakeOnLanAction();
		 * }
		 * });
		 */

		jMenuDeletePackageCaches.setText(configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
		jMenuDeletePackageCaches.addActionListener((ActionEvent e) -> deletePackageCachesAction());

		jMenuOpsiClientdEvent = new JMenu(configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : main.getPersistenceController().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItem(event);
			item.setFont(Globals.defaultFont);

			item.addActionListener((ActionEvent e) -> fireOpsiclientdEventAction(event));

			jMenuOpsiClientdEvent.add(item);
		}

		jMenuShowPopupMessage.setText(configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
		jMenuShowPopupMessage.addActionListener((ActionEvent e) -> showPopupOnClientsAction());

		jMenuShutdownClient.setText(configed.getResourceValue("MainFrame.jMenuShutdownClient"));
		jMenuShutdownClient.addActionListener((ActionEvent e) -> shutdownClientsAction());

		jMenuRequestSessionInfo.setText(configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
		jMenuRequestSessionInfo.addActionListener((ActionEvent e) -> {
			main.setColumnSessionInfo(true);
			getSessionInfo();
		});

		jMenuRebootClient.setText(configed.getResourceValue("MainFrame.jMenuRebootClient"));
		jMenuRebootClient.addActionListener((ActionEvent e) -> rebootClientsAction());

		jMenuDeleteClient.setText(configed.getResourceValue("MainFrame.jMenuDeleteClient"));
		jMenuDeleteClient.addActionListener((ActionEvent e) -> deleteClientAction());

		menuItemsHost.get(ITEM_DELETE_CLIENT).add(jMenuDeleteClient);

		jMenuFreeLicences.setText(configed.getResourceValue("MainFrame.jMenuFreeLicences"));
		jMenuFreeLicences.addActionListener((ActionEvent e) -> freeLicencesAction());

		menuItemsHost.get(ITEM_FREE_LICENCES).add(jMenuFreeLicences);

		jMenuRemoteControl.setText(configed.getResourceValue("MainFrame.jMenuRemoteControl"));
		jMenuRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		// produces a global reaction when pressing space
		jMenuRemoteControl.addActionListener((ActionEvent e) -> remoteControlAction());

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

		// --
		jMenuClients.add(jMenuDeleteClient);
		jMenuClients.add(jMenuAddClient);
		jMenuClients.add(jMenuResetProductOnClientWithStates);
		jMenuClients.add(jMenuResetProductOnClient);
		jMenuClients.add(jMenuChangeClientID);
		if (multidepot)
			jMenuClients.add(jMenuChangeDepot);

		jMenuClients.addSeparator();

		// --
		// jMenuClients.add (jCheckBoxMenuItem_displayClientList);

		jMenuClients.add(jCheckBoxMenuItem_showWANactiveColumn);
		jMenuClients.add(jCheckBoxMenuItem_showIPAddressColumn);
		jMenuClients.add(jCheckBoxMenuItem_showHardwareAddressColumn);
		jMenuClients.add(jCheckBoxMenuItem_showSessionInfoColumn);
		jMenuClients.add(jCheckBoxMenuItem_showInventoryNumberColumn);
		jMenuClients.add(jCheckBoxMenuItem_showCreatedColumn);
		jMenuClients.add(jCheckBoxMenuItem_showUefiBoot);
		jMenuClients.add(jCheckBoxMenuItem_showInstallByShutdown);
		jMenuClients.add(jCheckBoxMenuItem_showDepotColumn);
	}

	public void updateSSHConnectedInfoMenu(String status) {
		SSHCommandFactory factory = SSHCommandFactory.getInstance(main);
		// String connectiondata = factory.getConnection().getConnectedUser() + "@" +
		// factory.getConnection().getConnectedHost();
		String connectiondata = SSHConnectionInfo.getInstance().getUser() + "@"
				+ SSHConnectionInfo.getInstance().getHost();

		jMenuSSHConnection.setText(connectiondata.trim() + " " + SSHCommandFactory.UNKNOWN);
		jMenuSSHConnection.setForeground(Globals.unknownBlue);
		if (status.equals(SSHCommandFactory.NOT_CONNECTED)) {
			// jMenuSSHConnection.setForeground(Globals.actionRed);
			jMenuSSHConnection.setForeground(Globals.lightBlack);
			jMenuSSHConnection.setText(connectiondata.trim() + " " + SSHCommandFactory.NOT_CONNECTED);
		} else if (status.equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED)) {
			jMenuSSHConnection.setForeground(Globals.actionRed);
			jMenuSSHConnection.setText(connectiondata.trim() + " " + SSHCommandFactory.CONNECTION_NOT_ALLOWED);

		} else if (status.equals(SSHCommandFactory.CONNECTED)) {
			jMenuSSHConnection.setForeground(Globals.okGreen);
			jMenuSSHConnection.setText(connectiondata.trim() + " " + SSHCommandFactory.CONNECTED);
		}
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
		logging.info(this, "setupMenuServer ");
		final SSHCommandFactory factory = SSHCommandFactory.getInstance(main);
		SSHConnectionInfo connectionInfo = SSHConnectionInfo.getInstance();

		factory.setMainFrame(this);
		JMenu menuOpsi = new JMenu();
		menuOpsi.setText(SSHCommandFactory.parentOpsi);

		jMenuServer.removeAll();
		jMenuServer.setText(SSHCommandFactory.parentNull);
		boolean isReadOnly = de.uib.configed.Globals.isGlobalReadOnly();
		boolean methodsExists = factory.checkSSHCommandMethod();

		logging.info(this, "setupMenuServer add configpage");
		jMenuSSHConfig = new JMenuItem();
		jMenuSSHConfig.setText(configed.getResourceValue("MainFrame.jMenuSSHConfig"));
		jMenuSSHConfig.addActionListener((ActionEvent e) -> startSSHConfigAction());

		jMenuSSHConnection.setEnabled(false);
		if (configed.sshconnect_onstart)
			factory.testConnection(connectionInfo.getUser(), connectionInfo.getHost());

		/*
		 * SSH TERMINAL / SSHTERMINAL
		 * logging. info(this, "setupMenuServer add terminal");
		 * jMenuRemoteTerminal = new JMenuItem();
		 * jMenuRemoteTerminal.setText(configed.getResourceValue(
		 * "MainFrame.jMenuSSHTerminal") );
		 * jMenuRemoteTerminal.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * if (factory.getConnectionState().equals(SSHCommandFactory.NOT_CONNECTED))
		 * logging.error(this,
		 * configed.getResourceValue("SSHConnection.not_connected.message") + " " +
		 * factory.getConnectionState());
		 * else if
		 * (factory.getConnectionState().equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED
		 * ))
		 * logging.error(this,
		 * configed.getResourceValue("SSHConnection.CONNECTION_NOT_ALLOWED.message"));
		 * else if (factory.getConnectionState().equals(SSHCommandFactory.UNKNOWN))
		 * logging.error(this,
		 * configed.getResourceValue("SSHConnection.not_connected.message") + " " +
		 * factory.getConnectionState());
		 * else
		 * remoteSSHTerminalAction();
		 * }
		 * });
		 */

		if (factory.checkSSHCommandMethod()) {
			logging.info(this, "setupMenuServer add commandcontrol");
			jMenuSSHCommandControl = new JMenuItem();
			jMenuSSHCommandControl.setText(configed.getResourceValue("MainFrame.jMenuSSHCommandControl"));
			jMenuSSHCommandControl.addActionListener((ActionEvent e) -> startSSHControlAction());
		}
		// SSHCommandControlDialog
		// jMenuServer.add(jMenuRemoteExec);
		jMenuServer.add(jMenuSSHConnection);
		jMenuServer.add(jMenuSSHConfig);
		if (factory.checkSSHCommandMethod())
			jMenuServer.add(jMenuSSHCommandControl);
		// jMenuServer.addSeparator();
		jMenuServer.addSeparator();
		/*
		 * SSH TERMINAL / SSHTERMINAL
		 * jMenuServer.add(jMenuRemoteTerminal);
		 * jMenuServer.addSeparator();
		 */
		// jMenuServer.add(jMenuSSHConfig);

		// Map<String, Object> serverConfigs =
		// main.getPersistenceController().getConfig(main.getPersistenceController().getHostInfoCollections().getConfigServer());
		// the same as main.getPersistenceController().getConfigDefaultValues()

		logging.info(this, "setupMenuServer getCurrentUserConfig " + UserConfig.getCurrentUserConfig());

		boolean commandsAreDeactivated = (UserConfig.getCurrentUserConfig() == null
				|| UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDS_ACTIVE) == null
				|| !UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDS_ACTIVE));
		logging.info(this, "setupMenuServer commandsAreDeactivated " + commandsAreDeactivated);

		if (methodsExists) {
			factory.retrieveSSHCommandListRequestRefresh();
			factory.retrieveSSHCommandList();
			java.util.LinkedHashMap<String, java.util.List<SSHCommand_Template>> sortedComs = factory
					.getSSHCommandMapSortedByParent();

			logging.debug(this, "setupMenuServer add commands to menu commands sortedComs " + sortedComs);
			boolean firstParentGroup = true;
			boolean commands_exists = false;
			for (Map.Entry<String, java.util.List<SSHCommand_Template>> entry : sortedComs.entrySet()) {
				String parentMenuName = entry.getKey();
				LinkedList<SSHCommand_Template> list_com = new LinkedList<SSHCommand_Template>(entry.getValue());
				Collections.sort(list_com);
				JMenu parentMenu = new JMenu();
				parentMenu.setText(parentMenuName);
				logging.info(this, "ssh parent menu text " + parentMenuName);
				if (parentMenuName.equals(SSHCommandFactory.parentdefaultForOwnCommands)) {
					parentMenu.setText("");
					parentMenu.setIcon(Globals.createImageIcon("images/burger_menu_09.png", "..."));
				}

				if (!(parentMenuName.equals(SSHCommandFactory.parentNull)))
					firstParentGroup = false;

				for (final SSHCommand_Template com : list_com) {
					commands_exists = true;
					JMenuItem jMenuItem = new JMenuItem();
					jMenuItem.setText(com.getMenuText());
					logging.info(this, "ssh command menuitem text " + com.getMenuText());
					jMenuItem.setToolTipText(com.getToolTipText());
					jMenuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (factory.getConnectionState().equals(SSHCommandFactory.NOT_CONNECTED))
								logging.error(this, configed.getResourceValue("SSHConnection.not_connected.message")
										+ " " + factory.getConnectionState());
							else if (factory.getConnectionState().equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED))
								logging.error(this,
										configed.getResourceValue("SSHConnection.CONNECTION_NOT_ALLOWED.message"));
							else if (factory.getConnectionState().equals(SSHCommandFactory.UNKNOWN))
								logging.error(this, configed.getResourceValue("SSHConnection.not_connected.message")
										+ " " + factory.getConnectionState());
							else {
								// Create new instance of the same command, so that further
								// modifications would not affect the original command.
								final SSHCommand_Template c = new SSHCommand_Template(com);
								remoteSSHExecAction(c);
							}
						}
					});

					if (parentMenuName.equals(SSHCommandFactory.parentNull)) {
						jMenuServer.add(jMenuItem);
					} else {
						parentMenu.add(jMenuItem);
						if (parentMenuName.equals(SSHCommandFactory.parentOpsi)) {
							menuOpsi = parentMenu;
							jMenuServer.add(menuOpsi);
						} else
							jMenuServer.add(parentMenu);
					}
					if (isReadOnly)
						jMenuItem.setEnabled(false);
					if (commandsAreDeactivated)
						jMenuItem.setEnabled(false);
				}
				if (firstParentGroup && commands_exists)
					jMenuServer.addSeparator();

				firstParentGroup = false;

			}
			menuOpsi.addSeparator();
		} else
			jMenuServer.add(menuOpsi);
		java.util.List<SSHCommand> commands = factory.getSSHCommandParameterList();
		logging.info(this, "setupMenuServer add parameterDialogs to opsi commands" + commands);
		for (final SSHCommand command : commands) {
			JMenuItem jMenuOpsiCommand = new JMenuItem();
			jMenuOpsiCommand.setText(command.getMenuText());
			jMenuOpsiCommand.setToolTipText(command.getToolTipText());
			jMenuOpsiCommand.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (factory.getConnectionState().equals(SSHCommandFactory.NOT_CONNECTED))
						logging.error(this, configed.getResourceValue("SSHConnection.not_connected.message") + " "
								+ factory.getConnectionState());
					else if (factory.getConnectionState().equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED))
						logging.error(this, configed.getResourceValue("SSHConnection.CONNECTION_NOT_ALLOWED.message"));
					else if (factory.getConnectionState().equals(SSHCommandFactory.UNKNOWN))
						logging.error(this, configed.getResourceValue("SSHConnection.not_connected.message") + " "
								+ factory.getConnectionState());
					else
						remoteSSHExecAction(command);
				}
			});
			if (!jMenuServer.isMenuComponent(menuOpsi))
				jMenuServer.add(menuOpsi);
			menuOpsi.add(jMenuOpsiCommand);
			if (isReadOnly)
				jMenuOpsiCommand.setEnabled(false);
			if (commandsAreDeactivated)
				jMenuOpsiCommand.setEnabled(false);
		}

		logging.info(this, "setupMenuServer create/read command menu configs");
		// jMenuRemoteTerminal.setEnabled(getBoolConfigValueForUser(UserSshConfig.KEY_SSH_SHELL_ACTIVE,
		// UserSshConfig.KEY_SSH_SHELL_ACTIVE_defaultvalue, serverConfigs));

		boolean userConfigExists = UserConfig.getCurrentUserConfig() != null;

		jMenuSSHConfig.setEnabled(userConfigExists
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_CONFIG_ACTIVE));

		logging.info(this, "setupMenuServer create/read command menu configs current user config "
				+ UserConfig.getCurrentUserConfig());
		jMenuSSHCommandControl.setEnabled(userConfigExists
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDCONTROL_ACTIVE));

		jMenuServer.setEnabled(userConfigExists && !isReadOnly
		// &&
		// getBoolConfigValueForUser(PersistenceController.PARTKEY_USER_PRIVILEGE_SERVER_READWRITE,
		// true, serverConfigs)
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_MENU_ACTIVE));
	}

	private void setupMenuGrouping() {
		jMenuClientselection.setText(configed.getResourceValue("MainFrame.jMenuClientselection"));

		/*
		 * jMenuClientselectionSaveGroup.setText(
		 * configed.getResourceValue("MainFrame.jMenuClientselectionSaveGroup") );
		 * jMenuClientselectionSaveGroup.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * saveGroupAction();
		 * }
		 * });
		 * 
		 */

		jMenuClientselectionGetGroup.setText(configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jMenuClientselectionGetGroup.addActionListener((ActionEvent e) -> callSelectionDialog());

		jMenuClientselectionGetSavedSearch
				.setText(configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		jMenuClientselectionGetSavedSearch.addActionListener((ActionEvent e) -> main.clientSelectionGetSavedSearch());

		jMenuClientselectionProductNotUptodate
				.setText(configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersion"));
		jMenuClientselectionProductNotUptodate.addActionListener((ActionEvent e) -> groupByNotCurrentProductVersion());

		jMenuClientselectionProductNotUptodateOrBroken.setText(configed
				.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersionOrUnknownState"));
		jMenuClientselectionProductNotUptodateOrBroken
				.addActionListener((ActionEvent e) -> groupByNotCurrentProductVersionOrBrokenInstallation());

		jMenuClientselectionFailedProduct
				.setText(configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedForProduct"));
		jMenuClientselectionFailedProduct.addActionListener((ActionEvent e) -> groupByFailedProduct());

		jMenuClientselectionFailedInPeriod
				.setText(configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedInTimespan"));

		for (final String value : searchedTimeSpans.values()) {
			JMenuItem item = new JMenuItemFormatted(value);
			item.setFont(Globals.defaultFont);

			item.addActionListener((ActionEvent e) -> main.selectClientsByFailedAtSomeTimeAgo(value));

			jMenuClientselectionFailedInPeriod.add(item);
		}

		/*
		 * jMenuClientselectionDeselect.setText(
		 * configed.getResourceValue("MainFrame.jMenuClientselectionDeselect") );
		 * jMenuClientselectionDeselect.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * noGroupingAction();
		 * }
		 * });
		 * 
		 */

		jMenuClientselectionToggleClientFilter
				.setText(configed.getResourceValue("MainFrame.jMenuClientselectionToggleClientFilter"));
		jMenuClientselectionToggleClientFilter.setState(false);
		jMenuClientselectionToggleClientFilter.addActionListener((ActionEvent e) -> toggleClientFilterAction());

		jMenuClientselection.add(jMenuClientselectionGetGroup);
		jMenuClientselection.add(jMenuClientselectionGetSavedSearch);

		jMenuClientselection.addSeparator();

		jMenuClientselection.add(jMenuClientselectionProductNotUptodate);
		jMenuClientselection.add(jMenuClientselectionProductNotUptodateOrBroken);
		jMenuClientselection.add(jMenuClientselectionFailedProduct);
		jMenuClientselection.add(jMenuClientselectionFailedInPeriod);

		// jMenuClientselection.add(jMenuClientselectionSaveGroup);
		// ----------
		jMenuClientselection.addSeparator();
		jMenuClientselection.add(jMenuClientselectionDeselect);
		jMenuClientselection.add(jMenuClientselectionToggleClientFilter);
	}

	private void setupMenuFrames() {
		jMenuFrames.setText(configed.getResourceValue("MainFrame.jMenuFrames"));

		jMenuFrameLicences.setText(configed.getResourceValue("MainFrame.jMenuFrameLicences"));
		jMenuFrameLicences.setEnabled(false);
		jMenuFrameLicences.addActionListener(this);

		jMenuFrameWorkOnProducts.setText(configed.getResourceValue("MainFrame.jMenuFrameWorkOnProducts"));
		jMenuFrameWorkOnProducts.addActionListener(this);

		jMenuFrameWorkOnGroups.setText(configed.getResourceValue("MainFrame.jMenuFrameWorkOnGroups"));
		jMenuFrameWorkOnGroups.setVisible(main.getPersistenceController().isWithLocalImaging());
		jMenuFrameWorkOnGroups.addActionListener(this);

		jMenuFrameShowDialogs.setText(configed.getResourceValue("MainFrame.jMenuFrameShowDialogs"));
		jMenuFrameShowDialogs.setEnabled(false);
		jMenuFrameShowDialogs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logging.info(this, "actionPerformed");
				executeCommandOnInstances("arrange", FEditObject.runningInstances.getAll());
			}

		});

		jMenuFrames.add(jMenuFrameLicences);
		jMenuFrames.add(jMenuFrameWorkOnProducts);
		jMenuFrames.add(jMenuFrameWorkOnGroups);
		jMenuFrames.addSeparator();
		jMenuFrames.add(jMenuFrameShowDialogs);

	}

	private void setupMenuHelp() {
		jMenuHelp.setText(configed.getResourceValue("MainFrame.jMenuHelp"));

		jMenuHelpDoc.setText(configed.getResourceValue("MainFrame.jMenuDoc"));;
		jMenuHelp.add(jMenuHelpDoc);

		jMenuHelpForum.setText(configed.getResourceValue("MainFrame.jMenuForum"));
		jMenuHelp.add(jMenuHelpForum);

		jMenuHelpSupport.setText(configed.getResourceValue("MainFrame.jMenuSupport"));

		jMenuHelp.add(jMenuHelpSupport);

		jMenuHelp.addSeparator();

		jMenuHelpOpsiVersion
				.setText(configed.getResourceValue("MainFrame.jMenuHelpOpsiService") + ": " + main.getOpsiVersion());
		jMenuHelpOpsiVersion.setEnabled(false);
		jMenuHelpOpsiVersion.setForeground(Globals.lightBlack);

		// dummy entry just for displaying the version
		/*
		 * jMenuHelpOpsiVersion.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * }
		 * });
		 */
		jMenuHelp.add(jMenuHelpOpsiVersion);

		jMenuHelpOpsiModuleInformation.setText(configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		if (main.getOpsiVersion().length() == 0 || main.getOpsiVersion().charAt(0) == '<'
				|| main.getOpsiVersion().compareTo("3.4") < 0) {
			jMenuHelpOpsiModuleInformation.setEnabled(false);
		} else {
			jMenuHelpOpsiModuleInformation.addActionListener((ActionEvent e) -> showOpsiModules());
		}

		jMenuHelp.add(jMenuHelpOpsiModuleInformation);

		jMenuHelpInternalConfiguration.setText(configed.getResourceValue("MainFrame.jMenuHelpInternalConfiguration"));
		jMenuHelpInternalConfiguration.addActionListener((ActionEvent e) -> showBackendConfigurationAction());
		jMenuHelp.add(jMenuHelpInternalConfiguration);

		ActionListener selectLoglevelListener = (ActionEvent e) -> {
			for (int i = 0; i < logging.LEVEL_SECRET; i++) {
				if (e.getSource() == rbLoglevelItems[i]) {
					rbLoglevelItems[i].setSelected(true);
					logging.setLogLevel(i);
				} else {
					if (rbLoglevelItems[i] != null)
						rbLoglevelItems[i].setSelected(false);
				}
			}
		};

		jMenuHelpLoglevel.setText(configed.getResourceValue("MainFrame.jMenuLoglevel"));

		for (int i = logging.LEVEL_NONE; i <= logging.LEVEL_SECRET; i++) {
			rbLoglevelItems[i] = new JRadioButtonMenuItem("[" + i + "] " + logging.levelText(i).toLowerCase());
			//String commented = configed.getResourceValue("MainFrame.jMenuLoglevel." + i);
			//if (!configed.getResourceValue("MainFrame.jMenuLoglevel." + i).equals("MainFrame.jMenuLoglevel." + i))
			//	rbLoglevelItems[i].setText(commented);

			jMenuHelpLoglevel.add(rbLoglevelItems[i]);
			if (i == logging.LOG_LEVEL_CONSOLE)
				rbLoglevelItems[i].setSelected(true);

			rbLoglevelItems[i].addActionListener(selectLoglevelListener);
		}
		jMenuHelp.add(jMenuHelpLoglevel);

		jMenuHelpServerInfoPage.setText("opsi server InfoPage");

		jMenuHelpServerInfoPage.addActionListener((ActionEvent e) -> showInfoPage());

		jMenuHelpLogfileLocation.setText(configed.getResourceValue("MainFrame.jMenuHelpLogfileLocation"));
		jMenuHelpLogfileLocation.addActionListener((ActionEvent e) -> showLogfileLocationAction());

		jMenuHelp.add(jMenuHelpLogfileLocation);

		jMenuHelp.addSeparator();

		jMenuHelpAbout.setText(configed.getResourceValue("MainFrame.jMenuHelpAbout"));
		jMenuHelpAbout.addActionListener((ActionEvent e) -> showAboutAction());

		jMenuHelp.add(jMenuHelpAbout);
	}

	// ------------------------------------------------------------------------------------------
	// icon pane
	private void setupIcons1() {
		// buttonWindowStack = new
		// JButton(Globals.createImageIcon("images/stackWindows1.png","") );
		// buttonWindowStack.setText("12");
		// buttonWindowStack.setHorizontalAlignment(SwingConstants.TRAILING);

		/*
		 * buttonWindowStack = new IconButton(
		 * "fenster anordnen",
		 * "images/stackWindows1.png",
		 * "images/stackWindows1.png",
		 * "");
		 * buttonWindowStack.setEnabled(false);
		 * buttonWindowStack.addActionListener(
		 * new java.awt.event.ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * executeCommandOnInstances( "arrange", FEditObject.runningInstances.getAll()
		 * );
		 * }
		 * });
		 */

		iconButtonReload = new IconButton(de.uib.configed.configed.getResourceValue("MainFrame.iconButtonReload"),
				"images/reload.gif", "images/reload_over.gif", " ");

		iconButtonReloadLicenses = new IconButton(
				de.uib.configed.configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"),
				"images/reload_licenses.png", "images/reload_licenses_over.png", " ", false);
		iconButtonReloadLicenses.setVisible(false);

		iconButtonNewClient = new IconButton(de.uib.configed.configed.getResourceValue("MainFrame.iconButtonNewClient"),
				"images/newClient.gif", "images/newClient_over.gif", " ");

		iconButtonSetGroup = new IconButton(de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSetGroup"),
				"images/setGroup.gif", "images/setGroup_over.gif", " ");
		iconButtonSaveConfiguration = new IconButton(
				de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
				"images/apply_over.gif", " ", "images/apply_disabled.gif", false);

		iconButtonCancelChanges = new IconButton(
				de.uib.configed.configed.getResourceValue("MainFrame.iconButtonCancelChanges"), "images/cancel-32.png",
				"images/cancel_over-32.png", " ", false);

		iconButtonReachableInfo = new IconButton(
				de.uib.configed.configed.getResourceValue("MainFrame.iconButtonReachableInfo"),
				"images/new_networkconnection.png", "images/new_networkconnection.png",
				"images/new_networkconnection.png", main.host_displayFields.get("clientConnected"));

		String[] waitingCircle = new String[] { "images/systemusers_sessioninfo_activitycircle/loading_01.png",
				"images/systemusers_sessioninfo_activitycircle/loading_02.png" };
		/*
		 * {
		 * "images/systemusers_sessioninfo_loadingcircle/loading_02.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_03.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_04.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_05.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_06.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_07.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_09.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_10.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_11.png",
		 * "images/systemusers_sessioninfo_loadingcircle/loading_12.png"
		 * };
		 */

		iconButtonSessionInfo = new IconButton(
				de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSessionInfo"),
				"images/system-users-query.png", "images/system-users-query_over.png",
				"images/system-users-query_over.png", waitingCircle,
				// new String[]{"images/system-users-query_waiting_full.png",
				// "images/system-users-query_waiting_half.png"},
				500, main.host_displayFields.get("clientSessionInfo"));
		iconButtonSessionInfo.setEnabled(true);

		// iconButtonUptimeInfo = new IconButton(
		/*
		 * iconButtonSessionInfo = new IconButton(
		 * de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSessionInfo"),
		 * "images/uptime.png",
		 * "images/uptime.png",
		 * "images/uptime.png",
		 * 
		 * main.host_displayFields.get("clientSessionInfo"));
		 * iconButtonSessionInfo.setEnabled( true );
		 */

		iconButtonToggleClientFilter = new IconButton(
				de.uib.configed.configed.getResourceValue("MainFrame.iconButtonToggleClientFilter"),
				"images/view-filter_disabled-32.png", "images/view-filter_over-32.png", "images/view-filter-32.png",
				true);

		iconButtonSaveGroup = new IconButton(de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSaveGroup"),
				"images/saveGroup.gif", "images/saveGroup_over.gif", " ");

		iconButtonReload.addActionListener((ActionEvent e) -> reloadAction());

		iconButtonReloadLicenses.addActionListener((ActionEvent e) -> reloadLicensesAction());

		iconButtonNewClient.addActionListener((ActionEvent e) -> addClientAction());

		iconButtonSetGroup.addActionListener((ActionEvent e) -> callSelectionDialog());

		iconButtonSaveConfiguration.addActionListener((ActionEvent e) -> saveAction());

		iconButtonCancelChanges.addActionListener((ActionEvent e) -> cancelAction());

		iconButtonReachableInfo.addActionListener((ActionEvent e) -> getReachableInfo());

		iconButtonSessionInfo.addActionListener((ActionEvent e) -> {
			main.setColumnSessionInfo(true);
			getSessionInfo();
		});

		iconButtonToggleClientFilter.addActionListener((ActionEvent e) -> toggleClientFilterAction());

		iconButtonSaveGroup.addActionListener((ActionEvent e) -> saveGroupAction());

		proceeding = new JPanel();
		ActivityPanel activity = new ActivityPanel();
		proceeding.add(activity);
		new Thread(activity).start();
		proceeding.setToolTipText("activity indicator");
	}

	/**
	 * Invoked when task's progress property changes. public void
	 * propertyChange(PropertyChangeEvent evt) { if ("progress" ==
	 * evt.getPropertyName()) { int progress = (Integer) evt.getNewValue();
	 * proceeding.setIndeterminate(false); proceeding.setValue(progress);
	 * //taskOutput.append(String.format( // "Completed %d%% of task.\n",
	 * progress)); } }
	 */
	// ------------------------------------------------------------------------------------------
	// context menus

	private void setupPopupMenuClientsTab() {
		/*
		 * popupDisplayClientList.setText(configed.getResourceValue(
		 * "MainFrame.jMenuShowSelectedClients"));
		 * popupDisplayClientList.setFont(Globals.defaultFontBig);
		 * 
		 * setClientSelectionText(""); // creates fListSelectedClients with empty
		 * content
		 * popupDisplayClientList.addItemListener(new ItemListener()
		 * {
		 * public void itemStateChanged (ItemEvent e)
		 * {
		 * if (fListSelectedClients != null)
		 * fListSelectedClients.setVisible(popupDisplayClientList.isSelected());
		 * }
		 * });
		 */

		popupShowCreatedColumn.setText(configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		combinedMenuItemCreatedColumn.show(main.host_displayFields.get(HostInfo.created_DISPLAY_FIELD_LABEL));

		popupShowCreatedColumn.addItemListener((ItemEvent e) -> main.toggleColumnCreated());

		popupShowWANactiveColumn.setText(configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		combinedMenuItemWANactiveColumn.show(main.host_displayFields.get(HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL));

		popupShowWANactiveColumn.addItemListener((ItemEvent e) -> main.toggleColumnWANactive());

		popupShowIPAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		combinedMenuItemIPAddressColumn.show(main.host_displayFields.get(HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL));

		popupShowIPAddressColumn.addItemListener((ItemEvent e) -> main.toggleColumnIPAddress());

		popupShowHardwareAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		combinedMenuItemHardwareAddressColumn
				.show(main.host_displayFields.get(HostInfo.clientMacAddress_DISPLAY_FIELD_LABEL));

		popupShowHardwareAddressColumn.addItemListener((ItemEvent e) -> main.toggleColumnHardwareAddress());

		popupShowSessionInfoColumn.setText(configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		combinedMenuItemSessionInfoColumn
				.show(main.host_displayFields.get(HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL));

		popupShowSessionInfoColumn.addItemListener((ItemEvent e) -> main.toggleColumnSessionInfo());

		popupShowInventoryNumberColumn.setText(configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		combinedMenuItemInventoryNumberColumn
				.show(main.host_displayFields.get(HostInfo.clientInventoryNumber_DISPLAY_FIELD_LABEL));

		popupShowInventoryNumberColumn.addItemListener((ItemEvent e) -> main.toggleColumnInventoryNumber());

		popupShowUefiBoot.setText(configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		combinedMenuItemUefiBootColumn.show(main.host_displayFields.get(HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL));

		popupShowUefiBoot.addItemListener((ItemEvent e) -> main.toggleColumnUEFIactive());

		popupShowInstallByShutdownColumn.setText(configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		combinedMenuItemInstallByShutdownColumn
				.show(main.host_displayFields.get(HostInfo.clientInstallByShutdown_DISPLAY_FIELD_LABEL));

		popupShowInstallByShutdownColumn.addItemListener((ItemEvent e) -> main.toggleColumnInstallByShutdownActive());

		popupShowDepotColumn.setText(configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		combinedMenuItemDepotColumn.show(main.host_displayFields.get(HostInfo.depotOfClient_DISPLAY_FIELD_LABEL));

		popupShowDepotColumn.addItemListener((ItemEvent e) -> main.toggleColumnDepot());

		popupChangeDepot.setText(configed.getResourceValue("MainFrame.jMenuChangeDepot"));

		popupChangeDepot.addActionListener((ActionEvent e) -> changeDepotAction());

		popupChangeClientID.setText(configed.getResourceValue("MainFrame.jMenuChangeClientID"));

		popupChangeClientID.addActionListener((ActionEvent e) -> changeClientIDAction());

		popupResetProductOnClientWithStates
				.setText(configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithStates"));

		popupResetProductOnClientWithStates.addActionListener((ActionEvent e) -> resetProductOnClientAction(true));

		popupResetProductOnClient
				.setText(configed.getResourceValue("MainFrame.jMenuResetProductOnClientWithoutStates"));

		popupResetProductOnClient.addActionListener((ActionEvent e) -> resetProductOnClientAction(false));

		popupAddClient.setText(configed.getResourceValue("MainFrame.jMenuAddClient"));

		popupAddClient.addActionListener((ActionEvent e) -> addClientAction());

		menuItemsHost.get(ITEM_ADD_CLIENT).add(popupAddClient);

		popupWakeOnLan.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan"));

		popupWakeOnLanDirect.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan.direct"));
		popupWakeOnLanDirect.addActionListener((ActionEvent e) -> wakeOnLanAction());
		popupWakeOnLan.add(popupWakeOnLanDirect);

		final MainFrame f = this;
		popupWakeOnLanScheduler.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler"));
		popupWakeOnLanScheduler.addActionListener((ActionEvent e) -> {
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(f,
					Globals.APPNAME + ": " + configed.getResourceValue("FStartWakeOnLan.title"), main);
			fStartWakeOnLan.centerOn(f);
			// fStartWakeOnLan.setup();
			fStartWakeOnLan.setVisible(true);
			fStartWakeOnLan.setPredefinedDelays(labelledDelays);

			fStartWakeOnLan.setClients();
		});
		popupWakeOnLan.add(popupWakeOnLanScheduler);

		popupDeletePackageCaches.setText(configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
		popupDeletePackageCaches.addActionListener((ActionEvent e) -> deletePackageCachesAction());

		// subOpsiClientdEvent = new JMenu("abcd");
		// configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent")
		// );
		/*
		 * for (final String event :
		 * main.getPersistenceController().getOpsiclientdExtraEvents())
		 * {
		 * JMenuItem item = new JMenuItemFormatted(event);
		 * item.setFont(Globals.defaultFont);
		 * 
		 * item.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * fireOpsiclientdEventAction(event);
		 * }
		 * });
		 * 
		 * subOpsiClientdEvent.add(item);
		 * }
		 */

		popupShowPopupMessage.setText(configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
		popupShowPopupMessage.addActionListener((ActionEvent e) -> showPopupOnClientsAction());

		popupRequestSessionInfo.setText(configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
		popupRequestSessionInfo.addActionListener((ActionEvent e) -> getSessionInfo());

		popupShutdownClient.setText(configed.getResourceValue("MainFrame.jMenuShutdownClient"));
		popupShutdownClient.addActionListener((ActionEvent e) -> shutdownClientsAction());

		popupRebootClient.setText(configed.getResourceValue("MainFrame.jMenuRebootClient"));
		popupRebootClient.addActionListener((ActionEvent e) -> rebootClientsAction());

		popupDeleteClient.setText(configed.getResourceValue("MainFrame.jMenuDeleteClient"));
		popupDeleteClient.addActionListener((ActionEvent e) -> deleteClientAction());

		menuItemsHost.get(ITEM_DELETE_CLIENT).add(popupDeleteClient);

		popupFreeLicences.setText(configed.getResourceValue("MainFrame.jMenuFreeLicences"));
		popupFreeLicences.addActionListener((ActionEvent e) -> freeLicencesAction());

		menuItemsHost.get(ITEM_FREE_LICENCES).add(popupFreeLicences);

		popupRemoteControl.setText(configed.getResourceValue("MainFrame.jMenuRemoteControl"));

		popupRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		popupRemoteControl.addActionListener((ActionEvent e) -> remoteControlAction());

		popupSelectionGetGroup.setText(configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		popupSelectionGetGroup.addActionListener((ActionEvent e) -> callSelectionDialog());

		popupSelectionGetSavedSearch.setText(configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		popupSelectionGetSavedSearch.addActionListener((ActionEvent e) -> main.clientSelectionGetSavedSearch());

		/*
		 * popupSelectionDeselect.setText(
		 * configed.getResourceValue("MainFrame.jMenuClientselectionDeselect") );
		 * popupSelectionDeselect.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * noGroupingAction();
		 * }
		 * });
		 */

		// pdf generating
		popupCreatePdf.setText(configed.getResourceValue("FGeneralDialog.pdf"));
		popupCreatePdf.addActionListener((ActionEvent e) -> createPdf());
		//

		popupSelectionToggleClientFilter
				.setText(configed.getResourceValue("MainFrame.jMenuClientselectionToggleClientFilter"));
		popupSelectionToggleClientFilter.setState(false);
		popupSelectionToggleClientFilter.setFont(Globals.defaultFontBig);

		popupSelectionToggleClientFilter.addActionListener((ActionEvent e) -> toggleClientFilterAction());

		popupRebuildClientList.addActionListener((ActionEvent e) -> main.reloadHosts());

		// ----

		popupClients.add(popupWakeOnLan);

		menuPopupOpsiClientdEvent = new JMenu(configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : main.getPersistenceController().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItemFormatted(event);
			item.setFont(Globals.defaultFont);

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
		popupClients.add(popupDeleteClient);
		popupClients.add(popupResetProductOnClientWithStates);
		popupClients.add(popupResetProductOnClient);
		popupClients.add(popupFreeLicences);
		popupClients.add(popupChangeClientID);
		if (multidepot)
			popupClients.add(popupChangeDepot);
		popupClients.addSeparator();
		// popupClients.add(popupDisplayClientList);
		popupClients.add(popupShowWANactiveColumn);
		popupClients.add(popupShowIPAddressColumn);
		popupClients.add(popupShowHardwareAddressColumn);
		popupClients.add(popupShowSessionInfoColumn);
		popupClients.add(popupShowInventoryNumberColumn);
		popupClients.add(popupShowCreatedColumn);
		popupClients.add(popupShowUefiBoot);
		popupClients.add(popupShowInstallByShutdownColumn);
		popupClients.add(popupShowDepotColumn);

		// ----
		popupClients.addSeparator();
		popupClients.add(popupSelectionGetGroup);
		popupClients.add(popupSelectionGetSavedSearch);

		// popupClients.add(popupSelectionSaveGroup);
		// popupClients.add(popupSelectionDeleteGroup);
		popupClients.addSeparator();
		popupClients.add(popupSelectionDeselect);
		popupClients.add(popupSelectionToggleClientFilter);

		// popupClients.addSeparator();
		popupClients.add(popupRebuildClientList);
		popupClients.add(popupCreatePdf);

		exportTable.addMenuItemsTo(popupClients);

	}

	public void createPdf() {
		TableModel tm = main.getSelectedClientsTableModel();
		JTable jTable = new JTable(tm);
		// logging.debug("Gruppe in createPDF: " + statusPane.getGroupName());
		try {
			HashMap<String, String> metaData = new HashMap<String, String>();
			String title = configed.getResourceValue("MainFrame.ClientList");
			// group: " + statusPane.getGroupName()
			// jTable;
			if (statusPane.getGroupName().length() != 0) {
				title = title + ": " + statusPane.getGroupName();
			}
			metaData.put("header", title);
			title = "";
			if (statusPane.getInvolvedDepots().length() != 0) {
				title = title + "Depot(s) : " + statusPane.getInvolvedDepots();
			}
			/*
			 * if (statusPane.getSelectedClientNames().length()!=0) {
			 * title = title + "; Clients: " + statusPane.getSelectedClientNames();
			 * }
			 */

			metaData.put("title", title);
			metaData.put("subject", "report of table");
			metaData.put("keywords", "");

			ExporterToPDF pdfExportTable = new ExporterToPDF(panelClientlist.getTable());

			pdfExportTable.setMetaData(metaData);
			pdfExportTable.setPageSizeA4_Landscape();
			pdfExportTable.execute(null, jTable.getSelectedRowCount() != 0);

			/**
			 * old pdf exporting tableToPDF = new DocumentToPdf (null,
			 * metaData); // no filename, metadata
			 * tableToPDF.createContentElement("table", jTable);
			 * tableToPDF.setPageSizeA4_Landscape(); // tableToPDF.toPDF(); //
			 * create Pdf
			 */
		} catch (Exception ex) {
			logging.error("pdf printing error " + ex);
		}
	}

	// ------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------

	public void clear() {
		baseContainer.remove(allPane);
	}

	public void updateHostCheckboxenText() {
		if (main.getPersistenceController().isWithUEFI()) {
			cbUefiBoot.setText(configed.getResourceValue("NewClientDialog.boottype"));
		} else {
			cbUefiBoot.setText(configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			cbUefiBoot.setEnabled(false);
		}

		if (main.getPersistenceController().isWithWAN()) {
			cbWANConfig.setText(configed.getResourceValue("NewClientDialog.wanConfig"));
		} else {
			cbWANConfig.setText(configed.getResourceValue("NewClientDialog.wan_not_activated"));
			cbWANConfig.setEnabled(false);
		}
	}

	private void guiInit() {
		this.addWindowListener(this);
		this.setFont(Globals.defaultFont);
		this.setIconImage(Globals.mainIcon);

		// setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[your
		// symbol]")));

		allPane = new SizeListeningPanel();
		// allPane = (JPanel) this.getallPane();

		// contentLayout = new GroupLayout(allPane);
		// allPane.setLayout(contentLayout);
		allPane.setLayout(borderLayout1);

		baseContainer.add(allPane);

		initMenuData();

		setupMenuLists();

		setupMenuFile();
		setupMenuGrouping();
		setupMenuClients();
		setupMenuServer();
		setupMenuFrames();
		setupMenuHelp();

		jMenuBar1.add(jMenuFile);
		// jMenuBar1.add(jMenuFileLanguage);
		jMenuBar1.add(jMenuClientselection);
		jMenuBar1.add(jMenuClients);
		jMenuBar1.add(jMenuServer);
		jMenuBar1.add(jMenuFrames);
		jMenuBar1.add(jMenuHelp);

		this.setJMenuBar(jMenuBar1);

		setupPopupMenuClientsTab();

		// clientPane
		clientPane = new JPanel();

		/*
		 * clientPaneComponentListener = new ComponentAdapter(){
		 * public void componentResized(ComponentEvent e)
		 * {
		 * clientPaneW = getSize().width;
		 * 
		 * logging.info(this, "componentResized new width " + clientPaneW
		 * );
		 * 
		 * }
		 * }
		 * ;
		 * 
		 * 
		 * clientPane.addComponentListener( clientPaneComponentListener );
		 */

		clientPane.setPreferredSize(new Dimension(fwidth_righthanded, fheight + 40));
		clientPane.setBorder(Globals.createPanelBorder());
		// new LineBorder(Globals.backBlue, 2, true));
		csClientPane = new Containership(clientPane);

		GroupLayout layoutClientPane = new GroupLayout(clientPane);
		clientPane.setLayout(layoutClientPane);

		labelHost = new JLabel((Icon) Globals.createImageIcon("images/client.png", ""), SwingConstants.LEFT);
		labelHost.setPreferredSize(Globals.buttonDimension);

		labelHostID = new JLabel("");
		labelHostID.setFont(Globals.defaultFontStandardBold);

		// JLabel labelBelongsTo = new JLabel("In Depot");
		// labelBelongsTo.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientDescription = new JLabel(configed.getResourceValue("MainFrame.jLabelDescription"));
		labelClientDescription.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientInventoryNumber = new JLabel(configed.getResourceValue("MainFrame.jLabelInventoryNumber"));
		labelClientInventoryNumber.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientNotes = new JLabel(configed.getResourceValue("MainFrame.jLabelNotes"));
		// jLabelClientNotes.setFont(Globals.defaultFontStandardBold);

		JLabel labelClientMacAddress = new JLabel(configed.getResourceValue("MainFrame.jLabelMacAddress"));
		// jLabelClientMacAddress.setFont(Globals.defaultFontStandardBold);

		JLabel labelClientIPAddress = new JLabel(configed.getResourceValue("MainFrame.jLabelIPAddress")); // configed.getResourceValue("MainFrame.jLabelIPAddress")
																											// );

		JLabel labelOneTimePassword = new JLabel(configed.getResourceValue("MainFrame.jLabelOneTimePassword"));

		JLabel labelOpsiHostKey = new JLabel("opsiHostKey");

		jFieldInDepot = new JTextArea();
		jFieldInDepot.setEditable(false);
		jFieldInDepot.setFont(Globals.defaultFontBig);
		jFieldInDepot.setBackground(Globals.backgroundLightGrey);

		jTextFieldDescription = new JTextEditorField("");
		jTextFieldDescription.setEditable(true);
		jTextFieldDescription.setPreferredSize(Globals.textfieldDimension);
		jTextFieldDescription.setFont(Globals.defaultFontBig);
		jTextFieldDescription.addKeyListener(this);
		jTextFieldDescription.addMouseListener(this);

		jTextFieldInventoryNumber = new JTextEditorField("");
		jTextFieldInventoryNumber.setEditable(true);
		jTextFieldInventoryNumber.setPreferredSize(Globals.textfieldDimension);
		jTextFieldInventoryNumber.setFont(Globals.defaultFontBig);
		jTextFieldInventoryNumber.addKeyListener(this);
		jTextFieldInventoryNumber.addMouseListener(this);

		jTextAreaNotes = new JTextArea();

		jTextAreaNotes.setEditable(true);
		jTextAreaNotes.setLineWrap(true);
		jTextAreaNotes.setWrapStyleWord(true);
		jTextAreaNotes.setFont(Globals.defaultFontBig);
		GraphicsEnvironment.getLocalGraphicsEnvironment();
		jTextAreaNotes.addKeyListener(this);
		jTextAreaNotes.addMouseListener(this);

		scrollpaneNotes = new JScrollPane(jTextAreaNotes);
		scrollpaneNotes.setPreferredSize(Globals.textfieldDimension);
		scrollpaneNotes.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpaneNotes.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		macAddressField = new JTextEditorField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3',
				'4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':', 2, true), "", 17);
		// new SeparatedField(6, 2, 2, ':', new char[] { '0', '1', '2', '3', '4', '5',
		// '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' } );
		macAddressField.addKeyListener(this);
		macAddressField.addMouseListener(this);

		ipAddressField = new JTextEditorField(new SeparatedDocument(
				/* allowedChars */ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' }, 12, '.', 3,
				false), "", 24);
		ipAddressField.addKeyListener(this);
		ipAddressField.addMouseListener(this);

		// JLabel labelUefiBoot = new JLabel( configed.getResourceValue("Uefi Boot" ) );

		final Icon unselectedIcon;
		final Icon selectedIcon;
		final Icon nullIcon;

		unselectedIcon = de.uib.configed.Globals.createImageIcon("images/checked_not.png", "");
		selectedIcon = de.uib.configed.Globals.createImageIcon("images/checked.png", "");
		nullIcon = de.uib.configed.Globals.createImageIcon("images/checked_box_mixed.png", "");

		cbUefiBoot = new CheckedLabel(configed.getResourceValue("NewClientDialog.boottype"), selectedIcon,
				unselectedIcon, nullIcon, false);
		cbUefiBoot.addActionListener(this);
		// JLabel labelWANConfig = new JLabel( configed.getResourceValue("vpnConfig" )
		// );
		cbWANConfig = new CheckedLabel(configed.getResourceValue("WAN Konfiguration"), selectedIcon, unselectedIcon,
				nullIcon, false);
		cbWANConfig.setSelected(false);
		cbWANConfig.setEnabled(true);
		cbWANConfig.addActionListener(this);

		cbInstallByShutdown = new CheckedLabel(configed.getResourceValue("NewClientDialog.installByShutdown"),
				selectedIcon, unselectedIcon, nullIcon, false);
		cbInstallByShutdown.setSelected(false);
		cbInstallByShutdown.setEnabled(true);
		cbInstallByShutdown.addActionListener(this);

		updateHostCheckboxenText();

		/*
		 * cbUefiBoot.setEnabled(true); //only for colors, therefore we remove/disable
		 * listeners
		 * cbUefiBoot.addKeyListener(new KeyAdapter(){
		 * public void keyPressed(KeyEvent e)
		 * {
		 * e.consume();
		 * //logging.info(this, " " + e);
		 * }
		 * }
		 * );
		 * 
		 * for (int i = 0; i < cbUefiBoot.getMouseListeners().length; i++)
		 * {
		 * cbUefiBoot.removeMouseListener(cbUefiBoot.getMouseListeners()[i]);
		 * }
		 */

		// cbUefiBoot.setText(configed.getResourceValue("Uefi Boot") /*, STATUS*/);

		jTextFieldOneTimePassword = new JTextEditorField("");
		jTextFieldOneTimePassword.addKeyListener(this);
		jTextFieldOneTimePassword.addMouseListener(this);

		jTextFieldHostKey = new JTextHideField();

		layoutClientPane.setHorizontalGroup(layoutClientPane.createParallelGroup()
				/////// HOST
				.addGroup(layoutClientPane.createSequentialGroup()
						.addComponent(labelHost, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)
						.addComponent(labelHostID, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				/////// DESCRIPTION
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelClientDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)
						.addComponent(jTextFieldDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				/////// INVENTORY NUMBER
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelClientInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)
						.addComponent(jTextFieldInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				/////// MAC ADDRESS
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelClientMacAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)
						.addComponent(macAddressField, Globals.firstLabelWidth, Globals.firstLabelWidth,
								Globals.firstLabelWidth)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))

				/////// IP ADDRESS
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelClientIPAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)
						.addComponent(ipAddressField, Globals.firstLabelWidth, Globals.firstLabelWidth,
								Globals.firstLabelWidth)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))

				/////// INSTALL BY SHUTDOWN

				/*
				 * .addGroup(layoutClientPane.createSequentialGroup()
				 * .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				 * .addGap(Globals.minHGapSize,Globals.hGapSize,Globals.hGapSize)
				 * .addComponent(btnAktivateInstallByShutdown) //, 0 , Globals.buttonHeight ,
				 * Globals.buttonHeight )
				 * .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
				 * .addComponent(btnDeaktivateInstallByShutdown)//, 0 , Globals.buttonWidth/4 ,
				 * Globals.buttonHeight )
				 * // .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
				 * )
				 */

				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(cbInstallByShutdown, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize))

				/////// UEFI BOOT
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(cbUefiBoot, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize))
				/////// WAN CONFIG
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(cbWANConfig, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize))

				// .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				/////// ONE TIME PASSWORD
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)
						.addComponent(jTextFieldOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))

				////// opsiHostKey
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelOpsiHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))

				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)
						.addComponent(jTextFieldHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))

				/////// NOTES
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelClientNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)
						.addComponent(scrollpaneNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize)));

		layoutClientPane.setVerticalGroup(layoutClientPane.createSequentialGroup()
				/////// HOST
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize).addComponent(labelHost)
				.addComponent(labelHostID, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				/////// DESCRIPTION
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addComponent(labelClientDescription)
				.addComponent(jTextFieldDescription, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				/////// INVENTORY NUMBER
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addComponent(labelClientInventoryNumber)
				.addComponent(jTextFieldInventoryNumber, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				/////// MAC ADDRESS
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addComponent(labelClientMacAddress)
				.addComponent(macAddressField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				/////// IP ADDRESS
				.addComponent(labelClientIPAddress)
				.addComponent(ipAddressField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)

				////// INSTALL BY SHUTDOWN
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				// .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
				.addComponent(cbInstallByShutdown, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)

				/*
				 * .addGroup( layoutClientPane.createParallelGroup(GroupLayout.Alignment.CENTER
				 * )
				 * .addGap(Globals.minVGapSize*2,Globals.minVGapSize*2,Globals.minVGapSize*2)
				 * .addComponent(jLabel_InstallByShutdown)
				 * // .addGroup(
				 * layoutClientPane.createParallelGroup(GroupLayout.Alignment.CENTER )
				 * .addGap(Globals.minVGapSize*2,Globals.minVGapSize*2,Globals.minVGapSize*2)
				 * .addComponent(btnAktivateInstallByShutdown)
				 * .addGap(Globals.minVGapSize*2,Globals.minVGapSize*2,Globals.minVGapSize*2)
				 * .addComponent(btnDeaktivateInstallByShutdown)
				 * )
				 */
				/////// UEFI BOOT & WAN Config

				.addComponent(cbUefiBoot, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				// .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
				.addComponent(cbWANConfig, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)

				/////// ONE TIME PASSWORD
				.addGap(Globals.minVGapSize * 2, Globals.minVGapSize * 2, Globals.minVGapSize * 2)
				.addComponent(labelOneTimePassword, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(jTextFieldOneTimePassword, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)

				////// opsiHostKey
				// .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
				.addComponent(labelOpsiHostKey, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(jTextFieldHostKey, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)

				/////// NOTES
				.addGap(Globals.minVGapSize * 2, Globals.minVGapSize * 2, Globals.minVGapSize * 2)
				.addComponent(labelClientNotes)
				.addComponent(scrollpaneNotes, Globals.lineHeight, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
		/**/

		jPanel3.setBorder(BorderFactory.createEtchedBorder());
		jPanel3.setLayout(new BorderLayout());

		// jLabel_Clientname.setFont(Globals.defaultFontBig);
		// jLabel_Clientname.setText(
		// configed.getResourceValue("MainFrame.jLabel_Clientname") );
		// jLabel_Productselection.setFont(Globals.defaultFontBig);
		jCheckBoxSorted.setSelected(true);
		jCheckBoxSorted.setText(configed.getResourceValue("MainFrame.jCheckBoxSorted"));

		jButtonSaveList.setText(configed.getResourceValue("MainFrame.jButtonSaveList"));
		jButtonSaveList.setBackground(Globals.backBlue);
		jButtonSaveList.addActionListener(this::jButtonSaveList_actionPerformed);

		jRadioRequiredAll.setMargin(new Insets(0, 0, 0, 0));
		jRadioRequiredAll.setAlignmentY((float) 0.0);
		jRadioRequiredAll.setText(configed.getResourceValue("MainFrame.jRadioRequiredAll"));
		jRadioRequiredOff.setMargin(new Insets(0, 0, 0, 0));
		jRadioRequiredOff.setSelected(true);
		jRadioRequiredOff.setText(configed.getResourceValue("MainFrame.jRadioRequiredOff"));
		jRadioRequiredOff.setToolTipText("");

		jLabelPath.setText(configed.getResourceValue("MainFrame.jLabelPath"));
		jLabel_Hostinfos.setText(configed.getResourceValue("MainFrame.jLabel_Hostinfos"));

		buttonGroupRequired.add(jRadioRequiredAll);
		buttonGroupRequired.add(jRadioRequiredOff);

		jComboBoxProductValues.setBackground(Globals.backBlue);
		jComboBoxProductValues.addActionListener(this::jComboBoxProductValues_actionPerformed);

		treeClients.setFont(Globals.defaultFont);
		// treeClients.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

		scrollpaneTreeClients = new JScrollPane();
		/*
		 * {
		 * public JScrollBar createVerticalScrollBar()
		 * {
		 * //Dimension minDim = new Dimension(10,2);
		 * final JScrollBar result = new JScrollBar();
		 * 
		 * BoundedRangeModel rangeModel = new DefaultBoundedRangeModel(2,5,1,40);
		 * rangeModel.addChangeListener(
		 * new ChangeListener()
		 * {
		 * public void stateChanged( ChangeEvent e )
		 * {
		 * logging.info(this, "in " + result + " changeEvent " + e);
		 * }
		 * }
		 * );
		 * 
		 * result.setModel(rangeModel);
		 * 
		 * 
		 * 
		 * logging.info(this, " just created");
		 * 
		 * return result;
		 * }
		 * };
		 * 
		 * 
		 */

		scrollpaneTreeClients.getViewport().add(treeClients);
		scrollpaneTreeClients.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setPreferredSize(treeClients.getMaximumSize());

		logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimum() " +

				scrollpaneTreeClients.getVerticalScrollBar().getMinimum()

		);

		logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() " +

				scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize()

		);

		// scrollpaneTreeClients.getVerticalScrollBar().setMinimumSize( null ); //new
		// Dimension(2,2) );
		logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() " +

				scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize()

		);
		JSplitPane splitpaneClientSelection = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
				depotListPresenter.getScrollpaneDepotslist(), scrollpaneTreeClients);

		logging.info(this, "multidepot " + multidepot);
		if (multidepot)
			splitpaneClientSelection.setDividerLocation(dividerLocationClientTreeMultidepot);
		else
			splitpaneClientSelection.setDividerLocation(dividerLocationClientTreeSingledepot);

		// logging.info(this, "treeClients.getMaximumSize() " +
		// treeClients.getMaximumSize());
		// logging.info(this, "depotslist.getMaximumSize() " +
		// depotslist.getMaximumSize());

		// System.exit(0);
		panelTreeClientSelection = new JPanel();
		GroupLayout layoutPanelTreeClientSelection = new GroupLayout(panelTreeClientSelection);
		panelTreeClientSelection.setLayout(layoutPanelTreeClientSelection);

		layoutPanelTreeClientSelection.setHorizontalGroup(layoutPanelTreeClientSelection.createSequentialGroup()
				.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
				.addGroup(layoutPanelTreeClientSelection.createParallelGroup(GroupLayout.Alignment.LEADING)

						.addComponent(depotListPresenter, minHSizeTreePanel, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						/*
						 * .addGroup(layoutPanelTreeClientSelection.createSequentialGroup()
						 * .addGap(10)
						 * .addComponent(labelDepotServer, 50, GroupLayout.PREFERRED_SIZE,
						 * Short.MAX_VALUE)
						 * .addGap(10)
						 * .addComponent(buttonSelectDepotsWithEqualProperties,
						 * Globals.squareButtonWidth, GroupLayout.PREFERRED_SIZE,
						 * GroupLayout.PREFERRED_SIZE)
						 * .addComponent(buttonSelectDepotsAll, Globals.squareButtonWidth,
						 * GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						 * //.addComponent(buttonCommitChangedDepotSelection,
						 * GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						 * GroupLayout.PREFERRED_SIZE)
						 * //.addComponent(buttonCancelChangedDepotSelection,
						 * GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						 * GroupLayout.PREFERRED_SIZE)
						 * .addGap(10, 10, 10)
						 * )
						 */
						.addComponent(splitpaneClientSelection, minHSizeTreePanel, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				// .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
				// .addComponent(groupActionPanel, GroupLayout.PREFERRED_SIZE,
				// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2));

		layoutPanelTreeClientSelection.setVerticalGroup(layoutPanelTreeClientSelection
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				// .addComponent(groupActionPanel, Globals.vGapSize/2,
				// GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGroup(layoutPanelTreeClientSelection.createSequentialGroup()
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
						.addComponent(depotListPresenter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(splitpaneClientSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)));

		jButtonServerConfiguration = new JButton("", Globals.createImageIcon("images/opsiconsole_deselected.png", ""));
		jButtonServerConfiguration.setSelectedIcon(Globals.createImageIcon("images/opsiconsole.png", ""));
		jButtonServerConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonServerConfiguration.setToolTipText(configed.getResourceValue("MainFrame.labelServerConfiguration"));

		jButtonDepotsConfiguration = new JButton("", Globals.createImageIcon("images/opsidepots_deselected.png", ""));
		jButtonDepotsConfiguration.setSelectedIcon(Globals.createImageIcon("images/opsidepots.png", ""));
		jButtonDepotsConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonDepotsConfiguration.setToolTipText(configed.getResourceValue("MainFrame.labelDepotsConfiguration"));

		jButtonClientsConfiguration = new JButton("", Globals.createImageIcon("images/opsiclients_deselected.png", ""));
		jButtonClientsConfiguration.setSelectedIcon(Globals.createImageIcon("images/opsiclients.png", ""));
		jButtonClientsConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonClientsConfiguration.setToolTipText(configed.getResourceValue("MainFrame.labelClientsConfiguration"));

		jButtonLicences = new JButton("", Globals.createImageIcon("images/licences_deselected.png", ""));
		jButtonLicences.setEnabled(false);
		jButtonLicences.setSelectedIcon(Globals.createImageIcon("images/licences.png", ""));
		jButtonLicences.setPreferredSize(Globals.modeSwitchDimension);
		jButtonLicences.setToolTipText(configed.getResourceValue("MainFrame.labelLicences"));

		jButtonServerConfiguration.addActionListener(this);
		jButtonDepotsConfiguration.addActionListener(this);
		jButtonClientsConfiguration.addActionListener(this);
		jButtonLicences.addActionListener(this);

		jButtonWorkOnGroups = new JButton("", Globals.createImageIcon("images/group_all_unselected_40.png", ""));
		jButtonWorkOnGroups.setSelectedIcon(Globals.createImageIcon("images/group_all_selected_40.png", ""));
		jButtonWorkOnGroups.setPreferredSize(Globals.modeSwitchDimension);
		jButtonWorkOnGroups.setToolTipText(configed.getResourceValue("MainFrame.labelWorkOnGroups"));

		jButtonWorkOnGroups.setEnabled(
				// main.getPersistenceController().isWithScalability1()
				// ||
				main.getPersistenceController().isWithLocalImaging()
		// true
		);
		jButtonWorkOnGroups.addActionListener(this);

		jButtonWorkOnProducts = new JButton("", Globals.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setSelectedIcon(Globals.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setPreferredSize(Globals.modeSwitchDimension);
		jButtonWorkOnProducts.setToolTipText(configed.getResourceValue("MainFrame.labelWorkOnProducts"));

		/*
		 * jButtonWorkOnProducts.setEnabled(
		 * main.getPersistenceController().isWithScalability1()
		 * ||
		 * main.getPersistenceController().isWithLocalImaging()
		 * );
		 */
		jButtonWorkOnProducts.addActionListener(this);

		jButtonDash = new JButton("", Globals.createImageIcon("images/dash_unselected.png", ""));
		jButtonDash.setSelectedIcon(Globals.createImageIcon("images/dash_selected.png", ""));
		jButtonDash.setPreferredSize(Globals.modeSwitchDimension);
		jButtonDash.setToolTipText("Dashboard");

		jButtonDash.setEnabled(ConfigedMain.DASH_ENABLED);
		jButtonDash.setVisible(ConfigedMain.DASH_ENABLED);
		jButtonDash.addActionListener(this);

		iconPaneTargets = new JPanel();
		iconPaneTargets.setBorder(new LineBorder(Globals.blueGrey, 1, true));

		GroupLayout layoutIconPaneTargets = new GroupLayout(iconPaneTargets);
		iconPaneTargets.setLayout(layoutIconPaneTargets);

		layoutIconPaneTargets.setHorizontalGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layoutIconPaneTargets.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						// .addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						// .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)));
		layoutIconPaneTargets.setVerticalGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPaneTargets.createSequentialGroup()
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
						.addGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						// .addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						).addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)));

		iconPaneExtraFrames = new JPanel();
		iconPaneExtraFrames.setBorder(new LineBorder(Globals.blueGrey, 1, true));

		GroupLayout layoutIconPaneExtraFrames = new GroupLayout(iconPaneExtraFrames);
		iconPaneExtraFrames.setLayout(layoutIconPaneExtraFrames);

		layoutIconPaneExtraFrames
				.setHorizontalGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGroup(layoutIconPaneExtraFrames.createSequentialGroup()
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jButtonDash, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jButtonLicences, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)));
		layoutIconPaneExtraFrames
				.setVerticalGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layoutIconPaneExtraFrames.createSequentialGroup()
								.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
								.addGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonDash, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonLicences, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)));

		iconPane0 = new JPanel();

		GroupLayout layoutIconPane0 = new GroupLayout(iconPane0);
		iconPane0.setLayout(layoutIconPane0);

		layoutIconPane0
				.setHorizontalGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layoutIconPane0.createSequentialGroup()
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)));
		layoutIconPane0.setVerticalGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layoutIconPane0.createSequentialGroup()
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
						.addGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)));

		setupIcons1();
		iconPane1 = new JPanel();

		GroupLayout layoutIconPane1 = new GroupLayout(iconPane1);
		iconPane1.setLayout(layoutIconPane1);

		layoutIconPane1.setHorizontalGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPane1.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Globals.hGapSize)
						// .addComponent(buttonWindowStack, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						// .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
						.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						.addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						.addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						// .addComponent(iconButtonSaveGroup, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						// .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
						.addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						.addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						// .addGap(2, 2, 2)
						// .addComponent(iconButtonCancelChanges, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						// .addGap(2, 2, 2)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						.addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						.addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						.addComponent(iconButtonSessionInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, 2 * Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(proceeding, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)));
		layoutIconPane1.setVerticalGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPane1.createSequentialGroup()
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
						.addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER)
								// .addComponent(buttonWindowStack, GroupLayout.PREFERRED_SIZE,
								// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								// .addComponent(iconButtonSaveGroup, GroupLayout.PREFERRED_SIZE,
								// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								// .addComponent(iconButtonCancelChanges, GroupLayout.PREFERRED_SIZE,
								// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonSessionInfo, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(proceeding, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)));

		/*
		 * iconBarPane = new JPanel();
		 * 
		 * FlowLayout flowLayoutIconBarPane = new FlowLayout();
		 * flowLayoutIconBarPane.setAlignment(FlowLayout.LEFT);
		 * iconBarPane = new JPanel(flowLayoutIconBarPane);
		 * 
		 * iconBarPane.add(iconPane1);
		 * iconBarPane.add(iconPane0);
		 * 
		 */

		iconBarPane = new JPanel();
		iconBarPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		iconBarPane.add(iconPane1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy = 0;
		iconBarPane.add(iconPane0, c);

		JSplitPane centralPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, panelTreeClientSelection,
				jTabbedPaneConfigPanes);
		centralPane.setDividerLocation(dividerLocationCentralPane);

		// statusPane

		statusPane = new HostsStatusPanel();

		allPane.add(iconBarPane, BorderLayout.NORTH);
		allPane.add(centralPane, BorderLayout.CENTER);
		allPane.add(statusPane, BorderLayout.SOUTH);

		// tab panes

		jTabbedPaneConfigPanes.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// report state change request to
				int visualIndex = jTabbedPaneConfigPanes.getSelectedIndex();

				// report state change request to controller
				// if ( localboot_productPropertiesPanel != null)
				// localboot_productPropertiesPanel.stopEditing();

				// if (netboot_productPropertiesPanel != null)
				// netboot_productPropertiesPanel.stopEditing();
				logging.info(this, "stateChanged of tabbedPane, visualIndex " + visualIndex);
				main.setViewIndex(visualIndex);

				// retrieve the state index finally produced by main
				int newStateIndex = main.getViewIndex();

				// if the controller did not accept the new index set it back
				// observe that we get a recursion since we initiate another state change
				// the recursion breaks since main.setViewIndex does not yield a different value
				if (visualIndex != newStateIndex) {
					jTabbedPaneConfigPanes.setSelectedIndex(newStateIndex);
				}
			}
		});

		// --- panel_Clientselection

		panelClientlist.addMouseListener(new utils.PopupMouseListener(popupClients));

		panel_Clientselection = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelClientlist, clientPane);

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.panel_Clientselection"),
				Globals.createImageIcon("images/clientselection.png", ""), panel_Clientselection,
				configed.getResourceValue("MainFrame.panel_Clientselection"), ConfigedMain.viewClients);

		panel_LocalbootProductsettings = new PanelGroupedProductSettings(
				configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), main,
				main.getDisplayFieldsLocalbootProducts());

		panel_NetbootProductsettings = new PanelGroupedProductSettings(
				configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), main,
				main.getDisplayFieldsNetbootProducts());

		/*
		 * new PanelProductSettings(configed.getResourceValue(
		 * "MainFrame.panel_NetbootProductsettings"), main,
		 * main.getDisplayFieldsNetbootProducts())
		 * {
		 * 
		 * @Override
		 * protected void init()
		 * {
		 * super.init();
		 * //subOpsiClientdEvent.setVisible(false);
		 * showPopupOpsiclientdEvent(false);
		 * }
		 * 
		 * }
		 * ;
		 */

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),
				Globals.createImageIcon("images/package.png", ""), panel_LocalbootProductsettings,
				configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),
				ConfigedMain.viewLocalbootProducts);

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.panel_NetbootProductsettings"),
				Globals.createImageIcon("images/bootimage.png", ""), panel_NetbootProductsettings,
				configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), ConfigedMain.viewNetbootProducts);

		panel_HostConfig = new PanelHostConfig() {
			@Override
			protected void reloadHostConfig() {
				logging.info(this, "reloadHostConfig");
				super.reloadHostConfig();
				main.cancelChanges();

				main.getPersistenceController().configOptionsRequestRefresh();
				// main.requestReloadConfigsForSelectedClients();
				main.getPersistenceController().hostConfigsRequestRefresh();
				main.resetView(ConfigedMain.viewNetworkconfiguration);
			}

			// overwrite in subclasses
			@Override
			protected void saveHostConfig() {
				super.saveHostConfig();
				main.checkSaveAll(false);
			}

		};

		panel_HostConfig.registerDataChangedObserver(main.getHostConfigsDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_NetworkConfig"),
				Globals.createImageIcon("images/config_pro.png", ""), panel_HostConfig,
				configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), ConfigedMain.viewNetworkconfiguration);

		showHardwareLog = new JPanel();

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_hardwareLog"),
				Globals.createImageIcon("images/hwaudit.png", ""), showHardwareLog,
				configed.getResourceValue("MainFrame.jPanel_hardwareLog"), ConfigedMain.viewHardwareInfo);

		panelSWInfo = new PanelSWInfo(main) {
			@Override
			protected void reload() {
				super.reload();
				main.clearSwInfo();
				main.getPersistenceController().installedSoftwareInformationRequestRefresh();
				main.getPersistenceController().softwareAuditOnClientsRequestRefresh();
				main.resetView(ConfigedMain.viewSoftwareInfo);
			}
		};

		labelNoSoftware = new JLabel();
		labelNoSoftware.setFont(Globals.defaultFontBig);

		showSoftwareLog_NotFound = new JPanel(new FlowLayout());
		showSoftwareLog_NotFound.add(labelNoSoftware);
		showSoftwareLog_NotFound.setBackground(Globals.backgroundLightGrey);

		showSoftwareLog = showSoftwareLog_NotFound;

		showSoftwareLog_MultiClientReport = new PanelSWMultiClientReport();
		SwExporter swExporter = new SwExporter(showSoftwareLog_MultiClientReport, panelSWInfo);
		showSoftwareLog_MultiClientReport.setActionListenerForStart(swExporter);

		/*
		 * jTabbedPaneConfigPanes.addTab(
		 * configed.getResourceValue("MainFrame.jPanel_softwareLog"),
		 * Globals.createImageIcon("images/swaudit.png", "" ),
		 * showSoftwareLog
		 * );
		 */

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_softwareLog"),
				Globals.createImageIcon("images/swaudit.png", ""), showSoftwareLog,
				configed.getResourceValue("MainFrame.jPanel_softwareLog"), ConfigedMain.viewSoftwareInfo);

		showLogfiles = new PanelTabbedDocuments(Globals.logtypes,
				// null)
				configed.getResourceValue("MainFrame.DefaultTextForLogfiles")) {
			@Override
			public void loadDocument(String logtype) {
				super.loadDocument(logtype);
				logging.info(this, "loadDocument logtype " + logtype);
				setUpdatedLogfilePanel(logtype);
			}
		};

		/*
		 * jTabbedPaneConfigPanes.addTab(
		 * configed.getResourceValue("MainFrame.jPanel_logfiles"),
		 * Globals.createImageIcon("images/logfile.png", "" ),
		 * showLogfiles
		 * );
		 */

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_logfiles"),
				Globals.createImageIcon("images/logfile.png", ""), showLogfiles,
				configed.getResourceValue("MainFrame.jPanel_logfiles"), ConfigedMain.viewLog);

		showLogfiles.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// logging.debug(this, " stateChanged " + e);
				logging.debug(this, " new logfiles tabindex " + showLogfiles.getSelectedIndex());

				String logtype = Globals.logtypes[showLogfiles.getSelectedIndex()];

				// logfile empty?
				if (!main.logfileExists(logtype))
					setUpdatedLogfilePanel(logtype);

			}
		});

		panel_ProductProperties = new PanelProductProperties(main);
		panel_ProductProperties.propertiesPanel.registerDataChangedObserver(main.getGeneralDataChangedKeeper());

		/*
		 * jTabbedPaneConfigPanes.addTab
		 * (
		 * configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
		 * Globals.createImageIcon("images/config_pro.png", "" ),
		 * panel_ProductProperties
		 * );
		 */

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				Globals.createImageIcon("images/config_pro.png", ""), panel_ProductProperties,
				configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				ConfigedMain.viewProductProperties);

		logging.info(this,
				"added tab  " + configed.getResourceValue("MainFrame.panel_ProductGlobalProperties") + " index "
						+ jTabbedPaneConfigPanes
								.indexOfTab(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		panel_HostProperties = new PanelHostProperties();
		panel_HostProperties.registerDataChangedObserver(main.getGeneralDataChangedKeeper());

		/*
		 * jTabbedPaneConfigPanes.addTab
		 * (
		 * configed.getResourceValue("MainFrame.jPanel_HostProperties"),
		 * Globals.createImageIcon("images/config_pro.png", "" ),
		 * panel_HostProperties
		 * );
		 */

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_HostProperties"),
				Globals.createImageIcon("images/config_pro.png", ""), panel_HostProperties,
				configed.getResourceValue("MainFrame.jPanel_HostProperties"), ConfigedMain.viewHostProperties);

		logging.info(this, "added tab  " + configed.getResourceValue("MainFrame.jPanel_HostProperties") + " index "
				+ jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_HostProperties")));

		jTabbedPaneConfigPanes.setSelectedIndex(0);

		setTitle(main.getAppTitle());

		Containership csjPanel_allContent = new Containership(allPane);

		csjPanel_allContent.doForAllContainedCompisOfClass("setDragEnabled", new Object[] { true },
				new Class[] { boolean.class }, javax.swing.text.JTextComponent.class);

		// set colors of panels
		csjPanel_allContent.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.backLightBlue },
				JPanel.class);

		// groupActionPanel.setBackground(Globals.backgroundWhite);
		depotListPresenter.setBackground(depotListPresenter.getMyColor());

		Containership cspanel_LocalbootProductsettings = new Containership(panel_LocalbootProductsettings);
		cspanel_LocalbootProductsettings.doForAllContainedCompisOfClass("setBackground",
				new Object[] { Globals.backgroundLightGrey }, VerticalPositioner.class); // JPanel.class);
		panel_LocalbootProductsettings.setBackground(Globals.backgroundLightGrey);

		Containership cspanel_NetbootProductsettings = new Containership(panel_NetbootProductsettings);
		cspanel_NetbootProductsettings.doForAllContainedCompisOfClass("setBackground",
				new Object[] { Globals.backgroundLightGrey }, VerticalPositioner.class); // JPanel.class);
		panel_NetbootProductsettings.setBackground(Globals.backgroundLightGrey);

		// iconPane0.setBackground(Globals.backgroundLightGrey);
		iconPane0.setBackground(Globals.backLightBlue);
		iconBarPane.setBackground(Globals.backLightBlue);
		iconPane1.setBackground(Globals.backLightBlue);
		panelTreeClientSelection.setBackground(Globals.backLightBlue);
		statusPane.setBackground(Globals.backLightBlue);
		// clientPane.setBackground(Globals.backLightBlue);

		setSize(fwidth, fheight);
		glass.setSize(fwidth, fheight);
		glass.setVisible(true);
		glass.setOpaque(true);
		setGlassPane(glass);

		pack();

	}

	public void showPopupClients() {

		Rectangle rect = panelClientlist.getCellRect(panelClientlist.getSelectedRow(), 0, false);
		popupClients.show(panelClientlist, rect.x + (rect.width / 2), rect.y + (rect.height / 2));
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
		return (jTabbedPaneConfigPanes.indexOfTab(tabname));
	}

	// -- helper methods for interaction
	public void saveConfigurationsSetEnabled(boolean b) {
		// logging.debug (" ------- we should now show in the menu that data have
		// changed");

		if (Globals.isGlobalReadOnly() && b)
			return;

		logging.debug(this, "saveConfigurationsSetEnabled " + b);

		jMenuFileSaveConfigurations.setEnabled(b);
		iconButtonSaveConfiguration.setEnabled(b);
		iconButtonCancelChanges.setEnabled(b);
	}

	public void saveGroupSetEnabled(boolean b) {
		// jMenuClientselectionSaveGroup.setEnabled(b);
		iconButtonSaveGroup.setEnabled(b);
	}

	// ----------------------------------------------------------------------------------------
	// action methods for visual interactions
	public void wakeOnLanActionWithDelay(int secs) {
		// main.wakeSelectedClients();
		main.wakeSelectedClientsWithDelay(secs);
	}

	public void wakeOnLanAction() {
		main.wakeSelectedClients();
	}

	public void deletePackageCachesAction() {
		main.deletePackageCachesOfSelectedClients();
	}

	public void fireOpsiclientdEventAction(String event) {
		main.fireOpsiclientdEventOnSelectedClients(event);
	}

	public void showPopupOnClientsAction() {
		FEditTextWithExtra fText = new FEditTextWithExtra("", configed.getResourceValue("MainFrame.writePopupMessage"),
				configed.getResourceValue("MainFrame.writePopupDuration")) {
			@Override
			protected void commit() {
				super.commit();
				Float duration = 0.0f;
				if (!getExtra().isEmpty()) {
					duration = Float.parseFloat(getExtra());
				}
				main.showPopupOnSelectedClients(getText(), duration);
			}
		};

		fText.setTitle(configed.getResourceValue("MainFrame.popupFrameTitle"));
		fText.setAreaDimension(new Dimension(350, 130));
		fText.init();
		fText.setVisible(true);
		fText.centerOn(this);
	}

	public void shutdownClientsAction() {
		main.shutdownSelectedClients();
	}

	public void rebootClientsAction() {
		main.rebootSelectedClients();
	}

	public void deleteClientAction() {
		main.deleteSelectedClients();
	}

	public void freeLicencesAction() {
		logging.info(this, "freeLicencesAction ");
		main.freeAllPossibleLicencesForSelectedClients();
	}

	public void remoteControlAction() {
		logging.debug(this, "jMenuRemoteControl");
		main.startRemoteControlForSelectedClients();
	}

	/**
	 * Calls method from configedMain to start the execution of given command
	 * 
	 * @param SSHCommand command
	 */
	public void remoteSSHExecAction(SSHCommand command) {
		logging.debug(this, "jMenuRemoteSSHExecAction");
		main.startSSHOpsiServerExec(command);
	}

	/**
	 * Calls method from configedMain to start the terminal
	 */
	public void remoteSSHTerminalAction() {
		logging.debug(this, "jMenuRemoteSSHTerminalAction");
		main.startSSHOpsiServerTerminal();
	}

	/**
	 * Calls method from configedMain to start the config dialog
	 */
	public void startSSHConfigAction() {
		logging.debug(this, "jMenuSSHConfigAction");
		main.startSSHConfigDialog();
	}

	/**
	 * Calls method from configedMain to start the command control dialog
	 */
	public void startSSHControlAction() {
		logging.debug(this, "jMenuSSHControlAction");
		main.startSSHControlDialog();
	}

	public void toggleClientFilterAction() {
		main.toggleFilterClientList();
		jMenuClientselectionToggleClientFilter.setState(main.getFilterClientList());
		popupSelectionToggleClientFilter.setState(main.getFilterClientList());

		if (!main.getFilterClientList())
			iconButtonToggleClientFilter.setIcon(Globals.createImageIcon("images/view-filter_disabled-32.png", ""));
		else
			iconButtonToggleClientFilter.setIcon(Globals.createImageIcon("images/view-filter-32.png", ""));
		// setActivated( !main.getFilterClientList() );

	}

	public void invertClientselection() {
		main.invertClientselection();
	}

	public void exitAction() {
		main.finishApp(true, 0);
	}

	public void saveAction() {
		main.checkSaveAll(false);
	}

	public void cancelAction() {
		main.cancelChanges();
	}

	public void getSessionInfo() {
		// iconButtonSessionInfo.setEnabled(false);
		main.getSessionInfo();

		/*
		 * try
		 * {
		 * SwingUtilities.invokeLater(new Runnable()
		 * {ß
		 * public void run()
		 * {
		 * main.getSessionInfo(onlySelectedClients);
		 * //iconButtonSessionInfo.setEnabled(true);
		 * }
		 * }
		 * )
		 * ;
		 * }
		 * catch(Exception ex)
		 * {
		 * logging.debug(this, "Exception " + ex);
		 * }
		 */
	}

	protected void getReachableInfo()// final boolean onlySelectedClients )
	{
		iconButtonReachableInfo.setEnabled(false);
		try {
			SwingUtilities.invokeLater(main::getReachableInfo);
		} catch (Exception ex) {
			logging.debug(this, "Exception " + ex);
		}
	}

	public void callSelectionDialog() {
		main.callClientSelectionDialog();
	}

	private java.util.List<String> getProduct(Vector<String> completeList) {
		FEditList fList = new FEditList();
		fList.setListModel(new DefaultComboBoxModel<>(completeList));
		fList.setTitle(Globals.APPNAME + ": " + configed.getResourceValue("MainFrame.productSelection"));
		fList.init();

		fList.setLocation((int) this.getX() + 40, (int) this.getY() + 40);
		fList.setSize(fwidth / 2, this.getHeight());

		fList.setModal(true);
		fList.setVisible(true);

		logging.debug(this, "fList getSelectedValue " + fList.getSelectedList());

		return (java.util.List<String>) fList.getSelectedList();
	}

	private void groupByNotCurrentProductVersion() {
		java.util.List<String> products = getProduct(new Vector<String>(new TreeSet<String>(main.getProductNames())));

		if (!products.isEmpty())
			main.selectClientsNotCurrentProductInstalled(products, false);
		// java.util.Arrays.asList("javavm"));
	}

	private void groupByNotCurrentProductVersionOrBrokenInstallation() {
		java.util.List<String> products = getProduct(new Vector<String>(new TreeSet<String>(main.getProductNames())));

		if (!products.isEmpty())
			main.selectClientsNotCurrentProductInstalled(products, true);
		// java.util.Arrays.asList("javavm"));
	}

	private void groupByFailedProduct() {
		java.util.List<String> products = getProduct(new Vector<String>(new TreeSet<String>(main.getProductNames())));

		if (!products.isEmpty())
			main.selectClientsWithFailedProduct(products);
		// java.util.Arrays.asList("javavm"));
	}

	public void saveGroupAction() {
		main.callSaveGroupDialog();
	}

	public void deleteGroupAction() {
		main.callDeleteGroupDialog();
	}

	/*
	 * public void noGroupingAction()
	 * {
	 * main.loadClientGroup("", "", "", "", "", "", new HashMap());
	 * }
	 */

	public void deselectSetEnabled(boolean b) {
		jMenuClientselectionDeselect.setEnabled(b);
	}

	public void menuClientSelectionSetEnabled(boolean b) {
		jMenuClientselectionGetGroup.setEnabled(b);
		jMenuClientselectionGetSavedSearch.setEnabled(b);
		jMenuClientselectionProductNotUptodate.setEnabled(b);
		jMenuClientselectionProductNotUptodateOrBroken.setEnabled(b);
		iconButtonSetGroup.setEnabled(b);
	}

	public void reloadAction() {
		main.reload();
	}

	public void reloadLicensesAction() {
		main.reloadLicensesData();
		main.licencesFrame.setVisible(true);

	}

	public void checkMenuItemsDisabling() {
		// for (String itemName : menuItemsHost.keySet())

		if (menuItemsHost == null) {
			logging.info(this, "checkMenuItemsDisabling: menuItemsHost not yet enabled");
			return;
		}

		java.util.List<String> disabledClientMenuEntries = main.getPersistenceController()
				.getDisabledClientMenuEntries();

		if (disabledClientMenuEntries != null) {

			for (String menuActionType : disabledClientMenuEntries) {
				for (JMenuItem menuItem : menuItemsHost.get(menuActionType)) {
					logging.debug(this, "disable " + menuActionType + ", " + menuItem);
					menuItem.setEnabled(false);
				}
			}

			iconButtonNewClient.setEnabled(!disabledClientMenuEntries.contains(ITEM_ADD_CLIENT));

			if (!main.getPersistenceController().isCreateClientPermission()) {
				jMenuAddClient.setEnabled(false);
				popupAddClient.setEnabled(false);
				iconButtonNewClient.setVisible(false);
			}

		}
	}

	private void initializeMenuItemsForClientsDependentOnSelectionCount() {
		for (int i = 0; i < clientMenuItemsDependOnSelectionCount.length; i++) {
			clientMenuItemsDependOnSelectionCount[i].setEnabled(false);
		}
		for (int i = 0; i < clientPopupsDependOnSelectionCount.length; i++) {
			clientPopupsDependOnSelectionCount[i].setEnabled(false);
			// logging.debug(this, " i" + i + " : " +
			// clientPopupsDependOnSelectionCount[i].getText());
		}

		// checkMenuItemsDisabling(); produces NPEs since method seems to be called
		// sometimes
		// before the menu is built completely

	}

	public void enableMenuItemsForClients(int countSelectedClients) {
		logging.debug(this, " enableMenuItemsForClients, countSelectedClients " + countSelectedClients);

		initializeMenuItemsForClientsDependentOnSelectionCount();

		if (countSelectedClients < 0) {
			checkMenuItemsDisabling();
			return;
		}

		if (countSelectedClients == 0) {
			jMenuAddClient.setEnabled(true);
			popupAddClient.setEnabled(true);
		} else {
			if (countSelectedClients >= 1) {
				for (int i = 0; i < clientMenuItemsDependOnSelectionCount.length; i++) {
					clientMenuItemsDependOnSelectionCount[i].setEnabled(true);
				}
				for (int i = 0; i < clientPopupsDependOnSelectionCount.length; i++) {
					clientPopupsDependOnSelectionCount[i].setEnabled(true);
				}
			}

			if (countSelectedClients == 1) {
				jMenuChangeClientID.setEnabled(true);
				popupChangeClientID.setEnabled(true);
			}

		}

		checkMenuItemsDisabling();

	}

	// ------------------- set visual toggle items

	public void showWANactiveColumn(Boolean b) {
		/*
		 * ItemListener[] theListeners = stopItemListeners(
		 * jCheckBoxMenuItem_showWANactiveColumn );
		 * jCheckBoxMenuItem_showWANactiveColumn.setState( b );
		 * startItemListeners( jCheckBoxMenuItem_showWANactiveColumn, theListeners );
		 * 
		 * theListeners = stopItemListeners(popupShowWANactiveColumn);
		 * popupShowWANactiveColumn.setState( b );
		 * startItemListeners( popupShowWANactiveColumn, theListeners );
		 */
	}

	public void showIPAddressColumn(Boolean b) {
		/*
		 * ItemListener[] theListeners = stopItemListeners(
		 * jCheckBoxMenuItem_showIPAddressColumn );
		 * jCheckBoxMenuItem_showIPAddressColumn.setState( b );
		 * startItemListeners( jCheckBoxMenuItem_showIPAddressColumn, theListeners );
		 * 
		 * theListeners = stopItemListeners(popupShowIPAddressColumn);
		 * popupShowIPAddressColumn.setState( b );
		 * startItemListeners( popupShowIPAddressColumn, theListeners );
		 */
	}

	// -------------------

	public void resetProductOnClientAction(boolean withProductProperties) {
		main.resetProductsForSelectedClients(withProductProperties);
	}

	public void addClientAction() {
		main.callNewClientDialog();
	}

	public void changeClientIDAction() {
		main.callChangeClientIDDialog();
	}

	public void changeDepotAction() {
		main.callChangeDepotDialog();
	}

	public void showBackendConfigurationAction() {

		FEditorPane backendInfoDialog = new FEditorPane(this,
				Globals.APPNAME + ":  " + configed.getResourceValue("MainFrame.InfoInternalConfiguration"), false,
				new String[] { configed.getResourceValue("MainFrame.InfoInternalConfiguration.close") }, 800, 600);
		backendInfoDialog.insertHTMLTable(main.getBackendInfos(), "");
		// backendInfoDialog.setSize (new Dimension (400, 400));

		backendInfoDialog.setVisible(true);
	}

	private void showAboutAction() {
		FTextArea info = new FTextArea(this, de.uib.configed.Globals.APPNAME + " Copyright Information", true,
				new String[] { "ok" }, 700, 300);

		StringBuffer message = new StringBuffer();

		for (String line : CopyrightInfos.get()) {
			message.append("\n");
			message.append(line);
		}

		info.setMessage(message.toString());

		info.setVisible(true);
	}

	private void showLogfileLocationAction() {
		// logging.info(this, "showLogfileLocationAction on " + this);
		FTextArea info = new FTextArea((MainFrame) this,
				de.uib.configed.Globals.APPNAME + " " + configed.getResourceValue("MainFrame.showLogFileInfoTitle"),
				false,
				new String[] { configed.getResourceValue("MainFrame.showLogFileCopyToClipboard"),
						configed.getResourceValue("MainFrame.showLogFileOpen"),
						configed.getResourceValue("MainFrame.showLogFileClose") },
				new Icon[] { null, de.uib.configed.Globals.createImageIcon("images/document-view16.png", ""),
						de.uib.configed.Globals.createImageIcon("images/cancel16_small.png", "") },
				500, 150) {
			@Override
			public void doAction1() {
				getTextComponent().copy();
				// super.doAction1();
			}

			@Override
			public void doAction2() {
				try {
					Desktop.getDesktop().open(new java.io.File(logging.getCurrentLogfilePath()));
				} catch (Exception e) {
					logging.error("cannot open: " + logging.getCurrentLogfilePath() + " :\n " + e);
				}
				super.doAction2();

			}

		};

		StringBuffer message = new StringBuffer();

		message.append(configed.getResourceValue("MainFrame.showLogFileInfoText"));
		message.append("\n\n");
		message.append(logging.getCurrentLogfilePath());

		info.setMessage(message.toString());

		info.getTextComponent().setSelectionEnd(message.toString().length());
		info.getTextComponent()
				.setSelectionStart(message.toString().length() - logging.getCurrentLogfilePath().length());

		info.setVisible(true);

	}

	private void showOpsiModules() {
		/*
		 * de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo
		 * f =
		 * new de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo(
		 * null, //owner frame
		 * "Licensing Information", //title getInstance
		 * false, //modal
		 * 
		 * new String[]{
		 * "ok",
		 * "cancel"
		 * },
		 * 
		 * new Icon[]{
		 * Globals.createImageIcon( "images/checked_withoutbox_blue14.png", "" ),
		 * Globals.createImageIcon( "images/cancel16_small.png", "" )
		 * },
		 * 1, //lastButtonNo,with "1" we get only the first button
		 * 1050, 600,
		 * true, //lazylayout, i.e, we have a chance to define components and use them
		 * for the layout
		 * null //addPanel predefined
		 * );
		 * 
		 */

		if (main.getPersistenceController()
				.getOpsiLicensingInfoVersion() == LicensingInfoMap.OPSI_LICENSING_INFO_VERSION_OLD) {

			FTextArea f = new FTextArea(this, configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"),
					true, 1);
			StringBuffer message = new StringBuffer();
			Map<String, Object> modulesInfo = main.getPersistenceController().getOpsiModulesInfos();

			int count = 0;
			for (String key : modulesInfo.keySet()) {
				count++;
				message.append("\n " + key + ": " + modulesInfo.get(key));
			}
			f.setSize(new Dimension(300, 50 + count * 25));

			f.setMessage(message.toString());
			f.setVisible(true);
		}

		else {
			callOpsiLicensingInfo();

		}

	}

	private void showInfoPage() {
		FEditorPane fEditPane = new FEditorPane(this, "opsi server infoPage", false, new String[] { "ok" }, 500, 400);
		fEditPane.setPage("https://" + main.getConfigserver() + ":4447/info");

		// fEditPane.setPage("https://google.de");
		fEditPane.setVisible(true);
	}

	public void callOpsiLicensingInfo() {

		if (fDialogOpsiLicensingInfo == null) {

			fDialogOpsiLicensingInfo = new de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo(null, // owner frame
					// title
					configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), false, // modal

					new String[] { configed.getResourceValue("Dash.close"),
					// ,"cancel"
					},

					new Icon[] {
							// Globals.createImageIcon( "images/checked_withoutbox_blue14.png", "" ),
							Globals.createImageIcon("images/cancel16_small.png", "") },
					1, // lastButtonNo,with "1" we get only the first button
					900, 680, true, // lazylayout, i.e, we have a chance to define components and use them for the
					// layout
					null // addPanel predefined
			);
		} else
			fDialogOpsiLicensingInfo.setVisible(true);
	}

	// ----------------------------------------------------------------------------------------

	void jComboBoxProductValues_actionPerformed(ActionEvent e) {
		if (!settingSchalter) {
			String currentkey, newvalue;
			/*
			 * if (jListProducts.getSelectedValue() != null)
			 * {
			 * currentkey = jListProducts.getSelectedValue().toString();
			 * currentkey =
			 * currentkey.copyValueOf(currentkey.toCharArray(),0,currentkey.indexOf("="));
			 * newvalue = jComboBoxProductValues.getSelectedItem().toString();
			 * logging.debugOut(this, logging.LEVEL_NONE,
			 * "jComboBoxProductValues_actionPerformed: set "+currentkey+"="+newvalue);
			 * //dm.setPcProfileValueWithRequired
			 * (currentkey,newvalue,jRadioRequiredAll.isSelected());
			 * //PersistenceController.getPersistenceController().
			 * setPcProductSwitchWithRequired (currentkey, newvalue,
			 * jRadioRequiredAll.isSelected());
			 * checkErrorList();
			 * dm.setDirty(true);
			 * refreshProductlist();
			 * }
			 */
		}
	}

	void jButtonSaveList_actionPerformed(ActionEvent e) {
		main.checkSaveAll(false);
	}

	public void setSavingFramePosition(boolean b) {
		savingFramePosition = b;
	}

	// ComponentListener
	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
		saveLocation(e);
	}

	public void componentResized(ComponentEvent e) {
		saveLocation(e);
	}

	public void componentShown(ComponentEvent e) {
	}

	private void saveLocation(ComponentEvent e) {
		logging.debug(this, "componentEvent " + e + " saving active " + savingFramePosition);

		if (savingFramePosition) {
			configed.savedStates.saveMainLocationX.serialize(e.getComponent().getBounds().x, 0);
			configed.savedStates.saveMainLocationY.serialize(e.getComponent().getBounds().y, 0);
			configed.savedStates.saveMainLocationWidth.serialize(e.getComponent().getBounds().width, fwidth);
			configed.savedStates.saveMainLocationHeight.serialize(e.getComponent().getBounds().height, fheight);
		}

	}

	/* WindowListener implementation */
	public void windowClosing(WindowEvent e) {
		main.finishApp(true, 0);
	}

	public void windowOpened(WindowEvent e) {
		;
	}

	public void windowClosed(WindowEvent e) {
		;
	}

	public void windowActivated(WindowEvent e) {
		;
	}

	public void windowDeactivated(WindowEvent e) {
		;
	}

	public void windowIconified(WindowEvent e) {
		;
	}

	public void windowDeiconified(WindowEvent e) {
		;
	}

	private Map<String, String> getChangedClientInfoFor(String client) {
		if (changedClientInfos == null)
			changedClientInfos = new HashMap<String, Map<String, String>>();

		Map<String, String> changedClientInfo = changedClientInfos.get(client);

		if (changedClientInfo == null) {
			changedClientInfo = new HashMap<String, String>();
			changedClientInfos.put(client, changedClientInfo);
		}

		return changedClientInfo;

	}

	// ChangeListener implementation
	/*
	 * public void stateChanged(ChangeEvent e)
	 * {
	 * if (e.getSource() == cbUefiBoot)
	 * {
	 * for (String client : main.getSelectedClients())
	 * {
	 * Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
	 * 
	 * 
	 * changedClientInfo.put(HostInfo.clientUefiBootKEY,
	 * ((Boolean)cbUefiBoot.isSelected()).toString()
	 * );
	 * logging.debug(this, "changedClientInfo client , " + client + ", " +
	 * changedClientInfo);
	 * }
	 * 
	 * main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
	 * }
	 * else if (e.getSource() == cbWANConfig)
	 * {
	 * //JOptionPane.showMessageDialog(this, "MainFrame.stateChanged(cbWANConfig)");
	 * 
	 * for (String client : main.getSelectedClients())
	 * {
	 * Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
	 * 
	 * 
	 * changedClientInfo.put(HostInfo.clientWanConfigKEY,
	 * ((Boolean)cbWANConfig.isSelected()).toString()
	 * );
	 * logging.debug(this, "changedClientInfo client , " + client + ", " +
	 * changedClientInfo);
	 * }
	 * 
	 * main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
	 * 
	 * }
	 * 
	 * 
	 * // else if (e.getSource() == cbInstallByShutdown)
	 * // {
	 * // JOptionPane.showMessageDialog(this,
	 * "MainFrame.stateChanged(cbInstallByShutdown)");
	 * // changedClientInfo.put(HostInfo.clientShutdownInstallKEY,
	 * // ((Boolean)cbInstallByShutdown.isSelected()).toString()
	 * // );
	 * 
	 * // main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
	 * 
	 * // logging.info(this, "changedClientInfo : " + changedClientInfo);
	 * // }
	 * }
	 */

	protected void arrangeWs(Set<JDialog> frames) {
		// problem: https://bugs.openjdk.java.net/browse/JDK-7074504
		// Can iconify, but not deiconify a modal JDialog

		if (frames == null)
			return;

		int transpose = 20;

		for (java.awt.Window f : frames) {
			transpose = transpose + Globals.lineHeight;

			if (f != null) {
				f.setVisible(true);
				try {
					f.setLocation(getLocation().x + transpose, getLocation().y + transpose);
				} catch (Exception ex) {
					logging.info(this, "arrangeWs, could not get location");
				}
			}
		}
	}

	// RunningInstancesObserver
	public void instancesChanged(Set<JDialog> instances) {
		// logging.info(this, "instancesChanged, we have instances " + instances);
		boolean existJDialogInstances = (instances != null && instances.size() > 0);

		if (jMenuShowScheduledWOL != null) {
			jMenuShowScheduledWOL.setEnabled(existJDialogInstances);
		}
		if (jMenuFrameShowDialogs != null) {
			jMenuFrameShowDialogs.setEnabled(existJDialogInstances);
		}

		/*
		 * if (buttonWindowStack != null)
		 * {
		 * buttonWindowStack.setEnabled(existJDialogInstances);
		 * }
		 */
	}

	public void executeCommandOnInstances(String command, Set<JDialog> instances) {
		logging.info(this, "executeCommandOnInstances " + command + " for count instances " + instances.size());
		if (command.equals("arrange"))
			arrangeWs(instances);
	}

	private void reactToHostDataChange(InputEvent e) {
		for (String client : main.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

			if (e.getSource() == jTextFieldDescription) {
				logging.info(this,
						"key released on textfielddescription ischangedtext " + jTextFieldDescription.isChangedText());
				if (jTextFieldDescription.isChangedText()) {
					changedClientInfo.put(HostInfo.clientDescriptionKEY, jTextFieldDescription.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.clientDescriptionKEY);
				}
				// main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}

			else if (e.getSource() == jTextFieldInventoryNumber) {
				// logging.debug(this, " keyPressed on fieldinventorynumber , text, old text " +
				// jTextFieldInventoryNumber.getText() + ", " + oldInventoryNumber);
				if (jTextFieldInventoryNumber.isChangedText()) {
					changedClientInfo.put(HostInfo.clientInventoryNumberKEY, jTextFieldInventoryNumber.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else
					changedClientInfo.remove(HostInfo.clientInventoryNumberKEY);

				// main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}

			else if (e.getSource() == jTextFieldOneTimePassword) {
				if (jTextFieldOneTimePassword.isChangedText()) {
					changedClientInfo.put(HostInfo.clientOneTimePasswordKEY, jTextFieldOneTimePassword.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.clientOneTimePasswordKEY);
				}

				// logging.info(this, "key released fieldonetimepassword "+changedClientInfos +
				// " .. " + changedClientInfo);

				// main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);

			}

			else if (e.getSource() == jTextAreaNotes) {
				if (!jTextAreaNotes.getText().equals(oldNotes)) {
					changedClientInfo.put(HostInfo.clientNotesKEY, jTextAreaNotes.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.clientNotesKEY);
				}
				// main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}

			else if (e.getSource() == macAddressField) {
				// logging.debug (" -- key event from macAddressField , oldMacAddress " +
				// oldMacAddress
				// + ", address " + macAddressField.getText() );
				logging.debug(this, " keyPressed on macAddressField, text " + macAddressField.getText());

				if (macAddressField.isChangedText()) {
					changedClientInfo.put(HostInfo.clientMacAddressKEY, macAddressField.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.clientMacAddressKEY);
					// main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
				}
				// main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}

			else if (e.getSource() == ipAddressField) {
				// logging.debug (" -- key event from macAddressField , oldMacAddress " +
				// oldMacAddress
				// + ", address " + macAddressField.getText() );
				logging.debug(this, " keyPressed on ipAddressField, text " + ipAddressField.getText());

				if (ipAddressField.isChangedText()) {
					changedClientInfo.put(HostInfo.clientIpAddressKEY, ipAddressField.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.clientIpAddressKEY);
					// main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
				}
				// main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}

		}
	}

	// MouseListener implementation
	public void mouseClicked(MouseEvent e) {
		logging.debug(this, "mouse clicked " + Arrays.toString(main.getSelectedClients()));

		reactToHostDataChange(e);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	// KeyListener implementation
	public void keyPressed(KeyEvent e) {
		/*
		 * if (e.getSource() == macAddressField)
		 * {
		 * 
		 * 
		 * logging.debug(this, "MainFrame keyPressed on macAddressField " +
		 * e.getKeyChar());
		 * 
		 * if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		 * {
		 * 
		 * 
		 * //if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
		 * // e.consume();
		 * 
		 * //dont delete with backspace since we ruin our mask
		 * }
		 */

	}

	public void keyReleased(KeyEvent e) {
		logging.debug(this, "key released " + Arrays.toString(main.getSelectedClients()));

		reactToHostDataChange(e);
	}

	public void keyTyped(KeyEvent e) {
	}

	// ActionListener implementation
	public void actionPerformed(ActionEvent e) {

		logging.debug(this, "actionPerformed on " + e.getSource());
		if (e.getSource() == cbInstallByShutdown) {
			logging.info(this, "actionPerformed on cbInstallByShutdown");

			for (String client : main.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.clientShutdownInstallKEY,
						((Boolean) cbInstallByShutdown.isSelected()).toString());

				main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}

			/*
			 * if ( handleInstallByShutdownChange( (Boolean)cbInstallByShutdown.isSelected()
			 * ) )
			 * {
			 * 
			 * }
			 */
		}

		else if (e.getSource() == cbUefiBoot) {
			logging.info(this, "actionPerformed on cbUefiBoot");

			for (String client : main.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.clientUefiBootKEY, ((Boolean) cbUefiBoot.isSelected()).toString());

				main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}
		}

		else if (e.getSource() == cbWANConfig) {
			logging.info(this, "actionPerformed on cbWANConfig");

			for (String client : main.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.clientWanConfigKEY, ((Boolean) cbWANConfig.isSelected()).toString());
				main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}
		} else if (e.getSource() == jButtonClientsConfiguration) {
			main.setEditingTarget(ConfigedMain.EditingTarget.CLIENTS);
		}

		else if (e.getSource() == jButtonDepotsConfiguration) {
			main.setEditingTarget(ConfigedMain.EditingTarget.DEPOTS);
		}

		else if (e.getSource() == jButtonServerConfiguration) {
			main.setEditingTarget(ConfigedMain.EditingTarget.SERVER);
		}

		else if (e.getSource() == jButtonLicences || e.getSource() == jMenuFrameLicences) {
			main.handleLicencesManagementRequest();
			if (main.getPersistenceController().getGlobalBooleanConfigValue(
					PersistenceController.KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT,
					PersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT)

			) {

				// main.initDashInfo(); // main.showDashInfo();
				// logging.info(this, " show licences dash ");
				if (licenseDash == null) {
					licenseDash = new LicenseDash();
					licenseDash.initAndShowGUI();
				} else {
					licenseDash.show();
				}
			}

			// main.toggleLicencesFrame();
			// System.exit ( 0 );
		}

		else if (e.getSource() == jButtonWorkOnGroups || e.getSource() == jMenuFrameWorkOnGroups) {
			main.handleGroupActionRequest();

		}

		else if (e.getSource() == jButtonWorkOnProducts || e.getSource() == jMenuFrameWorkOnProducts) {
			main.handleProductActionRequest();

			// main.toggleLicencesFrame();
		} else if (e.getSource() == jButtonDash) {
			main.initDashInfo();
		}

		/*
		 * else if (e.getSource() == buttonSelectDepotsAll)
		 * {
		 * logging.info(this, "action on buttonSelectDepotsAll");
		 * //depotslist.setSelectionInterval(0, depotslist.getModel().getSize() - 1);
		 * depotslist.selectAll();
		 * }
		 * 
		 * else if (e.getSource() == buttonSelectDepotsWithEqualProperties)
		 * {
		 * logging.info(this, "action on buttonSelectDepotsWithEqualProperties");
		 * 
		 * if (depotslist.getSelectedIndex() > -1)
		 * {
		 * String depotSelected = (String) depotslist.getSelectedValue();
		 * java.util.List<String> depotsWithEqualStock
		 * = main.getPersistenceController().getAllDepotsWithIdenticalProductStock(
		 * depotSelected);
		 * depotslist.addToSelection(depotsWithEqualStock);
		 * 
		 * }
		 * }
		 */

	}

	public class LicenseDash {
		private final JFrame frame = new JFrame();
		private LicenseDisplayer licenseDisplayer;

		public void initAndShowGUI() {
			final JFXPanel fxPanel = new JFXPanel();
			frame.add(fxPanel);
			frame.setIconImage(Globals.mainIcon);
			frame.setTitle(configed.getResourceValue("Dashboard.licenseTitle"));
			frame.setVisible(true);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			Platform.runLater(() -> {
				try {
					initFX(fxPanel);
				} catch (IOException ioE) {
					ioE.printStackTrace();
					logging.error(this, "Unable to open fxml file");
				}
			});
		}

		public void initFX(final JFXPanel fxPanel) throws IOException {
			try {
				licenseDisplayer = new LicenseDisplayer();
				licenseDisplayer.initAndShowGUI();
			} catch (IOException ioE) {
				logging.debug(this, "Unable to open FXML file.");
			}
		}

		public void show() {
			licenseDisplayer.display();
		}
	}

	public void enableAfterLoading() {
		jButtonLicences.setEnabled(true);
		jMenuFrameLicences.setEnabled(true);
	}

	public void visualizeLicencesFramesActive(boolean b) {
		jButtonLicences.setSelected(b);
		iconButtonReloadLicenses.setVisible(true);
		iconButtonReloadLicenses.setEnabled(true);

		// jButtonLicences.setOpaque(false);
	}

	public void visualizeEditingTarget(ConfigedMain.EditingTarget t) {
		switch (t) {
		case CLIENTS:
			jButtonClientsConfiguration.setSelected(true);
			jButtonDepotsConfiguration.setSelected(false);
			jButtonServerConfiguration.setSelected(false);
			// logging.debug ( " 2 jButtonLicences == null " + (jButtonLicences ==
			// null));
			// jLabelLicences.setForeground (Globals.greyed);
			break;

		case DEPOTS:
			jButtonDepotsConfiguration.setSelected(true);
			jButtonServerConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);
			// jButtonLicences.setSelected(false);
			// jLabelLicences.setForeground (Globals.greyed);
			break;

		case SERVER:
			jButtonServerConfiguration.setSelected(true);
			jButtonDepotsConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);
			// jButtonLicences.setSelected(false);
			// jLabelLicences.setForeground (Globals.greyed);
			break;

		/*
		 * case LICENCES:
		<<<<<<< ours
		 * System.out.println(" tabbed pane visible false");
		=======
		 * logging.debug(" tabbed pane visible false");
		>>>>>>> theirs
		 * jButtonServerConfiguration.setSelected(false);
		 * jButtonClientsConfiguration.setSelected(false);
		 * jButtonLicences.setSelected(true);
		 * jLabelServerConfiguration.setForeground (Globals.greyed);
		 * jLabelClientsConfiguration.setForeground (Globals.greyed);
		 * jLabelLicences.setForeground (Globals.blue);
		 * break;
		 */

		default:
			break;
		}
	}

	public void producePanelReinstmgr(String pcname, Vector images) {
		panelReinstmgr.startFor(pcname, images);
	}

	public void initHardwareInfo(java.util.List config) {
		if (showHardwareLog_version2 == null) {
			showHardwareLog_version2 = new de.uib.configed.gui.hwinfopage.PanelHWInfo(main) {
				@Override
				protected void reload() {
					super.reload();
					main.clearHwInfo();
					// WaitCursor waitCursor = new WaitCursor(tree);
					// otherwise we get a wait cursor only in table component
					main.resetView(ConfigedMain.viewHardwareInfo);
				}
			};
		}
		showHardwareLog_version2.setHardwareConfig(config);
	}

	private void showHardwareInfo() {

		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_hardwareLog")),
				showHardwareLog);

		showHardwareLog.repaint();

		/*
		 * SwingUtilities.invokeLater(
		 * new Runnable()
		 * {
		 * public void run()
		 * {
		 * Globals.mainContainer.repaint();
		 * }
		 * }
		 * );
		 */
	}

	public void setHardwareInfoNotPossible(String label1S, String label2S) {
		logging.info(this, "setHardwareInfoNotPossible " + label1S);

		if (showHardwareLog_NotFound == null || showHardwareLogParentOfNotFoundPanel == null) {
			showHardwareLog_NotFound = new TitledPanel();
			showHardwareLogParentOfNotFoundPanel = new JPanel();
			showHardwareLog_NotFound.setBackground(Globals.backLightBlue);
			showHardwareLogParentOfNotFoundPanel.setLayout(new BorderLayout());
			showHardwareLogParentOfNotFoundPanel.add(showHardwareLog_NotFound);

			// showHardwareLog_NotFound.setBorder( new LineBorder( Color.WHITE, 3, true ) );
		}

		showHardwareLog_NotFound.setTitle(label1S, label2S);
		showHardwareLog = showHardwareLogParentOfNotFoundPanel;
		showHardwareInfo();
	}

	public void setHardwareInfoMultiClients(String[] clients) {
		if (showHardwareLog_MultiClientReport == null || controllerHWinfoMultiClients == null) {
			controllerHWinfoMultiClients = new ControllerHWinfoMultiClients(main, main.getPersistenceController());
			showHardwareLog_MultiClientReport = controllerHWinfoMultiClients.panel;
		}

		logging.info(this, "setHardwareInfoMultiClients " + clients.length);
		// main.getPersistenceController().getHwInfos( clients );
		controllerHWinfoMultiClients.setFilter();
		showHardwareLog = showHardwareLog_MultiClientReport;

		showHardwareInfo();

	}

	public void setHardwareInfo(Object hardwareInfo) {
		// logging.debug(this, "setHardwareInfo " + hardwareInfo);
		// labelNoHardware.setText(configed.getResourceValue("MainFrame.NoHardwareConfiguration"));

		if (hardwareInfo == null)
			showHardwareLog_version2.setHardwareInfo(null,
					configed.getResourceValue("MainFrame.NoHardwareConfiguration"));
		else
			showHardwareLog_version2.setHardwareInfo((Map) hardwareInfo, null);

		// showHardwareLog.setTitle(pc + " " +
		// configed.getResourceValue("PanelHWInfo.title"));

		/*
		 * if (hardwareInfo instanceof Map)
		 * {
		 * //logging.debug
		 * (" ------------- we should get a version2 hardware info");
		 * showHardwareLog_version2.setHardwareInfo( (Map) hardwareInfo);
		 * showHardwareLog = showHardwareLog_version2;
		 * showHardwareLog.setTitle(pc + "   " +
		 * configed.getResourceValue("PanelHWInfo.title"));
		 * }
		 * 
		 * else
		 * showHardwareLog = showHardwareLog_NotFound;
		 */

		// logging.debug("setComponentAt >>" +
		// configed.getResourceValue("MainFrame.jPanel_hardwareLog") + "<<");
		showHardwareLog = showHardwareLog_version2;
		showHardwareInfo();

	}

	private void showSoftwareAudit() {
		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_softwareLog")),
				showSoftwareLog);

		SwingUtilities.invokeLater(() -> Globals.mainContainer.repaint());
	}

	protected boolean handleInstallByShutdownChange(final boolean wantActive) {

		boolean goOn = true;

		String clientID = getClientID();
		if ((clientID == null) || (clientID.length() == 0))
			return goOn;

		/*
		 * ArrayList<String> shutdown_value = ( (ArrayList)
		 * main.getPersistenceController()
		 * .getCommonProductPropertyValues(
		 * new ArrayList(Arrays.asList(clientID)) ,
		 * "opsi-client-agent",
		 * "on_shutdown_install"
		 * )
		 * );
		 * 
		 * does not produce the default value
		 */

		// for testing commented out

		ArrayList<String> shutdown_valueX = null;
		try {
			shutdown_valueX = (ArrayList) main.getPersistenceController()
					.getProductproperties(clientID, "opsi-client-agent").get("on_shutdown_install");
		} catch (Exception ex) {
		}

		final ArrayList<String> shutdown_value = shutdown_valueX;

		// for testing defined with fixed values
		// final Boolean activate = false;
		final Boolean activate = wantActive;

		/*
		 * final ArrayList<String> shutdown_value = new ArrayList<String>();
		 * shutdown_value.add("off");
		 * 
		 * logging.info(this, "handleInstallByShutdownChange, old shutdown_value " +
		 * shutdown_value
		 * + ", wanted install by shutdown active " + activate);
		 */

		if (shutdown_value == null) {
			logging.info(this, "product property on_shutdown_install does not exist");
			return goOn;
		} else {

			FTextArea fObsolete = new FTextArea((JFrame) Globals.frame1,
					configed.getResourceValue("NewClientDialog.installByShutdown"), true,
					new String[] { "ok", "cancel" }, 300, 200) {
				@Override
				protected boolean wantToBeRegisteredWithRunningInstances() {
					return false;
				}

				@Override
				public void doAction1() {
					logging.info(this, "set property and call setup for the opsi-clientagent");
					handle(activate);
					result = 1;
					leave();
				}

				@Override
				public void doAction2() {
					logging.info(this, "cancel");
					result = 2;
					leave();
				}

				@Override
				public void doAction3() {
					logging.info(this, "cancel");
					result = 3;
					leave();
				}

				private void handle(boolean switchOn) {
					logging.info(this, "handle " + switchOn + ", old value for one client " + shutdown_value);

					main.setInstallByShutdownProductPropertyValue(switchOn);
					main.requestReloadStatesAndActions();

					/*
					 * 
					 * if (switchOn)
					 * {
					 * if (
					 * shutdown_value.size() == 0 ||
					 * ((shutdown_value.get(0) != null) && !(shutdown_value.get(0).equals("on")))
					 * )
					 * {
					 * 
					 * logging.info(this, "handle, shutdown_value.get(0) " + shutdown_value.get(0));
					 * 
					 * main.setInstallByShutdownProductPropertyValue(clientID, true );
					 * main.requestReloadStatesAndActions();
					 * 
					 * 
					 * }
					 * 
					 * else
					 * {
					 * JOptionPane.showMessageDialog(
					 * Globals.mainFrame,
					 * configed.getResourceValue(
					 * "MainFrame.JOptionPane_installByShutdown_already_set"),
					 * configed.getResourceValue(
					 * "MainFrame.JOptionPane_installByShutdown_already_set.title"),
					 * JOptionPane.INFORMATION_MESSAGE);
					 * }
					 * }
					 * else
					 * {
					 * if (
					 * shutdown_value.size() == 0 ||
					 * ((shutdown_value.get(0) != null) && !(shutdown_value.get(0).equals("off")))
					 * )
					 * {
					 * 
					 * logging.info(this, "handle, shutdown_value.get(0) " + shutdown_value.get(0));
					 * 
					 * 
					 * main.setInstallByShutdownProductPropertyValue(clientID, false );
					 * main.requestReloadStatesAndActions();
					 * 
					 * 
					 * }
					 * else
					 * {
					 * JOptionPane.showMessageDialog(
					 * Globals.mainFrame,
					 * configed.getResourceValue(
					 * "MainFrame.JOptionPane_installByShutdown_already_set"),
					 * configed.getResourceValue(
					 * "MainFrame.JOptionPane_installByShutdown_already_set.title"),
					 * JOptionPane.INFORMATION_MESSAGE);
					 * 
					 * }
					 * }
					 */
				}

			};

			fObsolete.setMessage("with opsi.client-agent up to 4.1 :\n\n" + "the property " + "on_shutdown_install"
					+ " \n" + "has to be set,\n\n " + "any change requires a setup of the client agent\n"
					+ "do it now?");

			fObsolete.setVisible(true);

			if (fObsolete.getResult() != 1)
				goOn = false;

		}

		return goOn;

	}

	private class SwExporter implements ActionListener {
		PanelSWMultiClientReport showSoftwareLog_MultiClientReport;
		PanelSWInfo panelSWInfo;

		SwExporter(PanelSWMultiClientReport showSoftwareLog_MultiClientReport, PanelSWInfo panelSWInfo) {
			this.showSoftwareLog_MultiClientReport = showSoftwareLog_MultiClientReport;
			this.panelSWInfo = panelSWInfo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			logging.info(this, "actionPerformed " + "  showSoftwareLog_MultiClientReport.wantsWithMsUpdates  "
					+ showSoftwareLog_MultiClientReport.wantsWithMsUpdates());

			// save states now

			configed.savedStates.saveSWauditExportFilePrefix
					.serialize(showSoftwareLog_MultiClientReport.getExportfilePrefix());

			String filepathStart = showSoftwareLog_MultiClientReport.getExportDirectory() + File.separator
					+ showSoftwareLog_MultiClientReport.getExportfilePrefix();

			String extension = "." + showSoftwareLog_MultiClientReport.wantsKindOfExport().toString().toLowerCase();

			panelSWInfo.setWithMsUpdates(showSoftwareLog_MultiClientReport.wantsWithMsUpdates());
			panelSWInfo.setWithMsUpdates2(showSoftwareLog_MultiClientReport.wantsWithMsUpdates2());

			panelSWInfo.setAskingForKindOfAction(false);
			panelSWInfo.setAskForOverwrite(showSoftwareLog_MultiClientReport.wantsAskForOverwrite());

			panelSWInfo.setKindOfExport(showSoftwareLog_MultiClientReport.wantsKindOfExport());

			java.util.List<String> clientsWithoutScan = new ArrayList<String>();

			for (String client : main.getSelectedClients()) {
				Map<String, Map> tableData = main.getPersistenceController().retrieveSoftwareAuditData(client);
				if (tableData == null || tableData.isEmpty())
					clientsWithoutScan.add(client);

				/*
				 * panelSWInfo.setSoftwareInfo(client,
				 * main.getPersistenceController().getSoftwareAudit( client ));
				 * panelSWInfo.sendToPDF();
				 */
			}

			logging.info(this, "clientsWithoutScan " + clientsWithoutScan);

			for (String client : main.getSelectedClients()) {
				panelSWInfo.setHost(client);
				// main.getPersistenceController().getSoftwareAudit( client )) will be called
				// via table.reload

				panelSWInfo.updateModel();

				String scandate = main.getPersistenceController().getLastSoftwareAuditModification(client);
				if (scandate != null) {
					int timePos = scandate.indexOf(' ');
					if (timePos >= 0)
						scandate = scandate.substring(0, timePos);
					else
						scandate = "__";
				}

				String filepath = filepathStart + client + "__scan_" + scandate + extension;
				logging.debug(this, "actionPerformed, write to " + filepath);
				panelSWInfo.setWriteToFile(filepath);

				panelSWInfo.export();
			}

			logging.info(this, "clientsWithoutScan " + clientsWithoutScan);

		}

	}

	public void setSoftwareAudit() {
		if (main.getSelectedClients() != null && main.getSelectedClients().length > 1) {
			logging.info(this, "setSoftwareAudit for clients " + main.getSelectedClients().length);

			showSoftwareLog = showSoftwareLog_MultiClientReport;
			showSoftwareAudit();

			// logging.info(this, "setSoftwareAudit clientsWithoutScan: " +
			// clientsWithoutScan);

		} else // case main.getSelectedClients() is null or length == 0; the case == 1 is
				// handled by the following methos
		{
			labelNoSoftware.setText(configed.getResourceValue("MainFrame.TabRequiresClientSelected"));
			showSoftwareLog = showSoftwareLog_NotFound;
			showSoftwareAudit();
		}

	}

	public void setSoftwareAuditNullInfo(String hostId) {
		labelNoSoftware.setText(configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));
		panelSWInfo.setSoftwareNullInfo(hostId);
	}

	public void setSoftwareAudit(String hostId) {
		labelNoSoftware.setText(configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));
		/*
		 * if (softwareInfo == null || softwareInfo.isEmpty())
		 * {
		 * //logging.debug(this, "set null SoftwareAudit for " + hostId);
		 * showSoftwareLog = showSoftwareLog_NotFound;
		 * 
		 * }
		 * else
		 */
		{
			logging.debug(this, "setSoftwareAudit for " + hostId);
			panelSWInfo.setAskingForKindOfAction(true);
			panelSWInfo.setAskForOverwrite(true);
			panelSWInfo.setHost(hostId);
			panelSWInfo.updateModel();

			showSoftwareLog = panelSWInfo;
		}
		showSoftwareAudit();
	}

	public void setUpdatedLogfilePanel(String logtype) {
		logging.info(this, "setUpdatedLogfilePanel " + logtype);
		// WaitCursor waitCursor = new WaitCursor(
		// de.uib.configed.Globals.mainContainer, "setUpdatedLogFilePanel" );
		setLogfilePanel(main.getLogfilesUpdating(logtype));
		// waitCursor.stop();
	}

	public void setLogfilePanel(final Map<String, String> logs) {
		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_logfiles")),
				showLogfiles);

		// showLogfiles.setDocuments(logs);

		// SwingUtilities.invokeLater( new Runnable(){
		// public void run(){
		// WaitCursor waitCursor = new WaitCursor( retrieveBasePane(), "setLogFilePanel"
		// );
		showLogfiles.setDocuments(logs, statusPane.getSelectedClientNames());
		// waitCursor.stop();
		// }
		// });

	}

	public void setLogview(String logtype) {
		int i = Arrays.asList(de.uib.configed.Globals.logtypes).indexOf(logtype);
		if (i < 0)
			return;

		showLogfiles.setSelectedIndex(i);
	}

	// client field editing
	public void setClientDescriptionText(String s) {
		jTextFieldDescription.setText(s);
		jTextFieldDescription.setCaretPosition(0);
		// oldDescription = s;
		// changedClientInfo.put("description", s);
	}

	public void setClientInventoryNumberText(String s) {
		jTextFieldInventoryNumber.setText(s);
		jTextFieldInventoryNumber.setCaretPosition(0);
		// oldInventoryNumber = s;
		// changedClientInfo.put("inventoryNumber", s);
	}

	public void setClientOneTimePasswordText(String s) {
		jTextFieldOneTimePassword.setText(s);
		jTextFieldOneTimePassword.setCaretPosition(0);
		// oldOneTimePassword = s;
		// changedClientInfo.put("oneTimePassword", s);
	}

	public void setClientNotesText(String s) {
		jTextAreaNotes.setText(s);
		jTextAreaNotes.setCaretPosition(0);
		oldNotes = s;
		// changedClientInfo.put("notes", s);
	}

	public void setClientMacAddress(String s) {
		macAddressField.setText(s);
		// oldMacAddress = s;
		// changedClientInfo.put("hardwareAddress", s);
	}

	public void setClientIpAddress(String s) {
		ipAddressField.setText(s);
		// oldMacAddress = s;
		// changedClientInfo.put("hardwareAddress", s);
	}

	public void setUefiBoot(Boolean b) {
		logging.info(this, "setUefiBoot " + b);
		cbUefiBoot.setSelected(b);
		// test if (b == null)
		// System.exit(0);
	}

	public void setWANConfig(Boolean b) {
		logging.info(this, "setWANConfig " + b);
		cbWANConfig.setSelected(b);
	}

	public void setOpsiHostKey(String s) {
		logging.info(this, "setOpsiHostKey " + s);
		jTextFieldHostKey.setText(s);
	}

	public void setShutdownInstall(Boolean b) {
		logging.info(this, "setShutdownInstall " + b);
		cbInstallByShutdown.setSelected(b);

		// handleInstallByShutdownChange();
	}

	public void setClientID(String s) {
		labelHostID.setText(s);
	}

	public String getClientID() {
		return labelHostID.getText();
	}

	public void setClientInfoediting(boolean singleClient) {
		// singleClient is primarily conceived as toggle: true for single host, false
		// for multi hosts editing

		// mix with global read only flag
		boolean gb = !Globals.isGlobalReadOnly();

		labelHost.setEnabled(singleClient);

		boolean b1 = false; // resulting toggle for multi hosts editing
		if (singleClient && gb)
			b1 = true;

		jTextFieldDescription.setEnabled(singleClient);
		jTextFieldDescription.setEditable(b1);
		jTextFieldInventoryNumber.setEnabled(singleClient);
		jTextFieldInventoryNumber.setEditable(b1);
		jTextFieldOneTimePassword.setEnabled(singleClient);
		jTextFieldOneTimePassword.setEditable(b1);
		jTextAreaNotes.setEnabled(singleClient);
		jTextAreaNotes.setEditable(b1);
		macAddressField.setEnabled(singleClient);
		macAddressField.setEditable(b1);
		ipAddressField.setEnabled(singleClient);
		ipAddressField.setEditable(b1);

		// multi host editing allowed
		cbUefiBoot.setEnabled(gb && main.getPersistenceController().isWithUEFI());
		cbWANConfig.setEnabled(gb && main.getPersistenceController().isWithWAN());
		cbInstallByShutdown.setEnabled(gb);
		// btnAktivateInstallByShutdown.setEnabled(b1);
		// btnDeaktivateInstallByShutdown.setEnabled(b1);
		jTextFieldHostKey.setMultiValue(!singleClient);
		jTextFieldHostKey.setEnabled(singleClient);

		if (singleClient) {
			jTextFieldDescription.setToolTipText(null);
			jTextFieldInventoryNumber.setToolTipText(null);
			jTextFieldOneTimePassword.setToolTipText(null);
			jTextAreaNotes.setToolTipText(null);
			jTextFieldDescription.setBackground(Globals.backgroundWhite);
			jTextFieldInventoryNumber.setBackground(Globals.backgroundWhite);
			jTextFieldOneTimePassword.setBackground(Globals.backgroundWhite);
			jTextAreaNotes.setBackground(Globals.backgroundWhite);
			macAddressField.setBackground(Globals.backgroundWhite);
			ipAddressField.setBackground(Globals.backgroundWhite);
			// jLabel_InstallByShutdown.setForeground(Globals.lightBlack);
			cbUefiBoot.setBackground(Globals.backgroundWhite);
			cbWANConfig.setBackground(Globals.backgroundWhite);
			jTextFieldHostKey.setBackground(Globals.backgroundWhite);
			cbInstallByShutdown.setBackground(Globals.backgroundWhite);

		} else {
			jTextFieldDescription
					.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldInventoryNumber
					.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldOneTimePassword
					.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextAreaNotes.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldDescription.setBackground(Globals.backgroundLightGrey);
			jTextFieldInventoryNumber.setBackground(Globals.backgroundLightGrey);
			jTextFieldOneTimePassword.setBackground(Globals.backgroundLightGrey);
			jTextAreaNotes.setBackground(Globals.backgroundLightGrey);
			// jLabel_InstallByShutdown.setForeground(Globals.greyed);
			macAddressField.setBackground(Globals.backgroundLightGrey);
			ipAddressField.setBackground(Globals.backgroundLightGrey);
			cbUefiBoot.setBackground(Globals.backgroundLightGrey);
			cbWANConfig.setBackground(Globals.backgroundLightGrey);
			jTextFieldHostKey.setBackground(Globals.backgroundLightGrey);
			cbInstallByShutdown.setBackground(Globals.backgroundLightGrey);

		}

	}

	/*
	 * private void showPopupMenu(JMenuItem[] items, Component c, int x, int y)
	 * {
	 * jPopupMenu = new JPopupMenu();
	 * for (int i=0; i<items.length; i++) {
	 * jPopupMenu.add( items[i] );
	 * }
	 * jPopupMenu.show(c, x, y);
	 * }
	 */

	public void setChangedDepotSelectionActive(boolean active) {
		depotListPresenter.setChangedDepotSelectionActive(active);
	}

	@Override
	public void paint(java.awt.Graphics g) {
		try {
			super.paint(g);
		} catch (java.lang.ClassCastException ex) {
			logging.warning(this, "the ugly well known exception " + ex);
			WaitCursor.stopAll();
		}
	}

}
