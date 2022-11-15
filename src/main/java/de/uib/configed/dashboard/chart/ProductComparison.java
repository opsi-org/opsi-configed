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

public class ProductComparison extends StackPane implements DataChangeListener
{
	@FXML private Text productsNoDataText;
	@FXML private PieChart productComparisonPieChart;

	public ProductComparison()
	{
		FXMLLoader fxmlLoader = new FXMLLoader(ProductComparison.class.getResource("/resources/fxml/charts/product_pie_chart.fxml"));
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
		productsNoDataText.setText(configed.getResourceValue("Dashboard.noData"));

		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

		List<String> netbootProducts = ProductData.getNetbootProducts();
		List<String> localbootProducts = ProductData.getLocalbootProducts();

		if (netbootProducts.isEmpty() &&
			localbootProducts.isEmpty())
		{
			productsNoDataText.setVisible(true);
			productComparisonPieChart.setLabelsVisible(false);
		}
		else
		{
			productsNoDataText.setVisible(false);
			productComparisonPieChart.setLabelsVisible(true);
		}

		int totalNetbootProducts = netbootProducts.size();
		int totalLocalbootProducts = localbootProducts.size();

		data.add(new PieChart.Data(String.format("%s %d", configed.getResourceValue("Dashboard.netbootProductsTitle"), totalNetbootProducts), totalNetbootProducts));
		data.add(new PieChart.Data(String.format("%s %d", configed.getResourceValue("Dashboard.localbootProductsTitle"), totalLocalbootProducts), totalLocalbootProducts));

		productComparisonPieChart.setData(data);
	}

	@Override
	public void update(String selectedDepot)
	{
		ProductData.retrieveData(selectedDepot);
	}
}
