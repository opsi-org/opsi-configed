/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.chart;

import java.io.IOException;
import java.util.List;

import javax.swing.UIManager;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.ComponentStyler;
import de.uib.configed.dashboard.DataChangeListener;
import de.uib.configed.dashboard.collector.ClientData;
import de.uib.utils.logging.Logging;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ClientActivityComparison extends StackPane implements DataChangeListener {
	@FXML
	private Text clientsActivityNoDataText;
	@FXML
	private PieChart clientActivityComparisonPieChart;

	public ClientActivityComparison() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				ClientActivityComparison.class.getResource("/fxml/charts/client_activity_pie_chart.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException ioE) {
			Logging.error(this.getClass(), "Could not load fxmlLoader", ioE);
		}
	}

	@Override
	public void display() {
		clientsActivityNoDataText.setText(Configed.getResourceValue("Dashboard.noData"));
		clientActivityComparisonPieChart.setTitle(Configed.getResourceValue("Dashboard.clientActivityTitle"));

		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		List<String> connectedClientsByMessagebus = ClientData.getConnectedClientsByMessagebus();
		List<String> notConnectedClientsByMessagebus = ClientData.getNotConnectedClientsByMessagebus();

		if (connectedClientsByMessagebus.isEmpty() && notConnectedClientsByMessagebus.isEmpty()) {
			clientsActivityNoDataText.setVisible(true);
			clientActivityComparisonPieChart.setLabelsVisible(false);
		} else {
			clientsActivityNoDataText.setVisible(false);
			clientActivityComparisonPieChart.setLabelsVisible(true);
		}

		int totalConnectedClientsByMessagebus = connectedClientsByMessagebus.size();
		int totalNotConnectedClientsByMessagebus = notConnectedClientsByMessagebus.size();

		data.add(new PieChart.Data(String.format("%s %d", Configed.getResourceValue("Dashboard.client.active"),
				totalConnectedClientsByMessagebus), totalConnectedClientsByMessagebus));
		data.add(new PieChart.Data(String.format("%s %d", Configed.getResourceValue("Dashboard.client.inactive"),
				totalNotConnectedClientsByMessagebus), totalNotConnectedClientsByMessagebus));

		clientActivityComparisonPieChart.setData(data);

		ComponentStyler.stylePieChartComponent(clientActivityComparisonPieChart);
		clientsActivityNoDataText
				.setStyle("-fx-fill: " + ComponentStyler.getHexColor(UIManager.getColor("Label.foreground")));
	}

	@Override
	public void update(String selectedDepot) {
		ClientData.retrieveData(selectedDepot);
	}
}
