package de.uib.configed.dashboard.chart;

import java.io.IOException;
import java.util.List;

import de.uib.configed.configed;
import de.uib.configed.dashboard.DataChangeListener;
import de.uib.configed.dashboard.collector.LicenseData;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class LicenseStatusComparison extends StackPane implements DataChangeListener {
	@FXML
	private Text licenseStatusNoDataText;
	@FXML
	private PieChart licenseStatusComparisonPieChart;

	public LicenseStatusComparison() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				LicenseStatusComparison.class.getResource("/fxml/charts/license_pie_chart.fxml"));
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
		licenseStatusNoDataText.setText(configed.getResourceValue("Dashboard.noData"));

		List<String> expiredLicenses = LicenseData.getExpiredLicenses();
		List<String> availableLicenses = LicenseData.getLicenses();
		List<String> activeLicenses = LicenseData.getActiveLicenses();

		if (activeLicenses.isEmpty() &&
				expiredLicenses.isEmpty() &&
				availableLicenses.isEmpty()) {
			licenseStatusNoDataText.setVisible(true);
			licenseStatusComparisonPieChart.setLabelsVisible(false);
		} else {
			licenseStatusNoDataText.setVisible(false);
			licenseStatusComparisonPieChart.setLabelsVisible(true);
		}

		int totalActiveLicenses = activeLicenses.size();
		int totalExpiredLicenses = expiredLicenses.size();

		if (licenseStatusComparisonPieChart.getData().isEmpty()) {
			licenseStatusComparisonPieChart.getData().add(new PieChart.Data("", 0));
			licenseStatusComparisonPieChart.getData().add(new PieChart.Data("", 0));
		} else {
			licenseStatusComparisonPieChart.getData().get(0).nameProperty().bindBidirectional(new SimpleStringProperty(
					String.format("%s %d", configed.getResourceValue("Dashboard.active"), totalActiveLicenses)));
			licenseStatusComparisonPieChart.getData().get(0).pieValueProperty()
					.bindBidirectional(new SimpleIntegerProperty(totalActiveLicenses));

			licenseStatusComparisonPieChart.getData().get(1).nameProperty().bindBidirectional(new SimpleStringProperty(
					String.format("%s %d", configed.getResourceValue("Dashboard.expired"), totalExpiredLicenses)));
			licenseStatusComparisonPieChart.getData().get(1).pieValueProperty()
					.bindBidirectional(new SimpleIntegerProperty(totalExpiredLicenses));
		}
	}

	@Override
	public void update(String selectedDepot) {
		LicenseData.retrieveData();
	}
}
