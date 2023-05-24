/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.collector;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Product {
	private final StringProperty id = new SimpleStringProperty();
	private final StringProperty depot = new SimpleStringProperty();
	private final StringProperty status = new SimpleStringProperty();
	private final ObjectProperty<List<String>> clients = new SimpleObjectProperty<>();

	public final String getId() {
		return id.get();
	}

	public final void setId(String value) {
		id.set(value);
	}

	public StringProperty idProperty() {
		return id;
	}

	public final String getDepot() {
		return depot.get();
	}

	public final void setDepot(String value) {
		depot.set(value);
	}

	public StringProperty depotProperty() {
		return depot;
	}

	public final String getStatus() {
		return status.get();
	}

	public final void setStatus(String value) {
		status.set(value);
	}

	public StringProperty statusProperty() {
		return status;
	}

	public final List<String> getClients() {
		return clients.get();
	}

	public final void setClients(List<String> value) {
		clients.set(value);
	}

	public ObjectProperty<List<String>> clientsProperty() {
		return clients;
	}
}
