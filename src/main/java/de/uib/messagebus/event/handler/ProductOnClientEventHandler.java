/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus.event.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.uib.configed.ConfigedMain;

public class ProductOnClientEventHandler implements EventHandler {
	Map<String, Consumer<Map<String, Object>>> eventHandlers = new HashMap<>();

	public ProductOnClientEventHandler(ConfigedMain configedMain) {
		eventHandlers.put("productOnClient_created", configedMain::updateProduct);
		eventHandlers.put("productOnClient_updated", configedMain::updateProduct);
		eventHandlers.put("productOnClient_deleted", configedMain::updateProduct);
	}

	@Override
	public void handle(String event, Map<String, Object> eventData) {
		eventHandlers.get(event).accept(eventData);
	}
}
