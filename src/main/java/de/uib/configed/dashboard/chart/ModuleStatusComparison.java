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
import de.uib.configed.dashboard.collector.ModuleData;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

@SuppressWarnings("java:S110")
public class ModuleStatusComparison extends StackPane implements DataChangeListener {
	@FXML
	private Text moduleStatusNoDataText;
	@FXML
	private PieChart moduleStatusComparisonPieChart;

	public ModuleStatusComparison() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				ModuleStatusComparison.class.getResource("/fxml/charts/module_pie_chart.fxml"));
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
		moduleStatusNoDataText.setText(Configed.getResourceValue("Dashboard.noData"));

		List<String> activeModules = ModuleData.getActiveModules();
		List<String> expiredModules = ModuleData.getExpiredModules();
		List<String> availableModules = ModuleData.getModules();

		if (activeModules.isEmpty() && expiredModules.isEmpty() && availableModules.isEmpty()) {
			moduleStatusNoDataText.setVisible(true);
			moduleStatusComparisonPieChart.setLabelsVisible(false);
		} else {
			moduleStatusNoDataText.setVisible(false);
			moduleStatusComparisonPieChart.setLabelsVisible(true);
		}

		int totalActiveModules = activeModules.size();
		int totalExpiredModules = expiredModules.size();

		if (moduleStatusComparisonPieChart.getData().isEmpty()) {
			moduleStatusComparisonPieChart.getData().add(new PieChart.Data("", 0));
			moduleStatusComparisonPieChart.getData().add(new PieChart.Data("", 0));
		} else {
			moduleStatusComparisonPieChart.getData().get(0).nameProperty().bindBidirectional(new SimpleStringProperty(
					String.format("%s %d", Configed.getResourceValue("Dashboard.active"), totalActiveModules)));
			moduleStatusComparisonPieChart.getData().get(0).pieValueProperty()
					.bindBidirectional(new SimpleIntegerProperty(totalActiveModules));

			moduleStatusComparisonPieChart.getData().get(1).nameProperty().bindBidirectional(new SimpleStringProperty(
					String.format("%s %d", Configed.getResourceValue("Dashboard.expired"), totalExpiredModules)));
			moduleStatusComparisonPieChart.getData().get(1).pieValueProperty()
					.bindBidirectional(new SimpleIntegerProperty(totalExpiredModules));
		}

		ComponentStyler.stylePieChartComponent(moduleStatusComparisonPieChart);
		moduleStatusNoDataText
				.setStyle("-fx-fill: #" + ComponentStyler.getHexColor(UIManager.getColor("Label.foreground")));
	}

	@Override
	public void update(String selectedDepot) {
		ModuleData.retrieveData();
	}
}
