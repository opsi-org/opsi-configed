/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.FCreditsDialog;
import de.uib.configed.Globals;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.configed.tree.ClientTree;
import de.uib.messages.Messages;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsicommand.terminalcommand.TerminalCommandCurl;
import de.uib.opsicommand.terminalcommand.TerminalCommandDeployClientAgent;
import de.uib.opsicommand.terminalcommand.TerminalCommandModulesUpload;
import de.uib.opsicommand.terminalcommand.TerminalCommandOpsiMakeProductFile;
import de.uib.opsicommand.terminalcommand.TerminalCommandOpsiPackageManagerInstall;
import de.uib.opsicommand.terminalcommand.TerminalCommandOpsiPackageManagerUninstall;
import de.uib.opsicommand.terminalcommand.TerminalCommandOpsiSetRights;
import de.uib.opsicommand.terminalcommand.TerminalCommandPackageUpdater;
import de.uib.opsidatamodel.modulelicense.LicensingInfoDialog;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserSshConfig;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.savedstates.UserPreferences;
import utils.Utils;

public class MainFrame extends JFrame {
	private static final int DIVIDER_LOCATION_CENTRAL_PANE = 300;
	private static final int MIN_WIDTH_TREE_PANEL = 150;

	public static final int F_WIDTH = 800;

	private static final int F_WIDTH_RIGHTHANDED = 200;

	private static final int DIVIDER_LOCATION_CLIENT_TREE_MULTI_DEPOT = 200;
	private static final int DIVIDER_LOCATION_CLIENT_TREE_SINGLE_DEPOT = 50;

	private ConfigedMain configedMain;

	private JMenuItem jMenuFileSaveConfigurations;

	private ClientMenuManager clientMenu;

	private JMenu jMenuServer = new JMenu();
	private JMenu jMenuTerminal = new JMenu();

	private JMenuItem jMenuSSHConnection = new JMenuItem();

	private Map<String, String> searchedTimeSpans;
	private Map<String, String> searchedTimeSpansText;

	private JMenuItem jMenuFrameLicenses;
	private JMenuItem jMenuFrameShowDialogs;

	private TabbedConfigPanes jTabbedPaneConfigPanes;

	private HostsStatusPanel statusPane;

	private LicensingInfoDialog fDialogOpsiLicensingInfo;

	private ClientTable clientTable;

	private GlassPane glassPane;

	private boolean multidepot;

	private DepotListPresenter depotListPresenter;

	private ClientTree treeClients;

	private IconBarPanel iconBarPanel;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public MainFrame(ConfigedMain main, ClientTable panelClientlist, DepotsList depotsList, ClientTree treeClients) {
		// we handle it in the window listener method
		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.multidepot = persistenceController.getHostInfoCollections().getDepots().size() != 1;

		this.clientTable = panelClientlist;

		this.treeClients = treeClients;

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

	public boolean isMultiDepot() {
		return multidepot;
	}

	public ClientTable getClientTable() {
		return clientTable;
	}

	public IconBarPanel getIconBarPanel() {
		return iconBarPanel;
	}

	public ClientMenuManager getClientMenu() {
		return clientMenu;
	}

	public TabbedConfigPanes getTabbedConfigPanes() {
		return jTabbedPaneConfigPanes;
	}

	public HostsStatusPanel getHostsStatusPanel() {
		return statusPane;
	}

	// ------------------------------------------------------------------------------------------
	// configure interaction
	// ------------------------------------------------------------------------------------------
	// menus

	private JMenu createJMenuFile() {
		JMenu jMenuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));

		JMenuItem jMenuFileExit = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileExit"));
		jMenuFileExit.addActionListener((ActionEvent e) -> configedMain.finishApp(true, 0));

		jMenuFileSaveConfigurations = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileSaveConfigurations"));
		jMenuFileSaveConfigurations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSaveConfigurations.addActionListener((ActionEvent e) -> configedMain.checkSaveAll(false));

		JMenuItem jMenuFileReload = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileReload"));

		jMenuFileReload.addActionListener((ActionEvent e) -> {
			configedMain.reload();
			if (iconBarPanel.getIconButtonReloadLicenses().isEnabled()) {
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

		return jMenuFile;
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

		jMenuServer.add(menuOpsi);
		addSSHCommandsToMenuServer(menuOpsi, commandsAreDeactivated);
		if (menuOpsi.getSubElements().length != 0) {
			menuOpsi.addSeparator();
		}
		addSSHCommandsToMenuOpsi(menuOpsi, commandsAreDeactivated);

		Logging.info(this, "setupMenuServer create/read command menu configs");

		boolean userConfigExists = UserConfig.getCurrentUserConfig() != null;

		jMenuSSHConfig.setEnabled(userConfigExists
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_CONFIG_ACTIVE));

		Logging.info(this, "setupMenuServer create/read command menu configs current user config "
				+ UserConfig.getCurrentUserConfig());
		jMenuSSHCommandControl.setEnabled(userConfigExists
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_COMMANDCONTROL_ACTIVE));

		jMenuSSHCommandControl.setEnabled(true);

		jMenuServer.setEnabled(userConfigExists
				&& !PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
						.isGlobalReadOnly()
				&& UserConfig.getCurrentUserConfig().getBooleanValue(UserSshConfig.KEY_SSH_MENU_ACTIVE));
	}

	private void addSSHCommandsToMenuOpsi(JMenu menuOpsi, boolean commandsAreDeactivated) {
		final SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);
		List<SSHCommand> commands = factory.getSSHCommandParameterList();
		for (final SSHCommand command : commands) {
			JMenuItem jMenuOpsiCommand = new JMenuItem();
			jMenuOpsiCommand.setText(command.getMenuText());
			jMenuOpsiCommand.setToolTipText(command.getToolTipText());
			jMenuOpsiCommand.addActionListener((ActionEvent e) -> jMenuOptionCommandAction(factory, command));
			menuOpsi.add(jMenuOpsiCommand);
			jMenuOpsiCommand.setEnabled(!PersistenceControllerFactory.getPersistenceController()
					.getUserRolesConfigDataService().isGlobalReadOnly() && !commandsAreDeactivated);
		}
	}

	private void addSSHCommandsToMenuServer(JMenu menuOpsi, boolean commandsAreDeactivated) {
		final SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);
		Map<String, List<SSHCommandTemplate>> sortedComs = factory.getSSHCommandMapSortedByParent();

		Logging.debug(this, "setupMenuServer add commands to menu commands sortedComs " + sortedComs);
		boolean firstParentGroup = true;
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

			addSSHSubCommands(menuOpsi, parentMenu, listCom, commandsAreDeactivated);
			if (firstParentGroup && !listCom.isEmpty()) {
				jMenuServer.addSeparator();
			}

			firstParentGroup = false;
		}
	}

	private void addSSHSubCommands(JMenu menuOpsi, JMenu parentMenu, List<SSHCommandTemplate> listCom,
			boolean commandsAreDeactivated) {
		final SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);
		for (final SSHCommandTemplate com : listCom) {
			JMenuItem jMenuItem = new JMenuItem();
			jMenuItem.setText(com.getMenuText());
			Logging.info(this, "ssh command menuitem text " + com.getMenuText());
			jMenuItem.setToolTipText(com.getToolTipText());
			jMenuItem.addActionListener((ActionEvent e) -> jMenuItemAction(factory, com));

			String parentMenuName = parentMenu.getText();
			if (parentMenuName.equals(SSHCommandFactory.PARENT_NULL)) {
				jMenuServer.add(jMenuItem);
			} else {
				if (parentMenuName.equals(SSHCommandFactory.PARENT_OPSI)) {
					menuOpsi.add(jMenuItem);
				} else {
					parentMenu.add(jMenuItem);
					jMenuServer.add(parentMenu);
				}
			}
			jMenuItem.setEnabled(!PersistenceControllerFactory.getPersistenceController()
					.getUserRolesConfigDataService().isGlobalReadOnly() && !commandsAreDeactivated);
		}
	}

	private void setupMenuTerminal() {
		jMenuTerminal.removeAll();
		jMenuTerminal.setText("Terminal");
		jMenuTerminal.add(jMenuTerminal);

		TerminalCommandOpsiSetRights opsiSetRightsCommand = new TerminalCommandOpsiSetRights();
		JMenuItem opsiSetRightsCommandMenuItem = new JMenuItem();
		opsiSetRightsCommandMenuItem.setText(opsiSetRightsCommand.getMenuText());
		opsiSetRightsCommandMenuItem.setToolTipText(opsiSetRightsCommand.getToolTipText());
		opsiSetRightsCommandMenuItem
				.addActionListener((ActionEvent e) -> opsiSetRightsCommand.startParameterGui(configedMain));
		jMenuTerminal.add(opsiSetRightsCommandMenuItem);

		TerminalCommandCurl curlCommand = new TerminalCommandCurl();
		JMenuItem curlCommandMenuItem = new JMenuItem();
		curlCommandMenuItem.setText(curlCommand.getMenuText());
		curlCommandMenuItem.setToolTipText(curlCommand.getToolTipText());
		curlCommandMenuItem.addActionListener((ActionEvent e) -> curlCommand.startParameterGui(configedMain));
		jMenuTerminal.add(curlCommandMenuItem);

		TerminalCommandModulesUpload modulesUploadCommand = new TerminalCommandModulesUpload();
		JMenuItem modulesUploadCommandMenuItem = new JMenuItem();
		modulesUploadCommandMenuItem.setText(modulesUploadCommand.getMenuText());
		modulesUploadCommandMenuItem.setToolTipText(modulesUploadCommand.getToolTipText());
		modulesUploadCommandMenuItem
				.addActionListener((ActionEvent e) -> modulesUploadCommand.startParameterGui(configedMain));
		jMenuTerminal.add(modulesUploadCommandMenuItem);

		TerminalCommandOpsiPackageManagerInstall opsiPackageManagerInstallCommand = new TerminalCommandOpsiPackageManagerInstall();
		JMenuItem opsiPackageManagerInstallCommandMenuItem = new JMenuItem();
		opsiPackageManagerInstallCommandMenuItem.setText(opsiPackageManagerInstallCommand.getMenuText());
		opsiPackageManagerInstallCommandMenuItem.setToolTipText(opsiPackageManagerInstallCommand.getToolTipText());
		opsiPackageManagerInstallCommandMenuItem
				.addActionListener((ActionEvent e) -> opsiPackageManagerInstallCommand.startParameterGui(configedMain));
		jMenuTerminal.add(opsiPackageManagerInstallCommandMenuItem);

		TerminalCommandOpsiPackageManagerUninstall opsiPackageManagerUninstallCommand = new TerminalCommandOpsiPackageManagerUninstall();
		JMenuItem opsiPackageManagerUninstallCommandMenuItem = new JMenuItem();
		opsiPackageManagerUninstallCommandMenuItem.setText(opsiPackageManagerUninstallCommand.getMenuText());
		opsiPackageManagerUninstallCommandMenuItem.setToolTipText(opsiPackageManagerUninstallCommand.getToolTipText());
		opsiPackageManagerUninstallCommandMenuItem.addActionListener(
				(ActionEvent e) -> opsiPackageManagerUninstallCommand.startParameterGui(configedMain));
		jMenuTerminal.add(opsiPackageManagerUninstallCommandMenuItem);

		TerminalCommandOpsiMakeProductFile opsiMakeProductFileCommand = new TerminalCommandOpsiMakeProductFile();
		JMenuItem opsiMakeProductFileCommandMenuItem = new JMenuItem();
		opsiMakeProductFileCommandMenuItem.setText(opsiMakeProductFileCommand.getMenuText());
		opsiMakeProductFileCommandMenuItem.setToolTipText(opsiMakeProductFileCommand.getToolTipText());
		opsiMakeProductFileCommandMenuItem
				.addActionListener((ActionEvent e) -> opsiMakeProductFileCommand.startParameterGui(configedMain));
		jMenuTerminal.add(opsiMakeProductFileCommandMenuItem);

		TerminalCommandDeployClientAgent deployClientAgentCommand = new TerminalCommandDeployClientAgent();
		JMenuItem deployClientAgentCommandMenuItem = new JMenuItem();
		deployClientAgentCommandMenuItem.setText(deployClientAgentCommand.getMenuText());
		deployClientAgentCommandMenuItem.setToolTipText(deployClientAgentCommand.getToolTipText());
		deployClientAgentCommandMenuItem
				.addActionListener((ActionEvent e) -> deployClientAgentCommand.startParameterGui(configedMain));
		jMenuTerminal.add(deployClientAgentCommandMenuItem);

		TerminalCommandPackageUpdater packageUpdaterCommand = new TerminalCommandPackageUpdater();
		JMenuItem packageUpdaterCommandMenuItem = new JMenuItem();
		packageUpdaterCommandMenuItem.setText(packageUpdaterCommand.getMenuText());
		packageUpdaterCommandMenuItem.setToolTipText(packageUpdaterCommand.getToolTipText());
		packageUpdaterCommandMenuItem
				.addActionListener((ActionEvent e) -> packageUpdaterCommand.startParameterGui(configedMain));
		jMenuTerminal.add(packageUpdaterCommandMenuItem);
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

	private JMenu createJMenuClientSelection() {
		JMenu jMenuClientselection = new JMenu(Configed.getResourceValue("MainFrame.jMenuClientselection"));

		JMenuItem jMenuClientselectionGetGroup = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jMenuClientselectionGetGroup.addActionListener((ActionEvent e) -> configedMain.callClientSelectionDialog());

		JMenuItem jMenuClientselectionGetSavedSearch = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		jMenuClientselectionGetSavedSearch
				.addActionListener((ActionEvent e) -> configedMain.clientSelectionGetSavedSearch());

		JMenuItem jMenuClientselectionProductNotUptodate = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersion"));
		jMenuClientselectionProductNotUptodate.addActionListener((ActionEvent e) -> groupByNotCurrentProductVersion());

		JMenuItem jMenuClientselectionProductNotUptodateOrBroken = new JMenuItem(Configed
				.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersionOrUnknownState"));
		jMenuClientselectionProductNotUptodateOrBroken
				.addActionListener((ActionEvent e) -> groupByNotCurrentProductVersionOrBrokenInstallation());

		JMenuItem jMenuClientselectionFailedProduct = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedForProduct"));
		jMenuClientselectionFailedProduct.addActionListener((ActionEvent e) -> groupByFailedProduct());

		JMenu jMenuClientselectionFailedInPeriod = new JMenu(
				Configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedInTimespan"));

		for (Entry<String, String> entry : searchedTimeSpansText.entrySet()) {
			JMenuItem item = new JMenuItem(entry.getValue());

			item.addActionListener((ActionEvent e) -> configedMain
					.selectClientsByFailedAtSomeTimeAgo(searchedTimeSpans.get(entry.getKey())));

			jMenuClientselectionFailedInPeriod.add(item);
		}

		JCheckBoxMenuItem jMenuClientselectionToggleClientFilter = new JCheckBoxMenuItem(
				Configed.getResourceValue("MainFrame.jMenuClientselectionToggleClientFilter"));
		jMenuClientselectionToggleClientFilter.setState(false);
		jMenuClientselectionToggleClientFilter.addActionListener((ActionEvent e) -> toggleClientFilterAction());

		jMenuClientselection.add(jMenuClientselectionGetGroup);
		jMenuClientselection.add(jMenuClientselectionGetSavedSearch);

		jMenuClientselection.addSeparator();

		jMenuClientselection.add(jMenuClientselectionProductNotUptodate);
		jMenuClientselection.add(jMenuClientselectionProductNotUptodateOrBroken);
		jMenuClientselection.add(jMenuClientselectionFailedProduct);
		jMenuClientselection.add(jMenuClientselectionFailedInPeriod);

		jMenuClientselection.addSeparator();
		jMenuClientselection.add(jMenuClientselectionToggleClientFilter);

		return jMenuClientselection;
	}

	private JMenu createJMenuFrames() {
		JMenu jMenuFrames = new JMenu(Configed.getResourceValue("MainFrame.jMenuFrames"));

		JMenuItem jMenuFrameWorkOnGroups = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFrameWorkOnGroups"));
		jMenuFrameWorkOnGroups.setEnabled(persistenceController.getModuleDataService().isWithLocalImagingPD());
		jMenuFrameWorkOnGroups.addActionListener(event -> configedMain.handleGroupActionRequest());

		JMenuItem jMenuFrameWorkOnProducts = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuFrameWorkOnProducts"));
		jMenuFrameWorkOnProducts.addActionListener(event -> configedMain.handleProductActionRequest());

		JMenuItem jMenuFrameDashboard = new JMenuItem(Configed.getResourceValue("Dashboard.title"));
		jMenuFrameDashboard.addActionListener(event -> configedMain.initDashInfo());

		jMenuFrameLicenses = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFrameLicenses"));
		jMenuFrameLicenses.setEnabled(false);
		jMenuFrameLicenses.addActionListener(event -> configedMain.handleLicensesManagementRequest());

		jMenuFrameShowDialogs = ClientMenuManager.createArrangeWindowsMenuItem();

		JMenuItem jMenuFrameTerminal = new JMenuItem(Configed.getResourceValue("Terminal.title"));
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

		return jMenuFrames;
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

	private JMenu createJMenuHelp() {
		JMenu jMenuHelp = new JMenu(Configed.getResourceValue("MainFrame.jMenuHelp"));

		addHelpLinks(jMenuHelp);

		jMenuHelp.addSeparator();

		JMenuItem jMenuHelpOpsiVersion = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuHelpOpsiService") + ": " + ServerFacade.getServerVersion());
		jMenuHelpOpsiVersion.setEnabled(false);

		jMenuHelp.add(jMenuHelpOpsiVersion);

		JMenuItem jMenuHelpOpsiModuleInformation = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jMenuHelpOpsiModuleInformation.addActionListener((ActionEvent e) -> showOpsiModules());

		jMenuHelp.add(jMenuHelpOpsiModuleInformation);

		JMenuItem jMenuHelpInternalConfiguration = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuHelpInternalConfiguration"));
		jMenuHelpInternalConfiguration.addActionListener((ActionEvent e) -> showBackendConfigurationAction());

		if (!ServerFacade.isOpsi43()) {
			jMenuHelp.add(jMenuHelpInternalConfiguration);
		}

		addLogfileMenus(jMenuHelp, this);

		JMenuItem jMenuHelpCheckHealth = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		jMenuHelpCheckHealth.addActionListener((ActionEvent e) -> showHealthDataAction());

		if (ServerFacade.isOpsi43()) {
			jMenuHelp.add(jMenuHelpCheckHealth);
		}

		jMenuHelp.addSeparator();

		JMenuItem jMenuHelpCredits = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpCredits"));
		jMenuHelpCredits.addActionListener((ActionEvent e) -> FCreditsDialog.display(this));
		jMenuHelp.add(jMenuHelpCredits);

		JMenuItem jMenuHelpAbout = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpAbout"));
		jMenuHelpAbout.addActionListener((ActionEvent e) -> Utils.showAboutAction(this));
		jMenuHelp.add(jMenuHelpAbout);

		return jMenuHelp;
	}

	public static void addLogfileMenus(JMenu jMenuHelp, JFrame centerFrame) {
		JMenu jMenuHelpLoglevel = new JMenu(Configed.getResourceValue("MainFrame.jMenuLoglevel"));

		JRadioButtonMenuItem[] rbLoglevelItems = new JRadioButtonMenuItem[Logging.LEVEL_SECRET + 1];
		ButtonGroup loglevelGroup = new ButtonGroup();

		for (int i = Logging.LEVEL_NONE; i <= Logging.LEVEL_SECRET; i++) {
			rbLoglevelItems[i] = new JRadioButtonMenuItem(
					"[" + i + "] " + Logging.levelText(i).toLowerCase(Locale.ROOT));

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
		jMenuHelpLogfileLocation.addActionListener((ActionEvent e) -> showLogfileLocationAction(centerFrame));

		jMenuHelp.add(jMenuHelpLogfileLocation);
	}

	private void guiInit() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				configedMain.finishApp(true, 0);
			}
		});

		this.setIconImage(Utils.getMainIcon());

		setJMenuBar(initMenuBar());

		JPanel allPanel = new JPanel();
		JSplitPane centralPane = initCentralPane();
		statusPane = new HostsStatusPanel();
		iconBarPanel = new IconBarPanel(configedMain, this);
		allPanel.setLayout(new BorderLayout());
		allPanel.add(iconBarPanel, BorderLayout.NORTH);
		allPanel.add(centralPane, BorderLayout.CENTER);
		allPanel.add(statusPane, BorderLayout.SOUTH);
		allPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Logging.debug(this, "componentResized");
				moveDivider1(jTabbedPaneConfigPanes.getPanelClientSelection(),
						jTabbedPaneConfigPanes.getClientInfoPanel(), (int) (F_WIDTH_RIGHTHANDED * 0.2), 200,
						(int) (F_WIDTH_RIGHTHANDED * 1.5));
				Logging.debug(this, "componentResized ready");
			}
		});
		getContentPane().add(allPanel);

		setTitle(configedMain.getAppTitle());

		glassPane = new GlassPane();
		setGlassPane(glassPane);
	}

	private JMenuBar initMenuBar() {
		initMenuData();

		clientMenu = ClientMenuManager.getNewInstance(configedMain, this);
		setupMenuServer();
		setupMenuTerminal();

		JMenuBar jMenuBar = new JMenuBar();
		jMenuBar.add(createJMenuFile());
		jMenuBar.add(createJMenuClientSelection());
		jMenuBar.add(clientMenu.getJMenu());
		jMenuBar.add(jMenuServer);
		jMenuBar.add(jMenuTerminal);
		jMenuBar.add(createJMenuFrames());
		jMenuBar.add(createJMenuHelp());

		return jMenuBar;
	}

	private JSplitPane initCentralPane() {
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

		JSplitPane splitpaneClientSelection = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
				depotListPresenter.getScrollpaneDepotslist(), scrollpaneTreeClients);

		Logging.info(this, "multidepot " + multidepot);
		if (multidepot) {
			splitpaneClientSelection.setDividerLocation(DIVIDER_LOCATION_CLIENT_TREE_MULTI_DEPOT);
		} else {
			splitpaneClientSelection.setDividerLocation(DIVIDER_LOCATION_CLIENT_TREE_SINGLE_DEPOT);
		}

		JPanel panelTreeClientSelection = new JPanel();
		GroupLayout layoutPanelTreeClientSelection = new GroupLayout(panelTreeClientSelection);
		panelTreeClientSelection.setLayout(layoutPanelTreeClientSelection);

		layoutPanelTreeClientSelection
				.setHorizontalGroup(layoutPanelTreeClientSelection.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addGroup(layoutPanelTreeClientSelection.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(depotListPresenter, MIN_WIDTH_TREE_PANEL, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(splitpaneClientSelection, MIN_WIDTH_TREE_PANEL,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

		layoutPanelTreeClientSelection
				.setVerticalGroup(layoutPanelTreeClientSelection.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(depotListPresenter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(splitpaneClientSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE));

		jTabbedPaneConfigPanes = new TabbedConfigPanes(configedMain, this);
		JSplitPane centralPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, panelTreeClientSelection,
				jTabbedPaneConfigPanes);
		centralPane.setDividerLocation(DIVIDER_LOCATION_CENTRAL_PANE);

		return centralPane;
	}

	// -- helper methods for interaction
	public void saveConfigurationsSetEnabled(boolean b) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService().isGlobalReadOnly()
				&& b) {
			return;
		}

		Logging.debug(this, "saveConfigurationsSetEnabled " + b);

		jMenuFileSaveConfigurations.setEnabled(b);
		iconBarPanel.getIconButtonSaveConfiguration().setEnabled(b);
	}

	// ----------------------------------------------------------------------------------------
	// action methods for visual interactions

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
		clientMenu.getClientSelectionToggleFilterMenu().setState(configedMain.isFilterClientList());
		iconBarPanel.getIconButtonToggleClientFilter().setSelected(configedMain.isFilterClientList());
		clientTable.setFilterMark(configedMain.isFilterClientList());
	}

	private void groupByNotCurrentProductVersion() {
		String products = getProduct(new ArrayList<>(
				new TreeSet<>(persistenceController.getProductDataService().getAllLocalbootProductNames())));
		configedMain.selectClientsNotCurrentProductInstalled(products, false);
	}

	private void groupByNotCurrentProductVersionOrBrokenInstallation() {
		String products = getProduct(new ArrayList<>(
				new TreeSet<>(persistenceController.getProductDataService().getAllLocalbootProductNames())));
		configedMain.selectClientsNotCurrentProductInstalled(products, true);
	}

	private void groupByFailedProduct() {
		String products = getProduct(new ArrayList<>(
				new TreeSet<>(persistenceController.getProductDataService().getAllLocalbootProductNames())));
		configedMain.selectClientsWithFailedProduct(products);
	}

	private String getProduct(List<String> completeList) {
		FSelectionList fProductSelectionList = new FSelectionList(this,
				Configed.getResourceValue("MainFrame.productSelection"), true, new String[] { "", "" }, new Icon[] {
						Utils.createImageIcon("images/cancel.png", ""), Utils.createImageIcon("images/apply.png", "") },
				F_WIDTH / 2, 600, "productselection");
		fProductSelectionList.setListData(completeList);
		fProductSelectionList.setVisible(true);
		return fProductSelectionList.getResult() == 2 ? fProductSelectionList.getSelectedValue() : "";
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

	protected void reloadLicensesAction() {
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

	protected void showOpsiModules() {
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

	public void instancesChanged(Set<?> instances) {
		boolean existJDialogInstances = instances != null && !instances.isEmpty();

		clientMenu.instancesChanged(existJDialogInstances);

		jMenuFrameShowDialogs.setEnabled(existJDialogInstances);
	}

	public void enableAfterLoading() {
		iconBarPanel.enableAfterLoading();
		jMenuFrameLicenses.setEnabled(true);
	}

	public LicensingInfoDialog getFDialogOpsiLicensingInfo() {
		return fDialogOpsiLicensingInfo;
	}
}
