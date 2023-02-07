package de.uib.configed.dashboard.chart;

import java.io.IOException;
import java.util.List;

import javax.swing.UIManager;

import de.uib.configed.Configed;
import de.uib.configed.dashboard.ComponentStyler;
import de.uib.configed.dashboard.DataChangeListener;
import de.uib.configed.dashboard.collector.ProductData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class ProductComparison extends StackPane implements DataChangeListener {
	@FXML
	private Text productsNoDataText;
	@FXML
	private PieChart productComparisonPieChart;

	public ProductComparison() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				ProductComparison.class.getResource("/fxml/charts/product_pie_chart.fxml"));
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
		productsNoDataText.setText(Configed.getResourceValue("Dashboard.noData"));

		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

		List<String> netbootProducts = ProductData.getNetbootProducts();
		List<String> localbootProducts = ProductData.getLocalbootProducts();

		if (netbootProducts.isEmpty() && localbootProducts.isEmpty()) {
			productsNoDataText.setVisible(true);
			productComparisonPieChart.setLabelsVisible(false);
		} else {
			productsNoDataText.setVisible(false);
			productComparisonPieChart.setLabelsVisible(true);
		}

		int totalNetbootProducts = netbootProducts.size();
		int totalLocalbootProducts = localbootProducts.size();

		data.add(new PieChart.Data(String.format("%s %d", Configed.getResourceValue("Dashboard.netbootProductsTitle"),
				totalNetbootProducts), totalNetbootProducts));
		data.add(new PieChart.Data(String.format("%s %d", Configed.getResourceValue("Dashboard.localbootProductsTitle"),
				totalLocalbootProducts), totalLocalbootProducts));

		productComparisonPieChart.setData(data);

		ComponentStyler.stylePieChartComponent(productComparisonPieChart);
		productsNoDataText
				.setStyle("-fx-fill: #" + ComponentStyler.getHexColor(UIManager.getColor("Label.foreground")));
	}

	@Override
	public void update(String selectedDepot) {
		ProductData.retrieveData(selectedDepot);
	}
}
