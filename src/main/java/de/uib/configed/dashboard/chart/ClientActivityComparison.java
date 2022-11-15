package de.uib.configed.dashboard.chart;

import java.io.*;
import java.util.*;

import javafx.fxml.*;
import javafx.collections.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import de.uib.configed.*;
import de.uib.configed.dashboard.*;
import de.uib.configed.dashboard.collector.*;

public class ClientActivityComparison extends StackPane implements DataChangeListener
{
	@FXML private Text clientsActivityNoDataText;
	@FXML private PieChart clientActivityComparisonPieChart;

	public ClientActivityComparison()
	{
		FXMLLoader fxmlLoader = new FXMLLoader(ClientActivityComparison.class.getResource("/fxml/charts/client_activity_pie_chart.fxml"));
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
		clientsActivityNoDataText.setText(configed.getResourceValue("Dashboard.noData"));
		clientActivityComparisonPieChart.setTitle(configed.getResourceValue("Dashboard.clientActivityTitle"));

		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		List<String> activeClients = ClientData.getActiveClients();
		List<String> inactiveClients = ClientData.getInactiveClients();

		if (activeClients.isEmpty() && inactiveClients.isEmpty())
		{
			clientsActivityNoDataText.setVisible(true);
			clientActivityComparisonPieChart.setLabelsVisible(false);
		}
		else
		{
			clientsActivityNoDataText.setVisible(false);
			clientActivityComparisonPieChart.setLabelsVisible(true);
		}

		int totalActiveClients = activeClients.size();
		int totalInactiveClients = inactiveClients.size();

		data.add(new PieChart.Data(String.format("%s %d", configed.getResourceValue("Dashboard.client.active"), totalActiveClients), totalActiveClients));
		data.add(new PieChart.Data(String.format("%s %d", configed.getResourceValue("Dashboard.client.inactive"), totalInactiveClients), totalInactiveClients));

		clientActivityComparisonPieChart.setData(data);
	}

	@Override
	public void update(String selectedDepot)
	{
		ClientData.retrieveData(selectedDepot);
	}
}
