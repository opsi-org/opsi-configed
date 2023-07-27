/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Product implements Comparable<Product> {
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
		return clients.get() != null ? clients.get() : new ArrayList<>();
	}

	public final void setClients(List<String> value) {
		clients.set(value);
	}

	public ObjectProperty<List<String>> clientsProperty() {
		return clients;
	}

	@Override
	public int compareTo(Product o) {
		return getId().compareTo(o.getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Product otherProduct = (Product) o;
		return Objects.equals(getId(), otherProduct.getId()) && Objects.equals(getDepot(), otherProduct.getDepot())
				&& Objects.equals(getStatus(), otherProduct.getStatus())
				&& Objects.equals(getClients(), otherProduct.getClients());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getDepot(), getStatus(), getClients());
	}
}
