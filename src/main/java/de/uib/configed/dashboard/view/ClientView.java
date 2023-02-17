package de.uib.configed.dashboard.view;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.UIManager;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.uib.configed.Configed;
import de.uib.configed.dashboard.ComponentStyler;
import de.uib.configed.dashboard.Dashboard;
import de.uib.configed.dashboard.Helper;
import de.uib.configed.dashboard.chart.ClientActivityComparison;
import de.uib.configed.dashboard.chart.ClientLastSeenComparison;
import de.uib.configed.dashboard.collector.Client;
import de.uib.configed.dashboard.collector.ClientData;
import de.uib.messages.Messages;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ClientView implements View {
	@FXML
	private Label clientsNumberLabel;
	@FXML
	private Label activeClientsNumberLabel;
	@FXML
	private Label inactiveClientsNumberLabel;
	@FXML
	private Label fourteenOrLowerDaysNumberLabel;
	@FXML
	private Label betweenFifteenAndThirtyDaysNumberLabel;
	@FXML
	private Label moreThanThirtyDaysNumberLabel;
	@FXML
	private Label clientActivityLabel;
	@FXML
	private Label clientLastSeenLabel;
	@FXML
	private BorderPane clientNumberArea;
	@FXML
	private BorderPane activeClientNumberArea;
	@FXML
	private BorderPane inactiveClientNumberArea;
	@FXML
	private BorderPane fourteenOrLowerDaysNumberArea;
	@FXML
	private BorderPane betweenFifteenAndThirtyDaysNumberArea;
	@FXML
	private BorderPane moreThanThirtyDaysNumberArea;
	@FXML
	private Text clientNumberTitleText;
	@FXML
	private Text activeClientNumberTitleText;
	@FXML
	private Text inactiveClientNumberTitleText;
	@FXML
	private Text fourteenOrLowerDaysNumberTitleText;
	@FXML
	private Text betweenFifteenAndThirtyDaysNumberTitleText;
	@FXML
	private Text moreThanThirtyDaysNumberTitleText;
	@FXML
	private ComboBox<String> clientActivityStatusComboBox;
	@FXML
	private ComboBox<String> clientLastSeenComboBox;
	@FXML
	private ListView<String> clientListView;
	@FXML
	private TextField clientSearchbarTextField;
	@FXML
	private TableView<Client> clientTableView;
	@FXML
	private TableColumn<Client, String> hostnameTableColumn;
	@FXML
	private TableColumn<Client, String> lastSeenTableColumn;
	@FXML
	private TableColumn<Client, Boolean> clientActiveTableColumn;
	@FXML
	private ClientActivityComparison clientActivityComparison;
	@FXML
	private ClientLastSeenComparison clientLastSeenComparison;
	@FXML
	private Button backButton;
	@FXML
	private AnchorPane clientViewAnchorPane;
	@FXML
	private VBox clientTableArea;
	@FXML
	private VBox clientChartArea;
	@FXML
	private FontAwesomeIconView backButtonIcon;

	private JFXPanel fxPanel;
	private Scene scene;

	public ClientView(JFXPanel fxPanel) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(ClientView.class.getResource("/fxml/views/client_view.fxml"),
				Messages.getResource());
		fxmlLoader.setController(this);

		Parent root = fxmlLoader.load();
		this.scene = new Scene(root);
		this.fxPanel = fxPanel;

	}

	private void loadData() {
		List<Client> clients = ClientData.getClients();
		List<String> activeClients = ClientData.getActiveClients();
		List<String> inactiveClients = ClientData.getInactiveClients();
		Map<String, Integer> lastSeenData = ClientData.getLastSeenData();

		clientsNumberLabel.setText(String.valueOf(clients.size()));
		activeClientsNumberLabel.setText(String.valueOf(activeClients.size()));
		inactiveClientsNumberLabel.setText(String.valueOf(inactiveClients.size()));
		fourteenOrLowerDaysNumberLabel.setText(
				String.valueOf(lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"))));
		betweenFifteenAndThirtyDaysNumberLabel.setText(String.valueOf(
				lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"))));
		moreThanThirtyDaysNumberLabel.setText(
				String.valueOf(lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"))));

		hostnameTableColumn.setCellValueFactory(cellData -> cellData.getValue().hostnameProperty());
		lastSeenTableColumn.setCellValueFactory(cellData -> cellData.getValue().lastSeenProperty());
		clientActiveTableColumn.setCellValueFactory(cellData -> cellData.getValue().reachableProperty());
		clientActiveTableColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

		List<String> clientStatus = new ArrayList<>();
		clientStatus.add(Configed.getResourceValue("Dashboard.choiceBoxChoice.all"));
		clientStatus.add(Configed.getResourceValue("Dashboard.client.active"));
		clientStatus.add(Configed.getResourceValue("Dashboard.client.inactive"));
		final ObservableList<String> status = new FilteredList<>(FXCollections.observableArrayList(clientStatus));
		clientActivityStatusComboBox.setItems(status);
		clientActivityStatusComboBox.getSelectionModel().selectFirst();

		List<String> clientLastSeenData = new ArrayList<>();
		clientLastSeenData.add(Configed.getResourceValue("Dashboard.choiceBoxChoice.all"));
		clientLastSeenData.add(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"));
		clientLastSeenData.add(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"));
		clientLastSeenData.add(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"));
		clientLastSeenData.add(Configed.getResourceValue("Dashboard.lastSeen.never"));

		final ObservableList<String> lastSeen = new FilteredList<>(
				FXCollections.observableArrayList(clientLastSeenData));
		clientLastSeenComboBox.setItems(lastSeen);
		clientLastSeenComboBox.getSelectionModel().selectFirst();

		final FilteredList<Client> filteredData = new FilteredList<>(FXCollections.observableArrayList(clients));

		final ObjectProperty<Predicate<Client>> hostnameFilter = new SimpleObjectProperty<>();
		final ObjectProperty<Predicate<Client>> activeFilter = new SimpleObjectProperty<>();
		final ObjectProperty<Predicate<Client>> lastSeenFilter = new SimpleObjectProperty<>();

		hostnameFilter.bind(Bindings.createObjectBinding(() -> client -> {
			if (clientSearchbarTextField.getText() == null) {
				return true;
			}

			return client.getHostname().toLowerCase(Locale.ROOT)
					.contains(clientSearchbarTextField.getText().toLowerCase(Locale.ROOT));
		}, clientSearchbarTextField.textProperty()));
		lastSeenFilter.bind(Bindings.createObjectBinding(() -> client -> {
			if (clientLastSeenComboBox.getValue() == null || clientLastSeenComboBox.getValue()
					.equals(Configed.getResourceValue("Dashboard.choiceBoxChoice.all"))) {
				return true;
			}

			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			final LocalDate current = LocalDate.now();
			final LocalDate lastSeenDate = client.getLastSeen()
					.equals(Configed.getResourceValue("Dashboard.lastSeen.never")) ? LocalDate.parse("9999-12-31", dtf)
							: LocalDate.parse(client.getLastSeen().substring(0, 10), dtf);
			final long days = ChronoUnit.DAYS.between(lastSeenDate, current);

			return clientLastSeenComboBox.getValue()
					.equals(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")) && days <= 14
					&& days >= 0
					|| clientLastSeenComboBox.getValue()
							.equals(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"))
							&& days > 14 && days <= 30
					|| clientLastSeenComboBox.getValue()
							.equals(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")) && days > 30
					|| clientLastSeenComboBox.getValue().equals(Configed.getResourceValue("Dashboard.lastSeen.never"))
							&& client.getLastSeen().equals(Configed.getResourceValue("Dashboard.lastSeen.never"));
		}, clientLastSeenComboBox.valueProperty()));
		activeFilter.bind(Bindings.createObjectBinding(() -> client -> {
			if (clientActivityStatusComboBox.getValue() == null || clientActivityStatusComboBox.getValue()
					.equals(Configed.getResourceValue("Dashboard.choiceBoxChoice.all"))) {
				return true;
			}

			return client.getReachable()
					&& clientActivityStatusComboBox.getValue()
							.equals(Configed.getResourceValue("Dashboard.client.active"))
					|| !client.getReachable() && clientActivityStatusComboBox.getValue()
							.equals(Configed.getResourceValue("Dashboard.client.inactive"));
		}, clientActivityStatusComboBox.valueProperty()));

		filteredData.predicateProperty()
				.bind(Bindings.createObjectBinding(
						() -> hostnameFilter.get().and(lastSeenFilter.get().and(activeFilter.get())), hostnameFilter,
						lastSeenFilter, activeFilter));

		SortedList<Client> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(clientTableView.comparatorProperty());
		clientTableView.setItems(sortedData);

		backButton.setOnAction(e -> Platform.runLater(() -> ViewManager.displayView(Dashboard.MAIN_VIEW)));

		clientActivityComparison.display();
		clientLastSeenComparison.display();
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
		String foregroundColor = Integer.toHexString(UIManager.getColor("Label.foreground").getRGB()).substring(2);
		clientNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		activeClientNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		inactiveClientNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		fourteenOrLowerDaysNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		betweenFifteenAndThirtyDaysNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);
		moreThanThirtyDaysNumberTitleText.setStyle("-fx-fill: #" + foregroundColor);

		clientsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		activeClientsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		inactiveClientsNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		fourteenOrLowerDaysNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		betweenFifteenAndThirtyDaysNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);
		moreThanThirtyDaysNumberLabel.setStyle("-fx-text-fill: #" + foregroundColor);

		backButton.setStyle("-fx-text-fill: #" + foregroundColor);
		Color iconColor = UIManager.getColor("Label.foreground");
		backButtonIcon
				.setFill(javafx.scene.paint.Color.rgb(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue()));

		String lighterBackgroundColor = Integer
				.toHexString(Helper.adjustColorBrightness(UIManager.getColor("Panel.background")).getRGB())
				.substring(2);
		String backgroundColor = Integer.toHexString(UIManager.getColor("Panel.background").getRGB()).substring(2);
		fxPanel.setBackground(UIManager.getColor("Panel.background"));
		clientViewAnchorPane.setStyle("-fx-background-color: #" + backgroundColor);
		clientTableArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		clientChartArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);

		String labelForegroundColor = Integer.toHexString(UIManager.getColor("Label.foreground").getRGB()).substring(2);
		clientActivityLabel.setStyle("-fx-text-fill: #" + labelForegroundColor);
		clientLastSeenLabel.setStyle("-fx-text-fill: #" + labelForegroundColor);

		clientNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		activeClientNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		inactiveClientNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		fourteenOrLowerDaysNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		betweenFifteenAndThirtyDaysNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);
		moreThanThirtyDaysNumberArea.setStyle("-fx-background-color: #" + lighterBackgroundColor);

		ComponentStyler.styleTableViewComponent(clientTableView);
		ComponentStyler.styleTextFieldComponent(clientSearchbarTextField);
		ComponentStyler.styleComboBoxComponent(clientLastSeenComboBox);
		ComponentStyler.styleComboBoxComponent(clientActivityStatusComboBox);

	}
}
