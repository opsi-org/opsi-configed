/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Font;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.EditingTarget;
import de.uib.configed.Globals;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.gui.licenses.LicenseManagement;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.tree.ProductTree;
import de.uib.opsidatamodel.modulelicense.OpsiLicensing;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.ButtonTabComponent;

public class MainPanelManager {
	private static final int DIVIDER_LOCATION_CENTRAL_PANE = 375;

	private ClientTree clientTree;
	private ProductTree productTree;

	private ClientConfiguration clientConfiguration;
	private HostsStatusPanel hostsStatusPanel;
	private JTabbedPane leftTabs;

	private JSplitPane depotConfigurationSplitPane;
	private ServerConfiguration serverConfiguration;

	private JPanel dashboardPanel;
	private Dashboard dashboard;

	private JPanel licensingInfoPanel;

	private JPanel healthCheckPanel;

	private JPanel licenseManagementPanel;
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
		DepotListPresenter depotListPresenter = new DepotListPresenter(depotsList, configedMain);

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

		clientConfiguration = new ClientConfiguration(configedMain, mainFrame, productTree);
		hostsStatusPanel = new HostsStatusPanel();
	}

	public JTabbedPane getTabbedPane() {
		return leftTabs;
	}

	public JPanel getClientConfigurationPanel() {
		JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, leftTabs, clientConfiguration);
		jSplitPane.setDividerLocation(DIVIDER_LOCATION_CENTRAL_PANE);

		JPanel jPanel = new JPanel();
		GroupLayout groupLayout = new GroupLayout(jPanel);
		jPanel.setLayout(groupLayout);

		groupLayout.setVerticalGroup(
				groupLayout.createSequentialGroup().addComponent(jSplitPane).addComponent(hostsStatusPanel));

		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup().addComponent(jSplitPane).addComponent(hostsStatusPanel));

		return createPanel(jPanel, topToolBarManager.getConfigurationButtons(),
				Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
	}

	public JPanel getDepotConfigurationSplitPane() {
		if (depotConfigurationSplitPane == null) {
			DepotsList depotsList = new DepotsList(configedMain);
			depotsList.setListData(persistenceController.getHostInfoCollections().getDepotNamesList());
			depotsList.setInfo(persistenceController.getHostInfoCollections().getDepots());
			DepotConfiguration depotConfiguration = new DepotConfiguration(configedMain, depotsList);

			JLabel depotSelectionLabel = new JLabel(Configed.getResourceValue("depotSelection"));
			DepotListPresenter depotListPresenter = new DepotListPresenter(depotsList, configedMain);

			JPanel depotsListPanel = new JPanel();
			GroupLayout layout = new GroupLayout(depotsListPanel);
			depotsListPanel.setLayout(layout);

			layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
					.addComponent(depotSelectionLabel).addGap(Globals.MIN_GAP_SIZE).addComponent(depotListPresenter));
			layout.setHorizontalGroup(layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
							.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE)
							.addComponent(depotSelectionLabel)
							.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE))
					.addComponent(depotListPresenter));

			depotConfigurationSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, depotsListPanel,
					depotConfiguration);

			depotConfigurationSplitPane.setDividerLocation(DIVIDER_LOCATION_CENTRAL_PANE);
			depotConfigurationSplitPane.setBorder(new EmptyBorder(0, 0, Globals.MIN_GAP_SIZE, 0));
		}

		return createPanel(depotConfigurationSplitPane, null, Configed.getResourceValue("depotConfiguration"));
	}

	public JPanel getServerConfigurationPanel() {
		if (serverConfiguration == null) {
			// We start the loading animation because this takes a lot of time
			ConfigedMain.getMainFrame().activateLoadingCursor();
			serverConfiguration = new ServerConfiguration();
			serverConfiguration.setBorder(new EmptyBorder(0, 0, Globals.MIN_GAP_SIZE, 0));
			ConfigedMain.getMainFrame().deactivateLoadingCursor();
		}

		return createPanel(serverConfiguration, null, Configed.getResourceValue("MainFrame.labelServerConfiguration"));
	}

	public JPanel getDashBoardPanel() {
		Logging.info(this, "initDashboardpanel");
		if (dashboardPanel == null) {
			dashboard = new Dashboard(configedMain);
			dashboardPanel = createPanel(dashboard, null, Configed.getResourceValue("Dashboard.title"));
		}

		return dashboardPanel;
	}

	public JPanel getOpsiLicensingPanel() {
		if (licensingInfoPanel == null) {
			OpsiLicensing opsiLicensing = new OpsiLicensing();
			licensingInfoPanel = createPanel(opsiLicensing, topToolBarManager.getOpsiLicensingButtons(opsiLicensing),
					Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		}

		return licensingInfoPanel;
	}

	public JPanel getHealthCheckPanel() {
		Logging.info(this, "init health check panel", healthCheckPanel);
		if (healthCheckPanel == null) {
			HealthCheck healthCheck = new HealthCheck();
			healthCheckPanel = createPanel(healthCheck, topToolBarManager.getHealthCheckButtons(healthCheck),
					Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		}

		return healthCheckPanel;
	}

	public JPanel getLicenseManagementPanel() {
		if (licenseManagementPanel == null) {
			// show Loading pane only when something needs to be loaded from server
			ConfigedMain.getMainFrame().activateLoadingPane(Configed.getResourceValue("ConfigedMain.Licenses.Loading"));
			long startmillis = System.currentTimeMillis();
			Logging.info(this, "initLicensesFrame start ");
			licenseManagement = new LicenseManagement(configedMain);
			long endmillis = System.currentTimeMillis();
			Logging.info(this, "initLicensesFrame  diff ", endmillis - startmillis);

			licenseManagementPanel = createPanel(licenseManagement,
					topToolBarManager.getLicensingManagementButtons(this),
					Configed.getResourceValue("MainFrame.labelLicenses"));
		}

		return licenseManagementPanel;
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

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(generalToolBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(opsiLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addComponent(component));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addComponent(generalToolBar)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE).addComponent(titleLabel)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE).addComponent(opsiLogo))
				.addComponent(component));

		return panel;
	}

	public ClientConfiguration getClientConfiguration() {
		return clientConfiguration;
	}

	public HostsStatusPanel getHostsStatusPanel() {
		return hostsStatusPanel;
	}

	public void resetData() {
		depotConfigurationSplitPane = null;
		serverConfiguration = null;

		if (dashboardPanel != null) {
			// We need to clear all data, otherwise they will be kept
			dashboard.clearAllData();
			dashboardPanel = null;
		}

		licensingInfoPanel = null;
		healthCheckPanel = null;

		licenseManagementPanel = null;
	}

	public void reloadLicensesAction() {
		ConfigedMain.getMainFrame()
				.activateLoadingPane(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData") + " ...");
		new Thread() {
			@Override
			public void run() {
				persistenceController.reloadData(ReloadEvent.LICENSE_DATA_RELOAD.toString());
				licenseManagementPanel = null;
				ConfigedMain.getMainFrame().startLicensingManagement();
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
