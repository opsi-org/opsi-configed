/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus.event.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.uib.configed.terminal.Terminal;
import de.uib.configed.terminal.WebSocketInputStream;
import de.uib.messagebus.event.WebSocketEvent;
import de.uib.utilities.logging.Logging;

public class TerminalEventHandler implements EventHandler {
	Map<String, Consumer<Map<String, Object>>> eventHandlers = new HashMap<>();

	public TerminalEventHandler() {
		eventHandlers.put(WebSocketEvent.TERMINAL_DATA_READ.toString(), TerminalEventHandler::onTerminalDataRead);
		eventHandlers.put(WebSocketEvent.TERMINAL_OPEN_EVENT.toString(), TerminalEventHandler::onTerminalOpenEvent);
		eventHandlers.put(WebSocketEvent.TERMINAL_CLOSE_EVENT.toString(), eventData -> Terminal.getInstance().close());
	}

	@Override
	public void handle(String event, Map<String, Object> eventData) {
		eventHandlers.get(event).accept(eventData);
	}

	private static void onTerminalDataRead(Map<String, Object> eventData) {
		try {
			WebSocketInputStream.write((byte[]) eventData.get("data"));
		} catch (IOException e) {
			Logging.error("failed to write message: ", e);
		}
	}

	private static void onTerminalOpenEvent(Map<String, Object> data) {
		Terminal terminal = Terminal.getInstance();
		terminal.setTerminalId((String) data.get("terminal_id"));
		terminal.setTerminalChannel((String) data.get("back_channel"));
		terminal.unlock();
	}
}
