/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload;

import java.util.HashMap;
import java.util.Map;

import de.uib.opsidatamodel.serverdata.reload.handler.ReloadHandler;
import de.uib.utils.logging.Logging;

/**
 * Provides a way to trigger specific {@link ReloadHandler} implementation for
 * an event, without requiring to know specific implementations.
 * <p>
 * {@link ReloadDispatcher} is based on event bus design pattern.
 */
public class ReloadDispatcher {
	private Map<String, ReloadHandler> handlers = new HashMap<>();

	public void registerHandler(String event, ReloadHandler handler) {
		handlers.put(event, handler);
	}

	public void dispatch(String event) {
		if (handlers.containsKey(event)) {
			handlers.get(event).handle(event);
		} else {
			Logging.warning(this, "No reload handler is available for ", event, " reload event");
		}
	}
}
