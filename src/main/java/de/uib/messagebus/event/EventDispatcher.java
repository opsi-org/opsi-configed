/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus.event;

import java.util.HashMap;
import java.util.Map;

import de.uib.messagebus.event.handler.EventHandler;
import de.uib.utilities.logging.Logging;

public class EventDispatcher {
	private Map<String, EventHandler> handlers = new HashMap<>();

	public void registerHandler(String event, EventHandler handler) {
		handlers.put(event, handler);
	}

	public void dispatch(String event, Map<String, Object> eventData) {
		if (handlers.containsKey(event)) {
			handlers.get(event).handle(event, eventData);
		} else {
			Logging.warning(this, "No handler is available for " + event + " event");
		}
	}
}
