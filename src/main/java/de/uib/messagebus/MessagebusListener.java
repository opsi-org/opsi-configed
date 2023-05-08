package de.uib.messagebus;

import java.util.Map;

import org.java_websocket.handshake.ServerHandshake;

public interface MessagebusListener {

	void onOpen(ServerHandshake handshakeData);

	void onClose(int code, String reason, boolean remote);

	void onError(Exception ex);

	void onMessageReceived(Map<String, Object> message);
}
