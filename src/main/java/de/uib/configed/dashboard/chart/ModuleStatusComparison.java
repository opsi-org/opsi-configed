package de.uib.configed.dashboard.chart;

import java.io.*;
import java.net.BindException;
import java.util.*;

import javafx.fxml.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import de.uib.configed.*;
import de.uib.configed.dashboard.*;
import de.uib.configed.dashboard.collector.*;

public class ModuleStatusComparison extends StackPane implements DataChangeListener
{
	@FXML private Text moduleStatusNoDataText;
	@FXML private PieChart moduleStatusComparisonPieChart;

	public ModuleStatusComparison()
	{
		FXMLLoader fxmlLoader = new FXMLLoader(ModuleStatusComparison.class.getResource("/fxml/charts/module_pie_chart.fxml"));
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
		moduleStatusNoDataText.setText(configed.getResourceValue("Dashboard.noData"));

		List<String> activeModules = ModuleData.getActiveModules();
		List<String> expiredModules = ModuleData.getExpiredModules();
		List<String> availableModules = ModuleData.getModules();

		if (activeModules.isEmpty() &&
		    expiredModules.isEmpty() &&
		    availableModules.isEmpty())
		{
			moduleStatusNoDataText.setVisible(true);
			moduleStatusComparisonPieChart.setLabelsVisible(false);
		}
		else
		{
			moduleStatusNoDataText.setVisible(false);
			moduleStatusComparisonPieChart.setLabelsVisible(true);
		}

		int totalActiveModules = activeModules.size();
		int totalExpiredModules = expiredModules.size();


		if (moduleStatusComparisonPieChart.getData().isEmpty())
		{
			moduleStatusComparisonPieChart.getData().add(new PieChart.Data("", 0));
			moduleStatusComparisonPieChart.getData().add(new PieChart.Data("", 0));
		}
		else
		{
			moduleStatusComparisonPieChart.getData().get(0).nameProperty().bindBidirectional(new SimpleStringProperty(String.format("%s %d", configed.getResourceValue("Dashboard.active"), totalActiveModules)));
			moduleStatusComparisonPieChart.getData().get(0).pieValueProperty().bindBidirectional(new SimpleIntegerProperty(totalActiveModules));

			moduleStatusComparisonPieChart.getData().get(1).nameProperty().bindBidirectional(new SimpleStringProperty(String.format("%s %d", configed.getResourceValue("Dashboard.expired"), totalExpiredModules)));
			moduleStatusComparisonPieChart.getData().get(1).pieValueProperty().bindBidirectional(new SimpleIntegerProperty(totalExpiredModules));
		}
	}

	@Override
	public void update(String selectedDepot)
	{
		ModuleData.retrieveData();
	}
}
