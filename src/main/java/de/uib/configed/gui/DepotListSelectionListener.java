/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.share.logging.Logging;

public class DepotListSelectionListener implements ListSelectionListener {
	private int counter;

	private ConfigedMain configedMain;
	private DepotsList depotsList;
	private InitialDataLoader initialDataLoader;

	private int[] lastSelectedIndices = new int[0];

	public DepotListSelectionListener(ConfigedMain configedMain, DepotsList depotsList,
			InitialDataLoader initialDataLoader) {
		Logging.info(this, "DepotListSelectionListener constructor called");
		this.configedMain = configedMain;
		this.depotsList = depotsList;
		this.initialDataLoader = initialDataLoader;

		this.lastSelectedIndices = depotsList.getSelectedIndices();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		counter++;
		Logging.info(this, "depotSelection event count  ", counter);

		if (!e.getValueIsAdjusting()) {
			if (ChangedDataManager.checkSaveAll(true)) {
				depotsListValueChanged();
				lastSelectedIndices = depotsList.getSelectedIndices();
			} else {
				Logging.info(this, "depotSelection event ignored due to unsaved changes");
				depotsList.removeListSelectionListener(this);
				depotsList.setSelectedIndices(lastSelectedIndices);
				depotsList.addListSelectionListener(this);
			}
		}
	}

	private void depotsListValueChanged() {
		Logging.info(this, "depotsList selection changed");

		Configed.getSavedStates().setProperty("selectedDepots", depotsList.getSelectedValuesList().toString());

		Logging.info(this, " depotsList_valueChanged, omitted initialTreeActivation");

		// when running after the first run, we deactivate buttons
		if (initialDataLoader.isDataLoaded()) {
			PersistenceControllerFactory.getPersistenceController().getDataServices().hostInfoCollections
					.updateClientsForDepots(depotsList.getSelectedValuesList(), configedMain.getAllowedClients());
			configedMain.initialTreeActivation();

			configedMain.getProductTree().reInitTree();
			configedMain.refreshClientListKeepingGroup();

			configedMain.initTabComponents();

			ClientConfiguration clientConfiguration = ConfigedMain.getMainFrame().getMainPanelManager()
					.getClientConfiguration();

			if (clientConfiguration.getSelectedIndex() == 1) {
				clientConfiguration.getProductPageManager().setLocalbootProductsPage();
			} else if (clientConfiguration.getSelectedIndex() == 2) {
				clientConfiguration.getProductPageManager().setNetbootProductsPage();
			} else {
				// Do nothing.
			}
		}
	}
}
