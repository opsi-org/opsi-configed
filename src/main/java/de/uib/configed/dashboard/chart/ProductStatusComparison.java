package de.uib.configed.dashboard.chart;

import java.io.IOException;
import java.util.Map;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.DataChangeListener;
import de.uib.configed.dashboard.collector.Product;
import de.uib.configed.dashboard.collector.ProductData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ProductStatusComparison extends StackPane implements DataChangeListener {
	@FXML
	private Text productStatusNoDataText;
	@FXML
	private BarChart<String, Number> productStatusComparisonBarChart;

	public ProductStatusComparison() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				ProductStatusComparison.class.getResource("/fxml/charts/product_status_bar_chart.fxml"));
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
		productStatusNoDataText.setText(Configed.getResourceValue("Dashboard.noData"));

		Map<Product, Product> installedProducts = ProductData.getInstalledProducts();
		Map<Product, Product> failedProducts = ProductData.getFailedProducts();
		Map<Product, Product> unusedProducts = ProductData.getUnusedProducts();

		productStatusNoDataText
				.setVisible(installedProducts.isEmpty() && failedProducts.isEmpty() && unusedProducts.isEmpty());

		int totalInstalledProducts = installedProducts.size();
		int totalFailedProducts = failedProducts.size();
		int totalUnusedProducts = unusedProducts.size();

		XYChart.Series<String, Number> data = new XYChart.Series<>();

		data.getData().add(
				new XYChart.Data<>(Configed.getResourceValue("Dashboard.products.installed"), totalInstalledProducts));
		data.getData()
				.add(new XYChart.Data<>(Configed.getResourceValue("Dashboard.products.failed"), totalFailedProducts));
		data.getData()
				.add(new XYChart.Data<>(Configed.getResourceValue("Dashboard.products.unused"), totalUnusedProducts));

		productStatusComparisonBarChart.getData().clear();
		productStatusComparisonBarChart.getData().add(data);
	}

	@Override
	public void update(String selectedDepot) {
		ProductData.retrieveData(selectedDepot);
	}
}
