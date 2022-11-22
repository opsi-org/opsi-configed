package de.uib.configed.dashboard.chart;

import java.io.IOException;

import de.uib.configed.configed;
import de.uib.configed.dashboard.DataChangeListener;
import de.uib.configed.dashboard.collector.ProductData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class InstalledOSComparison extends StackPane implements DataChangeListener {
	private static final String LINUX = "Linux";
	private static final String WINDOWS = "Windows";
	private static final String MACOS = "MacOS";

	@FXML
	private Text installedOSNoDataText;
	@FXML
	private BarChart<String, Number> installedOSComparisonBarChart;

	public InstalledOSComparison() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				InstalledOSComparison.class.getResource("/fxml/charts/installed_os_bar_chart.fxml"));
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
		installedOSNoDataText.setText(configed.getResourceValue("Dashboard.noData"));

		XYChart.Series<String, Number> data = new XYChart.Series<>();

		int totalLinuxInstallations = ProductData.getTotalLinuxInstallations();
		int totalWindowsInstallations = ProductData.getTotalWindowsInstallations();
		int totalMacOSInstallations = ProductData.getTotalMacOSInstallations();

		if (totalLinuxInstallations == 0 &&
				totalWindowsInstallations == 0 &&
				totalMacOSInstallations == 0) {
			installedOSNoDataText.setVisible(true);
		} else {
			installedOSNoDataText.setVisible(false);
		}

		data.getData().add(new XYChart.Data<String, Number>(LINUX, ProductData.getTotalLinuxInstallations()));
		data.getData().add(new XYChart.Data<String, Number>(WINDOWS, ProductData.getTotalWindowsInstallations()));
		data.getData().add(new XYChart.Data<String, Number>(MACOS, ProductData.getTotalMacOSInstallations()));

		installedOSComparisonBarChart.getData().clear();
		installedOSComparisonBarChart.getData().add(data);
	}

	@Override
	public void update(String selectedDepot) {
		ProductData.retrieveData(selectedDepot);
	}
}
