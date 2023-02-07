package de.uib.configed.dashboard.chart;

import java.io.IOException;

import javax.swing.UIManager;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.ComponentStyler;
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
		installedOSNoDataText.setText(Configed.getResourceValue("Dashboard.noData"));

		XYChart.Series<String, Number> data = new XYChart.Series<>();

		int totalLinuxInstallations = ProductData.getTotalLinuxInstallations();
		int totalWindowsInstallations = ProductData.getTotalWindowsInstallations();
		int totalMacOSInstallations = ProductData.getTotalMacOSInstallations();

		installedOSNoDataText.setVisible(
				totalLinuxInstallations == 0 && totalWindowsInstallations == 0 && totalMacOSInstallations == 0);

		data.getData().add(new XYChart.Data<>(LINUX, ProductData.getTotalLinuxInstallations()));
		data.getData().add(new XYChart.Data<>(WINDOWS, ProductData.getTotalWindowsInstallations()));
		data.getData().add(new XYChart.Data<>(MACOS, ProductData.getTotalMacOSInstallations()));

		installedOSComparisonBarChart.getData().clear();
		installedOSComparisonBarChart.getData().add(data);

		ComponentStyler.styleBarChartComponent(installedOSComparisonBarChart);
		installedOSNoDataText
				.setStyle("-fx-fill: #" + ComponentStyler.getHexColor(UIManager.getColor("Label.foreground")));
	}

	@Override
	public void update(String selectedDepot) {
		ProductData.retrieveData(selectedDepot);
	}
}
