/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.CardLayout;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.certificate.CertificateValidatorFactory;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.gui.ConfigedMain.EditingTarget;
import de.uib.configed.gui.features.serverconsole.command.CommandFactory;
import de.uib.configed.gui.features.tree.ClientTree;
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.WindowsPositionManager;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.gui.FilterStateManager;
import net.miginfocom.swing.MigLayout;

public class MainFrame extends JFrame {
	private ConfigedMain configedMain;

	private LeftControlBar leftControlBar;
	private LeftToolBar leftToolBar;

	private MainPanelManager mainPanelManager;

	private ClientTablePanel clientTablePanel;

	private MenuBarController menuBarController;

	private GlassPane glassPane;

	private CardLayout cardLayout;
	private JPanel contentPanel;

	private Map<EditingTarget, Boolean> initializedPanels = new EnumMap<>(EditingTarget.class);

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

		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);

		getContentPane().setLayout(new MigLayout("insets 0, fill", "[pref!]0[grow]", "[grow]"));
		JPanel controlPanel = new JPanel(new MigLayout("insets 0, filly, wrap 1", "[pref!]", "[pref!]push[pref!]"));

		controlPanel.add(leftControlBar, "aligny top");
		controlPanel.add(leftToolBar, "aligny bottom, gaptop unrel");

		getContentPane().add(controlPanel, "growy");
		getContentPane().add(contentPanel, "grow");

		showPanel(EditingTarget.CLIENTS);

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
		initializedPanels.clear();
		contentPanel.removeAll();
	}

	public boolean checkSaveLicenses() {
		return mainPanelManager.checkSavedLicenses();
	}

	public static void resetInstanceData() {
		CacheManager.getInstance().clearAllCachedData();
		Configed.getSavedStates().removeAll();

		// We need to reset the validators so that new ones will be created when reconnecting
		CertificateValidatorFactory.resetCertificateValidators();
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

	public boolean showPanel(EditingTarget editingTarget) {
		if (!Boolean.TRUE.equals(initializedPanels.get(editingTarget))) {
			activateLoadingCursor();
			JPanel panel = mainPanelManager.getPanelForEditingTarget(editingTarget);
			if (panel == null) {
				deactivateLoadingCursor();
				return false;
			}
			contentPanel.add(panel, editingTarget.name());
			initializedPanels.put(editingTarget, true);
			deactivateLoadingCursor();
		}

		cardLayout.show(contentPanel, editingTarget.name());
		return true;
	}

	public void saveConfigurationsSetEnabled(boolean b) {
		menuBarController.saveConfigurationsSetEnabled(b);
	}

	public void activateLoadingPane(String infoText) {
		SwingUtils.runOnEventDispatchThread(() -> {
			glassPane.activate(true);
			glassPane.setInfoText(infoText);
		});
	}

	public void deactivateLoadingPane() {
		SwingUtils.runOnEventDispatchThread(() -> glassPane.activate(false));
	}

	public void activateLoadingCursor() {
		SwingUtils.runOnEventDispatchThread(() -> setCursor(Globals.WAIT_CURSOR));
	}

	public void deactivateLoadingCursor() {
		SwingUtils.runOnEventDispatchThread(() -> setCursor(null));
	}

	public void nextView() {
		switchView(true);
	}

	public void previousView() {
		switchView(false);
	}

	private void switchView(boolean isNext) {
		int nextView = ConfigedMain.getEditingTarget().ordinal() + (isNext ? 1 : -1);

		// Move targetView within bounds
		nextView = (nextView + EditingTarget.values().length) % EditingTarget.values().length;

		EditingTarget targetEditingTarget = EditingTarget.values()[nextView];
		leftControlBar.selectView(targetEditingTarget);
	}
}
