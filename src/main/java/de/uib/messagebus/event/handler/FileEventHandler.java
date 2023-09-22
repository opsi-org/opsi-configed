/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus.event.handler;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import de.uib.configed.terminal.Terminal;
import de.uib.messagebus.Messagebus;
import de.uib.messagebus.event.WebSocketEvent;

public class FileEventHandler implements EventHandler {
	Map<String, Consumer<Map<String, Object>>> eventHandlers = new HashMap<>();

	public FileEventHandler(Messagebus messagebus) {
		eventHandlers.put(WebSocketEvent.FILE_UPLOAD_RESULT.toString(),
				eventData -> onFileUploadResult(eventData, messagebus));
	}

	@Override
	public void handle(String event, Map<String, Object> eventData) {
		eventHandlers.get(event).accept(eventData);
	}

	private static void onFileUploadResult(Map<String, Object> eventData, Messagebus messagebus) {
		Map<String, Object> message = new HashMap<>();
		message.put("type", "terminal_data_write");
		message.put("id", UUID.randomUUID().toString());
		message.put("sender", "@");
		message.put("channel", Terminal.getInstance().getTerminalChannel());
		message.put("created", System.currentTimeMillis());
		message.put("expires", System.currentTimeMillis() + 10000);
		message.put("terminal_id", Terminal.getInstance().getTerminalId());
		message.put("data", ((String) eventData.get("path")).getBytes(StandardCharsets.UTF_8));
		messagebus.sendMessage(message);
	}
}
