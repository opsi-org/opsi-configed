package de.uib.configed.dashboard.view;

import java.io.IOException;

import javax.swing.UIManager;

import de.uib.configed.dashboard.ComponentStyler;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.dashboard.DataObserver;
import de.uib.configed.dashboard.DepotInfo;
import de.uib.configed.dashboard.Helper;
import de.uib.configed.dashboard.LicenseDisplayer;
import de.uib.configed.dashboard.chart.ClientLastSeenComparison;
import de.uib.configed.dashboard.chart.InstalledOSComparison;
import de.uib.configed.dashboard.chart.LicenseStatusComparison;
import de.uib.configed.dashboard.chart.ModuleStatusComparison;
import de.uib.configed.dashboard.chart.ProductStatusComparison;
import de.uib.configed.dashboard.collector.ClientData;
import de.uib.configed.dashboard.collector.DepotData;
import de.uib.configed.dashboard.collector.LicenseData;
import de.uib.configed.dashboard.collector.ModuleData;
import de.uib.configed.dashboard.collector.ProductData;
import de.uib.messages.Messages;
import de.uib.utilities.logging.Logging;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MainView implements View {
	@FXML
	private ClientLastSeenComparison clientLastSeenComparison;
	@FXML
	private InstalledOSComparison installedOSComparison;
	@FXML
	private LicenseStatusComparison licenseStatusComparison;
	@FXML
	private ModuleStatusComparison moduleStatusComparison;
	@FXML
	private ProductStatusComparison productStatusComparison;
	@FXML
	public ComboBox<String> selectedDepotComboBox;
	@FXML
	private VBox dashboardSceneVBox;
	@FXML
	private VBox depotDataDisplayAreaVBox;
	@FXML
	private VBox clientDataDisplayAreaVBox;
	@FXML
	private VBox productDataDisplayAreaVBox;
	@FXML
	private VBox installedOSDataDisplayAreaVBox;
	@FXML
	private VBox moduleDataDisplayAreaVBox;
	@FXML
	private VBox licenseDataDisplayAreaVBox;
	@FXML
	public Label depotsNumberLabel;
	@FXML
	public Label clientsNumberLabel;
	@FXML
	public Label productsNumberLabel;
	@FXML
	public Label localbootProductsNumberLabel;
	@FXML
	public Label netbootProductsNumberLabel;
	@FXML
	public Label installedOSsNumberLabel;
	@FXML
	public Label modulesNumberLabel;
	@FXML
	public Label licensesNumberLabel;
	@FXML
	public Label depotTypeLabel;
	@FXML
	public Label depotDescriptionLabel;
	@FXML
	public BorderPane depotNumberArea;
	@FXML
	public BorderPane clientNumberArea;
	@FXML
	public BorderPane productNumberArea;
	@FXML
	public BorderPane localbootProductNumberArea;
	@FXML
	public BorderPane netbootProductNumberArea;
	@FXML
	public BorderPane installedOSNumberArea;
	@FXML
	public BorderPane moduleNumberArea;
	@FXML
	public BorderPane licenseNumberArea;
	@FXML
	private Text depotNumberTitleText;
	@FXML
	private Text productNumberTitleText;
	@FXML
	private Text localbootProductNumberTitleText;
	@FXML
	private Text netbootProductNumberTitleText;
	@FXML
	private Text clientNumberTitleText;
	@FXML
	private Text installedOSNumberTitleText;
	@FXML
	private Text moduleNumberTitleText;
	@FXML
	private Text licenseNumberTitleText;
	@FXML
	public Text depotTypeText;
	@FXML
	public Text depotDescriptionText;
	@FXML
	public Text selectedDepotText;
	@FXML
	public Text reloadingText;
	@FXML
	public VBox dataLoadingScreenVBox;
	@FXML
	private Text depotTitleText;
	@FXML
	private Text productTitleText;
	@FXML
	private Text clientTitleText;
	@FXML
	private Text installedOSTitleText;
	@FXML
	private Text moduleTitleText;
	@FXML
	private Text licenseTitleText;
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private ProgressBar statusProgressBar;

	protected static final String DATA_CHANGED_SERVICE = "changed";
	protected static final String NEW_DEPOT_SELECTED_SERVICE = "selectedDepot";

	private JFXPanel fxPanel;
	private Scene scene;
	private DataObserver observer;

	private DepotInfo depotInfo;

	private LicenseDisplayer licenseDisplayer;

	public MainView(JFXPanel fxPanel) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(MainView.class.getResource("/fxml/dashboard.fxml"),
				Messages.getResource());
		fxmlLoader.setController(this);
		Parent root = fxmlLoader.load();

		this.scene = new Scene(root);
		this.fxPanel = fxPanel;

		observer = new DataObserver(DATA_CHANGED_SERVICE, NEW_DEPOT_SELECTED_SERVICE);
		observer.subscribe(DATA_CHANGED_SERVICE, clientLastSeenComparison);
		observer.subscribe(NEW_DEPOT_SELECTED_SERVICE, clientLastSeenComparison);
		observer.subscribe(DATA_CHANGED_SERVICE, productStatusComparison);
		observer.subscribe(NEW_DEPOT_SELECTED_SERVICE, productStatusComparison);
		observer.subscribe(DATA_CHANGED_SERVICE, installedOSComparison);
		observer.subscribe(DATA_CHANGED_SERVICE, moduleStatusComparison);
		observer.subscribe(DATA_CHANGED_SERVICE, licenseStatusComparison);

		depotInfo = new DepotInfo(this);
		observer.subscribe(DATA_CHANGED_SERVICE, depotInfo);
		observer.subscribe(NEW_DEPOT_SELECTED_SERVICE, depotInfo);
	}

	public void init() {
		Service<Void> retrieverThread = new Service<>() {
			@Override
			protected Task<Void> createTask() {
				InitialDataRetriever dataRetriever = new InitialDataRetriever();
				dataRetriever.setOnSucceeded(e -> {
					ViewManager.displayView(Dashboard.MAIN_VIEW);

					// When initial data is retrieved, we create and start another thread.
					// The created thread makes sure to load extra data, without dissallowing
					// for user to use dashbaord.
					Service<Void> extraDataRetrieverThread = new Service<>() {
						@Override
						protected Task<Void> createTask() {
							return new Task<Void>() {
								@Override
								protected Void call() throws Exception {
									ProductData.retrieveUnusedProducts();
									return null;
								}
							};
						}
					};

					extraDataRetrieverThread.setOnSucceeded(
							e2 -> observer.notify(DATA_CHANGED_SERVICE, selectedDepotComboBox.getValue()));
					extraDataRetrieverThread.start();
				});
				return dataRetriever;
			}
		};

		retrieverThread.start();
	}

	private class InitialDataRetriever extends Task<Void> {
		@Override
		public Void call() {
			ClientData.clear();
			ProductData.clear();
			ModuleData.clear();
			LicenseData.clear();
			DepotData.clear();

			setBlurriness(5);
			dataLoadingScreenVBox.setVisible(true);

			observer.notify(DATA_CHANGED_SERVICE, selectedDepotComboBox.getValue());

			dataLoadingScreenVBox.setVisible(false);
			setBlurriness(0);

			return null;
		}
	}

	private void loadData() {
		depotsNumberLabel.setText(String.valueOf(DepotData.getDepots().size()));
		clientsNumberLabel.setText(String.valueOf(ClientData.getClients().size()));
		productsNumberLabel.setText(String.valueOf(ProductData.getProducts().size()));
		localbootProductsNumberLabel.setText(String.valueOf(ProductData.getLocalbootProducts().size()));
		netbootProductsNumberLabel.setText(String.valueOf(ProductData.getNetbootProducts().size()));
		installedOSsNumberLabel.setText(String.valueOf(ProductData.getTotalOSInstallations()));
		modulesNumberLabel.setText(String.valueOf(ModuleData.getModules().size()));
		licensesNumberLabel.setText(String.valueOf(LicenseData.getLicenses().size()));

		clientLastSeenComparison.display();
		productStatusComparison.display();
		installedOSComparison.display();
		moduleStatusComparison.display();
		licenseStatusComparison.display();
		depotInfo.display();

		clientDataDisplayAreaVBox.setOnMouseClicked(e -> ViewManager.displayView(Dashboard.CLIENT_VIEW));
		productDataDisplayAreaVBox.setOnMouseClicked(e -> ViewManager.displayView(Dashboard.PRODUCT_VIEW));
		licenseDataDisplayAreaVBox.setOnMouseClicked(e -> displayLicenseInfo());
		selectedDepotComboBox.getSelectionModel().selectedItemProperty().addListener(
				(observableValue, oldValue, newValue) -> observer.notify(NEW_DEPOT_SELECTED_SERVICE, newValue));
	}

	@Override
	public void display() {
		Platform.runLater(() -> {
			fxPanel.setScene(scene);
			styleAccordingToSelectedTheme();
		});
		loadData();
	}

	public void setBlurriness(int iterations) {
		BoxBlur blurriness = new BoxBlur();
		blurriness.setIterations(iterations);
		dashboardSceneVBox.setEffect(blurriness);
	}

	private void displayLicenseInfo() {
		if (licenseDisplayer == null) {
			try {
				licenseDisplayer = new LicenseDisplayer();
				licenseDisplayer.initAndShowGUI();
			} catch (IOException ioE) {
				Logging.debug(this, "Unable to open FXML file.");
			}
		} else {
			licenseDisplayer.display();
		}
	}

	private void styleAccordingToSelectedTheme() {
		String foregroundColor = ComponentStyler.getHexColor(UIManager.getColor("Label.foreground"));
		depotTitleText.setStyle("-fx-fill: #" + foregroundColor);
		productTitleText.setStyle("-fx-fill: #" + foregroundColor);
		clientTitleText.setStyle("-fx-fill: #" + foregroundColor);
		installedOSTitleText.setStyle("-fx-fill: #" + foregroundColor);
		moduleTitleText.setStyle("-fx-fill: #" + foregroundColor);
		licenseTitleText.setStyle("-fx-fill: #" + foregroundColor);

		depotTypeText.setStyle("-fx-fill: #" + foregroundColor);
		depotDescriptionText.setStyle("-fx-fill: #" + foregroundColor);
		selectedDepotText.setStyle("-fx-fill: #" + foregroundColor);
		reloadingText.setStyle("-fx-fill: #" + foregroundColor);

		depotNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		productNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		localbootProductNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		netbootProductNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		clientNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		installedOSNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		moduleNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		licenseNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);

		depotsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		clientsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		productsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		netbootProductsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		localbootProductsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		installedOSsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		modulesNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		licensesNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);

		depotTypeLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		depotDescriptionLabel.setStyle("-fx-text-fill: #" + foregroundColor);

		String lighterBackgroundColor = ComponentStyler
				.getHexColor(Helper.adjustColorBrightness(UIManager.getColor("Panel.background")));
		String backgroundColor = ComponentStyler.getHexColor(UIManager.getColor("Panel.background"));
		fxPanel.setBackground(UIManager.getColor("Panel.background"));
		anchorPane.setStyle("-fx-background-color: #" + backgroundColor);
		dashboardSceneVBox.setStyle("-fx-background-color: #" + backgroundColor);
		depotDataDisplayAreaVBox.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		clientDataDisplayAreaVBox.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		productDataDisplayAreaVBox.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		installedOSDataDisplayAreaVBox.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		moduleDataDisplayAreaVBox.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		licenseDataDisplayAreaVBox.setStyle("-fx-background-color: #" + lighterBackgroundColor);

		depotNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		clientNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		productNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		netbootProductNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		localbootProductNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		installedOSNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		moduleNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		licenseNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);

		ComponentStyler.styleProgressBarComponent(statusProgressBar);
		ComponentStyler.styleComboBoxComponent(selectedDepotComboBox);
	}
}
