/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.chart;

import java.io.IOException;
import java.util.Map;

import javax.swing.UIManager;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.ComponentStyler;
import de.uib.configed.dashboard.DataChangeListener;
import de.uib.configed.dashboard.collector.ClientData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

@SuppressWarnings("java:S110")
public class ClientLastSeenComparison extends StackPane implements DataChangeListener {
	@FXML
	private Text clientsLastSeenNoDataText;
	@FXML
	private BarChart<Number, String> lastSeenComparisonBarChart;

	public ClientLastSeenComparison() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				ClientLastSeenComparison.class.getResource("/fxml/charts/client_last_seen_bar_chart.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException ioE) {
			throw new RuntimeException(ioE);
		}
	}

	@Override
	public void display() {
		XYChart.Series<Number, String> data = new XYChart.Series<>();
		Map<String, Integer> lastSeenData = ClientData.getLastSeenData();

		clientsLastSeenNoDataText.setText(Configed.getResourceValue("Dashboard.noData"));
		lastSeenComparisonBarChart.setTitle(Configed.getResourceValue("Dashboard.lastSeenTitle"));

		if (lastSeenData.isEmpty()) {
			lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"), 0);
			lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"), 0);
			lastSeenData.put(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"), 0);
		}

		clientsLastSeenNoDataText.setVisible(lastSeenData.values().stream().allMatch(v -> v == 0));

		data.getData()
				.add(new XYChart.Data<>(
						lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")),
						Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")));
		data.getData()
				.add(new XYChart.Data<>(
						lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays")),
						Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays")));
		data.getData()
				.add(new XYChart.Data<>(
						lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")),
						Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")));

		lastSeenComparisonBarChart.getData().clear();
		lastSeenComparisonBarChart.getData().add(data);

		ComponentStyler.styleBarChartComponent(lastSeenComparisonBarChart);
		clientsLastSeenNoDataText
				.setStyle("-fx-fill: #" + ComponentStyler.getHexColor(UIManager.getColor("Label.foreground")));
	}

	@Override
	public void update(String selectedDepot) {
		ClientData.retrieveData(selectedDepot);
	}
}
