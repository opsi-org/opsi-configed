/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.view;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import javax.swing.UIManager;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.uib.configed.Configed;
import de.uib.configed.dashboard.ComponentStyler;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.dashboard.Helper;
import de.uib.configed.dashboard.chart.ProductComparison;
import de.uib.configed.dashboard.chart.ProductStatusComparison;
import de.uib.configed.dashboard.collector.Product;
import de.uib.configed.dashboard.collector.ProductData;
import de.uib.messages.Messages;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

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
	private Label productStatusLabel;
	@FXML
	private BorderPane productNumberArea;
	@FXML
	private BorderPane localbootProductNumberArea;
	@FXML
	private BorderPane netbootProductNumberArea;
	@FXML
	private BorderPane installedProductNumberArea;
	@FXML
	private BorderPane failedProductNumberArea;
	@FXML
	private BorderPane unusedProductNumberArea;
	@FXML
	private Text productNumberTitleText;
	@FXML
	private Text localbootProductNumberTitleText;
	@FXML
	private Text netbootProductNumberTitleText;
	@FXML
	private Text installedProductNumberTitleText;
	@FXML
	private Text failedProductNumberTitleText;
	@FXML
	private Text unusedProductNumberTitleText;
	@FXML
	private TextField clientSearchbarTextField;
	@FXML
	private TextField productSearchbarTextField;
	@FXML
	private ComboBox<String> productStatusComboBox;
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
	@FXML
	private AnchorPane productViewAnchorPane;
	@FXML
	private HBox productStatusArea;
	@FXML
	private HBox productChartArea;
	@FXML
	private FontAwesomeIconView backButtonIcon;

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

		productStatusComboBox.getItems().clear();
		productStatusComboBox.getItems().add(Configed.getResourceValue("Dashboard.choiceBoxChoice.all"));
		productStatusComboBox.getItems().add(Configed.getResourceValue("Dashboard.products.installed"));
		productStatusComboBox.getItems().add(Configed.getResourceValue("Dashboard.products.failed"));
		productStatusComboBox.getItems().add(Configed.getResourceValue("Dashboard.products.unused"));
		productStatusComboBox.getSelectionModel().selectFirst();

		productIdTableColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		productStatusTableColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
		productInDepotTableColumn.setCellValueFactory(cellData -> cellData.getValue().depotProperty());

		List<Product> products = new ArrayList<>();
		products.addAll(ProductData.getInstalledProducts().keySet());
		products.addAll(ProductData.getFailedProducts().keySet());
		products.addAll(ProductData.getUnusedProducts().keySet());

		final ObjectProperty<Predicate<Product>> productIdFilter = new SimpleObjectProperty<>();

		productIdFilter.bind(Bindings.createObjectBinding(() -> (Product product) -> {
			if (productSearchbarTextField.getText() == null) {
				return true;
			}

			return product.getId().contains(productSearchbarTextField.getText());
		}, productSearchbarTextField.textProperty()));

		final ObjectProperty<Predicate<Product>> productStatusFilter = new SimpleObjectProperty<>();

		productStatusFilter.bind(
				Bindings.createObjectBinding(() -> this::isInProductStatus, productStatusComboBox.valueProperty()));

		final FilteredList<Product> filteredData = new FilteredList<>(FXCollections.observableArrayList(products));

		filteredData.predicateProperty().bind(Bindings.createObjectBinding(
				() -> productIdFilter.get().and(productStatusFilter.get()), productIdFilter, productStatusFilter));

		SortedList<Product> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(productTableView.comparatorProperty());
		productTableView.setItems(sortedData);

		productTableView.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<? extends Product> observableValue, Product oldValue, Product newValue) -> {
					if (newValue == null) {
						return;
					}

					bindDataToListView(newValue.getClients(), clientListView, clientSearchbarTextField);
				});

		backButton.setOnAction(e -> ViewManager.displayView(Dashboard.MAIN_VIEW));

		productComparison.display();
		productStatusComparison.display();
	}

	private boolean isInProductStatus(Product product) {
		if (productStatusComboBox.getValue() == null || productStatusComboBox.getValue()
				.equals(Configed.getResourceValue("Dashboard.choiceBoxChoice.all"))) {
			return true;
		}

		return productStatusComboBox.getValue().equals(product.getStatus());
	}

	private static void bindDataToListView(List<String> data, ListView<String> view, TextField searchbar) {
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

		filteredData.predicateProperty().bind(Bindings.createObjectBinding(productDataFilter::get, productDataFilter));
	}

	@Override
	public void display() {
		Platform.runLater(() -> {
			fxPanel.setScene(scene);
			loadData();
			styleAccordingToSelectedTheme();
		});
	}

	private void styleAccordingToSelectedTheme() {
		String foregroundColor = ComponentStyler.getHexColor(UIManager.getColor("Label.foreground"));
		productNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		localbootProductNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		netbootProductNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		installedProductNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		failedProductNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		unusedProductNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);

		productsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		netbootProductsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		localbootProductsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		installedProductsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		failedProductsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		unusedProductsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);

		backButton.setStyle("-fx-text-fill: #" + foregroundColor);
		Color iconColor = UIManager.getColor("Label.foreground");
		backButtonIcon
				.setFill(javafx.scene.paint.Color.rgb(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue()));

		String lighterBackgroundColor = ComponentStyler
				.getHexColor(Helper.adjustColorBrightness(UIManager.getColor("Panel.background")));
		String backgroundColor = ComponentStyler.getHexColor(UIManager.getColor("Panel.background"));
		fxPanel.setBackground(UIManager.getColor("Panel.background"));
		productViewAnchorPane.setStyle("-fx-background-color: #" + backgroundColor);
		productStatusArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		productChartArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);

		String labelForegroundColor = ComponentStyler.getHexColor(UIManager.getColor("Label.foreground"));
		productStatusLabel.setStyle("-fx-text-fill: #" + labelForegroundColor);

		productNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		netbootProductNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		localbootProductNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		installedProductNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		unusedProductNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		failedProductNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);

		ComponentStyler.styleTableViewComponent(productTableView);
		ComponentStyler.styleListViewComponent(clientListView);
		ComponentStyler.styleTextFieldComponent(productSearchbarTextField);
		ComponentStyler.styleTextFieldComponent(clientSearchbarTextField);
		ComponentStyler.styleComboBoxComponent(productStatusComboBox);
	}
}
