/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedUtilityMethods;
import de.uib.configed.ProductPageManager;
import de.uib.configed.UpdateCollectionManager;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.hwinfopage.PanelHWInfo;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.gui.swinfopage.PanelSWInfo;
import de.uib.configed.gui.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.tree.ProductTree;
import de.uib.opsidatamodel.datachanges.ConfigUpdateCollection;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.PopupMouseListener;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.ListCellOptions;

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

	private TabbedLogPane showLogfiles;
	private JSplitPane panelClientSelection;
	private ClientInfoPanel clientInfoPanel;

	private JPopupMenu popupClients;

	private Map<String, String> logfiles = new HashMap<>();

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

	public PanelHostConfig getPanelHostConfig() {
		return panelHostConfig;
	}

	public ClientInfoPanel getClientInfoPanel() {
		return clientInfoPanel;
	}

	private void init() {
		popupClients = mainFrame.getClientMenu().getPopupMenuClone();
		mainFrame.getClientTable().addMouseListener(new PopupMouseListener(popupClients));

		clientInfoPanel = new ClientInfoPanel(configedMain);
		panelClientSelection = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainFrame.getClientTable(), clientInfoPanel);
		panelClientSelection.setResizeWeight(1.0);

		panelLocalbootProductSettings = new PanelProductSettings(
				Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), configedMain, productTree,
				PanelProductSettings.ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS);

		panelNetbootProductSettings = new PanelProductSettings(
				Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), configedMain, productTree,
				PanelProductSettings.ProductSettingsType.NETBOOT_PRODUCT_SETTINGS);
		productTree.setPanels(panelLocalbootProductSettings, panelNetbootProductSettings);

		addTab(Configed.getResourceValue("MainFrame.panel_Clientselection"), panelClientSelection);

		addTab(Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), panelLocalbootProductSettings);

		addTab(Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), panelNetbootProductSettings);

		addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);

		addTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog"), null);

		addTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog"), showSoftwareLogNotFound);

		addTab(Configed.getResourceValue("MainFrame.jPanel_logfiles"), showLogfiles);
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
		if (showLogfiles != null) {
			return;
		}

		showLogfiles = new TabbedLogPane(configedMain);

		showLogfiles.addChangeListener((ChangeEvent e) -> {
			Logging.debug(this, " new logfiles tabindex ", showLogfiles.getSelectedIndex());

			String logtype = Utils.getLogType(showLogfiles.getSelectedIndex());

			// logfile empty?
			if (!logfileExists(logtype)) {
				setLogFileTab(logtype);
			}
		});
	}

	private void initHostConfigTab() {
		if (panelHostConfig != null) {
			return;
		}

		panelHostConfig = new PanelHostConfig(configedMain, this::setHostParameterPage);

		setComponentAt(getSelectedIndex(), panelHostConfig);
	}

	private void showSoftwareInfo(JPanel showSoftwareLog) {
		setComponentAt(getSelectedIndex(), showSoftwareLog);
		showSoftwareLog.repaint();
	}

	public void setLogFileTab(String logtype) {
		Logging.info(this, "setUpdatedLogfilePanel ", logtype);
		setComponentAt(getSelectedIndex(), showLogfiles);
		showLogfiles.setDocuments(getLogfilesUpdating(logtype),
				mainFrame.getHostsStatusPanel().getSelectedClientNames());
	}

	public boolean logfileExists(String logtype) {
		return logfiles != null && logfiles.get(logtype) != null && !logfiles.get(logtype).isEmpty()
				&& !logfiles.get(logtype).equals(Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
	}

	public Map<String, String> getLogfilesUpdating(String logtypeToUpdate) {
		Logging.info(this, "getLogfilesUpdating ", logtypeToUpdate);

		if (configedMain.getSelectedClients().size() == 1) {
			logfiles = persistenceController.getLogDataService().getLogfile(configedMain.getSelectedClients().get(0),
					logtypeToUpdate);
			Logging.debug(this, "log pages set");
		} else {
			for (String logType : Utils.getLogTypes()) {
				logfiles.put(logType, Configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
			}
		}

		return logfiles;
	}

	private void setLogview(String logtype) {
		int i = Arrays.asList(Utils.getLogTypes()).indexOf(logtype);
		if (i < 0) {
			return;
		}

		showLogfiles.setSelectedIndex(i);
	}

	public void showPopupClients() {
		popupClients.show(mainFrame.getClientTable(), -1, -1);
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

		configedMain.checkSaveAll(true);

		mainFrame.activateLoadingCursor();

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
			setHostParameterPage();
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

		mainFrame.deactivateLoadingCursor();
	}

	public void setHostParameterPage() {
		Logging.info(this, "setNetworkconfigurationPage ");
		Logging.info(this, "setNetworkconfigurationPage  selectedClients ", configedMain.getSelectedClients());

		if (configUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(configUpdateCollection);
		}

		configUpdateCollection = new ConfigUpdateCollection(configedMain.getSelectedClients());
		UpdateCollectionManager.addToGlobalUpdateCollection(configUpdateCollection);

		List<Map<String, Object>> additionalConfigs = configedMain
				.produceAdditionalConfigs(configedMain.getSelectedClients());
		Map<String, Object> mergedVisualMap = ConfigedUtilityMethods.mergeMaps(additionalConfigs);
		ConfigedUtilityMethods.removeKeysStartingWith(mergedVisualMap,
				OpsiServiceNOMPersistenceController.getConfigKeyStartersNotForClients());
		Map<String, ListCellOptions> configListCellOptions = deepCopyConfigListCellOptions(
				persistenceController.getConfigDataService().getConfigListCellOptionsPD());
		if (!configedMain.getSelectedClients().isEmpty()) {
			List<String> depotIds = new ArrayList<>();
			depotIds.add(persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps()
					.get(configedMain.getSelectedClients().get(0)).getInDepot());
			Map<String, Object> defaultValues = persistenceController.getConfigDataService()
					.getHostsConfigsWithDefaults(depotIds).get(0);
			for (Entry<String, ListCellOptions> entry : configListCellOptions.entrySet()) {
				configListCellOptions.get(entry.getKey())
						.setDefaultValues((List<Object>) defaultValues.get(entry.getKey()));
			}
		}
		Map<String, Object> originalMap = ConfigedUtilityMethods.mergeMaps(persistenceController.getConfigDataService()
				.getHostsConfigsWithoutDefaults(configedMain.getSelectedClients()));
		panelHostConfig.initEditing(Utils.getListStringRepresentation(configedMain.getSelectedClients(), null),
				mergedVisualMap, configListCellOptions, additionalConfigs, configUpdateCollection, false,
				OpsiServiceNOMPersistenceController.getPropertyClassesClient(), originalMap, true);
	}

	private static Map<String, ListCellOptions> deepCopyConfigListCellOptions(
			Map<String, ListCellOptions> originalMap) {
		Map<String, ListCellOptions> copy = new HashMap<>();
		for (Entry<String, ListCellOptions> entry : originalMap.entrySet()) {
			copy.put(entry.getKey(), entry.getValue().deepCopy());
		}
		return copy;
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
		setLogview("instlog");
	}
}
