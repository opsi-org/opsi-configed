/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uib.configed.core.domain.serverdata.reload.handler.ReloadHandler;
import de.uib.configed.share.logging.Logging;

/**
 * Provides a way to trigger specific {@link ReloadHandler} implementation for
 * an event, without requiring to know specific implementations.
 * <p>
 * {@link ReloadDispatcher} is based on event bus design pattern.
 */
public class ReloadDispatcher {
	private Map<String, ReloadHandler> handlers = new HashMap<>();
	private Set<String> activeEvents = new HashSet<>();

	public void registerHandler(String event, ReloadHandler handler) {
		handlers.put(event, handler);
	}

	public void dispatch(String event) {
		if (activeEvents.contains(event)) {
			Logging.warning(this, "Recursive dispatch detected for event: ", event,
					". Skipping to prevent stack overflow.");
			return;
		}
		if (handlers.containsKey(event)) {
			try {
				activeEvents.add(event);
				handlers.get(event).handle(event);
			} finally {
				activeEvents.remove(event);
			}
		} else {
			Logging.warning(this, "No reload handler is available for ", event, " reload event");
		}
	}
}
