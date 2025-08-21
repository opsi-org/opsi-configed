/**
 * Copyright (c) uib GmbH <info@uib.de>
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

	public DepotListSelectionListener(ConfigedMain configedMain, DepotsList depotsList,
			InitialDataLoader initialDataLoader) {
		Logging.info(this, "DepotListSelectionListener constructor called");
		this.configedMain = configedMain;
		this.depotsList = depotsList;
		this.initialDataLoader = initialDataLoader;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		counter++;
		Logging.info(this, "depotSelection event count  ", counter);

		if (!e.getValueIsAdjusting()) {
			depotsListValueChanged();
		}
	}

	private void depotsListValueChanged() {
		Logging.info(this, "depotsList selection changed");

		Configed.getSavedStates().setProperty("selectedDepots", depotsList.getSelectedValuesList().toString());

		Logging.info(this, " depotsList_valueChanged, omitted initialTreeActivation");

		// when running after the first run, we deactivate buttons
		if (initialDataLoader.isDataLoaded()) {
			PersistenceControllerFactory.getPersistenceController().getHostInfoCollections()
					.updateClientsForDepots(depotsList.getSelectedValuesList(), configedMain.getAllowedClients());
			configedMain.initialTreeActivation();

			configedMain.getProductTree().reInitTree();
			configedMain.refreshClientListKeepingGroup();

			configedMain.initTabComponents();

			configedMain.setDepotRepresentative(depotsList.getSelectedValuesList());
		}
	}
}
