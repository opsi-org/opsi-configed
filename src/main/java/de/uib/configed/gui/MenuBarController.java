/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import de.uib.configed.core.domain.permission.UserFeaturesConfig;
import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.ServerFacade;
import de.uib.configed.gui.ConfigedMain.EditingTarget;
import de.uib.configed.gui.features.messageoftheday.MessageOfTheDayDialog;
import de.uib.configed.gui.messages.Messages;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.BrowserUtils;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.userprefs.ThemeManager;
import de.uib.configed.share.userprefs.UserPreferences;

public class MenuBarController {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;
	private LeftControlBar leftControlBar;

	private ClientMenuManager clientMenu;
	private JMenuItem jMenuFileSaveConfigurations;

	public MenuBarController(ConfigedMain configedMain, LeftControlBar leftControlBar) {
		this.configedMain = configedMain;
		this.leftControlBar = leftControlBar;
	}

	public JMenu createJMenuFile() {
		JMenu jMenuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));

		JMenuItem jMenuFileExit = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileExit"));
		Icons.addThemeIconToMenuItem(jMenuFileExit, "exit");
		jMenuFileExit.addActionListener(actionEvent -> configedMain.saveAndQuit());

		jMenuFileSaveConfigurations = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileSaveConfigurations"));
		Icons.addIntellijIconToMenuItem(jMenuFileSaveConfigurations, "save");
		jMenuFileSaveConfigurations.setEnabled(false);
		jMenuFileSaveConfigurations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSaveConfigurations.addActionListener(actionEvent -> ChangedDataManager.checkSaveAll(false));

		JMenuItem jMenuFileReload = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		Icons.addIntellijIconToMenuItem(jMenuFileReload, "refresh");
		jMenuFileReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.CTRL_DOWN_MASK));
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

	private JMenu createJMenuView() {
		JMenu jMenuView = new JMenu(Configed.getResourceValue("MainFrame.jMenuView"));

		JMenuItem jMenuViewClientsConfiguration = new JMenuItem(
				Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
		jMenuViewClientsConfiguration.addActionListener(event -> leftControlBar.selectView(EditingTarget.CLIENTS));
		jMenuViewClientsConfiguration.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));
		Icons.addIntellijIconToMenuItem(jMenuViewClientsConfiguration, "desktop");

		JMenuItem jMenuViewDepotConfiguration = new JMenuItem(Configed.getResourceValue("depotConfiguration"));
		jMenuViewDepotConfiguration.addActionListener(event -> leftControlBar.selectView(EditingTarget.DEPOTS));
		jMenuViewDepotConfiguration.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));
		Icons.addIntellijIconToMenuItem(jMenuViewDepotConfiguration, "dbms");

		JMenuItem jMenuViewServerConfiguration = new JMenuItem(
				Configed.getResourceValue("MainFrame.labelServerConfiguration"));
		jMenuViewServerConfiguration.addActionListener(event -> leftControlBar.selectView(EditingTarget.SERVER));
		jMenuViewServerConfiguration.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK));
		Icons.addIntellijIconToMenuItem(jMenuViewServerConfiguration, "settings");

		JMenuItem jMenuViewDashboard = new JMenuItem(Configed.getResourceValue("Dashboard.title"));
		jMenuViewDashboard.addActionListener(event -> leftControlBar.selectView(EditingTarget.DASHBOARD));
		jMenuViewDashboard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK));
		Icons.addIntellijIconToMenuItem(jMenuViewDashboard, "dataSchema");

		JMenuItem jMenuViewOpsiModuleInformation = new JMenuItem(
				Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), Icons.getOpsiModulesIcon(16));
		jMenuViewOpsiModuleInformation.setSelectedIcon(Icons.getSelectedOpsiModulesIcon(16));
		jMenuViewOpsiModuleInformation
				.addActionListener(event -> leftControlBar.selectView(EditingTarget.OPSI_MODULES));
		jMenuViewOpsiModuleInformation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK));

		JMenuItem jMenuViewHealthCheck = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		Icons.addSelectedHealthCheckIcon(jMenuViewHealthCheck, 16);
		jMenuViewHealthCheck.addActionListener(event -> leftControlBar.selectView(EditingTarget.HEALTH_CHECK));
		jMenuViewHealthCheck.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK));

		JMenuItem jMenuViewLicenseManagement = new JMenuItem(Configed.getResourceValue("MainFrame.labelLicenses"));
		jMenuViewLicenseManagement
				.addActionListener(event -> leftControlBar.selectView(EditingTarget.LICENSE_MANAGEMENT));
		jMenuViewLicenseManagement.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK));
		Icons.addIntellijIconToMenuItem(jMenuViewLicenseManagement, "scriptingScript");

		JMenuItem prevView = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuViewPreviousView"));
		Icons.addIntellijIconToMenuItem(prevView, "up");
		prevView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));
		prevView.addActionListener(event -> ConfigedMain.getMainFrame().previousView());

		JMenuItem nextView = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuViewNextView"));
		Icons.addIntellijIconToMenuItem(nextView, "down");
		nextView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK));
		nextView.addActionListener(event -> ConfigedMain.getMainFrame().nextView());

		jMenuView.add(jMenuViewClientsConfiguration);
		jMenuView.add(jMenuViewDepotConfiguration);
		jMenuView.add(jMenuViewServerConfiguration);
		jMenuView.add(jMenuViewDashboard);
		jMenuView.add(jMenuViewOpsiModuleInformation);
		jMenuView.add(jMenuViewHealthCheck);
		jMenuView.add(jMenuViewLicenseManagement);
		jMenuView.addSeparator();
		jMenuView.add(prevView);
		jMenuView.add(nextView);

		return jMenuView;
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

	private JMenu jMenuExtras() {
		JMenu jMenuExtras = new JMenu(Configed.getResourceValue("MainFrame.jMenuExtras"));

		JMenuItem jMenuWorkOnGroups = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuWorkOnGroups"));
		jMenuWorkOnGroups.setEnabled(
				persistenceController.getDataServices().module.isOpsiModuleActive(OpsiModule.LOCAL_IMAGING));
		jMenuWorkOnGroups.addActionListener(event -> configedMain.handleGroupActionRequest());

		JMenuItem jMenuWorkOnProducts = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuWorkOnProducts"));
		jMenuWorkOnProducts.addActionListener(event -> ExtraFrameController.startProductActionFrame());

		jMenuExtras.add(jMenuWorkOnGroups);
		jMenuExtras.add(jMenuWorkOnProducts);

		JMenuItem jMenuFrameMsgOfTheDay = null;
		List<Object> forbiddenItemsMOTD = persistenceController.getDataServices().userRoles.getForbiddenMOTD();
		boolean forbiddenMOTD = forbiddenItemsMOTD.contains(UserFeaturesConfig.KEY_OPT_MOTD_DEVICE)
				&& forbiddenItemsMOTD.contains(UserFeaturesConfig.KEY_OPT_MOTD_USER);

		if (ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast("4.3.15.2")) {
			jMenuFrameMsgOfTheDay = new JMenuItem(Configed.getResourceValue("MessageOfTheDay.title"));
			jMenuFrameMsgOfTheDay.addActionListener((ActionEvent e) -> new MessageOfTheDayDialog());

			jMenuFrameMsgOfTheDay.setEnabled(!forbiddenMOTD);
			if (forbiddenMOTD) {
				jMenuFrameMsgOfTheDay.setText(String.format("%s %s", Configed.getResourceValue("MessageOfTheDay.title"),
						Configed.getResourceValue("MainFrame.jMenu.attribute.forbidden")));
			}

		}
		if (jMenuFrameMsgOfTheDay != null) {
			jMenuExtras.add(jMenuFrameMsgOfTheDay);
		}

		return jMenuExtras;
	}

	private static JMenu createJMenuHelp(MainFrame mainFrame) {
		JMenu jMenuHelp = new JMenu(Configed.getResourceValue("MainFrame.jMenuHelp"));

		addHelpLinks(jMenuHelp);

		jMenuHelp.addSeparator();

		JMenuItem jMenuHelpOpsiVersion = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpOpsiService")
				+ ": " + ServerFacade.getOpsiServerVersionRetriever().getServerVersion());
		jMenuHelpOpsiVersion.setEnabled(false);

		jMenuHelp.add(jMenuHelpOpsiVersion);

		addLogfileMenus(jMenuHelp, mainFrame);

		jMenuHelp.addSeparator();

		addCreditsMenus(jMenuHelp, mainFrame);

		return jMenuHelp;
	}

	public JMenuBar initMenuBar(LeftToolBar leftToolBar, MainFrame mainFrame) {
		clientMenu = ClientMenuManager.getNewInstance(configedMain, mainFrame);

		JMenuBar jMenuBar = new JMenuBar();
		jMenuBar.add(createJMenuFile());
		jMenuBar.add(createJMenuView());
		jMenuBar.add(createJMenuClientSelection());
		jMenuBar.add(clientMenu.getJMenu());
		jMenuBar.add(leftToolBar.getJMenuServerConsoleMenuBar());

		jMenuBar.add(jMenuExtras());
		jMenuBar.add(createJMenuHelp(mainFrame));

		return jMenuBar;
	}

	public static void addHelpLinks(JMenu jMenuHelp) {
		JMenuItem jMenuHelpDoc = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuDoc"));
		Icons.addIntellijIconToMenuItem(jMenuHelpDoc, "readerMode");
		jMenuHelpDoc.addActionListener(actionEvent -> BrowserUtils.openDocumentation());
		jMenuHelp.add(jMenuHelpDoc);

		JMenuItem jMenuHelpForum = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuForum"));
		Icons.addThemeIconToMenuItem(jMenuHelpForum, "comment");
		jMenuHelpForum.addActionListener(actionEvent -> BrowserUtils.openLink(Globals.OPSI_FORUM_PAGE));
		jMenuHelp.add(jMenuHelpForum);

		// Get the language used for the support page
		String language = "de".equals(Messages.getLocale().getLanguage()) ? "de" : "en";
		JMenuItem jMenuHelpSupport = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuSupport"));
		Icons.addIntellijIconToMenuItem(jMenuHelpSupport, "cwmEnableCall");
		jMenuHelpSupport
				.addActionListener(actionEvent -> BrowserUtils.openLink(Globals.UIB_PAGE + language + "/support"));
		jMenuHelp.add(jMenuHelpSupport);

		JMenuItem jMenuMoreAboutOpsi = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuMoreAboutOpsi"));
		Icons.addOpsiIconToMenuItem(jMenuMoreAboutOpsi);
		jMenuMoreAboutOpsi.addActionListener(actionEvent -> BrowserUtils.openLink(Globals.OPSI_PAGE + language + "/"));
		jMenuHelp.add(jMenuMoreAboutOpsi);
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

	private static void showLogfileLocationAction(JFrame centerFrame) {
		JTextArea jTextArea = new JTextArea(Logging.getCurrentLogfilePath());
		jTextArea.setEditable(false);

		JButton buttonCopy = new JButton(Configed.getResourceValue("copy"));
		buttonCopy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(Logging.getCurrentLogfilePath()), null));

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

	public JPopupMenu getPopupMenuClone() {
		return clientMenu.getPopupMenuClone();
	}

	private void logout() {
		if (persistenceController != null) {
			persistenceController.getExecutioner().clearAuthenticationData();
		}

		MainFrame.resetInstanceData();
		MainFrame.restartConfiged();
	}

	public void saveConfigurationsSetEnabled(boolean enabled) {
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()
				&& enabled) {
			return;
		}

		Logging.debug(this, "saveConfigurationsSetEnabled ", enabled);

		jMenuFileSaveConfigurations.setEnabled(enabled);
	}
}
