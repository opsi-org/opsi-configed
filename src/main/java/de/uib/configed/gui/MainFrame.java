package de.uib.configed.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

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
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableModel;

import de.uib.configed.Configed;
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
import de.uib.configed.dashboard.LicenseDisplayer;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.hwinfopage.ControllerHWinfoMultiClients;
import de.uib.configed.gui.hwinfopage.PanelHWInfo;
import de.uib.configed.gui.productpage.PanelGroupedProductSettings;
import de.uib.configed.gui.productpage.PanelProductProperties;
import de.uib.configed.gui.swinfopage.PanelSWInfo;
import de.uib.configed.gui.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.messagebus.Messagebus;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.HostInfo;
import de.uib.messages.Messages;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserSshConfig;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.RunningInstancesObserver;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;
import de.uib.utilities.swing.ActivityPanel;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.FEditObject;
import de.uib.utilities.swing.FEditStringList;
import de.uib.utilities.swing.FEditTextWithExtra;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.JTextEditorField;
import de.uib.utilities.swing.JTextHideField;
import de.uib.utilities.swing.SeparatedDocument;
import de.uib.utilities.swing.TitledPanel;
import de.uib.utilities.swing.VerticalPositioner;
import de.uib.utilities.table.AbstractExportTable;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.thread.WaitCursor;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class MainFrame extends JFrame
		implements WindowListener, KeyListener, MouseListener, ActionListener, RunningInstancesObserver<JDialog> {

	private static final int DIVIDER_LOCATION_CENTRAL_PANE = 300;
	private static final int MIN_WIDTH_TREE_PANEL = 150;

	public static final int F_WIDTH = 800;
	public static final int F_HEIGHT = 600;

	private static final int F_WIDTH_RIGHTHANDED = 200;

	private static final int DIVIDER_LOCATION_CLIENT_TREE_MULTI_DEPOT = 200;
	private static final int DIVIDER_LOCATION_CLIENT_TREE_SIGLE_DEPOT = 50;

	protected String oldNotes;

	private Map<String, Map<String, String>> changedClientInfos;

	private AbstractExportTable exportTable;

	private ConfigedMain configedMain;

	// menu system

	public static final String ITEM_ADD_CLIENT = "add client";
	public static final String ITEM_DELETE_CLIENT = "remove client";
	public static final String ITEM_FREE_LICENCES = "free licences for client";

	private Map<String, List<JMenuItem>> menuItemsHost;

	private JMenuBar jMenuBar1 = new JMenuBar();

	private JMenu jMenuFile;
	private JMenuItem jMenuFileExit;
	private JMenuItem jMenuFileSaveConfigurations;
	private JMenuItem jMenuFileReload;
	private JMenuItem jMenuFileLanguage;

	private JMenu jMenuClients = new JMenu();

	private JMenu jMenuWakeOnLan;
	private JMenuItem jMenuDirectWOL = new JMenuItem();
	private JMenuItem jMenuNewScheduledWOL = new JMenuItem();

	private JMenuItem jMenuShowScheduledWOL = new JMenuItem();
	private JMenuItem jMenuOpsiClientdEvent;
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

	private JMenuItem jMenuFreeLicences = new JMenuItem();
	private JMenuItem jMenuDeletePackageCaches = new JMenuItem();

	private JMenuItem jMenuResetProductOnClientWithStates = new JMenuItem();
	private JMenuItem jMenuResetProductOnClient = new JMenuItem();
	private JMenuItem jMenuResetLocalbootProductOnClientWithStates = new JMenuItem();
	private JMenuItem jMenuResetLocalbootProductOnClient = new JMenuItem();
	private JMenuItem jMenuResetNetbootProductOnClientWithStates = new JMenuItem();
	private JMenuItem jMenuResetNetbootProductOnClient = new JMenuItem();

	private JMenu jMenuServer = new JMenu();

	private JMenuItem jMenuSSHConfig = new JMenuItem();
	private JMenuItem jMenuSSHConnection = new JMenuItem();
	private JMenuItem jMenuSSHCommandControl = new JMenuItem();

	private LinkedHashMap<String, Integer> labelledDelays;

	private Map<String, String> searchedTimeSpans;
	private Map<String, String> searchedTimeSpansText;

	private JCheckBoxMenuItem jCheckBoxMenuItemShowCreatedColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowWANactiveColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowIPAddressColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowInventoryNumberColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowHardwareAddressColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowSessionInfoColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowUefiBoot = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowInstallByShutdown = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem jCheckBoxMenuItemShowDepotColumn = new JCheckBoxMenuItem();
	private JMenuItem jMenuRemoteControl = new JMenuItem();

	private JMenuItem[] clientMenuItemsDependOnSelectionCount = new JMenuItem[] { jMenuResetProducts, jMenuAddClient,
			jMenuCopyClient, jMenuDeleteClient, jMenuFreeLicences, jMenuChangeDepot, jMenuChangeClientID, };

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
	private JMenuItem jMenuFrameLicences = new JMenuItem();
	private JMenuItem jMenuFrameShowDialogs = new JMenuItem();
	private JMenuItem jMenuFrameTerminal = new JMenuItem();

	private JMenu jMenuHelp = new JMenu();
	private JMenuItem jMenuHelpSupport = new JMenuItem();
	private JMenuItem jMenuHelpDoc = new JMenuItem();
	private JMenuItem jMenuHelpForum = new JMenuItem();
	private JMenuItem jMenuHelpInternalConfiguration = new JMenuItem();
	private JMenuItem jMenuHelpAbout = new JMenuItem();
	private JMenuItem jMenuHelpOpsiVersion = new JMenuItem();
	private JMenuItem jMenuHelpOpsiModuleInformation = new JMenuItem();
	private JMenuItem jMenuHelpServerInfoPage = new JMenuItem();
	private JMenu jMenuHelpLoglevel = new JMenu();
	private JMenuItem jMenuHelpLogfileLocation = new JMenuItem();

	private JRadioButtonMenuItem[] rbLoglevelItems = new JRadioButtonMenuItem[Logging.LEVEL_SECRET + 1];

	private JPopupMenu popupClients = new JPopupMenu();

	private JMenu popupResetProducts = new JMenu(Configed.getResourceValue("MainFrame.jMenuResetProducts"));
	private JMenuItemFormatted popupResetLocalbootProductOnClientWithStates = new JMenuItemFormatted();
	private JMenuItemFormatted popupResetLocalbootProductOnClient = new JMenuItemFormatted();
	private JMenuItemFormatted popupResetNetbootProductOnClientWithStates = new JMenuItemFormatted();
	private JMenuItemFormatted popupResetNetbootProductOnClient = new JMenuItemFormatted();
	private JMenuItemFormatted popupResetProductOnClientWithStates = new JMenuItemFormatted();
	private JMenuItemFormatted popupResetProductOnClient = new JMenuItemFormatted();

	private JMenuItemFormatted popupAddClient = new JMenuItemFormatted();
	private JMenuItemFormatted popupCopyClient = new JMenuItemFormatted();
	private JMenuItemFormatted popupDeleteClient = new JMenuItemFormatted();
	private JMenuItemFormatted popupFreeLicences = new JMenuItemFormatted();
	private JMenuItemFormatted popupDeletePackageCaches = new JMenuItemFormatted();
	private JMenu popupWakeOnLan = new JMenu(Configed.getResourceValue("MainFrame.jMenuWakeOnLan"));
	private JMenuItemFormatted popupWakeOnLanDirect = new JMenuItemFormatted();
	private JMenuItemFormatted popupWakeOnLanScheduler = new JMenuItemFormatted();

	private JMenu menuPopupOpsiClientdEvent = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));
	private JMenuItemFormatted popupShowPopupMessage = new JMenuItemFormatted();
	private JMenuItemFormatted popupRequestSessionInfo = new JMenuItemFormatted();
	private JMenuItemFormatted popupShutdownClient = new JMenuItemFormatted();
	private JMenuItemFormatted popupRebootClient = new JMenuItemFormatted();
	private JMenuItemFormatted popupChangeDepot = new JMenuItemFormatted();
	private JMenuItemFormatted popupChangeClientID = new JMenuItemFormatted();
	private JMenuItemFormatted popupRemoteControl = new JMenuItemFormatted();

	private JMenuItem[] clientPopupsDependOnSelectionCount = new JMenuItem[] { popupResetProducts, popupAddClient,
			popupCopyClient, popupDeleteClient, popupFreeLicences, popupShowPopupMessage, popupRequestSessionInfo,
			popupDeletePackageCaches, popupRebootClient, popupShutdownClient, popupChangeDepot, popupChangeClientID,
			popupRemoteControl };

	private JCheckBoxMenuItem popupShowCreatedColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowWANactiveColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowIPAddressColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowHardwareAddressColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowSessionInfoColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowInventoryNumberColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowUefiBoot = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowInstallByShutdownColumn = new JCheckBoxMenuItem();
	private JCheckBoxMenuItem popupShowDepotColumn = new JCheckBoxMenuItem();

	private JMenuItemFormatted popupSelectionGetGroup = new JMenuItemFormatted();
	private JMenuItemFormatted popupSelectionGetSavedSearch = new JMenuItemFormatted();

	private JCheckBoxMenuItem popupSelectionToggleClientFilter = new JCheckBoxMenuItem();

	private JMenuItemFormatted popupRebuildClientList = new JMenuItemFormatted(
			Configed.getResourceValue("PopupMenuTrait.reload"), Globals.createImageIcon("images/reload16.png", ""));
	private JMenuItemFormatted popupCreatePdf = new JMenuItemFormatted(Configed.getResourceValue("FGeneralDialog.pdf"),
			Globals.createImageIcon("images/acrobat_reader16.png", ""));

	private JPanel iconBarPane;

	private JPanel iconPane0;

	private JPanel iconPaneTargets;
	private JButton jButtonServerConfiguration;
	private JButton jButtonDepotsConfiguration;
	private JButton jButtonClientsConfiguration;
	private JButton jButtonWorkOnGroups;
	private JButton jButtonWorkOnProducts;

	private JPanel iconPaneExtraFrames;

	private JButton jButtonDashboard;
	private JButton jButtonLicences;
	private JButton jButtonOpsiLicenses;

	private JPanel iconPane1;

	private IconButton iconButtonReload;
	private IconButton iconButtonReloadLicenses;
	private IconButton iconButtonNewClient;
	private IconButton iconButtonSaveGroup;/* gibts nicht **/
	private IconButton iconButtonSetGroup;
	private IconButton iconButtonSaveConfiguration;
	private IconButton iconButtonCancelChanges;
	private IconButton iconButtonToggleClientFilter;

	public IconButton iconButtonReachableInfo;
	public IconButton iconButtonSessionInfo;

	public CombinedMenuItem combinedMenuItemCreatedColumn = new CombinedMenuItem(jCheckBoxMenuItemShowCreatedColumn,
			popupShowCreatedColumn);

	public CombinedMenuItem combinedMenuItemWANactiveColumn = new CombinedMenuItem(jCheckBoxMenuItemShowWANactiveColumn,
			popupShowWANactiveColumn);

	private CombinedMenuItem combinedMenuItemIPAddressColumn = new CombinedMenuItem(
			jCheckBoxMenuItemShowIPAddressColumn, popupShowIPAddressColumn);

	private CombinedMenuItem combinedMenuItemHardwareAddressColumn = new CombinedMenuItem(
			jCheckBoxMenuItemShowHardwareAddressColumn, popupShowHardwareAddressColumn);

	public CombinedMenuItem combinedMenuItemSessionInfoColumn = new CombinedMenuItem(
			jCheckBoxMenuItemShowSessionInfoColumn, popupShowSessionInfoColumn);

	public CombinedMenuItem combinedMenuItemInventoryNumberColumn = new CombinedMenuItem(
			jCheckBoxMenuItemShowInventoryNumberColumn, popupShowInventoryNumberColumn);

	public CombinedMenuItem combinedMenuItemUefiBootColumn = new CombinedMenuItem(jCheckBoxMenuItemShowUefiBoot,
			popupShowUefiBoot);

	public CombinedMenuItem combinedMenuItemInstallByShutdownColumn = new CombinedMenuItem(
			jCheckBoxMenuItemShowInstallByShutdown, popupShowInstallByShutdownColumn);

	public CombinedMenuItem combinedMenuItemDepotColumn = new CombinedMenuItem(jCheckBoxMenuItemShowDepotColumn,
			popupShowDepotColumn);

	JPanel proceeding;

	protected JButton buttonSelectDepotsWithEqualProperties;
	protected JButton buttonSelectDepotsAll;

	BorderLayout borderLayout1 = new BorderLayout();
	GroupLayout contentLayout;
	JTabbedPane jTabbedPaneConfigPanes = new JTabbedPane();
	private JSplitPane panelClientSelection;

	private HostsStatusPanel statusPane;

	public PanelGroupedProductSettings panelLocalbootProductSettings;
	public PanelGroupedProductSettings panelNetbootProductSettings;
	public PanelHostConfig panelHostConfig;
	public PanelHostProperties panelHostProperties;
	public PanelProductProperties panelProductProperties;

	PanelHWInfo showHardwareLogVersion2;
	TitledPanel showHardwareLogNotFound;
	public ControllerHWinfoMultiClients controllerHWinfoMultiClients;
	JPanel showHardwareLogMultiClientReport;
	JPanel showHardwareLogParentOfNotFoundPanel;
	JPanel showHardwareLog;
	JLabel labelNoSoftware;

	PanelSWInfo panelSWInfo;
	JPanel showSoftwareLogNotFound;
	PanelSWMultiClientReport showSoftwareLogMultiClientReport;
	JPanel showSoftwareLog;

	PanelTabbedDocuments showLogfiles;

	JPanel jPanelSchalterstellung;

	public de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo fDialogOpsiLicensingInfo;
	LicensingInfoMap licensingInfoMap;

	JTextField jTextFieldConfigdir = new JTextField();
	JButton jButtonFileChooserConfigdir = new JButton();
	JPanel jPanel3 = new JPanel();

	JCheckBox jCheckBoxSorted = new JCheckBox();
	JButton jButtonSaveList = new JButton();
	JPanel jPanelButtonSaveList = new JPanel();
	String[] options = new String[] { "off", "on", "setup" };
	JComboBox<String> jComboBoxProductValues = new JComboBox<>(options);

	JLabel jLabelproperty = new JLabel();
	ButtonGroup buttonGroupRequired = new ButtonGroup();
	JRadioButton jRadioRequiredAll = new JRadioButton();
	JRadioButton jRadioRequiredOff = new JRadioButton();

	JButton jBtnAllOff = new JButton();

	JTableSelectionPanel panelClientlist;
	boolean shiftpressed = false;

	JLabel jLabelHostinfos = new JLabel();

	JLabel jLabelPath = new JLabel();

	JTextArea jFieldInDepot;
	JLabel labelHost;
	JLabel labelHostID;
	CheckedLabel cbInstallByShutdown;
	CheckedLabel cbUefiBoot;
	CheckedLabel cbWANConfig;

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

	JPanel jPanelChooseDomain;

	JPanel panelTreeClientSelection;
	JPanel jPanelProductsConfig;

	boolean multidepot = false;

	DepotListPresenter depotListPresenter;

	ClientTree treeClients;
	JScrollPane scrollpaneTreeClients;

	JPanel clientPane;

	private LicenseDisplayer licenseDisplayer;

	class GlassPane extends JComponent {
		GlassPane() {
			super();
			Logging.debug(this, "glass pane initialized");
			super.setVisible(true);
			super.setOpaque(true);
			super.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					Logging.debug(this, "key typed on glass pane");
				}
			});
			super.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Logging.info(this, "mouse on glass pane");
				}
			});

		}

		@Override
		public void paintComponent(Graphics g) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5));

			g.setColor(Globals.F_GENERAL_DIALOG_FADING_MIRROR_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
		}

	}

	GlassPane glass;

	public MainFrame(ConfigedMain main, JTableSelectionPanel selectionPanel, DepotsList depotsList,
			ClientTree treeClients, boolean multidepot) {

		// we handle it in the window listener method
		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.multidepot = multidepot;

		panelClientlist = selectionPanel;

		exportTable = new ExporterToCSV(selectionPanel.getTable());

		this.treeClients = treeClients;

		depotListPresenter = new DepotListPresenter(depotsList, multidepot, main.getPersistenceController());

		this.configedMain = main;

		glass = new GlassPane();

		guiInit();
		initData();

		UIManager.put("OptionPane.yesButtonText", Configed.getResourceValue("UIManager.yesButtonText"));
		UIManager.put("OptionPane.noButtonText", Configed.getResourceValue("UIManager.noButtonText"));
		UIManager.put("OptionPane.cancelButtonText", Configed.getResourceValue("UIManager.cancelButtonText"));

		FEditObject.runningInstances.addObserver(this);
	}

	@Override
	public void setVisible(boolean b) {
		Logging.info(this, "setVisible from MainFrame " + b);
		super.setVisible(b);
	}

	private void initData() {
		statusPane.updateValues(0, null, null, null);
	}

	public HostsStatusInfo getHostsStatusInfo() {
		return statusPane;
	}

	// This shall be called after MainFrame is made visible
	public void initSplitPanes() {
		panelClientSelection.setDividerLocation(0.8);
		panelLocalbootProductSettings.setDividerLocation(0.75);
		panelNetbootProductSettings.setDividerLocation(0.75);
		panelProductProperties.setDividerLocation(0.75);
	}

	public class SizeListeningPanel extends JPanel implements ComponentListener {
		SizeListeningPanel() {
			super.addComponentListener(this);
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

			try {
				repairSizes();
			} catch (Exception ex) {
				Logging.info(this, "componentResized " + ex);
			}
			Logging.debug(this, "componentResized ready");
		}

		private void moveDivider1(JSplitPane splitpane, JComponent rightpane, int minRightWidth, int minLeftWidth,
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

		public void repairSizes() {
			// repair sizes when the frame is resized

			if (panelClientSelection == null) {
				return;
			}

			moveDivider1(panelClientSelection, clientPane, (int) (F_WIDTH_RIGHTHANDED * 0.2), 200,
					(int) (F_WIDTH_RIGHTHANDED * 1.5));
		}

	}

	// ------------------------------------------------------------------------------------------
	// configure interaction
	// ------------------------------------------------------------------------------------------
	// menus

	private void setupMenuLists() {

		menuItemsHost = new LinkedHashMap<>();
		menuItemsHost.put(ITEM_ADD_CLIENT, new ArrayList<>());
		menuItemsHost.put(ITEM_DELETE_CLIENT, new ArrayList<>());
		menuItemsHost.put(ITEM_FREE_LICENCES, new ArrayList<>());

	}

	private void setupMenuFile() {
		jMenuFile = new JMenu();
		jMenuFileExit = new JMenuItem();
		jMenuFileSaveConfigurations = new JMenuItem();
		jMenuFileReload = new JMenuItem();
		jMenuFileLanguage = new JMenu(); // submenu

		jMenuFile.setText(Configed.getResourceValue("MainFrame.jMenuFile"));

		jMenuFileExit.setText(Configed.getResourceValue("MainFrame.jMenuFileExit"));
		jMenuFileExit.addActionListener((ActionEvent e) -> exitAction());

		jMenuFileSaveConfigurations.setText(Configed.getResourceValue("MainFrame.jMenuFileSaveConfigurations"));
		jMenuFileSaveConfigurations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSaveConfigurations.addActionListener((ActionEvent e) -> saveAction());

		jMenuFileReload.setText(Configed.getResourceValue("MainFrame.jMenuFileReload"));

		jMenuFileReload.addActionListener((ActionEvent e) -> {
			reloadAction();
			if (iconButtonReloadLicenses.isEnabled()) {
				reloadLicensesAction();
			}
		});

		jMenuFileLanguage.setText(Configed.getResourceValue("MainFrame.jMenuFileChooseLanguage"));
		ButtonGroup groupLanguages = new ButtonGroup();

		String selectedLocale = Messages.getSelectedLocale();
		Logging.debug(this, "selectedLocale " + selectedLocale);

		for (final String localeName : Messages.getLocaleInfo().keySet()) {
			ImageIcon localeIcon = null;
			String imageIconName = Messages.getLocaleInfo().get(localeName);
			if (imageIconName != null && imageIconName.length() > 0) {
				try {
					localeIcon = new ImageIcon(Messages.class.getResource(imageIconName));
				} catch (Exception ex) {
					Logging.info(this, "icon not found: " + imageIconName + ", " + ex);
				}
			}

			JMenuItem menuItem = new JRadioButtonMenuItem(localeName, localeIcon);
			Logging.debug(this, "selectedLocale " + selectedLocale);
			menuItem.setSelected(selectedLocale.equals(localeName));
			jMenuFileLanguage.add(menuItem);
			groupLanguages.add(menuItem);

			menuItem.addActionListener((ActionEvent e) -> {
				configedMain.closeInstance(true);
				de.uib.messages.Messages.setLocale(localeName);
				new Thread() {
					@Override
					public void run() {
						Configed.startWithLocale();
					}
				}.start();

				// we put it into to special thread to avoid invokeAndWait runtime error
			});
		}

		jMenuFile.add(jMenuFileSaveConfigurations);
		jMenuFile.add(jMenuFileReload);
		jMenuFile.add(jMenuFileLanguage);

		jMenuFile.add(jMenuFileExit);
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
				enableMenuItemsForClients(configedMain.getSelectedClients().length);
			}
		});

		jCheckBoxMenuItemShowCreatedColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		combinedMenuItemCreatedColumn.show(configedMain.hostDisplayFields.get(HostInfo.CREATED_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowCreatedColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnCreated());

		jCheckBoxMenuItemShowWANactiveColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		combinedMenuItemWANactiveColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowWANactiveColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnWANactive());

		jCheckBoxMenuItemShowIPAddressColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		combinedMenuItemIPAddressColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowIPAddressColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnIPAddress());

		jCheckBoxMenuItemShowHardwareAddressColumn
				.setText(Configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		combinedMenuItemHardwareAddressColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowHardwareAddressColumn
				.addItemListener((ItemEvent e) -> configedMain.toggleColumnHardwareAddress());

		jCheckBoxMenuItemShowSessionInfoColumn
				.setText(Configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		combinedMenuItemSessionInfoColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowSessionInfoColumn.addItemListener((ItemEvent e) -> {
			Logging.info(this, "toggleColumnSessionInfo by CheckBoxMenuItem");
			configedMain.toggleColumnSessionInfo();

		});

		jCheckBoxMenuItemShowInventoryNumberColumn
				.setText(Configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		combinedMenuItemInventoryNumberColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowInventoryNumberColumn
				.addItemListener((ItemEvent e) -> configedMain.toggleColumnInventoryNumber());

		jCheckBoxMenuItemShowUefiBoot.setText(Configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		combinedMenuItemUefiBootColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowUefiBoot.addItemListener((ItemEvent e) -> configedMain.toggleColumnUEFIactive());

		jCheckBoxMenuItemShowInstallByShutdown
				.setText(Configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		combinedMenuItemUefiBootColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowInstallByShutdown
				.addItemListener((ItemEvent e) -> configedMain.toggleColumnInstallByShutdownActive());

		jCheckBoxMenuItemShowDepotColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		combinedMenuItemDepotColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowDepotColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnDepot());

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

		jMenuWakeOnLan = new JMenu(Configed.getResourceValue("MainFrame.jMenuWakeOnLan"));

		jMenuDirectWOL.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.direct"));
		jMenuDirectWOL.addActionListener((ActionEvent e) -> wakeOnLanAction());

		jMenuWakeOnLan.add(jMenuDirectWOL);

		jMenuNewScheduledWOL.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler"));
		final MainFrame f = this;
		jMenuNewScheduledWOL.addActionListener((ActionEvent e) -> {
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(
					Globals.APPNAME + ": " + Configed.getResourceValue("FStartWakeOnLan.title"), configedMain);
			fStartWakeOnLan.setLocationRelativeTo(f);

			fStartWakeOnLan.setVisible(true);
			fStartWakeOnLan.setPredefinedDelays(labelledDelays);

			fStartWakeOnLan.setClients();

		});

		jMenuWakeOnLan.add(jMenuNewScheduledWOL);

		jMenuWakeOnLan.addSeparator();

		jMenuShowScheduledWOL.setEnabled(false);
		jMenuShowScheduledWOL.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.showRunning"));
		jMenuShowScheduledWOL.addActionListener((ActionEvent e) -> {
			Logging.info(this, "actionPerformed");
			executeCommandOnInstances("arrange", FEditObject.runningInstances.getAll());
		});

		jMenuWakeOnLan.add(jMenuShowScheduledWOL);

		jMenuDeletePackageCaches.setText(Configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
		jMenuDeletePackageCaches.addActionListener((ActionEvent e) -> deletePackageCachesAction());

		jMenuOpsiClientdEvent = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : configedMain.getPersistenceController().getOpsiclientdExtraEvents()) {
			JMenuItem item = new JMenuItem(event);
			item.setFont(Globals.defaultFont);

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

		jMenuFreeLicences.setText(Configed.getResourceValue("MainFrame.jMenuFreeLicences"));
		jMenuFreeLicences.addActionListener((ActionEvent e) -> freeLicencesAction());

		menuItemsHost.get(ITEM_FREE_LICENCES).add(jMenuFreeLicences);

		jMenuRemoteControl.setText(Configed.getResourceValue("MainFrame.jMenuRemoteControl"));
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

		jMenuClients.add(jMenuAddClient);
		if (ConfigedMain.OPSI_4_3) {
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

		jMenuClients.add(jMenuFreeLicences);
		jMenuClients.add(jMenuChangeClientID);
		if (multidepot) {
			jMenuClients.add(jMenuChangeDepot);
		}
		jMenuClients.addSeparator();

		// --

		jMenuClients.add(jCheckBoxMenuItemShowWANactiveColumn);
		jMenuClients.add(jCheckBoxMenuItemShowIPAddressColumn);
		jMenuClients.add(jCheckBoxMenuItemShowHardwareAddressColumn);
		jMenuClients.add(jCheckBoxMenuItemShowSessionInfoColumn);
		jMenuClients.add(jCheckBoxMenuItemShowInventoryNumberColumn);
		jMenuClients.add(jCheckBoxMenuItemShowCreatedColumn);
		jMenuClients.add(jCheckBoxMenuItemShowUefiBoot);
		jMenuClients.add(jCheckBoxMenuItemShowInstallByShutdown);
		jMenuClients.add(jCheckBoxMenuItemShowDepotColumn);
	}

	public void updateSSHConnectedInfoMenu(String status) {

		String connectiondata = SSHConnectionInfo.getInstance().getUser() + "@"
				+ SSHConnectionInfo.getInstance().getHost();

		jMenuSSHConnection.setText(connectiondata.trim() + " " + SSHCommandFactory.UNKNOWN);
		jMenuSSHConnection.setForeground(Globals.UNKNOWN_COLOR);
		if (status.equals(SSHCommandFactory.NOT_CONNECTED)) {

			jMenuSSHConnection.setForeground(Globals.lightBlack);
			jMenuSSHConnection.setText(connectiondata.trim() + " " + SSHCommandFactory.NOT_CONNECTED);
		} else if (status.equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED)) {
			jMenuSSHConnection.setForeground(Globals.ACTION_COLOR);
			jMenuSSHConnection.setText(connectiondata.trim() + " " + SSHCommandFactory.CONNECTION_NOT_ALLOWED);

		} else if (status.equals(SSHCommandFactory.CONNECTED)) {
			jMenuSSHConnection.setForeground(Globals.OK_COLOR);
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
		Logging.info(this, "setupMenuServer ");
		final SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);
		SSHConnectionInfo connectionInfo = SSHConnectionInfo.getInstance();

		factory.setMainFrame(this);
		JMenu menuOpsi = new JMenu();
		menuOpsi.setText(SSHCommandFactory.PARENT_OPSI);

		jMenuServer.removeAll();
		jMenuServer.setText(SSHCommandFactory.PARENT_NULL);
		boolean isReadOnly = Globals.isGlobalReadOnly();
		boolean methodsExists = factory.checkSSHCommandMethod();

		Logging.info(this, "setupMenuServer add configpage");
		jMenuSSHConfig = new JMenuItem();
		jMenuSSHConfig.setText(Configed.getResourceValue("MainFrame.jMenuSSHConfig"));
		jMenuSSHConfig.addActionListener((ActionEvent e) -> startSSHConfigAction());

		jMenuSSHConnection.setEnabled(false);
		if (Configed.sshConnectOnStart) {
			factory.testConnection(connectionInfo.getUser(), connectionInfo.getHost());
		}

		if (factory.checkSSHCommandMethod()) {
			Logging.info(this, "setupMenuServer add commandcontrol");
			jMenuSSHCommandControl = new JMenuItem();
			jMenuSSHCommandControl.setText(Configed.getResourceValue("MainFrame.jMenuSSHCommandControl"));
			jMenuSSHCommandControl.addActionListener((ActionEvent e) -> startSSHControlAction());
		}
		// SSHCommandControlDialog

		jMenuServer.add(jMenuSSHConnection);
		jMenuServer.add(jMenuSSHConfig);
		if (factory.checkSSHCommandMethod()) {
			jMenuServer.add(jMenuSSHCommandControl);
		}

		jMenuServer.addSeparator();

		Logging.info(this, "setupMenuServer getCurrentUserConfig " + UserConfig.getCurrentUserConfig());

		boolean commandsAreDeactivated = (UserConfig.getCurrentUserConfig() == null
				|| UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDS_ACTIVE) == null
				|| !UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDS_ACTIVE));
		Logging.info(this, "setupMenuServer commandsAreDeactivated " + commandsAreDeactivated);

		if (methodsExists) {
			factory.retrieveSSHCommandListRequestRefresh();
			factory.retrieveSSHCommandList();
			Map<String, List<SSHCommandTemplate>> sortedComs = factory.getSSHCommandMapSortedByParent();

			Logging.debug(this, "setupMenuServer add commands to menu commands sortedComs " + sortedComs);
			boolean firstParentGroup = true;
			boolean commandsExist = false;
			for (Map.Entry<String, List<SSHCommandTemplate>> entry : sortedComs.entrySet()) {
				String parentMenuName = entry.getKey();
				LinkedList<SSHCommandTemplate> listCom = new LinkedList<>(entry.getValue());
				Collections.sort(listCom);
				JMenu parentMenu = new JMenu();
				parentMenu.setText(parentMenuName);
				Logging.info(this, "ssh parent menu text " + parentMenuName);
				if (parentMenuName.equals(SSHCommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS)) {
					parentMenu.setText("");
					parentMenu.setIcon(Globals.createImageIcon("images/burger_menu_09.png", "..."));
				}

				if (!(parentMenuName.equals(SSHCommandFactory.PARENT_NULL))) {
					firstParentGroup = false;
				}

				for (final SSHCommandTemplate com : listCom) {
					commandsExist = true;
					JMenuItem jMenuItem = new JMenuItem();
					jMenuItem.setText(com.getMenuText());
					Logging.info(this, "ssh command menuitem text " + com.getMenuText());
					jMenuItem.setToolTipText(com.getToolTipText());
					jMenuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (factory.getConnectionState().equals(SSHCommandFactory.NOT_CONNECTED)) {
								Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message")
										+ " " + factory.getConnectionState());
							} else if (factory.getConnectionState().equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED)) {
								Logging.error(this,
										Configed.getResourceValue("SSHConnection.CONNECTION_NOT_ALLOWED.message"));
							} else if (factory.getConnectionState().equals(SSHCommandFactory.UNKNOWN)) {
								Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message")
										+ " " + factory.getConnectionState());
							} else {
								// Create new instance of the same command, so that further
								// modifications would not affect the original command.
								final SSHCommandTemplate c = new SSHCommandTemplate(com);
								remoteSSHExecAction(c);
							}
						}
					});

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
					if (isReadOnly) {
						jMenuItem.setEnabled(false);
					}
					if (commandsAreDeactivated) {
						jMenuItem.setEnabled(false);
					}
				}
				if (firstParentGroup && commandsExist) {
					jMenuServer.addSeparator();
				}

				firstParentGroup = false;
			}
			menuOpsi.addSeparator();
		} else {
			jMenuServer.add(menuOpsi);
		}
		List<SSHCommand> commands = factory.getSSHCommandParameterList();
		Logging.info(this, "setupMenuServer add parameterDialogs to opsi commands" + commands);
		for (final SSHCommand command : commands) {
			JMenuItem jMenuOpsiCommand = new JMenuItem();
			jMenuOpsiCommand.setText(command.getMenuText());
			jMenuOpsiCommand.setToolTipText(command.getToolTipText());
			jMenuOpsiCommand.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (factory.getConnectionState().equals(SSHCommandFactory.NOT_CONNECTED)) {
						Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message") + " "
								+ factory.getConnectionState());
					} else if (factory.getConnectionState().equals(SSHCommandFactory.CONNECTION_NOT_ALLOWED)) {
						Logging.error(this, Configed.getResourceValue("SSHConnection.CONNECTION_NOT_ALLOWED.message"));
					} else if (factory.getConnectionState().equals(SSHCommandFactory.UNKNOWN)) {
						Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message") + " "
								+ factory.getConnectionState());
					} else {
						remoteSSHExecAction(command);
					}
				}
			});

			if (!jMenuServer.isMenuComponent(menuOpsi)) {
				jMenuServer.add(menuOpsi);
			}

			menuOpsi.add(jMenuOpsiCommand);
			if (isReadOnly) {
				jMenuOpsiCommand.setEnabled(false);
			}

			if (commandsAreDeactivated) {
				jMenuOpsiCommand.setEnabled(false);
			}
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
			JMenuItem item = new JMenuItemFormatted(entry.getValue());
			item.setFont(Globals.defaultFont);

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
		jMenuFrameWorkOnGroups.setVisible(configedMain.getPersistenceController().isWithLocalImaging());
		jMenuFrameWorkOnGroups.addActionListener(this);

		jMenuFrameWorkOnProducts.setText(Configed.getResourceValue("MainFrame.jMenuFrameWorkOnProducts"));
		jMenuFrameWorkOnProducts.addActionListener(this);

		jMenuFrameDashboard.setText(Configed.getResourceValue("Dashboard.title"));
		jMenuFrameDashboard.addActionListener(this);
		jMenuFrameDashboard.setVisible(ConfigedMain.OPSI_4_3);

		jMenuFrameLicences.setText(Configed.getResourceValue("MainFrame.jMenuFrameLicences"));
		jMenuFrameLicences.setEnabled(false);
		jMenuFrameLicences.addActionListener(this);

		jMenuFrameShowDialogs.setText(Configed.getResourceValue("MainFrame.jMenuFrameShowDialogs"));
		jMenuFrameShowDialogs.setEnabled(false);
		jMenuFrameShowDialogs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.info(this, "actionPerformed");
				executeCommandOnInstances("arrange", FEditObject.runningInstances.getAll());
			}
		});

		jMenuFrameTerminal.setText("Terminal");
		jMenuFrameTerminal.setEnabled(true);
		jMenuFrameTerminal.addActionListener(e -> {
			try {
				Messagebus messagebus = new Messagebus();
				boolean connected = messagebus.connect();
				if (!connected) {
					return;
				}

				messagebus.connectTerminal();
			} catch (InterruptedException ex) {
				Logging.error(this, "cannot open terminal, thread interrupted", ex);
				Thread.currentThread().interrupt();
			} catch (URISyntaxException ex) {
				Logging.error(this, "cannot open terminal, Exception thrown", ex);
			}
		});

		jMenuFrames.add(jMenuFrameWorkOnProducts);
		jMenuFrames.add(jMenuFrameWorkOnGroups);
		jMenuFrames.add(jMenuFrameWorkOnProducts);
		if (ConfigedMain.OPSI_4_3) {
			jMenuFrames.add(jMenuFrameDashboard);
		}
		jMenuFrames.add(jMenuFrameLicences);
		if (ConfigedMain.OPSI_4_3) {
			jMenuFrames.add(jMenuFrameTerminal);
		}
		jMenuFrames.addSeparator();
		jMenuFrames.add(jMenuFrameShowDialogs);

	}

	private void setupMenuHelp() {
		jMenuHelp.setText(Configed.getResourceValue("MainFrame.jMenuHelp"));

		jMenuHelpDoc.setText(Configed.getResourceValue("MainFrame.jMenuDoc"));
		jMenuHelpDoc.addActionListener(actionEvent -> Globals.showExternalDocument(Globals.OPSI_DOC_PAGE));
		jMenuHelp.add(jMenuHelpDoc);

		jMenuHelpForum.setText(Configed.getResourceValue("MainFrame.jMenuForum"));
		jMenuHelpForum.addActionListener(actionEvent -> Globals.showExternalDocument(Globals.OPSI_FORUM_PAGE));
		jMenuHelp.add(jMenuHelpForum);

		jMenuHelpSupport.setText(Configed.getResourceValue("MainFrame.jMenuSupport"));
		jMenuHelpSupport.addActionListener(actionEvent -> Globals.showExternalDocument(Globals.OPSI_SUPPORT_PAGE));
		jMenuHelp.add(jMenuHelpSupport);

		jMenuHelp.addSeparator();

		jMenuHelpOpsiVersion.setText(
				Configed.getResourceValue("MainFrame.jMenuHelpOpsiService") + ": " + configedMain.getOpsiVersion());
		jMenuHelpOpsiVersion.setEnabled(false);
		jMenuHelpOpsiVersion.setForeground(Globals.lightBlack);

		jMenuHelp.add(jMenuHelpOpsiVersion);

		jMenuHelpOpsiModuleInformation.setText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		if (configedMain.getOpsiVersion().length() == 0 || configedMain.getOpsiVersion().charAt(0) == '<'
				|| configedMain.getOpsiVersion().compareTo("3.4") < 0) {
			jMenuHelpOpsiModuleInformation.setEnabled(false);
		} else {
			jMenuHelpOpsiModuleInformation.addActionListener((ActionEvent e) -> showOpsiModules());
		}

		jMenuHelp.add(jMenuHelpOpsiModuleInformation);

		jMenuHelpInternalConfiguration.setText(Configed.getResourceValue("MainFrame.jMenuHelpInternalConfiguration"));
		jMenuHelpInternalConfiguration.addActionListener((ActionEvent e) -> showBackendConfigurationAction());
		jMenuHelp.add(jMenuHelpInternalConfiguration);

		ActionListener selectLoglevelListener = (ActionEvent e) -> {
			for (int i = Logging.LEVEL_NONE; i <= Logging.LEVEL_SECRET; i++) {
				if (e.getSource() == rbLoglevelItems[i]) {
					rbLoglevelItems[i].setSelected(true);
					Logging.setLogLevel(i);
				} else {
					if (rbLoglevelItems[i] != null) {
						rbLoglevelItems[i].setSelected(false);
					}
				}
			}
		};

		jMenuHelpLoglevel.setText(Configed.getResourceValue("MainFrame.jMenuLoglevel"));

		for (int i = Logging.LEVEL_NONE; i <= Logging.LEVEL_SECRET; i++) {
			rbLoglevelItems[i] = new JRadioButtonMenuItem("[" + i + "] " + Logging.levelText(i).toLowerCase());

			jMenuHelpLoglevel.add(rbLoglevelItems[i]);
			if (i == Logging.getLogLevelConsole()) {
				rbLoglevelItems[i].setSelected(true);
			}

			rbLoglevelItems[i].addActionListener(selectLoglevelListener);
		}
		jMenuHelp.add(jMenuHelpLoglevel);

		jMenuHelpServerInfoPage.setText("opsi server InfoPage");

		jMenuHelpServerInfoPage.addActionListener((ActionEvent e) -> showInfoPage());

		jMenuHelpLogfileLocation.setText(Configed.getResourceValue("MainFrame.jMenuHelpLogfileLocation"));
		jMenuHelpLogfileLocation.addActionListener((ActionEvent e) -> showLogfileLocationAction());

		jMenuHelp.add(jMenuHelpLogfileLocation);

		jMenuHelp.addSeparator();

		jMenuHelpAbout.setText(Configed.getResourceValue("MainFrame.jMenuHelpAbout"));
		jMenuHelpAbout.addActionListener((ActionEvent e) -> showAboutAction());

		jMenuHelp.add(jMenuHelpAbout);
	}

	// ------------------------------------------------------------------------------------------
	// icon pane
	private void setupIcons1() {

		iconButtonReload = new IconButton(Configed.getResourceValue("MainFrame.iconButtonReload"), "images/reload.gif",
				"images/reload_over.gif", " ");

		iconButtonReloadLicenses = new IconButton(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"),
				"images/reload_licenses.png", "images/reload_licenses_over.png", " ", false);
		iconButtonReloadLicenses.setVisible(false);

		iconButtonNewClient = new IconButton(Configed.getResourceValue("MainFrame.iconButtonNewClient"),
				"images/newClient.gif", "images/newClient_over.gif", " ");

		iconButtonSetGroup = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSetGroup"),
				"images/setGroup.gif", "images/setGroup_over.gif", " ");
		iconButtonSaveConfiguration = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
				"images/apply_over.gif", " ", "images/apply_disabled.gif", false);

		iconButtonCancelChanges = new IconButton(Configed.getResourceValue("MainFrame.iconButtonCancelChanges"),
				"images/cancel-32.png", "images/cancel_over-32.png", " ", false);

		iconButtonReachableInfo = new IconButton(Configed.getResourceValue("MainFrame.iconButtonReachableInfo"),
				"images/new_networkconnection.png", "images/new_networkconnection.png",
				"images/new_networkconnection.png", configedMain.hostDisplayFields.get("clientConnected"));

		String[] waitingCircle = new String[] { "images/systemusers_sessioninfo_activitycircle/loading_01.png",
				"images/systemusers_sessioninfo_activitycircle/loading_02.png" };

		iconButtonSessionInfo = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSessionInfo"),
				"images/system-users-query.png", "images/system-users-query_over.png",
				"images/system-users-query_over.png", waitingCircle,

				500, configedMain.hostDisplayFields.get("clientSessionInfo"));
		iconButtonSessionInfo.setEnabled(true);

		iconButtonToggleClientFilter = new IconButton(
				Configed.getResourceValue("MainFrame.iconButtonToggleClientFilter"),
				"images/view-filter_disabled-32.png", "images/view-filter_over-32.png", "images/view-filter-32.png",
				true);

		iconButtonSaveGroup = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSaveGroup"),
				"images/saveGroup.gif", "images/saveGroup_over.gif", " ");

		iconButtonReload.addActionListener((ActionEvent e) -> reloadAction());

		iconButtonReloadLicenses.addActionListener((ActionEvent e) -> reloadLicensesAction());

		iconButtonNewClient.addActionListener((ActionEvent e) -> addClientAction());

		iconButtonSetGroup.addActionListener((ActionEvent e) -> callSelectionDialog());

		iconButtonSaveConfiguration.addActionListener((ActionEvent e) -> saveAction());

		iconButtonCancelChanges.addActionListener((ActionEvent e) -> cancelAction());

		iconButtonReachableInfo.addActionListener((ActionEvent e) -> getReachableInfo());

		iconButtonSessionInfo.addActionListener((ActionEvent e) -> {
			configedMain.setColumnSessionInfo(true);
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

	// ------------------------------------------------------------------------------------------
	// context menus

	private void setupPopupMenuClientsTab() {

		popupShowCreatedColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		combinedMenuItemCreatedColumn.show(configedMain.hostDisplayFields.get(HostInfo.CREATED_DISPLAY_FIELD_LABEL));

		popupShowCreatedColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnCreated());

		popupShowWANactiveColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		combinedMenuItemWANactiveColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

		popupShowWANactiveColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnWANactive());

		popupShowIPAddressColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		combinedMenuItemIPAddressColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));

		popupShowIPAddressColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnIPAddress());

		popupShowHardwareAddressColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		combinedMenuItemHardwareAddressColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));

		popupShowHardwareAddressColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnHardwareAddress());

		popupShowSessionInfoColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		combinedMenuItemSessionInfoColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));

		popupShowSessionInfoColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnSessionInfo());

		popupShowInventoryNumberColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		combinedMenuItemInventoryNumberColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));

		popupShowInventoryNumberColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnInventoryNumber());

		popupShowUefiBoot.setText(Configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		combinedMenuItemUefiBootColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

		popupShowUefiBoot.addItemListener((ItemEvent e) -> configedMain.toggleColumnUEFIactive());

		popupShowInstallByShutdownColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		combinedMenuItemInstallByShutdownColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

		popupShowInstallByShutdownColumn
				.addItemListener((ItemEvent e) -> configedMain.toggleColumnInstallByShutdownActive());

		popupShowDepotColumn.setText(Configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		combinedMenuItemDepotColumn
				.show(configedMain.hostDisplayFields.get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));

		popupShowDepotColumn.addItemListener((ItemEvent e) -> configedMain.toggleColumnDepot());

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

		final MainFrame f = this;
		popupWakeOnLanScheduler.setText(Configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler"));
		popupWakeOnLanScheduler.addActionListener((ActionEvent e) -> {
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(
					Globals.APPNAME + ": " + Configed.getResourceValue("FStartWakeOnLan.title"), configedMain);
			fStartWakeOnLan.setLocationRelativeTo(f);

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

		popupFreeLicences.setText(Configed.getResourceValue("MainFrame.jMenuFreeLicences"));
		popupFreeLicences.addActionListener((ActionEvent e) -> freeLicencesAction());

		menuItemsHost.get(ITEM_FREE_LICENCES).add(popupFreeLicences);

		popupRemoteControl.setText(Configed.getResourceValue("MainFrame.jMenuRemoteControl"));

		popupRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		popupRemoteControl.addActionListener((ActionEvent e) -> remoteControlAction());

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
		popupSelectionToggleClientFilter.setFont(Globals.defaultFontBig);

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
				enableMenuItemsForClients(configedMain.getSelectedClients().length);
			}
		});

		popupClients.add(popupWakeOnLan);

		menuPopupOpsiClientdEvent = new JMenu(Configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));

		for (final String event : configedMain.getPersistenceController().getOpsiclientdExtraEvents()) {
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
		if (ConfigedMain.OPSI_4_3) {
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

		popupClients.add(popupFreeLicences);
		popupClients.add(popupChangeClientID);
		if (multidepot) {
			popupClients.add(popupChangeDepot);
		}
		popupClients.addSeparator();

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

		popupClients.addSeparator();
		popupClients.add(popupSelectionToggleClientFilter);

		popupClients.add(popupRebuildClientList);
		popupClients.add(popupCreatePdf);

		exportTable.addMenuItemsTo(popupClients);

	}

	public void createPdf() {
		TableModel tm = configedMain.getSelectedClientsTableModel();
		JTable jTable = new JTable(tm);

		try {
			HashMap<String, String> metaData = new HashMap<>();
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

		} catch (Exception ex) {
			Logging.error("pdf printing error " + ex);
		}
	}

	// ------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------

	public void updateHostCheckboxenText() {
		if (configedMain.getPersistenceController().isWithUEFI()) {
			cbUefiBoot.setText(Configed.getResourceValue("NewClientDialog.boottype"));
		} else {
			cbUefiBoot.setText(Configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			cbUefiBoot.setEnabled(false);
		}

		if (configedMain.getPersistenceController().isWithWAN()) {
			cbWANConfig.setText(Configed.getResourceValue("NewClientDialog.wanConfig"));
		} else {
			cbWANConfig.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
			cbWANConfig.setEnabled(false);
		}
	}

	private void guiInit() {
		this.addWindowListener(this);
		this.setFont(Globals.defaultFont);
		this.setIconImage(Globals.mainIcon);

		SizeListeningPanel allPane = new SizeListeningPanel();

		allPane.setLayout(borderLayout1);

		getContentPane().add(allPane);

		initMenuData();

		setupMenuLists();

		setupMenuFile();
		setupMenuGrouping();
		setupMenuClients();
		setupMenuServer();
		setupMenuFrames();
		setupMenuHelp();

		jMenuBar1.add(jMenuFile);

		jMenuBar1.add(jMenuClientselection);
		jMenuBar1.add(jMenuClients);
		jMenuBar1.add(jMenuServer);
		jMenuBar1.add(jMenuFrames);
		jMenuBar1.add(jMenuHelp);

		this.setJMenuBar(jMenuBar1);

		setupPopupMenuClientsTab();

		// clientPane
		clientPane = new JPanel();

		clientPane.setPreferredSize(new Dimension(F_WIDTH_RIGHTHANDED, F_HEIGHT + 40));
		clientPane.setBorder(Globals.createPanelBorder());

		GroupLayout layoutClientPane = new GroupLayout(clientPane);
		clientPane.setLayout(layoutClientPane);

		labelHost = new JLabel(Globals.createImageIcon("images/client.png", ""), SwingConstants.LEFT);
		labelHost.setPreferredSize(Globals.buttonDimension);

		labelHostID = new JLabel("");
		labelHostID.setFont(Globals.defaultFontStandardBold);

		JLabel labelClientDescription = new JLabel(Configed.getResourceValue("MainFrame.jLabelDescription"));
		labelClientDescription.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientInventoryNumber = new JLabel(Configed.getResourceValue("MainFrame.jLabelInventoryNumber"));
		labelClientInventoryNumber.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientNotes = new JLabel(Configed.getResourceValue("MainFrame.jLabelNotes"));

		JLabel labelClientMacAddress = new JLabel(Configed.getResourceValue("MainFrame.jLabelMacAddress"));

		JLabel labelClientIPAddress = new JLabel(Configed.getResourceValue("MainFrame.jLabelIPAddress"));

		JLabel labelOneTimePassword = new JLabel(Configed.getResourceValue("MainFrame.jLabelOneTimePassword"));

		JLabel labelOpsiHostKey = new JLabel("opsiHostKey");

		jFieldInDepot = new JTextArea();
		jFieldInDepot.setEditable(false);
		jFieldInDepot.setFont(Globals.defaultFontBig);
		jFieldInDepot.setBackground(Globals.BACKGROUND_COLOR_3);

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

		unselectedIcon = Globals.createImageIcon("images/checked_not.png", "");
		selectedIcon = Globals.createImageIcon("images/checked.png", "");
		nullIcon = Globals.createImageIcon("images/checked_box_mixed.png", "");

		cbUefiBoot = new CheckedLabel(Configed.getResourceValue("NewClientDialog.boottype"), selectedIcon,
				unselectedIcon, nullIcon, false);
		cbUefiBoot.addActionListener(this);

		cbWANConfig = new CheckedLabel(Configed.getResourceValue("WAN Konfiguration"), selectedIcon, unselectedIcon,
				nullIcon, false);
		cbWANConfig.setSelected(false);
		cbWANConfig.setEnabled(true);
		cbWANConfig.addActionListener(this);

		cbInstallByShutdown = new CheckedLabel(Configed.getResourceValue("NewClientDialog.installByShutdown"),
				selectedIcon, unselectedIcon, nullIcon, false);
		cbInstallByShutdown.setSelected(false);
		cbInstallByShutdown.setEnabled(true);
		cbInstallByShutdown.addActionListener(this);

		updateHostCheckboxenText();

		jTextFieldOneTimePassword = new JTextEditorField("");
		jTextFieldOneTimePassword.addKeyListener(this);
		jTextFieldOneTimePassword.addMouseListener(this);

		jTextFieldHostKey = new JTextHideField();

		layoutClientPane.setHorizontalGroup(layoutClientPane.createParallelGroup()
				/////// HOST
				.addGroup(layoutClientPane.createSequentialGroup()
						.addComponent(labelHost, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)
						.addComponent(labelHostID, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				/////// DESCRIPTION
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelClientDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)
						.addComponent(jTextFieldDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				/////// INVENTORY NUMBER
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelClientInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)
						.addComponent(jTextFieldInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				/////// MAC ADDRESS
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelClientMacAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)
						.addComponent(macAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))

				/////// IP ADDRESS
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelClientIPAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)
						.addComponent(ipAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))

				/////// INSTALL BY SHUTDOWN

				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(cbInstallByShutdown, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE))

				/////// UEFI BOOT
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(cbUefiBoot, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE))
				/////// WAN CONFIG
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(cbWANConfig, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE))

				/////// ONE TIME PASSWORD
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)
						.addComponent(jTextFieldOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))

				////// opsiHostKey
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelOpsiHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))

				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)
						.addComponent(jTextFieldHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))

				/////// NOTES
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelClientNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)
						.addComponent(scrollpaneNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE)));

		layoutClientPane.setVerticalGroup(layoutClientPane.createSequentialGroup()
				/////// HOST
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE).addComponent(labelHost)
				.addComponent(labelHostID, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				/////// DESCRIPTION
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(labelClientDescription)
				.addComponent(jTextFieldDescription, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				/////// INVENTORY NUMBER
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(labelClientInventoryNumber)
				.addComponent(jTextFieldInventoryNumber, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				/////// MAC ADDRESS
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(labelClientMacAddress)
				.addComponent(macAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				/////// IP ADDRESS
				.addComponent(labelClientIPAddress)
				.addComponent(ipAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				////// INSTALL BY SHUTDOWN
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(cbInstallByShutdown, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// UEFI BOOT & WAN Config

				.addComponent(cbUefiBoot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				.addComponent(cbWANConfig, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// ONE TIME PASSWORD
				.addGap(Globals.MIN_VGAP_SIZE * 2, Globals.MIN_VGAP_SIZE * 2, Globals.MIN_VGAP_SIZE * 2)
				.addComponent(labelOneTimePassword, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(jTextFieldOneTimePassword, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				////// opsiHostKey
				.addComponent(labelOpsiHostKey, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(jTextFieldHostKey, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// NOTES
				.addGap(Globals.MIN_VGAP_SIZE * 2, Globals.MIN_VGAP_SIZE * 2, Globals.MIN_VGAP_SIZE * 2)
				.addComponent(labelClientNotes)
				.addComponent(scrollpaneNotes, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		jPanel3.setBorder(BorderFactory.createEtchedBorder());
		jPanel3.setLayout(new BorderLayout());

		jCheckBoxSorted.setSelected(true);
		jCheckBoxSorted.setText(Configed.getResourceValue("MainFrame.jCheckBoxSorted"));

		jButtonSaveList.setText(Configed.getResourceValue("MainFrame.jButtonSaveList"));
		jButtonSaveList.setBackground(Globals.BACKGROUND_COLOR_6);
		jButtonSaveList.addActionListener(this::jButtonSaveListActionPerformed);

		jRadioRequiredAll.setMargin(new Insets(0, 0, 0, 0));
		jRadioRequiredAll.setAlignmentY((float) 0.0);
		jRadioRequiredAll.setText(Configed.getResourceValue("MainFrame.jRadioRequiredAll"));
		jRadioRequiredOff.setMargin(new Insets(0, 0, 0, 0));
		jRadioRequiredOff.setSelected(true);
		jRadioRequiredOff.setText(Configed.getResourceValue("MainFrame.jRadioRequiredOff"));
		jRadioRequiredOff.setToolTipText("");

		jLabelPath.setText(Configed.getResourceValue("MainFrame.jLabelPath"));
		jLabelHostinfos.setText(Configed.getResourceValue("MainFrame.jLabel_Hostinfos"));

		buttonGroupRequired.add(jRadioRequiredAll);
		buttonGroupRequired.add(jRadioRequiredOff);

		jComboBoxProductValues.setBackground(Globals.BACKGROUND_COLOR_6);

		treeClients.setFont(Globals.defaultFont);

		scrollpaneTreeClients = new JScrollPane();

		scrollpaneTreeClients.getViewport().add(treeClients);
		scrollpaneTreeClients.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setPreferredSize(treeClients.getMaximumSize());

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimum() " +

				scrollpaneTreeClients.getVerticalScrollBar().getMinimum()

		);

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() " +

				scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize()

		);

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() " +

				scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize()

		);
		JSplitPane splitpaneClientSelection = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
				depotListPresenter.getScrollpaneDepotslist(), scrollpaneTreeClients);

		Logging.info(this, "multidepot " + multidepot);
		if (multidepot) {
			splitpaneClientSelection.setDividerLocation(DIVIDER_LOCATION_CLIENT_TREE_MULTI_DEPOT);
		} else {
			splitpaneClientSelection.setDividerLocation(DIVIDER_LOCATION_CLIENT_TREE_SIGLE_DEPOT);
		}

		panelTreeClientSelection = new JPanel();
		GroupLayout layoutPanelTreeClientSelection = new GroupLayout(panelTreeClientSelection);
		panelTreeClientSelection.setLayout(layoutPanelTreeClientSelection);

		layoutPanelTreeClientSelection.setHorizontalGroup(layoutPanelTreeClientSelection.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addGroup(layoutPanelTreeClientSelection.createParallelGroup(GroupLayout.Alignment.LEADING)

						.addComponent(depotListPresenter, MIN_WIDTH_TREE_PANEL, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addComponent(splitpaneClientSelection, MIN_WIDTH_TREE_PANEL, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))

				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2));

		layoutPanelTreeClientSelection.setVerticalGroup(layoutPanelTreeClientSelection
				.createParallelGroup(GroupLayout.Alignment.LEADING)

				.addGroup(layoutPanelTreeClientSelection.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addComponent(depotListPresenter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(splitpaneClientSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)));

		jButtonServerConfiguration = new JButton("", Globals.createImageIcon("images/opsiconsole_deselected.png", ""));
		jButtonServerConfiguration.setSelectedIcon(Globals.createImageIcon("images/opsiconsole.png", ""));
		jButtonServerConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));

		jButtonDepotsConfiguration = new JButton("", Globals.createImageIcon("images/opsidepots_deselected.png", ""));
		jButtonDepotsConfiguration.setSelectedIcon(Globals.createImageIcon("images/opsidepots.png", ""));
		jButtonDepotsConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelDepotsConfiguration"));

		jButtonClientsConfiguration = new JButton("", Globals.createImageIcon("images/opsiclients_deselected.png", ""));
		jButtonClientsConfiguration.setSelectedIcon(Globals.createImageIcon("images/opsiclients.png", ""));
		jButtonClientsConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonClientsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelClientsConfiguration"));

		jButtonLicences = new JButton("", Globals.createImageIcon("images/licences_deselected.png", ""));
		jButtonLicences.setEnabled(false);
		jButtonLicences.setSelectedIcon(Globals.createImageIcon("images/licences.png", ""));
		jButtonLicences.setPreferredSize(Globals.modeSwitchDimension);
		jButtonLicences.setToolTipText(Configed.getResourceValue("MainFrame.labelLicences"));

		jButtonServerConfiguration.addActionListener(this);
		jButtonDepotsConfiguration.addActionListener(this);
		jButtonClientsConfiguration.addActionListener(this);
		jButtonLicences.addActionListener(this);

		jButtonWorkOnGroups = new JButton("", Globals.createImageIcon("images/group_all_unselected_40.png", ""));
		jButtonWorkOnGroups.setSelectedIcon(Globals.createImageIcon("images/group_all_selected_40.png", ""));
		jButtonWorkOnGroups.setPreferredSize(Globals.modeSwitchDimension);
		jButtonWorkOnGroups.setToolTipText(Configed.getResourceValue("MainFrame.labelWorkOnGroups"));

		jButtonWorkOnGroups.setEnabled(configedMain.getPersistenceController().isWithLocalImaging());
		jButtonWorkOnGroups.addActionListener(this);

		jButtonWorkOnProducts = new JButton("", Globals.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setSelectedIcon(Globals.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setPreferredSize(Globals.modeSwitchDimension);
		jButtonWorkOnProducts.setToolTipText(Configed.getResourceValue("MainFrame.labelWorkOnProducts"));

		jButtonWorkOnProducts.addActionListener(this);

		jButtonDashboard = new JButton("", Globals.createImageIcon("images/dash_unselected.png", ""));
		jButtonDashboard.setSelectedIcon(Globals.createImageIcon("images/dash_selected.png", ""));
		jButtonDashboard.setPreferredSize(Globals.modeSwitchDimension);
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));

		jButtonDashboard.setEnabled(ConfigedMain.OPSI_4_3);
		jButtonDashboard.setVisible(ConfigedMain.OPSI_4_3);
		jButtonDashboard.addActionListener(this);

		if (!configedMain.getPersistenceController().getOpsiLicencingInfoVersion()
				.equals(LicensingInfoMap.OPSI_LICENSING_INFO_VERSION_OLD) && licensingInfoMap == null) {

			licensingInfoMap = LicensingInfoMap.getInstance(
					configedMain.getPersistenceController().getOpsiLicencingInfo(),
					configedMain.getPersistenceController().getConfigDefaultValues(),
					!FGeneralDialogLicensingInfo.extendedView);

			if (licensingInfoMap.warningExists()) {
				if (licensingInfoMap.getWarningLevel().equals(LicensingInfoMap.STATE_OVER_LIMIT)) {
					jButtonOpsiLicenses = new JButton("",
							Globals.createImageIcon("images/opsi-licenses-error-small.png", ""));
				}
				if (licensingInfoMap.getWarningLevel().equals(LicensingInfoMap.STATE_CLOSE_TO_LIMIT)) {
					jButtonOpsiLicenses = new JButton("",
							Globals.createImageIcon("images/opsi-licenses-warning-small.png", ""));
				}

			} else {
				jButtonOpsiLicenses = new JButton("", Globals.createImageIcon("images/opsi-licenses.png", ""));
			}

		} else {

			jButtonOpsiLicenses = new JButton("", Globals.createImageIcon("images/opsi-licenses.png", ""));

		}

		jButtonOpsiLicenses.setPreferredSize(Globals.modeSwitchDimension);
		jButtonOpsiLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelOpsiLicenses"));
		jButtonOpsiLicenses.addActionListener(this);

		iconPaneTargets = new JPanel();
		iconPaneTargets.setBorder(new LineBorder(Globals.blueGrey, 1, true));

		GroupLayout layoutIconPaneTargets = new GroupLayout(iconPaneTargets);
		iconPaneTargets.setLayout(layoutIconPaneTargets);

		layoutIconPaneTargets.setHorizontalGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layoutIconPaneTargets.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)));
		layoutIconPaneTargets.setVerticalGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPaneTargets.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		iconPaneExtraFrames = new JPanel();
		iconPaneExtraFrames.setBorder(new LineBorder(Globals.blueGrey, 1, true));

		GroupLayout layoutIconPaneExtraFrames = new GroupLayout(iconPaneExtraFrames);
		iconPaneExtraFrames.setLayout(layoutIconPaneExtraFrames);

		layoutIconPaneExtraFrames
				.setHorizontalGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGroup(layoutIconPaneExtraFrames.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(jButtonDashboard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(ConfigedMain.OPSI_4_3 ? Globals.HGAP_SIZE : 0,
										ConfigedMain.OPSI_4_3 ? Globals.HGAP_SIZE : 0,
										ConfigedMain.OPSI_4_3 ? Globals.HGAP_SIZE : 0)
								.addComponent(jButtonOpsiLicenses, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(jButtonLicences, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)));
		layoutIconPaneExtraFrames
				.setVerticalGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layoutIconPaneExtraFrames.createSequentialGroup()
								.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
								.addGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonDashboard, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonOpsiLicenses, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonLicences, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		iconPane0 = new JPanel();

		GroupLayout layoutIconPane0 = new GroupLayout(iconPane0);
		iconPane0.setLayout(layoutIconPane0);

		layoutIconPane0.setHorizontalGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPane0.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)));
		layoutIconPane0.setVerticalGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layoutIconPane0.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		setupIcons1();
		iconPane1 = new JPanel();

		GroupLayout layoutIconPane1 = new GroupLayout(iconPane1);
		iconPane1.setLayout(layoutIconPane1);

		layoutIconPane1.setHorizontalGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPane1.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(iconButtonSessionInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, 2 * Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(proceeding, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)));
		layoutIconPane1.setVerticalGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPane1.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonSessionInfo, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(proceeding, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

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
		centralPane.setDividerLocation(DIVIDER_LOCATION_CENTRAL_PANE);

		// statusPane

		statusPane = new HostsStatusPanel();

		allPane.add(iconBarPane, BorderLayout.NORTH);
		allPane.add(centralPane, BorderLayout.CENTER);
		allPane.add(statusPane, BorderLayout.SOUTH);

		// tab panes

		jTabbedPaneConfigPanes.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
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
			}
		});

		// --- panel_Clientselection

		panelClientlist.addMouseListener(new utils.PopupMouseListener(popupClients));

		panelClientSelection = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelClientlist, clientPane);

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.panel_Clientselection"),
				Globals.createImageIcon("images/clientselection.png", ""), panelClientSelection,
				Configed.getResourceValue("MainFrame.panel_Clientselection"), ConfigedMain.VIEW_CLIENTS);

		panelLocalbootProductSettings = new PanelGroupedProductSettings(
				Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), configedMain,
				configedMain.getDisplayFieldsLocalbootProducts());

		panelNetbootProductSettings = new PanelGroupedProductSettings(
				Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), configedMain,
				configedMain.getDisplayFieldsNetbootProducts());

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),
				Globals.createImageIcon("images/package.png", ""), panelLocalbootProductSettings,
				Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),
				ConfigedMain.VIEW_LOCALBOOT_PRODUCTS);

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"),
				Globals.createImageIcon("images/bootimage.png", ""), panelNetbootProductSettings,
				Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"),
				ConfigedMain.VIEW_NETBOOT_PRODUCTS);

		panelHostConfig = new PanelHostConfig() {
			@Override
			protected void reloadHostConfig() {
				Logging.info(this, "reloadHostConfig");
				super.reloadHostConfig();
				configedMain.cancelChanges();

				configedMain.getPersistenceController().configOptionsRequestRefresh();

				configedMain.getPersistenceController().hostConfigsRequestRefresh();
				configedMain.resetView(ConfigedMain.VIEW_NETWORK_CONFIGURATION);
			}

			// overwrite in subclasses
			@Override
			protected void saveHostConfig() {
				super.saveHostConfig();
				configedMain.checkSaveAll(false);
			}

		};

		panelHostConfig.registerDataChangedObserver(configedMain.getHostConfigsDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"),
				Globals.createImageIcon("images/config_pro.png", ""), panelHostConfig,
				Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), ConfigedMain.VIEW_NETWORK_CONFIGURATION);

		showHardwareLog = new JPanel();

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog"),
				Globals.createImageIcon("images/hwaudit.png", ""), showHardwareLog,
				Configed.getResourceValue("MainFrame.jPanel_hardwareLog"), ConfigedMain.VIEW_HARDWARE_INFO);

		panelSWInfo = new PanelSWInfo(configedMain) {
			@Override
			protected void reload() {
				super.reload();
				configedMain.clearSwInfo();
				configedMain.getPersistenceController().installedSoftwareInformationRequestRefresh();
				configedMain.getPersistenceController().softwareAuditOnClientsRequestRefresh();
				configedMain.resetView(ConfigedMain.VIEW_SOFTWARE_INFO);
			}
		};

		labelNoSoftware = new JLabel();
		labelNoSoftware.setFont(Globals.defaultFontBig);

		showSoftwareLogNotFound = new JPanel(new FlowLayout());
		showSoftwareLogNotFound.add(labelNoSoftware);
		showSoftwareLogNotFound.setBackground(Globals.BACKGROUND_COLOR_3);

		showSoftwareLog = showSoftwareLogNotFound;

		showSoftwareLogMultiClientReport = new PanelSWMultiClientReport();
		SwExporter swExporter = new SwExporter(showSoftwareLogMultiClientReport, panelSWInfo);
		showSoftwareLogMultiClientReport.setActionListenerForStart(swExporter);

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog"),
				Globals.createImageIcon("images/swaudit.png", ""), showSoftwareLog,
				Configed.getResourceValue("MainFrame.jPanel_softwareLog"), ConfigedMain.VIEW_SOFTWARE_INFO);

		showLogfiles = new PanelTabbedDocuments(Globals.getLogTypes(),
				Configed.getResourceValue("MainFrame.DefaultTextForLogfiles")) {
			@Override
			public void loadDocument(String logtype) {
				super.loadDocument(logtype);
				Logging.info(this, "loadDocument logtype " + logtype);
				setUpdatedLogfilePanel(logtype);
			}
		};

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_logfiles"),
				Globals.createImageIcon("images/logfile.png", ""), showLogfiles,
				Configed.getResourceValue("MainFrame.jPanel_logfiles"), ConfigedMain.VIEW_LOG);

		showLogfiles.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				Logging.debug(this, " new logfiles tabindex " + showLogfiles.getSelectedIndex());

				String logtype = Globals.getLogType(showLogfiles.getSelectedIndex());

				// logfile empty?
				if (!configedMain.logfileExists(logtype)) {
					setUpdatedLogfilePanel(logtype);
				}

			}
		});

		panelProductProperties = new PanelProductProperties(configedMain);
		panelProductProperties.propertiesPanel.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				Globals.createImageIcon("images/config_pro.png", ""), panelProductProperties,
				Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				ConfigedMain.VIEW_PRODUCT_PROPERTIES);

		Logging.info(this,
				"added tab  " + Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties") + " index "
						+ jTabbedPaneConfigPanes
								.indexOfTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		panelHostProperties = new PanelHostProperties();
		panelHostProperties.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties"),
				Globals.createImageIcon("images/config_pro.png", ""), panelHostProperties,
				Configed.getResourceValue("MainFrame.jPanel_HostProperties"), ConfigedMain.VIEW_HOST_PROPERTIES);

		Logging.info(this, "added tab  " + Configed.getResourceValue("MainFrame.jPanel_HostProperties") + " index "
				+ jTabbedPaneConfigPanes.indexOfTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties")));

		jTabbedPaneConfigPanes.setSelectedIndex(0);

		setTitle(configedMain.getAppTitle());

		Containership csJPanelAllContent = new Containership(allPane);

		csJPanelAllContent.doForAllContainedCompisOfClass("setDragEnabled", new Object[] { true },
				new Class[] { boolean.class }, javax.swing.text.JTextComponent.class);

		// set colors of panels
		csJPanelAllContent.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_7 },
				JPanel.class);

		depotListPresenter.setBackground(depotListPresenter.getMyColor());

		Containership containershipPanelLocalbootProductsettings = new Containership(panelLocalbootProductSettings);
		containershipPanelLocalbootProductsettings.doForAllContainedCompisOfClass("setBackground",
				new Object[] { Globals.BACKGROUND_COLOR_3 }, VerticalPositioner.class);
		panelLocalbootProductSettings.setBackground(Globals.BACKGROUND_COLOR_3);

		Containership containershipPanelNetbootProductsettings = new Containership(panelNetbootProductSettings);
		containershipPanelNetbootProductsettings.doForAllContainedCompisOfClass("setBackground",
				new Object[] { Globals.BACKGROUND_COLOR_3 }, VerticalPositioner.class);
		panelNetbootProductSettings.setBackground(Globals.BACKGROUND_COLOR_3);

		iconPane0.setBackground(Globals.BACKGROUND_COLOR_7);
		iconBarPane.setBackground(Globals.BACKGROUND_COLOR_7);
		iconPane1.setBackground(Globals.BACKGROUND_COLOR_7);
		panelTreeClientSelection.setBackground(Globals.BACKGROUND_COLOR_7);
		statusPane.setBackground(Globals.BACKGROUND_COLOR_7);

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

		if (Globals.isGlobalReadOnly() && b) {
			return;
		}

		Logging.debug(this, "saveConfigurationsSetEnabled " + b);

		jMenuFileSaveConfigurations.setEnabled(b);
		iconButtonSaveConfiguration.setEnabled(b);
		iconButtonCancelChanges.setEnabled(b);
	}

	public void saveGroupSetEnabled(boolean b) {

		iconButtonSaveGroup.setEnabled(b);
	}

	// ----------------------------------------------------------------------------------------
	// action methods for visual interactions
	public void wakeOnLanActionWithDelay(int secs) {

		configedMain.wakeSelectedClientsWithDelay(secs);
	}

	public void wakeOnLanAction() {
		configedMain.wakeSelectedClients();
	}

	public void deletePackageCachesAction() {
		configedMain.deletePackageCachesOfSelectedClients();
	}

	public void fireOpsiclientdEventAction(String event) {
		configedMain.fireOpsiclientdEventOnSelectedClients(event);
	}

	public void showPopupOnClientsAction() {
		FEditTextWithExtra fText = new FEditTextWithExtra("", Configed.getResourceValue("MainFrame.writePopupMessage"),
				Configed.getResourceValue("MainFrame.writePopupDuration")) {
			@Override
			protected void commit() {
				super.commit();
				Float duration = 0.0f;
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

	public void shutdownClientsAction() {
		configedMain.shutdownSelectedClients();
	}

	public void rebootClientsAction() {
		configedMain.rebootSelectedClients();
	}

	public void deleteClientAction() {
		configedMain.deleteSelectedClients();
	}

	public void copyClientAction() {
		configedMain.copySelectedClient();
	}

	public void freeLicencesAction() {
		Logging.info(this, "freeLicencesAction ");
		configedMain.freeAllPossibleLicencesForSelectedClients();
	}

	public void remoteControlAction() {
		Logging.debug(this, "jMenuRemoteControl");
		configedMain.startRemoteControlForSelectedClients();
	}

	/**
	 * Calls method from configedMain to start the execution of given command
	 * 
	 * @param SSHCommand command
	 */
	public void remoteSSHExecAction(SSHCommand command) {
		Logging.debug(this, "jMenuRemoteSSHExecAction");
		configedMain.startSSHOpsiServerExec(command);
	}

	/**
	 * Calls method from configedMain to start the terminal
	 */
	public void remoteSSHTerminalAction() {
		Logging.debug(this, "jMenuRemoteSSHTerminalAction");
		configedMain.startSSHOpsiServerTerminal();
	}

	/**
	 * Calls method from configedMain to start the config dialog
	 */
	public void startSSHConfigAction() {
		Logging.debug(this, "jMenuSSHConfigAction");
		configedMain.startSSHConfigDialog();
	}

	/**
	 * Calls method from configedMain to start the command control dialog
	 */
	public void startSSHControlAction() {
		Logging.debug(this, "jMenuSSHControlAction");
		configedMain.startSSHControlDialog();
	}

	public void toggleClientFilterAction() {
		configedMain.toggleFilterClientList();
		jMenuClientselectionToggleClientFilter.setState(configedMain.getFilterClientList());
		popupSelectionToggleClientFilter.setState(configedMain.getFilterClientList());

		if (!configedMain.getFilterClientList()) {
			iconButtonToggleClientFilter.setIcon(Globals.createImageIcon("images/view-filter_disabled-32.png", ""));
		} else {
			iconButtonToggleClientFilter.setIcon(Globals.createImageIcon("images/view-filter-32.png", ""));
		}

	}

	public void invertClientselection() {
		configedMain.invertClientselection();
	}

	public void exitAction() {
		configedMain.finishApp(true, 0);
	}

	public void saveAction() {
		configedMain.checkSaveAll(false);
	}

	public void cancelAction() {
		configedMain.cancelChanges();
	}

	public void getSessionInfo() {

		configedMain.getSessionInfo();

	}

	protected void getReachableInfo() {
		iconButtonReachableInfo.setEnabled(false);
		try {
			SwingUtilities.invokeLater(configedMain::getReachableInfo);
		} catch (Exception ex) {
			Logging.debug(this, "Exception " + ex);
		}
	}

	public void callSelectionDialog() {
		configedMain.callClientSelectionDialog();
	}

	private List<String> getProduct(List<String> completeList) {
		FEditStringList fList = new FEditStringList();
		fList.setListModel(new DefaultComboBoxModel<>(completeList.toArray(new String[0])));
		fList.setTitle(Globals.APPNAME + ": " + Configed.getResourceValue("MainFrame.productSelection"));
		fList.init();

		fList.setSize(F_WIDTH / 2, this.getHeight() - 100);
		fList.setModal(true);
		fList.setLocationRelativeTo(this);
		fList.setVisible(true);

		Logging.debug(this, "fList getSelectedValue " + fList.getSelectedList());

		return fList.getSelectedList();
	}

	private void groupByNotCurrentProductVersion() {
		List<String> products = getProduct(new ArrayList<>(new TreeSet<>(configedMain.getProductNames())));

		if (!products.isEmpty()) {
			configedMain.selectClientsNotCurrentProductInstalled(products, false);
		}
	}

	private void groupByNotCurrentProductVersionOrBrokenInstallation() {
		List<String> products = getProduct(new ArrayList<>(new TreeSet<>(configedMain.getProductNames())));

		if (!products.isEmpty()) {
			configedMain.selectClientsNotCurrentProductInstalled(products, true);
		}
	}

	private void groupByFailedProduct() {
		List<String> products = getProduct(new ArrayList<>(new TreeSet<>(configedMain.getProductNames())));

		if (!products.isEmpty()) {
			configedMain.selectClientsWithFailedProduct(products);
		}
	}

	public void saveGroupAction() {
		configedMain.callSaveGroupDialog();
	}

	public void deleteGroupAction() {
		configedMain.callDeleteGroupDialog();
	}

	public void menuClientSelectionSetEnabled(boolean b) {
		jMenuClientselectionGetGroup.setEnabled(b);
		jMenuClientselectionGetSavedSearch.setEnabled(b);
		jMenuClientselectionProductNotUptodate.setEnabled(b);
		jMenuClientselectionProductNotUptodateOrBroken.setEnabled(b);
		iconButtonSetGroup.setEnabled(b);
	}

	public void reloadAction() {
		configedMain.reload();
	}

	public void reloadLicensesAction() {
		configedMain.reloadLicensesData();
		configedMain.licencesFrame.setVisible(true);
	}

	public void checkMenuItemsDisabling() {
		if (menuItemsHost == null) {
			Logging.info(this, "checkMenuItemsDisabling: menuItemsHost not yet enabled");
			return;
		}

		List<String> disabledClientMenuEntries = configedMain.getPersistenceController().getDisabledClientMenuEntries();

		if (disabledClientMenuEntries != null) {
			for (String menuActionType : disabledClientMenuEntries) {
				for (JMenuItem menuItem : menuItemsHost.get(menuActionType)) {
					Logging.debug(this, "disable " + menuActionType + ", " + menuItem);
					menuItem.setEnabled(false);
				}
			}

			iconButtonNewClient.setEnabled(!disabledClientMenuEntries.contains(ITEM_ADD_CLIENT));

			if (!configedMain.getPersistenceController().isCreateClientPermission()) {
				jMenuAddClient.setEnabled(false);
				jMenuCopyClient.setEnabled(false);
				popupAddClient.setEnabled(false);
				popupCopyClient.setEnabled(false);
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

		}

		// sometimes
		// before the menu is built completely
	}

	public void enableMenuItemsForClients(int countSelectedClients) {
		Logging.debug(this, " enableMenuItemsForClients, countSelectedClients " + countSelectedClients);

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

				jMenuResetProducts.setEnabled(true);
				popupResetProducts.setEnabled(true);
			} else {
				for (int i = 0; i < clientMenuItemsDependOnSelectionCount.length; i++) {
					clientMenuItemsDependOnSelectionCount[i].setEnabled(false);
				}

				for (int i = 0; i < clientPopupsDependOnSelectionCount.length; i++) {
					clientPopupsDependOnSelectionCount[i].setEnabled(false);
				}

				jMenuResetProducts.setEnabled(false);
				popupResetProducts.setEnabled(false);
			}

			if (countSelectedClients == 1) {
				jMenuChangeClientID.setEnabled(true);
				jMenuCopyClient.setEnabled(true);
				popupChangeClientID.setEnabled(true);
				popupCopyClient.setEnabled(true);
			} else {
				jMenuChangeClientID.setEnabled(false);
				jMenuCopyClient.setEnabled(false);
				popupChangeClientID.setEnabled(false);
				popupCopyClient.setEnabled(false);
			}
		}

		checkMenuItemsDisabling();
	}

	// ------------------- set visual toggle items

	// -------------------

	public void resetProductOnClientAction(boolean withProductProperties, boolean resetLocalbootProducts,
			boolean resetNetbootProducts) {
		configedMain.resetProductsForSelectedClients(withProductProperties, resetLocalbootProducts,
				resetNetbootProducts);
	}

	public void addClientAction() {
		configedMain.callNewClientDialog();
	}

	public void changeClientIDAction() {
		configedMain.callChangeClientIDDialog();
	}

	public void changeDepotAction() {
		configedMain.callChangeDepotDialog();
	}

	public void showBackendConfigurationAction() {

		FEditorPane backendInfoDialog = new FEditorPane(this,
				Globals.APPNAME + ":  " + Configed.getResourceValue("MainFrame.InfoInternalConfiguration"), false,
				new String[] { Configed.getResourceValue("MainFrame.InfoInternalConfiguration.close") }, 800, 600);
		backendInfoDialog.insertHTMLTable(configedMain.getBackendInfos(), "");

		backendInfoDialog.setVisible(true);
	}

	private void showAboutAction() {
		FTextArea info = new FTextArea(this, Globals.APPNAME + " Copyright Information", true,
				new String[] { Configed.getResourceValue("FGeneralDialog.ok") }, 700, 300);

		StringBuilder message = new StringBuilder();

		for (String line : CopyrightInfos.get()) {
			message.append("\n");
			message.append(line);
		}

		info.setMessage(message.toString());
		info.setVisible(true);
	}

	private void showLogfileLocationAction() {

		FTextArea info = new FTextArea(this,
				Globals.APPNAME + " " + Configed.getResourceValue("MainFrame.showLogFileInfoTitle"), false,
				new String[] { Configed.getResourceValue("MainFrame.showLogFileClose"),
						Configed.getResourceValue("MainFrame.showLogFileCopyToClipboard"),
						Configed.getResourceValue("MainFrame.showLogFileOpen") },
				new Icon[] { Globals.createImageIcon("images/cancel16_small.png", ""), null,
						Globals.createImageIcon("images/document-view16.png", "") },
				Globals.WIDTH_INFO_LOG_FILE, Globals.HEIGHT_INFO_LOG_FILE) {
			@Override
			public void doAction2() {
				getTextComponent().copy();
			}

			@Override
			public void doAction3() {
				try {
					Desktop.getDesktop().open(new File(Logging.getCurrentLogfilePath()));
				} catch (Exception e) {
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

	private void showOpsiModules() {
		if (configedMain.getPersistenceController().getOpsiLicencingInfoVersion()
				.equals(LicensingInfoMap.OPSI_LICENSING_INFO_VERSION_OLD)) {

			FTextArea f = new FTextArea(this, Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"),
					true);
			StringBuilder message = new StringBuilder();
			Map<String, Object> modulesInfo = configedMain.getPersistenceController().getOpsiModulesInfos();

			int count = 0;
			for (Entry<String, Object> modulesInfoEntry : modulesInfo.entrySet()) {
				count++;
				message.append("\n " + modulesInfoEntry.getKey() + ": " + modulesInfoEntry.getValue());
			}
			f.setSize(new Dimension(300, 50 + count * 25));

			f.setMessage(message.toString());
			f.setVisible(true);
		} else {
			callOpsiLicensingInfo();
		}
	}

	private void showInfoPage() {
		FEditorPane fEditPane = new FEditorPane(this, "opsi server infoPage", false, new String[] { "ok" }, 500, 400);
		fEditPane.setPage("https://" + configedMain.getConfigserver() + ":4447/info");

		fEditPane.setVisible(true);
	}

	public void callOpsiLicensingInfo() {

		if (fDialogOpsiLicensingInfo == null) {

			fDialogOpsiLicensingInfo = new de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo(this, // owner frame
					// title
					Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), false, // modal

					new String[] { Configed.getResourceValue("Dash.close"),

					},

					new Icon[] {

							Globals.createImageIcon("images/cancel16_small.png", "") },
					1, 900, 680, true, // lazylayout, i.e, we have a chance to define components and use them for the
					// layout
					null // addPanel predefined
			);
		} else {
			fDialogOpsiLicensingInfo.setLocationRelativeTo(this);
			fDialogOpsiLicensingInfo.setVisible(true);
		}
	}

	// ----------------------------------------------------------------------------------------

	void jButtonSaveListActionPerformed(ActionEvent e) {
		configedMain.checkSaveAll(false);
	}

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

	// TODO: kann das weg? arrange dialogs for opsi-client wake on LAN...
	protected void arrangeWs(Set<JDialog> frames) {
		// problem: https://bugs.openjdk.java.net/browse/JDK-7074504
		// Can iconify, but not deiconify a modal JDialog

		if (frames == null) {
			return;
		}

		int transpose = 20;

		for (java.awt.Window f : frames) {
			transpose = transpose + Globals.LINE_HEIGHT;

			if (f != null) {
				f.setVisible(true);
				try {
					f.setLocation(getLocation().x + transpose, getLocation().y + transpose);
				} catch (Exception ex) {
					Logging.info(this, "arrangeWs, could not get location");
				}
			}
		}
	}

	// RunningInstancesObserver
	@Override
	public void instancesChanged(Set<JDialog> instances) {

		boolean existJDialogInstances = (instances != null && !instances.isEmpty());

		if (jMenuShowScheduledWOL != null) {
			jMenuShowScheduledWOL.setEnabled(existJDialogInstances);
		}
		if (jMenuFrameShowDialogs != null) {
			jMenuFrameShowDialogs.setEnabled(existJDialogInstances);
		}
	}

	@Override
	public void executeCommandOnInstances(String command, Set<JDialog> instances) {
		Logging.info(this, "executeCommandOnInstances " + command + " for count instances " + instances.size());
		if (command.equals("arrange")) {
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

			}

			else if (e.getSource() == jTextFieldInventoryNumber) {

				if (jTextFieldInventoryNumber.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, jTextFieldInventoryNumber.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_INVENTORY_NUMBER_KEY);
				}

			}

			else if (e.getSource() == jTextFieldOneTimePassword) {
				if (jTextFieldOneTimePassword.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY, jTextFieldOneTimePassword.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY);
				}

			}

			else if (e.getSource() == jTextAreaNotes) {
				if (!jTextAreaNotes.getText().equals(oldNotes)) {
					changedClientInfo.put(HostInfo.CLIENT_NOTES_KEY, jTextAreaNotes.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_NOTES_KEY);
				}

			}

			else if (e.getSource() == macAddressField) {

				// oldMacAddress

				Logging.debug(this, " keyPressed on macAddressField, text " + macAddressField.getText());

				if (macAddressField.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macAddressField.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_MAC_ADRESS_KEY);

				}

			}

			else if (e.getSource() == ipAddressField) {

				// oldMacAddress

				Logging.debug(this, " keyPressed on ipAddressField, text " + ipAddressField.getText());

				if (ipAddressField.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipAddressField.getText());
					configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_IP_ADDRESS_KEY);

				}

			}

		}
	}

	// MouseListener implementation
	@Override
	public void mouseClicked(MouseEvent e) {
		Logging.debug(this, "mouse clicked " + Arrays.toString(configedMain.getSelectedClients()));

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
		Logging.debug(this, "key released " + Arrays.toString(configedMain.getSelectedClients()));

		reactToHostDataChange(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	// ActionListener implementation
	@Override
	public void actionPerformed(ActionEvent e) {
		Logging.debug(this, "actionPerformed on " + e.getSource());
		if (e.getSource() == cbInstallByShutdown) {
			Logging.info(this, "actionPerformed on cbInstallByShutdown");

			for (String client : configedMain.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY,
						(cbInstallByShutdown.isSelected()).toString());

				configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}

		} else if (e.getSource() == cbUefiBoot) {
			Logging.info(this, "actionPerformed on cbUefiBoot");

			for (String client : configedMain.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.CLIENT_UEFI_BOOT_KEY, (cbUefiBoot.isSelected()).toString());

				configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}
		} else if (e.getSource() == cbWANConfig) {
			Logging.info(this, "actionPerformed on cbWANConfig");

			for (String client : configedMain.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.CLIENT_WAN_CONFIG_KEY, (cbWANConfig.isSelected()).toString());
				configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}
		} else if (e.getSource() == jButtonClientsConfiguration) {
			configedMain.setEditingTarget(ConfigedMain.EditingTarget.CLIENTS);
		} else if (e.getSource() == jButtonDepotsConfiguration) {
			configedMain.setEditingTarget(ConfigedMain.EditingTarget.DEPOTS);
		} else if (e.getSource() == jButtonServerConfiguration) {
			configedMain.setEditingTarget(ConfigedMain.EditingTarget.SERVER);
		} else if (e.getSource() == jButtonLicences || e.getSource() == jMenuFrameLicences) {
			configedMain.handleLicencesManagementRequest();
			if (Boolean.TRUE.equals(configedMain.getPersistenceController().getGlobalBooleanConfigValue(
					AbstractPersistenceController.KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT,
					AbstractPersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT))) {
				// Starting JavaFX-Thread by creating a new JFXPanel, but not
				// using it since it is not needed.
				new JFXPanel();

				Platform.runLater(() -> {
					if (licenseDisplayer == null) {
						try {
							licenseDisplayer = new LicenseDisplayer();
							licenseDisplayer.initAndShowGUI();
						} catch (IOException ioE) {
							Logging.debug(this, "Unable to open FXML file.");
						}
					} else {
						licenseDisplayer.display();
					}
				});
			}
		} else if (e.getSource() == jButtonWorkOnGroups || e.getSource() == jMenuFrameWorkOnGroups) {
			configedMain.handleGroupActionRequest();

		} else if (e.getSource() == jButtonWorkOnProducts || e.getSource() == jMenuFrameWorkOnProducts) {
			configedMain.handleProductActionRequest();

		} else if (e.getSource() == jButtonDashboard || e.getSource() == jMenuFrameDashboard) {
			configedMain.initDashInfo();
		}

		else if (e.getSource() == jButtonOpsiLicenses) {

			showOpsiModules();
		}

	}

	public class LicenseDash {
		private final JFrame frame = new JFrame();
		private LicenseDisplayer licenseDisplayer;

		public void initAndShowGUI() {
			final JFXPanel fxPanel = new JFXPanel();
			frame.add(fxPanel);
			frame.setIconImage(Globals.mainIcon);
			frame.setTitle(Configed.getResourceValue("Dashboard.licenseTitle"));
			frame.setVisible(true);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			Platform.runLater(() -> {
				try {
					initFX();
				} catch (IOException ioE) {
					Logging.error(this, "Unable to open fxml file");
				}
			});
		}

		public void initFX() throws IOException {
			try {
				licenseDisplayer = new LicenseDisplayer();
				licenseDisplayer.initAndShowGUI();
			} catch (IOException ioE) {
				Logging.debug(this, "Unable to open FXML file.");
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

	}

	public void visualizeEditingTarget(ConfigedMain.EditingTarget t) {
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

	public void initHardwareInfo(List config) {
		if (showHardwareLogVersion2 == null) {
			showHardwareLogVersion2 = new de.uib.configed.gui.hwinfopage.PanelHWInfo(configedMain) {
				@Override
				protected void reload() {
					super.reload();
					configedMain.clearHwInfo();

					// otherwise we get a wait cursor only in table component
					configedMain.resetView(ConfigedMain.VIEW_HARDWARE_INFO);
				}
			};
		}
		showHardwareLogVersion2.setHardwareConfig(config);
	}

	private void showHardwareInfo() {

		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog")),
				showHardwareLog);

		showHardwareLog.repaint();

	}

	public void setHardwareInfoNotPossible(String label1S, String label2S) {
		Logging.info(this, "setHardwareInfoNotPossible " + label1S);

		if (showHardwareLogNotFound == null || showHardwareLogParentOfNotFoundPanel == null) {
			showHardwareLogNotFound = new TitledPanel();
			showHardwareLogParentOfNotFoundPanel = new JPanel();
			showHardwareLogNotFound.setBackground(Globals.BACKGROUND_COLOR_7);
			showHardwareLogParentOfNotFoundPanel.setLayout(new BorderLayout());
			showHardwareLogParentOfNotFoundPanel.add(showHardwareLogNotFound);

		}

		showHardwareLogNotFound.setTitle(label1S, label2S);
		showHardwareLog = showHardwareLogParentOfNotFoundPanel;
		showHardwareInfo();
	}

	public void setHardwareInfoMultiClients(String[] clients) {
		if (showHardwareLogMultiClientReport == null || controllerHWinfoMultiClients == null) {
			controllerHWinfoMultiClients = new ControllerHWinfoMultiClients(configedMain,
					configedMain.getPersistenceController());
			showHardwareLogMultiClientReport = controllerHWinfoMultiClients.panel;
		}

		Logging.info(this, "setHardwareInfoMultiClients " + clients.length);

		controllerHWinfoMultiClients.setFilter();
		showHardwareLog = showHardwareLogMultiClientReport;

		showHardwareInfo();

	}

	public void setHardwareInfo(Object hardwareInfo) {

		if (hardwareInfo == null) {
			showHardwareLogVersion2.setHardwareInfo(null,
					Configed.getResourceValue("MainFrame.NoHardwareConfiguration"));
		} else {
			showHardwareLogVersion2.setHardwareInfo((Map) hardwareInfo, null);
		}

		showHardwareLog = showHardwareLogVersion2;
		showHardwareInfo();

	}

	private void showSoftwareAudit() {
		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog")),
				showSoftwareLog);

		SwingUtilities.invokeLater(() -> ConfigedMain.getMainFrame().repaint());
	}

	protected boolean handleInstallByShutdownChange(final boolean wantActive) {

		boolean goOn = true;

		String clientID = getClientID();
		if ((clientID == null) || (clientID.length() == 0)) {
			return goOn;
		}

		// for testing commented out

		List<String> shutdownValueX = null;
		try {
			shutdownValueX = (List) configedMain.getPersistenceController()
					.getProductProperties(clientID, "opsi-client-agent").get("on_shutdown_install");
		} catch (Exception ex) {
			Logging.error("cannot get product property  on_shutdown_install for client " + clientID, ex);
		}

		final List<String> shutdownValue = shutdownValueX;

		// for testing defined with fixed values

		final Boolean activate = wantActive;

		if (shutdownValue == null) {
			Logging.info(this, "product property on_shutdown_install does not exist");
			return goOn;
		} else {

			FTextArea fObsolete = new FTextArea(Globals.frame1,
					Configed.getResourceValue("NewClientDialog.installByShutdown"), true,
					new String[] { Configed.getResourceValue("FGeneralDialog.cancel"),
							Configed.getResourceValue("FGeneralDialog.ok") },
					300, 200) {
				@Override
				protected boolean wantToBeRegisteredWithRunningInstances() {
					return false;
				}

				@Override
				public void doAction1() {
					Logging.info(this, "cancel");
					result = 1;
					leave();
				}

				@Override
				public void doAction2() {
					Logging.info(this, "set property and call setup for the opsi-clientagent");
					handle(activate);
					result = 2;
					leave();
				}

				@Override
				public void doAction3() {
					Logging.info(this, "cancel");
					result = 3;
					leave();
				}

				private void handle(boolean switchOn) {
					Logging.info(this, "handle " + switchOn + ", old value for one client " + shutdownValue);

					configedMain.setInstallByShutdownProductPropertyValue(switchOn);
					configedMain.requestReloadStatesAndActions();

				}

			};

			fObsolete.setMessage("with opsi.client-agent up to 4.1 :\n\n" + "the property " + "on_shutdown_install"
					+ " \n" + "has to be set,\n\n " + "any change requires a setup of the client agent\n"
					+ "do it now?");

			fObsolete.setVisible(true);

			if (fObsolete.getResult() != 1) {
				goOn = false;
			}
		}

		return goOn;

	}

	private class SwExporter implements ActionListener {
		PanelSWMultiClientReport showSoftwareLogMultiClientReport;
		PanelSWInfo panelSWInfo;

		SwExporter(PanelSWMultiClientReport showSoftwareLogMultiClientReport, PanelSWInfo panelSWInfo) {
			this.showSoftwareLogMultiClientReport = showSoftwareLogMultiClientReport;
			this.panelSWInfo = panelSWInfo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			Logging.info(this, "actionPerformed " + "  showSoftwareLog_MultiClientReport.wantsWithMsUpdates  "
					+ showSoftwareLogMultiClientReport.wantsWithMsUpdates());

			// save states now

			Configed.savedStates.saveSWauditExportFilePrefix
					.serialize(showSoftwareLogMultiClientReport.getExportfilePrefix());

			String filepathStart = showSoftwareLogMultiClientReport.getExportDirectory() + File.separator
					+ showSoftwareLogMultiClientReport.getExportfilePrefix();

			String extension = "." + showSoftwareLogMultiClientReport.wantsKindOfExport().toString().toLowerCase();

			panelSWInfo.setWithMsUpdates(showSoftwareLogMultiClientReport.wantsWithMsUpdates());
			panelSWInfo.setWithMsUpdates2(showSoftwareLogMultiClientReport.wantsWithMsUpdates2());

			panelSWInfo.setAskingForKindOfAction(false);
			panelSWInfo.setAskForOverwrite(showSoftwareLogMultiClientReport.wantsAskForOverwrite());

			panelSWInfo.setKindOfExport(showSoftwareLogMultiClientReport.wantsKindOfExport());

			List<String> clientsWithoutScan = new ArrayList<>();

			for (String client : configedMain.getSelectedClients()) {
				Map<String, Map> tableData = configedMain.getPersistenceController().retrieveSoftwareAuditData(client);
				if (tableData == null || tableData.isEmpty()) {
					clientsWithoutScan.add(client);
				}

			}

			Logging.info(this, "clientsWithoutScan " + clientsWithoutScan);

			for (String client : configedMain.getSelectedClients()) {
				panelSWInfo.setHost(client);

				panelSWInfo.updateModel();

				String scandate = configedMain.getPersistenceController().getLastSoftwareAuditModification(client);
				if (scandate != null) {
					int timePos = scandate.indexOf(' ');
					if (timePos >= 0) {
						scandate = scandate.substring(0, timePos);
					} else {
						scandate = "__";
					}
				}

				String filepath = filepathStart + client + "__scan_" + scandate + extension;
				Logging.debug(this, "actionPerformed, write to " + filepath);
				panelSWInfo.setWriteToFile(filepath);

				panelSWInfo.export();
			}

			Logging.info(this, "clientsWithoutScan " + clientsWithoutScan);

		}

	}

	public void setSoftwareAudit() {
		if (configedMain.getSelectedClients() != null && configedMain.getSelectedClients().length > 1) {
			Logging.info(this, "setSoftwareAudit for clients " + configedMain.getSelectedClients().length);

			showSoftwareLog = showSoftwareLogMultiClientReport;
			showSoftwareAudit();

		} else
		// handled by the following methos
		{
			labelNoSoftware.setText(Configed.getResourceValue("MainFrame.TabRequiresClientSelected"));
			showSoftwareLog = showSoftwareLogNotFound;
			showSoftwareAudit();
		}

	}

	public void setSoftwareAuditNullInfo(String hostId) {
		labelNoSoftware.setText(Configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));
		panelSWInfo.setSoftwareNullInfo(hostId);
	}

	public void setSoftwareAudit(String hostId) {
		labelNoSoftware.setText(Configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));

		Logging.debug(this, "setSoftwareAudit for " + hostId);
		panelSWInfo.setAskingForKindOfAction(true);
		panelSWInfo.setAskForOverwrite(true);
		panelSWInfo.setHost(hostId);
		panelSWInfo.updateModel();

		showSoftwareLog = panelSWInfo;

		showSoftwareAudit();
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
		int i = Arrays.asList(Globals.getLogTypes()).indexOf(logtype);
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

	public void setClientIpAddress(String s) {
		ipAddressField.setText(s);

	}

	public void setUefiBoot(Boolean b) {
		Logging.info(this, "setUefiBoot " + b);
		cbUefiBoot.setSelected(b);

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

	public String getClientID() {
		return labelHostID.getText();
	}

	public void setClientInfoediting(boolean singleClient) {
		// singleClient is primarily conceived as toggle: true for single host, false
		// for multi hosts editing

		// mix with global read only flag
		boolean gb = !Globals.isGlobalReadOnly();

		labelHost.setEnabled(singleClient);

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
		macAddressField.setEnabled(singleClient);
		macAddressField.setEditable(b1);
		ipAddressField.setEnabled(singleClient);
		ipAddressField.setEditable(b1);

		// multi host editing allowed
		cbUefiBoot.setEnabled(gb && configedMain.getPersistenceController().isWithUEFI());
		cbWANConfig.setEnabled(gb && configedMain.getPersistenceController().isWithWAN());
		cbInstallByShutdown.setEnabled(gb);

		jTextFieldHostKey.setMultiValue(!singleClient);
		jTextFieldHostKey.setEnabled(singleClient);

		if (singleClient) {
			jTextFieldDescription.setToolTipText(null);
			jTextFieldInventoryNumber.setToolTipText(null);
			jTextFieldOneTimePassword.setToolTipText(null);
			jTextAreaNotes.setToolTipText(null);
			jTextFieldDescription.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			jTextFieldInventoryNumber.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			jTextFieldOneTimePassword.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			jTextAreaNotes.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			macAddressField.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			ipAddressField.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

			cbUefiBoot.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			cbWANConfig.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			jTextFieldHostKey.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			cbInstallByShutdown.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

		} else {
			jTextFieldDescription
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldInventoryNumber
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldOneTimePassword
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextAreaNotes.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldDescription.setBackground(Globals.BACKGROUND_COLOR_3);
			jTextFieldInventoryNumber.setBackground(Globals.BACKGROUND_COLOR_3);
			jTextFieldOneTimePassword.setBackground(Globals.BACKGROUND_COLOR_3);
			jTextAreaNotes.setBackground(Globals.BACKGROUND_COLOR_3);

			macAddressField.setBackground(Globals.BACKGROUND_COLOR_3);
			ipAddressField.setBackground(Globals.BACKGROUND_COLOR_3);
			cbUefiBoot.setBackground(Globals.BACKGROUND_COLOR_3);
			cbWANConfig.setBackground(Globals.BACKGROUND_COLOR_3);
			jTextFieldHostKey.setBackground(Globals.BACKGROUND_COLOR_3);
			cbInstallByShutdown.setBackground(Globals.BACKGROUND_COLOR_3);

		}

	}

	public void setChangedDepotSelectionActive(boolean active) {
		depotListPresenter.setChangedDepotSelectionActive(active);
	}

	@Override
	public void paint(java.awt.Graphics g) {
		try {
			super.paint(g);
		} catch (java.lang.ClassCastException ex) {
			Logging.warning(this, "the ugly well known exception " + ex);
			WaitCursor.stopAll();
		}
	}
}
