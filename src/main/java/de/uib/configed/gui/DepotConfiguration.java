/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.ChangedDataManager;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedUtilityMethods;
import de.uib.configed.UpdateCollectionManager;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.configed.gui.productpage.PanelProductProperties;
import de.uib.opsidatamodel.datachanges.ConfigUpdateCollection;
import de.uib.opsidatamodel.datachanges.HostUpdateCollection;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class DepotConfiguration extends JTabbedPane implements ChangeListener, ListSelectionListener {
	private ConfigUpdateCollection configUpdateCollection;

	private PanelHostConfig panelHostConfig;

	private PanelHostProperties panelHostProperties;
	private HostUpdateCollection hostUpdateCollection;

	private PanelProductProperties panelProductProperties;

	private ConfigedMain configedMain;
	private DepotsList depotsList;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public DepotConfiguration(ConfigedMain configedMain, DepotsList depotsList) {
		this.configedMain = configedMain;
		this.depotsList = depotsList;

		initTabs();

		// At the beginning, we want to have the same depots selected as in the client configuration
		depotsList.setSelectedValues(configedMain.getSelectedDepots());
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

		setSelectedIndex(1);
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		updateTab();
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (!event.getValueIsAdjusting()) {
			Logging.info(this, "value changed of depot selection, update tab in depot configuration");
			updateTab();
		}
	}

	private void updateTab() {
		ConfigedMain.getMainFrame().activateLoadingCursor();

		depotsList.requestFocus();

		switch (getSelectedIndex()) {
		case 0:
			initHostConfigTab();
			setHostConfigTab();
			break;

		case 1:
			initProductPropertiesTab();
			panelProductProperties.setProductProperties();
			depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			break;

		case 2:
			initHostParameterTab();
			setHostParameterPage();
			break;

		default:
			Logging.warning(this, "unexpected visualViewIndex ", getSelectedIndex(), " in depots view");
			break;
		}

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private void initHostConfigTab() {
		if (panelHostConfig != null) {
			return;
		}

		panelHostConfig = new PanelHostConfig(this::setHostConfigTab, false);

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

		panelHostProperties = new PanelHostProperties();
		panelHostProperties.registerDataChangedObserver(ChangedDataManager.getGeneralDataChangedKeeper());
		setComponentAt(getSelectedIndex(), panelHostProperties);
	}

	private void setHostParameterPage() {
		Logging.debug(this, "setHostPropertiesPage");

		depotsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		Map<String, Map<String, Object>> depotPropertiesForPermittedDepots = persistenceController.getDepotDataService()
				.getDepotPropertiesForPermittedDepots();

		if (hostUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(hostUpdateCollection);
		}

		String depot = "";
		if (!depotsList.getSelectedValuesList().isEmpty()) {
			depot = depotsList.getSelectedValuesList().get(0);
		}

		hostUpdateCollection = new HostUpdateCollection(depot, depotPropertiesForPermittedDepots.get(depot));
		UpdateCollectionManager.addToGlobalUpdateCollection(hostUpdateCollection);

		panelHostProperties.initMultipleHostsEditing(depotPropertiesForPermittedDepots.get(depot), hostUpdateCollection,
				OpsiServiceNOMPersistenceController.KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT);
	}

	private void setHostConfigTab() {
		Logging.info(this, "setHostConfigTab  selected Depots ", depotsList.getSelectedValuesList());

		if (configUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(configUpdateCollection);
		}

		configUpdateCollection = new ConfigUpdateCollection(depotsList.getSelectedValuesList());
		UpdateCollectionManager.addToGlobalUpdateCollection(configUpdateCollection);

		depotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		List<Map<String, Object>> additionalConfigs = persistenceController.getConfigDataService()
				.getHostsConfigsWithDefaults(depotsList.getSelectedValuesList());
		Map<String, List<Object>> mergedVisualMap = ConfigedUtilityMethods.mergeMaps(additionalConfigs);
		ConfigedUtilityMethods.removeKeysStartingWith(mergedVisualMap,
				OpsiServiceNOMPersistenceController.getConfigKeyStartersNotForClients());
		Map<String, List<Object>> originalMap = ConfigedUtilityMethods.mergeMaps(persistenceController
				.getConfigDataService().getHostsConfigsWithoutDefaults(depotsList.getSelectedValuesList()));
		panelHostConfig.initEditing(Utils.getListStringRepresentation(depotsList.getSelectedValuesList()),
				mergedVisualMap, additionalConfigs, configUpdateCollection,
				OpsiServiceNOMPersistenceController.getPropertyClassesClient(), originalMap, false);
	}
}
