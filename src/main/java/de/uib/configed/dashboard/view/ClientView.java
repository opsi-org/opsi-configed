/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

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
				Messages.getResourceBundle());
		fxmlLoader.setController(this);

		Parent root = fxmlLoader.load();
		this.scene = new Scene(root);
		this.fxPanel = fxPanel;
	}

	private void loadData() {
		List<Client> clients = ClientData.getClients();
		List<String> connectedClientsByMessagebus = ClientData.getConnectedClientsByMessagebus();
		List<String> notConnectedClientsByMessagebus = ClientData.getNotConnectedClientsByMessagebus();
		Map<String, Integer> lastSeenData = ClientData.getLastSeenData();

		clientsNumberLabel.setText(String.valueOf(clients.size()));
		activeClientsNumberLabel.setText(String.valueOf(connectedClientsByMessagebus.size()));
		inactiveClientsNumberLabel.setText(String.valueOf(notConnectedClientsByMessagebus.size()));
		fourteenOrLowerDaysNumberLabel.setText(
				String.valueOf(lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"))));
		betweenFifteenAndThirtyDaysNumberLabel.setText(String.valueOf(
				lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"))));
		moreThanThirtyDaysNumberLabel.setText(
				String.valueOf(lastSeenData.get(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"))));

		hostnameTableColumn.setCellValueFactory(cellData -> cellData.getValue().hostnameProperty());
		lastSeenTableColumn.setCellValueFactory(cellData -> cellData.getValue().lastSeenProperty());
		clientActiveTableColumn.setCellValueFactory(cellData -> cellData.getValue().connectedWithMessagebusProperty());
		clientActiveTableColumn.setCellFactory(tc -> new CheckBoxTableCell<>());
		clientActiveTableColumn.getCellFactory().call(clientActiveTableColumn)
				.setStyle("-fx-text-background-color: #ffffff; -fx-text-fill: #ffffff");

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

		final ObjectProperty<Predicate<Client>> hostnameFilter = new SimpleObjectProperty<>();

		hostnameFilter.bind(Bindings.createObjectBinding(() -> (Client client) -> {
			if (clientSearchbarTextField.getText() == null) {
				return true;
			}

			return client.getHostname().toLowerCase(Locale.ROOT)
					.contains(clientSearchbarTextField.getText().toLowerCase(Locale.ROOT));
		}, clientSearchbarTextField.textProperty()));

		final ObjectProperty<Predicate<Client>> lastSeenFilter = new SimpleObjectProperty<>();

		lastSeenFilter.bind(Bindings.createObjectBinding(() -> this::checkIfClientLastSeen,
				clientLastSeenComboBox.valueProperty()));

		final ObjectProperty<Predicate<Client>> activeFilter = new SimpleObjectProperty<>();

		activeFilter.bind(Bindings.createObjectBinding(() -> (Client client) -> {
			if (clientActivityStatusComboBox.getValue() == null || clientActivityStatusComboBox.getValue()
					.equals(Configed.getResourceValue("Dashboard.choiceBoxChoice.all"))) {
				return true;
			}

			return (client.isConnectedWithMessagebus() && clientActivityStatusComboBox.getValue()
					.equals(Configed.getResourceValue("Dashboard.client.active")))
					|| (!client.isConnectedWithMessagebus() && clientActivityStatusComboBox.getValue()
							.equals(Configed.getResourceValue("Dashboard.client.inactive")));
		}, clientActivityStatusComboBox.valueProperty()));

		final FilteredList<Client> filteredData = new FilteredList<>(FXCollections.observableArrayList(clients));

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

	private boolean checkIfClientLastSeen(Client client) {
		if (clientLastSeenComboBox.getValue() == null || clientLastSeenComboBox.getValue()
				.equals(Configed.getResourceValue("Dashboard.choiceBoxChoice.all"))) {
			return true;
		}

		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		final LocalDate current = LocalDate.now();
		final LocalDate lastSeenDate;

		if (client.getLastSeen().equals(Configed.getResourceValue("Dashboard.lastSeen.never"))) {
			lastSeenDate = LocalDate.parse("9999-12-31", dtf);
		} else {
			lastSeenDate = LocalDate.parse(client.getLastSeen().substring(0, 10), dtf);
		}

		final long days = ChronoUnit.DAYS.between(lastSeenDate, current);
		String value = clientLastSeenComboBox.getValue();
		String clientLastSeen = client.getLastSeen();

		return isLastSeen(days, value, clientLastSeen);
	}

	private static boolean isLastSeen(long days, String value, String clientLastSeen) {
		if (value.equals(Configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")) && days >= 0
				&& days <= 14) {
			return true;
		}

		if (value.equals(Configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays")) && days > 14
				&& days <= 30) {
			return true;
		}

		if (value.equals(Configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")) && days > 30) {
			return true;
		}

		return value.equals(Configed.getResourceValue("Dashboard.lastSeen.never"))
				&& clientLastSeen.equals(Configed.getResourceValue("Dashboard.lastSeen.never"));
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
		clientNumberTitleText.setStyle("-fx-fill: " + foregroundColor);
		activeClientNumberTitleText.setStyle("-fx-fill: " + foregroundColor);
		inactiveClientNumberTitleText.setStyle("-fx-fill: " + foregroundColor);
		fourteenOrLowerDaysNumberTitleText.setStyle("-fx-fill: " + foregroundColor);
		betweenFifteenAndThirtyDaysNumberTitleText.setStyle("-fx-fill: " + foregroundColor);
		moreThanThirtyDaysNumberTitleText.setStyle("-fx-fill: " + foregroundColor);

		clientsNumberLabel.setStyle("-fx-text-fill: " + foregroundColor);
		activeClientsNumberLabel.setStyle("-fx-text-fill: " + foregroundColor);
		inactiveClientsNumberLabel.setStyle("-fx-text-fill: " + foregroundColor);
		fourteenOrLowerDaysNumberLabel.setStyle("-fx-text-fill: " + foregroundColor);
		betweenFifteenAndThirtyDaysNumberLabel.setStyle("-fx-text-fill: " + foregroundColor);
		moreThanThirtyDaysNumberLabel.setStyle("-fx-text-fill: " + foregroundColor);

		backButton.setStyle("-fx-text-fill: " + foregroundColor);
		Color iconColor = UIManager.getColor("Label.foreground");
		backButtonIcon
				.setFill(javafx.scene.paint.Color.rgb(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue()));

		String lighterBackgroundColor = ComponentStyler
				.getHexColor(Helper.adjustColorBrightness(UIManager.getColor("Panel.background")));
		String backgroundColor = ComponentStyler.getHexColor(UIManager.getColor("Panel.background"));
		fxPanel.setBackground(UIManager.getColor("Panel.background"));
		clientViewAnchorPane.setStyle("-fx-background-color: " + backgroundColor);
		clientTableArea.setStyle("-fx-background-color: " + lighterBackgroundColor);
		clientChartArea.setStyle("-fx-background-color: " + lighterBackgroundColor);

		String labelForegroundColor = ComponentStyler.getHexColor(UIManager.getColor("Label.foreground"));
		clientActivityLabel.setStyle("-fx-text-fill: " + labelForegroundColor);
		clientLastSeenLabel.setStyle("-fx-text-fill: " + labelForegroundColor);

		clientNumberArea.setStyle("-fx-background-color: " + lighterBackgroundColor);
		activeClientNumberArea.setStyle("-fx-background-color: " + lighterBackgroundColor);
		inactiveClientNumberArea.setStyle("-fx-background-color: " + lighterBackgroundColor);
		fourteenOrLowerDaysNumberArea.setStyle("-fx-background-color: " + lighterBackgroundColor);
		betweenFifteenAndThirtyDaysNumberArea.setStyle("-fx-background-color: " + lighterBackgroundColor);
		moreThanThirtyDaysNumberArea.setStyle("-fx-background-color: " + lighterBackgroundColor);

		ComponentStyler.styleTableViewComponent(clientTableView);
		ComponentStyler.styleTextFieldComponent(clientSearchbarTextField);
		ComponentStyler.styleComboBoxComponent(clientLastSeenComboBox);
		ComponentStyler.styleComboBoxComponent(clientActivityStatusComboBox);
	}
}
