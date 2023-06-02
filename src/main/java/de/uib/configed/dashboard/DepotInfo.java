/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard;

import java.util.Map;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.collector.ClientData;
import de.uib.configed.dashboard.collector.DepotData;
import de.uib.configed.dashboard.collector.ProductData;
import de.uib.configed.dashboard.view.MainView;
import de.uib.configed.type.HostInfo;
import de.uib.utilities.logging.Logging;

public class DepotInfo implements DataChangeListener {
	private MainView controller;
	private String selectedDepot;

	public DepotInfo(MainView controller) {
		this.controller = controller;
	}

	@Override
	public void display() {
		if (controller.selectedDepotComboBox.getItems().isEmpty()) {
			controller.selectedDepotComboBox.getItems().add(Configed.getResourceValue("Dashboard.selection.allDepots"));
			controller.selectedDepotComboBox.setValue(Configed.getResourceValue("Dashboard.selection.allDepots"));
			controller.selectedDepotComboBox.getItems().addAll(DepotData.getDepots().keySet());
		} else if (controller.selectedDepotComboBox.getItems().size() == 1) {
			controller.selectedDepotComboBox.getItems().addAll(DepotData.getDepots().keySet());
		} else {
			Logging.warning(this, "it should not be possible to select several values");
		}

		String depotType = "-";
		String depotDescription = "-";

		Map<String, Map<String, Object>> depots = DepotData.getDepots();

		if (selectedDepot != null
				&& !selectedDepot.equals(Configed.getResourceValue("Dashboard.selection.allDepots"))) {
			if (depots.get(selectedDepot).get("type") != null
					|| !depots.get(selectedDepot).get("type").toString().isEmpty()) {
				depotType = depots.get(selectedDepot).get("type").toString();
			}

			if (depots.get(selectedDepot).get("description") != null
					|| !depots.get(selectedDepot).get("description").toString().isEmpty()) {
				depotDescription = depots.get(selectedDepot).get("description").toString();
			}
		}

		if (depotType.equals(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT)) {
			depotType = "Client";
		}

		if (depotType.equals(HostInfo.HOST_TYPE_VALUE_OPSI_DEPOT_SERVER)) {
			depotType = "Depot Server";
		}

		if (depotType.equals(HostInfo.HOST_TYPE_VALUE_OPSI_CONFIG_SERVER)) {
			depotType = "Config Server";
		}

		controller.depotTypeText.setText(depotType);
		controller.depotDescriptionText.setText(depotDescription);

		controller.clientsNumberLabel.setText(String.valueOf(ClientData.getClients().size()));
		controller.productsNumberLabel.setText(String.valueOf(ProductData.getProducts().size()));
		controller.localbootProductsNumberLabel.setText(String.valueOf(ProductData.getLocalbootProducts().size()));
		controller.netbootProductsNumberLabel.setText(String.valueOf(ProductData.getNetbootProducts().size()));
	}

	@Override
	public void update(String selectedDepot) {
		this.selectedDepot = selectedDepot;
		DepotData.retrieveData();
	}
}
