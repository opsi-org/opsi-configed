/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.gui.features.hostconfigs.PanelClientHostConfig;
import de.uib.configed.gui.features.hwinfopage.PanelHWInfo;
import de.uib.configed.gui.features.productpage.PanelProductSettings;
import de.uib.configed.gui.features.productpage.PanelProductSettings.ProductSettingsType;
import de.uib.configed.gui.features.swinfopage.PanelSWInfo;
import de.uib.configed.gui.features.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.share.logging.Logging;

public class ClientConfiguration extends JTabbedPane implements ChangeListener {
	public static final float DIVIDER_LOCATION = 0.8F;

	private ConfigedMain configedMain;
	private MainFrame mainFrame;
	private ProductTree productTree;

	private PanelProductSettings panelLocalbootProductSettings;
	private PanelProductSettings panelNetbootProductSettings;
	private PanelClientHostConfig panelClientHostConfig;

	private PanelSWInfo panelSWInfo;
	private PanelSWMultiClientReport showSoftwareLogMultiClientReport;

	private PanelHWInfo panelHWInfo;

	private TabbedLogPane tabbedLogPane;
	private JSplitPane panelClientSelection;
	private ClientInfoPanel clientInfoPanel;

	private ProductPageManager productPageManager;

	private int lastSelectedIndex;

	public ClientConfiguration(ConfigedMain configedMain, MainFrame mainFrame, ProductTree productTree) {
		this.configedMain = configedMain;
		this.mainFrame = mainFrame;
		this.productTree = productTree;

		init();

		super.addChangeListener(this);
	}

	public PanelProductSettings getPanelLocalbootProductSettings() {
		return panelLocalbootProductSettings;
	}

	public PanelProductSettings getPanelNetbootProductSettings() {
		return panelNetbootProductSettings;
	}

	public ProductPageManager getProductPageManager() {
		return productPageManager;
	}

	public ClientInfoPanel getClientInfoPanel() {
		return clientInfoPanel;
	}

	public JSplitPane getPanelClientSelection() {
		return panelClientSelection;
	}

	private void init() {
		clientInfoPanel = new ClientInfoPanel(configedMain);
		panelClientSelection = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainFrame.getClientTablePanel(),
				clientInfoPanel);

		panelClientSelection.setResizeWeight(1.0);

		panelLocalbootProductSettings = new PanelProductSettings(configedMain, productTree,
				ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS);

		panelNetbootProductSettings = new PanelProductSettings(configedMain, productTree,
				ProductSettingsType.NETBOOT_PRODUCT_SETTINGS);

		productTree.setPanels(panelLocalbootProductSettings, panelNetbootProductSettings);

		productPageManager = new ProductPageManager(configedMain, this);

		panelLocalbootProductSettings.setUpdater(productPageManager::setLocalbootProductsPage);
		panelNetbootProductSettings.setUpdater(productPageManager::setNetbootProductsPage);

		addTab(Configed.getResourceValue("MainFrame.panel_Clientselection"), panelClientSelection);

		addTab(Configed.getResourceValue("localbootProducts"), panelLocalbootProductSettings);

		addTab(Configed.getResourceValue("netbootProducts"), panelNetbootProductSettings);

		addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), null);

		addTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog"), null);

		addTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog"), null);

		addTab(Configed.getResourceValue("MainFrame.jPanel_logfiles"), null);
	}

	private void setSoftwareInfoTab() {
		if (panelSWInfo == null) {
			panelSWInfo = new PanelSWInfo(configedMain, true);
			showSoftwareLogMultiClientReport = new PanelSWMultiClientReport();
			SwExporter swExporter = new SwExporter(showSoftwareLogMultiClientReport, panelSWInfo, configedMain);
			showSoftwareLogMultiClientReport.setActionListenerForStart(swExporter);

			setComponentAt(getSelectedIndex(), panelSWInfo);
		}

		panelSWInfo.updateTab(configedMain.getSelectedClients().size());

		if (configedMain.getSelectedClients().size() <= 1) {
			// Nothing will be done
			showSoftwareInfo(panelSWInfo);
		} else {
			Logging.info(this, "setSoftwareAudit for clients ", configedMain.getSelectedClients().size());

			showSoftwareInfo(showSoftwareLogMultiClientReport);
		}

	}

	private void setHardwareInfoTab() {
		if (panelHWInfo == null) {
			panelHWInfo = new PanelHWInfo(true, configedMain, this);
			setComponentAt(getSelectedIndex(), panelHWInfo);
		}

		panelHWInfo.updateTab(configedMain.getSelectedClients().size());
	}

	private void setLogFilesTab() {
		if (tabbedLogPane == null) {
			tabbedLogPane = new TabbedLogPane(configedMain);
			setComponentAt(getSelectedIndex(), tabbedLogPane);
		}

		tabbedLogPane.updateTab(configedMain.getSelectedClients().size());
	}

	private void setHostConfigTab() {
		if (panelClientHostConfig == null) {
			panelClientHostConfig = new PanelClientHostConfig(configedMain);
			setComponentAt(getSelectedIndex(), panelClientHostConfig);
		}

		panelClientHostConfig.updateTab(configedMain.getSelectedClients().size());
	}

	private void showSoftwareInfo(JPanel showSoftwareLog) {
		setComponentAt(getSelectedIndex(), showSoftwareLog);
		showSoftwareLog.repaint();
	}

	public void setLogFileTab(String logtype, final boolean resetCaret) {
		Logging.info(this, "setUpdatedLogfilePanel ", logtype);
		tabbedLogPane.setDocuments(logtype, resetCaret);
	}

	public void initSplitPanes() {
		panelClientSelection.setDividerLocation(DIVIDER_LOCATION);
		panelLocalbootProductSettings.getContentPane().setDividerLocation(DIVIDER_LOCATION);
		panelNetbootProductSettings.getContentPane().setDividerLocation(DIVIDER_LOCATION);
	}

	public void updateProductTab() {
		if (getSelectedIndex() == 1 || getSelectedIndex() == 2) {
			stateChanged(null);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Logging.info(this, "state change in clientConfiguration with selected index", getSelectedIndex());

		if (lastSelectedIndex != getSelectedIndex() && !ChangedDataManager.checkSaveAll(true)) {
			// We don't want to trigger state change events while changing the selected index
			this.removeChangeListener(this);
			setSelectedIndex(lastSelectedIndex);
			this.addChangeListener(this);
			// If switching is cancelled due to unsaved data, we abort the state change
			return;
		}

		ConfigedMain.getMainFrame().activateLoadingCursor();

		switch (getSelectedIndex()) {
		case 0 -> {
			// Client page does not need to be updated
		}
		case 1 -> panelLocalbootProductSettings.updateTab(configedMain.getSelectedClients().size());
		case 2 -> panelNetbootProductSettings.updateTab(configedMain.getSelectedClients().size());
		case 3 -> setHostConfigTab();
		case 4 -> setHardwareInfoTab();
		case 5 -> setSoftwareInfoTab();
		case 6 -> setLogFilesTab();
		default -> Logging.warning(this, "unexpected visualViewIndex ", getSelectedIndex(), " in clients view");
		}

		lastSelectedIndex = getSelectedIndex();

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}
}
