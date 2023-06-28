/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.utilities.logging.Logging;

@SuppressWarnings("java:S109")
public class WebSocketClientEndpoint extends WebSocketClient {

	private List<MessagebusListener> listeners = new ArrayList<>();

	public WebSocketClientEndpoint(URI serverURI) {
		super(serverURI);
	}

	public void registerListener(MessagebusListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void unregisterListener(MessagebusListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	@Override
	public boolean isOpen() {
		WebSocket con = getConnection();
		if (con == null) {
			return false;
		}
		return con.isOpen();
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		Logging.debug(this, "Websocket opened");

		for (MessagebusListener listener : listeners) {
			listener.onOpen(handshakeData);
		}
	}

	@Override
	public void onMessage(String data) {
		// We receive message in bytes rather than Strings. Therefore, there is
		// nothing to handle in this method.
	}

	@Override
	public void onMessage(ByteBuffer data) {
		Logging.debug(this, "Websocket received message");
		ObjectMapper mapper = new MessagePackMapper();
		try {
			Map<String, Object> message = mapper.readValue(data.array(), new TypeReference<Map<String, Object>>() {
			});
			long expires = (long) message.get("expires");
			Date now = new Date();
			if (now.getTime() >= expires) {
				Logging.info("Expired message received");
				return;
			}

			for (MessagebusListener listener : listeners) {
				listener.onMessageReceived(message);
			}
		} catch (IOException e) {
			Logging.error(this, "cannot read received message: ", e);
		}
	}

	@SuppressWarnings("java:S1774")
	@Override
	public void onClose(int code, String reason, boolean remote) {
		// The close codes are documented in class org.java_websocket.framing.CloseFrame
		Logging.debug(this, "Websocket closed by " + (remote ? "opsi service" : "us") + " Code=" + code + " Reason='"
				+ reason + "'");

		for (MessagebusListener listener : listeners) {
			listener.onClose(code, reason, remote);
		}
	}

	@Override
	public void onError(Exception ex) {
		Logging.debug(this, "Websocket error: " + ex);
		for (MessagebusListener listener : listeners) {
			listener.onError(ex);
		}
	}
}
