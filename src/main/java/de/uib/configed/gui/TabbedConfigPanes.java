/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.hwinfopage.ControllerHWinfoMultiClients;
import de.uib.configed.gui.hwinfopage.PanelHWInfo;
import de.uib.configed.gui.productpage.PanelProductProperties;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.gui.swinfopage.PanelSWInfo;
import de.uib.configed.gui.swinfopage.PanelSWMultiClientReport;
import de.uib.configed.tree.ProductTree;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import utils.PopupMouseListener;
import utils.Utils;

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
	private JPanel showHardwareLog;
	private JPanel showSoftwareLog;
	private JLabel labelNoSoftware;

	private PanelHWInfo panelHWInfo;
	private JPanel showHardwareLogNotFound;
	private ControllerHWinfoMultiClients controllerHWinfoMultiClients;
	private JPanel showHardwareLogMultiClientReport;
	private JPanel showHardwareLogParentOfNotFoundPanel;

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

	public ControllerHWinfoMultiClients getControllerHWinfoMultiClients() {
		return controllerHWinfoMultiClients;
	}

	public ClientInfoPanel getClientInfoPanel() {
		return clientInfoPanel;
	}

	public JSplitPane getPanelClientSelection() {
		return panelClientSelection;
	}

	private void init() {
		setBorder(new EmptyBorder(0, 0, 0, Globals.MIN_GAP_SIZE));

		addChangeListener(this);

		popupClients = mainFrame.getClientMenu().getPopupMenuClone();
		mainFrame.getClientTable().addMouseListener(new PopupMouseListener(popupClients));

		clientInfoPanel = new ClientInfoPanel(configedMain);
		panelClientSelection = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainFrame.getClientTable(), clientInfoPanel);

		insertTab(Configed.getResourceValue("MainFrame.panel_Clientselection"), null, panelClientSelection, null,
				ConfigedMain.VIEW_CLIENTS);

		panelLocalbootProductSettings = new PanelProductSettings(
				Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), configedMain, productTree,
				configedMain.getDisplayFieldsLocalbootProducts(),
				PanelProductSettings.ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS);

		panelNetbootProductSettings = new PanelProductSettings(
				Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), configedMain, productTree,
				configedMain.getDisplayFieldsNetbootProducts(),
				PanelProductSettings.ProductSettingsType.NETBOOT_PRODUCT_SETTINGS);
		productTree.setPanels(panelLocalbootProductSettings, panelNetbootProductSettings);

		insertTab(Configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"), null,
				panelLocalbootProductSettings, null, ConfigedMain.VIEW_LOCALBOOT_PRODUCTS);

		insertTab(Configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), null,
				panelNetbootProductSettings, null, ConfigedMain.VIEW_NETBOOT_PRODUCTS);

		panelHostConfig = new PanelHostConfig(configedMain);

		panelHostConfig.registerDataChangedObserver(configedMain.getHostConfigsDataChangedKeeper());

		insertTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), null, panelHostConfig, null,
				ConfigedMain.VIEW_NETWORK_CONFIGURATION);

		showHardwareLog = new JPanel();

		insertTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog"), null, showHardwareLog, null,
				ConfigedMain.VIEW_HARDWARE_INFO);

		initSoftWareInfo();
		initHardwareInfo();

		labelNoSoftware = new JLabel();

		showSoftwareLogNotFound = new JPanel(new FlowLayout());
		showSoftwareLogNotFound.add(labelNoSoftware);

		showSoftwareLog = showSoftwareLogNotFound;

		showSoftwareLogMultiClientReport = new PanelSWMultiClientReport();
		SwExporter swExporter = new SwExporter(showSoftwareLogMultiClientReport, panelSWInfo, configedMain);
		showSoftwareLogMultiClientReport.setActionListenerForStart(swExporter);

		insertTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog"), null, showSoftwareLog, null,
				ConfigedMain.VIEW_SOFTWARE_INFO);

		showLogfiles = new TabbedLogPane(configedMain) {
			@Override
			public void loadDocument(String logtype) {
				super.loadDocument(logtype);
				Logging.info(this, "loadDocument logtype " + logtype);
				setUpdatedLogfilePanel(logtype);
			}
		};

		insertTab(Configed.getResourceValue("MainFrame.jPanel_logfiles"), null, showLogfiles, null,
				ConfigedMain.VIEW_LOG);

		showLogfiles.addChangeListener((ChangeEvent e) -> {
			Logging.debug(this, " new logfiles tabindex " + showLogfiles.getSelectedIndex());

			String logtype = Utils.getLogType(showLogfiles.getSelectedIndex());

			// logfile empty?
			if (!configedMain.logfileExists(logtype)) {
				setUpdatedLogfilePanel(logtype);
			}
		});

		panelProductProperties = new PanelProductProperties(configedMain);

		insertTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"), null, panelProductProperties,
				null, ConfigedMain.VIEW_PRODUCT_PROPERTIES);

		Logging.info(this, "added tab  " + Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")
				+ " index " + indexOfTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		panelHostProperties = new PanelHostProperties();
		panelHostProperties.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());

		insertTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties"), null, panelHostProperties, null,
				ConfigedMain.VIEW_HOST_PROPERTIES);

		Logging.info(this, "added tab  " + Configed.getResourceValue("MainFrame.jPanel_HostProperties") + " index "
				+ indexOfTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties")));

		setSelectedIndex(0);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// report state change request to
		int visualIndex = getSelectedIndex();

		// report state change request to controller

		Logging.info(this, "stateChanged of tabbedPane, visualIndex " + visualIndex);
		configedMain.setViewIndex(visualIndex);

		// retrieve the state index finally produced by main
		int newStateIndex = configedMain.getViewIndex();

		// if the controller did not accept the new index set it back
		// observe that we get a recursion since we initiate another state change
		// the recursion breaks since main.setViewIndex does not yield a different value
		if (visualIndex != newStateIndex) {
			setSelectedIndex(newStateIndex);
		}
	}

	private void initSoftWareInfo() {
		panelSWInfo = new PanelSWInfo(true) {
			@Override
			protected void reload() {
				super.reload();
				persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
				configedMain.resetView(ConfigedMain.VIEW_SOFTWARE_INFO);
			}
		};
	}

	private void initHardwareInfo() {
		if (panelHWInfo == null) {
			panelHWInfo = new PanelHWInfo(configedMain) {
				@Override
				protected void reload() {
					super.reload();
					// otherwise we get a wait cursor only in table component
					configedMain.resetView(ConfigedMain.VIEW_HARDWARE_INFO);
				}
			};
		}
	}

	public void setSoftwareAudit() {
		if (configedMain.getSelectedClients() != null && configedMain.getSelectedClients().size() > 1) {
			Logging.info(this, "setSoftwareAudit for clients " + configedMain.getSelectedClients().size());

			showSoftwareLog = showSoftwareLogMultiClientReport;
			showSoftwareInfo();
		} else {
			// handled by the following methods
			labelNoSoftware.setText(Configed.getResourceValue("MainFrame.TabRequiresClientSelected"));
			showSoftwareLog = showSoftwareLogNotFound;
			showSoftwareInfo();
		}
	}

	public void setSoftwareAudit(String hostId) {
		labelNoSoftware.setText(Configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));

		Logging.debug(this, "setSoftwareAudit for " + hostId);
		panelSWInfo.setAskForOverwrite(true);
		panelSWInfo.setHost(hostId);
		panelSWInfo.updateModel();

		showSoftwareLog = panelSWInfo;

		showSoftwareInfo();
	}

	private void showHardwareInfo() {
		setComponentAt(indexOfTab(Configed.getResourceValue("MainFrame.jPanel_hardwareLog")), showHardwareLog);

		showHardwareLog.repaint();
	}

	public void setHardwareInfoNotPossible(String label) {
		Logging.info(this, "setHardwareInfoNotPossible");

		if (showHardwareLogNotFound == null || showHardwareLogParentOfNotFoundPanel == null) {
			showHardwareLogNotFound = new JPanel();
			showHardwareLogNotFound.add(new JLabel(label));
			showHardwareLogParentOfNotFoundPanel = new JPanel();

			showHardwareLogParentOfNotFoundPanel.setLayout(new BorderLayout());
			showHardwareLogParentOfNotFoundPanel.add(showHardwareLogNotFound);
		}

		showHardwareLog = showHardwareLogParentOfNotFoundPanel;
		showHardwareInfo();
	}

	public void setHardwareInfoMultiClients() {
		if (showHardwareLogMultiClientReport == null || controllerHWinfoMultiClients == null) {
			controllerHWinfoMultiClients = new ControllerHWinfoMultiClients(configedMain);
			showHardwareLogMultiClientReport = controllerHWinfoMultiClients.getPanel();
		}

		Logging.info(this, "setHardwareInfoMultiClients ");

		controllerHWinfoMultiClients.setFilter();
		showHardwareLog = showHardwareLogMultiClientReport;

		showHardwareInfo();
	}

	public void setHardwareInfo(Map<String, List<Map<String, Object>>> hardwareInfo) {
		panelHWInfo.setHardwareInfo(hardwareInfo);

		showHardwareLog = panelHWInfo;
		showHardwareInfo();
	}

	private void showSoftwareInfo() {
		setComponentAt(indexOfTab(Configed.getResourceValue("MainFrame.jPanel_softwareLog")), showSoftwareLog);
		SwingUtilities.invokeLater(() -> ConfigedMain.getMainFrame().repaint());
	}

	public void setUpdatedLogfilePanel(String logtype) {
		Logging.info(this, "setUpdatedLogfilePanel " + logtype);
		setLogfilePanel(configedMain.getLogfilesUpdating(logtype));
	}

	public void setLogfilePanel(final Map<String, String> logs) {
		setComponentAt(indexOfTab(Configed.getResourceValue("MainFrame.jPanel_logfiles")), showLogfiles);
		showLogfiles.setDocuments(logs, mainFrame.getHostsStatusPanel().getSelectedClientNames());
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

	public void setConfigPanesEnabled(boolean b) {
		for (int i = 0; i < getTabCount(); i++) {
			setEnabledAt(i, b);
		}
	}

	public void setVisualViewIndex(int i) {
		if (i >= 0 && i < getTabCount()) {
			setSelectedIndex(i);
		}
	}
}
