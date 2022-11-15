package de.uib.configed.dashboard.view;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.time.format.*;
import java.time.temporal.*;

import javafx.application.Platform;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.embed.swing.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;

import de.uib.configed.*;
import de.uib.configed.dashboard.*;
import de.uib.configed.dashboard.chart.*;
import de.uib.configed.dashboard.collector.*;
import de.uib.messages.*;

public class ClientView implements View
{
	@FXML private Label clientsNumberLabel;
	@FXML private Label activeClientsNumberLabel;
	@FXML private Label inactiveClientsNumberLabel;
	@FXML private Label fourteenOrLowerDaysNumberLabel;
	@FXML private Label betweenFifteenAndThirtyDaysNumberLabel;
	@FXML private Label moreThanThirtyDaysNumberLabel;
	@FXML private ChoiceBox<String> clientActivityStatusChoiceBox;
	@FXML private CheckBox clientActivityStatusCheckBox;
	@FXML private ChoiceBox<String> clientLastSeenChoiceBox;
	@FXML private CheckBox clientLastSeenCheckBox;
	@FXML private ListView<String> clientListView;
	@FXML private TextField clientSearchbarTextField;
	@FXML private TableView<Client> clientTableView;
	@FXML private TableColumn<Client, String> hostnameTableColumn;
	@FXML private TableColumn<Client, String> lastSeenTableColumn;
	@FXML private TableColumn<Client, Boolean> clientActiveTableColumn;
	@FXML private ClientActivityComparison clientActivityComparison;
	@FXML private ClientLastSeenComparison clientLastSeenComparison;
	@FXML private Button backButton;

	private JFXPanel fxPanel;
	private Scene scene;

	public ClientView(JFXPanel fxPanel) throws IOException
	{
		FXMLLoader fxmlLoader = new FXMLLoader(ClientView.class.getResource("/fxml/views/client_view.fxml"), Messages.getResource());
		fxmlLoader.setController(this);

		Parent root = fxmlLoader.load();
		this.scene = new Scene(root);
		this.fxPanel = fxPanel;
	}

	private void loadData()
	{
		List<Client> clients = ClientData.getClients();
		List<String> activeClients = ClientData.getActiveClients();
		List<String> inactiveClients = ClientData.getInactiveClients();
		Map<String, Integer> lastSeenData = ClientData.getLastSeenData();

		clientsNumberLabel.setText(String.valueOf(clients.size()));
		activeClientsNumberLabel.setText(String.valueOf(activeClients.size()));
		inactiveClientsNumberLabel.setText(String.valueOf(inactiveClients.size()));
		fourteenOrLowerDaysNumberLabel.setText(String.valueOf(lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"))));
		betweenFifteenAndThirtyDaysNumberLabel.setText(String.valueOf(lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"))));
		moreThanThirtyDaysNumberLabel.setText(String.valueOf(lastSeenData.get(configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"))));

		hostnameTableColumn.setCellValueFactory(cellData -> cellData.getValue().hostnameProperty());
		lastSeenTableColumn.setCellValueFactory(cellData -> cellData.getValue().lastSeenProperty());
		clientActiveTableColumn.setCellValueFactory(cellData -> cellData.getValue().reachableProperty());
		clientActiveTableColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

		List<String> clientStatus = new ArrayList<>();
		clientStatus.add(configed.getResourceValue("Dashboard.client.active"));
		clientStatus.add(configed.getResourceValue("Dashboard.client.inactive"));
		final ObservableList<String> status = new FilteredList<>(FXCollections.observableArrayList(clientStatus));
		clientActivityStatusChoiceBox.setItems(status);

		List<String> clientLastSeenData = new ArrayList<>();
		clientLastSeenData.add(configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays"));
		clientLastSeenData.add(configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays"));
		clientLastSeenData.add(configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays"));
		clientLastSeenData.add(configed.getResourceValue("Dashboard.lastSeen.never"));

		final ObservableList<String> lastSeen = new FilteredList<>(FXCollections.observableArrayList(clientLastSeenData));
		clientLastSeenChoiceBox.setItems(lastSeen);

		final FilteredList<Client> filteredData = new FilteredList<>(FXCollections.observableArrayList(clients));

		final ObjectProperty<Predicate<Client>> hostnameFilter = new SimpleObjectProperty<>();
		final ObjectProperty<Predicate<Client>> activeFilter = new SimpleObjectProperty<>();
		final ObjectProperty<Predicate<Client>> lastSeenFilter = new SimpleObjectProperty<>();

		hostnameFilter.bind(Bindings.createObjectBinding(() ->
			client ->
			{
				if (clientSearchbarTextField.getText() == null) return true;
				return client.getHostname().toLowerCase(Locale.ROOT).contains(clientSearchbarTextField.getText().toLowerCase(Locale.ROOT));
			},
			clientSearchbarTextField.textProperty()));
		lastSeenFilter.bind(Bindings.createObjectBinding(() ->
			client ->
			{
				if (clientLastSeenChoiceBox.getValue() == null) return true;

				final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				final LocalDate current = LocalDate.now();
				final LocalDate lastSeenDate = client.getLastSeen().equals(configed.getResourceValue("Dashboard.lastSeen.never")) ?
					                           LocalDate.parse("9999-12-31", dtf) :
					                           LocalDate.parse(client.getLastSeen().substring(0, 10), dtf);
				final long days = ChronoUnit.DAYS.between(lastSeenDate, current);

				return clientLastSeenChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.lastSeen.fourteenOrLowerDays")) && days <= 14 && days >= 0 ||
					   clientLastSeenChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.lastSeen.betweenFifteenAndThirtyDays")) && days > 14  && days <= 30||
					   clientLastSeenChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.lastSeen.moreThanThirtyDays")) && days > 30 ||
					   clientLastSeenChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.lastSeen.never")) && client.getLastSeen().equals(configed.getResourceValue("Dashboard.lastSeen.never"));
			},
			clientLastSeenChoiceBox.valueProperty()));
		activeFilter.bind(Bindings.createObjectBinding(() ->
			client ->
			{
				if (clientActivityStatusChoiceBox.getValue() == null) return true;
				return client.getReachable() && clientActivityStatusChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.client.active")) ||
					   !client.getReachable() && clientActivityStatusChoiceBox.getValue().equals(configed.getResourceValue("Dashboard.client.inactive"));
			},
			clientActivityStatusChoiceBox.valueProperty()));

		filteredData.predicateProperty().bind(Bindings.createObjectBinding(() ->
			hostnameFilter.get().and(lastSeenFilter.get().and(activeFilter.get())), hostnameFilter, lastSeenFilter, activeFilter));

		SortedList<Client> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(clientTableView.comparatorProperty());
		clientTableView.setItems(sortedData);

		backButton.setOnAction(e -> Platform.runLater(() -> ViewManager.displayView(Dashboard.MAIN_VIEW)));
		clientActivityStatusCheckBox.setOnAction(e -> changeClientActivityStatusState());
		clientLastSeenCheckBox.setOnAction(e -> changeClientLastSeenState());

		clientActivityComparison.display();
		clientLastSeenComparison.display();
	}

	@Override
	public void display()
	{
		Platform.runLater(() -> fxPanel.setScene(scene));
		loadData();
	}

	private void changeClientActivityStatusState()
	{
		if (clientActivityStatusCheckBox.isSelected())
		{
			clientActivityStatusChoiceBox.setDisable(false);
		}
		else
		{
			clientActivityStatusChoiceBox.getSelectionModel().clearSelection();
			clientActivityStatusChoiceBox.setDisable(true);
		}
	}

	private void changeClientLastSeenState()
	{
		if (clientLastSeenCheckBox.isSelected())
		{
			clientLastSeenChoiceBox.setDisable(false);
		}
		else
		{
			clientLastSeenChoiceBox.getSelectionModel().clearSelection();
			clientLastSeenChoiceBox.setDisable(true);
		}
	}
}
