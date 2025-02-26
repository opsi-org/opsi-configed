/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.uib.configed.ChangedDataManager;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ExtraFrameController;
import de.uib.configed.Globals;
import de.uib.configed.dashboard.LicenseDisplayer;
import de.uib.configed.messageoftheday.MessageOfTheDayDialog;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.tree.ProductTree;
import de.uib.messages.Messages;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsicommand.certificate.CertificateValidatorFactory;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserFeaturesConfig;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.FeatureActivationChecker;
import de.uib.utils.Icons;
import de.uib.utils.PopupMouseListener;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.userprefs.ThemeManager;
import de.uib.utils.userprefs.UserPreferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class MainFrame extends JFrame {
	private ConfigedMain configedMain;

	private JMenuItem jMenuFileSaveConfigurations;

	private ClientMenuManager clientMenu;

	private LeftControlBar leftControlBar;
	private LeftToolBar leftToolBar;

	private MainPanelManager mainPanelManager;

	private LicenseDisplayer licenseDisplayer;

	private ClientTablePanel clientTablePanel;

	private GlassPane glassPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public MainFrame(ConfigedMain configedMain, ClientTablePanel clientTablePanel, DepotsList depotsList,
			ClientTree clientTree, ProductTree productTree) {
		// we handle it in the window listener method
		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.clientTablePanel = clientTablePanel;
		this.configedMain = configedMain;

		guiInit(depotsList, clientTree, productTree);

		// We can add this listener as soon as the clientMenu has been created
		clientTablePanel.addMouseListener(new PopupMouseListener(clientMenu.getPopupMenuClone()));
	}

	private void guiInit(DepotsList depotsList, ClientTree clientTree, ProductTree productTree) {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				ConfigedMain.finishApp(true, 0);
			}
		});

		this.setIconImage(Icons.getMainIcon());

		setJMenuBar(initMenuBar());

		leftControlBar = new LeftControlBar();
		leftToolBar = new LeftToolBar(configedMain);
		mainPanelManager = new MainPanelManager(configedMain, this, depotsList, clientTree, productTree);

		showClientConfiguration();

		setTitle("(" + ConfigedMain.getUser() + ") " + ConfigedMain.getHost() + " - " + Globals.APPNAME);

		glassPane = new GlassPane();
		setGlassPane(glassPane);
	}

	public JTabbedPane getTabbedPane() {
		return mainPanelManager.getTabbedPane();
	}

	public ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
	}

	public ClientConfiguration getClientConfiguration() {
		return mainPanelManager.getClientConfiguration();
	}

	public HostsStatusPanel getHostsStatusPanel() {
		return mainPanelManager.getHostsStatusPanel();
	}

	// ------------------------------------------------------------------------------------------
	// configure interaction
	// ------------------------------------------------------------------------------------------
	// menus

	private JMenu createJMenuFile() {
		JMenu jMenuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));

		JMenuItem jMenuFileExit = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileExit"));
		Icons.addThemeIconToMenuItem(jMenuFileExit, "exit");
		jMenuFileExit.addActionListener(actionEvent -> ConfigedMain.finishApp(true, 0));

		jMenuFileSaveConfigurations = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileSaveConfigurations"));
		Icons.addIntellijIconToMenuItem(jMenuFileSaveConfigurations, "save");
		jMenuFileSaveConfigurations.setEnabled(false);
		jMenuFileSaveConfigurations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSaveConfigurations.addActionListener(actionEvent -> ChangedDataManager.checkSaveAll(false));

		JMenuItem jMenuFileReload = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		Icons.addIntellijIconToMenuItem(jMenuFileReload, "refresh");
		jMenuFileReload.addActionListener(actionEvent -> configedMain.reload());

		JMenuItem jMenuFileLogout = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileLogout"));
		Icons.addThemeIconToMenuItem(jMenuFileLogout, "exit");
		jMenuFileLogout.addActionListener(actionEvent -> logout());

		jMenuFile.add(jMenuFileSaveConfigurations);
		jMenuFile.add(jMenuFileReload);
		jMenuFile.add(Messages.createJMenuLanguages(MainFrame::restartConfiged));
		jMenuFile.add(createJMenuTheme(MainFrame::restartConfiged));
		jMenuFile.add(jMenuFileLogout);
		jMenuFile.add(jMenuFileExit);

		return jMenuFile;
	}

	public static JMenu createJMenuTheme(Runnable runnable) {
		JMenu jMenuTheme = new JMenu(Configed.getResourceValue("theme"));
		Icons.addThemeIconInvertedToMenuItem(jMenuTheme, "systemTheme");
		ButtonGroup groupThemes = new ButtonGroup();
		String selectedTheme = ThemeManager.getSelectedTheme();

		for (final String theme : ThemeManager.getAvailableThemes()) {
			JMenuItem themeItem = new JRadioButtonMenuItem(ThemeManager.getThemeTranslation(theme));
			ThemeManager.setThemeIcon(themeItem, theme);
			Logging.debug("selectedTheme ", theme);
			themeItem.setSelected(selectedTheme.equals(theme));
			jMenuTheme.add(themeItem);
			groupThemes.add(themeItem);

			themeItem.addActionListener((ActionEvent e) -> {
				UserPreferences.set(UserPreferences.THEME, theme);
				ThemeManager.setTheme(theme);
				ThemeManager.setOpsiLaf();

				runnable.run();
			});
		}

		return jMenuTheme;
	}

	private void logout() {
		ConfigedMain.setHost(null);
		ConfigedMain.setUser(null);
		ConfigedMain.setPassword(null);
		ConfigedMain.setUseSSO(false);
		CacheManager.getInstance().clearAllCachedData();
		Configed.getSavedStates().removeAll();
		resetData();

		// We need to reset the validators so that new ones will be created when reconnecting
		CertificateValidatorFactory.resetCertificateValidators();

		restartConfiged();
	}

	public void resetData() {
		mainPanelManager.resetData();

		licenseDisplayer = null;
	}

	public boolean checkSaveLicenses() {
		return mainPanelManager.checkSavedLicenses();
	}

	private static void restartConfiged() {
		ConfigedMain.closeInstance(true);
		new Thread() {
			@Override
			public void run() {
				Configed.startConfiged();
			}
		}.start();
	}

	public void reloadServerConsoleMenu() {
		leftToolBar.reloadServerConsoleMenu();
	}

	private JMenu createJMenuClientSelection() {
		JMenu jMenuClientselection = new JMenu(Configed.getResourceValue("MainFrame.jMenuClientselection"));

		JMenuItem jMenuClientselectionGetGroup = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jMenuClientselectionGetGroup
				.addActionListener(actionEvent -> ExtraFrameController.callClientSelectionDialog(configedMain));

		JMenuItem jMenuClientselectionGetSavedSearch = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		jMenuClientselectionGetSavedSearch
				.addActionListener(actionEvent -> ExtraFrameController.clientSelectionGetSavedSearch(configedMain));

		JMenuItem jMenuClientselectionProductNotUptodate = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersion"));
		jMenuClientselectionProductNotUptodate
				.addActionListener(actionEvent -> configedMain.getClientSearch().groupByNotCurrentProductVersion());

		JMenuItem jMenuClientselectionProductNotUptodateOrBroken = new JMenuItem(Configed
				.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersionOrUnknownState"));
		jMenuClientselectionProductNotUptodateOrBroken.addActionListener(
				actionEvent -> configedMain.getClientSearch().groupByNotCurrentProductVersionOrBrokenInstallation());

		JMenuItem jMenuClientselectionFailedProduct = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedForProduct"));
		jMenuClientselectionFailedProduct
				.addActionListener(actionEvent -> configedMain.getClientSearch().groupByFailedProduct());

		JMenu jMenuClientselectionFailedInPeriod = new JMenu(
				Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedInTimespan"));

		configedMain.getClientSearch().addSearchSpansToJMenu(jMenuClientselectionFailedInPeriod);

		jMenuClientselection.add(jMenuClientselectionGetGroup);
		jMenuClientselection.add(jMenuClientselectionGetSavedSearch);

		jMenuClientselection.addSeparator();

		jMenuClientselection.add(jMenuClientselectionProductNotUptodate);
		jMenuClientselection.add(jMenuClientselectionProductNotUptodateOrBroken);
		jMenuClientselection.add(jMenuClientselectionFailedProduct);
		jMenuClientselection.add(jMenuClientselectionFailedInPeriod);

		return jMenuClientselection;
	}

	private JMenu jMenuExtras() {
		JMenu jMenuExtras = new JMenu(Configed.getResourceValue("MainFrame.jMenuExtras"));

		JMenuItem jMenuWorkOnGroups = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuWorkOnGroups"));
		jMenuWorkOnGroups
				.setEnabled(persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.LOCAL_IMAGING));
		jMenuWorkOnGroups.addActionListener(event -> configedMain.handleGroupActionRequest());

		JMenuItem jMenuWorkOnProducts = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuWorkOnProducts"));
		jMenuWorkOnProducts.addActionListener(event -> ExtraFrameController.startProductActionFrame());

		jMenuExtras.add(jMenuWorkOnGroups);
		jMenuExtras.add(jMenuWorkOnProducts);

		JMenuItem jMenuFrameMsgOfTheDay = null;
		List<Object> forbiddenItemsMOTD = UserConfig.getCurrentUserConfig()
				.getValues(UserFeaturesConfig.KEY_MOTD_ACCESS_FORBIDDEN);
		boolean forbiddenMOTD = forbiddenItemsMOTD.contains(UserFeaturesConfig.KEY_OPT_MOTD_DEVICE)
				&& forbiddenItemsMOTD.contains(UserFeaturesConfig.KEY_OPT_MOTD_USER);

		if (ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast("4.3.15.2")
				&& FeatureActivationChecker.isFeatureActivated(FeatureActivationChecker.Feature.MESSAGE_OF_THE_DAY)) {
			jMenuFrameMsgOfTheDay = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFrameMessageOfTheDay"));
			jMenuFrameMsgOfTheDay.addActionListener((ActionEvent e) -> showMsgOfTheDay());

			jMenuFrameMsgOfTheDay.setEnabled(!forbiddenMOTD);
			if (forbiddenMOTD) {
				jMenuFrameMsgOfTheDay.setText(
						String.format("%s %s", Configed.getResourceValue("MainFrame.jMenuFrameMessageOfTheDay"),
								Configed.getResourceValue("MainFrame.jMenu.attribute.forbidden")));
			}

		}
		if (jMenuFrameMsgOfTheDay != null) {
			jMenuExtras.add(jMenuFrameMsgOfTheDay);
		}

		return jMenuExtras;
	}

	public static void addHelpLinks(JMenu jMenuHelp) {
		JMenuItem jMenuHelpDoc = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuDoc"));
		Icons.addOpsiIconToMenuItem(jMenuHelpDoc);
		jMenuHelpDoc.addActionListener(actionEvent -> Utils.showExternalDocument(Globals.OPSI_DOC_PAGE));
		jMenuHelp.add(jMenuHelpDoc);

		JMenuItem jMenuHelpForum = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuForum"));
		Icons.addOpsiIconToMenuItem(jMenuHelpForum);
		jMenuHelpForum.addActionListener(actionEvent -> Utils.showExternalDocument(Globals.OPSI_FORUM_PAGE));
		jMenuHelp.add(jMenuHelpForum);

		// Get the language used for the support page
		String language = "de".equals(Messages.getLocale().getLanguage()) ? "de" : "en";
		JMenuItem jMenuHelpSupport = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuSupport"));
		Icons.addOpsiIconToMenuItem(jMenuHelpSupport);
		jMenuHelpSupport
				.addActionListener(actionEvent -> Utils.showExternalDocument(Globals.UIB_PAGE + language + "/support"));
		jMenuHelp.add(jMenuHelpSupport);
	}

	private JMenu createJMenuHelp() {
		JMenu jMenuHelp = new JMenu(Configed.getResourceValue("MainFrame.jMenuHelp"));

		addHelpLinks(jMenuHelp);

		jMenuHelp.addSeparator();

		JMenuItem jMenuHelpOpsiVersion = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpOpsiService")
				+ ": " + ServerFacade.getOpsiServerVersionRetriever().getServerVersion());
		jMenuHelpOpsiVersion.setEnabled(false);

		jMenuHelp.add(jMenuHelpOpsiVersion);

		addLogfileMenus(jMenuHelp, this);

		jMenuHelp.addSeparator();

		addCreditsMenus(jMenuHelp, this);

		return jMenuHelp;
	}

	public static void addCreditsMenus(JMenu jMenuHelp, JFrame owner) {
		JMenuItem jMenuHelpCredits = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpCredits"));
		jMenuHelpCredits.addActionListener(actionEvent -> Utils.showCreditsAction(owner));
		jMenuHelp.add(jMenuHelpCredits);

		JMenuItem jMenuHelpAbout = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpAbout"),
				Icons.getIntellijIcon("info"));
		jMenuHelpAbout.addActionListener(actionEvent -> Utils.showAboutAction(owner));
		jMenuHelp.add(jMenuHelpAbout);
	}

	public static void addLogfileMenus(JMenu jMenuHelp, JFrame centerFrame) {
		JMenu jMenuHelpLoglevel = new JMenu(Configed.getResourceValue("loglevel"));

		JRadioButtonMenuItem[] rbLoglevelItems = new JRadioButtonMenuItem[Logging.LEVEL_SECRET + 1];
		ButtonGroup loglevelGroup = new ButtonGroup();

		for (int i = Logging.LEVEL_NONE; i <= Logging.LEVEL_SECRET; i++) {
			rbLoglevelItems[i] = new JRadioButtonMenuItem(
					"[" + i + "] " + Logging.LEVEL_TO_NAME.get(i).toLowerCase(Locale.ROOT));

			jMenuHelpLoglevel.add(rbLoglevelItems[i]);
			loglevelGroup.add(rbLoglevelItems[i]);

			if (i == Logging.getLogLevelConsole()) {
				rbLoglevelItems[i].setSelected(true);
			}

			final int loglevel = i;
			rbLoglevelItems[loglevel].addActionListener(e -> Logging.setLogLevel(loglevel));
		}

		jMenuHelp.add(jMenuHelpLoglevel);

		JMenuItem jMenuHelpLogfileLocation = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuHelpLogfileLocation"));
		jMenuHelpLogfileLocation.addActionListener(actionEvent -> showLogfileLocationAction(centerFrame));

		jMenuHelp.add(jMenuHelpLogfileLocation);
	}

	public void showDashboard() {
		showPanel(mainPanelManager.getDashBoardPanel());
	}

	public void showHealthCheckPanel() {
		showPanel(mainPanelManager.getHealthCheckPanel());
	}

	public void showClientConfiguration() {
		showPanel(mainPanelManager.getClientConfigurationPanel());
	}

	public void showDepotConfiguration() {
		showPanel(mainPanelManager.getDepotConfigurationSplitPane());
	}

	public void showServerConfiguration() {
		showPanel(mainPanelManager.getServerConfigurationPanel());
	}

	private void showPanel(JComponent panel) {
		getContentPane().removeAll();

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(leftControlBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE).addComponent(leftToolBar,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(panel));
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(leftControlBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(leftToolBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addComponent(panel).addGap(Globals.MIN_GAP_SIZE));
	}

	private JMenuBar initMenuBar() {
		clientMenu = ClientMenuManager.getNewInstance(configedMain, this);

		JMenuBar jMenuBar = new JMenuBar();
		jMenuBar.add(createJMenuFile());
		jMenuBar.add(createJMenuClientSelection());
		jMenuBar.add(clientMenu.getJMenu());

		jMenuBar.add(jMenuExtras());
		jMenuBar.add(createJMenuHelp());

		return jMenuBar;
	}

	public void saveConfigurationsSetEnabled(boolean b) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService().isGlobalReadOnly()
				&& b) {
			return;
		}

		Logging.debug(this, "saveConfigurationsSetEnabled ", b);

		jMenuFileSaveConfigurations.setEnabled(b);
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

	private static void showLogfileLocationAction(JFrame centerFrame) {
		JTextArea jTextArea = new JTextArea(Logging.getCurrentLogfilePath());
		jTextArea.setEditable(false);

		JButton buttonCopy = new JButton(Configed.getResourceValue("copy"));
		buttonCopy.addActionListener(e -> jTextArea.copy());

		JButton buttonOpen = new JButton(Configed.getResourceValue("MainFrame.showLogFileOpen"));
		buttonOpen.addActionListener((ActionEvent event) -> {
			try {
				Desktop.getDesktop().open(new File(Logging.getCurrentLogfilePath()));
			} catch (IOException ex) {
				Logging.error(ex, "cannot open: ", Logging.getCurrentLogfilePath());
			}
		});

		JOptionPane.showOptionDialog(centerFrame, jTextArea,
				Configed.getResourceValue("MainFrame.jMenuHelpLogfileLocation"), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null,
				new Object[] { Configed.getResourceValue("buttonClose"), buttonCopy, buttonOpen }, null);
	}

	private static void showMsgOfTheDay() {
		new MessageOfTheDayDialog();
	}

	public void showHealthDataAction() {
		if (!persistenceController.getHealthDataService().isHealthDataAlreadyLoaded()) {
			activateLoadingPane(Configed.getResourceValue("HealthCheckDialog.loadData"));
		}

		new HealthCheckDataLoader().execute();
	}

	public void showOpsiModules() {
		if (!persistenceController.getModuleDataService().isOpsiUserAdminPD()) {
			Map<String, Object> modulesInfo = persistenceController.getModuleDataService().getOpsiModulesInfosPD();

			StringJoiner message = new StringJoiner("\n");
			for (Entry<String, Object> modulesInfoEntry : modulesInfo.entrySet()) {
				message.add(modulesInfoEntry.getKey() + ": " + modulesInfoEntry.getValue());
			}

			JTextArea textArea = new JTextArea(message.toString());
			textArea.setEditable(false);

			JOptionPane.showMessageDialog(this, textArea,
					Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), JOptionPane.PLAIN_MESSAGE);
		} else {
			showPanel(mainPanelManager.getOpsiLicensingPanel());
		}
	}

	public void startLicensingManagement() {
		Logging.info(this, "startLicensingManagement called");

		new Thread() {
			@Override
			public void run() {
				persistenceController.getModuleDataService().retrieveOpsiModules();

				if (persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
					Logging.info(this, "show licensing pane");
					showPanel(mainPanelManager.getLicenseManagementPanel());
				} else {
					Utils.showMissingLicenseModules(
							Configed.getResourceValue("ConfigedMain.LicensemanagementNotActive"));
				}

				if (Boolean.TRUE.equals(persistenceController.getConfigDataService().getGlobalBooleanConfigValue(
						OpsiServiceNOMPersistenceController.KEY_SHOW_DASH_FOR_LICENSEMANAGEMENT,
						OpsiServiceNOMPersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENSEMANAGEMENT))) {
					// Starting JavaFX-Thread by creating a new JFXPanel, but not
					// using it since it is not needed.
					new JFXPanel();

					Platform.runLater(() -> showLicenseDisplayer());
				}

				deactivateLoadingPane();
			}
		}.start();
	}

	private void showLicenseDisplayer() {
		if (licenseDisplayer == null) {
			try {
				licenseDisplayer = new LicenseDisplayer();
				licenseDisplayer.setConfigedMain(configedMain);
				licenseDisplayer.initAndShowGUI();
			} catch (IOException ioE) {
				Logging.warning(this, ioE, "Unable to open FXML file.");
			}
		} else {
			licenseDisplayer.display();
		}
	}
}
