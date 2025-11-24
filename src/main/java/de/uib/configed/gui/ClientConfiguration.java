/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.glass.events.KeyEvent;

import de.uib.configed.core.domain.datachanges.ConfigUpdateCollection;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.features.hwinfopage.PanelHWInfo;
import de.uib.configed.gui.features.productpage.PanelProductSettings;
import de.uib.configed.gui.features.productpage.PanelProductSettings.ProductSettingsType;
import de.uib.configed.gui.features.swinfopage.PanelSWInfo;
import de.uib.configed.gui.features.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class ClientConfiguration extends JTabbedPane implements ChangeListener {
	public static final float DIVIDER_LOCATION = 0.8F;

	private ConfigUpdateCollection configUpdateCollection;

	private ConfigedMain configedMain;
	private MainFrame mainFrame;
	private ProductTree productTree;

	private PanelProductSettings panelLocalbootProductSettings;
	private PanelProductSettings panelNetbootProductSettings;
	private PanelHostConfig panelHostConfig;

	private PanelSWInfo panelSWInfo;
	private JPanel showSoftwareLogNotFound;
	private PanelSWMultiClientReport showSoftwareLogMultiClientReport;

	private PanelHWInfo panelHWInfo;
	private JPanel showHardwareLogNotFoundPanel;

	private TabbedLogPane tabbedLogPane;
	private JSplitPane panelClientSelection;
	private ClientInfoPanel clientInfoPanel;

	private ProductPageManager productPageManager;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientConfiguration(ConfigedMain configedMain, MainFrame mainFrame, ProductTree productTree) {
		this.configedMain = configedMain;
		this.mainFrame = mainFrame;
		this.productTree = productTree;

		init();

		productPageManager = new ProductPageManager(configedMain, this);

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

	private void init() {
		clientInfoPanel = new ClientInfoPanel(configedMain);
		panelClientSelection = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainFrame.getClientTablePanel(),
				clientInfoPanel);
		Utils.addKeyBindingToJComponent(panelClientSelection, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
				configedMain::reloadHosts);
		Utils.addKeyBindingToJComponent(panelClientSelection,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				configedMain::invertSelection);

		panelClientSelection.setResizeWeight(1.0);

		panelLocalbootProductSettings = new PanelProductSettings(configedMain, productTree,
				ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS);

		panelNetbootProductSettings = new PanelProductSettings(configedMain, productTree,
				ProductSettingsType.NETBOOT_PRODUCT_SETTINGS);
		productTree.setPanels(panelLocalbootProductSettings, panelNetbootProductSettings);

		addTab(Configed.getResourceValue("MainFrame.panel_Clientselection"), panelClientSelection);

		addTab(Configed.getResourceValue("localbootProducts"), panelLocalbootProductSettings);

		addTab(Configed.getResourceValue("netbootProducts"), panelNetbootProductSettings);

		addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);

		addTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog"), null);

		addTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog"), showSoftwareLogNotFound);

		addTab(Configed.getResourceValue("MainFrame.jPanel_logfiles"), tabbedLogPane);

		setMinimumSize(new Dimension());
	}

	private void initSoftWareInfoTab() {
		if (panelSWInfo != null) {
			return;
		}

		panelSWInfo = new PanelSWInfo(configedMain, true);

		showSoftwareLogNotFound = new JPanel();
		showSoftwareLogNotFound.add(new JLabel(Configed.getResourceValue("MainFrame.TabRequiresClientSelected")));

		showSoftwareLogMultiClientReport = new PanelSWMultiClientReport();
		SwExporter swExporter = new SwExporter(showSoftwareLogMultiClientReport, panelSWInfo, configedMain);
		showSoftwareLogMultiClientReport.setActionListenerForStart(swExporter);
	}

	private void initHardwareInfoTab() {
		if (panelHWInfo != null) {
			return;
		}

		panelHWInfo = new PanelHWInfo(true, configedMain, this);
		setComponentAt(getSelectedIndex(), panelHWInfo);
	}

	private void initLogTab() {
		if (tabbedLogPane != null) {
			return;
		}

		tabbedLogPane = new TabbedLogPane(configedMain);
	}

	private void initHostConfigTab() {
		if (panelHostConfig != null) {
			return;
		}

		panelHostConfig = new PanelHostConfig(this::setHostConfigPage, false);

		setComponentAt(getSelectedIndex(), panelHostConfig);
	}

	private void showSoftwareInfo(JPanel showSoftwareLog) {
		setComponentAt(getSelectedIndex(), showSoftwareLog);
		showSoftwareLog.repaint();
	}

	public void setLogFileTab(String logtype) {
		setLogFileTab(logtype, false);
	}

	public void setLogFileTab(String logtype, final boolean resetCaret) {
		Logging.info(this, "setUpdatedLogfilePanel ", logtype);
		setComponentAt(getSelectedIndex(), tabbedLogPane);
		tabbedLogPane.setDocuments(logtype, resetCaret);
	}

	public void initSplitPanes() {
		panelClientSelection.setDividerLocation(DIVIDER_LOCATION);
		panelLocalbootProductSettings.setDividerLocation(DIVIDER_LOCATION);
		panelNetbootProductSettings.setDividerLocation(DIVIDER_LOCATION);
	}

	public void updateProductTab() {
		if (getSelectedIndex() == 1 || getSelectedIndex() == 2) {
			stateChanged(null);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Logging.info(this, "state change in clientConfiguration with selected index", getSelectedIndex());

		ChangedDataManager.checkSaveAll(true);

		ConfigedMain.getMainFrame().activateLoadingCursor();

		switch (getSelectedIndex()) {
		case 0:
			// This is client view, nothing needs to be done...
			break;

		case 1:
			productPageManager.setLocalbootProductsPage();
			break;

		case 2:
			productPageManager.setNetbootProductsPage();
			break;

		case 3:
			initHostConfigTab();
			setHostConfigPage();
			break;

		case 4:
			initHardwareInfoTab();
			setHardwareInfoPage();
			break;

		case 5:
			initSoftWareInfoTab();
			setSoftwareAudit();
			break;

		case 6:
			initLogTab();
			setLogPage();
			break;

		default:
			Logging.warning(this, "unexpected visualViewIndex ", getSelectedIndex(), " in clients view");
			break;
		}

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	public void setHostConfigPage() {
		Logging.info(this, "setNetworkconfigurationPage ");
		Logging.info(this, "setNetworkconfigurationPage  selectedClients ", configedMain.getSelectedClients());

		if (configUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(configUpdateCollection);
		}

		configUpdateCollection = new ConfigUpdateCollection(configedMain.getSelectedClients());
		UpdateCollectionManager.addToGlobalUpdateCollection(configUpdateCollection);

		List<Map<String, Object>> additionalConfigs = persistenceController.getConfigDataService()
				.getHostsConfigsWithDefaults(configedMain.getSelectedClients());
		Map<String, List<Object>> mergedVisualMap = ConfigedUtilityMethods.mergeMaps(additionalConfigs);
		ConfigedUtilityMethods.removeKeysStartingWith(mergedVisualMap,
				OpsiServiceNOMPersistenceController.getConfigKeyStartersNotForClients());

		Map<String, List<Object>> originalMap = ConfigedUtilityMethods.mergeMaps(persistenceController
				.getConfigDataService().getHostsConfigsWithoutDefaults(configedMain.getSelectedClients()));
		panelHostConfig.initEditing(Utils.getListStringRepresentation(configedMain.getSelectedClients()),
				mergedVisualMap, additionalConfigs, configUpdateCollection,
				OpsiServiceNOMPersistenceController.getPropertyClassesClient(), originalMap, true);
	}

	public void setHardwareInfoPage() {
		Logging.info(this, "setHardwareInfoPage for, clients count ", configedMain.getSelectedClients().size());

		if (configedMain.getSelectedClients().size() == 1) {
			setHardwareInfo(persistenceController.getHardwareDataService()
					.getHardwareInfo(configedMain.getSelectedClients().get(0)));
		} else {
			setHardwareInfoNotPossible();
		}
	}

	private void showHardwareInfo(JPanel showHardwareLog) {
		setComponentAt(getSelectedIndex(), showHardwareLog);
		showHardwareLog.repaint();
	}

	private void setHardwareInfoNotPossible() {
		Logging.info(this, "setHardwareInfoNotPossible");

		if (showHardwareLogNotFoundPanel == null) {
			showHardwareLogNotFoundPanel = new JPanel();
			showHardwareLogNotFoundPanel
					.add(new JLabel(Configed.getResourceValue("MainFrame.TabActiveForSingleClient")));
		}

		showHardwareInfo(showHardwareLogNotFoundPanel);
	}

	private void setHardwareInfo(Map<String, List<Map<String, Object>>> hardwareInfo) {
		panelHWInfo.setHardwareInfo(hardwareInfo);
		showHardwareInfo(panelHWInfo);
	}

	public void setSoftwareAudit() {
		if (configedMain.getSelectedClients().isEmpty()) {
			showSoftwareInfo(showSoftwareLogNotFound);
		} else if (configedMain.getSelectedClients().size() == 1) {
			String hostId = configedMain.getSelectedClients().getFirst();
			Logging.debug(this, "setSoftwareAudit for ", hostId);
			panelSWInfo.setAskForOverwrite(true);
			panelSWInfo.setHost(hostId);
			panelSWInfo.updateModel();

			showSoftwareInfo(panelSWInfo);
		} else {
			Logging.info(this, "setSoftwareAudit for clients ", configedMain.getSelectedClients().size());

			showSoftwareInfo(showSoftwareLogMultiClientReport);
		}
	}

	private void setLogPage() {
		Logging.debug(this, "setLogPage");
		setLogFileTab("instlog");
		tabbedLogPane.setLogview("instlog");
	}
}
