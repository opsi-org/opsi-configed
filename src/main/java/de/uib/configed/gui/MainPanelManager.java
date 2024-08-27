/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.gui.licenses.LicensesPanel;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.tree.ProductTree;
import de.uib.opsidatamodel.modulelicense.LicensingInfoPanel;
import de.uib.utils.logging.Logging;

public class MainPanelManager {
	private static final int DIVIDER_LOCATION_CENTRAL_PANE = 375;

	private DepotListPresenter depotListPresenter;

	private ClientTree clientTree;
	private ProductTree productTree;

	private JPanel configurationPanel;
	private TabbedConfigPanes tabbedPaneConfigPanes;
	private HostsStatusPanel hostsStatusPanel;

	private Dashboard dashboard;

	private LicensingInfoPanel licensingInfoPanel;

	private HealthCheckPanel healthCheckPanel;

	private LicensesPanel licensesPanel;

	private ConfigedMain configedMain;

	public MainPanelManager(ConfigedMain configedMain, MainFrame mainFrame, DepotsList depotsList,
			ClientTree clientTree, ProductTree productTree) {
		this.configedMain = configedMain;
		this.clientTree = clientTree;
		this.productTree = productTree;

		depotListPresenter = new DepotListPresenter(depotsList, configedMain);

		configurationPanel = initConfigurationPanel(mainFrame);
	}

	private JPanel initConfigurationPanel(MainFrame mainFrame) {
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

		return jPanel;
	}

	public JPanel getConfigurationPanel() {
		return configurationPanel;
	}

	public Dashboard getDashBoardPanel() {
		Logging.info(this, "initDashboard ", dashboard);
		if (dashboard == null) {
			dashboard = new Dashboard(configedMain);
		}

		return dashboard;
	}

	public LicensingInfoPanel getLicensingInfoPanel() {
		if (licensingInfoPanel == null) {
			licensingInfoPanel = new LicensingInfoPanel();
		}
		return licensingInfoPanel;
	}

	public HealthCheckPanel getHealthCheckPanel() {
		Logging.info(this, "init health check dialog ", healthCheckPanel);
		if (healthCheckPanel == null) {
			healthCheckPanel = new HealthCheckPanel();
		}

		return healthCheckPanel;
	}

	public LicensesPanel getLicensesPanel() {
		if (licensesPanel == null) {
			// show Loading pane only when something needs to be loaded from server
			ConfigedMain.getMainFrame().activateLoadingPane(Configed.getResourceValue("ConfigedMain.Licenses.Loading"));

			long startmillis = System.currentTimeMillis();
			Logging.info(this, "initLicensesFrame start ");
			licensesPanel = new LicensesPanel(configedMain);
			long endmillis = System.currentTimeMillis();
			Logging.info(this, "initLicensesFrame  diff ", endmillis - startmillis);
		}

		return licensesPanel;
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
		healthCheckPanel = null;

		licensesPanel = null;
	}

	// TODO find a way to reload the licenses
	private void reloadLicensesAction() {
		ConfigedMain.getMainFrame()
				.activateLoadingPane(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData") + " ...");
		new Thread() {
			@Override
			public void run() {
				licensesPanel.reloadLicensesData();
				ConfigedMain.getMainFrame().deactivateLoadingPane();
			}
		}.start();
	}

	public boolean checkSavedLicenses() {
		return licensesPanel == null || licensesPanel.checkSavedLicensesPane();
	}
}
