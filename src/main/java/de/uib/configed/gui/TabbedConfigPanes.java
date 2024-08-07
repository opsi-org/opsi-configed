/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.EditingTarget;
import de.uib.configed.ConfigedMain.ViewIndex;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.hwinfopage.PanelHWInfo;
import de.uib.configed.gui.productpage.PanelProductProperties;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.gui.swinfopage.PanelSWInfo;
import de.uib.configed.gui.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.tree.ProductTree;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.PopupMouseListener;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class TabbedConfigPanes extends JTabbedPane implements ChangeListener {
	private ConfigedMain configedMain;
	private MainFrame mainFrame;
	private ProductTree productTree;

	private PanelProductSettings panelLocalbootProductSettings;
	private PanelProductSettings panelNetbootProductSettings;
	private PanelHostConfig panelHostConfig;
	private PanelHostProperties panelHostProperties;
	private PanelProductProperties panelProductProperties;

	private PanelSWInfo panelSWInfo;
	private JPanel showSoftwareLogNotFound;
	private PanelSWMultiClientReport showSoftwareLogMultiClientReport;
	private JLabel labelNoSoftware;

	private PanelHWInfo panelHWInfo;
	private JPanel showHardwareLogNotFoundPanel;

	private TabbedLogPane showLogfiles;
	private JSplitPane panelClientSelection;
	private ClientInfoPanel clientInfoPanel;

	private JPopupMenu popupClients;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public TabbedConfigPanes(ConfigedMain configedMain, MainFrame mainFrame, ProductTree productTree) {
		this.configedMain = configedMain;
		this.mainFrame = mainFrame;
		this.productTree = productTree;

		init();
	}

	public PanelProductSettings getPanelLocalbootProductSettings() {
		return panelLocalbootProductSettings;
	}

	public PanelProductSettings getPanelNetbootProductSettings() {
		return panelNetbootProductSettings;
	}

	public PanelHostConfig getPanelHostConfig() {
		return panelHostConfig;
	}

	public PanelHostProperties getPanelHostProperties() {
		return panelHostProperties;
	}

	public PanelProductProperties getPanelProductProperties() {
		return panelProductProperties;
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

		initHostConfigTab();
		initSoftWareInfo();
		initHardwareInfo();
		initLogTab();

		panelProductProperties = new PanelProductProperties(configedMain);

		initHostPropertiesTab();

		initView(EditingTarget.CLIENTS, 0);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// report state change request to
		int visualIndex = getSelectedIndex();

		// report state change request to controller

		Logging.info(this, "stateChanged of tabbedPane, visualIndex ", visualIndex);
		configedMain.setViewIndex(visualIndex);
	}

	private void initSoftWareInfo() {
		panelSWInfo = new PanelSWInfo(true) {
			@Override
			protected void reload() {
				super.reload();
				persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
				configedMain.resetView(ViewIndex.VIEW_SOFTWARE_INFO);
			}
		};

		labelNoSoftware = new JLabel();

		showSoftwareLogNotFound = new JPanel();
		showSoftwareLogNotFound.add(labelNoSoftware);

		showSoftwareLogMultiClientReport = new PanelSWMultiClientReport();
		SwExporter swExporter = new SwExporter(showSoftwareLogMultiClientReport, panelSWInfo, configedMain);
		showSoftwareLogMultiClientReport.setActionListenerForStart(swExporter);
	}

	private void initHardwareInfo() {
		if (panelHWInfo == null) {
			panelHWInfo = new PanelHWInfo(configedMain) {
				@Override
				protected void reload() {
					super.reload();
					// otherwise we get a wait cursor only in table component
					configedMain.resetView(ViewIndex.VIEW_HARDWARE_INFO);
				}
			};
		}
	}

	private void initLogTab() {
		showLogfiles = new TabbedLogPane(configedMain) {
			@Override
			public void loadDocument(String logtype) {
				super.loadDocument(logtype);
				Logging.info(this, "loadDocument logtype ", logtype);
				setUpdatedLogfilePanel(logtype);
			}
		};

		showLogfiles.addChangeListener((ChangeEvent e) -> {
			Logging.debug(this, " new logfiles tabindex ", showLogfiles.getSelectedIndex());

			String logtype = Utils.getLogType(showLogfiles.getSelectedIndex());

			// logfile empty?
			if (!configedMain.logfileExists(logtype)) {
				setUpdatedLogfilePanel(logtype);
			}
		});
	}

	private void initHostConfigTab() {
		panelHostConfig = new PanelHostConfig(configedMain);
		panelHostConfig.registerDataChangedObserver(configedMain.getHostConfigsDataChangedKeeper());
	}

	private void initHostPropertiesTab() {
		panelHostProperties = new PanelHostProperties();
		panelHostProperties.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());
	}

	public void setSoftwareAudit() {
		if (configedMain.getSelectedClients() != null && configedMain.getSelectedClients().size() > 1) {
			Logging.info(this, "setSoftwareAudit for clients ", configedMain.getSelectedClients().size());

			showSoftwareInfo(showSoftwareLogMultiClientReport);
		} else {
			// handled by the following methods
			labelNoSoftware.setText(Configed.getResourceValue("MainFrame.TabRequiresClientSelected"));
			showSoftwareInfo(showSoftwareLogNotFound);
		}
	}

	public void setSoftwareAudit(String hostId) {
		labelNoSoftware.setText(Configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));

		Logging.debug(this, "setSoftwareAudit for ", hostId);
		panelSWInfo.setAskForOverwrite(true);
		panelSWInfo.setHost(hostId);
		panelSWInfo.updateModel();

		showSoftwareInfo(panelSWInfo);
	}

	private void showHardwareInfo(JPanel showHardwareLog) {
		setComponentAt(indexOfTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog")), showHardwareLog);

		showHardwareLog.repaint();
	}

	public void setHardwareInfoNotPossible() {
		Logging.info(this, "setHardwareInfoNotPossible");

		if (showHardwareLogNotFoundPanel == null) {
			showHardwareLogNotFoundPanel = new JPanel();
			showHardwareLogNotFoundPanel
					.add(new JLabel(Configed.getResourceValue("MainFrame.TabActiveForSingleClient")));
		}

		showHardwareInfo(showHardwareLogNotFoundPanel);
	}

	public void setHardwareInfo(Map<String, List<Map<String, Object>>> hardwareInfo) {
		panelHWInfo.setHardwareInfo(hardwareInfo);

		showHardwareInfo(panelHWInfo);
	}

	private void showSoftwareInfo(JPanel showSoftwareLog) {
		setComponentAt(indexOfTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog")), showSoftwareLog);
		SwingUtilities.invokeLater(() -> ConfigedMain.getMainFrame().repaint());
	}

	public void setUpdatedLogfilePanel(String logtype) {
		Logging.info(this, "setUpdatedLogfilePanel ", logtype);
		setComponentAt(indexOfTab(Configed.getResourceValue("MainFrame.jPanel_logfiles")), showLogfiles);
		showLogfiles.setDocuments(configedMain.getLogfilesUpdating(logtype),
				mainFrame.getHostsStatusPanel().getSelectedClientNames());
	}

	public void setLogview(String logtype) {
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
		panelClientSelection.setDividerLocation(0.8);
		panelLocalbootProductSettings.setDividerLocation(0.8);
		panelNetbootProductSettings.setDividerLocation(0.8);
		panelProductProperties.setDividerLocation(0.8);
	}

	public void initView(EditingTarget editingTarget, int view) {
		Logging.debug(this, "initView for editing target ", editingTarget, "and view", view);

		removeChangeListener(this);
		removeAll();

		switch (editingTarget) {
		case CLIENTS:
			addClientTabs();
			break;

		case DEPOTS:
			addDepotTabs();
			break;

		case SERVER:
			addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);
			break;
		}

		setSelectedIndex(view);

		addChangeListener(this);

		fireStateChanged();
	}

	private void addClientTabs() {
		addTab(Configed.getResourceValue("MainFrame.panel_Clientselection"), panelClientSelection);

		addTab(Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), panelLocalbootProductSettings);

		addTab(Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), panelNetbootProductSettings);

		addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);

		addTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog"), new JPanel());

		addTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog"), showSoftwareLogNotFound);

		addTab(Configed.getResourceValue("MainFrame.jPanel_logfiles"), showLogfiles);
	}

	private void addDepotTabs() {
		addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);

		addTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"), panelProductProperties);
		Logging.info(this, "added tab  ", Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				" index ", indexOfTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		addTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties"), panelHostProperties);

		Logging.info(this, "added tab  ", Configed.getResourceValue("MainFrame.jPanel_HostProperties"), " index ",
				indexOfTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties")));

	}

	public void setVisualViewIndex(int i) {
		if (i >= 0 && i < getTabCount()) {
			setSelectedIndex(i);
		}
	}
}
