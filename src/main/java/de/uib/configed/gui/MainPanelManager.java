/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.uib.configed.core.domain.modulelicense.OpsiLicensing;
import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.ConfigedMain.EditingTarget;
import de.uib.configed.gui.features.dashboard.Dashboard;
import de.uib.configed.gui.features.dashboard.LicenseDisplayer;
import de.uib.configed.gui.features.licenses.LicenseManagement;
import de.uib.configed.gui.features.tree.ClientTree;
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.gui.healthcheck.HealthCheckComponent;
import de.uib.configed.gui.share.Icons;
import de.uib.configed.gui.share.swing.ButtonTabComponent;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import net.miginfocom.swing.MigLayout;

public class MainPanelManager {
	private static final int DIVIDER_LOCATION_CENTRAL_PANE = 375;

	private ClientTree clientTree;
	private ProductTree productTree;

	private ClientConfiguration clientConfiguration;
	private HostsStatusPanel hostsStatusPanel;
	private JTabbedPane leftTabs;

	private ServerConfiguration serverConfiguration;

	private LicenseManagement licenseManagement;

	private TopToolBarManager topToolBarManager;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public MainPanelManager(ConfigedMain configedMain, MainFrame mainFrame, DepotsList depotsList,
			ClientTree clientTree, ProductTree productTree) {
		this.configedMain = configedMain;
		this.clientTree = clientTree;
		this.productTree = productTree;

		topToolBarManager = new TopToolBarManager(configedMain);

		initialInitialization(depotsList, mainFrame);
	}

	private void initialInitialization(DepotsList depotsList, MainFrame mainFrame) {
		DepotListPresenter depotListPresenter = new DepotListPresenter(depotsList);

		JScrollPane scrollpaneTreeClients = new JScrollPane();
		scrollpaneTreeClients.getViewport().add(clientTree);
		scrollpaneTreeClients.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setPreferredSize(clientTree.getMaximumSize());

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimum() ",
				scrollpaneTreeClients.getVerticalScrollBar().getMinimum());

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() ",
				scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize());

		Logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() ",
				scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize());

		JScrollPane scrollpaneTreeProducts = new JScrollPane();
		scrollpaneTreeProducts.getViewport().add(productTree);
		scrollpaneTreeProducts.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeProducts.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeProducts.setPreferredSize(productTree.getMaximumSize());

		leftTabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		leftTabs.addTab(null, depotListPresenter);
		leftTabs.addTab(null, scrollpaneTreeClients);
		leftTabs.addTab(null, scrollpaneTreeProducts);

		leftTabs.setTabComponentAt(0, new ButtonTabComponent(Icons.getIntellijIcon("selectAll"),
				Configed.getResourceValue("DepotListPresenter.depots"),
				Configed.getResourceValue("DepotListPresenter.depots.tooltip"), () -> configedMain.selectAllDepots()));
		leftTabs.setTabComponentAt(1,
				new ButtonTabComponent(Icons.getIntellijIcon("selectAll"),
						Configed.getResourceValue("MainFrame.tab_ClientTree"),
						Configed.getResourceValue("MainFrame.tab_ClientTree.tooltip"),
						() -> configedMain.activateGroup(false, ClientTree.ALL_CLIENTS_NAME)));
		leftTabs.setTabComponentAt(2,
				new ButtonTabComponent(Icons.getIntellijIcon("selectAll"),
						Configed.getResourceValue("MainFrame.tab_ProductTree"),
						Configed.getResourceValue("MainFrame.tab_ProductTree.tooltip"),
						() -> configedMain.activateAllProductsGroup()));

		leftTabs.setSelectedIndex(1);

		leftTabs.setMinimumSize(new Dimension());
		clientConfiguration = new ClientConfiguration(configedMain, mainFrame, productTree);
		hostsStatusPanel = new HostsStatusPanel(configedMain);
	}

	public JTabbedPane getTabbedPane() {
		return leftTabs;
	}

	public JPanel getPanelForEditingTarget(EditingTarget editingTarget) {
		return switch (editingTarget) {
		case CLIENTS -> getClientConfigurationPanel();
		case DEPOTS -> getDepotConfigurationSplitPane();
		case SERVER -> getServerConfigurationPanel();
		case DASHBOARD -> getDashBoardPanel();
		case OPSI_MODULES -> getOpsiLicensingPanel();
		case HEALTH_CHECK -> getHealthCheckPanel();
		case LICENSE_MANAGEMENT -> getLicenseManagementPanel();
		};
	}

	private JPanel getClientConfigurationPanel() {
		JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, leftTabs, clientConfiguration);
		jSplitPane.setDividerLocation(DIVIDER_LOCATION_CENTRAL_PANE);

		JPanel jPanel = new JPanel();
		jPanel.setLayout(new MigLayout("insets 0, fill, wrap 1", "[grow]", "[grow][]"));
		jPanel.add(jSplitPane, "grow");
		jPanel.add(hostsStatusPanel, "growx");

		return createPanel(jPanel, topToolBarManager.getConfigurationButtons(),
				Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
	}

	private JPanel getDepotConfigurationSplitPane() {
		DepotsList depotsList = new DepotsList(configedMain);
		depotsList.setListData(persistenceController.getDataServices().hostInfoCollections.getAllDepotNamesList());
		depotsList.setInfo(persistenceController.getDataServices().hostInfoCollections.getAllDepots());
		DepotConfiguration depotConfiguration = new DepotConfiguration(configedMain, depotsList);

		JLabel depotSelectionLabel = Utils.createBoldLabel(Configed.getResourceValue("depotSelection"));
		DepotListPresenter depotListPresenter = new DepotListPresenter(depotsList);

		JPanel depotsListPanel = new JPanel();
		depotsListPanel.setLayout(new MigLayout("insets 0", "[grow]", "[]" + Globals.MIN_GAP_SIZE + "[]"));

		depotsListPanel.add(depotSelectionLabel, "align center, wrap");
		depotsListPanel.add(depotListPresenter, "grow, push");

		depotsListPanel.setMinimumSize(new Dimension());

		JSplitPane depotConfigurationSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, depotsListPanel,
				depotConfiguration);

		depotConfigurationSplitPane.setDividerLocation(DIVIDER_LOCATION_CENTRAL_PANE);
		depotConfigurationSplitPane.setBorder(new EmptyBorder(0, 0, Globals.MIN_GAP_SIZE, 0));

		return createPanel(depotConfigurationSplitPane, null, Configed.getResourceValue("depotConfiguration"));
	}

	private JPanel getServerConfigurationPanel() {
		serverConfiguration = new ServerConfiguration();
		serverConfiguration.setBorder(new EmptyBorder(0, 0, Globals.MIN_GAP_SIZE, 0));
		return createPanel(serverConfiguration, null, Configed.getResourceValue("MainFrame.labelServerConfiguration"));
	}

	public JPanel getDashBoardPanel() {
		Logging.info(this, "initDashboardpanel");
		return createPanel(new Dashboard(configedMain), null, Configed.getResourceValue("Dashboard.title"));
	}

	public JPanel getOpsiLicensingPanel() {
		if (!persistenceController.getDataServices().module.isOpsiUserAdminPD()) {
			Map<String, Object> modulesInfo = persistenceController.getDataServices().module.getOpsiModulesInfosPD();

			StringJoiner message = new StringJoiner("\n");
			for (Entry<String, Object> modulesInfoEntry : modulesInfo.entrySet()) {
				message.add(modulesInfoEntry.getKey() + ": " + modulesInfoEntry.getValue());
			}

			JTextArea textArea = new JTextArea(message.toString());
			textArea.setEditable(false);

			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), textArea,
					Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), JOptionPane.PLAIN_MESSAGE);
			return null;
		} else {
			OpsiLicensing opsiLicensing = new OpsiLicensing();
			return createPanel(opsiLicensing, topToolBarManager.getOpsiLicensingButtons(opsiLicensing),
					Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		}
	}

	public JPanel getHealthCheckPanel() {
		Logging.info(this, "init health check panel");
		HealthCheckComponent healthCheck = new HealthCheckComponent();
		return createPanel(healthCheck.initUI(), topToolBarManager.getHealthCheckButtons(healthCheck),
				Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
	}

	public JPanel getLicenseManagementPanel() {
		Logging.info(this, "startLicensingManagement called");

		if (!persistenceController.getDataServices().module.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			Utils.showMissingLicenseModules(Configed.getResourceValue("ConfigedMain.LicensemanagementNotActive"));
			return null;
		}

		new Thread() {
			@Override
			public void run() {

				if (Boolean.TRUE.equals(persistenceController.getDataServices().config.getGlobalBooleanConfigValue(
						OpsiServiceNOMPersistenceController.KEY_SHOW_DASH_FOR_LICENSEMANAGEMENT,
						OpsiServiceNOMPersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENSEMANAGEMENT))) {
					// Starting JavaFX-Thread by creating a new JFXPanel, but not
					// using it since it is not needed.
					new JFXPanel();

					Platform.runLater(() -> LicenseDisplayer.showLicenseDisplayer(configedMain));
				}
			}
		}.start();

		// show Loading pane only when something needs to be loaded from server
		long startmillis = System.currentTimeMillis();
		Logging.info(this, "initLicensesFrame start ");
		licenseManagement = new LicenseManagement(configedMain);
		long endmillis = System.currentTimeMillis();
		Logging.info(this, "initLicensesFrame  diff ", endmillis - startmillis);

		return createPanel(licenseManagement, topToolBarManager.getLicensingManagementButtons(this),
				Configed.getResourceValue("MainFrame.labelLicenses"));
	}

	private JPanel createPanel(JComponent component, List<JButton> toolBarButtons, String title) {
		JLabel opsiLogo = new JLabel(Icons.getOpsiLogoWide());
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(
				titleLabel.getFont().deriveFont(Font.BOLD).deriveFont((float) (titleLabel.getFont().getSize() + 2)));

		JToolBar generalToolBar = topToolBarManager.createGeneralToolBar();

		if (toolBarButtons != null) {
			generalToolBar.addSeparator();
			toolBarButtons.forEach(generalToolBar::add);
		}

		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 " + Globals.MIN_GAP_SIZE + ", fill, wrap 3",
				"[pref!][grow][pref!]", "[pref!][grow]"));
		panel.add(generalToolBar, "aligny center");
		panel.add(titleLabel, "align center");
		panel.add(opsiLogo, "aligny center, gapright " + Globals.GAP_SIZE);
		panel.add(component, "span 3, grow");

		return panel;
	}

	public ClientConfiguration getClientConfiguration() {
		return clientConfiguration;
	}

	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public HostsStatusPanel getHostsStatusPanel() {
		return hostsStatusPanel;
	}

	public void reloadLicensesAction() {
		ConfigedMain.getMainFrame()
				.activateLoadingPane(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData") + " ...");
		new Thread() {
			@Override
			public void run() {
				persistenceController.reloadData(ReloadEvent.LICENSE_DATA_RELOAD.toString());
				ConfigedMain.getMainFrame().showPanel(EditingTarget.LICENSE_MANAGEMENT);
				ConfigedMain.getMainFrame().deactivateLoadingPane();
			}
		}.start();
	}

	public boolean checkSavedLicenses() {
		boolean checkSavedLicensesFrame = licenseManagement == null || licenseManagement.checkSavedLicensesPane();

		if (!checkSavedLicensesFrame) {
			ConfigedMain.setEditingTarget(EditingTarget.LICENSE_MANAGEMENT);
		}

		Logging.info(this, "close instance result ", checkSavedLicensesFrame);

		return checkSavedLicensesFrame;
	}
}
