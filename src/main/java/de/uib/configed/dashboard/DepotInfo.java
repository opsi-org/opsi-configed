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
import de.uib.utils.logging.Logging;

public class DepotInfo implements DataChangeListener {
	private MainView controller;
	private String selectedDepot;

	public DepotInfo(MainView controller) {
		this.controller = controller;
	}

	@Override
	public void display() {
		if (controller.selectedDepotComboBox.getItems().isEmpty()
				|| controller.selectedDepotComboBox.getItems().size() == 1) {
			controller.selectedDepotComboBox.getItems().clear();
			controller.selectedDepotComboBox.getItems().add(Configed.getResourceValue("Dashboard.selection.allDepots"));
			controller.selectedDepotComboBox.setValue(Configed.getResourceValue("Dashboard.selection.allDepots"));
			controller.selectedDepotComboBox.getItems().addAll(DepotData.getDepots().keySet());
		}

		String depotType = "-";
		String depotDescription = "-";
		Map<String, Map<String, Object>> depots = DepotData.getDepots();

		if (!depots.containsKey(selectedDepot)) {
			selectedDepot = Configed.getResourceValue("Dashboard.selection.allDepots");
		}

		if (!selectedDepot.equals(Configed.getResourceValue("Dashboard.selection.allDepots"))) {
			if (depots.get(selectedDepot).get("type") != null
					|| !depots.get(selectedDepot).get("type").toString().isEmpty()) {
				depotType = depots.get(selectedDepot).get("type").toString();
			}

			if (depots.get(selectedDepot).get("description") != null
					|| !depots.get(selectedDepot).get("description").toString().isEmpty()) {
				depotDescription = depots.get(selectedDepot).get("description").toString();
			}
		}

		controller.depotTypeText.setText(convertDepotTypeToDisplayText(depotType));
		controller.depotDescriptionText.setText(depotDescription);
		controller.clientsNumberLabel.setText(String.valueOf(ClientData.getClients().size()));
		controller.productsNumberLabel.setText(String.valueOf(ProductData.getProducts().size()));
		controller.localbootProductsNumberLabel.setText(String.valueOf(ProductData.getLocalbootProducts().size()));
		controller.netbootProductsNumberLabel.setText(String.valueOf(ProductData.getNetbootProducts().size()));
	}

	private static String convertDepotTypeToDisplayText(String depotType) {
		String result = "-";

		if ("-".equals(depotType)) {
			return result;
		}

		if (depotType.equals(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT)) {
			result = "Client";
		} else if (depotType.equals(HostInfo.HOST_TYPE_VALUE_OPSI_DEPOT_SERVER)) {
			result = "Depot Server";
		} else if (depotType.equals(HostInfo.HOST_TYPE_VALUE_OPSI_CONFIG_SERVER)) {
			result = "Config Server";
		} else {
			Logging.warning("Encountered unhandled depot type: " + depotType);
		}

		return result;
	}

	@Override
	public void update(String selectedDepot) {
		this.selectedDepot = selectedDepot;
		DepotData.retrieveData();
	}
}
