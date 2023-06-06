/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.util.HashSet;
import java.util.Set;

public final class ConnectionErrorObserver {
	private static ConnectionErrorObserver instance;
	private Set<ConnectionErrorListener> listeners = new HashSet<>();

	private ConnectionErrorObserver() {
	}

	public static ConnectionErrorObserver getInstance() {
		if (instance == null) {
			instance = new ConnectionErrorObserver();
		}

		return instance;
	}

	public static void destroy() {
		instance = null;
	}

	public void subscribe(ConnectionErrorListener listener) {
		listeners.add(listener);
	}

	public void unsubscribe(ConnectionErrorListener listener) {
		listeners.remove(listener);
	}

	public void notify(String message, ConnectionErrorType errorType) {
		listeners.forEach(listener -> listener.onError(message, errorType));
	}
}
