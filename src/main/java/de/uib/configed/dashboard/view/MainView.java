package de.uib.configed.dashboard.view;

import java.io.IOException;

import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.dashboard.DataObserver;
import de.uib.configed.dashboard.DepotInfo;
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
import de.uib.utilities.logging.logging;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
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
	public ChoiceBox<String> selectedDepotChoiceBox;
	@FXML
	private VBox dashboardSceneVBox;
	@FXML
	private VBox clientDataDisplayAreaVBox;
	@FXML
	private VBox productDataDisplayAreaVBox;
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
	public Text depotTypeText;
	@FXML
	public Text depotDescriptionText;
	@FXML
	public VBox dataLoadingScreenVBox;

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
							e2 -> observer.notify(DATA_CHANGED_SERVICE, selectedDepotChoiceBox.getValue()));
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

			observer.notify(DATA_CHANGED_SERVICE, selectedDepotChoiceBox.getValue());

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
		selectedDepotChoiceBox.getSelectionModel().selectedItemProperty().addListener(
				(observableValue, oldValue, newValue) -> observer.notify(NEW_DEPOT_SELECTED_SERVICE, newValue));
	}

	@Override
	public void display() {
		Platform.runLater(() -> fxPanel.setScene(scene));
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
				logging.debug(this, "Unable to open FXML file.");
			}
		} else {
			licenseDisplayer.display();
		}
	}
}
