package de.uib.configed.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
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
import java.util.List;
import java.util.Map;
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

public class MainFrame extends JFrame implements WindowListener, KeyListener, MouseListener, ActionListener,
		RunningInstancesObserver<JDialog>, ComponentListener {

	private static final int DIVIDER_LOCATION_CENTRAL_PANE = 300;
	protected int minHSizeTreePanel = 150;

	public static final int F_WIDTH = 800;
	public static final int F_HEIGHT = 600;

	private static final int F_WIDTH_RIGHTHANDED = 200;

	private static final int DIVIDER_LOCATION_CLIENT_TREE_MULTI_DEPOT = 200;
	private static final int DIVIDER_LOCATION_CLIENT_TREE_SIGLE_DEPOT = 50;

	protected String oldNotes;

	private Map<String, Map<String, String>> changedClientInfos;

	private ExportTable exportTable;

	private ConfigedMain main;

	private SizeListeningPanel allPane;

	// menu system

	public static final String ITEM_ADD_CLIENT = "add client";
	public static final String ITEM_DELETE_CLIENT = "remove client";
	public static final String ITEM_FREE_LICENCES = "free licences for client";

	private Map<String, List<JMenuItem>> menuItemsHost;

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

	JMenuItem jMenuSSHConfig = new JMenuItem();
	JMenuItem jMenuSSHConnection = new JMenuItem();
	JMenuItem jMenuSSHCommandControl = new JMenuItem();

	LinkedHashMap<String, Integer> labelledDelays;

	private Map<String, String> searchedTimeSpans;
	private Map<String, String> searchedTimeSpansText;

	// JCheckBoxMenuItem jCheckBoxMenuItem_displayClientList = new

	private JCheckBoxMenuItem jCheckBoxMenuItemShowCreatedColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItemShowWANactiveColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItemShowIPAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItemShowInventoryNumberColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItemShowHardwareAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItemShowSessionInfoColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItemShowUefiBoot = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItemShowInstallByShutdown = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItemShowDepotColumn = new JCheckBoxMenuItem();
	JMenuItem jMenuRemoteControl = new JMenuItem();

	JMenuItem[] clientMenuItemsDependOnSelectionCount = new JMenuItem[] { jMenuResetProductOnClientWithStates,
			jMenuResetProductOnClient, jMenuAddClient, jMenuDeleteClient, jMenuFreeLicences, jMenuChangeDepot,
			jMenuChangeClientID, };

	JMenu jMenuClientselection = new JMenu();
	JMenuItem jMenuClientselectionGetGroup = new JMenuItem();
	JMenuItem jMenuClientselectionGetSavedSearch = new JMenuItem();
	JMenuItem jMenuClientselectionProductNotUptodate = new JMenuItem();
	JMenuItem jMenuClientselectionProductNotUptodateOrBroken = new JMenuItem();
	JMenuItem jMenuClientselectionFailedProduct = new JMenuItem();
	JMenu jMenuClientselectionFailedInPeriod = new JMenu();

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

	JMenu menuPopupOpsiClientdEvent = new JMenu(configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent"));
	JMenuItemFormatted popupShowPopupMessage = new JMenuItemFormatted();
	JMenuItemFormatted popupRequestSessionInfo = new JMenuItemFormatted();
	JMenuItemFormatted popupShutdownClient = new JMenuItemFormatted();
	JMenuItemFormatted popupRebootClient = new JMenuItemFormatted();
	JMenuItemFormatted popupChangeDepot = new JMenuItemFormatted();
	JMenuItemFormatted popupChangeClientID = new JMenuItemFormatted();
	JMenuItemFormatted popupRemoteControl = new JMenuItemFormatted();

	JMenuItemFormatted[] clientPopupsDependOnSelectionCount = new JMenuItemFormatted[] { popupResetProductOnClient,
			popupAddClient, popupDeleteClient, popupFreeLicences, popupShowPopupMessage, popupRequestSessionInfo,
			popupDeletePackageCaches, popupRebootClient, popupShutdownClient, popupChangeDepot, popupChangeClientID,
			popupRemoteControl };

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

	JMenuItemFormatted popupSelectionDeselect = new JMenuItemFormatted();
	JCheckBoxMenuItem popupSelectionToggleClientFilter = new JCheckBoxMenuItem();

	JMenuItemFormatted popupRebuildClientList = new JMenuItemFormatted(
			configed.getResourceValue("PopupMenuTrait.reload"), Globals.createImageIcon("images/reload16.png", ""));
	JMenuItemFormatted popupCreatePdf = new JMenuItemFormatted(configed.getResourceValue("FGeneralDialog.pdf"),
			Globals.createImageIcon("images/acrobat_reader16.png", ""));

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

	public CombinedMenuItem combinedMenuItemCreatedColumn = new CombinedMenuItem(jCheckBoxMenuItemShowCreatedColumn,
			popupShowCreatedColumn);

	public CombinedMenuItem combinedMenuItemWANactiveColumn = new CombinedMenuItem(jCheckBoxMenuItemShowWANactiveColumn,
			popupShowWANactiveColumn);

	public CombinedMenuItem combinedMenuItemIPAddressColumn = new CombinedMenuItem(jCheckBoxMenuItemShowIPAddressColumn,
			popupShowIPAddressColumn);

	public CombinedMenuItem combinedMenuItemHardwareAddressColumn = new CombinedMenuItem(
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
	public JSplitPane panelClientSelection;

	private HostsStatusPanel statusPane;

	public PanelGroupedProductSettings panelLocalbootProductSettings;
	public PanelGroupedProductSettings panelNetbootProductSettings;
	public PanelHostConfig panelHostConfig;
	public PanelHostProperties panelHostProperties;
	public PanelProductProperties panelProductProperties;

	de.uib.configed.gui.hwinfopage.PanelHWInfo showHardwareLogVersion2;
	TitledPanel showHardwareLogNotFound;
	public ControllerHWinfoMultiClients controllerHWinfoMultiClients;
	JPanel showHardwareLogMultiClientReport;
	JPanel showHardwareLogParentOfNotFoundPanel;
	JPanel showHardwareLog;
	JLabel labelNoSoftware;
	Panelreinst panelReinstmgr = new Panelreinst();

	PanelSWInfo panelSWInfo;
	JPanel showSoftwareLogNotFound;
	PanelSWMultiClientReport showSoftwareLogMultiClientReport;
	JPanel showSoftwareLog;

	PanelTabbedDocuments showLogfiles;

	JPanel jPanelSchalterstellung;

	public de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo fDialogOpsiLicensingInfo;

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
	Containership csClientPane;

	int splitterPanelClientSelection = 0;
	int prefClientPaneW = 100;
	int clientPaneW;

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

			g.setColor(Globals.F_GENERAL_DIALOG_FADING_MIRROR_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
		}

	}

	GlassPane glass;

	public MainFrame(ConfigedMain main, JTableSelectionPanel selectionPanel, DepotsList depotsList,
			ClientTree treeClients, boolean multidepot) {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // we handle it in the window listener method

		this.multidepot = multidepot;

		panelClientlist = selectionPanel;

		exportTable = new ExporterToCSV(selectionPanel.getTable());

		this.treeClients = treeClients;

		depotListPresenter = new DepotListPresenter(depotsList, multidepot, main.getPersistenceController());

		this.main = main;
		addComponentListener(this);

		baseContainer = this.getContentPane();

		Globals.mainContainer = baseContainer;

		glass = new GlassPane();

		guiInit();
		initData();

		UIManager.put("OptionPane.yesButtonText", configed.getResourceValue("UIManager.yesButtonText"));
		UIManager.put("OptionPane.noButtonText", configed.getResourceValue("UIManager.noButtonText"));
		UIManager.put("OptionPane.cancelButtonText", configed.getResourceValue("UIManager.cancelButtonText"));

		FEditObject.runningInstances.addObserver(this);
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

	// This shall be called after MainFrame is made visible
	public void initSplitPanes() {
		panelClientSelection.setDividerLocation(0.8);
		panelLocalbootProductSettings.setDividerLocation(0.75);
		panelNetbootProductSettings.setDividerLocation(0.75);
		panelProductProperties.setDividerLocation(0.75);
	}

	public class SizeListeningPanel extends JPanel implements ComponentListener {
		SizeListeningPanel() {
			addComponentListener(this);
		}
		// ComponentListener implementation

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentResized(ComponentEvent e) {
			logging.debug(this, "componentResized");

			try {
				repairSizes();
			} catch (Exception ex) {
				logging.info(this, "componentResized " + ex);
			}
			logging.debug(this, "componentResized ready");

		}

		@Override
		public void componentShown(ComponentEvent e) {
		}

		private void moveDivider1(JSplitPane splitpane, JComponent rightpane, int min_right_width, int min_left_width,
				int max_right_width) {
			if (splitpane == null || rightpane == null)
				return;

			int dividerLocation = splitpane.getDividerLocation();

			int sizeOfRightPanel = (int) rightpane.getSize().getWidth();
			int missingSpace = min_right_width - sizeOfRightPanel;
			if (missingSpace > 0 && dividerLocation > min_left_width) {
				splitpane.setDividerLocation(dividerLocation - missingSpace);

			}

			if (sizeOfRightPanel > max_right_width) {
				splitpane.setDividerLocation(dividerLocation + (sizeOfRightPanel - max_right_width));
			}

		}

		public void repairSizes() {
			// repair sizes when the frame is resized

			if (panelClientSelection == null)
				return;

			splitterPanelClientSelection = panelClientSelection.getSize().width - clientPaneW;

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

		jMenuFile.setText(configed.getResourceValue("MainFrame.jMenuFile"));

		jMenuFileExit.setText(configed.getResourceValue("MainFrame.jMenuFileExit"));
		jMenuFileExit.addActionListener((ActionEvent e) -> exitAction());

		jMenuFileSaveConfigurations.setText(configed.getResourceValue("MainFrame.jMenuFileSaveConfigurations"));
		jMenuFileSaveConfigurations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSaveConfigurations.addActionListener((ActionEvent e) -> saveAction());

		jMenuFileReload.setText(configed.getResourceValue("MainFrame.jMenuFileReload"));

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

		searchedTimeSpansText.put(TODAY, configed.getResourceValue("MainFrame.TODAY"));
		searchedTimeSpansText.put(SINCE_YESTERDAY, configed.getResourceValue("MainFrame.SINCE_YESTERDAY"));
		searchedTimeSpansText.put(LAST_3_DAYS, configed.getResourceValue("MainFrame.LAST_3_DAYS"));
		searchedTimeSpansText.put(LAST_7_DAYS, configed.getResourceValue("MainFrame.LAST_7_DAYS"));
		searchedTimeSpansText.put(LAST_MONTH, configed.getResourceValue("MainFrame.LAST_MONTH"));
		searchedTimeSpansText.put(ANY_TIME, configed.getResourceValue("MainFrame.ANY_TIME"));

	}

	private void setupMenuClients() {
		jMenuClients.setText(configed.getResourceValue("MainFrame.jMenuClients"));

		jCheckBoxMenuItemShowCreatedColumn.setText(configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		combinedMenuItemCreatedColumn.show(main.hostDisplayFields.get(HostInfo.CREATED_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowCreatedColumn.addItemListener((ItemEvent e) -> main.toggleColumnCreated());

		jCheckBoxMenuItemShowWANactiveColumn.setText(configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		combinedMenuItemWANactiveColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowWANactiveColumn.addItemListener((ItemEvent e) -> main.toggleColumnWANactive());

		jCheckBoxMenuItemShowIPAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		combinedMenuItemIPAddressColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowIPAddressColumn.addItemListener((ItemEvent e) -> main.toggleColumnIPAddress());

		jCheckBoxMenuItemShowHardwareAddressColumn
				.setText(configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		combinedMenuItemHardwareAddressColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowHardwareAddressColumn.addItemListener((ItemEvent e) -> main.toggleColumnHardwareAddress());

		jCheckBoxMenuItemShowSessionInfoColumn
				.setText(configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		combinedMenuItemSessionInfoColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowSessionInfoColumn.addItemListener((ItemEvent e) -> {
			logging.info(this, "toggleColumnSessionInfo by CheckBoxMenuItem");
			main.toggleColumnSessionInfo();

		});

		jCheckBoxMenuItemShowInventoryNumberColumn
				.setText(configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		combinedMenuItemInventoryNumberColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowInventoryNumberColumn.addItemListener((ItemEvent e) -> main.toggleColumnInventoryNumber());

		jCheckBoxMenuItemShowUefiBoot.setText(configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		combinedMenuItemUefiBootColumn.show(main.hostDisplayFields.get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowUefiBoot.addItemListener((ItemEvent e) -> main.toggleColumnUEFIactive());

		jCheckBoxMenuItemShowInstallByShutdown
				.setText(configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		combinedMenuItemUefiBootColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowInstallByShutdown
				.addItemListener((ItemEvent e) -> main.toggleColumnInstallByShutdownActive());

		jCheckBoxMenuItemShowDepotColumn.setText(configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		combinedMenuItemDepotColumn.show(main.hostDisplayFields.get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));

		jCheckBoxMenuItemShowDepotColumn.addItemListener((ItemEvent e) -> main.toggleColumnDepot());

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

		jMenuNewScheduledWOL.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler"));
		final MainFrame f = this;
		jMenuNewScheduledWOL.addActionListener((ActionEvent e) -> {
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(
					Globals.APPNAME + ": " + configed.getResourceValue("FStartWakeOnLan.title"), main);
			fStartWakeOnLan.centerOn(f);

			fStartWakeOnLan.setVisible(true);
			fStartWakeOnLan.setPredefinedDelays(labelledDelays);

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
		logging.info(this, "setupMenuServer ");
		final SSHCommandFactory factory = SSHCommandFactory.getInstance(main);
		SSHConnectionInfo connectionInfo = SSHConnectionInfo.getInstance();

		factory.setMainFrame(this);
		JMenu menuOpsi = new JMenu();
		menuOpsi.setText(SSHCommandFactory.parentOpsi);

		jMenuServer.removeAll();
		jMenuServer.setText(SSHCommandFactory.parentNull);
		boolean isReadOnly = Globals.isGlobalReadOnly();
		boolean methodsExists = factory.checkSSHCommandMethod();

		logging.info(this, "setupMenuServer add configpage");
		jMenuSSHConfig = new JMenuItem();
		jMenuSSHConfig.setText(configed.getResourceValue("MainFrame.jMenuSSHConfig"));
		jMenuSSHConfig.addActionListener((ActionEvent e) -> startSSHConfigAction());

		jMenuSSHConnection.setEnabled(false);
		if (configed.sshconnect_onstart)
			factory.testConnection(connectionInfo.getUser(), connectionInfo.getHost());

		if (factory.checkSSHCommandMethod()) {
			logging.info(this, "setupMenuServer add commandcontrol");
			jMenuSSHCommandControl = new JMenuItem();
			jMenuSSHCommandControl.setText(configed.getResourceValue("MainFrame.jMenuSSHCommandControl"));
			jMenuSSHCommandControl.addActionListener((ActionEvent e) -> startSSHControlAction());
		}
		// SSHCommandControlDialog

		jMenuServer.add(jMenuSSHConnection);
		jMenuServer.add(jMenuSSHConfig);
		if (factory.checkSSHCommandMethod())
			jMenuServer.add(jMenuSSHCommandControl);

		jMenuServer.addSeparator();

		logging.info(this, "setupMenuServer getCurrentUserConfig " + UserConfig.getCurrentUserConfig());

		boolean commandsAreDeactivated = (UserConfig.getCurrentUserConfig() == null
				|| UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDS_ACTIVE) == null
				|| !UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDS_ACTIVE));
		logging.info(this, "setupMenuServer commandsAreDeactivated " + commandsAreDeactivated);

		if (methodsExists) {
			factory.retrieveSSHCommandListRequestRefresh();
			factory.retrieveSSHCommandList();
			Map<String, List<SSHCommand_Template>> sortedComs = factory.getSSHCommandMapSortedByParent();

			logging.debug(this, "setupMenuServer add commands to menu commands sortedComs " + sortedComs);
			boolean firstParentGroup = true;
			boolean commandsExist = false;
			for (Map.Entry<String, List<SSHCommand_Template>> entry : sortedComs.entrySet()) {
				String parentMenuName = entry.getKey();
				LinkedList<SSHCommand_Template> listCom = new LinkedList<>(entry.getValue());
				Collections.sort(listCom);
				JMenu parentMenu = new JMenu();
				parentMenu.setText(parentMenuName);
				logging.info(this, "ssh parent menu text " + parentMenuName);
				if (parentMenuName.equals(SSHCommandFactory.parentdefaultForOwnCommands)) {
					parentMenu.setText("");
					parentMenu.setIcon(Globals.createImageIcon("images/burger_menu_09.png", "..."));
				}

				if (!(parentMenuName.equals(SSHCommandFactory.parentNull)))
					firstParentGroup = false;

				for (final SSHCommand_Template com : listCom) {
					commandsExist = true;
					JMenuItem jMenuItem = new JMenuItem();
					jMenuItem.setText(com.getMenuText());
					logging.info(this, "ssh command menuitem text " + com.getMenuText());
					jMenuItem.setToolTipText(com.getToolTipText());
					jMenuItem.addActionListener(new ActionListener() {
						@Override
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
				if (firstParentGroup && commandsExist)
					jMenuServer.addSeparator();

				firstParentGroup = false;

			}
			menuOpsi.addSeparator();
		} else
			jMenuServer.add(menuOpsi);
		List<SSHCommand> commands = factory.getSSHCommandParameterList();
		logging.info(this, "setupMenuServer add parameterDialogs to opsi commands" + commands);
		for (final SSHCommand command : commands) {
			JMenuItem jMenuOpsiCommand = new JMenuItem();
			jMenuOpsiCommand.setText(command.getMenuText());
			jMenuOpsiCommand.setToolTipText(command.getToolTipText());
			jMenuOpsiCommand.addActionListener(new ActionListener() {

				@Override
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

		boolean userConfigExists = UserConfig.getCurrentUserConfig() != null;

		jMenuSSHConfig.setEnabled(userConfigExists
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_CONFIG_ACTIVE));

		logging.info(this, "setupMenuServer create/read command menu configs current user config "
				+ UserConfig.getCurrentUserConfig());
		jMenuSSHCommandControl.setEnabled(userConfigExists
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDCONTROL_ACTIVE));

		jMenuSSHCommandControl.setEnabled(true);

		jMenuServer.setEnabled(userConfigExists && !isReadOnly

				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_MENU_ACTIVE));
	}

	private void setupMenuGrouping() {
		jMenuClientselection.setText(configed.getResourceValue("MainFrame.jMenuClientselection"));

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

		for (final String value : searchedTimeSpansText.values()) {
			JMenuItem item = new JMenuItemFormatted(value);
			item.setFont(Globals.defaultFont);

			item.addActionListener((ActionEvent e) -> main.selectClientsByFailedAtSomeTimeAgo(value));

			jMenuClientselectionFailedInPeriod.add(item);
		}

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
			@Override
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
		jMenuHelp.setText( configed.getResourceValue("MainFrame.jMenuHelp") );

		jMenuHelpDoc.setText(configed.getResourceValue("MainFrame.jMenuDoc"));
		jMenuHelpDoc.addActionListener(new ActionListener(){
			                               public void actionPerformed(ActionEvent e)
			                               {
				                               main.showExternalDocument(Globals.OPSI);
			                               }
		                               });
		jMenuHelp.add(jMenuHelpDoc);


		jMenuHelpForum.setText(configed.getResourceValue("MainFrame.jMenuForum"));
		jMenuHelpForum.addActionListener(new ActionListener(){
			                                 public void actionPerformed(ActionEvent e)
			                                 {
				                                 main.showExternalDocument(Globals.opsiForumpage);
			                                 }
		                                 });
		jMenuHelp.add(jMenuHelpForum);

		jMenuHelpSupport.setText(configed.getResourceValue("MainFrame.jMenuSupport"));
		jMenuHelpSupport.addActionListener(new ActionListener(){
			                                   public void actionPerformed(ActionEvent e)
			                                   {
				                                   main.showExternalDocument(Globals.opsiSupportpage);
			                                   }
		                                   });
		jMenuHelp.add(jMenuHelpSupport);

		jMenuHelp.addSeparator();

		jMenuHelpOpsiVersion
				.setText(configed.getResourceValue("MainFrame.jMenuHelpOpsiService") + ": " + main.getOpsiVersion());
		jMenuHelpOpsiVersion.setEnabled(false);
		jMenuHelpOpsiVersion.setForeground(Globals.lightBlack);

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

		iconButtonReload = new IconButton(configed.getResourceValue("MainFrame.iconButtonReload"), "images/reload.gif",
				"images/reload_over.gif", " ");

		iconButtonReloadLicenses = new IconButton(configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"),
				"images/reload_licenses.png", "images/reload_licenses_over.png", " ", false);
		iconButtonReloadLicenses.setVisible(false);

		iconButtonNewClient = new IconButton(configed.getResourceValue("MainFrame.iconButtonNewClient"),
				"images/newClient.gif", "images/newClient_over.gif", " ");

		iconButtonSetGroup = new IconButton(configed.getResourceValue("MainFrame.iconButtonSetGroup"),
				"images/setGroup.gif", "images/setGroup_over.gif", " ");
		iconButtonSaveConfiguration = new IconButton(configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
				"images/apply_over.gif", " ", "images/apply_disabled.gif", false);

		iconButtonCancelChanges = new IconButton(configed.getResourceValue("MainFrame.iconButtonCancelChanges"),
				"images/cancel-32.png", "images/cancel_over-32.png", " ", false);

		iconButtonReachableInfo = new IconButton(configed.getResourceValue("MainFrame.iconButtonReachableInfo"),
				"images/new_networkconnection.png", "images/new_networkconnection.png",
				"images/new_networkconnection.png", main.hostDisplayFields.get("clientConnected"));

		String[] waitingCircle = new String[] { "images/systemusers_sessioninfo_activitycircle/loading_01.png",
				"images/systemusers_sessioninfo_activitycircle/loading_02.png" };

		iconButtonSessionInfo = new IconButton(configed.getResourceValue("MainFrame.iconButtonSessionInfo"),
				"images/system-users-query.png", "images/system-users-query_over.png",
				"images/system-users-query_over.png", waitingCircle,

				500, main.hostDisplayFields.get("clientSessionInfo"));
		iconButtonSessionInfo.setEnabled(true);

		iconButtonToggleClientFilter = new IconButton(
				configed.getResourceValue("MainFrame.iconButtonToggleClientFilter"),
				"images/view-filter_disabled-32.png", "images/view-filter_over-32.png", "images/view-filter-32.png",
				true);

		iconButtonSaveGroup = new IconButton(configed.getResourceValue("MainFrame.iconButtonSaveGroup"),
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

	// ------------------------------------------------------------------------------------------
	// context menus

	private void setupPopupMenuClientsTab() {

		popupShowCreatedColumn.setText(configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		combinedMenuItemCreatedColumn.show(main.hostDisplayFields.get(HostInfo.CREATED_DISPLAY_FIELD_LABEL));

		popupShowCreatedColumn.addItemListener((ItemEvent e) -> main.toggleColumnCreated());

		popupShowWANactiveColumn.setText(configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		combinedMenuItemWANactiveColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

		popupShowWANactiveColumn.addItemListener((ItemEvent e) -> main.toggleColumnWANactive());

		popupShowIPAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		combinedMenuItemIPAddressColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL));

		popupShowIPAddressColumn.addItemListener((ItemEvent e) -> main.toggleColumnIPAddress());

		popupShowHardwareAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		combinedMenuItemHardwareAddressColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL));

		popupShowHardwareAddressColumn.addItemListener((ItemEvent e) -> main.toggleColumnHardwareAddress());

		popupShowSessionInfoColumn.setText(configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		combinedMenuItemSessionInfoColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));

		popupShowSessionInfoColumn.addItemListener((ItemEvent e) -> main.toggleColumnSessionInfo());

		popupShowInventoryNumberColumn.setText(configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		combinedMenuItemInventoryNumberColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL));

		popupShowInventoryNumberColumn.addItemListener((ItemEvent e) -> main.toggleColumnInventoryNumber());

		popupShowUefiBoot.setText(configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		combinedMenuItemUefiBootColumn.show(main.hostDisplayFields.get(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

		popupShowUefiBoot.addItemListener((ItemEvent e) -> main.toggleColumnUEFIactive());

		popupShowInstallByShutdownColumn.setText(configed.getResourceValue("MainFrame.jMenuShowInstallByShutdown"));
		combinedMenuItemInstallByShutdownColumn
				.show(main.hostDisplayFields.get(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

		popupShowInstallByShutdownColumn.addItemListener((ItemEvent e) -> main.toggleColumnInstallByShutdownActive());

		popupShowDepotColumn.setText(configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		combinedMenuItemDepotColumn.show(main.hostDisplayFields.get(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL));

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
			FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(
					Globals.APPNAME + ": " + configed.getResourceValue("FStartWakeOnLan.title"), main);
			fStartWakeOnLan.centerOn(f);

			fStartWakeOnLan.setVisible(true);
			fStartWakeOnLan.setPredefinedDelays(labelledDelays);

			fStartWakeOnLan.setClients();
		});
		popupWakeOnLan.add(popupWakeOnLanScheduler);

		popupDeletePackageCaches.setText(configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
		popupDeletePackageCaches.addActionListener((ActionEvent e) -> deletePackageCachesAction());

		popupShowPopupMessage.setText(configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
		popupShowPopupMessage.addActionListener((ActionEvent e) -> showPopupOnClientsAction());

		popupRequestSessionInfo.setText(configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
		popupRequestSessionInfo.addActionListener((ActionEvent e) -> {
			main.setColumnSessionInfo(true);
			getSessionInfo();
		});

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
		popupClients.add(popupSelectionDeselect);
		popupClients.add(popupSelectionToggleClientFilter);

		popupClients.add(popupRebuildClientList);
		popupClients.add(popupCreatePdf);

		exportTable.addMenuItemsTo(popupClients);

	}

	public void createPdf() {
		TableModel tm = main.getSelectedClientsTableModel();
		JTable jTable = new JTable(tm);

		try {
			HashMap<String, String> metaData = new HashMap<>();
			String title = configed.getResourceValue("MainFrame.ClientList");

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
			pdfExportTable.setPageSizeA4_Landscape();
			pdfExportTable.execute(null, jTable.getSelectedRowCount() != 0);

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

		allPane = new SizeListeningPanel();

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

		csClientPane = new Containership(clientPane);

		GroupLayout layoutClientPane = new GroupLayout(clientPane);
		clientPane.setLayout(layoutClientPane);

		labelHost = new JLabel(Globals.createImageIcon("images/client.png", ""), SwingConstants.LEFT);
		labelHost.setPreferredSize(Globals.buttonDimension);

		labelHostID = new JLabel("");
		labelHostID.setFont(Globals.defaultFontStandardBold);

		JLabel labelClientDescription = new JLabel(configed.getResourceValue("MainFrame.jLabelDescription"));
		labelClientDescription.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientInventoryNumber = new JLabel(configed.getResourceValue("MainFrame.jLabelInventoryNumber"));
		labelClientInventoryNumber.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientNotes = new JLabel(configed.getResourceValue("MainFrame.jLabelNotes"));

		JLabel labelClientMacAddress = new JLabel(configed.getResourceValue("MainFrame.jLabelMacAddress"));

		JLabel labelClientIPAddress = new JLabel(configed.getResourceValue("MainFrame.jLabelIPAddress"));

		JLabel labelOneTimePassword = new JLabel(configed.getResourceValue("MainFrame.jLabelOneTimePassword"));

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

		ipAddressField = new JTextEditorField(new SeparatedDocument(
				/* allowedChars */ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' }, 12, '.', 3,
				false), "", 24);
		ipAddressField.addKeyListener(this);
		ipAddressField.addMouseListener(this);

		final Icon unselectedIcon;
		final Icon selectedIcon;
		final Icon nullIcon;

		unselectedIcon = Globals.createImageIcon("images/checked_not.png", "");
		selectedIcon = Globals.createImageIcon("images/checked.png", "");
		nullIcon = Globals.createImageIcon("images/checked_box_mixed.png", "");

		cbUefiBoot = new CheckedLabel(configed.getResourceValue("NewClientDialog.boottype"), selectedIcon,
				unselectedIcon, nullIcon, false);
		cbUefiBoot.addActionListener(this);

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
		jCheckBoxSorted.setText(configed.getResourceValue("MainFrame.jCheckBoxSorted"));

		jButtonSaveList.setText(configed.getResourceValue("MainFrame.jButtonSaveList"));
		jButtonSaveList.setBackground(Globals.BACKGROUND_COLOR_6);
		jButtonSaveList.addActionListener(this::jButtonSaveList_actionPerformed);

		jRadioRequiredAll.setMargin(new Insets(0, 0, 0, 0));
		jRadioRequiredAll.setAlignmentY((float) 0.0);
		jRadioRequiredAll.setText(configed.getResourceValue("MainFrame.jRadioRequiredAll"));
		jRadioRequiredOff.setMargin(new Insets(0, 0, 0, 0));
		jRadioRequiredOff.setSelected(true);
		jRadioRequiredOff.setText(configed.getResourceValue("MainFrame.jRadioRequiredOff"));
		jRadioRequiredOff.setToolTipText("");

		jLabelPath.setText(configed.getResourceValue("MainFrame.jLabelPath"));
		jLabelHostinfos.setText(configed.getResourceValue("MainFrame.jLabel_Hostinfos"));

		buttonGroupRequired.add(jRadioRequiredAll);
		buttonGroupRequired.add(jRadioRequiredOff);

		jComboBoxProductValues.setBackground(Globals.BACKGROUND_COLOR_6);

		treeClients.setFont(Globals.defaultFont);

		scrollpaneTreeClients = new JScrollPane();

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

		logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() " +

				scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize()

		);
		JSplitPane splitpaneClientSelection = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
				depotListPresenter.getScrollpaneDepotslist(), scrollpaneTreeClients);

		logging.info(this, "multidepot " + multidepot);
		if (multidepot)
			splitpaneClientSelection.setDividerLocation(DIVIDER_LOCATION_CLIENT_TREE_MULTI_DEPOT);
		else
			splitpaneClientSelection.setDividerLocation(DIVIDER_LOCATION_CLIENT_TREE_SIGLE_DEPOT);

		panelTreeClientSelection = new JPanel();
		GroupLayout layoutPanelTreeClientSelection = new GroupLayout(panelTreeClientSelection);
		panelTreeClientSelection.setLayout(layoutPanelTreeClientSelection);

		layoutPanelTreeClientSelection.setHorizontalGroup(layoutPanelTreeClientSelection.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addGroup(layoutPanelTreeClientSelection.createParallelGroup(GroupLayout.Alignment.LEADING)

						.addComponent(depotListPresenter, minHSizeTreePanel, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addComponent(splitpaneClientSelection, minHSizeTreePanel, GroupLayout.PREFERRED_SIZE,
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

		jButtonWorkOnGroups.setEnabled(main.getPersistenceController().isWithLocalImaging());
		jButtonWorkOnGroups.addActionListener(this);

		jButtonWorkOnProducts = new JButton("", Globals.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setSelectedIcon(Globals.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setPreferredSize(Globals.modeSwitchDimension);
		jButtonWorkOnProducts.setToolTipText(configed.getResourceValue("MainFrame.labelWorkOnProducts"));

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
								.addComponent(jButtonDash, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
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
										.addComponent(jButtonDash, GroupLayout.PREFERRED_SIZE,
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

		panelClientSelection = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelClientlist, clientPane);

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.panel_Clientselection"),
				Globals.createImageIcon("images/clientselection.png", ""), panelClientSelection,
				configed.getResourceValue("MainFrame.panel_Clientselection"), ConfigedMain.VIEW_CLIENTS);

		panelLocalbootProductSettings = new PanelGroupedProductSettings(
				configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), main,
				main.getDisplayFieldsLocalbootProducts());

		panelNetbootProductSettings = new PanelGroupedProductSettings(
				configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), main,
				main.getDisplayFieldsNetbootProducts());

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),
				Globals.createImageIcon("images/package.png", ""), panelLocalbootProductSettings,
				configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),
				ConfigedMain.VIEW_LOCALBOOT_PRODUCTS);

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.panel_NetbootProductsettings"),
				Globals.createImageIcon("images/bootimage.png", ""), panelNetbootProductSettings,
				configed.getResourceValue("MainFrame.panel_NetbootProductsettings"),
				ConfigedMain.VIEW_NETBOOT_PRODUCTS);

		panelHostConfig = new PanelHostConfig() {
			@Override
			protected void reloadHostConfig() {
				logging.info(this, "reloadHostConfig");
				super.reloadHostConfig();
				main.cancelChanges();

				main.getPersistenceController().configOptionsRequestRefresh();

				main.getPersistenceController().hostConfigsRequestRefresh();
				main.resetView(ConfigedMain.VIEW_NETWORK_CONFIGURATION);
			}

			// overwrite in subclasses
			@Override
			protected void saveHostConfig() {
				super.saveHostConfig();
				main.checkSaveAll(false);
			}

		};

		panelHostConfig.registerDataChangedObserver(main.getHostConfigsDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_NetworkConfig"),
				Globals.createImageIcon("images/config_pro.png", ""), panelHostConfig,
				configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), ConfigedMain.VIEW_NETWORK_CONFIGURATION);

		showHardwareLog = new JPanel();

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_hardwareLog"),
				Globals.createImageIcon("images/hwaudit.png", ""), showHardwareLog,
				configed.getResourceValue("MainFrame.jPanel_hardwareLog"), ConfigedMain.VIEW_HARDWARE_INFO);

		panelSWInfo = new PanelSWInfo(main) {
			@Override
			protected void reload() {
				super.reload();
				main.clearSwInfo();
				main.getPersistenceController().installedSoftwareInformationRequestRefresh();
				main.getPersistenceController().softwareAuditOnClientsRequestRefresh();
				main.resetView(ConfigedMain.VIEW_SOFTWARE_INFO);
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

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_softwareLog"),
				Globals.createImageIcon("images/swaudit.png", ""), showSoftwareLog,
				configed.getResourceValue("MainFrame.jPanel_softwareLog"), ConfigedMain.VIEW_SOFTWARE_INFO);

		showLogfiles = new PanelTabbedDocuments(Globals.logtypes,
				configed.getResourceValue("MainFrame.DefaultTextForLogfiles")) {
			@Override
			public void loadDocument(String logtype) {
				super.loadDocument(logtype);
				logging.info(this, "loadDocument logtype " + logtype);
				setUpdatedLogfilePanel(logtype);
			}
		};

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_logfiles"),
				Globals.createImageIcon("images/logfile.png", ""), showLogfiles,
				configed.getResourceValue("MainFrame.jPanel_logfiles"), ConfigedMain.VIEW_LOG);

		showLogfiles.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				logging.debug(this, " new logfiles tabindex " + showLogfiles.getSelectedIndex());

				String logtype = Globals.logtypes[showLogfiles.getSelectedIndex()];

				// logfile empty?
				if (!main.logfileExists(logtype))
					setUpdatedLogfilePanel(logtype);

			}
		});

		panelProductProperties = new PanelProductProperties(main);
		panelProductProperties.propertiesPanel.registerDataChangedObserver(main.getGeneralDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				Globals.createImageIcon("images/config_pro.png", ""), panelProductProperties,
				configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				ConfigedMain.VIEW_PRODUCT_PROPERTIES);

		logging.info(this,
				"added tab  " + configed.getResourceValue("MainFrame.panel_ProductGlobalProperties") + " index "
						+ jTabbedPaneConfigPanes
								.indexOfTab(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		panelHostProperties = new PanelHostProperties();
		panelHostProperties.registerDataChangedObserver(main.getGeneralDataChangedKeeper());

		jTabbedPaneConfigPanes.insertTab(configed.getResourceValue("MainFrame.jPanel_HostProperties"),
				Globals.createImageIcon("images/config_pro.png", ""), panelHostProperties,
				configed.getResourceValue("MainFrame.jPanel_HostProperties"), ConfigedMain.VIEW_HOST_PROPERTIES);

		logging.info(this, "added tab  " + configed.getResourceValue("MainFrame.jPanel_HostProperties") + " index "
				+ jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_HostProperties")));

		jTabbedPaneConfigPanes.setSelectedIndex(0);

		setTitle(main.getAppTitle());

		Containership csjPanel_allContent = new Containership(allPane);

		csjPanel_allContent.doForAllContainedCompisOfClass("setDragEnabled", new Object[] { true },
				new Class[] { boolean.class }, javax.swing.text.JTextComponent.class);

		// set colors of panels
		csjPanel_allContent.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_7 },
				JPanel.class);

		depotListPresenter.setBackground(depotListPresenter.getMyColor());

		Containership cspanel_LocalbootProductsettings = new Containership(panelLocalbootProductSettings);
		cspanel_LocalbootProductsettings.doForAllContainedCompisOfClass("setBackground",
				new Object[] { Globals.BACKGROUND_COLOR_3 }, VerticalPositioner.class);
		panelLocalbootProductSettings.setBackground(Globals.BACKGROUND_COLOR_3);

		Containership cspanel_NetbootProductsettings = new Containership(panelNetbootProductSettings);
		cspanel_NetbootProductsettings.doForAllContainedCompisOfClass("setBackground",
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

		if (Globals.isGlobalReadOnly() && b)
			return;

		logging.debug(this, "saveConfigurationsSetEnabled " + b);

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
		fText.init();
		fText.centerOn(this);
		fText.setVisible(true);
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

		main.getSessionInfo();

	}

	protected void getReachableInfo() {
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

	private List<String> getProduct(List<String> completeList) {
		FEditList fList = new FEditList();
		fList.setListModel(new DefaultComboBoxModel<>(completeList.toArray()));
		fList.setTitle(Globals.APPNAME + ": " + configed.getResourceValue("MainFrame.productSelection"));
		fList.init();

		fList.setLocation(this.getX() + 40, this.getY() + 40);
		fList.setSize(F_WIDTH / 2, this.getHeight());

		fList.setModal(true);
		fList.setVisible(true);

		logging.debug(this, "fList getSelectedValue " + fList.getSelectedList());

		return (List<String>) fList.getSelectedList();
	}

	private void groupByNotCurrentProductVersion() {
		List<String> products = getProduct(new ArrayList<>(new TreeSet<>(main.getProductNames())));

		if (!products.isEmpty())
			main.selectClientsNotCurrentProductInstalled(products, false);

	}

	private void groupByNotCurrentProductVersionOrBrokenInstallation() {
		List<String> products = getProduct(new ArrayList<>(new TreeSet<>(main.getProductNames())));

		if (!products.isEmpty())
			main.selectClientsNotCurrentProductInstalled(products, true);

	}

	private void groupByFailedProduct() {
		List<String> products = getProduct(new ArrayList<>(new TreeSet<>(main.getProductNames())));

		if (!products.isEmpty())
			main.selectClientsWithFailedProduct(products);

	}

	public void saveGroupAction() {
		main.callSaveGroupDialog();
	}

	public void deleteGroupAction() {
		main.callDeleteGroupDialog();
	}

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
		if (menuItemsHost == null) {
			logging.info(this, "checkMenuItemsDisabling: menuItemsHost not yet enabled");
			return;
		}

		List<String> disabledClientMenuEntries = main.getPersistenceController().getDisabledClientMenuEntries();

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

		}

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

	}

	public void showIPAddressColumn(Boolean b) {

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

		backendInfoDialog.setVisible(true);
	}

	private void showAboutAction() {
		FTextArea info = new FTextArea(this, Globals.APPNAME + " Copyright Information", true, new String[] { "ok" },
				700, 300);

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
				Globals.APPNAME + " " + configed.getResourceValue("MainFrame.showLogFileInfoTitle"), false,
				new String[] { configed.getResourceValue("MainFrame.showLogFileCopyToClipboard"),
						configed.getResourceValue("MainFrame.showLogFileOpen"),
						configed.getResourceValue("MainFrame.showLogFileClose") },
				new Icon[] { null, Globals.createImageIcon("images/document-view16.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "") },
				Globals.WIDTH_INFO_LOG_FILE, Globals.HEIGHT_INFO_LOG_FILE) {
			@Override
			public void doAction1() {
				getTextComponent().copy();
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

		StringBuilder message = new StringBuilder();

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
		if (main.getPersistenceController().getOpsiLicensingInfoVersion()
				.equals(LicensingInfoMap.OPSI_LICENSING_INFO_VERSION_OLD)) {

			FTextArea f = new FTextArea(this, configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"),
					true);
			StringBuilder message = new StringBuilder();
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

		fEditPane.setVisible(true);
	}

	public void callOpsiLicensingInfo() {

		if (fDialogOpsiLicensingInfo == null) {

			fDialogOpsiLicensingInfo = new de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo(null, // owner frame
					// title
					configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), false, // modal

					new String[] { configed.getResourceValue("Dash.close"),

					},

					new Icon[] {

							Globals.createImageIcon("images/cancel16_small.png", "") },
					1, 900, 680, true, // lazylayout, i.e, we have a chance to define components and use them for the
					// layout
					null // addPanel predefined
			);
		} else
			fDialogOpsiLicensingInfo.setVisible(true);
	}

	// ----------------------------------------------------------------------------------------

	void jButtonSaveList_actionPerformed(ActionEvent e) {
		main.checkSaveAll(false);
	}

	// ComponentListener
	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	/* WindowListener implementation */
	@Override
	public void windowClosing(WindowEvent e) {
		main.finishApp(true, 0);
	}

	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	private Map<String, String> getChangedClientInfoFor(String client) {
		if (changedClientInfos == null)
			changedClientInfos = new HashMap<>();

		Map<String, String> changedClientInfo = changedClientInfos.get(client);

		if (changedClientInfo == null) {
			changedClientInfo = new HashMap<>();
			changedClientInfos.put(client, changedClientInfo);
		}

		return changedClientInfo;

	}

	protected void arrangeWs(Set<JDialog> frames) {
		// problem: https://bugs.openjdk.java.net/browse/JDK-7074504
		// Can iconify, but not deiconify a modal JDialog

		if (frames == null)
			return;

		int transpose = 20;

		for (java.awt.Window f : frames) {
			transpose = transpose + Globals.LINE_HEIGHT;

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
					changedClientInfo.put(HostInfo.CLIENT_DESCRIPTION_KEY, jTextFieldDescription.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_DESCRIPTION_KEY);
				}

			}

			else if (e.getSource() == jTextFieldInventoryNumber) {

				if (jTextFieldInventoryNumber.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, jTextFieldInventoryNumber.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else
					changedClientInfo.remove(HostInfo.CLIENT_INVENTORY_NUMBER_KEY);

			}

			else if (e.getSource() == jTextFieldOneTimePassword) {
				if (jTextFieldOneTimePassword.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY, jTextFieldOneTimePassword.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY);
				}

			}

			else if (e.getSource() == jTextAreaNotes) {
				if (!jTextAreaNotes.getText().equals(oldNotes)) {
					changedClientInfo.put(HostInfo.CLIENT_NOTES_KEY, jTextAreaNotes.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_NOTES_KEY);
				}

			}

			else if (e.getSource() == macAddressField) {

				// oldMacAddress

				logging.debug(this, " keyPressed on macAddressField, text " + macAddressField.getText());

				if (macAddressField.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macAddressField.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_MAC_ADRESS_KEY);

				}

			}

			else if (e.getSource() == ipAddressField) {

				// oldMacAddress

				logging.debug(this, " keyPressed on ipAddressField, text " + ipAddressField.getText());

				if (ipAddressField.isChangedText()) {
					changedClientInfo.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipAddressField.getText());
					main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
				} else {
					changedClientInfo.remove(HostInfo.CLIENT_IP_ADDRESS_KEY);

				}

			}

		}
	}

	// MouseListener implementation
	@Override
	public void mouseClicked(MouseEvent e) {
		logging.debug(this, "mouse clicked " + Arrays.toString(main.getSelectedClients()));

		reactToHostDataChange(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	// KeyListener implementation
	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		logging.debug(this, "key released " + Arrays.toString(main.getSelectedClients()));

		reactToHostDataChange(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	// ActionListener implementation
	@Override
	public void actionPerformed(ActionEvent e) {

		logging.debug(this, "actionPerformed on " + e.getSource());
		if (e.getSource() == cbInstallByShutdown) {
			logging.info(this, "actionPerformed on cbInstallByShutdown");

			for (String client : main.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY,
						((Boolean) cbInstallByShutdown.isSelected()).toString());

				main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}

		}

		else if (e.getSource() == cbUefiBoot) {
			logging.info(this, "actionPerformed on cbUefiBoot");

			for (String client : main.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.CLIENT_UEFI_BOOT_KEY, (cbUefiBoot.isSelected()).toString());

				main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			}
		}

		else if (e.getSource() == cbWANConfig) {
			logging.info(this, "actionPerformed on cbWANConfig");

			for (String client : main.getSelectedClients()) {
				Map<String, String> changedClientInfo = getChangedClientInfoFor(client);

				changedClientInfo.put(HostInfo.CLIENT_WAN_CONFIG_KEY, (cbWANConfig.isSelected()).toString());
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

				if (licenseDash == null) {
					licenseDash = new LicenseDash();
					licenseDash.initAndShowGUI();
				} else {
					licenseDash.show();
				}
			}

		}

		else if (e.getSource() == jButtonWorkOnGroups || e.getSource() == jMenuFrameWorkOnGroups) {
			main.handleGroupActionRequest();

		}

		else if (e.getSource() == jButtonWorkOnProducts || e.getSource() == jMenuFrameWorkOnProducts) {
			main.handleProductActionRequest();

		} else if (e.getSource() == jButtonDash) {
			main.initDashInfo();
		}

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
					initFX();
				} catch (IOException ioE) {
					logging.error(this, "Unable to open fxml file");
				}
			});
		}

		public void initFX() throws IOException {
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

	public void producePanelReinstmgr(String pcname, List images) {
		panelReinstmgr.startFor(pcname, images);
	}

	public void initHardwareInfo(List config) {
		if (showHardwareLogVersion2 == null) {
			showHardwareLogVersion2 = new de.uib.configed.gui.hwinfopage.PanelHWInfo(main) {
				@Override
				protected void reload() {
					super.reload();
					main.clearHwInfo();

					// otherwise we get a wait cursor only in table component
					main.resetView(ConfigedMain.VIEW_HARDWARE_INFO);
				}
			};
		}
		showHardwareLogVersion2.setHardwareConfig(config);
	}

	private void showHardwareInfo() {

		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_hardwareLog")),
				showHardwareLog);

		showHardwareLog.repaint();

	}

	public void setHardwareInfoNotPossible(String label1S, String label2S) {
		logging.info(this, "setHardwareInfoNotPossible " + label1S);

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
			controllerHWinfoMultiClients = new ControllerHWinfoMultiClients(main, main.getPersistenceController());
			showHardwareLogMultiClientReport = controllerHWinfoMultiClients.panel;
		}

		logging.info(this, "setHardwareInfoMultiClients " + clients.length);

		controllerHWinfoMultiClients.setFilter();
		showHardwareLog = showHardwareLogMultiClientReport;

		showHardwareInfo();

	}

	public void setHardwareInfo(Object hardwareInfo) {

		if (hardwareInfo == null)
			showHardwareLogVersion2.setHardwareInfo(null,
					configed.getResourceValue("MainFrame.NoHardwareConfiguration"));
		else
			showHardwareLogVersion2.setHardwareInfo((Map) hardwareInfo, null);

		showHardwareLog = showHardwareLogVersion2;
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

		// for testing commented out

		List<String> shutdownValueX = null;
		try {
			shutdownValueX = (List) main.getPersistenceController().getProductproperties(clientID, "opsi-client-agent")
					.get("on_shutdown_install");
		} catch (Exception ex) {
		}

		final List<String> shutdownValue = shutdownValueX;

		// for testing defined with fixed values

		final Boolean activate = wantActive;

		if (shutdownValue == null) {
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
					logging.info(this, "handle " + switchOn + ", old value for one client " + shutdownValue);

					main.setInstallByShutdownProductPropertyValue(switchOn);
					main.requestReloadStatesAndActions();

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
		PanelSWMultiClientReport showSoftwareLogMultiClientReport;
		PanelSWInfo panelSWInfo;

		SwExporter(PanelSWMultiClientReport showSoftwareLogMultiClientReport, PanelSWInfo panelSWInfo) {
			this.showSoftwareLogMultiClientReport = showSoftwareLogMultiClientReport;
			this.panelSWInfo = panelSWInfo;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			logging.info(this, "actionPerformed " + "  showSoftwareLog_MultiClientReport.wantsWithMsUpdates  "
					+ showSoftwareLogMultiClientReport.wantsWithMsUpdates());

			// save states now

			configed.savedStates.saveSWauditExportFilePrefix
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

			for (String client : main.getSelectedClients()) {
				Map<String, Map> tableData = main.getPersistenceController().retrieveSoftwareAuditData(client);
				if (tableData == null || tableData.isEmpty())
					clientsWithoutScan.add(client);

			}

			logging.info(this, "clientsWithoutScan " + clientsWithoutScan);

			for (String client : main.getSelectedClients()) {
				panelSWInfo.setHost(client);

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

			showSoftwareLog = showSoftwareLogMultiClientReport;
			showSoftwareAudit();

		} else
		// handled by the following methos
		{
			labelNoSoftware.setText(configed.getResourceValue("MainFrame.TabRequiresClientSelected"));
			showSoftwareLog = showSoftwareLogNotFound;
			showSoftwareAudit();
		}

	}

	public void setSoftwareAuditNullInfo(String hostId) {
		labelNoSoftware.setText(configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));
		panelSWInfo.setSoftwareNullInfo(hostId);
	}

	public void setSoftwareAudit(String hostId) {
		labelNoSoftware.setText(configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));

		logging.debug(this, "setSoftwareAudit for " + hostId);
		panelSWInfo.setAskingForKindOfAction(true);
		panelSWInfo.setAskForOverwrite(true);
		panelSWInfo.setHost(hostId);
		panelSWInfo.updateModel();

		showSoftwareLog = panelSWInfo;

		showSoftwareAudit();
	}

	public void setUpdatedLogfilePanel(String logtype) {
		logging.info(this, "setUpdatedLogfilePanel " + logtype);

		setLogfilePanel(main.getLogfilesUpdating(logtype));

	}

	public void setLogfilePanel(final Map<String, String> logs) {
		jTabbedPaneConfigPanes.setComponentAt(
				jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_logfiles")),
				showLogfiles);

		showLogfiles.setDocuments(logs, statusPane.getSelectedClientNames());

	}

	public void setLogview(String logtype) {
		int i = Arrays.asList(Globals.logtypes).indexOf(logtype);
		if (i < 0)
			return;

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
		logging.info(this, "setUefiBoot " + b);
		cbUefiBoot.setSelected(b);

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
					.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldInventoryNumber
					.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldOneTimePassword
					.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextAreaNotes.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
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
			logging.warning(this, "the ugly well known exception " + ex);
			WaitCursor.stopAll();
		}
	}

}
