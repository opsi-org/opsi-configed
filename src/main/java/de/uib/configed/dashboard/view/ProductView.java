package de.uib.configed.dashboard.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import de.uib.configed.configed;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.dashboard.chart.ProductComparison;
import de.uib.configed.dashboard.chart.ProductStatusComparison;
import de.uib.configed.dashboard.collector.Product;
import de.uib.configed.dashboard.collector.ProductData;
import de.uib.messages.Messages;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ProductView implements View {
	@FXML
	private Label productsNumberLabel;
	@FXML
	private Label localbootProductsNumberLabel;
	@FXML
	private Label netbootProductsNumberLabel;
	@FXML
	private Label installedProductsNumberLabel;
	@FXML
	private Label failedProductsNumberLabel;
	@FXML
	private Label unusedProductsNumberLabel;
	@FXML
	private TextField clientSearchbarTextField;
	@FXML
	private TextField productSearchbarTextField;
	@FXML
	private ChoiceBox<String> productStatusChoiceBox;
	@FXML
	private ListView<String> clientListView;
	@FXML
	private TableView<Product> productTableView;
	@FXML
	private TableColumn<Product, String> productIdTableColumn;
	@FXML
	private TableColumn<Product, String> productStatusTableColumn;
	@FXML
	private TableColumn<Product, String> productInDepotTableColumn;
	@FXML
	private Button backButton;
	@FXML
	private ProductComparison productComparison;
	@FXML
	private ProductStatusComparison productStatusComparison;

	private JFXPanel fxPanel;
	private Scene scene;

	public ProductView(JFXPanel fxPanel) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(ProductView.class.getResource("/fxml/views/product_view.fxml"),
				Messages.getResource());
		fxmlLoader.setController(this);

		Parent root = fxmlLoader.load();
		this.scene = new Scene(root);
		this.fxPanel = fxPanel;
	}

	private void loadData() {
		productsNumberLabel.setText(String.valueOf(ProductData.getProducts().size()));
		localbootProductsNumberLabel.setText(String.valueOf(ProductData.getLocalbootProducts().size()));
		netbootProductsNumberLabel.setText(String.valueOf(ProductData.getNetbootProducts().size()));
		installedProductsNumberLabel.setText(String.valueOf(ProductData.getInstalledProducts().size()));
		failedProductsNumberLabel.setText(String.valueOf(ProductData.getFailedProducts().size()));
		unusedProductsNumberLabel.setText(String.valueOf(ProductData.getUnusedProducts().size()));

		productStatusChoiceBox.getItems().clear();
		productStatusChoiceBox.getItems().add(configed.getResourceValue("Dashboard.choiceBoxChoice.all"));
		productStatusChoiceBox.getItems().add(configed.getResourceValue("Dashboard.products.installed"));
		productStatusChoiceBox.getItems().add(configed.getResourceValue("Dashboard.products.failed"));
		productStatusChoiceBox.getItems().add(configed.getResourceValue("Dashboard.products.unused"));
		productStatusChoiceBox.getSelectionModel().selectFirst();

		productIdTableColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		productStatusTableColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
		productInDepotTableColumn.setCellValueFactory(cellData -> cellData.getValue().depotProperty());

		List<Product> products = new ArrayList<>();
		products.addAll(ProductData.getInstalledProducts().keySet());
		products.addAll(ProductData.getFailedProducts().keySet());
		products.addAll(ProductData.getUnusedProducts().keySet());

		final FilteredList<Product> filteredData = new FilteredList<>(FXCollections.observableArrayList(products));

		final ObjectProperty<Predicate<Product>> productIdFilter = new SimpleObjectProperty<>();
		final ObjectProperty<Predicate<Product>> productStatusFilter = new SimpleObjectProperty<>();

		productIdFilter.bind(Bindings.createObjectBinding(() -> product -> {
			if (productSearchbarTextField.getText() == null) {
				return true;
			}

			return product.getId().contains(productSearchbarTextField.getText());
		}, productSearchbarTextField.textProperty()));
		productStatusFilter.bind(Bindings.createObjectBinding(() -> product -> {
			if (productStatusChoiceBox.getValue() == null || productStatusChoiceBox.getValue()
					.equals(configed.getResourceValue("Dashboard.choiceBoxChoice.all"))) {
				return true;
			}

			return productStatusChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.products.installed"))
					&& product.getStatus().equals(configed.getResourceValue("Dashboard.products.installed"))
					|| productStatusChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.products.failed"))
							&& product.getStatus().equals(configed.getResourceValue("Dashboard.products.failed"))
					|| productStatusChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.products.unused"))
							&& product.getStatus().equals(configed.getResourceValue("Dashboard.products.unused"));
		}, productStatusChoiceBox.valueProperty()));

		filteredData.predicateProperty().bind(Bindings.createObjectBinding(
				() -> productIdFilter.get().and(productStatusFilter.get()), productIdFilter, productStatusFilter));

		SortedList<Product> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(productTableView.comparatorProperty());
		productTableView.setItems(sortedData);

		productTableView.getSelectionModel().selectedItemProperty()
				.addListener((observableValue, oldValue, newValue) -> {
					if (newValue == null) {
						return;
					}

					bindDataToListView(newValue.getClients(), clientListView, clientSearchbarTextField);
				});

		backButton.setOnAction(e -> ViewManager.displayView(Dashboard.MAIN_VIEW));

		productComparison.display();
		productStatusComparison.display();
	}

	private void bindDataToListView(List<String> data, ListView<String> view, TextField searchbar) {
		if (data == null || data.isEmpty()) {
			return;
		}

		FilteredList<String> filteredData = new FilteredList<>(FXCollections.observableArrayList(data));

		ObjectProperty<Predicate<String>> productDataFilter = new SimpleObjectProperty<>();
		productDataFilter
				.bind(Bindings
						.createObjectBinding(
								() -> product -> product.toLowerCase(Locale.ROOT)
										.contains(searchbar.getText().toLowerCase(Locale.ROOT)),
								searchbar.textProperty()));

		view.setItems(filteredData);

		filteredData.predicateProperty()
				.bind(Bindings.createObjectBinding(() -> productDataFilter.get(), productDataFilter));
	}

	@Override
	public void display() {
		Platform.runLater(() -> fxPanel.setScene(scene));
		loadData();
	}
}
