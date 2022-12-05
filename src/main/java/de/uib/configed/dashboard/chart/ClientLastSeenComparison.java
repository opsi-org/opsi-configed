package de.uib.configed.dashboard.chart;

import java.io.IOException;
import java.util.Map;

import de.uib.configed.configed;
import de.uib.configed.dashboard.DataChangeListener;
import de.uib.configed.dashboard.collector.ClientData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

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

		clientsLastSeenNoDataText.setText(configed.getResourceValue("Dashboard.noData"));
		lastSeenComparisonBarChart.setTitle(configed.getResourceValue("Dashboard.lastSeenTitle"));

		if (lastSeenData.isEmpty()) {
			lastSeenData.put(configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"), 0);
			lastSeenData.put(configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"), 0);
			lastSeenData.put(configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"), 0);
		}

		clientsLastSeenNoDataText.setVisible(lastSeenData.values().stream().allMatch(v -> v == 0));

		data.getData()
				.add(new XYChart.Data<>(
						lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")),
						configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")));
		data.getData()
				.add(new XYChart.Data<>(
						lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays")),
						configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays")));
		data.getData()
				.add(new XYChart.Data<>(
						lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")),
						configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")));

		lastSeenComparisonBarChart.getData().clear();
		lastSeenComparisonBarChart.getData().add(data);
	}

	@Override
	public void update(String selectedDepot) {
		ClientData.retrieveData(selectedDepot);
	}
}
