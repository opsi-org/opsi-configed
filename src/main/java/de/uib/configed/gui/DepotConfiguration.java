/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.ViewIndex;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.productpage.PanelProductProperties;
import de.uib.utils.logging.Logging;

public class DepotConfiguration extends JTabbedPane implements ChangeListener, ListSelectionListener {
	private PanelHostConfig panelHostConfig;
	private PanelHostProperties panelHostProperties;
	private PanelProductProperties panelProductProperties;

	private ConfigedMain configedMain;
	private DepotsList depotsList;

	public DepotConfiguration(ConfigedMain configedMain, DepotsList depotsList) {
		this.configedMain = configedMain;
		this.depotsList = depotsList;

		depotsList.addListSelectionListener(this);

		initTabs();

		super.addChangeListener(this);
	}

	private void initTabs() {
		addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);

		addTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"), panelProductProperties);
		Logging.info(this, "added tab  ", Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				" index ", indexOfTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		addTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties"), panelHostProperties);

		Logging.info(this, "added tab  ", Configed.getResourceValue("MainFrame.jPanel_HostProperties"), " index ",
				indexOfTab(Configed.getResourceValue("MainFrame.jPanel_HostProperties")));

		setSelectedIndex(1);
	}

	public PanelHostProperties getPanelHostProperties() {
		return panelHostProperties;
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		updateTab();
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		updateTab();
	}

	private void updateTab() {
		switch (getSelectedIndex()) {
		case 0:
			initHostConfigTab();
			configedMain.setViewIndex(ViewIndex.VIEW_NETWORK_CONFIGURATION);
			break;

		case 1:
			initPanelPropertiesTab();
			panelProductProperties.setProductProperties();
			depotsList.setEnabled(true);
			depotsList.requestFocus();
			depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			break;

		case 2:
			initHostPropertiesTab();
			configedMain.setViewIndex(ViewIndex.VIEW_HOST_PROPERTIES);
			break;

		default:
			Logging.warning(this, "unexpected visualViewIndex ", getSelectedIndex(), " in depots view");
			break;
		}
	}

	private void initHostConfigTab() {
		if (panelHostConfig != null) {
			return;
		}

		panelHostConfig = new PanelHostConfig(configedMain);
		panelHostConfig.registerDataChangedObserver(configedMain.getHostConfigsDataChangedKeeper());

		setComponentAt(getSelectedIndex(), panelHostConfig);
	}

	private void initPanelPropertiesTab() {
		if (panelProductProperties != null) {
			return;
		}

		panelProductProperties = new PanelProductProperties(configedMain, depotsList);
		setComponentAt(getSelectedIndex(), panelProductProperties);
	}

	private void initHostPropertiesTab() {
		if (panelHostProperties != null) {
			return;
		}

		panelHostProperties = new PanelHostProperties();
		panelHostProperties.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());
		setComponentAt(getSelectedIndex(), panelHostProperties);
	}
}
