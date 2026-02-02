/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.gui.features.hostconfigs.PanelConfigurationHostConfig;
import de.uib.configed.gui.features.productpage.PanelProductProperties;
import de.uib.configed.share.logging.Logging;

public class DepotConfiguration extends JTabbedPane implements ChangeListener, ListSelectionListener {
	private static final int INITIAL_SELECTED_TAB = 1;

	private PanelConfigurationHostConfig panelHostConfig;

	private PanelHostProperties panelHostProperties;

	private PanelProductProperties panelProductProperties;

	private ConfigedMain configedMain;
	private DepotsList depotsList;

	private int lastSelectedTab = INITIAL_SELECTED_TAB;
	private int[] lastSelectedDepots;

	public DepotConfiguration(ConfigedMain configedMain, DepotsList depotsList) {
		this.configedMain = configedMain;
		this.depotsList = depotsList;

		initTabs();

		// At the beginning, we want to have the same depots selected as in the client configuration
		depotsList.setSelectedValues(configedMain.getSelectedDepots());
		lastSelectedDepots = depotsList.getSelectedIndices();
		updateTab();

		depotsList.addListSelectionListener(this);

		super.addChangeListener(this);
	}

	private void initTabs() {
		addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);

		addTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"), panelProductProperties);
		Logging.info(this, "added tab  ", Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
				" index ", indexOfTab(Configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

		addTab(Configed.getResourceValue("depotConfiguration"), panelHostProperties);

		Logging.info(this, "added tab  ", Configed.getResourceValue("depotConfiguration"), " index ",
				indexOfTab(Configed.getResourceValue("depotConfiguration")));

		setSelectedIndex(INITIAL_SELECTED_TAB);

		setMinimumSize(new Dimension());
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		if (ChangedDataManager.checkSaveAll(true)) {
			updateTab();
			lastSelectedTab = getSelectedIndex();
		} else {
			removeChangeListener(this);
			setSelectedIndex(lastSelectedTab);
			addChangeListener(this);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (!event.getValueIsAdjusting()) {
			Logging.info(this, "value changed of depot selection, update tab in depot configuration");
			if (ChangedDataManager.checkSaveAll(true)) {
				updateTab();
				lastSelectedDepots = depotsList.getSelectedIndices();
			} else {
				depotsList.removeListSelectionListener(this);
				depotsList.setSelectedIndices(lastSelectedDepots);
				depotsList.addListSelectionListener(this);
			}
		}
	}

	private void updateTab() {
		ConfigedMain.getMainFrame().activateLoadingCursor();

		depotsList.requestFocus();

		switch (getSelectedIndex()) {
		case 0 -> {
			initHostConfigTab();
			panelHostConfig.updateTab(depotsList.getSelectedIndices().length);
		}
		case 1 -> {
			initProductPropertiesTab();
			panelProductProperties.updateTab(depotsList.getSelectedIndices().length);
		}
		case 2 -> {
			initHostParameterTab();
			panelHostProperties.updateTab(depotsList.getSelectedIndices().length);
		}
		default -> Logging.warning(this, "unexpected visualViewIndex ", getSelectedIndex(), " in depots view");
		}

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private void initHostConfigTab() {
		if (panelHostConfig != null) {
			return;
		}

		panelHostConfig = new PanelConfigurationHostConfig(false, depotsList::getSelectedValuesList);
		setComponentAt(getSelectedIndex(), panelHostConfig);
	}

	private void initProductPropertiesTab() {
		if (panelProductProperties != null) {
			return;
		}

		panelProductProperties = new PanelProductProperties(configedMain, depotsList);
		setComponentAt(getSelectedIndex(), panelProductProperties);
	}

	private void initHostParameterTab() {
		if (panelHostProperties != null) {
			return;
		}

		panelHostProperties = new PanelHostProperties(depotsList::getSelectedValue);
		setComponentAt(getSelectedIndex(), panelHostProperties);
	}
}
