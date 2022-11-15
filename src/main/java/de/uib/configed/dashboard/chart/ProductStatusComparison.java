package de.uib.configed.dashboard.chart;

import java.io.*;
import java.util.*;

import javafx.fxml.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import de.uib.configed.*;
import de.uib.configed.dashboard.*;
import de.uib.configed.dashboard.collector.*;

public class ProductStatusComparison extends StackPane implements DataChangeListener
{
	@FXML private Text productStatusNoDataText;
	@FXML private BarChart<String, Number> productStatusComparisonBarChart;

	public ProductStatusComparison()
	{
		FXMLLoader fxmlLoader = new FXMLLoader(ProductStatusComparison.class.getResource("/fxml/charts/product_status_bar_chart.fxml"));
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
		productStatusNoDataText.setText(configed.getResourceValue("Dashboard.noData"));

		Map<Product, Product> installedProducts = ProductData.getInstalledProducts();
		Map<Product, Product> failedProducts = ProductData.getFailedProducts();
		Map<Product, Product> unusedProducts = ProductData.getUnusedProducts();

		if (installedProducts.isEmpty() &&
		    failedProducts.isEmpty() &&
		    unusedProducts.isEmpty())
		{
			productStatusNoDataText.setVisible(true);
		}
		else
		{
			productStatusNoDataText.setVisible(false);
		}

		int totalInstalledProducts = installedProducts.size();
		int totalFailedProducts = failedProducts.size();
		int totalUnusedProducts = unusedProducts.size();

		XYChart.Series<String, Number> data = new XYChart.Series<>();

		data.getData().add(new XYChart.Data<>(configed.getResourceValue("Dashboard.products.installed"), totalInstalledProducts));
		data.getData().add(new XYChart.Data<>(configed.getResourceValue("Dashboard.products.failed"), totalFailedProducts));
		data.getData().add(new XYChart.Data<>(configed.getResourceValue("Dashboard.products.unused"), totalUnusedProducts));

		productStatusComparisonBarChart.getData().clear();
		productStatusComparisonBarChart.getData().add(data);
	}

	@Override
	public void update(String selectedDepot)
	{
		ProductData.retrieveData(selectedDepot);
	}
}
