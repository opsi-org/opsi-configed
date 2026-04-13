/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;

public class DataObserver {
	private final Map<String, List<DataChangeListener>> listeners = new HashMap<>();

	public DataObserver(String... operations) {
		for (String operation : operations) {
			this.listeners.put(operation, new ArrayList<>());
		}
	}

	public void subscribe(String type, DataChangeListener listener) {
		List<DataChangeListener> subscribers = listeners.get(type);
		subscribers.add(listener);
	}

	public void notify(String type, String depot) {
		List<DataChangeListener> subscribers = listeners.get(type);
		for (DataChangeListener subscriber : subscribers) {
			subscriber.update(depot);
			Platform.runLater(subscriber::display);
		}
	}
}
