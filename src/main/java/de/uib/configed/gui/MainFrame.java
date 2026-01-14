/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.gui.ConfigedMain.EditingTarget;
import de.uib.configed.gui.features.dashboard.LicenseDisplayer;
import de.uib.configed.gui.features.serverconsole.command.CommandFactory;
import de.uib.configed.gui.features.tree.ClientTree;
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.gui.healthcheck.HealthCheckDataLoader;
import de.uib.configed.gui.share.table.gui.FilterStateManager;
import de.uib.configed.share.Icons;
import de.uib.configed.share.PopupMouseListener;
import de.uib.configed.share.Utils;
import de.uib.configed.share.WindowsPositionManager;
import de.uib.configed.share.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class MainFrame extends JFrame {
	private ConfigedMain configedMain;

	private LeftControlBar leftControlBar;
	private LeftToolBar leftToolBar;

	private MainPanelManager mainPanelManager;

	private ClientTablePanel clientTablePanel;

	private MenuBarController menuBarController;

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
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			WindowsPositionManager.saveWindowProperties(ConfigedMain.getMainFrame(),
					WindowsPositionManager.MAIN_WINDOW);
			configedMain.saveAndQuit();
		}
	}

	private void guiInit(DepotsList depotsList, ClientTree clientTree, ProductTree productTree) {
		this.setIconImage(Icons.getMainIcon());

		leftControlBar = new LeftControlBar();
		leftToolBar = new LeftToolBar(configedMain);
		mainPanelManager = new MainPanelManager(configedMain, this, depotsList, clientTree, productTree);

		menuBarController = new MenuBarController(configedMain, leftControlBar);

		// We need to give 'this' as an argument since the variable mainFrame is not 
		// initialized at this point.
		setJMenuBar(menuBarController.initMenuBar(leftToolBar, this));

		showClientConfiguration();

		setTitle("(" + persistenceController.getExecutioner().getUsername() + ") "
				+ persistenceController.getExecutioner().getHost() + " - " + Globals.APPNAME);

		glassPane = new GlassPane();
		setGlassPane(glassPane);

		PopupMouseListener.addPopupMouseListenerToComponents(menuBarController.getPopupMenuClone(),
				new JComponent[] { clientTablePanel });
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

	public ServerConfiguration getServerConfiguration() {
		return mainPanelManager.getServerConfiguration();
	}

	public HostsStatusPanel getHostsStatusPanel() {
		return mainPanelManager.getHostsStatusPanel();
	}

	// ------------------------------------------------------------------------------------------
	// configure interaction
	// ------------------------------------------------------------------------------------------
	// menus

	public void resetData() {
		mainPanelManager.resetData();
	}

	public boolean checkSaveLicenses() {
		return mainPanelManager.checkSavedLicenses();
	}

	public static void restartConfiged() {
		restartConfiged(true);
	}

	private static void restartConfiged(boolean checkdirty) {
		Messagebus.getInstance().disconnect();
		ConfigedMain.closeInstance(checkdirty);
		ExtraFrameController.deleteInstances();
		CommandFactory.destroyInstance();
		FilterStateManager.clear();
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

	public void saveConfigurationsSetEnabled(boolean b) {
		menuBarController.saveConfigurationsSetEnabled(b);
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

	public void showHealthDataAction() {
		if (!persistenceController.getDataServices().health.isHealthDataAlreadyLoaded()) {
			activateLoadingPane(Configed.getResourceValue("HealthCheckDialog.loadData"));
		}

		new HealthCheckDataLoader().execute();
	}

	public void showOpsiModules() {
		if (!persistenceController.getDataServices().module.isOpsiUserAdminPD()) {
			Map<String, Object> modulesInfo = persistenceController.getDataServices().module.getOpsiModulesInfosPD();

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

	public boolean startLicensingManagement() {
		Logging.info(this, "startLicensingManagement called");

		if (!persistenceController.getDataServices().module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Utils.showMissingLicenseModules(Configed.getResourceValue("ConfigedMain.LicensemanagementNotActive"));
			return false;
		}

		new Thread() {
			@Override
			public void run() {
				showPanel(mainPanelManager.getLicenseManagementPanel());

				if (Boolean.TRUE.equals(persistenceController.getDataServices().config.getGlobalBooleanConfigValue(
						OpsiServiceNOMPersistenceController.KEY_SHOW_DASH_FOR_LICENSEMANAGEMENT,
						OpsiServiceNOMPersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENSEMANAGEMENT))) {
					// Starting JavaFX-Thread by creating a new JFXPanel, but not
					// using it since it is not needed.
					new JFXPanel();

					Platform.runLater(() -> LicenseDisplayer.showLicenseDisplayer(configedMain));
				}

				deactivateLoadingPane();
			}
		}.start();

		return true;
	}

	public void nextView() {
		switchView(true);
	}

	public void previousView() {
		switchView(false);
	}

	private void switchView(boolean isNext) {
		EditingTarget editingTarget = ConfigedMain.getEditingTarget();
		int currentView = editingTarget.ordinal() + 1;
		int targetView = currentView;

		targetView += isNext ? 1 : -1;

		if (targetView < 1) {
			targetView = 7;
		} else if (targetView > 7) {
			targetView = 1;
		} else {
			// Not needed.
		}

		switchViewBasedOnViewIndex(targetView);
	}

	private void switchViewBasedOnViewIndex(int index) {
		switch (index) {
		case 1 -> leftControlBar.selectView(EditingTarget.CLIENTS);
		case 2 -> leftControlBar.selectView(EditingTarget.DEPOTS);
		case 3 -> leftControlBar.selectView(EditingTarget.SERVER);
		case 4 -> leftControlBar.selectView(EditingTarget.DASHBOARD);
		case 5 -> leftControlBar.selectView(EditingTarget.OPSI_MODULES);
		case 6 -> leftControlBar.selectView(EditingTarget.HEALTH_CHECK);
		case 7 -> leftControlBar.selectView(EditingTarget.LICENSE_MANAGEMENT);
		default -> Logging.info(this, "Unknown view index" + index);
		}
	}
}
