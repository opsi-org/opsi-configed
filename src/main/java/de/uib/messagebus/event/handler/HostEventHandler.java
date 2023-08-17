/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus.event.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.ConfigedMain;

public class HostEventHandler implements EventHandler {
	Map<String, Consumer<Map<String, Object>>> eventHandlers = new HashMap<>();

	public HostEventHandler(ConfigedMain configedMain) {
		eventHandlers.put("host_connected", eventData -> configedMain.addClientToConnectedList(getHostId(eventData)));
		eventHandlers.put("host_disconnected",
				eventData -> configedMain.removeClientFromConnectedList(getHostId(eventData)));
		eventHandlers.put("host_created", eventData -> configedMain.addClientToTable((String) eventData.get("id")));
		eventHandlers.put("host_deleted",
				eventData -> configedMain.removeClientFromTable((String) eventData.get("id")));
	}

	@Override
	public void handle(String event, Map<String, Object> eventData) {
		eventHandlers.get(event).accept(eventData);
	}

	private static String getHostId(Map<String, Object> eventData) {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
		};
		Map<String, Object> hostData = objectMapper.convertValue(eventData.get("host"), typeRef);
		return (String) hostData.get("id");
	}
}
