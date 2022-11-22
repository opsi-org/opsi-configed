package de.uib.configed.dashboard;

import java.util.Map;

import de.uib.configed.configed;
import de.uib.configed.dashboard.collector.ClientData;
import de.uib.configed.dashboard.collector.DepotData;
import de.uib.configed.dashboard.collector.ProductData;
import de.uib.configed.dashboard.view.MainView;

public class DepotInfo implements DataChangeListener {
	private MainView controller;
	private String selectedDepot;

	public DepotInfo(MainView controller) {
		this.controller = controller;
	}

	@Override
	public void display() {
		if (controller.selectedDepotChoiceBox.getItems().size() == 0) {
			controller.selectedDepotChoiceBox.getItems()
					.add(configed.getResourceValue("Dashboard.selection.allDepots"));
			controller.selectedDepotChoiceBox.setValue(configed.getResourceValue("Dashboard.selection.allDepots"));
			controller.selectedDepotChoiceBox.getItems().addAll(DepotData.getDepots().keySet());
		} else if (controller.selectedDepotChoiceBox.getItems().size() == 1) {
			controller.selectedDepotChoiceBox.getItems().addAll(DepotData.getDepots().keySet());
		}

		String depotType = "-";
		String depotDescription = "-";

		Map<String, Map<String, Object>> depots = DepotData.getDepots();

		if (selectedDepot != null
				&& !selectedDepot.equals(configed.getResourceValue("Dashboard.selection.allDepots"))) {
			if (depots.get(selectedDepot).get("type") != null
					|| !depots.get(selectedDepot).get("type").toString().isEmpty())
				depotType = depots.get(selectedDepot).get("type").toString();
			if (depots.get(selectedDepot).get("description") != null
					|| !depots.get(selectedDepot).get("description").toString().isEmpty())
				depotDescription = depots.get(selectedDepot).get("description").toString();
		}

		if (depotType.equals("OpsiClient"))
			depotType = "Client";
		if (depotType.equals("OpsiDepotserver"))
			depotType = "Depot Server";
		if (depotType.equals("OpsiConfigserver"))
			depotType = "Config Server";

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
