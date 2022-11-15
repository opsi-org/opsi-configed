package de.uib.configed.dashboard.chart;

import java.io.IOException;
import java.util.*;

import javafx.fxml.*;
import javafx.collections.*;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.*;

import de.uib.configed.*;
import de.uib.configed.dashboard.*;
import de.uib.configed.dashboard.collector.*;

public class ClientLastSeenComparison extends StackPane implements DataChangeListener
{
	@FXML private Text clientsLastSeenNoDataText;
	@FXML private BarChart<Number, String> lastSeenComparisonBarChart;

	public ClientLastSeenComparison()
	{
		FXMLLoader fxmlLoader = new FXMLLoader(ClientLastSeenComparison.class.getResource("/resources/fxml/charts/client_last_seen_bar_chart.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try
		{
			fxmlLoader.load();
		}
		catch (IOException ioE)
		{
			throw new RuntimeException(ioE);
		}
	}

	@Override
	public void display()
	{
		XYChart.Series<Number, String> data = new XYChart.Series<Number, String>();
		Map<String, Integer> lastSeenData = ClientData.getLastSeenData();

        clientsLastSeenNoDataText.setText(configed.getResourceValue("Dashboard.noData"));
        lastSeenComparisonBarChart.setTitle(configed.getResourceValue("Dashboard.lastSeenTitle"));

		if (lastSeenData.isEmpty())
		{
			lastSeenData.put(configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"), 0);
			lastSeenData.put(configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"), 0);
			lastSeenData.put(configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"), 0);
		}

		if (lastSeenData.values().stream().allMatch(v -> v == 0))
		{
			clientsLastSeenNoDataText.setVisible(true);
		}
		else
		{
			clientsLastSeenNoDataText.setVisible(false);
		}

		data.getData().add(new XYChart.Data<Number, String>(lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")), configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")));
		data.getData().add(new XYChart.Data<Number, String>(lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays")), configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays")));
		data.getData().add(new XYChart.Data<Number, String>(lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")), configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")));

		lastSeenComparisonBarChart.getData().clear();
		lastSeenComparisonBarChart.getData().add(data);
	}

	@Override
	public void update(String selectedDepot)
	{
		ClientData.retrieveData(selectedDepot);
	}
}
