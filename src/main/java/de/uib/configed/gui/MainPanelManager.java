/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.gui.licenses.LicenseManagement;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.tree.ProductTree;
import de.uib.opsidatamodel.modulelicense.OpsiLicensing;
import de.uib.utils.logging.Logging;

public class MainPanelManager {
	private static final int DIVIDER_LOCATION_CENTRAL_PANE = 375;

	private DepotListPresenter depotListPresenter;

	private ClientTree clientTree;
	private ProductTree productTree;

	private TabbedConfigPanes tabbedPaneConfigPanes;
	private HostsStatusPanel hostsStatusPanel;

	private JPanel dashboardPanel;
	private Dashboard dashboard;

	private JPanel licensingInfoPanel;

	private JPanel healthCheckPanel;
	private HealthCheck healthCheck;

	private JPanel licenseManagementPanel;
	private LicenseManagement licenseManagement;

	private TopToolBarManager topToolBarManager;

	private ConfigedMain configedMain;

	public MainPanelManager(ConfigedMain configedMain, DepotsList depotsList, ClientTree clientTree,
			ProductTree productTree) {
		this.configedMain = configedMain;
		this.clientTree = clientTree;
		this.productTree = productTree;

		topToolBarManager = new TopToolBarManager(configedMain);

		depotListPresenter = new DepotListPresenter(depotsList, configedMain);
	}

	public JPanel getConfigurationPanel(MainFrame mainFrame) {
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

		JTabbedPane jTabbedPaneClientSelection = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		jTabbedPaneClientSelection.addTab(Configed.getResourceValue("DepotListPresenter.depots"), depotListPresenter);
		jTabbedPaneClientSelection.addTab(Configed.getResourceValue("MainFrame.tab_ClientTree"), scrollpaneTreeClients);
		jTabbedPaneClientSelection.addTab(Configed.getResourceValue("MainFrame.tab_ProductTree"),
				scrollpaneTreeProducts);

		jTabbedPaneClientSelection.setSelectedIndex(1);

		tabbedPaneConfigPanes = new TabbedConfigPanes(configedMain, mainFrame, productTree);
		JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, jTabbedPaneClientSelection,
				tabbedPaneConfigPanes);
		jSplitPane.setDividerLocation(DIVIDER_LOCATION_CENTRAL_PANE);

		hostsStatusPanel = new HostsStatusPanel();

		JPanel jPanel = new JPanel();
		GroupLayout groupLayout = new GroupLayout(jPanel);
		jPanel.setLayout(groupLayout);

		groupLayout.setVerticalGroup(
				groupLayout.createSequentialGroup().addComponent(jSplitPane).addComponent(hostsStatusPanel));

		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup().addComponent(jSplitPane).addComponent(hostsStatusPanel));

		return createPanel(jPanel, topToolBarManager.getConfigurationToolBar(), "?");
	}

	public JPanel getDashBoardPanel() {
		Logging.info(this, "initDashboardpanel");
		if (dashboardPanel == null) {
			dashboard = new Dashboard(configedMain);
			dashboardPanel = createPanel(dashboard, new JToolBar(), Configed.getResourceValue("Dashboard.title"));
		}

		return dashboardPanel;
	}

	public JPanel getOpsiLicensingPanel() {
		if (licensingInfoPanel == null) {
			OpsiLicensing opsiLicensing = new OpsiLicensing();
			licensingInfoPanel = createPanel(opsiLicensing, topToolBarManager.getOpsiLicensingToolBar(opsiLicensing),
					Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		}

		return licensingInfoPanel;
	}

	public JPanel getHealthCheckPanel() {
		Logging.info(this, "init health check ", healthCheck);
		if (healthCheckPanel == null) {
			healthCheck = new HealthCheck();
			healthCheckPanel = createPanel(healthCheck, topToolBarManager.getHealthCheckToolBar(healthCheck),
					Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		}

		return healthCheckPanel;
	}

	public JPanel getLicenseManagementPanel() {
		if (licenseManagement == null) {
			// show Loading pane only when something needs to be loaded from server
			ConfigedMain.getMainFrame().activateLoadingPane(Configed.getResourceValue("ConfigedMain.Licenses.Loading"));
			long startmillis = System.currentTimeMillis();
			Logging.info(this, "initLicensesFrame start ");
			licenseManagement = new LicenseManagement(configedMain);
			long endmillis = System.currentTimeMillis();
			Logging.info(this, "initLicensesFrame  diff ", endmillis - startmillis);

			licenseManagementPanel = createPanel(licenseManagement, new JToolBar(),
					Configed.getResourceValue("MainFrame.labelLicenses"));
		}

		return licenseManagementPanel;
	}

	private static JPanel createPanel(JComponent component, JToolBar toolBar, String title) {
		JLabel titleLabel = new JLabel(title);

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(toolBar).addComponent(titleLabel))
				.addComponent(component));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addComponent(toolBar)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE).addComponent(titleLabel)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE))
				.addComponent(component));

		return panel;
	}

	public TabbedConfigPanes getTabbedConfigPanes() {
		return tabbedPaneConfigPanes;
	}

	public void rebuildDepotPopup() {
		depotListPresenter.rebuildPopup();
	}

	public HostsStatusPanel getHostsStatusPanel() {
		return hostsStatusPanel;
	}

	public void resetData() {
		if (dashboard != null) {
			dashboard.clearAllData();
			dashboard = null;
		}

		licensingInfoPanel = null;
		healthCheck = null;

		licenseManagement = null;
	}

	// TODO find a way to reload the licenses
	private void reloadLicensesAction() {
		ConfigedMain.getMainFrame()
				.activateLoadingPane(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData") + " ...");
		new Thread() {
			@Override
			public void run() {
				licenseManagement.reloadLicensesData();
				ConfigedMain.getMainFrame().deactivateLoadingPane();
			}
		}.start();
	}

	public boolean checkSavedLicenses() {
		return licenseManagement == null || licenseManagement.checkSavedLicensesPane();
	}
}
