package de.uib.configed.dashboard.collector;

import de.uib.configed.configed;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Client {
	private StringProperty hostname = new SimpleStringProperty();
	private StringProperty lastSeen = new SimpleStringProperty();
	private BooleanProperty reachable = new SimpleBooleanProperty();

	public final String getHostname() {
		return hostname.get();
	}

	public final void setHostname(String value) {
		hostname.set(value);
	}

	public StringProperty hostnameProperty() {
		return hostname;
	}

	public final String getLastSeen() {
		return lastSeen.get().isEmpty() ? configed.getResourceValue("Dashboard.lastSeen.never") : lastSeen.get();
	}

	public final void setLastSeen(String value) {
		lastSeen.setValue(value);
	}

	public StringProperty lastSeenProperty() {
		if (lastSeen.get().isEmpty()) {
			lastSeen.set(configed.getResourceValue("Dashboard.lastSeen.never"));
		}

		return lastSeen;
	}

	public final boolean getReachable() {
		return reachable.get();
	}

	public final void setReachable(boolean value) {
		reachable.setValue(value);
	}

	public BooleanProperty reachableProperty() {
		return reachable;
	}
}
